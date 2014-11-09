package org.semanticweb.ontop.owlrefplatform.injection;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import org.semanticweb.ontop.injection.*;
import org.semanticweb.ontop.owlrefplatform.core.Quest;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;

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

        Module componentFactoryModule = buildFactory(ImmutableList.<Class>of(Quest.class,
                        NativeQueryGenerator.class),
                QuestComponentFactory.class);
        install(componentFactoryModule);
    }


}
