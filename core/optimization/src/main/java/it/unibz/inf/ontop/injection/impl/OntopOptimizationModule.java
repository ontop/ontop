package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.datalog.UnionFlattener;
import it.unibz.inf.ontop.injection.OntopOptimizationConfiguration;
import it.unibz.inf.ontop.injection.OntopOptimizationSettings;
import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.injection.OptimizerFactory;
import it.unibz.inf.ontop.iq.optimizer.*;
import it.unibz.inf.ontop.iq.optimizer.splitter.PreventDistinctProjectionSplitter;
import it.unibz.inf.ontop.iq.planner.QueryPlanner;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.iq.transformer.*;
import it.unibz.inf.ontop.iq.lens.LensUnfolder;
import it.unibz.inf.ontop.iq.visitor.RequiredExtensionalDataNodeExtractor;

public class OntopOptimizationModule extends OntopAbstractModule {

    private OntopOptimizationConfiguration configuration;

    protected OntopOptimizationModule(OntopOptimizationConfiguration configuration) {
        super(configuration.getSettings());
        // Temporary (will be dropped)
        this.configuration = configuration;
    }


    @Override
    protected void configure() {
        bind(OntopOptimizationSettings.class).toInstance(configuration.getSettings());

        // Executors
        bindFromSettings(UnionBasedQueryMerger.class);
        bindFromSettings(JoinLikeOptimizer.class);
        bindFromSettings(UnionAndBindingLiftOptimizer.class);
        bindFromSettings(UnionFlattener.class);
        bindFromSettings(TermTypeTermLifter.class);
        bindFromSettings(OrderBySimplifier.class);
        bindFromSettings(AggregationSimplifier.class);
        bindFromSettings(PostProcessableFunctionLifter.class);
        bindFromSettings(InnerJoinIQOptimizer.class);
        bindFromSettings(LeftJoinIQOptimizer.class);
        bindFromSettings(BooleanExpressionPushDownTransformer.class);
        bindFromSettings(EmptyRowsValuesNodeTransformer.class);
        bindFromSettings(GeneralStructuralAndSemanticIQOptimizer.class);
        bindFromSettings(QueryPlanner.class);
        bindFromSettings(SelfJoinSameTermIQOptimizer.class);
        bindFromSettings(RequiredExtensionalDataNodeExtractor.class);
        bindFromSettings(SelfJoinUCIQOptimizer.class);
        bindFromSettings(RedundantJoinFKOptimizer.class);
        bindFromSettings(BelowDistinctJoinWithClassUnionOptimizer.class);
        bindFromSettings(LensUnfolder.class);
        bindFromSettings(AggregationSplitter.class);
        bindFromSettings(FlattenLifter.class);
        bindFromSettings(FilterLifter.class);
        bindFromSettings(BooleanExpressionPushDownOptimizer.class);
        bindFromSettings(PreventDistinctOptimizer.class);
        bindFromSettings(PreventDistinctProjectionSplitter.class);

        bind(OptimizationSingletons.class).to(OptimizationSingletonsImpl.class);

        Module optimizerModule = buildFactory(ImmutableList.of(
                ExplicitEqualityTransformer.class,
                TermTypeTermLiftTransformer.class,
                DefinitionPushDownTransformer.class),
                OptimizerFactory.class);
        install(optimizerModule);
        // Releases the configuration (enables some GC)
        this.configuration = null;
    }
}
