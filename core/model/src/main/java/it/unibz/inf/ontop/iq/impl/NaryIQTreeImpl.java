package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.node.NaryOperatorNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;
import java.util.Optional;

public class NaryIQTreeImpl extends AbstractCompositeIQTree<NaryOperatorNode> implements NaryIQTree {

    // Lazy
    @Nullable
    private VariableNullability variableNullability;
    @Nullable
    private ImmutableSet<ImmutableSubstitution<NonVariableTerm>> variableDefinition;
    @Nullable
    private ImmutableSet<ImmutableSet<Variable>> uniqueConstraints;
    @Nullable
    private Boolean isDistinct;

    @AssistedInject
    private NaryIQTreeImpl(@Assisted NaryOperatorNode rootNode, @Assisted ImmutableList<IQTree> children,
                           @Assisted IQProperties iqProperties, IQTreeTools iqTreeTools,
                           IntermediateQueryFactory iqFactory, OntopModelSettings settings) {
        super(rootNode, children, iqProperties, iqTreeTools, iqFactory);
        if (children.size() < 2)
            throw new IllegalArgumentException("At least two children are required for a n-ary node");
        variableNullability = null;
        variableDefinition = null;
        isDistinct = null;

        if (settings.isTestModeEnabled())
            validate();
    }

    @AssistedInject
    private NaryIQTreeImpl(@Assisted NaryOperatorNode rootNode, @Assisted ImmutableList<IQTree> children,
                           @Assisted VariableNullability variableNullability,
                           @Assisted IQProperties iqProperties, IQTreeTools iqTreeTools,
                           IntermediateQueryFactory iqFactory, OntopModelSettings settings) {
        super(rootNode, children, iqProperties, iqTreeTools, iqFactory);
        if (children.size() < 2)
            throw new IllegalArgumentException("At least two children are required for a n-ary node");
        this.variableNullability = variableNullability;
        variableDefinition = null;
        isDistinct = null;

        if (settings.isTestModeEnabled())
            validate();
    }


    @AssistedInject
    private NaryIQTreeImpl(@Assisted NaryOperatorNode rootNode, @Assisted ImmutableList<IQTree> children,
                           IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory, OntopModelSettings settings) {
        this(rootNode, children, iqFactory.createIQProperties(), iqTreeTools, iqFactory, settings);
    }

    @Override
    protected void validateNode() throws InvalidIntermediateQueryException {
        getRootNode().validateNode(getChildren());
    }

    @Override
    public IQTree acceptTransformer(IQTreeVisitingTransformer transformer) {
        return getRootNode().acceptTransformer(this, transformer, getChildren());
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor) {
        return getRootNode().acceptVisitor(visitor, getChildren());
    }

    @Override
    public IQTree normalizeForOptimization(VariableGenerator variableGenerator) {
        return getProperties().isNormalizedForOptimization()
                ? this
                : getRootNode().normalizeForOptimization(getChildren(), variableGenerator, getProperties());
    }

    /**
     * TODO: use the properties for optimization purposes?
     */
    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, VariableGenerator variableGenerator) {
        return getRootNode().liftIncompatibleDefinitions(variable, getChildren(), variableGenerator);
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

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution) {
        try {
            return normalizeDescendingSubstitution(descendingSubstitution)
                    .map(s -> getRootNode().applyDescendingSubstitutionWithoutOptimizing(s, getChildren()))
                    .orElse(this);

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
    public boolean isDistinct() {
        if (isDistinct == null)
            isDistinct = getRootNode().isDistinct(getChildren());
        return isDistinct;
    }

    @Override
    public boolean isDeclaredAsEmpty() {
        return false;
    }

    @Override
    public VariableNullability getVariableNullability() {
        if (variableNullability == null)
            variableNullability = getRootNode().getVariableNullability(getChildren());
        return variableNullability;
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint) {
        return getRootNode().propagateDownConstraint(constraint, getChildren());
    }

    @Override
    public IQTree replaceSubTree(IQTree subTreeToReplace, IQTree newSubTree) {
        if (equals(subTreeToReplace))
            return newSubTree;

        ImmutableList<IQTree> newChildren = getChildren().stream()
                .map(c -> c.replaceSubTree(subTreeToReplace, newSubTree))
                .collect(ImmutableCollectors.toList());

        return iqFactory.createNaryIQTree(getRootNode(), newChildren);
    }

    @Override
    public ImmutableSet<ImmutableSubstitution<NonVariableTerm>> getPossibleVariableDefinitions() {
        if (variableDefinition == null)
            variableDefinition = getRootNode().getPossibleVariableDefinitions(getChildren());
        return variableDefinition;
    }

    @Override
    public IQTree removeDistincts() {
        IQProperties properties = getProperties();
        return properties.areDistinctAlreadyRemoved()
                ? this
                : getRootNode().removeDistincts(getChildren(), properties);
    }

    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints() {
        if (uniqueConstraints == null) {
            uniqueConstraints = getRootNode().inferUniqueConstraints(getChildren());
        }
        return uniqueConstraints;
    }
}
