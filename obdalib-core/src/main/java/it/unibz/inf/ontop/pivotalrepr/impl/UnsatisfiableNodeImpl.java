package it.unibz.inf.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.*;

public class UnsatisfiableNodeImpl extends QueryNodeImpl implements UnsatisfiableNode {

    private static final String PREFIX = "UNSATISFIABLE";

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public UnsatisfiableNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException, NotNeededNodeException {
        return transformer.transform(this);
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return ImmutableSet.of();
    }

    @Override
    public SubstitutionResults<UnsatisfiableNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query) {
        return new SubstitutionResultsImpl<>(this);
    }

    @Override
    public SubstitutionResults<UnsatisfiableNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        return new SubstitutionResultsImpl<>(this);
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof UnsatisfiableNode);
    }

    @Override
    public UnsatisfiableNode clone() {
        return new UnsatisfiableNodeImpl();
    }

    @Override
    public String toString() {
        return PREFIX;
    }

}
