/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro.
 * All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public
 * License v.3 (see OBDAAPI_LICENSE.txt for details). The components of this
 * work include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, 
 * b) third-party components licensed under terms that may be different from 
 *   those of the LGPL.  Information about such licenses can be found in the 
 *   file named OBDAAPI_3DPARTY-LICENSES.txt.
 */
package inf.unibz.it.obda.gui.swing.querycontroller.tree;

import inf.unibz.it.obda.api.controller.QueryController;
import inf.unibz.it.obda.api.controller.QueryControllerEntity;
import inf.unibz.it.obda.api.controller.QueryControllerListener;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class QueryControllerTreeModel extends DefaultTreeModel implements
		QueryControllerListener {

	private static final long serialVersionUID = -5182895959682699380L;
	private QueryController qc = null;

	public QueryControllerTreeModel(QueryController qc) {
		super(new DefaultMutableTreeNode(""));
		this.qc = qc;
	}

	/**
	 * Takes all the existing nodes and constructs the tree.
	 */
	public void init() {
		Vector<QueryControllerEntity> elements = qc.getElements();
		if (elements.size() > 0) {
			for (QueryControllerEntity treeElement : elements) {
				if (treeElement instanceof QueryControllerGroup) {
					QueryControllerGroup group = (QueryControllerGroup) treeElement;
					QueryGroupTreeElement queryGroupEle = new QueryGroupTreeElement(
							group.getID());
					Vector<QueryControllerQuery> queries = group.getQueries();

					for (QueryControllerQuery query : queries) {
						QueryTreeElement queryTreeEle = new QueryTreeElement(
								query.getID(), query.getQuery());
						insertNodeInto(queryTreeEle,
								(DefaultMutableTreeNode) queryGroupEle,
								queryGroupEle.getChildCount());
					}
					insertNodeInto(queryGroupEle,
							(DefaultMutableTreeNode) root, root.getChildCount());
				} else {
					QueryControllerQuery query = (QueryControllerQuery) treeElement;
					QueryTreeElement queryTreeEle = new QueryTreeElement(query
							.getID(), query.getQuery());
					insertNodeInto(queryTreeEle, (DefaultMutableTreeNode) root,
							root.getChildCount());
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
			insertNodeInto(ele, (DefaultMutableTreeNode) root, root
					.getChildCount());
			nodeStructureChanged(root);
		} else if (element instanceof QueryControllerQuery) {
			QueryControllerQuery query = (QueryControllerQuery) element;
			QueryTreeElement ele = new QueryTreeElement(query.getID(), query
					.getQuery());
			insertNodeInto(ele, (DefaultMutableTreeNode) root, root
					.getChildCount());
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
				if (!(temporal instanceof QueryGroupTreeElement))
					continue;
				QueryGroupTreeElement groupTElement = (QueryGroupTreeElement) temporal;
				if (groupTElement.getID().equals(ele.getID())) {
					removeNodeFromParent(groupTElement);
					nodeStructureChanged(root);
					break;
				}
			}
		} else if (element instanceof QueryControllerQuery) {
			QueryControllerQuery query = (QueryControllerQuery) element;
			QueryTreeElement elementQuery = new QueryTreeElement(query.getID(),
					query.getQuery());
			Enumeration<TreeNode> elements = root.children();
			while (elements.hasMoreElements()) {
				TreeNode currentElement = elements.nextElement();
				if (currentElement instanceof QueryGroupTreeElement)
					continue;

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
	public void elementAdded(QueryControllerQuery query,
			QueryControllerGroup group) {
		QueryGroupTreeElement groupTElement = null;
		QueryTreeElement elemQ = new QueryTreeElement(query.getID(), query
				.getQuery());
		Enumeration<TreeNode> groups = root.children();
		while (groups.hasMoreElements()) {
			Object temporal = groups.nextElement();
			if (!(temporal instanceof QueryGroupTreeElement))
				continue;
			groupTElement = (QueryGroupTreeElement) temporal;
			if (groupTElement.getID().equals(group.getID())) {
				insertNodeInto(elemQ, (DefaultMutableTreeNode) groupTElement,
						groupTElement.getChildCount());
				nodeStructureChanged(root);
				break;
			}
		}
	}

	@Override
	/*
	 * * Removes a TreeNode query from a group into the tree
	 */
	public void elementRemoved(QueryControllerQuery query,
			QueryControllerGroup group) {
		QueryGroupTreeElement ele = new QueryGroupTreeElement(group.getID());
		QueryTreeElement elemQ = new QueryTreeElement(query.getID(), query
				.getQuery());

		Enumeration<TreeNode> groups = root.children();
		while (groups.hasMoreElements()) {
			QueryGroupTreeElement groupTElement = (QueryGroupTreeElement) groups
					.nextElement();
			if (groupTElement.getID().equals(ele.getID())) {
				Enumeration<TreeNode> queries = groupTElement.children();
				while (queries.hasMoreElements()) {
					QueryTreeElement queryTElement = (QueryTreeElement) queries
							.nextElement();
					if (queryTElement.getID().equals(elemQ.getID())) {
						removeNodeFromParent(queryTElement);
						nodeStructureChanged(root);
						break;
					}
				}
			}
		}
	}

	@Override
	public void elementChanged(QueryControllerQuery query) {
	}

	@Override
	public void elementChanged(QueryControllerQuery query,
			QueryControllerGroup group) {
		// TODO Auto-generated method stub
	}

	/**
	 * Search a TreeElement node group or query and returns the object else
	 * returns null
	 * 
	 * @param element
	 * @return
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
	 * Search a query node in a group and returns the object else returns null
	 * 
	 * @param element
	 * @param group
	 * @return
	 */
	public QueryTreeElement getElementQuery(String element, String group) {
		QueryTreeElement node = null;
		Enumeration<TreeNode> elements = root.children();
		while (elements.hasMoreElements()) {
			TreeElement currentNode = (TreeElement) elements.nextElement();
			if (currentNode instanceof QueryGroupTreeElement
					&& currentNode.getID().equals(group)) {
				QueryGroupTreeElement groupTElement = (QueryGroupTreeElement) currentNode;
				Enumeration<TreeNode> queries = groupTElement.children();
				while (queries.hasMoreElements()) {
					QueryTreeElement queryTElement = (QueryTreeElement) queries
							.nextElement();
					if (queryTElement.getID().equals(element)) {
						node = queryTElement;
						break;
					}
				}
			} else
				continue;
		}
		return node;
	}
}
