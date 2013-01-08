package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.Description;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class ABoxToFactConverter {

	private static OBDADataFactory factory = OBDADataFactoryImpl.getInstance();

	public static void addFacts(Iterator<Assertion> assetions, DatalogProgram p, Map<Predicate, Description> equivalences) {
		while (assetions.hasNext()) {
			Assertion next = assetions.next();
			addFact(next, p, equivalences);
		}
	}

	private static void addFact(Assertion assertion, DatalogProgram p, Map<Predicate, Description> equivalences) {
		CQIE fact = getRule(assertion);
		if (fact != null)
			p.appendRule(fact);
	}

	private static CQIE getRule(Assertion assertion) {
		Atom head = null;
		if (assertion instanceof ClassAssertion) {
			ClassAssertion ca = (ClassAssertion) assertion;
			ObjectConstant c = ca.getObject();
			Predicate p = ca.getConcept();
			Predicate urifuction = factory.getUriTemplatePredicate(1);
			head = factory.getAtom(p, factory.getFunctionalTerm(urifuction, c));
			// head = factory.getAtom(p, c);
		} else {
			return null;
		}
		/***
		 * The rest is not supported yet
		 */
		return factory.getCQIE(head, new LinkedList<Atom>());
	}
}
