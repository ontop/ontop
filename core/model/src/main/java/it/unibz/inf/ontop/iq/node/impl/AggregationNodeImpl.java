package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.AggregationNormalizer;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.*;
import java.util.stream.Stream;

public class AggregationNodeImpl extends ExtendedProjectionNodeImpl implements AggregationNode {

    private static final String AGGREGATE_NODE_STR = "AGGREGATE";

    private final ImmutableSet<Variable> projectedVariables;
    private final ImmutableSet<Variable> groupingVariables;
    private final Substitution<ImmutableFunctionalTerm> substitution;
    private final ImmutableSet<Variable> childVariables;

    private final AggregationNormalizer aggregationNormalizer;

    @AssistedInject
    protected AggregationNodeImpl(@Assisted ImmutableSet<Variable> groupingVariables,
                                  @Assisted Substitution<ImmutableFunctionalTerm> substitution,
                                  SubstitutionFactory substitutionFactory,
                                  IntermediateQueryFactory iqFactory,
                                  AggregationNormalizer aggregationNormalizer,
                                  IQTreeTools iqTreeTools,
                                  TermFactory termFactory,
                                  OntopModelSettings settings) {
        super(substitutionFactory, iqFactory, iqTreeTools, termFactory);
        this.groupingVariables = groupingVariables;
        this.substitution = substitution;
        this.aggregationNormalizer = aggregationNormalizer;
        this.projectedVariables = Sets.union(groupingVariables, substitution.getDomain()).immutableCopy();
        this.childVariables = Sets.union(groupingVariables, substitution.getRangeVariables()).immutableCopy();

        if (settings.isTestModeEnabled())
            validateNode();
    }


    @Override
    public IQTree applyDescendingSubstitution(DownPropagation dp, IQTree child) {
        return applyDescendingSubstitutionOrBlock(
                dp.getOptionalDescendingSubstitution().get(),
                child,
                dp.getVariableGenerator(),
                r -> propagateDescendingSubstitutionToChild(child, r, dp),
                this::createAggregationNode);
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                                               IQTree child, VariableGenerator variableGenerator) {
        return applyDescendingSubstitutionOrBlock(
                descendingSubstitution,
                child,
                variableGenerator,
                r -> iqTreeTools.applyDownPropagationWithoutOptimization(child, r.delta, variableGenerator),
                this::createAggregationNode);
    }

    private Optional<AggregationNode> createAggregationNode(ImmutableSet<Variable> newProjectedVariables,
                                                            Substitution<ImmutableTerm> theta, IQTree newChild) {
        return Optional.of(iqFactory.createAggregationNode(
                Sets.difference(newProjectedVariables, theta.getDomain()).immutableCopy(),
                theta.transform(t -> (ImmutableFunctionalTerm)t)));
    }

