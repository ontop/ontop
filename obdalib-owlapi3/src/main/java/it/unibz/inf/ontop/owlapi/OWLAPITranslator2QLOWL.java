package it.unibz.inf.ontop.owlapi;

import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;

import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;

import it.unibz.inf.ontop.ontology.ClassExpression;
import it.unibz.inf.ontop.ontology.OClass;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * Translate Ontop objects to OWL API objects.
 * 
 * @author Ana Oliveira da Costa
 */
public class OWLAPITranslator2QLOWL {

	private final OWLDataFactory dataFactory = new OWLDataFactoryImpl();

	public OWLClass translate(OClass ca) {
		IRI conceptIRI = IRI.create(ca.getName());
		return dataFactory.getOWLClass(conceptIRI);
	} 
	
	public NodeSet<OWLClass> translate(Set<ClassExpression> itOclasses) {
		OWLClassNodeSet listOWLClasses = new OWLClassNodeSet();

		//Translate all the Ontop objects to OWLClassExpression objects 
		OWLAPITranslator2QLOWL twoQLTranslator = new OWLAPITranslator2QLOWL();
		for(ClassExpression classExp : itOclasses) {
			if(classExp instanceof OClass) {
				//TODO: OClass has the implementation!
				OClass oclass = (OClass) classExp;
				listOWLClasses.addEntity(twoQLTranslator.translate(oclass));
			} else {
				//TODO: What should I do here?
			}
		}
		return listOWLClasses;
	}
	
	
}