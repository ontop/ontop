package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import com.google.inject.Module;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.MissingInputMappingException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.executor.ProposalExecutor;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.impl.OntopOptimizationConfigurationImpl.DefaultOntopOptimizationBuilderFragment;
import it.unibz.inf.ontop.injection.impl.OntopOptimizationConfigurationImpl.OntopOptimizationOptions;
import it.unibz.inf.ontop.iq.proposal.QueryOptimizationProposal;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.OBDASpecificationExtractor;
import org.apache.commons.rdf.api.Graph;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;


public class OntopMappingConfigurationImpl extends OntopOBDAConfigurationImpl implements OntopMappingConfiguration {

    private final OntopMappingSettings settings;
    private final OntopMappingOptions options;
    private final OntopOptimizationConfigurationImpl optimizationConfiguration;

    OntopMappingConfigurationImpl(OntopMappingSettings settings, OntopMappingOptions options) {
        super(settings, options.obdaOptions);
        this.settings = settings;
        this.options = options;
        this.optimizationConfiguration = new OntopOptimizationConfigurationImpl(settings, options.optimizationOptions);
    }

    OntopMappingConfigurationImpl(OntopMappingSettings settings, OntopMappingOptions options,
                                  Supplier<Injector> injectorSupplier) {
        super(settings, options.obdaOptions, injectorSupplier);
        this.settings = settings;
        this.options = options;
        this.optimizationConfiguration = new OntopOptimizationConfigurationImpl(settings, options.optimizationOptions);
    }

    @Override
    public Optional<TMappingExclusionConfig> getTmappingExclusions() {
        return options.excludeFromTMappings;
    }

    @Override
    public OntopMappingSettings getSettings() {
        return settings;
    }

    /**
     * Can be overloaded by sub-classes
     */
    @Override
    protected ImmutableMap<Class<? extends QueryOptimizationProposal>, Class<? extends ProposalExecutor>>
    generateOptimizationConfigurationMap() {
        ImmutableMap.Builder<Class<? extends QueryOptimizationProposal>, Class<? extends ProposalExecutor>>
                internalExecutorMapBuilder = ImmutableMap.builder();
        internalExecutorMapBuilder.putAll(super.generateOptimizationConfigurationMap());
        internalExecutorMapBuilder.putAll(optimizationConfiguration.generateOptimizationConfigurationMap());

        return internalExecutorMapBuilder.build();
    }

    /**
     * Can be overloaded.
     * However, the expected usage is to use the other method loadSpecification(...).
     */
    @Override
    public OBDASpecification loadSpecification() throws OBDASpecificationException {
        return loadSpecification(
                Optional::empty,
                Optional::empty,
                Optional::empty,
                Optional::empty,
                Optional::empty,
                Optional::empty
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
        Optional<DBMetadata> optionalMetadata = options.dbMetadata;

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

    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                Stream.concat(
                        super.buildGuiceModules(),
                        optimizationConfiguration.buildGuiceModules()),
                Stream.of(new OntopMappingModule(this)));
    }

    static class OntopMappingOptions {

        final OntopOBDAOptions obdaOptions;
        final OntopOptimizationOptions optimizationOptions;
        private final Optional<TMappingExclusionConfig> excludeFromTMappings;
        final Optional<DBMetadata> dbMetadata;

        private OntopMappingOptions(Optional<DBMetadata> dbMetadata,
                                    Optional<TMappingExclusionConfig> excludeFromTMappings,
                                    OntopOBDAOptions obdaOptions, OntopOptimizationOptions optimizationOptions) {
            this.excludeFromTMappings = excludeFromTMappings;
            this.obdaOptions = obdaOptions;
            this.optimizationOptions = optimizationOptions;
            this.dbMetadata = dbMetadata;
        }
    }

