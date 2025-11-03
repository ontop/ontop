package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ExtendedProjectionNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.node.normalization.impl.ConditionSimplifierImpl;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
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
            var newConstraint = applySubstitutionToConstraint(dp, getSubstitution(), child::getVariableNullability);
            var newDp = iqTreeTools.createDownPropagation(newConstraint, child.getVariables(), dp.getVariableGenerator());
            IQTree newChild = newDp.propagate(child);
            return iqFactory.createUnaryIQTree(this, newChild);
        }
        catch (DownPropagation.InconsistentDownPropagationException e) {
            return iqFactory.createEmptyNode(dp.getResultingProjectedVariables());
        }
    }


    protected final PropagationResults propagateTau(DownPropagation tau, ImmutableSet<Variable> childVariables, Supplier<VariableNullability> variableNullabilitySupplier) throws DownPropagation.InconsistentDownPropagationException {

        Substitution<? extends VariableOrGroundTerm> descendingSubstitution = tau.getDescendingSubstitution();
        ImmutableSet<Variable> projectedVariables = getVariables();
        Substitution<? extends ImmutableTerm> substitution = getSubstitution();

        // tauC applied to thetaC: dealing with variables and constants

        Substitution<NonFunctionalTerm> tauC = descendingSubstitution.restrictRangeTo(NonFunctionalTerm.class);
        ImmutableSet<Variable> projectedVariablesAfterTauC = DownPropagation.getProjectedVariablesAfterDescendingSubstitution(tauC, projectedVariables);

        Substitution<NonFunctionalTerm> thetaC = substitution.restrictRangeTo(NonFunctionalTerm.class);

        // a solution of the variable-and-constant equalities avoiding projectedVariablesAfterTauC as much as possible
        Substitution<NonFunctionalTerm> newEta = substitutionFactory.onNonFunctionalTerms().unifierBuilder()
                .unify(thetaC)
                .unify(tauC)
                .buildNormalized(projectedVariablesAfterTauC)
                .orElseThrow(DownPropagation.InconsistentDownPropagationException::new);

        Substitution<NonFunctionalTerm> thetaCBar = newEta.restrictDomainTo(projectedVariablesAfterTauC);

        Substitution<NonFunctionalTerm> deltaC = newEta
                .removeFromDomain(Sets.union(thetaC.getDomain(), Sets.difference(thetaCBar.getDomain(), projectedVariables)));

        //  deltaC applied to thetaF (the theta-complement of thetaC)

        Substitution<ImmutableFunctionalTerm> thetaF = substitution.restrictRangeTo(ImmutableFunctionalTerm.class);

        ImmutableList<Map.Entry<ImmutableFunctionalTerm, NonFunctionalTerm>> deltaCThetaFEqualities = thetaF.stream()
                .map(e -> Maps.immutableEntry(
                        deltaC.apply(e.getValue()),
                        substitutionFactory.onNonFunctionalTerms().apply(deltaC, e.getKey())))
                .collect(ImmutableCollectors.toList());

        Substitution<ImmutableFunctionalTerm> thetaFBar = substitutionFactory.extractInverseSubstitution(deltaCThetaFEqualities.stream(), childVariables);

        Substitution<ImmutableTerm> gamma = deltaC.builder()
                .removeFromDomain(Sets.union(thetaF.getDomain(), Sets.difference(thetaFBar.getDomain(), projectedVariables)))
                .transform(thetaFBar::applyToTerm)
                .build();

        Substitution<NonFunctionalTerm> newDeltaC = gamma.restrictRangeTo(NonFunctionalTerm.class);

        // tauF propagation

        Substitution<GroundFunctionalTerm> tauF = descendingSubstitution.restrictRangeTo(GroundFunctionalTerm.class);
        Substitution<ImmutableTerm> thetaBar = substitutionFactory.union(thetaFBar, thetaCBar);

        Substitution<VariableOrGroundTerm> delta = substitutionFactory.onVariableOrGroundTerms().compose(
                tauF.removeFromDomain(Sets.union(thetaBar.getDomain(), newDeltaC.getDomain())),
                newDeltaC);

        var resultingSubstitution = thetaBar.removeFromDomain(tauF.getDomain());

        Optional<ImmutableExpression> newConstraint = applySubstitutionToConstraint(tau, resultingSubstitution, variableNullabilitySupplier);
        var newDp = iqTreeTools.createDownPropagation(delta, newConstraint, childVariables, tau.getVariableGenerator());

        Optional<ImmutableExpression> newF = termFactory.getConjunction(Stream.concat(
                Stream.concat(
                        iqTreeTools.getRemainingEqualitiesInverse(deltaCThetaFEqualities, thetaFBar),
                        iqTreeTools.getRemainingEqualitiesSimple(gamma, newDeltaC)),
                Stream.concat(
                        matchingEqualities(tauF, thetaBar),
                        matchingEqualities(tauF, newDeltaC))));

        return new PropagationResults(
                resultingSubstitution,
                newDp,
                newF);
    }

    private Stream<ImmutableExpression> matchingEqualities(Substitution<?> sub1, Substitution<?> sub2) {
        return Sets.intersection(sub1.getDomain(), sub2.getDomain()).stream()
                .map(v -> termFactory.getStrictEquality(sub1.apply(v), sub2.apply(v)));
    }

    private Optional<ImmutableExpression> applySubstitutionToConstraint(DownPropagation dp, Substitution<? extends ImmutableTerm> substitution, Supplier<VariableNullability> variableNullabilitySupplier) throws DownPropagation.InconsistentDownPropagationException {
        Optional<ImmutableExpression> optionalSubstitutedConstraint = dp.getConstraint().map(substitution::apply);

        return optionalSubstitutedConstraint.isPresent()
                ? ConditionSimplifierImpl.evaluateCondition(optionalSubstitutedConstraint.get(), dp.extendVariableNullability(variableNullabilitySupplier.get()))
                : optionalSubstitutedConstraint;
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

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected static class PropagationResults {

        private final DownPropagation dp;
        private final Optional<ImmutableExpression> filter;
        private final Substitution<ImmutableTerm> theta;

        PropagationResults(Substitution<ImmutableTerm> theta,
                           DownPropagation dp,
                           Optional<ImmutableExpression> filter) {
            this.theta = theta;
            this.dp = dp;
            this.filter = filter;
        }

        Optional<ImmutableExpression> getOptionalFilter() {
            return filter;
        }

        Substitution<ImmutableTerm> getSubstitution() {
            return theta;
        }

        DownPropagation getDownPropagation() {
            return dp;
        }
    }
}
