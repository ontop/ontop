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
package inf.unibz.it.obda.gui.swing.datasource;

import inf.unibz.it.obda.api.controller.DatasourcesControllerListener;
import inf.unibz.it.obda.domain.DataSource;

import java.net.URI;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class DatasourceTreeModel extends DefaultTreeModel implements DatasourcesControllerListener {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 6283495101253307672L;

	private URI					currentOntologyURI	= null;

	public DatasourceTreeModel() {
		super(new DefaultMutableTreeNode());
	}

	public void datasourceAdded(DataSource source) {
//		if (URI.create(source.getUri()).equals(currentOntologyURI)) {
//			System.out.println("add "+ source.getName());
			insertNodeInto(new DefaultMutableTreeNode(source.getName()), (DefaultMutableTreeNode) root, root.getChildCount());
//		}
	}

	public void datasourceDeleted(DataSource source) {
//		if (URI.create(source.getUri()).equals(currentOntologyURI)) {
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.root;
			Enumeration<DefaultMutableTreeNode> children = root.children();
			DefaultMutableTreeNode affectedchild = null;
			while (children.hasMoreElements()) {
				DefaultMutableTreeNode current = children.nextElement();
				if (current.getUserObject().toString().equals(source.getName())) {
					affectedchild = current;
					break;
				}
			}
			removeNodeFromParent(affectedchild);
			nodeStructureChanged(root);
//		}
	}

	public void datasourceUpdated(String oldname, DataSource currendata) {
//		if (URI.create(currendata.getUri()).equals(currentOntologyURI)) {
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
			affectedchild.setUserObject(currendata.getName());
			nodeChanged(affectedchild);
//		}
	}

	public void alldatasourcesDeleted() {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.root;
		root.removeAllChildren();
		nodeStructureChanged(root);
	}

	/***************************************************************************
	 * Does nothing.
	 */
	public void currentDatasourceChange(DataSource previousdatasource, DataSource currentsource) {

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
}
