package it.unibz.inf.ontop.model;

import it.unibz.inf.ontop.model.type.TermTypeException;

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
    default Optional<TermType> getOptionalTermType() throws TermTypeException {
        try {
            OperationPredicate predicate = getFunctionSymbol();
            return predicate.getTermTypeInferenceRule().inferType(
                    getTerms(), predicate.getArgumentTypes());
        } catch (TermTypeException e) {
            throw new TermTypeException(this, e);
        }
    }
}
