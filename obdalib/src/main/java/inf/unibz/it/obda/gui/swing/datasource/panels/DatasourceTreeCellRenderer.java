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
package inf.unibz.it.obda.gui.swing.datasource.panels;

import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.gui.IconLoader;

import java.awt.Color;
import java.awt.Component;
import java.net.URI;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;

public class DatasourceTreeCellRenderer extends DefaultTreeCellRenderer {
		Icon dbms_source_icon = null;
		Icon root_node_icon = null;
		final String PATH_RDBMS_ICON = "images/rdbms.png";
		
		DatasourcesController dsc = null;
		
		
    public DatasourceTreeCellRenderer(DatasourcesController dsc) {
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
	            DataSource ds = dsc.getDataSource(URI.create(value.toString()));
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
