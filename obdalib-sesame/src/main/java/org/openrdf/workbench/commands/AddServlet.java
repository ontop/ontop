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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Resource;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class AddServlet extends TransformationServlet {

	private static final String URL = "url";

	private final Logger logger = LoggerFactory.getLogger(AddServlet.class);

	@Override
	protected void doPost(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws IOException, RepositoryException, FileUploadException, QueryResultHandlerException
	{
		try {
			String baseURI = req.getParameter("baseURI");
			String contentType = req.getParameter("Content-Type");
			if (req.isParameterPresent(CONTEXT)) {
				Resource context = req.getResource(CONTEXT);
				if (req.isParameterPresent(URL)) {
					add(req.getUrl(URL), baseURI, contentType, context);
				}
				else {
					add(req.getContentParameter(), baseURI, contentType, req.getContentFileName(), context);
				}
			}
			else {
				if (req.isParameterPresent(URL)) {
					add(req.getUrl(URL), baseURI, contentType);
				}
				else {
					add(req.getContentParameter(), baseURI, contentType, req.getContentFileName());
				}
			}
			resp.sendRedirect("summary");
		}
		catch (BadRequestException exc) {
			logger.warn(exc.toString(), exc);
			TupleResultBuilder builder = getTupleResultBuilder(req, resp, resp.getOutputStream());
			builder.transform(xslPath, "add.xsl");
			builder.start("error-message", "baseURI", CONTEXT, "Content-Type");
			builder.link(Arrays.asList(INFO));
			String baseURI = req.getParameter("baseURI");
			String context = req.getParameter(CONTEXT);
			String contentType = req.getParameter("Content-Type");
			builder.result(exc.getMessage(), baseURI, context, contentType);
			builder.end();
		}
	}

	private void add(InputStream stream, String baseURI, String contentType, String contentFileName,
			Resource... context)
		throws BadRequestException, RepositoryException, IOException
	{
		if (contentType == null) {
			throw new BadRequestException("No Content-Type provided");
		}

		RDFFormat format = null;
		if ("autodetect".equals(contentType)) {
			format = RDFFormat.forFileName(contentFileName);
			if (format == null) {
				throw new BadRequestException("Could not automatically determine Content-Type for content: "
						+ contentFileName);
			}
		}
		else {
			format = RDFFormat.forMIMEType(contentType);
			if (format == null) {
				throw new BadRequestException("Unknown Content-Type: " + contentType);
			}

		}

		RepositoryConnection con = repository.getConnection();
		try {
			con.add(stream, baseURI, format, context);
		}
		catch (RDFParseException exc) {
			throw new BadRequestException(exc.getMessage(), exc);
		}
		catch (IllegalArgumentException exc) {
			throw new BadRequestException(exc.getMessage(), exc);
		}
		finally {
			con.close();
		}
	}

	private void add(URL url, String baseURI, String contentType, Resource... context)
		throws BadRequestException, RepositoryException, IOException
	{
		if (contentType == null) {
			throw new BadRequestException("No Content-Type provided");
		}

		RDFFormat format = null;
		if ("autodetect".equals(contentType)) {
			format = RDFFormat.forFileName(url.getFile());
			if (format == null) {
				throw new BadRequestException("Could not automatically determine Content-Type for content: "
						+ url.getFile());
			}
		}
		else {
			format = RDFFormat.forMIMEType(contentType);
			if (format == null) {
				throw new BadRequestException("Unknown Content-Type: " + contentType);
			}

		}

		try {
			RepositoryConnection con = repository.getConnection();
			try {
				con.add(url, baseURI, format, context);
			}
			finally {
				con.close();
			}
		}
		catch (RDFParseException exc) {
			throw new BadRequestException(exc.getMessage(), exc);
		}
		catch (MalformedURLException exc) {
			throw new BadRequestException(exc.getMessage(), exc);
		}
		catch (IllegalArgumentException exc) {
			throw new BadRequestException(exc.getMessage(), exc);
		}
	}

	@Override
	public void service(TupleResultBuilder builder, String xslPath)
		throws RepositoryException, QueryResultHandlerException
	{
		// TupleResultBuilder builder = getTupleResultBuilder(req, resp);
		builder.transform(xslPath, "add.xsl");
		builder.start();
		builder.link(Arrays.asList(INFO));
		builder.end();
	}

}