package org.semanticweb.ontop.pivotalrepr.impl.tree;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.BinaryAsymmetricOperatorNode;
import org.semanticweb.ontop.pivotalrepr.QueryNode;

import java.util.LinkedList;
import java.util.List;

/**
 * TODO: explain
 */
public class StandardChildrenRelation implements ChildrenRelation {

    private final List<TreeNode> children;
    private final TreeNode parent;

    protected StandardChildrenRelation(TreeNode parent) {
        if (parent.getQueryNode() instanceof BinaryAsymmetricOperatorNode) {
            throw new IllegalArgumentException("The StandardChildrenRelation does not accept " +
                    "BinaryAsymmetricOperatorNode as parents");
        }
        this.parent = parent;
        this.children = new LinkedList<>();
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public ImmutableList<TreeNode> getChildren() {
        return ImmutableList.copyOf(children);
    }

    @Override
    public boolean contains(TreeNode node) {
        return children.contains(node);
    }

    @Override
    public void addChild(TreeNode childNode, Optional<BinaryAsymmetricOperatorNode.ArgumentPosition> optionalPosition) {
        if (optionalPosition.isPresent()) {
            throw new IllegalArgumentException("The StandardChildrenRelation does not accept argument positions");
        }
        if (!contains(childNode)) {
            children.add(childNode);
        }
    }

    @Override
    public void replaceChild(TreeNode formerChild, TreeNode newChild) {
        int index = children.indexOf(formerChild);
        switch(index) {
            case -1:
                throw new IllegalArgumentException("The former child is not in the child relation");
            default:
                children.set(index, newChild);
        }
    }

    @Override
    public void removeChild(TreeNode childNode) {
        if (contains(childNode)) {
            children.remove(childNode);
        }
    }

    @Override
    public ImmutableList<QueryNode> getChildQueryNodes() {
        ImmutableList.Builder<QueryNode> builder = ImmutableList.builder();
        for (TreeNode treeNode : children) {
            builder.add(treeNode.getQueryNode());
        }
        return builder.build();
    }

    @Override
    public Optional<BinaryAsymmetricOperatorNode.ArgumentPosition> getOptionalPosition(TreeNode childTreeNode) {
        return Optional.absent();
    }
}
