package it.unibz.inf.ontop.injection.impl;


import com.google.inject.Guice;
import com.google.inject.Injector;
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
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OBDACoreConfigurationImpl implements OBDACoreConfiguration {

    private final OBDAProperties obdaProperties;
    private final OBDAConfigurationOptions options;

    // Late-building
    private Injector injector;

    protected OBDACoreConfigurationImpl(OBDAProperties obdaProperties,
                                        OBDAConfigurationOptions options) {
        this.obdaProperties = obdaProperties;
        this.options = options;
        // Will be built on-demand
        injector = null;
    }

    /**
     * To be overloaded
     *
     */
    protected Stream<Module> buildGuiceModules() {
        return Stream.of(new OBDACoreModule(this));
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
        else {
            return Optional.empty();
        }
    }

    /**
     * TODO: complete
     */
    @Override
    public void validate() throws InvalidOBDAConfigurationException {
        // TODO: complete if multiple alternatives for building the OBDAModel are provided
    }

    protected boolean areMappingsDefined() {
        return obdaProperties.contains(OBDAProperties.MAPPING_FILE_PATH)
                || options.mappingFile.isPresent()
                || options.mappingGraph.isPresent()
                || options.mappingReader.isPresent()
                || options.predefinedMappingModel.isPresent();
    }

    @Override
    public final Injector getInjector() {
        if (injector == null) {
            injector = Guice.createInjector(buildGuiceModules().collect(Collectors.toList()));
        }
        return injector;
    }

    @Override
    public Optional<ImplicitDBConstraintsReader> getImplicitDBConstraintsReader() {
        return options.implicitDBConstraintsReader;
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
    public static class BuilderImpl<B extends Builder, P extends OBDAProperties, C extends OBDACoreConfiguration> implements Builder<B> {

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
        private Optional<Properties> properties = Optional.empty();
        private Optional<Boolean> obtainFullMetadata = Optional.empty();
        private Optional<String> jdbcUrl = Optional.empty();

        private boolean useR2rml = false;
        private boolean areMappingsDefined = false;

        /**
         * Not for end-users! Please consider giving a mapping file or a mapping reader.
         */
        @Override
        public B obdaModel(@Nonnull OBDAModel obdaModel) {
            if (areMappingsDefined) {
                throw new InvalidOBDAConfigurationException("OBDA model or mappings already defined!");
            }
            areMappingsDefined = true;
            this.obdaModel = Optional.of(obdaModel);
            return (B) this;
        }

        @Override
        public B nativeOntopMappingFile(@Nonnull File mappingFile) {
            if (areMappingsDefined) {
                throw new InvalidOBDAConfigurationException("OBDA model or mappings already defined!");
            }
            areMappingsDefined = true;
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
            if (areMappingsDefined) {
                throw new InvalidOBDAConfigurationException("OBDA model or mappings already defined!");
            }
            areMappingsDefined = true;
            this.mappingReader = Optional.of(mappingReader);
            return (B) this;
        }

        @Override
        public B r2rmlMappingFile(@Nonnull File mappingFile) {
            if (areMappingsDefined) {
                throw new InvalidOBDAConfigurationException("OBDA model or mappings already defined!");
            }
            areMappingsDefined = true;
            useR2rml = true;
            this.mappingFile = Optional.of(mappingFile);
            return (B) this;
        }

        @Override
        public B r2rmlMappingFile(@Nonnull String mappingFilename) {
            if (areMappingsDefined) {
                throw new InvalidOBDAConfigurationException("OBDA model or mappings already defined!");
            }
            areMappingsDefined = true;
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
                    throw new InvalidOBDAConfigurationException("Currently only local files are supported" +
                            "as R2RML mapping files");
                }
                return (B) this;
            } catch (URISyntaxException e) {
                throw new InvalidOBDAConfigurationException("Invalid mapping file path: " + e.getMessage());
            }
        }

        @Override
        public B r2rmlMappingReader(@Nonnull Reader mappingReader) {
            if (areMappingsDefined) {
                throw new InvalidOBDAConfigurationException("OBDA model or mappings already defined!");
            }
            areMappingsDefined = true;
            useR2rml = true;
            this.mappingReader = Optional.of(mappingReader);
            return (B) this;
        }

        @Override
        public B r2rmlMappingGraph(@Nonnull Model rdfGraph) {
            if (areMappingsDefined) {
                throw new InvalidOBDAConfigurationException("OBDA model or mappings already defined!");
            }
            areMappingsDefined = true;
            useR2rml = true;
            this.mappingGraph = Optional.of(rdfGraph);
            return (B) this;
        }

        /**
         * Have precedence over other parameters
         */
        @Override
        public B properties(@Nonnull Properties properties) {
            this.properties = Optional.of(properties);
            return (B) this;
        }

        @Override
        public B propertyFile(String propertyFilePath) {
            try {
                URI fileURI = new URI(propertyFilePath);
                String scheme = fileURI.getScheme();
                if (scheme == null) {
                    return propertyFile(new File(fileURI.getPath()));
                }
                else if (scheme.equals("file")) {
                    return propertyFile(new File(fileURI));
                }
                else {
                    throw new InvalidOBDAConfigurationException("Currently only local property files are supported.");
                }
            } catch (URISyntaxException e) {
                throw new InvalidOBDAConfigurationException("Invalid property file path: " + e.getMessage());
            }
        }

        @Override
        public B propertyFile(File propertyFile) {
            try {
                Properties p = new Properties();
                p.load(new FileReader(propertyFile));
                return properties(p);

            } catch (IOException e) {
                throw new InvalidOBDAConfigurationException("Cannot reach the property file: " + propertyFile);
            }
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

        @Override
        public final C build() {
            Properties p = generateProperties();

            /**
             * User-provided properties have the highest precedence.
             */
            properties.ifPresent(p::putAll);

            P obdaProperties = createOBDAProperties(p);

            return createConfiguration(obdaProperties);

        }

        /**
         * TODO: explain
         * TODO: find a better term
         *
         * Can be overloaded (for extensions)
         */
        protected Properties generateProperties() {
            Properties p = new Properties();

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
        protected P createOBDAProperties(Properties p) {
            return (P) new OBDAPropertiesImpl(p, useR2rml);
        }

        /**
         * To be overloaded by specialized classes (extensions).
         *
         * Default implementation for P == OBDAConfiguration
         */
        protected C createConfiguration(P obdaProperties) {
            return (C) new OBDACoreConfigurationImpl(obdaProperties, createOBDAConfigurationArguments());
        }

        protected final OBDAConfigurationOptions createOBDAConfigurationArguments() {
            return new OBDAConfigurationOptions(mappingFile, mappingReader, mappingGraph, obdaModel, userConstraints);
        }

        protected boolean isR2rml() {
            return useR2rml;
        }

        protected final void setMappingFile(String mappingFilename) {
            if (areMappingsDefined) {
                throw new InvalidOBDAConfigurationException("OBDA model or mappings already defined!");
            }
            areMappingsDefined = true;
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
                    throw new InvalidOBDAConfigurationException("Currently only local files are supported" +
                            "as mapping files");
                }
            } catch (URISyntaxException e) {
                throw new InvalidOBDAConfigurationException("Invalid mapping file path: " + e.getMessage());
            }
        }
    }

}
