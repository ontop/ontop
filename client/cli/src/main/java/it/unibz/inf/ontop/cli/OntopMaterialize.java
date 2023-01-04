package it.unibz.inf.ontop.cli;


import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.restrictions.AllowedEnumValues;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration.Builder;
import it.unibz.inf.ontop.injection.impl.OntopModelConfigurationImpl;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.rdf4j.materialization.RDF4JMaterializer;
import org.apache.commons.rdf.api.IRI;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.rio.jsonld.JSONLDWriter;
import org.eclipse.rdf4j.rio.nquads.NQuadsWriter;
import org.eclipse.rdf4j.rio.ntriples.NTriplesWriter;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.eclipse.rdf4j.rio.trig.TriGWriter;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static it.unibz.inf.ontop.injection.OntopSQLCoreSettings.JDBC_URL;
import static it.unibz.inf.ontop.injection.OntopSQLCredentialSettings.JDBC_PASSWORD;
import static it.unibz.inf.ontop.injection.OntopSQLCredentialSettings.JDBC_USER;
import static it.unibz.inf.ontop.injection.OntopSystemSQLSettings.FETCH_SIZE;
import static org.apache.commons.io.FilenameUtils.removeExtension;

@Command(name = "materialize",
        description = "Materialize the RDF graph exposed by the mapping and the OWL ontology")
public class OntopMaterialize extends OntopMappingOntologyRelatedCommand {

    private enum PredicateType {
        CLASS("C"),
        PROPERTY("P");

        private final String code;

        PredicateType(String code) {
            this.code = code;
        }

        String getCode() {
            return code;
        }
    }


    private static final int TRIPLE_LIMIT_PER_FILE = 500000;
    private static final String DEFAULT_FETCH_SIZE = "50000";

    public enum OutputFormat {
        rdfxml(".rdf", RDFXMLWriter::new),
        turtle(".ttl", w ->  new TurtleWriter(w).set(BasicWriterSettings.PRETTY_PRINT, false)),
        ntriples(".nt", w ->  new NTriplesWriter(w).set(BasicWriterSettings.PRETTY_PRINT, false)),
        nquads(".nq", w ->  new NQuadsWriter(w).set(BasicWriterSettings.PRETTY_PRINT, false)),
        trig(".trig", TriGWriter::new),
        jsonld(".jsonld", JSONLDWriter::new);

        private final String extension;
        private final Function<BufferedWriter, RDFHandler> rdfHandlerProvider;

        OutputFormat(String extension, Function<BufferedWriter, RDFHandler> rdfHandlerProvider) {
            this.extension = extension;
            this.rdfHandlerProvider = rdfHandlerProvider;
        }

        String getExtension() {
            return extension;
        }

        RDFHandler createRDFHandler(BufferedWriter writer) {
            return rdfHandlerProvider.apply(writer);
        }
    }
    
    @Option(type = OptionType.COMMAND, override = true, name = {"-o", "--output"},
            title = "output", description = "output file (default) or prefix (only for --separate-files)")
    //@BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    private String outputFile;

    @Option(type = OptionType.COMMAND, name = {"-f", "--format"}, title = "outputFormat",
            description = "The format of the materialized ontology. " +
                    //" Options: rdfxml, turtle. " +
                    "Default: rdfxml")
    @AllowedEnumValues(OutputFormat.class)
    public OutputFormat format = OutputFormat.rdfxml;

    @Option(type = OptionType.COMMAND, name = {"--separate-files"}, title = "output to separate files",
            description = "generating separate files for different classes/properties. This is useful for" +
                    " materializing large OBDA setting. Default: false.")
    public boolean separate = false;

    @Option(type = OptionType.COMMAND, name = {"--no-streaming"}, title = "do not execute streaming of results",
            description = "All the SQL results of one big query will be stored in memory. Not recommended. Default: false.")
    private boolean noStream = false;

    public OntopMaterialize() {
    }

    @Override
    public void run() {

        RDF4JMaterializer materializer = createMaterializer();

        if (separate) {
            runWithSeparateFiles(materializer);
        } else {
            runWithSingleFile(materializer);
        }
    }

    private RDF4JMaterializer createMaterializer() {
        try {
            Builder<? extends Builder<?>> configurationBuilder = createAndInitConfigurationBuilder();

            return RDF4JMaterializer.defaultMaterializer(
                    configurationBuilder.build(),
                    MaterializationParams.defaultBuilder()
                            .build()
            );
        } catch (OBDASpecificationException e) {
            throw new RuntimeException(e);
        }
    }

