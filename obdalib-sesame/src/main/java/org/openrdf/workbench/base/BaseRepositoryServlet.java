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

import java.net.MalformedURLException;
import java.net.URL;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.rio.RDFFormat;
import org.openrdf.workbench.RepositoryServlet;
import org.openrdf.workbench.exceptions.MissingInitParameterException;

public abstract class BaseRepositoryServlet extends BaseServlet implements RepositoryServlet {
	public static final String REPOSITORY_PARAM = "repository";
	public static final String MANAGER_PARAM = "repository-manager";
	protected RepositoryManager manager;
	protected RepositoryInfo info;
	protected Repository repository;
	protected ValueFactory vf;

	public void setRepositoryManager(RepositoryManager manager) {
		this.manager = manager;
	}

	public void setRepositoryInfo(RepositoryInfo info) {
		this.info = info;
	}

	public void setRepository(Repository repository) {
		if (repository == null) {
			this.vf = new ValueFactoryImpl();
		} else {
			this.repository = repository;
			this.vf = repository.getValueFactory();
			
			if (this.repository instanceof HTTPRepository) {
				((HTTPRepository)this.repository).setPreferredRDFFormat(RDFFormat.BINARY);
			}
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if (repository == null) {
			if (config.getInitParameter(REPOSITORY_PARAM) != null)
				setRepository((Repository) lookup(config, REPOSITORY_PARAM));
		}
		if (manager == null) {
			if (config.getInitParameter(MANAGER_PARAM) == null)
				throw new MissingInitParameterException(MANAGER_PARAM);
			manager = (RepositoryManager) lookup(config, MANAGER_PARAM);
		}
		if (info == null) {
			info = new RepositoryInfo();
			info.setId(config.getInitParameter("id"));
			info.setDescription(config.getInitParameter("description"));
			try {
				if (repository == null) {
					info.setReadable(false);
					info.setWritable(false);
				} else {
					info.setReadable(true);
					info.setWritable(repository.isWritable());
				}
				String location = config.getInitParameter("location");
				if (location != null && location.trim().length() > 0) {
					info.setLocation(new URL(location));
				}
			} catch (MalformedURLException e) {
				throw new ServletException(e);
			} catch (RepositoryException e) {
				throw new ServletException(e);
			}
		}
	}

	private Object lookup(ServletConfig config, String name) throws ServletException {
		String param = config.getInitParameter(name);
		try {
			InitialContext ctx = new InitialContext();
			return ctx.lookup(param);
		} catch (NamingException e) {
			throw new ServletException(e);
		}
	}
}
