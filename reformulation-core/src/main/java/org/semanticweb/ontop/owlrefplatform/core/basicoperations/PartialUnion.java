package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.ImmutableFunctionalTerm;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.ImmutableTerm;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.model.impl.VariableImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableUnificationTools.computeMGU;

/**
 * TODO: explain
 *
 */
public class PartialUnion<T extends ImmutableTerm> {

    /**
     * Possibly mutable attributes that can only be modified
     * in methods only called by the constructor.
     *
     * Not super elegant but made necessary by the use of generics.
     *
     * MUST NOT BE SHARED to make this class immutable!
     */
    private final Set<VariableImpl> conflictingVariables;
    private final Map<VariableImpl, T> substitutionMap;

    /**
     * Bootstrap. Then use newPartialUnion().
     */
    public PartialUnion(ImmutableSubstitution<T> substitution) {
        conflictingVariables = ImmutableSet.of();
        substitutionMap = substitution.getImmutableMap();
    }

    public PartialUnion(ImmutableSubstitution<T> substitution1,
                        ImmutableSubstitution<T> substitution2,
                        ImmutableSet<VariableImpl> alreadyConflictingVariables) {

        substitutionMap = new HashMap<>();
        conflictingVariables = new HashSet<>(alreadyConflictingVariables);

        loadOneSubstitutionEntries(substitution1, substitution2);
        loadOneSubstitutionEntries(substitution2, substitution1);
    }

    /**
     * RESERVED FOR CONSTRUCTORS!!!!
     * Presumes mutable attributes.
     *
     * TODO: further explain
     */
    private void loadOneSubstitutionEntries(ImmutableSubstitution<T> substitutionToLoad,
                                            ImmutableSubstitution<T> otherSubstitution) {
        ImmutableMap<VariableImpl, T> substitutionMapToLoad = substitutionToLoad.getImmutableMap();
        for(VariableImpl variable : substitutionMapToLoad.keySet()) {

            if (conflictingVariables.contains(variable)) {
                continue;
            }

            T term = substitutionMapToLoad.get(variable);

            /**
             * TODO: explain
             */
            if (otherSubstitution.isDefining(variable)) {
                if (!areEquivalent(otherSubstitution.get(variable), term)) {
                    conflictingVariables.add(variable);
                }
            }
            else {
                substitutionMap.put(variable, term);
            }
        }
    }


    public ImmutableSet<VariableImpl> getConflictingVariables() {
        return ImmutableSet.copyOf(conflictingVariables);
    }

    public ImmutableSubstitution<T> getPartialUnionSubstitution() {
        return new ImmutableSubstitutionImpl<>(ImmutableMap.copyOf(substitutionMap));
    }

    public PartialUnion<T> newPartialUnion(ImmutableSubstitution<T> additionalSubstitution) {
        return new PartialUnion<>(getPartialUnionSubstitution(), additionalSubstitution, getConflictingVariables());
    }

    /**
     * TODO: explain
     */
    private static boolean areEquivalent(ImmutableTerm term1, ImmutableTerm term2) {
        /**
         * TODO: probably too simple. Think more about it.
         */
        if (term1 instanceof VariableOrGroundTerm) {
            return term1.equals(term2);
        }
        /**
         *
         */
        else if (term1 instanceof ImmutableFunctionalTerm) {
            if (term2 instanceof ImmutableFunctionalTerm) {
                Optional<ImmutableSubstitution<ImmutableTerm>> optionalMGU = computeMGU((ImmutableFunctionalTerm) term1, (ImmutableFunctionalTerm) term2);
                return optionalMGU.isPresent();
            }
            else {
                return false;
            }
        }
        else {
            throw new IllegalArgumentException("Unknown term1: " + term1);
        }
    }
}
