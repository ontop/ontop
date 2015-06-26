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
package org.openrdf.workbench.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.util.Literals;
import org.openrdf.model.util.LiteralUtilException;
import org.openrdf.query.Binding;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.impl.BindingImpl;
import org.openrdf.query.resultio.QueryResultWriter;

/**
 * A small wrapper around {@link QueryResultWriter} to make it easier to
 * generate results in servlets.
 * 
 * @author peter
 */
public class TupleResultBuilder {

	private final QueryResultWriter out;

	private final ValueFactory vf;

	private List<String> variables = new ArrayList<String>();

	public TupleResultBuilder(QueryResultWriter writer, ValueFactory valueFactory) {
		this.out = writer;
		this.vf = valueFactory;
	}

	public void prefix(String prefix, String namespace)
		throws QueryResultHandlerException
	{
		out.handleNamespace(prefix, namespace);
	}

	public TupleResultBuilder transform(String path, String xsl)
		throws QueryResultHandlerException
	{
		out.handleStylesheet(path + "/" + xsl);
		return this;
	}

	/**
	 * This must be called before calling {@link #namedResult(String, Object)} or
	 * {@link #result(Object...)}.
	 * 
	 * @param variables
	 *        one or more variable names
	 * @return this builder, for the convenience of chaining calls
	 * @throws QueryResultHandlerException
	 */
	public TupleResultBuilder start(String... variables)
		throws QueryResultHandlerException
	{
		variables(variables);
		return this;
	}

	public TupleResultBuilder startBoolean() {
		// Do not need to do anything here currently
		return this;
	}

	public TupleResultBuilder variables(String... names)
		throws QueryResultHandlerException
	{
		variables = Arrays.asList(names);
		out.startQueryResult(variables);
		return this;
	}

	public TupleResultBuilder link(List<String> url)
		throws QueryResultHandlerException
	{
		out.handleLinks(url);
		return this;
	}

	public TupleResultBuilder bool(boolean result)
		throws QueryResultHandlerException
	{
		out.handleBoolean(result);
		return this;
	}

	/**
	 * {@link #start(String...)} must be called before using this method.
	 * 
	 * @param result
	 *        a single result, one value for each variable, in the same order as
	 *        the variable names were provided
	 * @return this builder, for the convenience of chaining calls
	 * @throws QueryResultHandlerException
	 */
	public TupleResultBuilder result(Object... result)
		throws QueryResultHandlerException
	{
		QueryBindingSet bindingSet = new QueryBindingSet();
		for (int i = 0; i < result.length; i++) {
			if (result[i] == null)
				continue;
			bindingSet.addBinding(outputNamedResult(variables.get(i), result[i]));
		}
		out.handleSolution(bindingSet);
		return this;
	}

	/**
	 * {@link #start(String...)} must be called before using this method.
	 * 
	 * @param name
	 *        the variable name, from the set of provided variable names
	 * @param result
	 *        the result value associated with the given variable name
	 * @return this builder, for the convenience of chaining calls
	 * @throws QueryResultHandlerException
	 */
	public TupleResultBuilder namedResult(String name, Object result)
		throws QueryResultHandlerException
	{
		QueryBindingSet bindingSet = new QueryBindingSet();
		bindingSet.addBinding(outputNamedResult(name, result));
		out.handleSolution(bindingSet);
		return this;
	}

	private Binding outputNamedResult(String name, Object result)
		throws QueryResultHandlerException
	{
		final Value nextValue;
		if (result instanceof Value) {
			nextValue = (Value)result;
		}
		else if (result instanceof URL) {
			nextValue = vf.createURI(result.toString());
		}
		else {
			try {
				nextValue = Literals.createLiteralOrFail(vf, result);
			}
			catch (LiteralUtilException e) {
				throw new QueryResultHandlerException("Could not convert an object to a Value", e);
			}
		}
		return new BindingImpl(name, nextValue);
	}

	/**
	 * This must be called if {@link #start(String...)} is used, after all
	 * results are generated using either {@link #namedResult(String, Object)} or
	 * {@link #result(Object...)}.
	 * <p>
	 * This must not be called if {@link #bool(boolean)} or {@link #endBoolean()}
	 * have been called.
	 * 
	 * @return This object, for chaining with other calls.
	 * @throws QueryResultHandlerException
	 */
	public TupleResultBuilder end()
		throws QueryResultHandlerException
	{
		out.endQueryResult();
		return this;
	}

	public TupleResultBuilder endBoolean() {
		// do nothing, as the call to handleBoolean always ends the document
		return this;
	}

	public void flush() {
	}

}
