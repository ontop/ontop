package it.unibz.inf.ontop.protege.gui.models;

/*
 * #%L
 * ontop-protege4
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.inf.ontop.protege.core.QueryManager;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class QueryManagerTreeModel extends DefaultTreeModel implements QueryManager.EventListener {

	private static final long serialVersionUID = -5182895959682699380L;

	public static class GroupNode extends DefaultMutableTreeNode implements MutableTreeNode {

		private static final long serialVersionUID = 7496292557025215559L;

		private final String groupId;

		public GroupNode(String groupId) {
			this.groupId = groupId;
			setUserObject(groupId);
		}

		public String getGroupID() {
			return groupId;
		}
	}

	public static class QueryNode extends DefaultMutableTreeNode implements MutableTreeNode {

		private static final long serialVersionUID = -5221902062065891204L;

		private final String groupId, queryId;
		private String query;

		public QueryNode(String groupId, String queryId, String query) {
			this.groupId = groupId;
			this.queryId = queryId;
			this.query = query;
			setUserObject(queryId);
			setAllowsChildren(false);
		}

		public String getGroupID() {
			return groupId;
		}

		public String getQueryID() {
			return queryId;
		}

		public String getQuery() {
			return query;
		}

		public void setQuery(String query) {
			this.query = query;
		}
	}


	public QueryManagerTreeModel() {
		super(new DefaultMutableTreeNode(""));
	}

	/**
	 * Takes all the existing nodes and constructs the tree.
	 */
	public void synchronize(QueryManager qc) {
		for (QueryManager.Group group : qc.getGroups()) {
			if (!group.isDegenerate()) {
				GroupNode groupNode = new GroupNode(group.getID());
				for (QueryManager.Query query : group.getQueries()) {
					QueryNode queryNode = new QueryNode(group.getID(), query.getID(), query.getQuery());
					insertNodeInto(queryNode, groupNode, groupNode.getChildCount());
				}
				insertNodeInto(groupNode, (DefaultMutableTreeNode) root, root.getChildCount());
			}
			else {
				QueryManager.Query query = group.getQueries().iterator().next();
				QueryNode queryNode = new QueryNode(null, query.getID(), query.getQuery());
				insertNodeInto(queryNode, (DefaultMutableTreeNode) root, root.getChildCount());
			}
		}
	}

	@Override
	public void added(QueryManager.Group group) {
		GroupNode groupNode = new GroupNode(group.getID());
		insertNodeInto(groupNode, (MutableTreeNode) root, root.getChildCount());
		nodeStructureChanged(root);
	}

	@Override
	public void added(QueryManager.Query query) {
		MutableTreeNode parent = (query.getGroup().isDegenerate())
			? (MutableTreeNode)root
			: getGroupNode(query.getGroup());

		QueryNode queryNode = new QueryNode(query.getGroup().getID(), query.getID(), query.getQuery());
		insertNodeInto(queryNode, parent, parent.getChildCount());
		nodeStructureChanged(root);
	}

	@Override
	public void removed(QueryManager.Group group) {
		GroupNode groupNode = getGroupNode(group);
		removeNodeFromParent(groupNode);
		nodeStructureChanged(root);
	}

	@Override
	public void removed(QueryManager.Query query) {
		QueryNode queryNode = getQueryNode(query);
		removeNodeFromParent(queryNode);
		nodeStructureChanged(root);
	}

	@Override
	public void changed(QueryManager.Query query) {
		QueryNode queryNode = getQueryNode(query);
		queryNode.setQuery(query.getQuery());
		nodeChanged(queryNode);
	}

	public QueryNode getQueryNode(QueryManager.Query query) {
		if (!query.getGroup().isDegenerate()) {
			GroupNode groupNode = getGroupNode(query.getGroup());
			return getQueryNode(groupNode, query.getID());
		}
		else
			return getQueryNode(root, query.getID());
	}

	public GroupNode getGroupNode(QueryManager.Group group) {
		String groupId = group.getID();
		Enumeration<? extends TreeNode> parent = root.children();
		while (parent.hasMoreElements()) {
			TreeNode node = parent.nextElement();
			if (node instanceof GroupNode) {
				GroupNode groupNode = (GroupNode) node;
				if (groupNode.getGroupID().equals(groupId)) {
					return groupNode;
				}
			}
		}
		throw new IllegalArgumentException("GroupNode " + groupId + " not found");
	}

	private QueryNode getQueryNode(TreeNode root, String queryId) {
		Enumeration<? extends TreeNode> parent = root.children();
		while (parent.hasMoreElements()) {
			TreeNode node = parent.nextElement();
			if (node instanceof QueryNode) {
				QueryNode queryNode = (QueryNode) node;
				if (queryNode.getQueryID().equals(queryId)) {
					return queryNode;
				}
			}
		}
		throw new IllegalArgumentException("QueryNode " + queryId + " not found");
	}

}
