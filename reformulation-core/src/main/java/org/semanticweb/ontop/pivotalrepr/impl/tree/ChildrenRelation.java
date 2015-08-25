package org.semanticweb.ontop.pivotalrepr.impl.tree;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.BinaryAsymmetricOperatorNode.ArgumentPosition;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.impl.IllegalTreeUpdateException;

/**
 * TODO: explain
 */
public interface ChildrenRelation {

    TreeNode getParent();

    ImmutableList<TreeNode> getChildren();

    boolean contains(TreeNode node);

    void addChild(TreeNode childNode, Optional<ArgumentPosition> optionalPosition) throws IllegalTreeUpdateException;

    void removeChild(TreeNode childNode);

    ImmutableList<QueryNode> getChildQueryNodes();
}
