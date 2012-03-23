package it.unibz.krdb.obda.owlapi3;

import it.unibz.krdb.obda.gui.swing.treemodel.TargetQueryVocabularyValidator;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.net.URI;
import java.util.Vector;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TargetQueryValidator implements TargetQueryVocabularyValidator {
	
	/** The source ontology for validating the target query */
	private OWLOntology ontology;

	/** Data factory **/
	private OBDADataFactory dataFactory = OBDADataFactoryImpl.getInstance();

	/** List of invalid predicates */
	private Vector<String> invalidPredicates = new Vector<String>();

	/** Logger */
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public TargetQueryValidator(OWLOntology ontology) {
		this.ontology = ontology;
	}
	
	@Override
	public boolean validate(CQIE targetQuery) {
		// Reset the invalid list
		invalidPredicates.clear();

		// Get the predicates in the target query.
		for (Atom atom : targetQuery.getBody()) {
			URI predicateUri = atom.getPredicate().getName();

			boolean isClass = isClass(predicateUri);
			boolean isObjectProp = isObjectProperty(predicateUri);
			boolean isDataProp = isDataProperty(predicateUri);

			// Check if the predicate contains in the ontology vocabulary as one
			// of these components (i.e., class, object property, data property).
			boolean isPredicateValid = isClass || isObjectProp || isDataProp;

			String debugMsg = "The predicate: [" + predicateUri.toString() + "]";
			if (isPredicateValid) {
				COL_TYPE colType[] = null;
				if (isClass) {
					colType = new COL_TYPE[] { COL_TYPE.OBJECT };
					debugMsg += " is a Class.";
				} else if (isObjectProp) {
					colType = new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT };
					debugMsg += " is an Object property.";
				} else if (isDataProp) {
					colType = new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL };
					debugMsg += " is a Data property.";
				}
				Predicate predicate = dataFactory.getPredicate(predicateUri, atom.getArity(), colType);
				atom.setPredicate(predicate); // TODO Fix the API!
				log.debug(debugMsg);
			} else {
				invalidPredicates.add(predicateUri.toString());
				log.warn("WARNING: " + debugMsg + " is missing in the ontology!");
			}
		}
		boolean isValid = true;
		if (!invalidPredicates.isEmpty()) {
			isValid = false; // if the list is not empty means the string is invalid!
		}
		return isValid;
	}

	@Override
	public Vector<String> getInvalidPredicates() {
		return invalidPredicates;
	}

	@Override
	public boolean isClass(URI predicate) {
		return ontology.containsClassInSignature(IRI.create(predicate));
	}
	
	@Override
	public boolean isObjectProperty(URI predicate) {
		return ontology.containsObjectPropertyInSignature(IRI.create(predicate));
	}

	@Override
	public boolean isDataProperty(URI predicate) {
		return ontology.containsDataPropertyInSignature(IRI.create(predicate));
	}
}
