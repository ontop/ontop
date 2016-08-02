package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.pivotalrepr.ConstructionNode;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.UnionNode;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.Stream;

/**
 * Only deals with construction and union nodes
 */
public class UnionFriendlyBindingExtractor implements BindingExtractor {

    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
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

        return query.getChildrenStream(currentNode)
                .map(c -> extractBindings(query, c)
                        .collect(ImmutableCollectors.toMap()))
                .reduce((c1, c2) -> combineUnionChildBindings(query, c1, c2))
                .orElseThrow(() -> new IllegalStateException("A union must have children"))
                .entrySet().stream();

//        Map<Variable, ImmutableTerm> substitutionMap = new HashMap<>();
//        Set<Variable> commonVariables = new HashSet<>();
//        Set<Variable> variablesWithConflictingDefinitions = new HashSet<>();
//
//
//        query.getFirstChild(currentNode).ifPresent(child -> {
//            //get variables from the first child
//            ImmutableSet<Variable> varsFirstChild = query.getVariables(child);
//
//            commonVariables.addAll(varsFirstChild); }
//        );
//
//        //update commonVariables between the children
//        query.getChildren(currentNode).forEach(child ->
//                commonVariables.retainAll(query.getVariables(child)));
//
//        query.getChildren(currentNode).stream()
//                    .map(c -> extractBindings(query, c))
//                    .forEach(entries -> entries
//                            .forEach(e -> {
//                                Variable variable = e.getKey();
//                                ImmutableTerm value = e.getValue();
//
//                                //return only the common bindings between the child sub tree and the non conflicting one
//                                if (commonVariables.contains(variable) && !variablesWithConflictingDefinitions.contains(variable)) {
//                                    Optional<ImmutableTerm> optionalPreviousValue = Optional.ofNullable(substitutionMap.get(variable));
//
//                                    if (optionalPreviousValue.isPresent()) {
//                                        if (!areCompatible(optionalPreviousValue.get(), value)) {
//                                            substitutionMap.remove(variable);
//                                            variablesWithConflictingDefinitions.add(variable);
//                                            irregularVariables.add(variable);
//                                        }
//                                        // otherwise does nothing
//                                    }
//                                    /**
//                                     * New variable
//                                     */
//                                    else {
//                                        substitutionMap.put(variable, value);
//                                    }
//                                }
//                                else{
//                                    commonVariables.remove(variable);
//                                    irregularVariables.add(variable);
//                                }
//                            }));
//
//        return substitutionMap.entrySet().stream();
    }

    /**
     * TODO: explain
     */
    private ImmutableMap<Variable, ImmutableTerm> combineUnionChildBindings(
            IntermediateQuery query,
            ImmutableMap<Variable, ImmutableTerm> firstChildBindings,
            ImmutableMap<Variable, ImmutableTerm> secondChildBindings) {

        return firstChildBindings.entrySet().stream()
                .map(cb -> Optional.ofNullable(secondChildBindings.get(cb.getKey()))
                        .flatMap(v -> combineUnionChildBindingValues(query, cb.getValue(), v))
                        .map(value -> new AbstractMap.SimpleEntry<>(cb.getKey(), value)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableCollectors.toMap());
    }

    /**
     * TODO: explain
     */
    private Optional<ImmutableTerm> combineUnionChildBindingValues(IntermediateQuery query, ImmutableTerm firstValue, ImmutableTerm secondValue) {

        if (firstValue.equals(secondValue)) {
            return Optional.of(firstValue);
        }
        else if (firstValue instanceof Variable)  {
            return Optional.of(query.generateNewVariable((Variable) firstValue));
        }
        else if (secondValue instanceof Variable)  {
            return Optional.of(query.generateNewVariable((Variable) secondValue));
        }
        else if ((firstValue instanceof ImmutableFunctionalTerm) && (secondValue instanceof ImmutableFunctionalTerm)) {
            ImmutableFunctionalTerm functionalTerm1 = (ImmutableFunctionalTerm) firstValue;
            ImmutableFunctionalTerm functionalTerm2 = (ImmutableFunctionalTerm) secondValue;

            /**
             * NB: function symbols are in charge of enforcing the declared arities
             */
            if (!functionalTerm1.getFunctionSymbol().equals(functionalTerm2.getFunctionSymbol())) {
                return Optional.empty();
            }

            ImmutableList<? extends ImmutableTerm> arguments1 = functionalTerm1.getArguments();
            ImmutableList<? extends ImmutableTerm> arguments2 = functionalTerm2.getArguments();

            ImmutableList.Builder<ImmutableTerm> argumentBuilder = ImmutableList.builder();
            for(int i=0; i <  arguments1.size(); i++) {
                Optional<ImmutableTerm> optionalNewArgument = combineUnionChildBindingValues(query,
                        arguments1.get(i), arguments2.get(i));
                if (optionalNewArgument.isPresent()) {
                    argumentBuilder.add(optionalNewArgument.get());
                }
                else {
                    return Optional.empty();
                }
            }
            return Optional.of(DATA_FACTORY.getImmutableFunctionalTerm(functionalTerm1.getFunctionSymbol(),
                    argumentBuilder.build()));
        }
        else {
            return Optional.empty();
        }
    }

}
