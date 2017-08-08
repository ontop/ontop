package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OntopMappingSQLTemporalConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSQLTemporalSettings;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.spec.TOBDASpecInput;
import it.unibz.inf.ontop.spec.dbschema.PreProcessedImplicitRelationalDBConstraintExtractor;
import it.unibz.inf.ontop.spec.dbschema.RDBMetadataExtractor;
import it.unibz.inf.ontop.spec.impl.TOBDASpecInputImpl;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.mapping.TemporalMappingExtractor;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.parser.TemporalMappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMappingConverter;
import it.unibz.inf.ontop.spec.mapping.pp.TemporalPPMappingConverter;

/**
 * Created by elem on 08/08/2017.
 */
public class OntopTemporalModule extends OntopAbstractModule{

    private final OntopMappingSQLTemporalSettings settings;

    protected OntopTemporalModule(OntopMappingSQLTemporalConfiguration configuration) {
        super(configuration.getSettings());
        settings = configuration.getSettings();
    }

    @Override
    protected void configure() {
        bind(OntopMappingSQLTemporalSettings.class).toInstance(settings);

        bindFromPreferences(SQLPPMappingFactory.class);
        bindFromPreferences(SQLMappingParser.class);
        bindFromPreferences(SQLPPMappingConverter.class);
        bindFromPreferences(PreProcessedImplicitRelationalDBConstraintExtractor.class);
        bindFromPreferences(MappingExtractor.class);
        bindFromPreferences(TemporalMappingExtractor.class);
        bindFromPreferences(TemporalMappingParser.class);
        bindFromPreferences(TemporalPPMappingConverter.class);

        Module nativeQLFactoryModule = buildFactory(
                ImmutableList.of(RDBMetadataExtractor.class),
                NativeQueryLanguageComponentFactory.class);
        install(nativeQLFactoryModule);
    }
}
