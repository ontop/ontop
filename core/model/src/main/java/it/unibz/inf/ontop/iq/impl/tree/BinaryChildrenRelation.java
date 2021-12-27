package it.unibz.inf.ontop.iq.impl.tree;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode;
import it.unibz.inf.ontop.iq.exception.IllegalTreeUpdateException;
import it.unibz.inf.ontop.iq.node.QueryNode;

import java.util.Map;
import java.util.Optional;

/**
 * TODO: explain
 */
public class BinaryChildrenRelation implements ChildrenRelation {

    private final TreeNode parent;
    private Optional<TreeNode> optionalLeftChild;
    private Optional<TreeNode> optionalRightChild;


    protected BinaryChildrenRelation(TreeNode parent) {
        if (!(parent.getQueryNode() instanceof BinaryOrderedOperatorNode)) {
            throw new IllegalArgumentException("The StandardChildrenRelation requires " +
                    "BinaryAsymmetricOperatorNode as parents");
        }

        this.parent = parent;
        this.optionalLeftChild = Optional.empty();
        this.optionalRightChild = Optional.empty();
    }

    private BinaryChildrenRelation(TreeNode parent, Optional<TreeNode> optionalLeftChild,
                                   Optional<TreeNode> optionalRightChild) {
        this.parent = parent;
        this.optionalLeftChild = optionalLeftChild;
        this.optionalRightChild = optionalRightChild;
    }


    @Override
    public ImmutableList<TreeNode> getChildren() {
        ImmutableList.Builder<TreeNode> builder = ImmutableList.builder();
        if (optionalLeftChild.isPresent()) {
            builder.add(optionalLeftChild.get());
        }
        if (optionalRightChild.isPresent()) {
            builder.add(optionalRightChild.get());
        }
        return builder.build();
    }

    @Override
    public void addChild(TreeNode childNode, Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalPosition)
            throws IllegalTreeUpdateException {
        if (!optionalPosition.isPresent()) {
            throw new IllegalArgumentException("The BinaryChildrenRelation requires argument positions");
        }

        switch (optionalPosition.get()) {
            case LEFT:
                if (optionalLeftChild.isPresent() && (optionalLeftChild.get() != childNode)) {
                    throw new IllegalTreeUpdateException("Left child node is already present");
                }
                else {
                    optionalLeftChild = Optional.of(childNode);
                }
                break;
            case RIGHT:
                if (optionalRightChild.isPresent() && (optionalRightChild.get() != childNode)) {
                        throw new IllegalTreeUpdateException("Right child node is already present");
                }
                else {
                    optionalRightChild = Optional.of(childNode);
                }
                break;
        }
    }

    @Override
    public ChildrenRelation clone(Map<QueryNode, TreeNode> newNodeIndex) {
        return new BinaryChildrenRelation(parent.findNewTreeNode(newNodeIndex),
                optionalLeftChild.map(n -> n.findNewTreeNode(newNodeIndex)),
                optionalRightChild.map(n -> n.findNewTreeNode(newNodeIndex))
                );
    }
}
