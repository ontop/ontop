package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.FlattenNormalizer;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.GenericDBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.substitution.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Objects;
import java.util.Optional;

public class FlattenNodeImpl extends CompositeQueryNodeImpl implements FlattenNode {

    private final Variable flattenedVariable;
    private final Variable outputVariable;
    private final Optional<Variable> indexVariable;
    private final DBTermType flattenedType;
    private final FlattenNormalizer normalizer;

    @AssistedInject
    private FlattenNodeImpl(@Assisted("outputVariable") Variable outputVariable,
                            @Assisted("flattenedVariable") Variable flattenedVariable,
                            @Assisted Optional<Variable> indexVariable,
                            @Assisted DBTermType flattenedType,
                            SubstitutionFactory substitutionFactory,
                            IntermediateQueryFactory iqFactory,
                            TermFactory termFactory,
                            FlattenNormalizer normalizer,
                            IQTreeTools iqTreeTools) {
        super(substitutionFactory, termFactory, iqFactory, iqTreeTools);
        this.outputVariable = outputVariable;
        this.flattenedVariable = flattenedVariable;
        this.indexVariable = indexVariable;
        this.flattenedType = flattenedType;
        this.normalizer = normalizer;
    }

    @Override
    public Variable getFlattenedVariable() {
        return flattenedVariable;
    }

    @Override
    public DBTermType getFlattenedType() {
        return flattenedType;
    }

    @Override
    public Variable getOutputVariable() {
        return outputVariable;
    }

    @Override
    public Optional<Variable> getIndexVariable() {
        return indexVariable;
    }

    @Override
    public Optional<TermType> inferOutputType(Optional<TermType> flattenedVarType) {
        return flattenedVarType
                .filter(t -> t instanceof DBTermType)
                .map(t -> (DBTermType) t)
                .flatMap(t -> {
                    switch (t.getCategory()){
                        case JSON:
                            //e.g. STRING is used by SparkSQL instead of JSON.
                        case STRING:
                            return flattenedVarType;
                        case ARRAY:
                            return Optional.of(((GenericDBTermType)t).getGenericArguments().get(0));
                        default:
                            return Optional.empty();
                    }
                });
    }

    @Override
    public Optional<TermType> getIndexVariableType() {
        return Optional.of(termFactory.getTypeFactory().getDBTypeFactory().getDBLargeIntegerType());
    }

    @Override
    public ImmutableSet<Variable> getVariables(ImmutableSet<Variable> childVariables) {
        return Sets.union(
                        Sets.difference(childVariables, getLocalVariables()),
                        getLocallyDefinedVariables())
                .immutableCopy();
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return ImmutableSet.of(flattenedVariable);
    }

    @Override
    public String toString() {
        return "FLATTEN  [" +
                outputVariable + "/flatten(" + flattenedVariable + ")" +
                indexVariable.map(v -> ", " + v + "/indexIn(" + flattenedVariable + ")").orElse("") +
                "]";
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return ImmutableSet.of(flattenedVariable);
    }

    @Override
    public IQTree normalizeForOptimization(IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        return normalizer.normalizeForOptimization(this, child, variableGenerator, treeCache);
    }

    @Override
    public IQTree applyDescendingSubstitution(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint, IQTree child,
                                              VariableGenerator variableGenerator) {
        return applyDescendingSubstitution(descendingSubstitution, constraint, child, variableGenerator,
                IQTree::applyDescendingSubstitution);
    }

