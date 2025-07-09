package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.UnionAndBindingLiftOptimizer;
import it.unibz.inf.ontop.iq.visit.impl.DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * TODO: explicit assumptions
 */
@Singleton
public class BottomUpUnionAndBindingLiftOptimizer extends AbstractIQOptimizer implements UnionAndBindingLiftOptimizer {

    private static final int ITERATION_BOUND = 10000;

    @Inject
    private BottomUpUnionAndBindingLiftOptimizer(IntermediateQueryFactory iqFactory) {
        super(iqFactory, NO_ACTION);
    }

    @Override
    protected IQTree transformTree(IQTree tree, VariableGenerator variableGenerator) {
        Lifter lifter = new Lifter(variableGenerator);

        // Non-final
        IQTree previousTree;
        IQTree newTree = tree.normalizeForOptimization(variableGenerator);
        int i=0;
        do {
            previousTree = newTree;
            newTree = previousTree.acceptVisitor(lifter)
                    .normalizeForOptimization(variableGenerator);

        } while (!newTree.equals(previousTree) && (++i < ITERATION_BOUND));

        if (i >= ITERATION_BOUND)
            throw new MinorOntopInternalBugException(getClass().getName() + " did not converge after "
                    + ITERATION_BOUND + " iterations");
        return newTree;
    }

    private class Lifter extends DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator {

        Lifter(VariableGenerator variableGenerator) {
            super(BottomUpUnionAndBindingLiftOptimizer.this.iqFactory, variableGenerator);
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

        Stream<Variable> coOccurringVariables = NaryIQTreeTools.coOccurringVariablesStream(newChildren);

        return Stream.concat(
                    optionalFilterCondition.stream()
                        .flatMap(ImmutableTerm::getVariableStream),
                    coOccurringVariables)
                .distinct()
                .filter(tree::isConstructed);
    }
}
