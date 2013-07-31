/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.gui.swing;

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
