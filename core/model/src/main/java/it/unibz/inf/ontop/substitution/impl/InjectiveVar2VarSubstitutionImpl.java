package it.unibz.inf.ontop.substitution.impl;


import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.exception.ConversionException;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class InjectiveVar2VarSubstitutionImpl extends AbstractImmutableSubstitutionImpl<Variable> implements InjectiveVar2VarSubstitution {

    private final ImmutableMap<Variable, Variable> map;

    /**
     * Regular constructor
     */
    protected InjectiveVar2VarSubstitutionImpl(ImmutableMap<Variable, Variable> substitutionMap,
                                               TermFactory termFactory) {
        super(termFactory);
        this.map = substitutionMap;

        if (!isInjective(substitutionMap))
            throw new IllegalArgumentException("Non-injective map given: " + substitutionMap);
    }

    @Override
    public String toString() {
        return Joiner.on(", ").withKeyValueSeparator("/").join(map);
    }

    @Override
    public ImmutableMap<Variable, Variable> getImmutableMap() {
        return map;
    }

    @Override
    public <T extends ImmutableTerm> T applyToTerm(T term) {
        return (T) super.apply(term);
    }

    @Override
    public ImmutableList<? extends VariableOrGroundTerm> applyToArguments(ImmutableList<? extends VariableOrGroundTerm> arguments) {
        return arguments.stream()
                .map(this::applyToTerm)
                .collect(ImmutableCollectors.toList());
    }

    @Override
    protected ImmutableSubstitution<Variable> constructNewSubstitution(ImmutableMap<Variable, Variable> map) {
        throw new RuntimeException("NEW SUB");
    }
    
    private InjectiveVar2VarSubstitution of(ImmutableMap<Variable, Variable> map) {
        return new InjectiveVar2VarSubstitutionImpl(map, termFactory);
    }

    @Override
    public <T extends ImmutableTerm> ImmutableSubstitution<T> applyRenaming(ImmutableSubstitution<T> substitutionToRename) {
        if (isEmpty())
            return substitutionToRename;

        ImmutableMap<Variable, T> substitutionMap = substitutionToRename.entrySet().stream()
                // Substitutes the keys and values of the substitution to rename.
                .map(e -> Maps.immutableEntry(applyToVariable(e.getKey()), applyToTerm(e.getValue())))
                // Safe because the local substitution is injective
                .filter(e -> !e.getValue().equals(e.getKey()))
                .collect(ImmutableCollectors.toMap());

        return new ImmutableSubstitutionImpl<>(substitutionMap, termFactory);
    }

    @Override
    public ImmutableList<Variable> applyToVariableArguments(ImmutableList<Variable> arguments)
            throws ConversionException {
        ImmutableList<? extends ImmutableTerm> newArguments = apply(arguments);

        if (!newArguments.stream().allMatch(t -> t instanceof Variable))
            throw new ConversionException("The substitution applied to an argument map has produced some non-Variable arguments " + newArguments);

        return (ImmutableList) newArguments;
    }



    @Override
    public InjectiveVar2VarSubstitution filter(Predicate<Variable> filter) {
        return of(entrySet().stream()
                        .filter(e -> filter.test(e.getKey()))
                        .collect(ImmutableCollectors.toMap()));
    }

    private static boolean isInjective(ImmutableMap<Variable, ? extends VariableOrGroundTerm> substitutionMap) {
        ImmutableSet<VariableOrGroundTerm> valueSet = ImmutableSet.copyOf(substitutionMap.values());
        return valueSet.size() == substitutionMap.keySet().size();
    }
}
