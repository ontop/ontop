package org.semanticweb.ontop.owlrefplatform.injection;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.semanticweb.ontop.injection.OBDAAbstractModule;
import org.semanticweb.ontop.injection.OBDAProperties;
import org.semanticweb.ontop.owlrefplatform.core.Quest;
import org.semanticweb.ontop.owlrefplatform.core.QuestImpl;

import java.util.Properties;

/**
 *
 */
public class QuestComponentModule extends OBDAAbstractModule {

    public QuestComponentModule(OBDAProperties configuration) {
        super(configuration);
    }

    @Override
    protected void configure() {
        configureBasically();

        FactoryModuleBuilder builder = new FactoryModuleBuilder();

        //TODO: use the configuration instead (and consider more classes)
        builder.implement(Quest.class, QuestImpl.class);

        install(builder.build(QuestComponentFactory.class));
    }


}
