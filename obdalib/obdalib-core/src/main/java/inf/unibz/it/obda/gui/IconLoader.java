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
package inf.unibz.it.obda.gui;

import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IconLoader {

	private final static Logger log = LoggerFactory.getLogger("IconLoader");

	/** Returns an ImageIcon, or null if the path was invalid. */
	public static ImageIcon getImageIcon(String path) {
		java.net.URL imgURL = IconLoader.class.getResource(path);
	    if (imgURL != null) {
	        return new ImageIcon(imgURL);
	    }
	    else {
	    	log.warn("Couldn't find file: " + path);
	        return null;
	    }
	}

}
