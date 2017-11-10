package it.unibz.inf.ontop.model.term;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.functionsymbol.OperationPredicate;
import it.unibz.inf.ontop.exception.IncompatibleTermException;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;

/**
 * An expression has an Operation predicate.
 *
 * Can be used an Effective Boolean Value.
 *
 * https://www.w3.org/TR/sparql11-query/#ebv
 *
 */
public interface Expression extends Function {

    @Override
    OperationPredicate getFunctionSymbol();

    @Override
    Expression clone();

    /**
     * TODO: generalize
     */
    default Optional<TermType> getOptionalTermType() throws IncompatibleTermException {
        try {
            OperationPredicate predicate = getFunctionSymbol();
            return predicate.getTermTypeInferenceRule().inferType(
                    getTerms(), predicate.getArgumentTypes());
        } catch (IncompatibleTermException e) {
            throw new IncompatibleTermException(this, e);
        }
    }

    default Optional<TermType> getOptionalTermType(ImmutableList<Optional<TermType>> actualArgumentTypes) {
        try {
            OperationPredicate predicate = getFunctionSymbol();
            return predicate.getTermTypeInferenceRule().inferTypeFromArgumentTypes(
                    actualArgumentTypes, predicate.getArgumentTypes());
        } catch (IncompatibleTermException e) {
            throw new IncompatibleTermException(this, e);
        }
    }
}
