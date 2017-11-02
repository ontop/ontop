package it.unibz.inf.ontop.iq.optimizer.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.optimizer.BindingExtractor;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * Only deals with construction and union nodes
 */
@Singleton
public class UnionFriendlyBindingExtractor implements BindingExtractor {

    private static class PartialExtraction {


//        Stream<Map.Entry<Variable, ImmutableTerm>> mergedBindings;
        ImmutableMap<Variable, ImmutableTerm> mergedBindings;

        ImmutableSet<Variable> mergedVariables;

//        private Stream<Map.Entry<Variable, ImmutableTerm>> getMergedBindings() {
//            return mergedBindings;
//        }

        private Stream<Map.Entry<Variable, ImmutableTerm>> getMergedBindings() {
            return mergedBindings.entrySet().stream();
        }
        private ImmutableSet<Variable> getMergedVariables() {
            return mergedVariables;
        }

        private PartialExtraction(Stream<Map.Entry<Variable, ImmutableTerm>> mergedBindings, ImmutableSet<Variable> mergedVariables) {

            this.mergedBindings = mergedBindings.collect(ImmutableCollectors.toMap());
            this.mergedVariables =  mergedVariables;

        }
//        private PartialExtraction(Stream<Map.Entry<Variable, ImmutableTerm>> mergedBindings, ImmutableSet<Variable> mergedVariables) {
//
//            this.mergedBindings = mergedBindings;
//            this.mergedVariables =  mergedVariables;
//
//        }
    }

    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;

    @Inject
    private UnionFriendlyBindingExtractor(SubstitutionFactory substitutionFactory, TermFactory termFactory) {
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
    }

    @Override
    public Extraction extractInSubTree(IntermediateQuery query, QueryNode subTreeRootNode) {

        PartialExtraction substitutionMap = extractBindings(query, subTreeRootNode);


        return new Extraction() {
            @Override
            public Optional<ImmutableSubstitution<ImmutableTerm>> getOptionalSubstitution() {
                return Optional.of(substitutionMap.getMergedBindings().collect(ImmutableCollectors.toMap())).filter(m -> !m.isEmpty())
                        .map(substitutionFactory::getSubstitution);
            }

            @Override
            public ImmutableSet<Variable> getVariablesWithConflictingBindings() {
                return substitutionMap.getMergedVariables();
            }
        };

    }


    private PartialExtraction extractBindings(IntermediateQuery query, QueryNode currentNode){

        if (currentNode instanceof ConstructionNode) {
            return  new PartialExtraction(extractBindingsFromConstructionNode(query, (ConstructionNode) currentNode),  ImmutableSet.of());
        }
        else if (currentNode instanceof UnionNode) {
            return extractBindingsFromUnionNode(query, (UnionNode) currentNode);
        }
        /**
         * Stops when another node is found
         */
        else {
            return new PartialExtraction(Stream.of(), ImmutableSet.of());
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
                .flatMap(c -> extractBindings(query, c).getMergedBindings());

        return Stream.concat(localBindings, childBindings)
                .filter(e -> currentNode.getVariables().contains(e.getKey()));
    }

    /**
     * Extract the bindings from the union node, searching recursively for bindings in its children.
     * Ignore conflicting definitions of variables and return only the common bindings between the subtree
     */
    private PartialExtraction extractBindingsFromUnionNode(IntermediateQuery query,
                                                                           UnionNode currentNode) {

        ImmutableList<ImmutableMap<Variable, ImmutableTerm>> childrenBindings = query.getChildrenStream(currentNode)
                .map(c -> extractBindings(query, c).getMergedBindings()
                        .collect(ImmutableCollectors.toMap()))
                .collect(ImmutableCollectors.toList());

        ImmutableSet<Map.Entry<Variable, ImmutableTerm>> mergedBindings = childrenBindings.stream()
                .reduce((c1, c2) -> combineUnionChildBindings(query, c1, c2))
                .orElseThrow(() -> new IllegalStateException("A union must have children"))
                .entrySet();

        ImmutableSet<Variable> mergedVariables = mergedBindings.stream()
                .map(e -> e.getKey())
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<Variable> conflictingVariables = childrenBindings.stream()
                .flatMap(c -> c.keySet().stream())
                .filter(v -> !mergedVariables.contains(v))
                .collect(ImmutableCollectors.toSet());

        return new PartialExtraction(mergedBindings.stream(), conflictingVariables);
    }

    /**
     * Get the common and compatible bindings between the children of the union
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
     * Compare and combine the bindings, returning only the compatible values.
     * In case of variable, we generate and return a new variable to avoid inconsistency during propagation
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
            if(arguments1.size()!=arguments2.size()){
                throw new IllegalStateException("Functions have different arities, they cannot be combined");
            }

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
            return Optional.of(termFactory.getImmutableFunctionalTerm(functionalTerm1.getFunctionSymbol(),
                    argumentBuilder.build()));
        }
        else {
            return Optional.empty();
        }
    }

}
