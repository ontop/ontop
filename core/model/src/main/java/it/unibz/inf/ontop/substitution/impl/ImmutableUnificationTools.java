package it.unibz.inf.ontop.substitution.impl;

import com.google.common.collect.*;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Tools for new-gen immutable unifying substitutions.
 */
@Singleton
public class ImmutableUnificationTools {

    private final SubstitutionFactory substitutionFactory;

    @Inject
    public ImmutableUnificationTools(SubstitutionFactory substitutionFactory) {
        this.substitutionFactory = substitutionFactory;
    }

    public Optional<ImmutableSubstitution<ImmutableTerm>> computeMGU(ImmutableTerm args1, ImmutableTerm args2) {
        return substitutionFactory.onImmutableTerms().unifierBuilder().unifyTerms(args1, args2).build();
    }


    private VariableOrGroundTermUnifierBuilder<VariableOrGroundTerm> getVariableOrGroundTermUnifierBuilder() {
        ImmutableSubstitution<VariableOrGroundTerm> substitution = substitutionFactory.getSubstitution();
        return new VariableOrGroundTermUnifierBuilder<>(((SubstitutionFactoryImpl)substitutionFactory).termFactory, substitutionFactory.onVariableOrGroundTerms(), substitution);
    }


    public final class ArgumentMapUnification {
        private final ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap;
        private final ImmutableSubstitution<VariableOrGroundTerm> substitution;

        private ArgumentMapUnification(ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap,
                                      ImmutableSubstitution<VariableOrGroundTerm> substitution) {
            this.argumentMap = argumentMap;
            this.substitution = substitution;
        }

        public  ImmutableMap<Integer, ? extends VariableOrGroundTerm> getArgumentMap() {
            return argumentMap;
        }

        public ImmutableSubstitution<VariableOrGroundTerm> getSubstitution() {
            return substitution;
        }

        private Optional<ImmutableUnificationTools.ArgumentMapUnification> unify(
                ImmutableMap<Integer, ? extends VariableOrGroundTerm> newArgumentMap) {

            ImmutableMap<Integer, VariableOrGroundTerm> updatedArgumentMap =
                    substitutionFactory.onVariableOrGroundTerms().applyToTerms(substitution, newArgumentMap);

            Optional<ImmutableSubstitution<VariableOrGroundTerm>> unifier = substitutionFactory.onVariableOrGroundTerms().unifierBuilder()
                            .unifyTermStreams(Sets.intersection(argumentMap.keySet(), updatedArgumentMap.keySet()).stream(), argumentMap::get, updatedArgumentMap::get)
                            .build();

            return unifier
                    .flatMap(u -> substitutionFactory.onVariableOrGroundTerms().unifierBuilder(substitution)
                            .unifyTermStreams(u.entrySet().stream(), Map.Entry::getKey, Map.Entry::getValue)
                            .build()
                            .map(s -> new ArgumentMapUnification(
                                    substitutionFactory.onVariableOrGroundTerms().applyToTerms(u, ExtensionalDataNode.union(argumentMap, updatedArgumentMap)),
                                    s)));
        }
    }

    public Collector<ImmutableSubstitution<VariableOrGroundTerm>, VariableOrGroundTermUnifierBuilder<VariableOrGroundTerm>, Optional<ImmutableSubstitution<VariableOrGroundTerm>>> unifierCollector() {
        return Collector.of(
                this::getVariableOrGroundTermUnifierBuilder,
                (a, s) -> a.unifyTermStreams(s.entrySet().stream(), Map.Entry::getKey, Map.Entry::getValue),
                UnifierBuilder::merge,
                UnifierBuilder::build);
    }

    public Optional<ImmutableUnificationTools.ArgumentMapUnification> getArgumentMapUnifier(
            Stream<ImmutableMap<Integer, ? extends VariableOrGroundTerm>> arguments) {
        return arguments
                .reduce(Optional.of(new ArgumentMapUnification(ImmutableMap.of(), substitutionFactory.getSubstitution())),
                        (o, n) -> o.flatMap(u -> u.unify(n)),
                        (m1, m2) -> {
                            throw new MinorOntopInternalBugException("Not expected to be run in parallel");
                        });
    }



    /**
     * Creates a unifier for args1 and args2
     *
     * The operation is as follows
     *
     * {x/y, m/y} composed with (y,z) is equal to {x/z, m/z, y/z}
     *
     * @return true the substitution (of null if it does not)
     */


    public static abstract class UnifierBuilder<T extends ImmutableTerm, R extends UnifierBuilder<T, R>> {

        private final TermFactory termFactory;
        private final SubstitutionOperations<T> operations;
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private Optional<ImmutableSubstitution<T>> optionalSubstitution;

        UnifierBuilder(TermFactory termFactory, SubstitutionOperations<T> operations, ImmutableSubstitution<T> substitution) {
            this.termFactory = termFactory;
            this.operations = operations;
            optionalSubstitution = Optional.of(substitution);
        }

