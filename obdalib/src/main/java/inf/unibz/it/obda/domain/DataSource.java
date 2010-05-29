/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro. All rights reserved.
 * 
 * The OBDA-API is licensed under the terms of the Lesser General Public License
 * v.3 (see OBDAAPI_LICENSE.txt for details). The components of this work
 * include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, b)
 * third-party components licensed under terms that may be different from those
 * of the LGPL. Information about such licenses can be found in the file named
 * OBDAAPI_3DPARTY-LICENSES.txt.
 */
package inf.unibz.it.obda.domain;

import inf.unibz.it.obda.api.controller.APIController;

import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

//TODO make this an entity too?
public class DataSource {
	// private String uri = "";
	private String		name		= null;
	private boolean		enabled		= true;
	private boolean		registred	= false;
	private URI			ontologyURI	= null;

	private Properties	parameters	= null;

	/***************************************************************************
	 * Creates a new DataSource object
	 * 
	 * @param uri
	 *            The URI of the data source
	 * @param name
	 *            A generic name for this data source
	 */
	public DataSource(String name) {
		this.name = name;
		parameters = new Properties();
	}

	/****
	 * Sets the Ontology URI to which this datasource is associated
	 * 
	 * @param uri
	 */
	public void setUri(String uri) {
		ontologyURI = URI.create(uri);
	}

	public void setParameter(String parameter_uri, String value) {
		this.parameters.setProperty(parameter_uri, value);
	}

	/***
	 * Returns the Ontology URI to which this data source is associated.
	 * 
	 * @return
	 */
	public String getUri() {
		return ontologyURI.toString();
	}

	public String getParameter(String parameter_uri) {
		return parameters.getProperty(parameter_uri);
	}

	public Set<Object> getParameters() {
		return parameters.keySet();
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("DatasourceName=" + name + "\n");
		buff.append("DatasourceURI=" + ontologyURI.toString());
		Enumeration<Object> keys = parameters.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			buff.append("\n" + key + "=" + parameters.getProperty(key));
		}
		return buff.toString();
	}

	/***************************************************************************
	 * This method is unsafe and has been deprecated. Use a DatasourceXMLCodec
	 * instead.
	 * 
	 * Builds a DataSource object from a string that encodes a data source. The
	 * format of the string must be the same as the one produced by the
	 * DataSource.toString() method.
	 * 
	 * Format is:
	 * 
	 * DatasourceURI=uri_value\nparameterkey=parametervalue\n . . .
	 * 
	 * where uri_value, parameterkey and parametervalue must be replaced with
	 * the appropiate strings. Do not use characters "\n", "=" or "|" for these
	 * values.
	 * 
	 * @param enc_datasource
	 * @return
	 */
	@Deprecated
	public static DataSource getFromString(String enc_datasource) {
		StringTokenizer tokenizer = new StringTokenizer(enc_datasource, "\n");

		if (tokenizer.countTokens() < 1)
			throw new IllegalArgumentException("Badly formed string");

		DataSource new_src = null;

		try {
			String name = tokenizer.nextToken().substring(15);
			String uri = tokenizer.nextToken().substring(14);
			new_src = new DataSource(name);

			// TODO remove this and modify all my current OBDA FILES
			/***
			 * This if is only done because before, URI and name were used
			 * interchangable. Since now URI stands for the ontology URI we
			 * check if they are the same, if the are, it means its an old file
			 * and the URI is set to the current ontlogy's URI
			 */
			if (!name.equals(uri)) {
				new_src.setUri(uri);
			} else {
				throw new IllegalArgumentException("ERROR: data source name = URI. Fix the URI value to point to the ontology URI to which this data sources is associated");
//				APIController controller = APIController.getController();
//				URI currentOntologyURI = controller.getCurrentOntologyURI();
//				new_src.setUri(currentOntologyURI.toString());
			}
			while (tokenizer.hasMoreTokens()) {
				StringTokenizer tok2 = new StringTokenizer(tokenizer.nextToken(), "=");
				new_src.setParameter(tok2.nextToken(), tok2.nextToken());
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Badly formed data source string", e);
		}
		return new_src;
	}

	public static void main(String[] args) {
		DataSource d = new DataSource("somename");
		d.setUri("someuri");
		d.setParameter("uri1", "value1");
		d.setParameter("uri2", "value2");
		System.out.println(d);
		DataSource b = DataSource.getFromString(d.toString());
		System.out.println(b);
	}

	public String getName() {
		return name;
	}

	public static String encodeDataSources(HashMap<String, DataSource> datasources) {
		StringBuffer encoded = new StringBuffer();
		if ((datasources == null) || datasources.isEmpty()) {
			return "";
		}

		Iterator<String> keys = datasources.keySet().iterator();

		encoded.append(datasources.get(keys.next()).toString());

		while (keys.hasNext()) {
			encoded.append("|" + datasources.get(keys.next()).toString());
		}
		return encoded.toString();
	}

	public static HashMap<String, DataSource> decodeDataSources(String enc_srcs) {
		if ((enc_srcs == null) || (enc_srcs.equals("")))
			return null;
		StringTokenizer tokenizer = new StringTokenizer(enc_srcs, "|");
		HashMap<String, DataSource> sources = null;
		if (tokenizer.hasMoreElements()) {
			sources = new HashMap<String, DataSource>();
			while (tokenizer.hasMoreElements()) {
				DataSource newsource = DataSource.getFromString(tokenizer.nextToken());
				sources.put(newsource.getName(), newsource);
			}
		}
		return sources;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setRegistred(boolean registred) {
		this.registred = registred;
	}

	public boolean isRegistred() {
		return registred;
	}
	
	public void setName(String name){
		this.name = name;
	}

}
