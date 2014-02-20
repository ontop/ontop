package it.unibz.krdb.obda.owlapi3.directmapping;

/*
 * #%L
 * ontop-obdalib-owlapi3
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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
	
	//The template IRIs of class, datatype property and objecttype property
	private String classIRI;
	private String dataIRI;
	private String objectIRI;
	
	public OntoSchema(){
	}
	
	public OntoSchema(DataDefinition dd){
		this.tablename=dd.getName();
		this.attrList=dd.getAttributes();
		
		baseURI=new String("http://example.org/");
		classIRI=new String(baseURI+"%s");
		dataIRI=new String(baseURI+"%s"+"#"+"%s");
		objectIRI=new String(baseURI+"%s"+"#ref-"+"%s");
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
			
			//The uri is created according to the class template. The name of the table is percent-encoded.
			OWLClass newclass = dataFactory.getOWLClass(IRI.create(String.format(classIRI, percentEncode(tablename))));
			OWLDeclarationAxiom declarationAxiom = dataFactory.getOWLDeclarationAxiom(newclass);
			manager.addAxiom(rootOntology,declarationAxiom );
			manager.saveOntology(rootOntology);
		}
	}
	
	private boolean existClass(OWLOntology rootOntology){
		boolean existClass=false;
		for(OWLClass cls : rootOntology.getClassesInSignature()){
			if(cls.toString().equalsIgnoreCase("<"+String.format(classIRI, percentEncode(tablename))+">")){
				existClass=true;
			}
		}
		return existClass;
	}
	
	public void addProperty(OWLOntology rootOntology, Attribute at) throws OWLOntologyStorageException{
		OWLOntologyManager manager =rootOntology.getOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		if(!existDataProperty(rootOntology, at)){
			OWLDataProperty newdproperty = dataFactory.getOWLDataProperty(IRI.create(String.format(dataIRI, percentEncode(tablename), percentEncode(at.getName()))));
			OWLDeclarationAxiom declarationAxiom = dataFactory.getOWLDeclarationAxiom(newdproperty);
			manager.addAxiom(rootOntology,declarationAxiom );
			manager.saveOntology(rootOntology);
		}
		if(at.isForeignKey() && !existObjectProperty(rootOntology, at)){
			OWLObjectProperty newoproperty = dataFactory.getOWLObjectProperty(IRI.create(String.format(objectIRI, percentEncode(tablename), percentEncode(at.getName()))));
			OWLDeclarationAxiom declarationAxiom = dataFactory.getOWLDeclarationAxiom(newoproperty);
			manager.addAxiom(rootOntology,declarationAxiom );
			manager.saveOntology(rootOntology);
		}
	}
	
	private boolean existDataProperty(OWLOntology rootOntology, Attribute at){
		boolean existDataProperty=false;
		for(OWLDataProperty dtpp : rootOntology.getDataPropertiesInSignature()){
			if(dtpp.toString().equalsIgnoreCase("<"+String.format(dataIRI, percentEncode(tablename), percentEncode(at.getName()))+">")){
				existDataProperty=true;
			}
		}
		return existDataProperty;
	}
	
	private boolean existObjectProperty(OWLOntology rootOntology, Attribute at){
		boolean existObjectProperty=false;
		for(OWLObjectProperty obpp : rootOntology.getObjectPropertiesInSignature()){
			if(obpp.toString().equalsIgnoreCase("<"+String.format(objectIRI, percentEncode(tablename), percentEncode(at.getName()))+">")){
				existObjectProperty=true;
			}
		}
		return existObjectProperty;
	}
	
	
	private String percentEncode(String pe){
		pe = pe.replace("#", "%23");
		pe = pe.replace(".", "%2E");
		pe = pe.replace("-", "%2D");
		pe = pe.replace("/", "%2F");
		
		pe = pe.replace(" ", "%20");
		pe = pe.replace("!", "%21");
		pe = pe.replace("$", "%24");
		pe = pe.replace("&", "%26");
		pe = pe.replace("'", "%27");
		pe = pe.replace("(", "%28");
		pe = pe.replace(")", "%29");
		pe = pe.replace("*", "%2A");
		pe = pe.replace("+", "%2B");
		pe = pe.replace(",", "%2C");
		pe = pe.replace(":", "%3A");
		pe = pe.replace(";", "%3B");
		pe = pe.replace("=", "%3D");
		pe = pe.replace("?", "%3F");
		pe = pe.replace("@", "%40");
		pe = pe.replace("[", "%5B");
		pe = pe.replace("]", "%5D");
		return new String(pe);
	}
	


	

}
