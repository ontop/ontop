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
 * The SLICE operator, as defined in <a
 * href="http://www.w3.org/TR/rdf-sparql-query/#defn_algSlice">SPARQL Query
 * Language for RDF</a>. The SLICE operator selects specific results from the
 * underlying tuple expression based on an offset and limit value (both
 * optional).
 * 
 * @author Arjohn Kampman
 */
public class Slice extends UnaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private long offset;

	private long limit;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Slice() {
	}

	public Slice(TupleExpr arg) {
		this(arg, 0, -1);
	}

	public Slice(TupleExpr arg, long offset2, long limit2) {
		super(arg);
		setOffset(offset2);
		setLimit(limit2);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	/**
	 * Checks whether the row selection has a (valid) offset.
	 * 
	 * @return <tt>true</tt> when <tt>offset &gt; 0</tt>
	 */
	public boolean hasOffset() {
		return offset > 0L;
	}

	public long getLimit() {
		return limit;
	}

	public void setLimit(long limit) {
		this.limit = limit;
	}

	/**
	 * Checks whether the row selection has a (valid) limit.
	 * 
	 * @return <tt>true</tt> when <tt>offset &gt;= 0</tt>
	 */
	public boolean hasLimit() {
		return limit >= 0L;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public String getSignature() {
		StringBuilder sb = new StringBuilder(256);

		sb.append(super.getSignature());
		sb.append(" ( ");

		if (hasLimit()) {
			sb.append("limit=").append(getLimit());
		}
		if (hasOffset()) {
			sb.append("offset=").append(getOffset());
		}

		sb.append(" )");

		return sb.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Slice && super.equals(other)) {
			Slice o = (Slice)other;
			return offset == o.getOffset() && limit == o.getLimit();
		}
		return false;
	}

	@Override
	public int hashCode() {
		// casting long to int is not safe, but shouldn't matter for hashcode, should it?
		return super.hashCode() ^ (int)offset ^ (int)limit;
	}

	@Override
	public Slice clone() {
		return (Slice)super.clone();
	}
}
