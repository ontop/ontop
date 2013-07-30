/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;

/***
 * An assertion for data properties, e.g., name(mariano,"Mariano Rodriguez").
 * Corresponds to RDF triple: :mariano :name "Mariano Rodriguez".
 */
public interface DataPropertyAssertion extends Assertion {

	public ObjectConstant getObject();

	public ValueConstant getValue();

	/***
	 * Use get predicate instead
	 * 
	 * @return
	 */
	@Deprecated
	public Predicate getAttribute();
}
