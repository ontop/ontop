package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The first sub-term encodes the index of the term to return.
 * Such values correspond to the following sub-terms
 *
 * For instance DB_IDX(1, "roger", "francis", "ernest") returns "francis"
 *
 */
public class DBIntIndexFunctionSymbolImpl extends AbstractArgDependentTypedDBFunctionSymbol {

    protected DBIntIndexFunctionSymbolImpl(DBTermType dbIntegerType, DBTermType rootDBType, int nbEntries) {
        super("DB_IDX_" + nbEntries,
                Stream.concat(
                        Stream.of(dbIntegerType),
                        IntStream.range(0, nbEntries)
                                .mapToObj(i -> rootDBType))
                        .collect(ImmutableCollectors.toList()));
        if (nbEntries == 0) {
            throw new IllegalArgumentException("nbEntries must be positive");
        }
    }

    /**
     * Should provoke a fatal error when its argument is NULL
     */
    @Override
    protected boolean tolerateNulls() {
        return true;
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }

    @Override
    protected Stream<? extends ImmutableTerm> extractPossibleValues(ImmutableList<? extends ImmutableTerm> terms) {
        return IntStream.range(2, terms.size())
                .filter(i -> i % 2 == 0)
                .mapToObj(terms::get);
    }

    @Override
    public boolean isPreferringToBePostProcessedOverBeingBlocked() {
        return true;
    }


    @Override
    public ImmutableTerm simplify(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory,
                                  VariableNullability variableNullability) {
        ImmutableTerm idTerm = terms.get(0)
                .simplify(variableNullability);

        if (idTerm instanceof NonNullConstant) {
            NonNullConstant constant = (NonNullConstant) idTerm;
            if (!constant.getType().equals(termFactory.getTypeFactory().getDBTypeFactory().getDBLargeIntegerType()))
                throw new MinorOntopInternalBugException("The idTerm was expecting to a be an integer");

            int index = Integer.parseInt(constant.getValue()) + 1;
            if ((index < 0) || (index >= getArity()))
                throw new MinorOntopInternalBugException("Invalid index in idTerm");

            return terms.get(index)
                    .simplify(variableNullability);
        }

        // Simplifies the terms and so on
        return super.simplify(terms, termFactory, variableNullability);
    }

    /**
     * Encodes it as a SQL case
     */
    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter,
                                    TermFactory termFactory) {
        ImmutableTerm subTerm = terms.get(0);

        ImmutableFunctionalTerm caseTerm = termFactory.getDBCase(
                IntStream.range(0, getArity() - 1)
                        .mapToObj(i -> Maps.immutableEntry(
                                termFactory.getStrictEquality(subTerm, termFactory.getDBIntegerConstant(i)),
                                terms.get(i + 1))),
                // TODO: find a way to cause a fatal error for the default case (instead of returning NULL)
                termFactory.getNullConstant(), false);

        return termConverter.apply(caseTerm);
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    public IncrementalEvaluation evaluateStrictEq(ImmutableList<? extends ImmutableTerm> terms, ImmutableTerm otherTerm,
                                                  TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableTerm indexTerm = terms.get(0);

        /*
         * Same function symbol, same index term
         *  --> simplifies into a disjunction
         */
        if ((otherTerm instanceof ImmutableFunctionalTerm)
                && equals(((ImmutableFunctionalTerm) otherTerm).getFunctionSymbol())
                && ((ImmutableFunctionalTerm) otherTerm).getTerm(0).equals(indexTerm)) {
            ImmutableList<? extends ImmutableTerm> otherTerms = ((ImmutableFunctionalTerm) otherTerm).getTerms();

            ImmutableExpression disjunction = termFactory.getDisjunction(
                    IntStream.range(1, getArity())
                            .mapToObj(i -> termFactory.getConjunction(
                                    termFactory.getStrictEquality(indexTerm, termFactory.getDBIntegerConstant(i)),
                                    termFactory.getStrictEquality(terms.get(i), otherTerms.get(i+1)))))
                    .orElseThrow(() -> new MinorOntopInternalBugException("Arity > 1 was expected"));

            return disjunction
                    .evaluate(variableNullability, true);
        }
        else
            return super.evaluateStrictEq(terms, otherTerm, termFactory, variableNullability);
    }
}
