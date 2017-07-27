package it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing;

import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.impl.MappingSameAsPredicateExtractorImpl;

public interface MappingSameAsPredicateExtractor {

    interface Result {

        boolean isSubjectOnlySameAsRewritingTarget(Predicate pred);

        boolean isTwoArgumentsSameAsRewritingTarget(Predicate pred);
    }

    MappingSameAsPredicateExtractorImpl.Result extract(Mapping mapping);
}
