package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.google.inject.Module;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.exception.MissingInputMappingException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.OBDASpecificationExtractor;
import it.unibz.inf.ontop.spec.mapping.TMappingExclusionConfig;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedTriplesMap;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import org.apache.commons.rdf.api.Graph;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;


public class OntopMappingConfigurationImpl extends OntopKGQueryConfigurationImpl implements OntopMappingConfiguration {

    private final OntopMappingSettings settings;
    private final OntopMappingOptions options;

    OntopMappingConfigurationImpl(OntopMappingSettings settings, OntopMappingOptions options) {
        super(settings, options.queryOptions);
        this.settings = settings;
        this.options = options;
    }

    OntopMappingConfigurationImpl(OntopMappingSettings settings, OntopMappingOptions options,
                                  Supplier<Injector> injectorSupplier) {
        super(settings, options.queryOptions, injectorSupplier);
        this.settings = settings;
        this.options = options;
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
                Optional::empty,
                Optional::empty
                );
    }

    OBDASpecification loadSpecification(OntologySupplier ontologySupplier,
                                        FactsSupplier factsSupplier,
                                                  Supplier<Optional<? extends PreProcessedMapping<? extends PreProcessedTriplesMap>>> ppMappingSupplier,
                                                  Supplier<Optional<File>> mappingFileSupplier,
                                                  Supplier<Optional<Reader>> mappingReaderSupplier,
                                                  Supplier<Optional<Graph>> mappingGraphSupplier,
                                                  Supplier<Optional<File>> constraintFileSupplier,
                                                  Supplier<Optional<File>> dbMetadataFileSupplier,
                                                  Supplier<Optional<Reader>> dbMetadataReaderSupplier,
                                                  Supplier<Optional<File>> lensesSupplier,
                                                  Supplier<Optional<Reader>> lensesReaderSupplier
                                                  ) throws OBDASpecificationException {
        OBDASpecificationExtractor extractor = getInjector().getInstance(OBDASpecificationExtractor.class);

        Optional<Ontology> optionalOntology = ontologySupplier.get();
        ImmutableSet<RDFFact> optionalFacts = factsSupplier.get().orElse(ImmutableSet.of());

        /*
         * Pre-processed mapping
         */
        Optional<? extends PreProcessedMapping<? extends PreProcessedTriplesMap>> optionalPPMapping = ppMappingSupplier.get();

        OBDASpecInput.Builder specInputBuilder = OBDASpecInput.defaultBuilder();
        constraintFileSupplier.get()
                .ifPresent(specInputBuilder::addConstraintFile);
        dbMetadataFileSupplier.get()
                .ifPresent(specInputBuilder::addDBMetadataFile);
        dbMetadataReaderSupplier.get()
                .ifPresent(specInputBuilder::addDBMetadataReader);
        lensesSupplier.get()
                .ifPresent(specInputBuilder::addLensesFile);
        lensesReaderSupplier.get()
                .ifPresent(specInputBuilder::addLensesReader);
        options.sparqlRulesFile
                .ifPresent(specInputBuilder::addSparqlRuleFile);
        options.sparqlRulesReader
                .ifPresent(specInputBuilder::addSparqlRuleReader);

        if (optionalPPMapping.isPresent()) {
            PreProcessedMapping<? extends PreProcessedTriplesMap> ppMapping = optionalPPMapping.get();
            return extractor.extract(specInputBuilder.build(), ppMapping, optionalOntology, optionalFacts);
        }

        /*
         * Mapping file
         */
        Optional<File> optionalMappingFile = mappingFileSupplier.get();
        if (optionalMappingFile.isPresent()) {
            specInputBuilder.addMappingFile(optionalMappingFile.get());

            return extractor.extract(specInputBuilder.build(), optionalOntology, optionalFacts);
        }

        /*
         * Reader
         */
        Optional<Reader> optionalMappingReader = mappingReaderSupplier.get();
        if (optionalMappingReader.isPresent()) {
            specInputBuilder.addMappingReader(optionalMappingReader.get());

            return extractor.extract(specInputBuilder.build(), optionalOntology, optionalFacts);
        }

        /*
         * Graph
         */
        Optional<Graph> optionalMappingGraph = mappingGraphSupplier.get();
        if (optionalMappingGraph.isPresent()) {
            specInputBuilder.addMappingGraph(optionalMappingGraph.get());

            return extractor.extract(specInputBuilder.build(), optionalOntology, optionalFacts);
        }

        throw new MissingInputMappingException();
    }

    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                super.buildGuiceModules(),
                Stream.of(new OntopMappingModule(this)));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static class OntopMappingOptions {

        final OntopKGQueryOptions queryOptions;
        private final Optional<TMappingExclusionConfig> excludeFromTMappings;

        final Optional<File> sparqlRulesFile;

        final Optional<Reader> sparqlRulesReader;

        private OntopMappingOptions(Optional<TMappingExclusionConfig> excludeFromTMappings,
                                    Optional<File> sparqlRulesFile, Optional<Reader> sparqlRulesReader, OntopKGQueryOptions queryOptions) {
            this.excludeFromTMappings = excludeFromTMappings;
            this.queryOptions = queryOptions;
            this.sparqlRulesFile = sparqlRulesFile;
            this.sparqlRulesReader = sparqlRulesReader;
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected static abstract class DefaultOntopMappingBuilderFragment<B extends OntopMappingConfiguration.Builder<B>>
            implements OntopMappingBuilderFragment<B> {

        private Optional<Boolean> queryingAnnotationsInOntology = Optional.empty();
        private Optional<Boolean> inferDefaultDatatype =  Optional.empty();
        private Optional<TMappingExclusionConfig> excludeFromTMappings = Optional.empty();

        private Optional<File> sparqlRulesFile = Optional.empty();

        private Optional<Reader> sparqlRulesReader = Optional.empty();

        protected abstract B self();

        @Override
        public B tMappingExclusionConfig(@Nonnull TMappingExclusionConfig config) {
            this.excludeFromTMappings = Optional.of(config);
            return self();
        }

        @Override
        public B enableOntologyAnnotationQuerying(boolean queryingAnnotationsInOntology) {
            this.queryingAnnotationsInOntology = Optional.of(queryingAnnotationsInOntology);
            return self();
        }

        @Override
        public B enableDefaultDatatypeInference(boolean inferDefaultDatatype) {
            this.inferDefaultDatatype = Optional.of(inferDefaultDatatype);
            return self();
        }

        @Override
        public B sparqlRulesFile(@Nonnull File file) {
            this.sparqlRulesFile = Optional.of(file);
            return self();
        }

        @Override
        public B sparqlRulesFile(@Nonnull String path) {
            return sparqlRulesFile(new File(path));
        }

        @Override
        public B sparqlRulesReader(@Nonnull Reader reader) {
            this.sparqlRulesReader = Optional.of(reader);
            return self();
        }

        protected final OntopMappingOptions generateMappingOptions(OntopKGQueryOptions queryOptions) {
            return new OntopMappingOptions(excludeFromTMappings, sparqlRulesFile, sparqlRulesReader, queryOptions);
        }

        protected Properties generateProperties() {
            Properties properties = new Properties();
            queryingAnnotationsInOntology.ifPresent(b -> properties.put(OntopMappingSettings.QUERY_ONTOLOGY_ANNOTATIONS, b));
            inferDefaultDatatype.ifPresent(b -> properties.put(OntopMappingSettings.INFER_DEFAULT_DATATYPE, b));

            return properties;
        }
    }

    protected static abstract class OntopMappingBuilderMixin<B extends OntopMappingConfiguration.Builder<B>>
        extends OntopKGQueryBuilderMixin<B>
        implements OntopMappingConfiguration.Builder<B> {

        private final DefaultOntopMappingBuilderFragment<B> mappingBuilderFragment;
        private boolean isMappingDefined;
        private boolean isDBMetadataDefined;
        private boolean areLensesDefined;

        OntopMappingBuilderMixin() {
            this.mappingBuilderFragment = new DefaultOntopMappingBuilderFragment<>() {
                @Override
                protected B self() {
                    return OntopMappingBuilderMixin.this.self();
                }
            };
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

        @Override
        public B sparqlRulesFile(@Nonnull File file) {
            return mappingBuilderFragment.sparqlRulesFile(file);
        }

        @Override
        public B sparqlRulesFile(@Nonnull String path) {
            return mappingBuilderFragment.sparqlRulesFile(path);
        }

        @Override
        public B sparqlRulesReader(@Nonnull Reader reader) {
            return mappingBuilderFragment.sparqlRulesReader(reader);
        }

        protected final OntopMappingOptions generateMappingOptions() {
            return generateMappingOptions(generateOBDAOptions());
        }

        protected final OntopMappingOptions generateMappingOptions(OntopOBDAOptions obdaOptions) {
            return generateMappingOptions(generateKGQueryOptions(obdaOptions));
        }

        protected final OntopMappingOptions generateMappingOptions(OntopKGQueryOptions queryOptions) {
            return mappingBuilderFragment.generateMappingOptions(queryOptions);
        }

        @Override
        protected Properties generateProperties() {
            Properties properties = new Properties();
            properties.putAll(super.generateProperties());
            properties.putAll(mappingBuilderFragment.generateProperties());

            return properties;
        }

        protected final void declareDBMetadataDefined() {
            if (isOBDASpecificationAssigned()) {
                throw new InvalidOntopConfigurationException("The OBDA specification has already been assigned");
            }
            isDBMetadataDefined = true;
        }

        protected final void declareLensesDefined() {
            if (isOBDASpecificationAssigned()) {
                throw new InvalidOntopConfigurationException("The OBDA specification has already been assigned");
            }
            areLensesDefined = true;
        }

        @Override
        protected void declareOBDASpecificationAssigned() {
            super.declareOBDASpecificationAssigned();

            if (isDBMetadataDefined) {
                throw new InvalidOntopConfigurationException("DBMetadata is already defined, " +
                        "cannot assign the OBDA specification");
            }
            if (isMappingDefined()) {
                throw new InvalidOntopConfigurationException("The mapping is already defined, " +
                        "cannot assign the OBDA specification");
            }
            if (areLensesDefined) {
                throw new InvalidOntopConfigurationException("Lenses are already defined, " +
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

    }

    public static class BuilderImpl extends OntopMappingBuilderMixin<BuilderImpl> {

        @Override
        public OntopMappingConfiguration build() {
            Properties properties = generateProperties();
            OntopMappingSettings settings = new OntopMappingSettingsImpl(properties);

            OntopMappingOptions options = generateMappingOptions();

            return new OntopMappingConfigurationImpl(settings, options);
        }

        @Override
        protected BuilderImpl self() {
            return this;
        }
    }
}
