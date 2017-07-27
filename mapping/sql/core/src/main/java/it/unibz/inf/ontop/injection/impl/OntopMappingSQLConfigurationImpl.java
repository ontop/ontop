package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableMap;
import com.google.inject.Module;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.executor.ProposalExecutor;
import it.unibz.inf.ontop.injection.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopMappingSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.injection.impl.OntopMappingConfigurationImpl.OntopMappingOptions;
import it.unibz.inf.ontop.model.SQLMappingParser;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.iq.proposal.QueryOptimizationProposal;
import it.unibz.inf.ontop.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.mapping.pp.SQLPPMapping;
import org.apache.commons.rdf.api.Graph;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class OntopMappingSQLConfigurationImpl extends OntopSQLCoreConfigurationImpl implements OntopMappingSQLConfiguration {

    private final OntopMappingSQLSettings settings;
    private final OntopMappingSQLOptions options;
    private final OntopMappingConfigurationImpl mappingConfiguration;

    OntopMappingSQLConfigurationImpl(OntopMappingSQLSettings settings,
                                        OntopMappingSQLOptions options) {
        super(settings, options.sqlOptions);
        this.settings = settings;
        this.options = options;
        this.mappingConfiguration = new OntopMappingConfigurationImpl(settings, options.mappingOptions,
                this::getInjector);
    }

    boolean isInputMappingDefined() {
        return options.ppMapping.isPresent();
    }

    /**
     * To be overloaded
     *
     */
    @Override
    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(Stream.concat(
                    super.buildGuiceModules(),
                    mappingConfiguration.buildGuiceModules()),
                Stream.of(
                        new OntopMappingSQLModule(this),
                        new OntopMappingPostModule(getSettings())));
    }

    @Override
    public Optional<TMappingExclusionConfig> getTmappingExclusions() {
        return mappingConfiguration.getTmappingExclusions();
    }

    @Override
    public OntopMappingSQLSettings getSettings() {
        return settings;
    }

    /**
     * To be overloaded
     */
    @Override
    public OBDASpecification loadSpecification() throws OBDASpecificationException {
        return loadSpecification(Optional::empty, Optional::empty, Optional::empty, Optional::empty, Optional::empty);
    }

    OBDASpecification loadSpecification(OntologySupplier ontologySupplier,
                                                  Supplier<Optional<File>> mappingFileSupplier,
                                                  Supplier<Optional<Reader>> mappingReaderSupplier,
                                                  Supplier<Optional<Graph>> mappingGraphSupplier,
                                                  Supplier<Optional<File>> constraintFileSupplier)
            throws OBDASpecificationException {
        return mappingConfiguration.loadSpecification(
                ontologySupplier,
                () -> options.ppMapping.map(m -> (PreProcessedMapping) m),
                mappingFileSupplier,
                mappingReaderSupplier,
                mappingGraphSupplier,
                constraintFileSupplier
        );
    }


    @Override
    public Optional<SQLPPMapping> loadPPMapping() throws MappingIOException, InvalidMappingException, DuplicateMappingException {
        return loadPPMapping(Optional::empty, Optional::empty, Optional::empty, Optional::empty);
    }

    /**
     * TODO: also consider the other steps
     */
    Optional<SQLPPMapping> loadPPMapping(OntologySupplier ontologySupplier,
                                         Supplier<Optional<File>> mappingFileSupplier,
                                         Supplier<Optional<Reader>> mappingReaderSupplier,
                                         Supplier<Optional<Graph>> mappingGraphSupplier)
            throws MappingIOException, InvalidMappingException, DuplicateMappingException {

        if (options.ppMapping.isPresent()) {
            return options.ppMapping;
        }

        SQLMappingParser parser = getInjector().getInstance(SQLMappingParser.class);

        Optional<File> optionalMappingFile = mappingFileSupplier.get();
        if (optionalMappingFile.isPresent()) {
            return Optional.of(parser.parse(optionalMappingFile.get()));
        }

        Optional<Reader> optionalMappingReader = mappingReaderSupplier.get();
        if (optionalMappingReader.isPresent()) {
            return Optional.of(parser.parse(optionalMappingReader.get()));
        }
        Optional<Graph> optionalMappingGraph = mappingGraphSupplier.get();
        if (optionalMappingGraph.isPresent()) {
            return Optional.of(parser.parse(optionalMappingGraph.get()));
        }

        return Optional.empty();
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
        internalExecutorMapBuilder.putAll(mappingConfiguration.generateOptimizationConfigurationMap());

        return internalExecutorMapBuilder.build();
    }


    /**
     * Groups all the options required by the OBDAConfiguration.
     *
     * Useful for extensions
     *
     */
    public static class OntopMappingSQLOptions {
        final OntopSQLOptions sqlOptions;
        final OntopMappingOptions mappingOptions;
        final Optional<SQLPPMapping> ppMapping;

        private OntopMappingSQLOptions(Optional<SQLPPMapping> ppMapping, OntopSQLOptions sqlOptions,
                                       OntopMappingOptions mappingOptions) {
            this.sqlOptions = sqlOptions;
            this.mappingOptions = mappingOptions;
            this.ppMapping = ppMapping;
        }
    }

    protected static class DefaultMappingSQLBuilderFragment<B extends OntopMappingSQLConfiguration.Builder<B>>
            implements OntopMappingSQLBuilderFragment<B> {

        private final B builder;
        private final Supplier<Boolean> isMappingDefinedSupplier;
        private final Runnable declareMappingDefinedCB;
        private Optional<SQLPPMapping> ppMapping = Optional.empty();

        /**
         * Default constructor
         */
        protected DefaultMappingSQLBuilderFragment(B builder,
                                                   Supplier<Boolean> isMappingDefinedSupplier,
                                                   Runnable declareMappingDefinedCB) {
            this.builder = builder;
            this.isMappingDefinedSupplier = isMappingDefinedSupplier;
            this.declareMappingDefinedCB = declareMappingDefinedCB;
        }

        /**
         * Not for end-users! Please consider giving a mapping file or a mapping reader.
         */
        @Override
        public B ppMapping(@Nonnull SQLPPMapping ppMapping) {
            if (isMappingDefinedSupplier.get()) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
            declareMappingDefinedCB.run();
            this.ppMapping = Optional.of(ppMapping);
            return builder;
        }


        final OntopMappingSQLOptions generateMappingSQLOptions(OntopSQLOptions sqlOptions,
                                                               OntopMappingOptions mappingOptions) {
            return new OntopMappingSQLOptions(ppMapping, sqlOptions, mappingOptions);
        }

        Properties generateProperties() {
            return new Properties();
        }
    }

    protected abstract static class OntopMappingSQLBuilderMixin<B extends OntopMappingSQLConfiguration.Builder<B>>
            extends OntopMappingConfigurationImpl.OntopMappingBuilderMixin<B>
            implements OntopMappingSQLConfiguration.Builder<B> {

        private final DefaultMappingSQLBuilderFragment<B> localBuilderFragment;
        private final DefaultOntopSQLBuilderFragment<B> sqlBuilderFragment;

        protected OntopMappingSQLBuilderMixin() {
            B builder = (B) this;
            localBuilderFragment = new DefaultMappingSQLBuilderFragment<>(builder,
                    this::isMappingDefined,
                    this::declareMappingDefined);
            sqlBuilderFragment = new DefaultOntopSQLBuilderFragment<>(builder);
        }

        @Override
        public B ppMapping(@Nonnull SQLPPMapping ppMapping) {
            return localBuilderFragment.ppMapping(ppMapping);
        }

        @Override
        protected Properties generateProperties() {
            Properties properties = super.generateProperties();
            properties.putAll(localBuilderFragment.generateProperties());
            properties.putAll(sqlBuilderFragment.generateProperties());
            return properties;
        }

        final OntopMappingSQLOptions generateMappingSQLOptions() {
            OntopOBDAOptions obdaOptions = generateOBDAOptions();

            return localBuilderFragment.generateMappingSQLOptions(
                    sqlBuilderFragment.generateSQLOptions(obdaOptions),
                    generateMappingOptions(obdaOptions));
        }

        @Override
        public B jdbcName(String dbName) {
            return sqlBuilderFragment.jdbcName(dbName);
        }

        @Override
        public B jdbcUrl(String jdbcUrl) {
            return sqlBuilderFragment.jdbcUrl(jdbcUrl);
        }

        @Override
        public B jdbcUser(String username) {
            return sqlBuilderFragment.jdbcUser(username);
        }

        @Override
        public B jdbcPassword(String password) {
            return sqlBuilderFragment.jdbcPassword(password);
        }

        @Override
        public B jdbcDriver(String jdbcDriver) {
            return sqlBuilderFragment.jdbcDriver(jdbcDriver);
        }
    }


    public static class BuilderImpl<B extends OntopMappingSQLConfiguration.Builder<B>>
            extends OntopMappingSQLBuilderMixin<B> {

        @Override
        public OntopMappingSQLConfiguration build() {
            Properties properties = generateProperties();
            OntopMappingSQLSettings settings = new OntopMappingSQLSettingsImpl(properties);

            OntopMappingSQLOptions options = generateMappingSQLOptions();

            return new OntopMappingSQLConfigurationImpl(settings, options);
        }
    }

}
