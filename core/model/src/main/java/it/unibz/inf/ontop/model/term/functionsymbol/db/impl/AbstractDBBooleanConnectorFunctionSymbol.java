package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
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
                        .mapToObj(i -> dbBooleanTermType)
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

        ImmutableList<ImmutableTerm> termsAfterInteraction = simplify2VLInteractions(newSubTerms, termFactory,
                variableNullability);

        return buildTermAfterEvaluation(termsAfterInteraction, termFactory, variableNullability);
    }

    protected abstract ImmutableList<ImmutableTerm> simplify2VLInteractions(
            ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory, VariableNullability variableNullability);

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

        // Removes duplicates to avoid returning false in cases like OR2(IS_NOT_NULL(b),IS_NOT_NULL(b))
        ImmutableList<ImmutableTerm> distinctTerms = newTerms.stream()
                .distinct()
                .collect(ImmutableCollectors.toList());

        // { index -> termToNullify }
        ImmutableMap<Integer, ImmutableTerm> termToNullifyMap = IntStream.range(0, distinctTerms.size())
                .mapToObj(i -> Maps.immutableEntry(i, Optional.of(distinctTerms.get(i))
                        .filter(t -> t instanceof ImmutableExpression)
                        .map(t -> (ImmutableExpression) t)
                        .filter(e -> (e.getFunctionSymbol() instanceof DBIsNullOrNotFunctionSymbol)
                                && (((DBIsNullOrNotFunctionSymbol) e.getFunctionSymbol()).isTrueWhenNull() == lookForIsNull))
                        .map(e -> e.getTerm(0))))
                .filter(e -> e.getValue().isPresent())
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().get()));

        return termToNullifyMap.entrySet().stream()
                .reduce(distinctTerms,
                        (ts, e) -> IntStream.range(0, distinctTerms.size())
                                .mapToObj(i -> e.getKey().equals(i)
                                        ? ts.get(i)
                                        // Only tries to nullify other entries.
                                        // NB: nullifying only replaces the same syntactic term by NULL
                                        : Nullifiers.nullify(ts.get(i), e.getValue(), termFactory)
                                            .simplify(variableNullability))
                                .collect(ImmutableCollectors.toList()),
                        (ts1, ts2) -> {
                            throw new MinorOntopInternalBugException("No merging was expected");
                        });
    }
}
