package it.unibz.inf.ontop.substitution.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.exception.ConversionException;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
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

public class InjectiveVar2VarSubstitutionImpl extends Var2VarSubstitutionImpl implements InjectiveVar2VarSubstitution {

    /**
     * Regular constructor
     */
    protected InjectiveVar2VarSubstitutionImpl(ImmutableMap<Variable, Variable> substitutionMap,
                                               TermFactory termFactory, SubstitutionFactory substitutionFactory) {
        super(substitutionMap, termFactory, substitutionFactory);

        if (!isInjective(substitutionMap))
            throw new IllegalArgumentException("Non-injective map given: " + substitutionMap);
    }

    @Override
    public <T extends ImmutableTerm> ImmutableSubstitution<T> applyRenaming(ImmutableSubstitution<T> substitutionToRename) {
        if (isEmpty())
            return substitutionToRename;

        ImmutableMap<Variable, T> substitutionMap = substitutionToRename.getImmutableMap().entrySet().stream()
                // Substitutes the keys and values of the substitution to rename.
                .map(e -> Maps.immutableEntry(applyToVariable(e.getKey()), applyToTerm(e.getValue())))
                // Safe because the local substitution is injective
                .filter(e -> !e.getValue().equals(e.getKey()))
                .collect(ImmutableCollectors.toMap());

        return substitutionFactory.getSubstitution(substitutionMap);
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
    public Optional<InjectiveVar2VarSubstitution> composeWithAndPreserveInjectivity(
            InjectiveVar2VarSubstitution g, Set<Variable> variablesToExcludeFromTheDomain) {
        ImmutableSet<Variable> gDomain = g.getDomain();

        Stream<Map.Entry<Variable, Variable>> gEntryStream = g.getImmutableMap().entrySet().stream()
                .map(e1 -> Maps.immutableEntry(e1.getKey(), applyToVariable(e1.getValue())));

        Stream<Map.Entry<Variable, Variable>> localEntryStream = getImmutableMap().entrySet().stream()
                .filter(e -> !gDomain.contains(e.getKey()));

        ImmutableMap<Variable, Variable> newMap = Stream.concat(gEntryStream, localEntryStream)
                .filter(e -> !e.getKey().equals(e.getValue()))
                // Removes some excluded entries
                .filter(e -> !variablesToExcludeFromTheDomain.contains(e.getKey()))
                .collect(ImmutableCollectors.toMap());

        return Optional.of(newMap)
                .filter(InjectiveVar2VarSubstitutionImpl::isInjective)
                .map(substitutionFactory::getInjectiveVar2VarSubstitution);
    }

    @Override
    public InjectiveVar2VarSubstitution filter(Predicate<Variable> filter) {
        return substitutionFactory.getInjectiveVar2VarSubstitution(
                getImmutableMap().entrySet().stream()
                        .filter(e -> filter.test(e.getKey()))
                        .collect(ImmutableCollectors.toMap()));
    }

    private static boolean isInjective(ImmutableMap<Variable, ? extends VariableOrGroundTerm> substitutionMap) {
        ImmutableSet<VariableOrGroundTerm> valueSet = ImmutableSet.copyOf(substitutionMap.values());
        return valueSet.size() == substitutionMap.keySet().size();
    }
}
