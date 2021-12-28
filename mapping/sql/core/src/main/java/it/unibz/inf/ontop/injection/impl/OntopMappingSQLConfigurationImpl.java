package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableMap;
import com.google.inject.Module;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.impl.OntopSQLCoreConfigurationImpl.DefaultOntopSQLCoreBuilderFragment;
import it.unibz.inf.ontop.injection.impl.OntopSQLCredentialConfigurationImpl.DefaultOntopSQLCredentialBuilderFragment;
import it.unibz.inf.ontop.injection.impl.OntopSQLCredentialConfigurationImpl.OntopSQLCredentialOptions;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopMappingSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import org.apache.commons.rdf.api.Graph;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class OntopMappingSQLConfigurationImpl extends OntopMappingConfigurationImpl implements OntopMappingSQLConfiguration {

    private final OntopMappingSQLSettings settings;
    private final OntopMappingSQLOptions options;
    private final OntopSQLCredentialConfigurationImpl sqlConfiguration;

    OntopMappingSQLConfigurationImpl(OntopMappingSQLSettings settings, OntopMappingSQLOptions options) {
        super(settings, options.mappingOptions);
        this.settings = settings;
        this.options = options;
        this.sqlConfiguration = new OntopSQLCredentialConfigurationImpl(settings, options.sqlOptions, this::getInjector);
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
                    sqlConfiguration.buildGuiceModules()),
                Stream.of(
                        new OntopMappingSQLModule(this),
                        new OntopMappingPostModule(getSettings())));
    }

    @Override
    public OntopMappingSQLSettings getSettings() {
        return settings;
    }

    /**
     * Can be overloaded
     *
     * With this default implementation, can load a given pre-processed mapping
     *
     */
    @Override
    protected OBDASpecification loadOBDASpecification() throws OBDASpecificationException {
        return loadSpecification(Optional::empty, Optional::empty, Optional::empty, Optional::empty, Optional::empty,
                Optional::empty, Optional::empty, Optional::empty, Optional::empty);
    }

    OBDASpecification loadSpecification(OntologySupplier ontologySupplier,
                                        Supplier<Optional<File>> mappingFileSupplier,
                                        Supplier<Optional<Reader>> mappingReaderSupplier,
                                        Supplier<Optional<Graph>> mappingGraphSupplier,
                                        Supplier<Optional<File>> constraintFileSupplier,
                                        Supplier<Optional<File>> dbMetadataFileSupplier,
                                        Supplier<Optional<Reader>> dbMetadataReaderSupplier,
                                        Supplier<Optional<File>> ontopViewFileSupplier,
                                        Supplier<Optional<Reader>> ontopViewReaderSupplier)
            throws OBDASpecificationException {
        return loadSpecification(
                ontologySupplier,
                () -> options.ppMapping.map(m -> (PreProcessedMapping) m),
                mappingFileSupplier,
                mappingReaderSupplier,
                mappingGraphSupplier,
                constraintFileSupplier,
                dbMetadataFileSupplier,
                dbMetadataReaderSupplier,
                ontopViewFileSupplier,
                ontopViewReaderSupplier
        );
    }


    @Override
    public Optional<SQLPPMapping> loadPPMapping() throws MappingIOException, InvalidMappingException {
        return loadPPMapping(Optional::empty, Optional::empty, Optional::empty);
    }

    /**
     * TODO: also consider the other steps
     */
    Optional<SQLPPMapping> loadPPMapping(Supplier<Optional<File>> mappingFileSupplier,
                                         Supplier<Optional<Reader>> mappingReaderSupplier,
                                         Supplier<Optional<Graph>> mappingGraphSupplier)
            throws MappingIOException, InvalidMappingException {

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
            // The parser is in charge of closing the reader
            return Optional.of(parser.parse(optionalMappingReader.get()));
        }
        Optional<Graph> optionalMappingGraph = mappingGraphSupplier.get();
        if (optionalMappingGraph.isPresent()) {
            return Optional.of(parser.parse(optionalMappingGraph.get()));
        }

        return Optional.empty();
    }


    /**
     * Groups all the options required by the OBDAConfiguration.
     *
     * Useful for extensions
     *
     */
    public static class OntopMappingSQLOptions {
        final OntopSQLCredentialOptions sqlOptions;
        final OntopMappingOptions mappingOptions;
        final Optional<SQLPPMapping> ppMapping;

        private OntopMappingSQLOptions(Optional<SQLPPMapping> ppMapping, OntopSQLCredentialOptions sqlOptions,
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


        final OntopMappingSQLOptions generateMappingSQLOptions(OntopSQLCredentialOptions sqlOptions,
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
        private final DefaultOntopSQLCoreBuilderFragment<B> sqlCoreBuilderFragment;
        private final DefaultOntopSQLCredentialBuilderFragment<B> sqlCredentialBuilderFragment;

        protected OntopMappingSQLBuilderMixin() {
            B builder = (B) this;
            localBuilderFragment = new DefaultMappingSQLBuilderFragment<>(builder,
                    this::isMappingDefined,
                    this::declareMappingDefined);
            sqlCoreBuilderFragment = new DefaultOntopSQLCoreBuilderFragment<>(builder);
            sqlCredentialBuilderFragment = new DefaultOntopSQLCredentialBuilderFragment<>(builder);
        }

        @Override
        public B ppMapping(@Nonnull SQLPPMapping ppMapping) {
            return localBuilderFragment.ppMapping(ppMapping);
        }

        @Override
        protected Properties generateProperties() {
            Properties properties = super.generateProperties();
            properties.putAll(localBuilderFragment.generateProperties());
            properties.putAll(sqlCoreBuilderFragment.generateProperties());
            properties.putAll(sqlCredentialBuilderFragment.generateProperties());
            return properties;
        }

        final OntopMappingSQLOptions generateMappingSQLOptions() {
            OntopOBDAConfigurationImpl.OntopOBDAOptions obdaOptions = generateOBDAOptions();

            OntopSQLCredentialOptions sqlOptions = sqlCredentialBuilderFragment.generateSQLCredentialOptions(
                    sqlCoreBuilderFragment.generateSQLCoreOptions(obdaOptions.modelOptions));

            return localBuilderFragment.generateMappingSQLOptions(sqlOptions, generateMappingOptions(obdaOptions));
        }

        @Override
        public B jdbcUrl(String jdbcUrl) {
            return sqlCoreBuilderFragment.jdbcUrl(jdbcUrl);
        }

        @Override
        public B jdbcUser(String username) {
            return sqlCredentialBuilderFragment.jdbcUser(username);
        }

        @Override
        public B jdbcPassword(String password) {
            return sqlCredentialBuilderFragment.jdbcPassword(password);
        }

        @Override
        public B jdbcDriver(String jdbcDriver) {
            return sqlCoreBuilderFragment.jdbcDriver(jdbcDriver);
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
