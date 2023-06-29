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
        return loadSpecification(Optional::empty, Optional::empty);
    }

    OBDASpecification loadSpecification(OntologySupplier ontologySupplier, FactsSupplier factsSupplier)
            throws OBDASpecificationException {

        return loadSpecification(ontologySupplier, factsSupplier,
                () -> options.mappingFile,
                () -> options.mappingReader,
                () -> options.mappingGraph,
                () -> options.constraintFile,
                () -> options.dbMetadataFile,
                () -> options.dbMetadataReader,
                () -> options.lensesFile,
                () -> options.lensesReader);
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
        private final Optional<File> lensesFile;
        private final Optional<Reader> lensesReader;
        final OntopMappingSQLOptions mappingSQLOptions;


        OntopMappingSQLAllOptions(Optional<File> mappingFile, Optional<Reader> mappingReader,
                                  Optional<Graph> mappingGraph, Optional<File> constraintFile,
                                  Optional<File> dbMetadataFile, Optional<Reader> dbMetadataReader,
                                  Optional<File> lensesFile, Optional<Reader> lensesReader,
                                  OntopMappingSQLOptions mappingSQLOptions) {
            this.mappingFile = mappingFile;
            this.mappingReader = mappingReader;
            this.mappingGraph = mappingGraph;
            this.constraintFile = constraintFile;
            this.dbMetadataFile = dbMetadataFile;
            this.dbMetadataReader = dbMetadataReader;
            this.lensesFile = lensesFile;
            this.lensesReader = lensesReader;
            this.mappingSQLOptions = mappingSQLOptions;
        }
    }


    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static abstract class StandardMappingSQLAllBuilderFragment<B extends OntopMappingSQLAllConfiguration.Builder<B>>
            implements OntopMappingSQLAllBuilderFragment<B> {

        private Optional<File> mappingFile = Optional.empty();
        private Optional<Reader> mappingReader = Optional.empty();
        private Optional<Graph> mappingGraph = Optional.empty();
        private Optional<File> constraintFile = Optional.empty();
        private Optional<File> dbMetadataFile = Optional.empty();
        private Optional<Reader> dbMetadataReader = Optional.empty();
        private Optional<File> lensesFile = Optional.empty();
        private Optional<Reader> lensesReader = Optional.empty();
        private boolean useR2rml = false;


        protected abstract B self();

        protected abstract void declareMappingDefined();

        protected abstract void declareImplicitConstraintSetDefined();

        protected abstract void declareDBMetadataDefined();

        protected abstract void declareLensesDefined();

        @Override
        public B nativeOntopMappingFile(@Nonnull File mappingFile) {
            declareMappingDefined();
            this.mappingFile = Optional.of(mappingFile);
            return self();
        }

        @Override
        public B nativeOntopMappingFile(@Nonnull String mappingFilename) {
            setMappingFile(mappingFilename);
            return self();
        }

        @Override
        public B nativeOntopMappingReader(@Nonnull Reader mappingReader) {
            declareMappingDefined();
            this.mappingReader = Optional.of(mappingReader);
            return self();
        }

        @Override
        public B r2rmlMappingFile(@Nonnull File mappingFile) {
            declareMappingDefined();
            useR2rml = true;
            this.mappingFile = Optional.of(mappingFile);
            return self();
        }

        @Override
        public B r2rmlMappingFile(@Nonnull String mappingFilename) {
            declareMappingDefined();
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
                return self();
            } catch (URISyntaxException e) {
                throw new InvalidOntopConfigurationException("Invalid mapping file path: " + e.getMessage());
            }
        }

        @Override
        public B r2rmlMappingReader(@Nonnull Reader mappingReader) {
            declareMappingDefined();
            useR2rml = true;
            this.mappingReader = Optional.of(mappingReader);
            return self();
        }

        @Override
        public B r2rmlMappingGraph(@Nonnull Graph rdfGraph) {
            declareMappingDefined();
            useR2rml = true;
            this.mappingGraph = Optional.of(rdfGraph);
            return self();
        }

        @Override
        public B basicImplicitConstraintFile(@Nonnull File constraintFile) {
            declareImplicitConstraintSetDefined();
            this.constraintFile = Optional.of(constraintFile);
            return self();
        }

        @Override
        public B basicImplicitConstraintFile(@Nonnull String constraintFilename) {
            declareImplicitConstraintSetDefined();
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
                return self();
            } catch (URISyntaxException e) {
                throw new InvalidOntopConfigurationException("Invalid implicit constraint file path: " + e.getMessage());
            }
        }

            @Override
            public B dbMetadataFile(@Nonnull File dbMetadataFile) {
                declareDBMetadataDefined();
                this.dbMetadataFile = Optional.of(dbMetadataFile);
                return self();
            }

            @Override
            public B dbMetadataFile(@Nonnull String dbMetadataFilename) {
                declareDBMetadataDefined();
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
                    return self();
                } catch (URISyntaxException e) {
                    throw new InvalidOntopConfigurationException("Invalid db-metadata file path: " + e.getMessage());
                }
            }

        @Override
        public B dbMetadataReader(@Nonnull Reader dbMetadataReader) {
            declareDBMetadataDefined();
            this.dbMetadataReader = Optional.of(dbMetadataReader);
            return self();
        }

        @Override
        public B lensesFile(@Nonnull File lensesFile) {
            declareLensesDefined();
            this.lensesFile = Optional.of(lensesFile);
            return self();
        }

        @Override
        public B lensesFile(@Nonnull String lensesFilename) {
            declareLensesDefined();
            try {
                URI fileURI = new URI(lensesFilename);
                String scheme = fileURI.getScheme();
                if (scheme == null) {
                    this.lensesFile = Optional.of(new File(fileURI.getPath()));
                }
                else if (scheme.equals("file")) {
                    this.lensesFile = Optional.of(new File(fileURI));
                }
                else {
                    throw new InvalidOntopConfigurationException("Currently only local files are supported" +
                            "as lerses files");
                }
                return self();
            } catch (URISyntaxException e) {
                throw new InvalidOntopConfigurationException("Invalid lenses file path: " + e.getMessage());
            }
        }

        @Override
        public B lensesReader(@Nonnull Reader lensesReader) {
            declareLensesDefined();
            this.lensesReader = Optional.of(lensesReader);
            return self();
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
            declareMappingDefined();
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
                        dbMetadataFile, dbMetadataReader, lensesFile, lensesReader, mappingOptions);
        }

    }

    protected abstract static class OntopMappingSQLAllBuilderMixin<B extends OntopMappingSQLAllConfiguration.Builder<B>>
            extends OntopMappingSQLBuilderMixin<B>
            implements OntopMappingSQLAllConfiguration.Builder<B> {

        private final StandardMappingSQLAllBuilderFragment<B> localFragmentBuilder;
        private boolean isImplicitConstraintSetDefined = false;

        OntopMappingSQLAllBuilderMixin() {
            this.localFragmentBuilder = new StandardMappingSQLAllBuilderFragment<>() {
                @Override
                protected B self() {
                    return OntopMappingSQLAllBuilderMixin.this.self();
                }

                @Override
                protected void declareMappingDefined() {
                    OntopMappingSQLAllBuilderMixin.this.declareMappingDefined();
                }

                @Override
                protected void declareImplicitConstraintSetDefined() {
                    OntopMappingSQLAllBuilderMixin.this.declareImplicitConstraintSetDefined();
                }

                @Override
                protected void declareDBMetadataDefined() {
                    OntopMappingSQLAllBuilderMixin.this.declareDBMetadataDefined();
                }

                @Override
                protected void declareLensesDefined() {
                    OntopMappingSQLAllBuilderMixin.this.declareLensesDefined();
                }
            };
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
        public B lensesFile(@Nonnull File ontopViewFile) {
            return localFragmentBuilder.lensesFile(ontopViewFile);
        }

        @Override
        public B lensesFile(@Nonnull String lensesFilename) {
            return localFragmentBuilder.lensesFile(lensesFilename);
        }

        @Override
        public B lensesReader(@Nonnull Reader lensesReader) {
            return localFragmentBuilder.lensesReader(lensesReader);
        }

        protected final OntopMappingSQLAllOptions generateMappingSQLAllOptions() {
            OntopMappingSQLOptions sqlMappingOptions = generateMappingSQLOptions();
            return localFragmentBuilder.generateMappingSQLAllOptions(sqlMappingOptions);
        }

        @Override
        protected Properties generateProperties() {
            Properties p = super.generateProperties();
            p.putAll(localFragmentBuilder.generateProperties());
            return p;
        }

        protected final boolean isR2rml() {
            return localFragmentBuilder.isR2rml();
        }

        protected final void declareImplicitConstraintSetDefined() {
            if (isImplicitConstraintSetDefined)
                throw new InvalidOntopConfigurationException("The implicit constraint file is already defined");
            isImplicitConstraintSetDefined = true;
        }
    }

    public static class BuilderImpl extends OntopMappingSQLAllBuilderMixin<BuilderImpl> {

        @Override
        public OntopMappingSQLAllConfiguration build() {
            OntopMappingSQLAllSettings settings = new OntopMappingSQLAllSettingsImpl(generateProperties(), isR2rml());
            OntopMappingSQLAllOptions options = generateMappingSQLAllOptions();

            return new OntopMappingSQLAllConfigurationImpl(settings, options);
        }

        @Override
        protected BuilderImpl self() {
            return this;
        }
    }
}
