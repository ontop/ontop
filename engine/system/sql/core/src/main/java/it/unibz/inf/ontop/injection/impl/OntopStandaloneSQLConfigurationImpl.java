package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Module;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.answering.reformulation.input.InputQueryFactory;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.impl.OntopSQLCredentialConfigurationImpl.OntopSQLCredentialOptions;
import it.unibz.inf.ontop.iq.executor.ProposalExecutor;
import it.unibz.inf.ontop.injection.OntopStandaloneSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopStandaloneSQLSettings;
import it.unibz.inf.ontop.injection.impl.OntopSystemSQLConfigurationImpl.OntopSystemSQLOptions;
import it.unibz.inf.ontop.injection.impl.OntopReformulationConfigurationImpl.DefaultOntopReformulationBuilderFragment;
import it.unibz.inf.ontop.injection.impl.OntopReformulationSQLConfigurationImpl.DefaultOntopReformulationSQLBuilderFragment;
import it.unibz.inf.ontop.injection.impl.OntopReformulationSQLConfigurationImpl.OntopReformulationSQLOptions;
import it.unibz.inf.ontop.iq.proposal.QueryOptimizationProposal;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;


public class OntopStandaloneSQLConfigurationImpl extends OntopMappingSQLAllConfigurationImpl
        implements OntopStandaloneSQLConfiguration {

    private final OntopStandaloneSQLSettings settings;
    private final OntopSystemSQLConfigurationImpl systemConfiguration;

    OntopStandaloneSQLConfigurationImpl(OntopStandaloneSQLSettings settings, OntopStandaloneSQLOptions options) {
        super(settings, options.mappingOptions);
        this.settings = settings;
        systemConfiguration = new OntopSystemSQLConfigurationImpl(settings, options.systemOptions,
                this::loadOBDASpecification);
    }

    @Override
    public OntopStandaloneSQLSettings getSettings() {
        return settings;
    }

    @Override
    public Optional<IRIDictionary> getIRIDictionary() {
        return systemConfiguration.getIRIDictionary();
    }

    @Override
    public QueryReformulator loadQueryReformulator() throws OBDASpecificationException {
        return systemConfiguration.loadQueryReformulator();
    }

    @Override
    public InputQueryFactory getInputQueryFactory() {
        return getInjector()
                .getInstance(InputQueryFactory.class);
    }

    @Override
    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                super.buildGuiceModules(),
                systemConfiguration.buildGuiceModules());
    }

    /**
     * Can be overloaded by sub-classes
     */
    @Override
    protected ImmutableMap<Class<? extends QueryOptimizationProposal>, Class<? extends ProposalExecutor>>
    generateOptimizationConfigurationMap() {
        return Stream.concat(
                    super.generateOptimizationConfigurationMap().entrySet().stream(),
                    systemConfiguration.generateOptimizationConfigurationMap().entrySet().stream())
                .distinct()
                .collect(ImmutableCollectors.toMap());
    }


    static class OntopStandaloneSQLOptions {
        final OntopSystemSQLOptions systemOptions;
        final OntopMappingSQLAllOptions mappingOptions;

        OntopStandaloneSQLOptions(OntopSystemSQLOptions systemOptions, OntopMappingSQLAllOptions mappingOptions) {
            this.systemOptions = systemOptions;
            this.mappingOptions = mappingOptions;
        }
    }



    static abstract class OntopStandaloneSQLBuilderMixin<B extends OntopStandaloneSQLConfiguration.Builder<B>>
            extends OntopMappingSQLAllBuilderMixin<B>
            implements OntopStandaloneSQLConfiguration.Builder<B> {

        private final DefaultOntopReformulationSQLBuilderFragment<B> sqlTranslationFragmentBuilder;
        private final DefaultOntopReformulationBuilderFragment<B> translationFragmentBuilder;
        private final DefaultOntopSystemBuilderFragment<B> systemFragmentBuilder;

        OntopStandaloneSQLBuilderMixin() {
            B builder = (B) this;
            this.sqlTranslationFragmentBuilder = new DefaultOntopReformulationSQLBuilderFragment<>(builder);
            this.translationFragmentBuilder = new DefaultOntopReformulationBuilderFragment<>(builder);
            this.systemFragmentBuilder = new DefaultOntopSystemBuilderFragment<>(builder);
        }

        @Override
        public B enableIRISafeEncoding(boolean enable) {
            return translationFragmentBuilder.enableIRISafeEncoding(enable);
        }

        @Override
        public B enableExistentialReasoning(boolean enable) {
            return translationFragmentBuilder.enableExistentialReasoning(enable);
        }

        @Override
        public B iriDictionary(@Nonnull IRIDictionary iriDictionary) {
            return translationFragmentBuilder.iriDictionary(iriDictionary);
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

        final OntopStandaloneSQLOptions generateStandaloneSQLOptions() {
            OntopMappingSQLAllOptions sqlMappingOptions = generateMappingSQLAllOptions();
            OntopReformulationConfigurationImpl.OntopReformulationOptions translationOptions =
                    this.translationFragmentBuilder.generateReformulationOptions(
                        sqlMappingOptions.mappingSQLOptions.mappingOptions.obdaOptions,
                        sqlMappingOptions.mappingSQLOptions.mappingOptions.optimizationOptions);

            OntopSQLCredentialOptions sqlOptions = sqlMappingOptions.mappingSQLOptions.sqlOptions;

            OntopReformulationSQLOptions sqlTranslationOptions = sqlTranslationFragmentBuilder.generateSQLReformulationOptions(
                    translationOptions, sqlOptions.sqlCoreOptions);

            OntopSystemSQLOptions systemSQLOptions = new OntopSystemSQLOptions(sqlTranslationOptions, sqlOptions);

            return new OntopStandaloneSQLOptions(systemSQLOptions, sqlMappingOptions);
        }

    }

    public static final class BuilderImpl<B extends OntopStandaloneSQLConfiguration.Builder<B>>
            extends OntopStandaloneSQLBuilderMixin<B> {

        @Override
        public OntopStandaloneSQLConfiguration build() {
            OntopStandaloneSQLSettings settings = new OntopStandaloneSQLSettingsImpl(generateProperties(),
                    isR2rml());
            OntopStandaloneSQLOptions options = generateStandaloneSQLOptions();
            return new OntopStandaloneSQLConfigurationImpl(settings, options);
        }
    }



}
