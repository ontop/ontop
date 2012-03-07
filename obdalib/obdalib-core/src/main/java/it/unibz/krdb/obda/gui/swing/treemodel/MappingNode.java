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
package it.unibz.krdb.obda.gui.swing.treemodel;

import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.impl.RDBMSMappingAxiomImpl;

import javax.swing.tree.DefaultMutableTreeNode;

public class MappingNode extends MappingTreeNode {

	private static final long serialVersionUID = -9116413704531119587L;

	OBDAMappingAxiom mapping = null;

	public MappingNode(String name) {
		super(name);
	}

	
	
	public MappingNode(OBDAMappingAxiom mapping) {
		super(mapping.getId());
		this.mapping = mapping;
	}

	public OBDAMappingAxiom getMapping() {
		return mapping;
	}

	public void setMapping(OBDAMappingAxiom mapping) {
		this.mapping = mapping;
	}

	public static MappingNode getMappingNodeFromMapping(RDBMSMappingAxiomImpl mapping) {
		MappingNode node = new MappingNode(mapping.getId());
		MappingBodyNode body = null;
		MappingHeadNode head = null;
		if (mapping.getSourceQuery() != null) {
			body = new MappingBodyNode(mapping.getSourceQuery().toString());
		} else {
			body = new MappingBodyNode("");
		}
		if (mapping.getTargetQuery() != null) {
			head = new MappingHeadNode(mapping.getTargetQuery().toString());
		} else {
			head = new MappingHeadNode("");
		}
		node.add(head);
		node.add(body);
		return node;
	}

	public MappingHeadNode getHeadNode() {
		for (int i = 0; i < getChildCount(); i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) getChildAt(i);
			if (node.getClass().equals(MappingHeadNode.class)) {
				return (MappingHeadNode) node;
			}
		}
		return null;
	}

	public MappingBodyNode getBodyNode() {
		for (int i = 0; i < getChildCount(); i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) getChildAt(i);
			if (node.getClass().equals(MappingBodyNode.class)) {
				return (MappingBodyNode) node;
			}
		}
		return null;
	}

	public String getMappingID() {
		return (String) getUserObject();
	}

	public void setMappingID(String newid) {
		setUserObject(newid);
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append(getMappingID());
		str.trimToSize();
		return str.toString();
		// return this.getMappingID();
	}
}
