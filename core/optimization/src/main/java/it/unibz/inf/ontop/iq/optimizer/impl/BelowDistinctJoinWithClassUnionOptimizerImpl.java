package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.BelowDistinctJoinWithClassUnionOptimizer;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.visitor.RequiredExtensionalDataNodeExtractor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.inject.Inject;
import java.util.Optional;
import java.util.stream.Stream;

public class BelowDistinctJoinWithClassUnionOptimizerImpl implements BelowDistinctJoinWithClassUnionOptimizer {

    private final IQTreeTransformer lookForDistinctTransformer;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected BelowDistinctJoinWithClassUnionOptimizerImpl(CoreSingletons coreSingletons, IntermediateQueryFactory iqFactory,
                                                           RequiredExtensionalDataNodeExtractor requiredExtensionalDataNodeExtractor) {
        this.iqFactory = iqFactory;
        this.lookForDistinctTransformer = new LookForDistinctTransformerImpl(
                t -> new JoinWithClassUnionTransformer(t, coreSingletons, requiredExtensionalDataNodeExtractor),
                coreSingletons);
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree initialTree = query.getTree();
        IQTree newTree = lookForDistinctTransformer.transform(initialTree);
        return (newTree.equals(initialTree))
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree)
                .normalizeForOptimization();
    }

    protected static class JoinWithClassUnionTransformer extends AbstractBelowDistinctInnerJoinTransformer {
        private final RequiredExtensionalDataNodeExtractor requiredExtensionalDataNodeExtractor;

        protected JoinWithClassUnionTransformer(IQTreeTransformer lookForDistinctTransformer,
                                                CoreSingletons coreSingletons,
                                                RequiredExtensionalDataNodeExtractor requiredExtensionalDataNodeExtractor) {
            super(lookForDistinctTransformer, coreSingletons);
            this.requiredExtensionalDataNodeExtractor = requiredExtensionalDataNodeExtractor;
        }

        /**
         * Should not return any false positive
         *
         */
        protected boolean isDetectedAsRedundant(IQTree child, Stream<IQTree> otherChildrenStream) {
            ImmutableSet<IQTree> otherChildren = otherChildrenStream.collect(ImmutableCollectors.toSet());

            return Stream.of(child)
                    .filter(c -> c.getRootNode() instanceof UnionNode)
                    .flatMap(c -> c.getChildren().stream())
                    .flatMap(c -> Optional.of(c)
                            .flatMap(this::extractExtensionalNode)
                            .map(Stream::of)
                            .orElseGet(Stream::empty))
                    .anyMatch(c -> otherChildren.stream()
                            .flatMap(t -> t.acceptVisitor(requiredExtensionalDataNodeExtractor))
                            .anyMatch(o -> isDetectedAsRedundant(c, o)));
        }

        private Optional<ExtensionalDataNode> extractExtensionalNode(IQTree unionChild) {
            QueryNode rootNode = unionChild.getRootNode();

            /*
             * Filters just make much the variables are non-null can be eliminating,
             * because we are interested in cases where we join over these variables
             */
            if (rootNode instanceof FilterNode) {
                VariableNullability variableNullability = coreSingletons.getCoreUtilsFactory()
                        .createEmptyVariableNullability(unionChild.getVariables());

                ImmutableExpression filterCondition = ((FilterNode) rootNode).getFilterCondition();


                return filterCondition.evaluate2VL(variableNullability)
                        .getValue()
                        .filter(b -> b.equals(ImmutableExpression.Evaluation.BooleanValue.TRUE))
                        // Continue to the child
                        .flatMap(b -> extractExtensionalNode(unionChild.getChildren().get(0)));
            }
            else if (rootNode instanceof ExtensionalDataNode)
                return Optional.of((ExtensionalDataNode) rootNode);
            else
                return Optional.empty();
        }
    }
}
