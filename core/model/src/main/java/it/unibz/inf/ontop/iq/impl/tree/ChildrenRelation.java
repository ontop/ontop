package it.unibz.inf.ontop.iq.impl.tree;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode;
import it.unibz.inf.ontop.iq.exception.IllegalTreeUpdateException;
import it.unibz.inf.ontop.iq.node.QueryNode;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * TODO: explain
 */
public interface ChildrenRelation {

    ImmutableList<TreeNode> getChildren();

    boolean contains(TreeNode node);

    void addChild(TreeNode childNode, Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalPosition, boolean canReplace)
            throws IllegalTreeUpdateException;

    void removeChild(TreeNode childNode);

    ImmutableList<QueryNode> getChildQueryNodes();

    ChildrenRelation clone(Map<QueryNode, TreeNode> newNodeIndex);
}
