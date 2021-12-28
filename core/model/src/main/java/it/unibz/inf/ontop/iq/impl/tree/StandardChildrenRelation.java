package it.unibz.inf.ontop.iq.impl.tree;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode;
import it.unibz.inf.ontop.iq.node.QueryNode;

import java.util.LinkedList;
import java.util.List;

/**
 * TODO: explain
 */
public class StandardChildrenRelation implements ChildrenRelation {

    private final List<QueryNode> children;

    protected StandardChildrenRelation(QueryNode parent) {
        if (parent instanceof BinaryOrderedOperatorNode) {
            throw new IllegalArgumentException("The StandardChildrenRelation does not accept " +
                    "BinaryOrderedOperatorNodes as parents");
        }
        this.children = new LinkedList<>();
    }

    @Override
    public ImmutableList<QueryNode> getChildren() {
        return ImmutableList.copyOf(children);
    }

    @Override
    public void addChild(QueryNode childNode, Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalPosition) {
        if (optionalPosition.isPresent()) {
            throw new IllegalArgumentException("The StandardChildrenRelation does not accept argument positions");
        }
        if (!children.contains(childNode)) {
            children.add(childNode);
        }
    }
}
