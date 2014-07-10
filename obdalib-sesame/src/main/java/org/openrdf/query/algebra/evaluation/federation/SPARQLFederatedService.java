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
package org.openrdf.query.algebra.evaluation.federation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.Iterations;
import info.aduna.iteration.SingletonIteration;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.evaluation.iterator.CollectionIteration;
import org.openrdf.query.algebra.evaluation.iterator.SilentIteration;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.repository.sparql.query.InsertBindingSetCursor;

/**
 * Federated Service wrapping the {@link SPARQLRepository} to communicate with a
 * SPARQL endpoint.
 * 
 * @author Andreas Schwarte
 */
public class SPARQLFederatedService implements FederatedService {

	final static Logger logger = LoggerFactory.getLogger(SPARQLFederatedService.class);

	/**
	 * A convenience iteration for SERVICE expression which evaluates
	 * intermediate results in batches and manages all results. Uses
	 * {@link JoinExecutorBase} facilities to guarantee correct access to the
	 * final results
	 * 
	 * @author as
	 */
	private class BatchingServiceIteration extends JoinExecutorBase<BindingSet> {

		private final int blockSize;

		private final Service service;

		/**
		 * @param inputBindings
		 * @throws QueryEvaluationException
		 */
		public BatchingServiceIteration(CloseableIteration<BindingSet, QueryEvaluationException> inputBindings,
				int blockSize, Service service)
			throws QueryEvaluationException
		{
			super(inputBindings, null, EmptyBindingSet.getInstance());
			this.blockSize = blockSize;
			this.service = service;
			run();
		}

		@Override
		protected void handleBindings()
			throws Exception
		{
			while (!closed && leftIter.hasNext()) {

				ArrayList<BindingSet> blockBindings = new ArrayList<BindingSet>(blockSize);
				for (int i = 0; i < blockSize; i++) {
					if (!leftIter.hasNext())
						break;
					blockBindings.add(leftIter.next());
				}
				CloseableIteration<BindingSet, QueryEvaluationException> materializedIter = new CollectionIteration<BindingSet, QueryEvaluationException>(
						blockBindings);
				addResult(evaluateInternal(service, materializedIter, service.getBaseURI()));
			}
		}
	}

	protected final SPARQLRepository rep;

	protected RepositoryConnection conn = null;

	/**
	 * @param serviceUrl
	 *        the serviceUrl use to initialize the inner {@link SPARQLRepository}
	 */
	public SPARQLFederatedService(String serviceUrl) {
		super();
		this.rep = new SPARQLRepository(serviceUrl);
	}

