package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.injection.MappingFactory;
import it.unibz.inf.ontop.injection.OntopOBDAConfiguration;
import it.unibz.inf.ontop.injection.OntopOBDASettings;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.MappingMetadata;

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
                MappingMetadata.class,
                Mapping.class
                ),
                MappingFactory.class);
        install(mappingFactoryModule);
    }
}
