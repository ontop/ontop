package it.unibz.krdb.obda.gui.swing.treemodel;

import it.unibz.krdb.obda.model.CQIE;

import java.net.URI;
import java.util.Vector;

public interface TargetQueryVocabularyValidator {

	public boolean validate(CQIE targetQuery);

	public Vector<String> getInvalidPredicates();

	/**
	 * Checks whether the predicate is a class assertion.
	 * 
	 * @param predicate
	 *            The predicate name in full URI.
	 * @return Returns true if the predicate is a class assertion from the input
	 *         ontology, or false otherwise.
	 */
	public boolean isClass(URI predicate);

	/**
	 * Checks whether the predicate is a object property assertion.
	 * 
	 * @param predicate
	 *            The predicate name in full URI.
	 * @return Returns true if the predicate is a object property assertion from
	 *         the input ontology, or false otherwise.
	 */
	public boolean isObjectProperty(URI predicate);

	/**
	 * Checks whether the predicate is a data property assertion.
	 * 
	 * @param predicate
	 *            The predicate name in full URI.
	 * @return Returns true if the predicate is a data property assertion from
	 *         the input ontology, or false otherwise.
	 */
	public boolean isDataProperty(URI predicate);

}