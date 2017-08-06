package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.OntopMappingSQLTemporalSettings;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.OBDASpecificationExtractor;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.injection.OntopMappingSQLTemporalConfiguration;
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

public class OntopMappingSQLTemporalConfigurationImpl extends OntopMappingSQLConfigurationImpl implements OntopMappingSQLTemporalConfiguration {

    private final OntopMappingSQLTemporalSettings settings;
    private final OntopMappingSQLTemporalConfigurationImpl.OntopMappingSQLTemporalOptions options;

    OntopMappingSQLTemporalConfigurationImpl(OntopMappingSQLTemporalSettings settings, OntopMappingSQLTemporalConfigurationImpl.OntopMappingSQLTemporalOptions options) {
        super(settings, options.mappingSQLOptions);
        this.settings = settings;
        this.options = options;
    }

    @Override
    public OntopMappingSQLTemporalSettings getSettings() {
        return settings;
    }

    @Override
    public OBDASpecification loadSpecification() throws OBDASpecificationException {
        return loadSpecification(Optional::empty);
    }

    OBDASpecification loadSpecification(OntologySupplier ontologySupplier)
            throws OBDASpecificationException {

        return loadSpecification(ontologySupplier,
                () -> options.mappingFile,
                () -> options.mappingReader,
                () -> options.mappingGraph,
                () -> options.constraintFile);
    }

    OBDASpecification loadSpecification(OntologySupplier ontologySupplier,
                                        Supplier<Optional<File>> mappingFileSupplier,
                                        Supplier<Optional<Reader>> mappingReaderSupplier,
                                        Supplier<Optional<Graph>> mappingGraphSupplier,
                                        Supplier<Optional<File>> constraintFileSupplier)
            throws OBDASpecificationException {
        return loadSpecification(
                ontologySupplier,
                () -> options.mappingSQLOptions.ppMapping.map(m -> (PreProcessedMapping) m),
                mappingFileSupplier,
                mappingReaderSupplier,
                mappingGraphSupplier,
                constraintFileSupplier
        );
    }

    OBDASpecification loadSpecification(OntologySupplier ontologySupplier,
                                        Supplier<Optional<PreProcessedMapping>> ppMappingSupplier,
                                        Supplier<Optional<File>> mappingFileSupplier,
                                        Supplier<Optional<Reader>> mappingReaderSupplier,
                                        Supplier<Optional<Graph>> mappingGraphSupplier,
                                        Supplier<Optional<File>> constraintFileSupplier
    ) throws OBDASpecificationException {
        OBDASpecificationExtractor extractor = getInjector().getInstance(OBDASpecificationExtractor.class);

        Optional<Ontology> optionalOntology = ontologySupplier.get();
        Optional<DBMetadata> optionalMetadata = options.mappingSQLOptions.mappingOptions.dbMetadata;

        /*
         * Pre-processed mapping
         */
        Optional<PreProcessedMapping> optionalPPMapping = ppMappingSupplier.get();

        OBDASpecInput.Builder specInputBuilder = OBDASpecInput.defaultBuilder();
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
        return loadPPMapping(ontologySupplier,
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


    static class OntopMappingSQLTemporalOptions {
        private final Optional<File> temporalMappingFile;
        private final Optional<File> mappingFile;
        private final Optional<Reader> mappingReader;
        private final Optional<Graph> mappingGraph;
        private final Optional<File> constraintFile;
        final OntopMappingSQLOptions mappingSQLOptions;

        OntopMappingSQLTemporalOptions(Optional<File> temporalMappingFile,
                                       Optional<File> mappingFile, Optional<Reader> mappingReader,
                                       Optional<Graph> mappingGraph, Optional<File> constraintFile,
                                       OntopMappingSQLOptions mappingSQLOptions) {
            this.temporalMappingFile = temporalMappingFile;
            this.mappingFile = mappingFile;
            this.mappingReader = mappingReader;
            this.mappingGraph = mappingGraph;
            this.constraintFile = constraintFile;
            this.mappingSQLOptions = mappingSQLOptions;
        }
    }

    static class StandardMappingSQLTemporalBuilderFragment<B extends OntopMappingSQLTemporalConfiguration.Builder<B>>
            implements OntopMappingSQLTemporalBuilderFragment<B>  {

        private final B builder;
        private final Runnable declareMappingDefinedCB;
        private final Runnable declareImplicitConstraintSetDefinedCB;

        private Optional<File> mappingFile = Optional.empty();
        private Optional<Reader> mappingReader = Optional.empty();
        private Optional<Graph> mappingGraph = Optional.empty();
        private Optional<File> constraintFile = Optional.empty();

        private Optional<File> temporalMappingFile = Optional.empty();
        private Optional<Reader> temporalMappingReader = Optional.empty();

        private boolean useTemporal = false;
        private boolean useR2rml = false;

        /**
         * Default constructor
         */
        protected StandardMappingSQLTemporalBuilderFragment(B builder, Runnable declareMappingDefinedCB,
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

        final OntopMappingSQLTemporalOptions generateMappingSQLTemporalOptions(OntopMappingSQLOptions mappingOptions) {
            return new OntopMappingSQLTemporalOptions(temporalMappingFile, mappingFile, mappingReader, mappingGraph, constraintFile, mappingOptions);
        }
    }

    protected abstract static class OntopMappingSQLTemporalBuilderMixin<B extends OntopMappingSQLTemporalConfiguration.Builder<B>>
            extends OntopMappingSQLAllConfigurationImpl.OntopMappingSQLAllBuilderMixin<B>
            implements OntopMappingSQLTemporalConfiguration.Builder<B> {

        private final OntopMappingSQLTemporalConfigurationImpl.StandardMappingSQLTemporalBuilderFragment<B> localFragmentBuilder;
        private boolean isImplicitConstraintSetDefined = false;

        OntopMappingSQLTemporalBuilderMixin() {
            B builder = (B) this;
            this.localFragmentBuilder = new OntopMappingSQLTemporalConfigurationImpl.StandardMappingSQLTemporalBuilderFragment<>(builder,
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

        final OntopMappingSQLTemporalConfigurationImpl.OntopMappingSQLTemporalOptions generateMappingSQLTemporalOptions() {
            OntopMappingSQLOptions sqlMappingOptions = generateMappingSQLAllOptions().mappingSQLOptions;
            return localFragmentBuilder.generateMappingSQLTemporalOptions(sqlMappingOptions);
        }

        @Override
        protected Properties generateProperties() {
            Properties p = super.generateProperties();
            p.putAll(localFragmentBuilder.generateProperties());
            return p;
        }

        boolean isTemporal(){return localFragmentBuilder.isTemporal();}

        void declareImplicitConstraintSetDefined() {
            if (isImplicitConstraintSetDefined)
                throw new InvalidOntopConfigurationException("The implicit constraint file is already defined");
            isImplicitConstraintSetDefined = true;
        }

    }

    public static class BuilderImpl<B extends OntopMappingSQLTemporalConfiguration.Builder<B>>
            extends OntopMappingSQLTemporalConfigurationImpl.OntopMappingSQLTemporalBuilderMixin<B> {

        @Override
        public OntopMappingSQLTemporalConfiguration build() {
            OntopMappingSQLTemporalSettings settings = new OntopMappingSQLTemporalSettingsImpl(generateProperties(), isR2rml(), isTemporal());
            OntopMappingSQLTemporalConfigurationImpl.OntopMappingSQLTemporalOptions options = generateMappingSQLTemporalOptions();

            return new OntopMappingSQLTemporalConfigurationImpl(settings, options);
        }
    }
}
