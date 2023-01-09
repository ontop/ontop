package it.unibz.inf.ontop.substitution.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.ConversionException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Common abstract class for ImmutableSubstitutionImpl and Var2VarSubstitutionImpl
 */
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



    @Override
    public boolean equals(Object other) {
        if (other instanceof ImmutableSubstitution) {
            return getImmutableMap().equals(((ImmutableSubstitution<?>) other).getImmutableMap());
        }
        return false;
    }

    protected abstract ImmutableSubstitution<T> constructNewSubstitution(ImmutableMap<Variable, T> map);

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
    public <S extends ImmutableTerm> ImmutableSubstitution<S> castTo(Class<S> type) {
        if (getImmutableMap().entrySet().stream()
                .anyMatch(e -> !type.isInstance(e.getValue())))
            throw new ClassCastException();

        return (ImmutableSubstitution<S>) this;
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
