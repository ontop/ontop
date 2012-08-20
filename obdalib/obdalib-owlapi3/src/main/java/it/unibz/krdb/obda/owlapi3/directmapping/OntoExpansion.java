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

	//Two situation that one table would be used to enrich the ontology, listed below
	public void enrichOntology(DBMetadata md, OWLOntology rootOntology) throws OWLOntologyStorageException{
		for(int i=0;i<md.getTableList().size();i++){
			//If the table has a primary key, the all column will be added to the ontology
			//Or the table has no PK, but has more than two FK, which is a n-ary relation
			if(existPK(md.getTableList().get(i))){
				OntoSchema os = new OntoSchema(md.getTableList().get(i));
				os.setBaseURI(this.baseURI);
				os.enrichOntology(rootOntology);
			}else if(numFK(md.getTableList().get(i))>1){
				OntoSchema os = new OntoSchema(md.getTableList().get(i));
				os.setBaseURI(this.baseURI);
				os.enrichOntology(rootOntology);
			}
		}
	}
	
	private boolean existPK(DataDefinition dd){
		boolean existPK =false;
		for(int i=0;i<dd.getAttributes().size();i++){
			if(dd.getAttribute(i+1).bPrimaryKey){
				existPK=true;
			}
		}
		return existPK;
	}
	
	private int numFK(DataDefinition dd){
		int numFK = 0;
		for(int i=0;i<dd.getAttributes().size();i++){
			if(dd.getAttribute(i+1).bForeignKey){
				numFK ++;
			}
		}
		return numFK;
	}

}
