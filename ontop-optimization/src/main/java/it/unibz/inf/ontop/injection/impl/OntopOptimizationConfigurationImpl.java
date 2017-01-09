package it.unibz.inf.ontop.injection.impl;

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
import it.unibz.inf.ontop.injection.OntopOptimizationConfiguration;
import it.unibz.inf.ontop.injection.OntopOptimizationSettings;
import it.unibz.inf.ontop.pivotalrepr.proposal.*;

import java.util.Properties;
import java.util.stream.Stream;

public class OntopOptimizationConfigurationImpl extends OntopModelConfigurationImpl
        implements OntopOptimizationConfiguration {

    protected OntopOptimizationConfigurationImpl(OntopOptimizationSettings settings, OntopOptimizationConfigurationOptions options) {
        super(settings, options.getModelOptions());
    }

    public static class OntopOptimizationConfigurationOptions {
        private final OntopModelConfigurationOptions modelOptions;

        OntopOptimizationConfigurationOptions(OntopModelConfigurationOptions modelOptions) {
            this.modelOptions = modelOptions;
        }

        public OntopModelConfigurationOptions getModelOptions() {
            return modelOptions;
        }
    }

    @Override
    public OntopOptimizationSettings getSettings() {
        return (OntopOptimizationSettings) super.getSettings();
    }

    /**
     * To be overloaded
     *
     */
    @Override
    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                super.buildGuiceModules(),
                Stream.of(new OntopOptimizationModule(this)));
    }

    /**
     * Can be overloaded by sub-classes
     */
    @Override
    protected ImmutableMap<Class<? extends QueryOptimizationProposal>, Class<? extends InternalProposalExecutor>>
    generateOptimizationConfigurationMap() {
        ImmutableMap.Builder<Class<? extends QueryOptimizationProposal>, Class<? extends InternalProposalExecutor>>
                internalExecutorMapBuilder = ImmutableMap.builder();
        internalExecutorMapBuilder.putAll(super.generateOptimizationConfigurationMap());

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

    protected static class DefaultOntopOptimizationBuilderFragment<B extends OntopOptimizationConfiguration.Builder>
            implements OntopOptimizationBuilderFragment<B> {

        private final B builder;

        /**
         * For sub-classes ONLY!
         */
        protected DefaultOntopOptimizationBuilderFragment() {
            builder = (B) this;
        }

        /**
         * When not inheriting
         */
        protected DefaultOntopOptimizationBuilderFragment(B builder) {
            this.builder = builder;
        }

        protected Properties generateProperties() {
            return new Properties();
        }

        protected final OntopOptimizationConfigurationOptions generateOntopOptimizationConfigurationOptions(
                OntopModelConfigurationOptions modelOptions) {
            return new OntopOptimizationConfigurationOptions(modelOptions);
        }

    }

    /**
     * Inherits from DefaultOntopModelBuilderFragment because it has more methods
     * than DefaultOntopOptimizationBuilderFragment (more convenient).
     */
    protected static abstract class AbstractOntopOptimizationBuilderMixin<B extends OntopOptimizationConfiguration.Builder>
            extends DefaultOntopModelBuilderFragment<B>
            implements OntopOptimizationConfiguration.Builder<B> {

        private final DefaultOntopOptimizationBuilderFragment<B> optimizationBuilderFragment;

        protected AbstractOntopOptimizationBuilderMixin() {
            optimizationBuilderFragment = new DefaultOntopOptimizationBuilderFragment<>((B)this);
        }

        @Override
        protected Properties generateProperties() {
            // Properties from OntopModelBuilderFragmentImpl
            Properties properties = super.generateProperties();
            // Higher priority (however should be orthogonal) for the OntopOptimizationBuilderFragment.
            properties.putAll(optimizationBuilderFragment.generateProperties());

            return properties;
        }

        protected OntopOptimizationConfigurationOptions generateOntopOptimizationConfigurationOptions() {
            OntopModelConfigurationOptions modelOptions = generateModelOptions();
            return optimizationBuilderFragment.generateOntopOptimizationConfigurationOptions(modelOptions);
        }
    }


    public final static class BuilderImpl<B extends OntopOptimizationConfiguration.Builder>
            extends AbstractOntopOptimizationBuilderMixin<B> {

        @Override
        public OntopOptimizationConfiguration build() {
            Properties properties = generateProperties();

            OntopOptimizationConfigurationOptions options = generateOntopOptimizationConfigurationOptions();
            OntopOptimizationSettings settings = new OntopOptimizationSettingsImpl(properties);

            return new OntopOptimizationConfigurationImpl(settings, options);
        }
    }
}
