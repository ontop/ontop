package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.NonGroundFunctionalTermImpl;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.VariableImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.semanticweb.ontop.model.impl.GroundTermTools.isGroundTerm;
import static org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableUnificationTools.computeMGU;

/**
 * TODO: explain
 *
 * TODO: explain why we always generate new variables
 *
 */
public class PartialUnion<T extends ImmutableTerm> {

    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();

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
    private final VariableGenerator variableGenerator;

    /**
     * Bootstrap. Then use newPartialUnion().
     */
    public PartialUnion(ImmutableSubstitution<T> substitution, VariableGenerator variableGenerator) {
        conflictingVariables = ImmutableSet.of();
        this.variableGenerator = variableGenerator;
        substitutionMap = replaceTargetVariables(substitution.getImmutableMap());
    }

    private PartialUnion(ImmutableSubstitution<T> substitution1,
                        ImmutableSubstitution<T> substitution2,
                        ImmutableSet<VariableImpl> alreadyConflictingVariables,
                        VariableGenerator variableGenerator) {

        substitutionMap = new HashMap<>();
        conflictingVariables = new HashSet<>(alreadyConflictingVariables);
        this.variableGenerator = variableGenerator;

        loadOneSubstitutionEntries(substitution1, substitution2, false);
        loadOneSubstitutionEntries(substitution2, substitution1, true);
    }

    /**
     * TODO: explain
     *
     * Cannot be static because of the generics.
     */
    private ImmutableMap<VariableImpl, T> replaceTargetVariables(ImmutableMap<VariableImpl, T> importedSubstitutionMap) {
        ImmutableMap.Builder<VariableImpl, T> mapBuilder = ImmutableMap.builder();

        for (Map.Entry<VariableImpl, T> entry : importedSubstitutionMap.entrySet()) {
            mapBuilder.put(entry.getKey(), replaceVariablesInTerm(entry.getValue()));
        }
        return mapBuilder.build();
    }

    /**
     * RESERVED FOR CONSTRUCTORS!!!!
     * Presumes mutable attributes.
     *
     * TODO: further explain
     */
    private void loadOneSubstitutionEntries(ImmutableSubstitution<T> substitutionToLoad,
                                            ImmutableSubstitution<T> otherSubstitution, boolean replaceVariables) {
        ImmutableMap<VariableImpl, T> substitutionMapToLoad = substitutionToLoad.getImmutableMap();
        for(VariableImpl variable : substitutionMapToLoad.keySet()) {

            if (conflictingVariables.contains(variable)) {
                continue;
            }

            T term = substitutionMapToLoad.get(variable);

            /**
             * TODO: explain
             */
            boolean addToSubstitutionMap;
            if (otherSubstitution.isDefining(variable)) {
                if (areEquivalent(otherSubstitution.get(variable), term)) {
                    addToSubstitutionMap = !substitutionMap.containsKey(variable);
                }
                else {
                    conflictingVariables.add(variable);
                    addToSubstitutionMap = false;
                }
            }
            else {
                addToSubstitutionMap = true;
            }


            if (addToSubstitutionMap) {
                T newTerm;
                if (replaceVariables) {
                    newTerm = replaceVariablesInTerm(term);
                }
                else {
                    newTerm = term;
                }
                substitutionMap.put(variable, newTerm);
            }
        }
    }

    /**
     * TODO: explain
     */
    private T replaceVariablesInTerm(T term) {
        return (T) replaceVariablesInTerm(term, variableGenerator);
    }

    private static ImmutableTerm replaceVariablesInTerm(ImmutableTerm term, VariableGenerator variableGenerator) {
        if (isGroundTerm(term)) {
            return term;
        }
        else if (term instanceof VariableImpl) {
            return variableGenerator.generateNewVariable();
        }
        /**
         * Non-ground functional term
         */
        else if (term instanceof ImmutableFunctionalTerm) {
            return replaceAllVariables((ImmutableFunctionalTerm) term, variableGenerator);
        }
        else {
            throw new IllegalArgumentException("Unknown term: " + term);
        }
    }

    /**
     * The functional term must non-ground (maybe not explicitly typed as such)
     */
    private static NonGroundFunctionalTerm replaceAllVariables(ImmutableFunctionalTerm nonGroundFunctionalTerm,
                                                               VariableGenerator variableGenerator) {
        ImmutableList.Builder<ImmutableTerm> subTermBuilder = ImmutableList.builder();
        for (ImmutableTerm subTerm : nonGroundFunctionalTerm.getImmutableTerms()) {
            /**
             * Indirectly recursive call
             */
            subTermBuilder.add(replaceVariablesInTerm(subTerm, variableGenerator));
        }
        return DATA_FACTORY.getNonGroundFunctionalTerm(nonGroundFunctionalTerm.getFunctionSymbol(),
                subTermBuilder.build());
    }


    public ImmutableSet<VariableImpl> getConflictingVariables() {
        return ImmutableSet.copyOf(conflictingVariables);
    }

    public ImmutableSubstitution<T> getPartialUnionSubstitution() {
        return new ImmutableSubstitutionImpl<>(ImmutableMap.copyOf(substitutionMap));
    }

    public PartialUnion<T> newPartialUnion(ImmutableSubstitution<T> additionalSubstitution) {
        return new PartialUnion<>(getPartialUnionSubstitution(), additionalSubstitution, getConflictingVariables(),
                variableGenerator);
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
