package it.unibz.inf.ontop.pivotalrepr.impl.tree;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode;
import it.unibz.inf.ontop.pivotalrepr.impl.IllegalTreeUpdateException;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * TODO: explain
 */
public interface ChildrenRelation {

    TreeNode getParent();

    ImmutableList<TreeNode> getChildren();

    Stream<TreeNode> getChildrenStream();

    boolean contains(TreeNode node);

    void addChild(TreeNode childNode, Optional<NonCommutativeOperatorNode.ArgumentPosition> optionalPosition, boolean canReplace)
            throws IllegalTreeUpdateException;

    void replaceChild(TreeNode formerChild, TreeNode newChild);

    void removeChild(TreeNode childNode);

    ImmutableList<QueryNode> getChildQueryNodes();

    Stream<QueryNode> getChildQueryNodeStream();

    Optional<NonCommutativeOperatorNode.ArgumentPosition> getOptionalPosition(TreeNode childTreeNode);

    Optional<TreeNode> getChild(NonCommutativeOperatorNode.ArgumentPosition position);

    ChildrenRelation clone(Map<QueryNode, TreeNode> newNodeIndex);

    /**
     * May return itself (no cloning)
     */
    ChildrenRelation convertToBinaryChildrenRelation();

    /**
     * May return itself (no cloning)
     */
    ChildrenRelation convertToStandardChildrenRelation();
}
