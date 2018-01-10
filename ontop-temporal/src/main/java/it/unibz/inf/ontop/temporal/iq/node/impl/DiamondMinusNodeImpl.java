package it.unibz.inf.ontop.temporal.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.QueryNodeSubstitutionException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.impl.DefaultSubstitutionResults;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.temporal.iq.node.DiamondMinusNode;
import it.unibz.inf.ontop.temporal.iq.node.TemporalQueryNodeVisitor;
import it.unibz.inf.ontop.temporal.model.TemporalRange;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public class DiamondMinusNodeImpl extends TemporalOperatorWithRangeImpl implements DiamondMinusNode{

    private static final String DIAMONDMINUS_NODE_STR = "DIAMOND MINUS" ;

    @AssistedInject
    protected DiamondMinusNodeImpl(@Assisted TemporalRange temporalRange) {
        super(temporalRange);
    }

    @Override
    public String toString() {
        return DIAMONDMINUS_NODE_STR + getRange();
    }


    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        ((TemporalQueryNodeVisitor)visitor).visit(this);
    }

    @Override
    public QueryNode clone() {
        return null;
    }

    @Override
    public DiamondMinusNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return this;
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return null;
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return ImmutableSet.of();
    }

    @Override
    public SubstitutionResults<? extends QueryNode> applyAscendingSubstitution(ImmutableSubstitution<? extends ImmutableTerm> substitution, QueryNode childNode, IntermediateQuery query) throws QueryNodeSubstitutionException {
        return DefaultSubstitutionResults.noChange();
    }

    @Override
    public SubstitutionResults<? extends QueryNode> applyDescendingSubstitution(ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) throws QueryNodeSubstitutionException {
        return DefaultSubstitutionResults.noChange();
    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        return false;
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return false;
    }

    @Override
    public NodeTransformationProposal reactToEmptyChild(IntermediateQuery query, EmptyNode emptyChild) {
        return null;
    }

    @Override
    public NodeTransformationProposal reactToTrueChildRemovalProposal(IntermediateQuery query, TrueNode trueNode) {
        return null;
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return null;
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        return null;
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return null;
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        return false;
    }

    @Override
    public IQTree liftBinding(IQTree childIQTree, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        return null;
    }

    @Override
    public IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution, Optional<ImmutableExpression> constraint, IQTree child) {
        return null;
    }

    @Override
    public ImmutableSet<Variable> getNullableVariables(IQTree child) {
        return null;
    }

    @Override
    public boolean isConstructed(Variable variable, IQTree child) {
        return false;
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child) {
        return null;
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child) {
        return null;
    }
}
