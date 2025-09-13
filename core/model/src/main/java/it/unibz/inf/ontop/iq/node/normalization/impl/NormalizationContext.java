package it.unibz.inf.ontop.iq.node.normalization.impl;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.utils.VariableGenerator;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryOperatorSequence;

public class NormalizationContext {
    protected final VariableGenerator variableGenerator;

    public NormalizationContext(VariableGenerator variableGenerator) {
        this.variableGenerator = variableGenerator;
    }

    protected static abstract class NormalizationState<T extends UnaryOperatorNode> {
        private final UnaryOperatorSequence<T> ancestors;

        protected NormalizationState(UnaryOperatorSequence<T> ancestors) {
            this.ancestors = ancestors;
        }

        protected UnaryOperatorSequence<T> getAncestors() {
            return ancestors;
        }

        protected abstract IQTree asIQTree();
    }
}
