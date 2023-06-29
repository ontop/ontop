package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.node.normalization.OrderByNormalizer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class OrderByNodeImpl extends QueryModifierNodeImpl implements OrderByNode {

    private static final String ORDER_BY_NODE_STR = "ORDER BY";

    private final ImmutableList<OrderComparator> comparators;
    private final OrderByNormalizer normalizer;
    private final SubstitutionFactory substitutionFactory;


    @AssistedInject
    private OrderByNodeImpl(@Assisted ImmutableList<OrderComparator> comparators, IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory,
                            OrderByNormalizer normalizer) {
        super(iqFactory);
        this.substitutionFactory = substitutionFactory;
        this.comparators = comparators;
        this.normalizer = normalizer;
    }

    @Override
    public ImmutableList<OrderComparator> getComparators() {
        return comparators;
    }

    @Override
    public Optional<OrderByNode> applySubstitution(Substitution<? extends ImmutableTerm> substitution) {
        ImmutableList<OrderComparator> newComparators = comparators.stream()
                .flatMap(c -> Stream.of(substitution.applyToTerm(c.getTerm()))
                        .filter(t -> t instanceof NonGroundTerm)
                        .map(t -> iqFactory.createOrderComparator((NonGroundTerm) t, c.isAscending())))
                .collect(ImmutableCollectors.toList());

        return Optional.of(newComparators)
                .filter(cs -> !cs.isEmpty())
                .map(iqFactory::createOrderByNode);
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child, VariableGenerator variableGenerator) {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public IQTree normalizeForOptimization(IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        return normalizer.normalizeForOptimization(this, child, variableGenerator, treeCache);
    }

    @Override
    public IQTree applyDescendingSubstitution(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint, IQTree child, VariableGenerator variableGenerator) {

        Optional<OrderByNode> newOrderByNode = applySubstitution(descendingSubstitution);
        IQTree newChild = child.applyDescendingSubstitution(descendingSubstitution, constraint, variableGenerator);

        return newOrderByNode
                .map(o -> (IQTree) iqFactory.createUnaryIQTree(o, newChild))
                .orElse(newChild);
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution, IQTree child, VariableGenerator variableGenerator) {

        Optional<OrderByNode> newOrderByNode = applySubstitution(descendingSubstitution);
        IQTree newChild = child.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution, variableGenerator);

        return newOrderByNode
                .map(o -> (IQTree) iqFactory.createUnaryIQTree(o, newChild))
                .orElse(newChild);
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution, IQTree child, IQTreeCache treeCache) {
        IQTree newChild = child.applyFreshRenaming(renamingSubstitution);

        OrderByNode newOrderByNode = applySubstitution(renamingSubstitution)
                .orElseThrow(() -> new MinorOntopInternalBugException("The order by was expected to be kept"));

        IQTreeCache newTreeCache = treeCache.applyFreshRenaming(renamingSubstitution);
        return iqFactory.createUnaryIQTree(newOrderByNode, newChild, newTreeCache);
    }

    @Override
    public boolean isDistinct(IQTree tree, IQTree child) {
        return child.isDistinct();
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformOrderBy(tree, this, child);
    }

    @Override
    public <T> IQTree acceptTransformer(IQTree tree, IQTreeExtendedTransformer<T> transformer, IQTree child, T context) {
        return transformer.transformOrderBy(tree, this, child, context);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor, IQTree child) {
        return visitor.visitOrderBy(this, child);
    }

    @Override
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {
        if (!child.getVariables().containsAll(getLocalVariables())) {
            throw new InvalidIntermediateQueryException("Some variables used in the node " + this
                    + " are not provided by its child " + child);
        }
    }

    @Override
    public IQTree removeDistincts(IQTree child, IQTreeCache treeCache) {
        IQTree newChild = child.removeDistincts();
        IQTreeCache newTreeCache = treeCache.declareDistinctRemoval(newChild.equals(child));
        return iqFactory.createUnaryIQTree(this, newChild, newTreeCache);
    }

    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(IQTree child) {
        return child.inferUniqueConstraints();
    }

    @Override
    public FunctionalDependencies inferFunctionalDependencies(IQTree child, ImmutableSet<ImmutableSet<Variable>> uniqueConstraints, ImmutableSet<Variable> variables) {
        return child.inferFunctionalDependencies();
    }

    /**
     * Subtracts from the variables proposed by the child the one used for ordering
     */
    @Override
    public VariableNonRequirement computeVariableNonRequirement(IQTree child) {
        ImmutableSet<Variable> localVariables = getLocalVariables();

        return child.getVariableNonRequirement()
                .filter((v, conds) -> !localVariables.contains(v));
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public OrderByNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return comparators.stream()
                .flatMap(c -> c.getTerm().getVariableStream())
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return getLocalVariables();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return ImmutableSet.of();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderByNodeImpl that = (OrderByNodeImpl) o;
        return comparators.equals(that.comparators);
    }

    @Override
    public int hashCode() {
        return Objects.hash(comparators);
    }

    @Override
    public String toString() {
        return ORDER_BY_NODE_STR + " " + comparators;
    }
}
