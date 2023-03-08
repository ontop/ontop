package it.unibz.inf.ontop.generation.normalization.impl;

import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeExtendedTransformer;
import it.unibz.inf.ontop.model.term.NonGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
Used to get rid of limits in sub-queries that are not necessary, for dialects like Denodo, that don't allow limits in sub-queries.
 */
public class EliminateLimitsFromSubQueriesNormalizer extends DefaultRecursiveIQTreeExtendedTransformer<VariableGenerator> implements DialectExtraNormalizer {


    @Inject
    protected EliminateLimitsFromSubQueriesNormalizer(CoreSingletons coreSingletons) {
        super(coreSingletons);
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptTransformer(this, variableGenerator);
    }

    @Override
    public IQTree transformSlice(IQTree tree, SliceNode sliceNode, IQTree child, VariableGenerator context) {
        //We only perform this normalization if there is no OFFSET
        if(sliceNode.getOffset() != 0 || sliceNode.getLimit().isEmpty())
            return super.transformSlice(tree, sliceNode, child, context);

        return iqFactory.createUnaryIQTree(sliceNode, normalizeRecursive(child, sliceNode.getLimit().get(), context));
    }

    protected IQTree normalizeRecursive(IQTree tree, long limit, VariableGenerator variableGenerator) {
        if(tree.getRootNode() instanceof SliceNode) {
            var subSlice = (SliceNode)tree.getRootNode();

            //If the child slice has a lower limit than the parent, we cannot drop it
            //We once again only perform this normalization if there is no OFFSET
            if(subSlice.getOffset() != 0 || subSlice.getLimit().isEmpty() || subSlice.getLimit().get() < limit)
                return transform(tree, variableGenerator);

            return normalizeRecursive(tree.getChildren().get(0), limit, variableGenerator);

        }

        //On DISTINCT we have to stop
        if(tree.getRootNode() instanceof DistinctNode)
            return transform(tree, variableGenerator);
        //On LEFT JOIN, we only continue on the left
        if(tree.getRootNode() instanceof LeftJoinNode) {
            var leftSubTree = normalizeRecursive(tree.getChildren().get(0), limit, variableGenerator);
            var rightSubTree = transform(tree.getChildren().get(1), variableGenerator);
            if(leftSubTree.equals(tree.getChildren().get(0)) && rightSubTree.equals(tree.getChildren().get(1)))
                return tree;
            return iqFactory.createBinaryNonCommutativeIQTree((LeftJoinNode)tree.getRootNode(), leftSubTree, rightSubTree);
        }
        //On anything else, we continue on each child
        if(tree.getRootNode() instanceof UnaryOperatorNode) {
            var newSubTree = normalizeRecursive(tree.getChildren().get(0), limit, variableGenerator);
            if(newSubTree.equals(tree.getChildren().get(0)))
                return tree;
            return iqFactory.createUnaryIQTree((UnaryOperatorNode) tree.getRootNode(), newSubTree);
        }
        if(tree.getRootNode() instanceof BinaryNonCommutativeOperatorNode) {
            var leftSubTree = normalizeRecursive(tree.getChildren().get(0), limit, variableGenerator);
            var rightSubTree = normalizeRecursive(tree.getChildren().get(1), limit, variableGenerator);
            if(leftSubTree.equals(tree.getChildren().get(0)) && rightSubTree.equals(tree.getChildren().get(1)))
                return tree;
            return iqFactory.createBinaryNonCommutativeIQTree((BinaryNonCommutativeOperatorNode) tree.getRootNode(),
                    leftSubTree,
                    rightSubTree);
        }
        if(tree.getRootNode() instanceof NaryOperatorNode) {
            var newChildTrees = tree.getChildren().stream().map(
                    child -> normalizeRecursive(child, limit, variableGenerator)
            ).collect(ImmutableCollectors.toList());
            if(IntStream.range(0, newChildTrees.size()).allMatch(i -> newChildTrees.get(i).equals(tree.getChildren().get(i))))
                return tree;
            return iqFactory.createNaryIQTree((NaryOperatorNode) tree.getRootNode(), newChildTrees);
        }

        //On any remaining edge case
        return transform(tree, variableGenerator);
    }
}
