package it.unibz.inf.ontop.cli;

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


import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.restrictions.AllowedValues;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration.Builder;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.owlapi.OntopOWLAPIMaterializer;
import it.unibz.inf.ontop.owlapi.resultset.MaterializedGraphOWLResultSet;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.WriterDocumentTarget;
import org.semanticweb.owlapi.model.*;

import java.io.*;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;


@Command(name = "materialize",
        description = "Materialize the RDF graph exposed by the mapping and the OWL ontology")
public class OntopMaterialize extends OntopReasoningCommandBase {

    private enum PredicateType {
        CLASS("C"),
        DATA_PROPERTY("DP"),
        OBJECT_PROPERTY("OP"),
        ANNOTATION_PROPERTY("AP");

        private final String code;

        PredicateType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    private static final int TRIPLE_LIMIT_PER_FILE = 500000;
    private static final String RDF_XML = "rdfxml";
    private static final String OWL_XML = "owlxml";
    private static final String TURTLE = "turtle";
    private static final String N3 = "n3";


    @Option(type = OptionType.COMMAND, override = true, name = {"-o", "--output"},
            title = "output", description = "output file (default) or directory (only for --separate-files)")
    //@BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    protected String outputFile;

    @Option(type = OptionType.COMMAND, name = {"-f", "--format"}, title = "outputFormat",
            description = "The format of the materialized ontology. " +
                    //" Options: rdfxml, owlxml, turtle, n3. " +
                    "Default: rdfxml")
    @AllowedValues(allowedValues = {RDF_XML, OWL_XML, TURTLE, N3})
    public String format = RDF_XML;

    @Option(type = OptionType.COMMAND, name = {"--separate-files"}, title = "output to separate files",
            description = "generating separate files for different classes/properties. This is useful for" +
                    " materializing large OBDA setting. Default: false.")
    public boolean separate = false;

    @Option(type = OptionType.COMMAND, name = {"--no-streaming"}, title = "do not execute streaming of results",
            description = "All the SQL results of one big query will be stored in memory. Not recommended. Default: false.")
    private boolean noStream = false;

    private boolean doStreamResults = true;

    public OntopMaterialize(){}

