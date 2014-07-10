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

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.sparql.ast.ASTOperationContainer;
import org.openrdf.query.parser.sparql.ast.ASTString;
import org.openrdf.query.parser.sparql.ast.VisitorException;

/**
 * Processes escape sequences in strings, replacing the escape sequence with
 * their actual value. Escape sequences for SPARQL are documented in section <a
 * href="http://www.w3.org/TR/rdf-sparql-query/#grammarEscapes">A.7 Escape
 * sequences in strings</a>.
 * 
 * @author Arjohn Kampman
 */
public class StringEscapesProcessor {

	/**
	 * Processes escape sequences in ASTString objects.
	 * 
	 * @param qc
	 *        The query that needs to be processed.
	 * @throws MalformedQueryException
	 *         If an invalid escape sequence was found.
	 */
	public static void process(ASTOperationContainer qc)
		throws MalformedQueryException
	{
		StringProcessor visitor = new StringProcessor();
		try {
			qc.jjtAccept(visitor, null);
		}
		catch (VisitorException e) {
			throw new MalformedQueryException(e);
		}
	}

	private static class StringProcessor extends ASTVisitorBase {

		public StringProcessor() {
		}

		@Override
		public Object visit(ASTString stringNode, Object data)
			throws VisitorException
		{
			String value = stringNode.getValue();
			try {
				value = SPARQLUtil.decodeString(value);
				stringNode.setValue(value);
			}
			catch (IllegalArgumentException e) {
				// Invalid escape sequence
				throw new VisitorException(e.getMessage());
			}

			return super.visit(stringNode, data);
		}
	}
}
