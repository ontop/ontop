package it.unibz.krdb.obda.gui.swing.treemodel;

import javax.swing.tree.MutableTreeNode;

public interface TreeElement extends MutableTreeNode {
	
	public String getNodeName();

	public String getID();
}
