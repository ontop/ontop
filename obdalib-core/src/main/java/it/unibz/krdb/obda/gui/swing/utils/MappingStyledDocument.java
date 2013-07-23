package it.unibz.krdb.obda.gui.swing.utils;

import it.unibz.krdb.obda.gui.swing.treemodel.TargetQueryVocabularyValidator;
import it.unibz.krdb.obda.model.OBDAModel;

import javax.swing.JTextPane;
import javax.swing.text.DefaultStyledDocument;

public class MappingStyledDocument extends DefaultStyledDocument {

	private static final long serialVersionUID = -1541062682306964359L;

	public MappingStyledDocument(JTextPane parent, OBDAModel apic, TargetQueryVocabularyValidator validator) {
		super();
	}
}
