/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro. All rights reserved.
 * 
 * The OBDA-API is licensed under the terms of the Lesser General Public License
 * v.3 (see OBDAAPI_LICENSE.txt for details). The components of this work
 * include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, b)
 * third-party components licensed under terms that may be different from those
 * of the LGPL. Information about such licenses can be found in the file named
 * OBDAAPI_3DPARTY-LICENSES.txt.
 */
package it.unibz.krdb.obda.gui.swing.treemodel;

import it.unibz.krdb.obda.model.DataSource;
import it.unibz.krdb.obda.model.OBDAModelListener;

import java.net.URI;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class DatasourceTreeModel extends DefaultTreeModel implements OBDAModelListener {

	private static final long	serialVersionUID	= 6283495101253307672L;

	private URI					currentOntologyURI	= null;

	public DatasourceTreeModel() {
		super(new DefaultMutableTreeNode());
	}

	public void datasourceAdded(DataSource source) {
		insertNodeInto(new DefaultMutableTreeNode(source.getSourceID()), (DefaultMutableTreeNode) root, root.getChildCount());
	}

	public void datasourceDeleted(DataSource source) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.root;
		Enumeration<DefaultMutableTreeNode> children = root.children();
		DefaultMutableTreeNode affectedchild = null;
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode current = children.nextElement();
			if (current.getUserObject().toString().equals(source.getSourceID().toString())) {
				affectedchild = current;
				break;
			}
		}
		removeNodeFromParent(affectedchild);
		nodeStructureChanged(root);
	}

	public void datasourceUpdated(String oldname, DataSource currendata) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.root;
		Enumeration<DefaultMutableTreeNode> children = root.children();
		DefaultMutableTreeNode affectedchild = null;
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode current = children.nextElement();
			if (current.getUserObject().toString().equals(oldname)) {
				affectedchild = current;
				break;
			}
		}
		if (affectedchild != null) {
			affectedchild.setUserObject(currendata.getSourceID());
			nodeChanged(affectedchild);
		}
	}

	public void alldatasourcesDeleted() {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.root;
		root.removeAllChildren();
		nodeStructureChanged(root);
	}

	public void currentDatasourceChange(DataSource previousdatasource, DataSource currentsource) {

	}

	/**
	 * Synchronize an array of <code>DataSource</code> objects to the tree
	 * model.
	 * 
	 * @param datasources
	 *            the array of data sources.
	 */
	public void synchronize(List<DataSource> datasources) {
		alldatasourcesDeleted();
		int size = datasources.size();
		for (int i = 0; i < size; i++) {
			datasourceAdded(datasources.get(i));
		}
	}

	/***
	 * 
	 * @param uri
	 * 
	 * @deprecated no concept of "current" ontology anymore
	 */
	public void currentOntologyChanged(URI uri) {
		currentOntologyURI = uri;
	}

	@Override
	public void datasourcParametersUpdated() {
	}
}
