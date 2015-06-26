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

import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.util.TupleResultBuilder;

public class InformationServlet extends TransformationServlet {

	@Override
	public void service(final TupleResultBuilder builder, final String xslPath)
		throws RepositoryException, QueryResultHandlerException
	{
		// final TupleResultBuilder builder = getTupleResultBuilder(req, resp);
		builder.transform(xslPath, "information.xsl");
		builder.start("version", "os", "jvm", "user", "memory-used", "maximum-memory");
		builder.link(Arrays.asList(INFO));
		final String version = this.appConfig.getVersion().toString();
		final String osName = getOsName();
		final String jvm = getJvmName();
		final String user = System.getProperty("user.name");
		final long total = Runtime.getRuntime().totalMemory();
		final long free = Runtime.getRuntime().freeMemory();
		final String used = ((total - free) / 1024 / 1024) + " MB";
		final String max = (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " MB";
		builder.result(version, osName, jvm, user, used, max);
		builder.end();
	}

	private String getOsName() {
		final StringBuilder builder = new StringBuilder();
		builder.append(System.getProperty("os.name")).append(" ");
		builder.append(System.getProperty("os.version")).append(" (");
		builder.append(System.getProperty("os.arch")).append(")");
		return builder.toString();
	}

	private String getJvmName() {
		final StringBuilder builder = new StringBuilder();
		builder.append(System.getProperty("java.vm.vendor")).append(" ");
		builder.append(System.getProperty("java.vm.name")).append(" (");
		builder.append(System.getProperty("java.version")).append(")");
		return builder.toString();
	}

}
