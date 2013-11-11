/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.quest;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Below is an example of using ResultSetInfo in the test framework.
 * <p>
 * 1. To check the size of result set:
 * 
 * <pre>
 * {@code
 * (at)prefix rsi: <http://ontop.inf.unibz.it/tests/rs-info#> .
 * (at)prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
 * (at)prefix :    <http://example.org/#> .
 *
 * []  rdf:type               rsi:ResultSetInfo ;
 *     rsi:size               "99" .
 * }
 * </pre>
 * 
 * 2. To check the thrown exception (the <code>rsi:size</code> is optional):
 * <pre>
 * {@code
 * (at)prefix rsi: <http://ontop.inf.unibz.it/tests/rs-info#> .
 * (at)prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
 * (at)prefix :    <http://example.org/#> .
 *
 * []  rdf:type               rsi:ResultSetInfo ;
 *     rsi:size               "-1" ;
 *     rsi:thrownException    "java.io.IOException" .
 * } 
 */
public class ResultSetInfoSchema {
	
	public static final String NAMESPACE = "http://ontop.inf.unibz.it/tests/rs-info#";

	public static final URI RESULTSET_INFO;

	public static final URI RESULTSET_SIZE;

	public static final URI THROWN_EXCEPTION;

	static {
		ValueFactory vf = ValueFactoryImpl.getInstance();
		RESULTSET_INFO = vf.createURI(NAMESPACE, "ResultSetInfo");
		RESULTSET_SIZE = vf.createURI(NAMESPACE, "size");
		THROWN_EXCEPTION = vf.createURI(NAMESPACE, "thrownException");
	}
}
