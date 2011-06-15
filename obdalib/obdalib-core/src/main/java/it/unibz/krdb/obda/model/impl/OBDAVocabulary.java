package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Predicate;

import java.net.URI;

public class OBDAVocabulary {

	/* Boolean predicate URIs */

	public static final Predicate	AND	= new BooleanOperationPredicateImpl(URI.create("http://obdalib.org/predicates/boolean#AND"), 2);

	public static final Predicate	EQ	= new BooleanOperationPredicateImpl(URI.create("http://obdalib.org/predicates/boolean#EQ"), 2);

	public static final Predicate	GTE	= new BooleanOperationPredicateImpl(URI.create("http://obdalib.org/predicates/boolean#GTE"), 2);

	public static final Predicate	GT	= new BooleanOperationPredicateImpl(URI.create("http://obdalib.org/predicates/boolean#GT"), 2);

	public static final Predicate	LTE	= new BooleanOperationPredicateImpl(URI.create("http://obdalib.org/predicates/boolean#LTE"), 2);

	public static final Predicate	LT	= new BooleanOperationPredicateImpl(URI.create("http://obdalib.org/predicates/boolean#LT"), 2);

	public static final Predicate	NEQ	= new BooleanOperationPredicateImpl(URI.create("http://obdalib.org/predicates/boolean#NEQ"), 2);

	public static final Predicate	NOT	= new BooleanOperationPredicateImpl(URI.create("http://obdalib.org/predicates/boolean#NOT"), 1);

	public static final Predicate	OR	= new BooleanOperationPredicateImpl(URI.create("http://obdalib.org/predicates/boolean#OR"), 2);

}
