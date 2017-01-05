package it.unibz.inf.ontop.pivotalrepr.impl;

import it.unibz.inf.ontop.model.DataAtom;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.pivotalrepr.*;

public class IntensionalDataNodeImpl extends DataNodeImpl implements IntensionalDataNode {

    private static final String INTENSIONAL_DATA_NODE_STR = "INTENSIONAL";

    public IntensionalDataNodeImpl(DataAtom atom) {
        super(atom);
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public IntensionalDataNode clone() {
        return new IntensionalDataNodeImpl(getProjectionAtom());
    }

    @Override
    public IntensionalDataNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public SubstitutionResults<IntensionalDataNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode childNode, IntermediateQuery query) {
        return applySubstitution((IntensionalDataNode)this, substitution);
    }

    @Override
    public SubstitutionResults<IntensionalDataNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) {
        return applySubstitution((IntensionalDataNode)this, substitution);
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof IntensionalDataNode)
                && ((IntensionalDataNode) node).getProjectionAtom().equals(this.getProjectionAtom());
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public String toString() {
        return INTENSIONAL_DATA_NODE_STR + " " + getProjectionAtom();
    }

    @Override
    public IntensionalDataNode newAtom(DataAtom newAtom) {
        return new IntensionalDataNodeImpl(newAtom);
    }
}
