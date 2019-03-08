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
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration.Builder;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.materialization.OntopRDFMaterializer;
import it.unibz.inf.ontop.owlapi.OntopOWLAPIMaterializer;
import it.unibz.inf.ontop.owlapi.resultset.MaterializedGraphOWLResultSet;
import it.unibz.inf.ontop.rdf4j.materialization.RDF4JMaterializer;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import org.apache.commons.rdf.api.IRI;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.n3.N3Writer;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.*;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

//import org.semanticweb.owlapi.apibinding.OWLManager;
//import org.semanticweb.owlapi.io.WriterDocumentTarget;
//import org.semanticweb.owlapi.model.*;


@Command(name = "materialize",
        description = "Materialize the RDF graph exposed by the mapping and the OWL ontology")
public class OntopMaterialize extends OntopReasoningCommandBase {

    private enum PredicateType {
        CLASS("C"),
        PROPERTY("P");

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

    public OntopMaterialize() {
    }

    @Override
    public void run() {

        //   Streaming it's necessary to materialize large RDF graphs without
        //   storing all the SQL results of one big query in memory.
        if (noStream) {
            doStreamResults = false;
        }
        RDF4JMaterializer materializer = getMaterializer();
        if (separate) {
            runWithSeparateFiles(materializer);
        } else {
            runWithSingleFile(materializer);
        }
    }

    private RDF4JMaterializer getMaterializer() {

        RDF4JMaterializer materializer = null;
        try {
            OWLOntology ontology = loadOntology();
            OntopSQLOWLAPIConfiguration materializerConfiguration = createAndInitConfigurationBuilder()
                    .ontology(ontology)
                    .build();
            materializer = RDF4JMaterializer.defaultMaterializer(
                    materializerConfiguration,
                    MaterializationParams.defaultBuilder()
                            .enableDBResultsStreaming(doStreamResults)
                            .build()
            );
        } catch (OBDASpecificationException e) {
            e.printStackTrace();
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        return materializer;
    }

    private OWLOntology loadOntology() throws OWLOntologyCreationException {
        if (owlFile != null) {
            OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File(owlFile));
            if (disableReasoning) {
                return extractDeclarations(ontology.getOWLOntologyManager(), ontology);
            }
            return ontology;
        }
        return OWLManager.createOWLOntologyManager().createOntology();
    }

