package it.unibz.krdb.obda.owlapi3.directmapping;


import it.unibz.krdb.sql.DataDefinition;
import it.unibz.krdb.sql.api.Attribute;

import java.util.List;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;



public class OntoSchema {
	
	private String tablename;
	private List<Attribute> attrList;
	private String baseURI;
	
	public OntoSchema(){
		this.baseURI=new String("http://www.semanticweb.org/owlapi/ontologies/ontology#");
	}
	
	public OntoSchema(DataDefinition dd){
		this.tablename=dd.getName();
		this.attrList=dd.getAttributes();
	}
	
	public void setBaseURI(String uri){
		this.baseURI=new String(uri);
	}
	
	public void setTN(String tn){
		this.tablename=new String(tn);
	}
	
	
	//Add all the classes, data properties and object properties to the root ontology
	//Existing class/property (Class/Property sharing same name in database and ontology) won't be added
	public void enrichOntology(OWLOntology rootOntology) throws OWLOntologyStorageException{
		addClass(rootOntology);
		for(int i=0;i<this.attrList.size();i++){
			Attribute att = this.attrList.get(i);
			addProperty(rootOntology, att);
		}
	}
	
	
	
	public void addClass(OWLOntology rootOntology) throws OWLOntologyStorageException{
		if(!existClass(rootOntology)){
			OWLOntologyManager manager =rootOntology.getOWLOntologyManager();		
			OWLDataFactory dataFactory = manager.getOWLDataFactory();
			OWLClass newclass = dataFactory.getOWLClass(IRI.create(baseURI+this.tablename));
			OWLDeclarationAxiom declarationAxiom = dataFactory.getOWLDeclarationAxiom(newclass);
			manager.addAxiom(rootOntology,declarationAxiom );
			manager.saveOntology(rootOntology);
		}
	}
	
	private boolean existClass(OWLOntology rootOntology){
		boolean existClass=false;
		for(OWLClass cls : rootOntology.getClassesInSignature()){
			if(cls.toString().contains(tablename+">")){
				existClass=true;
			}
		}
		return existClass;
	}
	
	public void addProperty(OWLOntology rootOntology, Attribute at) throws OWLOntologyStorageException{
		OWLOntologyManager manager =rootOntology.getOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		if(!existDataProperty(rootOntology, at)){
			OWLDataProperty newdproperty = dataFactory.getOWLDataProperty(IRI.create(baseURI+this.tablename+"-"+new String(at.name)));
			OWLDeclarationAxiom declarationAxiom = dataFactory.getOWLDeclarationAxiom(newdproperty);
			manager.addAxiom(rootOntology,declarationAxiom );
			manager.saveOntology(rootOntology);
		}
		if(at.bForeignKey && !existObjectProperty(rootOntology, at)){
			OWLObjectProperty newoproperty = dataFactory.getOWLObjectProperty(IRI.create(baseURI+this.tablename+"-ref-"+new String(at.name)));
			OWLDeclarationAxiom declarationAxiom = dataFactory.getOWLDeclarationAxiom(newoproperty);
			manager.addAxiom(rootOntology,declarationAxiom );
			manager.saveOntology(rootOntology);
		}
	}
	
	private boolean existDataProperty(OWLOntology rootOntology, Attribute at){
		boolean existDataProperty=false;
		for(OWLDataProperty dtpp : rootOntology.getDataPropertiesInSignature()){
			if(dtpp.toString().contains(this.tablename+"-"+at.name+">")){
				existDataProperty=true;
			}
		}
		return existDataProperty;
	}
	
	private boolean existObjectProperty(OWLOntology rootOntology, Attribute at){
		boolean existObjectProperty=false;
		for(OWLObjectProperty obpp : rootOntology.getObjectPropertiesInSignature()){
			if(obpp.toString().contains(this.tablename+"-ref-"+at.name+">")){
				existObjectProperty=true;
			}
		}
		return existObjectProperty;
	}
	


	

}
