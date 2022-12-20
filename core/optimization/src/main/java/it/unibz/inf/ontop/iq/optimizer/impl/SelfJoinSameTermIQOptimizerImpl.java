package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.optimizer.SelfJoinSameTermIQOptimizer;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.visitor.RequiredExtensionalDataNodeExtractor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class SelfJoinSameTermIQOptimizerImpl implements SelfJoinSameTermIQOptimizer {

    private final IQTreeTransformer lookForDistinctTransformer;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected SelfJoinSameTermIQOptimizerImpl(CoreSingletons coreSingletons, IntermediateQueryFactory iqFactory,
                                              RequiredExtensionalDataNodeExtractor requiredExtensionalDataNodeExtractor) {
        this.iqFactory = iqFactory;
        this.lookForDistinctTransformer = new LookForDistinctOrLimit1TransformerImpl(
                t -> new SameTermSelfJoinTransformer(t, coreSingletons, requiredExtensionalDataNodeExtractor),
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

    /**
     * TODO: explain
     */
    protected static class SameTermSelfJoinTransformer extends AbstractBelowDistinctInnerJoinTransformer {
        private final RequiredExtensionalDataNodeExtractor requiredExtensionalDataNodeExtractor;

        protected SameTermSelfJoinTransformer(IQTreeTransformer lookForDistinctTransformer,
                                              CoreSingletons coreSingletons,
                                              RequiredExtensionalDataNodeExtractor requiredExtensionalDataNodeExtractor) {
            super(lookForDistinctTransformer, coreSingletons);
            this.requiredExtensionalDataNodeExtractor = requiredExtensionalDataNodeExtractor;
        }

        /**
         * Should not return any false positive
         */
        @Override
        protected boolean isDetectedAsRedundant(IQTree child, Stream<IQTree> otherChildren) {
            return Optional.of(child)
                    .filter(c -> c instanceof ExtensionalDataNode)
                    .map(c -> (ExtensionalDataNode) c)
                    .filter(d1 -> otherChildren
                            .flatMap(t -> t.acceptVisitor(requiredExtensionalDataNodeExtractor))
                            .anyMatch(d2 -> isDetectedAsRedundant(d1, d2)))
                    .isPresent();
        }
    }
}