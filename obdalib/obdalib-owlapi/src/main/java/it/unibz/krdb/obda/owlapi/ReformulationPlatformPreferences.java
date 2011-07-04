package it.unibz.krdb.obda.owlapi;

import it.unibz.krdb.obda.utils.OBDAPreferencePersistanceManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that represents the preferences which can be modified by the user
 * 
 */

public class ReformulationPlatformPreferences{
	// TODO create a configuration listener to handle changes in these values

	/**
	 * 
	 */
	private static final long	serialVersionUID		= -5954970472045517594L;

	private static final String	DEFAULT_PROPERTIESFILE	= "default.properties";

	public static final String	CREATE_TEST_MAPPINGS	= "org.obda.owlreformulationplatform.createTestMappings";
	public static final String	REFORMULATION_TECHNIQUE	= "org.obda.owlreformulationplatform.reformulationTechnique";

	public static final String	ABOX_MODE				= "org.obda.owlreformulationplatform.aboxmode";
	public static final String	DBTYPE					= "org.obda.owlreformulationplatform.dbtype";
	public static final String	DATA_LOCATION			= "org.obda.owlreformulationplatform.datalocation";

	private Logger				log						= LoggerFactory.getLogger(ReformulationPlatformPreferences.class);

	private Properties preferences = null;
	
	private OBDAPreferencePersistanceManager persistanceManager = null;
	
	public ReformulationPlatformPreferences(OBDAPreferencePersistanceManager man) {
		preferences = new Properties();
		persistanceManager = man;
		try {
			readDefaultPropertiesFile();
			loadProperties();
		} catch (IOException e1) {
			log.error("Error reading default properties for resoner.");
			log.debug(e1.getMessage(), e1);
		}
	}

	public ReformulationPlatformPreferences(Properties values, OBDAPreferencePersistanceManager man) {
		persistanceManager = man;
		preferences = new Properties();
		preferences.putAll(values);
	}
	
	private void loadProperties(){
		
		String value = persistanceManager.loadPreference(CREATE_TEST_MAPPINGS);
		if(value != null){
			preferences.setProperty(CREATE_TEST_MAPPINGS, value);
		}
		
		value = persistanceManager.loadPreference(REFORMULATION_TECHNIQUE);
		if(value != null){
			preferences.setProperty(REFORMULATION_TECHNIQUE, value);
		}
		
		value = persistanceManager.loadPreference(ABOX_MODE);
		if(value != null){
			preferences.setProperty(ABOX_MODE, value);
		}
		
		value = persistanceManager.loadPreference(DBTYPE);
		if(value != null){
			preferences.setProperty(DBTYPE, value);
		}
		
		value = persistanceManager.loadPreference(DATA_LOCATION);
		if(value != null){
			preferences.setProperty(DATA_LOCATION, value);
		}
	}
	
	public void readDefaultPropertiesFile() throws IOException {
		InputStream in = ReformulationPlatformPreferences.class.getResourceAsStream(DEFAULT_PROPERTIESFILE);
		readDefaultPropertiesFile(in);
	}


	/**
	 * Reads the properties from the input stream and sets them as default
	 * 
	 * @param in
	 *            the input stream
	 * @throws IOException
	 */
	public void readDefaultPropertiesFile(InputStream in) throws IOException {
		preferences.load(in);
	}


	/**
	 * Returns the current value for the given parameter
	 * 
	 * @param var
	 *            the parameter value
	 * @return the current value
	 */
	public String getCurrentValue(String var) {
		return preferences.getProperty(var);
	}

	/**
	 * Returns the current value as boolean for the given parameter
	 * 
	 * @param var
	 *            the parameter value
	 * @return the current value as boolean if possible null otherwise
	 */
	public boolean getCurrentBooleanValueFor(String var) {
		String value = (String) getCurrentValue(var);
		return Boolean.parseBoolean(value);
	}

	/**
	 * Returns the current value as int for the given parameter
	 * 
	 * @param var
	 *            the parameter value
	 * @return the current value as int if possible null otherwise
	 */
	public int getCurrentIntegerValueFor(String var) {
		String value = (String) getCurrentValue(var);
		return Integer.parseInt(value);
	}

	/**
	 * Updates the current value of the given parameter to the given object
	 * 
	 * @param var
	 *            the parameter
	 * @param obj
	 *            the new current value
	 */
	public void setCurrentValueOf(String var, String obj) {
		preferences.setProperty(var, obj);
		persistanceManager.storePreference(var, obj);
	}
}
