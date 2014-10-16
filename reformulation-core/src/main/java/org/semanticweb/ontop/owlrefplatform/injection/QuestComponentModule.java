package org.semanticweb.ontop.owlrefplatform.injection;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.semanticweb.ontop.injection.OntopAbstractModule;
import org.semanticweb.ontop.owlrefplatform.core.Quest;
import org.semanticweb.ontop.owlrefplatform.core.QuestImpl;

import java.util.Properties;

/**
 *
 */
public class QuestComponentModule extends OntopAbstractModule {

    public QuestComponentModule(Properties configuration) {
        super(configuration);
    }

    @Override
    protected void configure() {
        FactoryModuleBuilder builder = new FactoryModuleBuilder();

        //TODO: use the configuration instead (and consider more classes)
        builder.implement(Quest.class, QuestImpl.class);

        install(builder.build(QuestComponentFactory.class));
    }


}
