package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.JoinLikeNode;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
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
public class SubQueryFromComplexJoinExtraNormalizer implements DialectExtraNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final Transformer transformer;

    @Inject
    protected SubQueryFromComplexJoinExtraNormalizer(CoreSingletons coreSingletons) {
        this.iqFactory = coreSingletons.getIQFactory();
        this.transformer = new Transformer();
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptVisitor(transformer);
    }

    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {

        Transformer() {
            super(SubQueryFromComplexJoinExtraNormalizer.this.iqFactory);
        }

        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            IQTree newLeftChild = createSubQueryIfJoin(leftChild);
            IQTree newRightChild = createSubQueryIfJoin(rightChild);

            return newLeftChild.equals(leftChild) && newRightChild.equals(rightChild) && rootNode.equals(tree.getRootNode())
                    ? tree
                    : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild);
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<IQTree> newChildren = NaryIQTreeTools.transformChildren(children, this::createSubQueryIfJoin);

            return newChildren.equals(children) && rootNode.equals(tree.getRootNode())
                    ? tree
                    : iqFactory.createNaryIQTree(rootNode, newChildren);
        }

        private IQTree createSubQueryIfJoin(IQTree child) {
            IQTree transformedChild = transformChild(child);

            if (transformedChild.getRootNode() instanceof JoinLikeNode) {
                return iqFactory.createUnaryIQTree(
                        iqFactory.createConstructionNode(transformedChild.getVariables()),
                        transformedChild);
            }
            return transformedChild;
        }

    }
}
