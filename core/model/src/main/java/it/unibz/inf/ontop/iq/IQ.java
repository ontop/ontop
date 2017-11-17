package it.unibz.inf.ontop.iq;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.QueryNode;


public interface IQ {

    QueryNode getRootNode();

    ImmutableList<IQ> getSubTrees();

    default boolean isLeaf() {
        return getSubTrees().isEmpty();
    }
}
