/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.gui.swing.treemodel;

import javax.swing.tree.MutableTreeNode;

public interface TreeElement extends MutableTreeNode {
	
	public String getNodeName();

	public String getID();
}
