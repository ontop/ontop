package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OBDAFactoryWithException;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.mapping.MappingParser;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.nativeql.DBMetadataExtractor;

public class OntopMappingModule extends OntopAbstractModule {

    private final OntopMappingSettings settings;

    OntopMappingModule(OntopMappingConfiguration configuration) {
        super(configuration.getSettings());
        this.settings = configuration.getSettings();
    }

    @Override
    protected void configure() {
        bind(OntopMappingSettings.class).toInstance(settings);

        bindFromPreferences(OBDAFactoryWithException.class);

        Module nativeQLFactoryModule = buildFactory(ImmutableList.<Class>of(
                MappingParser.class,
                DBMetadataExtractor.class,
                OBDAMappingAxiom.class
                ),
                NativeQueryLanguageComponentFactory.class);
        install(nativeQLFactoryModule);

        // TODO: continue
    }
}
