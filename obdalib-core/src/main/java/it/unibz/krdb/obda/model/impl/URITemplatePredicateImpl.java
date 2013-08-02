/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.URITemplatePredicate;

public class URITemplatePredicateImpl extends PredicateImpl implements URITemplatePredicate {

	private static final long serialVersionUID = 1L;

	public URITemplatePredicateImpl(int arity) {
		// TODO: BAD CODE! Predicate shouldn't store the arity and the type.
		super(OBDAVocabulary.QUEST_URI, arity, null);
	}
	
	@Override
	public URITemplatePredicateImpl clone() {
		return this;
	}
}
