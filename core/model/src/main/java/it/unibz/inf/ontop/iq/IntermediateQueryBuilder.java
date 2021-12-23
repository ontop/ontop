package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.IntermediateQueryBuilderException;
import it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;

/**
 * TODO: describe
 *
 * Can create only one intermediateQuery (to be used once).
 *
 * See IntermediateQueryFactory for creating a new instance.
 *
 */
public interface IntermediateQueryBuilder {

    void init(DistinctVariableOnlyDataAtom projectionAtom, QueryNode rootNode)
            throws IntermediateQueryBuilderException;

    /**
     * When the parent is NOT a BinaryAsymetricOperatorNode
     */
    void addChild(QueryNode parentNode, QueryNode child) throws IntermediateQueryBuilderException;

    /**
     * When the parent is a BinaryAsymetricOperatorNode.
     */
    void addChild(QueryNode parentNode, QueryNode child, BinaryOrderedOperatorNode.ArgumentPosition position)
            throws IntermediateQueryBuilderException;

    IntermediateQuery build() throws IntermediateQueryBuilderException;

    IQ buildIQ() throws IntermediateQueryBuilderException;

    boolean contains(QueryNode node);
}
