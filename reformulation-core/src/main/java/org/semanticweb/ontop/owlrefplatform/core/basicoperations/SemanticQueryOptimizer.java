package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.impl.AnonymousVariable;
import org.semanticweb.ontop.ontology.ClassDescription;
import org.semanticweb.ontop.ontology.OClass;
import org.semanticweb.ontop.ontology.Property;
import org.semanticweb.ontop.ontology.PropertySomeRestriction;
import org.semanticweb.ontop.ontology.SubDescriptionAxiom;
import org.semanticweb.ontop.ontology.impl.PropertySomeRestrictionImpl;
import org.semanticweb.ontop.ontology.impl.SubClassAxiomImpl;
import org.semanticweb.ontop.ontology.impl.SubPropertyAxiomImpl;

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
		List<Function> body = copy.getBody();
		for (int i = 0; i < body.size(); i++) {
			int previoussize = body.size();
			
			Function focusAtom = (Function) body.get(i);

			/* redundancy of an atom without PIs */
			if (focusAtom.getPredicate().getArity() == 1) {
				/* removing anonymous atoms A(_) */
				if (!(focusAtom.getTerms().get(0) instanceof AnonymousVariable))
					continue;
				for (int j = 0; j < body.size(); j++) {
					if (j == i)
						continue;
					Function atom2 = (Function) body.get(j);

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

					Function atom2 = (Function) body.get(j);

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
				Function checkAtom = null;
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
							checkAtom = fac.getFunction(left.getPredicate(), focusAtom.getTerms());

						} else if (included instanceof PropertySomeRestriction) {
							/*
							 * Case left side of inclusion is exists R or exists
							 * R-
							 */
							/* Case left side of inclusion is B */
							PropertySomeRestriction left = (PropertySomeRestriction) included;
							if (!left.isInverse()) {
								List<Term> terms = new LinkedList<Term>(focusAtom.getTerms());
								terms.add(fac.getVariableNondistinguished());
								checkAtom = fac.getFunction(left.getPredicate(), terms);
							} else {
								List<Term> terms = new LinkedList<Term>(focusAtom.getTerms());
								terms.add(0, fac.getVariableNondistinguished());
								checkAtom = fac.getFunction(left.getPredicate(), terms);
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
								checkAtom = fac.getFunction(left.getPredicate(), focusAtom.getTerms().get(0));
							} else if (right.isInverse() && focusAtom.getTerms().get(0) instanceof AnonymousVariable) {
								checkAtom = fac.getFunction(left.getPredicate(), focusAtom.getTerms().get(1));
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
									checkAtom = fac.getFunction(left.getPredicate(), focusAtom.getTerms().get(0),
											fac.getVariableNondistinguished());
								} else {
									checkAtom = fac.getFunction(left.getPredicate(), fac.getVariableNondistinguished(), focusAtom.getTerms()
											.get(0));
								}
							} else if (right.isInverse() && focusAtom.getTerms().get(0) instanceof AnonymousVariable) {
								if (!left.isInverse()) {
									checkAtom = fac.getFunction(left.getPredicate(), focusAtom.getTerms().get(1),
											fac.getVariableNondistinguished());
								} else {
									checkAtom = fac.getFunction(left.getPredicate(), fac.getVariableNondistinguished(), focusAtom.getTerms()
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
						checkAtom = fac.getFunction(leftrole.getPredicate(), focusAtom.getTerms());
					} else {
						LinkedList<Term> terms = new LinkedList<Term>();
						terms.add(focusAtom.getTerms().get(1));
						terms.add(focusAtom.getTerms().get(0));
						checkAtom = fac.getFunction(leftrole.getPredicate(), terms);
					}

				}

				/*
				 * now we search for the new atom, if we find it, the current
				 * atom is redundant
				 */
				for (int j = 0; j < body.size(); j++) {
					if (j == i)
						continue;
					Function atom2 = (Function) body.get(j);

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
