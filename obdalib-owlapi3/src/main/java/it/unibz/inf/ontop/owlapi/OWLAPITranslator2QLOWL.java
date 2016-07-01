package it.unibz.inf.ontop.owlapi;

import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNode;
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNodeSet;

import it.unibz.inf.ontop.ontology.ClassExpression;
import it.unibz.inf.ontop.ontology.OClass;
import it.unibz.inf.ontop.ontology.ObjectPropertyExpression;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * Translate Ontop objects to OWL API objects.
 * 
 * It includes:
 * <ul>
 * <li> simple translation between objects {@link #translate(OClass)}
 * <li> translation between Set of Ontop objects and specific OWL API collections
 * (Example : {@link #translateToNode(Set)} or {@link #translateClassToNodeSet(Set)}) 
 * 
 * @author Ana Oliveira da Costa
 */
public class OWLAPITranslator2QLOWL {

	private final OWLDataFactory dataFactory = new OWLDataFactoryImpl();

	/*
	 * CLASS
	 */
	public OWLClass translate(OClass ca) {
		IRI conceptIRI = IRI.create(ca.getName());
		return dataFactory.getOWLClass(conceptIRI);
	} 
	
	public NodeSet<OWLClass> translateClassToNodeSet(Set<ClassExpression> itOclasses) {
		OWLClassNodeSet listOWLClasses = new OWLClassNodeSet();

		for(ClassExpression classExp : itOclasses) {
			if(classExp instanceof OClass) {
				//TODO: OClass has the implementation!
				OClass oclass = (OClass) classExp;
				listOWLClasses.addEntity(translate(oclass));
			} else {
				//TODO: What should I do here?
			}
		}
		return listOWLClasses;
	}
	
	public Node<OWLClass> translateToNode(Set<ClassExpression> itOclasses) {
		OWLClassNode nodeOWLClasses = new OWLClassNode();

		for(ClassExpression classExp : itOclasses) {
			if(classExp instanceof OClass) {
				OClass oclass = (OClass) classExp;
				nodeOWLClasses.add(translate(oclass));
			} else {
				//TODO: What should I do here?
			}
		}
		return nodeOWLClasses;
	}
	
	/*
	 * OBJECT PROPERTY
	 */
	public OWLObjectProperty translate(ObjectPropertyExpression op) {
		IRI roleIRI = IRI.create(op.getPredicate().getName());
		return dataFactory.getOWLObjectProperty(roleIRI);
	}
	
	
	public NodeSet<OWLObjectPropertyExpression> translateObjPropertyToNodeSet(
			Set<ObjectPropertyExpression> itOPclasses) {
		
		OWLObjectPropertyNodeSet listOWLClasses = new OWLObjectPropertyNodeSet();

		for(ObjectPropertyExpression obProperty : itOPclasses) {
			listOWLClasses.addEntity(translate(obProperty));
		}
		return listOWLClasses;
	}
	
	public Node<OWLObjectPropertyExpression> translateObjPropertyToNode(
			Set<ObjectPropertyExpression> itOPclasses) {
		
		OWLObjectPropertyNode nodeObjProperty = new OWLObjectPropertyNode();

		for(ObjectPropertyExpression obProperty : itOPclasses) {
			nodeObjProperty.add(translate(obProperty));
		}
		return nodeObjProperty;
	}
	
	
	
}