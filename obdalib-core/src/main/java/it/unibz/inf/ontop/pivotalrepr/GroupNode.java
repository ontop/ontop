package it.unibz.inf.ontop.pivotalrepr;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.NonGroundTerm;

/**
 * GROUP BY query node.
 */
public interface GroupNode extends QueryNode {

    ImmutableList<NonGroundTerm> getGroupingTerms();

    @Override
    GroupNode clone();

    @Override
    GroupNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;

    @Override
    SubstitutionResults<GroupNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode childNode, IntermediateQuery query) ;

    @Override
    SubstitutionResults<GroupNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) ;
}
