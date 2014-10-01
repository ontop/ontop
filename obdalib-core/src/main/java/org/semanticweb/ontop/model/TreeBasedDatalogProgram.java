package org.semanticweb.ontop.model;

import java.util.*;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.Multimap;
import fj.*;
import fj.data.*;
import fj.data.List;
import fj.data.TreeMap;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.semanticweb.ontop.utils.DatalogDependencyGraphGenerator;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Immutable DatalogProgram for queries.
 * It assumes that the rules can be organized in a tree (predicate tree).
 *
 * Due to its immutable collections, a functional style should be adopted
 * to manipulate its attributes.
 * In this implementation, we use FunctionalJava.
 *
 * Former name: indexedDatalogProgram. This name has been replaced
 * because most indexes have been replaced by a tree structure.
 *
 */
public class TreeBasedDatalogProgram {

    public static final Ord<Predicate> predicateOrder = Ord.ord(
            new F<Predicate, F<Predicate, Ordering>>() {
                public F<Predicate, Ordering> f(final Predicate a1) {
                    return new F<Predicate, Ordering>() {
                        public Ordering f(final Predicate a2) {
                            //TODO: check arity later
                            final int x = a1.getName().compareTo(a2.getName());
                            return x < 0 ? Ordering.LT : x == 0 ? Ordering.EQ : Ordering.GT;
                        }
                    };
                }
            });

    /**
     * Predicate hierarchy in this query-oriented DatalogProgram.
     *
     * Final and immutable structure.
     */
    private final Tree<Predicate> predicateTree;

    /**
     * Map of rules indexed by the predicate they defined.
     * Multiple definitions for a given rule are possible.
     *
     * Final and immutable structure.
     */
    private final TreeMap<Predicate, List<CQIE>> predicateDefinitions;

    /**
     * Index subtrees so that to efficiently retrieve them
     * (without iterating through the tree).
     *
     * TODO: Not used for the moment, see if is really needed.
     */
    //private final TreeMap<Predicate, Tree<Predicate>> predicateSubTreeIndex;

    /**
     * Predicates in bodies
     * TODO: do we really need this?
     */
    //private final TreeMap<Predicate, List<CQIE>> predicateUsages;


    /**
     * Very expensive constructor:
     *   - The rule lists have to be converted and indexed.
     *   - The predicate tree has to be built.
     *
     * @param rules
     */
    public TreeBasedDatalogProgram(java.util.List<CQIE> rules) {
        DatalogDependencyGraphGenerator dependencyGraphGenerator = new DatalogDependencyGraphGenerator(rules);

        predicateTree = convertDirectedGraphToTree(dependencyGraphGenerator.getPredicateDependencyGraph());
        predicateDefinitions = convertMultimapToMap(dependencyGraphGenerator.getRuleIndex());
        //predicateSubTreeIndex = buildIndexFromTree(predicateTree);
        //predicateUsages = convertMultimapToMap(dependencyGraphGenerator.getRuleIndexByBodyPredicate());
    }

    /**
     * TODO: worth it?
     *
     * Expensive constructor: the predicate tree has to be built
     * @param predicateDefinitions
     */
    public TreeBasedDatalogProgram(TreeMap<Predicate, List<CQIE>> predicateDefinitions) {
        //TODO: implement it
        throw new NotImplementedException();
    }

    /**
     * Semi-expensive constructor: the predicate tree as to be checked.
     *
     * @param predicateTree
     * @param predicateDefinitions
     */
    public TreeBasedDatalogProgram(TreeMap<Predicate, List<CQIE>> predicateDefinitions, Tree<Predicate> predicateTree) {
        this(predicateTree, predicateDefinitions);
        //TODO: quickly check the children
    }

    /**
     * Fast but dangerous constructor.
     * Inconsistency between the predicate tree and the predicate definitions are not be detected.
     *
     * @param predicateTree
     * @param predicateDefinitions
     */
    private TreeBasedDatalogProgram(Tree<Predicate> predicateTree, TreeMap<Predicate, List<CQIE>> predicateDefinitions) {
        this.predicateTree = predicateTree;
        this.predicateDefinitions = predicateDefinitions;
    }

    private TreeBasedDatalogProgram(TreeBasedDatalogProgram that, CQIE newRule) {
        predicateTree = addDependencyToTree(that.predicateTree, newRule);
        predicateDefinitions = addPredicateDefinition(that.predicateDefinitions, newRule);
        //predicateUsages = addPredicateUsage(that.predicateUsages, newRule);
    }

