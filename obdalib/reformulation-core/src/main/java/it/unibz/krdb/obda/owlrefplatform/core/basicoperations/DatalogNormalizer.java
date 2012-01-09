package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DatalogNormalizer {

	private final static OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	/***
	 * Normalizes all the rules in a Datalog program, pushing equalities into
	 * the atoms of the queries, when possible
	 * 
	 * @param dp
	 */
	public static DatalogProgram normalizeDatalogProgram(DatalogProgram dp) {
		DatalogProgram clone = fac.getDatalogProgram();
		for (CQIE cq : dp.getRules()) {
			CQIE normalized = normalizeCQIE(cq);
			if (normalized != null) {
				clone.appendRule(normalized);
			}
		}
		return clone;
	}

	public static CQIE normalizeCQIE(CQIE query) {
		CQIE result = normalizeANDTrees(query);
		result = normalizeEQ(result);
		if (result == null)
			return null;
		return result;
	}

	/***
	 * This expands all AND trees into individual comparison atoms
	 * 
	 * @param query
	 * @return
	 */
	private static CQIE normalizeANDTrees(CQIE query) {
		CQIE result = query.clone();
		List<Atom> body = result.getBody();
		/* Collecting all necessary conditions */
		for (int i = 0; i < body.size(); i++) {
			Atom currentAtom = body.get(i);
			if (currentAtom.getPredicate() == OBDAVocabulary.AND) {
				body.remove(i);
				body.addAll(getUnfolderAtomList(currentAtom));
			}
		}
		return result;
	}

	/***
	 * Eliminates all equalities in the query by applying substitions to the
	 * database predicates.
	 * 
	 * @param query
	 *            null if there is an unsatisfiable equality
	 * @return
	 */
	private static CQIE normalizeEQ(CQIE query) {
		CQIE result = query.clone();
		List<Atom> body = result.getBody();
		Map<Variable, Term> mgu = new HashMap<Variable, Term>();

		for (int i = 0; i < body.size(); i++) {
			Atom atom = body.get(i);
			Unifier.applyUnifier(atom, mgu);
			if (atom.getPredicate() == OBDAVocabulary.EQ) {
				Substitution s = Unifier.getSubstitution(atom.getTerm(0), atom.getTerm(1));
				if (s == null) {
					return null;
				}

				if (!(s instanceof NeutralSubstitution)) {
					Unifier.composeUnifiers(mgu, s);
				}
				body.remove(i);
				i -= 1;
				continue;
			}
		}
		result = Unifier.applyUnifier(result, mgu);
		return result;
	}

	/***
	 * Takes an AND atom and breaks it into a list of individual condition
	 * atoms.
	 * 
	 * @param atom
	 * @return
	 */
	public static List<Atom> getUnfolderAtomList(Atom atom) {
		if (atom.getPredicate() != OBDAVocabulary.AND)
			throw new InvalidParameterException();
		List<Term> innerFunctionalTerms = new LinkedList<Term>();
		for (Term term : atom.getTerms()) {
			innerFunctionalTerms.addAll(getUnfolderTermList((Function) term));
		}
		List<Atom> newatoms = new LinkedList<Atom>();
		for (Term innerterm : innerFunctionalTerms) {
			Function f = (Function) innerterm;
			Atom newatom = fac.getAtom(f.getFunctionSymbol(), f.getTerms());
			newatoms.add(newatom);
		}
		return newatoms;
	}

	/***
	 * Takes an AND atom and breaks it into a list of individual condition
	 * atoms.
	 * 
	 * @param atom
	 * @return
	 */
	public static List<Term> getUnfolderTermList(Function term) {

		List<Term> result = new LinkedList<Term>();

		if (term.getFunctionSymbol() != OBDAVocabulary.AND)
			result.add(term);
		else {
			List<Term> terms = term.getTerms();
			for (Term currentterm : terms) {
				if (currentterm instanceof Function) {
					result.addAll(getUnfolderTermList((Function) currentterm));
				} else {
					result.add(currentterm);
				}
			}
		}

		return result;
	}
}
