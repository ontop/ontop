package it.unibz.inf.ontop.substitution.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionOperations;
import it.unibz.inf.ontop.substitution.UnifierBuilder;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;

public abstract class AbstractSubstitutionOperations<T extends ImmutableTerm> extends AbstractSubstitutionBasicOperations<T> implements SubstitutionOperations<T> {

    private final Function<Variable, T> typeCast;
    private final Function<Substitution<Variable>, Substitution<? extends T>> substitutionTypeCast;
    private final Function<Map.Entry<Variable, T>, T> keyMapper;

    /**
     * @param typeCast             effectively ensures that Variable is a subtype of T (normally, it's simply v -> v)
     * @param substitutionTypeCast effectively ensures that Variable is a subtype of T (normally, it's simply s -> s)
     */
    AbstractSubstitutionOperations(TermFactory termFactory,
                                   Function<Variable, T> typeCast,
                                   Function<Substitution<Variable>, Substitution<? extends T>> substitutionTypeCast) {
        super(termFactory);
        this.typeCast = typeCast;
        this.substitutionTypeCast = substitutionTypeCast;
        this.keyMapper = e -> typeCast.apply(e.getKey());
    }

    @Override
    public T apply(Substitution<? extends T> substitution, Variable variable) {
        return applyToVariable(substitution, variable, typeCast);
    }

    @Override
    public ImmutableFunctionalTerm apply(Substitution<? extends T> substitution, ImmutableFunctionalTerm term) {
        if (term.getFunctionSymbol() instanceof BooleanFunctionSymbol)
            return apply(substitution, (ImmutableExpression)term);

        if (substitution.isEmpty())
            return term;

        return termFactory.getImmutableFunctionalTerm(term.getFunctionSymbol(), substitution.applyToTerms(term.getTerms()));
    }

    @Override
    public ImmutableExpression apply(Substitution<? extends T> substitution, ImmutableExpression expression) {
        if (substitution.isEmpty())
            return expression;

        return termFactory.getImmutableExpression(expression.getFunctionSymbol(), substitution.applyToTerms(expression.getTerms()));
    }

    @Override
    public ImmutableList<T> apply(Substitution<? extends T> substitution, ImmutableList<? extends Variable> variables) {
        return variables.stream()
                .map(v -> apply(substitution, v))
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public ImmutableSet<T> apply(Substitution<? extends T> substitution, ImmutableSet<? extends Variable> terms) {
        return terms.stream()
                .map(v -> apply(substitution, v))
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public T rename(Substitution<Variable> renaming, T t) {
        return applyToTerm(substitutionTypeCast.apply(renaming), t);
    }


    protected Substitution<T> emptySubstitution() {
        return termFactory.getSubstitution(ImmutableMap.of());
    }

    @Override
    public AbstractUnifierBuilder<T> unifierBuilder() {
        return unifierBuilder(emptySubstitution());
    }

    @Override // ensures that there is no cast in toUnifier()
    public abstract AbstractUnifierBuilder<T> unifierBuilder(Substitution<T> substitution);

    @Override
    public Collector<Substitution<T>, ?, Optional<Substitution<T>>> toUnifier() {
        return Collector.of(
                this::unifierBuilder,
                (a, s) -> a.unify(s.stream(), keyMapper, Map.Entry::getValue),
                AbstractUnifierBuilder::merge,
                UnifierBuilder::build);
    }


    private static final class ArgumentMapUnifierImpl<T extends ImmutableTerm> implements ArgumentMapUnifier<T> {
        private final ImmutableMap<Integer, ? extends T> argumentMap;
        private final Substitution<T> substitution;

        ArgumentMapUnifierImpl(ImmutableMap<Integer, ? extends T> argumentMap, Substitution<T> substitution) {
            this.argumentMap = argumentMap;
            this.substitution = substitution;
        }

        @Override
        public  ImmutableMap<Integer, ? extends T> getArgumentMap() {
            return argumentMap;
        }

        @Override
        public Substitution<T> getSubstitution() {
            return substitution;
        }

        @Override
        public String toString() { return argumentMap + " with " + substitution; }
    }


    private final class ArgumentMapUnifierBuilder {
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private Optional<ArgumentMapUnifier<T>> optional;

        ArgumentMapUnifierBuilder() {
            optional = Optional.of(new ArgumentMapUnifierImpl<>(ImmutableMap.of(), emptySubstitution()));
        }

        void unify(ImmutableMap<Integer, ? extends T> argumentMap) {
            if (optional.isEmpty())
                return;

            ArgumentMapUnifier<T> unifier = optional.get();

            ImmutableMap<Integer, T> updatedArgumentMap = applyToTerms(unifier.getSubstitution(), argumentMap);

            Optional<Substitution<T>> optionalUpdatedSubstitution = unifierBuilder()
                    .unify(
                            Sets.intersection(unifier.getArgumentMap().keySet(), updatedArgumentMap.keySet()).stream(),
                            unifier.getArgumentMap()::get,
                            updatedArgumentMap::get)
                    .build();

            optional = optionalUpdatedSubstitution
                    .flatMap(u -> unifierBuilder(unifier.getSubstitution())
                            .unify(u.stream(), keyMapper, Map.Entry::getValue)
                            .build()
                            .map(s -> new ArgumentMapUnifierImpl<>(
                                    applyToTerms(u, ExtensionalDataNode.union(unifier.getArgumentMap(), updatedArgumentMap)),
                                    s)));
        }

        ArgumentMapUnifierBuilder merge(ArgumentMapUnifierBuilder another) {
            throw new MinorOntopInternalBugException("Not expected to be run in parallel");
        }

        Optional<ArgumentMapUnifier<T>> build() {
            return optional;
        }
     }

    @Override
    public Collector<ImmutableMap<Integer, ? extends T>, ?, Optional<ArgumentMapUnifier<T>>> toArgumentMapUnifier() {
        return Collector.of(ArgumentMapUnifierBuilder::new, ArgumentMapUnifierBuilder::unify, ArgumentMapUnifierBuilder::merge, ArgumentMapUnifierBuilder::build);
    }
}
