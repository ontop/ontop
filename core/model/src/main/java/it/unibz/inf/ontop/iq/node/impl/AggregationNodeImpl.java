package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.AggregationNormalizer;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

public class AggregationNodeImpl extends ExtendedProjectionNodeImpl implements AggregationNode {


    private static final String AGGREGATE_NODE_STR = "AGGREGATE";

    private final ImmutableSet<Variable> projectedVariables;
    private final ImmutableSet<Variable> groupingVariables;
    private final ImmutableSubstitution<ImmutableFunctionalTerm> substitution;
    private final ImmutableSet<Variable> childVariables;
    private final AggregationNormalizer aggregationNormalizer;

    @AssistedInject
    protected AggregationNodeImpl(@Assisted ImmutableSet<Variable> groupingVariables,
                                  @Assisted ImmutableSubstitution<ImmutableFunctionalTerm> substitution,
                                  SubstitutionFactory substitutionFactory, IntermediateQueryFactory iqFactory,
                                  AggregationNormalizer aggregationNormalizer,
                                  ImmutableUnificationTools unificationTools, ConstructionNodeTools constructionNodeTools,
                                  ImmutableSubstitutionTools substitutionTools, TermFactory termFactory,
                                  CoreUtilsFactory coreUtilsFactory, OntopModelSettings settings) {
        super(substitutionFactory, iqFactory, unificationTools, constructionNodeTools, substitutionTools,
                termFactory, coreUtilsFactory);
        this.groupingVariables = groupingVariables;
        this.substitution = substitution;
        this.aggregationNormalizer = aggregationNormalizer;
        this.projectedVariables = Sets.union(groupingVariables, substitution.getDomain()).immutableCopy();
        this.childVariables = extractChildVariables(groupingVariables, substitution);

        if (settings.isTestModeEnabled())
            validateNode();
    }

    public static ImmutableSet<Variable> extractChildVariables(ImmutableSet<Variable> groupingVariables,
                                                               ImmutableSubstitution<ImmutableFunctionalTerm> substitution) {
        return Sets.union(groupingVariables,
                substitution.getImmutableMap().values().stream()
                        .flatMap(ImmutableTerm::getVariableStream)
                        .collect(ImmutableCollectors.toSet())).immutableCopy();
    }

