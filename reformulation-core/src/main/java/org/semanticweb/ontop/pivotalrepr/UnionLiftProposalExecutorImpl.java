package org.semanticweb.ontop.pivotalrepr;

import java.util.Optional;
import org.semanticweb.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import org.semanticweb.ontop.pivotalrepr.proposal.ProposalResults;
import org.semanticweb.ontop.pivotalrepr.proposal.impl.ProposalResultsImpl;

public class UnionLiftProposalExecutorImpl implements UnionLiftProposalExecutor {

    private final HomogeneousQueryNodeTransformer queryNodeCloner = new QueryNodeCloner();

    public IntermediateQuery apply(UnionNode unionNode, QueryNode targetQueryNode, IntermediateQuery inputQuery) {

        IntermediateQueryBuilder builder = new DefaultIntermediateQueryBuilder(inputQuery.getMetadata());

        ConstructionNode rootNode = inputQuery.getRootConstructionNode();
        try {
            ConstructionNode newRootNode = rootNode.acceptNodeTransformer(queryNodeCloner);
            builder.init(newRootNode);
            recursive(unionNode, targetQueryNode, builder, inputQuery, rootNode, newRootNode, Optional.<Integer>empty());
        } catch (NotNeededNodeException e) {
            throw new IllegalStateException("UnionLiftProposalExecutor should not remove any node");
        }
        return builder.build();
    }


    public void recursive(UnionNode unionNode, QueryNode targetNode, IntermediateQueryBuilder builder,
                          IntermediateQuery query, QueryNode parentNode,
                          QueryNode newParentNode, Optional<Integer> optionalBranchIndexInsideUnion)
            throws QueryNodeTransformationException, NotNeededNodeException, IntermediateQueryBuilderException {

        for (QueryNode subNode : query.getChildren(parentNode)) {

            Optional<NonCommutativeOperatorNode.ArgumentPosition> optionalPosition
                    = query.getOptionalPosition(parentNode, subNode);

            QueryNode newSubNode = subNode.acceptNodeTransformer(queryNodeCloner);

            if (subNode == targetNode) {
                UnionNode unionNodeClone = unionNode.acceptNodeTransformer(queryNodeCloner);

                builder.addChild(newParentNode, unionNodeClone, optionalPosition);

                int arityOfUnion = query.getChildren(unionNode).size();

                for (int i = 0; i < arityOfUnion; i++) {
                    if (i > 0) {
                        newSubNode = subNode.acceptNodeTransformer(queryNodeCloner);
                    }
                    builder.addChild(unionNodeClone, newSubNode, optionalPosition);
                    recursive(unionNode, targetNode, builder, query, subNode, newSubNode, Optional.of(i));
                }
            } else if (subNode == unionNode) {

                if(!optionalBranchIndexInsideUnion.isPresent()){
                    throw new IllegalStateException();
                }

                Integer index = optionalBranchIndexInsideUnion.get();
                QueryNode subNodeOfUnion = query.getChildren(subNode).get(index);

                QueryNode subNodeOfUnionClone = subNodeOfUnion.acceptNodeTransformer(queryNodeCloner);

                builder.addChild(newParentNode, subNodeOfUnionClone, optionalPosition);

                recursive(unionNode, targetNode, builder, query, subNodeOfUnion, subNodeOfUnionClone,
                        Optional.<Integer>empty());

            } else {

                builder.addChild(newParentNode, newSubNode, optionalPosition);

                recursive(unionNode, targetNode, builder, query, subNode, newSubNode, optionalBranchIndexInsideUnion);
            }
        }

    }

    @Override
    public ProposalResults apply(UnionLiftProposal proposal, IntermediateQuery inputQuery) {
        Optional<QueryNode> targetQueryNode = findTargetQueryNode(inputQuery, proposal.getUnionNode());

        if(!targetQueryNode.isPresent()){
            return new ProposalResultsImpl(inputQuery);
        }

        IntermediateQuery newQuery = apply(proposal.getUnionNode(), targetQueryNode.get(), inputQuery);
        return new ProposalResultsImpl(newQuery);
    }

    private Optional<QueryNode> findTargetQueryNode(IntermediateQuery inputQuery, UnionNode unionNode) {

        QueryNode current = unionNode;
        QueryNode target = current;

        boolean movingUp = true;
        do {
            Optional<QueryNode> optionalParent = inputQuery.getParent(current);

            if(!optionalParent.isPresent()){
                movingUp = false;
            } else {
                QueryNode parent = optionalParent.get();
                if(parent == inputQuery.getRootConstructionNode()){
                    movingUp = false;
                } else {
                    Optional<NonCommutativeOperatorNode.ArgumentPosition> optionalPosition
                            = inputQuery.getOptionalPosition(parent, current);
                    if(parent instanceof LeftJoinNode && optionalPosition.isPresent()
                            && optionalPosition.get().equals(NonCommutativeOperatorNode.ArgumentPosition.RIGHT)){
                        movingUp = false;
                    } else {
                        current = parent;

                        if(!(current instanceof ConstructionNode)){
                            target = current;
                        }


                    }
                }
            }
        } while (movingUp);

        if(target == unionNode){
            return  Optional.empty();
        } else {
            return Optional.of(target);
        }

    }
}
