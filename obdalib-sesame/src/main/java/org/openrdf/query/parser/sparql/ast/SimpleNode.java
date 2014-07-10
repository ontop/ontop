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

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class SimpleNode implements Node {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	protected Node parent;

	protected List<Node> children;

	protected int id;

	protected SyntaxTreeBuilder parser;

	public SimpleNode(int id) {
		this.id = id;
		children = new ArrayList<Node>();
	}

	public SimpleNode(SyntaxTreeBuilder parser, int id) {
		this(id);
		this.parser = parser;
	}

	public void jjtOpen() {
	}

	public void jjtClose() {
	}

	public void jjtSetParent(Node n) {
		parent = n;
	}

	public Node jjtGetParent() {
		return parent;
	}

	public void jjtAddChild(Node n, int i) {
		while (i >= children.size()) {
			// Add dummy nodes
			children.add(null);
		}

		children.set(i, n);
	}

	public void jjtAppendChild(Node n) {
		children.add(n);
	}

	public void jjtInsertChild(Node n, int i) {
		children.add(i, n);
	}

	public void jjtReplaceChild(Node oldNode, Node newNode) {
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i) == oldNode) {
				children.set(i, newNode);
			}
		}
	}

	/**
	 * Replaces this node with the supplied one in the AST.
	 * 
	 * @param newNode
	 *        The replacement node.
	 */
	public void jjtReplaceWith(Node newNode) {
		if (parent != null) {
			parent.jjtReplaceChild(this, newNode);
		}

		for (Node childNode : children) {
			childNode.jjtSetParent(newNode);
		}
	}

	public List<Node> jjtGetChildren() {
		return children;
	}

	public Node jjtGetChild(int i) {
		return children.get(i);
	}

	/**
	 * Gets the (first) child of this node that is of the specific type.
	 * 
	 * @param type
	 *        The type of the child node that should be returned.
	 * @return The (first) child node of the specified type, or <tt>null</tt>
	 *         if no such child node was found.
	 */
	public <T extends Node> T jjtGetChild(Class<T> type) {
		for (Node n : children) {
			if (type.isInstance(n)) {
				return (T)n;
			}
		}

		return null;
	}

	public <T extends Node> List<T> jjtGetChildren(Class<T> type) {
		List<T> result = new ArrayList<T>(children.size());

		for (Node n : children) {
			if (type.isInstance(n)) {
				result.add((T)n);
			}
		}

		return result;
	}

	public int jjtGetNumChildren() {
		return children.size();
	}

	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	/**
	 * Accept the visitor. 
	 */
	public Object childrenAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		for (Node childNode : children) {
			// Note: modified JavaCC code, child's data no longer ignored
			data = childNode.jjtAccept(visitor, data);
		}

		return data;
	}

	/*
	 * You can override these two methods in subclasses of SimpleNode to
	 * customize the way the node appears when the tree is dumped. If your output
	 * uses more than one line you should override toString(String), otherwise
	 * overriding toString() is probably all you need to do.
	 */

	@Override
	public String toString()
	{
		return SyntaxTreeBuilderTreeConstants.jjtNodeName[id];
	}

	public String toString(String prefix) {
		return prefix + toString();
	}

	/**
	 * Writes a tree-like representation of this node and all of its subnodes
	 * (recursively) to the supplied Appendable.
	 */
	public void dump(String prefix, Appendable out)
		throws IOException
	{
		out.append(prefix).append(this.toString());

		for (Node childNode : children) {
			if (childNode != null) {
				out.append(LINE_SEPARATOR);
				((SimpleNode)childNode).dump(prefix + " ", out);
			}
		}
	}

	/**
	 * Writes a tree-like representation of this node and all of its subnodes
	 * (recursively) and returns it as a string.
	 */
	public String dump(String prefix) {
		StringWriter out = new StringWriter(256);
		try {
			dump(prefix, out);
			return out.toString();
		}
		catch (IOException e) {
			throw new RuntimeException("Unexpected I/O error while writing to StringWriter", e);
		}
	}
}