    private void runWithSeparateFiles(RDF4JMaterializer materializer) {


//        if (owlFile == null) {
//            throw new NullPointerException("You have to specify an ontology file!");
//        }
//        try {
//
//            OntopSQLOWLAPIConfiguration configuration = createAndInitConfigurationBuilder()
//                    .ontologyFile(owlFile)
//                    .build();
//
//            // Loads it only once
//            SQLPPMapping ppMapping = configuration.loadProvidedPPMapping();
//            OntopSQLOWLAPIConfiguration materializationConfig = OntopSQLOWLAPIConfiguration.defaultBuilder()
//                    .propertyFile(propertiesFile)
//                    // To avoid parsing it again and again
//                    .ppMapping(ppMapping)
//                    .build();
//
//            OntopOWLAPIMaterializer materializer = OntopOWLAPIMaterializer.defaultMaterializer(
//                    materializationConfig,
//                    MaterializationParams.defaultBuilder()
//                            .enableDBResultsStreaming(doStreamResults)
//                            .build()
//            );
        try {
            materializeClassesByFile(materializer, format, outputFile);
            materializePropertiesByFile(materializer, format, outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        } catch (OWLOntologyCreationException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void materializeClassesByFile(RDF4JMaterializer materializer, String format, String outputFile) throws Exception {
        ImmutableSet<IRI> classes = materializer.getClasses();
        int total = classes.size();
        AtomicInteger i = new AtomicInteger();
        for (IRI c : classes) {
            serializePredicate(materializer, c, PredicateType.CLASS, outputFile, format, i.incrementAndGet(), total);
        }
    }

    private void materializePropertiesByFile(RDF4JMaterializer materializer, String format, String outputFile) throws Exception {
        ImmutableSet<IRI> properties = materializer.getProperties();
        int total = properties.size();
        AtomicInteger i = new AtomicInteger();
        for (IRI p : properties) {
            serializePredicate(materializer, p, PredicateType.PROPERTY, outputFile, format, i.incrementAndGet(), total);
        }
    }

    /**
     * Serializes the A-box corresponding to a predicate into one or multiple file.
     */
    private void serializePredicate(RDF4JMaterializer materializer, IRI predicateIRI,
                                    PredicateType predicateType, String outputFile, String format,
                                    int index, int total) throws Exception {
        final long startTime = System.currentTimeMillis();


        System.err.println(String.format("Materializing %s (%d/%d)", predicateIRI, index, total));
        System.err.println("Starts writing triples into files.");

        int tripleCount = 0;
        int fileCount = 0;

        String outputDir = outputFile;

        String filePrefix = Paths.get(outputDir, predicateIRI.toString().replaceAll("[^a-zA-Z0-9]", "_")
                + predicateType.getCode() + "_").toString();

        GraphQueryResult result = materializer.materialize(ImmutableSet.of(predicateIRI)).evaluate();

        while (result.hasNext()) {
            tripleCount += serializeTripleBatch(result, filePrefix, fileCount, format, Optional.of(TRIPLE_LIMIT_PER_FILE));
            fileCount++;
        }

        System.out.println("NR of TRIPLES: " + tripleCount);

        final long endTime = System.currentTimeMillis();
        final long time = endTime - startTime;
        System.out.println("Elapsed time to materialize: " + time + " {ms}");
    }

    /**
     * Serializes a batch of triples corresponding to a predicate into one file.
     * Upper bound: TRIPLE_LIMIT_PER_FILE.
     */
    private int serializeTripleBatch(GraphQueryResult result, String filePrefix, int fileCount, String format, Optional<Integer> limitPerFile) throws Exception {
        String suffix;
        Function<Writer, RDFHandler> handlerConstructor;

        switch (format) {
            case RDF_XML:
                suffix = ".rdf";
                handlerConstructor = RDFXMLWriter::new;
                break;
//            case OWL_XML:
//                suffix = ".owl";
//                break;
            case TURTLE:
                suffix = ".ttl";
                handlerConstructor = TurtleWriter::new;
                break;
            case N3:
                suffix = ".n3";
                handlerConstructor = N3Writer::new;
                break;
            default:
                throw new Exception("Unknown format: " + format);
        }
        String fileName = filePrefix + fileCount + suffix;


//            String outfile = out.getAbsolutePath();
//            System.out.println(outfile);

//            MaterializationParams materializationParams = MaterializationParams.defaultBuilder()
//                    .enableDBResultsStreaming(DO_STREAM_RESULTS)
//                    .build();

//            RDF4JMaterializer materializer = RDF4JMaterializer.defaultMaterializer(
//                    configuration,
//                    materializationParams
//            );
//            MaterializationGraphQuery graphQuery = materializer.materialize();
        int tripleCount = 0;

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
        RDFHandler handler = handlerConstructor.apply(writer);
        handler.startRDF();
        while (result.hasNext() && (!limitPerFile.isPresent() || tripleCount < limitPerFile.get()))
            handler.handleStatement(result.next());
        handler.endRDF();

//        } finally {
        if (writer != null)
            writer.close();
//        }
//        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//
//        // Main buffer
//        OWLOntology aBox = manager.createOntology(IRI.create(predicateName));
//
//        int tripleCount = 0;
//        while (graphQuery.hasNext() && (tripleCount < TRIPLE_LIMIT_PER_FILE )) {
//            manager.addAxiom(aBox, graphQuery.next());
//            tripleCount++;
//        }
//
//        //BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(outputPath.toFile()));
//        //BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
//        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
//        manager.saveOntology(aBox, getDocumentFormat(format), new WriterDocumentTarget(writer));

        return tripleCount;
    }


    public void runWithSingleFile(RDF4JMaterializer materializer) {
        BufferedOutputStream output = null;
        BufferedWriter writer = null;

        int tripleCount = 0;

        final long startTime = System.currentTimeMillis();
        try {
            if (outputFile != null) {
                output = new BufferedOutputStream(new FileOutputStream(outputFile));
            } else {
                output = new BufferedOutputStream(System.out);
            }
            writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));

            GraphQueryResult result = materializer.materialize().evaluate();

            tripleCount += serializeTripleBatch(result, outputFile, 1, format, Optional.of(TRIPLE_LIMIT_PER_FILE));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        System.out.println("NR of TRIPLES: " + tripleCount);

        final long endTime = System.currentTimeMillis();
        final long time = endTime - startTime;
        System.out.println("Elapsed time to materialize: " + time + " {ms}");

//            Builder configBuilder = createAndInitConfigurationBuilder();
//            if (owlFile != null) {
//                configBuilder.ontologyFile(owlFile);
//            }
//
//            OntopSQLOWLAPIConfiguration initialConfiguration = configBuilder.build();
            //OBDAModel obdaModel = initialConfiguration.loadSpecification();

//            OWLOntology ontology;
//            OntopSQLOWLAPIConfiguration materializerConfiguration;

//            if (owlFile != null) {
//                // Loading the OWL ontology from the file as with normal OWLReasoners
////                OWLOntology initialOntology = initialConfiguration.loadProvidedInputOntology();
//
//                if (disableReasoning) {
//                    ontology = extractDeclarations(initialOntology.getOWLOntologyManager(), initialOntology);
//                } else {
//                    ontology = initialOntology;
//                }
//                materializerConfiguration = createAndInitConfigurationBuilder()
//                        .ontology(ontology)
//                        .build();
//
//            } else {
//                ontology = OWLManager.createOWLOntologyManager().createOntology();
//                materializerConfiguration = createAndInitConfigurationBuilder()
//                        .ontology(ontology)
//                        .build();
//            }
//
//            OntopOWLAPIMaterializer materializer = OntopOWLAPIMaterializer.defaultMaterializer(
//                    materializerConfiguration,
//                    MaterializationParams.defaultBuilder()
//                            .enableDBResultsStreaming(doStreamResults)
//                            .build()
//            );

            // OBDAModelSynchronizer.declarePredicates(ontology, obdaModel);

//
//        System.out.println("NR of TRIPLES: " + tripleCount);
//
//        final long endTime = System.currentTimeMillis();
//        final long time = endTime - startTime;
//        System.out.println("Elapsed time to materialize: " + time + " {ms}");
//
//            OWLOntologyManager manager = ontology.getOWLOntologyManager();
//            try (MaterializedGraphOWLResultSet graphResults = materializer.materialize()) {
//
//                while (graphResults.hasNext())
//                    manager.addAxiom(ontology, graphResults.next());
//
//                OWLDocumentFormat DocumentFormat = getDocumentFormat(format);
//
//                manager.saveOntology(ontology, DocumentFormat, new WriterDocumentTarget(writer));
//
//                System.err.println("NR of TRIPLES: " + graphResults.getTripleCountSoFar());
//                System.err.println("VOCABULARY SIZE (NR of QUERIES): " + graphResults.getSelectedVocabulary().size());
//            }
//            if (outputFile != null)
//                output.close();
//
//            final long endTime = System.currentTimeMillis();
//            final long time = endTime - startTime;
//            System.out.println("Elapsed time to materialize: " + time + " {ms}");
//
//        } catch (Exception e) {
//            System.out.println("Error materializing ontology:");
//            e.printStackTrace();
//        }
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
        } else {
            configBuilder.nativeOntopMappingFile(mappingFile);
        }

        return configBuilder
                .propertyFile(propertiesFile)
                .enableOntologyAnnotationQuerying(true);
    }
}
