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

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class UpdateServlet extends TransformationServlet {

	private final Logger logger = LoggerFactory.getLogger(UpdateServlet.class);

	@Override
	public String[] getCookieNames() {
		return new String[] { "Content-Type" };
	}

	@Override
	protected void doPost(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws Exception, IOException
	{
		try {
			String updateString = req.getParameter("update");

			executeUpdate(updateString);

			resp.sendRedirect("summary");
		}
		catch (BadRequestException exc) {
			logger.warn(exc.toString(), exc);
			TupleResultBuilder builder = getTupleResultBuilder(req, resp, resp.getOutputStream());
			builder.transform(xslPath, "update.xsl");
			builder.start("error-message", "update");
			builder.link(Arrays.asList(INFO, "namespaces"));

			String updateString = req.getParameter("update");
			builder.result(exc.getMessage(), updateString);
			builder.end();
		}
	}

	private void executeUpdate(String updateString)
		throws Exception
	{
		RepositoryConnection con = repository.getConnection();
		Update update;
		try {
			update = con.prepareUpdate(QueryLanguage.SPARQL, updateString);
			update.execute();
		}
		catch (RepositoryException e) {
			throw new BadRequestException(e.getMessage());
		}
		catch (MalformedQueryException e) {
			throw new BadRequestException(e.getMessage());
		}
		catch (UpdateExecutionException e) {
			throw new BadRequestException(e.getMessage());
		}
		finally {
			con.close();
		}
	}

	@Override
	public void service(TupleResultBuilder builder, String xslPath)
		throws RepositoryException, QueryResultHandlerException
	{
		builder.transform(xslPath, "update.xsl");
		builder.start();
		builder.link(Arrays.asList(INFO, "namespaces"));
		builder.end();
	}

}