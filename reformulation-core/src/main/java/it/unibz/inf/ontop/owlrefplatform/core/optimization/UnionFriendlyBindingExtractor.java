package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.pivotalrepr.ConstructionNode;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.UnionNode;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Only deals with construction and union nodes
 */
public class UnionFriendlyBindingExtractor implements BindingExtractor {

    //store conflicting and not common variables of the subTreeRootNode
    private Set<Variable> irregularVariables;


    @Override
    public Optional<ImmutableSubstitution<ImmutableTerm>> extractInSubTree(IntermediateQuery query,
                                                                           QueryNode subTreeRootNode) {

        irregularVariables = new HashSet<>();

        ImmutableMap<Variable, ImmutableTerm> substitutionMap = extractBindings(query, subTreeRootNode)
                .collect(ImmutableCollectors.toMap());

        return Optional.of(substitutionMap)
                .filter(m -> !m.isEmpty())
                .map(ImmutableSubstitutionImpl::new);
    }

    @Override
    public Optional<ImmutableSet<Variable>> getIrregularVariables(){

        return Optional.of(irregularVariables.stream().collect(ImmutableCollectors.toSet()))
                .filter(m -> !m.isEmpty());

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
     * Extract the bindings from the construction node, searching recursively for bindings in its children
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
     * Extract the bindings from the union node, searching recursively for bindings in its children.
     * Ignore conflicting definitions of variables and return only the common bindings between the subtree
     */
    private Stream<Map.Entry<Variable, ImmutableTerm>> extractBindingsFromUnionNode(IntermediateQuery query,
                                                                           UnionNode currentNode) {
        Map<Variable, ImmutableTerm> substitutionMap = new HashMap<>();
        Set<Variable> commonVariables = new HashSet<>();
        Set<Variable> variablesWithConflictingDefinitions = new HashSet<>();


        query.getFirstChild(currentNode).ifPresent(child -> {
            //get variables from the first child
            ImmutableSet<Variable> varsFirstChild = query.getVariables(child);

            commonVariables.addAll(varsFirstChild); }
        );

        //update commonVariables between the children
        query.getChildren(currentNode).forEach(child ->
                commonVariables.retainAll(query.getVariables(child)));

        query.getChildren(currentNode).stream()
                    .map(c -> extractBindings(query, c))
                    .forEach(entries -> entries
                            .forEach(e -> {
                                Variable variable = e.getKey();
                                ImmutableTerm value = e.getValue();

                                //return only the common bindings between the child sub tree and the non conflicting one
                                if (commonVariables.contains(variable) && !variablesWithConflictingDefinitions.contains(variable)) {
                                    Optional<ImmutableTerm> optionalPreviousValue = Optional.ofNullable(substitutionMap.get(variable));

                                    if (optionalPreviousValue.isPresent()) {
                                        if (!areCompatible(optionalPreviousValue.get(), value)) {
                                            substitutionMap.remove(variable);
                                            variablesWithConflictingDefinitions.add(variable);
                                            irregularVariables.add(variable);
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
                                else{
                                    commonVariables.remove(variable);
                                    irregularVariables.add(variable);
                                }
                            }));

        return substitutionMap.entrySet().stream();
    }

    /**
     * TODO: make explicit its assumptions and make sure they hold
     * Verify if the ImmutableTerm are Compatible
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
