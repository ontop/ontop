package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.ExplicitEqualityTransformer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

public class ExplicitEqualityTransformerImpl extends DefaultRecursiveIQTransformer implements ExplicitEqualityTransformer {


    @Inject
    public ExplicitEqualityTransformerImpl(IntermediateQueryFactory iqFactory) {
        super(iqFactory);
    }

    private IQTree transform(QueryNode root,
                             ImmutableList<IQTree> children,
                             CNLifter CNLifter,
                             Updater updater){

        ImmutableList<IQTree> transformedChildren = children.stream()
                .map(t -> t.acceptTransformer(this))
                .collect(ImmutableCollectors.toList());

        IQTree combination = CNLifter.liftCn(root, transformedChildren);
        return updater.update(combination);

    }

    protected IQTree transformUnaryNode(UnaryOperatorNode rootNode, IQTree child) {
        transform()
        IQTree transformedChild = child.acceptTransformer(this);
        return combine(rootNode, transformedChild);
    }

    protected IQTree transformNaryCommutativeNode(NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        return combine(
                rootNode,
                children.stream()
                        .map(t -> t.acceptTransformer(this))
                        .collect(ImmutableCollectors.toList())
        );
    }

    private IQTree combine(UnaryOperatorNode rootNode, IQTree transformedChild) {
        Optional<ConstructionNode> idleCn = getIdleCn(transformedChild);
        return idleCn.isPresent() ?
                liftIdleCn(idleCn.get(), rootNode, trimIdleCn(transformedChild)) :
                iqFactory.createUnaryIQTree(rootNode, transformedChild);
    }

    private IQTree combine(NaryOperatorNode rootNode, ImmutableList<IQTree> transformedChildren) {

        ImmutableList<ConstructionNode> idleCns = transformedChildren.stream()
                .map(t -> getIdleCn(t))
                .filter(o -> o.isPresent())
                .map(o -> o.get())
                .collect(ImmutableCollectors.toList());

        return idleCns.isEmpty() ?
                iqFactory.createNaryIQTree(rootNode, transformedChildren) :
                liftIdleCns(
                        mergeIdleCns(idleCns),
                        rootNode,
                        transformedChildren.stream()
                                .map(t -> trimIdleCn(t))
                                .collect(ImmutableCollectors.toList())
                );
    }

    private IQTree trimIdleCn(IQTree tree) {
        return getIdleCn(tree).isPresent() ?
                ((UnaryIQTree) tree).getChild() :
                tree;
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

    private ConstructionNode mergeIdleCns(ImmutableList<ConstructionNode> cns) {
        return iqFactory.createConstructionNode(cns.stream()
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet())
        );
    }

    private IQTree liftIdleCn(ConstructionNode constructionNode, UnaryOperatorNode rootNode, IQTree trimmedChild) {
        return iqFactory.createUnaryIQTree(
                constructionNode,
                iqFactory.createUnaryIQTree(
                        rootNode,
                        trimmedChild
                ));
    }

    private IQTree liftIdleCns(ConstructionNode cn, NaryOperatorNode root, ImmutableList<IQTree> trimmedChildren) {
        return iqFactory.createUnaryIQTree(
                cn,
                iqFactory.createNaryIQTree(
                        root,
                        trimmedChildren
                ));
    }

    class UnionCNLifter implements CNLifter<UnionNode> {

        @Override
        public IQTree combine(UnionNode root, ImmutableList<IQTree> children) {

        }
    }
}