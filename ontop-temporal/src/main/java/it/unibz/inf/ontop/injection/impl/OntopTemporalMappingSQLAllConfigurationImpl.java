package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Module;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllSettings;
import it.unibz.inf.ontop.injection.OntopTemporalMappingSQLAllConfiguration;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.OBDASpecificationExtractor;
import it.unibz.inf.ontop.spec.TOBDASpecInput;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import org.apache.commons.rdf.api.Graph;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class OntopTemporalMappingSQLAllConfigurationImpl extends OntopMappingSQLAllConfigurationImpl
        implements OntopTemporalMappingSQLAllConfiguration {

    private final OntopMappingSQLAllSettings settings;
    private final OntopTemporalMappingSQLAllOptions options;

    OntopTemporalMappingSQLAllConfigurationImpl(OntopMappingSQLAllSettings settings,
                                                OntopTemporalMappingSQLAllOptions options) {
        super(settings, options.mappingSQLOptions);
        this.settings = settings;
        this.options = options;
    }

    /**
     * To be overloaded
     *
     */
    @Override
    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(super.buildGuiceModules(),
                Stream.of(new OntopTemporalModule(this)));
    }

    @Override
    public OntopMappingSQLAllSettings getSettings() {
        return settings;
    }

    @Override
    public OBDASpecification loadOBDASpecification() throws OBDASpecificationException {
        return loadSpecification(Optional::empty);
    }

    OBDASpecification loadSpecification(OntologySupplier ontologySupplier)
            throws OBDASpecificationException {

        return loadTemporalSpecification(ontologySupplier,
                () -> options.temporalMappingFile,
                () -> options.mappingFile,
                () -> options.mappingReader,
                () -> options.mappingGraph,
                () -> options.constraintFile);
    }

    OBDASpecification loadTemporalSpecification(OntologySupplier ontologySupplier,
                                        Supplier<Optional<File>> temporalMappingFileSupplier,
                                        Supplier<Optional<File>> mappingFileSupplier,
                                        Supplier<Optional<Reader>> mappingReaderSupplier,
                                        Supplier<Optional<Graph>> mappingGraphSupplier,
                                        Supplier<Optional<File>> constraintFileSupplier)
            throws OBDASpecificationException {
        return loadSpecification(
                ontologySupplier,
                () -> options.mappingSQLOptions.mappingSQLOptions.ppMapping.map(m -> (PreProcessedMapping) m),
                temporalMappingFileSupplier,
                mappingFileSupplier,
                mappingReaderSupplier,
                mappingGraphSupplier,
                constraintFileSupplier
        );
    }

    OBDASpecification loadSpecification(OntologySupplier ontologySupplier,
                                        Supplier<Optional<PreProcessedMapping>> ppMappingSupplier,
                                        Supplier<Optional<File>> temporalMappingFileSupplier,
                                        Supplier<Optional<File>> mappingFileSupplier,
                                        Supplier<Optional<Reader>> mappingReaderSupplier,
                                        Supplier<Optional<Graph>> mappingGraphSupplier,
                                        Supplier<Optional<File>> constraintFileSupplier
    ) throws OBDASpecificationException {
        OBDASpecificationExtractor extractor = getInjector().getInstance(OBDASpecificationExtractor.class);

        Optional<Ontology> optionalOntology = ontologySupplier.get();
        Optional<DBMetadata> optionalMetadata = options.mappingSQLOptions.mappingSQLOptions.mappingOptions.dbMetadata;

        /*
         * Pre-processed mapping
         */
        Optional<PreProcessedMapping> optionalPPMapping = ppMappingSupplier.get();

        TOBDASpecInput.Builder specInputBuilder = TOBDASpecInput.defaultBuilder();
        constraintFileSupplier.get()
                .ifPresent(specInputBuilder::addConstraintFile);

        if (optionalPPMapping.isPresent()) {
            PreProcessedMapping ppMapping = optionalPPMapping.get();

            return extractor.extract(specInputBuilder.build(), ppMapping, optionalMetadata, optionalOntology,
                    getExecutorRegistry());
        }

        /*
         * Mapping file
         */
        Optional<File> optionalMappingFile = mappingFileSupplier.get();

        if (optionalMappingFile.isPresent()) {
            specInputBuilder.addMappingFile(optionalMappingFile.get());
        }

        Optional<File> optionalTemporalMappingFile = temporalMappingFileSupplier.get();
        if (optionalTemporalMappingFile.isPresent()) {
            specInputBuilder.addTemporalMappingFile(optionalTemporalMappingFile.get());
        }

        if (optionalTemporalMappingFile.isPresent() || optionalMappingFile.isPresent()) {
            return extractor.extract(specInputBuilder.build(), optionalMetadata, optionalOntology,
                    getExecutorRegistry());
        }

        /*
         * Reader
         */
        Optional<Reader> optionalMappingReader = mappingReaderSupplier.get();
        if (optionalMappingReader.isPresent()) {
            specInputBuilder.addMappingReader(optionalMappingReader.get());

            return extractor.extract(specInputBuilder.build(), optionalMetadata, optionalOntology,
                    getExecutorRegistry());
        }

        /*
         * Graph
         */
        Optional<Graph> optionalMappingGraph = mappingGraphSupplier.get();
        if (optionalMappingGraph.isPresent()) {
            specInputBuilder.addMappingGraph(optionalMappingGraph.get());

            return extractor.extract(specInputBuilder.build(), optionalMetadata, optionalOntology,
                    getExecutorRegistry());
        }

        throw new MissingInputMappingException();
    }

    @Override
    public Optional<SQLPPMapping> loadPPMapping() throws MappingIOException, InvalidMappingException, DuplicateMappingException {
        return loadPPMapping(Optional::empty);
    }

    Optional<SQLPPMapping> loadPPMapping(OntologySupplier ontologySupplier)
            throws MappingIOException, InvalidMappingException, DuplicateMappingException {
        return loadPPMapping(
                () -> options.temporalMappingFile,
                () -> null,
                () -> null);
    }

    /**
     * Please overload isMappingDefined() instead.
     */
    @Override
    boolean isInputMappingDefined() {
        return super.isInputMappingDefined()
                || options.temporalMappingFile.isPresent();
    }


    static class OntopTemporalMappingSQLAllOptions {
        private final Optional<File> temporalMappingFile;
        private final Optional<File> mappingFile;
        private final Optional<Reader> mappingReader;
        private final Optional<Graph> mappingGraph;
        private final Optional<File> constraintFile;
        final OntopMappingSQLAllOptions mappingSQLOptions;

        OntopTemporalMappingSQLAllOptions(Optional<File> temporalMappingFile,
                                          Optional<File> mappingFile, Optional<Reader> mappingReader,
                                          Optional<Graph> mappingGraph, Optional<File> constraintFile,
                                          OntopMappingSQLAllOptions mappingSQLOptions) {
            this.temporalMappingFile = temporalMappingFile;
            this.mappingFile = mappingFile;
            this.mappingReader = mappingReader;
            this.mappingGraph = mappingGraph;
            this.constraintFile = constraintFile;
            this.mappingSQLOptions = mappingSQLOptions;
        }
    }

    static class StandardTemporalMappingSQLBuilderFragment<B extends OntopTemporalMappingSQLAllConfiguration.Builder<B>>
            implements OntopTemporalMappingSQLAllBuilderFragment<B> {

        private final B builder;
        private final Runnable declareMappingDefinedCB;
        private final Runnable declareImplicitConstraintSetDefinedCB;

        Optional<File> mappingFile = Optional.empty();
        Optional<Reader> mappingReader = Optional.empty();
        Optional<Graph> mappingGraph = Optional.empty();
        Optional<File> constraintFile = Optional.empty();

        Optional<File> temporalMappingFile = Optional.empty();
        Optional<Reader> temporalMappingReader = Optional.empty();

        boolean useTemporal = false;
        boolean useR2rml = false;

        /**
         * Default constructor
         */
        protected StandardTemporalMappingSQLBuilderFragment(B builder, Runnable declareMappingDefinedCB,
                                                            Runnable declareImplicitConstraintSetDefinedCB) {
            this.builder = builder;
            this.declareMappingDefinedCB = declareMappingDefinedCB;
            this.declareImplicitConstraintSetDefinedCB = declareImplicitConstraintSetDefinedCB;
        }

        @Override
        public B nativeOntopTemporalMappingFile(@Nonnull File mappingFile) {
            this.temporalMappingFile = Optional.of(mappingFile);
            useTemporal = true;
            return builder;
        }

        @Override
        public B nativeOntopTemporalMappingFile(@Nonnull String mappingFilename) {
            setTemporalMappingFile(mappingFilename);
            useTemporal = true;
            return builder;
        }

        @Override
        public B nativeOntopTemporalMappingReader(@Nonnull Reader mappingReader) {
            this.temporalMappingReader = Optional.of(mappingReader);
            useTemporal = true;
            return builder;
        }

        @Override
        public B nativeOntopMappingFile(@Nonnull File mappingFile) {
            declareMappingDefinedCB.run();
            this.mappingFile = Optional.of(mappingFile);
            return builder;
        }

        @Override
        public B nativeOntopMappingFile(@Nonnull String mappingFilename) {
            setMappingFile(mappingFilename);
            return builder;
        }

        @Override
        public B nativeOntopMappingReader(@Nonnull Reader mappingReader) {
            declareMappingDefinedCB.run();
            this.mappingReader = Optional.of(mappingReader);
            return builder;
        }

        @Override
        public B r2rmlMappingFile(@Nonnull File mappingFile) {
            declareMappingDefinedCB.run();
            useR2rml = true;
            this.mappingFile = Optional.of(mappingFile);
            return builder;
        }

        @Override
        public B r2rmlMappingFile(@Nonnull String mappingFilename) {
            declareMappingDefinedCB.run();
            useR2rml = true;

            try {
                URI fileURI = new URI(mappingFilename);
                String scheme = fileURI.getScheme();
                if (scheme == null) {
                    this.mappingFile = Optional.of(new File(fileURI.getPath()));
                }
                else if (scheme.equals("file")) {
                    this.mappingFile = Optional.of(new File(fileURI));
                }
                else {
                    throw new InvalidOntopConfigurationException("Currently only local files are supported" +
                            "as R2RML mapping files");
                }
                return builder;
            } catch (URISyntaxException e) {
                throw new InvalidOntopConfigurationException("Invalid mapping file path: " + e.getMessage());
            }
        }

        @Override
        public B r2rmlMappingReader(@Nonnull Reader mappingReader) {
            declareMappingDefinedCB.run();
            useR2rml = true;
            this.mappingReader = Optional.of(mappingReader);
            return builder;
        }

        @Override
        public B r2rmlMappingGraph(@Nonnull Graph rdfGraph) {
            declareMappingDefinedCB.run();
            useR2rml = true;
            this.mappingGraph = Optional.of(rdfGraph);
            return builder;
        }

        @Override
        public B basicImplicitConstraintFile(@Nonnull File constraintFile) {
            declareImplicitConstraintSetDefinedCB.run();
            this.constraintFile = Optional.of(constraintFile);
            return builder;
        }

        @Override
        public B basicImplicitConstraintFile(@Nonnull String constraintFilename) {
            declareImplicitConstraintSetDefinedCB.run();
            try {
                URI fileURI = new URI(constraintFilename);
                String scheme = fileURI.getScheme();
                if (scheme == null) {
                    this.constraintFile = Optional.of(new File(fileURI.getPath()));
                }
                else if (scheme.equals("file")) {
                    this.constraintFile = Optional.of(new File(fileURI));
                }
                else {
                    throw new InvalidOntopConfigurationException("Currently only local files are supported" +
                            "as implicit constraint files");
                }
                return builder;
            } catch (URISyntaxException e) {
                throw new InvalidOntopConfigurationException("Invalid implicit constraint file path: " + e.getMessage());
            }
        }

        protected Properties generateProperties() {
            Properties p = new Properties();

            // Never puts the mapping file path

            return p;
        }

        boolean isR2rml() {
            return useR2rml;
        }

        protected final void setMappingFile(String mappingFilename) {
            declareMappingDefinedCB.run();
            try {
                URI fileURI = new URI(mappingFilename);
                String scheme = fileURI.getScheme();
                if (scheme == null) {
                    this.mappingFile = Optional.of(new File(fileURI.getPath()));
                }
                else if (scheme.equals("file")) {
                    this.mappingFile = Optional.of(new File(fileURI));
                }
                else {
                    throw new InvalidOntopConfigurationException("Currently only local files are supported" +
                            "as mapping files");
                }
            } catch (URISyntaxException e) {
                throw new InvalidOntopConfigurationException("Invalid mapping file path: " + e.getMessage());
            }
        }

        boolean isTemporal() {
            return useTemporal;
        }

        protected final void setTemporalMappingFile(String mappingFilename) {
            try {
                URI fileURI = new URI(mappingFilename);
                String scheme = fileURI.getScheme();
                if (scheme == null) {
                    this.temporalMappingFile = Optional.of(new File(fileURI.getPath()));
                }
                else if (scheme.equals("file")) {
                    this.temporalMappingFile = Optional.of(new File(fileURI));
                }
                else {
                    throw new InvalidOntopConfigurationException("Currently only local files are supported" +
                            "as mapping files");
                }
            } catch (URISyntaxException e) {
                throw new InvalidOntopConfigurationException("Invalid mapping file path: " + e.getMessage());
            }
        }

        final OntopTemporalMappingSQLAllOptions generateMappingSQLTemporalOptions(OntopMappingSQLOptions mappingSQLOptions) {
            return new OntopTemporalMappingSQLAllOptions(temporalMappingFile, mappingFile, mappingReader, mappingGraph, constraintFile,
                    new OntopMappingSQLAllOptions(mappingFile, mappingReader, mappingGraph, constraintFile, mappingSQLOptions));
        }
    }

    protected abstract static class OntopMappingSQLTemporalBuilderMixin<B extends OntopTemporalMappingSQLAllConfiguration.Builder<B>>
            extends OntopMappingSQLBuilderMixin<B>
            implements OntopTemporalMappingSQLAllConfiguration.Builder<B> {

        private final StandardTemporalMappingSQLBuilderFragment<B> localFragmentBuilder;
        private boolean isImplicitConstraintSetDefined = false;

        OntopMappingSQLTemporalBuilderMixin() {
            B builder = (B) this;
            this.localFragmentBuilder = new StandardTemporalMappingSQLBuilderFragment<>(builder,
                    this::declareMappingDefined, this::declareImplicitConstraintSetDefined);
        }

        @Override
        public B nativeOntopTemporalMappingFile(@Nonnull File mappingFile) {
            return localFragmentBuilder.nativeOntopTemporalMappingFile(mappingFile);
        }

        @Override
        public B nativeOntopTemporalMappingFile(@Nonnull String mappingFilename) {
            return localFragmentBuilder.nativeOntopTemporalMappingFile(mappingFilename);
        }

        @Override
        public B nativeOntopTemporalMappingReader(@Nonnull Reader mappingReader) {
            return localFragmentBuilder.nativeOntopTemporalMappingReader(mappingReader);
        }

        @Override
        public B nativeOntopMappingFile(@Nonnull File mappingFile) {
            return localFragmentBuilder.nativeOntopMappingFile(mappingFile);
        }

        @Override
        public B nativeOntopMappingFile(@Nonnull String mappingFilename) {
            return localFragmentBuilder.nativeOntopMappingFile(mappingFilename);
        }

        @Override
        public B nativeOntopMappingReader(@Nonnull Reader mappingReader) {
            return localFragmentBuilder.nativeOntopMappingReader(mappingReader);
        }

        @Override
        public B r2rmlMappingFile(@Nonnull File mappingFile) {
            return localFragmentBuilder.r2rmlMappingFile(mappingFile);
        }

        @Override
        public B r2rmlMappingFile(@Nonnull String mappingFilename) {
            return localFragmentBuilder.r2rmlMappingFile(mappingFilename);
        }

        @Override
        public B r2rmlMappingReader(@Nonnull Reader mappingReader) {
            return localFragmentBuilder.r2rmlMappingReader(mappingReader);
        }

        @Override
        public B r2rmlMappingGraph(@Nonnull Graph rdfGraph) {
            return localFragmentBuilder.r2rmlMappingGraph(rdfGraph);
        }

        @Override
        public B basicImplicitConstraintFile(@Nonnull File constraintFile) {
            return localFragmentBuilder.basicImplicitConstraintFile(constraintFile);
        }

        @Override
        public B basicImplicitConstraintFile(@Nonnull String constraintFilename) {
            return localFragmentBuilder.basicImplicitConstraintFile(constraintFilename);
        }

        final OntopTemporalMappingSQLAllOptions generateTemporalMappingSQLAllOptions() {
            return localFragmentBuilder.generateMappingSQLTemporalOptions(generateMappingSQLOptions());
        }

        @Override
        protected Properties generateProperties() {
            Properties p = super.generateProperties();
            p.putAll(localFragmentBuilder.generateProperties());
            return p;
        }

        boolean isTemporal(){return localFragmentBuilder.isTemporal();}

        boolean isR2rml() {
            return localFragmentBuilder.isR2rml();
        }

        void declareImplicitConstraintSetDefined() {
            if (isImplicitConstraintSetDefined)
                throw new InvalidOntopConfigurationException("The implicit constraint file is already defined");
            isImplicitConstraintSetDefined = true;
        }

    }

    public static class BuilderImpl<B extends OntopTemporalMappingSQLAllConfiguration.Builder<B>>
            extends OntopTemporalMappingSQLAllConfigurationImpl.OntopMappingSQLTemporalBuilderMixin<B> {

        @Override
        public OntopTemporalMappingSQLAllConfiguration build() {
            OntopMappingSQLAllSettings settings = new OntopTemporalMappingSQLAllSettingsImpl(generateProperties(), isR2rml(), isTemporal());
            OntopTemporalMappingSQLAllOptions options = generateTemporalMappingSQLAllOptions();

            return new OntopTemporalMappingSQLAllConfigurationImpl(settings, options);
        }
    }
}
