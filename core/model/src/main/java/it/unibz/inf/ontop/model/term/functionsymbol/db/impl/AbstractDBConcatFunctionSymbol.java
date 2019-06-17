package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

public abstract class AbstractDBConcatFunctionSymbol extends AbstractTypedDBFunctionSymbol implements DBConcatFunctionSymbol {

    private final DBFunctionSymbolSerializer serializer;

    protected AbstractDBConcatFunctionSymbol(String nameInDialect, int arity, DBTermType dbStringType,
                                             DBTermType rootDBTermType, DBFunctionSymbolSerializer serializer) {
        super(nameInDialect + arity,
                // No restriction on the input types TODO: check if OK
                IntStream.range(0, arity)
                        .boxed()
                        .map(i -> (TermType) rootDBTermType)
                        .collect(ImmutableCollectors.toList()), dbStringType);
        this.serializer = serializer;
    }

    @Override
    public Optional<ImmutableFunctionalTerm.FunctionalTermDecomposition> analyzeInjectivity(
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

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializer.getNativeDBString(terms, termConverter, termFactory);
    }
}
