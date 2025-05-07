package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.UnionAndBindingLiftOptimizer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
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

    private IQTree liftTree(IQTree previousTree, VariableGenerator variableGenerator) {
        Lifter lifter = new Lifter(variableGenerator);
        return lifter.transform(previousTree);
    }

    private class Lifter extends DefaultRecursiveIQTreeVisitingTransformer {
        private final VariableGenerator variableGenerator;

        protected Lifter(VariableGenerator variableGenerator) {
            super(BottomUpUnionAndBindingLiftOptimizer.this.iqFactory);
            this.variableGenerator = variableGenerator;
        }

        @Override
        protected boolean nodesEqual(QueryNode node1, QueryNode node2) {
            return true;
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode node, ImmutableList<IQTree> children) {
            NaryIQTree newTree = (NaryIQTree) super.transformInnerJoin(tree, node, children);

            return extractCandidateVariables(tree, node.getOptionalFilterCondition(), newTree.getChildren())
                    .map(variable -> newTree.liftIncompatibleDefinitions(variable, variableGenerator))
                    .filter(t -> !t.equals(tree))
                    .findFirst()
                    .orElse(newTree)
                    .normalizeForOptimization(variableGenerator);
        }

        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode node, IQTree leftChild, IQTree rightChild) {
            BinaryNonCommutativeIQTree newTree = (BinaryNonCommutativeIQTree)super.transformLeftJoin(tree, node, leftChild, rightChild);

            ImmutableSet<Variable> leftVariables = newTree.getLeftChild().getVariables();
            return extractCandidateVariables(tree, node.getOptionalFilterCondition(), ImmutableList.of(newTree.getLeftChild(), newTree.getRightChild()))
                    .filter(leftVariables::contains)
                    .map(variable -> newTree.liftIncompatibleDefinitions(variable, variableGenerator))
                    .filter(t -> !t.equals(tree))
                    .findFirst()
                    .orElse(newTree)
                    .normalizeForOptimization(variableGenerator);
        }
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
}
