package it.unibz.inf.ontop.owlrefplatform.injection.impl;


import com.google.common.collect.ImmutableMap;
import com.google.inject.Module;
import it.unibz.inf.ontop.executor.InternalProposalExecutor;
import it.unibz.inf.ontop.executor.expression.PushDownExpressionExecutor;
import it.unibz.inf.ontop.executor.groundterm.GroundTermRemovalFromDataNodeExecutor;
import it.unibz.inf.ontop.executor.join.InnerJoinExecutor;
import it.unibz.inf.ontop.executor.leftjoin.LeftJoinExecutor;
import it.unibz.inf.ontop.executor.merging.QueryMergingExecutor;
import it.unibz.inf.ontop.executor.projection.ProjectionShrinkingExecutor;
import it.unibz.inf.ontop.executor.pullout.PullVariableOutOfDataNodeExecutor;
import it.unibz.inf.ontop.executor.pullout.PullVariableOutOfSubTreeExecutor;
import it.unibz.inf.ontop.executor.substitution.SubstitutionPropagationExecutor;
import it.unibz.inf.ontop.executor.truenode.TrueNodeRemovalExecutor;
import it.unibz.inf.ontop.executor.union.UnionLiftInternalExecutor;
import it.unibz.inf.ontop.executor.unsatisfiable.RemoveEmptyNodesExecutor;
import it.unibz.inf.ontop.injection.InvalidOBDAConfigurationException;
import it.unibz.inf.ontop.injection.impl.OBDACoreConfigurationImpl;
import it.unibz.inf.ontop.model.DataSourceMetadata;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.owlrefplatform.injection.QuestCoreConfiguration;
import it.unibz.inf.ontop.owlrefplatform.injection.QuestCorePreferences;
import it.unibz.inf.ontop.pivotalrepr.OptimizationConfiguration;
import it.unibz.inf.ontop.pivotalrepr.impl.OptimizationConfigurationImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.*;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.owlrefplatform.injection.QuestCoreConfiguration.CardinalityPreservationMode.LOOSE;

public class QuestCoreConfigurationImpl extends OBDACoreConfigurationImpl implements QuestCoreConfiguration {

    private final QuestCorePreferences preferences;
    private final QuestCoreOptions options;
    private final OptimizationConfiguration optimizationConfiguration;
    private final CardinalityPreservationMode cardinalityMode;

    protected QuestCoreConfigurationImpl(QuestCorePreferences preferences, OBDAConfigurationOptions obdaOptions,
                                         QuestCoreOptions options) {
        super(preferences, obdaOptions);
        this.preferences = preferences;
        this.options = options;
        this.optimizationConfiguration = new OptimizationConfigurationImpl(generateOptimizationConfigurationMap());

        // TODO: allow the other modes
        cardinalityMode = LOOSE;
    }

    /**
     * TODO: complete
     */
    @Override
    public void validate() throws InvalidOBDAConfigurationException {

        boolean isMapping = isMappingDefined();

        if ((!isMapping) && preferences.isInVirtualMode()) {
            throw new InvalidOBDAConfigurationException("Mapping is not specified in virtual mode", this);
        } else if (isMapping && (!preferences.isInVirtualMode())) {
            throw new InvalidOBDAConfigurationException("Mapping is specified in classic A-box mode", this);
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
    public Optional<DataSourceMetadata> getDatasourceMetadata() {
        return options.dbMetadata;
    }

    @Override
    public QuestCorePreferences getPreferences() {
        return preferences;
    }

    @Override
    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                super.buildGuiceModules(),
                Stream.of(new QuestComponentModule(this)));
    }

    /**
     * Can be overloaded by sub-classes
     */
    protected ImmutableMap<Class<? extends QueryOptimizationProposal>, Class<? extends InternalProposalExecutor>>
    generateOptimizationConfigurationMap() {
        ImmutableMap.Builder<Class<? extends QueryOptimizationProposal>, Class<? extends InternalProposalExecutor>>
                internalExecutorMapBuilder = ImmutableMap.builder();
        internalExecutorMapBuilder.put(InnerJoinOptimizationProposal.class, InnerJoinExecutor.class);
        internalExecutorMapBuilder.put(SubstitutionPropagationProposal.class, SubstitutionPropagationExecutor.class);
        internalExecutorMapBuilder.put(PushDownBooleanExpressionProposal.class, PushDownExpressionExecutor.class);
        internalExecutorMapBuilder.put(GroundTermRemovalFromDataNodeProposal.class, GroundTermRemovalFromDataNodeExecutor.class);
        internalExecutorMapBuilder.put(PullVariableOutOfDataNodeProposal.class, PullVariableOutOfDataNodeExecutor.class);
        internalExecutorMapBuilder.put(PullVariableOutOfSubTreeProposal.class, PullVariableOutOfSubTreeExecutor.class);
        internalExecutorMapBuilder.put(RemoveEmptyNodeProposal.class, RemoveEmptyNodesExecutor.class);
        internalExecutorMapBuilder.put(QueryMergingProposal.class, QueryMergingExecutor.class);
        internalExecutorMapBuilder.put(UnionLiftProposal.class, UnionLiftInternalExecutor.class);
        internalExecutorMapBuilder.put(LeftJoinOptimizationProposal.class, LeftJoinExecutor.class);
        internalExecutorMapBuilder.put(ProjectionShrinkingProposal.class, ProjectionShrinkingExecutor.class);
        internalExecutorMapBuilder.put(TrueNodeRemovalProposal.class, TrueNodeRemovalExecutor.class);
        return internalExecutorMapBuilder.build();
    }