	/**
	 * Evaluate the provided sparqlQueryString at the initialized
	 * {@link SPARQLRepository} of this {@link FederatedService}. Dependent on
	 * the type (ASK/SELECT) different evaluation is necessary: SELECT: insert
	 * bindings into SELECT query and evaluate ASK: insert bindings, send ask
	 * query and return final result
	 */
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(String sparqlQueryString,
			BindingSet bindings, String baseUri, QueryType type, Service service)
		throws QueryEvaluationException
	{

		try {

			if (type == QueryType.SELECT) {

				TupleQuery query = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, sparqlQueryString,
						baseUri);

				Iterator<Binding> bIter = bindings.iterator();
				while (bIter.hasNext()) {
					Binding b = bIter.next();
					if (service.getServiceVars().contains(b.getName()))
						query.setBinding(b.getName(), b.getValue());
				}

				TupleQueryResult res = query.evaluate();

				// insert original bindings again
				return new InsertBindingSetCursor(res, bindings);

			}
			else if (type == QueryType.ASK) {
				BooleanQuery query = getConnection().prepareBooleanQuery(QueryLanguage.SPARQL, sparqlQueryString,
						baseUri);

				Iterator<Binding> bIter = bindings.iterator();
				while (bIter.hasNext()) {
					Binding b = bIter.next();
					if (service.getServiceVars().contains(b.getName()))
						query.setBinding(b.getName(), b.getValue());
				}

				boolean exists = query.evaluate();

				// check if triples are available (with inserted bindings)
				if (exists)
					return new SingletonIteration<BindingSet, QueryEvaluationException>(bindings);
				else
					return new EmptyIteration<BindingSet, QueryEvaluationException>();
			}
			else
				throw new QueryEvaluationException("Unsupported QueryType: " + type.toString());

		}
		catch (MalformedQueryException e) {
			throw new QueryEvaluationException(e);
		}
		catch (RepositoryException e) {
			throw new QueryEvaluationException("SPARQLRepository for endpoint " + rep.toString()
					+ " could not be initialized.", e);
		}
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Service service,
			CloseableIteration<BindingSet, QueryEvaluationException> bindings, String baseUri)
		throws QueryEvaluationException
	{

		// the number of bindings sent in a single subquery.
		// if blockSize is set to 0, the entire input stream is used as block
		// input
		// the block size effectively determines the number of remote requests
		int blockSize = 15; // TODO configurable block size

		if (blockSize > 0) {
			return new BatchingServiceIteration(bindings, blockSize, service);
		}
		else {
			// if blocksize is 0 (i.e. disabled) the entire iteration is used as
			// block
			return evaluateInternal(service, bindings, service.getBaseURI());
		}
	}

	/**
	 * Evaluate the SPARQL query that can be constructed from the SERVICE node at
	 * the initialized {@link SPARQLRepository} of this {@link FederatedService}.
	 * Use specified bindings as constraints to the query. Try to evaluate using
	 * BINDINGS clause, if this yields an exception fall back to the naive
	 * implementation. This method deals with SILENT SERVICEs.
	 */
	protected CloseableIteration<BindingSet, QueryEvaluationException> evaluateInternal(Service service,
			CloseableIteration<BindingSet, QueryEvaluationException> bindings, String baseUri)
		throws QueryEvaluationException
	{

		// materialize all bindings (to allow for fallback in case of errors)
		// note that this may be blocking depending on the underlying iterator
		List<BindingSet> allBindings = new LinkedList<BindingSet>();
		while (bindings.hasNext()) {
			allBindings.add(bindings.next());
		}

		if (allBindings.size() == 0) {
			return new EmptyIteration<BindingSet, QueryEvaluationException>();
		}

		// projection vars
		Set<String> projectionVars = new HashSet<String>(service.getServiceVars());
		projectionVars.removeAll(allBindings.get(0).getBindingNames());

		// below we need to take care for SILENT services
		CloseableIteration<BindingSet, QueryEvaluationException> result = null;
		try {
			// fallback to simple evaluation (just a single binding)
			if (allBindings.size() == 1) {
				String queryString = service.getQueryString(projectionVars);
				result = evaluate(queryString, allBindings.get(0), baseUri, QueryType.SELECT, service);
				result = service.isSilent() ? new SilentIteration(result) : result;
				return result;
			}

			// To be able to insert the input bindings again later on, we need some
			// means to identify the row of each binding. hence, we use an
			// additional
			// projection variable, which is also passed in the BINDINGS clause
			// with the value of the actual row. The value corresponds to the index
			// of the binding in the index list
			projectionVars.add("__rowIdx");

			String queryString = service.getQueryString(projectionVars);

			List<String> relevantBindingNames = getRelevantBindingNames(allBindings, service.getServiceVars());

			if (relevantBindingNames.size() != 0) {
				// append the VALUES clause to the query
				queryString += buildVALUESClause(allBindings, relevantBindingNames);
			}

			TupleQuery query = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, queryString, baseUri);
			TupleQueryResult res = null;
			try {
				query.setMaxQueryTime(60); // TODO how to retrieve max query value
													// from actual setting?
				res = query.evaluate();
			}
			catch (QueryEvaluationException q) {

				closeQuietly(res);

				// use fallback: endpoint might not support BINDINGS clause
				String preparedQuery = service.getQueryString(projectionVars);
				result = new ServiceFallbackIteration(service, preparedQuery, allBindings, this);
				result = service.isSilent() ? new SilentIteration(result) : result;
				return result;
			}

			if (relevantBindingNames.size() == 0)
				result = new ServiceCrossProductIteration(res, allBindings); // cross
																									// product
			else
				result = new ServiceJoinConversionIteration(res, allBindings); // common
																									// join

			result = service.isSilent() ? new SilentIteration(result) : result;
			return result;

		}
		catch (RepositoryException e) {
			Iterations.closeCloseable(result);
			if (service.isSilent())
				return new CollectionIteration<BindingSet, QueryEvaluationException>(allBindings);
			throw new QueryEvaluationException("SPARQLRepository for endpoint " + rep.toString()
					+ " could not be initialized.", e);
		}
		catch (MalformedQueryException e) {
			// this exception must not be silenced, bug in our code
			throw new QueryEvaluationException(e);
		}
		catch (QueryEvaluationException e) {
			Iterations.closeCloseable(result);
			if (service.isSilent())
				return new CollectionIteration<BindingSet, QueryEvaluationException>(allBindings);
			throw e;
		}
		catch (RuntimeException e) {
			Iterations.closeCloseable(result);
			// suppress special exceptions (e.g. UndeclaredThrowable with wrapped
			// QueryEval) if silent
			if (service.isSilent())
				return new CollectionIteration<BindingSet, QueryEvaluationException>(allBindings);
			throw e;
		}
	}

	public void initialize()
		throws RepositoryException
	{
		rep.initialize();
	}

	private void closeQuietly(TupleQueryResult res) {
		try {
			if (res != null)
				res.close();
		}
		catch (Exception e) {
			logger.debug("Could not close connection properly: " + e.getMessage(), e);
		}
	}

	public void shutdown()
		throws RepositoryException
	{
		if (conn != null)
			conn.close();
		rep.shutDown();
	}

	protected RepositoryConnection getConnection()
		throws RepositoryException
	{
		// use a cache connection if possible
		// (TODO add mechanism to unset/close connection)
		if (conn == null) {
			conn = rep.getConnection();
		}
		return conn;
	}

	/**
	 * Compute the relevant binding names using the variables occuring in the
	 * service expression and the input bindings. The idea is find all variables
	 * which need to be projected in the subquery, i.e. those that will not be
	 * bound by an input binding.
	 * <p>
	 * If the resulting list is empty, the cross product needs to be formed.
	 * 
	 * @param bindings
	 * @param serviceVars
	 * @return the list of relevant bindings (if empty: the cross product needs
	 *         to be formed)
	 */
	private List<String> getRelevantBindingNames(List<BindingSet> bindings, Set<String> serviceVars) {

		// get the bindings variables
		// TODO CHECK: does the first bindingset give all relevant names

		List<String> relevantBindingNames = new ArrayList<String>(5);
		for (String bName : bindings.get(0).getBindingNames()) {
			if (serviceVars.contains(bName))
				relevantBindingNames.add(bName);
		}

		return relevantBindingNames;
	}

	/**
	 * Computes the VALUES clause for the set of relevant input bindings. The
	 * VALUES clause is attached to a subquery for block-nested-loop evaluation.
	 * Implementation note: we use a special binding to mark the rowIndex of the
	 * input binding.
	 * 
	 * @param bindings
	 * @param relevantBindingNames
	 * @return a string with the VALUES clause for the given set of relevant
	 *         input bindings
	 * @throws QueryEvaluationException
	 */
	private String buildVALUESClause(List<BindingSet> bindings, List<String> relevantBindingNames)
		throws QueryEvaluationException
	{

		StringBuilder sb = new StringBuilder();
		sb.append(" VALUES (?__rowIdx"); // __rowIdx: see comment in evaluate()

		for (String bName : relevantBindingNames) {
			sb.append(" ?").append(bName);
		}

		sb.append(") { ");

		int rowIdx = 0;
		for (BindingSet b : bindings) {
			sb.append(" (");
			sb.append("\"").append(rowIdx++).append("\" "); // identification of
																			// the row for post
																			// processing
			for (String bName : relevantBindingNames) {
				appendValueAsString(sb, b.getValue(bName)).append(" ");
			}
			sb.append(")");
		}

		sb.append(" }");
		return sb.toString();
	}

	protected StringBuilder appendValueAsString(StringBuilder sb, Value value) {

		// TODO check if there is some convenient method in Sesame!

		if (value == null)
			return sb.append("UNDEF"); // see grammar for BINDINGs def

		else if (value instanceof URI)
			return appendURI(sb, (URI)value);

		else if (value instanceof Literal)
			return appendLiteral(sb, (Literal)value);

		// XXX check for other types ? BNode ?
		throw new RuntimeException("Type not supported: " + value.getClass().getCanonicalName());
	}

	/**
	 * Append the uri to the stringbuilder, i.e. <uri.stringValue>.
	 * 
	 * @param sb
	 * @param uri
	 * @return the StringBuilder, for convenience
	 */
	protected static StringBuilder appendURI(StringBuilder sb, URI uri) {
		sb.append("<").append(uri.stringValue()).append(">");
		return sb;
	}

	/**
	 * Append the literal to the stringbuilder: "myLiteral"^^<dataType>
	 * 
	 * @param sb
	 * @param lit
	 * @return the StringBuilder, for convenience
	 */
	protected static StringBuilder appendLiteral(StringBuilder sb, Literal lit) {
		sb.append('"');
		sb.append(lit.getLabel().replace("\"", "\\\""));
		sb.append('"');

		if (lit.getLanguage() != null) {
			sb.append('@');
			sb.append(lit.getLanguage());
		}
		else if (lit.getDatatype() != null) {
			sb.append("^^<");
			sb.append(lit.getDatatype().stringValue());
			sb.append('>');
		}
		return sb;
	}
}
