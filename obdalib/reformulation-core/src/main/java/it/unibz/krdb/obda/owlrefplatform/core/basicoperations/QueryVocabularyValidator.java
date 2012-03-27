package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.BooleanOperationPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.Property;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryVocabularyValidator implements Serializable {
	/** The source ontology for validating the target query */

	/**
	 * 
	 */
	private static final long serialVersionUID = -2901421485090507301L;

	/** List of invalid predicates */
	private Vector<String>				invalidPredicates	= new Vector<String>();

	Logger								log					= LoggerFactory.getLogger(QueryVocabularyValidator.class);

	private Ontology					ontology;

	private Map<Predicate, Description>	equivalences;

	public QueryVocabularyValidator(Ontology ontology, Map<Predicate, Description> equivalences) {
		this.ontology = ontology;
		this.equivalences = equivalences;
	}

	public boolean validate(DatalogProgram input) {
		// Reset the invalid list
		invalidPredicates.clear();

		List<CQIE> rules = input.getRules();
		for (CQIE query : rules) {
			validate(query);
		}

		boolean isValid = true;
		if (!invalidPredicates.isEmpty()) {
			isValid = false; // if the list is not empty means the string is
								// invalid!
		}
		return isValid;
	}

	private boolean isEmptyOntology() {
		return (ontology.getAssertions().size() == 0) ? true : false;
	}
	
	private void validate(CQIE query) {
		if (isEmptyOntology()) {
			// Skip if the input ontology is empty.
			return;
		}
		
		// Get the predicates in the target query.
		Iterator<Atom> iterAtom = query.getBody().iterator();
		while (iterAtom.hasNext()) {
			Atom a1 = iterAtom.next();
			if (!(a1 instanceof Atom)) {
				continue;
			}
			Atom atom = (Atom) a1;

			Predicate predicate = atom.getPredicate();

			boolean isClass = false;
			boolean isObjectProp = false;
			boolean isDataProp = false;
			boolean isBooleanOpFunction = false;
			
			isClass = isClass || ontology.getConcepts().contains(predicate) || (equivalences.get(predicate) != null);
			isObjectProp = isObjectProp || ontology.getRoles().contains(predicate) || (equivalences.get(predicate) != null);
			isDataProp = isDataProp || ontology.getRoles().contains(predicate) || (equivalences.get(predicate) != null);
			isBooleanOpFunction = (predicate instanceof BooleanOperationPredicate);
					
			// Check if the predicate contains in the ontology vocabulary as one
			// of these components (i.e., class, object property, data
			// property).
			boolean isPredicateValid = isClass || isObjectProp || isDataProp || isBooleanOpFunction;

			String debugMsg = "The predicate: [" + predicate.toString() + "]";
			if (isPredicateValid) {
				if (isClass) {
					debugMsg += " is a Class.";
				} else if (isObjectProp) {
					debugMsg += " is an Object property.";
				} else if (isDataProp) {
					debugMsg += " is a Data property.";
				} else if (isBooleanOpFunction) {
					debugMsg += " is a Boolean operation function.";
				}
				log.debug(debugMsg);
			} else {
				invalidPredicates.add(predicate.toString());
				log.warn("WARNING: " + debugMsg + " is missing in the ontology!");
			}
		}
	}

	/***
	 * Substite atoms based on the equivalence map.
	 * 
	 * @param queries
	 * @return
	 */
	public DatalogProgram replaceEquivalences(DatalogProgram queries) {
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		DatalogProgram newprogram = fac.getDatalogProgram();
		newprogram.setQueryModifiers(queries.getQueryModifiers());
		for (CQIE query : queries.getRules()) {
			newprogram.appendRule(replaceEquivalences(query));
		}
		return newprogram;
	}

	public CQIE replaceEquivalences(CQIE query) {
		Atom newhead = query.getHead();
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		List<Atom> newbody = new LinkedList<Atom>();

		// Get the predicates in the target query.

		for (int i = 0; i < query.getBody().size(); i++) {
			Atom atom = query.getBody().get(i);

			Description equivalent = equivalences.get(atom.getPredicate());

			if (equivalent == null)
				newbody.add(atom);
			else {
				Atom newatom = null;
				if (equivalent instanceof OClass) {
					newatom = fac.getAtom(((OClass) equivalent).getPredicate(), atom.getTerm(0));
				} else if (equivalent instanceof Property) {
					Property equiproperty = (Property) equivalent;
					if (!equiproperty.isInverse()) {
						newatom = fac.getAtom(equiproperty.getPredicate(), atom.getTerm(0), atom.getTerm(1));
					} else {
						newatom = fac.getAtom(equiproperty.getPredicate(), atom.getTerm(1), atom.getTerm(0));
					}
				}
				newbody.add(newatom);
			}
		}
		CQIE newquery = fac.getCQIE(newhead, newbody);
		return newquery;
	}

	public Vector<String> getInvalidPredicates() {
		return invalidPredicates;
	}
}
