package it.unibz.inf.ontop.iq.node;

public interface LimitNode extends QueryModifierNode {

    long getLimit();

    @Override
    LimitNode clone();

}
