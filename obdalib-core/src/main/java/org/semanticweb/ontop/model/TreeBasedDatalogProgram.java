package org.semanticweb.ontop.model;

import java.util.*;

import com.google.common.collect.Multimap;
import fj.*;
import fj.data.*;
import fj.data.List;
import fj.data.HashMap;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.semanticweb.ontop.utils.DatalogDependencyGraphGenerator;

/**
 * Immutable DatalogProgram for queries.
 * It assumes that the rules can be organized in a tree (predicate tree).
 *
 * Due to its immutable collections, a functional style should be adopted
 * to manipulate its attributes.
 * In this implementation, we use FunctionalJava.
 *
 *
 * TODO: PROPOSAL: use a Tree<P2<Predicate, List<CQIE>>> instead of predicateTree and predicateDefinitions.
 * Apparently, we may not need global indexes. Tree navigation seems to be enough.
 *
 */
public class TreeBasedDatalogProgram {

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
     * Beware, it is not a java.util.HashMap !!
     *
     * Sorry, we do not have a nice convenient interface for it.
     */
    private final HashMap<Predicate, List<CQIE>> predicateDefinitions;

    /**
     * Very expensive construction method:
     *   - The rule lists have to be converted and indexed.
     *   - The predicate tree has to be built.
     *
     * @param rules
     */
    public static TreeBasedDatalogProgram fromRules(java.util.List<CQIE> rules) {
        return new TreeBasedDatalogProgram(rules);
    }

    private TreeBasedDatalogProgram(java.util.List<CQIE> rules) {
        DatalogDependencyGraphGenerator dependencyGraphGenerator = new DatalogDependencyGraphGenerator(rules);

        predicateTree = convertDirectedGraphToTree(dependencyGraphGenerator.getPredicateDependencyGraph());
        predicateDefinitions = convertMultimapToMap(dependencyGraphGenerator.getRuleIndex());
    }

    /**
     * Construction method from a normal rule tree.
     *
     */
    public static TreeBasedDatalogProgram fromRuleTree(Tree<P2<Predicate, List<CQIE>>> ruleTree) {
        return new TreeBasedDatalogProgram(ruleTree);
    }

    /**
     * Construction method from a P3 rule tree.
     *
     * Third elements of the tree labels are ignored.
     */
    public static TreeBasedDatalogProgram fromP3RuleTree(Tree<P3<Predicate, List<CQIE>, Option<Function>>> p3Tree) {
        return new TreeBasedDatalogProgram(convertP32P2RuleTree(p3Tree));
    }

    private TreeBasedDatalogProgram(Tree<P2<Predicate, List<CQIE>>> ruleTree) {
        this.predicateTree = ruleTree.fmap(P2.<Predicate, List<CQIE>>__1());
        this.predicateDefinitions = HashMap.from(ruleTree);
    }


    /**
     * Converts the P3 rule tree into a P2 tree without the type proposal element.
     */
    private static Tree<P2<Predicate, List<CQIE>>> convertP32P2RuleTree(Tree<P3<Predicate, List<CQIE>, Option<Function>>> p3Tree) {
        return p3Tree.fmap(new F<P3<Predicate, List<CQIE>, Option<Function>>, P2<Predicate, List<CQIE>>>() {
            @Override
            public P2<Predicate, List<CQIE>> f(P3<Predicate, List<CQIE>, Option<Function>> label) {
                return P.p(label._1(), label._2());
            }
        });
    }

    public List<CQIE> getRules() {
        return List.join(predicateDefinitions.values());
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
     * Returns an immutable list
     */
    private List<CQIE> getDefinition(Predicate predicate) {
        Option<List<CQIE>> optionalRules = predicateDefinitions.get(predicate);
        if (optionalRules.isSome()) {
            return optionalRules.some();
        }
        return List.nil();
    }

    private static HashMap<Predicate,List<CQIE>> convertMultimapToMap(Multimap<Predicate, CQIE> ruleIndex) {
        Map<Predicate, Collection<CQIE>> map1 = ruleIndex.asMap();
        java.util.List<P2<Predicate, List<CQIE>>> pairList = new ArrayList<>();

        for (Map.Entry<Predicate, Collection<CQIE>> entry: map1.entrySet()) {
            pairList.add(P.p(entry.getKey(), List.iterableList(entry.getValue())));
        }
        return HashMap.from(pairList);
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
     * Recursive call.
     *
     * TODO: replace it to prevent stack overflow (for big Datalog programs).
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