    private IQTree applyDescendingSubstitutionOrBlock(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                                      IQTree child,
                                                      VariableGenerator variableGenerator,
                                                      DescendingSubstitutionChildUpdateFunction updateChildFct,
                                                      ExtendedProjectionNodeConstructor ctr) {

        ImmutableSet<Variable> aggregationVariables = substitution.getDomain();

        Substitution<GroundTerm> blockedGroundTermSubstitution = descendingSubstitution.builder()
                .restrictDomainTo(aggregationVariables)
                .restrictRangeTo(GroundTerm.class)
                .build();

        Substitution<Variable> descendingVar2Var = descendingSubstitution.restrictRangeTo(Variable.class);
        // Variables whose entries are blocked
        ImmutableSet<Variable> blockedVariables = descendingVar2Var.getRangeSet().stream()
                .flatMap(v -> extractBlockedDomainVars(v, descendingVar2Var.getPreImage(t -> t.equals(v)), aggregationVariables).stream())
                .collect(ImmutableCollectors.toSet());
        Substitution<Variable> blockedVariableSubstitution = descendingVar2Var.restrictDomainTo(blockedVariables);

        Substitution<? extends VariableOrGroundTerm> blockedSubstitution =
                substitutionFactory.union(blockedGroundTermSubstitution, blockedVariableSubstitution);

        Substitution<? extends VariableOrGroundTerm> nonBlockedSubstitution = descendingSubstitution.removeFromDomain(blockedSubstitution.getDomain());
        IQTree newSubTree = applyDescendingSubstitution(nonBlockedSubstitution, child, variableGenerator, updateChildFct, ctr);

        if (blockedSubstitution.isEmpty())
            return newSubTree;

        // Blocked entries -> reconverted into a filter
        ImmutableExpression condition = termFactory.getConjunction(
                blockedSubstitution.builder().toStream(termFactory::getStrictEquality).collect(ImmutableCollectors.toList()));

        FilterNode filterNode = iqFactory.createFilterNode(condition);

        InjectiveSubstitution<Variable> renamingSubstitution = filterNode.getLocalVariables().stream()
                .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));

        IQTree filterTree = iqTreeTools.applyDownPropagation(renamingSubstitution, iqFactory.createUnaryIQTree(filterNode, newSubTree));

        return iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(
                        DownPropagation.computeProjectedVariables(descendingSubstitution, getVariables())),
                filterTree);
    }

    private Set<Variable> extractBlockedDomainVars(Variable rangeVariable, ImmutableSet<Variable> domainVariables,
                                                      ImmutableSet<Variable> aggregationVariables) {
        // Equalities to aggregation variable are blocked
        if (aggregationVariables.contains(rangeVariable))
            return domainVariables;

        Set<Variable> aggregationDomainVariables = Sets.intersection(domainVariables, aggregationVariables);

        // Equalities from an aggregation variable to a grouping variable are blocked
        if (groupingVariables.contains(rangeVariable))
            return aggregationDomainVariables;

        // Fresh variables: need at least one variable to become projected
        // the latter may be an aggregation variable if there is no grouping variable
        Variable dominantVariable = aggregationDomainVariables.stream()
                .findAny()
                .orElseGet(() -> domainVariables.iterator().next());

        return Sets.difference(aggregationDomainVariables, ImmutableSet.of(dominantVariable));
    }


    @Override
    public IQTree normalizeForOptimization(IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        return aggregationNormalizer.normalizeForOptimization(this, child, variableGenerator, treeCache);
    }

    @Override
    public AggregationNode applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution) {
        return iqFactory.createAggregationNode(
                substitutionFactory.apply(renamingSubstitution, groupingVariables),
                substitutionFactory.onImmutableFunctionalTerms().rename(renamingSubstitution, substitution));
    }


    @Override
    public boolean isDistinct(IQTree tree, IQTree child) {
        return true;
    }

    /**
     * By default does not lift.
     * TODO: see if in some cases we could lift
     */
    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child, VariableGenerator variableGenerator) {
        return iqFactory.createUnaryIQTree(this, child);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof AggregationNodeImpl) {
            AggregationNodeImpl that = (AggregationNodeImpl) o;
            return groupingVariables.equals(that.groupingVariables) && substitution.equals(that.substitution);
        }
        return false;
    }

    @Override
    public boolean wouldKeepDescendingGroundTermInFilterAbove(Variable variable, boolean isConstant) {
        return substitution.isDefining(variable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupingVariables, substitution);
    }

    @Override
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {
        if (!(child instanceof TrueNode)
                && !child.getVariables().containsAll(getLocallyRequiredVariables())) {
            throw new InvalidIntermediateQueryException("The child of the aggregation node is missing some variables: "
                    + Sets.difference(child.getVariables(), getLocallyRequiredVariables()));
        }
    }

    private void validateNode() throws InvalidIntermediateQueryException {

        if (!Sets.intersection(substitution.getDomain(), substitution.getRangeVariables()).isEmpty()) {
            throw new InvalidIntermediateQueryException("AggregationNode: substitution redefines variables it is using: " + this);
        }

        if (!Sets.intersection(groupingVariables, substitution.getDomain()).isEmpty()) {
            throw new InvalidIntermediateQueryException(String.format(
                    "AggregationNode: the grouping variables (%s) and the substitution domain (%s) must be disjoint",
                    groupingVariables, substitution.getDomain()));
        }

        if (substitution.rangeAnyMatch(t -> !t.getFunctionSymbol().isAggregation())) {
            throw new InvalidIntermediateQueryException(String.format(
                    "The substitution of the aggregation node should only define aggregates, not %s",
                    substitution.builder().restrictRange(t -> !t.getFunctionSymbol().isAggregation()).build()));
        }
    }


    @Override
    public ImmutableSet<Substitution<NonVariableTerm>> getPossibleVariableDefinitions(IQTree child) {

        ImmutableSet<Substitution<NonVariableTerm>> groupingVariableDefs = child.getPossibleVariableDefinitions().stream()
                .map(s -> s.restrictDomainTo(groupingVariables))
                .collect(ImmutableCollectors.toSet());

        Substitution<NonVariableTerm> def = substitution.restrictRangeTo(NonVariableTerm.class);

        if (groupingVariableDefs.isEmpty()) {
            return def.isEmpty()
                    ? ImmutableSet.of(substitutionFactory.getSubstitution())
                    : ImmutableSet.of(def);
        }

        // For Aggregation functional terms, we don't look further on for child definitions
        return groupingVariableDefs.stream()
                .map(childDef -> substitutionFactory.union(childDef, def))
                .collect(ImmutableCollectors.toSet());
    }

    /**
     * By default, blocks the distinct removal
     * TODO: detect when we can do it (absence of cardinality-sensitive aggregation functions)
     */
    @Override
    public IQTree removeDistincts(IQTree child, IQTreeCache treeCache) {
        return iqFactory.createUnaryIQTree(this, child, treeCache.declareDistinctRemoval(true));
    }

    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(IQTree child) {
        return groupingVariables.isEmpty()
                // Only one tuple (NO GROUP BY)
                ? ImmutableSet.of(getVariables())
                // Grouping variables + possible sub-sets of them
                : Stream.concat(
                        child.inferUniqueConstraints().stream()
                                .filter(groupingVariables::containsAll),
                        Stream.of(getGroupingVariables()))
                  .collect(ImmutableCollectors.toSet());
    }

    @Override
    public FunctionalDependencies inferFunctionalDependencies(IQTree child, ImmutableSet<ImmutableSet<Variable>> uniqueConstraints, ImmutableSet<Variable> variables) {
        var childFDs = child.inferFunctionalDependencies();

        //Return all of the child's functional dependencies that are included entirely inside the grouping variables.
        return childFDs.stream()
                .filter(e -> groupingVariables.containsAll(e.getKey()))
                .filter(e -> e.getValue().stream()
                        .anyMatch(groupingVariables::contains))
                .map(e -> Maps.immutableEntry(e.getKey(), e.getValue().stream()
                        .filter(groupingVariables::contains)
                        .collect(ImmutableCollectors.toSet())))
                .collect(FunctionalDependencies.toFunctionalDependencies())
                .concat(FunctionalDependencies.fromUniqueConstraints(uniqueConstraints, variables));
    }

    /**
     * Out of the projected variables, only the grouping variables are required
     */
    @Override
    public VariableNonRequirement computeVariableNonRequirement(IQTree child) {
        return VariableNonRequirement.of(substitution.getDomain());
    }

    @Override
    public ImmutableSet<Variable> inferStrictDependents(UnaryIQTree tree, IQTree child) {
        return IQTreeTools.computeStrictDependentsFromFunctionalDependencies(tree);
    }

    @Override
    public Substitution<ImmutableFunctionalTerm> getSubstitution() {
        return substitution;
    }

    @Override
    public ImmutableSet<Variable> getGroupingVariables() {
        return groupingVariables;
    }

    @Override
    public ImmutableSet<Variable> getChildVariables() {
        return childVariables;
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return projectedVariables;
    }

    @Override
    public String toString() {
        // TODO: display the query modifiers
        return AGGREGATE_NODE_STR + " " + groupingVariables + " " + "[" + substitution + "]" ;
    }
}
