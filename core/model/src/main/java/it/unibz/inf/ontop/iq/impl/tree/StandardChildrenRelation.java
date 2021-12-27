package it.unibz.inf.ontop.iq.impl.tree;

import java.util.Map;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode;
import it.unibz.inf.ontop.iq.node.QueryNode;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO: explain
 */
public class StandardChildrenRelation implements ChildrenRelation {

    private final List<TreeNode> children;
    private final TreeNode parent;

    protected StandardChildrenRelation(TreeNode parent) {
        if (parent.getQueryNode() instanceof BinaryOrderedOperatorNode) {
            throw new IllegalArgumentException("The StandardChildrenRelation does not accept " +
                    "BinaryOrderedOperatorNodes as parents");
        }
        this.parent = parent;
        this.children = new LinkedList<>();
    }

    private StandardChildrenRelation(TreeNode parent, List<TreeNode> children) {
        this.parent = parent;
        this.children = children;
    }

    @Override
    public ImmutableList<TreeNode> getChildren() {
        return ImmutableList.copyOf(children);
    }

    @Override
    public void addChild(TreeNode childNode, Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalPosition) {
        if (optionalPosition.isPresent()) {
            throw new IllegalArgumentException("The StandardChildrenRelation does not accept argument positions");
        }
        if (!children.contains(childNode)) {
            children.add(childNode);
        }
    }

    @Override
    public ChildrenRelation clone(Map<QueryNode, TreeNode> newNodeIndex) {
        return new StandardChildrenRelation(parent.findNewTreeNode(newNodeIndex),
                children.stream()
                        .map(c -> c.findNewTreeNode(newNodeIndex))
                        .collect(Collectors.toList()));
    }
}
