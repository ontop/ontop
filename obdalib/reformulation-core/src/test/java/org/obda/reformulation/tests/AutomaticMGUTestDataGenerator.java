package org.obda.reformulation.tests;

import inf.unibz.it.obda.model.Atom;
import inf.unibz.it.obda.model.OBDADataFactory;
import inf.unibz.it.obda.model.Predicate;
import inf.unibz.it.obda.model.Term;
import inf.unibz.it.obda.model.impl.AtomImpl;
import inf.unibz.it.obda.model.impl.OBDADataFactoryImpl;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.obda.owlrefplatform.core.basicoperations.Substitution;

/***
 * This is an auxiliary class for the MGU generation test. This class is in
 * charge of taking a test case string, e.g.,
 *
 * A(x) | A(f(y,z)) = {x/f(y,z)}
 *
 * And producing the objects that are needed to execute the query, i.e., atoms,
 * terms, expected mgu list, etc.
 *
 * @author Mariano Rodriguez Muro
 *
 */
public class AutomaticMGUTestDataGenerator {

	OBDADataFactory	predFac	= OBDADataFactoryImpl.getInstance();
	OBDADataFactory					termFac	= OBDADataFactoryImpl.getInstance();

	/***
	 * Checks if all substitutions in unifier1 are also in unifier2.
	 *
	 * @param unifier1
	 * @param unifier2
	 * @return
	 */
	public boolean compareUnifiers(List<Substitution> unifier1, List<Substitution> unifier2) {
		if (unifier1.size() != unifier2.size())
			return false;

		for (int i = 0; i < unifier1.size(); i++) {
			boolean result = false;
			for (int j = 0; j < unifier2.size(); j++) {
				result = result || compareSubstitutions(unifier1.get(i), unifier2.get(j));
			}
			if (!result) {
				/*
				 * we couldn't find a substitution in unifier2 that matched the
				 * current substitution in unifier1
				 */
				return false;
			}
		}
		/*
		 * We found a match for every substitution in unifier1
		 */
		return true;

	}

	/***
	 * Checks if two substitutions are equal (syntactic)
	 *
	 * @param s1
	 * @param s2
	 * @return
	 */
	public boolean compareSubstitutions(Substitution s1, Substitution s2) {
		boolean equalVars = s1.getVariable().toString().equals(s2.getVariable().toString());
		boolean equalTerms = s1.getTerm().toString().equals(s2.getTerm().toString());

		return equalVars && equalTerms;
	}

	/***
	 * Gets list of substitutions encoded in the string mgustr. mgustr is
	 * normally used to encode the expected MGU for a test.
	 *
	 * @param mgustr
	 * @return
	 */
	public List<Substitution> getMGU(String mgustr) {
		if (mgustr.trim().equals("NULL"))
			return null;

		mgustr = mgustr.substring(1, mgustr.length() - 1);
		String[] mguStrings = mgustr.split(" ");

		List<Substitution> mgu = new ArrayList<Substitution>();
		for (int i = 0; i < mguStrings.length; i++) {
			String string = mguStrings[i];
			if (string.equals(""))
				continue;
			String[] elements = string.split("/");
			Substitution s = new Substitution(getTerm(elements[0]), getTerm(elements[1]));
			mgu.add(s);
		}
		return mgu;
	}

	/***
	 * Gets the list of size 2, of the two atoms in the string atomstr. Only
	 * supports 2 atoms!.
	 *
	 * @param atomstrs
	 * @return
	 */
	public List<Atom> getAtoms(String atomstrs) {
		atomstrs = atomstrs.trim();
		List<Atom> atoms = new ArrayList<Atom>();
		String[] atomstr = atomstrs.split("\\|");
		String str1 = atomstr[0].trim();
		Atom atom1 = getAtom(str1);
		atoms.add(atom1);
		String str2 = atomstr[1].trim();
		Atom atom2 = getAtom(str2);
		atoms.add(atom2);
		return atoms;
	}

	public Atom getAtom(String atomstr) {
		String termstr = atomstr.substring(2, atomstr.length() - 1);
		List<Term> terms = new ArrayList<Term>();

		String[] termstra = termstr.split(" ");
		for (int i = 0; i < termstra.length; i++) {
			terms.add(getTerm(termstra[i].trim()));
		}
		Atom atom = new AtomImpl(predFac.createPredicate(URI.create(atomstr.substring(0, 1)), terms.size()), terms);
		return atom;
	}

	public Term getTerm(String termstrs) {
		// List<Term> terms = new ArrayList<Term>();
		// String[] termstra = termstrs.split(",");
		// for (int i = 0; i < termstra.length; i++) {

		String termstr = termstrs.trim();

		if (termstr.indexOf('(') != -1) {
			String[] subtermstr = termstr.substring(2, termstrs.length() - 1).split(",");
			List<Term> fuctTerms = new ArrayList<Term>();
			for (int i = 0; i < subtermstr.length; i++) {
				fuctTerms.add(getTerm(subtermstr[i]));
			}
			Predicate fs = predFac.createPredicate(URI.create(termstr.substring(0, 1)), fuctTerms.size());
			return termFac.createFunctionalTerm(fs, fuctTerms);
		} else if (termstr.charAt(0) == '"') {
			return termFac.createValueConstant(termstr.substring(1, termstr.length() - 1));
		} else if (termstr.charAt(0) == '<') {
			return termFac.createURIConstant(URI.create(termstr.substring(1, termstr.length() - 1)));
		} else if (termstr.equals("#")) {
			return termFac.createUndistinguishedVariable();
		} else {
			return termFac.createVariable(termstr);
			/* variable */
		}
		// }

	}
}
