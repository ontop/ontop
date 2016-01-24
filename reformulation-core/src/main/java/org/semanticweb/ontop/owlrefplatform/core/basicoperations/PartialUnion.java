package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;

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
    private final Set<Variable> conflictingVariables;
    private final Map<Variable, T> substitutionMap;
    private final IntermediateQuery query;

    /**
     * Bootstrap. Then use newPartialUnion().
     */
    public PartialUnion(ImmutableSubstitution<T> substitution, IntermediateQuery query) {
        conflictingVariables = ImmutableSet.of();
        this.query = query;
        substitutionMap = replaceTargetVariables(substitution.getImmutableMap());
    }

    private PartialUnion(ImmutableSubstitution<T> substitution1,
                        ImmutableSubstitution<T> substitution2,
                        ImmutableSet<Variable> alreadyConflictingVariables,
                        IntermediateQuery query) {

        substitutionMap = new HashMap<>();
        conflictingVariables = new HashSet<>(alreadyConflictingVariables);
        this.query = query;

        loadOneSubstitutionEntries(substitution1, substitution2, false);
        loadOneSubstitutionEntries(substitution2, substitution1, true);
    }

    /**
     * TODO: explain
     *
     * Cannot be static because of the generics.
     */
    private ImmutableMap<Variable, T> replaceTargetVariables(ImmutableMap<Variable, T> importedSubstitutionMap) {
        ImmutableMap.Builder<Variable, T> mapBuilder = ImmutableMap.builder();

        for (Map.Entry<Variable, T> entry : importedSubstitutionMap.entrySet()) {
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
        ImmutableMap<Variable, T> substitutionMapToLoad = substitutionToLoad.getImmutableMap();
        for(Variable variable : substitutionMapToLoad.keySet()) {

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
        return (T) replaceVariablesInTerm(term, query);
    }

    private static ImmutableTerm replaceVariablesInTerm(ImmutableTerm term, IntermediateQuery query) {
        if (isGroundTerm(term)) {
            return term;
        }
        else if (term instanceof Variable) {
            return query.generateNewVariable();
        }
        /**
         * Non-ground functional term
         */
        else if (term instanceof ImmutableFunctionalTerm) {
            return replaceAllVariables((ImmutableFunctionalTerm) term, query);
        }
        else {
            throw new IllegalArgumentException("Unknown term: " + term);
        }
    }

    /**
     * The functional term must non-ground (maybe not explicitly typed as such)
     */
    private static NonGroundFunctionalTerm replaceAllVariables(ImmutableFunctionalTerm nonGroundFunctionalTerm,
                                                               IntermediateQuery query) {
        ImmutableList.Builder<ImmutableTerm> subTermBuilder = ImmutableList.builder();
        for (ImmutableTerm subTerm : nonGroundFunctionalTerm.getArguments()) {
            /**
             * Indirectly recursive call
             */
            subTermBuilder.add(replaceVariablesInTerm(subTerm, query));
        }
        return DATA_FACTORY.getNonGroundFunctionalTerm(nonGroundFunctionalTerm.getFunctionSymbol(),
                subTermBuilder.build());
    }


    public ImmutableSet<Variable> getConflictingVariables() {
        return ImmutableSet.copyOf(conflictingVariables);
    }

    public ImmutableSubstitution<T> getPartialUnionSubstitution() {
        return new ImmutableSubstitutionImpl<>(ImmutableMap.copyOf(substitutionMap));
    }

    public PartialUnion<T> newPartialUnion(ImmutableSubstitution<T> additionalSubstitution) {
        return new PartialUnion<>(getPartialUnionSubstitution(), additionalSubstitution, getConflictingVariables(),
                query);
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
