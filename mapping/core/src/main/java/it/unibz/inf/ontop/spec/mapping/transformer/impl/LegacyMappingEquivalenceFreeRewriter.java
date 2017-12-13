package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.spec.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.datalog.impl.LegacyMappingVocabularyValidator;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingEquivalenceFreeRewriter;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

public class LegacyMappingEquivalenceFreeRewriter implements MappingEquivalenceFreeRewriter {

    private final boolean enabled;
    private final Mapping2DatalogConverter mapping2DatalogConverter;
    private final Datalog2QueryMappingConverter datalog2MappingConverter;

    @Inject
    private LegacyMappingEquivalenceFreeRewriter(OntopMappingSettings settings, Mapping2DatalogConverter
            mapping2DatalogConverter, Datalog2QueryMappingConverter datalog2MappingConverter) {
        this.enabled = settings.isEquivalenceOptimizationEnabled();
        this.mapping2DatalogConverter = mapping2DatalogConverter;
        this.datalog2MappingConverter = datalog2MappingConverter;
    }

    @Override
    public Mapping rewrite(Mapping mapping, TBoxReasoner tBox, ImmutableOntologyVocabulary vocabulary, DBMetadata dbMetadata) {
        if (enabled) {

//            ImmutableList<CQIE> rules = mapping2DatalogConverter.convert(mapping).collect(ImmutableCollectors.toList());

//            ImmutableList<CQIE> updatedRules = rules;
            //        new LegacyMappingVocabularyValidator(tBox, vocabulary).replaceEquivalences(rules);
//            return datalog2MappingConverter.convertMappingRules(updatedRules, dbMetadata, mapping.getExecutorRegistry(),
//                    mapping.getMetadata());
        }
        return mapping;
    }
}
