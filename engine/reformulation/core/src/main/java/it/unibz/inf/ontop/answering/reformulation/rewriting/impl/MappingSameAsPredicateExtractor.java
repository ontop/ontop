package it.unibz.inf.ontop.answering.reformulation.rewriting.impl;

import it.unibz.inf.ontop.spec.mapping.Mapping;
import org.apache.commons.rdf.api.IRI;

/**
 * TODO: find a better name
 */
public interface MappingSameAsPredicateExtractor {

    /**
     * TODO: what about the case where only the object deserves a SameAs rewriting?
     */
    interface SameAsTargets {

        boolean isSubjectOnlySameAsRewritingTarget(IRI pred);

        boolean isTwoArgumentsSameAsRewritingTarget(IRI pred);
    }

    SameAsTargets extract(Mapping mapping);
}
