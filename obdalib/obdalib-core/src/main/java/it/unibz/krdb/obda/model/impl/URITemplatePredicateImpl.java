package it.unibz.krdb.obda.model.impl;

import java.net.URI;

import com.hp.hpl.jena.iri.IRIFactory;

import it.unibz.krdb.obda.model.URITemplatePredicate;

public class URITemplatePredicateImpl extends PredicateImpl implements URITemplatePredicate {

	private static final long serialVersionUID = 1L;

	public URITemplatePredicateImpl(int arity) {
		/**
		 * TODO: BAD CODE! Predicate shouldn't store the arity and the type.
		 */
		super(OBDADataFactoryImpl.getIRI(OBDAVocabulary.QUEST_URI), arity, null);
	}
	
	@Override
	public URITemplatePredicateImpl clone() {
		return this;
	}
}
