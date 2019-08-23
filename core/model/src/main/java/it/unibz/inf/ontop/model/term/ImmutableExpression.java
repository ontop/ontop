package it.unibz.inf.ontop.model.term;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.IncompatibleTermException;
import it.unibz.inf.ontop.model.term.functionsymbol.OperationPredicate;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;

public interface ImmutableExpression extends ImmutableFunctionalTerm {

    @Override
    OperationPredicate getFunctionSymbol();

    /**
     * Flattens AND expressions.
     */
    ImmutableSet<ImmutableExpression> flattenAND();

    /**
     * Flattens OR expressions.
     */
    ImmutableSet<ImmutableExpression> flattenOR();

    /**
     * Generalization of flattening (AND, OR, etc.).
     *
     * It is the responsibility of the caller to make sure such a flattening makes sense.
     */
    ImmutableSet<ImmutableExpression> flatten(OperationPredicate operator);

    boolean isVar2VarEquality();

    /**
     * TODO: inject termFactory and typeFactory
     *
     */
    default Optional<TermType> getOptionalTermType() throws IncompatibleTermException {
        try {
            OperationPredicate predicate = getFunctionSymbol();
            return predicate.inferType(getTerms());
        } catch (IncompatibleTermException e) {
            throw new IncompatibleTermException(this, e);
        }
    }

    default Optional<TermType> getOptionalTermType(ImmutableList<Optional<TermType>> actualArgumentTypes) {
        try {
            OperationPredicate predicate = getFunctionSymbol();
            return predicate.inferTypeFromArgumentTypes(actualArgumentTypes);
        } catch (IncompatibleTermException e) {
            throw new IncompatibleTermException(this, e);
        }
    }
}
