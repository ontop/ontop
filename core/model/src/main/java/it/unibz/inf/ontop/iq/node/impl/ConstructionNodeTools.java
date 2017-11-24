package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ImmutableQueryModifiers;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.Stream;

/**
 * TODO: explain
 */
@Singleton
public class ConstructionNodeTools {


    private final SubstitutionFactory substitutionFactory;

    @Inject
    private ConstructionNodeTools(SubstitutionFactory substitutionFactory) {
        this.substitutionFactory = substitutionFactory;
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

}
