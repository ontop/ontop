package it.unibz.inf.ontop.injection.impl;


import com.google.inject.Module;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopMappingSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.injection.impl.OntopMappingConfigurationImpl.OntopMappingOptions;
import it.unibz.inf.ontop.mapping.MappingParser;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.mapping.extraction.PreProcessedMapping;
import it.unibz.inf.ontop.model.OBDAModel;
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

public class OntopMappingSQLConfigurationImpl extends OntopSQLConfigurationImpl implements OntopMappingSQLConfiguration {

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
        return options.predefinedMappingModel.isPresent();
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
                Stream.of(new OntopMappingSQLModule(this)));
    }

    @Override
    public Optional<ImplicitDBConstraintsReader> getImplicitDBConstraintsReader() {
        return mappingConfiguration.getImplicitDBConstraintsReader();
    }

    @Override
    public OntopMappingSQLSettings getSettings() {
        return settings;
    }

    /**
     * To be overloaded
     */
    @Override
    public Optional<OBDASpecification> loadSpecification() throws IOException, OBDASpecificationException {
        return loadSpecification(Optional::empty, Optional::empty, Optional::empty, Optional::empty);
    }

    Optional<OBDASpecification> loadSpecification(Supplier<Optional<Ontology>> ontologySupplier,
                                                  Supplier<Optional<File>> mappingFileSupplier,
                                                  Supplier<Optional<Reader>> mappingReaderSupplier,
                                                  Supplier<Optional<Model>> mappingGraphSupplier)
            throws IOException, OBDASpecificationException {
        return mappingConfiguration.loadSpecification(
                ontologySupplier,
                () -> options.predefinedMappingModel.map(m -> (PreProcessedMapping) m),
                mappingFileSupplier,
                mappingReaderSupplier,
                mappingGraphSupplier
        );
    }


    @Override
    public Optional<OBDAModel> loadPPMapping() throws IOException, InvalidMappingException, DuplicateMappingException {
        return loadPPMapping(Optional::empty, Optional::empty, Optional::empty, Optional::empty);
    }

    /**
     * TODO: also consider the other steps
     */
    Optional<OBDAModel> loadPPMapping(Supplier<Optional<Ontology>> ontologySupplier,
                                      Supplier<Optional<File>> mappingFileSupplier,
                                      Supplier<Optional<Reader>> mappingReaderSupplier,
                                      Supplier<Optional<Model>> mappingGraphSupplier)
            throws IOException, InvalidMappingException, DuplicateMappingException {

        if (options.predefinedMappingModel.isPresent()) {
            return options.predefinedMappingModel;
        }

        MappingParser parser = getInjector().getInstance(MappingParser.class);

        Optional<File> optionalMappingFile = mappingFileSupplier.get();
        if (optionalMappingFile.isPresent()) {
            return Optional.of(parser.parse(optionalMappingFile.get()));
        }

        Optional<Reader> optionalMappingReader = mappingReaderSupplier.get();
        if (optionalMappingReader.isPresent()) {
            return Optional.of(parser.parse(optionalMappingReader.get()));
        }
        Optional<Model> optionalMappingGraph = mappingGraphSupplier.get();
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
        final OntopSQLOptions sqlOptions;
        final OntopMappingOptions mappingOptions;
        final Optional<OBDAModel> predefinedMappingModel;

        private OntopMappingSQLOptions(Optional<OBDAModel> predefinedMappingModel, OntopSQLOptions sqlOptions,
                                       OntopMappingOptions mappingOptions) {
            this.sqlOptions = sqlOptions;
            this.mappingOptions = mappingOptions;
            this.predefinedMappingModel = predefinedMappingModel;
        }
    }

    protected static class DefaultMappingSQLBuilderFragment<B extends OntopMappingSQLConfiguration.Builder<B>>
            implements OntopMappingSQLBuilderFragment<B> {

        private final B builder;
        private final Supplier<Boolean> isMappingDefinedSupplier;
        private final Runnable declareMappingDefinedCB;
        private Optional<OBDAModel> obdaModel = Optional.empty();

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
        public B obdaModel(@Nonnull OBDAModel obdaModel) {
            if (isMappingDefinedSupplier.get()) {
                throw new InvalidOntopConfigurationException("OBDA model or mappings already defined!");
            }
            declareMappingDefinedCB.run();
            this.obdaModel = Optional.of(obdaModel);
            return builder;
        }


        final OntopMappingSQLOptions generateMappingSQLOptions(OntopSQLOptions sqlOptions,
                                                               OntopMappingOptions mappingOptions) {
            return new OntopMappingSQLOptions(obdaModel, sqlOptions, mappingOptions);
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
        public B obdaModel(@Nonnull OBDAModel obdaModel) {
            return localBuilderFragment.obdaModel(obdaModel);
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
