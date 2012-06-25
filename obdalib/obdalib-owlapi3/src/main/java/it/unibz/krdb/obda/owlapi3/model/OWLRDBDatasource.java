package it.unibz.krdb.obda.owlapi3.model;

import java.net.URI;

public interface OWLRDBDatasource {
	public URI getID();

	public String getJDBCURL();

	public String getPassword();

	public String getUsername();

	public String getJDBCDriver();
}
