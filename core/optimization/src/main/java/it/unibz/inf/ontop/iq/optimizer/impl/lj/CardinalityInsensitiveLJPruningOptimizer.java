package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.BinaryNonCommutativeIQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.LeftJoinIQOptimizer;
import it.unibz.inf.ontop.iq.optimizer.impl.LookForDistinctOrLimit1TransformerImpl;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultNonRecursiveIQTreeTransformer;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

/**
 * Prunes right children when their variables are not used outside the LJ
 * in a cardinality-insensitive context
 */
@Singleton
public class CardinalityInsensitiveLJPruningOptimizer implements LeftJoinIQOptimizer {
    private final CoreSingletons coreSingletons;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected CardinalityInsensitiveLJPruningOptimizer(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
        this.iqFactory = coreSingletons.getIQFactory();
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree initialTree = query.getTree();

        IQTreeVisitingTransformer transformer = new LookForDistinctOrLimit1TransformerImpl(
                (childTree, parentTransformer) -> new CardinalityInsensitiveLJPruningTransformer(
                        parentTransformer,
                        coreSingletons,
                        childTree.getVariables()),
                coreSingletons);

        IQTree newTree = transformer.transform(initialTree);

        return newTree.equals(initialTree)
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }

    protected static class CardinalityInsensitiveLJPruningTransformer extends DefaultNonRecursiveIQTreeTransformer {

        private final IQTreeTransformer lookForDistinctTransformer;
        private final CoreSingletons coreSingletons;
        private final ImmutableSet<Variable> variablesUsedByAncestors;
        private final IntermediateQueryFactory iqFactory;

        protected CardinalityInsensitiveLJPruningTransformer(IQTreeTransformer lookForDistinctTransformer,
                                                             CoreSingletons coreSingletons,
                                                             ImmutableSet<Variable> variablesUsedByAncestors) {
            this.lookForDistinctTransformer = lookForDistinctTransformer;
            this.coreSingletons = coreSingletons;
            this.variablesUsedByAncestors = variablesUsedByAncestors;
            this.iqFactory = coreSingletons.getIQFactory();
        }

        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
            var newVariablesUsed = Sets.union(variablesUsedByAncestors, rootNode.getLocallyRequiredVariables());

            var newTransformer = newVariablesUsed.equals(variablesUsedByAncestors)
                    ? this
                    : computeNewTransformer(newVariablesUsed.immutableCopy());

            IQTree newChild = newTransformer.transform(child);
            return newChild.equals(child) && rootNode.equals(tree.getRootNode())
                    ? tree
                    : iqFactory.createUnaryIQTree(rootNode, newChild);
        }

        @Override
        public IQTree transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
            var newTransformer = rootNode.getOptionalFilterCondition()
                    .map(ImmutableFunctionalTerm::getVariables)
                    .filter(vs -> !vs.isEmpty())
                    .map(vs -> Sets.union(variablesUsedByAncestors, vs).immutableCopy())
                    .map(this::computeNewTransformer)
                    .orElse(this);

            IQTree newChild = newTransformer.transform(child);
            return newChild.equals(child) && rootNode.equals(tree.getRootNode())
                    ? tree
                    : iqFactory.createUnaryIQTree(rootNode, newChild);
        }

        @Override
        public IQTree transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
            return applyRecursivelyToUnaryNode(tree, rootNode, child);
        }

        protected IQTree applyRecursivelyToUnaryNode(UnaryIQTree tree, UnaryOperatorNode rootNode, IQTree child) {
            IQTree newChild = transform(child);
            return newChild.equals(child) && rootNode.equals(tree.getRootNode())
                    ? tree
                    : iqFactory.createUnaryIQTree(rootNode, newChild);
        }

        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            var treeVariables = tree.getVariables();

            if (treeVariables.isEmpty() || leftChild.getVariables().containsAll(Sets.intersection(variablesUsedByAncestors, treeVariables)))
                // Prunes the right child
                return transform(leftChild);

            var commonVariables = BinaryNonCommutativeIQTreeTools.commonVariables(leftChild, rightChild);

            var newVariablesUsed = rootNode.getOptionalFilterCondition()
                    .map(ImmutableFunctionalTerm::getVariables)
                    .filter(vs -> !vs.isEmpty())
                    .map(vs -> Sets.union(variablesUsedByAncestors, Sets.union(vs, commonVariables)).immutableCopy())
                    .orElse(Sets.union(variablesUsedByAncestors, commonVariables).immutableCopy());

            var newTransformer = newVariablesUsed.equals(variablesUsedByAncestors)
                    ? this
                    : computeNewTransformer(newVariablesUsed);

            var newLeft = newTransformer.transform(leftChild);
            var newRight = newTransformer.transform(rightChild);

            return newLeft.equals(leftChild) && newRight.equals(rightChild)
                    ? tree
                    : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeft, newRight);
        }

        @Override
        public IQTree transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
            return applyRecursivelyToNaryNode(tree, rootNode, children);
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            var newTransformer = rootNode.getOptionalFilterCondition()
                    .map(ImmutableFunctionalTerm::getVariables)
                    .filter(vs -> !vs.isEmpty())
                    .map(vs -> Sets.union(variablesUsedByAncestors, vs).immutableCopy())
                    .map(this::computeNewTransformer)
                    .orElse(this);

            ImmutableList<IQTree> newChildren = NaryIQTreeTools.transformChildren(children,
                    newTransformer::transform);

            return newChildren.equals(children) && rootNode.equals(tree.getRootNode())
                    ? tree
                    : iqFactory.createNaryIQTree(rootNode, newChildren);
        }

        protected IQTree applyRecursivelyToNaryNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<IQTree> newChildren = NaryIQTreeTools.transformChildren(children,
                    this::transform);

            return newChildren.equals(children) && rootNode.equals(tree.getRootNode())
                    ? tree
                    : iqFactory.createNaryIQTree(rootNode, newChildren);
        }

        /**
         * Default behavior
         */
        @Override
        protected IQTree transformUnaryNode(UnaryIQTree tree, UnaryOperatorNode rootNode, IQTree child) {
            return lookForDistinctTransformer.transform(tree);
        }

        /**
         * Default behavior
         */
        @Override
        protected IQTree transformNaryCommutativeNode(NaryIQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
            return lookForDistinctTransformer.transform(tree);
        }

        /**
         * Default behavior
         */
        @Override
        protected IQTree transformBinaryNonCommutativeNode(BinaryNonCommutativeIQTree tree, BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
            return lookForDistinctTransformer.transform(tree);
        }

        private CardinalityInsensitiveLJPruningTransformer computeNewTransformer(ImmutableSet<Variable> newVariablesUsedByAncestors) {
            return new CardinalityInsensitiveLJPruningTransformer(lookForDistinctTransformer, coreSingletons, newVariablesUsedByAncestors);
        }
    }
}
