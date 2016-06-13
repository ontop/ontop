package it.unibz.inf.ontop.pivotalrepr;

import java.util.Optional;
import it.unibz.inf.ontop.model.ImmutableBooleanExpression;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.VariableOrGroundTerm;

/**
 * TODO: explain
 */
public interface JoinOrFilterNode extends QueryNode {

    public Optional<ImmutableBooleanExpression> getOptionalFilterCondition();

    @Override
    SubstitutionResults<? extends JoinOrFilterNode> applyAscendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query) throws QueryNodeSubstitutionException;

    @Override
    SubstitutionResults<? extends JoinOrFilterNode> applyDescendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution) throws QueryNodeSubstitutionException;

}
