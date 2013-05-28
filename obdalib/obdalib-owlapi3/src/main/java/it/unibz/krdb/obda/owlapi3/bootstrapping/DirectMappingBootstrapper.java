package it.unibz.krdb.obda.owlapi3.bootstrapping;

import org.semanticweb.owlapi.model.OWLOntology;

import it.unibz.krdb.obda.model.OBDAModel;

public class DirectMappingBootstrapper extends AbstractDBMetadata{
	
	private String url, user, password, driver;
	
	public DirectMappingBootstrapper() {
		
	}
	
	public DirectMappingBootstrapper(String url, String user, String password, String driver){
		this.url = url;
		this.user = user;
		this.password = password;
		this.driver = driver;
	}

/***
* Creates an OBDA model using direct mappings
*/
public OBDAModel getMappings() {
	getOntologyAndDirectMappings(null, "file:/C:/Project/ontology.owl");
	return getOBDAModel();
}

/***
* Creates an OBDA file using direct mappings. Internally this one calls the previous one and just
* renders the file.
*/
public  void getMappings(String targetFile){
	getOntologyAndDirectMappings(targetFile, "file:/C:/Project/ontology.owl");
}

@Override
protected String getDriverName() {
	return this.driver;
}

@Override
protected String getConnectionString() {
	return this.url;
}

@Override
protected String getConnectionUsername() {
	return this.user;
}

@Override
protected String getConnectionPassword() {
	return this.password;
}


}
