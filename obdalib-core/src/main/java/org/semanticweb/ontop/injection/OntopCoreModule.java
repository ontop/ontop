package org.semanticweb.ontop.injection;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.semanticweb.ontop.io.PrefixManager;
import org.semanticweb.ontop.io.SQLMappingParser;
import org.semanticweb.ontop.io.SimplePrefixManager;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.OBDAModelImpl;

import java.util.Properties;

public class OntopCoreModule extends OntopAbstractModule {

    public OntopCoreModule(Properties configuration) {
        super(configuration);
    }

    @Override
    protected void configure() {
        FactoryModuleBuilder nativeQLbuilder = new FactoryModuleBuilder();

        //TODO: use the configuration instead (and consider more classes)
        nativeQLbuilder.implement(OBDAModel.class, OBDAModelImpl.class)
                .implement(MappingParser.class, SQLMappingParser.class)
                .implement(PrefixManager.class, SimplePrefixManager.class);

        install(nativeQLbuilder.build(NativeQueryLanguageComponentFactory.class));
    }
}