    /**
     * TODO: implement it!
     */
    public TreeBasedDatalogProgram(TreeZipper<P3<Predicate, List<CQIE>, Option<Function>>> treeZipper) {
        throw new NotImplementedException();
    }

    public List<CQIE> getRules() {
        return List.join(predicateDefinitions.values());
    }

    /**
     * Immutable
     */
    public Tree<Predicate> getPredicateTree() {
        return predicateTree;
    }

    /**
     * Computes a rule tree.
     *
     * This tree can be seen as a merge of the predicate tree
     * and the predicate definition map.
     *
     * Each node refers to a predicate, its definition rules
     * but also an optional Function. The latter is none.
     *
     * This structure is used for lifting types.
     */
    public Tree<P3<Predicate, List<CQIE>, Option<Function>>> computeRuleTree() {
        Tree<P3<Predicate, List<CQIE>, Option<Function>>> ruleTree = predicateTree.fmap(
                new F<Predicate, P3<Predicate, List<CQIE>, Option<Function>>>() {
                    @Override
                    public P3<Predicate, List<CQIE>, Option<Function>> f(Predicate predicate) {
                        return P.p(predicate, getDefinition(predicate), Option.<Function>none());
                    }
                });
        return ruleTree;
    }

    /**
     * Immutable
     */
    public TreeMap<Predicate, List<CQIE>> getPredicateDefinitions() {
        return predicateDefinitions;
    }

    /**
     * Returns an immutable list
     */
    public List<CQIE> getDefinition(Predicate predicate) {
        Option<List<CQIE>> optionalRules = predicateDefinitions.get(predicate);
        if (optionalRules.isSome()) {
            return optionalRules.some();
        }
        return List.nil();
    }


    /**
     * TODO: do we really need this method?
     * Is trying to update the predicate tree really necessary?
     */
    public TreeBasedDatalogProgram addRule(CQIE newRule) {
        return new TreeBasedDatalogProgram(this, newRule);
    }

    /**
     * TODO: do we need this method?
     */
    public TreeBasedDatalogProgram replaceRule(CQIE formerRule, CQIE newRule) {
        //TODO: implement it
        throw new NotImplementedException();
    }

    /**
     * TODO: do we need this structure?
     */
    public TreeBasedDatalogProgram replaceRule(CQIE formerRule, List<CQIE> newRule) {
        //TODO: implement it
        throw new NotImplementedException();
    }

    /**
     *
     * TODO: do we need this structure?
     */
    public TreeBasedDatalogProgram replaceRules(Predicate headPredicate, CQIE newRule) {
        //TODO: implement it
        throw new NotImplementedException();
    }

    /**
     * TODO: do we really need this structure?
     */
    public TreeBasedDatalogProgram replaceRules(Predicate headPredicate, List<CQIE> newRules) {
        //TODO: implement it
        throw new NotImplementedException();
    }

    /**
     * TODO: do we really need this structure?
     *
     * It may become necessary to update the predicate tree
     *
     */
    public TreeBasedDatalogProgram removeRule(CQIE rule) {
        //TODO: implement it
        throw new NotImplementedException();
    }


    private static TreeMap<Predicate,List<CQIE>> convertMultimapToMap(Multimap<Predicate, CQIE> ruleIndex) {
        Map<Predicate, Collection<CQIE>> map1 = ruleIndex.asMap();
        Map<Predicate, List<CQIE>> map2 = new HashMap<>();

        for (Map.Entry<Predicate, Collection<CQIE>> entry: map1.entrySet()) {
            map2.put(entry.getKey(), List.iterableList(entry.getValue()));
        }

        return TreeMap.fromMutableMap(predicateOrder, map2);
    }

    private static Tree<Predicate> convertDirectedGraphToTree(DirectedGraph<Predicate, DefaultEdge> originalGraph) {
        TopologicalOrderIterator<Predicate, DefaultEdge> iter = new TopologicalOrderIterator<>(originalGraph);
        if (!iter.hasNext()) {
            throw new IllegalArgumentException("Empty dependency graph given");
        }
        Predicate rootPredicate = iter.next();
        return createSubTree(rootPredicate, originalGraph);
    }

