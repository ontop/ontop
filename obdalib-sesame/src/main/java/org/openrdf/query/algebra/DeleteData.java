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
public class DeleteData extends QueryModelNodeBase implements UpdateExpr {

	private TupleExpr deleteExpr;

	public DeleteData(TupleExpr deleteExpr) {
		setDeleteExpr(deleteExpr);
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
		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (deleteExpr == current) {
			setDeleteExpr((TupleExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public DeleteData clone() {

		TupleExpr deleteClone = deleteExpr != null ? deleteExpr.clone() : null;
		return new DeleteData(deleteClone);
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

	public boolean isSilent() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
