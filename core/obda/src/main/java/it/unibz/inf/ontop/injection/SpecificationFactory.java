package it.unibz.inf.ontop.injection;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.OBDASpecification;

/**
 * To be build by Guice (Assisted inject pattern)
 */
public interface SpecificationFactory {

    PrefixManager createPrefixManager(ImmutableMap<String, String> prefixToURIMap);

    MappingMetadata createMetadata(PrefixManager prefixManager, UriTemplateMatcher templateMatcher);

    Mapping createMapping(MappingMetadata metadata, ImmutableMap<AtomPredicate, IntermediateQuery> mappingMap, ExecutorRegistry executorRegistry);

    OBDASpecification createSpecification(Mapping saturatedMapping, DBMetadata dbMetadata, TBoxReasoner tBox,
                                          ImmutableOntologyVocabulary vocabulary);
}
