package it.unibz.inf.ontop.pivotalrepr.impl;

import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.pivotalrepr.*;

/**
 * "Default" implementation for an extensional data node.
 *
 * Most likely (but not necessarily) will be overwritten by native query language specific implementations.
 */
public class ExtensionalDataNodeImpl extends DataNodeImpl implements ExtensionalDataNode {

    private static final String EXTENSIONAL_NODE_STR = "EXTENSIONAL";

    public ExtensionalDataNodeImpl(DataAtom atom) {
        super(atom);
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ExtensionalDataNode clone() {
        return new ExtensionalDataNodeImpl(getProjectionAtom());
    }

    @Override
    public ExtensionalDataNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public SubstitutionResults<ExtensionalDataNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode childNode, IntermediateQuery query) {
        return applySubstitution((ExtensionalDataNode) this, substitution);
    }

    @Override
    public SubstitutionResults<ExtensionalDataNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) {
        return applySubstitution((ExtensionalDataNode)this, substitution);
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof ExtensionalDataNode)
                && ((ExtensionalDataNode) node).getProjectionAtom().equals(this.getProjectionAtom());
    }

    @Override
    public String toString() {
        return EXTENSIONAL_NODE_STR + " " + getProjectionAtom();
    }

    @Override
    public ExtensionalDataNode newAtom(DataAtom newAtom) {
        return new ExtensionalDataNodeImpl(newAtom);
    }
}
