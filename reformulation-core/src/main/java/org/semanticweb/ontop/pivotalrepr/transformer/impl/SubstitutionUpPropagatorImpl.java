package org.semanticweb.ontop.pivotalrepr.transformer.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionTools;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.ConstructionNodeImpl;
import org.semanticweb.ontop.pivotalrepr.impl.LeftJoinNodeImpl;
import org.semanticweb.ontop.pivotalrepr.transformer.StopPropagationException;
import org.semanticweb.ontop.pivotalrepr.transformer.SubstitutionUpPropagator;

import java.util.Map;

public class SubstitutionUpPropagatorImpl extends SubstitutionPropagatorImpl<StopPropagationException,
        QueryNodeTransformationException> implements SubstitutionUpPropagator {

    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();


    private final QueryNode descendantNode;
    private final IntermediateQuery query;

    public SubstitutionUpPropagatorImpl(IntermediateQuery query, QueryNode descendantNode, ImmutableSubstitution substitution) {
        super(substitution);
        this.query = query;
        this.descendantNode = descendantNode;

        if (substitution.isEmpty()) {
            throw new IllegalArgumentException("The substitution must not be empty!");
        }
    }

    /**
     * TODO: explain
     */
    @Override
    public LeftJoinNode transform(LeftJoinNode leftJoinNode) throws StopPropagationException {
        if (isFromRightBranch(descendantNode, leftJoinNode, query)) {
            throw new StopPropagationException(integrateSubstitutionAsLeftJoinCondition(leftJoinNode));
        }
        else {
            return new LeftJoinNodeImpl(
                    transformOptionalBooleanExpression(leftJoinNode.getOptionalFilterCondition()));
        }
    }

    /**
     * TODO: explain
     */
    private LeftJoinNode integrateSubstitutionAsLeftJoinCondition(LeftJoinNode leftJoinNode) {
        // Ok because the substitution is guaranteed not being empty.
        ImmutableBooleanExpression newEqualities = getSubstitution().convertIntoBooleanExpression().get();

        Optional<ImmutableBooleanExpression> optionalFormerCondition = leftJoinNode.getOptionalFilterCondition();
        ImmutableBooleanExpression newFilterCondition;
        if (optionalFormerCondition.isPresent()) {
            newFilterCondition = DATA_FACTORY.getImmutableBooleanExpression(OBDAVocabulary.AND,
                    optionalFormerCondition.get(), newEqualities);
        }
        else {
            newFilterCondition = newEqualities;
        }
        return new LeftJoinNodeImpl(Optional.of(newFilterCondition));
    }

    /**
     * TODO: explain
     */
    @Override
    public ConstructionNode transform(ConstructionNode constructionNode) throws StopPropagationException {
        ImmutableMap<Variable, ImmutableTerm> formerNodeSubstitutionMap = constructionNode.getSubstitution()
                .getImmutableMap();
        ImmutableSet<Variable> boundVariables = formerNodeSubstitutionMap.keySet();

        ImmutableMap.Builder<Variable, ImmutableTerm> newSubstitutionMapBuilder = ImmutableMap.builder();
        newSubstitutionMapBuilder.putAll(formerNodeSubstitutionMap);

        ImmutableSet<Variable> projectedVariables = constructionNode.getProjectionAtom().getVariables();

        for (Map.Entry<Variable, ? extends VariableOrGroundTerm> entry : getSubstitution().getImmutableMap().entrySet()) {
            Variable replacedVariable = entry.getKey();
            if (projectedVariables.contains(replacedVariable)) {
                if (boundVariables.contains(replacedVariable)) {
                    throw new RuntimeException(
                            "Inconsistent query: an already bound has been also found in the sub-tree: "
                                    + replacedVariable);
                }
                else {
                    newSubstitutionMapBuilder.put(replacedVariable, entry.getValue());
                }
            }
        }

        ImmutableSubstitution<ImmutableTerm> newSubstitution = new ImmutableSubstitutionImpl<>(
                newSubstitutionMapBuilder.build());

        ConstructionNode newConstructionNode = new ConstructionNodeImpl(constructionNode.getProjectionAtom(),
                newSubstitution, constructionNode.getOptionalModifiers());

        throw new StopPropagationException(newConstructionNode);
    }

    /**
     * TODO: explain
     */
    private boolean isFromRightBranch(QueryNode descendantNode, NonCommutativeOperatorNode ancestorNode, IntermediateQuery query) {

        Optional<QueryNode> optionalCurrentNode = Optional.of(descendantNode);

        while (optionalCurrentNode.isPresent()) {
            QueryNode currentNode = optionalCurrentNode.get();
            Optional<QueryNode> optionalAncestor = query.getParent(currentNode);

            if (optionalAncestor.isPresent() && (optionalAncestor.get() == ancestorNode)) {
                Optional<NonCommutativeOperatorNode.ArgumentPosition> optionalPosition = query.getOptionalPosition(ancestorNode, currentNode);
                if (optionalPosition.isPresent()) {
                    switch(optionalPosition.get()) {
                        case LEFT:
                            return false;
                        case RIGHT:
                            return true;
                        default:
                            throw new RuntimeException("Unexpected position: " + optionalPosition.get());
                    }
                }
                else {
                    throw new RuntimeException("Inconsistent tree: no argument position after " + ancestorNode);
                }
            }
            else {
                optionalCurrentNode = optionalAncestor;
            }
        }
        throw new IllegalArgumentException(descendantNode.toString() +  " is not a descendant of " + ancestorNode);
    }
}
