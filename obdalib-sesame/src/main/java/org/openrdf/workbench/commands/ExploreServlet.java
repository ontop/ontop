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
package org.openrdf.workbench.commands;

import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.workbench.base.TupleServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class ExploreServlet extends TupleServlet {

	private final Logger logger = LoggerFactory.getLogger(ExploreServlet.class);

	public ExploreServlet() {
		super("explore.xsl", "subject", "predicate", "object", "context");
	}

	@Override
	public String[] getCookieNames() {
		return new String[] { "limit", "total_result_count", "show-datatypes" };
	}

	@Override
	public void service(final WorkbenchRequest req, final HttpServletResponse resp, final String xslPath)
		throws Exception
	{
		try {
			super.service(req, resp, xslPath);
		}
		catch (BadRequestException exc) {
			logger.warn(exc.toString(), exc);
			final TupleResultBuilder builder = getTupleResultBuilder(req, resp, resp.getOutputStream());
			builder.transform(xslPath, "explore.xsl");
			builder.start("error-message");
			builder.link(Arrays.asList(INFO));
			builder.result(exc.getMessage());
			builder.end();
		}
	}

	@Override
	protected void service(final WorkbenchRequest req, final HttpServletResponse resp,
			final TupleResultBuilder builder, final RepositoryConnection con)
		throws BadRequestException, OpenRDFException
	{
		final Value value = req.getValue("resource");
		logger.debug("resource = {}", value);

		// At worst, malicious parameter value could cause inaccurate
		// reporting of count in page.
		int count = req.getInt("know_total");
		if (count == 0) {
			count = this.processResource(con, builder, value, 0, Integer.MAX_VALUE, false).getTotalResultCount();
		}
		this.cookies.addTotalResultCountCookie(req, resp, (int)count);
		final int offset = req.getInt("offset");
		int limit = req.getInt("limit");
		if (limit == 0) {
			limit = Integer.MAX_VALUE;
		}
		this.processResource(con, builder, value, offset, limit, true);
	}

	/**
	 * Query the repository for all instances of the given value, optionally
	 * writing the results into the HTTP response.
	 * 
	 * @param con
	 *        the connection to the repository
	 * @param builder
	 *        used for writing to the HTTP response
	 * @param value
	 *        the value to query the repository for
	 * @param offset
	 *        The result at which to start rendering results.
	 * @param limit
	 *        The limit on the number of results to render.
	 * @param render
	 *        If false, suppresses output to the HTTP response.
	 * @throws OpenRDFException
	 *         if there is an issue iterating through results
	 * @return The count of all triples in the repository using the given value.
	 */
	protected ResultCursor processResource(final RepositoryConnection con, final TupleResultBuilder builder,
			final Value value, final int offset, final int limit, final boolean render)
		throws OpenRDFException
	{
		final ResultCursor cursor = new ResultCursor(offset, limit, render);
		boolean resource = value instanceof Resource;
		if (resource) {
			export(con, builder, cursor, (Resource)value, null, null);
			logger.debug("After subject, total = {}", cursor.getTotalResultCount());
		}
		if (value instanceof URI) {
			export(con, builder, cursor, null, (URI)value, null);
			logger.debug("After predicate, total = {}", cursor.getTotalResultCount());
		}
		if (value != null) {
			export(con, builder, cursor, null, null, value);
			logger.debug("After object, total = {}", cursor.getTotalResultCount());
		}
		if (resource) {
			export(con, builder, cursor, null, null, null, (Resource)value);
			logger.debug("After context, total = {}", cursor.getTotalResultCount());
		}
		return cursor;
	}

	/**
	 * <p>
	 * Render statements in the repository matching the given pattern to the HTTP
	 * response. It is an implicit assumption when this calls
	 * {@link #isFirstTimeSeen} that {@link #processResource} 's calls into here
	 * have been made in the following order:
	 * </p>
	 * <ol>
	 * <li>export(*, subject, null, null, null)</li>
	 * <li>export(*, null, predicate, null, null)</li>
	 * <li>export(*, null, null, object, null)</li>
	 * <li>export(*, null, null, null, context)</li>
	 * </ol>
	 * 
	 * @param con
	 *        the connection to the repository
	 * @param builder
	 *        used for writing to the HTTP response
	 * @param cursor
	 *        used for keeping track of our location in the result set
	 * @param subj
	 *        the triple subject
	 * @param pred
	 *        the triple predicate
	 * @param obj
	 *        the triple object
	 * @param context
	 *        the triple context
	 */
	private void export(RepositoryConnection con, TupleResultBuilder builder, ResultCursor cursor,
			Resource subj, URI pred, Value obj, Resource... context)
		throws OpenRDFException, MalformedQueryException, QueryEvaluationException
	{
		RepositoryResult<Statement> result = con.getStatements(subj, pred, obj, true, context);
		try {
			while (result.hasNext()) {
				Statement statement = result.next();
				if (isFirstTimeSeen(statement, pred, obj, context)) {
					if (cursor.mayRender()) {
						builder.result(statement.getSubject(), statement.getPredicate(), statement.getObject(),
								statement.getContext());
					}
					cursor.advance();
				}
			}
		}
		finally {
			result.close();
		}
	}

	/**
	 * Gets whether this is the first time the result quad has been seen.
	 * 
	 * @param patternPredicate
	 *        the predicate asked for, or null if another quad element was asked
	 *        for
	 * @param patternObject
	 *        the object asked for, or null if another quad element was asked for
	 * @param result
	 *        the result statement to determine if we've already seen
	 * @param patternContext
	 *        the context asked for, or null if another quad element was asked
	 *        for
	 * @return true, if this is the first time the quad has been seen, false
	 *         otherwise
	 */
	private boolean isFirstTimeSeen(Statement result, URI patternPredicate, Value patternObject,
			Resource... patternContext)
	{
		Resource resultSubject = result.getSubject();
		URI resultPredicate = result.getPredicate();
		Value resultObject = result.getObject();
		boolean firstTimeSeen;
		if (1 == patternContext.length) {
			// I.e., when context matches explore value.
			Resource ctx = patternContext[0];
			firstTimeSeen = !(ctx.equals(resultSubject) || ctx.equals(resultPredicate) || ctx.equals(resultObject));
		}
		else if (null != patternObject) {
			// I.e., when object matches explore value.
			firstTimeSeen = !(resultObject.equals(resultSubject) || resultObject.equals(resultPredicate));
		}
		else if (null != patternPredicate) {
			// I.e., when predicate matches explore value.
			firstTimeSeen = !(resultPredicate.equals(resultSubject));
		}
		else {
			// I.e., when subject matches explore value.
			firstTimeSeen = true;
		}
		return firstTimeSeen;
	}

	/**
	 * Class for keeping track of location within the result set, relative to
	 * offset and limit.
	 * 
	 * @author Dale Visser
	 */
	protected class ResultCursor {

		private int untilFirst;

		private int totalResults = 0;

		private int renderedResults = 0;

		private final int limit;

		private final boolean render;

		/**
		 * @param offset
		 *        the desired offset at which rendering should start
		 * @param limit
		 *        the desired maximum number of results to render
		 * @param render
		 *        if false, suppresses any rendering
		 */
		public ResultCursor(final int offset, final int limit, final boolean render) {
			this.render = render;
			this.limit = limit > 0 ? limit : Integer.MAX_VALUE;
			this.untilFirst = offset >= 0 ? offset : 0;
		}

		/**
		 * Gets the total number of results. Only meant to be called after
		 * advance() has been called for all results in the set.
		 * 
		 * @return the number of times advance() has been called
		 */
		public int getTotalResultCount() {
			return this.totalResults;
		}

		/**
		 * Gets the number of results that were actually rendered. Only meant to
		 * be called after advance() has been called for all results in the set.
		 * 
		 * @return the number of times advance() has been called when
		 *          this.mayRender() evaluated to true
		 */
		public int getRenderedResultCount() {
			return this.renderedResults;
		}

		/**
		 * @return whether it is allowed to render the next result
		 */
		public boolean mayRender() {
			return this.render && (this.untilFirst == 0 && this.renderedResults < this.limit);
		}

		/**
		 * Advances the cursor, incrementing the total count, and moving other
		 * internal counters.
		 */
		public void advance() {
			this.totalResults++;
			if (this.mayRender()) {
				this.renderedResults++;
			}

			if (this.untilFirst > 0) {
				this.untilFirst--;
			}
		}
	}
}