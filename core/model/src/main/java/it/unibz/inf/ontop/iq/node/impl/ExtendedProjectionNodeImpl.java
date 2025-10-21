package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ExtendedProjectionNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class ExtendedProjectionNodeImpl extends CompositeQueryNodeImpl implements ExtendedProjectionNode {

    public ExtendedProjectionNodeImpl(SubstitutionFactory substitutionFactory,
                                      IntermediateQueryFactory iqFactory,
                                      IQTreeTools iqTreeTools,
                                      TermFactory termFactory) {
        super(substitutionFactory, termFactory, iqFactory, iqTreeTools);
    }


    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return getChildVariables();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return getSubstitution().getDomain();
    }


    @Override
    public IQTree propagateDownConstraint(DownPropagation dp, IQTree child) {
        try {
            var newConstraint = iqTreeTools.applySubstitutionToConstraint(dp, getSubstitution(), child::getVariableNullability);
            var newDp = iqTreeTools.createDownPropagation(newConstraint, child.getVariables(), dp.getVariableGenerator());
            IQTree newChild = newDp.propagate(child);
            return iqFactory.createUnaryIQTree(this, newChild);
        }
        catch (DownPropagation.InconsistentDownPropagationException e) {
            return iqTreeTools.createEmptyNode(dp);
        }
    }

    protected final IQTree applyDescendingSubstitution(DownPropagation tau,
                                                       IQTree child,
                                                       ExtendedProjectionNodeConstructor ctr) {

        try {
            PropagationResults tauPropagationResults = propagateTau(tau, child.getVariables());

            Optional<ImmutableExpression> newConstraint = iqTreeTools.applySubstitutionToConstraint(tau, tauPropagationResults.getSubstitution(), child::getVariableNullability);

            var newDp = iqTreeTools.createDownPropagation(tauPropagationResults.getDescendingSubstitution(), newConstraint, child.getVariables(), tau.getVariableGenerator());

            IQTree newChild = newDp.propagate(child);

            Optional<? extends ExtendedProjectionNode> projectionNode = ctr.create(
                    tau.computeProjectedVariables(),
                    tauPropagationResults.getSubstitution(),
                    newChild);

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(projectionNode)
                    .append(tauPropagationResults.getOptionalFilter())
                    .build(newChild);
        }
        catch (DownPropagation.InconsistentDownPropagationException e) {
            return iqTreeTools.createEmptyNode(tau.computeProjectedVariables());
        }
    }


    private PropagationResults propagateTau(DownPropagation tau, ImmutableSet<Variable> childVariables) throws DownPropagation.InconsistentDownPropagationException {

        Substitution<? extends VariableOrGroundTerm> descendingSubstitution = tau.getDescendingSubstitution();
        ImmutableSet<Variable> projectedVariables = getVariables();
        Substitution<? extends ImmutableTerm> substitution = getSubstitution();

        // tauC to thetaC

        Substitution<NonFunctionalTerm> tauC = descendingSubstitution.restrictRangeTo(NonFunctionalTerm.class);
        ImmutableSet<Variable> projectedVariablesAfterTauC = DownPropagation.computeProjectedVariables(tauC, projectedVariables);

        Substitution<NonFunctionalTerm> thetaC = substitution.restrictRangeTo(NonFunctionalTerm.class);

        Substitution<NonFunctionalTerm> newEta = substitutionFactory.onNonFunctionalTerms().unifierBuilder()
                .unify(thetaC)
                .unify(tauC)
                .buildNormalized(projectedVariablesAfterTauC)
                .orElseThrow(DownPropagation.InconsistentDownPropagationException::new);

        Substitution<NonFunctionalTerm> thetaCBar = newEta.restrictDomainTo(projectedVariablesAfterTauC);

        Substitution<NonFunctionalTerm> deltaC = newEta
                .removeFromDomain(Sets.union(thetaC.getDomain(), Sets.difference(thetaCBar.getDomain(), projectedVariables)));

        //  deltaC to thetaF

        Substitution<ImmutableFunctionalTerm> thetaF = substitution.restrictRangeTo(ImmutableFunctionalTerm.class);

        ImmutableList<Map.Entry<ImmutableFunctionalTerm, NonFunctionalTerm>> deltaCThetaFEqualities = thetaF.stream()
                .map(e -> Maps.immutableEntry(
                        deltaC.apply(e.getValue()),
                        substitutionFactory.onNonFunctionalTerms().apply(deltaC, e.getKey())))
                .collect(ImmutableCollectors.toList());

        Substitution<ImmutableFunctionalTerm> thetaFBar = substitutionFactory.extractInverseSubstitution(deltaCThetaFEqualities.stream(), childVariables);

        Stream<ImmutableExpression> thetaFRemainingEqualities = iqTreeTools.getRemainingEqualities(deltaCThetaFEqualities, thetaFBar);

        Substitution<ImmutableTerm> gamma = deltaC.builder()
                .removeFromDomain(Sets.union(thetaF.getDomain(), Sets.difference(thetaFBar.getDomain(), projectedVariables)))
                .transform(thetaFBar::applyToTerm)
                .build();

        Substitution<NonFunctionalTerm> newDeltaC = gamma.restrictRangeTo(NonFunctionalTerm.class);

        Stream<ImmutableExpression> blockedExpressions = gamma.builder()
                .restrictRangeTo(ImmutableFunctionalTerm.class)
                .toStream(termFactory::getStrictEquality);

        // tauF propagation

        Substitution<GroundFunctionalTerm> tauF = descendingSubstitution.restrictRangeTo(GroundFunctionalTerm.class);
        Substitution<ImmutableTerm> thetaBar = substitutionFactory.union(thetaFBar, thetaCBar);

        Substitution<VariableOrGroundTerm> delta = substitutionFactory.onVariableOrGroundTerms().compose(
                tauF.removeFromDomain(Sets.union(thetaBar.getDomain(), newDeltaC.getDomain())),
                newDeltaC);

        Optional<ImmutableExpression> newF = termFactory.getConjunction(Stream.concat(
                Stream.concat(thetaFRemainingEqualities, blockedExpressions),
                Stream.concat(
                        matchingEqualities(tauF, thetaBar),
                        matchingEqualities(tauF, newDeltaC))));

        return new PropagationResults(
                thetaBar.removeFromDomain(tauF.getDomain()),
                delta,
                newF);
    }

    private Stream<ImmutableExpression> matchingEqualities(Substitution<?> sub1, Substitution<?> sub2) {
        return Sets.intersection(sub1.getDomain(), sub2.getDomain()).stream()
                .map(v -> termFactory.getStrictEquality(sub1.apply(v), sub2.apply(v)));
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
    protected interface ExtendedProjectionNodeConstructor {
        Optional<? extends ExtendedProjectionNode> create(
                ImmutableSet<Variable> variables, Substitution<ImmutableTerm> substitution, IQTree newChild);
    }


    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected class PropagationResults {

        private final Substitution<VariableOrGroundTerm> delta;
        private final Optional<FilterNode> filter;
        private final Substitution<ImmutableTerm> theta;

        PropagationResults(Substitution<ImmutableTerm> theta,
                           Substitution<VariableOrGroundTerm> delta,
                           Optional<ImmutableExpression> filter) {
            this.theta = theta;
            this.delta = delta;
            this.filter = iqTreeTools.createOptionalFilterNode(filter);
        }

        Optional<FilterNode> getOptionalFilter() {
            return filter;
        }

        Substitution<ImmutableTerm> getSubstitution() {
            return theta;
        }

        Substitution<VariableOrGroundTerm> getDescendingSubstitution() {
            return delta;
        }

    }
}
