package it.unibz.inf.ontop.spec.trans.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.mapping.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.bascioperations.LegacyMappingVocabularyValidator;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.spec.trans.MappingEquivalenceFreeRewriter;

import java.util.stream.Stream;

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
            Stream<CQIE> rules = mapping2DatalogConverter.convert(mapping);
            ImmutableList<CQIE> updatedRules = new LegacyMappingVocabularyValidator(tBox, vocabulary)
                    .replaceEquivalences(rules);
            return datalog2MappingConverter.convertMappingRules(updatedRules, dbMetadata, mapping.getExecutorRegistry(),
                    mapping.getMetadata());
        }
        return mapping;
    }
}
