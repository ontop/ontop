package it.unibz.krdb.obda.owlapi3;

import it.unibz.krdb.obda.gui.swing.treemodel.TargetQueryVocabularyValidator;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TargetQueryValidator implements TargetQueryVocabularyValidator {
	
	/** The OBDA model for validating the target query */
	private OBDAModel obdaModel;

	/** Data factory **/
	private OBDADataFactory dataFactory = OBDADataFactoryImpl.getInstance();

	/** List of invalid predicates */
	private Vector<String> invalidPredicates = new Vector<String>();

	/** Logger */
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public TargetQueryValidator(OBDAModel obdaModel) {
		this.obdaModel = obdaModel;
	}
	
	@Override
	public boolean validate(CQIE targetQuery) {
		// Reset the invalid list
		invalidPredicates.clear();

		// Get the predicates in the target query.
		for (Atom atom : targetQuery.getBody()) {
			Predicate p = atom.getPredicate();

			boolean isClass = isClass(p);
			boolean isObjectProp = isObjectProperty(p);
			boolean isDataProp = isDataProperty(p);

			// Check if the predicate contains in the ontology vocabulary as one
			// of these components (i.e., class, object property, data property).
			boolean isPredicateValid = isClass || isObjectProp || isDataProp;

			String debugMsg = "The predicate: [" + p.getName().toString() + "]";
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
				Predicate predicate = dataFactory.getPredicate(p.getName(), atom.getArity(), colType);
				atom.setPredicate(predicate); // TODO Fix the API!
				log.debug(debugMsg);
			} else {
				invalidPredicates.add(p.getName().toString());
//				log.warn("WARNING: " + debugMsg + " is missing in the ontology!");
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
	public boolean isClass(Predicate predicate) {
		return obdaModel.isDeclaredClass(predicate);
	}
	
	@Override
	public boolean isObjectProperty(Predicate predicate) {
		return obdaModel.isDeclaredObjectProperty(predicate);
	}

	@Override
	public boolean isDataProperty(Predicate predicate) {
		return obdaModel.isDeclaredDataProperty(predicate);
	}
}
