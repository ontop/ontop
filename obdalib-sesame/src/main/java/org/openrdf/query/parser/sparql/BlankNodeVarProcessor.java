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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.sparql.ast.ASTBasicGraphPattern;
import org.openrdf.query.parser.sparql.ast.ASTBlankNode;
import org.openrdf.query.parser.sparql.ast.ASTBlankNodePropertyList;
import org.openrdf.query.parser.sparql.ast.ASTCollection;
import org.openrdf.query.parser.sparql.ast.ASTOperationContainer;
import org.openrdf.query.parser.sparql.ast.ASTVar;
import org.openrdf.query.parser.sparql.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.query.parser.sparql.ast.VisitorException;

/**
 * Processes blank nodes in the query body, replacing them with variables while
 * retaining scope.
 * 
 * @author Arjohn Kampman
 */
public class BlankNodeVarProcessor extends ASTVisitorBase {

	
	public static Set<String> process(ASTOperationContainer qc)
		throws MalformedQueryException
	{
		try {
			BlankNodeToVarConverter converter = new BlankNodeToVarConverter();
			qc.jjtAccept(converter, null);
			return converter.getUsedBNodeIDs();
		}
		catch (VisitorException e) {
			throw new MalformedQueryException(e);
		}
	}

	/*-------------------------------------*
	 * Inner class BlankNodeToVarConverter *
	 *-------------------------------------*/

	private static class BlankNodeToVarConverter extends ASTVisitorBase {

		private int anonVarNo = 1;

		private Map<String, String> conversionMap = new HashMap<String, String>();

		private Set<String> usedBNodeIDs = new HashSet<String>();

		private String createAnonVarName() {
			return "-anon-" + anonVarNo++;
		}
		
		public Set<String> getUsedBNodeIDs() {
			usedBNodeIDs.addAll(conversionMap.keySet());
			return Collections.unmodifiableSet(usedBNodeIDs);
		}

		@Override
		public Object visit(ASTBasicGraphPattern node, Object data)
			throws VisitorException
		{
			// The same Blank node ID cannot be used across Graph Patterns
			usedBNodeIDs.addAll(conversionMap.keySet());

			// Blank nodes are scoped to Basic Graph Patterns
			conversionMap.clear();

			return super.visit(node, data);
		}

		@Override
		public Object visit(ASTBlankNode node, Object data)
			throws VisitorException
		{
			String bnodeID = node.getID();
			String varName = findVarName(bnodeID);

			if (varName == null) {
				varName = createAnonVarName();

				if (bnodeID != null) {
					conversionMap.put(bnodeID, varName);
				}
			}

			ASTVar varNode = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
			varNode.setName(varName);
			varNode.setAnonymous(true);

			node.jjtReplaceWith(varNode);

			return super.visit(node, data);
		}

		private String findVarName(String bnodeID) throws VisitorException {
			if (bnodeID == null)
				return null;
			String varName = conversionMap.get(bnodeID);
			if (varName == null && usedBNodeIDs.contains(bnodeID))
				throw new VisitorException(
						"BNodeID already used in another scope: " + bnodeID);
			return varName;
		}

		@Override
		public Object visit(ASTBlankNodePropertyList node, Object data)
			throws VisitorException
		{
			node.setVarName(createAnonVarName());
			return super.visit(node, data);
		}

		@Override
		public Object visit(ASTCollection node, Object data)
			throws VisitorException
		{
			node.setVarName(createAnonVarName());
			return super.visit(node, data);
		}
	}
}
