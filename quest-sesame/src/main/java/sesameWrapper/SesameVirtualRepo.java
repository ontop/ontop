package sesameWrapper;

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

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.questdb.QuestDBVirtualStore;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.UserConstraints;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import org.openrdf.model.Graph;
import org.openrdf.model.Model;
import org.openrdf.repository.RepositoryException;
import org.semanticweb.owlapi.model.OWLOntology;

public class SesameVirtualRepo extends SesameAbstractRepo {

	private QuestDBVirtualStore virtualStore;
	private QuestDBConnection questDBConn;

	@Deprecated
	public SesameVirtualRepo(String name, String obdaFile, boolean existential, String rewriting)
			throws Exception {
		this(name, null, obdaFile, existential, rewriting);
	}
	
	
	public SesameVirtualRepo(String name, String obdaFile, String configFileName) throws Exception {
		this(name, null, obdaFile, configFileName);
	}
	
	@Deprecated
	public SesameVirtualRepo(String name, String tboxFile, String obdaFile, boolean existential, String rewriting)
			throws Exception {
		super();
		createRepo(name, tboxFile, obdaFile, getPreferencesFromSettings(existential, rewriting), null);
	}	
	
	
	public SesameVirtualRepo(String name, String tboxFile, String obdaFile, String configFileName) throws Exception {
		super();
		createRepo(name, tboxFile, obdaFile, getPreferencesFromFile(configFileName), null);
	}
	
	@Deprecated	
	public SesameVirtualRepo(String name, OWLOntology tbox, Model mappings, String configFileName) throws Exception {
		super();
		createRepo(name, tbox, mappings, null, getPreferencesFromFile(configFileName), null);
	}

	public SesameVirtualRepo(String name, OWLOntology tbox, Model mappings, QuestPreferences config) throws Exception {
		this(name, tbox, mappings, null, config);
	}
	
	public SesameVirtualRepo(String name, OWLOntology tbox, Model mappings, DBMetadata metadata, QuestPreferences prop) throws Exception {
		super();
		createRepo(name, tbox, mappings, metadata, prop, null);
	}
	
	/**
	 * Generate QuestPreferences from a config file
	 * @param configFileName - the path to the config file
	 * @return the read QuestPreferences object
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private QuestPreferences getPreferencesFromFile(String configFileName) throws FileNotFoundException, IOException {
		QuestPreferences pref = new QuestPreferences();
		if (!configFileName.isEmpty()) {
			File configFile = new File(URI.create(configFileName));
			pref.readDefaultPropertiesFile(new FileInputStream(configFile));
		} else {
			pref.readDefaultPropertiesFile();
		}
		return pref;
	}
	
	/**
	 * Generate a QuestPreferences object from some passed
	 * arguments as settings
	 * @param existential - boolean to turn existential reasoning on or off (default=false)
	 * @param rewriting - String to indicate rewriting technique to be used (default=TreeWitness)
	 * @return the QuestPreferences object
	 */
	private QuestPreferences getPreferencesFromSettings(boolean existential, String rewriting) {
		QuestPreferences pref = new QuestPreferences();
		pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		if (existential)
			pref.setCurrentValueOf(QuestPreferences.REWRITE, "true");
		else
			pref.setCurrentValueOf(QuestPreferences.REWRITE, "false");
		if (rewriting.equals("TreeWitness"))
			pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
		else if (rewriting.equals("Default"))
			pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		return pref;
	}
	
	private void createRepo(String name, OWLOntology tbox, Model mappings, DBMetadata metadata, QuestPreferences pref, UserConstraints userConstraints) throws Exception 
	{
		this.virtualStore = new QuestDBVirtualStore(name, tbox, mappings, metadata, pref, userConstraints);
	}
	
	
	private void createRepo(String name, String tboxFile, String mappingFile, QuestPreferences pref, UserConstraints userConstraints) throws Exception
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
				this.virtualStore = new QuestDBVirtualStore(name, obdaURI, pref, userConstraints);
			} else {
				//if we have both owl and mappings file
				//generate tboxURI
				URI tboxURI;
				if (tboxFile.startsWith("file:"))
					 tboxURI = URI.create(tboxFile);
				else 
					tboxURI = new File(tboxFile).toURI();
				this.virtualStore = new QuestDBVirtualStore(name, tboxURI,	obdaURI, pref, userConstraints);
			}
		}
	}
	
	@Override
	public QuestDBConnection getQuestConnection() throws OBDAException {
		questDBConn = this.virtualStore.getConnection();
		return questDBConn;
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
		try {
			questDBConn.close();
			virtualStore.close();
		} catch (OBDAException e) {
			e.printStackTrace();
		}
	}

	public String getType() {
		return QuestConstants.VIRTUAL;
	}

}