    @Override
    public void run(){

        //   Streaming it's necessary to materialize large RDF graphs without
        //   storing all the SQL results of one big query in memory.
        if (noStream){
            doStreamResults = false;
        }
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
        try {

            OntopSQLOWLAPIConfiguration configuration = createAndInitConfigurationBuilder()
                    .ontologyFile(owlFile)
                    .build();

            OWLOntology ontology = configuration.loadProvidedInputOntology();

            if (disableReasoning) {
                /*
                 * when reasoning is disabled, we extract only the declaration assertions for the vocabulary
                 */
                ontology = extractDeclarations(ontology.getOWLOntologyManager(), ontology);
            }

//            ImmutableMap<IRI, PredicateType> predicateMap = extractPredicates(ontology);

            // Loads it only once
            SQLPPMapping ppMapping = configuration.loadProvidedPPMapping();
            OntopSQLOWLAPIConfiguration materializationConfig = OntopSQLOWLAPIConfiguration.defaultBuilder()
                    .propertyFile(propertiesFile)
                    // To avoid parsing it again and again
                    .ppMapping(ppMapping)
                    .build();

            OntopOWLAPIMaterializer materializer = OntopOWLAPIMaterializer.defaultMaterializer(
                    materializationConfig,
                    MaterializationParams.defaultBuilder()
                            .enableDBResultsStreaming(doStreamResults)
                            .build()
            );
            materializeClassesByFile(materializer, format, ontology, outputFile);

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void materializeClassesByFile(OntopOWLAPIMaterializer materializer, String format, OWLOntology ontology, String outputFile) {
        ImmutableSet<IRI> classes = materializer.getClasses();
        int total = classes.size();
        AtomicInteger i = new AtomicInteger();
        classes.forEach(c -> {
            try {
                serializePredicate(materializer, c, PredicateType.CLASS, outputFile, format, ontology, i.incrementAndGet(), total);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

//    private ImmutableMap<IRI, PredicateType> extractPredicates(OWLOntology ontology) {
//        ImmutableMap.Builder<IRI, PredicateType> predicateMapBuilder = ImmutableMap.builder();
//
//        for (OWLClass owlClass : ontology.getClassesInSignature()) {
//            predicateMapBuilder.put(owlClass.getIRI(), PredicateType.CLASS);
//        }
//        for (OWLDataProperty owlDataProperty : ontology.getDataPropertiesInSignature()) {
//            predicateMapBuilder.put(owlDataProperty.getIRI(), PredicateType.DATA_PROPERTY);
//        }
//        for(OWLObjectProperty owlObjectProperty: ontology.getObjectPropertiesInSignature()){
//            predicateMapBuilder.put(owlObjectProperty.getIRI(), PredicateType.OBJECT_PROPERTY);
//        }
//        for (OWLAnnotationProperty owlAnnotationProperty : ontology.getAnnotationPropertiesInSignature()) {
//            predicateMapBuilder.put(owlAnnotationProperty.getIRI(), PredicateType.ANNOTATION_PROPERTY);
//        }
//        return predicateMapBuilder.build();
//    }

    /**
     * Serializes the A-box corresponding to a predicate into one or multiple file.
     */
    private void serializePredicate(OntopOWLAPIMaterializer materializer, IRI predicateIRI,
                                    PredicateType predicateType, String outputFile, String format,
                                    OWLOntology ontology, int index, int total) throws Exception {
        final long startTime = System.currentTimeMillis();


        System.err.println(String.format("Materializing %s (%d/%d)", predicateIRI, index, total));
        System.err.println("Starts writing triples into files.");

        int tripleCount = 0;
        int fileCount = 0;

        String outputDir = outputFile;

        String filePrefix = Paths.get(outputDir, predicateIRI.toString().replaceAll("[^a-zA-Z0-9]", "_")
                + predicateType.getCode() +"_" ).toString();
        MaterializationParams materializationParams = MaterializationParams.defaultBuilder()
                .enableDBResultsStreaming(doStreamResults)
                .build();

        try (MaterializedGraphOWLResultSet graphResultSet = materializer.materialize(ImmutableSet.of(predicateIRI))) {

            while (graphResultSet.hasNext()) {
                tripleCount += serializeTripleBatch(ontology, graphResultSet, filePrefix, predicateIRI.toString(), fileCount, format);
                fileCount++;
            }

            System.out.println("NR of TRIPLES: " + tripleCount);
            System.out.println("VOCABULARY SIZE (NR of QUERIES): " + graphResultSet.getSelectedVocabulary().size());
        }

        final long endTime = System.currentTimeMillis();
        final long time = endTime - startTime;
        System.out.println("Elapsed time to materialize: " + time + " {ms}");
    }

    /**
     * Serializes a batch of triples corresponding to a predicate into one file.
     * Upper bound: TRIPLE_LIMIT_PER_FILE.
     *
     */
    private int serializeTripleBatch(OWLOntology ontology, MaterializedGraphOWLResultSet iterator,
                                     String filePrefix, String predicateName, int fileCount, String format) throws Exception {
        String suffix;

        switch (format) {
            case RDF_XML:
                suffix = ".rdf";
                break;
            case OWL_XML:
                suffix = ".owl";
                break;
            case TURTLE:
                suffix = ".ttl";
                break;
            case N3:
                suffix = ".n3";
                break;
            default:
                throw new OntopMaterializeException("Unknown format: " + format);
        }
        String fileName = filePrefix + fileCount + suffix;

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
        manager.saveOntology(aBox, getDocumentFormat(format), new WriterDocumentTarget(writer));

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

            Builder configBuilder = createAndInitConfigurationBuilder();

            if (owlFile != null) {
                configBuilder.ontologyFile(owlFile);
            }

            OntopSQLOWLAPIConfiguration initialConfiguration = configBuilder.build();
            //OBDAModel obdaModel = initialConfiguration.loadSpecification();

            OWLOntology ontology;
            OntopSQLOWLAPIConfiguration materializerConfiguration;

            if (owlFile != null) {
            // Loading the OWL ontology from the file as with normal OWLReasoners
                OWLOntology initialOntology = initialConfiguration.loadProvidedInputOntology();

                if (disableReasoning) {
                    ontology = extractDeclarations(initialOntology.getOWLOntologyManager(), initialOntology);
                }
                else {
                    ontology = initialOntology;
                }
                materializerConfiguration = createAndInitConfigurationBuilder()
                        .ontology(ontology)
                        .build();

            } else {
                ontology = OWLManager.createOWLOntologyManager().createOntology();
                materializerConfiguration = createAndInitConfigurationBuilder()
                        .ontology(ontology)
                        .build();
            }

            OntopOWLAPIMaterializer materializer = OntopOWLAPIMaterializer.defaultMaterializer(
                    materializerConfiguration,
                    MaterializationParams.defaultBuilder()
                            .enableDBResultsStreaming(doStreamResults)
                            .build()
            );

            // OBDAModelSynchronizer.declarePredicates(ontology, obdaModel);

            OWLOntologyManager manager = ontology.getOWLOntologyManager();
            try (MaterializedGraphOWLResultSet graphResults = materializer.materialize()) {

                while (graphResults.hasNext())
                    manager.addAxiom(ontology, graphResults.next());

                OWLDocumentFormat DocumentFormat = getDocumentFormat(format);

                manager.saveOntology(ontology, DocumentFormat, new WriterDocumentTarget(writer));

                System.err.println("NR of TRIPLES: " + graphResults.getTripleCountSoFar());
                System.err.println("VOCABULARY SIZE (NR of QUERIES): " + graphResults.getSelectedVocabulary().size());
            }
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


    /**
     * Mapping file + connection info
     */
    private Builder<? extends Builder> createAndInitConfigurationBuilder() {

        final Builder<? extends Builder> configBuilder = OntopSQLOWLAPIConfiguration.defaultBuilder();

//        if (!Strings.isNullOrEmpty(owlFile)){
//            configBuilder.ontologyFile(owlFile);
//        }

        if (isR2rmlFile(mappingFile)) {
            configBuilder.r2rmlMappingFile(mappingFile);
        }
        else {
            configBuilder.nativeOntopMappingFile(mappingFile);
        }

        return configBuilder
                .propertyFile(propertiesFile)
                .enableOntologyAnnotationQuerying(true);
    }

    private class OntopMaterializeException extends OntopInternalBugException {
        OntopMaterializeException(String message) {
            super(message);
        }
    }
}
