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
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.Iterations;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.util.TupleResultBuilder;

public class SummaryServlet extends TransformationServlet {

	private final ExecutorService executorService = Executors.newCachedThreadPool();

	private static final Logger LOGGER = LoggerFactory.getLogger(SummaryServlet.class);

	@Override
	public void service(TupleResultBuilder builder, String xslPath)
		throws RepositoryException, QueryEvaluationException, MalformedQueryException,
		QueryResultHandlerException
	{
		builder.transform(xslPath, "summary.xsl");
		builder.start("id", "description", "location", "server", "size", "contexts");
		builder.link(Arrays.asList(INFO));
		final RepositoryConnection con = repository.getConnection();
		try {
			String size = null;
			String numContexts = null;
			try {
				List<Future<String>> futures = getRepositoryStatistics(con);
				size = getResult("repository size.", futures.get(0));
				numContexts = getResult("labeled contexts.", futures.get(1));
			}
			catch (InterruptedException e) {
				LOGGER.warn("Interrupted while requesting repository statistics.", e);
			}
			builder.result(info.getId(), info.getDescription(), info.getLocation(), getServer(), size,
					numContexts);
			builder.end();
		}
		finally {
			con.close();
		}
	}

	private String getResult(String itemRequested, Future<String> future) {
		String result = "Unexpected interruption while requesting " + itemRequested;
		try {
			if (future.isCancelled()) {
				result = "Timed out while requesting " + itemRequested;
			}
			else {
				try {
					result = future.get();
				}
				catch (ExecutionException e) {
					LOGGER.warn("Exception occured during async request.", e);
					result = "Exception occured while requesting " + itemRequested;
				}
			}
		}
		catch (InterruptedException e) {
			LOGGER.error("Unexpected exception", e);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private List<Future<String>> getRepositoryStatistics(final RepositoryConnection con)
		throws InterruptedException
	{
		List<Future<String>> futures;
		futures = executorService.invokeAll(Arrays.asList(new Callable<String>() {

			@Override
			public String call()
				throws RepositoryException
			{
				return Long.toString(con.size());
			}

		}, new Callable<String>() {

			@Override
			public String call()
				throws RepositoryException
			{
				return Integer.toString(Iterations.asList(con.getContextIDs()).size());
			}

		}), 2000, TimeUnit.MILLISECONDS);
		return futures;
	}

	private String getServer() {
		String result = null; // gracefully ignored by builder.result(...)
		if (manager instanceof LocalRepositoryManager) {
			result = ((LocalRepositoryManager)manager).getBaseDir().toString();
		}
		else if (manager instanceof RemoteRepositoryManager) {
			result = ((RemoteRepositoryManager)manager).getServerURL();
		}
		return result;
	}
}
