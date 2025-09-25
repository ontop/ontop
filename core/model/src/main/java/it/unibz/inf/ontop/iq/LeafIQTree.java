package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.iq.node.ExplicitVariableProjectionNode;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;

public interface LeafIQTree extends IQTree, ExplicitVariableProjectionNode {

    @Override
    LeafIQTree getRootNode();

    LeafIQTree applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution);
}
