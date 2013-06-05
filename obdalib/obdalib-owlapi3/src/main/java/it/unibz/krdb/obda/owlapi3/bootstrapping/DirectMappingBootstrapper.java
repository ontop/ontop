package it.unibz.krdb.obda.owlapi3.bootstrapping;

import java.sql.SQLException;

import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl;

public class DirectMappingBootstrapper extends AbstractDBMetadata{
	
	
	public DirectMappingBootstrapper() {
		
	}
	
	public DirectMappingBootstrapper(String uri, String url, String user, String password, String driver) throws OWLOntologyCreationException, OWLOntologyStorageException, SQLException, DuplicateMappingException{
		OBDADataFactory fact = OBDADataFactoryImpl.getInstance();
		OBDADataSource source = fact.getJDBCDataSource(url, user, password, driver);
		//create empty ontology and model, add source to model
		OWLOntologyManager mng = OWLManager.createOWLOntologyManager();
		OWLOntology onto = mng.createOntology(IRI.create(uri));
		OBDAModel model = fact.getOBDAModel();
		model.addSource(source);
		getOntologyAndDirectMappings(onto, model, source);
	}

	public DirectMappingBootstrapper(OWLOntology ontology, OBDAModel model, OBDADataSource source) throws Exception{
		getOntologyAndDirectMappings(ontology, model, source);
	}

	/***
	 * Creates an OBDA model using direct mappings
	 */
	public OBDAModel getModel() {
		return getOBDAModel();
	}

	/***
	 * Creates an OBDA file using direct mappings. Internally this one calls the
	 * previous one and just renders the file.
	 */
	public OWLOntology getOntology() {
		return getOWLOntology();
	}

}
