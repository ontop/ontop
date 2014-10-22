package org.semanticweb.ontop.injection;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import org.semanticweb.ontop.io.PrefixManager;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.OBDAModel;

import java.util.Properties;

public class OBDACoreModule extends OBDAAbstractModule {

    public OBDACoreModule(OBDAProperties configuration) {
        super(configuration);
    }

    @Override
    protected void configure() {
        configureBasically();

        Module nativeQLFactoryModule = buildFactory(ImmutableList.<Class>of(
                        OBDAModel.class,
                        MappingParser.class,
                        PrefixManager.class),
                NativeQueryLanguageComponentFactory.class);
        install(nativeQLFactoryModule);

        bind(OBDAFactoryWithException.class).to(OBDAFactoryWithExceptionImpl.class);
    }
}
