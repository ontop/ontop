package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.DownConstraint;
import it.unibz.inf.ontop.iq.impl.DescendingSubstitution;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ExtendedProjectionNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
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
    public IQTree applyDescendingSubstitution(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
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
                                                          PropagationResults tauFPropagationResults,
                                                          Optional<ImmutableExpression> constraint,
                                                          VariableGenerator variableGenerator) throws UnsatisfiableConditionException {

        DownConstraint initialDownConstraint = new DownConstraint(constraint);
        DownConstraint downConstraint = computeChildConstraint(tauFPropagationResults.theta, initialDownConstraint, child);

        return Optional.of(tauFPropagationResults.delta)
                .filter(delta -> !delta.isEmpty())
                .map(delta -> child.applyDescendingSubstitution(delta, downConstraint.getConstraint(), variableGenerator))
                .orElse(child);
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution, IQTree child, VariableGenerator variableGenerator) {
        return applyDescendingSubstitution(descendingSubstitution, child,
                (c, r) -> Optional.of(r.delta)
                        .filter(delta -> !delta.isEmpty())
                        .map(d -> c.applyDescendingSubstitutionWithoutOptimizing(d, variableGenerator))
                        .orElse(c));
    }

    private IQTree applyDescendingSubstitution(Substitution<? extends VariableOrGroundTerm> tau, IQTree child,
                                               DescendingSubstitutionChildUpdateFunction updateChildFct) {

        DescendingSubstitution ds = new DescendingSubstitution(tau, getVariables());

        try {
            PropagationResults tauPropagationResults = propagateTau(tau, child.getVariables());

            IQTree newChild = updateChildFct.apply(child, tauPropagationResults);

            Optional<FilterNode> filterNode = iqTreeTools.createOptionalFilterNode(tauPropagationResults.filter);

            Optional<? extends ExtendedProjectionNode> projectionNode = computeNewProjectionNode(
                    ds.computeProjectedVariables(),
                    tauPropagationResults.theta, newChild);

            return iqTreeTools.createOptionalUnaryIQTree(projectionNode, filterNode, newChild);
        }
        catch (UnsatisfiableConditionException e) {
            return iqTreeTools.createEmptyNode(ds);
        }
    }

    protected abstract Optional<? extends ExtendedProjectionNode> computeNewProjectionNode(
            ImmutableSet<Variable> newProjectedVariables, Substitution<ImmutableTerm> theta, IQTree newChild);


    private PropagationResults propagateTau(Substitution<? extends VariableOrGroundTerm> tau, ImmutableSet<Variable> childVariables) throws UnsatisfiableConditionException {

        ImmutableSet<Variable> projectedVariables = getVariables();
        Substitution<? extends ImmutableTerm> substitution = getSubstitution();

        // tauC to thetaC

        Substitution<NonFunctionalTerm> tauC = tau.restrictRangeTo(NonFunctionalTerm.class);
        Substitution<NonFunctionalTerm> thetaC = substitution.restrictRangeTo(NonFunctionalTerm.class);

        DescendingSubstitution ds2 = new DescendingSubstitution(tauC, getVariables());
        ImmutableSet<Variable> projectedVariablesAfterTauC = ds2.computeProjectedVariables();

        Substitution<NonFunctionalTerm> newEta = substitutionFactory.onNonFunctionalTerms().unifierBuilder(thetaC)
                .unify(tauC.stream(), Map.Entry::getKey, Map.Entry::getValue)
                .build()
                .map(eta -> substitutionFactory.onNonFunctionalTerms()
                        .compose(substitutionFactory.getPrioritizingRenaming(eta, projectedVariablesAfterTauC), eta))
                .orElseThrow(UnsatisfiableConditionException::new);

        Substitution<NonFunctionalTerm> thetaCBar = newEta.restrictDomainTo(projectedVariablesAfterTauC);

        Substitution<NonFunctionalTerm> deltaC = newEta.builder()
                .removeFromDomain(thetaC.getDomain())
                .removeFromDomain(Sets.difference(thetaCBar.getDomain(), projectedVariables))
                .build();

        //  deltaC to thetaF

        Substitution<ImmutableFunctionalTerm> thetaF = substitution.restrictRangeTo(ImmutableFunctionalTerm.class);

        ImmutableMultimap<NonFunctionalTerm, ImmutableFunctionalTerm> m = thetaF.stream()
                .collect(ImmutableCollectors.toMultimap(
                        e -> substitutionFactory.onNonFunctionalTerms().apply(deltaC, e.getKey()),
                        e -> substitutionFactory.onImmutableTerms().apply(deltaC, e.getValue())));

        Substitution<ImmutableFunctionalTerm> thetaFBar = m.asMap().entrySet().stream()
                .filter(e -> e.getKey() instanceof Variable)
                .filter(e -> !childVariables.contains((Variable)e.getKey()))
                .collect(substitutionFactory.toSubstitution(
                        e -> (Variable) e.getKey(),
                        e -> e.getValue().iterator().next())); // choose some

        Substitution<ImmutableTerm> gamma = deltaC.builder()
                .removeFromDomain(thetaF.getDomain())
                .removeFromDomain(Sets.difference(thetaFBar.getDomain(), projectedVariables))
                .transform(v -> substitutionFactory.onImmutableTerms().applyToTerm(thetaFBar, v))
                .build();

        Substitution<NonFunctionalTerm> newDeltaC = gamma.restrictRangeTo(NonFunctionalTerm.class);

        // compare with AbstractJoinTransferLJTransformer.extractEqualities
        Stream<ImmutableExpression> thetaFRelatedExpressions = m.entries().stream()
                .filter(e -> !(e.getKey() instanceof Variable)
                        || !thetaFBar.isDefining((Variable) e.getKey())
                        || !thetaFBar.get((Variable) e.getKey()).equals(e.getValue()))
                .map(e -> termFactory.getStrictEquality(thetaFBar.applyToTerm(e.getKey()), e.getValue()));

        Stream<ImmutableExpression> blockedExpressions = gamma.builder()
                .restrictRangeTo(ImmutableFunctionalTerm.class)
                .toStream(termFactory::getStrictEquality);

        Optional<ImmutableExpression> f = Optional.of(
                        Stream.concat(thetaFRelatedExpressions, blockedExpressions).collect(ImmutableCollectors.toList()))
                .filter(l -> !l.isEmpty())
                .map(termFactory::getConjunction);


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

        Stream<ImmutableExpression> newConditionStream = Stream.concat(
                tauF.builder() // tauF vs thetaBar
                        .restrictDomainTo(thetaBar.getDomain())
                        .toStream((v, t) -> termFactory.getStrictEquality(thetaBar.apply(v), t)),
                tauF.builder() // tauF vs newDelta
                        .restrictDomainTo(newDeltaC.getDomain())
                        .toStream((v, t) -> termFactory.getStrictEquality(newDeltaC.apply(v), t)));

        Optional<ImmutableExpression> newF = termFactory.getConjunction(f, newConditionStream);

        return new PropagationResults(newTheta, delta, newF);
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child, VariableGenerator variableGenerator) {
        try {
            DownConstraint initial = new DownConstraint(Optional.of(constraint));
            DownConstraint dc = computeChildConstraint(getSubstitution(), initial, child);

            IQTree newChild = dc.propagateDownOptionalConstraint(child, variableGenerator);
            return iqFactory.createUnaryIQTree(this, newChild);
        }
        catch (UnsatisfiableConditionException e) {
            return iqFactory.createEmptyNode(getVariables());
        }
    }

    private DownConstraint computeChildConstraint(Substitution<? extends ImmutableTerm> theta,
                                                  DownConstraint initialConstraint,
                                                  IQTree child)
            throws UnsatisfiableConditionException {

        if (initialConstraint.getConstraint().isEmpty())
            return initialConstraint;

        VariableNullability extendedVariableNullability = child.getVariableNullability()
                .extendToExternalVariables(initialConstraint.getVariableStream());

        return new DownConstraint(theta.apply(initialConstraint.getConstraint().get()), extendedVariableNullability);
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

        IQTree apply(IQTree child, PropagationResults tauFPropagationResults)
                throws UnsatisfiableConditionException;
    }


    private static class PropagationResults {

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
