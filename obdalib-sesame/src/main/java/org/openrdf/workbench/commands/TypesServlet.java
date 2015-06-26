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
package org.openrdf.workbench.commands;

import static org.openrdf.query.QueryLanguage.SPARQL;

import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.workbench.base.TupleServlet;
import org.openrdf.workbench.util.TupleResultBuilder;

public class TypesServlet extends TupleServlet {

	public TypesServlet() {
		super("types.xsl", "type");
	}

	private static final String DISTINCT_TYPE = "SELECT DISTINCT ?type WHERE { ?subj a ?type }";

	@Override
	protected void service(TupleResultBuilder builder, RepositoryConnection con)
			throws Exception {
		TupleQuery query = con.prepareTupleQuery(SPARQL, DISTINCT_TYPE);
		TupleQueryResult result = query.evaluate();
		try {
			while (result.hasNext()) {
				builder.result(result.next().getValue("type"));
			}
		} finally {
			result.close();
		}
	}

}