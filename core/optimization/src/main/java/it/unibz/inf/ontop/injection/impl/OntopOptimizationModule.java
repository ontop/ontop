package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.executor.expression.PushDownBooleanExpressionExecutor;
import it.unibz.inf.ontop.executor.expression.PushUpBooleanExpressionExecutor;
import it.unibz.inf.ontop.executor.groundterm.GroundTermRemovalFromDataNodeExecutor;
import it.unibz.inf.ontop.executor.join.InnerJoinExecutor;
import it.unibz.inf.ontop.executor.leftjoin.LeftJoinExecutor;
import it.unibz.inf.ontop.executor.merging.QueryMergingExecutor;
import it.unibz.inf.ontop.executor.projection.ProjectionShrinkingExecutor;
import it.unibz.inf.ontop.executor.pullout.PullVariableOutOfDataNodeExecutor;
import it.unibz.inf.ontop.executor.pullout.PullVariableOutOfSubTreeExecutor;
import it.unibz.inf.ontop.executor.substitution.SubstitutionPropagationExecutor;
import it.unibz.inf.ontop.executor.truenode.TrueNodeRemovalExecutor;
import it.unibz.inf.ontop.executor.union.UnionLiftExecutor;
import it.unibz.inf.ontop.executor.unsatisfiable.RemoveEmptyNodesExecutor;
import it.unibz.inf.ontop.injection.OntopOptimizationConfiguration;
import it.unibz.inf.ontop.injection.OntopOptimizationSettings;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.InnerJoinOptimizer;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.JoinLikeOptimizer;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.LeftJoinOptimizer;
import it.unibz.inf.ontop.iq.datalog.DatalogProgram2QueryConverter;
import it.unibz.inf.ontop.iq.tools.QueryUnionSplitter;

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
        bindFromPreferences(InnerJoinExecutor.class);
        bindFromPreferences(SubstitutionPropagationExecutor.class);
        bindFromPreferences(PushDownBooleanExpressionExecutor.class);
        bindFromPreferences(PushUpBooleanExpressionExecutor.class);
        bindFromPreferences(GroundTermRemovalFromDataNodeExecutor.class);
        bindFromPreferences(PullVariableOutOfDataNodeExecutor.class);
        bindFromPreferences(PullVariableOutOfSubTreeExecutor.class);
        bindFromPreferences(RemoveEmptyNodesExecutor.class);
        bindFromPreferences(QueryMergingExecutor.class);
        bindFromPreferences(UnionLiftExecutor.class);
        bindFromPreferences(LeftJoinExecutor.class);
        bindFromPreferences(ProjectionShrinkingExecutor.class);
        bindFromPreferences(TrueNodeRemovalExecutor.class);
        bindFromPreferences(DatalogProgram2QueryConverter.class);
        bindFromPreferences(QueryUnionSplitter.class);
        bindFromPreferences(InnerJoinOptimizer.class);
        bindFromPreferences(JoinLikeOptimizer.class);
        bindFromPreferences(LeftJoinOptimizer.class);

        // Releases the configuration (enables some GC)
        this.configuration = null;
    }
}
