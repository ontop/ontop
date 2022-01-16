package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBAndFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBStrictEqFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.substitution.ProtoSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DefaultDBAndFunctionSymbol extends AbstractDBBooleanConnectorFunctionSymbol implements DBAndFunctionSymbol {

    private final String argumentSeparator;

    protected DefaultDBAndFunctionSymbol(String nameInDialect, int arity, DBTermType dbBooleanTermType) {
        super(nameInDialect, arity, dbBooleanTermType);
        if (arity < 2)
            throw new IllegalArgumentException("Arity must be >= 2");
        this.argumentSeparator = String.format(" %s ", nameInDialect);
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

        DBConstant falseValue = termFactory.getDBBooleanConstant(false);
        if (simplifiedTerms.stream()
                .anyMatch(falseValue::equals))
            return falseValue;

        Optional<ImmutableTerm> optionalNull = simplifiedTerms.stream()
                .filter(t -> (t instanceof Constant) && t.isNull())
                .findFirst();

        ImmutableList<ImmutableExpression> others = simplifiedTerms.stream()
                .map(t -> (t instanceof Variable) ? termFactory.getIsTrue((Variable)t) : t)
                // We don't care about TRUE
                .filter(t -> (t instanceof ImmutableExpression))
                .map(t -> (ImmutableExpression) t)
                // Flattens nested ANDs
                .flatMap(t -> (t.getFunctionSymbol() instanceof DBAndFunctionSymbol)
                        ? t.getTerms().stream()
                            .map(s -> (ImmutableExpression)s)
                        : Stream.of(t))
                .distinct()
                .collect(ImmutableCollectors.toList());

        return others.isEmpty()
                ? optionalNull.orElseGet(() -> termFactory.getDBBooleanConstant(true))
                :  optionalNull
                .map(n -> (ImmutableTerm) termFactory.getFalseOrNullFunctionalTerm(others))
                .orElseGet(() -> others.size() == 1 ? others.get(0) : termFactory.getConjunction(others));
    }

    /**
     * Look at the interaction between conjuncts
     */
    protected ImmutableList<ImmutableTerm> simplifyInteractions(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                              VariableNullability variableNullability) {
        return simplifyIsNullOrIsNotNull(newTerms, termFactory, variableNullability, true);
    }

    @Override
    protected ImmutableList<ImmutableTerm> simplify2VLInteractions(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory, VariableNullability variableNullability) {
        return simplifyStrictEqConstants(newTerms, termFactory, variableNullability);
    }

    private ImmutableList<ImmutableTerm> simplifyStrictEqConstants(ImmutableList<ImmutableTerm> initialTerms,
                                                                   TermFactory termFactory,
                                                                   VariableNullability variableNullability) {
        return IntStream.range(0, initialTerms.size())
                .boxed()
                .reduce(initialTerms,
                        (ts, i) -> simplifyStrictEqConstant(ts, i, termFactory, variableNullability),
                        (ts1, ts2) -> {throw new MinorOntopInternalBugException("No merge expected"); });
    }

    /**
     * TODO: also consider variable to variable strict equalities and arities > 2
     */
    private ImmutableList<ImmutableTerm> simplifyStrictEqConstant(ImmutableList<ImmutableTerm> terms, int i,
                                                                  TermFactory termFactory, VariableNullability variableNullability) {
        Optional<ProtoSubstitution<NonNullConstant>> substitution = Optional.of(terms.get(i))
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .map(t -> (ImmutableFunctionalTerm) t)
                .filter(t -> t.getFunctionSymbol() instanceof DBStrictEqFunctionSymbol)
                .map(ImmutableFunctionalTerm::getTerms)
                .filter(eqTerms -> eqTerms.stream().anyMatch(t -> t instanceof Variable))
                .filter(eqTerms -> eqTerms.stream().anyMatch(t -> t instanceof NonNullConstant))
                .map(eqTerms -> ImmutableMap.of(
                        eqTerms.stream()
                                .filter(t -> t instanceof Variable)
                                .map(t -> (Variable) t)
                                .findAny()
                                .get(),
                        eqTerms.stream()
                                .filter(t -> t instanceof NonNullConstant)
                                .map(t -> (NonNullConstant) t)
                                .findAny()
                                .get()))
                .map(termFactory::getProtoSubstitution);

        return substitution
                .map(s -> IntStream.range(0, terms.size())
                        .mapToObj(j -> i == j
                                ? terms.get(i)
                                : s.apply(terms.get(j)).simplify(variableNullability))
                        .collect(ImmutableCollectors.toList()))
                .orElse(terms);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return inBrackets(terms.stream()
                        .map(termConverter)
                        .collect(Collectors.joining(argumentSeparator)));
    }

    @Override
    public boolean blocksNegation() {
        return false;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        return termFactory.getDisjunction(
                subTerms.stream()
                    .map(t -> (ImmutableExpression) t)
                    .map(t -> t.negate(termFactory))
                    .collect(ImmutableCollectors.toList()));
    }
}
