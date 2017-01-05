package it.unibz.inf.ontop.injection.impl;


import com.google.inject.Module;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.io.InvalidDataSourceException;
import it.unibz.inf.ontop.mapping.MappingParser;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.sql.ImplicitDBConstraintsReader;
import org.openrdf.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public class OBDACoreConfigurationImpl extends OntopModelConfigurationImpl implements OBDACoreConfiguration {

    private final OBDAProperties obdaProperties;
    private final OBDAConfigurationOptions options;

    protected OBDACoreConfigurationImpl(OBDAProperties obdaProperties,
                                        OntopModelConfigurationOptions modelOptions,
                                        OBDAConfigurationOptions options) {
        super(obdaProperties, modelOptions);
        this.obdaProperties = obdaProperties;
        this.options = options;
    }

    /**
     * To be overloaded
     *
     */
    @Override
    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                super.buildGuiceModules(),
                Stream.of(new OBDACoreModule(this)));
    }

    @Override
    public OBDAProperties getOBDAProperties() {
        return obdaProperties;
    }

    @Override
    public Optional<OBDAModel> loadMapping() throws InvalidDataSourceException, IOException, InvalidMappingException {
        if (options.predefinedMappingModel.isPresent()) {
            return options.predefinedMappingModel;
        }

        NativeQueryLanguageComponentFactory nativeQLFactory = getInjector().getInstance(
                NativeQueryLanguageComponentFactory.class);

        Optional<File> optionalMappingFile = options.mappingFile
                .map(Optional::of)
                .orElseGet(() -> obdaProperties.getMappingFilePath()
                        .map(File::new));

        if (optionalMappingFile.isPresent()) {
            MappingParser parser = nativeQLFactory.create(optionalMappingFile.get());
            return Optional.of(parser.getOBDAModel());
        }
        else if (options.mappingReader.isPresent()) {
            MappingParser parser = nativeQLFactory.create(options.mappingReader.get());
            return Optional.of(parser.getOBDAModel());
        }
        else if (options.mappingGraph.isPresent()) {
            MappingParser parser = nativeQLFactory.create(options.mappingGraph.get());
            return Optional.of(parser.getOBDAModel());
        }
        /**
         * Hook
         */
        else {
            return loadAlternativeMapping();
        }
    }

    /**
     * TODO: complete
     */
    @Override
    public void validate() throws InvalidOntopConfigurationException {
        // TODO: complete if multiple alternatives for building the OBDAModel are provided
    }

    /**
     * Please overload isMappingDefined() instead.
     */
    protected boolean isInputMappingDefined() {
        return obdaProperties.contains(OBDAProperties.MAPPING_FILE_PATH)
                || options.mappingFile.isPresent()
                || options.mappingGraph.isPresent()
                || options.mappingReader.isPresent()
                || options.predefinedMappingModel.isPresent();
    }

    /**
     * To be overloaded
     */
    protected boolean isMappingDefined() {
        return isInputMappingDefined();
    }

    @Override
    public Optional<ImplicitDBConstraintsReader> getImplicitDBConstraintsReader() {
        return options.implicitDBConstraintsReader;
    }

    /**
     * To be overloaded.
     *
     * By default, returns nothing.
     */
    protected Optional<OBDAModel> loadAlternativeMapping() throws InvalidDataSourceException {
        return Optional.empty();
    }

    /**
     * Groups all the options required by the OBDAConfiguration.
     *
     * Useful for extensions
     *
     */
    public static class OBDAConfigurationOptions {
        public final Optional<File> mappingFile;
        public final Optional<Reader> mappingReader;
        public final Optional<Model> mappingGraph;
        public final Optional<OBDAModel> predefinedMappingModel;
        public final Optional<ImplicitDBConstraintsReader> implicitDBConstraintsReader;

        public OBDAConfigurationOptions(Optional<File> mappingFile, Optional<Reader> mappingReader, Optional<Model> mappingGraph,
                                        Optional<OBDAModel> predefinedMappingModel,
                                        Optional<ImplicitDBConstraintsReader> implicitDBConstraintsReader) {
            this.mappingFile = mappingFile;
            this.mappingReader = mappingReader;
            this.mappingGraph = mappingGraph;
            this.predefinedMappingModel = predefinedMappingModel;
            this.implicitDBConstraintsReader = implicitDBConstraintsReader;
        }
    }

    /**
     * Builder
     *
     */
    public static class BuilderImpl<B extends OBDACoreConfiguration.Builder, P extends OBDAProperties, C extends OBDACoreConfiguration>
            extends OntopModelConfigurationImpl.BuilderImpl<B,P,C>
            implements OBDACoreConfiguration.Builder<B> {

        /**
         * Please make sure it is an instance of B!
         */
        public BuilderImpl() {
        }

        private Optional<ImplicitDBConstraintsReader> userConstraints = Optional.empty();
        private Optional<OBDAModel> obdaModel = Optional.empty();
        private Optional<File> mappingFile = Optional.empty();
        private Optional<Reader> mappingReader = Optional.empty();
        private Optional<Model> mappingGraph = Optional.empty();
        private Optional<Boolean> obtainFullMetadata = Optional.empty();
        private Optional<String> jdbcUrl = Optional.empty();

        private boolean useR2rml = false;
        private boolean isMappingDefined = false;

        /**
         * Not for end-users! Please consider giving a mapping file or a mapping reader.
         */
        @Override
        public B obdaModel(@Nonnull OBDAModel obdaModel) {
            if (isMappingDefined) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
            declareMappingDefined();
            this.obdaModel = Optional.of(obdaModel);
            return (B) this;
        }

        @Override
        public B nativeOntopMappingFile(@Nonnull File mappingFile) {
            if (isMappingDefined) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
            declareMappingDefined();
            this.mappingFile = Optional.of(mappingFile);
            return (B) this;
        }

        @Override
        public B nativeOntopMappingFile(@Nonnull String mappingFilename) {
            setMappingFile(mappingFilename);
            return (B) this;
        }

        @Override
        public B nativeOntopMappingReader(@Nonnull Reader mappingReader) {
            if (isMappingDefined) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
            declareMappingDefined();
            this.mappingReader = Optional.of(mappingReader);
            return (B) this;
        }

        @Override
        public B r2rmlMappingFile(@Nonnull File mappingFile) {
            if (isMappingDefined) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
            declareMappingDefined();
            useR2rml = true;
            this.mappingFile = Optional.of(mappingFile);
            return (B) this;
        }

        @Override
        public B r2rmlMappingFile(@Nonnull String mappingFilename) {
            if (isMappingDefined) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
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
                return (B) this;
            } catch (URISyntaxException e) {
                throw new InvalidOntopConfigurationException("Invalid mapping file path: " + e.getMessage());
            }
        }

        @Override
        public B r2rmlMappingReader(@Nonnull Reader mappingReader) {
            if (isMappingDefined) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
            declareMappingDefined();
            useR2rml = true;
            this.mappingReader = Optional.of(mappingReader);
            return (B) this;
        }

        @Override
        public B r2rmlMappingGraph(@Nonnull Model rdfGraph) {
            if (isMappingDefined) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
            declareMappingDefined();
            useR2rml = true;
            this.mappingGraph = Optional.of(rdfGraph);
            return (B) this;
        }

        @Override
        public B dbConstraintsReader(@Nonnull ImplicitDBConstraintsReader constraints) {
            this.userConstraints = Optional.of(constraints);
            return (B) this;
        }

        @Override
        public B enableFullMetadataExtraction(boolean obtainFullMetadata) {
            this.obtainFullMetadata = Optional.of(obtainFullMetadata);
            return (B) this;
        }

        @Override
        public B jdbcUrl(String jdbcUrl) {
            this.jdbcUrl = Optional.of(jdbcUrl);
            return (B) this;
        }

        /**
         * Allows to detect double mapping definition (error).
         */
        protected final void declareMappingDefined() {
            isMappingDefined = true;
        }

        protected final boolean isMappingDefined() {
            return isMappingDefined;
        }

        @Override
        protected Properties generateProperties() {
            Properties p = super.generateProperties();

            // Never puts the mapping file path

            obtainFullMetadata.ifPresent(m -> p.put(OBDAProperties.OBTAIN_FULL_METADATA, m));
            jdbcUrl.ifPresent(u -> p.put(OBDAProperties.JDBC_URL, u));

            return p;
        }

        /**
         * To be overloaded by specialized classes (extensions).
         *
         * Default implementation for P == OBDAProperties
         */
        @Override
        protected P createOntopModelProperties(Properties p) {
            return (P) new OBDAPropertiesImpl(p, useR2rml);
        }

        /**
         * To be overloaded by specialized classes (extensions).
         *
         * Default implementation for P == OBDAConfiguration
         */
        @Override
        protected C createConfiguration(P obdaProperties) {
            return (C) new OBDACoreConfigurationImpl(obdaProperties, createOntopModelConfigurationArguments(),
                    createOBDAConfigurationArguments());
        }

        protected final OBDAConfigurationOptions createOBDAConfigurationArguments() {
            return new OBDAConfigurationOptions(mappingFile, mappingReader, mappingGraph, obdaModel, userConstraints);
        }

        protected boolean isR2rml() {
            return useR2rml;
        }

        protected final void setMappingFile(String mappingFilename) {
            if (isMappingDefined) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
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
    }

}
