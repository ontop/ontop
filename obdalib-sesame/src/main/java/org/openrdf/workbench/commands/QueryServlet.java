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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.Iterations;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.IntegerLiteralImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.UnsupportedQueryResultFormatException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPQueryEvaluationException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.QueryEvaluator;
import org.openrdf.workbench.util.QueryStorage;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class QueryServlet extends TransformationServlet {

	protected static final String REF = "ref";

	private static final String LIMIT = "limit";

	private static final String QUERY_LN = "queryLn";

	private static final String INFER = "infer";

	private static final String ACCEPT = "Accept";

	protected static final String QUERY = "query";

	private static final String[] EDIT_PARAMS = new String[] { QUERY_LN, QUERY, INFER, LIMIT };

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryServlet.class);

	private static final QueryEvaluator EVAL = QueryEvaluator.INSTANCE;

	private QueryStorage storage;

	protected boolean writeQueryCookie;

	// Poor Man's Cache: At the very least, garbage collection can clean up keys
	// followed by values whenever the JVM faces memory pressure.
	private static Map<String, String> queryCache = Collections.synchronizedMap(new WeakHashMap<String, String>());

	/**
	 * For testing purposes only.
	 * 
	 * @param testQueryCache
	 *        cache to use instead of the production cache instance
	 */
	protected static void substituteQueryCache(Map<String, String> testQueryCache) {
		queryCache = testQueryCache;
	}

	protected void substituteQueryStorage(QueryStorage storage) {
		this.storage = storage;
	}

	/**
	 * @return the names of the cookies that will be retrieved from the request,
	 *         and returned in the response
	 */
	@Override
	public String[] getCookieNames() {
		String[] result;
		if (writeQueryCookie) {
			result = new String[] { QUERY, REF, LIMIT, QUERY_LN, INFER, "total_result_count", "show-datatypes" };
		}
		else {
			result = new String[] { REF, LIMIT, QUERY_LN, INFER, "total_result_count", "show-datatypes" };
		}
		return result;
	}

	/**
	 * Initialize this instance of the servlet.
	 * 
	 * @param config
	 *        configuration passed in by the application container
	 */
	@Override
	public void init(final ServletConfig config)
		throws ServletException
	{
		super.init(config);
		try {
			this.storage = QueryStorage.getSingletonInstance(this.appConfig);
		}
		catch (RepositoryException e) {
			throw new ServletException(e);
		}
		catch (IOException e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void destroy() {
		this.storage.shutdown();
		super.destroy();
	}

	/**
	 * Long query strings could blow past the Tomcat default 8k HTTP header limit
	 * if stuffed into a cookie. In this case, we need to set a flag to avoid
	 * this happening before
	 * {@link TransformationServlet#service(HttpServletRequest, HttpServletResponse)}
	 * is called. A much lower limit on the size of the query text is used to
	 * stay well below the Tomcat limitation.
	 */
	@Override
	public final void service(final HttpServletRequest req, final HttpServletResponse resp)
		throws ServletException, IOException
	{
		this.writeQueryCookie = shouldWriteQueryCookie(req.getParameter(QUERY));
		super.service(req, resp);
	}

	/**
	 * <p>
	 * Determines if the servlet should write out the query text into a cookie as
	 * received, or write it's hash instead.
	 * </p>
	 * <p>
	 * Note: This is a separate method for testing purposes.
	 * </p>
	 * 
	 * @param queryText
	 *        the text received as the value for the parameter 'query'
	 */
	protected boolean shouldWriteQueryCookie(String queryText) {
		return (null == queryText || queryText.length() <= 2048);
	}

	@Override
	protected void service(final WorkbenchRequest req, final HttpServletResponse resp, final String xslPath)
		throws IOException, OpenRDFException, BadRequestException, JSONException
	{
		if (!writeQueryCookie) {
			// If we suppressed putting the query text into the cookies before.
			cookies.addCookie(req, resp, REF, "hash");
			String queryValue = req.getParameter(QUERY);
			String hash = String.valueOf(queryValue.hashCode());
			queryCache.put(hash, queryValue);
			cookies.addCookie(req, resp, QUERY, hash);
		}
		if ("get".equals(req.getParameter("action"))) {
			JSONObject json = new JSONObject();
			json.put("queryText", getQueryText(req));
			PrintWriter writer = new PrintWriter(new BufferedWriter(resp.getWriter()));
			try {
				writer.write(json.toString());
			}
			finally {
				writer.flush();
			}
		}
		else {
			handleStandardBrowserRequest(req, resp, xslPath);
		}
	}

	private void handleStandardBrowserRequest(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws IOException, OpenRDFException, QueryResultHandlerException
	{
		setContentType(req, resp);
		OutputStream out = resp.getOutputStream();
		try {
			service(req, resp, out, xslPath);
		}
		catch (BadRequestException exc) {
			LOGGER.warn(exc.toString(), exc);
			TupleResultBuilder builder = getTupleResultBuilder(req, resp, out);
			builder.transform(xslPath, "query.xsl");
			builder.start("error-message");
			builder.link(Arrays.asList(INFO, "namespaces"));
			builder.result(exc.getMessage());
			builder.end();
		}
		catch (HTTPQueryEvaluationException exc) {
			LOGGER.warn(exc.toString(), exc);
			TupleResultBuilder builder = getTupleResultBuilder(req, resp, out);
			builder.transform(xslPath, "query.xsl");
			builder.start("error-message");
			builder.link(Arrays.asList(INFO, "namespaces"));
			builder.result(exc.getMessage());
			builder.end();
		}
		finally {
			out.flush();
		}
	}

	@Override
	protected void doPost(final WorkbenchRequest req, final HttpServletResponse resp, final String xslPath)
		throws IOException, BadRequestException, OpenRDFException, JSONException
	{
		final String action = req.getParameter("action");
		if ("save".equals(action)) {
			saveQuery(req, resp);
		}
		else if ("edit".equals(action)) {
			if (canReadSavedQuery(req)) {
				/* only need read access for edit action, since we are only reading the saved query text
				   to present it in the editor */
				final TupleResultBuilder builder = getTupleResultBuilder(req, resp, resp.getOutputStream());
				builder.transform(xslPath, "query.xsl");
				builder.start(EDIT_PARAMS);
				builder.link(Arrays.asList(INFO, "namespaces"));
				final String queryLn = req.getParameter(EDIT_PARAMS[0]);
				final String query = getQueryText(req);
				final Boolean infer = Boolean.valueOf(req.getParameter(EDIT_PARAMS[2]));
				final IntegerLiteralImpl limit = new IntegerLiteralImpl(new BigInteger(
						req.getParameter(EDIT_PARAMS[3])));
				builder.result(queryLn, query, infer, limit);
				builder.end();
			}
			else {
				throw new BadRequestException("Current user may not read the given query.");
			}
		}
		else if ("exec".equals(action)) {
			if (canReadSavedQuery(req)) {
				service(req, resp, xslPath);
			}
			else {
				throw new BadRequestException("Current user may not read the given query.");
			}
		}
		else {
			throw new BadRequestException("POST with unexpected action parameter value: " + action);
		}
	}

	private void saveQuery(final WorkbenchRequest req, final HttpServletResponse resp)
		throws IOException, BadRequestException, OpenRDFException, JSONException
	{
		resp.setContentType("application/json");
		final JSONObject json = new JSONObject();
		final HTTPRepository http = (HTTPRepository)repository;
		final boolean accessible = storage.checkAccess(http);
		json.put("accessible", accessible);
		if (accessible) {
			final String queryName = req.getParameter("query-name");
			String userName = getUserNameFromParameter(req, SERVER_USER);
			final boolean existed = storage.askExists(http, queryName, userName);
			json.put("existed", existed);
			final boolean written = Boolean.valueOf(req.getParameter("overwrite")) || !existed;
			if (written) {
				final boolean shared = !Boolean.valueOf(req.getParameter("save-private"));
				final QueryLanguage queryLanguage = QueryLanguage.valueOf(req.getParameter(QUERY_LN));
				final String queryText = req.getParameter(QUERY);
				final boolean infer = req.isParameterPresent(INFER) ? Boolean.valueOf(req.getParameter(INFER))
						: false;
				final int rowsPerPage = Integer.valueOf(req.getParameter(LIMIT));
				if (existed) {
					final URI query = storage.selectSavedQuery(http, userName, queryName);
					storage.updateQuery(query, userName, shared, queryLanguage, queryText, infer, rowsPerPage);
				}
				else {
					storage.saveQuery(http, queryName, userName, shared, queryLanguage, queryText, infer,
							rowsPerPage);
				}
			}
			json.put("written", written);
		}
		final PrintWriter writer = new PrintWriter(new BufferedWriter(resp.getWriter()));
		writer.write(json.toString());
		writer.flush();
	}

	private String getUserNameFromParameter(WorkbenchRequest req, String parameter) {
		String userName = req.getParameter(parameter);
		if (null == userName) {
			userName = "";
		}
		return userName;
	}

	private void setContentType(final WorkbenchRequest req, final HttpServletResponse resp) {
		String result = "application/xml";
		String ext = "xml";
		if (req.isParameterPresent(ACCEPT)) {
			final String accept = req.getParameter(ACCEPT);
			final RDFFormat format = RDFFormat.forMIMEType(accept);
			if (format != null) {
				result = format.getDefaultMIMEType();
				ext = format.getDefaultFileExtension();
			}
			else {
				final TupleQueryResultFormat tupleFormat = TupleQueryResultFormat.forMIMEType(accept);

				if (tupleFormat != null) {
					result = tupleFormat.getDefaultMIMEType();
					ext = tupleFormat.getDefaultFileExtension();
				}
				else {
					final BooleanQueryResultFormat booleanFormat = BooleanQueryResultFormat.forMIMEType(accept);

					if (booleanFormat != null) {
						result = booleanFormat.getDefaultMIMEType();
						ext = booleanFormat.getDefaultFileExtension();
					}
				}
			}
		}

		resp.setContentType(result);
		if (!result.equals("application/xml")) {
			final String attachment = "attachment; filename=query." + ext;
			resp.setHeader("Content-disposition", attachment);
		}
	}

	private void service(final WorkbenchRequest req, final HttpServletResponse resp, final OutputStream out,
			final String xslPath)
		throws BadRequestException, OpenRDFException, UnsupportedQueryResultFormatException, IOException
	{
		final RepositoryConnection con = repository.getConnection();
		con.setParserConfig(NON_VERIFYING_PARSER_CONFIG);
		try {
			final TupleResultBuilder builder = getTupleResultBuilder(req, resp, resp.getOutputStream());
			for (Namespace ns : Iterations.asList(con.getNamespaces())) {
				builder.prefix(ns.getPrefix(), ns.getName());
			}
			String query = getQueryText(req);
			if (query.isEmpty()) {
				builder.transform(xslPath, "query.xsl");
				builder.start();
				builder.link(Arrays.asList(INFO, "namespaces"));
				builder.end();
			}
			else {
				try {
					EVAL.extractQueryAndEvaluate(builder, resp, out, xslPath, con, query, req, this.cookies);
				}
				catch (MalformedQueryException exc) {
					throw new BadRequestException(exc.getMessage(), exc);
				}
				catch (HTTPQueryEvaluationException exc) {
					if (exc.getCause() instanceof MalformedQueryException) {
						throw new BadRequestException(exc.getCause().getMessage(), exc);
					}
					throw exc;
				}
			}
		}
		finally {
			con.close();
		}
	}

	/**
	 * @param req
	 *        for looking at the request parameters
	 * @return the query text, if it can somehow be retrieved from request
	 *         parameters, otherwise an empty string
	 * @throws BadRequestException
	 *         if a problem occurs grabbing the request from storage
	 * @throws OpenRDFException
	 *         if a problem occurs grabbing the request from storage
	 */
	protected String getQueryText(WorkbenchRequest req)
		throws BadRequestException, OpenRDFException
	{
		String result;
		if (req.isParameterPresent(QUERY)) {
			String query = req.getParameter(QUERY);
			if (req.isParameterPresent(REF)) {
				String ref = req.getParameter(REF);
				if ("text".equals(ref)) {
					result = query;
				}
				else if ("hash".equals(ref)) {
					result = queryCache.get(query);
					if (null == result) {
						result = "";
					}
				}
				else if ("id".equals(ref)) {
					result = storage.getQueryText((HTTPRepository)repository,
							getUserNameFromParameter(req, "owner"), query);
				}
				else {
					// if ref not recognized assume request meant "text"
					result = query;
				}
			}
			else {
				result = query;
			}
		}
		else {
			result = "";
		}
		return result;
	}

	private boolean canReadSavedQuery(WorkbenchRequest req)
		throws BadRequestException, OpenRDFException
	{
		if (req.isParameterPresent(REF)) {
			return "id".equals(req.getParameter(REF)) ? storage.canRead(storage.selectSavedQuery(
					(HTTPRepository)repository, getUserNameFromParameter(req, "owner"), req.getParameter(QUERY)),
					getUserNameFromParameter(req, SERVER_USER)) : true;
		}
		else {
			throw new BadRequestException("Expected 'ref' parameter in request.");
		}
	}

}