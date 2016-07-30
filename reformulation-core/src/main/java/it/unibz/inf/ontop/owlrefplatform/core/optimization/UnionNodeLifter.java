package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.UnionNode;

import java.util.Optional;

/**
 *  Choose the query node where to lift the UnionNode
 *
 *  TODO: explain
 */

public interface UnionNodeLifter {

    public Optional<QueryNode> chooseLevelLift(IntermediateQuery currentQuery, UnionNode unionNode, ImmutableSet<Variable> unionVariables);


}