        protected abstract R self();

        protected R emptySelf() {
            optionalSubstitution = Optional.empty();
            return self();
        }

        public R unifyTermLists(ImmutableList<? extends T> args1, ImmutableList<? extends T> args2) {
            if (args1.size() == args2.size())
                return unifyTermStreams(IntStream.range(0, args1.size()), args1::get, args2::get);

            return emptySelf();
        }

        public R unifyTermStreams(IntStream indexes, IntFunction<? extends T> args1, IntFunction<? extends T> args2) {
            return indexes.collect(
                    this::self,
                    (s, i) -> s.unifyTerms(args1.apply(i), args2.apply(i)),
                    UnifierBuilder::merge);
        }

        public <B> R unifyTermStreams(Stream<B> stream, Function<B, T> args1, Function<B, T> args2) {
            return stream.collect(
                    this::self,
                    (s, i) -> s.unifyTerms(args1.apply(i), args2.apply(i)),
                    UnifierBuilder::merge);
        }

        public R unifyTerms(T t1, T t2) {
            if (optionalSubstitution.isEmpty())
                return self();

            T term1 = operations.applyToTerm(optionalSubstitution.get(), t1);
            T term2 = operations.applyToTerm(optionalSubstitution.get(), t2);

            if (term1.equals(term2))
                return self();

            return unifyUnequalTerms(term1, term2);
        }

        abstract protected R unifyUnequalTerms(T t1, T t2);

        protected R extendSubstitution(Variable variable, T term) {
            ImmutableSubstitution<T> s = termFactory.getSubstitution(ImmutableMap.of(variable, term));
            optionalSubstitution = Optional.of(operations.compose(s, optionalSubstitution.get()));
            return self();
        }

        public Optional<ImmutableSubstitution<T>> build() {
            return optionalSubstitution;
        }

        R merge(R another) {
            throw new MinorOntopInternalBugException("Not expected to be run in parallel");
        }
    }

    public static class VariableOrGroundTermUnifierBuilder<T extends VariableOrGroundTerm> extends UnifierBuilder<T, VariableOrGroundTermUnifierBuilder<T>> {

        VariableOrGroundTermUnifierBuilder(TermFactory termFactory, SubstitutionOperations<T> operations, ImmutableSubstitution<T> substitution) {
            super(termFactory, operations, substitution);
        }

        @Override
        protected VariableOrGroundTermUnifierBuilder<T> self() {
            return this;
        }

        @Override
        protected VariableOrGroundTermUnifierBuilder<T> unifyUnequalTerms(T term1, T term2) {
            if (term1 instanceof Variable)
                return extendSubstitution((Variable) term1, term2);
            if (term2 instanceof Variable)
                return extendSubstitution((Variable) term2, term1);

            return emptySelf(); // neither is a variable, impossible to unify distinct terms
        }
    }

    public static class ImmutableUnifierBuilder extends UnifierBuilder<ImmutableTerm, ImmutableUnifierBuilder> {

        ImmutableUnifierBuilder(TermFactory termFactory, SubstitutionOperations<ImmutableTerm> operations, ImmutableSubstitution<ImmutableTerm> substitution) {
            super(termFactory, operations, substitution);
        }

        @Override
        protected ImmutableUnifierBuilder self() {
            return this;
        }

        @Override
        protected ImmutableUnifierBuilder unifyUnequalTerms(ImmutableTerm term1, ImmutableTerm term2) {

            // Special case: unification of two functional terms (possibly recursive)
            if ((term1 instanceof ImmutableFunctionalTerm) && (term2 instanceof ImmutableFunctionalTerm)) {
                ImmutableFunctionalTerm f1 = (ImmutableFunctionalTerm) term1;
                ImmutableFunctionalTerm f2 = (ImmutableFunctionalTerm) term2;
                if (f1.getFunctionSymbol().equals(f2.getFunctionSymbol()))
                    return unifyTermLists(f1.getTerms(), f2.getTerms());

                return emptySelf();
            }
            else {
                // avoid unifying x with f(g(x))
                if (term1 instanceof Variable && term2.getVariableStream().noneMatch(term1::equals))
                    return extendSubstitution((Variable) term1, term2);
                if (term2 instanceof Variable && term1.getVariableStream().noneMatch(term2::equals))
                    return extendSubstitution((Variable) term2, term1);

                return emptySelf(); // neither is a variable, impossible to unify distinct terms
            }
        }
    }

    public InjectiveVar2VarSubstitution getPrioritizingRenaming(ImmutableSubstitution<?> substitution, ImmutableSet<Variable> priorityVariables) {
        ImmutableSubstitution<Variable> renaming = substitution.builder()
                .restrictDomainTo(priorityVariables)
                .restrictRangeTo(Variable.class)
                .restrictRange(t -> !priorityVariables.contains(t))
                .build();

        return substitutionFactory.extractAnInjectiveVar2VarSubstitutionFromInverseOf(renaming);
    }


}