    private void runWithSingleFile(RDF4JMaterializer materializer) {
        int tripleCount = 0;

        final long startTime = System.currentTimeMillis();

        GraphQueryResult result = materializer.materialize().evaluate();

        try {
            BufferedWriter writer = createWriter(Optional.empty());
            tripleCount += serializeTripleBatch(
                    result,
                    Optional.empty(),
                    writer,
                    format.createRDFHandler(writer)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("NR of TRIPLES: " + tripleCount);

        final long endTime = System.currentTimeMillis();
        final long time = endTime - startTime;
        System.out.println("Elapsed time to materialize: " + time + " {ms}");
    }

    private void runWithSeparateFiles(RDF4JMaterializer materializer) {
        try {
            materializeClassesByFile(materializer);
            materializePropertiesByFile(materializer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void materializeClassesByFile(RDF4JMaterializer materializer) throws Exception {
        ImmutableSet<IRI> classes = materializer.getClasses();
        int total = classes.size();
        AtomicInteger i = new AtomicInteger();
        for (IRI c : classes) {
            serializePredicate(materializer, c, PredicateType.CLASS, i.incrementAndGet(), total);
        }
    }

    private void materializePropertiesByFile(RDF4JMaterializer materializer) throws Exception {
        ImmutableSet<IRI> properties = materializer.getProperties();

        int total = properties.size();
        AtomicInteger i = new AtomicInteger();
        for (IRI p : properties) {
            serializePredicate(materializer, p, PredicateType.PROPERTY, i.incrementAndGet(), total);
        }
    }

    /**
     * Serializes the A-box corresponding to a predicate into one or multiple file.
     */
    private void serializePredicate(RDF4JMaterializer materializer, IRI predicateIRI,
                                    PredicateType predicateType, int index, int total) throws Exception {
        final long startTime = System.currentTimeMillis();


        System.err.println(String.format("Materializing %s (%d/%d)", predicateIRI, index, total));
        System.err.println("Starts writing triples into files.");

        int tripleCount = 0;
        int fileCount = 0;

        String fileSubstring = predicateIRI.toString().replaceAll("[^a-zA-Z0-9]", "_")
                + predicateType.getCode() + "_";

        GraphQueryResult result = materializer.materialize(ImmutableSet.of(predicateIRI)).evaluate();

        while (result.hasNext()) {
            BufferedWriter writer = createWriter(Optional.of(fileSubstring + fileCount));
            tripleCount += serializeTripleBatch(
                    result,
                    Optional.of(TRIPLE_LIMIT_PER_FILE),
                    writer,
                    format.createRDFHandler(writer)
            );
            fileCount++;
        }

        System.out.println("NR of TRIPLES: " + tripleCount);

        final long endTime = System.currentTimeMillis();
        final long time = endTime - startTime;
        System.out.println("Elapsed time to materialize: " + time + " {ms}");
    }

    // We need direct access to the writer to close it (cannot be done via the RDFHandler)
    private BufferedWriter createWriter(Optional<String> prefixExtension) throws IOException {
        if (outputFile != null) {
            String prefix = removeExtension(outputFile);
            String suffix = format.getExtension();
            return Files.newBufferedWriter(
                    prefixExtension
                            .map(s -> Paths.get(prefix, s + suffix))
                            .orElseGet(() -> Paths.get(prefix + suffix)),
                    StandardCharsets.UTF_8
            );
        }
        return new BufferedWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8));
    }

    /**
     * Serializes a batch of triples into one file.
     * Upper bound: TRIPLE_LIMIT_PER_FILE.
     */
    private int serializeTripleBatch(GraphQueryResult result, Optional<Integer> limitPerFile, BufferedWriter writer, RDFHandler handler) throws IOException {
        int tripleCount = 0;
        handler.startRDF();
        while (result.hasNext() && (!limitPerFile.isPresent() || tripleCount < limitPerFile.get())) {
            handler.handleStatement(result.next());
            tripleCount++;
        }
        handler.endRDF();
        writer.close();
        return tripleCount;
    }

    /**
     * Mapping file + connection info
     */
    private Builder<? extends Builder<?>> createAndInitConfigurationBuilder() {

        final Builder<? extends Builder<?>> configBuilder = OntopSQLOWLAPIConfiguration.defaultBuilder();

        if (owlFile != null)
            configBuilder.ontologyFile(owlFile);

        if (isR2rmlFile(mappingFile)) {
            configBuilder.r2rmlMappingFile(mappingFile);
        } else {
            configBuilder.nativeOntopMappingFile(mappingFile);
        }

        if (dbMetadataFile != null)
            configBuilder.dbMetadataFile(dbMetadataFile);

        if (ontopLensesFile != null)
            configBuilder.lensesFile(ontopLensesFile);

        if (sparqlRulesFile != null)
            configBuilder.sparqlRulesFile(sparqlRulesFile);

        Properties properties = OntopModelConfigurationImpl.extractProperties(
                OntopModelConfigurationImpl.extractPropertyFile(propertiesFile));


        @Nullable
        String userFetchSizeStr = properties.getProperty(FETCH_SIZE);

        if (userFetchSizeStr != null) {
            try {
                int userFetchSize = Integer.parseInt(userFetchSizeStr);
                if (noStream && userFetchSize > 0)
                    throw new InvalidOntopConfigurationException("Do not provide a positive " + FETCH_SIZE
                            + " together with no streaming option");
                else if ((!noStream) && userFetchSize <= 0) {
                    throw new InvalidOntopConfigurationException("Do not provide a non-positive " + FETCH_SIZE
                            + " together with the streaming option");
                }
            } catch (NumberFormatException e ) {
                throw new InvalidOntopConfigurationException(FETCH_SIZE + " was expected an integer");
            }
        }
        /*
         * Set the default FETCH_SIZE for materializer
         */
        else if (!noStream)
            properties.setProperty(FETCH_SIZE, DEFAULT_FETCH_SIZE);
        else
            properties.setProperty(FETCH_SIZE, "-1");

        if (dbPassword != null)
            properties.setProperty(JDBC_PASSWORD, dbPassword);

        if (dbUrl != null)
            properties.setProperty(JDBC_URL, dbUrl);

        if (dbUser != null)
            properties.setProperty(JDBC_USER, dbUser);

        configBuilder
                .properties(properties)
                .enableOntologyAnnotationQuerying(true);

        return configBuilder;
    }

}
