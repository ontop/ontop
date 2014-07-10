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
 * @author jeen
 */
public class Modify extends QueryModelNodeBase implements UpdateExpr {

	private TupleExpr deleteExpr;

	private TupleExpr insertExpr;

	private TupleExpr whereExpr;
	
	public Modify(TupleExpr deleteExpr, TupleExpr insertExpr) {
		this(deleteExpr, insertExpr, null);
	}
	
	public Modify(TupleExpr deleteExpr, TupleExpr insertExpr, TupleExpr whereExpr) {
		setDeleteExpr(deleteExpr);
		setInsertExpr(insertExpr);
		setWhereExpr(whereExpr);
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		if (deleteExpr != null) {
			deleteExpr.visit(visitor);
		}
		if (insertExpr != null) {
			insertExpr.visit(visitor);
		}
		if (whereExpr != null) {
			whereExpr.visit(visitor);
		}
		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (deleteExpr == current) {
			setDeleteExpr((TupleExpr)replacement);
		}
		else if (insertExpr == current) {
			setInsertExpr((TupleExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public Modify clone() {

		TupleExpr deleteClone = deleteExpr != null ? deleteExpr.clone() : null;
		TupleExpr insertClone = insertExpr != null ? insertExpr.clone() : null;
		TupleExpr whereClone = whereExpr != null ? whereExpr.clone() : null;
		return new Modify(deleteClone, insertClone, whereClone);
	}

	/**
	 * @param deleteExpr
	 *        The deleteExpr to set.
	 */
	public void setDeleteExpr(TupleExpr deleteExpr) {
		this.deleteExpr = deleteExpr;
	}

	/**
	 * @return Returns the deleteExpr.
	 */
	public TupleExpr getDeleteExpr() {
		return deleteExpr;
	}

	/**
	 * @param insertExpr
	 *        The insertExpr to set.
	 */
	public void setInsertExpr(TupleExpr insertExpr) {
		this.insertExpr = insertExpr;
	}

	/**
	 * @return Returns the insertExpr.
	 */
	public TupleExpr getInsertExpr() {
		return insertExpr;
	}

	/**
	 * @param whereExpr The whereExpr to set.
	 */
	public void setWhereExpr(TupleExpr whereExpr) {
		this.whereExpr = whereExpr;
	}

	/**
	 * @return Returns the whereExpr.
	 */
	public TupleExpr getWhereExpr() {
		return whereExpr;
	}

	public boolean isSilent() {
		// TODO Auto-generated method stub
		return false;
	}

}
