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

import info.aduna.net.ParsedURI;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.sparql.ast.ASTBaseDecl;
import org.openrdf.query.parser.sparql.ast.ASTIRI;
import org.openrdf.query.parser.sparql.ast.ASTIRIFunc;
import org.openrdf.query.parser.sparql.ast.ASTOperationContainer;
import org.openrdf.query.parser.sparql.ast.ASTServiceGraphPattern;
import org.openrdf.query.parser.sparql.ast.VisitorException;

/**
 * Resolves relative URIs in a query model using either an external base URI or
 * using the base URI specified in the query model itself. The former takes
 * precedence over the latter.
 * 
 * @author Arjohn Kampman
 */
public class BaseDeclProcessor {

	/**
	 * Resolves relative URIs in the supplied query model using either the
	 * specified <tt>externalBaseURI</tt> or, if this parameter is <tt>null</tt>,
	 * the base URI specified in the query model itself.
	 * 
	 * @param qc
	 *        The query model to resolve relative URIs in.
	 * @param externalBaseURI
	 *        The external base URI to use for resolving relative URIs, or
	 *        <tt>null</tt> if the base URI that is specified in the query model
	 *        should be used.
	 * @throws IllegalArgumentException
	 *         If an external base URI is specified that is not an absolute URI.
	 * @throws MalformedQueryException
	 *         If the base URI specified in the query model is not an absolute
	 *         URI.
	 */
	public static void process(ASTOperationContainer qc, String externalBaseURI)
		throws MalformedQueryException
	{
		ParsedURI parsedBaseURI = null;

		// Use the query model's own base URI, if available
		ASTBaseDecl baseDecl = qc.getBaseDecl();
		if (baseDecl != null) {
			parsedBaseURI = new ParsedURI(baseDecl.getIRI());

			if (!parsedBaseURI.isAbsolute()) {
				throw new MalformedQueryException("BASE IRI is not an absolute IRI: " + externalBaseURI);
			}
		}
		else if (externalBaseURI != null) {
			// Use external base URI if the query doesn't contain one itself
			parsedBaseURI = new ParsedURI(externalBaseURI);

			if (!parsedBaseURI.isAbsolute()) {
				throw new IllegalArgumentException("Supplied base URI is not an absolute IRI: " + externalBaseURI);
			}
		}
		else {
			// FIXME: use the "Default Base URI"?
		}

		if (parsedBaseURI != null) {
			RelativeIRIResolver visitor = new RelativeIRIResolver(parsedBaseURI);
			try {
				qc.jjtAccept(visitor, null);
			}
			catch (VisitorException e) {
				throw new MalformedQueryException(e);
			}
		}
	}

	private static class RelativeIRIResolver extends ASTVisitorBase {

		private ParsedURI parsedBaseURI;

		public RelativeIRIResolver(ParsedURI parsedBaseURI) {
			this.parsedBaseURI = parsedBaseURI;
		}

		@Override
		public Object visit(ASTIRI node, Object data)
			throws VisitorException
		{
			ParsedURI resolvedURI = parsedBaseURI.resolve(node.getValue());
			node.setValue(resolvedURI.toString());

			return super.visit(node, data);
		}

		@Override
		public Object visit(ASTIRIFunc node, Object data)
			throws VisitorException
		{
			node.setBaseURI(parsedBaseURI.toString());
			return super.visit(node, data);
		}

		@Override
		public Object visit(ASTServiceGraphPattern node, Object data)
			throws VisitorException
		{
			node.setBaseURI(parsedBaseURI.toString());
			return super.visit(node, data);
		}
	}
}
