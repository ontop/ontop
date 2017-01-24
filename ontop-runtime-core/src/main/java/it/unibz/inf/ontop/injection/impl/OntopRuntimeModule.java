package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.injection.OntopRuntimeConfiguration;
import it.unibz.inf.ontop.injection.OntopRuntimeSettings;
import it.unibz.inf.ontop.injection.ReformulationFactory;
import it.unibz.inf.ontop.reformulation.unfolding.QueryUnfolder;

public class OntopRuntimeModule extends OntopAbstractModule {

    private final OntopRuntimeSettings settings;

    protected OntopRuntimeModule(OntopRuntimeConfiguration configuration) {
        super(configuration.getSettings());
        settings = configuration.getSettings();
    }

    @Override
    protected void configure() {
        bind(OntopRuntimeSettings.class).toInstance(settings);

        Module reformulationFactoryModule = buildFactory(
                ImmutableList.of(
                        QueryUnfolder.class),
                ReformulationFactory.class);

        install(reformulationFactoryModule);
    }
}
