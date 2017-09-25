package it.unibz.inf.ontop.model.type;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.IncompatibleTermException;
import it.unibz.inf.ontop.model.term.ImmutableTerm;

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
    Optional<TermType> inferType(List<? extends ImmutableTerm> terms, ImmutableList<Optional<TermType>> expectedBaseTypes)
            throws IncompatibleTermException;

    /**
     * TODO: explain
     *
     */
    Optional<TermType> inferTypeFromArgumentTypes(ImmutableList<Optional<TermType>> actualArgumentTypes,
                                                  ImmutableList<Optional<TermType>> expectedBaseTypes)
            throws IncompatibleTermException;
}
