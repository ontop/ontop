package org.semanticweb.ontop.pivotalrepr;

import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.impl.JgraphtIntermediateQueryBuilder;

public class UnionLiftProposalExecutorImpl implements UnionLiftProposalExecutor {

    private final QueryNodeTransformer queryNodeCloner = new QueryNodeCloner();

    public IntermediateQuery apply(UnionNode unionNode, QueryNode targetQueryNode, IntermediateQuery inputQuery) {

        IntermediateQueryBuilder builder = new JgraphtIntermediateQueryBuilder();

        ConstructionNode rootNode = inputQuery.getRootConstructionNode();
        try {
            ConstructionNode newRootNode = rootNode.acceptNodeTransformer(queryNodeCloner);
            builder.init(newRootNode);
            recursive(unionNode, targetQueryNode, builder, inputQuery, rootNode, newRootNode, Optional.<Integer>absent());
        } catch (IntermediateQueryBuilderException | NotNeededNodeException | QueryNodeTransformationException e) {
            e.printStackTrace();
        }

        try {
            return builder.build();
        } catch (IntermediateQueryBuilderException e) {
            throw new RuntimeException(e);
        }
    }


    public void recursive(UnionNode unionNode, QueryNode targetNode, IntermediateQueryBuilder builder, IntermediateQuery query, QueryNode parentNode,
                          QueryNode newParentNode, Optional<Integer> branchIndexInsideUnion)
            throws QueryNodeTransformationException, NotNeededNodeException, IntermediateQueryBuilderException {

        for (QueryNode subNode : query.getCurrentSubNodesOf(parentNode)) {

            Optional<BinaryAsymmetricOperatorNode.ArgumentPosition> optionalPosition = query.getOptionalPosition(parentNode, subNode);

            QueryNode newSubNode = subNode.acceptNodeTransformer(queryNodeCloner);

            if (subNode == targetNode) {
                UnionNode unionNodeClone = unionNode.acceptNodeTransformer(queryNodeCloner);

                builder.addChild(newParentNode, unionNodeClone, optionalPosition);

                int arityOfUnion = query.getCurrentSubNodesOf(unionNode).size();

                for (int i = 0; i < arityOfUnion; i++) {
                    if (i > 0) {
                        newSubNode = subNode.acceptNodeTransformer(queryNodeCloner);
                    }
                    builder.addChild(unionNodeClone, newSubNode, optionalPosition);
                    recursive(unionNode, targetNode, builder, query, subNode, newSubNode, Optional.of(i));
                }
            } else if (subNode == unionNode) {

                QueryNode subNodeOfUnion = query.getCurrentSubNodesOf(subNode).get(branchIndexInsideUnion.get());

                QueryNode subNodeOfUnionClone = subNodeOfUnion.acceptNodeTransformer(queryNodeCloner);

                builder.addChild(newParentNode, subNodeOfUnionClone, optionalPosition);

                recursive(unionNode, targetNode, builder, query, subNodeOfUnion, subNodeOfUnionClone, Optional.<Integer>absent());

            } else {

                builder.addChild(newParentNode, newSubNode, optionalPosition);

                recursive(unionNode, targetNode, builder, query, subNode, newSubNode, branchIndexInsideUnion);
            }
        }

    }

//    public String toString() {
//        return "UnionLiftTransformerImpl[" + unionNode + "," + targetNode + "]";
//    }

    @Override
    public IntermediateQuery apply(UnionLiftProposal proposal, IntermediateQuery inputQuery) {


        return apply(proposal.getUnionNode(), proposal.getTargetQueryNode(), inputQuery);
    }
}
