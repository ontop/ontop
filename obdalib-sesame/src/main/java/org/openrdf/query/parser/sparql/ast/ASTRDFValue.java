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
package org.openrdf.query.parser.sparql.ast;

import org.openrdf.model.Value;

/**
 * @author jeen
 */
public abstract class ASTRDFValue extends SimpleNode {

	private Value value;

	/**
	 * @param id
	 */
	public ASTRDFValue(int id) {
		super(id);
	}

	/**
	 * @param parser
	 * @param id
	 */
	public ASTRDFValue(SyntaxTreeBuilder parser, int id) {
		super(parser, id);
	}

	public Value getRDFValue() {
		return value;

	}

	public void setRDFValue(final Value value) {
		this.value = value;
	}

}
