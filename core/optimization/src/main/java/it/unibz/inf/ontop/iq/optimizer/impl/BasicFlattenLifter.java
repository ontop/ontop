package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.FlattenLifter;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.function.BiFunction;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryOperatorSequence;

public class BasicFlattenLifter implements FlattenLifter {

    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    private BasicFlattenLifter(IntermediateQueryFactory iqFactory, IQTreeTools iqTreeTools) {
        this.iqFactory = iqFactory;
        this.iqTreeTools = iqTreeTools;
    }

    @Override
    public IQ optimize(IQ query) {
        TreeTransformer treeTransformer = new TreeTransformer();
        return iqFactory.createIQ(
                query.getProjectionAtom(),
                query.getTree().acceptTransformer(treeTransformer));
    }

    private class TreeTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        TreeTransformer() {
            super(BasicFlattenLifter.this.iqFactory);
        }

        @Override
        public IQTree transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
            IQTree updatedChild = transformChild(child);
            State flattenSubtree = decompose(updatedChild);
            if (flattenSubtree.liftableFlatten.isEmpty())
                return iqFactory.createUnaryIQTree(rootNode, updatedChild);

            State subtree = rootNode.getFilterCondition().flattenAND()
                    .collect(simpleAccumulatorOf(
                            flattenSubtree,
                            (t, conjunct) -> {
                                State split = split(t.liftableFlatten, t.tail, conjunct.getVariables());
                                return split.insertAboveNonLiftable(iqFactory.createFilterNode(conjunct));
                            }));

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(subtree.liftableFlatten)
                    .build(subtree.tail);
        }

        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode cn, IQTree child) {
            IQTree updatedChild = transformChild(child);
            if (tree.getRootNode().equals(cn))
                return iqFactory.createUnaryIQTree(cn, updatedChild);

            State split = splitOf(updatedChild, ImmutableSet.of());
            ConstructionNode constructionNode =
                    split.liftableFlatten.stream()
                            .collect(simpleAccumulatorOf(cn, this::updateConstructionNode));

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(split.liftableFlatten)
                    .append(constructionNode)
                    .build(split.nonLiftableSubtree());
        }

        private ConstructionNode updateConstructionNode(ConstructionNode cn, FlattenNode fn) {
            ImmutableSet<Variable> projectedVars = Sets.union(
                            Sets.difference(cn.getVariables(), fn.getLocallyDefinedVariables()),
                            ImmutableSet.of(fn.getFlattenedVariable()))
                    .immutableCopy();

            return iqFactory.createConstructionNode(projectedVars, cn.getSubstitution());
        }

        /**
         * Assumption: the join carries no (explicit) joining condition
         */
        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode join, ImmutableList<IQTree> initialChildren) {
            ImmutableList<IQTree> children = transformChildren(initialChildren);

            ImmutableSet<Variable> blockingVars = NaryIQTreeTools.coOccurringVariablesStream(children)
                    .collect(ImmutableCollectors.toSet());

            ImmutableList<State> splits = NaryIQTreeTools.transformChildren(children,
                    c -> splitOf(c, blockingVars));

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(splits.stream()
                            .flatMap(fs -> fs.liftableFlatten.stream()))
                    .build(iqFactory.createNaryIQTree(join, splits.stream()
                            .map(State::nonLiftableSubtree)
                            .collect(ImmutableCollectors.toList())));
        }

        /**
         * For now the implementation is identical to the one of inner joins, with the exception that all variables involved in the LJ condition are blocking.
         * TODO: identify additional cases where flatten nodes could be lifted, by creating a filter
         * (note that this only possible if extra integrity constraints hold, or in the presence of a distinct)
         */
        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree initialLeftChild, IQTree initialRightChild) {
            IQTree leftChild = transformChild(initialLeftChild);
            IQTree rightChild = transformChild(initialRightChild);

            // all variables involved in the joining condition are blocking
            ImmutableSet<Variable> blockingVars = Stream.concat(
                    NaryIQTreeTools.coOccurringVariablesStream(ImmutableList.of(leftChild, rightChild)),
                    rootNode.getLocallyRequiredVariables().stream())
                    .collect(ImmutableCollectors.toSet());

            State leftSplit = splitOf(leftChild, blockingVars);
            State rightSplit = splitOf(rightChild, blockingVars);

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(Stream.of(leftSplit, rightSplit)
                        .flatMap(c -> c.liftableFlatten.stream()))
                    .build(iqFactory.createBinaryNonCommutativeIQTree(
                            rootNode,
                            leftSplit.nonLiftableSubtree(),
                            rightSplit.nonLiftableSubtree()));
        }

    }

    private State split(UnaryOperatorSequence<FlattenNode> sequence, IQTree tail, ImmutableSet<Variable> blockingVars) {
        return sequence.stream()
                .collect(simpleAccumulatorOf(
                        new State(UnaryOperatorSequence.of(), UnaryOperatorSequence.of(), tail, blockingVars),
                        State::append));
    }


    private State decompose(IQTree tree) {
        var list = Stream.iterate(
                        IQTreeTools.UnaryIQTreeDecomposition.of(tree, FlattenNode.class),
                        IQTreeTools.UnaryIQTreeDecomposition::isPresent,
                        d -> IQTreeTools.UnaryIQTreeDecomposition.of(d.getChild(), FlattenNode.class))
                .collect(ImmutableCollectors.toList());

        return new State(
               UnaryOperatorSequence.of(
                        list.stream()
                                .map(IQTreeTools.UnaryIQTreeDecomposition::getNode)),
                UnaryOperatorSequence.of(),
                list.isEmpty() ? tree : list.get(list.size() - 1).getChild(),
                ImmutableSet.of());
    }


    private class State {
        private final UnaryOperatorSequence<FlattenNode> liftableFlatten;
        private final UnaryOperatorSequence<UnaryOperatorNode> nonLiftableUnary;
        private final IQTree tail;
        private final ImmutableSet<Variable> blockingVars;

        State(UnaryOperatorSequence<FlattenNode> liftableFlatten, UnaryOperatorSequence<UnaryOperatorNode> nonLiftableUnary, IQTree tail, ImmutableSet<Variable> blockingVars) {
            this.liftableFlatten = liftableFlatten;
            this.nonLiftableUnary = nonLiftableUnary;
            this.tail = tail;
            this.blockingVars = blockingVars;
        }

        State append(FlattenNode fn) {
            return Sets.intersection(fn.getLocallyDefinedVariables(), blockingVars).isEmpty()
                    ? new State(
                        liftableFlatten.append(fn),
                        nonLiftableUnary,
                        tail,
                        blockingVars)
                    : new State(
                        liftableFlatten,
                        nonLiftableUnary.append(fn),
                        tail,
                        Stream.concat(blockingVars.stream(), Stream.of(fn.getFlattenedVariable())).collect(ImmutableCollectors.toSet()));
        }

        IQTree nonLiftableSubtree() {
            return iqTreeTools.unaryIQTreeBuilder()
                    .append(nonLiftableUnary)
                    .build(tail);
        }

        State insertAboveNonLiftable(UnaryOperatorNode node) {
            return new State(liftableFlatten, UnaryOperatorSequence.of(),
                    iqFactory.createUnaryIQTree(node, nonLiftableSubtree()), ImmutableSet.of());
        }
    }

    private State splitOf(IQTree tree, ImmutableSet<Variable> blockingVars) {
        var f = decompose(tree);
        return split(f.liftableFlatten, f.tail, blockingVars);
    }

    private static <T,A> Collector<T,?,A> simpleAccumulatorOf(A initial, BiFunction<A,T,A> function) {
        class Store {
            private A value;
        }

        return Collector.of(
                () -> { Store s = new Store(); s.value = initial; return s; },
                (s, t) -> { s.value = function.apply(s.value, t); },
                (a1, a2) -> { throw new UnsupportedOperationException("Should not be called"); },
                s -> s.value);
    }
}
