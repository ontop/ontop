package it.unibz.inf.ontop.owlrefplatform.questdb;

/*
 * #%L
 * ontop-quest-db
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


import java.io.IOException;

import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.QuestComponentFactory;
import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.owlrefplatform.core.DBConnector;
import it.unibz.inf.ontop.owlrefplatform.core.IQuestConnection;
import it.unibz.inf.ontop.owlrefplatform.core.QuestDBConnection;
import it.unibz.inf.ontop.reformulation.OBDAQueryProcessor;
import it.unibz.inf.ontop.spec.OBDASpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/***
 * A bean that holds all the data about a store, generates a store folder and
 * maintains this data.
 */
public class QuestDBVirtualStore implements AutoCloseable {

	private static Logger log = LoggerFactory.getLogger(QuestDBVirtualStore.class);
	
	private boolean isinitalized = false;

	// Temporary (dropped after initialization)
	@Nullable
	private QuestConfiguration configuration;
	private DBConnector dbConnector;


	public QuestDBVirtualStore(@Nonnull QuestConfiguration config) {
		// TODO: re-cast the exception to a Sesame-specific one
		config.validate();

		this.configuration = config;
	}

	/**
	 * Must be called once after the constructor call and before any queries are run, that is,
	 * before the call to getQuestConnection.
	 *
	 */
	public void initialize() throws IOException, OBDASpecificationException {
		if(this.isinitalized){
			log.warn("Double initialization of QuestDBVirtualStore");
		} else {
			this.isinitalized = true;
		}

		OBDASpecification obdaSpecification = configuration.loadProvidedSpecification();
		QuestComponentFactory componentFactory = configuration.getInjector().getInstance(QuestComponentFactory.class);

		OBDAQueryProcessor queryProcessor = componentFactory.create(obdaSpecification, configuration.getExecutorRegistry());
		dbConnector = componentFactory.create(queryProcessor);
		dbConnector.connect();
	}

	public QuestDBConnection getConnection() throws OBDAException {
		//	System.out.println("getquestdbconn..");
		return new QuestDBConnection(getQuestConnection());
	}

	/**
	 * Get a Quest connection from the Quest instance
	 * @return the QuestConnection
	 */
	private IQuestConnection getQuestConnection() throws OBDAException {
		if(!this.isinitalized)
			throw new Error("The QuestDBVirtualStore must be initialized before getQuestConnection can be run.");
		// System.out.println("getquestconn..");
		return dbConnector.getConnection();
	}

	/**
	 * Shut down Quest and its connections.
	 */
	public void close() {
		dbConnector.close();
	}
}
