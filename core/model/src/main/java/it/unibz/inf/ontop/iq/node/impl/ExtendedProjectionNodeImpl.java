package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtendedProjectionNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class ExtendedProjectionNodeImpl extends CompositeQueryNodeImpl implements ExtendedProjectionNode {

    private final ImmutableUnificationTools unificationTools;
    protected final ConstructionNodeTools constructionNodeTools;
    private final ImmutableSubstitutionTools substitutionTools;

    public ExtendedProjectionNodeImpl(SubstitutionFactory substitutionFactory, IntermediateQueryFactory iqFactory,
                                      ImmutableUnificationTools unificationTools,
                                      ConstructionNodeTools constructionNodeTools,
                                      ImmutableSubstitutionTools substitutionTools, TermFactory termFactory) {
        super(substitutionFactory, termFactory, iqFactory);
        this.unificationTools = unificationTools;
        this.constructionNodeTools = constructionNodeTools;
        this.substitutionTools = substitutionTools;
    }

    @Override
    public IQTree applyDescendingSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint, IQTree child, VariableGenerator variableGenerator) {

        return applyDescendingSubstitution(descendingSubstitution, child,
                (c, r) -> propagateDescendingSubstitutionToChild(c, r, constraint, variableGenerator));
    }

    /**
     *
     * TODO: better handle the constraint
     *
     * Returns the new child
     */
    private IQTree propagateDescendingSubstitutionToChild(IQTree child,
                                                          ConstructionNodeImpl.PropagationResults<VariableOrGroundTerm> tauFPropagationResults,
                                                          Optional<ImmutableExpression> constraint,
                                                          VariableGenerator variableGenerator) throws EmptyTreeException {

        Optional<ImmutableExpression> descendingConstraint;
        if (constraint.isPresent()) {
            ImmutableExpression initialConstraint = constraint.get();

            VariableNullability extendedVariableNullability = child.getVariableNullability()
                    .extendToExternalVariables(initialConstraint.getVariableStream());

            descendingConstraint = computeChildConstraint(tauFPropagationResults.theta, initialConstraint,
                    extendedVariableNullability);
        }
        else
            descendingConstraint = Optional.empty();

        return Optional.of(tauFPropagationResults.delta)
                .filter(delta -> !delta.isEmpty())
                .map(delta -> child.applyDescendingSubstitution(delta, descendingConstraint, variableGenerator))
                .orElse(child);
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution, IQTree child, VariableGenerator variableGenerator) {
        return applyDescendingSubstitution(descendingSubstitution, child,
                (c, r) -> Optional.of(r.delta)
                        .filter(delta -> !delta.isEmpty())
                        .map(d -> c.applyDescendingSubstitutionWithoutOptimizing(d, variableGenerator))
                        .orElse(c));
    }

    private IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> tau, IQTree child,
                                               DescendingSubstitutionChildUpdateFunction updateChildFct) {

        ImmutableSet<Variable> newProjectedVariables = constructionNodeTools.computeNewProjectedVariables(tau, getVariables());

        ImmutableSubstitution<NonFunctionalTerm> tauC = tau.builder().restrictRangeTo(NonFunctionalTerm.class).build();
        ImmutableSubstitution<GroundFunctionalTerm> tauF = tau.builder().restrictRangeTo(GroundFunctionalTerm.class).build();

        try {
            ConstructionNodeImpl.PropagationResults<NonFunctionalTerm> tauCPropagationResults = propagateTauC(tauC, child);
            ConstructionNodeImpl.PropagationResults<VariableOrGroundTerm> tauFPropagationResults = propagateTauF(tauF, tauCPropagationResults);

            Optional<FilterNode> filterNode = tauFPropagationResults.filter
                    .map(iqFactory::createFilterNode);

            IQTree newChild = updateChildFct.apply(child, tauFPropagationResults);

            Optional<ExtendedProjectionNode> projectionNode = computeNewProjectionNode(newProjectedVariables,
                    tauFPropagationResults.theta, newChild);

            IQTree filterTree = filterNode
                    .<IQTree>map(n -> iqFactory.createUnaryIQTree(n, newChild))
                    .orElse(newChild);

            return projectionNode
                    .<IQTree>map(n -> iqFactory.createUnaryIQTree(n, filterTree))
                    .orElse(filterTree);

        } catch (EmptyTreeException e) {
            return iqFactory.createEmptyNode(newProjectedVariables);
        }
    }

    protected abstract Optional<ExtendedProjectionNode> computeNewProjectionNode(
            ImmutableSet<Variable> newProjectedVariables, ImmutableSubstitution<ImmutableTerm> theta, IQTree newChild);

    private ConstructionNodeImpl.PropagationResults<NonFunctionalTerm> propagateTauC(ImmutableSubstitution<NonFunctionalTerm> tauC, IQTree child)
            throws EmptyTreeException {

        ImmutableSet<Variable> projectedVariables = getVariables();
        ImmutableSubstitution<? extends ImmutableTerm> substitution = getSubstitution();

        /* ---------------
         * tauC to thetaC
         * ---------------
         */

        ImmutableSubstitution<NonFunctionalTerm> thetaC = substitution.builder().restrictRangeTo(NonFunctionalTerm.class).build();

        // Projected variables after propagating tauC
        ImmutableSet<Variable> vC = constructionNodeTools.computeNewProjectedVariables(tauC, projectedVariables);

        ImmutableSubstitution<NonFunctionalTerm> newEta = unificationTools.computeMGUS2(thetaC, tauC)
                .map(eta -> substitutionTools.prioritizeRenaming(eta, vC))
                .orElseThrow(ConstructionNodeImpl.EmptyTreeException::new);

        ImmutableSubstitution<NonFunctionalTerm> thetaCBar = newEta.restrictDomain(vC);

        ImmutableSubstitution<NonFunctionalTerm> deltaC = newEta.builder()
                .restrictDomain(v -> !thetaC.isDefining(v))
                .restrictDomain(v -> !thetaCBar.isDefining(v) || projectedVariables.contains(v))
                .build();

        /* ---------------
         * deltaC to thetaF
         * ---------------
         */
        ImmutableSubstitution<ImmutableFunctionalTerm> thetaF = substitution.builder().restrictRangeTo(ImmutableFunctionalTerm.class).build();

        ImmutableMultimap<ImmutableTerm, ImmutableFunctionalTerm> m = thetaF.entrySet().stream()
                .collect(ImmutableCollectors.toMultimap(
                        e -> deltaC.applyToVariable(e.getKey()),
                        e -> deltaC.applyToFunctionalTerm(e.getValue())));

        ImmutableSubstitution<ImmutableFunctionalTerm> thetaFBar = substitutionFactory.getSubstitution(
                m.asMap().entrySet().stream()
                        .filter(e -> e.getKey() instanceof Variable)
                        .filter(e -> !child.getVariables().contains(e.getKey()))
                        .collect(ImmutableCollectors.toMap(
                                e -> (Variable) e.getKey(),
                                e -> e.getValue().iterator().next()
                        )));

        ImmutableSubstitution<ImmutableTerm> gamma = deltaC.builder()
                .restrictDomain(v -> !thetaF.isDefining(v))
                .restrictDomain(v -> !thetaFBar.isDefining(v) || projectedVariables.contains(v))
                .transform(thetaFBar::apply)
                .build();

        ImmutableSubstitution<NonFunctionalTerm> newDeltaC = gamma.builder().restrictRangeTo(NonFunctionalTerm.class).build();

        ImmutableSet<Map.Entry<Variable, ImmutableFunctionalTerm>> thetaFBarEntries = thetaFBar.entrySet();

        Stream<ImmutableExpression> thetaFRelatedExpressions = m.entries().stream()
                .filter(e -> !thetaFBarEntries.contains(e))
                .map(e -> termFactory.getStrictEquality(thetaFBar.apply(e.getKey()), e.getValue()));

        Stream<ImmutableExpression> blockedExpressions = gamma.builder()
                .restrictDomain(v -> !newDeltaC.isDefining(v))
                .toStrictEqualities();

        Optional<ImmutableExpression> f = Optional.of(Stream.concat(thetaFRelatedExpressions, blockedExpressions)
                        .collect(ImmutableCollectors.toList()))
                .filter(l -> !l.isEmpty())
                .map(termFactory::getConjunction);

        return new ConstructionNodeImpl.PropagationResults<>(substitutionFactory.compose(thetaFBar, thetaCBar), newDeltaC, f);

    }

    private ConstructionNodeImpl.PropagationResults<VariableOrGroundTerm> propagateTauF(ImmutableSubstitution<GroundFunctionalTerm> tauF,
                                                                                        ConstructionNodeImpl.PropagationResults<NonFunctionalTerm> tauCPropagationResults) {

        ImmutableSubstitution<ImmutableTerm> thetaBar = tauCPropagationResults.theta;

        ImmutableSubstitution<VariableOrGroundTerm> delta = substitutionFactory.compose(
                tauF.builder()
                        .restrictDomain(v -> !thetaBar.isDefining(v))
                        .restrictDomain(v -> !tauCPropagationResults.delta.isDefining(v))
                        .build(),
                tauCPropagationResults.delta);

        ImmutableSubstitution<ImmutableTerm> newTheta = thetaBar.builder().restrictDomain(v -> !tauF.isDefining(v)).build();

        Stream<ImmutableExpression> newConditionStream =
                Stream.concat(
                        // tauF vs thetaBar
                        tauF.entrySet().stream()
                                .filter(e -> thetaBar.isDefining(e.getKey()))
                                .map(e -> termFactory.getStrictEquality(thetaBar.apply(e.getKey()), tauF.apply(e.getValue()))),
                        // tauF vs newDelta
                        tauF.entrySet().stream()
                                .filter(e -> tauCPropagationResults.delta.isDefining(e.getKey()))
                                .map(e -> termFactory.getStrictEquality(tauCPropagationResults.delta.apply(e.getKey()),
                                        tauF.apply(e.getValue()))));

        Optional<ImmutableExpression> newF = termFactory.getConjunction(tauCPropagationResults.filter, newConditionStream);

        return new ConstructionNodeImpl.PropagationResults<>(newTheta, delta, newF);
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child, VariableGenerator variableGenerator) {
        try {
            Optional<ImmutableExpression> childConstraint = computeChildConstraint(getSubstitution(), constraint,
                    child.getVariableNullability().extendToExternalVariables(constraint.getVariableStream()));
            IQTree newChild = childConstraint
                    .map(c -> child.propagateDownConstraint(c, variableGenerator))
                    .orElse(child);
            return iqFactory.createUnaryIQTree(this, newChild);

        } catch (EmptyTreeException e) {
            return iqFactory.createEmptyNode(getVariables());
        }
    }

    private Optional<ImmutableExpression> computeChildConstraint(ImmutableSubstitution<? extends ImmutableTerm> theta,
                                                                 ImmutableExpression initialConstraint,
                                                                 VariableNullability variableNullabilityForConstraint)
            throws EmptyTreeException {

        ImmutableExpression.Evaluation descendingConstraintResults = theta.applyToBooleanExpression(initialConstraint)
                .evaluate2VL(variableNullabilityForConstraint);

        if (descendingConstraintResults.isEffectiveFalse())
            throw new EmptyTreeException();

        return descendingConstraintResults.getExpression();
    }

    @Override
    public VariableNullability getVariableNullability(IQTree child) {
        return child.getVariableNullability().update(getSubstitution(), getVariables());
    }

    @Override
    public boolean isConstructed(Variable variable, IQTree child) {
        return getSubstitution().isDefining(variable)
                || (getChildVariables().contains(variable) && child.isConstructed(variable));
    }

    @FunctionalInterface
    protected interface DescendingSubstitutionChildUpdateFunction {

        IQTree apply(IQTree child, ConstructionNodeImpl.PropagationResults<VariableOrGroundTerm> tauFPropagationResults)
                throws EmptyTreeException;

    }

    protected static class EmptyTreeException extends Exception {
    }
}
