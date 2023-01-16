package it.unibz.inf.ontop.substitution.impl;


import com.google.common.collect.*;
import it.unibz.inf.ontop.exception.ConversionException;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

public class InjectiveVar2VarSubstitutionImpl extends ImmutableSubstitutionImpl<Variable> implements InjectiveVar2VarSubstitution {

    protected InjectiveVar2VarSubstitutionImpl(ImmutableMap<Variable, Variable> substitutionMap, TermFactory termFactory) {
        super(substitutionMap, termFactory);

        if (!isInjective(this.map))
            throw new IllegalArgumentException("Non-injective map given: " + substitutionMap);
    }

    @Override
    public String toString() {
        return "injective " + super.toString();
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
    public ImmutableList<Variable> applyToVariableArguments(ImmutableList<Variable> arguments) throws ConversionException {
        ImmutableList<? extends ImmutableTerm> newArguments = apply(arguments);

        if (!newArguments.stream().allMatch(t -> t instanceof Variable))
            throw new ConversionException("The substitution applied to an argument map has produced some non-Variable arguments " + newArguments);

        return (ImmutableList) newArguments;
    }


    @Override
    public InjectiveVar2VarSubstitution restrictDomainTo(ImmutableSet<Variable> set) {
        return new InjectiveVar2VarSubstitutionImpl(map.entrySet().stream()
                .filter(e -> set.contains(e.getKey()))
                .collect(ImmutableCollectors.toMap()), termFactory);
    }

    static <T> boolean isInjective(ImmutableMap<Variable, T> map) {
        ImmutableCollection<T> values = map.values();
        return values.size() == ImmutableSet.copyOf(values).size();
    }

}
