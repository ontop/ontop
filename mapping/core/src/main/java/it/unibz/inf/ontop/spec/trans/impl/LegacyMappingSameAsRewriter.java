package it.unibz.inf.ontop.spec.trans.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.mapping.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.MappingSameAs;
import it.unibz.inf.ontop.spec.trans.MappingSameAsRewriter;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

public class LegacyMappingSameAsRewriter implements MappingSameAsRewriter{

    private final boolean enabled;
    private final Mapping2DatalogConverter mapping2DatalogConverter;
    private final Datalog2QueryMappingConverter datalog2MappingConverter;

    @Inject
    private LegacyMappingSameAsRewriter(OntopMappingSettings settings, Mapping2DatalogConverter mapping2DatalogConverter,
                                  Datalog2QueryMappingConverter datalog2MappingConverter) {
        this.enabled = settings.isSameAsInMappingsEnabled();
        this.mapping2DatalogConverter = mapping2DatalogConverter;
        this.datalog2MappingConverter = datalog2MappingConverter;
    }

    @Override
    public Mapping rewrite(Mapping mapping, DBMetadata dbMetadata) {
        if(enabled){
            ImmutableList<CQIE> rules = mapping2DatalogConverter.convert(mapping)
                    .collect(ImmutableCollectors.toList());
            ImmutableList<CQIE> updatedRules = MappingSameAs.addSameAsInverse(rules);
            return datalog2MappingConverter.convertMappingRules(updatedRules, dbMetadata, mapping.getExecutorRegistry(),
                    mapping.getMetadata());
        }
        return mapping;
    }
}