    /**
     * Iterates over all the nodes (with a zipper structure) and adds
     *
     * TODO: replace the mutable map to immutable maps
     */
    private static TreeMap<Predicate,Tree<Predicate>> buildIndexFromTree(Tree<Predicate> predicateTree) {
        TreeZipper<Predicate> rootZipper =  TreeZipper.fromTree(predicateTree);

        Map<Predicate, Tree<Predicate>> mutableMap = new HashMap<>();
        Iterator<TreeZipper<Predicate>> iterator = rootZipper.iterator();
        while (iterator.hasNext()) {
            TreeZipper<Predicate> currentZipper =  iterator.next();
            mutableMap.put(currentZipper.getLabel(), currentZipper.focus());
        }

        return TreeMap.fromMutableMap(predicateOrder, mutableMap);
    }

    private static Tree<Predicate> addDependencyToTree(Tree<Predicate> predicateTree, final CQIE newRule) {
        TreeZipper<Predicate> rootZipper = TreeZipper.fromTree(predicateTree);

        final Predicate headPredicate = newRule.getHead().getFunctionSymbol();

        Option<TreeZipper<Predicate>> optionalHeadPredicateZipper = rootZipper.findChild(new F<Tree<Predicate>, Boolean>() {
            @Override
            public Boolean f(Tree<Predicate> tree) {
                return tree.root().equals(headPredicate);
            }
        });

        java.util.Set<Predicate> bodyPredicates = new HashSet<>();
        for (Function bodyAtom: newRule.getBody()) {
            // TODO: make it robust to left join (implement this in the CQIE class)
            // TODO: see QueryUtils for reuse
            bodyPredicates.add(bodyAtom.getFunctionSymbol());
        }

        /**
         * If the head predicate is not in the tree, this tree should be empty but this is not possible.
         *
         * --> Throws an Exception
         */
        if (optionalHeadPredicateZipper.isNone()) {
//            List<Tree<Predicate>> bodyPredicateForest = Stream.iterableStream(bodyPredicates).map(
//                    new F<Predicate, Tree<Predicate>>() {
//                        @Override
//                        public Tree<Predicate> f(Predicate predicate) {
//                            return Tree.node(predicate, List.<Tree<Predicate>>nil());
//                        }
//                    }).toList();
            throw new IllegalArgumentException("The head predicate of the rule is not already present " +
                    "in the predicate tree of the DatalogProgram");
        }
        /**
         * If the head predicate of the rule is already within the tree
         */
        else  {
            TreeZipper<Predicate> predicateZipper = optionalHeadPredicateZipper.some();
            // TODO: check if predicateZipper.toForest() also does the job
            Stream<Tree<Predicate>> children = predicateZipper.toTree().subForest()._1();

            // Adds new body function symbols (if not already present)
            for (final Predicate predicate : bodyPredicates) {

                /*
                 * Boolean function that returns True when the root of a sub-tree
                 * is the expected predicate.
                 */
                F<Tree<Predicate>, Boolean> equalsFunction = new F<Tree<Predicate>, Boolean>() {
                    @Override
                    public Boolean f(Tree<Predicate> tree) {
                        return tree.root().equals(predicate);
                    }
                };

                /**
                 * If this body predicate is not yet a child of the head predicate,
                 * adds it.
                 */
                if (children.indexOf(equalsFunction).isNone())
                    //TODO: avoid such re-affecting
                    predicateZipper = predicateZipper.insertDownLast(Tree.node(predicate, List.<Tree<Predicate>>nil())).parent().some();
            }
            return predicateZipper.root().toTree();
        }
    }


    private static TreeMap<Predicate,List<CQIE>> addPredicateDefinition(TreeMap<Predicate, List<CQIE>> predicateDefinitions, CQIE newRule) {
        // TODO: implement it
        return null;
    }

    /**
     * Recursive call.
     *
     * TODO: replace it to prevent stack overflow (for big DatalogPrograms).
     */
    private static Tree<Predicate> createSubTree(Predicate predicate, DirectedGraph<Predicate, DefaultEdge> originalGraph) {
        java.util.List<Tree<Predicate>> subTrees = new ArrayList<>();

        for (DefaultEdge edge: originalGraph.outgoingEdgesOf(predicate)) {
            Predicate subPredicate = (Predicate)edge.getTarget();
            subTrees.add(createSubTree(subPredicate, originalGraph));
        }

        return Tree.node(predicate, List.iterableList(subTrees));
    }

}
