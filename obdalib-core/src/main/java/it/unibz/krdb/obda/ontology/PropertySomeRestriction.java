/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.Predicate;

/**
 * A non-qualified property some restriction. Corresponds to DL
 * "exists Property"
 */
public interface PropertySomeRestriction extends BasicClassDescription {

	public boolean isInverse();

	public Predicate getPredicate();
}
