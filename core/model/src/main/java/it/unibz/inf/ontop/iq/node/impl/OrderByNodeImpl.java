package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.iq.node.normalization.OrderByNormalizer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Objects;
import java.util.Optional;

public class OrderByNodeImpl extends QueryModifierNodeImpl implements OrderByNode {

    private static final String ORDER_BY_NODE_STR = "ORDER BY";

    private final ImmutableList<OrderComparator> comparators;
    private final OrderByNormalizer normalizer;
    private final IQTreeTools iqTreeTools;
    private final SubstitutionFactory substitutionFactory;


    @AssistedInject
    private OrderByNodeImpl(@Assisted ImmutableList<OrderComparator> comparators, IntermediateQueryFactory iqFactory,
                            OrderByNormalizer normalizer, IQTreeTools iqTreeTools, SubstitutionFactory substitutionFactory) {
        super(iqFactory);
        this.comparators = comparators;
        this.normalizer = normalizer;
        this.iqTreeTools = iqTreeTools;
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public ImmutableList<OrderComparator> getComparators() {
        return comparators;
    }

    @Override
    public Optional<OrderByNode> applySubstitution(Substitution<? extends ImmutableTerm> substitution) {
        ImmutableList<OrderComparator> newComparators = iqTreeTools.transformComparators(comparators, substitution::applyToTerm);
        return iqTreeTools.createOptionalOrderByNode(Optional.of(newComparators));
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

        return iqTreeTools.unaryIQTreeBuilder()
                .append(applySubstitution(descendingSubstitution))
                .build(child.applyDescendingSubstitution(descendingSubstitution, constraint, variableGenerator));
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution, IQTree child, VariableGenerator variableGenerator) {

        return iqTreeTools.unaryIQTreeBuilder()
                .append(applySubstitution(descendingSubstitution))
                .build(child.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution, variableGenerator));
    }

    @Override
    public OrderByNode applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution) {
        var f = substitutionFactory.onNonGroundTerms();
        ImmutableList<OrderByNode.OrderComparator> newComparators = iqTreeTools.transformComparators(
                comparators, t -> f.rename(renamingSubstitution, t));

        return iqFactory.createOrderByNode(newComparators);
    }

    @Override
    public boolean isDistinct(IQTree tree, IQTree child) {
        return child.isDistinct();
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
        return child.getVariableNonRequirement().withRequiredVariables(getLocallyRequiredVariables());
    }

    @Override
    public ImmutableSet<Variable> inferStrictDependents(UnaryIQTree tree, IQTree child) {
        return child.inferStrictDependents();
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return comparators.stream()
                .map(OrderComparator::getTerm)
                .flatMap(ImmutableTerm::getVariableStream)
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return ImmutableSet.of();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof OrderByNodeImpl) {
            OrderByNodeImpl that = (OrderByNodeImpl) o;
            return comparators.equals(that.comparators);
        }
        return false;
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
