package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Injector;
import com.google.inject.Module;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.query.KGQueryFactory;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.impl.OntopSQLCredentialConfigurationImpl.OntopSQLCredentialOptions;
import it.unibz.inf.ontop.injection.OntopStandaloneSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopStandaloneSQLSettings;
import it.unibz.inf.ontop.injection.impl.OntopSystemSQLConfigurationImpl.OntopSystemSQLOptions;
import it.unibz.inf.ontop.injection.impl.OntopReformulationConfigurationImpl.DefaultOntopReformulationBuilderFragment;
import it.unibz.inf.ontop.injection.impl.OntopReformulationSQLConfigurationImpl.DefaultOntopReformulationSQLBuilderFragment;
import it.unibz.inf.ontop.injection.impl.OntopReformulationSQLConfigurationImpl.OntopReformulationSQLOptions;

import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;


public class OntopStandaloneSQLConfigurationImpl extends OntopMappingSQLAllConfigurationImpl
        implements OntopStandaloneSQLConfiguration {

    private final OntopStandaloneSQLSettings settings;
    private final OntopSystemSQLConfigurationImpl systemConfiguration;

    OntopStandaloneSQLConfigurationImpl(OntopStandaloneSQLSettings settings, OntopStandaloneSQLOptions options) {
        super(settings, options.mappingOptions);
        this.settings = settings;
        systemConfiguration = new OntopSystemSQLConfigurationImpl(settings, options.systemOptions,
                this::loadOBDASpecification, this::getInjector);
    }

    OntopStandaloneSQLConfigurationImpl(OntopStandaloneSQLSettings settings, OntopStandaloneSQLOptions options, Supplier<Injector> injectorSupplier) {
        super(settings, options.mappingOptions, injectorSupplier);
        this.settings = settings;
        systemConfiguration = new OntopSystemSQLConfigurationImpl(settings, options.systemOptions,
            this::loadOBDASpecification, injectorSupplier);
    }

    @Override
    public OntopStandaloneSQLSettings getSettings() {
        return settings;
    }

    @Override
    public QueryReformulator loadQueryReformulator() throws OBDASpecificationException {
        return systemConfiguration.loadQueryReformulator();
    }

    @Override
    public KGQueryFactory getKGQueryFactory() {
        return getInjector()
                .getInstance(KGQueryFactory.class);
    }

    @Override
    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                super.buildGuiceModules(),
                systemConfiguration.buildGuiceModules());
    }


    static class OntopStandaloneSQLOptions {
        final OntopSystemSQLOptions systemOptions;
        final OntopMappingSQLAllOptions mappingOptions;

        OntopStandaloneSQLOptions(OntopSystemSQLOptions systemOptions, OntopMappingSQLAllOptions mappingOptions) {
            this.systemOptions = systemOptions;
            this.mappingOptions = mappingOptions;
        }
    }



    protected static abstract class OntopStandaloneSQLBuilderMixin<B extends OntopStandaloneSQLConfiguration.Builder<B>>
            extends OntopMappingSQLAllBuilderMixin<B>
            implements OntopStandaloneSQLConfiguration.Builder<B> {

        private final DefaultOntopReformulationSQLBuilderFragment<B> sqlTranslationFragmentBuilder;
        private final DefaultOntopReformulationBuilderFragment<B> translationFragmentBuilder;
        private final DefaultOntopSystemBuilderFragment<B> systemFragmentBuilder;

        OntopStandaloneSQLBuilderMixin() {
            this.sqlTranslationFragmentBuilder = new DefaultOntopReformulationSQLBuilderFragment<>();
            this.translationFragmentBuilder = new DefaultOntopReformulationBuilderFragment<>() {
                @Override
                protected B self() {
                    return OntopStandaloneSQLBuilderMixin.this.self();
                }
            };
            this.systemFragmentBuilder = new DefaultOntopSystemBuilderFragment<>() {
                @Override
                protected B self() {
                    return OntopStandaloneSQLBuilderMixin.this.self();
                }
            };
        }

        @Override
        public B enableExistentialReasoning(boolean enable) {
            return translationFragmentBuilder.enableExistentialReasoning(enable);
        }

        @Override
        public B keepPermanentDBConnection(boolean keep) {
            return systemFragmentBuilder.keepPermanentDBConnection(keep);
        }

        @Override
        protected Properties generateProperties() {
            Properties p = super.generateProperties();
            p.putAll(systemFragmentBuilder.generateProperties());
            p.putAll(sqlTranslationFragmentBuilder.generateProperties());
            p.putAll(translationFragmentBuilder.generateProperties());
            return p;
        }

        protected final OntopStandaloneSQLOptions generateStandaloneSQLOptions() {
            OntopMappingSQLAllOptions sqlMappingOptions = generateMappingSQLAllOptions();
            OntopReformulationConfigurationImpl.OntopReformulationOptions translationOptions =
                    this.translationFragmentBuilder.generateReformulationOptions(
                        sqlMappingOptions.mappingSQLOptions.mappingOptions.queryOptions);

            OntopSQLCredentialOptions sqlOptions = sqlMappingOptions.mappingSQLOptions.sqlOptions;

            OntopReformulationSQLOptions sqlTranslationOptions = sqlTranslationFragmentBuilder.generateSQLReformulationOptions(
                    translationOptions, sqlOptions.sqlCoreOptions);

            OntopSystemSQLOptions systemSQLOptions = new OntopSystemSQLOptions(sqlTranslationOptions, sqlOptions);

            return new OntopStandaloneSQLOptions(systemSQLOptions, sqlMappingOptions);
        }
    }

    public static final class BuilderImpl extends OntopStandaloneSQLBuilderMixin<BuilderImpl> {

        @Override
        public OntopStandaloneSQLConfiguration build() {
            OntopStandaloneSQLSettings settings = new OntopStandaloneSQLSettingsImpl(generateProperties(),
                    isR2rml());
            OntopStandaloneSQLOptions options = generateStandaloneSQLOptions();
            return new OntopStandaloneSQLConfigurationImpl(settings, options);
        }

        @Override
        protected BuilderImpl self() {
            return this;
        }
    }



}
