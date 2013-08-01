/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.protege4.utils;

import it.unibz.krdb.obda.io.TargetQueryVocabularyValidator;
import it.unibz.krdb.obda.model.OBDAModel;

import javax.swing.JTextPane;
import javax.swing.text.DefaultStyledDocument;

public class MappingStyledDocument extends DefaultStyledDocument {

	private static final long serialVersionUID = -1541062682306964359L;

	public MappingStyledDocument(JTextPane parent, OBDAModel apic, TargetQueryVocabularyValidator validator) {
		super();
	}
}
