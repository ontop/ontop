package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.normalization.NotRequiredVariableRemover;
import it.unibz.inf.ontop.model.term.Variable;

public class NotRequiredVariableRemoverImpl implements NotRequiredVariableRemover {

    /**
     * TODO: implement it seriously
     */
    @Override
    public IQTree optimize(IQTree tree, ImmutableSet<Variable> requiredVariables) {
        return tree;
    }
}
