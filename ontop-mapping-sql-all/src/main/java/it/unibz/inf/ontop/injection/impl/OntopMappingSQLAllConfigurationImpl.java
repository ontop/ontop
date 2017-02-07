package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllSettings;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.spec.OBDASpecification;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
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
    public Optional<OBDASpecification> loadSpecification() throws IOException, OBDASpecificationException {
        return loadSpecification(Optional::empty);
    }

    Optional<OBDASpecification> loadSpecification(OntologySupplier ontologySupplier)
            throws IOException, OBDASpecificationException {

        return loadSpecification(ontologySupplier,
                () -> options.mappingFile,
                () -> options.mappingReader,
                () -> options.mappingGraph);
    }

    @Override
    public Optional<OBDAModel> loadPPMapping() throws IOException, InvalidMappingException, DuplicateMappingException {
        return loadPPMapping(Optional::empty);
    }

    Optional<OBDAModel> loadPPMapping(OntologySupplier ontologySupplier)
            throws IOException, InvalidMappingException, DuplicateMappingException {
        return loadPPMapping(ontologySupplier,
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
        private final Optional<Model> mappingGraph;
        final OntopMappingSQLOptions mappingSQLOptions;

        OntopMappingSQLAllOptions(Optional<File> mappingFile, Optional<Reader> mappingReader,
                                  Optional<Model> mappingGraph, OntopMappingSQLOptions mappingSQLOptions) {
            this.mappingFile = mappingFile;
            this.mappingReader = mappingReader;
            this.mappingGraph = mappingGraph;
            this.mappingSQLOptions = mappingSQLOptions;
        }
    }


    static class StandardMappingSQLAllBuilderFragment<B extends OntopMappingSQLAllConfiguration.Builder<B>>
            implements OntopMappingSQLAllBuilderFragment<B> {
        private final B builder;
        private final Runnable declareMappingDefinedCB;

        private Optional<File> mappingFile = Optional.empty();
        private Optional<Reader> mappingReader = Optional.empty();
        private Optional<Model> mappingGraph = Optional.empty();

        private boolean useR2rml = false;

        /**
         * Default constructor
         */
        protected StandardMappingSQLAllBuilderFragment(B builder, Runnable declareMappingDefinedCB) {
            this.builder = builder;
            this.declareMappingDefinedCB = declareMappingDefinedCB;
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
        public B r2rmlMappingGraph(@Nonnull Model rdfGraph) {
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
            return new OntopMappingSQLAllOptions(mappingFile, mappingReader, mappingGraph, mappingOptions);
        }

    }

    protected abstract static class OntopMappingSQLAllBuilderMixin<B extends OntopMappingSQLAllConfiguration.Builder<B>>
            extends OntopMappingSQLBuilderMixin<B>
            implements OntopMappingSQLAllConfiguration.Builder<B> {

        private final StandardMappingSQLAllBuilderFragment<B> localFragmentBuilder;

        OntopMappingSQLAllBuilderMixin() {
            B builder = (B) this;
            this.localFragmentBuilder = new StandardMappingSQLAllBuilderFragment<>(builder,
                    this::declareMappingDefined);
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
        public B r2rmlMappingGraph(@Nonnull Model rdfGraph) {
            return localFragmentBuilder.r2rmlMappingGraph(rdfGraph);
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
