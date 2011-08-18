package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.PredicateAtom;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.UndistinguishedVariable;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.AtomicConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ExistentialConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PositiveInclusion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.AtomicConceptDescriptionImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.AtomicRoleDescriptionImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterConceptInclusionImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterRoleInclusionImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.ExistentialConceptDescriptionImpl;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.SemanticQueryOptimizer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class PositiveInclusionApplicator {

	AtomUnifier						unifier		= new AtomUnifier();
	QueryAnonymizer					anonymizer	= new QueryAnonymizer();
	OBDADataFactory					termFactory	= OBDADataFactoryImpl.getInstance();
	private SemanticQueryOptimizer	sqoOptimizer;

	public PositiveInclusionApplicator(SemanticQueryOptimizer sqoOptimizer) {
		this.sqoOptimizer = sqoOptimizer;
	}

	public PositiveInclusionApplicator() {
	}

	/**
	 * Check whether the given positive inclusion is applicable to the given
	 * atom
	 * 
	 * @param pi
	 *            the positive inclusion
	 * @param atom
	 *            the atom
	 * @return true if the positive inclusion is applicable to the atom, false
	 *         otherwise
	 */
	public boolean isPIApplicable(PositiveInclusion pi, PredicateAtom atom) {
		/*
		 * checks: (3) I is a role inclusion assertion and its right-hand side
		 * is either P or P-
		 */
		if (pi instanceof DLLiterRoleInclusionImpl) {
			RoleDescription including = ((DLLiterRoleInclusionImpl) pi).getIncluding();
			if (including instanceof AtomicRoleDescriptionImpl) {
				AtomicRoleDescriptionImpl role = (AtomicRoleDescriptionImpl) including;
				return role.getPredicate().equals(atom.getPredicate());
			} else {
				throw new RuntimeException("Error, unsupported role inclusion. " + pi);
			}
		} else if (pi instanceof DLLiterConceptInclusionImpl) {
			/*
			 * I is applicable to an atom A(x) if it has A in its right-hand
			 * side
			 */
			Predicate pred = atom.getPredicate();
			ConceptDescription inc = ((DLLiterConceptInclusionImpl) pi).getIncluding();
			Predicate inc_predicate = null;
			if (inc instanceof AtomicConceptDescription) {
				inc_predicate = ((AtomicConceptDescription) inc).getPredicate();
			} else if (inc instanceof ExistentialConceptDescription) {
				inc_predicate = ((ExistentialConceptDescription) inc).getPredicate();
			}

			if (!pred.equals(inc_predicate))
				return false;

			if (pred.getArity() == 1 && inc_predicate.getArity() == 1) {
				return true;
			} else if (pred.getArity() == 2 && inc_predicate.getArity() == 2) {
				Term t2 = atom.getTerms().get(1);
				Term t1 = atom.getTerms().get(0);
				ConceptDescription including = ((DLLiterConceptInclusionImpl) pi).getIncluding();
				if (including instanceof ExistentialConceptDescriptionImpl) {
					ExistentialConceptDescriptionImpl imp = (ExistentialConceptDescriptionImpl) including;
					if (t2 instanceof UndistinguishedVariable && !imp.isInverse()) {
						/*
						 * I is applicable to an atom P(x1, x2) if (1) x2 = _
						 * and the right-hand side of I is exist P
						 */
						return !imp.isInverse();

					} else if (t1 instanceof UndistinguishedVariable && imp.isInverse()) {
						/*
						 * I is applicable to an atom P(x1, x2) if (1) x1 = _
						 * and the right-hand side of I is exist P-
						 */
						return imp.isInverse();
					} else {
						return false;
					}
				} else {
					throw new RuntimeException("PositiveInclusionApplicator: Unknown postive inclusion type: " + pi);
				}
			} else {
				throw new RuntimeException("Unsupported arity in a positive inclusion. " + pi);
			}
		} else {
			throw new RuntimeException("PositiveInclusionApplicator: Unknown postive inclusion type: " + pi);
		}
	}

	public List<CQIE> apply(Collection<CQIE> cqs, Collection<PositiveInclusion> pis) throws Exception {
		List<CQIE> newqueries = new LinkedList<CQIE>();
		for (CQIE cq : cqs) {
			
			MemoryUtils.checkAvailableMemory();
			
			newqueries.addAll(apply(cq, pis));
		}
		return newqueries;
	}

	public List<CQIE> apply(CQIE query, Collection<PositiveInclusion> pis) throws Exception {
		int bodysize = query.getBody().size();
		HashSet<CQIE> newqueries = new HashSet<CQIE>(bodysize * pis.size() * 2);
		newqueries.add(query);

		for (int atomindex = 0; atomindex < bodysize; atomindex++) {
			
			MemoryUtils.checkAvailableMemory();
			
			HashSet<CQIE> currentatomresults = new HashSet<CQIE>(bodysize * pis.size() * 2);
			for (CQIE cq : newqueries) {
				List<Atom> body = cq.getBody();
				PredicateAtom atom = (PredicateAtom) body.get(atomindex);

				for (PositiveInclusion pi : pis) {
					if (isPIApplicable(pi, atom)) {
						currentatomresults.addAll(Collections.singletonList(applyPI(cq, pi, atomindex)));
					}
				}
			}
			newqueries.addAll(currentatomresults);
			
			
		}
		LinkedList<CQIE> result = new LinkedList<CQIE>();
		if (sqoOptimizer != null) {
			result.addAll(sqoOptimizer.optimizeBySQO(newqueries));
		} else {
			result.addAll(newqueries);
		}
		return result;
	}

	/***
	 * Applies a set of existential quantifications that have the same concept
	 * description CR on the right side of the inclusion, to a set of queries.
	 * It is very important that the inclusions share the same right side, since
	 * this method will apply a 'targeted' unification strategy to do only
	 * unification operations that are required for the application of CR.
	 * 
	 * @param cq
	 * @param pis
	 * @return
	 * @throws Exception
	 * @throws Exception
	 */
	public Collection<CQIE> applyExistentialInclusions(Collection<CQIE> cqs, Collection<PositiveInclusion> pis) throws Exception {

		// HashSet<CQIE> result = new HashSet<CQIE>(6000);
		if (pis == null || pis.isEmpty())
			return new HashSet<CQIE>(1);

		DLLiterConceptInclusionImpl samplepi = (DLLiterConceptInclusionImpl) pis.iterator().next();

		ExistentialConceptDescriptionImpl ex = (ExistentialConceptDescriptionImpl) samplepi.getIncluding();
		Set<CQIE> saturatedset = new HashSet<CQIE>();
		saturatedset.addAll(cqs);

		/*
		 * We try to saturate by unification, but only saturating if the atoms
		 * share already a variable, in the left or in the right, depending on
		 * exist R, or exist R-
		 */
		saturatedset = saturateByUnification(saturatedset, ex.getPredicate(), ex.isInverse());

		HashSet<CQIE> results = new HashSet<CQIE>(2500);
		/* Now we try to apoly the inclusions and collect only the results */

		for (PositiveInclusion pi : pis) {
			
			MemoryUtils.checkAvailableMemory();
			
			for (CQIE query : saturatedset) {
				List<Atom> body = query.getBody();
				for (int i = 0; i < body.size(); i++) {
					if (isPIApplicable(pi, (PredicateAtom) body.get(i))) {
						if (sqoOptimizer != null) {
							results.add(anonymizer.anonymize(sqoOptimizer.optimizeBySQO(applyPI(query, pi, i))));
						} else {
							results.add(anonymizer.anonymize(applyPI(query, pi, i)));
						}
					}
				}

			}
		}

		return results;
	}

	// public Collection<CQIE> applyExistentialInclusions(CQIE cq,
	// Collection<PositiveInclusion> pis) throws Exception {
	// HashSet<CQIE> result = new HashSet<CQIE>(6000);
	//
	// for (PositiveInclusion pi : pis) {
	// result.addAll(applyExistentialInclusion(cq, pi));
	// }
	// return result;
	// }

	/**
	 * This will saturate by unifying atoms that share some terms already. If is
	 * leftTermUnified, it will attempt to unify atoms that share a term on the
	 * left side already, if its false, it will do it on the right side.
	 * 
	 * This method only works for binary predicates
	 * 
	 * @param initialset
	 * @param predicate
	 * @param isInverse
	 * @return
	 * @throws Exception
	 */
	public Set<CQIE> saturateByUnification(Set<CQIE> initialset, Predicate predicate, boolean leftTermEqual) throws Exception {

		HashSet<CQIE> saturatedset = new HashSet<CQIE>(2500);
		saturatedset.addAll(initialset);
		HashSet<CQIE> newset = new HashSet<CQIE>(2500);

		/*
		 * Saturate by unification of every atoms with atom.predicate =
		 * predicate and anonymize
		 */
		boolean loop = true;
		while (loop) {
			
			MemoryUtils.checkAvailableMemory();
			
			loop = false;
			newset = new HashSet<CQIE>();
			for (CQIE currentcq : saturatedset) {
				List<Atom> body = currentcq.getBody();
				for (int i = 0; i < body.size(); i++) {
					/* Predicates are diferent, dont even try to unify */
					if (!((PredicateAtom) body.get(i)).getPredicate().equals(predicate))
						continue;
					/*
					 * We found an atom with the correct predicate, try to unify
					 * with the rest of the atoms
					 */
					for (int j = i + 1; j < body.size(); j++) {

						if (!((PredicateAtom) body.get(j)).getPredicate().equals(predicate))
							continue;

						PredicateAtom a1 = (PredicateAtom) body.get(i);
						PredicateAtom a2 = (PredicateAtom) body.get(j);

						Term ta10 = a1.getTerms().get(0);
						Term ta11 = a1.getTerms().get(1);
						Term ta20 = a2.getTerms().get(0);
						Term ta21 = a2.getTerms().get(1);

						boolean unify = false;

						/*
						 * We found a candidate, but we should only unify if the
						 * atoms already share a variable in the same position
						 * (left or right depends on the exist R. Note that, we
						 * consider equal also anonymous variables #
						 * 
						 * so if left = true we saturate if
						 * 
						 * a1 = P(#,x) or a2 = P(#,x) or if a1 = P(x,y) and a2 =
						 * P(x,z)
						 * 
						 * if left = false we saturate if
						 * 
						 * a1 = P(x,#) or a2 = P(x,#) or if a1 = P(y,x) and a2 =
						 * P(z,x)
						 */
						if (!leftTermEqual) {
							unify = ta11 instanceof UndistinguishedVariable || ta21 instanceof UndistinguishedVariable || ta11.equals(ta21);

							/*
							 * New condition, if left=true, and a1 = P(x,y) and
							 * a2 = P(x,z) only unify if for each atom ax1 where
							 * y appears, there is a another atom ax2 that is
							 * equal to ax1 except for the presence of y instead
							 * of $z$
							 */
							// if (unify)
							// unify = unify && matchingAtoms(currentcq, ta10,
							// ta20);

						} else {
							unify = ta10 instanceof UndistinguishedVariable || ta20 instanceof UndistinguishedVariable || ta10.equals(ta20);
							// if (unify)
							// unify = unify && matchingAtoms(currentcq, ta11,
							// ta21);
						}

						if (unify) {
							CQIE unifiedQuery = unifier.unify(currentcq, i, j);
							if (unifiedQuery != null) {
								newset.add(anonymizer.anonymize(unifiedQuery));
							}
						}
					}

				}
			}
			loop = loop || saturatedset.addAll(newset);
		}
		return saturatedset;
	}

	// /***
	// * This function is used in an optimization that still needs to be tested
	// *
	// * @param q
	// * @param t1
	// * @param t2
	// * @return
	// */
	// private boolean matchingAtoms(CQIE q, Term t1, Term t2) {
	// for (Atom a1 : q.getBody()) {
	// int t1idx = a1.getFirstOcurrance(t1, 0);
	// if (t1idx == -1)
	// continue;
	// /* this atom conatains the focus term, t1 */
	//
	// for (Atom a2 : q.getBody()) {
	//
	// /*
	// * we dont want to compare against the same atom, not
	// * interesting
	// */
	// if (a2.equals(a1))
	// continue;
	//
	// int t2idx = a2.getFirstOcurrance(t2, 0);
	// if (t2idx != -1)
	// continue;
	//
	// /*
	// * the atom contains t2, now we need to check that they match,
	// * except for the t1 and t2
	// */
	//
	// /*
	// * If the predicates are different, stop, any unification of t1
	// * and t2 will fail
	// */
	// if (!a1.getPredicate().equals(a2.getPredicate()))
	// return false;
	//
	// List<Term> terms1 = a1.getTerms();
	// List<Term> terms2 = a2.getTerms();
	// for (int m = 0; m < a1.getPredicate().getArity(); m++) {
	// Term a1t = terms1.get(m);
	// Term a2t = terms2.get(m);
	//
	// /*
	// * if the any of the terms are #, its safe to unify, check
	// * another term
	// */
	//
	// if ((a1t instanceof UndistinguishedVariable) || (a2t instanceof
	// UndistinguishedVariable))
	// continue;
	//
	// /*
	// * If the terms are different, but equal to t1 and t2, its
	// * also safe
	// */
	//
	// if ((a1t.equals(t1) && (a2t.equals(t2))) || (a1t.equals(t2) &&
	// (a2t.equals(t1))))
	// continue;
	//
	// /*
	// * if the terms are actually different, then there is no
	// * point in unifying
	// */
	// if (!a1t.equals(a2t))
	// return false;
	// }
	//
	// }
	// }
	// return true;
	// }

	// public Collection<CQIE> applyExistentialInclusion(CQIE cq,
	// PositiveInclusion pi) throws Exception {
	// DLLiterConceptInclusionImpl cinc = (DLLiterConceptInclusionImpl) pi;
	//
	// Predicate predicate = null;
	//
	// if (cinc.getIncluding() instanceof AtomicConceptDescription) {
	// predicate = ((AtomicConceptDescription)
	// cinc.getIncluding()).getPredicate();
	// } else if (cinc.getIncluding() instanceof ExistentialConceptDescription)
	// {
	// predicate = ((ExistentialConceptDescription)
	// cinc.getIncluding()).getPredicate();
	// }
	//
	// ExistentialConceptDescriptionImpl ex =
	// (ExistentialConceptDescriptionImpl) cinc.getIncluding();
	//
	// HashSet<CQIE> initialset = new HashSet<CQIE>();
	// initialset.add(cq);
	// Set<CQIE> saturatedset = saturateByUnification(initialset, predicate,
	// ex.isInverse());
	//
	// HashSet<CQIE> results = new HashSet<CQIE>(2500);
	// /* Now we try to apoly the inclusions and collect only the results */
	//
	// for (CQIE query : saturatedset) {
	// List<Atom> body = query.getBody();
	// for (int i = 0; i < body.size(); i++) {
	// if (isPIApplicable(pi, body.get(i))) {
	// results.add(applyPI(query, pi, i));
	// }
	// }
	//
	// }
	// return results;
	// }

	public CQIE applyPI(CQIE q, PositiveInclusion inclusion, int atomindex) {

		CQIE newquery = q.clone();

		List<Atom> body = newquery.getBody();
		PredicateAtom a = (PredicateAtom) body.get(atomindex);

		if (a.getArity() == 1) {

			/*
			 * Only concept inclusions
			 */

			if (inclusion instanceof DLLiterConceptInclusionImpl) {
				DLLiterConceptInclusionImpl inc = (DLLiterConceptInclusionImpl) inclusion;
				ConceptDescription lefthandside = inc.getIncluded();
				ConceptDescription righthandside = inc.getIncluding();

				if (lefthandside instanceof AtomicConceptDescriptionImpl) {

					/* This is the simplest case A(x) generates B(x) */

					List<Term> terms = a.getTerms();
					LinkedList<Term> v = new LinkedList<Term>();
					Iterator<Term> tit = terms.iterator();
					while (tit.hasNext()) {
						v.add(tit.next().clone());
					}

					Predicate predicate = null;

					if (lefthandside instanceof AtomicConceptDescription) {
						predicate = ((AtomicConceptDescription) lefthandside).getPredicate();
					} else if (lefthandside instanceof ExistentialConceptDescription) {
						predicate = ((ExistentialConceptDescription) lefthandside).getPredicate();
					}

					PredicateAtom newatom = termFactory.getAtom(predicate.clone(), v);

					body.set(atomindex, newatom);

				} else if (lefthandside instanceof ExistentialConceptDescriptionImpl) {

					/*
					 * Generating a role atom from a concept atom A(x) genrates
					 * A(x,#)
					 */
					Term t = a.getTerms().get(0);
					Term anonym = termFactory.getNondistinguishedVariable();
					PredicateAtom newatom = null;

					if (((ExistentialConceptDescriptionImpl) lefthandside).isInverse()) {
						LinkedList<Term> v = new LinkedList<Term>();
						v.add(0, anonym);
						v.add(1, t);

						Predicate predicate = null;

						if (lefthandside instanceof AtomicConceptDescription) {
							predicate = ((AtomicConceptDescription) lefthandside).getPredicate();
						} else if (lefthandside instanceof ExistentialConceptDescription) {
							predicate = ((ExistentialConceptDescription) lefthandside).getPredicate();
						}
						newatom = termFactory.getAtom(predicate.clone(), v);
					} else {
						LinkedList<Term> v = new LinkedList<Term>();
						v.add(0, t);
						v.add(1, anonym);

						Predicate predicate = null;

						if (lefthandside instanceof AtomicConceptDescription) {
							predicate = ((AtomicConceptDescription) lefthandside).getPredicate();
						} else if (lefthandside instanceof ExistentialConceptDescription) {
							predicate = ((ExistentialConceptDescription) lefthandside).getPredicate();
						}
						newatom = termFactory.getAtom(predicate.clone(), v);
					}

					body.set(atomindex, newatom);
				} else {
					throw new RuntimeException("Unsupported PI application" + inclusion);
				}
			} else {
				throw new RuntimeException("Application of a non-concept inclusion pi to a non-unary atom is impossible.");
			}

		} else if (inclusion instanceof DLLiterConceptInclusionImpl) {

			/*
			 * These cases cover unification an going from R atoms to C atoms.
			 */

			DLLiterConceptInclusionImpl inc = (DLLiterConceptInclusionImpl) inclusion;
			ConceptDescription lefthandside = inc.getIncluded();
			ExistentialConceptDescription righthandside = (ExistentialConceptDescription) inc.getIncluding();

			Term t1 = a.getTerms().get(0);
			Term t2 = a.getTerms().get(1);

			PredicateAtom newatom = null;

			if (t2 instanceof UndistinguishedVariable && !righthandside.isInverse()) {

				/* These are the cases that go from a P(x,#) to a A(x) */

				if (lefthandside instanceof AtomicConceptDescriptionImpl) {
					LinkedList<Term> v = new LinkedList<Term>();
					v.add(0, t1);

					Predicate predicate = null;

					if (lefthandside instanceof AtomicConceptDescription) {
						predicate = ((AtomicConceptDescription) lefthandside).getPredicate();
					} else if (lefthandside instanceof ExistentialConceptDescription) {
						predicate = ((ExistentialConceptDescription) lefthandside).getPredicate();
					}
					newatom = termFactory.getAtom(predicate, v);

				} else if (((ExistentialConceptDescription) lefthandside).isInverse()) {
					LinkedList<Term> v = new LinkedList<Term>();
					v.add(0, t2);
					v.add(1, t1);

					Predicate predicate = null;

					if (lefthandside instanceof AtomicConceptDescription) {
						predicate = ((AtomicConceptDescription) lefthandside).getPredicate();
					} else if (lefthandside instanceof ExistentialConceptDescription) {
						predicate = ((ExistentialConceptDescription) lefthandside).getPredicate();
					}
					newatom = termFactory.getAtom(predicate, v);

				} else if (!((ExistentialConceptDescription) lefthandside).isInverse()) {
					LinkedList<Term> v = new LinkedList<Term>();
					v.add(0, t1);
					v.add(1, t2);

					Predicate predicate = null;

					if (lefthandside instanceof AtomicConceptDescription) {
						predicate = ((AtomicConceptDescription) lefthandside).getPredicate();
					} else if (lefthandside instanceof ExistentialConceptDescription) {
						predicate = ((ExistentialConceptDescription) lefthandside).getPredicate();
					}
					newatom = termFactory.getAtom(predicate, v);

				}
			} else if (t1 instanceof UndistinguishedVariable && righthandside.isInverse()) {

				/* These cases go from R(#,x) to A(x), S(x,#) or S(#,x) */

				if (lefthandside instanceof AtomicConceptDescriptionImpl) {
					LinkedList<Term> v = new LinkedList<Term>();
					v.add(0, t2);

					Predicate predicate = null;

					if (lefthandside instanceof AtomicConceptDescription) {
						predicate = ((AtomicConceptDescription) lefthandside).getPredicate();
					} else if (lefthandside instanceof ExistentialConceptDescription) {
						predicate = ((ExistentialConceptDescription) lefthandside).getPredicate();
					}
					newatom = termFactory.getAtom(predicate, v);

				} else if (((ExistentialConceptDescription) lefthandside).isInverse()) {
					LinkedList<Term> v = new LinkedList<Term>();
					v.add(0, t1);
					v.add(1, t2);

					Predicate predicate = null;

					if (lefthandside instanceof AtomicConceptDescription) {
						predicate = ((AtomicConceptDescription) lefthandside).getPredicate();
					} else if (lefthandside instanceof ExistentialConceptDescription) {
						predicate = ((ExistentialConceptDescription) lefthandside).getPredicate();
					}
					newatom = termFactory.getAtom(predicate, v);

				} else if (!((ExistentialConceptDescription) lefthandside).isInverse()) {
					LinkedList<Term> v = new LinkedList<Term>();
					v.add(0, t2);
					v.add(1, t1);

					Predicate predicate = null;

					if (lefthandside instanceof AtomicConceptDescription) {
						predicate = ((AtomicConceptDescription) lefthandside).getPredicate();
					} else if (lefthandside instanceof ExistentialConceptDescription) {
						predicate = ((ExistentialConceptDescription) lefthandside).getPredicate();
					}
					newatom = termFactory.getAtom(predicate, v);

				}
			}

			if (newatom != null)
				body.set(atomindex, newatom);

		} else if (inclusion instanceof DLLiterRoleInclusionImpl) {

			/*
			 * For role inclusion P \ISA S
			 */

			DLLiterRoleInclusionImpl inc = (DLLiterRoleInclusionImpl) inclusion;
			RoleDescription lefthandside = inc.getIncluded();
			RoleDescription righthandside = inc.getIncluding();

			PredicateAtom newatom = null;

			Term t1 = a.getTerms().get(0);
			Term t2 = a.getTerms().get(1);

			/* All these cases go from R(x,y) to S(x,y) */

			if ((righthandside.isInverse() && lefthandside.isInverse()) || (!righthandside.isInverse() && !lefthandside.isInverse())) {
				LinkedList<Term> v = new LinkedList<Term>();
				v.add(0, t1);
				v.add(1, t2);
				newatom = termFactory.getAtom(lefthandside.getPredicate(), v);

			} else {
				LinkedList<Term> v = new LinkedList<Term>();
				v.add(0, t2);
				v.add(1, t1);
				newatom = termFactory.getAtom(lefthandside.getPredicate(), v);

			}

			if (newatom != null)
				body.set(atomindex, newatom);

		}

		return newquery;
	}
}
