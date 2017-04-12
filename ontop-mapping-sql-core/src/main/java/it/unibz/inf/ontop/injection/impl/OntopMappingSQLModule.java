package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OBDAFactoryWithException;
import it.unibz.inf.ontop.injection.OntopMappingSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.mapping.SQLMappingParser;
import it.unibz.inf.ontop.mapping.conversion.SQLPPMapping2OBDASpecificationConverter;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.nativeql.RDBMetadataExtractor;
import it.unibz.inf.ontop.owlrefplatform.core.translator.MappingVocabularyFixer;

public class OntopMappingSQLModule extends OntopAbstractModule {


    private final OntopMappingSQLSettings settings;

    protected OntopMappingSQLModule(OntopMappingSQLConfiguration configuration) {
        super(configuration.getSettings());
        settings = configuration.getSettings();
    }

    @Override
    protected void configure() {
        bind(OntopMappingSQLSettings.class).toInstance(settings);

        bindFromPreferences(OBDAFactoryWithException.class);
        bindFromPreferences(SQLMappingParser.class);
        bindFromPreferences(SQLPPMapping2OBDASpecificationConverter.class);
        bindFromPreferences(MappingVocabularyFixer.class);

        Module nativeQLFactoryModule = buildFactory(ImmutableList.of(
                RDBMetadataExtractor.class,
                OBDAMappingAxiom.class
                ),
                NativeQueryLanguageComponentFactory.class);
        install(nativeQLFactoryModule);
    }
}
