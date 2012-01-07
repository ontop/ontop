package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.AnonymousVariable;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.SubDescriptionAxiom;
import it.unibz.krdb.obda.ontology.impl.PropertySomeRestrictionImpl;
import it.unibz.krdb.obda.ontology.impl.SubClassAxiomImpl;
import it.unibz.krdb.obda.ontology.impl.SubPropertyAxiomImpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SemanticQueryOptimizer {
	
	 Map<Predicate,Set<SubDescriptionAxiom>> axiomIndex;
	 OBDADataFactory fac;
	
	public SemanticQueryOptimizer(OBDADataFactory fac, Map<Predicate,Set<SubDescriptionAxiom>> includedPredicateIndex) {
		this.fac = fac;
		this.axiomIndex = includedPredicateIndex;
	}
	
	public Collection<CQIE> optimizeBySQO(Collection<CQIE> queries) {
		HashSet<CQIE> newqueries = new HashSet<CQIE>();
		for (CQIE query : queries) {
			CQIE optimized = optimizeBySQO(query);
			if (!newqueries.contains(optimized)) {
				newqueries.add(optimized);
			}
		}
		return newqueries;
	}

	// TODO check that this optmization is complete
	/***
	 * An atom is redundant if there is a PI and an atom B such that applying
	 * the PI to B, we get A.
	 * 
	 * 
	 * 
	 * An atom A(x) is redundant if there is another atom B(x) such that B ISA
	 * A. An atom A(_) is redundant if there is another atom B(_) such that B
	 * ISA A. An atom A(_) is redundant if there is another atom B(x) such that
	 * B ISA A. An atom A(x) is redundant if there is another atom R(x,y) such
	 * that ER ISA A. An atom A(_) is redundant if there is another atom R(x,y)
	 * such that ER ISA A. An atom A(_) is redundant if there is another atom
	 * R(_,y) such that ER ISA A.
	 * 
	 * @param query
	 * @return
	 */
	public CQIE optimizeBySQO(CQIE query) {
		QueryAnonymizer anonymizer = new QueryAnonymizer();
		CQIE copy = anonymizer.anonymize(query.clone());
		List<Atom> body = copy.getBody();
		for (int i = 0; i < body.size(); i++) {
			int previoussize = body.size();
			
			Atom focusAtom = (Atom) body.get(i);

			/* redundancy of an atom without PIs */
			if (focusAtom.getPredicate().getArity() == 1) {
				/* removing anonymous atoms A(_) */
				if (!(focusAtom.getTerms().get(0) instanceof AnonymousVariable))
					continue;
				for (int j = 0; j < body.size(); j++) {
					if (j == i)
						continue;
					Atom atom2 = (Atom) body.get(j);

					if (atom2.getPredicate().equals(focusAtom.getPredicate())) {
						body.remove(i);
						i -= 1;
						break;
					}

				}
			} else {
				/* removing anonymous atoms R(_,x) and R(x,_) or R(_,_) */

				if (!(focusAtom.getTerms().get(0) instanceof AnonymousVariable || focusAtom.getTerms().get(1) instanceof AnonymousVariable))
					continue;

				Term t1 = focusAtom.getTerms().get(0);
				Term t2 = focusAtom.getTerms().get(1);

				for (int j = 0; j < body.size(); j++) {
					if (j == i)
						continue;

					Atom atom2 = (Atom) body.get(j);

					/* case R(_,_) */

					if (t1 instanceof AnonymousVariable && t2 instanceof AnonymousVariable
							&& atom2.getPredicate().equals(focusAtom.getPredicate())) {
						body.remove(i);
						i -= 1;
						break;
					}

					/* case R(x,_) */
					if (t2 instanceof AnonymousVariable && atom2.getPredicate().equals(focusAtom.getPredicate())
							&& atom2.getTerms().get(0).equals(t1)) {
						body.remove(i);
						i -= 1;
						break;
					}

					/* case R(_,x) */
					if (t1 instanceof AnonymousVariable && atom2.getPredicate().equals(focusAtom.getPredicate())
							&& atom2.getTerms().get(1).equals(t2)) {
						body.remove(i);
						i -= 1;
						break;
					}

				}
			}
			if (previoussize != body.size()) {
				copy = anonymizer.anonymize(copy);
				body = copy.getBody();
				i = 0;
				continue;
			}

			Set<SubDescriptionAxiom> pis = axiomIndex.get(focusAtom.getPredicate());
			if (pis == null) {
				continue;
			}
			for (SubDescriptionAxiom pi : pis) {
				boolean breakPICycle = false;
				Atom checkAtom = null;
				if (pi instanceof SubClassAxiomImpl) {
					SubClassAxiomImpl ci = (SubClassAxiomImpl) pi;
					ClassDescription including = ci.getSuper();
					ClassDescription included = ci.getSub();

					if (focusAtom.getPredicate().getArity() == 1 && including instanceof OClass) {
						/* case we work with unary atom A(x) or A(_) */
						// Here the list of terms is always of size 1

						if (included instanceof OClass) {
							/* Case left side of inclusion is B */
							OClass left = (OClass) included;
							checkAtom = fac.getAtom(left.getPredicate(), focusAtom.getTerms());

						} else if (included instanceof PropertySomeRestriction) {
							/*
							 * Case left side of inclusion is exists R or exists
							 * R-
							 */
							/* Case left side of inclusion is B */
							PropertySomeRestriction left = (PropertySomeRestriction) included;
							if (!left.isInverse()) {
								List<Term> terms = new LinkedList<Term>(focusAtom.getTerms());
								terms.add(fac.getNondistinguishedVariable());
								checkAtom = fac.getAtom(left.getPredicate(), terms);
							} else {
								List<Term> terms = new LinkedList<Term>(focusAtom.getTerms());
								terms.add(0, fac.getNondistinguishedVariable());
								checkAtom = fac.getAtom(left.getPredicate(), terms);
							}
						} else {
							continue;
						}

					} else if (focusAtom.getPredicate().getArity() == 2 && including instanceof ClassDescription) {
						/*
						 * case we work with unary atom R(x,_), R(_,y)
						 */

						if (including instanceof PropertySomeRestriction && included instanceof OClass) {
							/*
							 * Case with left side of inclusion is A
							 * 
							 * R(x,_) is redundant if we have A ISA ER and A(x)
							 * is in the body
							 */
							PropertySomeRestriction right = (PropertySomeRestriction) including;
							OClass left = (OClass) included;
							if (!right.isInverse() && focusAtom.getTerms().get(1) instanceof AnonymousVariable) {
								checkAtom = fac.getAtom(left.getPredicate(), focusAtom.getTerms().get(0));
							} else if (right.isInverse() && focusAtom.getTerms().get(0) instanceof AnonymousVariable) {
								checkAtom = fac.getAtom(left.getPredicate(), focusAtom.getTerms().get(1));
							} else {
								continue;
							}

						} else if (including instanceof PropertySomeRestriction
								&& included instanceof PropertySomeRestrictionImpl) {

							/*
							 * Case with left side of inclusion is exists R or
							 * exists R-
							 * 
							 * Cases R(x,_) with ES ISA ER we get S(x,_) Cases
							 * R(x,_) with ES- ISA ER we get S(_,x) Cases R(_,x)
							 * with ES ISA ER- we get S(x,_) Cases R(_,x) with
							 * ES ISA ER- we get S(x,_)
							 */

							PropertySomeRestriction right = (PropertySomeRestriction) including;
							PropertySomeRestriction left = (PropertySomeRestriction) included;
							if (!right.isInverse() && focusAtom.getTerms().get(1) instanceof AnonymousVariable) {
								if (!left.isInverse()) {
									checkAtom = fac.getAtom(left.getPredicate(), focusAtom.getTerms().get(0),
											fac.getNondistinguishedVariable());
								} else {
									checkAtom = fac.getAtom(left.getPredicate(), fac.getNondistinguishedVariable(), focusAtom.getTerms()
											.get(0));
								}
							} else if (right.isInverse() && focusAtom.getTerms().get(0) instanceof AnonymousVariable) {
								if (!left.isInverse()) {
									checkAtom = fac.getAtom(left.getPredicate(), focusAtom.getTerms().get(1),
											fac.getNondistinguishedVariable());
								} else {
									checkAtom = fac.getAtom(left.getPredicate(), fac.getNondistinguishedVariable(), focusAtom.getTerms()
											.get(1));
								}
							} else {
								continue;
							}

						} else {
							continue;
						}

					} else {
						// Arity more that 2
						continue;
					}
				} else if (pi instanceof SubPropertyAxiomImpl) {

					/*
					 * Case with left side of inclusion is S or S- and only for
					 * atom R(x,y) (no nondistinguished) can we do it for atoms
					 * R(x,_) check proof
					 * 
					 * TODO check if we can also apply role inclusions to atoms
					 * with anonymous variables
					 */

					SubPropertyAxiomImpl roleinclusion = (SubPropertyAxiomImpl) pi;
					Property rightrole = (Property) roleinclusion.getSuper();
					Property leftrole = (Property) roleinclusion.getSub();

					if (rightrole.isInverse() == leftrole.isInverse()) {
						checkAtom = fac.getAtom(leftrole.getPredicate(), focusAtom.getTerms());
					} else {
						LinkedList<Term> terms = new LinkedList<Term>();
						terms.add(focusAtom.getTerms().get(1));
						terms.add(focusAtom.getTerms().get(0));
						checkAtom = fac.getAtom(leftrole.getPredicate(), terms);
					}

				}

				/*
				 * now we search for the new atom, if we find it, the current
				 * atom is redundant
				 */
				for (int j = 0; j < body.size(); j++) {
					if (j == i)
						continue;
					Atom atom2 = (Atom) body.get(j);

					if (atom2.equals(checkAtom)) {
						body.remove(i);
						i -= 1;
						break;
					}

				}
				if (previoussize != body.size()) {
					copy = anonymizer.anonymize(copy);
					body = copy.getBody();
					i = 0;
					continue;
				}
			}

		}

		return copy;
	}


}
