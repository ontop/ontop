package it.unibz.krdb.obda.owlapi3.bootstrapping;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.owlapi3.directmapping.DirectMappingEngine;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public abstract class AbstractDBMetadata
{
	
	private OWLOntology onto;
	private OBDAModel model;
	
	protected DBMetadata getMetadata() 
	{
		DBMetadata metadata = null;
		try {
			Class.forName(getDriverName());
		}
		catch (ClassNotFoundException e) { /* NO-OP */ }

		try {
			Connection conn = DriverManager.getConnection(getConnectionString(), getConnectionUsername(), getConnectionPassword());
			metadata = JDBCConnectionManager.getMetaData(conn);
		
		} catch (SQLException e) { 
			e.printStackTrace();
		}
		return metadata;
	}
	
	protected void getOntologyAndDirectMappings(String mappings_file, String ontology_uri_str) {
	try{
		DirectMappingEngine engine = new DirectMappingEngine();
		OBDADataFactory fact = OBDADataFactoryImpl.getInstance();
		OBDADataSource source = fact.getJDBCDataSource(getConnectionString(), getConnectionUsername(), getConnectionPassword(), getDriverName());
		 model =  engine.extractMappings(source);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		 onto =  engine.getOntology(manager, model, ontology_uri_str);
		
		if (mappings_file != null)
		{
			ModelIOManager mng = new ModelIOManager(model);
			mng.save(mappings_file);
		}
	}
	catch(Exception e)
	{e.printStackTrace();}
	}
	
	protected OBDAModel getOBDAModel()
	{
		return this.model;
	}
	
	protected OWLOntology getOWLOntology()
	{
		return this.onto;
	}
	
	
	protected abstract String getDriverName();
	protected abstract String getConnectionString();
	protected abstract String getConnectionUsername();
	protected abstract String getConnectionPassword();
}