package inf.unibz.it.obda.owlapi;

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.model.APICoupler;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owl.util.OWLOntologyMerger;

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
	// EntityFinder finder = null;
	// private APIController apic;

	// private OWLModelManager mmgr;

	private OWLOntologyManager	mmgr	= null;

	private OWLOntology			mergedOntology;

	private HashSet<String>		dataProperties;

	private HashSet<String>		classesURIs;

	private HashSet<String>		objectProperties;

	private PrefixManager		prefixman;

	// private OWLOntology root;
	//

	public OWLAPICoupler(OWLOntologyManager mmgr, PrefixManager prefixman) {
		// this.mmgr = manager;
		this.prefixman = prefixman;
		this.mmgr = mmgr;

		// this.root = root;
		// this.datamanager = iomanager;

		classesURIs = new HashSet<String>();
		dataProperties = new HashSet<String>();
		objectProperties = new HashSet<String>();

		Set<OWLClass> set = mergedOntology.getReferencedClasses();
		Iterator<OWLClass> it = set.iterator();
		while (it.hasNext()) {
			classesURIs.add(it.next().getURI().toString());
		}
		// for (OWLClass c: mergedOntology.getClassesInSignature()) {
		// classesURIs.add(c.getURI().toString());
		// }
		for (OWLDataProperty c : mergedOntology.getReferencedDataProperties()) {
			dataProperties.add(c.getURI().toString());
		}
		for (OWLObjectProperty c : mergedOntology.getReferencedObjectProperties()) {
			objectProperties.add(c.getURI().toString());
		}

	}

	public boolean isDatatypeProperty(URI propertyURI) {
		return dataProperties.contains(propertyURI.toString());
	}

	public boolean isNamedConcept(URI propertyURI) {
		return classesURIs.contains(propertyURI.toString());
	}

	public boolean isObjectProperty(URI propertyURI) {
		return objectProperties.contains(propertyURI.toString());
	}

	public OWLOntologyManager getOWLOntologyManager() {
		return mmgr;
	}


}
