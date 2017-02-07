package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Injector;
import com.google.inject.Module;
import it.unibz.inf.ontop.injection.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.impl.OntopOptimizationConfigurationImpl.DefaultOntopOptimizationBuilderFragment;
import it.unibz.inf.ontop.injection.impl.OntopOptimizationConfigurationImpl.OntopOptimizationOptions;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.spec.OBDASpecificationExtractor;
import it.unibz.inf.ontop.mapping.extraction.PreProcessedMapping;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.sql.ImplicitDBConstraintsReader;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
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
    public Optional<ImplicitDBConstraintsReader> getImplicitDBConstraintsReader() {
        return options.implicitDBConstraintsReader;
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
    public Optional<OBDASpecification> loadSpecification() throws IOException, OBDASpecificationException {
        return loadSpecification(
                Optional::empty,
                Optional::empty,
                Optional::empty,
                Optional::empty,
                Optional::empty
                );
    }

    Optional<OBDASpecification> loadSpecification(OntologySupplier ontologySupplier,
                                                  Supplier<Optional<PreProcessedMapping>> ppMappingSupplier,
                                                  Supplier<Optional<File>> mappingFileSupplier,
                                                  Supplier<Optional<Reader>> mappingReaderSupplier,
                                                  Supplier<Optional<Model>> mappingGraphSupplier
                                                  ) throws IOException, OBDASpecificationException {
        OBDASpecificationExtractor extractor = getInjector().getInstance(OBDASpecificationExtractor.class);

         Optional<Ontology> optionalOntology = ontologySupplier.get();

        Optional<DBMetadata> optionalMetadata = options.dbMetadata;

        /*
         * Pre-processed mapping
         */
        Optional<PreProcessedMapping> optionalPPMapping = ppMappingSupplier.get();
        if (optionalPPMapping.isPresent()) {
            PreProcessedMapping ppMapping = optionalPPMapping.get();
            return Optional.of(extractor.extract(ppMapping, optionalMetadata, optionalOntology, getExecutorRegistry()));
        }

        /*
         * Mapping file
         */
        Optional<File> optionalMappingFile = mappingFileSupplier.get();
        if (optionalMappingFile.isPresent()) {
            File mappingFile = optionalMappingFile.get();
            return Optional.of(extractor.extract(mappingFile, optionalMetadata, optionalOntology, getExecutorRegistry()));
        }

        /*
         * Reader
         */
        Optional<Reader> optionalMappingReader = mappingReaderSupplier.get();
        if (optionalMappingReader.isPresent()) {
            Reader mappingReader = optionalMappingReader.get();
            return Optional.of(extractor.extract(mappingReader, optionalMetadata, optionalOntology, getExecutorRegistry()));
        }

        /*
         * Graph
         */
        Optional<Model> optionalMappingGraph = mappingGraphSupplier.get();
        if (optionalMappingGraph.isPresent()) {
            Model mappingGraph = optionalMappingGraph.get();
            return Optional.of(extractor.extract(mappingGraph, optionalMetadata, optionalOntology, getExecutorRegistry()));
        }

        return Optional.empty();
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
        private final Optional<ImplicitDBConstraintsReader> implicitDBConstraintsReader;
        private final Optional<TMappingExclusionConfig> excludeFromTMappings;
        final Optional<DBMetadata> dbMetadata;

        private OntopMappingOptions(Optional<ImplicitDBConstraintsReader> implicitDBConstraintsReader,
                                    Optional<DBMetadata> dbMetadata,
                                    Optional<TMappingExclusionConfig> excludeFromTMappings,
                                    OntopOBDAOptions obdaOptions, OntopOptimizationOptions optimizationOptions) {
            this.implicitDBConstraintsReader = implicitDBConstraintsReader;
            this.excludeFromTMappings = excludeFromTMappings;
            this.obdaOptions = obdaOptions;
            this.optimizationOptions = optimizationOptions;
            this.dbMetadata = dbMetadata;
        }
    }

    static class DefaultOntopMappingBuilderFragment<B extends OntopMappingConfiguration.Builder<B>>
            implements OntopMappingBuilderFragment<B> {

        private final B builder;
        private Optional<ImplicitDBConstraintsReader> userConstraints = Optional.empty();
        private Optional<Boolean> obtainFullMetadata = Optional.empty();
        private Optional<Boolean> queryingAnnotationsInOntology = Optional.empty();
        private Optional<DBMetadata> dbMetadata = Optional.empty();
        private Optional<TMappingExclusionConfig> excludeFromTMappings = Optional.empty();

        DefaultOntopMappingBuilderFragment(B builder) {
            this.builder = builder;
        }

        @Override
        public B dbConstraintsReader(@Nonnull ImplicitDBConstraintsReader constraints) {
            this.userConstraints = Optional.of(constraints);
            return builder;
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
        public B dbMetadata(@Nonnull DBMetadata dbMetadata) {
            this.dbMetadata = Optional.of(dbMetadata);
            return builder;
        }

        final OntopMappingOptions generateMappingOptions(OntopOBDAOptions obdaOptions,
                                                         OntopOptimizationOptions optimizationOptions) {
            return new OntopMappingOptions(userConstraints, dbMetadata, excludeFromTMappings, obdaOptions,
                    optimizationOptions);
        }

        Properties generateProperties() {
            Properties properties = new Properties();
            obtainFullMetadata.ifPresent(m -> properties.put(OntopMappingSettings.OBTAIN_FULL_METADATA, m));
            queryingAnnotationsInOntology.ifPresent(b -> properties.put(OntopMappingSettings.QUERY_ONTOLOGY_ANNOTATIONS, b));
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
        public B dbConstraintsReader(@Nonnull ImplicitDBConstraintsReader constraints) {
            return mappingBuilderFragment.dbConstraintsReader(constraints);
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
