package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllSettings;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.OBDASpecification;
import org.apache.commons.rdf.api.Graph;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Properties;

public class OntopMappingSQLAllConfigurationImpl extends OntopMappingSQLConfigurationImpl
        implements OntopMappingSQLAllConfiguration {


    private final OntopMappingSQLAllSettings settings;
    private final OntopMappingSQLAllOptions options;

    OntopMappingSQLAllConfigurationImpl(OntopMappingSQLAllSettings settings, OntopMappingSQLAllOptions options) {
        super(settings, options.mappingSQLOptions);
        this.settings = settings;
        this.options = options;
    }

    @Override
    public OntopMappingSQLAllSettings getSettings() {
        return settings;
    }

    @Override
    protected OBDASpecification loadOBDASpecification() throws OBDASpecificationException {
        return loadSpecification(Optional::empty);
    }

    OBDASpecification loadSpecification(OntologySupplier ontologySupplier)
            throws OBDASpecificationException {

        return loadSpecification(ontologySupplier,
                () -> options.mappingFile,
                () -> options.mappingReader,
                () -> options.mappingGraph,
                () -> options.constraintFile,
                () -> options.dbMetadataFile,
                () -> options.dbMetadataReader,
                () -> options.ontopViewFile,
                () -> options.ontopViewReader);
    }

    @Override
    public Optional<SQLPPMapping> loadPPMapping() throws MappingIOException, InvalidMappingException {
        return loadPPMapping(
                () -> options.mappingFile,
                () -> options.mappingReader,
                () -> options.mappingGraph);
    }

    /**
     * Please overload isMappingDefined() instead.
     */
    @Override
    boolean isInputMappingDefined() {
        return super.isInputMappingDefined()
                || options.mappingFile.isPresent()
                || options.mappingGraph.isPresent()
                || options.mappingReader.isPresent();
    }


    static class OntopMappingSQLAllOptions {
        private final Optional<File> mappingFile;
        private final Optional<Reader> mappingReader;
        private final Optional<Graph> mappingGraph;
        private final Optional<File> constraintFile;
        private final Optional<File> dbMetadataFile;
        private final Optional<Reader> dbMetadataReader;
        private final Optional<File> ontopViewFile;
        private final Optional<Reader> ontopViewReader;
        final OntopMappingSQLOptions mappingSQLOptions;


        OntopMappingSQLAllOptions(Optional<File> mappingFile, Optional<Reader> mappingReader,
                                  Optional<Graph> mappingGraph, Optional<File> constraintFile,
                                  Optional<File> dbMetadataFile, Optional<Reader> dbMetadataReader,
                                  Optional<File> ontopViewFile, Optional<Reader> ontopViewReader,
                                  OntopMappingSQLOptions mappingSQLOptions) {
            this.mappingFile = mappingFile;
            this.mappingReader = mappingReader;
            this.mappingGraph = mappingGraph;
            this.constraintFile = constraintFile;
            this.dbMetadataFile = dbMetadataFile;
            this.dbMetadataReader = dbMetadataReader;
            this.ontopViewFile = ontopViewFile;
            this.ontopViewReader = ontopViewReader;
            this.mappingSQLOptions = mappingSQLOptions;
        }
    }


    static class StandardMappingSQLAllBuilderFragment<B extends OntopMappingSQLAllConfiguration.Builder<B>>
            implements OntopMappingSQLAllBuilderFragment<B> {
        private final B builder;
        private final Runnable declareMappingDefinedCB;
        private final Runnable declareImplicitConstraintSetDefinedCB;
        private final Runnable declareDBMetadataSetDefinedCB;
        private final Runnable declareOntopViewSetDefinedCB;

        private Optional<File> mappingFile = Optional.empty();
        private Optional<Reader> mappingReader = Optional.empty();
        private Optional<Graph> mappingGraph = Optional.empty();
        private Optional<File> constraintFile = Optional.empty();
        private Optional<File> dbMetadataFile = Optional.empty();
        private Optional<Reader> dbMetadataReader = Optional.empty();
        private Optional<File> ontopViewFile = Optional.empty();
        private Optional<Reader> ontopViewReader = Optional.empty();
        private boolean useR2rml = false;


        /**
         * Default constructor
         */
        protected StandardMappingSQLAllBuilderFragment(B builder, Runnable declareMappingDefinedCB,
                                                       Runnable declareImplicitConstraintSetDefinedCB,
                                                       Runnable declareDBMetadataSetDefinedCB,
                                                       Runnable declareOntopViewSetDefinedCB) {
            this.builder = builder;
            this.declareMappingDefinedCB = declareMappingDefinedCB;
            this.declareImplicitConstraintSetDefinedCB = declareImplicitConstraintSetDefinedCB;
            this.declareDBMetadataSetDefinedCB = declareDBMetadataSetDefinedCB;
            this.declareOntopViewSetDefinedCB = declareOntopViewSetDefinedCB;
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

            @Override
            public B dbMetadataFile(@Nonnull File dbMetadataFile) {
                declareDBMetadataSetDefinedCB.run();
                this.dbMetadataFile = Optional.of(dbMetadataFile);
                return builder;
            }

            @Override
            public B dbMetadataFile(@Nonnull String dbMetadataFilename) {
                declareDBMetadataSetDefinedCB.run();
                try {
                    URI fileURI = new URI(dbMetadataFilename);
                    String scheme = fileURI.getScheme();
                    if (scheme == null) {
                        this.dbMetadataFile = Optional.of(new File(fileURI.getPath()));
                    }
                    else if (scheme.equals("file")) {
                        this.dbMetadataFile = Optional.of(new File(fileURI));
                    }
                    else {
                        throw new InvalidOntopConfigurationException("Currently only local files are supported" +
                                "as db-metadata files");
                    }
                    return builder;
                } catch (URISyntaxException e) {
                    throw new InvalidOntopConfigurationException("Invalid db-metadata file path: " + e.getMessage());
                }
            }

        @Override
        public B dbMetadataReader(@Nonnull Reader dbMetadataReader) {
            declareDBMetadataSetDefinedCB.run();
            this.dbMetadataReader = Optional.of(dbMetadataReader);
            return builder;
        }

        @Override
        public B ontopViewFile(@Nonnull File ontopViewFile) {
            declareOntopViewSetDefinedCB.run();
            this.ontopViewFile = Optional.of(ontopViewFile);
            return builder;
        }

        @Override
        public B ontopViewFile(@Nonnull String ontopViewFilename) {
            declareOntopViewSetDefinedCB.run();
            try {
                URI fileURI = new URI(ontopViewFilename);
                String scheme = fileURI.getScheme();
                if (scheme == null) {
                    this.ontopViewFile = Optional.of(new File(fileURI.getPath()));
                }
                else if (scheme.equals("file")) {
                    this.ontopViewFile = Optional.of(new File(fileURI));
                }
                else {
                    throw new InvalidOntopConfigurationException("Currently only local files are supported" +
                            "as Ontop view files");
                }
                return builder;
            } catch (URISyntaxException e) {
                throw new InvalidOntopConfigurationException("Invalid Ontop view file path: " + e.getMessage());
            }
        }

        @Override
        public B ontopViewReader(@Nonnull Reader ontopViewReader) {
            declareOntopViewSetDefinedCB.run();
            this.ontopViewReader = Optional.of(ontopViewReader);
            return builder;
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

        final OntopMappingSQLAllOptions generateMappingSQLAllOptions(OntopMappingSQLOptions mappingOptions) {
                return new OntopMappingSQLAllOptions(mappingFile, mappingReader, mappingGraph, constraintFile,
                        dbMetadataFile, dbMetadataReader, ontopViewFile, ontopViewReader, mappingOptions);
        }

    }

    protected abstract static class OntopMappingSQLAllBuilderMixin<B extends OntopMappingSQLAllConfiguration.Builder<B>>
            extends OntopMappingSQLBuilderMixin<B>
            implements OntopMappingSQLAllConfiguration.Builder<B> {

        private final StandardMappingSQLAllBuilderFragment<B> localFragmentBuilder;
        private boolean isImplicitConstraintSetDefined = false;

        OntopMappingSQLAllBuilderMixin() {
            B builder = (B) this;
            this.localFragmentBuilder = new StandardMappingSQLAllBuilderFragment<>(builder,
                    this::declareMappingDefined, this::declareImplicitConstraintSetDefined,
                    this::declareDBMetadataDefined, this::declareOntopViewDefined);
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

        @Override
        public B dbMetadataFile(@Nonnull File dbmetadataFile) {
            return localFragmentBuilder.dbMetadataFile(dbmetadataFile);
        }

        @Override
        public B dbMetadataFile(@Nonnull String dbmetadataFilename) {
            return localFragmentBuilder.dbMetadataFile(dbmetadataFilename);
        }

        @Override
        public B dbMetadataReader(@Nonnull Reader dbMetadataReader) {
            return localFragmentBuilder.dbMetadataReader(dbMetadataReader);
        }

        @Override
        public B ontopViewFile(@Nonnull File ontopViewFile) {
            return localFragmentBuilder.ontopViewFile(ontopViewFile);
        }

        @Override
        public B ontopViewFile(@Nonnull String ontopViewFilename) {
            return localFragmentBuilder.ontopViewFile(ontopViewFilename);
        }

        @Override
        public B ontopViewReader(@Nonnull Reader ontopViewReader) {
            return localFragmentBuilder.ontopViewReader(ontopViewReader);
        }

        final OntopMappingSQLAllOptions generateMappingSQLAllOptions() {
            OntopMappingSQLOptions sqlMappingOptions = generateMappingSQLOptions();
            return localFragmentBuilder.generateMappingSQLAllOptions(sqlMappingOptions);
        }

        @Override
        protected Properties generateProperties() {
            Properties p = super.generateProperties();
            p.putAll(localFragmentBuilder.generateProperties());
            return p;
        }

        boolean isR2rml() {
            return localFragmentBuilder.isR2rml();
        }

        void declareImplicitConstraintSetDefined() {
            if (isImplicitConstraintSetDefined)
                throw new InvalidOntopConfigurationException("The implicit constraint file is already defined");
            isImplicitConstraintSetDefined = true;
        }

    }

    public static class BuilderImpl<B extends OntopMappingSQLAllConfiguration.Builder<B>>
            extends OntopMappingSQLAllBuilderMixin<B> {

        @Override
        public OntopMappingSQLAllConfiguration build() {
            OntopMappingSQLAllSettings settings = new OntopMappingSQLAllSettingsImpl(generateProperties(), isR2rml());
            OntopMappingSQLAllOptions options = generateMappingSQLAllOptions();

            return new OntopMappingSQLAllConfigurationImpl(settings, options);
        }

    }
}
