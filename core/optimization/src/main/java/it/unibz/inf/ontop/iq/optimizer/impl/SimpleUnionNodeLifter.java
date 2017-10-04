package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.optimizer.UnionNodeLifter;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;

/**
 * Choose the ancestor for the UnionNode lift.
 * Stop at the first interesting match ( a JoinLikeNode ) having in its subtree one of the conflicting variable of the UnionNode
 * if not present choose a FilterNode instead, if available.
 *
 */

public class SimpleUnionNodeLifter implements UnionNodeLifter {


    @Override
    public Optional<QueryNode> chooseLiftLevel(IntermediateQuery currentQuery, UnionNode unionNode, ImmutableSet<Variable> unionVariables) {

        // Non-final
        Optional<QueryNode> optionalAncestor = currentQuery.getParent(unionNode);
        QueryNode ancestorChild = unionNode;

        Set<Variable> surroundingVariables = new HashSet<>();

        Optional<QueryNode> filterJoin = Optional.empty();

        while (optionalAncestor.isPresent()) {

            QueryNode ancestorNode = optionalAncestor.get();
            if(ancestorNode instanceof JoinOrFilterNode) {

                if(ancestorNode instanceof LeftJoinNode){

                    BinaryOrderedOperatorNode.ArgumentPosition position = currentQuery.getOptionalPosition(ancestorNode, ancestorChild)
                            .orElseThrow(() -> new IllegalStateException("Missing position of leftJoin child"));

                    //cannot lift coming from the right part of the left join
                    if (position.equals(RIGHT)){
                        return filterJoin;
                    }
                }

                for (Variable variable : unionVariables) {

                    ImmutableList<QueryNode> childrenAncestorNode = currentQuery.getChildren(ancestorNode);

                    //get all projected variables from the children of the parent node
                    childrenAncestorNode.stream()
                            .filter(child -> !child.equals(unionNode))
                            .forEach(child ->  surroundingVariables.addAll(currentQuery.getVariables(child)));

                    // get variable from the optional filter condition
                    surroundingVariables.addAll(ancestorNode.getLocalVariables());

                    if (surroundingVariables.contains(variable)) {

                        //if we found a filter node, we keep it as a possible point to lift the union,
                        //but continue to another ancestor searching for a better match
                        if(ancestorNode instanceof FilterNode) {
                            filterJoin = Optional.of(ancestorNode);
                        }
                        else {
                            return Optional.of(ancestorNode);
                        }

                    }
                }
            }
            else if (ancestorNode instanceof UnionNode){
                //cannot lift over a union
                return filterJoin;

            }
            //search in another ancestor
            optionalAncestor = currentQuery.getParent(ancestorNode);
            ancestorChild = ancestorNode;
        }

        //no innerJoin or leftJoin parent with the given variable, use highest filterJoin instead if present
        return filterJoin;

    }

}
