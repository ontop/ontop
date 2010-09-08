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
package inf.unibz.it.obda.gui.swing.action;


public abstract class AbstractOBDAAction implements OBDAAction {

//	private HashMap<String, Object> parameters = new HashMap<String, Object>();
	
	private OBDAActionResultViewer viewer = null;
	
//	public Object getParameter(String key) {
//		return parameters.get(key);
//	}
//
//	public void setParameter(String key, Object value) {
//		parameters.put(key, value);
//	}
	
	public void setResultViewer(OBDAActionResultViewer viewer) {
		this.viewer = viewer;
	}
	
	public OBDAActionResultViewer getResultViewer() {
		return viewer;
	}
	
	

}
