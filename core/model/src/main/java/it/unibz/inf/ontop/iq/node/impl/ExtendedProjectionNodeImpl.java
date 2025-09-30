package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ExtendedProjectionNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

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



    /**
     *
     * TODO: better handle the constraint
     *
     * Returns the new child
     */
    protected final IQTree propagateDescendingSubstitutionToChild(IQTree child,
                                                          PropagationResults tauFPropagationResults,
                                                          DownPropagation dp0) throws DownPropagation.InconsistentDownPropagationException {

        DownPropagation dp = iqTreeTools.createDownPropagation(tauFPropagationResults.delta, dp0.getConstraint(), dp0.getVariables(), dp0.getVariableGenerator());

        var newConstraint = iqTreeTools.updateDownPropagationConstraint(dp0, tauFPropagationResults.theta, Optional.empty(), child::getVariableNullability);

        return dp.propagateWithConstraint(newConstraint, child);
    }

    protected final IQTree propagateDescendingSubstitutionToChildWithoutOptimizing(Substitution<? extends VariableOrGroundTerm> descendingSubstitution, IQTree child, VariableGenerator variableGenerator) {
        return Optional.of(descendingSubstitution)
                .filter(delta -> !delta.isEmpty())
                .map(d -> child.applyDescendingSubstitutionWithoutOptimizing(d, variableGenerator))
                .orElse(child);
    }

    protected final IQTree applyDescendingSubstitution(Substitution<? extends VariableOrGroundTerm> tau,
                                                       IQTree child,
                                                       VariableGenerator variableGenerator,
                                                       DescendingSubstitutionChildUpdateFunction updateChildFct,
                                                       ExtendedProjectionNodeConstructor ctr) {

        try {
            DownPropagation ds = iqTreeTools.createDownPropagation(tau, Optional.empty(), getVariables(), variableGenerator);
            PropagationResults tauPropagationResults = propagateTau(ds, child.getVariables());

            IQTree newChild = updateChildFct.apply(tauPropagationResults);

            Optional<? extends ExtendedProjectionNode> projectionNode = ctr.create(
                    ds.computeProjectedVariables(),
                    tauPropagationResults.theta,
                    newChild);

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(projectionNode)
                    .append(iqTreeTools.createOptionalFilterNode(tauPropagationResults.filter))
                    .build(newChild);
        }
        catch (DownPropagation.InconsistentDownPropagationException e) {
            return iqTreeTools.createEmptyNode(DownPropagation.computeProjectedVariables(tau, getVariables()));
        }
    }


    private PropagationResults propagateTau(DownPropagation dp, ImmutableSet<Variable> childVariables) throws DownPropagation.InconsistentDownPropagationException {

        // tau is the descendingSubstitution
        // theta is the CONSTRUCT substitution

        Substitution<? extends VariableOrGroundTerm> tau = dp.getOptionalDescendingSubstitution().get();
        ImmutableSet<Variable> projectedVariables = dp.getVariables();
        Substitution<? extends ImmutableTerm> substitution = getSubstitution();

        // tauC to thetaC

        Substitution<NonFunctionalTerm> tauC = tau.restrictRangeTo(NonFunctionalTerm.class);
        Substitution<NonFunctionalTerm> thetaC = substitution.restrictRangeTo(NonFunctionalTerm.class);

        ImmutableSet<Variable> projectedVariablesAfterTauC = DownPropagation.computeProjectedVariables(tauC, dp.getVariables());

        Substitution<NonFunctionalTerm> newEta = substitutionFactory.onNonFunctionalTerms().unifierBuilder(thetaC)
                .unify(tauC.stream(), Map.Entry::getKey, Map.Entry::getValue)
                .build()
                .map(eta -> substitutionFactory.onNonFunctionalTerms()
                        .compose(substitutionFactory.getPrioritizingRenaming(eta, projectedVariablesAfterTauC), eta))
                .orElseThrow(DownPropagation.InconsistentDownPropagationException::new);

        Substitution<NonFunctionalTerm> thetaCBar = newEta.restrictDomainTo(projectedVariablesAfterTauC);

        Substitution<NonFunctionalTerm> deltaC = newEta.builder()
                .removeFromDomain(thetaC.getDomain())
                .removeFromDomain(Sets.difference(thetaCBar.getDomain(), projectedVariables))
                .build();

        //  deltaC to thetaF

        Substitution<ImmutableFunctionalTerm> thetaF = substitution.restrictRangeTo(ImmutableFunctionalTerm.class);

        // deltaC applied to thetaF as a list of equalities
        ImmutableMultimap<NonFunctionalTerm, ImmutableFunctionalTerm> m = thetaF.stream()
                .collect(ImmutableCollectors.toMultimap(
                        e -> substitutionFactory.onNonFunctionalTerms().apply(deltaC, e.getKey()),
                        e -> substitutionFactory.onImmutableTerms().apply(deltaC, e.getValue())));

        // for each Variable key (not in childVariables), picks one value from the group
        Substitution<ImmutableFunctionalTerm> thetaFBar = m.asMap().entrySet().stream()
                .filter(e -> e.getKey() instanceof Variable)
                .filter(e -> !childVariables.contains((Variable)e.getKey()))
                .collect(substitutionFactory.toSubstitution(
                        e -> (Variable) e.getKey(),
                        e -> e.getValue().iterator().next())); // choose some

        // compare with AbstractJoinTransferLJTransformer.extractEqualities
        // gets all entries of m that are not in thetaFBar
        Stream<ImmutableExpression> thetaFRemainingEqualities = m.entries().stream()
                .filter(e ->
                        !((e.getKey() instanceof Variable)
                        && thetaFBar.isDefining((Variable) e.getKey())
                        && thetaFBar.get((Variable) e.getKey()).equals(e.getValue())))
                .map(e -> termFactory.getStrictEquality(thetaFBar.applyToTerm(e.getKey()), e.getValue()));

        Substitution<ImmutableTerm> gamma = deltaC.builder()
                .removeFromDomain(thetaF.getDomain())
                .removeFromDomain(Sets.difference(thetaFBar.getDomain(), projectedVariables))
                .transform(v -> substitutionFactory.onImmutableTerms().applyToTerm(thetaFBar, v))
                .build();

        Substitution<NonFunctionalTerm> newDeltaC = gamma.restrictRangeTo(NonFunctionalTerm.class);

        Stream<ImmutableExpression> blockedExpressions = gamma.builder()
                .restrictRangeTo(ImmutableFunctionalTerm.class)
                .toStream(termFactory::getStrictEquality);

        // tauF propagation

        Substitution<GroundFunctionalTerm> tauF = tau.restrictRangeTo(GroundFunctionalTerm.class);
        Substitution<ImmutableTerm> thetaBar = thetaFBar.compose(thetaCBar);

        Substitution<VariableOrGroundTerm> delta = substitutionFactory.onVariableOrGroundTerms().compose(
                tauF.builder()
                        .removeFromDomain(thetaBar.getDomain())
                        .removeFromDomain(newDeltaC.getDomain())
                        .build(),
                newDeltaC);

        Substitution<ImmutableTerm> newTheta = thetaBar.builder().removeFromDomain(tauF.getDomain()).build();

        Optional<ImmutableExpression> newF = termFactory.getConjunction(Stream.concat(
                Stream.concat(thetaFRemainingEqualities, blockedExpressions),
                Stream.concat(
                        matchingEqualities(tauF, thetaBar),
                        matchingEqualities(tauF, newDeltaC))));

        return new PropagationResults(newTheta, delta, newF);
    }

    private Stream<ImmutableExpression> matchingEqualities(Substitution<?> sub1, Substitution<?> sub2) {
        return sub1.builder()
                .restrictDomainTo(sub2.getDomain())
                .toStream((v, t) -> termFactory.getStrictEquality(sub2.apply(v), t));
    }

    @Override
    public IQTree propagateDownConstraint(DownPropagation dp, IQTree child) {
        try {
            var newConstraint = iqTreeTools.updateDownPropagationConstraint(dp, getSubstitution(), Optional.empty(), child::getVariableNullability);
            IQTree newChild = dp.propagateWithConstraint(newConstraint, child);
            return iqFactory.createUnaryIQTree(this, newChild);
        }
        catch (DownPropagation.InconsistentDownPropagationException e) {
            return iqTreeTools.createEmptyNode(dp);
        }
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
        IQTree apply(PropagationResults tauFPropagationResults)
                throws DownPropagation.InconsistentDownPropagationException;
    }

    @FunctionalInterface
    protected interface ExtendedProjectionNodeConstructor {
        Optional<? extends ExtendedProjectionNode> create(
                ImmutableSet<Variable> newProjectedVariables, Substitution<ImmutableTerm> theta, IQTree newChild);
    }


    protected static class PropagationResults {

        public final Substitution<VariableOrGroundTerm> delta;
        public final Optional<ImmutableExpression> filter;
        public final Substitution<ImmutableTerm> theta;

        PropagationResults(Substitution<ImmutableTerm> theta,
                           Substitution<VariableOrGroundTerm> delta,
                           Optional<ImmutableExpression> filter) {
            this.theta = theta;
            this.delta = delta;
            this.filter = filter;
        }
    }

}
