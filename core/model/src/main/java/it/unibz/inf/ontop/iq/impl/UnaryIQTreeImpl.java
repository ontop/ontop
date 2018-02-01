package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.node.ExplicitVariableProjectionNode;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.iq.transform.IQTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public class UnaryIQTreeImpl extends AbstractCompositeIQTree<UnaryOperatorNode> implements UnaryIQTree {

    @AssistedInject
    private UnaryIQTreeImpl(@Assisted UnaryOperatorNode rootNode, @Assisted IQTree child,
                            @Assisted IQProperties iqProperties, IQTreeTools iqTreeTools,
                            IntermediateQueryFactory iqFactory, OntopModelSettings settings) {
        super(rootNode, ImmutableList.of(child), iqProperties, iqTreeTools, iqFactory);

        if (settings.isTestModeEnabled())
            validate();
    }

    @AssistedInject
    private UnaryIQTreeImpl(@Assisted UnaryOperatorNode rootNode, @Assisted IQTree child, IQTreeTools iqTreeTools,
                            IntermediateQueryFactory iqFactory, OntopModelSettings settings) {
        this(rootNode, child, iqFactory.createIQProperties(), iqTreeTools, iqFactory, settings);
    }

    @Override
    public IQTree liftBinding(VariableGenerator variableGenerator) {
        if (getProperties().isLifted())
            return this;
        else
            return getRootNode().liftBinding(getChild(), variableGenerator, getProperties());
    }

    /**
     * TODO: use the properties for optimization purposes
     */
    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable) {
        return getRootNode().liftIncompatibleDefinitions(variable, getChild());
    }

    @Override
    public IQTree applyDescendingSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint) {
        try {
            return normalizeDescendingSubstitution(descendingSubstitution)
                    .map(s -> getRootNode().applyDescendingSubstitution(s, constraint, getChild()))
                    .orElseGet(() -> constraint
                            .map(this::propagateDownConstraint)
                            .orElse(this));

        } catch (IQTreeTools.UnsatisfiableDescendingSubstitutionException e) {
            return iqFactory.createEmptyNode(iqTreeTools.computeNewProjectedVariables(descendingSubstitution, getVariables()));
        }
    }

    @Override
    public boolean isConstructed(Variable variable) {
        return getVariables().contains(variable)
                && getRootNode().isConstructed(variable, getChild());
    }

    @Override
    public boolean isDeclaredAsEmpty() {
        return false;
    }

    @Override
    public ImmutableSet<Variable> getNullableVariables() {
        return getRootNode().getNullableVariables(getChild());
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint) {
        return getRootNode().propagateDownConstraint(constraint, getChild());
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        UnaryOperatorNode rootNode = getRootNode();
        if (rootNode instanceof ExplicitVariableProjectionNode)
            return ((ExplicitVariableProjectionNode) rootNode).getVariables();
        else
            return getChild().getVariables();
    }

    @Override
    protected void validateNode() throws InvalidIntermediateQueryException {
        getRootNode().validateNode(getChild());
    }

    @Override
    public IQTree acceptTransformer(IQTransformer transformer) {
        return getRootNode().acceptTransformer(this, transformer, getChild());
    }

    @Override
    public IQTree getChild() {
        return getChildren().get(0);
    }
}
