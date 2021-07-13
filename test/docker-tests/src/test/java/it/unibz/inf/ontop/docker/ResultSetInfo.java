package it.unibz.inf.ontop.docker;

/*
 * #%L
 * ontop-test
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

public class ResultSetInfo {

	private static final String NAMESPACE = "http://ontop.inf.unibz.it/tests/rs-info#";

	private static final URI RESULTSET_INFO;
	private static final URI RESULTSET_SIZE;
	private static final URI THROWN_EXCEPTION;

	static {
		ValueFactory vf = ValueFactoryImpl.getInstance();
		RESULTSET_INFO = vf.createURI(NAMESPACE, "ResultSetInfo");
		RESULTSET_SIZE = vf.createURI(NAMESPACE, "size");
		THROWN_EXCEPTION = vf.createURI(NAMESPACE, "thrownException");
	}


	private final Map<String, Object> info = new HashMap<>();
	
	public ResultSetInfo() {
		// NO-OP
	}
	
	public ResultSetInfo(Map<String, Object> otherInfo) {
		info.putAll(otherInfo);
	}
	
	public void put(String key, Object value) {
		info.put(key, value);
	}
	
	public Object get(String key) {
		return info.get(key);
	}

	public static ResultSetInfo toResultSetInfo(Set<Statement> resultGraph) {
		ResultSetInfo rsInfo = new ResultSetInfo();
		for (Statement stmt : resultGraph) {
			IRI predicate = stmt.getPredicate();
			if (predicate.equals(RESULTSET_SIZE)) {
				rsInfo.put("counter", Integer.parseInt(stmt.getObject().stringValue()));
			} else if (predicate.equals(THROWN_EXCEPTION)) {
				rsInfo.put("thrownException", stmt.getObject().stringValue());
			} else if (predicate.equals(RDF.TYPE)) {
				// NO-OP
			} else {
				throw new RuntimeException("Unsupported URI: " + predicate);
			}
		}
		return rsInfo;
	}

}
