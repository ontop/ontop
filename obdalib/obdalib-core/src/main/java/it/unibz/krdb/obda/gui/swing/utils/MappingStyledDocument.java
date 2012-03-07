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
package it.unibz.krdb.obda.gui.swing.utils;

import it.unibz.krdb.obda.gui.swing.treemodel.TargetQueryVocabularyValidator;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.utils.OBDAPreferences;

import javax.swing.JTextPane;
import javax.swing.text.DefaultStyledDocument;

public class MappingStyledDocument extends DefaultStyledDocument {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1541062682306964359L;

	public MappingStyledDocument(JTextPane parent, OBDAModel apic, TargetQueryVocabularyValidator validator, OBDAPreferences preference) {
		super();

		QueryPainter painter = new QueryPainter(apic, preference, parent, validator);

	}

}