    @Override
    public IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint, IQTree child) {
        return applyDescendingSubstitutionOrBlock(descendingSubstitution,
                s -> super.applyDescendingSubstitution(s, constraint, child));
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                                               IQTree child) {
        return applyDescendingSubstitutionOrBlock(descendingSubstitution,
                s -> super.applyDescendingSubstitutionWithoutOptimizing(s, child));
    }

    private IQTree applyDescendingSubstitutionOrBlock(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                                      Function<ImmutableSubstitution<? extends VariableOrGroundTerm>, IQTree> applyNonBlockedSubstitutionFct) {

        ImmutableSet<Variable> aggregationVariables = substitution.getDomain();

        ImmutableSubstitution<GroundTerm> blockedSubstitutionToGroundTerm = descendingSubstitution.getFragment(GroundTerm.class)
                .reduceDomainToIntersectionWith(aggregationVariables);

        ImmutableSubstitution<Variable> blockedVar2VarSubstitution = extractBlockedVar2VarSubstitutionMap(
                descendingSubstitution.getFragment(Variable.class),
                aggregationVariables);

        ImmutableSubstitution<? extends VariableOrGroundTerm> nonBlockedSubstitution = descendingSubstitution
                .reduceDomainToIntersectionWith(
                        Sets.difference(descendingSubstitution.getDomain(),
                                Sets.union(blockedSubstitutionToGroundTerm.getDomain(), blockedVar2VarSubstitution.getDomain()))
                        .immutableCopy());

        IQTree newSubTree = applyNonBlockedSubstitutionFct.apply(nonBlockedSubstitution);

        if (blockedSubstitutionToGroundTerm.isEmpty() && blockedVar2VarSubstitution.isEmpty())
            return newSubTree;

        // Blocked entries -> reconverted into a filter
        ImmutableExpression condition = termFactory.getConjunction(
                Stream.concat(
                        blockedSubstitutionToGroundTerm.getImmutableMap().entrySet().stream()
                                .map(e -> termFactory.getStrictEquality(e.getKey(), e.getValue())),
                        blockedVar2VarSubstitution.getImmutableMap().entrySet().stream()
                                .map(e -> termFactory.getStrictEquality(e.getKey(), e.getValue()))))
                .orElseThrow(() -> new MinorOntopInternalBugException("Inconsistent with the previous check"));

        FilterNode filterNode = iqFactory.createFilterNode(condition);

        InjectiveVar2VarSubstitution renamingSubstitution = substitutionFactory.getInjectiveVar2VarSubstitution(
                filterNode.getLocalVariables().stream()
                        .collect(ImmutableCollectors.toMap(
                                v -> v,
                                v -> termFactory.getVariable("v" + UUID.randomUUID().toString())
                        )));

        IQTree filterTree = iqFactory.createUnaryIQTree(filterNode, newSubTree)
                .applyFreshRenaming(renamingSubstitution);

        return iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(
                        constructionNodeTools.computeNewProjectedVariables(descendingSubstitution, getVariables())),
                filterTree);
    }

    /**
     * Blocks implicit equalities involving aggregation variables but let other entries (like renamings) go.
     */
    private ImmutableSubstitution<Variable> extractBlockedVar2VarSubstitutionMap(ImmutableSubstitution<Variable> descendingVar2Var,
                                                                                 ImmutableSet<Variable> aggregationVariables) {
        // Substitution value -> substitution keys
        ImmutableMultimap<Variable, Variable> invertedMultimap = descendingVar2Var.getImmutableMap().entrySet().stream()
                .collect(ImmutableCollectors.toMultimap(
                        Map.Entry::getValue,
                        Map.Entry::getKey));

        // Variables whose entries are blocked
        ImmutableSet<Variable> blockedVariables = invertedMultimap.asMap().entrySet().stream()
                .flatMap(e -> extractBlockedDomainVars(e.getKey(), e.getValue(), aggregationVariables))
                .collect(ImmutableCollectors.toSet());

         return descendingVar2Var.reduceDomainToIntersectionWith(blockedVariables);
    }

    private Stream<Variable> extractBlockedDomainVars(Variable rangeVariable, Collection<Variable> domainVariables,
                                                      ImmutableSet<Variable> aggregationVariables) {
        // Equalities to aggregation variable are blocked
        if (aggregationVariables.contains(rangeVariable))
            return domainVariables.stream();

        // Equalities from an aggregation variable to a grouping variable are blocked
        if (groupingVariables.contains(rangeVariable))
            return domainVariables.stream()
                    .filter(aggregationVariables::contains);

        // Fresh variables: need at least one variable to become projected
        // the latter may be an aggregation variable if there is no grouping variable
        Variable dominantVariable = domainVariables.stream()
                .filter(groupingVariables::contains)
                .findAny()
                .orElseGet(() -> domainVariables.iterator().next());
        return domainVariables.stream()
                .filter(v -> aggregationVariables.contains(v) && (!dominantVariable.equals(v)));
    }

    @Override
    protected Optional<ExtendedProjectionNode> computeNewProjectionNode(ImmutableSet<Variable> newProjectedVariables,
                                                                        ImmutableSubstitution<ImmutableTerm> theta, IQTree newChild) {
        return Optional.of(iqFactory.createAggregationNode(
                Sets.difference(newProjectedVariables, theta.getDomain()).immutableCopy(),
                (ImmutableSubstitution<ImmutableFunctionalTerm>) (ImmutableSubstitution<?>)theta));
    }

    @Override
    public IQTree normalizeForOptimization(IQTree child, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        return aggregationNormalizer.normalizeForOptimization(this, child, variableGenerator,
                currentIQProperties);
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveVar2VarSubstitution renamingSubstitution, IQTree child, IQTreeCache treeCache) {
        IQTree newChild = child.applyFreshRenaming(renamingSubstitution);

        ImmutableSet<Variable> newGroupingVariables = groupingVariables.stream()
                .map(renamingSubstitution::applyToVariable)
                .collect(ImmutableCollectors.toSet());

        AggregationNode newNode = iqFactory.createAggregationNode(newGroupingVariables,
                renamingSubstitution.applyRenaming(substitution));

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
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return Optional.of(node)
                .filter(n -> n instanceof AggregationNode)
                .map(n -> (AggregationNode) n)
                .filter(n -> n.getGroupingVariables().equals(groupingVariables))
                .filter(n -> n.getSubstitution().equals(substitution))
                .isPresent();
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
    public boolean isEquivalentTo(QueryNode queryNode) {
        return isSyntacticallyEquivalentTo(queryNode);
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

        ImmutableMap<Variable, ImmutableFunctionalTerm> nonAggregateMap = substitution.getImmutableMap().entrySet().stream()
                .filter(e -> !e.getValue().getFunctionSymbol().isAggregation())
                .collect(ImmutableCollectors.toMap());
        if (!nonAggregateMap.isEmpty()) {
            throw new InvalidIntermediateQueryException("The substitution of the aggregation node " +
                    "should only define aggregates, not " + nonAggregateMap);
        }
    }


    @Override
    public ImmutableSet<ImmutableSubstitution<NonVariableTerm>> getPossibleVariableDefinitions(IQTree child) {

        ImmutableSet<ImmutableSubstitution<NonVariableTerm>> groupingVariableDefs = child.getPossibleVariableDefinitions().stream()
                .map(s -> s.reduceDomainToIntersectionWith(groupingVariables))
                .collect(ImmutableCollectors.toSet());

        ImmutableSubstitution<NonVariableTerm> def = substitution.getFragment(NonVariableTerm.class);

        if (groupingVariableDefs.isEmpty()) {
            return def.isEmpty()
                    ? ImmutableSet.of()
                    : ImmutableSet.of(def);
        }

        // For Aggregation functional terms, we don't look further on for child definitions
        return groupingVariableDefs.stream()
                .map(childDef -> childDef.union(def).get())
                .collect(ImmutableCollectors.toSet());
    }

    /**
     * By default, blocks the distinct removal
     * TODO: detect when we can do it (absence of cardinality-sensitive aggregation functions)
     */
    @Override
    public IQTree removeDistincts(IQTree child, IQProperties iqProperties) {
        return iqFactory.createUnaryIQTree(this, child, iqProperties.declareDistinctRemovalWithoutEffect());
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

    /**
     * Out of the projected variables, only the grouping variables are required
     */
    @Override
    public ImmutableSet<Variable> computeNotInternallyRequiredVariables(IQTree child) {
        return substitution.getImmutableMap().keySet();
    }

    @Override
    public ImmutableSubstitution<ImmutableFunctionalTerm> getSubstitution() {
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
    public AggregationNode clone() {
        throw new IllegalStateException("AggregationNode::clone");
    }

    @Override
    public String toString() {
        // TODO: display the query modifiers
        return AGGREGATE_NODE_STR + " " + groupingVariables + " " + "[" + substitution + "]" ;
    }
}
