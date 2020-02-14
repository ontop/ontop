package it.unibz.inf.ontop.model.atom;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.type.TermType;

/**
 * TODO: explain
 *
 */
public interface AtomPredicate extends Predicate {

    /**
     * Returns a base term type for each of its arguments.
     *
     * Since a base type may not type precisely the argument (can be very abstract),
     * it is expected to be used for VALIDATION ONLY (that is detecting obvious inconsistencies),
     * NOT FOR precise type inference.
     *
     */
    @JsonIgnore
    ImmutableList<TermType> getBaseTypesForValidation();
}
