package it.unibz.inf.ontop.injection.impl;


import com.google.inject.Module;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.*;

import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.ontology.Ontology;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
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


    /**
     * TODO: complete
     */
    @Override
    public void validate() throws InvalidOntopConfigurationException {
        // TODO: complete if multiple alternatives for building the OBDAModel are provided
    }

    @Override
    public Optional<OBDASpecification> loadSpecification() throws IOException, OBDASpecificationException {
        return loadSpecification(Optional::empty);
    }

    Optional<OBDASpecification> loadSpecification(Supplier<Optional<Ontology>> ontologySupplier)
            throws IOException, OBDASpecificationException {

        return loadSpecification(ontologySupplier,
                () -> options.mappingFile
                        .map(Optional::of)
                        .orElseGet(() -> settings.getMappingFilePath()
                                .map(File::new)),
                () -> options.mappingReader,
                () -> options.mappingGraph);
    }

    @Override
    public Optional<OBDAModel> loadPPMapping() throws IOException, InvalidMappingException, DuplicateMappingException {
        return loadPPMapping(Optional::empty);
    }

    Optional<OBDAModel> loadPPMapping(Supplier<Optional<Ontology>> ontologySupplier)
            throws IOException, InvalidMappingException, DuplicateMappingException {
        return loadPPMapping(ontologySupplier,
                () -> options.mappingFile
                        .map(Optional::of)
                        .orElseGet(() -> settings.getMappingFilePath()
                                .map(File::new)),
                () -> options.mappingReader,
                () -> options.mappingGraph);
    }

    /**
     * Please overload isMappingDefined() instead.
     */
    @Override
    boolean isInputMappingDefined() {
        return super.isInputMappingDefined()
                || settings.contains(OBDASettings.MAPPING_FILE_PATH)
                || options.mappingFile.isPresent()
                || options.mappingGraph.isPresent()
                || options.mappingReader.isPresent();
    }

    /**
     * To be overloaded
     */
    protected boolean isMappingDefined() {
        return isInputMappingDefined();
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
        public final OntopMappingSQLOptions mappingSqlOptions;

        public OBDAConfigurationOptions(Optional<File> mappingFile, Optional<Reader> mappingReader, Optional<Model> mappingGraph,
                                        OntopMappingSQLOptions mappingSqlOptions) {
            this.mappingFile = mappingFile;
            this.mappingReader = mappingReader;
            this.mappingGraph = mappingGraph;
            this.mappingSqlOptions = mappingSqlOptions;
        }
    }

    protected static class DefaultOBDACoreBuilderFragment<B extends OBDACoreConfiguration.Builder<B>>
            implements OBDACoreBuilderFragment<B> {

        private final B builder;
        private final Supplier<Boolean> isMappingDefinedSupplier;
        private final Runnable declareMappingDefinedCB;

        private Optional<File> mappingFile = Optional.empty();
        private Optional<Reader> mappingReader = Optional.empty();
        private Optional<Model> mappingGraph = Optional.empty();

        private boolean useR2rml = false;

        /**
         * Default constructor
         */
        protected DefaultOBDACoreBuilderFragment(B builder, Supplier<Boolean> isMappingDefinedSupplier,
                                                 Runnable declareMappingDefinedCB) {
            this.builder = builder;
            this.isMappingDefinedSupplier = isMappingDefinedSupplier;
            this.declareMappingDefinedCB = declareMappingDefinedCB;
        }


        @Override
        public B nativeOntopMappingFile(@Nonnull File mappingFile) {
            if (isMappingDefinedSupplier.get()) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
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
            if (isMappingDefinedSupplier.get()) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
            declareMappingDefinedCB.run();
            this.mappingReader = Optional.of(mappingReader);
            return builder;
        }

        @Override
        public B r2rmlMappingFile(@Nonnull File mappingFile) {
            if (isMappingDefinedSupplier.get()) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
            declareMappingDefinedCB.run();
            useR2rml = true;
            this.mappingFile = Optional.of(mappingFile);
            return builder;
        }

        @Override
        public B r2rmlMappingFile(@Nonnull String mappingFilename) {
            if (isMappingDefinedSupplier.get()) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
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
            if (isMappingDefinedSupplier.get()) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
            declareMappingDefinedCB.run();
            useR2rml = true;
            this.mappingReader = Optional.of(mappingReader);
            return builder;
        }

        @Override
        public B r2rmlMappingGraph(@Nonnull Model rdfGraph) {
            if (isMappingDefinedSupplier.get()) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
            declareMappingDefinedCB.run();
            useR2rml = true;
            this.mappingGraph = Optional.of(rdfGraph);
            return builder;
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
            if (isMappingDefinedSupplier.get()) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
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

        final OBDAConfigurationOptions generateOBDACoreOptions(OntopMappingSQLOptions mappingSqlOptions) {
            return new OBDAConfigurationOptions(mappingFile, mappingReader, mappingGraph, mappingSqlOptions);
        }
    }

    protected abstract static class OBDACoreConfigurationBuilderMixin<B extends OBDACoreConfiguration.Builder<B>>
            extends OntopMappingSQLBuilderMixin<B>
            implements OBDACoreConfiguration.Builder<B> {

        private final DefaultOBDACoreBuilderFragment<B> obdaBuilderFragment;

        protected OBDACoreConfigurationBuilderMixin() {
            obdaBuilderFragment = new DefaultOBDACoreBuilderFragment<B>((B) this,
                    this::isMappingDefined,
                    this::declareMappingDefined) {
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

        final OBDAConfigurationOptions generateOBDACoreOptions() {
            return obdaBuilderFragment.generateOBDACoreOptions(generateMappingSQLOptions());
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


    public static class BuilderImpl<B extends OBDACoreConfiguration.Builder<B>>
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
