package org.obda.reformulation.dllite;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.Predicate;
import org.obda.query.domain.Term;
import org.obda.query.domain.TermFactory;
import org.obda.query.domain.Variable;
import org.obda.query.domain.imp.AtomImpl;
import org.obda.query.domain.imp.TermFactoryImpl;
import org.obda.query.domain.imp.UndistinguishedVariable;
import org.obda.reformulation.domain.ConceptDescription;
import org.obda.reformulation.domain.PositiveInclusion;
import org.obda.reformulation.domain.RoleDescription;
import org.obda.reformulation.domain.imp.AtomicConceptDescriptionImpl;
import org.obda.reformulation.domain.imp.AtomicRoleDescriptionImpl;
import org.obda.reformulation.domain.imp.DLLiterConceptInclusionImpl;
import org.obda.reformulation.domain.imp.DLLiterRoleInclusionImpl;
import org.obda.reformulation.domain.imp.ExistentialConceptDescriptionImpl;

public class PositiveInclusionApplicator {

	AtomUnifier		unifier		= new AtomUnifier();
	QueryAnonymizer	anonymizer	= new QueryAnonymizer();
	
	TermFactory termFactory = TermFactoryImpl.getInstance();

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
	public boolean isPIApplicable(PositiveInclusion pi, Atom atom) {
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
			Predicate inc_predicate = inc.getPredicate();

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
						return !including.isInverse();

					} else if (t1 instanceof UndistinguishedVariable && imp.isInverse()) {
						/*
						 * I is applicable to an atom P(x1, x2) if (1) x1 = _
						 * and the right-hand side of I is exist P-
						 */
						return including.isInverse();
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

	public List<CQIE> apply(Collection<CQIE> cqs, Collection<PositiveInclusion> pis) {
		List<CQIE> newqueries = new LinkedList<CQIE>();
		for (CQIE cq : cqs) {
			newqueries.addAll(apply(cq, pis));
		}
		return newqueries;
	}

	public List<CQIE> apply(CQIE cq, Collection<PositiveInclusion> pis) {
		List<CQIE> newqueries = new LinkedList<CQIE>();
		List<Atom> body = cq.getBody();
		for (int atomindex = 0; atomindex < body.size(); atomindex++) {
			Atom atom = body.get(atomindex);
			for (PositiveInclusion pi : pis) {
				if (isPIApplicable(pi, atom)) {
					newqueries.add(applyPI(cq, pi, atomindex));
				}
			}
		}
		return newqueries;
	}

	/***
	 * Removes all atoms that are redundant w.r.t to query containment.This is
	 * done by going through all unifyiable atoms, attempting to unify them. If
	 * they unify with a MGU that is empty, then one of the atoms is redundant.
	 * 
	 * @param q
	 * @throws Exception
	 */
	public CQIE removeRundantAtoms(CQIE q) throws Exception {
		CQIE result = q;
		for (int i = 0; i < result.getBody().size(); i++) {
			Atom currentAtom = result.getBody().get(i);
			for (int j = i + 1; j < result.getBody().size(); j++) {
				Atom nextAtom = result.getBody().get(j);
				Map<Variable, Term> map = unifier.getMGU(currentAtom, nextAtom);
				if (map != null && map.isEmpty()) {
					result = unifier.unify(result, i, j);
				}
			}

		}
		return result;
	}
	
	/**
	 * Removes all atoms that are equalt (syntactically) and then all the atoms that are 
	 * redundant due to CQC.
	 * 
	 * @param queries
	 * @throws Exception 
	 */
	public HashSet<CQIE> removeDuplicateAtoms(Collection<CQIE> queries) throws Exception {
		HashSet<CQIE> newqueries = new HashSet<CQIE>(queries.size()*2);
		for (CQIE cq : queries) {
			List<Atom> body = cq.getBody();
			for (int i = 0; i < body.size(); i++) {
				Atom currentAtom = body.get(i);
				for (int j = i + 1; j < body.size(); j++) {
					Atom comparisonAtom = body.get(j);
					if (currentAtom.getPredicate().equals(comparisonAtom.getPredicate())) {
						if (currentAtom.toString().equals(comparisonAtom.toString())) {
							body.remove(j);
						}
					}
				}
			}
			newqueries.add(anonymizer.anonymize(removeRundantAtoms(cq)));
		}
		return newqueries;
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

		saturatedset = saturateByUnification(saturatedset, ex.getPredicate(), ex.isInverse());

		HashSet<CQIE> results = new HashSet<CQIE>(2500);
		/* Now we try to apoly the inclusions and collect only the results */

		for (PositiveInclusion pi : pis) {
			for (CQIE query : saturatedset) {
				List<Atom> body = query.getBody();
				for (int i = 0; i < body.size(); i++) {
					if (isPIApplicable(pi, body.get(i))) {
						results.add(anonymizer.anonymize((applyPI(query, pi, i))));
					}
				}

			}
		}

		return results;
	}

	public Collection<CQIE> applyExistentialInclusions(CQIE cq, Collection<PositiveInclusion> pis) throws Exception {
		HashSet<CQIE> result = new HashSet<CQIE>(6000);

		for (PositiveInclusion pi : pis) {
			result.addAll(applyExistentialInclusion(cq, pi));
		}
		return result;
	}

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

		/* Saturate by unification and anonymize */
		boolean loop = true;
		while (loop) {
			loop = false;
			newset = new HashSet<CQIE>();
			for (CQIE currentcq : saturatedset) {
				List<Atom> body = currentcq.getBody();
				for (int i = 0; i < body.size(); i++) {
					if (!body.get(i).getPredicate().equals(predicate))
						continue;
					for (int j = i + 1; j < body.size(); j++) {
						if (!body.get(j).getPredicate().equals(predicate))
							continue;

						Atom a1 = body.get(i);
						Atom a2 = body.get(j);

						Term ta10 = a1.getTerms().get(0);
						Term ta11 = a1.getTerms().get(1);
						Term ta20 = a2.getTerms().get(0);
						Term ta21 = a2.getTerms().get(1);

						boolean unify = false;
						unify = unify
								|| (!leftTermEqual && (ta11 instanceof UndistinguishedVariable || ta21 instanceof UndistinguishedVariable || ta11
										.getName().equals(ta21.getName())));
						unify = unify
								|| (leftTermEqual && (ta10 instanceof UndistinguishedVariable || ta20 instanceof UndistinguishedVariable || ta10
										.getName().equals(ta20.getName())));

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

	public Collection<CQIE> applyExistentialInclusion(CQIE cq, PositiveInclusion pi) throws Exception {
		DLLiterConceptInclusionImpl cinc = (DLLiterConceptInclusionImpl) pi;
		Predicate predicate = cinc.getIncluding().getPredicate();
		ExistentialConceptDescriptionImpl ex = (ExistentialConceptDescriptionImpl) cinc.getIncluding();

		HashSet<CQIE> initialset = new HashSet<CQIE>();
		initialset.add(cq);
		Set<CQIE> saturatedset = saturateByUnification(initialset, predicate, ex.isInverse());

		HashSet<CQIE> results = new HashSet<CQIE>(2500);
		/* Now we try to apoly the inclusions and collect only the results */

		for (CQIE query : saturatedset) {
			List<Atom> body = query.getBody();
			for (int i = 0; i < body.size(); i++) {
				if (isPIApplicable(pi, body.get(i))) {
					results.add(applyPI(query, pi, i));
				}
			}

		}

		return results;
	}

	public CQIE applyPI(CQIE q, PositiveInclusion inclusion, int atomindex) {

		CQIE newquery = q.clone();

		List<Atom> body = newquery.getBody();
		Atom a = body.get(atomindex);

		if (a.getArity() == 1) {
			if (inclusion instanceof DLLiterConceptInclusionImpl) {
				DLLiterConceptInclusionImpl inc = (DLLiterConceptInclusionImpl) inclusion;
				ConceptDescription lefthandside = inc.getIncluded();
				ConceptDescription righthandside = inc.getIncluding();

				if (lefthandside instanceof AtomicConceptDescriptionImpl) {

					List<Term> terms = a.getTerms();
					LinkedList<Term> v = new LinkedList<Term>();
					Iterator<Term> tit = terms.iterator();
					while (tit.hasNext()) {
						v.add(tit.next().copy());
					}
					AtomImpl newatom = new AtomImpl(lefthandside.getPredicate().copy(), v);

					body.set(atomindex, newatom);

				} else if (lefthandside instanceof ExistentialConceptDescriptionImpl) {

					Term t = a.getTerms().get(0);
					Term anonym = termFactory.createUndistinguishedVariable();
					AtomImpl newatom = null;

					if (((ExistentialConceptDescriptionImpl) lefthandside).isInverse()) {
						LinkedList<Term> v = new LinkedList<Term>();
						v.add(0, anonym);
						v.add(1, t);
						newatom = new AtomImpl(lefthandside.getPredicate().copy(), v);
					} else {
						LinkedList<Term> v = new LinkedList<Term>();
						v.add(0, t);
						v.add(1, anonym);
						newatom = new AtomImpl(lefthandside.getPredicate().copy(), v);
					}

					body.set(atomindex, newatom);
				} else {
					throw new RuntimeException("Unsupported PI application" + inclusion);
				}
			} else {
				throw new RuntimeException("Application of a non-concept inclusion pi to a non-unary atom is impossible.");
			}

		} else if (inclusion instanceof DLLiterConceptInclusionImpl) {
			DLLiterConceptInclusionImpl inc = (DLLiterConceptInclusionImpl) inclusion;
			ConceptDescription lefthandside = inc.getIncluded();
			ConceptDescription righthandside = inc.getIncluding();

			Term t1 = a.getTerms().get(0);
			Term t2 = a.getTerms().get(1);

			Atom newatom = null;

			if (t2 instanceof UndistinguishedVariable && !righthandside.isInverse()) {
				if (lefthandside instanceof AtomicConceptDescriptionImpl) {
					LinkedList<Term> v = new LinkedList<Term>();
					v.add(0, t1);
					newatom = new AtomImpl(lefthandside.getPredicate(), v);

				} else if (lefthandside.isInverse()) {
					LinkedList<Term> v = new LinkedList<Term>();
					v.add(0, t2);
					v.add(1, t1);
					newatom = new AtomImpl(lefthandside.getPredicate(), v);

				} else if (!lefthandside.isInverse()) {
					LinkedList<Term> v = new LinkedList<Term>();
					v.add(0, t1);
					v.add(1, t2);
					newatom = new AtomImpl(lefthandside.getPredicate(), v);

				}
			} else if (t1 instanceof UndistinguishedVariable && righthandside.isInverse()) {
				if (lefthandside instanceof AtomicConceptDescriptionImpl) {
					LinkedList<Term> v = new LinkedList<Term>();
					v.add(0, t2);
					newatom = new AtomImpl(lefthandside.getPredicate(), v);

				} else if (lefthandside.isInverse()) {
					LinkedList<Term> v = new LinkedList<Term>();
					v.add(0, t1);
					v.add(1, t2);
					newatom = new AtomImpl(lefthandside.getPredicate(), v);

				} else if (!lefthandside.isInverse()) {
					LinkedList<Term> v = new LinkedList<Term>();
					v.add(0, t2);
					v.add(1, t1);
					newatom = new AtomImpl(lefthandside.getPredicate(), v);

				}
			}

			if (newatom != null)
				body.set(atomindex, newatom);

		} else if (inclusion instanceof DLLiterRoleInclusionImpl) {

			DLLiterRoleInclusionImpl inc = (DLLiterRoleInclusionImpl) inclusion;
			RoleDescription lefthandside = inc.getIncluded();
			RoleDescription righthandside = inc.getIncluding();

			Atom newatom = null;

			Term t1 = a.getTerms().get(0);
			Term t2 = a.getTerms().get(1);
			if ((righthandside.isInverse() && lefthandside.isInverse()) || (!righthandside.isInverse() && !lefthandside.isInverse())) {
				LinkedList<Term> v = new LinkedList<Term>();
				v.add(0, t1);
				v.add(1, t2);
				newatom = new AtomImpl(lefthandside.getPredicate(), v);

			} else {
				LinkedList<Term> v = new LinkedList<Term>();
				v.add(0, t2);
				v.add(1, t1);
				newatom = new AtomImpl(lefthandside.getPredicate(), v);

			}

			if (newatom != null)
				body.set(atomindex, newatom);

		}

		return newquery;
	}
}
