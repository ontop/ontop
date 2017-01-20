package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Module;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.mapping.extraction.DataSourceModel;
import it.unibz.inf.ontop.mapping.extraction.DataSourceModelExtractor;
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

    OntopMappingConfigurationImpl(OntopMappingSettings settings, OntopMappingOptions options) {
        super(settings, options.obdaOptions);
        this.settings = settings;
        this.options = options;
    }

    @Override
    public Optional<ImplicitDBConstraintsReader> getImplicitDBConstraintsReader() {
        return options.implicitDBConstraintsReader;
    }

    @Override
    public OntopMappingSettings getSettings() {
        return settings;
    }

    /**
     * Can be overloaded.
     * However, the expected usage is to use the other method loadDataSourceModel(...).
     */
    @Override
    public Optional<DataSourceModel> loadDataSourceModel() throws IOException, InvalidMappingException {
        return loadDataSourceModel(
                Optional::empty,
                Optional::empty,
                Optional::empty,
                Optional::empty,
                Optional::empty
                );
    }

    Optional<DataSourceModel> loadDataSourceModel(Supplier<Optional<Ontology>> ontologySupplier,
                                                  Supplier<Optional<PreProcessedMapping>> ppMappingSupplier,
                                                  Supplier<Optional<File>> mappingFileSupplier,
                                                  Supplier<Optional<Reader>> mappingReaderSupplier,
                                                  Supplier<Optional<Model>> mappingGraphSupplier
                                                  ) throws IOException, InvalidMappingException {
        DataSourceModelExtractor extractor = getInjector().getInstance(DataSourceModelExtractor.class);

        /*
         * Pre-defined DataSourceModel
         */
        if (options.dataSourceModel.isPresent())
            return options.dataSourceModel;

         Optional<Ontology> optionalOntology= ontologySupplier.get();

        Optional<DBMetadata> optionalMetadata = getPredefinedDBMetadata();

        /*
         * Pre-processed mapping
         */
        Optional<PreProcessedMapping> optionalPPMapping = ppMappingSupplier.get();
        if (optionalPPMapping.isPresent()) {
            PreProcessedMapping ppMapping = optionalPPMapping.get();
            return Optional.of(extractor.extract(ppMapping, optionalMetadata, optionalOntology));
        }

        /*
         * Mapping file
         */
        Optional<File> optionalMappingFile = mappingFileSupplier.get();
        if (optionalMappingFile.isPresent()) {
            File mappingFile = optionalMappingFile.get();
            return Optional.of(extractor.extract(mappingFile, optionalMetadata, optionalOntology));
        }

        /*
         * Reader
         */
        Optional<Reader> optionalMappingReader = mappingReaderSupplier.get();
        if (optionalMappingReader.isPresent()) {
            Reader mappingReader = optionalMappingReader.get();
            return Optional.of(extractor.extract(mappingReader, optionalMetadata, optionalOntology));
        }

        /*
         * Graph
         */
        Optional<Model> optionalMappingGraph = mappingGraphSupplier.get();
        if (optionalMappingGraph.isPresent()) {
            Model mappingGraph = optionalMappingGraph.get();
            return Optional.of(extractor.extract(mappingGraph, optionalMetadata, optionalOntology));
        }

        return Optional.empty();
    }

    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                super.buildGuiceModules(),
                Stream.of(new OntopMappingModule(this)));
    }

    static class OntopMappingOptions {

        final OntopOBDAOptions obdaOptions;
        private final Optional<ImplicitDBConstraintsReader> implicitDBConstraintsReader;
        private final Optional<DataSourceModel> dataSourceModel;

        private OntopMappingOptions(Optional<DataSourceModel> dataSourceModel,
                                    Optional<ImplicitDBConstraintsReader> implicitDBConstraintsReader,
                                    OntopOBDAOptions obdaOptions) {
            this.dataSourceModel = dataSourceModel;
            this.implicitDBConstraintsReader = implicitDBConstraintsReader;
            this.obdaOptions = obdaOptions;
        }
    }

    static class DefaultOntopMappingBuilderFragment<B extends OntopMappingConfiguration.Builder<B>>
            implements OntopMappingBuilderFragment<B> {

        private final B builder;
        private final Supplier<Boolean> isMappingDefinedSupplier;
        private final Runnable declareMappingDefinedCB;
        private Optional<ImplicitDBConstraintsReader> userConstraints = Optional.empty();
        private Optional<DataSourceModel> dataSourceModel = Optional.empty();
        private Optional<Boolean> obtainFullMetadata = Optional.empty();
        private Optional<Boolean> queryingAnnotationsInOntology = Optional.empty();

        DefaultOntopMappingBuilderFragment(B builder,
                                           Supplier<Boolean> isMappingDefinedSupplier,
                                           Runnable declareMappingDefinedCB) {
            this.isMappingDefinedSupplier = isMappingDefinedSupplier;
            this.declareMappingDefinedCB = declareMappingDefinedCB;
            this.builder = builder;
        }


        @Override
        public B dataSourceModel(@Nonnull DataSourceModel dataSourceModel) {
            if (isMappingDefinedSupplier.get()) {
                throw new InvalidOntopConfigurationException("Mapping already defined!");
            }
            declareMappingDefinedCB.run();
            this.dataSourceModel = Optional.of(dataSourceModel);
            return builder;
        }

        @Override
        public B dbConstraintsReader(@Nonnull ImplicitDBConstraintsReader constraints) {
            this.userConstraints = Optional.of(constraints);
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

        final OntopMappingOptions generateMappingOptions(OntopOBDAOptions obdaOptions) {
            return new OntopMappingOptions(dataSourceModel, userConstraints, obdaOptions);
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
        private boolean isMappingDefined;

        OntopMappingBuilderMixin() {
            this.mappingBuilderFragment = new DefaultOntopMappingBuilderFragment<>((B)this,
                    this::isMappingDefined,
                    this::declareMappingDefined);
        }

        @Override
        public B dataSourceModel(@Nonnull DataSourceModel dataSourceModel) {
            return mappingBuilderFragment.dataSourceModel(dataSourceModel);
        }

        @Override
        public B dbConstraintsReader(@Nonnull ImplicitDBConstraintsReader constraints) {
            return mappingBuilderFragment.dbConstraintsReader(constraints);
        }

        @Override
        public B enableFullMetadataExtraction(boolean obtainFullMetadata) {
            return mappingBuilderFragment.enableFullMetadataExtraction(obtainFullMetadata);
        }

        @Override
        public B enableOntologyAnnotationQuerying(boolean queryingAnnotationsInOntology) {
            return mappingBuilderFragment.enableOntologyAnnotationQuerying(queryingAnnotationsInOntology);
        }

        final OntopMappingOptions generateMappingOptions() {
            return generateMappingOptions(generateOBDAOptions());
        }

        final OntopMappingOptions generateMappingOptions(OntopOBDAOptions obdaOptions) {
            return mappingBuilderFragment.generateMappingOptions(obdaOptions);
        }

        @Override
        protected Properties generateProperties() {
            Properties properties = new Properties();
            properties.putAll(super.generateProperties());
            properties.putAll(mappingBuilderFragment.generateProperties());

            return properties;
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
