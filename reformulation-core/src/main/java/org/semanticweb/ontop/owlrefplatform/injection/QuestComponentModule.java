package org.semanticweb.ontop.owlrefplatform.injection;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import com.google.inject.util.Providers;
import org.semanticweb.ontop.injection.*;
import org.semanticweb.ontop.owlrefplatform.core.IQuest;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;
import org.semanticweb.ontop.owlrefplatform.core.translator.MappingVocabularyFixer;
import org.semanticweb.ontop.sql.ImplicitDBConstraints;

/**
 *
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

        Module componentFactoryModule = buildFactory(ImmutableList.<Class>of(IQuest.class,
                        NativeQueryGenerator.class),
                QuestComponentFactory.class);
        install(componentFactoryModule);
        bindFromPreferences(MappingVocabularyFixer.class);
    }

    private void bindImplicitDBConstraints() {
        ImplicitDBConstraints dbContraints = (ImplicitDBConstraints) getPreferences().get(QuestPreferences.DB_CONSTRAINTS);
        if (dbContraints == null)
            bind(ImplicitDBConstraints.class).toProvider(Providers.<ImplicitDBConstraints>of(null));
        else
            bind(ImplicitDBConstraints.class).toInstance(dbContraints);
    }
}
