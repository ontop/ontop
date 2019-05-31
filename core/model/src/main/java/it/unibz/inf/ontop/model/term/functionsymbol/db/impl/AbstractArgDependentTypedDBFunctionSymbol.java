package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class AbstractArgDependentTypedDBFunctionSymbol extends FunctionSymbolImpl implements DBFunctionSymbol {

    protected AbstractArgDependentTypedDBFunctionSymbol(@Nonnull String name,
                                                        @Nonnull ImmutableList<TermType> expectedBaseTypes) {
        super(name, expectedBaseTypes);
    }

    /**
     * Is supposed to be strongly typed: does not compare the types of the possible values because
     * they are supposed to be the sames.
     */
    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {

        ImmutableList<TermTypeInference> typeInferences = extractPossibleValues(terms)
                .map(t -> (t instanceof Variable)
                        ? Optional.of(TermTypeInference.declareRedirectionToVariable((Variable) t))
                        : t.inferType())
                .flatMap(o -> o
                        .map(Stream::of)
                        .orElseGet(Stream::empty))
                .collect(ImmutableCollectors.toList());

        // Gives a preference to type inferences that determine the type (over the ones that redirect it to a variable)
        return typeInferences.stream()
                .filter(t -> t.getTermType().isPresent())
                .map(Optional::of)
                .findAny()
                .orElseGet(() -> typeInferences.stream()
                        .findAny());
    }

    protected abstract Stream<? extends ImmutableTerm> extractPossibleValues(ImmutableList<? extends ImmutableTerm> terms);
}
