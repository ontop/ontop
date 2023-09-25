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
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.FlattenNormalizer;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.GenericDBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.substitution.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

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
                            FlattenNormalizer normalizer) {
        super(substitutionFactory, termFactory, iqFactory);
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
        Optional<DBTermType> optTermType = flattenedVarType
                .filter(t -> t instanceof DBTermType)
                .map(t -> (DBTermType) t);
        if(optTermType.isPresent()){
            DBTermType type = optTermType.get();
            switch (type.getCategory()){
                case JSON:
                //e.g. STRING is used by SparkSQL instead of JSON.
                case STRING:
                    return flattenedVarType;
                case ARRAY:
                    return Optional.of(((GenericDBTermType)type).getGenericArguments().get(0));
                default:
                    return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<TermType> getIndexVariableType() {
        return Optional.of(termFactory.getTypeFactory().getDBTypeFactory().getDBLargeIntegerType());
    }

    @Override
    public ImmutableSet<Variable> getVariables(ImmutableSet<Variable> childVariables) {
        return
        Stream.concat(
                childVariables.stream()
                        .filter(v -> v != flattenedVariable),
                getLocallyDefinedVariables().stream()
        ).collect(ImmutableCollectors.toSet());
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
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
        var blockedSubstitution = descendingSubstitution
                .restrictRangeTo(GroundTerm.class)
                .restrictDomainTo(Stream.concat(Stream.of(outputVariable, flattenedVariable), indexVariable.stream())
                        .collect(ImmutableCollectors.toSet()));

        var renaming = blockedSubstitution.getDomain().stream()
                .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));

        var newDescendingSubstitution = blockedSubstitution.isEmpty()
                ? descendingSubstitution
                : substitutionFactory.union(
                renaming,
                descendingSubstitution.removeFromDomain(blockedSubstitution.getDomain()));

        var flattenTree = iqFactory.createUnaryIQTree(
                applySubstitution(newDescendingSubstitution),
                propagateToChild.apply(child, newDescendingSubstitution, constraint, variableGenerator));

        var renamedBlockedSubstitution = substitutionFactory.rename(renaming, blockedSubstitution);

        var filterNode = termFactory.getConjunction(renamedBlockedSubstitution.stream()
                        .map(e -> termFactory.getStrictEquality(e.getKey(), e.getValue())))
                .map(iqFactory::createFilterNode);

        return filterNode
                .map(f -> iqFactory.createUnaryIQTree(f, flattenTree))
                .map(t -> iqFactory.createUnaryIQTree(
                        iqFactory.createConstructionNode(
                                Sets.difference(t.getVariables(), renamedBlockedSubstitution.getDomain())
                                        .immutableCopy()),
                        t))
                .orElse(flattenTree);
    }

    protected Variable applySubstitution(Variable var, Substitution<? extends VariableOrGroundTerm> sub) {
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
        return ImmutableSet.of();
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

    /**
     * Unique constraints are lost after flattening
     */
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(IQTree child) {
        //If there is no index variable, we cannot infer unique constraints.
        if(indexVariable.isEmpty())
            return ImmutableSet.of();

        var childConstraints = child.inferUniqueConstraints();
        return childConstraints.stream()
                .map(constraint -> Stream.concat(constraint.stream(), Stream.of(indexVariable.get()))
                            .collect(ImmutableCollectors.toSet()))
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public FunctionalDependencies inferFunctionalDependencies(IQTree child, ImmutableSet<ImmutableSet<Variable>> uniqueConstraints, ImmutableSet<Variable> variables) {
        var childFDs = child.inferFunctionalDependencies();
        if(indexVariable.isEmpty())
            return childFDs;
        var index = indexVariable.get();
        //if FD A -> B exists, and B contains the flattened field, then there is a FD (A, index) -> output.
        return childFDs.stream()
                .filter(fd -> fd.getValue().contains(flattenedVariable))
                .map(fd -> Maps.immutableEntry(Sets.union(fd.getKey(), ImmutableSet.of(index)).immutableCopy(), ImmutableSet.of(outputVariable)))
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
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {
        if (!child.getVariables().contains(flattenedVariable)) {
            throw new InvalidIntermediateQueryException(
                    String.format( "Variable %s is flattened by Node %s but is not projected by its child",
                            flattenedVariable,
                            this
                    ));
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

        return child.getVariableNullability().extendToExternalVariables(
                Stream.of(Optional.of(outputVariable), indexVariable)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
        );
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child, VariableGenerator variableGenerator) {
        return iqFactory.createUnaryIQTree(this, child.propagateDownConstraint(constraint, variableGenerator));
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        ImmutableSet.Builder<Variable> builder = ImmutableSet.builder();
        builder.add(outputVariable);
        indexVariable.ifPresent(builder::add);
        return builder.build();
    }

    @Override
    public boolean isDistinct(IQTree tree, IQTree child) {
        return false;
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child, VariableGenerator variableGenerator) {
        IQTree newChild = child.liftIncompatibleDefinitions(variable, variableGenerator);
        QueryNode newChildRoot = newChild.getRootNode();

        /*
         * Lift the union above the flatten
         */
        if ((newChildRoot instanceof UnionNode)
                && ((UnionNode) newChildRoot).hasAChildWithLiftableDefinition(variable, newChild.getChildren())) {
            UnionNode unionNode = (UnionNode) newChildRoot;
            ImmutableList<IQTree> grandChildren = newChild.getChildren();

            ImmutableList<IQTree> newChildren = grandChildren.stream()
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(this, c))
                    .collect(ImmutableCollectors.toList());

            return iqFactory.createNaryIQTree(unionNode, newChildren);
        }
        return iqFactory.createUnaryIQTree(this, newChild);
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
        if (o == null || getClass() != o.getClass()) return false;
        FlattenNodeImpl that = (FlattenNodeImpl) o;
        return
                flattenedVariable.equals(that.flattenedVariable) &&
                outputVariable.equals(that.outputVariable) &&
                indexVariable.equals(that.indexVariable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flattenedVariable, outputVariable, indexVariable);
    }

    @Override
    public FlattenNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformFlatten(tree, this, child);
    }

    @Override
    public <T> IQTree acceptTransformer(IQTree tree, IQTreeExtendedTransformer<T> transformer, IQTree child, T context) {
        return transformer.transformFlatten(tree,this, child, context);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor, IQTree child) {
        return visitor.visitFlatten(this, child);
    }

    @Override
    public boolean wouldKeepDescendingGroundTermInFilterAbove(Variable variable, boolean isConstant) {
        return variable.equals(outputVariable) || indexVariable
                .filter(variable::equals)
                .isPresent();
    }

    /**
     * Avoids creating an instance if unnecessary (a similar optimization is implemented for Filter Nodes)
     */
    private FlattenNode applySubstitution(Substitution<? extends VariableOrGroundTerm> sub) {
        Variable sFlattenedVar = applySubstitution(flattenedVariable, sub);
        Variable sOutputVar = applySubstitution(outputVariable, sub);
        Optional<Variable> sIndexVar = indexVariable.map(variable -> applySubstitution(variable, sub));
        return sFlattenedVar.equals(flattenedVariable) &&
                sOutputVar.equals(outputVariable) &&
                sIndexVar.equals(indexVariable) ?
                this :
                iqFactory.createFlattenNode(
                        sOutputVar,
                        sFlattenedVar,
                        sIndexVar,
                        flattenedType
                );
    }

    @FunctionalInterface
    interface PropagateToChild {

        IQTree apply(IQTree child, Substitution<? extends VariableOrGroundTerm> substitution,
                     Optional<ImmutableExpression> optionalConstraint, VariableGenerator variableGenerator);
    }
}

