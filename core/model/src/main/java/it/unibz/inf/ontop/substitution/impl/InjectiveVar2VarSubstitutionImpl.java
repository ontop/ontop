package it.unibz.inf.ontop.substitution.impl;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.atom.AtomFactory;
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

public class InjectiveVar2VarSubstitutionImpl extends Var2VarSubstitutionImpl implements InjectiveVar2VarSubstitution {
    private final boolean isEmpty;

    /**
     * Regular constructor
     */
    protected InjectiveVar2VarSubstitutionImpl(Map<Variable, Variable> substitutionMap, AtomFactory atomFactory,
                                               TermFactory termFactory, SubstitutionFactory substitutionFactory) {
        super(substitutionMap, atomFactory, termFactory, substitutionFactory);
        isEmpty = substitutionMap.isEmpty();

        /**
         * Injectivity constraint
         */
        if (!isEmpty) {
            if (!isInjective(substitutionMap)) {
                throw new IllegalArgumentException("Non-injective map given: " + substitutionMap);
            }
        }
    }

    @Override
    public <T extends ImmutableTerm> ImmutableSubstitution<T> applyRenaming(ImmutableSubstitution<T> substitutionToRename) {
        if (isEmpty) {
            return substitutionToRename;
        }

        ImmutableMap.Builder<Variable, T> substitutionMapBuilder = ImmutableMap.builder();

        /**
         * Substitutes the keys and values of the substitution to rename.
         */
        for (Map.Entry<Variable, T> originalEntry : substitutionToRename.getImmutableMap().entrySet()) {

            Variable convertedVariable = applyToVariable(originalEntry.getKey());
            T convertedTargetTerm = applyToTerm(originalEntry.getValue());

            // Safe because the local substitution is injective
            if (!convertedTargetTerm.equals(convertedVariable))
                substitutionMapBuilder.put(convertedVariable, convertedTargetTerm);
        }

        return substitutionFactory.getSubstitution(substitutionMapBuilder.build());
    }

    @Override
    public Optional<InjectiveVar2VarSubstitution> composeWithAndPreserveInjectivity(
            InjectiveVar2VarSubstitution g, Set<Variable> variablesToExcludeFromTheDomain) {
        ImmutableMap<Variable, Variable> newMap = composeRenaming(g)
                // Removes some excluded entries
                .filter(e -> !variablesToExcludeFromTheDomain.contains(e.getKey()))
                .collect(ImmutableCollectors.toMap());

        return Optional.of(newMap)
                .filter(InjectiveVar2VarSubstitutionImpl::isInjective)
                .map(substitutionFactory::getInjectiveVar2VarSubstitution);
    }



    /**
     * More efficient implementation
     */
    @Override
    public <T extends ImmutableTerm> Optional<ImmutableSubstitution<T>> applyToSubstitution(
            ImmutableSubstitution<T> substitution) {
        return Optional.of(applyRenaming(substitution));
    }

    private static boolean isInjective(Map<Variable, ? extends VariableOrGroundTerm> substitutionMap) {
        ImmutableSet<VariableOrGroundTerm> valueSet = ImmutableSet.copyOf(substitutionMap.values());
        return valueSet.size() == substitutionMap.keySet().size();
    }
}
