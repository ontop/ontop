package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.BinaryNonCommutativeIQTreeTools;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.impl.UnaryIQTreeBuilder;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.BasicFlattenLifter;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.UnaryIQTreeTools.UnaryOperatorSequence;
import static it.unibz.inf.ontop.iq.impl.UnaryIQTreeTools.UnaryIQTreeDecomposition;

public class BasicFlattenLifterImpl implements BasicFlattenLifter {

    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTools iqTreeTools;
    private final Transformer transformer;

    @Inject
    private BasicFlattenLifterImpl(IntermediateQueryFactory iqFactory, IQTreeTools iqTreeTools) {
        // no equality check
        this.iqFactory = iqFactory;
        this.iqTreeTools = iqTreeTools;
        this.transformer = new Transformer();
    }

    @Override
    public IQTree transform(IQTree tree) {
        // avoid lifting FLATTEN above the top-level CONSTRUCTION
        var construction = UnaryIQTreeDecomposition.of(tree, ConstructionNode.class);
        return iqTreeTools.unaryIQTreeBuilder()
                .append(construction.getOptionalNode())
                .build(transformer.transform(construction.getTail()));
    }


    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {

        Transformer() {
            super(BasicFlattenLifterImpl.this.iqFactory);
        }

        @Override
        public IQTree transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
            IQTree updatedChild = transform(child);

            // just to avoid unnecessary splitting of the FILTER node
            var flatten = UnaryIQTreeDecomposition.of(updatedChild, FlattenNode.class);
            if (!flatten.isPresent())
                return iqFactory.createUnaryIQTree(rootNode, updatedChild);

            ImmutableList<ImmutableExpression> conjuncts = rootNode.getFilterCondition().flattenAND()
                    .collect(ImmutableCollectors.toList());

            IQTree current = updatedChild;
            // note: reverses the order of conjuncts in the tree
            for (ImmutableExpression conjunct : conjuncts) {
                LiftingState s = liftFlatten(current, conjunct.getVariables());
                current = iqTreeTools.unaryIQTreeBuilder()
                        .append(s.getLifted())
                        .append(iqFactory.createFilterNode(conjunct))
                        .build(s.getTree());
            }

            return current;
        }


        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode cn, IQTree child) {
            IQTree updatedChild = transform(child);

            LiftingState s = liftFlatten(updatedChild,
                    // cannot be lifted if required in the CONSTRUCTION substitution
                    //          or is projected away by the CONSTRUCTION node
                    Sets.union(cn.getSubstitution().getRangeVariables(),
                            Sets.difference(child.getVariables(), cn.getVariables())).immutableCopy());
            ImmutableSet<Variable> projectedVariables = s.getLifted().stream().reduce(cn.getVariables(),
                    (pv, fn) -> Sets.union(
                        Sets.difference(pv, fn.getLocallyDefinedVariables()),
                        ImmutableSet.of(fn.getFlattenedVariable())).immutableCopy(),
                    (s1, s2) -> { throw new MinorOntopInternalBugException("parallel streams unsupported"); });

            ConstructionNode constructionNode = iqFactory.createConstructionNode(projectedVariables, cn.getSubstitution());

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(s.getLifted())
                    .append(constructionNode)
                    .build(s.getTree());
        }

        /**
         * Assumption: the join carries no (explicit) joining condition
         */
        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode join, ImmutableList<IQTree> initialChildren) {
            ImmutableList<IQTree> children = NaryIQTreeTools.transformChildren(initialChildren, this::transform);

            ImmutableSet<Variable> blockingVars = NaryIQTreeTools.coOccurringVariablesStream(children)
                    .collect(ImmutableCollectors.toSet());

            ImmutableList<LiftingState> liftedChildren = NaryIQTreeTools.transformChildren(children,
                    c -> liftFlatten(c, blockingVars));

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(liftedChildren.stream()
                            .map(LiftingState::getLifted)
                            .flatMap(UnaryOperatorSequence::stream))
                    .build(iqFactory.createNaryIQTree(join, liftedChildren.stream()
                            .map(LiftingState::getTree)
                            .collect(ImmutableCollectors.toList())));
        }



        /**
         * For now the implementation is identical to the one of inner joins, with the exception that all variables involved in the LJ condition are blocking.
         * TODO: identify additional cases where flatten nodes could be lifted, by creating a filter
         * (note that this only possible if extra integrity constraints hold, or in the presence of a distinct)
         */
        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree initialLeftChild, IQTree initialRightChild) {
            IQTree leftChild = transform(initialLeftChild);
            IQTree rightChild = transform(initialRightChild);

            // all variables involved in the joining condition are blocking
            ImmutableSet<Variable> blockingVars = Stream.concat(
                    BinaryNonCommutativeIQTreeTools.commonVariables(leftChild, rightChild).stream(),
                    rootNode.getLocallyRequiredVariables().stream())
                    .collect(ImmutableCollectors.toSet());

            LiftingState leftSplit = liftFlatten(leftChild, blockingVars);
            LiftingState rightSplit = liftFlatten(rightChild, blockingVars);

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(Stream.of(leftSplit, rightSplit)
                            .map(LiftingState::getLifted)
                            .flatMap(UnaryOperatorSequence::stream))
                    .build(iqFactory.createBinaryNonCommutativeIQTree(
                            rootNode,
                            leftSplit.getTree(),
                            rightSplit.getTree()));
        }
    }

    private static class LiftingState {
        private final UnaryOperatorSequence<FlattenNode> lifted;
        private final IQTree tree;

        LiftingState(UnaryOperatorSequence<FlattenNode> lifted, IQTree tree) {
            this.lifted = lifted;
            this.tree = tree;
        }

        UnaryOperatorSequence<FlattenNode> getLifted() {
            return lifted;
        }

        IQTree getTree() {
            return tree;
        }
    }

    private LiftingState liftFlatten(IQTree tree, ImmutableSet<Variable> blockingVars) {
        UnaryOperatorSequence<FlattenNode> lifted = UnaryOperatorSequence.of();
        UnaryIQTreeBuilder<FlattenNode> nonLiftable = iqTreeTools.unaryIQTreeBuilder();
        IQTree current = tree;
        while (true) {
            var flatten = UnaryIQTreeDecomposition.of(current, FlattenNode.class);
            if (!flatten.isPresent())
                return new LiftingState(lifted, nonLiftable.build(current));

            FlattenNode fn = flatten.getNode();
            if (!Sets.intersection(fn.getLocallyDefinedVariables(), blockingVars).isEmpty()) {
                nonLiftable = nonLiftable.append(fn);
                blockingVars = Sets.union(blockingVars, ImmutableSet.of(fn.getFlattenedVariable())).immutableCopy();
            }
            else {
                lifted = lifted.append(fn);
            }

            current = flatten.getChild();
        }
    }
}
