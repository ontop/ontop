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
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

/**
 * Servlet responsible for presenting the list of repositories, and deleting the
 * chosen one.
 */
public class DeleteServlet extends TransformationServlet {

	/**
	 * Deletes the repository with the given ID, then redirects to the repository
	 * selection page. If given a "checkSafe" parameter, instead returns JSON
	 * response with safe field set to true if safe, false if not.
	 */
	@Override
	protected void doPost(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws Exception
	{
		dropRepository(req.getParameter("id"));
		resp.sendRedirect("../");
	}

	@Override
	protected void service(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws Exception
	{
		String checkSafe = req.getParameter("checkSafe");
		if (null == checkSafe) {
			// Display the form.
			super.service(req, resp, xslPath);
		}
		else {
			// Respond to 'checkSafe' XmlHttpRequest with JSON.
			final PrintWriter writer = new PrintWriter(new BufferedWriter(resp.getWriter()));
			writer.write(new JSONObject().put("safe", manager.isSafeToRemove(checkSafe)).toString());
			writer.flush();
		}

	}

	private void dropRepository(String identity)
		throws RepositoryException, RepositoryConfigException
	{
		manager.removeRepository(identity);
	}

	/**
	 * Presents a page where the user can choose a repository ID to delete.
	 */
	@Override
	public void service(TupleResultBuilder builder, String xslPath)
		throws RepositoryException, QueryResultHandlerException
	{
		builder.transform(xslPath, "delete.xsl");
		builder.start("readable", "writeable", "id", "description", "location");
		builder.link(Arrays.asList(INFO));
		for (RepositoryInfo info : manager.getAllRepositoryInfos()) {
			builder.result(info.isReadable(), info.isWritable(), info.getId(), info.getDescription(),
					info.getLocation());
		}
		builder.end();
	}

}
