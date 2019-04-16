package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.IntStream;

/**
 * For AND, OR, etc.
 */
public abstract class AbstractDBBooleanConnectorFunctionSymbol extends DBBooleanFunctionSymbolImpl {

    protected AbstractDBBooleanConnectorFunctionSymbol(String name, int arity, DBTermType dbBooleanTermType) {
        super(name + arity,
                IntStream.range(0, arity)
                        .boxed()
                        .map(i -> dbBooleanTermType)
                        .collect(ImmutableCollectors.toList()),
                dbBooleanTermType);
    }

    @Override
    protected boolean tolerateNulls() {
        return true;
    }

    /**
     * Propagates the info that 2VL can be applied to the sub-expressions.
     */
    @Override
    public ImmutableTerm simplify2VL(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory,
                                     VariableNullability variableNullability) {
        ImmutableList<ImmutableTerm> newSubTerms = terms.stream()
                .map(t -> (t instanceof ImmutableExpression)
                        ? ((ImmutableExpression) t).simplify2VL(variableNullability)
                        : t)
                .collect(ImmutableCollectors.toList());

        return buildTermAfterEvaluation(newSubTerms, termFactory, variableNullability);
    }
}
