package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.DistinctNormalizer;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;


public class DistinctNodeImpl extends QueryModifierNodeImpl implements DistinctNode {

    private static final String DISTINCT_NODE_STR = "DISTINCT";
    private final DistinctNormalizer normalizer;

    @Inject
    private DistinctNodeImpl(IntermediateQueryFactory iqFactory, DistinctNormalizer normalizer) {
        super(iqFactory);
        this.normalizer = normalizer;
    }

    @Override
    public IQTree normalizeForOptimization(IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        return normalizer.normalizeForOptimization(this, child, variableGenerator, treeCache);
    }

    /**
     * TODO: implement it seriously! (is currently blocking)
     */
    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child, VariableGenerator variableGenerator) {
        // TODO: stop blocking
        return iqFactory.createUnaryIQTree(this, child);
    }

    @Override
    public IQTree applyDescendingSubstitution(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint, IQTree child,
                                              VariableGenerator variableGenerator) {
        return iqFactory.createUnaryIQTree(this,
                child.applyDescendingSubstitution(descendingSubstitution, constraint, variableGenerator));
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution, IQTree child,
            VariableGenerator variableGenerator) {
        return iqFactory.createUnaryIQTree(this,
                child.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution, variableGenerator));
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution, IQTree child, IQTreeCache treeCache) {
        IQTree newChild = child.applyFreshRenaming(renamingSubstitution);
        IQTreeCache newTreeCache = treeCache.applyFreshRenaming(renamingSubstitution);
        return iqFactory.createUnaryIQTree(this, newChild, newTreeCache);
    }

    @Override
    public boolean isDistinct(IQTree tree, IQTree child) {
        return true;
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformDistinct(tree, this, child);
    }

    @Override
    public <T> IQTree acceptTransformer(IQTree tree, IQTreeExtendedTransformer<T> transformer, IQTree child, T context) {
        return transformer.transformDistinct(tree, this, child, context);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor, IQTree child) {
        return visitor.visitDistinct(this, child);
    }

    @Override
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {
    }

    @Override
    public IQTree removeDistincts(IQTree child, IQTreeCache treeCache) {
        return child.removeDistincts();
    }

    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(IQTree child) {
        var childConstraints = child.inferUniqueConstraints();
        return childConstraints.isEmpty()
                ? ImmutableSet.of(inferNewUC(child))
                : childConstraints;
    }

    private ImmutableSet<Variable> inferNewUC(IQTree child) {
        /*
         * NB: by using strict dependents, we are including in the UC intermediate determinants like b in a -> b -> c .
         * The benefit is that it is usually much cheaper to compute strict dependents than functional dependencies
         * (especially for large UNIONs).
         * We are still waiting for a real case where eliminating b in this example has a significant impact.
         */
        return Sets.difference(child.getVariables(), child.inferStrictDependents())
                .immutableCopy();
    }

    @Override
    public FunctionalDependencies inferFunctionalDependencies(IQTree child, ImmutableSet<ImmutableSet<Variable>> uniqueConstraints, ImmutableSet<Variable> variables) {
        return child.inferFunctionalDependencies();
    }

    @Override
    public VariableNonRequirement computeVariableNonRequirement(IQTree child) {
        var childVariableNonRequirement = child.getVariableNonRequirement();

        if (childVariableNonRequirement.isEmpty())
            return childVariableNonRequirement;

        ImmutableSet<Variable> requiredByDistinct = inferNewUC(child);
        if (requiredByDistinct.isEmpty())
            return childVariableNonRequirement;

        return childVariableNonRequirement
                .filter((v, conds) -> !requiredByDistinct.contains(v));
    }

    @Override
    public ImmutableSet<Variable> inferStrictDependents(UnaryIQTree tree, IQTree child) {
        return child.inferStrictDependents();
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public DistinctNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return ImmutableSet.of();
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
    public int hashCode() {
        return 238723871;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof DistinctNodeImpl;
    }

    @Override
    public String toString() {
        return DISTINCT_NODE_STR;
    }
}
