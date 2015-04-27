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

import com.google.common.collect.ImmutableSet;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OWLAPI3TranslatorUtility;
import it.unibz.krdb.obda.owlapi3.QuestOWLIndividualIterator;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.OWLAPI3Materializer;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.io.WriterDocumentTarget;
import org.semanticweb.owlapi.model.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

public class QuestOWLMaterializerCMD {

    private static final int TRIPLE_LIMIT_PER_FILE = 500000;
    private static String owlFile;
	private static String obdaFile;
	private static String format;
	private static String outputFile;
    private static boolean separate = false;

	public static void main(String args[]) {

		if (!parseArgs(args)) {
			printUsage();
			System.exit(1);
		}

        if (!separate){
            run();
        } else {
            runWithSeparateFiles();
        }
	}

    private static void runWithSeparateFiles() {


        if (owlFile == null) {
            throw new NullPointerException("You have to specify an ontology file!");
        }
        // Loading the OWL ontology from the file as with normal OWLReasoners
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = null;
        OBDADataFactory obdaDataFactory =  OBDADataFactoryImpl.getInstance();
        try {
            ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));

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

            Ontology inputOntology = OWLAPI3TranslatorUtility.translate(ontology);
            OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
            OBDAModel obdaModel = fac.getOBDAModel();
            obdaModel.declareAll(inputOntology.getVocabulary());
            ModelIOManager ioManager = new ModelIOManager(obdaModel);
            ioManager.load(obdaFile);

            // TESTING !!!!
            // predicates = ImmutableSet.of(obdaDataFactory.getClassPredicate("http://www.opengis.net/ont/sf#Geometry"));
            //predicates = ImmutableSet.of(obdaDataFactory.getClassPredicate("http://sws.ifi.uio.no/vocab/npd-v2#Award"));
            //predicates = ImmutableSet.of(obdaDataFactory.getClassPredicate("http://www.opengis.net/ont/gml#ArcByBulge"));
            int numPredicates = predicates.size();

            int i = 1;
            for (Predicate predicate : predicates) {
                System.err.println(String.format("Materializing %s (%d/%d)", predicate, i, numPredicates));
                serializePredicate(ontology, inputOntology, obdaModel, predicate);
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
    private static void serializePredicate(OWLOntology ontology, Ontology inputOntology, OBDAModel obdaModel, Predicate predicate) throws Exception {
        final long startTime = System.currentTimeMillis();

        OWLAPI3Materializer materializer = new OWLAPI3Materializer(obdaModel, inputOntology, predicate);
        QuestOWLIndividualIterator iterator = materializer.getIterator();


        int tripleCount = 0;
        int fileCount = 0;

        String outputDir = outputFile;
        String filePrefix = Paths.get(outputDir, predicate.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_").toString();

        while(iterator.hasNext()) {
            tripleCount += serializeTripleBatch(ontology, iterator, filePrefix, predicate.getName(), fileCount);
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
    private static int serializeTripleBatch(OWLOntology ontology, QuestOWLIndividualIterator iterator, String filePrefix, String predicateName, int fileCount) throws Exception {
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

    private static void run() {
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


            OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
            OBDAModel obdaModel = fac.getOBDAModel();
            ModelIOManager ioManager = new ModelIOManager(obdaModel);
            ioManager.load(obdaFile);

            OWLOntology ontology = null;
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLAPI3Materializer materializer = null;

            if (owlFile != null) {
            // Loading the OWL ontology from the file as with normal OWLReasoners
                ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));
                Ontology onto =  OWLAPI3TranslatorUtility.translate(ontology);
                obdaModel.declareAll(onto.getVocabulary());
                materializer = new OWLAPI3Materializer(obdaModel, onto);
            }
            else {
                ontology = manager.createOntology();
                materializer = new OWLAPI3Materializer(obdaModel);
            }


            // OBDAModelSynchronizer.declarePredicates(ontology, obdaModel);


            QuestOWLIndividualIterator iterator = materializer.getIterator();

            while(iterator.hasNext())
                manager.addAxiom(ontology, iterator.next());


            OWLOntologyFormat ontologyFormat = getOntologyFormat(format);

            manager.saveOntology(ontology, ontologyFormat, new WriterDocumentTarget(writer));

            System.out.println("NR of TRIPLES: " + materializer.getTriplesCount());
            System.out.println("VOCABULARY SIZE (NR of QUERIES): " + materializer.getVocabularySize());

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

    private static OWLOntologyFormat getOntologyFormat(String format) throws Exception {
		OWLOntologyFormat ontoFormat;
		
		if(format == null){
			ontoFormat = new RDFXMLOntologyFormat();
		}
		else {
		switch (format) {
			case "rdfxml":
				ontoFormat = new RDFXMLOntologyFormat();
				break;
			case "owlxml":
				ontoFormat = new OWLXMLOntologyFormat();
				break;
			case "turtle":
				ontoFormat = new TurtleOntologyFormat();
				break;
			default:
				throw new Exception("Unknown format: " + format);
			}
		}
		return ontoFormat;
	}

	private static void printUsage() {
		System.out.println("Usage");
		System.out.println(" QuestOWLMaterializerCMD -obda mapping.obda [-onto ontology.owl] [-format format] [-out outputFile] [--enable-reasoning | --disable-reasoning]");
		System.out.println("");
		System.out.println(" -obda mapping.obda    The full path to the OBDA file");
		System.out.println(" -onto ontology.owl    [OPTIONAL] The full path to the OWL file");
		System.out.println(" -format format        [OPTIONAL] The format of the materialized ontology: ");
		System.out.println("                          Options: rdfxml, owlxml, turtle. Default: rdfxml");
		System.out.println(" -out outputFile       [OPTIONAL] The full path to the output file. If not specified, the output will be stdout");
		System.out.println(" --enable-reasoning    [OPTIONAL] enable the OWL reasoning (default)");
		System.out.println(" --disable-reasoning   [OPTIONAL] disable the OWL reasoning (not implemented yet) ");
        System.out.println(" --separate-files      [OPTIONAL] generating separate files for different classes/properties. ");
        System.out.println("                          In this case, `-out` will be used as the output directory");
		System.out.println("");
	}


    public static boolean parseArgs(String[] args) {
        int i = 0;
        while (i < args.length) {
            switch (args[i]) {
                case "-obda":
                    obdaFile = args[i + 1];
                    i += 2;
                    break;
                case "-onto":
                    owlFile = args[i + 1];
                    i += 2;
                    break;
                case "-format":
                    format = args[i + 1];
                    i += 2;
                    break;
                case "-out":
                    outputFile = args[i + 1];
                    i += 2;
                    break;
                case "--separate-files":
                    separate = true;
                    i += 1;
                    break;
                default:
                    System.err.println("Unknown option " + args[i]);
                    System.err.println();
                    return false;
            }
        }

        if (obdaFile == null) {
            System.err.println("Please specify the ontology file\n");
            return false;
        }

        return true;
	}

}
