package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.SelfJoinSameVariableIQOptimizer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultNonRecursiveIQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SelfJoinSameVariableIQOptimizerImpl implements SelfJoinSameVariableIQOptimizer {

    private final IQTreeVisitingTransformer transformer;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected SelfJoinSameVariableIQOptimizerImpl(LookForDistinctTransformer transformer,
                                                  IntermediateQueryFactory iqFactory) {
        this.transformer = transformer;
        this.iqFactory = iqFactory;
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree initialTree = query.getTree();
        IQTree newTree = transformer.transform(initialTree);
        return (newTree.equals(initialTree))
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }


    /**
     * TODO: find a better name
     */
    @Singleton
    protected static class LookForDistinctTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        protected final CoreSingletons coreSingletons;

        @Inject
        protected LookForDistinctTransformer(CoreSingletons coreSingletons) {
            super(coreSingletons);
            this.coreSingletons = coreSingletons;
        }

        @Override
        public IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
            IQTreeVisitingTransformer newTransformer = new SimplifyAccordingToDistinctVariables(
                    child.getVariables(),
                    coreSingletons, this);

            IQTree newChild = newTransformer.transform(child);
            return (newChild.equals(child))
                    ? tree
                    : iqFactory.createUnaryIQTree(rootNode, newChild);
        }
    }


    /**
     * TODO: find a better name
     *
     * By default, nodes do not propagate the DISTINCT information to their children (as it may be incorrect).
     *
     */
    protected static class SimplifyAccordingToDistinctVariables extends DefaultNonRecursiveIQTreeTransformer {

        private final IQTreeVisitingTransformer lookForDistinctTransformer;

        /**
         * Variables to which the DISTINCT operator is applied
         */
        private final ImmutableSet<Variable> distinctVariables;
        protected final CoreSingletons coreSingletons;

        protected SimplifyAccordingToDistinctVariables(ImmutableSet<Variable> distinctVariables, CoreSingletons coreSingletons,
                                                       IQTreeVisitingTransformer lookForDistinctTransformer) {
            this.distinctVariables = distinctVariables;
            this.coreSingletons = coreSingletons;
            this.lookForDistinctTransformer = lookForDistinctTransformer;
        }

        /**
         * TODO: support the traversal of construction node
         */
        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
            return super.transformConstruction(tree, rootNode, child);
        }

        @Override
        public IQTree transformSlice(IQTree tree, SliceNode sliceNode, IQTree child) {
            IQTree newChild = transform(child);
            return (newChild.equals(child))
                    ? tree
                    : coreSingletons.getIQFactory().createUnaryIQTree(sliceNode, newChild);
        }

        @Override
        public IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child) {
            IQTree newChild = transform(child);
            return (newChild.equals(child))
                    ? tree
                    : coreSingletons.getIQFactory().createUnaryIQTree(rootNode, newChild);
        }

        /**
         * TODO: support inner joi
         */
        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            return super.transformInnerJoin(tree, rootNode, children);
        }

        /**
         * TODO: support filter
         */
        @Override
        public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
            return super.transformFilter(tree, rootNode, child);
        }

        @Override
        public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<IQTree> newChildren = children.stream()
                    .map(this::transform)
                    .collect(ImmutableCollectors.toList());

            return newChildren.equals(children)
                    ? tree
                    : coreSingletons.getIQFactory().createNaryIQTree(rootNode, newChildren);
        }

        /**
         * By default, switch back to the "LookForDistinctTransformer"
         */
        protected IQTree transformUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
            return lookForDistinctTransformer.transform(tree);
        }

        /**
         * By default, switch back to the "LookForDistinctTransformer"
         */
        protected IQTree transformNaryCommutativeNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
            return lookForDistinctTransformer.transform(tree);
        }

        /**
         * By default, switch back to the "LookForDistinctTransformer"
         */
        protected IQTree transformBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                           IQTree leftChild, IQTree rightChild) {
            return lookForDistinctTransformer.transform(tree);
        }
    }



}
