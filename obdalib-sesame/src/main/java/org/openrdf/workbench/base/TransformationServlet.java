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
package org.openrdf.workbench.base;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.workbench.exceptions.MissingInitParameterException;
import org.openrdf.workbench.util.CookieHandler;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public abstract class TransformationServlet extends BaseRepositoryServlet {

	protected static final ParserConfig NON_VERIFYING_PARSER_CONFIG;
	
	static {
		NON_VERIFYING_PARSER_CONFIG = new ParserConfig();
		NON_VERIFYING_PARSER_CONFIG.set(BasicParserSettings.VERIFY_DATATYPE_VALUES, false);
		NON_VERIFYING_PARSER_CONFIG.set(BasicParserSettings.VERIFY_LANGUAGE_TAGS, false);
		NON_VERIFYING_PARSER_CONFIG.set(BasicParserSettings.VERIFY_RELATIVE_URIS, false);
	}
	
	public static final String CONTEXT = "context";

	protected static final String INFO = "info";

	private static final String TRANSFORMATIONS = "transformations";

	private static final Logger LOGGER = LoggerFactory.getLogger(TransformationServlet.class);

	private final Map<String, String> defaults = new HashMap<String, String>();

	protected CookieHandler cookies;

	@Override
	public void init(final ServletConfig config)
		throws ServletException
	{
		super.init(config);
		cookies = new CookieHandler(config, this);
		if (config.getInitParameter(TRANSFORMATIONS) == null) {
			throw new MissingInitParameterException(TRANSFORMATIONS);
		}
		if (config != null) {
			final Enumeration<?> names = config.getInitParameterNames();
			while (names.hasMoreElements()) {
				final String name = (String)names.nextElement();
				if (name.startsWith("default-")) {
					defaults.put(name.substring("default-".length()), config.getInitParameter(name));
				}
			}
		}
	}

	public String[] getCookieNames() {
		return new String[0];
	}

	@Override
	public void service(final HttpServletRequest req, final HttpServletResponse resp)
		throws ServletException, IOException
	{
		if (req.getCharacterEncoding() == null) {
			req.setCharacterEncoding("UTF-8");
		}
		resp.setCharacterEncoding("UTF-8");
		resp.setDateHeader("Expires", new Date().getTime() - 10000L);
		resp.setHeader("Cache-Control", "no-cache, no-store");

		final String contextPath = req.getContextPath();
		final String path = config.getInitParameter(TRANSFORMATIONS);
		final String xslPath = contextPath + path;
		try {
			final WorkbenchRequest wreq = new WorkbenchRequest(repository, req, defaults);

			cookies.updateCookies(wreq, resp);
			if ("POST".equals(req.getMethod())) {
				doPost(wreq, resp, xslPath);
			}
			else {
				service(wreq, resp, xslPath);
			}
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
	}

	protected void doPost(final WorkbenchRequest wreq, final HttpServletResponse resp, final String xslPath)
		throws Exception
	{
		service(wreq, resp, xslPath);
	}

	protected void service(final WorkbenchRequest req, final HttpServletResponse resp, final String xslPath)
		throws Exception
	{
		service(getTupleResultBuilder(req, resp, resp.getOutputStream()), xslPath);
	}

	protected void service(final TupleResultBuilder writer, final String xslPath)
		throws Exception
	{
		LOGGER.info("Call made to empty superclass implementation of service(PrintWriter,String) for path: {}",
				xslPath);
	}

}