package it.unibz.inf.ontop.injection.impl;


import com.google.inject.Module;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.mapping.MappingParser;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.sql.ImplicitDBConstraintsReader;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public class OBDACoreConfigurationImpl extends OntopMappingSQLConfigurationImpl implements OBDACoreConfiguration {

    private final OBDASettings settings;
    private final OBDAConfigurationOptions options;

    protected OBDACoreConfigurationImpl(OBDASettings settings,
                                        OBDAConfigurationOptions options) {
        super(settings, options.mappingSqlOptions);
        this.settings = settings;
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
    public OBDASettings getSettings() {
        return settings;
    }

    @Override
    public Optional<OBDAModel> loadMapping() throws IOException, InvalidMappingException {
        if (options.predefinedMappingModel.isPresent()) {
            return options.predefinedMappingModel;
        }

        NativeQueryLanguageComponentFactory nativeQLFactory = getInjector().getInstance(
                NativeQueryLanguageComponentFactory.class);

        Optional<File> optionalMappingFile = options.mappingFile
                .map(Optional::of)
                .orElseGet(() -> settings.getMappingFilePath()
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
        return settings.contains(OBDASettings.MAPPING_FILE_PATH)
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

    /**
     * To be overloaded.
     *
     * By default, returns nothing.
     */
    protected Optional<OBDAModel> loadAlternativeMapping() {
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
        public final OntopMappingSQLOptions mappingSqlOptions;

        public OBDAConfigurationOptions(Optional<File> mappingFile, Optional<Reader> mappingReader, Optional<Model> mappingGraph,
                                        Optional<OBDAModel> predefinedMappingModel,
                                        OntopMappingSQLOptions mappingSqlOptions) {
            this.mappingFile = mappingFile;
            this.mappingReader = mappingReader;
            this.mappingGraph = mappingGraph;
            this.predefinedMappingModel = predefinedMappingModel;
            this.mappingSqlOptions = mappingSqlOptions;
        }
    }

    protected static class DefaultOBDACoreBuilderFragment<B extends OBDACoreConfiguration.Builder>
            implements OBDACoreBuilderFragment<B> {

        private final B builder;

        private Optional<OBDAModel> obdaModel = Optional.empty();
        private Optional<File> mappingFile = Optional.empty();
        private Optional<Reader> mappingReader = Optional.empty();
        private Optional<Model> mappingGraph = Optional.empty();

        private boolean useR2rml = false;
        private boolean isMappingDefined = false;

        /**
         * Default constructor
         */
        protected DefaultOBDACoreBuilderFragment(B builder) {
            this.builder = builder;
        }


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
            return builder;
        }

        @Override
        public B nativeOntopMappingFile(@Nonnull File mappingFile) {
            if (isMappingDefined) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
            declareMappingDefined();
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
            if (isMappingDefined) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
            declareMappingDefined();
            this.mappingReader = Optional.of(mappingReader);
            return builder;
        }

        @Override
        public B r2rmlMappingFile(@Nonnull File mappingFile) {
            if (isMappingDefined) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
            declareMappingDefined();
            useR2rml = true;
            this.mappingFile = Optional.of(mappingFile);
            return builder;
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
                return builder;
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
            return builder;
        }

        @Override
        public B r2rmlMappingGraph(@Nonnull Model rdfGraph) {
            if (isMappingDefined) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
            declareMappingDefined();
            useR2rml = true;
            this.mappingGraph = Optional.of(rdfGraph);
            return builder;
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

        protected Properties generateProperties() {
            Properties p = new Properties();

            // Never puts the mapping file path

            return p;
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

        final OBDAConfigurationOptions generateOBDACoreOptions(OntopMappingSQLOptions mappingSqlOptions) {
            return new OBDAConfigurationOptions(mappingFile, mappingReader, mappingGraph, obdaModel, mappingSqlOptions);
        }
    }

    protected abstract static class OBDACoreConfigurationBuilderMixin<B extends OBDACoreConfiguration.Builder>
            extends OntopMappingSQLBuilderMixin<B>
            implements OBDACoreConfiguration.Builder<B> {

        private final DefaultOBDACoreBuilderFragment<B> obdaBuilderFragment;

        protected OBDACoreConfigurationBuilderMixin() {
            obdaBuilderFragment = new DefaultOBDACoreBuilderFragment<B>((B) this) {
            };
        }

        @Override
        protected Properties generateProperties() {
            Properties properties = super.generateProperties();
            properties.putAll(obdaBuilderFragment.generateProperties());

            return properties;
        }

        protected boolean isR2rml() {
            return obdaBuilderFragment.isR2rml();
        }

        protected final void setMappingFile(String mappingFilename) {
            obdaBuilderFragment.setMappingFile(mappingFilename);
        }

        protected final void declareMappingDefined() {
            obdaBuilderFragment.declareMappingDefined();
        }

        protected final boolean isMappingDefined() {
            return obdaBuilderFragment.isMappingDefined();
        }

        final OBDAConfigurationOptions generateOBDACoreOptions() {
            return obdaBuilderFragment.generateOBDACoreOptions(generateMappingSQLOptions());
        }

        @Override
        public B obdaModel(@Nonnull OBDAModel obdaModel) {
            return obdaBuilderFragment.obdaModel(obdaModel);
        }

        @Override
        public B nativeOntopMappingFile(@Nonnull File mappingFile) {
            return obdaBuilderFragment.nativeOntopMappingFile(mappingFile);
        }

        @Override
        public B nativeOntopMappingFile(@Nonnull String mappingFilename) {
            return obdaBuilderFragment.nativeOntopMappingFile(mappingFilename);
        }

        @Override
        public B nativeOntopMappingReader(@Nonnull Reader mappingReader) {
            return obdaBuilderFragment.nativeOntopMappingReader(mappingReader);
        }

        @Override
        public B r2rmlMappingFile(@Nonnull File mappingFile) {
            return obdaBuilderFragment.r2rmlMappingFile(mappingFile);
        }

        @Override
        public B r2rmlMappingFile(@Nonnull String mappingFilename) {
            return obdaBuilderFragment.r2rmlMappingFile(mappingFilename);
        }

        @Override
        public B r2rmlMappingReader(@Nonnull Reader mappingReader) {
            return obdaBuilderFragment.r2rmlMappingReader(mappingReader);
        }

        @Override
        public B r2rmlMappingGraph(@Nonnull Model rdfGraph) {
            return obdaBuilderFragment.r2rmlMappingGraph(rdfGraph);
        }
    }


    public static class BuilderImpl<B extends OBDACoreConfiguration.Builder>
            extends OBDACoreConfigurationBuilderMixin<B> {

        @Override
        public OBDACoreConfiguration build() {
            Properties properties = generateProperties();

            OBDAConfigurationOptions options = generateOBDACoreOptions();
            OBDASettings settings = new OBDASettingsImpl(properties, isR2rml());

            return new OBDACoreConfigurationImpl(settings, options);
        }
    }

}
