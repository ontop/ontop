package it.unibz.inf.ontop.iq;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.model.term.Variable;


public interface IQ {

    QueryNode getRootNode();

    ImmutableList<IQ> getChildren();

    ImmutableSet<Variable> getVariables();

    IQ liftBinding();

    default boolean isLeaf() {
        return getChildren().isEmpty();
    }
}
