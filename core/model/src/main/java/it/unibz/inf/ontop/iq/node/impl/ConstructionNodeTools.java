package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ImmutableQueryModifiers;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.Stream;

/**
 * TODO: explain
 */
@Singleton
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
                return Optional.empty();
            }
            return Optional.of(substitutionToPropagate);
        }
    }


    private final SubstitutionFactory substitutionFactory;
    private final ImmutableSubstitutionTools substitutionTools;

    @Inject
    private ConstructionNodeTools(SubstitutionFactory substitutionFactory,
                                  ImmutableSubstitutionTools substitutionTools) {
        this.substitutionFactory = substitutionFactory;
        this.substitutionTools = substitutionTools;
    }

    public ConstructionNode merge(ConstructionNode parentConstructionNode,
                                         ConstructionNode childConstructionNode, IntermediateQueryFactory iqFactory) {

        ImmutableSubstitution<ImmutableTerm> composition = childConstructionNode.getSubstitution().composeWith(
                parentConstructionNode.getSubstitution());

        ImmutableSet<Variable> projectedVariables = parentConstructionNode.getVariables();

        ImmutableSubstitution<ImmutableTerm> newSubstitution = projectedVariables.containsAll(
                childConstructionNode.getVariables())
                ? composition
                : substitutionFactory.getSubstitution(
                composition.getImmutableMap().entrySet().stream()
                        .filter(e -> !projectedVariables.contains(e.getKey()))
                        .collect(ImmutableCollectors.toMap()));

        if (parentConstructionNode.getOptionalModifiers().isPresent()
                && childConstructionNode.getOptionalModifiers().isPresent()) {
            // TODO: find a better exception
            throw new RuntimeException("TODO: support combination of modifiers");
        }

        // TODO: should update the modifiers?
        Optional<ImmutableQueryModifiers> optionalModifiers = parentConstructionNode.getOptionalModifiers()
                .map(Optional::of)
                .orElseGet(childConstructionNode::getOptionalModifiers);

        return iqFactory.createConstructionNode(projectedVariables, newSubstitution, optionalModifiers);
    }

    public ImmutableSet<Variable> computeNewProjectedVariables(
            ImmutableSubstitution<? extends ImmutableTerm> descendingSubstitution, ImmutableSet<Variable> projectedVariables) {
        ImmutableSet<Variable> tauDomain = descendingSubstitution.getDomain();

        Stream<Variable> remainingVariableStream = projectedVariables.stream()
                .filter(v -> !tauDomain.contains(v));

        Stream<Variable> newVariableStream = descendingSubstitution.getImmutableMap().entrySet().stream()
                .filter(e -> projectedVariables.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(ImmutableTerm::getVariableStream);

        return Stream.concat(newVariableStream, remainingVariableStream)
                .collect(ImmutableCollectors.toSet());
    }

    public ImmutableSubstitution<ImmutableTerm> extractRelevantDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> descendingSubstitution,
            ImmutableSet<Variable> projectedVariables) {
        ImmutableMap<Variable, ImmutableTerm> newSubstitutionMap = descendingSubstitution.getImmutableMap().entrySet().stream()
                .filter(e -> projectedVariables.contains(e.getKey()))
                .map(e -> (Map.Entry<Variable, ImmutableTerm>) e)
                .collect(ImmutableCollectors.toMap());

        return substitutionFactory.getSubstitution(newSubstitutionMap);
    }


    /**
     * TODO: explain
     */
    public ConstructionNode newNodeWithAdditionalBindings(IntermediateQueryFactory iqFactory,
                                                                 ConstructionNode formerConstructionNode,
                                                                 ImmutableSubstitution<ImmutableTerm> additionalBindingsSubstitution)
            throws InconsistentBindingException {

        ImmutableSet<Variable> projectedVariables = formerConstructionNode.getVariables();

        /**
         * TODO: explain why the composition is too rich
         */
        ImmutableSubstitution<ImmutableTerm> composedSubstitution = additionalBindingsSubstitution.composeWith(formerConstructionNode.getSubstitution());
        ImmutableMap.Builder<Variable, ImmutableTerm> substitutionMapBuilder = ImmutableMap.builder();

        ImmutableMap<Variable, ImmutableTerm> compositionMap = composedSubstitution.getImmutableMap();

        for(Variable variable : compositionMap.keySet()) {
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

        return iqFactory.createConstructionNode(projectedVariables, substitutionFactory.getSubstitution(substitutionMapBuilder.build()),
                formerConstructionNode.getOptionalModifiers());

    }

    /**
     * TODO: explain
     *
     * TODO: refactor
     *
     */
    public BindingRemoval newNodeWithLessBindings(IntermediateQueryFactory iqFactory,
                                                         ConstructionNode formerConstructionNode,
                                                         ImmutableSubstitution<ImmutableTerm> bindingsToRemove)
            throws InconsistentBindingException {

        ImmutableSet<Variable> variablesToRemove = extractVariablesToRemove(formerConstructionNode, bindingsToRemove);

        ImmutableSet<Variable> newVariablesToProject = extractVariablesToProject(variablesToRemove, bindingsToRemove);

        NewSubstitutions newSubstitutions = computeSubstitutionToPropagateAndNewBindings(formerConstructionNode, bindingsToRemove,
               variablesToRemove, newVariablesToProject);

        ImmutableSubstitution<ImmutableTerm> newBindingSubstitution = computeNewBindingSubstitution(formerConstructionNode, variablesToRemove,
                newSubstitutions.getNewBindings());


        Optional<ImmutableQueryModifiers> newOptionalModifiers = computeNewOptionalModifiers(formerConstructionNode.getOptionalModifiers(),
                bindingsToRemove);

        ConstructionNode newConstructionNode = iqFactory.createConstructionNode(newVariablesToProject, newBindingSubstitution, newOptionalModifiers);

        return new BindingRemoval(newConstructionNode, newSubstitutions.getOptionalSubstitutionToPropagate());
    }

    /**
     * TODO: explain
     */
    private ImmutableSubstitution<ImmutableTerm> computeNewBindingSubstitution(
            ConstructionNode formerConstructionNode, ImmutableSet<Variable> variablesToRemove,
            ImmutableSubstitution<VariableOrGroundTerm> newBindings) {
        ImmutableMap.Builder<Variable, ImmutableTerm> mapBuilder = ImmutableMap.builder();

        ImmutableMap<Variable, ImmutableTerm> formerSubstitutionMap = formerConstructionNode.getSubstitution().getImmutableMap();
        for (Variable variable : formerSubstitutionMap.keySet()) {
            if (!variablesToRemove.contains(variable)) {
                mapBuilder.put(variable, formerSubstitutionMap.get(variable));
            }
        }

        mapBuilder.putAll(newBindings.getImmutableMap());

        return substitutionFactory.getSubstitution(mapBuilder.build());

    }

    /**
     * TODO: explain
     */
    private ImmutableList<ImmutableSubstitution<VariableOrGroundTerm>> computeBindingUnifiers(
            ConstructionNode formerConstructionNode, ImmutableSet<Variable> variablesToRemove,
            ImmutableSubstitution<ImmutableTerm> bindingsToRemove) {

        ImmutableSubstitution<ImmutableTerm> formerSubstitution = formerConstructionNode.getSubstitution();

        ImmutableList.Builder<ImmutableSubstitution<VariableOrGroundTerm>> substitutionListBuilder = ImmutableList.builder();

        for (Variable variable : variablesToRemove) {
            ImmutableTerm formerTerm = formerSubstitution.get(variable);
            ImmutableTerm newTerm = bindingsToRemove.get(variable);

            Optional<ImmutableSubstitution<ImmutableTerm>> optionalTermSubstitution =
                    substitutionTools.computeUnidirectionalSubstitution(formerTerm, newTerm);
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
    private NewSubstitutions computeSubstitutionToPropagateAndNewBindings(
            ConstructionNode formerConstructionNode, ImmutableSubstitution<ImmutableTerm> bindingsToRemove,
            ImmutableSet<Variable> variablesToRemove, ImmutableSet<Variable> newVariablesToProject) {

        ImmutableList<ImmutableSubstitution<VariableOrGroundTerm>> bindingUnifiers = computeBindingUnifiers(
                formerConstructionNode, variablesToRemove, bindingsToRemove);

        Map<Variable, VariableOrGroundTerm> substitutionMapToPropagate = new HashMap<>();
        ImmutableMap.Builder<Variable, VariableOrGroundTerm> newBindingsMapBuilder = ImmutableMap.builder();


        for (ImmutableSubstitution<VariableOrGroundTerm> unifier : bindingUnifiers) {
            ImmutableMap<Variable, VariableOrGroundTerm> unificationMap = unifier.getImmutableMap();

            if (!unifier.isEmpty()) {
                for (Variable replacedVariable : unificationMap.keySet()) {
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
                                if (targetTerm instanceof Variable) {
                                    /**
                                     * Registers the equality to the new substitution.
                                     */
                                    newBindingsMapBuilder.put((Variable) targetTerm, otherTermToPropagate);
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

        ImmutableSubstitution<VariableOrGroundTerm> substitutionToPropagate = substitutionFactory.getSubstitution(
                ImmutableMap.copyOf(substitutionMapToPropagate));
        ImmutableSubstitution<VariableOrGroundTerm> newBindings = substitutionFactory.getSubstitution(newBindingsMapBuilder.build());

        return new NewSubstitutions(substitutionToPropagate, newBindings);
    }

    /**
     * TODO: explain
     *
     */
    private static ImmutableSet<Variable> extractVariablesToRemove(ConstructionNode formerConstructionNode,
                                                                       ImmutableSubstitution<ImmutableTerm> bindingsToRemove)
            throws InconsistentBindingException {

        ImmutableSet<Variable> allVariablesToRemove = bindingsToRemove.getImmutableMap().keySet();

        // Mutable
        Set<Variable> localVariablesToRemove = new HashSet<>(allVariablesToRemove);
        localVariablesToRemove.retainAll(formerConstructionNode.getSubstitution().getImmutableMap().keySet());

        /**
         * Checks that no projected but not-bound variable was proposed to be removed.
         */
        ImmutableSet<Variable> projectedVariables = formerConstructionNode.getVariables();
        for (Variable variable : allVariablesToRemove) {
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
    private static ImmutableSet<Variable> extractVariablesToProject(ImmutableSet<Variable> variablesToRemove,
                                                                        ImmutableSubstitution<ImmutableTerm> bindingsToRemove) {

        Set<Variable> variablesToProject = new HashSet<>();

        for (Variable variableToRemove : variablesToRemove) {
            ImmutableTerm targetTerm = bindingsToRemove.get(variableToRemove);
            if (targetTerm instanceof Variable) {
                variablesToProject.add((Variable)targetTerm);
            }
            else if (targetTerm instanceof ImmutableFunctionalTerm) {
                variablesToProject.addAll(((ImmutableFunctionalTerm) targetTerm).getVariables());
            }
        }

        return ImmutableSet.copyOf(variablesToProject);
    }

    /**
     * TODO: explain
     */
    private Optional<ImmutableQueryModifiers> computeNewOptionalModifiers(Optional<ImmutableQueryModifiers> optionalModifiers,
                                                                                 ImmutableSubstitution<ImmutableTerm> bindingsToRemove) {
        if (!optionalModifiers.isPresent())
            return Optional.empty();

        throw new RuntimeException("TODO: support the update of modifiers");
    }

    private ImmutableSubstitution<VariableOrGroundTerm> convertToVarOrGroundTermSubstitution(
            ImmutableSubstitution<ImmutableTerm> substitution) throws SubstitutionConversionException {
        ImmutableMap.Builder<Variable, VariableOrGroundTerm> mapBuilder = ImmutableMap.builder();
        for (Map.Entry<Variable, ImmutableTerm> entry : substitution.getImmutableMap().entrySet()) {
            ImmutableTerm rightTerm = entry.getValue();
            if (rightTerm instanceof VariableOrGroundTerm) {
                mapBuilder.put(entry.getKey(), (VariableOrGroundTerm) rightTerm);
            }
        }
        return substitutionFactory.getSubstitution(mapBuilder.build());
    }

}
