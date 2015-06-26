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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.OpenRDFException;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.QueryStorage;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

/**
 * Servlet that provides a page to access saved queries.
 * 
 * @author Dale Visser
 */
public class SavedQueriesServlet extends TransformationServlet {

	private QueryStorage storage;

	@Override
	public String[] getCookieNames() {
		return new String[] { "limit", "queryLn", "infer", "total_result_count" };
	}

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
	protected void service(final WorkbenchRequest req, final HttpServletResponse resp, final String xslPath)
		throws IOException, OpenRDFException, BadRequestException
	{
		final TupleResultBuilder builder = getTupleResultBuilder(req, resp, resp.getOutputStream());
		builder.transform(xslPath, "saved-queries.xsl");
		builder.start();
		builder.link(Arrays.asList(INFO));
		this.getSavedQueries(req, builder);
		builder.end();
	}

	@Override
	protected void doPost(final WorkbenchRequest wreq, final HttpServletResponse resp, final String xslPath)
		throws BadRequestException, IOException, OpenRDFException
	{
		final String urn = wreq.getParameter("delete");
		if (null == urn || urn.isEmpty()) {
			throw new BadRequestException("Expected POST to contain a 'delete=' parameter.");
		}
		final boolean accessible = storage.checkAccess((HTTPRepository)this.repository);
		if (accessible) {
			String userName = wreq.getParameter(SERVER_USER);
			if (null == userName) {
				userName = "";
			}
			final URIImpl queryURI = new URIImpl(urn);
			if (storage.canChange(queryURI, userName)) {
				storage.deleteQuery(queryURI, userName);
			}
			else {
				throw new BadRequestException("User '" + userName + "' may not delete query id " + urn);
			}
		}
		this.service(wreq, resp, xslPath);
	}

	private void getSavedQueries(final WorkbenchRequest req, final TupleResultBuilder builder)
		throws OpenRDFException, BadRequestException
	{
		final HTTPRepository repo = (HTTPRepository)this.repository;
		String user = req.getParameter(SERVER_USER);
		if (null == user) {
			user = "";
		}
		if (!storage.checkAccess(repo)) {
			throw new BadRequestException("User '" + user + "' not authorized to access repository '"
					+ repo.getRepositoryURL() + "'");
		}
		storage.selectSavedQueries(repo, user, builder);
	}
}