package org.obda.owlapi;

import java.net.URI;

public interface OBDADatasource {
	public URI getID();

	public String getJDBCURL();

	public String getPassword();

	public String getUsername();

	public String getJDBCDriver();
}
