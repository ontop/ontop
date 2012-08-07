package it.unibz.krdb.obda.gui.swing.treemodel;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Predicate;

import java.util.Vector;

public interface TargetQueryVocabularyValidator {

	public boolean validate(CQIE targetQuery);

	public Vector<String> getInvalidPredicates();

	/**
	 * Checks whether the predicate is a class assertion.
	 * 
	 * @param predicate
	 *            The target predicate.
	 * @return Returns true if the predicate is a class assertion from the input
	 *         ontology, or false otherwise.
	 */
	public boolean isClass(Predicate predicate);

	/**
	 * Checks whether the predicate is a object property assertion.
	 * 
	 * @param predicate
	 *            The target predicate.
	 * @return Returns true if the predicate is a object property assertion from
	 *         the input ontology, or false otherwise.
	 */
	public boolean isObjectProperty(Predicate predicate);

	/**
	 * Checks whether the predicate is a data property assertion.
	 * 
	 * @param predicate
	 *            The target predicate.
	 * @return Returns true if the predicate is a data property assertion from
	 *         the input ontology, or false otherwise.
	 */
	public boolean isDataProperty(Predicate predicate);

}