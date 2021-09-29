package it.unibz.inf.ontop.iq.tools;

import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.QueryNode;

public interface IQConverter {

    IQ convert(IntermediateQuery query);

    IQTree convertTree(IntermediateQuery query, QueryNode rootNode);

    IntermediateQuery convert(IQ query) throws EmptyQueryException;
}
