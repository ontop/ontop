package it.unibz.inf.ontop.injection;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import com.google.inject.util.Providers;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.mapping.MappingParser;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.nativeql.DBMetadataExtractor;
import it.unibz.inf.ontop.sql.ImplicitDBConstraints;
import it.unibz.inf.ontop.utils.IMapping2DatalogConverter;

public class OBDACoreModule extends OBDAAbstractModule {

    public OBDACoreModule(OBDAProperties configuration) {
        super(configuration);
    }

    private void bindImplicitDBConstraints() {
        ImplicitDBConstraints dbContraints = (ImplicitDBConstraints) getPreferences().get(OBDAProperties.DB_CONSTRAINTS);
        if (dbContraints == null)
            bind(ImplicitDBConstraints.class).toProvider(Providers.<ImplicitDBConstraints>of(null));
        else
            bind(ImplicitDBConstraints.class).toInstance(dbContraints);
    }

    @Override
    protected void configure() {
        configurePreferences();

        bindImplicitDBConstraints();

        Module nativeQLFactoryModule = buildFactory(ImmutableList.<Class>of(
                        OBDAModel.class,
                        MappingParser.class,
                        PrefixManager.class,
                        DBMetadataExtractor.class,
                        OBDAMappingAxiom.class,
                        IMapping2DatalogConverter.class
                        ),
                NativeQueryLanguageComponentFactory.class);
        install(nativeQLFactoryModule);

        bind(OBDAFactoryWithException.class).to(OBDAFactoryWithExceptionImpl.class);
    }
}
