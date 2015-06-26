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

import static org.openrdf.query.parser.QueryParserRegistry.getInstance;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.openrdf.query.parser.QueryParserFactory;
import org.openrdf.query.resultio.BooleanQueryResultWriterFactory;
import org.openrdf.query.resultio.BooleanQueryResultWriterRegistry;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.TupleQueryResultWriterRegistry;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class InfoServlet extends TransformationServlet {

	@Override
	public String[] getCookieNames() {
		return new String[] { "limit", "queryLn", "infer", "Accept", "Content-Type" };
	}

	protected void service(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws Exception
	{
		String id = info.getId();
		
		// "Caching" of servlet instances can cause this request to succeed even
		// if the repository has been deleted. Client-side code using InfoServlet
		// for repository existential checks expects an error response when the id 
		// no longer exists. 
		if (null != id && !manager.hasRepositoryConfig(id)){
			throw new RepositoryConfigException(id + " does not exist.");
		}
		TupleResultBuilder builder = getTupleResultBuilder(req, resp, resp.getOutputStream());
		builder.start("id", "description", "location", "server", "readable", "writeable", "default-limit",
				"default-queryLn", "default-infer", "default-Accept", "default-Content-Type", "upload-format",
				"query-format", "graph-download-format", "tuple-download-format", "boolean-download-format");
		String desc = info.getDescription();
		URL loc = info.getLocation();
		URL server = getServer();
		builder.result(id, desc, loc, server, info.isReadable(), info.isWritable());
		builder.namedResult("default-limit", req.getParameter("limit"));
		builder.namedResult("default-queryLn", req.getParameter("queryLn"));
		builder.namedResult("default-infer", req.getParameter("infer"));
		builder.namedResult("default-Accept", req.getParameter("Accept"));
		builder.namedResult("default-Content-Type", req.getParameter("Content-Type"));
		for (RDFParserFactory parser : RDFParserRegistry.getInstance().getAll()) {
			String mimeType = parser.getRDFFormat().getDefaultMIMEType();
			String name = parser.getRDFFormat().getName();
			builder.namedResult("upload-format", mimeType + " " + name);
		}
		for (QueryParserFactory factory : getInstance().getAll()) {
			String name = factory.getQueryLanguage().getName();
			builder.namedResult("query-format", name + " " + name);
		}
		for (RDFWriterFactory writer : RDFWriterRegistry.getInstance().getAll()) {
			String mimeType = writer.getRDFFormat().getDefaultMIMEType();
			String name = writer.getRDFFormat().getName();
			builder.namedResult("graph-download-format", mimeType + " " + name);
		}
		for (TupleQueryResultWriterFactory writer : TupleQueryResultWriterRegistry.getInstance().getAll()) {
			String mimeType = writer.getTupleQueryResultFormat().getDefaultMIMEType();
			String name = writer.getTupleQueryResultFormat().getName();
			builder.namedResult("tuple-download-format", mimeType + " " + name);
		}
		for (BooleanQueryResultWriterFactory writer : BooleanQueryResultWriterRegistry.getInstance().getAll()) {
			String mimeType = writer.getBooleanQueryResultFormat().getDefaultMIMEType();
			String name = writer.getBooleanQueryResultFormat().getName();
			builder.namedResult("boolean-download-format", mimeType + " " + name);
		}
		builder.end();
	}

	private URL getServer() {
		try {
			return manager.getLocation();
		}
		catch (MalformedURLException exc) {
			return null;
		}
	}

}
