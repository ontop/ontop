package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.optimizer.LeftJoinIQOptimizer;

import java.util.function.Supplier;

public class CardinalitySensitiveJoinTransferLJOptimizer implements LeftJoinIQOptimizer {

    @Override
    public IQ optimize(IQ query) {
        throw new RuntimeException("TODO: implement");
    }

    protected static class Transformer extends AbstractJoinTransferLJTransformer {

        protected Transformer(Supplier<VariableNullability> variableNullabilitySupplier,
                              OptimizationSingletons optimizationSingletons) {
            super(variableNullabilitySupplier, optimizationSingletons);
        }

        @Override
        protected IQTree transformBySearchingFromScratch(IQTree tree) {
            Transformer newTransformer = new Transformer(tree::getVariableNullability, optimizationSingletons);
            return tree.acceptTransformer(newTransformer);
        }
    }


}
