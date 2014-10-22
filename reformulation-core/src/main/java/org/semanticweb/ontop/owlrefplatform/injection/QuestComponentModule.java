package org.semanticweb.ontop.owlrefplatform.injection;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.semanticweb.ontop.injection.*;
import org.semanticweb.ontop.io.PrefixManager;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.OBDAModel;
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

        Module componentFactoryModule = buildFactory(ImmutableList.<Class>of(Quest.class),
                QuestComponentFactory.class);
        install(componentFactoryModule);
    }


}
