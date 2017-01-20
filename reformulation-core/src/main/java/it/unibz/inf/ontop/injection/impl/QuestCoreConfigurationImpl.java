package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableMap;
import com.google.inject.Module;
import it.unibz.inf.ontop.executor.ProposalExecutor;
import it.unibz.inf.ontop.injection.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.impl.OntopOptimizationConfigurationImpl.DefaultOntopOptimizationBuilderFragment;
import it.unibz.inf.ontop.injection.impl.OntopOptimizationConfigurationImpl.OntopOptimizationConfigurationOptions;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.injection.QuestCoreConfiguration;
import it.unibz.inf.ontop.injection.QuestCoreSettings;
import it.unibz.inf.ontop.pivotalrepr.proposal.QueryOptimizationProposal;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public class QuestCoreConfigurationImpl extends OBDACoreConfigurationImpl implements QuestCoreConfiguration {

    private final QuestCoreSettings settings;
    private final QuestCoreOptions options;
    // Concrete implementation due to the "mixin style" (indirect inheritance)
    private final OntopOptimizationConfigurationImpl optimizationConfiguration;

    protected QuestCoreConfigurationImpl(QuestCoreSettings settings, QuestCoreOptions options) {
        super(settings, options.obdaOptions);
        this.settings = settings;
        this.options = options;
        this.optimizationConfiguration = new OntopOptimizationConfigurationImpl(settings, options.optimizationOptions);
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

    /**
     * TODO: complete
     */
    @Override
    public void validate() throws InvalidOntopConfigurationException {

        boolean isMapping = isMappingDefined();

        if ((!isMapping) && settings.isInVirtualMode()) {
            throw new InvalidOntopConfigurationException("Mapping is not specified in virtual mode", this);
        } else if (isMapping && (!settings.isInVirtualMode())) {
            throw new InvalidOntopConfigurationException("Mapping is specified in classic A-box mode", this);
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
    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                super.buildGuiceModules(),
                Stream.concat(optimizationConfiguration.buildGuiceModules(),
                    Stream.of(new QuestComponentModule(this))));
    }

    public static class QuestCoreOptions {
        public final Optional<TMappingExclusionConfig> excludeFromTMappings;
        private final OBDAConfigurationOptions obdaOptions;
        private final OntopOptimizationConfigurationOptions optimizationOptions;


        public QuestCoreOptions(Optional<TMappingExclusionConfig> excludeFromTMappings,
                                OBDAConfigurationOptions obdaOptions,
                                OntopOptimizationConfigurationOptions optimizationOptions) {
            this.excludeFromTMappings = excludeFromTMappings;
            this.obdaOptions = obdaOptions;
            this.optimizationOptions = optimizationOptions;
        }
    }


    protected static class DefaultQuestCoreBuilderFragment<B extends QuestCoreConfiguration.Builder<B>>
        implements QuestCoreBuilderFragment<B> {

        private final B builder;

        private Optional<TMappingExclusionConfig> excludeFromTMappings = Optional.empty();

        private Optional<Boolean> encodeIRISafely = Optional.empty();
        private Optional<Boolean> existentialReasoning = Optional.empty();

        protected DefaultQuestCoreBuilderFragment(B builder) {
            this.builder = builder;
        }


        @Override
        public B tMappingExclusionConfig(@Nonnull TMappingExclusionConfig config) {
            this.excludeFromTMappings = Optional.of(config);
            return builder;
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

        protected Properties generateUserProperties() {
            Properties p = new Properties();

            encodeIRISafely.ifPresent(e -> p.put(QuestCoreSettings.SQL_GENERATE_REPLACE, e));
            existentialReasoning.ifPresent(r -> {
                p.put(QuestCoreSettings.REWRITE, r);
                p.put(QuestCoreSettings.REFORMULATION_TECHNIQUE, QuestConstants.TW);
            });

            return p;
        }

        protected final QuestCoreOptions generateQuestCoreOptions(OBDAConfigurationOptions obdaOptions,
                                                                  OntopOptimizationConfigurationOptions optimizationOptions) {
            return new QuestCoreOptions(excludeFromTMappings, obdaOptions, optimizationOptions);
        }
    }


    protected abstract static class QuestCoreConfigurationBuilderMixin<B extends QuestCoreConfiguration.Builder<B>>
        extends OBDACoreConfigurationBuilderMixin<B>
        implements QuestCoreConfiguration.Builder<B> {

        private final DefaultQuestCoreBuilderFragment<B> questCoreBuilderFragment;
        private final DefaultOntopOptimizationBuilderFragment<B> optimizationBuilderFragment;

        protected QuestCoreConfigurationBuilderMixin() {
            B builder = (B) this;
            questCoreBuilderFragment = new DefaultQuestCoreBuilderFragment<>(builder);
            optimizationBuilderFragment = new DefaultOntopOptimizationBuilderFragment<>(builder);
        }

        @Override
        public B tMappingExclusionConfig(@Nonnull TMappingExclusionConfig config) {
            return questCoreBuilderFragment.tMappingExclusionConfig(config);
        }

        @Override
        public B enableIRISafeEncoding(boolean enable) {
            return questCoreBuilderFragment.enableIRISafeEncoding(enable);
        }

        @Override
        public B enableExistentialReasoning(boolean enable) {
            return questCoreBuilderFragment.enableExistentialReasoning(enable);
        }

        @Override
        protected Properties generateProperties() {
            Properties properties = super.generateProperties();
            properties.putAll(optimizationBuilderFragment.generateProperties());
            properties.putAll(questCoreBuilderFragment.generateUserProperties());
            return properties;
        }

        protected final QuestCoreOptions generateQuestCoreOptions() {
            OBDAConfigurationOptions obdaOptions = generateOBDACoreOptions();

            return questCoreBuilderFragment.generateQuestCoreOptions(obdaOptions,
                    optimizationBuilderFragment.generateOntopOptimizationConfigurationOptions(
                            obdaOptions.mappingSqlOptions.mappingOptions.obdaOptions.modelOptions));
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
