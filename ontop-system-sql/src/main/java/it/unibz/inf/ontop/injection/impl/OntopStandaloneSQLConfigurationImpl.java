package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Module;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopStandaloneSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopStandaloneSQLSettings;
import it.unibz.inf.ontop.injection.impl.OntopQueryAnsweringConfigurationImpl.DefaultOntopQueryAnsweringBuilderFragment;
import it.unibz.inf.ontop.injection.impl.OntopQueryAnsweringConfigurationImpl.OntopQueryAnsweringOptions;
import it.unibz.inf.ontop.injection.impl.OntopQueryAnsweringSQLConfigurationImpl.DefaultOntopQueryAnsweringSQLBuilderFragment;
import it.unibz.inf.ontop.injection.impl.OntopQueryAnsweringSQLConfigurationImpl.OntopQueryAnsweringSQLOptions;
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
import java.util.stream.Stream;


public class OntopStandaloneSQLConfigurationImpl extends OntopMappingSQLConfigurationImpl
        implements OntopStandaloneSQLConfiguration {

    private final OntopStandaloneSQLSettings settings;
    private final OntopQueryAnsweringConfigurationImpl qaConfiguration;
    private final OntopStandaloneSQLOptions options;

    OntopStandaloneSQLConfigurationImpl(OntopStandaloneSQLSettings settings, OntopStandaloneSQLOptions options) {
        super(settings, options.mappingOptions);
        this.settings = settings;
        qaConfiguration = new OntopQueryAnsweringSQLConfigurationImpl(settings, options.qaOptions);
        this.options = options;
    }

    @Override
    public OntopStandaloneSQLSettings getSettings() {
        return settings;
    }

    @Override
    public Optional<IRIDictionary> getIRIDictionary() {
        return qaConfiguration.getIRIDictionary();
    }

    @Override
    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                super.buildGuiceModules(),
                qaConfiguration.buildGuiceModules());
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



    static class OntopStandaloneSQLOptions {
        private final Optional<File> mappingFile;
        private final Optional<Reader> mappingReader;
        private final Optional<Model> mappingGraph;
        final OntopQueryAnsweringSQLOptions qaOptions;
        final OntopMappingSQLOptions mappingOptions;

        OntopStandaloneSQLOptions(Optional<File> mappingFile, Optional<Reader> mappingReader,
                                  Optional<Model> mappingGraph, OntopQueryAnsweringSQLOptions qaOptions,
                                  OntopMappingSQLOptions mappingOptions) {
            this.mappingFile = mappingFile;
            this.mappingReader = mappingReader;
            this.mappingGraph = mappingGraph;
            this.qaOptions = qaOptions;
            this.mappingOptions = mappingOptions;
        }
    }

    static class StandardStandaloneSQLConfigurationBuilderFragment<B extends OntopStandaloneSQLConfiguration.Builder<B>>
        implements OntopStandaloneSQLConfigurationBuilderFragment<B> {
        private final B builder;
        private final Runnable declareMappingDefinedCB;

        private Optional<File> mappingFile = Optional.empty();
        private Optional<Reader> mappingReader = Optional.empty();
        private Optional<Model> mappingGraph = Optional.empty();

        private boolean useR2rml = false;

        /**
         * Default constructor
         */
        protected StandardStandaloneSQLConfigurationBuilderFragment(B builder, Runnable declareMappingDefinedCB) {
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

        protected boolean isR2rml() {
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

        final OntopStandaloneSQLOptions generateStandaloneSQLOptions(OntopQueryAnsweringSQLOptions qaOptions,
                                                                     OntopMappingSQLOptions mappingOptions) {
            return new OntopStandaloneSQLOptions(mappingFile, mappingReader, mappingGraph, qaOptions, mappingOptions);
        }

    }

    static abstract class OntopStandaloneSQLBuilderMixin<B extends OntopStandaloneSQLConfiguration.Builder<B>>
            extends OntopMappingSQLConfigurationImpl.OntopMappingSQLBuilderMixin<B>
            implements OntopStandaloneSQLConfiguration.Builder<B> {

        private final DefaultOntopQueryAnsweringSQLBuilderFragment<B> sqlQAFragmentBuilder;
        private final DefaultOntopQueryAnsweringBuilderFragment<B> qaFragmentBuilder;
        private final StandardStandaloneSQLConfigurationBuilderFragment<B> localFragmentBuilder;

        OntopStandaloneSQLBuilderMixin() {
            B builder = (B) this;
            this.localFragmentBuilder = new StandardStandaloneSQLConfigurationBuilderFragment<>(builder,
                    this::declareMappingDefined);
                    this.sqlQAFragmentBuilder = new DefaultOntopQueryAnsweringSQLBuilderFragment<>(builder);
            this.qaFragmentBuilder = new DefaultOntopQueryAnsweringBuilderFragment<>(builder);
        }

        @Override
        public B enableIRISafeEncoding(boolean enable) {
            return qaFragmentBuilder.enableIRISafeEncoding(enable);
        }

        @Override
        public B enableExistentialReasoning(boolean enable) {
            return qaFragmentBuilder.enableExistentialReasoning(enable);
        }

        @Override
        public B iriDictionary(@Nonnull IRIDictionary iriDictionary) {
            return qaFragmentBuilder.iriDictionary(iriDictionary);
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

        final OntopStandaloneSQLOptions generateStandaloneSQLOptions() {
            OntopMappingSQLOptions sqlMappingOptions = generateMappingSQLOptions();
            OntopQueryAnsweringOptions qaOptions = this.qaFragmentBuilder.generateQAOptions(
                    sqlMappingOptions.mappingOptions.obdaOptions,
                    sqlMappingOptions.mappingOptions.optimizationOptions);

            return localFragmentBuilder.generateStandaloneSQLOptions(
                    sqlQAFragmentBuilder.generateQASQLOptions(qaOptions, sqlMappingOptions.sqlOptions),
                    sqlMappingOptions);
        }

        @Override
        protected Properties generateProperties() {
            Properties p = super.generateProperties();
            p.putAll(sqlQAFragmentBuilder.generateProperties());
            p.putAll(qaFragmentBuilder.generateProperties());
            p.putAll(localFragmentBuilder.generateProperties());
            return p;
        }

    }

    public static final class BuilderImpl<B extends OntopStandaloneSQLConfiguration.Builder<B>>
            extends OntopStandaloneSQLBuilderMixin<B> {

        @Override
        public OntopStandaloneSQLConfiguration build() {
            OntopStandaloneSQLSettings settings = new OntopStandaloneSQLSettingsImpl(generateProperties());
            OntopStandaloneSQLOptions options = generateStandaloneSQLOptions();
            return new OntopStandaloneSQLConfigurationImpl(settings, options);
        }
    }



}
