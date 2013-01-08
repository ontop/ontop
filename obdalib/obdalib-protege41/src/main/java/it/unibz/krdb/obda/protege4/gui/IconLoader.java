package it.unibz.krdb.obda.protege4.gui;

import javax.swing.ImageIcon;

public class IconLoader {
	
	/** 
	 * Returns an ImageIcon, or null if the path was invalid. 
	 */
	public static ImageIcon getImageIcon(String path) {
		java.net.URL imgURL = IconLoader.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			return null;
		}
	}
}
