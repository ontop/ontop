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
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultDelegatingIQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;

/**
 * When (left,inner) joins are having (left,inner) joins as children.
 *
 * Adds a ConstructionNode in those children to force the creation of a sub-query
 *
 * Useful for Dremio.
 *
 */
@Singleton
public class SubQueryFromComplexJoinExtraNormalizer extends DefaultDelegatingIQTreeVariableGeneratorTransformer implements DialectExtraNormalizer {

    @Inject
    protected SubQueryFromComplexJoinExtraNormalizer(CoreSingletons coreSingletons) {
        super(new Transformer(coreSingletons.getIQFactory())::transform);
    }

    private static class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {
        Transformer(IntermediateQueryFactory iqFactory) {
            super(iqFactory);
        }

        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            return withTransformedChildren(tree, createSubQueryIfJoin(leftChild), createSubQueryIfJoin(rightChild));
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            return withTransformedChildren(tree, NaryIQTreeTools.transformChildren(children, this::createSubQueryIfJoin));
        }

        private IQTree createSubQueryIfJoin(IQTree child) {
            IQTree transformedChild = transform(child);

            if (transformedChild.getRootNode() instanceof LeftJoinNode
                    || transformedChild.getRootNode() instanceof InnerJoinNode) {
                return iqFactory.createUnaryIQTree(
                        iqFactory.createConstructionNode(transformedChild.getVariables()),
                        transformedChild);
            }
            return transformedChild;
        }
    }
}
