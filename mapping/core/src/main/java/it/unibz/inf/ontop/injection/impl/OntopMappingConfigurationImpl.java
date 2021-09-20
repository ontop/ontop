package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import com.google.inject.Module;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.exception.MissingInputMappingException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.impl.OntopOptimizationConfigurationImpl.DefaultOntopOptimizationBuilderFragment;
import it.unibz.inf.ontop.injection.impl.OntopOptimizationConfigurationImpl.OntopOptimizationOptions;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.OBDASpecificationExtractor;
import it.unibz.inf.ontop.spec.mapping.TMappingExclusionConfig;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.ontology.Ontology;
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
     * Can be overloaded.
     * However, the expected usage is to use the other method loadSpecification(...).
     */
    @Override
    protected OBDASpecification loadOBDASpecification() throws OBDASpecificationException {
        return loadSpecification(
                Optional::empty,
                Optional::empty,
                Optional::empty,
                Optional::empty,
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
                                                  Supplier<Optional<File>> constraintFileSupplier,
                                                  Supplier<Optional<File>> dbMetadataFileSupplier,
                                                  Supplier<Optional<Reader>> dbMetadataReaderSupplier,
                                                  Supplier<Optional<File>> ontopViewFileSupplier,
                                                  Supplier<Optional<Reader>> ontopViewReaderSupplier
                                                  ) throws OBDASpecificationException {
        OBDASpecificationExtractor extractor = getInjector().getInstance(OBDASpecificationExtractor.class);

        Optional<Ontology> optionalOntology = ontologySupplier.get();

        /*
         * Pre-processed mapping
         */
        Optional<PreProcessedMapping> optionalPPMapping = ppMappingSupplier.get();

        OBDASpecInput.Builder specInputBuilder = OBDASpecInput.defaultBuilder();
        constraintFileSupplier.get()
                .ifPresent(specInputBuilder::addConstraintFile);
        dbMetadataFileSupplier.get()
                .ifPresent(specInputBuilder::addDBMetadataFile);
        dbMetadataReaderSupplier.get()
                .ifPresent(specInputBuilder::addDBMetadataReader);
        ontopViewFileSupplier.get()
                .ifPresent(specInputBuilder::addOntopViewFile);
        ontopViewReaderSupplier.get()
                .ifPresent(specInputBuilder::addOntopViewReader);

        if (optionalPPMapping.isPresent()) {
            PreProcessedMapping ppMapping = optionalPPMapping.get();

            return extractor.extract(specInputBuilder.build(), ppMapping, optionalOntology);
        }

        /*
         * Mapping file
         */
        Optional<File> optionalMappingFile = mappingFileSupplier.get();
        if (optionalMappingFile.isPresent()) {
            specInputBuilder.addMappingFile(optionalMappingFile.get());

            return extractor.extract(specInputBuilder.build(), optionalOntology);
        }

        /*
         * Reader
         */
        Optional<Reader> optionalMappingReader = mappingReaderSupplier.get();
        if (optionalMappingReader.isPresent()) {
            specInputBuilder.addMappingReader(optionalMappingReader.get());

            return extractor.extract(specInputBuilder.build(), optionalOntology);
        }

        /*
         * Graph
         */
        Optional<Graph> optionalMappingGraph = mappingGraphSupplier.get();
        if (optionalMappingGraph.isPresent()) {
            specInputBuilder.addMappingGraph(optionalMappingGraph.get());

            return extractor.extract(specInputBuilder.build(), optionalOntology);
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

        private OntopMappingOptions(Optional<TMappingExclusionConfig> excludeFromTMappings,
                                    OntopOBDAOptions obdaOptions, OntopOptimizationOptions optimizationOptions) {
            this.excludeFromTMappings = excludeFromTMappings;
            this.obdaOptions = obdaOptions;
            this.optimizationOptions = optimizationOptions;
        }
    }

    static class DefaultOntopMappingBuilderFragment<B extends OntopMappingConfiguration.Builder<B>>
            implements OntopMappingBuilderFragment<B> {

        private final B builder;
        private Optional<Boolean> queryingAnnotationsInOntology = Optional.empty();
        private Optional<Boolean> inferDefaultDatatype =  Optional.empty();
        private Optional<TMappingExclusionConfig> excludeFromTMappings = Optional.empty();

        DefaultOntopMappingBuilderFragment(B builder, Runnable declareDBMetadataCB) {
            this.builder = builder;
        }

        @Override
        public B tMappingExclusionConfig(@Nonnull TMappingExclusionConfig config) {
            this.excludeFromTMappings = Optional.of(config);
            return builder;
        }

        @Override
        public B enableOntologyAnnotationQuerying(boolean queryingAnnotationsInOntology) {
            this.queryingAnnotationsInOntology = Optional.of(queryingAnnotationsInOntology);
            return builder;
        }

        @Override
        public B enableDefaultDatatypeInference(boolean inferDefaultDatatype) {
            this.inferDefaultDatatype = Optional.of(inferDefaultDatatype);
            return builder;
        }

        final OntopMappingOptions generateMappingOptions(OntopOBDAOptions obdaOptions,
                                                         OntopOptimizationOptions optimizationOptions) {
            return new OntopMappingOptions(excludeFromTMappings, obdaOptions, optimizationOptions);
        }

        Properties generateProperties() {
            Properties properties = new Properties();
            queryingAnnotationsInOntology.ifPresent(b -> properties.put(OntopMappingSettings.QUERY_ONTOLOGY_ANNOTATIONS, b));
            inferDefaultDatatype.ifPresent(b -> properties.put(OntopMappingSettings.INFER_DEFAULT_DATATYPE, b));

            return properties;
        }

    }

    static abstract class OntopMappingBuilderMixin<B extends OntopMappingConfiguration.Builder<B>>
        extends OntopOBDAConfigurationBuilderMixin<B>
        implements OntopMappingConfiguration.Builder<B> {

        private final DefaultOntopMappingBuilderFragment<B> mappingBuilderFragment;
        private final DefaultOntopOptimizationBuilderFragment<B> optimizationBuilderFragment;
        private final DefaultOntopModelBuilderFragment<B> modelBuilderFragment;
        private boolean isMappingDefined;
        private boolean isDBMetadataDefined;
        private boolean isOntopViewDefined;

        OntopMappingBuilderMixin() {
            B builder = (B) this;
            this.mappingBuilderFragment = new DefaultOntopMappingBuilderFragment<>(builder,
                    this::declareDBMetadataDefined);
            this.optimizationBuilderFragment = new DefaultOntopOptimizationBuilderFragment<>(builder);
            this.modelBuilderFragment = new DefaultOntopModelBuilderFragment<>(builder);
            this.isMappingDefined = false;
            this.isDBMetadataDefined = false;
        }

        @Override
        public B tMappingExclusionConfig(@Nonnull TMappingExclusionConfig config) {
            return mappingBuilderFragment.tMappingExclusionConfig(config);
        }

        @Override
        public B enableOntologyAnnotationQuerying(boolean queryingAnnotationsInOntology) {
            return mappingBuilderFragment.enableOntologyAnnotationQuerying(queryingAnnotationsInOntology);
        }

        @Override
        public B enableDefaultDatatypeInference(boolean inferDefaultDatatype) {
            return mappingBuilderFragment.enableDefaultDatatypeInference(inferDefaultDatatype);
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
            properties.putAll(modelBuilderFragment.generateProperties());
            properties.putAll(mappingBuilderFragment.generateProperties());
            properties.putAll(optimizationBuilderFragment.generateProperties());

            return properties;
        }

        final void declareDBMetadataDefined() {
            if (isOBDASpecificationAssigned()) {
                throw new InvalidOntopConfigurationException("The OBDA specification has already been assigned");
            }
            isDBMetadataDefined = true;
        }

        final void declareOntopViewDefined() {
            if (isOBDASpecificationAssigned()) {
                throw new InvalidOntopConfigurationException("The OBDA specification has already been assigned");
            }
            isOntopViewDefined = true;
        }

        @Override
        void declareOBDASpecificationAssigned() {
            super.declareOBDASpecificationAssigned();

            if (isDBMetadataDefined) {
                throw new InvalidOntopConfigurationException("DBMetadata is already defined, " +
                        "cannot assign the OBDA specification");
            }
            if (isMappingDefined()) {
                throw new InvalidOntopConfigurationException("The mapping is already defined, " +
                        "cannot assign the OBDA specification");
            }
            if (isOntopViewDefined) {
                throw new InvalidOntopConfigurationException("Ontop views are already defined, " +
                        "cannot assign the OBDA specification");
            }
        }

        /**
         * Allows to detect double mapping definition (error).
         */
        protected final void declareMappingDefined() {
            if (isOBDASpecificationAssigned())
                throw new InvalidOntopConfigurationException("The OBDA specification has already been assigned");

            if (isMappingDefined)
                throw new InvalidOntopConfigurationException("The mapping is already defined");
            isMappingDefined = true;
        }

        protected final boolean isMappingDefined() {
            return isMappingDefined;
        }

        @Override
        public B properties(@Nonnull Properties properties) {
            return modelBuilderFragment.properties(properties);
        }

        @Override
        public B propertyFile(String propertyFilePath) {
            return modelBuilderFragment.propertyFile(propertyFilePath);
        }

        @Override
        public B propertyFile(File propertyFile) {
            return modelBuilderFragment.propertyFile(propertyFile);
        }

        @Override
        public B enableTestMode() {
            return modelBuilderFragment.enableTestMode();
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
