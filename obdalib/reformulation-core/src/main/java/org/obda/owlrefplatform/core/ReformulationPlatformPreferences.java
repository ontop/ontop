package org.obda.owlrefplatform.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

/**
 * A class that represents the preferences which can be modified by the user
 * 
 * @author Manfred Gerstgrasser
 */

public class ReformulationPlatformPreferences {
	// TODO create a configuration listener to handle changes in these values

	private static final String				DEFAULT_PROPERTIESFILE						= "default.properties";

	public static final String				UNFOLDING_MECHANMISM						= "org.obda.owlreformulationplatform.unflodingMechanism";
	public static final String				USE_INMEMORY_DB								= "org.obda.owlreformulationplatform.useInMemoryDB";
	public static final String				CREATE_TEST_MAPPINGS						= "org.obda.owlreformulationplatform.createTestMappings";
	public static final String				REFORMULATION_TECHNIQUE						= "org.obda.owlreformulationplatform.reformulationTechnique";
	
	private static final String				DIG_HTTP_PORT								= "dig.http.port";
	private static final String				QUONTO_TBOXXMLDUMP							= "quonto.tboxxmldump";
	private static final String				QUONTO_QUERYAUTICONSISTENCYCHECKING			= "quonto.query.autoconsistencychecking";
	private static final String				QUONTO_ABOX_TYPE							= "quonto.abox.type";
	private static final String				QUONTO_ABOX_MODE							= "quonto.abox.mode";
	private static final String				QUONTO_ABOX_MASTROI_USEVIEWS				= "quonto.abox.mastroi.useviews";
	private static final String				QUONTO_ABOX_JODS_PE_HASHCODE				= "quonto.abox.jods.pe.hashcode";
	private static final String				QUONTO_ABOX_JODS_PE_THREADS_UNFOLDING		= "quonto.abox.jods.pe.threads.unfolding";
	private static final String				QUONTO_ABOX_JODS_PE_USETYPECHECKING			= "quonto.abox.jods.pe.usetypechecking";
	private static final String				QUONTO_ABOX_JODS_PE_THREADS_EXECUTION		= "quonto.abox.jods.pe.threads.execution";
	private static final String				QUONTO_ABOX_JODS_PE_USECONSTRAINTS			= "quonto.abox.jods.pe.useconstraints";
	private static final String				QUONTO_ABOX_JODS_PE_PUTDISTICNT				= "quonto.abox.jods.pe.putdistinct";
	private static final String				QUONTO_ABOX_JODS_PE_RESULTSETMODE			= "quonto.abox.jods.pe.resultsetmode";
	private static final String				QUONTO_ABOX_JODS_PE_RESULTS_FORCEDISTINCT	= "quonto.abox.jods.pe.results.forceDistinct";
	private static final String				QUONTO_ABOX_JODS_PE_NONUNIQUE_CLUSTER_SIZE	= "quonto.abox.jods.pe.nonunique.cluster.size";
	private static final String				QUONTO_ABOX_JODS_PE_OPTIMIZATION_SQO		= "quonto.abox.jods.pe.optimization.SQO";

	private static HashMap<String, Object>	defaultValues								= null;

	private HashMap<String, Object>			values										= null;
	
	// private boolean enableCC = true;

	// protected static Logger log =
	// LoggerFactory.getLogger(QuontoConfiguration.class);

	public static enum ABoxType {
		DIRECT_MAPPINGS, COMPLEX_MAPPING
	}

	public static enum ABoxMode {
		JODS_METHOD, PREDICATEBASED_METHOD
	}

	public static enum ResutlSetMode {
		AGGREGATE, MERGE
	}

	// private ABoxType aboxType = null;
	
	public String toString() {
		StringBuffer string = new StringBuffer();
		string.append("Current values:\n");
		Set<String> keys = values.keySet();
		for (String key : keys) {
			string.append(key + "=" + values.get(key) + "\n");
		}
		string.append("Default values:\n");
		keys = defaultValues.keySet();
		for (String key : keys) {
			string.append(key + "=" + defaultValues.get(key) + "\n");
		}
		return string.toString();
	}

	// private boolean reasoningEnabled = true;

