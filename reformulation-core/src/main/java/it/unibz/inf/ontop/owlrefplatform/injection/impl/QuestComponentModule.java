package it.unibz.inf.ontop.owlrefplatform.injection.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.executor.expression.PushDownExpressionExecutor;
import it.unibz.inf.ontop.executor.groundterm.GroundTermRemovalFromDataNodeExecutor;
import it.unibz.inf.ontop.executor.join.InnerJoinExecutor;
import it.unibz.inf.ontop.executor.merging.QueryMergingExecutor;
import it.unibz.inf.ontop.executor.pullout.PullVariableOutOfDataNodeExecutor;
import it.unibz.inf.ontop.executor.pullout.PullVariableOutOfSubTreeExecutor;
import it.unibz.inf.ontop.executor.substitution.SubstitutionPropagationExecutor;
import it.unibz.inf.ontop.executor.union.UnionLiftInternalExecutor;
import it.unibz.inf.ontop.executor.unsatisfiable.RemoveEmptyNodesExecutor;
import it.unibz.inf.ontop.injection.impl.OBDAAbstractModule;
import it.unibz.inf.ontop.owlrefplatform.core.DBConnector;
import it.unibz.inf.ontop.owlrefplatform.core.IQuest;
import it.unibz.inf.ontop.owlrefplatform.core.QueryCache;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;
import it.unibz.inf.ontop.owlrefplatform.core.translator.MappingVocabularyFixer;
import it.unibz.inf.ontop.owlrefplatform.injection.QuestComponentFactory;
import it.unibz.inf.ontop.owlrefplatform.injection.QuestCoreConfiguration;
import it.unibz.inf.ontop.owlrefplatform.injection.QuestCorePreferences;
import it.unibz.inf.ontop.pivotalrepr.OptimizationConfiguration;

/**
 * TODO: describe
 */
public class QuestComponentModule extends OBDAAbstractModule {

    // Temporary
    private QuestCoreConfiguration configuration;

    protected QuestComponentModule(QuestCoreConfiguration configuration) {
        super(configuration.getPreferences());
        this.configuration = configuration;
    }

    @Override
    protected void configureCoreConfiguration() {
        super.configureCoreConfiguration();
        bind(QuestCorePreferences.class).toInstance((QuestCorePreferences) getPreferences());
        bind(OptimizationConfiguration.class).toInstance(configuration.getOptimizationConfiguration());
    }

    @Override
    protected void configure() {
        configureCoreConfiguration();

        bindTMappingExclusionConfig();

        Module componentFactoryModule = buildFactory(ImmutableList.<Class>of(IQuest.class,
                        NativeQueryGenerator.class, DBConnector.class),
                QuestComponentFactory.class);
        install(componentFactoryModule);
        bindFromPreferences(MappingVocabularyFixer.class);
        bindFromPreferences(QueryCache.class);
        // Executors
        bindFromPreferences(InnerJoinExecutor.class);
        bindFromPreferences(SubstitutionPropagationExecutor.class);
        bindFromPreferences(PushDownExpressionExecutor.class);
        bindFromPreferences(GroundTermRemovalFromDataNodeExecutor.class);
        bindFromPreferences(PullVariableOutOfDataNodeExecutor.class);
        bindFromPreferences(PullVariableOutOfSubTreeExecutor.class);
        bindFromPreferences(RemoveEmptyNodesExecutor.class);
        bindFromPreferences(QueryMergingExecutor.class);
        bindFromPreferences(UnionLiftInternalExecutor.class);


        // Releases the configuration (enables some GC)
        this.configuration = null;
    }

    private void bindTMappingExclusionConfig() {
        TMappingExclusionConfig tMappingExclusionConfig = configuration.getTmappingExclusions()
                .orElseGet(TMappingExclusionConfig::empty);

        bind(TMappingExclusionConfig.class).toInstance(tMappingExclusionConfig);
    }
}
