package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.JoinLikeNode;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

/**
 * When (left,inner) joins are having (left,inner) joins as children.
 *
 * Adds a ConstructionNode in those children to force the creation of a sub-query
 *
 * Useful for Dremio.
 *
 */
@Singleton
public class SubQueryFromComplexJoinExtraNormalizer extends DefaultRecursiveIQTreeVisitingTransformer
        implements DialectExtraNormalizer {

    @Inject
    protected SubQueryFromComplexJoinExtraNormalizer(CoreSingletons coreSingletons) {
        super(coreSingletons);
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return transform(tree);
    }

    @Override
    public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        IQTree newLeftChild = createSubQueryIfJoin(leftChild);
        IQTree newRightChild = createSubQueryIfJoin(rightChild);

        return newLeftChild.equals(leftChild) && newRightChild.equals(rightChild) && rootNode.equals(tree.getRootNode())
                ? tree
                : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild);
    }

    private IQTree createSubQueryIfJoin(IQTree child) {
        IQTree transformedChild = child.acceptTransformer(this);

        if (transformedChild.getRootNode() instanceof JoinLikeNode) {
            return iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(transformedChild.getVariables()),
                    transformedChild);
        }
        return transformedChild;
    }

    @Override
    public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        ImmutableList<IQTree> newChildren = children.stream()
                .map(this::createSubQueryIfJoin)
                .collect(ImmutableCollectors.toList());

        return newChildren.equals(children) && rootNode.equals(tree.getRootNode())
                ? tree
                : iqFactory.createNaryIQTree(rootNode, newChildren);
    }
}
