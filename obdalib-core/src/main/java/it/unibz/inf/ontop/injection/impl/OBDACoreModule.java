package it.unibz.inf.ontop.injection.impl;

import com.google.inject.util.Providers;
import it.unibz.inf.ontop.injection.OBDACoreConfiguration;
import it.unibz.inf.ontop.injection.OBDASettings;
import it.unibz.inf.ontop.sql.ImplicitDBConstraintsReader;
import it.unibz.inf.ontop.utils.IMapping2DatalogConverter;

import java.util.Optional;

public class OBDACoreModule extends OntopAbstractModule {

    // Keeps track of the config until module configuration
    private OBDACoreConfiguration configuration;

    protected OBDACoreModule(OBDACoreConfiguration configuration) {
        super(configuration.getSettings());
        this.configuration = configuration;
    }

    @Override
    protected void configureCoreConfiguration() {
        super.configureCoreConfiguration();
        bind(OBDASettings.class).toInstance((OBDASettings)getProperties());
    }


    @Override
    protected void configure() {
        configureCoreConfiguration();

        // Forgets the configuration (useful for the GC in case of large input objects)
        this.configuration = null;
    }
}