	public ReformulationPlatformPreferences() {
		values = new HashMap<String, Object>();
		try {
			if (defaultValues == null) {
				defaultValues = new HashMap<String, Object>();
				readDefaultPropertiesFile();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public ReformulationPlatformPreferences(Properties values) {
		this();
		setProperties(values);
	}

	public  void readDefaultPropertiesFile() throws IOException {
		InputStream in = ReformulationPlatformPreferences.class.getResourceAsStream(DEFAULT_PROPERTIESFILE);
		readDefaultPropertiesFile(in);
	}

	/***
	 * Reads all the properties and sets them as defaults;
	 * 
	 * @param properties
	 */
	public void setDefaultProperties(Properties properties) {
		// try {
		if (defaultValues == null) {
			defaultValues = new HashMap<String, Object>();
		}

		String prop = properties.getProperty(DIG_HTTP_PORT);
		if (prop != null) {
			int default_port = Integer.valueOf(prop);
			setDefaultValueOf(DIG_HTTP_PORT, default_port);
		}
		prop = properties.getProperty(QUONTO_TBOXXMLDUMP);
		if (prop != null) {
			Boolean aux = Boolean.valueOf(prop.trim());
			boolean default_dumpTBoxXMLs = aux.booleanValue();
			setDefaultValueOf(QUONTO_TBOXXMLDUMP, default_dumpTBoxXMLs);
		}
		prop = properties.getProperty(QUONTO_ABOX_MASTROI_USEVIEWS);
		if (prop != null) {
			Boolean aux = Boolean.valueOf(prop.trim());
			boolean default_useViews = aux.booleanValue();
			setDefaultValueOf(QUONTO_ABOX_MASTROI_USEVIEWS,
					default_useViews);
		}
		prop = properties.getProperty(QUONTO_ABOX_TYPE);
		if (prop != null) {
			if (prop.equals("direct")) {
				setDefaultValueOf(QUONTO_ABOX_TYPE,
						ABoxType.DIRECT_MAPPINGS);
			} else if (prop.equals("complex")) {
				setDefaultValueOf(QUONTO_ABOX_TYPE,
						ABoxType.COMPLEX_MAPPING);
				String mode = properties.getProperty(QUONTO_ABOX_MODE);
				// log.debug(QUONTO_ABOX_MODE + " = " + mode);
				if (mode.equals("jods_method")) {
					setDefaultValueOf(QUONTO_ABOX_MODE,
							ABoxMode.JODS_METHOD);
				} else if (mode.equals("predicatebased_method")) {
					setDefaultValueOf(QUONTO_ABOX_MODE,
							ABoxMode.PREDICATEBASED_METHOD);
				} else {
					throw new IllegalArgumentException("Invalid argument: " + mode);
				}
			} else {
				throw new IllegalArgumentException("Invalid argument: " + prop);
			}
		}
		prop = properties.getProperty(QUONTO_QUERYAUTICONSISTENCYCHECKING);
		if (prop != null) {
			Boolean aux = Boolean.valueOf(prop.trim());
			setDefaultValueOf(QUONTO_QUERYAUTICONSISTENCYCHECKING, aux
					.booleanValue());
			// log.debug(QUONTO_QUERYAUTICONSISTENCYCHECKING + " = " + prop);
		}
		prop = properties.getProperty(QUONTO_ABOX_JODS_PE_HASHCODE);
		if (prop != null) {
			Boolean aux = Boolean.valueOf(prop.trim());
			setDefaultValueOf(QUONTO_ABOX_JODS_PE_HASHCODE, aux
					.booleanValue());
			// log.debug(QUONTO_ABOX_JODS_PE_HASHCODE + " = " + prop);
		}
		prop = properties.getProperty(QUONTO_ABOX_JODS_PE_THREADS_UNFOLDING);
		if (prop != null) {
			setDefaultValueOf(QUONTO_ABOX_JODS_PE_THREADS_UNFOLDING,
					Integer.valueOf(prop));
			// log.debug(QUONTO_ABOX_JODS_PE_THREADS + " = " + prop);
		}
		prop = properties.getProperty(QUONTO_ABOX_JODS_PE_THREADS_EXECUTION);
		if (prop != null) {
			setDefaultValueOf(QUONTO_ABOX_JODS_PE_THREADS_EXECUTION,
					Integer.valueOf(prop));
			// log.debug(QUONTO_ABOX_JODS_PE_THREADS + " = " + prop);
		}
		prop = properties.getProperty(QUONTO_ABOX_JODS_PE_NONUNIQUE_CLUSTER_SIZE);
		if (prop != null) {
			setDefaultValueOf(QUONTO_ABOX_JODS_PE_NONUNIQUE_CLUSTER_SIZE,
					Integer.valueOf(prop));
			// log.debug(QUONTO_ABOX_JODS_PE_THREADS + " = " + prop);
		}
		prop = properties.getProperty(QUONTO_ABOX_JODS_PE_USETYPECHECKING);
		if (prop != null) {
			Boolean aux = Boolean.valueOf(prop.trim());
			setDefaultValueOf(QUONTO_ABOX_JODS_PE_USETYPECHECKING, aux
					.booleanValue());
			// log.debug(QUONTO_ABOX_JODS_PE_USETYPECHECKING + " = " + aux);
		}
		prop = properties.getProperty(QUONTO_ABOX_JODS_PE_USECONSTRAINTS);
		if (prop != null) {
			Boolean aux = Boolean.valueOf(prop.trim());
			setDefaultValueOf(QUONTO_ABOX_JODS_PE_USECONSTRAINTS, aux
					.booleanValue());
			// log.debug(QUONTO_ABOX_JODS_PE_USECONSTRAINTS + " = " + aux);
		}
		prop = properties.getProperty(QUONTO_ABOX_JODS_PE_PUTDISTICNT);
		if (prop != null) {
			Boolean aux = Boolean.valueOf(prop.trim());
			setDefaultValueOf(QUONTO_ABOX_JODS_PE_PUTDISTICNT, aux
					.booleanValue());
			// log.debug(QUONTO_ABOX_JODS_PE_PUTDISTICNT + " = " + aux);
		}
		prop = properties.getProperty(QUONTO_ABOX_JODS_PE_RESULTSETMODE);
		if (prop != null) {
			if (prop.equals("merge")) {
				setDefaultValueOf(QUONTO_ABOX_JODS_PE_RESULTSETMODE, ResutlSetMode.MERGE);
			} else if (prop.equals("aggregate")) {
				setDefaultValueOf(QUONTO_ABOX_JODS_PE_RESULTSETMODE, ResutlSetMode.AGGREGATE);
			} else {
				throw new IllegalArgumentException("Invalid argument: " + prop);
			}
		}
		prop = properties.getProperty(QUONTO_ABOX_JODS_PE_RESULTS_FORCEDISTINCT);
		if (prop != null) {
			Boolean aux = Boolean.valueOf(prop.trim());
			setDefaultValueOf(QUONTO_ABOX_JODS_PE_RESULTS_FORCEDISTINCT,
					aux.booleanValue());
			// log.debug(QUONTO_ABOX_JODS_PE_PUTDISTICNT + " = " + aux);
		}
		prop = properties.getProperty(QUONTO_ABOX_JODS_PE_OPTIMIZATION_SQO);
		if (prop != null) {
			Boolean aux = Boolean.valueOf(prop.trim());
			setDefaultValueOf(QUONTO_ABOX_JODS_PE_OPTIMIZATION_SQO, aux
					.booleanValue());
			// log.debug(QUONTO_ABOX_JODS_PE_PUTDISTICNT + " = " + aux);
		}
		prop = properties.getProperty(UNFOLDING_MECHANMISM);
		if (prop != null) {
			setDefaultValueOf(UNFOLDING_MECHANMISM, prop.trim());
			// log.debug(QUONTO_ABOX_JODS_PE_PUTDISTICNT + " = " + aux);
		}
		prop = properties.getProperty(USE_INMEMORY_DB);
		if (prop != null) {
			setDefaultValueOf(USE_INMEMORY_DB, prop.trim());
			// log.debug(QUONTO_ABOX_JODS_PE_PUTDISTICNT + " = " + aux);
		}
		prop = properties.getProperty(CREATE_TEST_MAPPINGS);
		if (prop != null) {
			setDefaultValueOf(CREATE_TEST_MAPPINGS, prop.trim());
			// log.debug(QUONTO_ABOX_JODS_PE_PUTDISTICNT + " = " + aux);
		}
		prop = properties.getProperty(REFORMULATION_TECHNIQUE);
		if (prop != null) {
			setDefaultValueOf(REFORMULATION_TECHNIQUE, prop.trim());
			// log.debug(QUONTO_ABOX_JODS_PE_PUTDISTICNT + " = " + aux);
		}
	}

	/***
	 * Reads all the properties and sets them as defaults;
	 * 
	 * @param properties
	 */
	public void setProperties(Properties properties) {
		// TODO Some of the parameters are not implemented yet!
		// try {
		String prop = properties.getProperty(DIG_HTTP_PORT);
		if (prop != null) {
			int default_port = Integer.valueOf(prop);
			setCurrentValueOf(DIG_HTTP_PORT, default_port);
			// log.debug(DIG_HTTP_PORT + " = " + prop);
		}
		prop = properties.getProperty(QUONTO_TBOXXMLDUMP);
		if (prop != null) {
			Boolean aux = Boolean.valueOf(prop.trim());
			boolean default_dumpTBoxXMLs = aux.booleanValue();
			setCurrentValueOf(QUONTO_TBOXXMLDUMP, default_dumpTBoxXMLs);
			// log.debug(QUONTO_TBOXXMLDUMP + " = " + prop);
		}
		prop = properties.getProperty(QUONTO_ABOX_MASTROI_USEVIEWS);
		if (prop != null) {
			Boolean aux = Boolean.valueOf(prop.trim());
			boolean default_useViews = aux.booleanValue();
			setCurrentValueOf(QUONTO_ABOX_MASTROI_USEVIEWS, default_useViews);
			// log.debug(QUONTO_ABOX_MASTROI_USEVIEWS + " = " + prop);
		}
		prop = properties.getProperty(QUONTO_ABOX_TYPE);
		if (prop != null) {
			// log.debug(QUONTO_ABOX_TYPE + " = " + prop);
			if (prop.equals("direct")) {
				setCurrentValueOf(QUONTO_ABOX_TYPE, ABoxType.DIRECT_MAPPINGS);
			} else if (prop.equals("complex")) {
				setCurrentValueOf(QUONTO_ABOX_TYPE, ABoxType.COMPLEX_MAPPING);
				String mode = properties.getProperty(QUONTO_ABOX_MODE);
				// log.debug(QUONTO_ABOX_MODE + " = " + mode);
				if (mode.equals("jods_method")) {
					setCurrentValueOf(QUONTO_ABOX_MODE, ABoxMode.JODS_METHOD);
				} else if (mode.equals("predicatebased_method")) {
					setCurrentValueOf(QUONTO_ABOX_MODE, ABoxMode.PREDICATEBASED_METHOD);
				} else {
					throw new IllegalArgumentException("Invalid argument: " + mode);
				}
			} else {
				throw new IllegalArgumentException("Invalid argument: " + prop);
			}
		}
		prop = properties.getProperty(QUONTO_QUERYAUTICONSISTENCYCHECKING);
		if (prop != null) {
			Boolean aux = Boolean.valueOf(prop.trim());
			setCurrentValueOf(QUONTO_QUERYAUTICONSISTENCYCHECKING, aux.booleanValue());
			// log.debug(QUONTO_QUERYAUTICONSISTENCYCHECKING + " = " + prop);
		}
		prop = properties.getProperty(QUONTO_ABOX_JODS_PE_HASHCODE);
		if (prop != null) {
			Boolean aux = Boolean.valueOf(prop.trim());
			setCurrentValueOf(QUONTO_ABOX_JODS_PE_HASHCODE, aux.booleanValue());
			// log.debug(QUONTO_ABOX_JODS_PE_HASHCODE + " = " + prop);
		}
		prop = properties.getProperty(QUONTO_ABOX_JODS_PE_THREADS_UNFOLDING);
		if (prop != null) {
			setCurrentValueOf(QUONTO_ABOX_JODS_PE_THREADS_UNFOLDING, Integer.valueOf(prop));
			// log.debug(QUONTO_ABOX_JODS_PE_THREADS + " = " + prop);
		}
		prop = properties.getProperty(QUONTO_ABOX_JODS_PE_THREADS_EXECUTION);
		if (prop != null) {
			setCurrentValueOf(QUONTO_ABOX_JODS_PE_THREADS_EXECUTION, Integer.valueOf(prop));
			// log.debug(QUONTO_ABOX_JODS_PE_THREADS + " = " + prop);
		}
		prop = properties.getProperty(QUONTO_ABOX_JODS_PE_NONUNIQUE_CLUSTER_SIZE);
		if (prop != null) {
			setCurrentValueOf(QUONTO_ABOX_JODS_PE_NONUNIQUE_CLUSTER_SIZE, Integer.valueOf(prop));
			// log.debug(QUONTO_ABOX_JODS_PE_THREADS + " = " + prop);
		}
		prop = properties.getProperty(QUONTO_ABOX_JODS_PE_USETYPECHECKING);
		if (prop != null) {
			Boolean aux = Boolean.valueOf(prop.trim());
			setCurrentValueOf(QUONTO_ABOX_JODS_PE_USETYPECHECKING, aux.booleanValue());
			// log.debug(QUONTO_ABOX_JODS_PE_USETYPECHECKING + " = " + aux);
		}
		prop = properties.getProperty(QUONTO_ABOX_JODS_PE_USECONSTRAINTS);
		if (prop != null) {
			Boolean aux = Boolean.valueOf(prop.trim());
			setCurrentValueOf(QUONTO_ABOX_JODS_PE_USECONSTRAINTS, aux.booleanValue());
			// log.debug(QUONTO_ABOX_JODS_PE_USECONSTRAINTS + " = " + aux);
		}
		prop = properties.getProperty(QUONTO_ABOX_JODS_PE_RESULTSETMODE);
		if (prop != null) {
			if (prop.equals("merge")) {
				setCurrentValueOf(QUONTO_ABOX_JODS_PE_RESULTSETMODE, ResutlSetMode.MERGE);
			} else if (prop.equals("aggregate")) {
				setCurrentValueOf(QUONTO_ABOX_JODS_PE_RESULTSETMODE, ResutlSetMode.AGGREGATE);
			} else {
				throw new IllegalArgumentException("Invalid argument: " + prop);
			}
		}
		prop = properties.getProperty(QUONTO_ABOX_JODS_PE_RESULTS_FORCEDISTINCT);
		if (prop != null) {
			Boolean aux = Boolean.valueOf(prop.trim());
			setCurrentValueOf(QUONTO_ABOX_JODS_PE_RESULTS_FORCEDISTINCT, aux.booleanValue());
			// log.debug(QUONTO_ABOX_JODS_PE_PUTDISTICNT + " = " + aux);
		}
		prop = properties.getProperty(QUONTO_ABOX_JODS_PE_OPTIMIZATION_SQO);
		if (prop != null) {
			Boolean aux = Boolean.valueOf(prop.trim());
			setCurrentValueOf(QUONTO_ABOX_JODS_PE_OPTIMIZATION_SQO, aux.booleanValue());
			// log.debug(QUONTO_ABOX_JODS_PE_PUTDISTICNT + " = " + aux);
		}
	}

	/**
	 * Reads the properties from the input stream and sets them as default
	 * 
	 * @param in
	 *            the input stream
	 * @throws IOException
	 */
	public void readDefaultPropertiesFile(InputStream in) throws IOException {
		Properties properties = new Properties();
		properties.load(in);
		setDefaultProperties(properties);

	}

	/**
	 * Returns the default value for the given parameter
	 * 
	 * @param var
	 *            the parameter value
	 * @return the default value
	 */
	public Object getDefaultValue(String var) {

		Object o = defaultValues.get(var);
		if (o == null) {
			try {
				throw new Exception("Property not set: " + var);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return o;

	}

	/**
	 * Returns the current value for the given parameter
	 * 
	 * @param var
	 *            the parameter value
	 * @return the current value
	 */
	public Object getCurrentValue(String var) {
		Object o = values.get(var);
		if (o == null) {
			o = getDefaultValue(var);
			if (o == null) {
				try {
					throw new Exception("Property not set: " + var);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				return o;
			}
		}
		return o;
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
	 * Returns the default value as Boolean for the given parameter
	 * 
	 * @param var
	 *            the parameter value
	 * @return the default value as Boolean if possible null otherwise
	 */
	public Boolean getDefaultBooleanValueFor(String var) {

		if (defaultValues != null) {
			Object o = defaultValues.get(var);
			if (o instanceof Boolean) {
				return (Boolean) o;
			} else {
				try {
					throw new Exception("Variable " + var + " is not of boolean type (" + o.toString() + ")");
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		} else {
			try {
				throw new Exception("Default values are not set!");
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * Returns the default value as int for the given parameter
	 * 
	 * @param var
	 *            the parameter value
	 * @return the default value as int if possible null otherwise
	 */
	public Integer getDefaultIntegerValueFor(String var) {

		if (defaultValues != null) {
			Object o = defaultValues.get(var);
			if (o instanceof Integer) {
				return (Integer) o;
			} else {
				try {
					throw new Exception("Variable " + var + " is not of integer type");
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		} else {
			try {
				throw new Exception("Default values are not set!");
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * Updates the default value of the given parameter to the given object
	 * 
	 * @param var
	 *            the parameter
	 * @param obj
	 *            the new default value
	 */
	public void setDefaultValueOf(String var, Object obj) {

		defaultValues.put(var, obj);
	}

	/**
	 * Updates the current value of the given parameter to the given object
	 * 
	 * @param var
	 *            the parameter
	 * @param obj
	 *            the new current value
	 */
	public void setCurrentValueOf(String var, Object obj) {
		values.put(var, obj);
	}
}
