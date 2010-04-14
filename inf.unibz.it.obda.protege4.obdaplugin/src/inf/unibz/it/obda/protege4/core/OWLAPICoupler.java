package inf.unibz.it.obda.protege4.core;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.APICoupler;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.find.EntityFinder;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;

/***
 * 
 * Interacts with OWLAPI objects to inspect the original ontology. Note that
 * this coupler only works fine if the active ontology is setup properly so that
 * the EntityFinder finds the classes.
 * 
 * TODO Needs to be refactored to handle URI's properly. In general, the whole
 * OBDA API needs to be refactored like this.
 * 
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class OWLAPICoupler implements APICoupler {

	// OWLModelManager owlman = null;
	EntityFinder			finder	= null;
	private APIController	apic;

	// private OWLModelManager mmgr;

	public OWLAPICoupler(APIController apic, EntityFinder finder) {
		// this.mmgr = manager;
		this.apic = apic;
		// this.owlman = manager;
		this.finder = finder;
		// mmgr.addListener(this);
	}

	public boolean isDatatypeProperty(URI propertyURI) {
		
		Set<OWLDataProperty> properties = finder.getMatchingOWLDataProperties("*" + propertyURI.toString());
		if (!properties.isEmpty())
			return true;
		return false;
	}

	public boolean isNamedConcept(URI propertyURI) {
		Set<OWLClass> classes = finder.getMatchingOWLClasses("*" + propertyURI.toString());
		if (!classes.isEmpty())
			return true;
		return false;
		// return classesURIs.contains(propertyURI);
	}

	public boolean isObjectProperty(URI propertyURI) {
		Set<OWLObjectProperty> properties = finder.getMatchingOWLObjectProperties("*" + propertyURI.toString());
		if (!properties.isEmpty())
			return true;
		return false;
	}

}
