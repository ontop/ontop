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
public class Copy extends QueryModelNodeBase implements UpdateExpr {

	private ValueConstant sourceGraph;

	private ValueConstant destinationGraph;

	private boolean silent;

	public Copy() {
		super();
	}

	public Copy(ValueConstant graph) {
		super();
		setSourceGraph(graph);
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
		if (sourceGraph != null) {
			sourceGraph.visit(visitor);
		}
		if (destinationGraph != null) {
			destinationGraph.visit(visitor);
		}
		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (sourceGraph == current) {
			setSourceGraph((ValueConstant)replacement);
		}
		else if (destinationGraph == current) {
			setDestinationGraph((ValueConstant)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public Copy clone() {
		Copy clone = new Copy();
		clone.setSilent(isSilent());
		if (getSourceGraph() != null) {
			clone.setSourceGraph(getSourceGraph().clone());
		}
		return clone;
	}

	/**
	 * @param graph
	 *        The graph to set.
	 */
	public void setSourceGraph(ValueConstant graph) {
		this.sourceGraph = graph;
	}

	/**
	 * The named graph from which to copy. If null, the default graph should be
	 * used.
	 * 
	 * @return Returns the graph.
	 */
	public ValueConstant getSourceGraph() {
		return sourceGraph;
	}

	/**
	 * @param silent
	 *        The silent to set.
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

	/**
	 * @param destinationGraph
	 *        The destinationGraph to set.
	 */
	public void setDestinationGraph(ValueConstant destinationGraph) {
		this.destinationGraph = destinationGraph;
	}

	/**
	 * The named graph to which to copy. If null, the default graph should be
	 * used.
	 * 
	 * @return Returns the destinationGraph.
	 */
	public ValueConstant getDestinationGraph() {
		return destinationGraph;
	}

}
