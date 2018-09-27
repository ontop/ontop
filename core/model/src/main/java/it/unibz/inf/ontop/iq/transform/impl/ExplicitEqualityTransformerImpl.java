package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.ExplicitEqualityTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;

public class ExplicitEqualityTransformerImpl implements ExplicitEqualityTransformer {

    private final ImmutableList<IQTreeTransformer> preTransformers;
    private final ImmutableList<IQTreeTransformer> postTransformers;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    public ExplicitEqualityTransformerImpl(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
        this.preTransformers = ImmutableList.of(new CnLifter());
        this.postTransformers = ImmutableList.of(new CnLifter());
    }

//    private IQTree transform(QueryNode root,
//                             ImmutableList<IQTree> children,
//                             CNLifter CNLifter,
//                             Updater updater){
//
//        ImmutableList<IQTree> transformedChildren = children.stream()
//                .map(t -> t.acceptTransformer(this))
//                .collect(ImmutableCollectors.toList());
//
//        IQTree combination = CNLifter.liftCn(root, transformedChildren);
//        return updater.update(combination);


    @Override
    public IQTree transform(IQTree tree) {
        return new CompositeRecursiveIQTreeTransformer(preTransformers, postTransformers, iqFactory).transform(tree);
    }

    /**
     * Affects (left) joins and data nodes.
     * - (left) join: if the same variable is returned by both operands (implicit equality), rename it in the right operand and make the euality explicit
     * - data node: create a variable and make the equality explicit (create a filter).
     */
    class LocalExplicitEqualityEnforcer extends DefaultNonRecursiveIQTreeTransformer {

        @Override
        public IQTree transformIntensionalData(IntensionalDataNode dataNode) {
            return transformDatanode(dataNode);
        }

        @Override
        public IQTree transformExtensionalData(ExtensionalDataNode dataNode) {
            return transformDatanode(dataNode);
        }

        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        }

        private IQTree transformDatanode(DataNode dataNode) {

        }
    }

    /**
     * Affects filters and (left) joins.
     * For each child, deletes its root if it is a filter node.
     * Then:
     * - join and filter: merge the boolean expression
     * - left join: merge boolean expressions coming from the right, and lift the ones coming from the left.
     * This lift is only performed for optimization purposes: may avoid a subquery during SQL generation.
     */
    class FilterChildNormalizer extends DefaultNonRecursiveIQTreeTransformer {

        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        }

        @Override
        public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
        }
    }

    /**
     * For each child, deletes its root if it is a substitution-free construction node (i.e. a simple projection).
     * Then lift the projection if needed (i.e. create a substitution-free construction node above the current one)
     */
    class CnLifter extends DefaultNonRecursiveIQTreeTransformer {

        @Override
        protected IQTree transformUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
            ImmutableList<ConstructionNode> idleCns = getIdleCns(Stream.of(child));
            return idleCns.isEmpty() ?
                    tree :
                    iqFactory.createUnaryIQTree(
                            idleCns.iterator().next(),
                            iqFactory.createUnaryIQTree(
                                    rootNode,
                                    trimIdleCn(child)
                            ));
        }

        @Override
        protected IQTree transformNaryCommutativeNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<ConstructionNode> idleCns = getIdleCns(children.stream());
            return idleCns.isEmpty() ?
                    tree :
                    iqFactory.createUnaryIQTree(
                            mergeProjections(idleCns),
                            iqFactory.createNaryIQTree(
                                    rootNode,
                                    children.stream()
                                            .map(t -> trimIdleCn(t))
                                            .collect(ImmutableCollectors.toList())
                            ));
        }

        @Override
        protected IQTree transformBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
            ImmutableList<IQTree> children = ImmutableList.of(leftChild, rightChild);
            ImmutableList<ConstructionNode> idleCns = getIdleCns(Stream.of(leftChild, rightChild));
            return idleCns.isEmpty() ?
                    tree :
                    iqFactory.createUnaryIQTree(
                            mergeProjections(idleCns),
                            iqFactory.createBinaryNonCommutativeIQTree(
                                    rootNode,
                                    trimIdleCn(leftChild),
                                    trimIdleCn(rightChild)
                            ));
        }

        private ImmutableList<ConstructionNode> getIdleCns(Stream<IQTree> trees) {
            return trees
                    .map(t -> getIdleCn(t))
                    .filter(o -> o.isPresent())
                    .map(o -> o.get())
                    .collect(ImmutableCollectors.toList());
        }

        private ConstructionNode mergeProjections(ImmutableList<ConstructionNode> idleCns) {
            return iqFactory.createConstructionNode(idleCns.stream()
                    .flatMap(c -> c.getVariables().stream())
                    .collect(ImmutableCollectors.toSet())
            );
        }

        private Optional<ConstructionNode> getIdleCn(IQTree tree) {
            QueryNode root = tree.getRootNode();
            if (root instanceof ConstructionNode) {
                ConstructionNode cn = ((ConstructionNode) root);
                if (cn.getSubstitution().isEmpty()) {
                    return Optional.of(cn);
                }
            }
            return Optional.empty();
        }

        private IQTree trimIdleCn(IQTree tree) {
            return getIdleCn(tree).isPresent() ?
                    ((UnaryIQTree) tree).getChild() :
                    tree;
        }
    }
}