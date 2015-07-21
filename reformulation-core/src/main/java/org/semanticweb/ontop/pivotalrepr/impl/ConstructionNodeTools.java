package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import org.semanticweb.ontop.pivotalrepr.ConstructionNode;
import org.semanticweb.ontop.pivotalrepr.ImmutableQueryModifiers;

import java.util.*;

import static org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionTools.computeUnidirectionalSubstitution;

/**
 * TODO: explain
 */
public class ConstructionNodeTools {

    /**
     * Runtime exception
     *
     * When "updating" a ConstructionNode with bindings to add or to remove.
     */
    public static class InconsistentBindingException extends RuntimeException {
        public InconsistentBindingException(String message) {
            super(message);
        }
    }

    protected static class SubstitutionConversionException extends Exception {
    }

    /**
     * TODO: explain
     */
    public static class BindingRemoval {

        private final ConstructionNode newConstructionNode;
        private final Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalSubstitutionToPropagateToAncestors;

        protected BindingRemoval(ConstructionNode newConstructionNode,
                                 Optional<ImmutableSubstitution<VariableOrGroundTerm>> substitutionToPropagateToAncestors) {
            this.newConstructionNode = newConstructionNode;
            this.optionalSubstitutionToPropagateToAncestors = substitutionToPropagateToAncestors;
        }

        public ConstructionNode getNewConstructionNode() {
            return newConstructionNode;
        }

        public Optional<ImmutableSubstitution<VariableOrGroundTerm>> getOptionalSubstitutionToPropagateToAncestors() {
            return optionalSubstitutionToPropagateToAncestors;
        }
    }

    /**
     * TODO: explain and find a better name
     */
    private static class NewSubstitutions {
        private final ImmutableSubstitution<VariableOrGroundTerm> substitutionToPropagate;
        private final ImmutableSubstitution<VariableOrGroundTerm> newBindings;

        protected NewSubstitutions(ImmutableSubstitution<VariableOrGroundTerm> substitutionToPropagate,
                                   ImmutableSubstitution<VariableOrGroundTerm> newBindings) {
            this.substitutionToPropagate = substitutionToPropagate;
            this.newBindings = newBindings;
        }

        public ImmutableSubstitution<VariableOrGroundTerm> getNewBindings() {
            return newBindings;
        }

        public Optional<ImmutableSubstitution<VariableOrGroundTerm>> getOptionalSubstitutionToPropagate() {
            if (substitutionToPropagate.isEmpty()) {
                return Optional.absent();
            }
            return Optional.of(substitutionToPropagate);
        }
    }

    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();


    /**
     * TODO: explain
     */
    public static ConstructionNode newNodeWithAdditionalBindings(ConstructionNode formerConstructionNode,
                                                                 ImmutableSubstitution<ImmutableTerm> additionalBindingsSubstitution)
            throws InconsistentBindingException {

        DataAtom projectionAtom = formerConstructionNode.getProjectionAtom();
        ImmutableSet<Variable> projectedVariables = projectionAtom.getVariables();

        /**
         * TODO: explain why the composition is too rich
         */
        ImmutableSubstitution<ImmutableTerm> composedSubstitution = additionalBindingsSubstitution.composeWith(formerConstructionNode.getSubstitution());
        ImmutableMap.Builder<VariableImpl, ImmutableTerm> substitutionMapBuilder = ImmutableMap.builder();

        ImmutableMap<VariableImpl, ImmutableTerm> compositionMap = composedSubstitution.getImmutableMap();

        for(VariableImpl variable : compositionMap.keySet()) {
            ImmutableTerm term = compositionMap.get(variable);

            /**
             * If the variable is not projected, no need to be in the substitution
             */
            if (!projectedVariables.contains(variable)) {
                continue;
            }

            /**
             * Checks for contradictory bindings between
             * the previous one (still present in the composition)
             * and the additional ones.
             */
            if (additionalBindingsSubstitution.isDefining(variable)
                    && (!additionalBindingsSubstitution.get(variable).equals(term))) {
                throw new InconsistentBindingException("Contradictory bindings found in the parent.");
            }

            substitutionMapBuilder.put(variable, term);
        }

        return new ConstructionNodeImpl(projectionAtom, new ImmutableSubstitutionImpl<>(substitutionMapBuilder.build()),
                formerConstructionNode.getOptionalModifiers());

    }

