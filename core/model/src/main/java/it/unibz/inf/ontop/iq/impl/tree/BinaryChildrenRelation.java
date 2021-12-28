package it.unibz.inf.ontop.iq.impl.tree;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode;
import it.unibz.inf.ontop.iq.exception.IllegalTreeUpdateException;
import it.unibz.inf.ontop.iq.node.QueryNode;

import java.util.Optional;

/**
 * TODO: explain
 */
public class BinaryChildrenRelation implements ChildrenRelation {

    private Optional<QueryNode> optionalLeftChild;
    private Optional<QueryNode> optionalRightChild;


    protected BinaryChildrenRelation(QueryNode parent) {
        if (!(parent instanceof BinaryOrderedOperatorNode)) {
            throw new IllegalArgumentException("The StandardChildrenRelation requires " +
                    "BinaryAsymmetricOperatorNode as parents");
        }

        this.optionalLeftChild = Optional.empty();
        this.optionalRightChild = Optional.empty();
    }


    @Override
    public ImmutableList<QueryNode> getChildren() {
        ImmutableList.Builder<QueryNode> builder = ImmutableList.builder();
        if (optionalLeftChild.isPresent()) {
            builder.add(optionalLeftChild.get());
        }
        if (optionalRightChild.isPresent()) {
            builder.add(optionalRightChild.get());
        }
        return builder.build();
    }

    @Override
    public void addChild(QueryNode childNode, Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalPosition)
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
}
