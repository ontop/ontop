package it.unibz.krdb.obda.owlapi;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that represents the preferences which can be modified by the user.
 */
public class ReformulationPlatformPreferences extends Properties {

	// TODO create a configuration listener to handle changes in these values
	private static final long	serialVersionUID		= -5954970472045517594L;

	private static final String	DEFAULT_PROPERTIESFILE	= "default.properties";

	public static final String	REFORMULATION_TECHNIQUE	= "org.obda.owlreformulationplatform.reformulationTechnique";
	public static final String	ABOX_MODE				= "org.obda.owlreformulationplatform.aboxmode";
	public static final String	DBTYPE					= "org.obda.owlreformulationplatform.dbtype";
	public static final String	DATA_LOCATION			= "org.obda.owlreformulationplatform.datalocation";
	public static final String  OBTAIN_FROM_ONTOLOGY	= "org.obda.owlreformulationplatform.obtainFromOntology";
	public static final String  OBTAIN_FROM_MAPPINGS	= "org.obda.owlreformulationplatform.obtainFromMappings";
	public static final String  ELIMINATE_EQUIVALENCES 	= "org.obda.owlreformulationplatform.eliminateEquivalences";
	
	private Logger				log						= LoggerFactory.getLogger(ReformulationPlatformPreferences.class);

	public ReformulationPlatformPreferences() {
		try {
			readDefaultPropertiesFile();
		} catch (IOException e1) {
			log.error("Error reading default properties for resoner.");
			log.debug(e1.getMessage(), e1);
		}
	}

	public ReformulationPlatformPreferences(Properties values) {
		this();
		this.putAll(values);
	}

	public void readDefaultPropertiesFile() throws IOException {
		InputStream in = ReformulationPlatformPreferences.class.getResourceAsStream(DEFAULT_PROPERTIESFILE);
		readDefaultPropertiesFile(in);
	}

	/**
	 * Reads the properties from the input stream and sets them as default.
	 * 
	 * @param in
	 *            The input stream.
	 */
	public void readDefaultPropertiesFile(InputStream in) throws IOException {
		this.load(in);
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
		keys.add(DATA_LOCATION);
		keys.add(OBTAIN_FROM_ONTOLOGY);
		keys.add(OBTAIN_FROM_MAPPINGS);
		keys.add(ELIMINATE_EQUIVALENCES);

		return keys;
	}
}
