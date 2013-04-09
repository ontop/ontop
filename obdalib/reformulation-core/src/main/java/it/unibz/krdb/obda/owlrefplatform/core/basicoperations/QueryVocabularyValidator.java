package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import it.unibz.krdb.obda.model.Function;
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
	private Vector<String> invalidPredicates = new Vector<String>();

	Logger log = LoggerFactory.getLogger(QueryVocabularyValidator.class);

	private Ontology ontology;

	private Map<Predicate, Description> equivalences;

	private static OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	public QueryVocabularyValidator(Ontology ontology, Map<Predicate, Description> equivalences) {
		this.ontology = ontology;
		this.equivalences = equivalences;
	}

	public boolean validatePredicates(DatalogProgram input) {
		// Reset the invalid list
		invalidPredicates.clear();

		List<CQIE> rules = input.getRules();
		for (CQIE query : rules) {
			validate(query);
		}

		boolean isValid = true;
		if (!invalidPredicates.isEmpty()) {
			isValid = false; // if the list is not empty means the string is invalid!
		}
		return isValid;
	}

	private void validate(CQIE query) {
		// Get the predicates in the target query.
		Iterator<Function> iterAtom = query.getBody().iterator();
		while (iterAtom.hasNext()) {
			Function a1 = iterAtom.next();
			if (!(a1 instanceof Function)) {
				continue;
			}
			Function atom = (Function) a1;

			Predicate predicate = atom.getPredicate();

			boolean isClass = false;
			boolean isObjectProp = false;
			boolean isDataProp = false;
			boolean isBooleanOpFunction = false;

			isClass = isClass || ontology.getConcepts().contains(predicate)
					|| (equivalences.get(predicate) != null);
			isObjectProp = isObjectProp
					|| ontology.getRoles().contains(predicate)
					|| (equivalences.get(predicate) != null);
			isDataProp = isDataProp || ontology.getRoles().contains(predicate)
					|| (equivalences.get(predicate) != null);
			isBooleanOpFunction = (predicate instanceof BooleanOperationPredicate);

			// Check if the predicate contains in the ontology vocabulary as one
			// of these components (i.e., class, object property, data
			// property).
			boolean isPredicateValid = isClass || isObjectProp || isDataProp
					|| isBooleanOpFunction;

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
	 */
	public DatalogProgram replaceEquivalences(DatalogProgram queries) {
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		DatalogProgram newprogram = fac.getDatalogProgram();
		newprogram.setQueryModifiers(queries.getQueryModifiers());
		for (CQIE query : queries.getRules()) {
			newprogram.appendRule(replaceEquivalences(query.clone(), true));
		}
		return newprogram;
	}

	public CQIE replaceEquivalences(CQIE query, boolean inplace) {
		if (!inplace) {
			query = query.clone();
		}
		replaceEquivalences(query.getBody());
		return query;
	}

	public void replaceEquivalences(List body) {
		// Get the predicates in the target query.
		for (int i = 0; i < body.size(); i++) {
			Function atom = (Function) body.get(i);

			/*
			 * Calling recursively for nested expressions
			 */
			if (atom.isAlgebraFunction()) {
				replaceEquivalences(atom.getTerms());
				continue;
			}
			
			if (atom.isBooleanFunction())
				continue;

			Description equivalent = equivalences.get(atom.getFunctionSymbol());
			if (equivalent == null) {
				/* Nothing to replace */
				continue;
			}
			Function newatom = null;

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
			body.set(i, newatom);
		}
	}

	public Vector<String> getInvalidPredicates() {
		return invalidPredicates;
	}
}
