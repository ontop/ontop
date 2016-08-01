package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.RIGHT;

/**
 * Choose the QueryNode for the UnionNode lift.
 * Stop at the first interesting match
 */
public class SimpleUnionNodeLifter implements UnionNodeLifter {


    @Override
    public Optional<QueryNode> chooseLevelLift(IntermediateQuery currentQuery, UnionNode unionNode, ImmutableSet<Variable> unionVariables) {

        // Non-final
        Optional<QueryNode> optionalParent = currentQuery.getParent(unionNode);
        Set<Variable> projectedVariables = new HashSet<>();
        FilterNode filterJoin = null;
        

        while (optionalParent.isPresent()) {

            QueryNode parentNode = optionalParent.get();
            if(parentNode instanceof JoinOrFilterNode) {

                if(parentNode instanceof LeftJoinNode){
                    LeftJoinNode leftJoin = (LeftJoinNode) parentNode;
                    Optional<NonCommutativeOperatorNode.ArgumentPosition> optionalPosition = currentQuery.getOptionalPosition(leftJoin, unionNode);
                    NonCommutativeOperatorNode.ArgumentPosition position = optionalPosition.orElseThrow(() -> new IllegalStateException("Missing position of leftJoin child"));

                    //cannot lift over a left join from the right part
                    if (position.equals(RIGHT)){
                        return Optional.empty();
                    }
                }

                for (Variable variable : unionVariables) {

                    ImmutableList<QueryNode> childrenParentNode = currentQuery.getChildren(parentNode);

                    //get all projected variables from the children of the parent node
                    childrenParentNode.stream()
                            .filter(child -> !child.equals(unionNode))
                            .forEach(child -> {
                        projectedVariables.addAll(currentQuery.getVariables(child));
                    });

                    if (projectedVariables.contains(variable)) {

                        if(parentNode instanceof FilterNode) {
                            filterJoin = (FilterNode) parentNode;
                        }
                        else {

                            return Optional.of(parentNode);
                        }

                    }
                }
            }
            else if (parentNode instanceof UnionNode){
                //cannot lift over a union
                return Optional.empty();

            }

            //search in another ancestor
            optionalParent = currentQuery.getParent(parentNode);
        }

        //no innerJoin or leftJoin parent with the given variable, use highest filterJoin instead
        if(filterJoin!=null){
            return Optional.of(filterJoin);
        }

        //I don't lift
        return Optional.empty();
    }

}
