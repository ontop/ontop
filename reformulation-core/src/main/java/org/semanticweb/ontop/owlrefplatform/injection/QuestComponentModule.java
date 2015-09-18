package org.semanticweb.ontop.owlrefplatform.injection;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import com.google.inject.util.Providers;
import org.semanticweb.ontop.injection.*;
import org.semanticweb.ontop.owlrefplatform.core.DBConnector;
import org.semanticweb.ontop.owlrefplatform.core.IQuest;
import org.semanticweb.ontop.owlrefplatform.core.QueryCache;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import org.semanticweb.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;
import org.semanticweb.ontop.owlrefplatform.core.translator.MappingVocabularyFixer;
import org.semanticweb.ontop.sql.ImplicitDBConstraints;

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

        bindImplicitDBConstraints();
        bindTMappingExclusionConfig();

        Module componentFactoryModule = buildFactory(ImmutableList.<Class>of(IQuest.class,
                        NativeQueryGenerator.class, DBConnector.class),
                QuestComponentFactory.class);
        install(componentFactoryModule);
        bindFromPreferences(MappingVocabularyFixer.class);
        bindFromPreferences(QueryCache.class);
    }

    private void bindImplicitDBConstraints() {
        ImplicitDBConstraints dbContraints = (ImplicitDBConstraints) getPreferences().get(QuestPreferences.DB_CONSTRAINTS);
        if (dbContraints == null)
            bind(ImplicitDBConstraints.class).toProvider(Providers.<ImplicitDBConstraints>of(null));
        else
            bind(ImplicitDBConstraints.class).toInstance(dbContraints);
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
