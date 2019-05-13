package it.unibz.inf.ontop.iq.node;

/**
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface DistinctNode extends QueryModifierNode {

    @Override
    DistinctNode clone();
}
