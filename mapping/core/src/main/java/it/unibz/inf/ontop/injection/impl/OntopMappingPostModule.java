package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.spec.dbschema.tools.DBMetadataExtractorAndSerializer;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingDistinctTransformer;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingTransformer;
import it.unibz.inf.ontop.spec.OBDASpecificationExtractor;

/**
 * POST-module: to be loaded after that all the dependencies of concrete implementations have been defined
 */
public class OntopMappingPostModule extends OntopAbstractModule {

    protected OntopMappingPostModule(OntopMappingSettings settings) {
        super(settings);
    }

    @Override
    public void configure() {
        bindFromSettings(MappingExtractor.class);
        bindFromSettings(MappingTransformer.class);
        bindFromSettings(MappingDistinctTransformer.class);
        bindFromSettings(OBDASpecificationExtractor.class);
        bindFromSettings(DBMetadataExtractorAndSerializer.class);
    }
}
