package it.unibz.inf.ontop.iq.node.normalization;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.Variable;

public interface NotRequiredVariableRemover {

    IQTree optimize(IQTree tree, ImmutableSet<Variable> requiredVariables);
}

