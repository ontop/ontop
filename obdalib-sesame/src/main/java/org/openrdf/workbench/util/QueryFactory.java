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
package org.openrdf.workbench.util;

import org.openrdf.OpenRDFException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;

/**
 * Utility class for generating query objects.
 */
public class QueryFactory {

	public static Query prepareQuery(final RepositoryConnection con, final QueryLanguage queryLn, final String query)
		throws OpenRDFException
	{
		Query rval = null;
		try {
			rval = con.prepareQuery(queryLn, query);
		}
		catch (UnsupportedOperationException exc) {
			// TODO must be an HTTP repository
			try {
				con.prepareTupleQuery(queryLn, query).evaluate().close();
				rval = con.prepareTupleQuery(queryLn, query);
			}
			catch (Exception e1) {
				// guess its not a tuple query
				try {
					con.prepareGraphQuery(queryLn, query).evaluate().close();
					rval = con.prepareGraphQuery(queryLn, query);
				}
				catch (Exception e2) {
					// guess its not a graph query
					try {
						con.prepareBooleanQuery(queryLn, query).evaluate();
						rval = con.prepareBooleanQuery(queryLn, query);
					}
					catch (Exception e3) {
						// guess its not a boolean query
						// let's assume it is an malformed tuple query
						rval = con.prepareTupleQuery(queryLn, query);
					}
				}
			}
		}
		return rval;
	}
}
