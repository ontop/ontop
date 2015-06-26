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

import info.aduna.iteration.Iterations;

import org.openrdf.model.Namespace;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class NamespacesServlet extends TransformationServlet {

	@Override
	protected void doPost(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws Exception
	{
		RepositoryConnection con = repository.getConnection();
		try {
			String prefix = req.getParameter("prefix");
			String namespace = req.getParameter("namespace");
			if (namespace.length() > 0) {
				con.setNamespace(prefix, namespace);
			}
			else {
				con.removeNamespace(prefix);
			}
		}
		finally {
			con.close();
		}
		super.service(req, resp, xslPath);
	}

	@Override
	public void service(TupleResultBuilder builder, String xslPath)
		throws RepositoryException, QueryResultHandlerException
	{
		// TupleResultBuilder builder = new TupleResultBuilder(out);
		builder.transform(xslPath, "namespaces.xsl");
		RepositoryConnection con = repository.getConnection();
		con.setParserConfig(NON_VERIFYING_PARSER_CONFIG);
		try {
			builder.start("prefix", "namespace");
			builder.link(Arrays.asList(INFO));
			for (Namespace ns : Iterations.asList(con.getNamespaces())) {
				builder.result(ns.getPrefix(), ns.getName());
			}
			builder.end();
		}
		finally {
			con.close();
		}
	}

}
