package it.unibz.inf.ontop.iq.node;

public interface OffsetNode extends QueryModifierNode {

    long getOffset();

    @Override
    OffsetNode clone();
}
