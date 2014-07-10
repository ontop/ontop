/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.query.parser.sparql;

import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.QueryParserFactory;

/**
 * A {@link QueryParserFactory} for SPARQL parsers.
 * 
 * @author Arjohn Kampman
 */
public class SPARQLParserFactory implements QueryParserFactory {

	private final SPARQLParser singleton = new SPARQLParser();

	/**
	 * Returns {@link QueryLanguage#SPARQL}.
	 */
	public QueryLanguage getQueryLanguage() {
		return QueryLanguage.SPARQL;
	}

	/**
	 * Returns a shared, thread-safe, instance of SPARQLParser.
	 */
	public QueryParser getParser() {
		return singleton;
	}
}
