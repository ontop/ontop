package org.semanticweb.ontop.owlrefplatform.core;

/*
 * #%L
 * ontop-reformulation-core
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.semanticweb.ontop.injection.OBDAProperties;
import org.semanticweb.ontop.utils.OBDAPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that represents the preferences which can be modified by the user.
 */
public class QuestPreferences extends OBDAProperties {

	// TODO create a configuration listener to handle changes in these values
	private static final long	serialVersionUID		= -5954970472045517594L;

	private static final String DEFAULT_QUEST_PROPERTIES_FILE = "QuestDefaults.properties";

	public static final String	REFORMULATION_TECHNIQUE	= "org.obda.owlreformulationplatform.reformulationTechnique";
	public static final String	ABOX_MODE				= "org.obda.owlreformulationplatform.aboxmode";
	public static final String	DBTYPE					= "org.obda.owlreformulationplatform.dbtype";
//	public static final String	DATA_LOCATION			= "org.obda.owlreformulationplatform.datalocation";
	public static final String  OBTAIN_FROM_ONTOLOGY	= "org.obda.owlreformulationplatform.obtainFromOntology";
	public static final String  OBTAIN_FROM_MAPPINGS	= "org.obda.owlreformulationplatform.obtainFromMappings";
	public static final String  OPTIMIZE_EQUIVALENCES 	= "org.obda.owlreformulationplatform.optimizeEquivalences";

    public static final String SQL_GENERATE_REPLACE = "org.obda.owlreformulationplatform.sqlGenerateReplace";

    public static final String  REWRITE 	= "rewrite";
	
	public static final String  OPTIMIZE_TBOX_SIGMA 	= "org.obda.owlreformulationplatform.optimizeTboxSigma";
//	public static final String 	CREATE_TEST_MAPPINGS 	= "org.obda.owlreformulationplatform.createTestMappings";

	public static final String STORAGE_LOCATION = "STORAGE_LOCATION";

    @Deprecated
	public static final String JDBC_URL = OBDAProperties.JDBC_URL;
    @Deprecated
	public static final String DBNAME = OBDAProperties.DB_NAME;
    @Deprecated
	public static final String DBUSER = OBDAProperties.DB_USER;
    @Deprecated
	public static final String DBPASSWORD = OBDAProperties.DB_PASSWORD;
    @Deprecated
	public static final String JDBC_DRIVER = OBDAProperties.JDBC_DRIVER;

	public static final String PRINT_KEYS = "PRINT_KEYS";

	// Tomcat connection pool properties
	public static final String MAX_POOL_SIZE = "max_pool_size";
	public static final String INIT_POOL_SIZE = "initial_pool_size";
	public static final String REMOVE_ABANDONED = "remove_abandoned";
	public static final String ABANDONED_TIMEOUT = "abandoned_timeout";
	public static final String KEEP_ALIVE = "keep_alive";	
	
	private Logger log = LoggerFactory.getLogger(QuestPreferences.class);

	public QuestPreferences() {
        super();
		try {
			readDefaultQuestPropertiesFile();
		} catch (IOException e1) {
			log.error("Error reading default properties for reasoner.");
			log.debug(e1.getMessage(), e1);
		}
	}

	public QuestPreferences(Properties values) {
		this();
		this.putAll(values);
	}

	public void readDefaultQuestPropertiesFile() throws IOException {
		readPropertiesFile(DEFAULT_QUEST_PROPERTIES_FILE);
	}

    private void readPropertiesFile(String fileName) throws IOException {
        InputStream in =QuestPreferences.class.getResourceAsStream(fileName);
        readDefaultPropertiesFile(in);
    }

	/**
	 * Returns the current value for the given parameter.
	 * 
	 * @param var
	 *            The parameter value.
	 * @return The current value.
	 */
	public Object getCurrentValue(String var) {
		return get(var);
	}

	/**
	 * Returns the current value as boolean for the given parameter.
	 * 
	 * @param var
	 *            The parameter value.
	 * @return The current value as boolean if possible null otherwise.
	 */
	public boolean getCurrentBooleanValueFor(String var) {
		String value = (String) getCurrentValue(var);
		return Boolean.parseBoolean(value);
	}

	/**
	 * Returns the current value as an integer for the given parameter.
	 * 
	 * @param var
	 *            The parameter value.
	 * @return The current value as an integer if possible null otherwise.
	 */
	public int getCurrentIntegerValueFor(String var) {
		String value = (String) getCurrentValue(var);
		return Integer.parseInt(value);
	}

	/**
	 * Updates the current value of the given parameter to the given object
	 * 
	 * @param var
	 *            The parameter.
	 * @param obj
	 *            The new current value.
	 */
	public void setCurrentValueOf(String var, Object obj) {
		put(var, obj);
	}
	
	public List<String> getReformulationPlatformPreferencesKeys(){
		ArrayList<String> keys = new ArrayList<String>();
		keys.add(REFORMULATION_TECHNIQUE);
		keys.add(ABOX_MODE);
		keys.add(DBTYPE);
//		keys.add(DATA_LOCATION);
		keys.add(OBTAIN_FROM_ONTOLOGY);
		keys.add(OBTAIN_FROM_MAPPINGS);
		keys.add(OPTIMIZE_EQUIVALENCES);
		keys.add(OPTIMIZE_TBOX_SIGMA);
//		keys.add(CREATE_TEST_MAPPINGS);

		return keys;
	}
}
