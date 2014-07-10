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
package org.openrdf.query.algebra;

/**
 * The LANG function, as defined in <a
 * href="http://www.w3.org/TR/rdf-sparql-query/#func-lang">SPARQL Query Language
 * for RDF</a>.
 * 
 * @author Arjohn Kampman
 */
public class Lang extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Lang() {
	}

	public Lang(ValueExpr arg) {
		super(arg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Lang && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "Lang".hashCode();
	}

	@Override
	public Lang clone() {
		return (Lang)super.clone();
	}
}
