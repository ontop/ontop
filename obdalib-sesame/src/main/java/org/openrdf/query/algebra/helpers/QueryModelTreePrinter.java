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
package org.openrdf.query.algebra.helpers;

import org.openrdf.query.algebra.QueryModelNode;

/**
 * QueryModelVisitor implementation that "prints" a tree representation of a
 * query model. The tree representations is printed to an internal character
 * buffer and can be retrieved using {@link #getTreeString()}. As an
 * alternative, the static utility method {@link #printTree(QueryModelNode)} can
 * be used.
 */
public class QueryModelTreePrinter extends QueryModelVisitorBase<RuntimeException> {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	/*-----------*
	 * Constants *
	 *-----------*/

	public static String printTree(QueryModelNode node) {
		QueryModelTreePrinter treePrinter = new QueryModelTreePrinter();
		node.visit(treePrinter);
		return treePrinter.getTreeString();
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private String indentString = "   ";

	private StringBuilder buf;

	private int indentLevel = 0;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public QueryModelTreePrinter() {
		buf = new StringBuilder(256);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getTreeString() {
		return buf.toString();
	}

	@Override
	protected void meetNode(QueryModelNode node)
	{
		for (int i = 0; i < indentLevel; i++) {
			buf.append(indentString);
		}

		buf.append(node.getSignature());
		buf.append(LINE_SEPARATOR);

		indentLevel++;

		super.meetNode(node);

		indentLevel--;
	}
}
