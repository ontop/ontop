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

import static org.openrdf.rio.RDFWriterRegistry.getInstance;

import javax.servlet.http.HttpServletResponse;

import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.workbench.base.TupleServlet;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class ExportServlet extends TupleServlet {

	private static final int LIMIT_DEFAULT = 100;

	public ExportServlet() {
		super("export.xsl", "subject", "predicate", "object", "context");
	}

	@Override
	public String[] getCookieNames() {
		return new String[] { "limit", "Accept" };
	}

	@Override
	protected void service(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws Exception
	{
		if (req.isParameterPresent("Accept")) {
			String accept = req.getParameter("Accept");
			RDFFormat format = RDFFormat.forMIMEType(accept);
			if (format != null) {
				resp.setContentType(accept);
				String ext = format.getDefaultFileExtension();
				String attachment = "attachment; filename=export." + ext;
				resp.setHeader("Content-disposition", attachment);
			}
			RepositoryConnection con = repository.getConnection();
			con.setParserConfig(NON_VERIFYING_PARSER_CONFIG);
			try {
				RDFWriterFactory factory = getInstance().get(format);
				if (format.getCharset() != null) {
					resp.setCharacterEncoding(format.getCharset().name());
				}
				con.export(factory.getWriter(resp.getOutputStream()));
			}
			finally {
				con.close();
			}
		}
		else {
			super.service(req, resp, xslPath);
		}
	}

	@Override
	protected void service(WorkbenchRequest req, HttpServletResponse resp,
			TupleResultBuilder builder, RepositoryConnection con) 
		throws Exception
	{
		int limit = LIMIT_DEFAULT;
		if (req.getInt("limit") > 0) {
			limit = req.getInt("limit");
		}
		RepositoryResult<Statement> result = con.getStatements(null, null, null, false);
		try {
			for (int i = 0; result.hasNext() && (i < limit || limit < 1); i++) {
				Statement st = result.next();
				builder.result(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
			}
		}
		finally {
			result.close();
		}
	}

}