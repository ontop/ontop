package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.injection.OntopMappingSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.spec.dbschema.ImplicitDBConstraintsProviderFactory;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMappingConverter;
import it.unibz.inf.ontop.spec.mapping.pp.impl.MetaMappingExpander;

public class OntopMappingSQLModule extends OntopAbstractModule {

    private final OntopMappingSQLSettings settings;

    protected OntopMappingSQLModule(OntopMappingSQLConfiguration configuration) {
        super(configuration.getSettings());
        settings = configuration.getSettings();
    }

    @Override
    protected void configure() {
        bind(OntopMappingSQLSettings.class).toInstance(settings);

        bindFromSettings(SQLPPMappingFactory.class);
        bindFromSettings(SQLMappingParser.class);
        bindFromSettings(SQLPPMappingConverter.class);
        bindFromSettings(ImplicitDBConstraintsProviderFactory.class);
        bindFromSettings(MappingExtractor.class);
        bindFromSettings(MetaMappingExpander.class);
        bindFromSettings(SQLPPSourceQueryFactory.class);
    }
}
