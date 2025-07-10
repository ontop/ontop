package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public abstract class AbstractLJTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

    protected final VariableGenerator variableGenerator;

    protected AbstractLJTransformer(IntermediateQueryFactory iqFactory, VariableGenerator variableGenerator) {

        super(iqFactory, t -> t.normalizeForOptimization(variableGenerator));

        this.variableGenerator = variableGenerator;
    }

    @Override
    public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        IQTree transformedLeftChild = transformChild(leftChild);
        // Cannot reuse
        IQTree transformedRightChild = preTransformLJRightChild(rightChild, rootNode.getOptionalFilterCondition(), leftChild.getVariables());

        if (preventRecursiveOptimizationOnRightChild()
                && !transformedRightChild.equals(rightChild))
            return postTransformer.apply(iqFactory.createBinaryNonCommutativeIQTree(rootNode, transformedLeftChild, transformedRightChild));

        return furtherTransformLeftJoin(rootNode, transformedLeftChild, transformedRightChild)
                .orElseGet(() -> withTransformedChildren(tree, transformedLeftChild, transformedRightChild));
    }

    /**
     * If this optimizer has just optimized the right child, stops the optimization.
     * This allows running other optimizers before running again this one.
     * This helps further simplify the right child before applying the optimization at this level.
     */
    protected boolean preventRecursiveOptimizationOnRightChild() {
        return false;
    }

    /**
     * Returns empty if no optimization has been applied
     */
    protected abstract Optional<IQTree> furtherTransformLeftJoin(LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild);


    @Override
    public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child, this::transformBySearchingFromScratch);
    }

    @Override
    public IQTree transformFlatten(UnaryIQTree tree, FlattenNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child, this::transformBySearchingFromScratch);
    }

    @Override
    public IQTree transformAggregation(UnaryIQTree tree, AggregationNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child, this::transformBySearchingFromScratch);
    }

    @Override
    public IQTree transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, rootNode, children, this::transformBySearchingFromScratch);
    }

    protected abstract IQTree transformBySearchingFromScratch(IQTree tree);

    /**
     * Can be overridden
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected abstract IQTree preTransformLJRightChild(IQTree rightChild, Optional<ImmutableExpression> ljCondition,
                                                       ImmutableSet<Variable> leftVariables);
}
