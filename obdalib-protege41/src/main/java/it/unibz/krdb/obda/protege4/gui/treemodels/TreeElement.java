package it.unibz.krdb.obda.protege4.gui.treemodels;

import javax.swing.tree.MutableTreeNode;

public interface TreeElement extends MutableTreeNode {
	
	public String getNodeName();

	public String getID();
}
