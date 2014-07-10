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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.query.algebra.StatementPattern.Scope;

/**
 * A tuple expression that matches a path of length zero against an RDF graph.
 * They can can be targeted at one of three context scopes: all contexts, null
 * context only, or named contexts only.
 */
public class ZeroLengthPath extends QueryModelNodeBase implements TupleExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Scope scope;

	private Var subjectVar;

	private Var objectVar;

	private Var contextVar;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ZeroLengthPath() {
	}

	/**
	 * Creates a zero-length path that matches a subject-, predicate- and object
	 * variable against statements from all contexts.
	 */
	public ZeroLengthPath(Var subject, Var object) {
		this(Scope.DEFAULT_CONTEXTS, subject, object);
	}

	/**
	 * Creates a zero-length path that matches a subject-, predicate- and object
	 * variable against statements from the specified context scope.
	 */
	public ZeroLengthPath(Scope scope, Var subject, Var object) {
		this(scope, subject, object, null);
	}

	/**
	 * Creates a zero-length path that matches a subject-, predicate-, object-
	 * and context variable against statements from all contexts.
	 */
	public ZeroLengthPath(Var subject, Var object, Var context) {
		this(Scope.DEFAULT_CONTEXTS, subject, object, context);
	}

	/**
	 * Creates a zero-length path that matches a subject-, predicate-, object-
	 * and context variable against statements from the specified context scope.
	 */
	public ZeroLengthPath(Scope scope, Var subjVar, Var objVar, Var conVar) {
		setScope(scope);
		setSubjectVar(subjVar);
		setObjectVar(objVar);
		setContextVar(conVar);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the context scope for the zero-length path.
	 */
	public Scope getScope() {
		return scope;
	}

	/**
	 * Sets the context scope for the zero-length path
	 */
	public void setScope(Scope scope) {
		assert scope != null : "scope must not be null";
		this.scope = scope;
	}

	public Var getSubjectVar() {
		return subjectVar;
	}

	public void setSubjectVar(Var subject) {
		assert subject != null : "subject must not be null";
		subject.setParentNode(this);
		subjectVar = subject;
	}

	public Var getObjectVar() {
		return objectVar;
	}

	public void setObjectVar(Var object) {
		assert object != null : "object must not be null";
		object.setParentNode(this);
		objectVar = object;
	}

	/**
	 * Returns the context variable, if available.
	 */
	public Var getContextVar() {
		return contextVar;
	}

	public void setContextVar(Var context) {
		if (context != null) {
			context.setParentNode(this);
		}
		contextVar = context;
	}

	public Set<String> getBindingNames() {
		return getAssuredBindingNames();
	}

	public Set<String> getAssuredBindingNames() {
		Set<String> bindingNames = new HashSet<String>(8);

		if (subjectVar != null) {
			bindingNames.add(subjectVar.getName());
		}
		if (objectVar != null) {
			bindingNames.add(objectVar.getName());
		}
		if (contextVar != null) {
			bindingNames.add(contextVar.getName());
		}

		return bindingNames;
	}

	public List<Var> getVarList() {
		return getVars(new ArrayList<Var>(4));
	}

	/**
	 * Adds the variables of this statement pattern to the supplied collection.
	 */
	public <L extends Collection<Var>> L getVars(L varCollection) {
		if (subjectVar != null) {
			varCollection.add(subjectVar);
		}
		if (objectVar != null) {
			varCollection.add(objectVar);
		}
		if (contextVar != null) {
			varCollection.add(contextVar);
		}

		return varCollection;
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
		if (subjectVar != null) {
			subjectVar.visit(visitor);
		}
		if (objectVar != null) {
			objectVar.visit(visitor);
		}
		if (contextVar != null) {
			contextVar.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (subjectVar == current) {
			setSubjectVar((Var)replacement);
		}
		else if (objectVar == current) {
			setObjectVar((Var)replacement);
		}
		else if (contextVar == current) {
			setContextVar((Var)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public String getSignature() {
		StringBuilder sb = new StringBuilder(128);

		sb.append(super.getSignature());

		if (scope == Scope.NAMED_CONTEXTS) {
			sb.append(" FROM NAMED CONTEXT");
		}

		return sb.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ZeroLengthPath) {
			ZeroLengthPath o = (ZeroLengthPath)other;
			return subjectVar.equals(o.getSubjectVar()) 
					&& objectVar.equals(o.getObjectVar()) && nullEquals(contextVar, o.getContextVar())
					&& scope.equals(o.getScope());
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = subjectVar.hashCode();
		result ^= objectVar.hashCode();
		if (contextVar != null) {
			result ^= contextVar.hashCode();
		}
		if (scope == Scope.NAMED_CONTEXTS) {
			result = ~result;
		}
		return result;
	}

	@Override
	public ZeroLengthPath clone() {
		ZeroLengthPath clone = (ZeroLengthPath)super.clone();
		clone.setSubjectVar(getSubjectVar().clone());
		clone.setObjectVar(getObjectVar().clone());

		if (getContextVar() != null) {
			clone.setContextVar(getContextVar().clone());
		}

		return clone;
	}
}
