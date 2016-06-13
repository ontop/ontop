package it.unibz.inf.ontop.owlrefplatform.injection;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.injection.OBDAAbstractModule;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.owlrefplatform.core.DBConnector;
import it.unibz.inf.ontop.owlrefplatform.core.IQuest;
import it.unibz.inf.ontop.owlrefplatform.core.QueryCache;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;
import it.unibz.inf.ontop.owlrefplatform.core.translator.MappingVocabularyFixer;

/**
 * TODO: describe
 */
public class QuestComponentModule extends OBDAAbstractModule {

    public QuestComponentModule(QuestPreferences configuration) {
        super(configuration);
    }

    @Override
    protected void configurePreferences() {
        super.configurePreferences();
        bind(QuestPreferences.class).toInstance((QuestPreferences) getPreferences());
    }

    @Override
    protected void configure() {
        configurePreferences();

        bindTMappingExclusionConfig();

        Module componentFactoryModule = buildFactory(ImmutableList.<Class>of(IQuest.class,
                        NativeQueryGenerator.class, DBConnector.class),
                QuestComponentFactory.class);
        install(componentFactoryModule);
        bindFromPreferences(MappingVocabularyFixer.class);
        bindFromPreferences(QueryCache.class);
    }

    private void bindTMappingExclusionConfig() {
        TMappingExclusionConfig tMappingExclusionConfig = (TMappingExclusionConfig) getPreferences().get(
                QuestPreferences.TMAPPING_EXCLUSION);
        if (tMappingExclusionConfig == null) {
            tMappingExclusionConfig = TMappingExclusionConfig.empty();
        }
        bind(TMappingExclusionConfig.class).toInstance(tMappingExclusionConfig);
    }
}
