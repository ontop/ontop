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
 * The AVG operator as defined in
 * http://www.w3.org/TR/sparql11-query/#aggregates.
 * <P>
 * Note that we introduce AVG as a first-class object into the algebra,
 * despite it being defined as a compound of other operators (namely, SUM and
 * COUNT). This allows us to more easily optimize evaluation.
 * 
 * @author Jeen Broekstra
 */
public class Avg extends AggregateOperatorBase {

	public Avg(ValueExpr arg) {
		super(arg);
	}
	
	public Avg(ValueExpr arg, boolean distinct) {
		super(arg, distinct);
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Avg && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "Avg".hashCode();
	}

	@Override
	public Avg clone() {
		return (Avg)super.clone();
	}
}
