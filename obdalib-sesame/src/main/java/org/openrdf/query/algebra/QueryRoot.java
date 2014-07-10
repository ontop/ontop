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
 * A semantics-less query model node that is used as the root of query model
 * trees. This is a placeholder that facilitates modifications to query model
 * trees, including the replacement of the actual (semantically relevant) root
 * node with another root node.
 * 
 * @author Arjohn Kampman
 */
public class QueryRoot extends UnaryTupleOperator {

	private QueryModelNode parent;

	public QueryRoot() {
		super();
	}

	public QueryRoot(TupleExpr tupleExpr) {
		super(tupleExpr);
	}

	@Override
	public void setParentNode(QueryModelNode parent) {
		if (parent instanceof QueryRoot) {
			this.parent = parent;
		}
		else {
			throw new UnsupportedOperationException("Not allowed to set a parent on a QueryRoot object");
		}
	}

	@Override
	public QueryModelNode getParentNode() {
		return this.parent;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof QueryRoot && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "QueryRoot".hashCode();
	}

	@Override
	public QueryRoot clone() {
		return (QueryRoot)super.clone();
	}
}
