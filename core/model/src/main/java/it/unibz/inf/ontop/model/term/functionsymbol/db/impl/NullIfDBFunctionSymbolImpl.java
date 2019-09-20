package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.IncrementalEvaluation;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;
import java.util.stream.Stream;

public class NullIfDBFunctionSymbolImpl extends AbstractArgDependentTypedDBFunctionSymbol {

    protected static final String NULLIF_STR = "NULLIF";
    private final DBFunctionSymbolSerializer serializer;

    protected NullIfDBFunctionSymbolImpl(String name, DBTermType rootDBTermType, DBFunctionSymbolSerializer serializer) {
        super(name, ImmutableList.of(rootDBTermType, rootDBTermType));
        this.serializer = serializer;
    }

    protected NullIfDBFunctionSymbolImpl(DBTermType rootDBTermType) {
        this(NULLIF_STR, rootDBTermType, Serializers.getRegularSerializer(NULLIF_STR));
    }

    @Override
    protected Stream<? extends ImmutableTerm> extractPossibleValues(ImmutableList<? extends ImmutableTerm> terms) {
        return Stream.of(terms.get(0));
    }

    @Override
    public boolean isPreferringToBePostProcessedOverBeingBlocked() {
        return false;
    }

    @Override
    public String getNativeDBString(
            ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter,
            TermFactory termFactory) {
        return serializer.getNativeDBString(terms, termConverter, termFactory);
    }

    @Override
    protected boolean tolerateNulls() {
        return true;
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return true;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    /**
     * Currently cannot be post-processed as we are not sure which kind of equality is considered by the DB engine
     * TODO: experiment
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        ImmutableTerm term1 = newTerms.get(0);
        ImmutableTerm term2 = newTerms.get(1);

        if (term1.isNull() || term2.isNull())
            return term1;
        // As the equality is probably non-strict (TODO: CHECK for most DBs), we only optimize the case of obvious equality
        if (term1.equals(term2))
            return termFactory.getNullConstant();

        return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    @Override
    public IncrementalEvaluation evaluateIsNotNull(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory,
                                                   VariableNullability variableNullability) {
        ImmutableTerm term1 = terms.get(0);
        ImmutableTerm term2 = terms.get(1);

        return termFactory.getConjunction(
                termFactory.getDBIsNotNull(term1),
                termFactory.getDisjunction(
                        termFactory.getDBIsNull(term2),
                        termFactory.getDBNot(termFactory.getDBNonStrictDefaultEquality(term1, term2))))
                .evaluate(variableNullability, true);
    }
}
