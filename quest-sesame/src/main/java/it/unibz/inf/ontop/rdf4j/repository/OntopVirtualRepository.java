package it.unibz.inf.ontop.rdf4j.repository;

/*
 * #%L
 * ontop-quest-sesame
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.injection.QuestComponentFactory;
import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.owlrefplatform.core.DBConnector;
import it.unibz.inf.ontop.owlrefplatform.core.OntopConnection;

import it.unibz.inf.ontop.answering.reformulation.OntopQueryReformulator;
import it.unibz.inf.ontop.spec.OBDASpecification;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class OntopVirtualRepository implements org.eclipse.rdf4j.repository.Repository, AutoCloseable {

	private boolean initialized = false;
	private static final Logger logger = LoggerFactory.getLogger(OntopVirtualRepository.class);
	private Map<String, String> namespaces;

	// Temporary (dropped after initialization)
	@Nullable
	private QuestConfiguration configuration;
	@Nullable
	private DBConnector dbConnector;

	public OntopVirtualRepository(QuestConfiguration configuration) {
		this.namespaces = new HashMap<>();
		this.configuration = configuration;
	}

	/**
	 * Returns a new RepositoryConnection.
	 *
	 * (No repository connection sharing for the sake
	 *  of thread-safeness)
	 *
	 */
	public RepositoryConnection getConnection() throws RepositoryException {
		try {
			return new OntopRepositoryConnection(this, getOntopConnection());
		} catch (OBDAException e) {
			logger.error("Error creating repo connection: " + e.getMessage());
			throw new RepositoryException(e.getMessage());
		}
	}


	/**
	 * This method leads to the reasoner being initialized (connecting to the database,
	 * analyzing mappings, etc.). This must be called before any queries are run, i.e. before {@code getConnection}.
	 * 
	 */
	@Override
	public void initialize() throws RepositoryException{
		initialized = true;
		try {
			OBDASpecification obdaSpecification = configuration.loadProvidedSpecification();
			QuestComponentFactory componentFactory = configuration.getInjector().getInstance(QuestComponentFactory.class);

			OntopQueryReformulator queryProcessor = componentFactory.create(obdaSpecification, configuration.getExecutorRegistry());
			dbConnector = componentFactory.create(queryProcessor);
			dbConnector.connect();
		}
		catch (Exception e){
			throw new RepositoryException(e);
		}
	}
	
	/**
	 * Returns a connection which can be used to run queries over the repository
	 * Before this method can be used, initialize() must be called once.
	 */
	private OntopConnection getOntopConnection() throws RepositoryException {
		if(!initialized)
			throw new RepositoryException("The OntopVirtualRepository must be initialized before getConnection can be run.");
		try {
			return dbConnector.getConnection();
		} catch (OntopConnectionException e) {
			throw new RepositoryException(e);
		}

	}

	@Override
	public boolean isWritable() throws RepositoryException {
		return false;
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public void shutDown() throws RepositoryException {
		initialized = false;
		try {
			dbConnector.close();
		} catch (Exception e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public File getDataDir() {
		throw new RepositoryException("Ontop does not have a data directory");
	}

	@Override
	public ValueFactory getValueFactory() {
		// Gets a ValueFactory for this Repository.
		return SimpleValueFactory.getInstance();
	}

	@Override
	public void setDataDir(File arg0) {
		// Ignores it
	}

	@Override
	public void close() throws RepositoryException {
		this.shutDown();
	}

	void setNamespace(String key, String value)
	{
		namespaces.put(key, value);
	}

	String getNamespace(String key)
	{
		return namespaces.get(key);
	}

	Map<String, String> getNamespaces()
	{
		return namespaces;
	}

	void setNamespaces(Map<String, String> nsp)
	{
		this.namespaces = nsp;
	}

	void removeNamespace(String key)
	{
		namespaces.remove(key);
	}
}