    /**
     * TODO: explain
     *
     */
    public static BindingRemoval newNodeWithLessBindings(ConstructionNode formerConstructionNode,
                                                         ImmutableSubstitution<ImmutableTerm> bindingsToRemove)
            throws InconsistentBindingException {

        ImmutableSet<VariableImpl> variablesToRemove = extractVariablesToRemove(formerConstructionNode, bindingsToRemove);

        ImmutableSet<VariableImpl> newVariablesToProject = extractVariablesToProject(variablesToRemove, bindingsToRemove);

        NewSubstitutions newSubstitutions = computeSubstitutionToPropagateAndNewBindings(formerConstructionNode, bindingsToRemove,
               variablesToRemove, newVariablesToProject);

        ImmutableSubstitution<ImmutableTerm> newBindingSubstitution = computeNewBindingSubstitution(formerConstructionNode, variablesToRemove,
                newSubstitutions.getNewBindings());

        DataAtom dataAtom = computeNewDataAtom(formerConstructionNode.getProjectionAtom(), variablesToRemove, newVariablesToProject);


        Optional<ImmutableQueryModifiers> newOptionalModifiers = computeNewOptionalModifiers(formerConstructionNode.getOptionalModifiers(),
                bindingsToRemove);

        ConstructionNode newConstructionNode = new ConstructionNodeImpl(dataAtom, newBindingSubstitution, newOptionalModifiers);

        return new BindingRemoval(newConstructionNode, newSubstitutions.getOptionalSubstitutionToPropagate());
    }

    /**
     * TODO: explain
     */
    private static ImmutableSubstitution<ImmutableTerm> computeNewBindingSubstitution(
            ConstructionNode formerConstructionNode, ImmutableSet<VariableImpl> variablesToRemove,
            ImmutableSubstitution<VariableOrGroundTerm> newBindings) {
        ImmutableMap.Builder<VariableImpl, ImmutableTerm> mapBuilder = ImmutableMap.builder();

        ImmutableMap<VariableImpl, ImmutableTerm> formerSubstitutionMap = formerConstructionNode.getDirectBindingSubstitution().getImmutableMap();
        for (VariableImpl variable : formerSubstitutionMap.keySet()) {
            if (!variablesToRemove.contains(variable)) {
                mapBuilder.put(variable, formerSubstitutionMap.get(variable));
            }
        }

        mapBuilder.putAll(newBindings.getImmutableMap());

        return new ImmutableSubstitutionImpl<>(mapBuilder.build());

    }

    /**
     * TODO: explain
     */
    private static ImmutableList<ImmutableSubstitution<VariableOrGroundTerm>> computeBindingUnifiers(
            ConstructionNode formerConstructionNode, ImmutableSet<VariableImpl> variablesToRemove,
            ImmutableSubstitution<ImmutableTerm> bindingsToRemove) {

        ImmutableSubstitution<ImmutableTerm> formerSubstitution = formerConstructionNode.getDirectBindingSubstitution();

        ImmutableList.Builder<ImmutableSubstitution<VariableOrGroundTerm>> substitutionListBuilder = ImmutableList.builder();

        for (VariableImpl variable : variablesToRemove) {
            ImmutableTerm formerTerm = formerSubstitution.get(variable);
            ImmutableTerm newTerm = bindingsToRemove.get(variable);

            Optional<ImmutableSubstitution<ImmutableTerm>> optionalTermSubstitution = computeUnidirectionalSubstitution(
                    formerTerm, newTerm);
            /**
             * If cannot be unified...
             */
            if (!optionalTermSubstitution.isPresent()) {
                throw new InconsistentBindingException("Contradictory bindings found in one child.");
            }
            try {
                substitutionListBuilder.add(
                        convertToVarOrGroundTermSubstitution(optionalTermSubstitution.get()));
            } catch (SubstitutionConversionException e) {
                throw new InconsistentBindingException("Incompatible bindings found in one child.");
            }
        }

        return substitutionListBuilder.build();
    }

    /**
     * TODO: explain
     *
     */
    private static NewSubstitutions computeSubstitutionToPropagateAndNewBindings(
            ConstructionNode formerConstructionNode, ImmutableSubstitution<ImmutableTerm> bindingsToRemove,
            ImmutableSet<VariableImpl> variablesToRemove, ImmutableSet<VariableImpl> newVariablesToProject) {

        ImmutableList<ImmutableSubstitution<VariableOrGroundTerm>> bindingUnifiers = computeBindingUnifiers(
                formerConstructionNode, variablesToRemove, bindingsToRemove);

        Map<VariableImpl, VariableOrGroundTerm> substitutionMapToPropagate = new HashMap<>();
        ImmutableMap.Builder<VariableImpl, VariableOrGroundTerm> newBindingsMapBuilder = ImmutableMap.builder();


        for (ImmutableSubstitution<VariableOrGroundTerm> unifier : bindingUnifiers) {
            ImmutableMap<VariableImpl, VariableOrGroundTerm> unificationMap = unifier.getImmutableMap();

            if (!unifier.isEmpty()) {
                for (VariableImpl replacedVariable : unificationMap.keySet()) {
                    VariableOrGroundTerm targetTerm = unificationMap.get(replacedVariable);

                    /**
                     * New variable ---> goes to the bindings
                     *
                     * TODO: understand it better
                     */
                    if (newVariablesToProject.contains(replacedVariable)) {
                        // TODO: check if some conflicts happen
                        newBindingsMapBuilder.put(replacedVariable, targetTerm);
                    }
                    /**
                     * Sub-tree variable is replaced
                     * ---> Need to be propagated
                     */
                    else {
                        if (!substitutionMapToPropagate.containsKey(replacedVariable)) {
                            substitutionMapToPropagate.put(replacedVariable, targetTerm);
                        }
                        else {
                            /**
                             * Should not have a "conflict" with a ground term. ---> must be a variable.
                             */
                            VariableOrGroundTerm otherTermToPropagate = substitutionMapToPropagate.get(replacedVariable);
                            if (!otherTermToPropagate.equals(targetTerm)) {
                                if (targetTerm instanceof VariableImpl) {
                                    /**
                                     * Registers the equality to the new substitution.
                                     */
                                    newBindingsMapBuilder.put((VariableImpl) targetTerm, otherTermToPropagate);
                                }
                                else {
                                    throw new InconsistentBindingException("Should not find a ground term here: " + targetTerm);
                                }
                            }
                        }
                    }
                }
            }
        }

        ImmutableSubstitution<VariableOrGroundTerm> substitutionToPropagate = new ImmutableSubstitutionImpl<>(
                ImmutableMap.copyOf(substitutionMapToPropagate));
        ImmutableSubstitution<VariableOrGroundTerm> newBindings = new ImmutableSubstitutionImpl<>(newBindingsMapBuilder.build());

        return new NewSubstitutions(substitutionToPropagate, newBindings);
    }

