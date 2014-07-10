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
 * The STR function, as defined in <a
 * href="http://www.w3.org/TR/rdf-sparql-query/#func-str">SPARQL Query Language
 * for RDF</a>; returns the label of literals or the string representation of
 * URIs.
 * 
 * @author Arjohn Kampman
 */
public class Str extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Str() {
	}

	public Str(ValueExpr arg) {
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
		return other instanceof Str && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "Str".hashCode();
	}

	@Override
	public Str clone() {
		return (Str)super.clone();
	}
}
