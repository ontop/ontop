package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.iq.visitor.RequiredExtensionalDataNodeExtractor;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class SelfJoinSameTermIQOptimizer implements IQTreeVariableGeneratorTransformer {

    private final CoreSingletons coreSingletons;
    private final RequiredExtensionalDataNodeExtractor requiredExtensionalDataNodeExtractor;
    private final IQTreeVariableGeneratorTransformer lookForDistinctTransformer;

    @Inject
    protected SelfJoinSameTermIQOptimizer(CoreSingletons coreSingletons,
                                          RequiredExtensionalDataNodeExtractor requiredExtensionalDataNodeExtractor) {
        this.coreSingletons = coreSingletons;
        this.requiredExtensionalDataNodeExtractor = requiredExtensionalDataNodeExtractor;
        this.lookForDistinctTransformer = IQTreeVariableGeneratorTransformer.of(new CaseInsensitiveIQTreeTransformerAdapter(coreSingletons.getIQFactory()) {
            private final IQVisitor<IQTree> transformer = new SameTermSelfJoinTransformer(IQTreeTransformer.of(this));

            @Override
            protected IQTree transformCardinalityInsensitiveTree(IQTree tree) {
                return tree.acceptVisitor(transformer);
            }
        });
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return lookForDistinctTransformer.transform(tree, variableGenerator);
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