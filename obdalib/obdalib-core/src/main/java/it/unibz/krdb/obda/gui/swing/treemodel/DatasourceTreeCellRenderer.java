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

import it.unibz.krdb.obda.gui.swing.IconLoader;
import it.unibz.krdb.obda.model.DataSource;
import it.unibz.krdb.obda.model.OBDAModel;

import java.awt.Color;
import java.awt.Component;
import java.net.URI;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class DatasourceTreeCellRenderer extends DefaultTreeCellRenderer {
		/**
	 * 
	 */
	private static final long	serialVersionUID	= -4477196457170510013L;
		Icon dbms_source_icon = null;
		Icon root_node_icon = null;
		final String PATH_RDBMS_ICON = "images/rdbms.png";
		
		OBDAModel dsc = null;
		
		
    public DatasourceTreeCellRenderer(OBDAModel dsc) {
    	this.dsc = dsc;
    	dbms_source_icon = IconLoader.getImageIcon(PATH_RDBMS_ICON);
    }

    public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus) {

        super.getTreeCellRendererComponent(
                        tree, value, sel,
                        expanded, leaf, row,
                        hasFocus);
        if (leaf) {
            setIcon(dbms_source_icon);
            if(value !=null && ((DefaultMutableTreeNode)value).getUserObject() != null){
	            DataSource ds = dsc.getSource(URI.create(value.toString()));
	            if (ds != null) {
	            	if (!ds.isRegistred()) {
	            		setForeground(Color.RED);
	            		setToolTipText("This datasource is not yet registred with the server.");
	            		          		
	            	} else if (!ds.isEnabled()) {
	            		setForeground(Color.GRAY);
	            		setToolTipText("This datasource is disabled");
	            	} else {
	            		setForeground(Color.BLACK);
	            	}
				}
        	}
        } else {
        	setIcon(IconLoader.getImageIcon("images/metadata.gif"));
        }

        return this;
    }
    


}
