package it.unibz.inf.ontop.datalog.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.OrderByNode;
import it.unibz.inf.ontop.iq.node.QueryModifierNode;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonGroundTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;

/**
 * Lifts the ORDER BY above the highest construction node, AS REQUIRED by our Datalog data structure
 *
 * If not possible, throws an OntopInternalBugException
 *
 * TEMPORARY CODE (quickly implemented)
 *
 */
public class OrderByLifter {

    private final IntermediateQueryFactory iqFactory;

    @Inject
    private OrderByLifter(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    public IQ liftOrderBy(IQ iq) {

        Stack<QueryModifierNode> ancestors = new Stack<>();

        // Non-final
        IQTree parentTree = iq.getTree();
        while ((parentTree.getRootNode() instanceof QueryModifierNode)) {
            ancestors.push((QueryModifierNode) parentTree.getRootNode());
            parentTree = ((UnaryIQTree)parentTree).getChild();
        }

        if (parentTree.getRootNode() instanceof ConstructionNode) {
            ConstructionNode constructionNode = (ConstructionNode) parentTree.getRootNode();

            IQTree childTree = ((UnaryIQTree)parentTree).getChild();
            if (childTree.getRootNode() instanceof OrderByNode) {
                OrderByNode formerOrderByNode = (OrderByNode) childTree.getRootNode();
                IQTree grandChildTree = ((UnaryIQTree) childTree).getChild();

                OrderByNode newOrderByNode = replaceTermsByProjectedVariables(formerOrderByNode,
                        constructionNode.getSubstitution());

                UnaryIQTree newConstructionTree = iqFactory.createUnaryIQTree(constructionNode, grandChildTree);
                UnaryIQTree orderByTree = iqFactory.createUnaryIQTree(newOrderByNode, newConstructionTree);

                // Non-final
                IQTree newTree = orderByTree;
                while (!ancestors.isEmpty()) {
                    newTree = iqFactory.createUnaryIQTree(ancestors.pop(), newTree);
                }
                return iqFactory.createIQ(iq.getProjectionAtom(), newTree);
            }
            else
                throw new IllegalArgumentException(
                        "An ORDER BY was expected to follow the highest construction node");
        }
        else
            throw new IllegalArgumentException(
                    "The first non query modifier was expecting to be a construction node");
    }

    private OrderByNode replaceTermsByProjectedVariables(OrderByNode formerOrderByNode,
                                                         ImmutableSubstitution<ImmutableTerm> substitution)
            throws OntopInternalBugException {
        ImmutableList<OrderByNode.OrderComparator> newComparators = formerOrderByNode.getComparators().stream()
                .map(c -> convertComparator(c, substitution))
                .collect(ImmutableCollectors.toList());

        return iqFactory.createOrderByNode(newComparators);
    }

    private OrderByNode.OrderComparator convertComparator(OrderByNode.OrderComparator comparator,
                                                          ImmutableSubstitution<ImmutableTerm> substitution) {
        NonGroundTerm term = comparator.getTerm();
        if (term instanceof Variable)
            return comparator;

        Optional<Variable> correspondingVariable = substitution.getImmutableMap().entrySet().stream()
                .filter(e -> e.getValue().equals(term))
                .map(Map.Entry::getKey)
                .findFirst();

        return correspondingVariable
                .map(v -> iqFactory.createOrderComparator(v, comparator.isAscending()))
                .orElseThrow(() -> new MinorOntopInternalBugException("The order condition " + term
                        + " is not projected by the construction node with " + substitution + " and therefore" +
                        "cannot be translated into the internal Datalog-like structure"));
    }
}
