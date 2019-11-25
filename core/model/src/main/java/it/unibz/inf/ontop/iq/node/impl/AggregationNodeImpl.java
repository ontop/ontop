package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.AggregationNormalizer;
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

import java.util.Optional;
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
    public IQTree applyFreshRenaming(InjectiveVar2VarSubstitution renamingSubstitution, IQTree child,
                                     IQProperties iqProperties, Optional<VariableNullability> currentVariableNullability) {
        IQTree newChild = child.applyFreshRenaming(renamingSubstitution);

        ImmutableSet<Variable> newGroupingVariables = groupingVariables.stream()
                .map(renamingSubstitution::applyToVariable)
                .collect(ImmutableCollectors.toSet());

        AggregationNode newNode = iqFactory.createAggregationNode(newGroupingVariables,
                renamingSubstitution.applyRenaming(substitution));

        return currentVariableNullability
                .map(v -> v.update(renamingSubstitution))
                .map(v -> iqFactory.createUnaryIQTree(newNode, newChild, v, iqProperties))
                .orElseGet(() -> iqFactory.createUnaryIQTree(newNode, newChild, iqProperties));
    }


    @Override
    public boolean isDistinct(IQTree child) {
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
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        // TODO: implement seriously!
        return true;
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
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        return getLocallyRequiredVariables();
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

        if (groupingVariableDefs.isEmpty()) {
            ImmutableSubstitution<NonVariableTerm> def = substitution.getNonVariableTermFragment();
            return def.isEmpty()
                    ? ImmutableSet.of()
                    : ImmutableSet.of(def);
        }

        // For Aggregation functional terms, we don't look further on for child definitions
        return groupingVariableDefs.stream()
                .map(childDef -> (ImmutableSubstitution<ImmutableTerm>)(ImmutableSubstitution<?>) childDef)
                .map(childDef -> childDef.union((ImmutableSubstitution<ImmutableTerm>)(ImmutableSubstitution<?>) substitution).get())
                .map(ImmutableSubstitution::getNonVariableTermFragment)
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
        return iqFactory.createAggregationNode(groupingVariables, substitution);
    }

    @Override
    public String toString() {
        // TODO: display the query modifiers
        return AGGREGATE_NODE_STR + " " + groupingVariables + " " + "[" + substitution + "]" ;
    }
}
