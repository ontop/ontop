package it.unibz.inf.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.*;

import static it.unibz.inf.ontop.pivotalrepr.SubstitutionResults.LocalAction.DECLARE_AS_EMPTY;
import static it.unibz.inf.ontop.pivotalrepr.SubstitutionResults.LocalAction.NO_CHANGE;

/**
 * Created by jcorman on 05/10/16.
 */
public class TrueNodeImpl implements TrueNode {


    private static final String PREFIX = "TRUE";

    public TrueNodeImpl() {
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return ImmutableSet.of();
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public TrueNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException, NotNeededNodeException {
        return transformer.transform(this);
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public SubstitutionResults<TrueNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode childNode, IntermediateQuery query) {
        return new SubstitutionResultsImpl<>(NO_CHANGE);
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        if (node instanceof TrueNode) {
            return true;
        }
        return false;
    }

    @Override
    public SubstitutionResults<TrueNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) {
        return new SubstitutionResultsImpl<>(clone());
    }

    @Override
    public TrueNodeImpl clone() {
        return new TrueNodeImpl();
    }

    @Override
    public String toString() {
        return PREFIX;
    }

    @Override
    public NodeTransformationProposal reactToEmptyChild(IntermediateQuery query, EmptyNode emptyChild) {
        throw new UnsupportedOperationException("A TrueNode is not expected to have a child");
    }

    @Override
    public NodeTransformationProposal reactToTrueChildRemovalProposal(IntermediateQuery query, TrueNode trueNode) {
        return new NodeTransformationProposalImpl(NodeTransformationProposedState.DECLARE_AS_TRUE, ImmutableSet.of());
    }
}
