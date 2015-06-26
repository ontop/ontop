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
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class RemoveServlet extends TransformationServlet {

	private final Logger logger = LoggerFactory.getLogger(RemoveServlet.class);

	@Override
	protected void doPost(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws IOException, RepositoryException, QueryResultHandlerException
	{
		String objectParameter = req.getParameter("obj");
		try {
			RepositoryConnection con = repository.getConnection();
			try {
				Resource subj = req.getResource("subj");
				URI pred = req.getURI("pred");
				Value obj = req.getValue("obj");
				if (subj == null && pred == null && obj == null && !req.isParameterPresent(CONTEXT)) {
					throw new BadRequestException("No values");
				}
				remove(con, subj, pred, obj, req);
				// HACK: HTML sends \r\n, but SAX strips out the \r, try both ways
				if (obj instanceof Literal && obj.stringValue().contains("\r\n")) {
					obj = Protocol.decodeValue(objectParameter.replace("\r\n", "\n"), con.getValueFactory());
					remove(con, subj, pred, obj, req);
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
			builder.transform(xslPath, "remove.xsl");
			builder.start("error-message", "subj", "pred", "obj", CONTEXT);
			builder.link(Arrays.asList(INFO));
			builder.result(exc.getMessage(), req.getParameter("subj"), req.getParameter("pred"),
					objectParameter, req.getParameter(CONTEXT));
			builder.end();
		}
	}

	private void remove(RepositoryConnection con, Resource subj, URI pred, Value obj, WorkbenchRequest req)
		throws BadRequestException, RepositoryException
	{
		if (req.isParameterPresent(CONTEXT)) {
			Resource ctx = req.getResource(CONTEXT);
			if (subj == null && pred == null && obj == null) {
				con.clear(ctx);
			}
			else {
				con.remove(subj, pred, obj, ctx);
			}
		}
		else {
			con.remove(subj, pred, obj);
		}
	}

	@Override
	public void service(TupleResultBuilder builder, String xslPath)
		throws RepositoryException, QueryResultHandlerException
	{
		builder.transform(xslPath, "remove.xsl");
		builder.start();
		builder.link(Arrays.asList(INFO));
		builder.end();
	}

}