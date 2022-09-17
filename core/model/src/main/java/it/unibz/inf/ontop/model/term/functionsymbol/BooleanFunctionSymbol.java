package it.unibz.inf.ontop.model.term.functionsymbol;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;

public interface BooleanFunctionSymbol extends FunctionSymbol {

    /**
     * Returns TRUE if the NOT operator has to stay ABOVE the expression
     *  (i.e. cannot lead to a change in the operator or cannot be delegated to sub-expressions)
     */
    boolean blocksNegation();

    /**
     * Usually NOT supported when the function symbol blocks negation. Please use ImmutableExpression.negate() instead.
     */
    ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory);

    /**
     * Can further simplify than the simplify(...) because here FALSE can be treated as equivalent to NULL (2-valued logic)
     *
     * By default, reuses simplify(...).
     */
    default ImmutableTerm simplify2VL(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory,
                              VariableNullability variableNullability) {
        ImmutableTerm newTerm = simplify(terms, termFactory, variableNullability);

        // Makes sure that the returned expression has been informed that "2VL simplifications" can be applied
        // Prevents an infinite loop
        if (newTerm instanceof ImmutableExpression) {
            ImmutableExpression newExpression = (ImmutableExpression) newTerm;
            if ((!this.equals(newExpression.getFunctionSymbol())) || (!terms.equals(newExpression.getTerms()))) {
                return newExpression.simplify2VL(variableNullability);
            }
        }
        else if (newTerm.isNull())
            return termFactory.getDBBooleanConstant(false);
        return newTerm;
    }
}
