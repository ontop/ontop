package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.injection.OntopOBDAConfiguration;
import it.unibz.inf.ontop.injection.OntopOBDASettings;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.OBDASpecification;

class OntopOBDAModule extends OntopAbstractModule {

    private final OntopOBDASettings settings;

    protected OntopOBDAModule(OntopOBDAConfiguration configuration) {
        super(configuration.getSettings());
        settings = configuration.getSettings();
    }

    @Override
    protected void configure() {
        bind(OntopOBDASettings.class).toInstance(settings);

        Module mappingFactoryModule = buildFactory(ImmutableList.of(
                PrefixManager.class,
                OBDASpecification.class
                ),
                SpecificationFactory.class);
        install(mappingFactoryModule);
    }
}
