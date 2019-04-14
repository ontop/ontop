package it.unibz.inf.ontop.model.term.functionsymbol;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
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
     * Can further simplify than the simplify(...) because here FALSE can be treated as equivalent (2-valued logic)
     *
     * By default, reuses simplify(...).
     */
    default ImmutableTerm simplify2VL(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory,
                              VariableNullability variableNullability) {
        return simplify(terms, termFactory, variableNullability);
    }
}
