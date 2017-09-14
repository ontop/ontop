package it.unibz.inf.ontop.substitution.impl;


import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class InjectiveVar2VarSubstitutionImpl extends Var2VarSubstitutionImpl implements InjectiveVar2VarSubstitution {
    private final boolean isEmpty;

    /**
     * Regular constructor
     */
    protected InjectiveVar2VarSubstitutionImpl(Map<Variable, Variable> substitutionMap) {
        super(substitutionMap);
        isEmpty = substitutionMap.isEmpty();

        /**
         * Injectivity constraint
         */
        if (!isEmpty) {
            if (!ImmutableSubstitutionTools.isInjective(substitutionMap)) {
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

        return new ImmutableSubstitutionImpl<>(substitutionMapBuilder.build());
    }

    @Override
    public Optional<InjectiveVar2VarSubstitution> composeWithAndPreserveInjectivity(
            InjectiveVar2VarSubstitution g, Set<Variable> variablesToExcludeFromTheDomain) {
        ImmutableMap<Variable, Variable> newMap = composeRenaming(g)
                // Removes some excluded entries
                .filter(e -> !variablesToExcludeFromTheDomain.contains(e.getKey()))
                .collect(ImmutableCollectors.toMap());

        return Optional.of(newMap)
                .filter(ImmutableSubstitutionTools::isInjective)
                .map(map -> (InjectiveVar2VarSubstitution) new InjectiveVar2VarSubstitutionImpl(map));
    }



    /**
     * More efficient implementation
     */
    @Override
    public <T extends ImmutableTerm> Optional<ImmutableSubstitution<T>> applyToSubstitution(
            ImmutableSubstitution<T> substitution) {
        return Optional.of(applyRenaming(substitution));
    }
}
