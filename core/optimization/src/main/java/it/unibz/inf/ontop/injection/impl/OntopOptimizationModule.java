package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.datalog.DatalogProgram2QueryConverter;
import it.unibz.inf.ontop.datalog.IntermediateQuery2DatalogTranslator;
import it.unibz.inf.ontop.injection.OntopOptimizationConfiguration;
import it.unibz.inf.ontop.injection.OntopOptimizationSettings;
import it.unibz.inf.ontop.iq.executor.construction.ConstructionNodeCleaningExecutor;
import it.unibz.inf.ontop.iq.executor.expression.PushDownBooleanExpressionExecutor;
import it.unibz.inf.ontop.iq.executor.expression.PushUpBooleanExpressionExecutor;
import it.unibz.inf.ontop.iq.executor.groundterm.GroundTermRemovalFromDataNodeExecutor;
import it.unibz.inf.ontop.iq.executor.join.InnerJoinExecutor;
import it.unibz.inf.ontop.iq.executor.leftjoin.LeftJoinExecutor;
import it.unibz.inf.ontop.iq.executor.leftjoin.LeftJoinRightChildNormalizationAnalyzer;
import it.unibz.inf.ontop.iq.executor.merging.QueryMergingExecutor;
import it.unibz.inf.ontop.iq.executor.projection.ProjectionShrinkingExecutor;
import it.unibz.inf.ontop.iq.executor.pullout.PullVariableOutOfDataNodeExecutor;
import it.unibz.inf.ontop.iq.executor.pullout.PullVariableOutOfSubTreeExecutor;
import it.unibz.inf.ontop.iq.executor.substitution.SubstitutionPropagationExecutor;
import it.unibz.inf.ontop.iq.executor.truenode.TrueNodeRemovalExecutor;
import it.unibz.inf.ontop.iq.executor.union.FlattenUnionExecutor;
import it.unibz.inf.ontop.iq.executor.union.UnionLiftExecutor;
import it.unibz.inf.ontop.iq.executor.unsatisfiable.RemoveEmptyNodesExecutor;
import it.unibz.inf.ontop.iq.optimizer.BindingLiftOptimizer;
import it.unibz.inf.ontop.iq.optimizer.InnerJoinOptimizer;
import it.unibz.inf.ontop.iq.optimizer.JoinLikeOptimizer;
import it.unibz.inf.ontop.iq.optimizer.LeftJoinOptimizer;
import it.unibz.inf.ontop.iq.tools.QueryUnionSplitter;
import it.unibz.inf.ontop.iq.tools.RootConstructionNodeEnforcer;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;

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
        bindFromSettings(InnerJoinExecutor.class);
        bindFromSettings(SubstitutionPropagationExecutor.class);
        bindFromSettings(PushDownBooleanExpressionExecutor.class);
        bindFromSettings(PushUpBooleanExpressionExecutor.class);
        bindFromSettings(GroundTermRemovalFromDataNodeExecutor.class);
        bindFromSettings(PullVariableOutOfDataNodeExecutor.class);
        bindFromSettings(PullVariableOutOfSubTreeExecutor.class);
        bindFromSettings(RemoveEmptyNodesExecutor.class);
        bindFromSettings(UnionBasedQueryMerger.class);
        bindFromSettings(QueryMergingExecutor.class);
        bindFromSettings(UnionLiftExecutor.class);
        bindFromSettings(LeftJoinExecutor.class);
        bindFromSettings(ProjectionShrinkingExecutor.class);
        bindFromSettings(TrueNodeRemovalExecutor.class);
        bindFromSettings(FlattenUnionExecutor.class);
        bindFromSettings(ConstructionNodeCleaningExecutor.class);
        bindFromSettings(DatalogProgram2QueryConverter.class);
        bindFromSettings(QueryUnionSplitter.class);
        bindFromSettings(InnerJoinOptimizer.class);
        bindFromSettings(JoinLikeOptimizer.class);
        bindFromSettings(LeftJoinOptimizer.class);
        bindFromSettings(BindingLiftOptimizer.class);
        bindFromSettings(IntermediateQuery2DatalogTranslator.class);
        bindFromSettings(LeftJoinRightChildNormalizationAnalyzer.class);
        bindFromSettings(RootConstructionNodeEnforcer.class);

        // Releases the configuration (enables some GC)
        this.configuration = null;
    }
}
