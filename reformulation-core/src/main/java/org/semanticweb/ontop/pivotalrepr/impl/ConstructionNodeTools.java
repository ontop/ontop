package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import org.semanticweb.ontop.pivotalrepr.ConstructionNode;

import java.util.HashMap;
import java.util.Map;

import static org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionUtilities.computeOneWayUnifier;

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
     * TODO: determine if the substitution to propagate MUST BE VAR-TO-VAR. Better no!
     */
    public static BindingRemoval newNodeWithLessBindings(ConstructionNode formerConstructionNode,
                                                         ImmutableSubstitution<ImmutableTerm> bindingsToRemove)
            throws InconsistentBindingException{
        ImmutableMap.Builder<VariableImpl, ImmutableTerm> substitutionMapBuilder = ImmutableMap.builder();

        ImmutableMap<VariableImpl, ImmutableTerm> formerSubstitutionMap = formerConstructionNode.
                getDirectBindingSubstitution().getImmutableMap();

        ImmutableSet.Builder<VariableImpl> projectedVariablesToRemove = ImmutableSet.builder();
        ImmutableSet.Builder<VariableImpl> projectedVariablesToAdd = ImmutableSet.builder();

        Map<VariableImpl, VariableOrGroundTerm> substitutionMapToPropagate = new HashMap<>();


        for (VariableImpl variable : formerSubstitutionMap.keySet()) {
            ImmutableTerm previousTerm = formerSubstitutionMap.get(variable);

            /**
             * TODO: explain
             */
            if (!bindingsToRemove.isDefining(variable)) {
                substitutionMapBuilder.put(variable, previousTerm);
            }
            /**
             * TODO: explain
             *
             * The previous binding will be added to the new substitution.
             *
             */
            else {
                ImmutableTerm newTerm = bindingsToRemove.get(variable);

                projectedVariablesToRemove.add(variable);
                // TODO: remove this cast
                projectedVariablesToAdd.addAll((ImmutableSet<VariableImpl>)(ImmutableSet<?>)newTerm.getReferencedVariables());

                Optional<ImmutableSubstitution<ImmutableTerm>> optionalTermSubstitution = computeOneWayUnifier(
                        previousTerm, newTerm);
                /**
                 * If cannot be unified...
                 */
                if (!optionalTermSubstitution.isPresent()) {
                    throw new InconsistentBindingException("Contradictory bindings found in one child.");
                }

                // TODO: find a better name
                ImmutableSubstitution<VariableOrGroundTerm> termSubstitution;
                try {
                    termSubstitution = convertToVarOrGroundTermSubstitution(optionalTermSubstitution.get());
                } catch (SubstitutionConversionException e) {
                    throw new InconsistentBindingException("Incompatible bindings found in one child.");
                }
                if (!termSubstitution.isEmpty()) {
                    for (VariableImpl replacedVariable : termSubstitution.getImmutableMap().keySet()) {
                        VariableOrGroundTerm termToPropagate = termSubstitution.getImmutableMap().get(replacedVariable);

                        if (!substitutionMapToPropagate.containsKey(replacedVariable)) {
                            substitutionMapToPropagate.put(replacedVariable, termToPropagate);
                        }
                        else {
                            /**
                             * Should not have a "conflict" with a ground term. ---> must be a variable.
                             */
                            VariableOrGroundTerm otherTermToPropagate = substitutionMapToPropagate.get(replacedVariable);
                            if (!otherTermToPropagate.equals(termToPropagate)) {
                                if (termToPropagate instanceof VariableImpl) {
                                    /**
                                     * Registers the equality to the new substitution.
                                     */
                                    substitutionMapBuilder.put((VariableImpl) termToPropagate, otherTermToPropagate);
                                }
                                else {
                                    throw new InconsistentBindingException("Should not find a ground term here: " + termToPropagate);
                                }
                            }
                        }
                    }
                }

            }
        }

        throw new RuntimeException("Not fully implemented yet");
    }

    private static ImmutableSubstitution<VariableOrGroundTerm> convertToVarOrGroundTermSubstitution(ImmutableSubstitution<ImmutableTerm> substitution)
        throws SubstitutionConversionException {
        return null;
    }

}
