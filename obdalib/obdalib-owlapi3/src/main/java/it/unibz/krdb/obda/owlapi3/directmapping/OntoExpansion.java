package it.unibz.krdb.obda.owlapi3.directmapping;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DataDefinition;

public class OntoExpansion {
	
	private String baseURI;
	
	public OntoExpansion(){
		this.baseURI=new String("http://www.semanticweb.org/owlapi/ontologies/ontology#");
	}
	
	public void setURI(String uri){
		this.baseURI = new String(uri);
	}

	
	public void enrichOntology(DBMetadata md, OWLOntology rootOntology) throws OWLOntologyStorageException{
		for(int i=0;i<md.getTableList().size();i++){
			OntoSchema os = new OntoSchema(md.getTableList().get(i));
			os.setBaseURI(this.baseURI);
			os.enrichOntology(rootOntology);
			
		}
	}

}
