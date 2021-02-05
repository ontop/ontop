package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBOrFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultDBOrFunctionSymbol extends AbstractDBBooleanConnectorFunctionSymbol implements DBOrFunctionSymbol {

    private final String argumentSeparator;

    protected DefaultDBOrFunctionSymbol(String nameInDialect, int arity, DBTermType dbBooleanTermType) {
        super(nameInDialect, arity, dbBooleanTermType);
        if (arity < 2)
            throw new IllegalArgumentException("Arity must be >= 2");
        this.argumentSeparator = String.format(" %s ", nameInDialect);
    }

    @Override
    protected ImmutableList<ImmutableTerm> simplify2VLInteractions(ImmutableList<ImmutableTerm> newTerms,
                                                                   TermFactory termFactory,
                                                                   VariableNullability variableNullability) {
        return newTerms;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    /**
     * NB: terms are assumed to be either TRUE, FALSE, NULL or ImmutableExpressions.
     * TODO: check it
     */
    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableList<ImmutableTerm> simplifiedTerms = simplifyInteractions(newTerms, termFactory, variableNullability);
        return computeNewDisjunction(simplifiedTerms, termFactory);
    }

    /**
     * Temporarily public
     * TODO: hide it again
     */
    public static ImmutableTerm computeNewDisjunction(ImmutableList<ImmutableTerm> evaluatedTerms, TermFactory termFactory) {


        DBConstant trueValue = termFactory.getDBBooleanConstant(true);
        if (evaluatedTerms.stream()
                .anyMatch(trueValue::equals))
            return trueValue;

        Optional<ImmutableTerm> optionalNull = evaluatedTerms.stream()
                .filter(t -> (t instanceof Constant) && ((Constant) t).isNull())
                .findFirst();

        ImmutableList<ImmutableExpression> others = evaluatedTerms.stream()
                .map(t -> (t instanceof Variable) ? termFactory.getIsTrue((Variable)t) : t)
                // We don't care about FALSE
                .filter(t -> (t instanceof ImmutableExpression))
                .map(t -> (ImmutableExpression) t)
                // Flattens nested ORs
                .flatMap(t -> (t.getFunctionSymbol() instanceof DBOrFunctionSymbol)
                        ? t.getTerms().stream()
                            .map(s -> (ImmutableExpression)s)
                        : Stream.of(t))
                .distinct()
                .collect(ImmutableCollectors.toList());

        return others.isEmpty()
                ? optionalNull.orElseGet(() -> termFactory.getDBBooleanConstant(false))
                :  optionalNull
                .map(n -> (ImmutableTerm) termFactory.getTrueOrNullFunctionalTerm(others))
                .orElseGet(() -> others.size() == 1 ? others.get(0) : termFactory.getDisjunction(others));
    }

    /**
     * Look at the interaction between disjuncts
     */
    protected ImmutableList<ImmutableTerm> simplifyInteractions(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                                VariableNullability variableNullability) {
        return simplifyIsNullOrIsNotNull(newTerms, termFactory, variableNullability, false);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return inBrackets(terms.stream()
                        .map(termConverter::apply)
                        .collect(Collectors.joining(argumentSeparator)));
    }

    @Override
    public boolean blocksNegation() {
        return false;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        return termFactory.getConjunction(
                subTerms.stream()
                    .map(t -> (ImmutableExpression) t)
                    .map(t -> t.negate(termFactory))
                    .collect(ImmutableCollectors.toList()));
    }
}
