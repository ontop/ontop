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

import java.util.ArrayList;
import java.util.List;

/**
 * A call to an (external) function that operates on zero or more arguments.
 * 
 * @author Arjohn Kampman
 */
public class FunctionCall extends QueryModelNodeBase implements ValueExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	protected String uri;

	/**
	 * The operator's argument.
	 */
	protected List<ValueExpr> args = new ArrayList<ValueExpr>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public FunctionCall() {
	}

	/**
	 * Creates a new unary value operator.
	 * 
	 * @param args
	 *        The operator's argument, must not be <tt>null</tt>.
	 */
	public FunctionCall(String uri, ValueExpr... args) {
		setURI(uri);
		addArgs(args);
	}

	public FunctionCall(String uri, Iterable<ValueExpr> args) {
		setURI(uri);
		addArgs(args);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getURI() {
		return uri;
	}

	public void setURI(String uri) {
		this.uri = uri;
	}

	public List<ValueExpr> getArgs() {
		return args;
	}

	public void setArgs(Iterable<ValueExpr> args) {
		this.args.clear();
		addArgs(args);
	}

	public void addArgs(ValueExpr... args) {
		for (ValueExpr arg : args) {
			addArg(arg);
		}
	}

	public void addArgs(Iterable<ValueExpr> args) {
		for (ValueExpr arg : args) {
			addArg(arg);
		}
	}

	public void addArg(ValueExpr arg) {
		assert arg != null : "arg must not be null";
		args.add(arg);
		arg.setParentNode(this);
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
		for (ValueExpr arg : args) {
			arg.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (replaceNodeInList(args, current, replacement)) {
			return;
		}
		super.replaceChildNode(current, replacement);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof FunctionCall) {
			FunctionCall o = (FunctionCall)other;
			return uri.equals(o.getURI()) && args.equals(o.getArgs());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return uri.hashCode() ^ args.hashCode();
	}

	@Override
	public FunctionCall clone() {
		FunctionCall clone = (FunctionCall)super.clone();

		clone.args = new ArrayList<ValueExpr>(getArgs().size());
		for (ValueExpr arg : getArgs()) {
			clone.addArg(arg.clone());
		}

		return clone;
	}
}
