package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DefaultDBConcatFunctionSymbol extends AbstractTypedDBFunctionSymbol implements DBConcatFunctionSymbol {

    private static final String FUNCTIONAL_TEMPLATE = "%s(%s)";
    private final String nameInDialect;

    protected DefaultDBConcatFunctionSymbol(String nameInDialect, int arity, DBTermType dbStringType, DBTermType rootDBTermType) {
        super(nameInDialect + arity,
                // No restriction on the input types TODO: check if OK
                IntStream.range(0, arity)
                .boxed()
                .map(i -> (TermType) rootDBTermType)
                .collect(ImmutableCollectors.toList()), dbStringType);
        this.nameInDialect = nameInDialect;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public Optional<ImmutableFunctionalTerm.InjectivityDecomposition> analyzeInjectivity(
            ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonFreeVariables,
            VariableNullability variableNullability, VariableGenerator variableGenerator, TermFactory termFactory) {

        if (arguments.stream()
                .filter(t -> (!(t instanceof GroundTerm)) || (!((GroundTerm) t).isDeterministic()))
                .filter(t -> !nonFreeVariables.contains(t))
                .count() <= 1) {
            return Optional.of(decomposeInjectiveTopFunctionalTerm(arguments, nonFreeVariables, variableNullability,
                    variableGenerator, termFactory));
        } else
            return Optional.empty();

    }

    /**
     * TODO: allow post-processing
     * @param arguments
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String parameterString = terms.stream()
                .map(termConverter::apply)
                .collect(Collectors.joining(","));
        return String.format(FUNCTIONAL_TEMPLATE, nameInDialect, parameterString);
    }
}
