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

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.Properties;

import org.openrdf.query.Dataset;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;

public class SesameClassicInMemoryRepo extends SesameClassicRepo {
	
	private static QuestPreferences p = new QuestPreferences();

	public SesameClassicInMemoryRepo(String name, String tboxFile, boolean existential, String rewriting) throws Exception {
		super();
		Properties props = new Properties();
		props.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		props.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		props.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		props.setProperty(QuestPreferences.OBTAIN_FROM_MAPPINGS, "false");
		props.setProperty(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");
		props.setProperty(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX); 
		props.setProperty(QuestPreferences.STORAGE_LOCATION, QuestConstants.INMEMORY);
		if (existential) {
			props.setProperty(QuestPreferences.REWRITE, "true");
		} else {
			props.setProperty(QuestPreferences.REWRITE, "false");
		}
		if (rewriting.equals("TreeWitness")) {
			props.setProperty(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
		} else if (rewriting.equals("Default")) {
			props.setProperty(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		}
		p = new QuestPreferences(props);
		createStore(name, tboxFile, p); 
	}
	
	public SesameClassicInMemoryRepo(String name, Dataset data) throws Exception {
		super();
		Properties props = new Properties();
		props.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		props.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		props.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		props.setProperty(QuestPreferences.OBTAIN_FROM_MAPPINGS, "false");
		props.setProperty(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");
		props.setProperty(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX); 
		props.setProperty(QuestPreferences.STORAGE_LOCATION, QuestConstants.INMEMORY);

		p = new QuestPreferences(props);
		
		createStore(name, data, p); 
	}
	
	public SesameClassicInMemoryRepo(String name, String tboxFilePath, String configFilePath) throws Exception {
		super();
		File configFile = new File(URI.create(configFilePath));
		Properties props = new Properties();
		props.load(new FileInputStream(configFile));

		p = new QuestPreferences(props);
		createStore(name, tboxFilePath, p);
	}
}
