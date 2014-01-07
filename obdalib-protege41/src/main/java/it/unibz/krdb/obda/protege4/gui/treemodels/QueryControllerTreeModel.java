package it.unibz.krdb.obda.protege4.gui.treemodels;

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

import it.unibz.krdb.obda.protege4.gui.treemodels.QueryGroupTreeElement;
import it.unibz.krdb.obda.protege4.gui.treemodels.QueryTreeElement;
import it.unibz.krdb.obda.protege4.gui.treemodels.TreeElement;
import it.unibz.krdb.obda.querymanager.QueryControllerEntity;
import it.unibz.krdb.obda.querymanager.QueryControllerGroup;
import it.unibz.krdb.obda.querymanager.QueryControllerListener;
import it.unibz.krdb.obda.querymanager.QueryControllerQuery;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class QueryControllerTreeModel extends DefaultTreeModel implements QueryControllerListener {

	private static final long serialVersionUID = -5182895959682699380L;

	public QueryControllerTreeModel() {
		super(new DefaultMutableTreeNode(""));
	}

	/**
	 * Takes all the existing nodes and constructs the tree.
	 */
	public void synchronize(List<QueryControllerEntity> queryEntities) {
	  if (queryEntities.size() > 0) {
			for (QueryControllerEntity queryEntity : queryEntities) {
				if (queryEntity instanceof QueryControllerGroup) {
					QueryControllerGroup group = (QueryControllerGroup) queryEntity;
					QueryGroupTreeElement queryGroupEle = new QueryGroupTreeElement(group.getID());
					Vector<QueryControllerQuery> queries = group.getQueries();
					for (QueryControllerQuery query : queries) {
						QueryTreeElement queryTreeEle = new QueryTreeElement(query.getID(), query.getQuery());
						insertNodeInto(queryTreeEle, queryGroupEle, queryGroupEle.getChildCount());
					}
					insertNodeInto(queryGroupEle, (DefaultMutableTreeNode) root, root.getChildCount());
				} else {
					QueryControllerQuery query = (QueryControllerQuery) queryEntity;
					QueryTreeElement queryTreeEle = new QueryTreeElement(query.getID(), query.getQuery());
					insertNodeInto(queryTreeEle, (DefaultMutableTreeNode) root, root.getChildCount());
				}
			}
		}
	}

	/**
	 * Remove all the nodes from the Tree
	 */
	public void reset() {
		Enumeration<TreeNode> children = root.children();
		while (children.hasMoreElements()) {
			removeNodeFromParent((MutableTreeNode) children.nextElement());
			children = root.children();
		}
	}

	/**
	 * Inserts a new node group or query into the Tree
	 */
	public void elementAdded(QueryControllerEntity element) {
		if (element instanceof QueryControllerGroup) {
			QueryControllerGroup group = (QueryControllerGroup) element;
			QueryGroupTreeElement ele = new QueryGroupTreeElement(group.getID());
			insertNodeInto(ele, (DefaultMutableTreeNode) root, root.getChildCount());
			nodeStructureChanged(root);
		} else if (element instanceof QueryControllerQuery) {
			QueryControllerQuery query = (QueryControllerQuery) element;
			QueryTreeElement ele = new QueryTreeElement(query.getID(), query.getQuery());
			insertNodeInto(ele, (DefaultMutableTreeNode) root, root.getChildCount());
			nodeStructureChanged(root);
		}
	}

	/**
	 * Removes a TreeNode group or query from the Tree
	 */
	public void elementRemoved(QueryControllerEntity element) {
		if (element instanceof QueryControllerGroup) {
			QueryControllerGroup group = (QueryControllerGroup) element;
			QueryGroupTreeElement ele = new QueryGroupTreeElement(group.getID());
			Enumeration<TreeNode> groups = root.children();
			while (groups.hasMoreElements()) {
				Object temporal = groups.nextElement();
				if (!(temporal instanceof QueryGroupTreeElement)) {
					continue;
				}
				QueryGroupTreeElement groupTElement = (QueryGroupTreeElement) temporal;
				if (groupTElement.getID().equals(ele.getID())) {
					removeNodeFromParent(groupTElement);
					nodeStructureChanged(root);
					break;
				}
			}
		} else if (element instanceof QueryControllerQuery) {
			QueryControllerQuery query = (QueryControllerQuery) element;
			QueryTreeElement elementQuery = new QueryTreeElement(query.getID(), query.getQuery());
			Enumeration<TreeNode> elements = root.children();
			while (elements.hasMoreElements()) {
				TreeNode currentElement = elements.nextElement();
				if (currentElement instanceof QueryGroupTreeElement) {
					continue;
				}
				if (currentElement instanceof QueryTreeElement) {
					QueryTreeElement deleteQuery = (QueryTreeElement) currentElement;
					if (deleteQuery.getID().equals(elementQuery.getID())) {
						removeNodeFromParent(deleteQuery);
						nodeStructureChanged(root);
						break;
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	/*
	 * * Inserts a new TreeNode query in a group into the tree
	 */
	public void elementAdded(QueryControllerQuery query, QueryControllerGroup group) {
		QueryGroupTreeElement groupTElement = null;
		QueryTreeElement elemQ = new QueryTreeElement(query.getID(), query.getQuery());
		Enumeration<TreeNode> groups = root.children();
		while (groups.hasMoreElements()) {
			Object temporal = groups.nextElement();
			if (!(temporal instanceof QueryGroupTreeElement)) {
				continue;
			}
			groupTElement = (QueryGroupTreeElement) temporal;
			if (groupTElement.getID().equals(group.getID())) {
				insertNodeInto(elemQ, groupTElement, groupTElement.getChildCount());
				nodeStructureChanged(root);
				break;
			}
		}
	}

	@Override
	/*
	 * * Removes a TreeNode query from a group into the tree
	 */
	public void elementRemoved(QueryControllerQuery query, QueryControllerGroup group) {
		QueryGroupTreeElement ele = new QueryGroupTreeElement(group.getID());
		QueryTreeElement elemQ = new QueryTreeElement(query.getID(), query.getQuery());

		Enumeration<TreeNode> groups = root.children();
		while (groups.hasMoreElements()) {
			TreeNode node = groups.nextElement();
			if (node instanceof QueryGroupTreeElement) {
			QueryGroupTreeElement groupTElement = (QueryGroupTreeElement) node;
				if (groupTElement.getID().equals(ele.getID())) {
					Enumeration<TreeNode> queries = groupTElement.children();
					while (queries.hasMoreElements()) {
						QueryTreeElement queryTElement = (QueryTreeElement) queries.nextElement();
						if (queryTElement.getID().equals(elemQ.getID())) {
							removeNodeFromParent(queryTElement);
							nodeStructureChanged(root);
							break;
						}
					}
				}
			}
		}
	}

	@Override
	public void elementChanged(QueryControllerQuery query) {
		QueryTreeElement node = getTreeNode(root, query);
		node.setQuery(query.getQuery());
		nodeChanged(node);
	}

	@Override
	public void elementChanged(QueryControllerQuery query,
			QueryControllerGroup group) {
		QueryTreeElement node = getTreeNode(root, group, query);
		node.setQuery(query.getQuery());
		nodeChanged(node);
	}

	private QueryTreeElement getTreeNode(TreeNode root, QueryControllerQuery query) {
		Enumeration<TreeNode> parent = root.children();
		while (parent.hasMoreElements()) {
			TreeNode node = parent.nextElement();
			if (node instanceof QueryTreeElement) {
				QueryTreeElement queryNode = (QueryTreeElement) node;
				if (queryNode.getID().equals(query.getID())) {
					return queryNode;
				}
			}
		}
		return null;
	}

	private QueryTreeElement getTreeNode(TreeNode root, QueryControllerGroup group, QueryControllerQuery query) {
		Enumeration<TreeNode> parent = root.children();
		while (parent.hasMoreElements()) {
			TreeNode node = parent.nextElement();
			if (node instanceof QueryGroupTreeElement) {
				QueryGroupTreeElement groupNode = (QueryGroupTreeElement) node;
				if (groupNode.getID().equals(group.getID())) {
					return getTreeNode(groupNode, query);
				}
			}
		}
		return null;
	}

	/**
	 * Search a TreeElement node group or query and returns the object else
	 * returns null.
	 */
	public TreeElement getNode(String element) {
		TreeElement node = null;
		Enumeration<TreeNode> elements = root.children();
		while (elements.hasMoreElements()) {
			TreeElement currentNode = (TreeElement) elements.nextElement();
			if (currentNode instanceof QueryGroupTreeElement) {
				QueryGroupTreeElement groupTElement = (QueryGroupTreeElement) currentNode;
				if (groupTElement.getID().equals(element)) {
					node = groupTElement;
					break;
				}
			}
			if (currentNode instanceof QueryTreeElement) {
				QueryTreeElement queryTreeElement = (QueryTreeElement) currentNode;
				if (queryTreeElement.getID().equals(element)) {
					node = queryTreeElement;
					break;
				}
			}
		}
		return node;
	}

	/**
	 * Search a query node in a group and returns the object else returns null.
	 */
	public QueryTreeElement getElementQuery(String element, String group) {
		QueryTreeElement node = null;
		Enumeration<TreeNode> elements = root.children();
		while (elements.hasMoreElements()) {
			TreeElement currentNode = (TreeElement) elements.nextElement();
			if (currentNode instanceof QueryGroupTreeElement && currentNode.getID().equals(group)) {
				QueryGroupTreeElement groupTElement = (QueryGroupTreeElement) currentNode;
				Enumeration<TreeNode> queries = groupTElement.children();
				while (queries.hasMoreElements()) {
					QueryTreeElement queryTElement = (QueryTreeElement) queries.nextElement();
					if (queryTElement.getID().equals(element)) {
						node = queryTElement;
						break;
					}
				}
			} else {
				continue;
			}
		}
		return node;
	}
}
