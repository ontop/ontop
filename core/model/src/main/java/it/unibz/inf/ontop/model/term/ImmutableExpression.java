package it.unibz.inf.ontop.model.term;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.model.term.functionsymbol.OperationPredicate;
import it.unibz.inf.ontop.model.type.TypeInference;


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

    default TypeInference getOptionalTermType(ImmutableList<TypeInference> actualArgumentTypes) {
        try {
            OperationPredicate predicate = getFunctionSymbol();
            return predicate.inferTypeFromArgumentTypes(actualArgumentTypes);
        } catch (FatalTypingException e) {
            throw new FatalTypingException(this, e);
        }
    }
}
