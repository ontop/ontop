package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.*;

import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;


import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Only deals with construction and union nodes
 */
public class UnionFriendlyBindingExtractor implements BindingExtractor {


    @Override
    public Optional<ImmutableSubstitution<ImmutableTerm>> extractInSubTree(IntermediateQuery query,
                                                                           QueryNode subTreeRootNode) {
        ImmutableMap<Variable, ImmutableTerm> substitutionMap = extractBindings(query, subTreeRootNode)
                .collect(ImmutableCollectors.toMap());

        return Optional.of(substitutionMap)
                .filter(ImmutableMap::isEmpty)
                .map(ImmutableSubstitutionImpl::new);
    }

    private Stream<Map.Entry<Variable, ImmutableTerm>> extractBindings(IntermediateQuery query, QueryNode currentNode){

        if (currentNode instanceof ConstructionNode) {
            return extractBindingsFromConstructionNode(query, (ConstructionNode) currentNode);
        }
        else if (currentNode instanceof UnionNode) {
            return extractBindingsFromUnionNode(query, (UnionNode) currentNode);
        }
        /**
         * Stops when another node is found
         */
        else {
            return Stream.of();
        }
    }

    /**
     * TODO: explain
     */
    private Stream<Map.Entry<Variable, ImmutableTerm>> extractBindingsFromConstructionNode(IntermediateQuery query,
                                                                                  ConstructionNode currentNode) {
        Stream<Map.Entry<Variable, ImmutableTerm>> localBindings = currentNode.getSubstitution()
                .getImmutableMap().entrySet().stream();

        // Recursive
        Stream<Map.Entry<Variable, ImmutableTerm>> childBindings = query.getChildren(currentNode).stream()
                .flatMap(c -> extractBindings(query, c));

        return Stream.concat(localBindings, childBindings);
    }

    /**
     * TODO: explain
     */
    private Stream<Map.Entry<Variable, ImmutableTerm>> extractBindingsFromUnionNode(IntermediateQuery query,
                                                                           UnionNode currentNode) {
        Map<Variable, ImmutableTerm> substitutionMap = new HashMap<>();
        Set<Variable> variablesWithConflictingDefinitions = new HashSet<>();

        query.getChildren(currentNode).stream()
                .map(c -> extractBindings(query, c))
                .forEach(entries -> entries
                        .forEach(e -> {
                            Variable variable = e.getKey();
                            ImmutableTerm value = e.getValue();

                            if (!variablesWithConflictingDefinitions.contains(variable)) {
                                Optional<ImmutableTerm> optionalPreviousValue = Optional.ofNullable(substitutionMap.get(variable));

                                if (optionalPreviousValue.isPresent()) {
                                    if (!areCompatible(optionalPreviousValue.get(), value)) {
                                        substitutionMap.remove(variable);
                                        variablesWithConflictingDefinitions.add(variable);
                                    }
                                    // otherwise does nothing
                                }
                                /**
                                 * New variable
                                 */
                                else {
                                    substitutionMap.put(variable, value);
                                }
                            }
                        }));

        return substitutionMap.entrySet().stream();
    }

    /**
     * TODO: make explicit its assumptions ans make sure they hold
     */
    private static boolean areCompatible(ImmutableTerm term1, ImmutableTerm term2) {
        if (term1.equals(term2)) {
            return true;
        }

        else if ((term1 instanceof Variable) || (term2 instanceof Variable)) {
            return true;
        }

        else if ((term1 instanceof ImmutableFunctionalTerm) && (term2 instanceof ImmutableFunctionalTerm)) {
            ImmutableFunctionalTerm functionalTerm1 = (ImmutableFunctionalTerm) term1;
            ImmutableFunctionalTerm functionalTerm2 = (ImmutableFunctionalTerm) term2;

            if (!functionalTerm1.getFunctionSymbol().equals(functionalTerm2.getFunctionSymbol())) {
                return false;
            }

            ImmutableList<? extends ImmutableTerm> arguments1 = functionalTerm1.getArguments();
            ImmutableList<? extends ImmutableTerm> arguments2 = functionalTerm2.getArguments();

            return IntStream.range(0, functionalTerm1.getArity())
                    .allMatch(i -> areCompatible(arguments1.get(i), arguments2.get(i)));
        }
        else {
            return false;
        }
    }

}
