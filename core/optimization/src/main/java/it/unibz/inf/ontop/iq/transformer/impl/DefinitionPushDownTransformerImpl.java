package it.unibz.inf.ontop.iq.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OptimizerFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.request.DefinitionPushDownRequest;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transformer.DefinitionPushDownTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.IntStream;

public class DefinitionPushDownTransformerImpl extends DefaultRecursiveIQTreeVisitingTransformer
        implements DefinitionPushDownTransformer {

    private final DefinitionPushDownRequest request;
    private final OptimizerFactory optimizerFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;

    private final IQTreeTools iqTreeTools;

    @AssistedInject
    protected DefinitionPushDownTransformerImpl(@Assisted DefinitionPushDownRequest request,
                                                IntermediateQueryFactory iqFactory,
                                                OptimizerFactory optimizerFactory,
                                                SubstitutionFactory substitutionFactory,
                                                TermFactory termFactory,
                                                IQTreeTools iqTreeTools) {
        super(iqFactory);
        this.request = request;
        this.optimizerFactory = optimizerFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
        this.iqTreeTools = iqTreeTools;
    }

    @Override
    public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
        Substitution<ImmutableTerm> initialSubstitution = rootNode.getSubstitution();

        ImmutableSet<Variable> newProjectedVariables = iqTreeTools.getChildrenVariables(tree,  request.getNewVariable());

        DefinitionPushDownRequest newRequest = request.newRequest(rootNode.getSubstitution());
        if (newRequest.equals(request))
            return iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(newProjectedVariables, initialSubstitution),
                    child.acceptTransformer(this));

        ImmutableExpression newCondition = newRequest.getCondition();
        Optional<ImmutableTerm> optionalLocalDefinition = newCondition.evaluate2VL(termFactory.createDummyVariableNullability(newCondition))
                .getValue()
                .map(v -> {
                    switch (v) {
                        case TRUE:
                            return newRequest.getDefinitionWhenConditionSatisfied();
                        case FALSE:
                        case NULL:
                        default:
                            return termFactory.getNullConstant();
                    }
                });

        return optionalLocalDefinition
                // Stops the definition to the new construction node
                .map(d -> iqFactory.createUnaryIQTree(
                        iqFactory.createConstructionNode(newProjectedVariables,
                                substitutionFactory.union(initialSubstitution, substitutionFactory.getSubstitution(newRequest.getNewVariable(), d))),
                        child))
                // Otherwise, continues
                .orElseGet(() -> iqFactory.createUnaryIQTree(
                        iqFactory.createConstructionNode(newProjectedVariables, initialSubstitution),
                        // "Recursive"
                        optimizerFactory.createDefinitionPushDownTransformer(newRequest).transform(child)));
    }

    /**
     * TODO: understand when the definition does not have to be blocked
     */
    @Override
    public IQTree transformAggregation(IQTree tree, AggregationNode rootNode, IQTree child) {
        return blockDefinition(tree);
    }

    /**
     * TODO: stop blocking systematically
     */
    @Override
    public IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
        return blockDefinition(tree);
    }

    /**
     * Blocks by default
     */
    @Override
    public IQTree transformNonStandardUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
        return blockDefinition(tree);
    }

    @Override
    public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        ImmutableSet<Variable> requestVariables = request.getDefinitionAndConditionVariables();
        if (leftChild.getVariables().containsAll(requestVariables))
            return iqFactory.createBinaryNonCommutativeIQTree(
                    rootNode,
                    leftChild.acceptTransformer(this),
                    rightChild);
        else if (rightChild.getVariables().containsAll(requestVariables))
            return iqFactory.createBinaryNonCommutativeIQTree(
                    rootNode,
                    leftChild,
                    rightChild.acceptTransformer(this));
        else
            return blockDefinition(tree);
    }

    @Override
    public IQTree transformNonStandardBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                               IQTree leftChild, IQTree rightChild) {
        return blockDefinition(tree);
    }

    @Override
    public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        ImmutableSet<Variable> requestVariables = request.getDefinitionAndConditionVariables();

        return IntStream.range(0, children.size())
                .filter(i -> children.get(i).getVariables().containsAll(requestVariables))
                .boxed()
                .findAny()
                .map(i -> IntStream.range(0, children.size())
                        .mapToObj(j -> i.equals(j)
                                // Pushes down the definition to selected child
                                ? children.get(j).acceptTransformer(this)
                                : children.get(j))
                        .collect(ImmutableCollectors.toList()))
                .map(newChildren -> (IQTree) iqFactory.createNaryIQTree(rootNode, newChildren))

                // Otherwise, blocks the definition
                .orElseGet(() -> blockDefinition(tree));
    }

    @Override
    public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        ImmutableList<IQTree> newChildren = children.stream()
                .map(c -> c.acceptTransformer(this))
                .collect(ImmutableCollectors.toList());

        UnionNode newRootNode = newChildren.stream()
                .findAny()
                .map(IQTree::getVariables)
                .map(iqFactory::createUnionNode)
                .orElseThrow(() -> new MinorOntopInternalBugException("An union always have multiple children"));

        return iqFactory.createNaryIQTree(newRootNode, newChildren);
    }

    @Override
    public IQTree transformNonStandardNaryNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        return blockDefinition(tree);
    }

    @Override
    protected IQTree transformLeaf(LeafIQTree leaf) {
        return blockDefinition(leaf);
    }

    protected IQTree blockDefinition(IQTree tree) {
        Variable newVariable = request.getNewVariable();

        ConstructionNode constructionNode = iqFactory.createConstructionNode(
                iqTreeTools.getChildrenVariables(tree, newVariable),
                substitutionFactory.getSubstitution(newVariable,
                        termFactory.getIfElseNull(
                                request.getCondition(),
                                request.getDefinitionWhenConditionSatisfied())));
        return iqFactory.createUnaryIQTree(constructionNode, tree);
    }
}
