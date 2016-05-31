package it.unibz.inf.ontop.pivotalrepr.impl.tree;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode;
import it.unibz.inf.ontop.pivotalrepr.impl.IllegalTreeUpdateException;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;

import java.util.Optional;

/**
 * TODO: explain
 */
public interface ChildrenRelation {

    TreeNode getParent();

    ImmutableList<TreeNode> getChildren();

    boolean contains(TreeNode node);

    void addChild(TreeNode childNode, Optional<NonCommutativeOperatorNode.ArgumentPosition> optionalPosition, boolean canReplace)
            throws IllegalTreeUpdateException;

    void replaceChild(TreeNode formerChild, TreeNode newChild);

    void removeChild(TreeNode childNode);

    ImmutableList<QueryNode> getChildQueryNodes();

    Optional<NonCommutativeOperatorNode.ArgumentPosition> getOptionalPosition(TreeNode childTreeNode);
}
