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

import java.util.ArrayList;

import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

public class MappingTreeSelectionModel extends DefaultTreeSelectionModel {
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 2709578624720866805L;

	public MappingTreeSelectionModel() {
		super();
	}
	
	public void addSelectionPath(TreePath path) {
		// JTree mappingTree = DataSourceManagerGUI.getMappingsTreeInstance();
		Object node = path.getLastPathComponent();
		if (node instanceof MappingNode) {
			super.addSelectionPath(path);
		}
	}
	
	public void addSelectionPaths(TreePath[] paths) {
		
		ArrayList <TreePath> valid_paths = new ArrayList<TreePath>();
		for (int i = 0; i < paths.length; i++) {
			Object node = paths[i].getLastPathComponent();
			if (node instanceof MappingNode) {
				valid_paths.add(paths[i]);
			}
		}
		if (paths.length > 0) {
			TreePath[] array = new TreePath[valid_paths.size()];
			for (int i = 0; i < valid_paths.size(); i++) {
				array[i] = valid_paths.get(i);
			}
			super.addSelectionPaths(array);
		}
	}
	@Override
	protected boolean canPathsBeAdded(TreePath[] paths) {
		boolean dont_add = false;
		for (int i = 0; i < paths.length; i++) {
			Object node = paths[i].getLastPathComponent();
			if (!(node instanceof MappingNode)) {
				dont_add = true;
				break;
			}
		}
		if (dont_add) {
			return false;
		} 
		return super.canPathsBeAdded(paths);
	}
	
	public void setSelectionPath(TreePath path) {
		if(path != null){
			Object node = path.getLastPathComponent();
			if (node instanceof MappingNode) {
				super.setSelectionPath(path);
			}
		}
	}
	
	public void setSelectionPaths(TreePath[] paths) {
		ArrayList<TreePath> valid_paths = new ArrayList<TreePath>();
		for (int i = 0; i < paths.length; i++) {
			Object node = paths[i].getLastPathComponent();
			if (node instanceof MappingNode) {
				valid_paths.add(paths[i]);
			}
		}
		if (!valid_paths.isEmpty()) {
			TreePath[] array = new TreePath[valid_paths.size()];
			for (int i = 0; i < valid_paths.size(); i++) {
				array[i] = valid_paths.get(i);
			}
			super.setSelectionPaths(array);
		}
	}
	
	
}
