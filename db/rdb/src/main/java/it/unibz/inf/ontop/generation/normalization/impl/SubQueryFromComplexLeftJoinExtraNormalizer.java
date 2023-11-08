package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.InnerJoinLikeNode;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.utils.VariableGenerator;

/**
 * When (left,inner) joins are having (left,inner) joins as children.
 * <p>
 * Adds a ConstructionNode in those children to force the creation of a sub-query
 * <p>
 * Useful for Dremio.
 */
@Singleton
public class SubQueryFromComplexLeftJoinExtraNormalizer extends DefaultRecursiveIQTreeVisitingTransformer
        implements DialectExtraNormalizer {

    @Inject
    protected SubQueryFromComplexLeftJoinExtraNormalizer(CoreSingletons coreSingletons) {
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

        if (transformedChild.getRootNode() instanceof InnerJoinLikeNode) {
            return iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(transformedChild.getVariables()),
                    transformedChild);
        }
        return transformedChild;
    }

}