    /**
     * TODO: explain
     *
     */
    private static ImmutableSet<VariableImpl> extractVariablesToRemove(ConstructionNode formerConstructionNode,
                                                                       ImmutableSubstitution<ImmutableTerm> bindingsToRemove)
            throws InconsistentBindingException {

        ImmutableSet<VariableImpl> allVariablesToRemove = bindingsToRemove.getImmutableMap().keySet();

        // Mutable
        Set<VariableImpl> localVariablesToRemove = new HashSet<>(allVariablesToRemove);
        localVariablesToRemove.retainAll(formerConstructionNode.getSubstitution().getImmutableMap().keySet());

        /**
         * Checks that no projected but not-bound variable was proposed to be removed.
         */
        ImmutableSet<Variable> projectedVariables = formerConstructionNode.getProjectionAtom().getVariables();
        for (VariableImpl variable : allVariablesToRemove) {
            if ((!localVariablesToRemove.contains(variable)) && projectedVariables.contains(variable)) {
                throw new InconsistentBindingException("The variable to remove " + variable + " is projected but" +
                        "not bound!");
            }
        }

        return ImmutableSet.copyOf(localVariablesToRemove);
    }

    /**
     * Extracts the variables that MUST be projected (if not already).
     *
     */
    private static ImmutableSet<VariableImpl> extractVariablesToProject(ImmutableSet<VariableImpl> variablesToRemove,
                                                                        ImmutableSubstitution<ImmutableTerm> bindingsToRemove) {

        Set<VariableImpl> variablesToProject = new HashSet<>();

        for (VariableImpl variableToRemove : variablesToRemove) {
            ImmutableTerm targetTerm = bindingsToRemove.get(variableToRemove);
            // TODO: remove this cast in the future
            variablesToProject.addAll((Collection<VariableImpl>)(Collection<?>)targetTerm.getReferencedVariables());
        }

        return ImmutableSet.copyOf(variablesToProject);
    }

    /**
     * TODO: explain
     */
    private static Optional<ImmutableQueryModifiers> computeNewOptionalModifiers(Optional<ImmutableQueryModifiers> optionalModifiers,
                                                                                 ImmutableSubstitution<ImmutableTerm> bindingsToRemove) {
        if (!optionalModifiers.isPresent())
            return Optional.absent();

        throw new RuntimeException("TODO: support the update of modifiers");
    }

    /**
     * TODO: explain
     *
     * TODO: check if the use of a set for ordering is safe
     */
    private static DataAtom computeNewDataAtom(DataAtom projectionAtom, ImmutableSet<VariableImpl> variablesToRemove,
                                               ImmutableSet<VariableImpl> newVariablesToProject) {
        // Mutable
        List<VariableOrGroundTerm> newArguments = new LinkedList<>(projectionAtom.getVariablesOrGroundTerms());
        newArguments.removeAll(variablesToRemove);
        for (VariableImpl newVariable : newVariablesToProject) {
            if (!newArguments.contains(newVariable)) {
                newArguments.add(newVariable);
            }
        }
        return DATA_FACTORY.getDataAtom(projectionAtom.getPredicate(), ImmutableList.copyOf(newArguments));
    }

    private static ImmutableSubstitution<VariableOrGroundTerm> convertToVarOrGroundTermSubstitution(
            ImmutableSubstitution<ImmutableTerm> substitution) throws SubstitutionConversionException {
        ImmutableMap.Builder<VariableImpl, VariableOrGroundTerm> mapBuilder = ImmutableMap.builder();
        for (Map.Entry<VariableImpl, ImmutableTerm> entry : substitution.getImmutableMap().entrySet()) {
            ImmutableTerm rightTerm = entry.getValue();
            if (rightTerm instanceof VariableOrGroundTerm) {
                mapBuilder.put(entry.getKey(), (VariableOrGroundTerm) rightTerm);
            }
        }
        return new ImmutableSubstitutionImpl<>(mapBuilder.build());
    }

}
