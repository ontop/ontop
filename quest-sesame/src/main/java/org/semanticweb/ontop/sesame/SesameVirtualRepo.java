package org.semanticweb.ontop.sesame;

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

import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestDBConnection;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.questdb.QuestDBVirtualStore;
import org.semanticweb.ontop.sql.DBMetadata;
import org.semanticweb.ontop.sql.ImplicitDBConstraints;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import org.openrdf.model.Model;
import org.openrdf.repository.RepositoryException;
import org.semanticweb.owlapi.model.OWLOntology;

public class SesameVirtualRepo extends SesameAbstractRepo {

	private QuestDBVirtualStore virtualStore;

	public SesameVirtualRepo(String name, String obdaFile, boolean existential, String rewriting)
			throws Exception {
		this(name, null, obdaFile, existential, rewriting);
	}
	
	public SesameVirtualRepo(String name, String obdaFile, String configFileName) throws Exception {
		this(name, null, obdaFile, configFileName);
	}
	
	public SesameVirtualRepo(String name, String tboxFile, String obdaFile, boolean existential, String rewriting)
			throws Exception {
		super();
		createRepo(name, tboxFile, obdaFile, getPreferencesFromSettings(existential, rewriting), null);
	}	
	
	public SesameVirtualRepo(String name, String tboxFile, String obdaFile, String configFileName) throws Exception {
		super();
		createRepo(name, tboxFile, obdaFile, getPreferencesFromFile(configFileName), null);
	}
	
	public SesameVirtualRepo(String name, OWLOntology tbox, Model mappings, String configFileName) throws Exception {
		super();
		createRepo(name, tbox, mappings, null, getPreferencesFromFile(configFileName));
	}

	public SesameVirtualRepo(String name, OWLOntology tbox, Model mappings, QuestPreferences config) throws Exception {
		this(name, tbox, mappings, null, config);
	}
	
	public SesameVirtualRepo(String name, OWLOntology tbox, Model mappings, DBMetadata metadata, QuestPreferences prop) throws Exception {
		super();
		createRepo(name, tbox, mappings, metadata, prop);
	}
	
	/**
	 * Generate QuestPreferences from a config file
	 * @param configFileName - the path to the config file
	 * @return the read QuestPreferences object
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private QuestPreferences getPreferencesFromFile(String configFileName) throws IOException {
		if (!configFileName.isEmpty()) {
			Properties p = new Properties();
			File configFile = new File(URI.create(configFileName));
			p.load(new FileInputStream(configFile));
			return new QuestPreferences(p);
		} else {
			return new QuestPreferences();
		}
	}
	
	/**
	 * Generate a QuestPreferences object from some passed
	 * arguments as settings
	 * @param existential - boolean to turn existential reasoning on or off (default=false)
	 * @param rewriting - String to indicate rewriting technique to be used (default=TreeWitness)
	 * @return the QuestPreferences object
	 */
	private QuestPreferences getPreferencesFromSettings(boolean existential, String rewriting) {
		Properties p = new Properties();
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		if (existential)
			p.setProperty(QuestPreferences.REWRITE, "true");
		else
			p.setProperty(QuestPreferences.REWRITE, "false");
		if (rewriting.equals("TreeWitness"))
			p.setProperty(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
		else if (rewriting.equals("Default"))
			p.setProperty(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		return new QuestPreferences(p);
	}
	
	private void createRepo(String name, OWLOntology tbox, Model mappings, DBMetadata metadata, QuestPreferences pref) throws Exception 
	{
		this.virtualStore = new QuestDBVirtualStore(name, tbox, mappings, metadata, pref);
	}
	
	/**
	 * Sets the implicit db constraints, i.e. primary and foreign keys not in the database
	 * Must be called before the call to initialize
	 * 
	 * @param userConstraints
	 */
	public void setImplicitDBConstraints(ImplicitDBConstraints userConstraints){
		if(userConstraints == null)
			throw new NullPointerException();
		if(this.isinitialized)
			throw new Error("Implicit DB Constraints must be given before the call to initialize to have effect. See https://github.com/ontop/ontop/wiki/Implicit-database-constraints and https://github.com/ontop/ontop/wiki/API-change-in-SesameVirtualRepo-and-QuestDBVirtualStore");
		this.virtualStore.setImplicitDBConstraints(userConstraints);
	}

	/**
	 * This method leads to the reasoner being initalized, which includes the call to {@link Quest.setupRepository}: connecting to the database, 
	 * analyzing mappings etc. This must be called before any queries are run, i.e. before {@link getQuestConnection}.
	 * @throws RepositoryException 
	 * 
	 */
	@Override
	public void initialize() throws RepositoryException{
		super.initialize();
		try {
			this.virtualStore.initialize();
		}
		catch (Exception e){
			throw new RepositoryException(e);
		}
	}
	
	private void createRepo(String name, String tboxFile, String mappingFile, QuestPreferences pref, ImplicitDBConstraints userConstraints) throws Exception
	{
		if (mappingFile == null) {
			//if we have no mappings 
			// (then user constraints are also no point)
			this.virtualStore = new QuestDBVirtualStore(name, pref);
			
		} else {
			//generate obdaURI
			URI obdaURI;
			if (mappingFile.startsWith("file:"))
				obdaURI = URI.create(mappingFile);
			else
				 obdaURI = new File(mappingFile).toURI();

			if (tboxFile == null) {
				//if we have no owl file
				this.virtualStore = new QuestDBVirtualStore(name, obdaURI, pref);
			} else {
				//if we have both owl and mappings file
				//generate tboxURI
				URI tboxURI;
				if (tboxFile.startsWith("file:"))
					 tboxURI = URI.create(tboxFile);
				else 
					tboxURI = new File(tboxFile).toURI();
				this.virtualStore = new QuestDBVirtualStore(name, tboxURI,	obdaURI, pref);
			}
		}
	}
	
	/**
	 * Returns a connection which can be used to run queries over the repository
	 * Before this method can be used, {@link initialize()} must be called once.
	 */
	@Override
	public QuestDBConnection getQuestConnection() throws OBDAException {
		if(!super.isinitialized)
			throw new Error("The SesameVirtualRepo must be initialized before getQuestConnection can be run. See https://github.com/ontop/ontop/wiki/API-change-in-SesameVirtualRepo-and-QuestDBVirtualStore");

        return virtualStore.getConnection();
	}

	@Override
	public boolean isWritable() throws RepositoryException {
		// Checks whether this repository is writable, i.e.
		// if the data contained in this repository can be changed.
		// The writability of the repository is determined by the writability
		// of the Sail that this repository operates on.
		return false;
	}
	
	@Override
	public void shutDown() throws RepositoryException {
		super.shutDown();
		virtualStore.close();
	}

	public String getType() {
		return QuestConstants.VIRTUAL;
	}

}
