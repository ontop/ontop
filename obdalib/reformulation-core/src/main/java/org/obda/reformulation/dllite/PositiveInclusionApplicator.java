package org.obda.reformulation.dllite;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.Predicate;
import org.obda.query.domain.Term;
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

	private final TermFactoryImpl termFactory = TermFactoryImpl.getInstance();

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

	//TODO
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

	public List<CQIE> applyExistentialInclusions(CQIE cq, Collection<PositiveInclusion> pis) {
		List<CQIE> newqueries = new LinkedList<CQIE>();
		List<Atom> body = cq.getBody();
		for (PositiveInclusion pi : pis) {
			DLLiterConceptInclusionImpl inc = (DLLiterConceptInclusionImpl)pi;
			Predicate relevantPredicate = inc.getIncluded().getPredicate();


		}
		return newqueries;
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
