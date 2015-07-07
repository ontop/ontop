package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.ImmutableTerm;
import org.semanticweb.ontop.model.impl.VariableImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
            if (otherSubstitution.isDefining(variable) && (!otherSubstitution.get(variable).equals(term))) {
                conflictingVariables.add(variable);
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
}
