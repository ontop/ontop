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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.workbench.base.BaseServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.exceptions.MissingInitParameterException;
import org.openrdf.workbench.util.BasicServletConfig;
import org.openrdf.workbench.util.DynamicHttpRequest;
import org.openrdf.workbench.util.TupleResultBuilder;

public class WorkbenchServlet extends BaseServlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(WorkbenchServlet.class);

	private static final String DEFAULT_PATH = "default-path";

	private static final String NO_REPOSITORY = "no-repository-id";

	public static final String SERVER_PARAM = "server";

	private RepositoryManager manager;

	private final ConcurrentMap<String, ProxyRepositoryServlet> repositories = new ConcurrentHashMap<String, ProxyRepositoryServlet>();

	@Override
	public void init(final ServletConfig config)
		throws ServletException
	{
		this.config = config;
		if (config.getInitParameter(DEFAULT_PATH) == null) {
			throw new MissingInitParameterException(DEFAULT_PATH);
		}
		final String param = config.getInitParameter(SERVER_PARAM);
		if (param == null || param.trim().isEmpty()) {
			throw new MissingInitParameterException(SERVER_PARAM);
		}
		try {
			manager = createRepositoryManager(param);
		}
		catch (IOException e) {
			throw new ServletException(e);
		}
		catch (RepositoryException e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void destroy() {
		for (Servlet servlet : repositories.values()) {
			servlet.destroy();
		}
		manager.shutDown();
	}

	public void resetCache() {
		for (ProxyRepositoryServlet proxy : repositories.values()) {
			// inform browser that server changed and cache is invalid
			proxy.resetCache();
		}
	}

	@Override
	public void service(final HttpServletRequest req, final HttpServletResponse resp)
		throws ServletException, IOException
	{
		final String pathInfo = req.getPathInfo();
		if (pathInfo == null) {
			final String defaultPath = config.getInitParameter(DEFAULT_PATH);
			resp.sendRedirect(req.getRequestURI() + defaultPath);
		}
		else if ("/".equals(pathInfo)) {
			final String defaultPath = config.getInitParameter(DEFAULT_PATH);
			resp.sendRedirect(req.getRequestURI() + defaultPath.substring(1));
		}
		else if ('/' == pathInfo.charAt(0)) {
			try {
				handleRequest(req, resp, pathInfo);
			}
			catch (QueryResultHandlerException e) {
				throw new IOException(e);
			}
		}
		else {
			throw new BadRequestException("Request path must contain a repository ID");
		}
	}

	/**
	 * @param req
	 *        the servlet request
	 * @param resp
	 *        the servlet response
	 * @param pathInfo
	 *        the path info from the request
	 * @throws IOException
	 * @throws ServletException
	 * @throws QueryResultHandlerException
	 */
	private void handleRequest(final HttpServletRequest req, final HttpServletResponse resp,
			final String pathInfo)
		throws IOException, ServletException, QueryResultHandlerException
	{
		int idx = pathInfo.indexOf('/', 1);
		if (idx < 0) {
			idx = pathInfo.length();
		}
		final String repoID = pathInfo.substring(1, idx);
		try {
			service(repoID, req, resp);
		}
		catch (RepositoryConfigException e) {
			throw new ServletException(e);
		}
		catch (UnauthorizedException e) {
			handleUnauthorizedException(req, resp);
		}
		catch (ServletException e) {
			if (e.getCause() instanceof UnauthorizedException) {
				handleUnauthorizedException(req, resp);
			}
			else {
				throw e;
			}
		}
		catch (RepositoryException e) {
			throw new ServletException(e);
		}
	}

	/**
	 * @param req
	 * @param resp
	 * @throws IOException
	 * @throws QueryResultHandlerException
	 */
	private void handleUnauthorizedException(final HttpServletRequest req, final HttpServletResponse resp)
		throws IOException, QueryResultHandlerException
	{
		// Invalid credentials or insufficient authorization. Present
		// entry form again with error message.
		final TupleResultBuilder builder = getTupleResultBuilder(req, resp, resp.getOutputStream());
		builder.transform(this.getTransformationUrl(req), "server.xsl");
		builder.start("error-message");
		builder.result("The entered credentials entered either failed to authenticate to the Sesame server, or were unauthorized for the requested operation.");
		builder.end();
	}

	private RepositoryManager createRepositoryManager(final String param)
		throws IOException, RepositoryException
	{
		RepositoryManager manager;
		if (param.startsWith("file:")) {
			manager = new LocalRepositoryManager(asLocalFile(new URL(param)));
		}
		else {
			manager = new RemoteRepositoryManager(param);
		}
		manager.initialize();
		return manager;
	}

	private File asLocalFile(final URL rdf)
		throws UnsupportedEncodingException
	{
		return new File(URLDecoder.decode(rdf.getFile(), "UTF-8"));
	}

	private void service(final String repoID, final HttpServletRequest req, final HttpServletResponse resp)
		throws RepositoryConfigException, RepositoryException, ServletException, IOException
	{
		LOGGER.info("Servicing repository: {}", repoID);
		setCredentials(req, resp);
		final DynamicHttpRequest http = new DynamicHttpRequest(req);
		final String path = req.getPathInfo();
		final int idx = path.indexOf(repoID) + repoID.length();
		http.setServletPath(http.getServletPath() + path.substring(0, idx));
		final String pathInfo = path.substring(idx);
		http.setPathInfo(pathInfo.length() == 0 ? null : pathInfo);
		if (repositories.containsKey(repoID)) {
			repositories.get(repoID).service(http, resp);
		}
		else {
			final Repository repository = manager.getRepository(repoID);
			if (repository == null) {
				final String noId = config.getInitParameter(NO_REPOSITORY);
				if (noId == null || !noId.equals(repoID)) {
					throw new BadRequestException("No such repository: " + repoID);
				}
			}
			final ProxyRepositoryServlet servlet = new ProxyRepositoryServlet();
			servlet.setRepositoryManager(manager);
			if (repository != null) {
				servlet.setRepositoryInfo(manager.getRepositoryInfo(repoID));
				servlet.setRepository(repository);
			}
			servlet.init(new BasicServletConfig(repoID, config));
			repositories.putIfAbsent(repoID, servlet);
			repositories.get(repoID).service(http, resp);
		}
	}

	private String getTransformationUrl(final HttpServletRequest req) {
		final String contextPath = req.getContextPath();
		return contextPath + config.getInitParameter(WorkbenchGateway.TRANSFORMATIONS);
	}

	/**
	 * Set the username and password for all requests to the repository.
	 * 
	 * @param req
	 *        the servlet request
	 * @param resp
	 *        the servlet response
	 * @throws MalformedURLException
	 *         if the repository location is malformed
	 */
	private void setCredentials(final HttpServletRequest req, final HttpServletResponse resp)
		throws MalformedURLException, RepositoryException
	{
		if (manager instanceof RemoteRepositoryManager) {
			final RemoteRepositoryManager rrm = (RemoteRepositoryManager)manager;
			LOGGER.info("RemoteRepositoryManager URL: {}", rrm.getLocation());
			final CookieHandler cookies = new CookieHandler(config);
			final String user = cookies.getCookieNullIfEmpty(req, resp, WorkbenchGateway.SERVER_USER);
			final String password = cookies.getCookieNullIfEmpty(req, resp, WorkbenchGateway.SERVER_PASSWORD);
			LOGGER.info("Setting user '{}' and password '{}'.", user, password);
			rrm.setUsernameAndPassword(user, password);
			// initialize() required to push credentials to internal HTTP
			// client.
			rrm.initialize();
		}
	}
}