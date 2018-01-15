package it.unibz.inf.ontop.iq.node;

public interface OffsetNode extends QueryModifierNode {

    int getOffset();

    @Override
    OffsetNode clone();
}
