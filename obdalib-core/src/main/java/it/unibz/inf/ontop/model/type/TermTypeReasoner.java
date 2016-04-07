package it.unibz.inf.ontop.model.type;

import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.model.Term;

import java.util.List;
import java.util.Optional;

/**
 * TODO: explain
 *
 * TODO: find a better name
 */
public interface TermTypeReasoner {

    /**
     * TODO: explain
     *
     */
    Optional<TermType> inferType(List<Term> terms, Predicate.COL_TYPE[] expectedBaseTypes) throws TermTypeError;
}
