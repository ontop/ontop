package it.unibz.krdb.obda.gui.swing.treemodel;

import it.unibz.krdb.obda.model.CQIE;

import java.util.Vector;

public interface TargetQueryVocabularyValidator {

	public abstract boolean validate(CQIE targetQuery);

	public abstract Vector<String> getInvalidPredicates();

}