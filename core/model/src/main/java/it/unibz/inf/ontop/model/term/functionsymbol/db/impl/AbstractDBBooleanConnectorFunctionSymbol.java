package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonFunctionalTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIsNullOrNotFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
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

    /**
     * Requires its arguments to be expressions
     */
    @Override
    protected ImmutableList<? extends ImmutableTerm> transformIntoRegularArguments(
            ImmutableList<? extends NonFunctionalTerm> arguments, TermFactory termFactory) {
        return arguments.stream()
                .map(termFactory::getIsTrue)
                .collect(ImmutableCollectors.toList());
    }


    /**
     * Look for conjuncts that are IS_NULL(...) or disjuncts that are IS_NOT_NULL(...)
     * and uses them to nullify some terms
     */
    protected ImmutableList<ImmutableTerm> simplifyIsNullOrIsNotNull(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                                     VariableNullability variableNullability,
                                                                     boolean lookForIsNull) {

        // { index -> termToNullify }
        ImmutableMap<Integer, ImmutableTerm> termToNullifyMap = IntStream.range(0, newTerms.size())
                .boxed()
                .map(i -> Maps.immutableEntry(i, Optional.of(newTerms.get(i))
                        .filter(t -> t instanceof ImmutableExpression)
                        .map(t -> (ImmutableExpression) t)
                        .filter(e -> (e.getFunctionSymbol() instanceof DBIsNullOrNotFunctionSymbol)
                                && (e.isNull() == lookForIsNull))
                        .map(e -> e.getTerm(0))))
                .filter(e -> e.getValue().isPresent())
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().get()));

        return termToNullifyMap.entrySet().stream()
                .reduce(newTerms,
                        (ts, e) -> IntStream.range(0, newTerms.size())
                                .boxed()
                                .map(i -> e.getKey().equals(i)
                                        ? ts.get(i)
                                        // Only tries to nullify other entries
                                        : Nullifiers.nullify(ts.get(i), e.getValue(), termFactory)
                                            .simplify(variableNullability))
                                .collect(ImmutableCollectors.toList()),
                        (ts1, ts2) -> {
                            throw new MinorOntopInternalBugException("No merging was expected");
                        });
    }
}
