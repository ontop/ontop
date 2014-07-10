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
public class Load extends QueryModelNodeBase implements UpdateExpr {

	private ValueConstant source;

	private ValueConstant graph;

	private boolean silent;
	
	public Load(ValueConstant source) {
		setSource(source);
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
		if (source != null) {
			source.visit(visitor);
		}
		if (graph != null) {
			graph.visit(visitor);
		}
		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (source == current) {
			setSource((ValueConstant)current);
		}
		else if (graph == current) {
			setGraph((ValueConstant)current);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public Load clone() {
		Load clone = new Load(source.clone());
		if (getGraph() != null) {
			clone.setGraph(getGraph().clone());
		}
		return clone;
	}

	/**
	 * @param graph
	 *        The graph to set.
	 */
	public void setGraph(ValueConstant graph) {
		this.graph = graph;
	}

	/**
	 * @return Returns the graph.
	 */
	public ValueConstant getGraph() {
		return graph;
	}

	/**
	 * @param source
	 *        The source to set.
	 */
	public void setSource(ValueConstant source) {
		this.source = source;
	}

	/**
	 * @return Returns the source.
	 */
	public ValueConstant getSource() {
		return source;
	}

	/**
	 * @param silent The silent to set.
	 */
	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	/**
	 * @return Returns the silent.
	 */
	public boolean isSilent() {
		return silent;
	}

}
