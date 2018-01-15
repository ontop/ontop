package it.unibz.inf.ontop.iq.node;

public interface LimitNode extends QueryModifierNode {

    int getLimit();

    @Override
    LimitNode clone();

}
