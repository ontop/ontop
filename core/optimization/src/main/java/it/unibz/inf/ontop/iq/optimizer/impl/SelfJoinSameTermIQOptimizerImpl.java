package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.optimizer.SelfJoinSameTermIQOptimizer;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.iq.visitor.RequiredExtensionalDataNodeExtractor;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class SelfJoinSameTermIQOptimizerImpl extends AbstractIQOptimizer implements SelfJoinSameTermIQOptimizer {

    private final CoreSingletons coreSingletons;
    private final RequiredExtensionalDataNodeExtractor requiredExtensionalDataNodeExtractor;
    private final IQVisitor<IQTree> lookForDistinctTransformer;

    @Inject
    protected SelfJoinSameTermIQOptimizerImpl(CoreSingletons coreSingletons,
                                              RequiredExtensionalDataNodeExtractor requiredExtensionalDataNodeExtractor) {
        super(coreSingletons.getIQFactory(), NORMALIZE_FOR_OPTIMIZATION);
        this.coreSingletons = coreSingletons;
        this.requiredExtensionalDataNodeExtractor = requiredExtensionalDataNodeExtractor;
        this.lookForDistinctTransformer = new CaseInsensitiveIQTreeTransformerAdapter(coreSingletons.getIQFactory()) {
            private final IQVisitor<IQTree> transformer = new SameTermSelfJoinTransformer(transformerOf(this));

            @Override
            protected IQTree transformCardinalityInsensitiveTree(IQTree tree) {
                return tree.acceptVisitor(transformer);
            }
        };
    }

    @Override
    protected IQTree transformTree(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptVisitor(lookForDistinctTransformer);
    }

    /**
     * TODO: explain
     */
    protected class SameTermSelfJoinTransformer extends AbstractBelowDistinctInnerJoinTransformer {
        protected SameTermSelfJoinTransformer(IQTreeTransformer lookForDistinctTransformer) {
            super(lookForDistinctTransformer, SelfJoinSameTermIQOptimizerImpl.this.coreSingletons);
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
                            .flatMap(requiredExtensionalDataNodeExtractor::transform)
                            .anyMatch(d2 -> isDetectedAsRedundant(d1, d2)))
                    .isPresent();
        }
    }
}