package inf.unibz.it.obda.protege4.core;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;

/**
 * The onto OntologyEntitiyInformation administrates all information about the 
 * entities in the referenced ontology and is kept up to date; all modifications 
 * are forwarded and immediately reflected.
 * 
 * @author Manfed Gerstgrasser
 *
 */

public class OntologyEntitiyInformation {

	/**
	 * The referenced ontology
	 */
	private OWLOntology	ontology;
	
	/**
	 * all data properties of the referenced ontology
	 */
	private HashSet<String>	dataProperties;

	/**
	 * all concepts of the referenced ontology
	 */
	private HashSet<String>	classesURIs;

	/**
	 * all object properties of the referenced ontology
	 */
	private HashSet<String>	objectProperties;
	
	
	/**
	 * the constructor. Creates a new instance fo the OntologyEntitiyInformation
	 * @param o the refernced ontology
	 */
	public OntologyEntitiyInformation(OWLOntology o){
	
		ontology = o;
		updateOntologyInfo();
	}
	
	/**
	 * Update all entity information
	 */
	public void refresh(){
		updateOntologyInfo();
	}
	
	/**
	 * Check whether the given URI belongs to a data property of the referenced 
	 * ontology.
	 * @param propertyURI the URI to check
	 * @return true if the URI belongs a data property of the referenced ontology, false otherwise.
	 */
	public boolean isDatatypeProperty(URI propertyURI) {
		return dataProperties.contains(propertyURI.toString());
	}

	/**
	 * Check whether the given URI belongs to a concept of the referenced 
	 * ontology.
	 * @param propertyURI the URI to check
	 * @return true if the URI belongs a concept of the referenced ontology, false otherwise.
	 */
	public boolean isNamedConcept(URI propertyURI) {
		return classesURIs.contains(propertyURI.toString());
	}

	/**
	 * Check whether the given URI belongs to a object property of the referenced 
	 * ontology.
	 * @param propertyURI the URI to check
	 * @return true if the URI belongs a object property of the referenced ontology, false otherwise.
	 */
	public boolean isObjectProperty(URI propertyURI) {
		return objectProperties.contains(propertyURI.toString());
	}
	
	/**
	 * private method that reloads the newest information from 
	 * the referenced ontolgy.
	 */
	
	private void updateOntologyInfo(){
		
		classesURIs = new HashSet<String>();
		dataProperties = new HashSet<String>();
		objectProperties = new HashSet<String>();
		
		Set<OWLClass> set = ontology.getReferencedClasses();
		Iterator<OWLClass> it = set.iterator();
		while(it.hasNext()){
			classesURIs.add(it.next().getURI().toString());
		}
		for (OWLDataProperty c: ontology.getReferencedDataProperties()) {
			dataProperties.add(c.getURI().toString());
		}
		for (OWLObjectProperty c: ontology.getReferencedObjectProperties()) {
			objectProperties.add(c.getURI().toString());
		}
	}
}
