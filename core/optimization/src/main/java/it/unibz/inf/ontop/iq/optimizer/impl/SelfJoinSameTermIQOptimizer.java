package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.iq.visitor.RequiredExtensionalDataNodeExtractor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class SelfJoinSameTermIQOptimizer extends AbstractExtendedIQOptimizer {

    private final CoreSingletons coreSingletons;
    private final RequiredExtensionalDataNodeExtractor requiredExtensionalDataNodeExtractor;
    private final IQVisitor<IQTree> lookForDistinctTransformer;

    @Inject
    protected SelfJoinSameTermIQOptimizer(CoreSingletons coreSingletons,
                                          RequiredExtensionalDataNodeExtractor requiredExtensionalDataNodeExtractor) {
        super(coreSingletons.getIQFactory(), NORMALIZE_FOR_OPTIMIZATION);
        this.coreSingletons = coreSingletons;
        this.requiredExtensionalDataNodeExtractor = requiredExtensionalDataNodeExtractor;
        this.lookForDistinctTransformer = new CaseInsensitiveIQTreeTransformerAdapter(coreSingletons.getIQFactory()) {
            private final IQVisitor<IQTree> transformer = new SameTermSelfJoinTransformer(IQTreeTransformer.of(this));

            @Override
            protected IQTree transformCardinalityInsensitiveTree(IQTree tree) {
                return tree.acceptVisitor(transformer);
            }
        };
    }

    @Override
    protected IQTreeVariableGeneratorTransformer getTransformer() {
        return IQTreeVariableGeneratorTransformer.of(lookForDistinctTransformer);
    }

    /**
     * TODO: explain
     */
    private class SameTermSelfJoinTransformer extends AbstractBelowDistinctInnerJoinTransformer {

        SameTermSelfJoinTransformer(IQTreeTransformer lookForDistinctTransformer) {
            super(lookForDistinctTransformer, SelfJoinSameTermIQOptimizer.this.coreSingletons);
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