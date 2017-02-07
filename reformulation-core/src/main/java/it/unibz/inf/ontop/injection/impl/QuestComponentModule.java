package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.owlrefplatform.core.QueryCache;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.owlrefplatform.core.translator.MappingVocabularyFixer;

/**
 * TODO: describe
 */
public class QuestComponentModule extends OntopAbstractModule {

    // Temporary
    private QuestCoreConfiguration configuration;

    protected QuestComponentModule(QuestCoreConfiguration configuration) {
        super(configuration.getSettings());
        this.configuration = configuration;
    }

    @Override
    protected void configureCoreConfiguration() {
        super.configureCoreConfiguration();
        bind(QuestCoreSettings.class).toInstance((QuestCoreSettings) getProperties());
    }

    @Override
    protected void configure() {
        configureCoreConfiguration();
    }
}
