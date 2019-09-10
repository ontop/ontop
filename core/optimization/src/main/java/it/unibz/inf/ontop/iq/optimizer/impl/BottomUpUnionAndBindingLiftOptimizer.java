package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.BinaryNonCommutativeOperatorNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.iq.node.NaryOperatorNode;
import it.unibz.inf.ontop.iq.optimizer.UnionAndBindingLiftOptimizer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * TODO: explicit assumptions
 */
@Singleton
public class BottomUpUnionAndBindingLiftOptimizer implements UnionAndBindingLiftOptimizer {

    private static final int ITERATION_BOUND = 10000;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    private BottomUpUnionAndBindingLiftOptimizer(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public IQ optimize(IQ query) {
        IQ bindingLiftedQuery = query.normalizeForOptimization();
        return liftUnionsInTree(bindingLiftedQuery);
    }

    /**
     * TODO: refactor
     */
    private IQ liftUnionsInTree(IQ query) {
        VariableGenerator variableGenerator = query.getVariableGenerator();

        // Non-final
        IQTree previousTree;
        IQTree newTree = query.getTree();
        int i=0;
        do {
            previousTree = newTree;
            newTree = liftTree(previousTree, variableGenerator)
                    .normalizeForOptimization(variableGenerator);

        } while (!newTree.equals(previousTree) && (++i < ITERATION_BOUND));

        if (i >= ITERATION_BOUND)
            throw new MinorOntopInternalBugException(getClass().getName() + " did not converge after "
                    + ITERATION_BOUND + " iterations");

        return newTree.equals(query.getTree())
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }


    /**
     * Recursive (to reach the descendency but does not loop over itself)
     */
    private IQTree liftTree(IQTree queryTree, VariableGenerator variableGenerator) {
        if (queryTree instanceof UnaryIQTree)
            return liftUnary((UnaryIQTree) queryTree, variableGenerator);
        else if (queryTree instanceof NaryIQTree)
            return liftNary((NaryIQTree) queryTree, variableGenerator);
        else if (queryTree instanceof BinaryNonCommutativeIQTree)
            return liftBinaryNonCommutative((BinaryNonCommutativeIQTree) queryTree, variableGenerator);
        // Leaf node
        else
            return queryTree;
    }

    private UnaryIQTree liftUnary(UnaryIQTree queryTree, VariableGenerator variableGenerator) {
        IQTree newChild = liftTree(queryTree.getChild(), variableGenerator);
        return newChild.equals(queryTree.getChild())
                ? queryTree
                : iqFactory.createUnaryIQTree(queryTree.getRootNode(), newChild);
    }

    private IQTree liftNary(NaryIQTree queryTree, VariableGenerator variableGenerator) {
        NaryOperatorNode root = queryTree.getRootNode();

        ImmutableList<IQTree> newChildren = queryTree.getChildren().stream()
                // Recursive
                .map(queryTree1 -> liftTree(queryTree1, variableGenerator))
                .collect(ImmutableCollectors.toList());

        if (root instanceof InnerJoinNode) {
            return liftInnerJoin(queryTree, newChildren, variableGenerator);
        }
        else
            return newChildren.equals(queryTree.getChildren())
                    ? queryTree
                    : iqFactory.createNaryIQTree(root, newChildren);

    }


    /**
     * TODO: refactor
     */
    private IQTree liftInnerJoin(NaryIQTree queryTree, ImmutableList<IQTree> newChildren, VariableGenerator variableGenerator) {
        InnerJoinNode joinNode = (InnerJoinNode) queryTree.getRootNode();

        NaryIQTree newQueryTree = newChildren.equals(queryTree.getChildren())
                ? queryTree
                : iqFactory.createNaryIQTree(joinNode, newChildren);

        return extractCandidateVariables(queryTree, joinNode.getOptionalFilterCondition(), newChildren)
                .map(variable -> newQueryTree.liftIncompatibleDefinitions(variable, variableGenerator))
                .filter(t -> !t.equals(queryTree))
                .findFirst()
                .orElse(newQueryTree)
                .normalizeForOptimization(variableGenerator);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Stream<Variable> extractCandidateVariables(IQTree tree,
                                                       Optional<ImmutableExpression> optionalFilterCondition,
                                                       ImmutableList<IQTree> newChildren) {
        Stream<Variable> coOccurringVariables = IntStream.range(0, newChildren.size() - 1)
                .boxed()
                .flatMap(i -> newChildren.get(i).getVariables().stream()
                        .filter(v1 -> IntStream.range(i + 1, newChildren.size())
                                .anyMatch(j -> newChildren.get(j).getVariables().stream().
                                        anyMatch(v1::equals))));

        return Stream.concat(
                    optionalFilterCondition
                        .map(ImmutableTerm::getVariableStream)
                        .orElseGet(Stream::empty),
                    coOccurringVariables)
                .distinct()
                .filter(tree::isConstructed);
    }

    private IQTree liftBinaryNonCommutative(BinaryNonCommutativeIQTree queryTree, VariableGenerator variableGenerator) {
        BinaryNonCommutativeOperatorNode root = queryTree.getRootNode();

        IQTree newLeftChild = liftTree(queryTree.getLeftChild(), variableGenerator);
        IQTree newRightChild = liftTree(queryTree.getRightChild(), variableGenerator);

        if (root instanceof LeftJoinNode) {
            return liftLJJoin(queryTree, newLeftChild, newRightChild, variableGenerator);
        }
        else
            return newLeftChild.equals(queryTree.getLeftChild()) && newRightChild.equals(queryTree.getRightChild())
                    ? queryTree
                    : iqFactory.createBinaryNonCommutativeIQTree(root, newLeftChild, newRightChild);
    }

    /**
     * TODO: refactor
     */
    private IQTree liftLJJoin(BinaryNonCommutativeIQTree queryTree, IQTree newLeftChild, IQTree newRightChild,
                              VariableGenerator variableGenerator) {
        LeftJoinNode leftJoinNode = (LeftJoinNode) queryTree.getRootNode();

        BinaryNonCommutativeIQTree newQueryTree = newLeftChild.equals(queryTree.getLeftChild())
                && newRightChild.equals(queryTree.getRightChild())
                ? queryTree
                : iqFactory.createBinaryNonCommutativeIQTree(leftJoinNode, newLeftChild, newRightChild);

        return extractCandidateVariables(queryTree, leftJoinNode.getOptionalFilterCondition(),
                    ImmutableList.of(newLeftChild, newRightChild))
                .filter(v -> newLeftChild.getVariables().contains(v))
                .map(variable -> newQueryTree.liftIncompatibleDefinitions(variable, variableGenerator))
                .filter(t -> !t.equals(queryTree))
                .findFirst()
                .orElse(newQueryTree)
                .normalizeForOptimization(variableGenerator);
    }

}
