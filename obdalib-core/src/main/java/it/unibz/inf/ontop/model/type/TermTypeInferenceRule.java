package it.unibz.inf.ontop.model.type;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.Predicate.COL_TYPE;
import it.unibz.inf.ontop.model.Term;

import java.util.List;
import java.util.Optional;

/**
 * TODO: explain
 */
public interface TermTypeInferenceRule {

    /**
     * TODO: explain
     *
     */
    Optional<TermType> inferType(List<Term> terms, ImmutableList<COL_TYPE> expectedBaseTypes) throws TermTypeException;
}