    @Override
    public OptimizationConfiguration getOptimizationConfiguration() {
        return optimizationConfiguration;
    }

    @Override
    public CardinalityPreservationMode getCardinalityPreservationMode() {
        return cardinalityMode;
    }

    public static class QuestCoreOptions {
        public final Optional<TMappingExclusionConfig> excludeFromTMappings;
        public final Optional<DataSourceMetadata> dbMetadata;


        public QuestCoreOptions(Optional<TMappingExclusionConfig> excludeFromTMappings,
                                Optional<DataSourceMetadata> dbMetadata) {
            this.excludeFromTMappings = excludeFromTMappings;
            this.dbMetadata = dbMetadata;
        }
    }


    public static class BuilderImpl<B extends QuestCoreConfiguration.Builder,
                                    P extends QuestCorePreferences,
                                    C extends QuestCoreConfiguration>
            extends OBDACoreConfigurationImpl.BuilderImpl<B,P,C>
            implements QuestCoreConfiguration.Builder<B> {

        private Optional<TMappingExclusionConfig> excludeFromTMappings = Optional.empty();

        private Optional<Boolean> queryingAnnotationsInOntology = Optional.empty();
        private Optional<Boolean> encodeIRISafely = Optional.empty();
        private Optional<Boolean> sameAsMappings = Optional.empty();
        private Optional<Boolean> optimizeEquivalences = Optional.empty();
        private Optional<DataSourceMetadata> dbMetadata = Optional.empty();
        private Optional<Boolean> existentialReasoning = Optional.empty();

        public BuilderImpl() {
        }

        @Override
        public B tMappingExclusionConfig(@Nonnull TMappingExclusionConfig config) {
            this.excludeFromTMappings = Optional.of(config);
            return (B) this;
        }

        @Override
        public B dbMetadata(@Nonnull DataSourceMetadata dbMetadata) {
            this.dbMetadata = Optional.of(dbMetadata);
            return (B) this;
        }

        @Override
        public B enableOntologyAnnotationQuerying(boolean queryingAnnotationsInOntology) {
            this.queryingAnnotationsInOntology = Optional.of(queryingAnnotationsInOntology);
            return (B) this;
        }

        @Override
        public B enableIRISafeEncoding(boolean enable) {
            this.encodeIRISafely = Optional.of(enable);
            return (B) this;
        }

        @Override
        public B sameAsMappings(boolean sameAsMappings) {
            this.sameAsMappings = Optional.of(sameAsMappings);
            return (B) this;
        }

        @Override
        public B enableEquivalenceOptimization(boolean enable) {
            this.optimizeEquivalences = Optional.of(enable);
            return (B) this;
        }

        @Override
        public B enableExistentialReasoning(boolean enable) {
            this.existentialReasoning = Optional.of(enable);
            return (B) this;

        }

        @Override
        protected Properties generateProperties() {
            Properties p = super.generateProperties();

            queryingAnnotationsInOntology.ifPresent(b -> p.put(QuestCorePreferences.ANNOTATIONS_IN_ONTO, b));
            encodeIRISafely.ifPresent(e -> p.put(QuestCorePreferences.SQL_GENERATE_REPLACE, e));
            sameAsMappings.ifPresent(b -> p.put(QuestCorePreferences.SAME_AS, b));
            optimizeEquivalences.ifPresent(b -> p.put(QuestCorePreferences.OPTIMIZE_EQUIVALENCES, b));
            existentialReasoning.ifPresent(r -> {
                p.put(QuestCorePreferences.REWRITE, r);
                p.put(QuestCorePreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
            });

            return p;
        }

        /**
         * Default implementation for P == QuestCorePreferences
         */
        @Override
        protected P createOBDAProperties(Properties p) {
            return (P) new QuestCorePreferencesImpl(p, isR2rml());
        }

        /**
         * Default implementation for P == QuestCorePreferences
         */
        @Override
        protected C createConfiguration(P questPreferences) {
            return (C) new QuestCoreConfigurationImpl(questPreferences, createOBDAConfigurationArguments(),
                    createQuestCoreArguments());
        }

        protected final QuestCoreOptions createQuestCoreArguments() {
            return new QuestCoreOptions(excludeFromTMappings, dbMetadata);
        }
    }
}
