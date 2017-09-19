package it.unibz.inf.ontop.answering.reformulation.rewriting.impl;

import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;

public interface MappingSameAsPredicateExtractor {

    interface Result {

        boolean isSubjectOnlySameAsRewritingTarget(Predicate pred);

        boolean isTwoArgumentsSameAsRewritingTarget(Predicate pred);
    }

    MappingSameAsPredicateExtractorImpl.Result extract(Mapping mapping);
}
