package it.unibz.inf.ontop.answering.reformulation.rewriting.impl;

import it.unibz.inf.ontop.spec.mapping.Mapping;
import org.apache.commons.rdf.api.IRI;

public interface MappingSameAsPredicateExtractor {

    interface Result {

        boolean isSubjectOnlySameAsRewritingTarget(IRI pred);

        boolean isTwoArgumentsSameAsRewritingTarget(IRI pred);
    }

    MappingSameAsPredicateExtractorImpl.Result extract(Mapping mapping);
}
