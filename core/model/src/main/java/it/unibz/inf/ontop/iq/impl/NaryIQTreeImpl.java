package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.node.NaryOperatorNode;
import it.unibz.inf.ontop.iq.transform.IQTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;
import java.util.Optional;

public class NaryIQTreeImpl extends AbstractCompositeIQTree<NaryOperatorNode> implements NaryIQTree {

    // Lazy
    @Nullable
    private ImmutableSet<Variable> nullableVariables;

    @AssistedInject
    private NaryIQTreeImpl(@Assisted NaryOperatorNode rootNode, @Assisted ImmutableList<IQTree> children,
                           @Assisted IQProperties iqProperties, IQTreeTools iqTreeTools,
                           IntermediateQueryFactory iqFactory) {
        super(rootNode, children, iqProperties, iqTreeTools, iqFactory);
        if (children.size() < 2)
            throw new IllegalArgumentException("At least two children are required for a n-ary node");
    }

    @AssistedInject
    private NaryIQTreeImpl(@Assisted NaryOperatorNode rootNode, @Assisted ImmutableList<IQTree> children,
                           IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory) {
        this(rootNode, children, new IQPropertiesImpl(), iqTreeTools, iqFactory);
    }

    @Override
    public IQTree acceptTransformer(IQTransformer transformer) {
        return getRootNode().acceptTransformer(transformer, getChildren());
    }

    @Override
    public IQTree liftBinding(VariableGenerator variableGenerator) {
        return getProperties().isLifted()
                ? this
                : getRootNode().liftBinding(getChildren(), variableGenerator, getProperties());
    }

    /**
     * TODO: use the properties for optimization purposes?
     */
    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable) {
        return getRootNode().liftIncompatibleDefinitions(variable, getChildren());
    }

    @Override
    public IQTree applyDescendingSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint) {

        try {
            return normalizeDescendingSubstitution(descendingSubstitution)
                    .map(s -> getRootNode().applyDescendingSubstitution(s, constraint, getChildren()))
                    .orElseGet(() -> constraint
                            .map(this::propagateDownConstraint)
                            .orElse(this));

        } catch (IQTreeTools.UnsatisfiableDescendingSubstitutionException e) {
            return iqFactory.createEmptyNode(iqTreeTools.computeNewProjectedVariables(descendingSubstitution, getVariables()));
        }
    }

    /**
     * TODO: should we cache the boolean?
     */
    @Override
    public boolean isConstructed(Variable variable) {
        return getVariables().contains(variable) && getRootNode().isConstructed(variable, getChildren());
    }

    @Override
    public boolean isDeclaredAsEmpty() {
        return false;
    }

    @Override
    public ImmutableSet<Variable> getNullableVariables() {
        if (nullableVariables == null)
            nullableVariables = getRootNode().getNullableVariables(getChildren());
        return nullableVariables;
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint) {
        return getRootNode().propagateDownConstraint(constraint, getChildren());
    }

    @Override
    public boolean containsNullableVariable(Variable variable) {
        return getNullableVariables().contains(variable);
    }
}
