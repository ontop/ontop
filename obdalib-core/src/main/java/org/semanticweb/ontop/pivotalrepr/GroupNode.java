package org.semanticweb.ontop.pivotalrepr;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.NonGroundTerm;

/**
 * GROUP BY query node.
 */
public interface GroupNode extends QueryNode {

    ImmutableList<NonGroundTerm> getGroupingTerms();

    @Override
    GroupNode clone();

    @Override
    GroupNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException, NotNeededNodeException;
}
