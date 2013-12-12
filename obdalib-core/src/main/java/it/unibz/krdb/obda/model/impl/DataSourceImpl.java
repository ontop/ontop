package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.OBDADataSource;

import java.net.URI;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

public class DataSourceImpl implements OBDADataSource {

	private static final long serialVersionUID = 7903755268613089609L;

	private boolean enabled = true;
	private boolean registred = false;
	private URI id = null;

	private Properties parameters = null;

	/**
	 * Creates a new DataSource object
	 * 
	 * @param uri
	 *            The URI of the data source
	 * @param name
	 *            A generic name for this data source
	 */
	protected DataSourceImpl(URI id) {
		this.id = id;
		parameters = new Properties();
	}
	
	@Override
	public void setParameter(String parameter_uri, String value) {
		this.parameters.setProperty(parameter_uri, value);
	}

	@Override
	public URI getSourceID() {
		return id;
	}

	@Override
	public void setNewID(URI newid) {
		this.id = newid;
	}

	@Override
	public String getParameter(String parameter_uri) {
		return parameters.getProperty(parameter_uri);
	}

	@Override
	public Set<Object> getParameters() {
		return parameters.keySet();
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("DatasourceURI=" + id.toString() + "\n");
		Enumeration<Object> keys = parameters.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			buff.append("\n" + key + "=" + parameters.getProperty(key));
		}
		return buff.toString();
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof DataSourceImpl)) {
			return false;
		}
		DataSourceImpl d2 = (DataSourceImpl) o;
		return d2.id.equals(this.id);

	}
	
	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setRegistred(boolean registred) {
		this.registred = registred;
	}

	@Override
	public boolean isRegistred() {
		return registred;
	}

	@Override
	public Object clone() {
		OBDADataSource clone = new DataSourceImpl(getSourceID());
		for (Object parameter : parameters.keySet()) {
			String key = (String) parameter;
			clone.setParameter(key, parameters.getProperty(key));
		}
		clone.setEnabled(isEnabled());
		clone.setRegistred(isRegistred());

		return clone;
	}
}
