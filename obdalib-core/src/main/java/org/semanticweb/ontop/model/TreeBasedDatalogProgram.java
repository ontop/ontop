package org.semanticweb.ontop.model;

import java.util.*;

import com.google.common.collect.Multimap;
import fj.*;
import fj.data.*;
import fj.data.List;
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
 */
public class TreeBasedDatalogProgram {

    /**
     * Predicate hierarchy in this query-oriented DatalogProgram.
     * To each predicate are associated the rules that define it.
     *
     * Final and immutable structure.
     */
    private final Tree<P2<Predicate, List<CQIE>>> ruleTree;

    /**
     * Very expensive construction method:
     *   - The rule lists have to be converted and indexed.
     *   - The predicate tree has to be built.
     *
     * @param rules
     */
    public static TreeBasedDatalogProgram fromRules(java.util.List<CQIE> rules) {
        Tree<P2<Predicate, List<CQIE>>> ruleTree = convertDependencyGraphToRuleTree(
                new DatalogDependencyGraphGenerator(rules));

        return new TreeBasedDatalogProgram(ruleTree);
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
        this.ruleTree = ruleTree;
    }


    /**
     * Persistent (immutable) list of rules.
     */
    public List<CQIE> getRules() {
        return List.join(List.iterableList(ruleTree.fmap(P2.<Predicate, List<CQIE>>__2())));
    }

    /**
     * Normal persistent (immutable) rule tree.
     */
    public Tree<P2<Predicate, List<CQIE>>> getRuleTree() {
        return ruleTree;
    }

    /**
     * Gets a persistent (immutable) P3 rule tree.
     *
     * This tree can be seen as a merge of the predicate tree
     * and the predicate definition map.
     *
     * Each node refers to a predicate, its definition rules
     * but also an optional Function. The latter is none.
     *
     * This structure is used for lifting types.
     */
    public Tree<P3<Predicate, List<CQIE>, Option<Function>>> getP3RuleTree() {
        Tree<P3<Predicate, List<CQIE>, Option<Function>>> ruleTree = this.ruleTree.fmap(
                new F<P2<Predicate, List<CQIE>>, P3<Predicate, List<CQIE>, Option<Function>>>() {
                    @Override
                    public P3<Predicate, List<CQIE>, Option<Function>> f(P2<Predicate, List<CQIE>> label) {
                        return P.p(label._1(), label._2(), Option.<Function>none());
                    }
                });
        return ruleTree;
    }

    @Override
    public String toString() {
        return ruleTree.draw(Show.showS(new F<P2<Predicate, List<CQIE>>, String>() {
            @Override
            public String f(P2<Predicate, List<CQIE>> label) {
                StringBuilder builder = new StringBuilder();
                for (CQIE rule: label._2()) {
                    builder.append(rule.toString() + "\n");
                }
                return builder.toString();
            }
        }));
    }

    private static Tree<P2<Predicate, List<CQIE>>> convertDependencyGraphToRuleTree(DatalogDependencyGraphGenerator
                                                                                            dependencyGraphGenerator) {
        DirectedGraph<Predicate, DefaultEdge> originalGraph = dependencyGraphGenerator.getPredicateDependencyGraph();
        Multimap<Predicate, CQIE> ruleIndex = dependencyGraphGenerator.getRuleIndex();
        TopologicalOrderIterator<Predicate, DefaultEdge> iter = new TopologicalOrderIterator<>(originalGraph);
        if (!iter.hasNext()) {
            throw new IllegalArgumentException("Empty dependency graph given");
        }
        Predicate rootPredicate = iter.next();
        return createSubTree(rootPredicate, originalGraph, ruleIndex);
    }

    /**
     * Recursive call.
     *
     * TODO: replace it to prevent stack overflow (for big Datalog programs).
     */
    private static Tree<P2<Predicate, List<CQIE>>> createSubTree(Predicate predicate, DirectedGraph<Predicate, DefaultEdge> originalGraph,
                                                                 Multimap<Predicate, CQIE> ruleIndex) {
        java.util.List<Tree<P2<Predicate, List<CQIE>>>> subTrees = new ArrayList<>();

        for (DefaultEdge edge: originalGraph.outgoingEdgesOf(predicate)) {
            // Do not try to call edge.getEdgeTarget(), it does not works... Nice API, isn't it?
            Predicate subPredicate = originalGraph.getEdgeTarget(edge);
            subTrees.add(createSubTree(subPredicate, originalGraph, ruleIndex));
        }

        P2<Predicate, List<CQIE>> label = P.p(predicate, List.iterableList(ruleIndex.get(predicate)));
        return Tree.node(label, List.iterableList(subTrees));
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
}
