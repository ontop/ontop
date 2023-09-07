package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.*;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.impl.JoinOrFilterVariableNullabilityTools;
import it.unibz.inf.ontop.iq.node.normalization.impl.RightProvenanceNormalizer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultNonRecursiveIQTreeTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractLJTransformer extends DefaultNonRecursiveIQTreeTransformer {

    private final Supplier<VariableNullability> variableNullabilitySupplier;
    // LAZY
    private VariableNullability variableNullability;

    protected final VariableGenerator variableGenerator;
    protected final RightProvenanceNormalizer rightProvenanceNormalizer;
    protected final CoreSingletons coreSingletons;
    protected final IntermediateQueryFactory iqFactory;
    protected final TermFactory termFactory;
    protected final SubstitutionFactory substitutionFactory;
    protected final JoinOrFilterVariableNullabilityTools variableNullabilityTools;

    protected AbstractLJTransformer(Supplier<VariableNullability> variableNullabilitySupplier,
                                    VariableGenerator variableGenerator,
                                    RightProvenanceNormalizer rightProvenanceNormalizer,
                                    JoinOrFilterVariableNullabilityTools variableNullabilityTools,
                                    CoreSingletons coreSingletons) {
        this.variableNullabilitySupplier = variableNullabilitySupplier;
        this.variableGenerator = variableGenerator;
        this.rightProvenanceNormalizer = rightProvenanceNormalizer;
        this.variableNullabilityTools = variableNullabilityTools;

        this.coreSingletons = coreSingletons;
        this.iqFactory = coreSingletons.getIQFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
    }

    @Override
    public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        IQTree transformedLeftChild = transform(leftChild);
        // Cannot reuse
        IQTree transformedRightChild = preTransformLJRightChild(rightChild, rootNode.getOptionalFilterCondition());

        if (preventRecursiveOptimizationOnRightChild()
                && !transformedRightChild.equals(rightChild))
            return iqFactory.createBinaryNonCommutativeIQTree(rootNode, transformedLeftChild, transformedRightChild)
                                        .normalizeForOptimization(variableGenerator);

        return furtherTransformLeftJoin(rootNode, transformedLeftChild, transformedRightChild)
                .orElseGet(() -> transformedLeftChild.equals(leftChild)
                        && transformedRightChild.equals(rightChild)
                                ? tree
                                : iqFactory.createBinaryNonCommutativeIQTree(rootNode, transformedLeftChild, transformedRightChild))
                                        .normalizeForOptimization(variableGenerator);
    }

    /**
     * If the right child has just been optimized by this optimizer, stops the optimization.
     * This allows to run other optimizers before running again this one.
     * This helps further simplifying the right child before applying the optimization at this level.
     */
    protected boolean preventRecursiveOptimizationOnRightChild() {
        return false;
    }

    /**
     * Returns empty if no optimization has been applied
     */
    protected abstract Optional<IQTree> furtherTransformLeftJoin(LeftJoinNode rootNode, IQTree leftChild,
                                                        IQTree rightChild);

    protected synchronized VariableNullability getInheritedVariableNullability() {
        if (variableNullability == null)
            variableNullability = variableNullabilitySupplier.get();

        return variableNullability;
    }


    @Override
    public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
        // Recursive
        return transformUnaryNode(tree, rootNode, child, this::transform);
    }

    @Override
    public IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
        // Recursive
        return transformUnaryNode(tree, rootNode, child, this::transform);
    }

    @Override
    public IQTree transformSlice(IQTree tree, SliceNode sliceNode, IQTree child) {
        // Recursive
        return transformUnaryNode(tree, sliceNode, child, this::transform);
    }

    @Override
    public IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child) {
        // Recursive
        return transformUnaryNode(tree, rootNode, child, this::transform);
    }

    @Override
    public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        // Recursive
        return transformNaryCommutativeNode(tree, rootNode, children, this::transform);
    }

    @Override
    protected IQTree transformUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child, this::transformBySearchingFromScratch);
    }

    protected IQTree transformUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child,
                                        Function<IQTree, IQTree> childTransformation) {
        IQTree newChild = childTransformation.apply(child);
        return newChild.equals(child)
                ? tree
                : iqFactory.createUnaryIQTree(rootNode, newChild)
                    .normalizeForOptimization(variableGenerator);
    }

    @Override
    protected IQTree transformNaryCommutativeNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, rootNode, children, this::transformBySearchingFromScratch);
    }

    protected IQTree transformNaryCommutativeNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children,
                                                  Function<IQTree, IQTree> childTransformation) {
        ImmutableList<IQTree> newChildren = children.stream()
                .map(childTransformation)
                .collect(ImmutableCollectors.toList());
        return newChildren.equals(children)
                ? tree
                : iqFactory.createNaryIQTree(rootNode, newChildren)
                    .normalizeForOptimization(variableGenerator);
    }

    @Override
    protected IQTree transformBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                       IQTree leftChild, IQTree rightChild) {
        return transformBinaryNonCommutativeNode(tree, rootNode, leftChild, rightChild,
                this::transformBySearchingFromScratch);
    }

    protected IQTree transformBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                       IQTree leftChild, IQTree rightChild,
                                                       Function<IQTree, IQTree> childTransformation) {
        IQTree newLeftChild = childTransformation.apply(leftChild);
        IQTree newRightChild = childTransformation.apply(rightChild);
        return newLeftChild.equals(leftChild) && newRightChild.equals(rightChild)
                ? tree
                : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild)
                    .normalizeForOptimization(variableGenerator);
    }

    protected abstract IQTree transformBySearchingFromScratch(IQTree tree);

    /**
     * Can be overridden
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    abstract protected IQTree preTransformLJRightChild(IQTree rightChild, Optional<ImmutableExpression> ljCondition);


    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected VariableNullability computeRightChildVariableNullability(IQTree rightChild, Optional<ImmutableExpression> ljCondition) {
        VariableNullability bottomUpNullability = rightChild.getVariableNullability();

        VariableNullability nullabilityWithLJCondition = ljCondition
                .map(c -> variableNullabilityTools.updateWithFilter(c, bottomUpNullability.getNullableGroups(),
                        rightChild.getVariables()))
                .orElse(bottomUpNullability);

        ImmutableSet<Variable> nullableVariablesAfterLJCondition = nullabilityWithLJCondition.getNullableVariables();

        if (nullableVariablesAfterLJCondition.isEmpty())
            return nullabilityWithLJCondition;

        VariableNullability inheritedNullability = getInheritedVariableNullability();

        // Non-nullability information coming from the ancestors
        Optional<ImmutableExpression> additionalFilter = termFactory.getConjunction(nullableVariablesAfterLJCondition.stream()
                .filter(v -> !inheritedNullability.isPossiblyNullable(v))
                .map(termFactory::getDBIsNotNull));

        return additionalFilter
                .map(c -> variableNullabilityTools.updateWithFilter(c, nullabilityWithLJCondition.getNullableGroups(),
                        rightChild.getVariables()))
                .orElse(nullabilityWithLJCondition);
    }


}
