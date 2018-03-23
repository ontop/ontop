package it.unibz.inf.ontop.temporal.iq.node;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.model.term.NonGroundTerm;

public interface TemporalCoalesceNode extends TemporalOperatorNode, UnaryOperatorNode {
    public ImmutableSet<NonGroundTerm> getTerms();
}
