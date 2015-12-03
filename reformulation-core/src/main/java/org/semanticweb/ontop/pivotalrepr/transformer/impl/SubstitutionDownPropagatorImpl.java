package org.semanticweb.ontop.pivotalrepr.transformer.impl;

import org.semanticweb.ontop.model.DataAtom;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.LeftJoinNodeImpl;
import org.semanticweb.ontop.pivotalrepr.impl.SubQueryUnificationTools;
import org.semanticweb.ontop.pivotalrepr.transformer.NewSubstitutionException;
import org.semanticweb.ontop.pivotalrepr.transformer.SubstitutionDownPropagator;
import org.semanticweb.ontop.pivotalrepr.transformer.UnificationException;


public class SubstitutionDownPropagatorImpl extends SubstitutionPropagatorImpl<UnificationException,
        NewSubstitutionException> implements SubstitutionDownPropagator {

    public SubstitutionDownPropagatorImpl(ImmutableSubstitution<VariableOrGroundTerm> substitution) {
        super(substitution);
    }

    /**
     * TODO: implement
     */
    @Override
    public ConstructionNode transform(ConstructionNode constructionNode)
            throws NewSubstitutionException, UnificationException {
        DataAtom newProjectionAtom = transformDataAtom(constructionNode.getProjectionAtom());

        try {
            /**
             * TODO: explain why it makes sense (interface)
             */
            SubQueryUnificationTools.ConstructionNodeUnification constructionNodeUnification =
                    SubQueryUnificationTools.unifyConstructionNode(constructionNode, newProjectionAtom);

            ConstructionNode newConstructionNode = constructionNodeUnification.getUnifiedNode();
            ImmutableSubstitution<VariableOrGroundTerm> newSubstitutionToPropagate =
                    constructionNodeUnification.getSubstitutionPropagator().getSubstitution();

            /**
             * If the substitution has changed, throws the new substitution
             * and the new construction node so that the "client" can continue
             * with the new substitution (for the children nodes).
             */
            if (!getSubstitution().equals(newSubstitutionToPropagate)) {
                throw new NewSubstitutionException(newSubstitutionToPropagate,
                        newConstructionNode);
            }

            /**
             * Otherwise, continues with the current substitution
             */
            return newConstructionNode;

        } catch (SubQueryUnificationTools.SubQueryUnificationException e) {
            throw new UnificationException(e.getMessage());
        }
    }

    @Override
    public LeftJoinNode transform(LeftJoinNode leftJoinNode) {
        return new LeftJoinNodeImpl(
                transformOptionalBooleanExpression(leftJoinNode.getOptionalFilterCondition()));
    }
}
