package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.AggregationNormalizer;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.*;
import java.util.function.Function;
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
                                  SubstitutionFactory substitutionFactory, IntermediateQueryFactory iqFactory,
                                  AggregationNormalizer aggregationNormalizer,
                                  IQTreeTools iqTreeTools,
                                  TermFactory termFactory,
                                  OntopModelSettings settings) {
        super(substitutionFactory, iqFactory, iqTreeTools, termFactory);
        this.groupingVariables = groupingVariables;
        this.substitution = substitution;
        this.aggregationNormalizer = aggregationNormalizer;
        this.projectedVariables = Sets.union(groupingVariables, substitution.getDomain()).immutableCopy();
        this.childVariables = extractChildVariables(groupingVariables, substitution);

        if (settings.isTestModeEnabled())
            validateNode();
    }

    public static ImmutableSet<Variable> extractChildVariables(ImmutableSet<Variable> groupingVariables,
                                                               Substitution<ImmutableFunctionalTerm> substitution) {
        return Sets.union(groupingVariables, substitution.getRangeVariables()).immutableCopy();
    }

    @Override
    public IQTree applyDescendingSubstitution(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint, IQTree child,
                                              VariableGenerator variableGenerator) {
        return applyDescendingSubstitutionOrBlock(descendingSubstitution,
                s -> super.applyDescendingSubstitution(s, constraint, child, variableGenerator), variableGenerator);
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                                               IQTree child, VariableGenerator variableGenerator) {
        return applyDescendingSubstitutionOrBlock(descendingSubstitution,
                s -> super.applyDescendingSubstitutionWithoutOptimizing(s, child, variableGenerator), variableGenerator);
    }

    private IQTree applyDescendingSubstitutionOrBlock(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                                      Function<Substitution<? extends VariableOrGroundTerm>, IQTree> applyNonBlockedSubstitutionFct,
                                                      VariableGenerator variableGenerator) {

        ImmutableSet<Variable> aggregationVariables = substitution.getDomain();

        Substitution<GroundTerm> blockedGroundTermSubstitution = descendingSubstitution.builder()
                .restrictDomainTo(aggregationVariables)
                .restrictRangeTo(GroundTerm.class)
                .build();

        Substitution<Variable> blockedVariableSubstitution = extractBlockedVar2VarSubstitutionMap(
                descendingSubstitution.restrictRangeTo(Variable.class),
                aggregationVariables);

        Sets.SetView<Variable> blockedDomain =
                Sets.union(blockedGroundTermSubstitution.getDomain(), blockedVariableSubstitution.getDomain());

        Substitution<? extends VariableOrGroundTerm> nonBlockedSubstitution = descendingSubstitution.removeFromDomain(blockedDomain);

        IQTree newSubTree = applyNonBlockedSubstitutionFct.apply(nonBlockedSubstitution);

        if (blockedDomain.isEmpty())
            return newSubTree;

        // Blocked entries -> reconverted into a filter
        ImmutableExpression condition = termFactory.getConjunction(
                Stream.concat(blockedGroundTermSubstitution.builder().toStream(termFactory::getStrictEquality),
                                blockedVariableSubstitution.builder().toStream(termFactory::getStrictEquality))
                        .collect(ImmutableCollectors.toList()));

        FilterNode filterNode = iqFactory.createFilterNode(condition);

        InjectiveSubstitution<Variable> renamingSubstitution = filterNode.getLocalVariables().stream()
                .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));

        IQTree filterTree = iqFactory.createUnaryIQTree(filterNode, newSubTree)
                .applyFreshRenaming(renamingSubstitution);

        return iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(
                        iqTreeTools.computeNewProjectedVariables(descendingSubstitution, getVariables())),
                filterTree);
    }

    /**
     * Blocks implicit equalities involving aggregation variables but let other entries (like renamings) go.
     */
    private Substitution<Variable> extractBlockedVar2VarSubstitutionMap(Substitution<Variable> descendingVar2Var,
                                                                        ImmutableSet<Variable> aggregationVariables) {
        // Variables whose entries are blocked
        ImmutableSet<Variable> blockedVariables = descendingVar2Var.getRangeSet().stream()
                .flatMap(var -> extractBlockedDomainVars(var, descendingVar2Var.getPreImage(t -> t.equals(var)), aggregationVariables).stream())
                .collect(ImmutableCollectors.toSet());

         return descendingVar2Var.restrictDomainTo(blockedVariables);
    }

    private Set<Variable> extractBlockedDomainVars(Variable rangeVariable, ImmutableSet<Variable> domainVariables,
                                                      ImmutableSet<Variable> aggregationVariables) {
        // Equalities to aggregation variable are blocked
        if (aggregationVariables.contains(rangeVariable))
            return domainVariables;

        Sets.SetView<Variable> aggregationDomainVariables = Sets.intersection(domainVariables, aggregationVariables);

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
    protected Optional<ExtendedProjectionNode> computeNewProjectionNode(ImmutableSet<Variable> newProjectedVariables,
                                                                        Substitution<ImmutableTerm> theta, IQTree newChild) {
        return Optional.of(iqFactory.createAggregationNode(
                Sets.difference(newProjectedVariables, theta.getDomain()).immutableCopy(),
                theta.transform(t -> (ImmutableFunctionalTerm)t)));
    }

    @Override
    public IQTree normalizeForOptimization(IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        return aggregationNormalizer.normalizeForOptimization(this, child, variableGenerator, treeCache);
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution, IQTree child, IQTreeCache treeCache) {
        IQTree newChild = child.applyFreshRenaming(renamingSubstitution);

        ImmutableSet<Variable> newGroupingVariables = substitutionFactory.apply(renamingSubstitution, groupingVariables);

        AggregationNode newNode = iqFactory.createAggregationNode(newGroupingVariables,
                substitutionFactory.onImmutableFunctionalTerms().rename(renamingSubstitution, substitution));

        IQTreeCache newTreeCache = treeCache.applyFreshRenaming(renamingSubstitution);
        return iqFactory.createUnaryIQTree(newNode, newChild, newTreeCache);
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
    public IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformAggregation(tree, this, child);
    }

    @Override
    public <T> IQTree acceptTransformer(IQTree tree, IQTreeExtendedTransformer<T> transformer, IQTree child, T context) {
        return transformer.transformAggregation(tree, this, child, context);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor, IQTree child) {
        return visitor.visitAggregation(this, child);
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public AggregationNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return Sets.union(getChildVariables(), substitution.getDomain()).immutableCopy();
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return getChildVariables();
    }


    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return substitution.getDomain();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AggregationNodeImpl that = (AggregationNodeImpl) o;
        return groupingVariables.equals(that.groupingVariables) && substitution.equals(that.substitution);
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
        validateNode();

        Sets.SetView<Variable> missingVariables = Sets.difference(getLocallyRequiredVariables(), child.getVariables());
        if (!missingVariables.isEmpty()) {
            throw new InvalidIntermediateQueryException("The child of the aggregation node is missing some variables: "
                    + missingVariables);
        }
    }

    protected void validateNode() throws InvalidIntermediateQueryException {
        if (!Sets.intersection(groupingVariables, substitution.getDomain()).isEmpty()) {
            throw new InvalidIntermediateQueryException(
                    String.format("AggregationNode: " +
                                    "the grouping variables (%s) and the substitution domain (%s) must be disjoint",
                            groupingVariables, substitution.getDomain()));
        }

        if (substitution.rangeAnyMatch(t -> !t.getFunctionSymbol().isAggregation())) {
            throw new InvalidIntermediateQueryException("The substitution of the aggregation node " +
                    "should only define aggregates, not " +
                    substitution.builder().restrictRange(t -> !t.getFunctionSymbol().isAggregation()).build());
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
                    ? ImmutableSet.of()
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
