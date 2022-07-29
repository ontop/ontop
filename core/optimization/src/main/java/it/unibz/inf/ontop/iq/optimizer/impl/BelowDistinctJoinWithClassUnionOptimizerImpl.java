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
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.inject.Inject;
import java.util.Optional;
import java.util.stream.Stream;

public class BelowDistinctJoinWithClassUnionOptimizerImpl implements BelowDistinctJoinWithClassUnionOptimizer {

    private final IQTreeTransformer lookForDistinctTransformer;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected BelowDistinctJoinWithClassUnionOptimizerImpl(OptimizationSingletons optimizationSingletons, IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
        this.lookForDistinctTransformer = new LookForDistinctTransformerImpl(
                JoinWithClassUnionTransformer::new,
                optimizationSingletons);
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
                                              OptimizationSingletons optimizationSingletons) {
            super(lookForDistinctTransformer, optimizationSingletons.getCoreSingletons());
            requiredExtensionalDataNodeExtractor = optimizationSingletons.getRequiredExtensionalDataNodeExtractor();
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
                            .map(t -> (t.getRootNode() instanceof FilterNode) ? t.getChildren().get(0) : t)
                            .filter(t -> t instanceof ExtensionalDataNode)
                            .map(t -> (ExtensionalDataNode) t)
                            .map(Stream::of)
                            .orElseGet(Stream::empty))
                    .anyMatch(c -> otherChildren.stream()
                            .flatMap(t -> t.acceptVisitor(requiredExtensionalDataNodeExtractor))
                            .anyMatch(o -> isDetectedAsRedundant(c, o)));
        }

    }
}
