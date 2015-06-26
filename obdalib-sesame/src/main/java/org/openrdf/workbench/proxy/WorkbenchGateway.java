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
package org.openrdf.workbench.proxy;

import static org.openrdf.workbench.proxy.WorkbenchServlet.SERVER_PARAM;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.workbench.base.BaseServlet;
import org.openrdf.workbench.exceptions.MissingInitParameterException;
import org.openrdf.workbench.util.BasicServletConfig;
import org.openrdf.workbench.util.TupleResultBuilder;

/**
 * All requests are serviced by this Servlet, though it usually delegates to
 * other Servlets.
 */
public class WorkbenchGateway extends BaseServlet {

	private static final String DEFAULT_SERVER = "default-server";

	private static final String CHANGE_SERVER = "change-server-path";

	private static final String SERVER_COOKIE = "workbench-server";

	protected static final String TRANSFORMATIONS = "transformations";

	/**
	 * Thread-safe map of server paths to their WorkbenchServlet instances.
	 */
	private final Map<String, WorkbenchServlet> servlets = new ConcurrentHashMap<String, WorkbenchServlet>();

	private CookieHandler cookies;

	private ServerValidator serverValidator;

	@Override
	public void init(final ServletConfig config)
		throws ServletException
	{
		super.init(config);
		if (getDefaultServerPath() == null) {
			throw new MissingInitParameterException(DEFAULT_SERVER);
		}
		if (config.getInitParameter(TRANSFORMATIONS) == null) {
			throw new MissingInitParameterException(TRANSFORMATIONS);
		}
		this.cookies = new CookieHandler(config);
		this.serverValidator = new ServerValidator(config);
	}

	@Override
	public void destroy() {
		for (WorkbenchServlet servlet : servlets.values()) {
			servlet.destroy();
		}
	}

	public String getChangeServerPath() {
		return config.getInitParameter(CHANGE_SERVER);
	}

	/**
	 * Returns the value of the default-server configuration variable. Often,
	 * this is simply a relative path on the same HTTP server.
	 * 
	 * @return the path to the default Sesame server instance
	 */
	public String getDefaultServerPath() {
		return config.getInitParameter(DEFAULT_SERVER);
	}

	/**
	 * Whether the server path is fixed, which is when the change-server-path
	 * configuration value is not set.
	 * 
	 * @return true, if the change-server-path configuration variable is not set,
	 *         meaning that changing the server is blocked
	 */
	public boolean isServerFixed() {
		return getChangeServerPath() == null;
	}

	@Override
	public void service(final HttpServletRequest req, final HttpServletResponse resp)
		throws ServletException, IOException
	{
		final String change = getChangeServerPath();
		if (change != null && change.equals(req.getPathInfo())) {
			try {
				changeServer(req, resp);
			}
			catch (QueryResultHandlerException e) {
				throw new IOException(e);
			}
		}
		else {
			final WorkbenchServlet servlet = findWorkbenchServlet(req, resp);
			if (servlet == null) {
				// Redirect to change-server-path
				final StringBuilder uri = new StringBuilder(req.getRequestURI());
				if (req.getPathInfo() != null) {
					uri.setLength(uri.length() - req.getPathInfo().length());
				}
				resp.sendRedirect(uri.append(getChangeServerPath()).toString());
			}
			else {
				servlet.service(req, resp);
			}
		}
	}

	private void resetCache() {
		for (WorkbenchServlet servlet : servlets.values()) {
			// inform browser that server changed and cache is invalid
			servlet.resetCache();
		}
	}

	/**
	 * Handles requests to the "change server" page.
	 * 
	 * @param req
	 *        the servlet request object
	 * @param resp
	 *        the servlet response object
	 * @throws IOException
	 *         if an issue occurs writing to the response
	 * @throws QueryResultHandlerException
	 */
	private void changeServer(final HttpServletRequest req, final HttpServletResponse resp)
		throws IOException, QueryResultHandlerException
	{
		final String server = req.getParameter(SERVER_COOKIE);
		if (server == null) {
			// Server parameter was not present, so present entry form.
			final TupleResultBuilder builder = getTupleResultBuilder(req, resp, resp.getOutputStream());
			builder.transform(getTransformationUrl(req), "server.xsl");
			builder.start();
			builder.end();
		}
		else if (this.serverValidator.isValidServer(server)) {
			// Valid server was submitted by form. Set cookie and redirect to
			// repository selection page.
			this.cookies.addNewCookie(req, resp, SERVER_COOKIE, server);
			final String user = getOptionalParameter(req, SERVER_USER);
			this.cookies.addNewCookie(req, resp, SERVER_USER, user);
			final String password = getOptionalParameter(req, SERVER_PASSWORD);
			this.cookies.addNewCookie(req, resp, SERVER_PASSWORD, password);
			final StringBuilder uri = new StringBuilder(req.getRequestURI());
			uri.setLength(uri.length() - req.getPathInfo().length());
			resetCache();
			resp.sendRedirect(uri.toString());
		}
		else {
			// Invalid server was submitted by form. Present entry form again
			// with error message.
			final TupleResultBuilder builder = getTupleResultBuilder(req, resp, resp.getOutputStream());
			builder.transform(getTransformationUrl(req), "server.xsl");
			builder.start("error-message");
			builder.result("Invalid Server URL");
			builder.end();
		}
	}

