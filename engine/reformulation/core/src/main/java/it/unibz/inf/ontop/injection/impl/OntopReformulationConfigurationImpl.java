package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Module;
import it.unibz.inf.ontop.iq.executor.ProposalExecutor;
import it.unibz.inf.ontop.injection.OntopReformulationConfiguration;
import it.unibz.inf.ontop.injection.OntopReformulationSettings;
import it.unibz.inf.ontop.iq.proposal.QueryOptimizationProposal;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.injection.impl.OntopOptimizationConfigurationImpl.*;


public class OntopReformulationConfigurationImpl extends OntopOBDAConfigurationImpl implements OntopReformulationConfiguration {

    private final OntopOptimizationConfigurationImpl optimizationConfiguration;
    private final OntopReformulationSettings settings;
    private final OntopTranslationOptions options;

    OntopReformulationConfigurationImpl(OntopReformulationSettings settings, OntopTranslationOptions options) {
        super(settings, options.obdaOptions);
        this.settings = settings;
        this.options = options;
        this.optimizationConfiguration = new OntopOptimizationConfigurationImpl(settings, options.optimizationOptions);
    }

    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                Stream.concat(
                        super.buildGuiceModules(),
                        optimizationConfiguration.buildGuiceModules()),
                Stream.of(new OntopTranslationModule(this)));
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

    @Override
    public OntopReformulationSettings getSettings() {
        return settings;
    }

    @Override
    public Optional<IRIDictionary> getIRIDictionary() {
        return options.iriDictionary;
    }

    static class OntopTranslationOptions {
        private final Optional<IRIDictionary> iriDictionary;
        final OntopOBDAOptions obdaOptions;
        final OntopOptimizationOptions optimizationOptions;

        OntopTranslationOptions(Optional<IRIDictionary> iriDictionary, OntopOBDAOptions obdaOptions,
                                OntopOptimizationOptions optimizationOptions) {
            this.iriDictionary = iriDictionary;
            this.obdaOptions = obdaOptions;
            this.optimizationOptions = optimizationOptions;
        }
    }

    static class DefaultOntopTranslationBuilderFragment<B extends OntopReformulationConfiguration.Builder<B>>
            implements OntopTranslationBuilderFragment<B> {

        private final B builder;
        private Optional<Boolean> encodeIRISafely = Optional.empty();
        private Optional<Boolean> existentialReasoning = Optional.empty();
        private Optional<IRIDictionary> iriDictionary = Optional.empty();

        DefaultOntopTranslationBuilderFragment(B builder) {
            this.builder = builder;
        }

        @Override
        public B enableIRISafeEncoding(boolean enable) {
            this.encodeIRISafely = Optional.of(enable);
            return builder;
        }

        @Override
        public B enableExistentialReasoning(boolean enable) {
            this.existentialReasoning = Optional.of(enable);
            return builder;

        }

        @Override
        public B iriDictionary(@Nonnull IRIDictionary iriDictionary) {
            this.iriDictionary = Optional.of(iriDictionary);
            return builder;
        }

        Properties generateProperties() {
            Properties p = new Properties();

            encodeIRISafely.ifPresent(e -> p.put(OntopReformulationSettings.SQL_GENERATE_REPLACE, e));
            existentialReasoning.ifPresent(r -> p.put(OntopReformulationSettings.EXISTENTIAL_REASONING, r));

            return p;
        }

        final OntopTranslationOptions generateTranslationOptions(OntopOBDAOptions obdaOptions,
                                                                 OntopOptimizationOptions optimizationOptions) {
            return new OntopTranslationOptions(iriDictionary, obdaOptions, optimizationOptions);
        }
    }

    static abstract class OntopTranslationBuilderMixin<B extends OntopReformulationConfiguration.Builder<B>>
            extends OntopOBDAConfigurationBuilderMixin<B>
            implements OntopReformulationConfiguration.Builder<B> {

        private final DefaultOntopTranslationBuilderFragment<B> localBuilderFragment;
        private final DefaultOntopOptimizationBuilderFragment<B> optimizationBuilderFragment;

        OntopTranslationBuilderMixin() {
            B builder = (B) this;
            localBuilderFragment = new DefaultOntopTranslationBuilderFragment<>(builder);
            optimizationBuilderFragment = new DefaultOntopOptimizationBuilderFragment<>(builder);
        }

        @Override
        public B enableIRISafeEncoding(boolean enable) {
            return localBuilderFragment.enableIRISafeEncoding(enable);
        }

        @Override
        public B enableExistentialReasoning(boolean enable) {
            return localBuilderFragment.enableExistentialReasoning(enable);
        }

        @Override
        public B iriDictionary(@Nonnull IRIDictionary iriDictionary) {
            return localBuilderFragment.iriDictionary(iriDictionary);
        }

        @Override
        protected Properties generateProperties() {
            Properties properties = super.generateProperties();
            properties.putAll(localBuilderFragment.generateProperties());
            return properties;
        }

        OntopTranslationOptions generateRuntimeOptions() {
            OntopOBDAOptions obdaOptions = generateOBDAOptions();
            return localBuilderFragment.generateTranslationOptions(obdaOptions,
                    optimizationBuilderFragment.generateOptimizationOptions(obdaOptions.modelOptions));
        }
    }

}
