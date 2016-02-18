package unibz.inf.ontop.pivotalrepr;

import com.google.common.collect.ImmutableList;
import unibz.inf.ontop.model.ImmutableSubstitution;
import unibz.inf.ontop.model.NonGroundTerm;
import unibz.inf.ontop.model.VariableOrGroundTerm;

/**
 * GROUP BY query node.
 */
public interface GroupNode extends QueryNode {

    ImmutableList<NonGroundTerm> getGroupingTerms();

    @Override
    GroupNode clone();

    @Override
    GroupNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException, NotNeededNodeException;

    @Override
    SubstitutionResults<GroupNode> applyAscendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query) ;

    @Override
    SubstitutionResults<GroupNode> applyDescendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution) ;
}