    private IQTree applyDescendingSubstitution(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                               Optional<ImmutableExpression> constraint, IQTree child,
                                               VariableGenerator variableGenerator, PropagateToChild propagateToChild) {
        Substitution<GroundTerm> blockedSubstitution = descendingSubstitution
                .restrictRangeTo(GroundTerm.class)
                .restrictDomainTo(extendWithIndexVariable(ImmutableSet.of(outputVariable, flattenedVariable)));

        InjectiveSubstitution<Variable> renaming = blockedSubstitution.getDomain().stream()
                .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));

        Substitution<? extends VariableOrGroundTerm> newDescendingSubstitution = blockedSubstitution.isEmpty()
                ? descendingSubstitution
                : substitutionFactory.union(
                renaming,
                descendingSubstitution.removeFromDomain(blockedSubstitution.getDomain()));

        UnaryIQTree flattenTree = iqFactory.createUnaryIQTree(
                applySubstitution(newDescendingSubstitution),
                propagateToChild.apply(child, newDescendingSubstitution, constraint, variableGenerator));

        if (blockedSubstitution.isEmpty())
            return flattenTree;

        Substitution<?> renamedBlockedSubstitution = substitutionFactory.rename(renaming, blockedSubstitution);

        ImmutableExpression condition = termFactory.getConjunction(
                renamedBlockedSubstitution.builder().toStream(termFactory::getStrictEquality).collect(ImmutableCollectors.toList()));

        IQTree filterTree = iqFactory.createUnaryIQTree(iqFactory.createFilterNode(condition), flattenTree);

        return iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(
                        Sets.difference(filterTree.getVariables(), renamedBlockedSubstitution.getDomain())
                                .immutableCopy()),
                filterTree);
    }

    private Variable applySubstitution(Variable var, Substitution<? extends VariableOrGroundTerm> sub) {
        VariableOrGroundTerm newVar = substitutionFactory.onVariableOrGroundTerms().apply(sub, var);
        if (!(newVar instanceof Variable))
            throw new InvalidIntermediateQueryException("This substitution application should yield a variable");

        return (Variable) newVar;
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                                               IQTree child, VariableGenerator variableGenerator) {

        return applyDescendingSubstitution(descendingSubstitution, Optional.empty(), child, variableGenerator,
                (c, s, constraint, vGenerator) -> c.applyDescendingSubstitutionWithoutOptimizing(s, vGenerator));
    }

    @Override
    public boolean isConstructed(Variable variable, IQTree child) {
        return child.isConstructed(variable);
    }

    @Override
    public ImmutableSet<Substitution<NonVariableTerm>> getPossibleVariableDefinitions(IQTree child) {
        return child.getPossibleVariableDefinitions();
    }

    /**
     * Same implementation as FilterNode
     */
    @Override
    public IQTree removeDistincts(IQTree child, IQTreeCache treeCache) {
        IQTree newChild = child.removeDistincts();
        IQTreeCache newTreeCache = treeCache.declareDistinctRemoval(newChild.equals(child));
        return iqFactory.createUnaryIQTree(this, newChild, newTreeCache);
    }

    private ImmutableSet<Variable> extendWithIndexVariable(ImmutableSet<Variable> set) {
        return indexVariable.map(index -> Sets.union(set, ImmutableSet.of(index)).immutableCopy()).orElse(set);
    }

    /**
     * Unique constraints are lost after flattening
     */
    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(IQTree child) {
        //If there is no index variable, we cannot infer unique constraints.
        if (indexVariable.isEmpty())
            return ImmutableSet.of();

        ImmutableSet<ImmutableSet<Variable>> childConstraints = child.inferUniqueConstraints();
        return childConstraints.stream()
                .map(this::extendWithIndexVariable)
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public FunctionalDependencies inferFunctionalDependencies(IQTree child, ImmutableSet<ImmutableSet<Variable>> uniqueConstraints, ImmutableSet<Variable> variables) {
        FunctionalDependencies childFDs = child.inferFunctionalDependencies();
        if (indexVariable.isEmpty())
            return childFDs;

        //if FD A -> B exists, and B contains the flattened field, then there is a FD (A, index) -> output.
        return childFDs.stream()
                .filter(fd -> fd.getValue().contains(flattenedVariable))
                .map(fd -> Maps.immutableEntry(extendWithIndexVariable(fd.getKey()), ImmutableSet.of(outputVariable)))
                .collect(FunctionalDependencies.toFunctionalDependencies())
                .concat(childFDs)
                .concat(FunctionalDependencies.fromUniqueConstraints(uniqueConstraints, variables));
    }

    /**
     * Only the flattened variable is required
     */
    @Override
    public VariableNonRequirement computeVariableNonRequirement(IQTree child) {
        return child.getVariableNonRequirement()
                .filter((v, conds) -> !v.equals(flattenedVariable));
    }

    @Override
    public ImmutableSet<Variable> inferStrictDependents(UnaryIQTree tree, IQTree child) {
        return IQTreeTools.computeStrictDependentsFromFunctionalDependencies(tree);
    }

    @Override
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {
        if (!child.getVariables().contains(flattenedVariable)) {
            throw new InvalidIntermediateQueryException(String.format(
                    "Variable %s is flattened by Node %s but is not projected by its child",
                    flattenedVariable, this));
        }
    }

    /**
     * Assumption: a flattened array can contain null values.
     * <p>
     * If so, even a relaxed flatten has no incidence on variable nullability
     * (a tuple may map the output variable to null, and the position variable to a non-null value)
     */
    @Override
    public VariableNullability getVariableNullability(IQTree child) {
        return child.getVariableNullability().extendToExternalVariables(getLocallyDefinedVariables().stream());
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child, VariableGenerator variableGenerator) {
        return iqFactory.createUnaryIQTree(this, child.propagateDownConstraint(constraint, variableGenerator));
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return extendWithIndexVariable(ImmutableSet.of(outputVariable));
    }

    @Override
    public boolean isDistinct(IQTree tree, IQTree child) {
        return false;
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child, VariableGenerator variableGenerator) {
        return iqTreeTools.liftIncompatibleDefinitions(this, variable, child, variableGenerator);
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution, IQTree child, IQTreeCache treeCache) {
        IQTree newChild = child.applyFreshRenaming(renamingSubstitution);
        IQTreeCache newTreeCache = treeCache.applyFreshRenaming(renamingSubstitution);
        return iqFactory.createUnaryIQTree(applySubstitution(renamingSubstitution), newChild, newTreeCache);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof FlattenNodeImpl) {
            FlattenNodeImpl that = (FlattenNodeImpl) o;
            return flattenedVariable.equals(that.flattenedVariable) &&
                    outputVariable.equals(that.outputVariable) &&
                    indexVariable.equals(that.indexVariable);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(flattenedVariable, outputVariable, indexVariable);
    }

    @Override
    public boolean wouldKeepDescendingGroundTermInFilterAbove(Variable variable, boolean isConstant) {
        return getLocallyDefinedVariables().contains(variable);
    }

    /**
     * Avoids creating an instance if unnecessary (a similar optimization is implemented for Filter Nodes)
     */
    private FlattenNode applySubstitution(Substitution<? extends VariableOrGroundTerm> sub) {
        Variable sFlattenedVar = applySubstitution(flattenedVariable, sub);
        Variable sOutputVar = applySubstitution(outputVariable, sub);
        Optional<Variable> sIndexVar = indexVariable.map(index -> applySubstitution(index, sub));
        return sFlattenedVar.equals(flattenedVariable) &&
                sOutputVar.equals(outputVariable) &&
                sIndexVar.equals(indexVariable)
                ? this
                : iqFactory.createFlattenNode(sOutputVar, sFlattenedVar, sIndexVar, flattenedType);
    }

    @FunctionalInterface
    interface PropagateToChild {

        IQTree apply(IQTree child, Substitution<? extends VariableOrGroundTerm> substitution,
                     Optional<ImmutableExpression> optionalConstraint, VariableGenerator variableGenerator);
    }
}

