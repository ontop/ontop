package it.unibz.inf.ontop.substitution.impl;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.exception.ConversionException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.ProtoSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.Var2VarSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Common abstract class for ImmutableSubstitutionImpl and Var2VarSubstitutionImpl
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class AbstractImmutableSubstitutionImpl<T  extends ImmutableTerm>
        extends AbstractProtoSubstitution<T> implements ImmutableSubstitution<T> {

    protected final SubstitutionFactory substitutionFactory;

    protected AbstractImmutableSubstitutionImpl(TermFactory termFactory,
                                                SubstitutionFactory substitutionFactory) {
        super(termFactory);
        this.substitutionFactory = substitutionFactory;
    }


    @Override
    public ImmutableList<? extends VariableOrGroundTerm> applyToArguments(ImmutableList<? extends VariableOrGroundTerm> arguments) throws ConversionException {
        ImmutableList<? extends ImmutableTerm> newArguments = apply(arguments);

        if (!newArguments.stream().allMatch(t -> t instanceof VariableOrGroundTerm))
            throw new ConversionException("The substitution applied to a DataAtom has produced some non-VariableOrGroundTerm arguments " + newArguments);

        return (ImmutableList<? extends VariableOrGroundTerm>) newArguments;
    }

    @Override
    public ImmutableMap<Integer, ? extends VariableOrGroundTerm> applyToArgumentMap(ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap)
            throws ConversionException {
        ImmutableMap<Integer, ? extends ImmutableTerm> newArgumentMap = argumentMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> apply(e.getValue())));

        if (!newArgumentMap.values().stream().allMatch(t -> t instanceof VariableOrGroundTerm))
            throw new ConversionException("The substitution applied to an argument map has produced some non-VariableOrGroundTerm arguments " + newArgumentMap);

        return (ImmutableMap<Integer, ? extends VariableOrGroundTerm>) newArgumentMap;
    }


    /**
     *" "this o f"
     *
     * Equivalent to the function x {@code ->} this.apply(f.apply(x))
     *
     * Follows the formal definition of a the composition of two substitutions.
     *
     */
    @Override
    public ImmutableSubstitution<ImmutableTerm> composeWith(ImmutableSubstitution<? extends ImmutableTerm> f) {
        if (isEmpty()) {
            return (ImmutableSubstitution<ImmutableTerm>)f;
        }
        if (f.isEmpty()) {
            return (ImmutableSubstitution<ImmutableTerm>)this;
        }

        return substitutionFactory.getSubstitution(Stream.concat(
                f.getImmutableMap().entrySet().stream()
                        .map(e -> Maps.immutableEntry(e.getKey(), apply(e.getValue()))),
                getImmutableMap().entrySet().stream())
                        .filter(e -> !e.getKey().equals(e.getValue()))
                        .collect(ImmutableCollectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                // keep the value from f
                                (vf, v) -> vf)));
    }

    @Override
    public ImmutableSubstitution<T> composeWith2(ImmutableSubstitution<? extends T> g) {
        return (ImmutableSubstitution<T>) composeWith(g);
    }

    private static class NotASubstitutionException extends RuntimeException {};

    @Override
    public Optional<ImmutableSubstitution<T>> union(ImmutableSubstitution<T> otherSubstitution) {
        if (otherSubstitution.isEmpty())
            return Optional.of(this);
        if (isEmpty())
            return Optional.of(otherSubstitution);

        try {
            ImmutableMap<Variable, T> map = Stream.of(this, otherSubstitution)
                    .map(ProtoSubstitution::getImmutableMap)
                    .map(ImmutableMap::entrySet)
                    .flatMap(Collection::stream)
                    .collect(ImmutableCollectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (v1, v2) -> {
                                if (!v1.equals(v2))
                                    throw new NotASubstitutionException();
                                return v1;
                            }));

            return Optional.of(substitutionFactory.getSubstitution(map));
        }
        catch (NotASubstitutionException e) {
            return Optional.empty();
        }
    }


    @Override
    public boolean equals(Object other) {
        if (other instanceof ImmutableSubstitution) {
            return getImmutableMap().equals(((ImmutableSubstitution) other).getImmutableMap());
        }
        return false;
    }

    protected abstract ImmutableSubstitution<T> constructNewSubstitution(ImmutableMap<Variable, T> map);

    @Override
    public ImmutableSubstitution<T> orientate(ImmutableList<Variable> priorityVariables) {
        if (priorityVariables.isEmpty() || isEmpty()) {
            return this;
        }

        ImmutableMap<Variable, T> localMap = getImmutableMap();
        ImmutableSet<Variable> domain = getDomain();

        if (localMap.values().stream()
                .flatMap(ImmutableTerm::getVariableStream)
                .anyMatch(domain::contains)) {
            throw new UnsupportedOperationException("The orientate() method requires the domain and the range to be disjoint");
        }

        ImmutableMap<Variable, Variable> renamingMap = localMap.entrySet().stream()
                // Will produce some results only if T is compatible with Variable
                .filter(e -> e.getValue() instanceof Variable)
                .filter(e -> {
                    int replacedVariableIndex = priorityVariables.indexOf(e.getKey());
                    int targetVariableIndex = priorityVariables.indexOf(e.getValue());
                    return replacedVariableIndex >= 0 && ((targetVariableIndex < 0)
                            || (replacedVariableIndex < targetVariableIndex));
                })
                .collect(ImmutableCollectors.toMap(
                        e -> (Variable) e.getValue(),
                        Map.Entry::getKey,
                        (v1, v2) -> priorityVariables.indexOf(v1) <= priorityVariables.indexOf(v2) ? v1 : v2
                ));

        /*
         * Applies the renaming
         */
        if (renamingMap.isEmpty()) {
            return this;
        }
        else {
            Var2VarSubstitution renamingSubstitution = substitutionFactory.getVar2VarSubstitution(renamingMap);

            ImmutableMap<Variable, T> orientedMap = Stream.concat(
                    localMap.entrySet().stream()
                            /*
                             * Removes entries that will be reversed
                             */
                            .filter(e -> !Optional.ofNullable(renamingMap.get(e.getValue()))
                                    .filter(newValue -> newValue.equals(e.getKey()))
                                    .isPresent()),
                    renamingMap.entrySet().stream()
                            .map(e -> (Map.Entry<Variable, T>) e))
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            e -> renamingSubstitution.applyToTerm(e.getValue())
                    ));

            return constructNewSubstitution(orientedMap);
        }
    }

    @Override
    public ImmutableSubstitution<T> filter(Predicate<Variable> filter) {
        ImmutableMap<Variable, T> newMap = getImmutableMap().entrySet().stream()
                .filter(e -> filter.test(e.getKey()))
                .collect(ImmutableCollectors.toMap());

        return (newMap.size() == getImmutableMap().size()) ? this : constructNewSubstitution(newMap);
    }

    @Override
    public ImmutableSubstitution<T> filter(BiPredicate<Variable, T> filter) {
        ImmutableMap<Variable, T> newMap = getImmutableMap().entrySet().stream()
                .filter(e -> filter.test(e.getKey(), e.getValue()))
                .collect(ImmutableCollectors.toMap());

        return (newMap.size() == getImmutableMap().size()) ? this : constructNewSubstitution(newMap);
    }

    @Override
    public <S extends ImmutableTerm> ImmutableSubstitution<S> getFragment(Class<S> type) {
        return new ImmutableSubstitutionImpl<>(getImmutableMap().entrySet().stream()
                .filter(e -> type.isInstance(e.getValue()))
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> type.cast(e.getValue()))),
                termFactory, substitutionFactory);
    }

    @Override
    public <T2 extends ImmutableTerm> ImmutableSubstitution<T2> transform(Function<T, T2> function) {
        return new ImmutableSubstitutionImpl<>(getImmutableMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> function.apply(e.getValue()))),
                termFactory, substitutionFactory);
    }

    @Override
    public <T2 extends ImmutableTerm> ImmutableSubstitution<T2> transform(BiFunction<Variable, T, T2> function) {
        return new ImmutableSubstitutionImpl<>(getImmutableMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> function.apply(e.getKey(), e.getValue()))),
                termFactory, substitutionFactory);
    }
}
