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

import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClearServlet extends TransformationServlet {

	private final Logger logger = LoggerFactory.getLogger(ClearServlet.class);

	@Override
	protected void doPost(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws IOException, RepositoryException, QueryResultHandlerException
	{
		try {
			RepositoryConnection con = repository.getConnection();
			try {
				if (req.isParameterPresent(CONTEXT)) {
					con.clear(req.getResource(CONTEXT));
				}
				else {
					con.clear();
				}
			}
			catch (ClassCastException exc) {
				throw new BadRequestException(exc.getMessage(), exc);
			}
			finally {
				con.close();
			}
			resp.sendRedirect("summary");
		}
		catch (BadRequestException exc) {
			logger.warn(exc.toString(), exc);
			TupleResultBuilder builder = getTupleResultBuilder(req, resp, resp.getOutputStream());
			builder.transform(xslPath, "clear.xsl");
			builder.start("error-message", CONTEXT);
			builder.link(Arrays.asList(INFO));
			builder.result(exc.getMessage(), req.getParameter(CONTEXT));
			builder.end();
		}
	}

	@Override
	public void service(TupleResultBuilder builder, String xslPath)
		throws RepositoryException, QueryResultHandlerException
	{
		// TupleResultBuilder builder = new TupleResultBuilder(out);
		builder.transform(xslPath, "clear.xsl");
		builder.start();
		builder.link(Arrays.asList(INFO));
		builder.end();
	}

}