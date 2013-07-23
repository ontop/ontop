package it.unibz.krdb.obda.owlapi3;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

public class OBDAModelSynchronizer {

	/***
	 * This method will declare all classes and proeprties in the owl ontology
	 * into the OBDA model. This is required
	 * 
	 * @param ontology
	 * @param model
	 * 
	 * @return the total number of declared entities.
	 */
	public static int declarePredicates(OWLOntology ontology, OBDAModel model) {
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

		int declarations = 0;
		for (OWLClass c : ontology.getClassesInSignature()) {
			model.declareClass(fac.getClassPredicate(c.getIRI().toString()));
			declarations += 1;
		}
		for (OWLDataProperty c : ontology.getDataPropertiesInSignature()) {
			model.declareDataProperty(fac.getDataPropertyPredicate(c.getIRI().toString()));
			declarations += 1;
		}
		for (OWLObjectProperty c : ontology.getObjectPropertiesInSignature()) {
			model.declareObjectProperty(fac.getObjectPropertyPredicate(c.getIRI().toString()));
			declarations += 1;
		}

		return declarations;
	}
}
