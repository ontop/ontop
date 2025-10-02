package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.SliceNormalizer;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.*;


public class SliceNodeImpl extends QueryModifierNodeImpl implements SliceNode {

    private static final String SLICE_STR = "SLICE";

    private final long offset;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final OptionalLong limit;

    private final IQTreeTools iqTreeTools;
    private final SliceNormalizer sliceNormalizer;

    @AssistedInject
    private SliceNodeImpl(@Assisted("offset") long offset, @Assisted("limit") long limit,
                          IntermediateQueryFactory iqFactory, SliceNormalizer sliceNormalizer, TermFactory termFactory, IQTreeTools iqTreeTools) {
        super(iqFactory, termFactory);
        this.iqTreeTools = iqTreeTools;
        if (offset < 0)
            throw new IllegalArgumentException("The offset must not be negative");
        if (limit < 0)
            throw new IllegalArgumentException("The limit must not be negative");
        this.offset = offset;
        this.limit = OptionalLong.of(limit);
        this.sliceNormalizer = sliceNormalizer;
    }

    @AssistedInject
    private SliceNodeImpl(@Assisted long offset, IntermediateQueryFactory iqFactory, SliceNormalizer sliceNormalizer, TermFactory termFactory, IQTreeTools iqTreeTools) {
        super(iqFactory, termFactory);
        this.iqTreeTools = iqTreeTools;
        if (offset < 0)
            throw new IllegalArgumentException("The offset must not be negative");
        this.offset = offset;
        this.limit = OptionalLong.empty();
        this.sliceNormalizer = sliceNormalizer;
    }

    /**
     * Does not lift unions, blocks them
     */
    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child, VariableGenerator variableGenerator) {
        return iqFactory.createUnaryIQTree(this, child);
    }

    @Override
    public IQTree normalizeForOptimization(IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        return sliceNormalizer.normalizeForOptimization(this, child, variableGenerator, treeCache);
    }

    /**
     * Stops constraints
     */
    @Override
    public IQTree propagateDownConstraint(DownPropagation dp, IQTree child) {
        return iqFactory.createUnaryIQTree(this, child);
    }

    @Override
    public IQTree applyDescendingSubstitution(DownPropagation dp, IQTree child) {
        return iqFactory.createUnaryIQTree(this, dp.propagateToChild(child));
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                                               IQTree child, VariableGenerator variableGenerator) {
        return iqFactory.createUnaryIQTree(this,
                iqTreeTools.applyDownPropagationWithoutOptimization(child, descendingSubstitution, variableGenerator));
    }

    @Override
    public SliceNode applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution) {
        return this;
    }

    @Override
    public boolean isDistinct(IQTree tree, IQTree child) {
        if (limit.isPresent() && limit.getAsLong() <= 1)
            return true;
        return child.isDistinct();
    }

    @Override
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {
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

    @Override
    public VariableNonRequirement computeVariableNonRequirement(IQTree child) {
        return child.getVariableNonRequirement();
    }

    @Override
    public ImmutableSet<Variable> inferStrictDependents(UnaryIQTree tree, IQTree child) {
        return child.inferStrictDependents();
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return ImmutableSet.of();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof SliceNodeImpl) {
            SliceNodeImpl that = (SliceNodeImpl) o;
            return offset == that.offset && limit.equals(that.limit);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, limit);
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public OptionalLong getLimit() {
        return limit;
    }

    @Override
    public String toString() {
        return SLICE_STR
                + (offset > 0 ? " offset=" + offset : "")
                + (limit.isEmpty() ? "" : " limit=" + limit);
    }
}