	/**
	 * @param req
	 *        the servlet request
	 * @param name
	 *        the name of the optional parameter
	 * @return the value of the parameter, or an empty String if it is not
	 *         present.
	 */
	private String getOptionalParameter(final HttpServletRequest req, final String name) {
		String value = req.getParameter(name);
		if (value == null) {
			value = "";
		}
		return value;
	}

	/**
	 * Returns the user requested server, if valid, or the default server.
	 * 
	 * @param req
	 *        the request
	 * @param resp
	 *        the response
	 * @return the user's requested server, if valid, or the default server
	 */
	private String findServer(final HttpServletRequest req, final HttpServletResponse resp) {
		final StringBuilder value = new StringBuilder();
		if (isServerFixed()) {
			value.append(getDefaultServer(req));
		}
		else {
			value.append(cookies.getCookie(req, resp, SERVER_COOKIE));
			if (0 == value.length()) {
				value.append(getDefaultServer(req));
			}
			else if (!this.serverValidator.isValidServer(value.toString())) {
				value.replace(0, value.length(), getDefaultServer(req));
			}
		}
		return value.toString();
	}

	/**
	 * Returns a WorkbenchServlet instance allocated for the requested server.
	 * 
	 * @param req
	 *        the current request
	 * @param resp
	 *        the current response
	 * @return a WorkbenchServlet instance allocated for the requested server
	 * @throws ServletException
	 *         if a problem occurs initializing a new servlet
	 */
	private WorkbenchServlet findWorkbenchServlet(final HttpServletRequest req, final HttpServletResponse resp)
		throws ServletException
	{
		WorkbenchServlet servlet = null;
		final String server = findServer(req, resp);
		if (servlets.containsKey(server)) {
			servlet = servlets.get(server);
		}
		else {
			if (isServerFixed() || this.serverValidator.isValidServer(server)) {
				synchronized (servlets) {
					// Even though the map is thread-safe, we only wish one
					// thread to be in this block at a time, to avoid abandoning
					// a WorkbenchServlet instance to the garbage collector.
					if (servlets.containsKey(server)) {
						servlet = servlets.get(server);
					}
					else {
						final Map<String, String> params = new HashMap<String, String>(3);
						params.put(SERVER_PARAM, server);
						params.put(CookieHandler.COOKIE_AGE_PARAM, this.cookies.getMaxAge());
						params.put(TRANSFORMATIONS, this.config.getInitParameter(TRANSFORMATIONS));
						final ServletConfig cfg = new BasicServletConfig(server, config, params);
						servlet = new WorkbenchServlet();
						servlet.init(cfg);
						servlets.put(server, servlet);
					}
				}
			}
		}
		return servlet;
	}

	/**
	 * Returns the full URL to the default server on the same server as the given
	 * request.
	 * 
	 * @param req
	 *        the request to find the default server relative to
	 * @return the full URL to the default server on the same server as the given
	 *         request
	 */
	private String getDefaultServer(final HttpServletRequest req) {
		String server = getDefaultServerPath();
		if ('/' == server.charAt(0)) {
			final StringBuffer url = req.getRequestURL();
			final StringBuilder path = getServerPath(req);
			url.setLength(url.indexOf(path.toString()));
			server = url.append(server).toString();
		}
		return server;
	}

	/**
	 * Returns the full path for the given request.
	 * 
	 * @param req
	 *        the request for which the path is sought
	 * @return the full path for the given request
	 */
	private StringBuilder getServerPath(final HttpServletRequest req) {
		final StringBuilder path = new StringBuilder();
		if (req.getContextPath() != null) {
			path.append(req.getContextPath());
		}
		if (req.getServletPath() != null) {
			path.append(req.getServletPath());
		}
		if (req.getPathInfo() != null) {
			path.append(req.getPathInfo());
		}
		return path;
	}

	private String getTransformationUrl(final HttpServletRequest req) {
		final String contextPath = req.getContextPath();
		return contextPath + config.getInitParameter(TRANSFORMATIONS);
	}
}