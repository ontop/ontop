package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableMap;
import com.google.inject.Module;
import it.unibz.inf.ontop.executor.ProposalExecutor;
import it.unibz.inf.ontop.injection.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.impl.OntopOptimizationConfigurationImpl.DefaultOntopOptimizationBuilderFragment;
import it.unibz.inf.ontop.injection.impl.OntopOptimizationConfigurationImpl.OntopOptimizationOptions;
import it.unibz.inf.ontop.injection.impl.OntopRuntimeConfigurationImpl.DefaultOntopRuntimeBuilderFragment;
import it.unibz.inf.ontop.injection.impl.OntopRuntimeConfigurationImpl.OntopRuntimeOptions;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.injection.QuestCoreConfiguration;
import it.unibz.inf.ontop.injection.QuestCoreSettings;
import it.unibz.inf.ontop.pivotalrepr.proposal.QueryOptimizationProposal;
import it.unibz.inf.ontop.reformulation.IRIDictionary;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public class QuestCoreConfigurationImpl extends OBDACoreConfigurationImpl implements QuestCoreConfiguration {

    private final QuestCoreSettings settings;
    private final QuestCoreOptions options;
    // Concrete implementation due to the "mixin style" (indirect inheritance)
    private final OntopRuntimeConfigurationImpl runtimeConfiguration;

    protected QuestCoreConfigurationImpl(QuestCoreSettings settings, QuestCoreOptions options) {
        super(settings, options.obdaOptions);
        this.settings = settings;
        this.options = options;
        this.runtimeConfiguration = new OntopRuntimeConfigurationImpl(settings, options.runtimeOptions);
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
        internalExecutorMapBuilder.putAll(runtimeConfiguration.generateOptimizationConfigurationMap());

        return internalExecutorMapBuilder.build();
    }

    /**
     * TODO: complete
     */
    @Override
    public void validate() throws InvalidOntopConfigurationException {

        boolean isMapping = isMappingDefined();

        if (!isMapping) {
            throw new InvalidOntopConfigurationException("Mapping is not specified", this);
        }
        /**
         * TODO: complete
         */

        // TODO: check the types of some Object properties.
    }

    @Override
    public Optional<TMappingExclusionConfig> getTmappingExclusions() {
        return options.excludeFromTMappings;
    }

    @Override
    public QuestCoreSettings getSettings() {
        return settings;
    }

    @Override
    public Optional<IRIDictionary> getIRIDictionary() {
        return runtimeConfiguration.getIRIDictionary();
    }

    @Override
    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                super.buildGuiceModules(),
                Stream.concat(runtimeConfiguration.buildGuiceModules(),
                    Stream.of(new QuestComponentModule(this))));
    }

    public static class QuestCoreOptions {
        public final Optional<TMappingExclusionConfig> excludeFromTMappings;
        private final OBDAConfigurationOptions obdaOptions;
        private final OntopRuntimeOptions runtimeOptions;


        public QuestCoreOptions(Optional<TMappingExclusionConfig> excludeFromTMappings,
                                OBDAConfigurationOptions obdaOptions,
                                OntopRuntimeOptions runtimeOptions) {
            this.excludeFromTMappings = excludeFromTMappings;
            this.obdaOptions = obdaOptions;
            this.runtimeOptions = runtimeOptions;
        }
    }


    protected static class DefaultQuestCoreBuilderFragment<B extends QuestCoreConfiguration.Builder<B>>
        implements QuestCoreBuilderFragment<B> {

        private final B builder;

        private Optional<TMappingExclusionConfig> excludeFromTMappings = Optional.empty();

        protected DefaultQuestCoreBuilderFragment(B builder) {
            this.builder = builder;
        }


        @Override
        public B tMappingExclusionConfig(@Nonnull TMappingExclusionConfig config) {
            this.excludeFromTMappings = Optional.of(config);
            return builder;
        }

        protected Properties generateUserProperties() {
            Properties p = new Properties();
            return p;
        }

        protected final QuestCoreOptions generateQuestCoreOptions(OBDAConfigurationOptions obdaOptions,
                                                                  OntopRuntimeOptions runtimeOptions) {
            return new QuestCoreOptions(excludeFromTMappings, obdaOptions, runtimeOptions);
        }
    }


    protected abstract static class QuestCoreConfigurationBuilderMixin<B extends QuestCoreConfiguration.Builder<B>>
        extends OBDACoreConfigurationBuilderMixin<B>
        implements QuestCoreConfiguration.Builder<B> {

        private final DefaultQuestCoreBuilderFragment<B> questCoreBuilderFragment;
        private final DefaultOntopOptimizationBuilderFragment<B> optimizationBuilderFragment;
        private final DefaultOntopRuntimeBuilderFragment<B> runtimeBuilderFragment;

        protected QuestCoreConfigurationBuilderMixin() {
            B builder = (B) this;
            questCoreBuilderFragment = new DefaultQuestCoreBuilderFragment<>(builder);
            optimizationBuilderFragment = new DefaultOntopOptimizationBuilderFragment<>(builder);
            runtimeBuilderFragment = new DefaultOntopRuntimeBuilderFragment<>(builder);
        }

        @Override
        public B tMappingExclusionConfig(@Nonnull TMappingExclusionConfig config) {
            return questCoreBuilderFragment.tMappingExclusionConfig(config);
        }

        @Override
        protected Properties generateProperties() {
            Properties properties = super.generateProperties();
            properties.putAll(optimizationBuilderFragment.generateProperties());
            properties.putAll(questCoreBuilderFragment.generateUserProperties());
            properties.putAll(runtimeBuilderFragment.generateProperties());
            return properties;
        }

        protected final QuestCoreOptions generateQuestCoreOptions() {
            OBDAConfigurationOptions obdaCoreOptions = generateOBDACoreOptions();
            OntopOBDAOptions obdaOptions =  obdaCoreOptions.mappingSqlOptions.mappingOptions.obdaOptions;
            OntopOptimizationOptions optimizationOptions = optimizationBuilderFragment.generateOptimizationOptions(
                    obdaOptions.modelOptions);
            OntopRuntimeOptions runtimeOptions = runtimeBuilderFragment.generateRuntimeOptions(obdaOptions,
                    optimizationOptions);

            return questCoreBuilderFragment.generateQuestCoreOptions(obdaCoreOptions, runtimeOptions);
        }

        @Override
        public B enableIRISafeEncoding(boolean enable) {
            return runtimeBuilderFragment.enableIRISafeEncoding(enable);
        }

        @Override
        public B enableExistentialReasoning(boolean enable) {
            return runtimeBuilderFragment.enableExistentialReasoning(enable);
        }

        @Override
        public B iriDictionary(@Nonnull IRIDictionary iriDictionary) {
            return runtimeBuilderFragment.iriDictionary(iriDictionary);
        }
    }


    public static final class BuilderImpl<B extends QuestCoreConfiguration.Builder<B>>
            extends QuestCoreConfigurationBuilderMixin<B> {

        @Override
        public QuestCoreConfiguration build() {
            Properties properties = generateProperties();
            QuestCoreSettings settings = new QuestCoreSettingsImpl(properties, isR2rml());

            return new QuestCoreConfigurationImpl(settings, generateQuestCoreOptions());
        }
    }
}
