package it.unibz.inf.ontop.owlrefplatform.core.basicoperations;


import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;

import java.util.Map;
import java.util.Optional;

import static it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionTools.isInjective;

public class InjectiveVar2VarSubstitutionImpl extends Var2VarSubstitutionImpl implements InjectiveVar2VarSubstitution {
    private final boolean isEmpty;

    /**
     * Regular constructor
     */
    public InjectiveVar2VarSubstitutionImpl(Map<Variable, Variable> substitutionMap) {
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
    public ImmutableSubstitution<ImmutableTerm> applyRenaming(ImmutableSubstitution<? extends ImmutableTerm> substitutionToRename) {
        if (isEmpty) {
            return (ImmutableSubstitution<ImmutableTerm>)substitutionToRename;
        }

        ImmutableMap.Builder<Variable, ImmutableTerm> substitutionMapBuilder = ImmutableMap.builder();

        /**
         * Substitutes the keys and values of the substitution to rename.
         */
        for (Map.Entry<Variable, ? extends ImmutableTerm> originalEntry : substitutionToRename.getImmutableMap().entrySet()) {

            Variable convertedVariable = applyToVariable(originalEntry.getKey());
            ImmutableTerm convertedTargetTerm = apply(originalEntry.getValue());

            // Safe because the local substitution is injective
            substitutionMapBuilder.put(convertedVariable, convertedTargetTerm);
        }

        return new ImmutableSubstitutionImpl<>(substitutionMapBuilder.build());
    }

    /**
     * More efficient implementation
     */
    @Override
    public Optional<ImmutableSubstitution<? extends ImmutableTerm>> applyToSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        return Optional.of(applyRenaming(substitution));
    }

}
