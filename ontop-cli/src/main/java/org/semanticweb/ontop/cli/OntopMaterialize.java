package org.semanticweb.ontop.cli;

/*
 * #%L
 * ontop-cli
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.rvesse.airline.Command;
import com.github.rvesse.airline.Option;
import com.github.rvesse.airline.OptionType;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OWLAPI3TranslatorUtility;
import it.unibz.krdb.obda.owlapi3.QuestOWLIndividualAxiomIterator;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.OWLAPI3Materializer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.WriterDocumentTarget;
import org.semanticweb.owlapi.model.*;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

@Command(name = "materialize",
        description = "Materialize the RDF graph exposed by the mapping and the OWL ontology")
public class OntopMaterialize extends OntopReasoningCommandBase {

    private static final int TRIPLE_LIMIT_PER_FILE = 500000;

    @Option(type = OptionType.COMMAND, name = {"-f", "--format"}, title = "outputFormat",
            allowedValues = {"rdfxml", "owlxml", "turtle"},
            description = "The format of the materialized ontology. " +
                    //" Options: rdfxml, owlxml, turtle. " +
                    "Default: rdfxml")
    public String format;

    @Option(type = OptionType.COMMAND, name = {"--separate-files"}, title = "output to separate files",
            description = "generating separate files for different classes/properties. This is useful for" +
                    " materializing large OBDA setting. Default: false.")
    public boolean separate = false;


    /**
     * Necessary for materialize large RDF graphs without
     * storing all the SQL results of one big query in memory.
     *
     * TODO: add an option to disable it.
     */
    private static boolean DO_STREAM_RESULTS = true;

	public static void main(String... args) {

	}

    public OntopMaterialize(){}

    @Override
    public void run(){
        if(separate) {
            runWithSeparateFiles();
        } else {
            runWithSingleFile();
        }
    }

    private void runWithSeparateFiles() {
        if (owlFile == null) {
            throw new NullPointerException("You have to specify an ontology file!");
        }

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = null;
        OBDADataFactory obdaDataFactory =  OBDADataFactoryImpl.getInstance();
        try {
            ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));

            if (disableReasoning) {
                /*
                 * when reasoning is disabled, we extract only the declaration assertions for the vocabulary
                 */
                ontology = extractDeclarations(manager, ontology);
            }

            Collection<Predicate> predicates = new ArrayList<>();

            for (OWLClass owlClass : ontology.getClassesInSignature()) {
                Predicate predicate = obdaDataFactory.getClassPredicate(owlClass.getIRI().toString());
                predicates.add(predicate);
            }
            for (OWLDataProperty owlDataProperty : ontology.getDataPropertiesInSignature()) {
                Predicate predicate = obdaDataFactory.getDataPropertyPredicate(owlDataProperty.getIRI().toString());
                predicates.add(predicate);
            }
            for(OWLObjectProperty owlObjectProperty: ontology.getObjectPropertiesInSignature()){
                Predicate predicate = obdaDataFactory.getObjectPropertyPredicate(owlObjectProperty.getIRI().toString());
                predicates.add(predicate);
            }


            OBDAModel obdaModel = loadMappingFile(mappingFile);

            Ontology inputOntology = OWLAPI3TranslatorUtility.translate(ontology);

            obdaModel.declareAll(inputOntology.getVocabulary());


            int numPredicates = predicates.size();

            int i = 1;
            for (Predicate predicate : predicates) {
                System.err.println(String.format("Materializing %s (%d/%d)", predicate, i, numPredicates));
                serializePredicate(ontology, inputOntology, obdaModel, predicate, outputFile, format);
                i++;
            }

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Serializes the A-box corresponding to a predicate into one or multiple file.
     */
    private static void serializePredicate(OWLOntology ontology, Ontology inputOntology, OBDAModel obdaModel,
                                           Predicate predicate, String outputFile, String format) throws Exception {
        final long startTime = System.currentTimeMillis();

        OWLAPI3Materializer materializer = new OWLAPI3Materializer(obdaModel, inputOntology, predicate, DO_STREAM_RESULTS);
        QuestOWLIndividualAxiomIterator iterator = materializer.getIterator();

        System.err.println("Starts writing triples into files.");

        int tripleCount = 0;
        int fileCount = 0;

        String outputDir = outputFile;
        String filePrefix = Paths.get(outputDir, predicate.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_").toString();

        while(iterator.hasNext()) {
            tripleCount += serializeTripleBatch(ontology, iterator, filePrefix, predicate.getName(), fileCount, format);
            fileCount++;
        }

        System.out.println("NR of TRIPLES: " + tripleCount);
        System.out.println("VOCABULARY SIZE (NR of QUERIES): " + materializer.getVocabularySize());

        final long endTime = System.currentTimeMillis();
        final long time = endTime - startTime;
        System.out.println("Elapsed time to materialize: " + time + " {ms}");
    }

    /**
     * Serializes a batch of triples corresponding to a predicate into one file.
     * Upper bound: TRIPLE_LIMIT_PER_FILE.
     *
     */
    private static int serializeTripleBatch(OWLOntology ontology, QuestOWLIndividualAxiomIterator iterator,
                                            String filePrefix, String predicateName, int fileCount, String format) throws Exception {
        String fileName = filePrefix + fileCount + ".owl";

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        // Main buffer
        OWLOntology aBox = manager.createOntology(IRI.create(predicateName));

        // Add the signatures
        for (OWLDeclarationAxiom axiom : ontology.getAxioms(AxiomType.DECLARATION)) {
            manager.addAxiom(aBox, axiom);
        }

        int tripleCount = 0;
        while (iterator.hasNext() && (tripleCount < TRIPLE_LIMIT_PER_FILE )) {
            manager.addAxiom(aBox, iterator.next());
            tripleCount++;
        }

        //BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(outputPath.toFile()));
        //BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        manager.saveOntology(aBox, getOntologyFormat(format), new WriterDocumentTarget(writer));

        return tripleCount;
    }


    public void runWithSingleFile() {
        BufferedOutputStream output = null;
        BufferedWriter writer = null;

        try {
            final long startTime = System.currentTimeMillis();

            if (outputFile != null) {
                output = new BufferedOutputStream(new FileOutputStream(outputFile));
            } else {
                output = new BufferedOutputStream(System.out);
            }
            writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));

            OBDAModel obdaModel = loadMappingFile(mappingFile);

            OWLOntology ontology = null;
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLAPI3Materializer materializer = null;

            if (owlFile != null) {
            // Loading the OWL ontology from the file as with normal OWLReasoners
                ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));

                if (disableReasoning) {
                /*
                 * when reasoning is disabled, we extract only the declaration assertions for the vocabulary
                 */
                    ontology = extractDeclarations(manager, ontology);
                }

                Ontology onto =  OWLAPI3TranslatorUtility.translate(ontology);
                obdaModel.declareAll(onto.getVocabulary());
                materializer = new OWLAPI3Materializer(obdaModel, onto, DO_STREAM_RESULTS);
            } else {
                ontology = manager.createOntology();
                materializer = new OWLAPI3Materializer(obdaModel, DO_STREAM_RESULTS);
            }


            // OBDAModelSynchronizer.declarePredicates(ontology, obdaModel);


            QuestOWLIndividualAxiomIterator iterator = materializer.getIterator();

            while(iterator.hasNext())
                manager.addAxiom(ontology, iterator.next());


            OWLOntologyFormat ontologyFormat = getOntologyFormat(format);

            manager.saveOntology(ontology, ontologyFormat, new WriterDocumentTarget(writer));

            System.err.println("NR of TRIPLES: " + materializer.getTriplesCount());
            System.err.println("VOCABULARY SIZE (NR of QUERIES): " + materializer.getVocabularySize());

            materializer.disconnect();
            if (outputFile!=null)
                output.close();

            final long endTime = System.currentTimeMillis();
            final long time = endTime - startTime;
            System.out.println("Elapsed time to materialize: "+time + " {ms}");

        } catch (Exception e) {
            System.out.println("Error materializing ontology:");
            e.printStackTrace();
        }
    }

}