    static class DefaultOntopMappingBuilderFragment<B extends OntopMappingConfiguration.Builder<B>>
            implements OntopMappingBuilderFragment<B> {

        private final B builder;
        private Optional<Boolean> obtainFullMetadata = Optional.empty();
        private Optional<Boolean> queryingAnnotationsInOntology = Optional.empty();
        private Optional<Boolean> completeDBMetadata = Optional.empty();
        private Optional<DBMetadata> dbMetadata = Optional.empty();
        private Optional<TMappingExclusionConfig> excludeFromTMappings = Optional.empty();

        DefaultOntopMappingBuilderFragment(B builder) {
            this.builder = builder;
        }

        @Override
        public B tMappingExclusionConfig(@Nonnull TMappingExclusionConfig config) {
            this.excludeFromTMappings = Optional.of(config);
            return builder;
        }

        @Override
        public B enableFullMetadataExtraction(boolean obtainFullMetadata) {
            this.obtainFullMetadata = Optional.of(obtainFullMetadata);
            return builder;
        }

        @Override
        public B enableOntologyAnnotationQuerying(boolean queryingAnnotationsInOntology) {
            this.queryingAnnotationsInOntology = Optional.of(queryingAnnotationsInOntology);
            return builder;
        }

        @Override
        public B enableProvidedDBMetadataCompletion(boolean dbMetadataCompletion) {
            this.completeDBMetadata = Optional.of(dbMetadataCompletion);
            return builder;
        }

        @Override
        public B dbMetadata(@Nonnull DBMetadata dbMetadata) {
            this.dbMetadata = Optional.of(dbMetadata);
            return builder;
        }

        final OntopMappingOptions generateMappingOptions(OntopOBDAOptions obdaOptions,
                                                         OntopOptimizationOptions optimizationOptions) {
            return new OntopMappingOptions(dbMetadata, excludeFromTMappings, obdaOptions, optimizationOptions);
        }

        Properties generateProperties() {
            Properties properties = new Properties();
            obtainFullMetadata.ifPresent(m -> properties.put(OntopMappingSettings.OBTAIN_FULL_METADATA, m));
            queryingAnnotationsInOntology.ifPresent(b -> properties.put(OntopMappingSettings.QUERY_ONTOLOGY_ANNOTATIONS, b));
            completeDBMetadata.ifPresent(b -> properties.put(OntopMappingSettings.COMPLETE_PROVIDED_METADATA, b));
            return properties;
        }

    }

    static abstract class OntopMappingBuilderMixin<B extends OntopMappingConfiguration.Builder<B>>
        extends OntopOBDAConfigurationBuilderMixin<B>
        implements OntopMappingConfiguration.Builder<B> {

        private final DefaultOntopMappingBuilderFragment<B> mappingBuilderFragment;
        private final DefaultOntopOptimizationBuilderFragment<B> optimizationBuilderFragment;
        private boolean isMappingDefined;

        OntopMappingBuilderMixin() {
            B builder = (B) this;
            this.mappingBuilderFragment = new DefaultOntopMappingBuilderFragment<>(builder);
            this.optimizationBuilderFragment = new DefaultOntopOptimizationBuilderFragment<>(builder);
        }

        @Override
        public B tMappingExclusionConfig(@Nonnull TMappingExclusionConfig config) {
            return mappingBuilderFragment.tMappingExclusionConfig(config);
        }

        @Override
        public B enableFullMetadataExtraction(boolean obtainFullMetadata) {
            return mappingBuilderFragment.enableFullMetadataExtraction(obtainFullMetadata);
        }

        @Override
        public B enableOntologyAnnotationQuerying(boolean queryingAnnotationsInOntology) {
            return mappingBuilderFragment.enableOntologyAnnotationQuerying(queryingAnnotationsInOntology);
        }

        @Override
        public B enableProvidedDBMetadataCompletion(boolean dbMetadataCompletion) {
            return mappingBuilderFragment.enableProvidedDBMetadataCompletion(dbMetadataCompletion);
        }

        @Override
        public B dbMetadata(@Nonnull DBMetadata dbMetadata) {
            return mappingBuilderFragment.dbMetadata(dbMetadata);
        }

        final OntopMappingOptions generateMappingOptions() {
            return generateMappingOptions(generateOBDAOptions());
        }

        final OntopMappingOptions generateMappingOptions(OntopOBDAOptions obdaOptions) {
            return generateMappingOptions(obdaOptions, optimizationBuilderFragment.generateOptimizationOptions(
                    obdaOptions.modelOptions));
        }

        final OntopMappingOptions generateMappingOptions(OntopOBDAOptions obdaOptions,
                                                         OntopOptimizationOptions optimizationOptions) {
            return mappingBuilderFragment.generateMappingOptions(obdaOptions, optimizationOptions);
        }

        @Override
        protected Properties generateProperties() {
            Properties properties = new Properties();
            properties.putAll(super.generateProperties());
            properties.putAll(mappingBuilderFragment.generateProperties());
            properties.putAll(optimizationBuilderFragment.generateProperties());

            return properties;
        }

        /**
         * Allows to detect double mapping definition (error).
         */
        protected final void declareMappingDefined() {
            if (isMappingDefined)
                throw new InvalidOntopConfigurationException("The mapping is already defined");
            isMappingDefined = true;
        }

        protected final boolean isMappingDefined() {
            return isMappingDefined;
        }

    }

    public static class BuilderImpl<B extends OntopMappingConfiguration.Builder<B>>
            extends OntopMappingBuilderMixin<B> {

        @Override
        public OntopMappingConfiguration build() {
            Properties properties = generateProperties();
            OntopMappingSettings settings = new OntopMappingSettingsImpl(properties);

            OntopMappingOptions options = generateMappingOptions();

            return new OntopMappingConfigurationImpl(settings, options);
        }

    }

}
