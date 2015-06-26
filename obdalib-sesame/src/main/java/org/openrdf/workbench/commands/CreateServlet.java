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
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Arrays;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import info.aduna.io.IOUtil;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Graph;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.ConfigTemplate;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.repository.manager.SystemRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.runtime.RepositoryManagerFederator;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class CreateServlet extends TransformationServlet {

	private RepositoryManagerFederator rmf;

	@Override
	public void init(final ServletConfig config)
		throws ServletException
	{
		super.init(config);
		this.rmf = new RepositoryManagerFederator(manager);
	}

	/**
	 * POST requests to this servlet come from the various specific create-* form
	 * submissions.
	 */
	@Override
	protected void doPost(final WorkbenchRequest req, final HttpServletResponse resp, final String xslPath)
		throws ServletException
	{
		try {
			resp.sendRedirect("../" + createRepositoryConfig(req) + "/summary");
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
	}

	/**
	 * GET requests to this servlet come from the Workbench side bar or from
	 * create.xsl form submissions.
	 * 
	 * @throws RepositoryException
	 * @throws QueryResultHandlerException 
	 */
	@Override
	protected void service(final WorkbenchRequest req, final HttpServletResponse resp, final String xslPath)
		throws IOException, RepositoryException, QueryResultHandlerException
	{
		final TupleResultBuilder builder = getTupleResultBuilder(req, resp, resp.getOutputStream());
		boolean federate;
		if (req.isParameterPresent("type")) {
			final String type = req.getTypeParameter();
			federate = "federate".equals(type);
			builder.transform(xslPath, "create-" + type + ".xsl");
		}
		else {
			federate = false;
			builder.transform(xslPath, "create.xsl");
		}
		builder.start(federate ? new String[] { "id", "description", "location" } : new String[] {});
		builder.link(Arrays.asList(INFO));
		if (federate) {
			for (RepositoryInfo info : manager.getAllRepositoryInfos()) {
				String identity = info.getId();
				if (!SystemRepository.ID.equals(identity)) {
					builder.result(identity, info.getDescription(), info.getLocation());
				}
			}
		}
		builder.end();
	}

	private String createRepositoryConfig(final WorkbenchRequest req)
		throws IOException, OpenRDFException
	{
		String type = req.getTypeParameter();
		String newID;
		if ("federate".equals(type)) {
			newID = req.getParameter("Local repository ID");
			rmf.addFed(newID, req.getParameter("Repository title"),
					Arrays.asList(req.getParameterValues("memberID")),
					Boolean.parseBoolean(req.getParameter("readonly")),
					Boolean.parseBoolean(req.getParameter("distinct")));
		}
		else {
			newID = updateRepositoryConfig(getConfigTemplate(type).render(req.getSingleParameterMap())).getID();
		}
		return newID;
	}

	private RepositoryConfig updateRepositoryConfig(final String configString)
		throws IOException, OpenRDFException
	{
		final Repository systemRepo = manager.getSystemRepository();
		final Graph graph = new LinkedHashModel();
		final RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE, systemRepo.getValueFactory());
		rdfParser.setRDFHandler(new StatementCollector(graph));
		rdfParser.parse(new StringReader(configString), RepositoryConfigSchema.NAMESPACE);
		final RepositoryConfig repConfig = RepositoryConfig.create(graph,
				GraphUtil.getUniqueSubject(graph, RDF.TYPE, RepositoryConfigSchema.REPOSITORY));
		repConfig.validate();
		RepositoryConfigUtil.updateRepositoryConfigs(systemRepo, repConfig);
		return repConfig;
	}

	private ConfigTemplate getConfigTemplate(final String type)
		throws IOException
	{
		final InputStream ttlInput = RepositoryConfig.class.getResourceAsStream(type + ".ttl");
		try {
			final String template = IOUtil.readString(new InputStreamReader(ttlInput, "UTF-8"));
			return new ConfigTemplate(template);
		}
		finally {
			ttlInput.close();
		}
	}
}
