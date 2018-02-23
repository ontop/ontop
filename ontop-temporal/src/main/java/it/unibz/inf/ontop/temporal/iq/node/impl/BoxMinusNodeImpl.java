package it.unibz.inf.ontop.temporal.iq.node.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTransformer;
import it.unibz.inf.ontop.iq.transform.TemporalIQTransformer;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.temporal.iq.node.BoxMinusNode;
import it.unibz.inf.ontop.temporal.iq.node.TemporalQueryNodeVisitor;
import it.unibz.inf.ontop.temporal.model.TemporalRange;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public class BoxMinusNodeImpl extends TemporalOperatorWithRangeImpl implements BoxMinusNode{

    private static final String BOXMINUS_NODE_STR = "BOX MINUS" ;
    private final IntermediateQueryFactory iqFactory;

    @AssistedInject
    protected BoxMinusNodeImpl(@Assisted TemporalRange temporalRange, IntermediateQueryFactory iqFactory) {
        super(temporalRange);
        this.iqFactory = iqFactory;
    }

    @Override
    public String toString() {
        return BOXMINUS_NODE_STR + getRange();
    }


    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        ((TemporalQueryNodeVisitor)visitor).visit(this);
    }

    @Override
    public QueryNode clone() {
        try {
            return new BoxMinusNodeImpl(this.getRange().clone(), this.iqFactory);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public BoxMinusNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
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
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        return false;
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return isEquivalentTo(node);
    }


    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        try {
            throw new Exception("getLocallyRequiredVariables is not implemented in box minus");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {

        try {
            throw new Exception("getRequiredVariables is not implemented in box minus");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {

        try {
            throw new Exception("getLocallyDefinedVariables is not implemented in box minus");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        return (queryNode instanceof BoxMinusNode)
                && getRange().equals(((BoxMinusNode) queryNode).getRange());
    }

    @Override
    public IQTree liftBinding(IQTree childIQTree, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        IQTree newChild = childIQTree.liftBinding(variableGenerator);
        QueryNode newChildRoot = newChild.getRootNode();
        if(newChildRoot instanceof ConstructionNode ){
            IQTree boxLevelTree =  iqFactory.createUnaryIQTree(this, ((UnaryIQTree)newChild).getChild(), currentIQProperties.declareLifted());
            return iqFactory.createUnaryIQTree((ConstructionNode)newChildRoot, boxLevelTree);
        }else if(newChildRoot instanceof EmptyNode){
            return newChild;
        }
        return iqFactory.createUnaryIQTree(this, newChild, currentIQProperties.declareLifted());
    }

    @Override
    public IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution, Optional<ImmutableExpression> constraint, IQTree child) {
        IQTree newChild = child.applyDescendingSubstitution(descendingSubstitution, constraint);
        return iqFactory.createUnaryIQTree(this, newChild);
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution, IQTree child) {
        IQTree newChild = child.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution);
        return iqFactory.createUnaryIQTree(this, newChild);
    }

    @Override
    public ImmutableSet<Variable> getNullableVariables(IQTree child) {

        try {
            throw new Exception("getNullableVariables is not implemented in box minus");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isConstructed(Variable variable, IQTree child) {
        return false;
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child) {
        try {
            throw new Exception("liftIncompatibleDefinitions is not implemented in box minus");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child) {
        return iqFactory.createUnaryIQTree(this, child);
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTransformer transformer, IQTree child) {
        if (transformer instanceof TemporalIQTransformer){
            return ((TemporalIQTransformer) transformer).transformBoxMinus(tree, this, child);
        } else {
            return transformer.transformNonStandardUnaryNode(tree, this, child);
        }
    }

    @Override
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {
    }
}
