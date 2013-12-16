/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.unfolding;

import it.unibz.krdb.obda.model.AlgebraOperatorPredicate;
import it.unibz.krdb.obda.model.BooleanOperationPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.VariableImpl;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQCUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.DatalogNormalizer;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.QueryAnonymizer;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.Unifier;
import it.unibz.krdb.obda.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator;
import it.unibz.krdb.obda.utils.DatalogDependencyGraphGenerator;
import it.unibz.krdb.obda.utils.QueryUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates partial evaluations of rules (the queries), with respect to a set
 * of (partial) facts (set of rules). The procedure uses extended resolution
 * such that inner terms are also evaluated.
 * 
 * <p/>
 * The input fact rules must be non-cyclic otherwise the procedures in this
 * class will not terminate.
 * 
 * @author mariano, mrezk
 */
public class DatalogUnfolder implements UnfoldingMechanism {

	private static final long serialVersionUID = 6088558456135748487L;

	private DatalogProgram unfoldingProgram;

	private static final OBDADataFactory termFactory = OBDADataFactoryImpl.getInstance();

	private static final Logger log = LoggerFactory.getLogger(DatalogUnfolder.class);

	private enum UnfoldingMode {
		UCQ, DATALOG
	};

	private UnfoldingMode unfoldingMode = UnfoldingMode.UCQ;

	private Map<Predicate, List<Integer>> primaryKeys = new HashMap<Predicate, List<Integer>>();

	private Map<Predicate, List<CQIE>> ruleIndex = new LinkedHashMap<Predicate, List<CQIE>>();

	/***
	 * Leaf predicates are those that do not appear in the head of any rule. If
	 * a predicate is a leaf predicate, it should not be unfolded, they indicate
	 * stop points to get partial evaluations.
	 * <p>
	 * Any atom that is not a leaf, and that cannot be unified with a rule
	 * (either cause of lack of MGU, or because of a rule for the predicate of
	 * the atom) is logically empty w.r.t. to the program.
	 */
	private Set<Predicate> extensionalPredicates = new HashSet<Predicate>();
	private HashSet<Predicate> allPredicates = new HashSet<Predicate>();

	public DatalogUnfolder(DatalogProgram unfoldingProgram) {
		this(unfoldingProgram, new HashMap<Predicate, List<Integer>>());
	}

	public DatalogUnfolder(DatalogProgram unfoldingProgram, Map<Predicate, List<Integer>> primaryKeys) {
		this.primaryKeys = primaryKeys;
		this.unfoldingProgram = unfoldingProgram;

		/*
		 * Creating a local index for the rules according to their predicate
		 */

	
		for (CQIE mappingrule : unfoldingProgram.getRules()) {
			Function head = mappingrule.getHead();

			List<CQIE> rules = ruleIndex.get(head.getFunctionSymbol());
			if (rules == null) {
				rules = new LinkedList<CQIE>();
				ruleIndex.put(head.getFunctionSymbol(), rules);
			}
			rules.add(mappingrule);

			/*
			 * Collecting the predicates that appear in the body of rules.
			 */
			for (Function atom : mappingrule.getBody()) {
				allPredicates.addAll(getPredicates(atom));
			}

		}

		/*
		 * the predicates taht do not appear in the head of rules are leaf
		 * predicates
		 */
		allPredicates.removeAll(ruleIndex.keySet());
		extensionalPredicates.addAll(allPredicates);
	}

	private Set<Predicate> getPredicates(Function atom) {
		Set<Predicate> predicates = new HashSet<Predicate>();
		Predicate currentpred = atom.getFunctionSymbol();
		if (currentpred instanceof BooleanOperationPredicate)
			return predicates;
		else if (currentpred instanceof AlgebraOperatorPredicate) {
			for (Term innerTerm : atom.getTerms()) {
				if (!(innerTerm instanceof Function))
					continue;
				predicates.addAll(getPredicates((Function) innerTerm));
			}
		} else {
			predicates.add(currentpred);
		}
		return predicates;
	}

	/***
	 * Given a query q and the {@link #unfoldingProgram}, this method will try
	 * to flatten the query as much as possible by applying resolution steps
	 * exaustively to every atom in the query against the rules in
	 * 'unfoldingProgram'. This will is exactly to computing a partial
	 * evaluation of q w.r.t. unfolding program, that is, an specialized verion
	 * of q w.r.t. to unfolding program that requires less steps to execute.
	 * <p>
	 * This is used to translate ontological queries to database queries in the
	 * when the unfolding program is a set of mapppings, and also to flatten the
	 * Datalog queries that are produced by the
	 * {@link SparqlAlgebraToDatalogTranslator} and in some other palces.
	 * 
	 * <p>
	 * Example: matching rule the unfolding program.
	 * <p>
	 * Unfolding Program<br>
	 * <br>
	 * A(x) :- table1(x,y)<br>
	 * A(x) :- table2(x,z)<br>
	 * B(x) :- table3(x,z)<br>
	 * <br>
	 * Query<br>
	 * <br>
	 * Q(x) :- A(x),B(x)<br>
	 * <br>
	 * Initially produces: <br>
	 * Q(x1) :- table1(x1,y1),B(x1)<br>
	 * Q(x1) :- table2(x1,z1),B(x1)<br>
	 * Q(x1) :- table1(x1,y1),table3(x1,z2)<br>
	 * Q(x1) :- table2(x1,z1),table3(x1,z2)<br>
	 * 
	 * But the final result is<br>
	 * Q(x1) :- table1(x1,y1),table3(x1,z2)<br>
	 * Q(x1) :- table2(x1,z1),table3(x1,z2)<br>
	 * 
	 * 
	 * <p>
	 * The strategy of this unfolding is simple, we cycle through all the
	 * queries and attempt to resolve atom 0 in the body againts the rules in
	 * unfoldng program. The resolution engine will generate 1 or more CQs as
	 * result. The original atom is removed, and the results are appended to the
	 * end of each query (hence its always safe to unfold atom 0). The new
	 * queries are kept for a next cycle. We stop when no new queries are
	 * produced.
	 * <p>
	 * The right side of left joins will never be touched.
	 * <p>
	 * Currently the method also is aware of functional dependencies (Primary
	 * keys) and will produce queres in which redundant joins w.r.t. to these
	 * are avoided.
	 * 
	 * @param inputquery
	 * @return
	 */
	private DatalogProgram flattenUCQ(DatalogProgram inputquery, String targetPredicate) {

		List<CQIE> workingSet = new LinkedList<CQIE>();
		workingSet.addAll(inputquery.getRules());

		for (CQIE query : workingSet) {	
			DatalogNormalizer.enforceEqualities(query, false);
		}

		//computePartialEvaluation(workingSet);
		computePartialEvaluationNew(workingSet);

		LinkedHashSet<CQIE> result = new LinkedHashSet<CQIE>();
		for (CQIE query : workingSet) {
			result.add(query);
		}
		
		
		DatalogProgram resultdp = termFactory.getDatalogProgram(result);
		
		/**
		 * We need to enforce equality again, because at this point it is 
		 *  possible that there is still some EQ(...) in the Program resultdp
		 * 
		 */
		DatalogNormalizer.enforceEqualities(resultdp, false);
		
		return resultdp;
	}

	/***
	 * Computes a Datalog unfolding. This is simply the original query, plus all
	 * the rules in the unfolding program that can be applied in a resolution
	 * step to the atoms in the original query. For example:
	 * <p>
	 * Unfolding Program<br>
	 * <br>
	 * A(x) :- table1(x,y)<br>
	 * A(x) :- table2(x,z)<br>
	 * B(x) :- table3(x,z)<br>
	 * <br>
	 * Query<br>
	 * <br>
	 * Q(x) :- A(x)<br>
	 * <br>
	 * Unfolding:<br>
	 * Q(x) :- A(x)<br>
	 * A(x) :- table1(x,y)<br>
	 * A(x) :- table2(x,z)<br>
	 * 
	 * @param inputquery
	 * @return
	 */
	private DatalogProgram unfoldToDatalog(DatalogProgram inputquery) {
		HashSet<CQIE> relevantrules = new HashSet<CQIE>();
		for (CQIE cq : inputquery.getRules()) {
			for (Function atom : cq.getBody()) {
				for (CQIE rule : unfoldingProgram.getRules(atom.getFunctionSymbol())) {
					/*
					 * No repeteatin is assured by the HashSet and the hashing
					 * implemented in each CQIE
					 */
					relevantrules.add(rule);
				}
			}
		}
		/**
		 * Done collecting relevant rules, appending the original query.
		 */
		LinkedList<CQIE> result = new LinkedList<CQIE>();
		result.addAll(inputquery.getRules());
		result.addAll(relevantrules);
		return termFactory.getDatalogProgram(result);
	}


	@Override
	/***
	 * Generates a partial evaluation of the rules in <b>inputquery</b> with respect to the
	 * with respect to the program given when this unfolder was initialized. 
	 * 
	 */
	public DatalogProgram unfold(DatalogProgram inputquery, String targetPredicate) {

		// log.debug("Unfolding mode: {}. Initial query size: {}",
		// unfoldingMode, inputquery.getRules().size());

		// inputquery = replaceURIsForFunctions(inputquery);


		/*
		 * Needed because the rewriter might generate query bodies like this
		 * B(x,_), R(x,_), underscores reperesnt uniquie anonymous varaibles.
		 * However, the SQL generator needs them to be explicitly unique.
		 * replacing B(x,newvar1), R(x,newvar2)
		 */
		inputquery = QueryAnonymizer.deAnonymize(inputquery);

		DatalogProgram partialEvaluation = flattenUCQ(inputquery, targetPredicate);

		DatalogProgram dp = termFactory.getDatalogProgram();
		
		QueryUtils.copyQueryModifiers(inputquery, dp);
		
		dp.appendRule(partialEvaluation.getRules());


		return dp;
	}

	// /***
	// * Unfolds the atom in position pos. The procedure will remove the atom in
	// * pos from the query, an for each rule in the unfoldingProgram that
	// matches
	// * the predicate of the atom, it will generate a new query in which the
	// body
	// * of the matching rule is appended to the input CQIE.
	// * <p/>
	// * Optimization, this method will use the Primary keys of the DB
	// predicates
	// * as follows: Given a primary Key on A, on columns 1,2, and an atom
	// * A(x,y,z) added by the resolution engine (always added at the end of the
	// * CQ body), we will look for other atom A(x,y,z') if the atom exists, we
	// * can unify both atoms, apply the MGU to the query and remove one of the
	// * atoms.
	// *
	// * <p/>
	// * If there is no rule that can be used to unfold the atom it will return
	// an
	// * empty list.
	// *
	// *
	// * @param pos
	// * @param currentQuery
	// * @return
	// */
	// private List<CQIE> unfoldAtom(int pos, CQIE currentQuery, int count) {
	// LinkedList<CQIE> partialEvaluations = new LinkedList<CQIE>();
	//
	// if (pos >= currentQuery.getBody().size())
	// return partialEvaluations;
	//
	// /* Do not unfold operator atoms */
	// Function atom = (Function) currentQuery.getBody().get(pos);
	//
	// // if (atom.getPredicate() instanceof OperationPredicate) {
	// // currentQuery.getBody().remove(pos);
	// // currentQuery.getBody().add(atom);
	// // partialEvaluations.add(currentQuery);
	// // return partialEvaluations;
	// // }
	//
	// Predicate atomPredicate = atom.getPredicate();
	//
	// String name = atomPredicate.getName().toString();
	// if (atomPredicate.equals(OBDAVocabulary.SPARQL_JOIN)) {
	// /*
	// * This is a nested atom, we need to handle it using the
	// */
	// partialEvaluations = unfoldNestedJoin(pos, currentQuery, count);
	//
	// } else if (atomPredicate.equals(OBDAVocabulary.SPARQL_LEFTJOIN_URI)) {
	//
	// } else {
	//
	// /*
	// * This is the normal case where there is no nesting and we resolve
	// * an atom with normal resolution steps
	// */
	//
	// List<CQIE> ruleList = unfoldingProgram.getRules(atomPredicate);
	//
	// for (CQIE mappingRule : ruleList) {
	// CQIE freshMappingRule = getFreshRule(mappingRule, count);
	//
	// CQIE pev = resolutionEngine.resolve(freshMappingRule,
	// currentQuery, pos);
	//
	// if (pev != null) {
	//
	// /*
	// * The following blocks eliminate redundant atoms w.r.t.
	// * query containment by doing syntactic checks on the atoms.
	// * This saves us from requiring full CQC checks. They are a
	// * bit hacky, and they change the cardinality of
	// * non-distinct queries.
	// */
	//
	// List<Function> newbody = pev.getBody();
	// int newatomcount = mappingRule.getBody().size();
	// int oldatoms = newbody.size() - newatomcount - 1;
	// for (int newatomidx = oldatoms + 1; newatomidx < newbody
	// .size(); newatomidx++) {
	// Function newatom = newbody.get(newatomidx);
	// if (newatom.getPredicate() instanceof BooleanOperationPredicate)
	// continue;
	//
	// /*
	// * OPTIMIZATION 1: PRIMARY KEYS
	// *
	// * We now take into account Primary Key constraints on
	// * the database to avoid adding redundant atoms to the
	// * query. This could also be done as an afterstep, using
	// * unification and CQC checks, however, its is much more
	// * expensive that way.
	// */
	//
	// /*
	// * Given a primary Key on A, on columns 1,2, and an atom
	// * A(x,y,z) added by the resolution engine (always added
	// * at the end of the CQ body), we will look for other
	// * atom A(x,y,z') if the atom exists, we can unify both
	// * atoms, apply the MGU to the query and remove one of
	// * the atoms.
	// */
	//
	// List<Integer> pkey = primaryKeys.get(newatom
	// .getPredicate());
	// if (pkey != null && !pkey.isEmpty()) {
	// /*
	// * the predicate has a primary key, looking for
	// * candidates for unification, when we find one we
	// * can stop, since the application of this
	// * optimization at each step of the derivation tree
	// * guarantees there wont be any other redundant
	// * atom.
	// */
	// Function replacement = null;
	//
	// Map<Variable, NewLiteral> mgu = null;
	// for (int idx2 = 0; idx2 <= oldatoms; idx2++) {
	// Function tempatom = newbody.get(idx2);
	//
	// if (tempatom.getPredicate().equals(
	// newatom.getPredicate())) {
	//
	// boolean redundant = true;
	// for (Integer termidx : pkey) {
	// if (!newatom.getTerm(termidx - 1)
	// .equals(tempatom
	// .getTerm(termidx - 1))) {
	// redundant = false;
	// break;
	// }
	// }
	// if (redundant) {
	// /* found a candidate replacement atom */
	// mgu = Unifier.getMGU(newatom, tempatom);
	// if (mgu != null) {
	// replacement = tempatom;
	// break;
	// }
	// }
	//
	// }
	// }
	//
	// if (replacement != null) {
	//
	// if (mgu == null)
	// throw new RuntimeException(
	// "Unexcpected case found while performing JOIN elimination. Contact the authors for debugging.");
	// pev = Unifier.applyUnifier(pev, mgu);
	// newbody = pev.getBody();
	// newbody.remove(newatomidx);
	// newatomidx -= 1;
	// continue;
	// }
	// }
	//
	// /*
	// * We remove all atoms that do not impose extra
	// * conditions on existing data. These are atoms that are
	// * implied by other atoms, and only check for the
	// * existance of data. They are redundant because there
	// * exists another atom that guarnatees satisfiabiliy of
	// * this atom. E.g.,
	// */
	//
	// // Function replacement = null;
	// // Map<Variable,Integer> variableCount =
	// // pev.getVariableCount();
	// //
	// // Map<Variable, Term> mgu = null;
	// // for (int idx2 = 0; idx2 <= oldatoms; idx2++) {
	// // Function tempatom = newbody.get(idx2);
	// //
	// // if
	// // (tempatom.getPredicate().equals(newatom.getPredicate()))
	// // {
	// //
	// // /*
	// // * Checking if all terms are the same, or if they
	// // * are different, the all variables in the current
	// // * atom are free variables (they do not appear
	// // anywhere
	// // else)
	// // */
	// // int termindex = 0;
	// // boolean redundant = true;
	// // for (termindex = 0; termindex <
	// // tempatom.getTerms().size(); termindex++) {
	// // Term currenTerm = newatom.getTerm(termindex);
	// // if (!currenTerm.equals(tempatom.getTerm(termindex))
	// // &&
	// // !(variableCount.get(currenTerm) == 1))
	// // {
	// // redundant = false;
	// // break;
	// // }
	// // }
	// //
	// // if (redundant) {
	// // /* found a candidate replacement atom */
	// // mgu = Unifier.getMGU(newatom, tempatom);
	// // if (mgu != null) {
	// // replacement = tempatom;
	// // break;
	// // }
	// // }
	// //
	// // }
	// // }
	// //
	// // if (replacement != null) {
	// //
	// // if (mgu == null)
	// // throw new RuntimeException(
	// //
	// "Unexcpected case found while performing JOIN elimination. Contact the authors for debugging.");
	// // pev = Unifier.applyUnifier(pev, mgu);
	// // newbody = pev.getBody();
	// // newbody.remove(newatomidx);
	// // newatomidx -= 1;
	// // continue;
	// // }
	//
	// /*
	// * We remove all atoms that do not impose extra
	// * conditions on existing data. These are atoms that are
	// * implied by other atoms, and only check for the
	// * existance of data. They are redundant because there
	// * exists another atom that guarnatees satisfiabiliy of
	// * this atom. E.g.,
	// *
	// * b1 = r(x,y,z), r(m,n,o) b1 = r(x,y,z), r(x,n,o) b1 =
	// * r(x,y,z), r(x,y,o)
	// *
	// * In all these bodies, the second atoms is redundant
	// * w.r.t. set semantics. Note that with bag semantics,
	// * removing the atoms changes the cardinality of the
	// * query. E.g., let the data for r be:
	// *
	// * r(1,2,3), r(4,5,6), r(1,7,8).
	// *
	// * Then we have that |b1| = 9, |b2| = 5 and |b3| = 3.
	// * However, since the current implementation of the
	// * system is relaxed w.r.t. count of non-distinct values
	// * (it will probably stay like that), we dont care and
	// * we can remove these atoms.
	// *
	// * The condition is, given 2 atoms A, B, freeze B, and
	// * try to unify A,B, if true, then B is redundant,
	// * eliminate, else non redundant.
	// */
	//
	// // boolean redundant = false;
	// // List<Function> body = pev.getBody();
	// // Map<Variable, Term> mgu = null;
	// // for (int idx2 = 0; idx2 <= oldatoms; idx2++) {
	// //
	// // Function atom2 = body.get(idx2);
	// // Function frozenAtom = atom2.clone();
	// // CQCUtilities.getCanonicalAtom(frozenAtom, 1, new
	// // HashMap<Variable, Term>());
	// // if (Unifier.getMGU(frozenAtom, newatom) != null) {
	// // System.out.println("Redundant");
	// // mgu = Unifier.getMGU(atom2, newatom);
	// // redundant = true;
	// // break;
	// // }
	// // }
	// // if (redundant) {
	// // pev = Unifier.applyUnifier(pev, mgu);
	// // newbody = pev.getBody();
	// // newbody.remove(newatomidx);
	// // newatomidx -= 1;
	// // continue;
	// // }
	//
	// }
	//
	// partialEvaluations.add(pev);
	// }
	// }
	// }
	//
	// return partialEvaluations;
	// }

	/***
	 * This method will attempt to unfold the TOP LEVEL nested JOINs. Adding
	 * them as conjuncts to the body of the UCQ </ul>
	 * 
	 * <p>
	 * 
	 * @param pos
	 * @param currentQuery
	 * @param count
	 * @return
	 */
	private void unfoldNestedJoin(CQIE currentQuery) {
		for (int atomIdx = 0; atomIdx < currentQuery.getBody().size(); atomIdx++) {
			Function function = currentQuery.getBody().get(atomIdx);
			/*
			 * Unfolding the Join atom
			 */

			Predicate innerPredicate = function.getFunctionSymbol();
			if (!(innerPredicate.getName().toString().equals(OBDAVocabulary.SPARQL_JOIN_URI)))
				continue;

			/* Found a join, removing the Join term and assimilating its terms */
			List<Function> body = currentQuery.getBody();
			body.remove(atomIdx);
			for (int subtermidx = function.getTerms().size() - 1; subtermidx >= 0; subtermidx--) {
				Term atom = function.getTerm(subtermidx);
				body.add(atomIdx, (Function)atom);
			}
			atomIdx += -1;
		}
	}

	// /***
	// * Unfolds the inner terms of a literal. If the literal is not a function,
	// * then returns the same literal. If the literal is a function, it will
	// try
	// * to recursively unfold each term, until no term is unfoldable.
	// */
	// private NewLiteral unfoldLiteral(NewLiteral literal, int count) {
	//
	// if (!(literal instanceof Function))
	// return literal;
	//
	// Function function = (Function) literal;
	// Predicate mainPredicate = function.getFunctionSymbol();
	//
	// if (mainPredicate instanceof DataTypePredicate
	// || mainPredicate instanceof URITemplatePredicate
	// || mainPredicate instanceof BooleanOperationPredicate) {
	// /*
	// * This is a casting, comparison or tempalte, nothing to unfold
	// */
	// return literal;
	// }
	//
	// /*
	// * All the inner terms are no longer unfoldable. Now we should unfold
	// * the current function.
	// */
	//
	// if (!(mainPredicate instanceof BuiltinPredicate)) {
	// /*
	// * This is a data atom, it should be unfolded with the usual
	// * resolution algorithm.
	// */
	//
	// /**
	// * TODODODOODDODOODODDO
	// */
	// }
	//
	// int numberofTerms = function.getTerms().size();
	// for (int idx = 0; idx < numberofTerms; idx++) {
	// while (isUnfoldable(function.getTerm(idx))) {
	// unfoldLiteral(function.getTerm(idx), count);
	// }
	// }
	//
	// /*
	// * We are in the case of Joins and LeftJoins. The inner terms are
	// * already not unfoldable.
	// *
	// *
	// * LeftJoins cannot be modified if the inner children are not
	// * unfoldable.
	// *
	// * If the current terms is a Join, If one of the childs is a join, and
	// * this is a Join, we can remove the inner join and incorporate it into
	// * the current join.
	// */
	//
	// if (mainPredicate.getName().toString()
	// .equals(OBDAVocabulary.SPARQL_LEFTJOIN_URI))
	// return function;
	//
	// /*
	// * This is a join, so any inner join should be broken and it's terms
	// * integrated.
	// */
	// Map<Integer, List<NewLiteral>> newTerms = new LinkedHashMap<Integer,
	// List<NewLiteral>>();
	//
	// for (int idx = 0; idx < numberofTerms; idx++) {
	// NewLiteral innerTerm = function.getTerm(idx);
	// if (!(innerTerm instanceof Function))
	// continue;
	// Function innerFunction = (Function) innerTerm;
	// if (!innerFunction.getFunctionSymbol().toString()
	// .equals(OBDAVocabulary.SPARQL_JOIN_URI))
	// continue;
	//
	// /* we found a Join, break it and assimilate */
	//
	// }
	//
	// Predicate atomPredicate = atom.getPredicate();
	//
	// String name = atomPredicate.getName().toString();
	// if (atomPredicate.equals(OBDAVocabulary.SPARQL_JOIN)) {
	// /*
	// * This is a nested atom, we need to handle it using the
	// */
	// partialEvaluations = unfoldNestedJoin(pos, currentQuery, count);
	//
	// } else if (atomPredicate.equals(OBDAVocabulary.SPARQL_LEFTJOIN_URI)) {
	//
	// } else {
	//
	// /*
	// * This is the normal case where there is no nesting and we resolve
	// * an atom with normal resolution steps
	// */
	//
	// List<CQIE> ruleList = unfoldingProgram.getRules(atomPredicate);
	//
	// for (CQIE mappingRule : ruleList) {
	// CQIE freshMappingRule = getFreshRule(mappingRule, count);
	//
	// CQIE pev = resolutionEngine.resolve(freshMappingRule,
	// currentQuery, pos);
	//
	// if (pev != null) {
	//
	// /*
	// * The following blocks eliminate redundant atoms w.r.t.
	// * query containment by doing syntactic checks on the atoms.
	// * This saves us from requiring full CQC checks. They are a
	// * bit hacky, and they change the cardinality of
	// * non-distinct queries.
	// */
	//
	// List<Function> newbody = pev.getBody();
	// int newatomcount = mappingRule.getBody().size();
	// int oldatoms = newbody.size() - newatomcount - 1;
	// for (int newatomidx = oldatoms + 1; newatomidx < newbody
	// .size(); newatomidx++) {
	// Function newatom = newbody.get(newatomidx);
	// if (newatom.getPredicate() instanceof BooleanOperationPredicate)
	// continue;
	//
	// /*
	// * OPTIMIZATION 1: PRIMARY KEYS
	// *
	// * We now take into account Primary Key constraints on
	// * the database to avoid adding redundant atoms to the
	// * query. This could also be done as an afterstep, using
	// * unification and CQC checks, however, its is much more
	// * expensive that way.
	// */
	//
	// /*
	// * Given a primary Key on A, on columns 1,2, and an atom
	// * A(x,y,z) added by the resolution engine (always added
	// * at the end of the CQ body), we will look for other
	// * atom A(x,y,z') if the atom exists, we can unify both
	// * atoms, apply the MGU to the query and remove one of
	// * the atoms.
	// */
	//
	// List<Integer> pkey = primaryKeys.get(newatom
	// .getPredicate());
	// if (pkey != null && !pkey.isEmpty()) {
	// /*
	// * the predicate has a primary key, looking for
	// * candidates for unification, when we find one we
	// * can stop, since the application of this
	// * optimization at each step of the derivation tree
	// * guarantees there wont be any other redundant
	// * atom.
	// */
	// Function replacement = null;
	//
	// Map<Variable, NewLiteral> mgu = null;
	// for (int idx2 = 0; idx2 <= oldatoms; idx2++) {
	// Function tempatom = newbody.get(idx2);
	//
	// if (tempatom.getPredicate().equals(
	// newatom.getPredicate())) {
	//
	// boolean redundant = true;
	// for (Integer termidx : pkey) {
	// if (!newatom.getTerm(termidx - 1)
	// .equals(tempatom
	// .getTerm(termidx - 1))) {
	// redundant = false;
	// break;
	// }
	// }
	// if (redundant) {
	// /* found a candidate replacement atom */
	// mgu = Unifier.getMGU(newatom, tempatom);
	// if (mgu != null) {
	// replacement = tempatom;
	// break;
	// }
	// }
	//
	// }
	// }
	//
	// if (replacement != null) {
	//
	// if (mgu == null)
	// throw new RuntimeException(
	// "Unexcpected case found while performing JOIN elimination. Contact the authors for debugging.");
	// pev = Unifier.applyUnifier(pev, mgu);
	// newbody = pev.getBody();
	// newbody.remove(newatomidx);
	// newatomidx -= 1;
	// continue;
	// }
	// }
	//
	// /*
	// * We remove all atoms that do not impose extra
	// * conditions on existing data. These are atoms that are
	// * implied by other atoms, and only check for the
	// * existance of data. They are redundant because there
	// * exists another atom that guarnatees satisfiabiliy of
	// * this atom. E.g.,
	// */
	//
	// // Function replacement = null;
	// // Map<Variable,Integer> variableCount =
	// // pev.getVariableCount();
	// //
	// // Map<Variable, Term> mgu = null;
	// // for (int idx2 = 0; idx2 <= oldatoms; idx2++) {
	// // Function tempatom = newbody.get(idx2);
	// //
	// // if
	// // (tempatom.getPredicate().equals(newatom.getPredicate()))
	// // {
	// //
	// // /*
	// // * Checking if all terms are the same, or if they
	// // * are different, the all variables in the current
	// // * atom are free variables (they do not appear
	// // anywhere
	// // else)
	// // */
	// // int termindex = 0;
	// // boolean redundant = true;
	// // for (termindex = 0; termindex <
	// // tempatom.getTerms().size(); termindex++) {
	// // Term currenTerm = newatom.getTerm(termindex);
	// // if (!currenTerm.equals(tempatom.getTerm(termindex))
	// // &&
	// // !(variableCount.get(currenTerm) == 1))
	// // {
	// // redundant = false;
	// // break;
	// // }
	// // }
	// //
	// // if (redundant) {
	// // /* found a candidate replacement atom */
	// // mgu = Unifier.getMGU(newatom, tempatom);
	// // if (mgu != null) {
	// // replacement = tempatom;
	// // break;
	// // }
	// // }
	// //
	// // }
	// // }
	// //
	// // if (replacement != null) {
	// //
	// // if (mgu == null)
	// // throw new RuntimeException(
	// //
	// "Unexcpected case found while performing JOIN elimination. Contact the authors for debugging.");
	// // pev = Unifier.applyUnifier(pev, mgu);
	// // newbody = pev.getBody();
	// // newbody.remove(newatomidx);
	// // newatomidx -= 1;
	// // continue;
	// // }
	//
	// /*
	// * We remove all atoms that do not impose extra
	// * conditions on existing data. These are atoms that are
	// * implied by other atoms, and only check for the
	// * existance of data. They are redundant because there
	// * exists another atom that guarnatees satisfiabiliy of
	// * this atom. E.g.,
	// *
	// * b1 = r(x,y,z), r(m,n,o) b1 = r(x,y,z), r(x,n,o) b1 =
	// * r(x,y,z), r(x,y,o)
	// *
	// * In all these bodies, the second atoms is redundant
	// * w.r.t. set semantics. Note that with bag semantics,
	// * removing the atoms changes the cardinality of the
	// * query. E.g., let the data for r be:
	// *
	// * r(1,2,3), r(4,5,6), r(1,7,8).
	// *
	// * Then we have that |b1| = 9, |b2| = 5 and |b3| = 3.
	// * However, since the current implementation of the
	// * system is relaxed w.r.t. count of non-distinct values
	// * (it will probably stay like that), we dont care and
	// * we can remove these atoms.
	// *
	// * The condition is, given 2 atoms A, B, freeze B, and
	// * try to unify A,B, if true, then B is redundant,
	// * eliminate, else non redundant.
	// */
	//
	// // boolean redundant = false;
	// // List<Function> body = pev.getBody();
	// // Map<Variable, Term> mgu = null;
	// // for (int idx2 = 0; idx2 <= oldatoms; idx2++) {
	// //
	// // Function atom2 = body.get(idx2);
	// // Function frozenAtom = atom2.clone();
	// // CQCUtilities.getCanonicalAtom(frozenAtom, 1, new
	// // HashMap<Variable, Term>());
	// // if (Unifier.getMGU(frozenAtom, newatom) != null) {
	// // System.out.println("Redundant");
	// // mgu = Unifier.getMGU(atom2, newatom);
	// // redundant = true;
	// // break;
	// // }
	// // }
	// // if (redundant) {
	// // pev = Unifier.applyUnifier(pev, mgu);
	// // newbody = pev.getBody();
	// // newbody.remove(newatomidx);
	// // newatomidx -= 1;
	// // continue;
	// // }
	//
	// }
	//
	// partialEvaluations.add(pev);
	// }
	// }
	// }
	//
	// return partialEvaluations;
	// }

	// /***
	// * Checks if a literal is unfoldable, i.e., a rule can be resolved against
	// * the literal or against an inner literal. Or, the literal can be
	// "broken"
	// * into its subcomponents.
	// *
	// * @param term
	// * @return
	// */
	// private boolean isUnfoldable(NewLiteral term) {
	// if (term instanceof Variable || term instanceof Constant) {
	// return false;
	// } else if (term instanceof Function) {
	// Predicate p = ((Function) term).getFunctionSymbol();
	// if (p instanceof BooleanOperationPredicate
	// || p instanceof DataTypePredicate
	// || p instanceof URITemplatePredicate) {
	// return false;
	// } else if (p instanceof AlgebraOperatorPredicate) {
	// // /*
	// // * This is an algebra operator. If its a Join, is unfoldable
	// // if
	// // * a) its parent is not a LeftJoin or b) at least one of its
	// // * inner terms is unfoldable If its a Left Join, it is
	// // * unfoldable is at least one of the inner terms is
	// // unfoldable.
	// // */
	// // String name = p.getName().toString();
	// // if (name.equals(OBDAVocabulary.SPARQL_JOIN_URI)
	// // && (term.getParent() == null || !((Function) term
	// // .getParent()).getFunctionSymbol().getName()
	// // .toASCIIString()
	// // .equals(OBDAVocabulary.SPARQL_LEFTJOIN_URI)))
	// // return true;
	// for (NewLiteral innerLit : ((Function) term).getTerms()) {
	// if (isUnfoldable(innerLit))
	// return true;
	// }
	// return false;
	// } else {
	// /*
	// * This is a data atom/term, it is unfoldable only if there is a
	// * rule int he mapping program that can be unified with the term
	// */
	// List<CQIE> rules = unfoldingProgram.getRules(p);
	// for (CQIE rule : rules) {
	// Map<Variable, NewLiteral> mgu = Unifier.getMGU(
	// rule.getHead(), (Function) term);
	// if (mgu != null)
	// return true;
	// }
	// return false;
	// }
	// }
	// throw new RuntimeException("Term type not supported: "
	// + term.getClass() + " Term: " + term.toString());
	//
	// }

	/***
	 * Replaces each variable 'v' in the query for a new variable constructed
	 * using the name of the original variable plus the counter. For example
	 * 
	 * <pre>
	 * q(x) :- C(x)
	 * 
	 * results in
	 * 
	 * q(x_1) :- C(x_1)
	 * 
	 * if counter = 1.
	 * </pre>
	 * 
	 * <p>
	 * This method can be used to generate "fresh" rules from a datalog program
	 * so that it can be used during a resolution step.
	 * 
	 * @param rule
	 * @param suffix
	 *            The integer that will be apended to every variable name
	 * @return
	 */
	public static CQIE getFreshRule(CQIE rule, int suffix) {
		// This method doesn't support nested functional terms
		CQIE freshRule = rule.clone();
		Function head = freshRule.getHead();
		List<Term> headTerms = head.getTerms();
		for (int i = 0; i < headTerms.size(); i++) {
			Term term = headTerms.get(i);
			Term newTerm = getFreshTerm(term, suffix);
			if (newTerm != null)
				headTerms.set(i, newTerm);
		}

		List<Function> body = freshRule.getBody();
		for (Function atom : body) {

			List<Term> atomTerms = ((Function) atom).getTerms();
			for (int i = 0; i < atomTerms.size(); i++) {
				Term term = atomTerms.get(i);
				Term newTerm = getFreshTerm(term, suffix);
				if (newTerm != null)
					atomTerms.set(i, newTerm);
			}
		}
		return freshRule;

	}

	public static Term getFreshTerm(Term term, int suffix) {
		Term newTerm = null;
		if (term instanceof VariableImpl) {
			VariableImpl variable = (VariableImpl) term;
			newTerm = termFactory.getVariable(variable.getName() + "_" + suffix);
		} else if (term instanceof Function) {
			Function functionalTerm = (Function) term;
			List<Term> innerTerms = functionalTerm.getTerms();
			List<Term> newInnerTerms = new LinkedList<Term>();
			for (int j = 0; j < innerTerms.size(); j++) {
				Term innerTerm = innerTerms.get(j);
				newInnerTerms.add(getFreshTerm(innerTerm, suffix));
			}
			Predicate newFunctionSymbol = functionalTerm.getFunctionSymbol();
			Function newFunctionalTerm = (Function) termFactory.getFunction(newFunctionSymbol, newInnerTerms);
			newTerm = newFunctionalTerm;
		} else if (term instanceof Constant) {
			newTerm = term.clone();
		} else {
			throw new RuntimeException("Unsupported term: " + term);
		}
		return newTerm;

	}

	/***
	 * This method asumes that the inner term (termidx) of term is a data atom,
	 * or a nested atom.
	 * <p>
	 * If the term is a data atom, it returns all the new rule resulting from
	 * resolving that atom with unifiable rules in the unfoldign program. If
	 * there are no such rules it returns null (the atom is logically empty).
	 * <p>
	 * If the atom is a Join or LeftJoin (algebra operators) it will recursively
	 * call unfoldin into each term until one method returns something different
	 * than the rule itself.
	 * <p>
	 * If the term is not a data atom, e.g., datatype atom, variable, constant,
	 * boolean atom., the method returns the original rule, without change.
	 * 
	 * otherwise it does nothing (i.e., variables, constants, etc cannot be
	 * resolved against rule
	 * 
	 * @param resolvent
	 * @param term
	 * @param termidx
	 * @return
//	 */
//	public int computePartialEvaluation(List<CQIE> workingList) {
//
//		int[] rcount = { 0, 0 };
//
//		for (int queryIdx = 0; queryIdx < workingList.size(); queryIdx++) {
//
//			CQIE rule = workingList.get(queryIdx);
//
//			Stack<Integer> termidx = new Stack<Integer>();
//
//			List<Function> currentTerms = rule.getBody();
//			List<Term> tempList = new LinkedList<Term>();
//			for (Function a : currentTerms) {
//				tempList.add(a);
//			}
//
//			List<CQIE> result = computePartialEvaluation(tempList, rule, rcount, termidx, false);
//
//			if (result == null) {
//
//				/*
//				 * If the result is null the rule is logically empty
//				 */
//				workingList.remove(queryIdx);
//				queryIdx -= 1;
//				continue;
//			} else if (result.size() == 0) {
//
//				/*
//				 * This rule is already a partial evaluation
//				 */
//				continue;
//			}
//
//			/*
//			 * One more step in the partial evaluation was computed, we need to
//			 * remove the old query and add the result instead. Each of the new
//			 * queries could still require more steps of evaluation, so we
//			 * decrease the index.
//			 */
//			workingList.remove(queryIdx);
//			for (CQIE newquery : result) {
//				if (!workingList.contains(newquery)) {
//					workingList.add(queryIdx, newquery);
//				}
//			}
//			queryIdx -= 1;
//		}
//		return rcount[1];
//	}

	// New method
	public int computePartialEvaluationNew(List<CQIE> workingList) {

		int[] rcount = { 0, 0 }; //int queryIdx = 0;
		
		DatalogDependencyGraphGenerator depGraph = new DatalogDependencyGraphGenerator(workingList);
		
		List<Predicate> predicatesInBottomUp = depGraph.getPredicatesInBottomUp();		
		Set<Predicate> extensionalPredicates = depGraph.getExtensionalPredicates();

		//for (int queryIdx = workingList.size() - 1; queryIdx > 0; queryIdx--) {	

		for (int predIdx = 0; predIdx < predicatesInBottomUp.size() -1; predIdx++) {

			Predicate pred = predicatesInBottomUp.get(predIdx);
			if (!extensionalPredicates.contains(pred)) {// it is a defined  predicate, like ans2,3.. etc

				ruleIndex = depGraph.getRuleIndex();
				List<CQIE> workingRules = ruleIndex.get(pred);
				Predicate preFather =  depGraph.getFatherPredicate(pred);
				List<CQIE> fatherWorkingRules = ruleIndex.get(preFather);
				
				
				for (CQIE rule : fatherWorkingRules) {
					// CQIE rule = workingList.get(queryIdx);

					Stack<Integer> termidx = new Stack<Integer>();

					List<Function> currentTerms = rule.getBody();
					List<Term> tempList = new LinkedList<Term>();
					
					for (Function a : currentTerms) {
							tempList.add(a);
					}
					List<CQIE> result = computePartialEvaluation( pred, tempList, rule, rcount, termidx, false);
			

					
					int queryIdx=workingList.indexOf(rule);
					
					if (result == null) {
						/*
						 * If the result is null the rule is logically empty
						 */
						
						workingList.remove(queryIdx);
						// queryIdx -= 1;
						continue;

					} else if (result.size() == 0) {
						/*
						 * This rule is already a partial evaluation
						 */
						continue;
					}
					/*
					 * One more step in the partial evaluation was computed, we
					 * need to remove the old query and add the result instead.
					 * Each of the new queries could still require more steps of
					 * evaluation, so we decrease the index.
					 */
					// workingList.remove(queryIdx);
					for (CQIE newquery : result) {
						if (!workingList.contains(newquery)) {
							workingList.remove(queryIdx);
							workingList.removeAll(workingRules);
							workingList.add(queryIdx, newquery);
							depGraph.removeAllPredicateFromRuleIndex(preFather);
							depGraph.setRuleInGraph(preFather, newquery);
						}
					}
				} // end for workingRules

			} else { // it is an extensional predicate
				continue;
			}

		} // end for over the ordered predicates
		return rcount[1];
	}


	
	
	/***
	 * Goes trhough each term, and recursively each inner term trying to resovle
	 * each atom. Returns an empty list if the partial evaluation is completed
	 * (no atoms can be resovled and each atom is a leaf atom), null if there is
	 * at least one atom that is not leaf and cant be resolved, or a list with
	 * one or more queries if there was one atom that could be resolved againts
	 * one or more rules. The list containts the result of the resolution steps
	 * againts those rules.
	 * @param resolvPred 
	 * 
	 * @param currentTerms
	 * @param rule
	 * @param resolutionCount
	 * @param termidx
	 * @return
	 */

	private List<CQIE> computePartialEvaluation(Predicate resolvPred, List<Term> currentTerms, CQIE rule, int[] resolutionCount, Stack<Integer> termidx,
			boolean parentIsLeftJoin) {

		int nonBooleanAtomCounter = 0;

		for (int atomIdx = 0; atomIdx < currentTerms.size(); atomIdx++) {
			termidx.push(atomIdx);

			Function focusLiteral = (Function) currentTerms.get(atomIdx);

			if (focusLiteral.isBooleanFunction() || focusLiteral.isArithmeticFunction() || focusLiteral.isDataTypeFunction()) {
				termidx.pop();
				continue;
			} else if (focusLiteral.isAlgebraFunction()) {
				nonBooleanAtomCounter += 1;
				/*
				 * These may contain data atoms that need to be unfolded, we
				 * need to recursively unfold each term.
				 */

				// for (int i = 0; i < focusLiteral.getTerms().size(); i++) {

				Predicate predicate = focusLiteral.getFunctionSymbol();
				boolean focusAtomIsLeftJoin = predicate.equals(OBDAVocabulary.SPARQL_LEFTJOIN);
				List<CQIE> result = new LinkedList<CQIE>();
				result = computePartialEvaluation(resolvPred,  focusLiteral.getTerms(), rule, resolutionCount, termidx, focusAtomIsLeftJoin);

				if (result == null)
					return null;

				if (result.size() > 0) {
					return result;
				}

			} else if (focusLiteral.isDataFunction()) {
				nonBooleanAtomCounter += 1;

				/*
				 * This is a data atom, it should be unfolded with the usual
				 * resolution algorithm.
				 */

				boolean isLeftJoinSecondArgument = nonBooleanAtomCounter == 2 && parentIsLeftJoin;
				List<CQIE> result = resolveDataAtom(resolvPred, focusLiteral, rule, termidx, resolutionCount, parentIsLeftJoin,
						isLeftJoinSecondArgument);

				if (result == null)
					return null;

				if (result.size() > 0)
					return result;
			} else {
				throw new IllegalArgumentException(
						"Error during unfolding, trying to unfold a non-algrbra/non-data function. Offending atom: "
								+ focusLiteral.toString());
			}
			termidx.pop();
		}

		return new LinkedList<CQIE>();
	}
	
	private final List<CQIE> emptyList = Collections.unmodifiableList(new LinkedList<CQIE>());

	/***
	 * Applies a resolution step over a non-boolean/non-algebra atom (i.e. data
	 * atoms). The resolution step will will try to match the <strong>focus atom
	 * a</strong> in the input <strong>rule r</strong> againts the rules in
	 * {@link #unfoldingProgram}.
	 * <p>
	 * For each <strong>rule s</strong>, if the <strong>head h</strong> of s is
	 * unifiable with a, then this method will do the following:
	 * <ul>
	 * <li>Compute a most general unifier <strong>mgu</strong> for h and a (see
	 * {@link Unifier#getMGU(Function, Function)})</li>
	 * <li>Create a clone r' of r</li>
	 * <li>We replace a in r' with the body of s
	 * <li>
	 * <li>We apply mgu to r' (see {@link Unifier#applyUnifier(CQIE, Map)})</li>
	 * <li>return r'
	 * </ul>
	 * @param resolvPred 
	 * 
	 * 
	 * 
	 * @param focusAtom
	 *            The atom to be resolved.
	 * @param rule
	 *            The rule in which this atom resides
	 * @param termidx
	 *            The index of the atom in the rule (ifts nested, the stack
	 *            indicates the nesting, position by position, the first being
	 *            "list" positions (function term lists) and the last the focus
	 *            atoms position.
	 * @param resolutionCount
	 *            The number of resolution attemts done globaly, needed to spawn
	 *            fresh variables.
	 * @param atomindx
	 *            The location of the focustAtom in the currentlist
	 * @return <ul>
	 *         <li>null if there is no s whos head unifies with a, we return
	 *         null. </li><li>An empty list if the atom a is <strong>extensional
	 *         predicate (those that have no defining rules)</strong> or the
	 *         second data atom in a left join </li><li>a list with one ore more
	 *         rules otherwise</li>
	 *         <ul>
	 * 
	 * @see Unifier
	 */
	public List<CQIE> resolveDataAtom(Predicate resolvPred, Function focusAtom, CQIE rule, Stack<Integer> termidx, int[] resolutionCount, boolean isLeftJoin,
			boolean isSecondAtomInLeftJoin) {

		if (!focusAtom.isDataFunction())
			throw new RuntimeException("Cannot unfold a non-data atom: " + focusAtom);

		/*
		 * Leaf predicates are ignored (as boolean or algebra predicates)
		 */
		Predicate pred = focusAtom.getFunctionSymbol();
		if (extensionalPredicates.contains(pred)) {
			// The atom is a leaf, that means that is a data atom that
			// has no resolvent rule, and marks the end points to compute
			// partial evaluations

			return emptyList;
		}
		if (ruleIndex.keySet().contains(pred) && !pred.equals(resolvPred)) {
			//Since it is not resolvPred, we are are not going to resolve it
			return emptyList;
		}
		
		
		/*
		 * This is a real data atom, it either generates something, or null
		 * (empty)
		 */

		List<CQIE> rulesDefiningTheAtom = ruleIndex.get(pred);

		/*
		 * If there are none, the atom is logically empty, careful, LEFT JOIN
		 * alert!
		 */

		List<CQIE> result = null;
		if (rulesDefiningTheAtom == null) {
			if (!isSecondAtomInLeftJoin)
				return null;
			else {
				CQIE newRuleWithNullBindings = generateNullBindingsForLeftJoin(focusAtom, rule, termidx);
				result = new LinkedList<CQIE>();
				result.add(newRuleWithNullBindings);
			}
		} else {
			
			//result = generateResolutionResultParent(parentRule, focusAtom, rule, termidx, resolutionCount, rulesDefiningTheAtom, isLeftJoin, isSecondAtomInLeftJoin);
			result = generateResolutionResult(focusAtom, rule, termidx, resolutionCount, rulesDefiningTheAtom, isLeftJoin, isSecondAtomInLeftJoin);
		}

		if (result == null) {
			// this is the case for second atom in leaft join generating more
			// than one rull, we
			// must reutrn an empty result i ndicating its already a partial
			// evaluation.
			result = emptyList;
		} else if (result.size() == 0) {
			if (!isSecondAtomInLeftJoin)
				return null;
			else {
				CQIE newRuleWithNullBindings = generateNullBindingsForLeftJoin(focusAtom, rule, termidx);
				result = new LinkedList<CQIE>();
				result.add(newRuleWithNullBindings);
			}
		}
		return result;
	}

	/***
	 * * Normalizes a rule that has multiple data atoms to a rule in which there
	 * is one single Join atom by creating a nested Join structure. Required to
	 * resolve atoms in LeftJoins (any, left or right) to avoid ending up with
	 * left joins with more than 2 table defintions. If the body contains only
	 * one data atom (or none) it will return <strong>null</strong>. For
	 * example:
	 * 
	 * <pre>
	 * m(x,y) :- R(x,y), R(y,z), R(z,m)
	 * 
	 * into
	 * 
	 * m(x,y) :- Join(R(x,y), Join(R(y,z), R(z,m))
	 * </pre>
	 * 
	 * @param mapping
	 *            <bold>null</bold> if the body contains 0 or 1 data atoms,
	 *            otherwhise returns a new query with the nested joins.
	 * @return
	 */
	private CQIE foldJOIN(CQIE mapping) {
		// Checking if the rule has more that 1 data atom, otherwise we do
		// nothing. Now we count, and at the same time collect the atoms we
		// will need to manipulate in 2 temporal lists.

		// Data atoms in the list will be folded, boolean atoms will be
		// added to the bod in the end

		int dataAtoms = 0;

		List<Function> body = mapping.getBody();

		List<Function> dataAtomsList = new LinkedList<Function>();
		List<Function> otherAtomsList = new ArrayList<Function>(body.size() * 2);

		for (Function subAtom : body) {
			if (subAtom.isDataFunction() || subAtom.isAlgebraFunction()) {
				dataAtoms += 1;
				dataAtomsList.add(subAtom);
			} else {
				otherAtomsList.add(subAtom);
			}
		}
		if (dataAtoms == 1) {
			return null;
		}

		/*
		 * This mapping can be transformed into a normal join with ON
		 * conditions. Doing so.
		 */
		Function foldedJoinAtom = null;

		while (dataAtomsList.size() > 1) {
			foldedJoinAtom = termFactory.getFunction(OBDAVocabulary.SPARQL_JOIN, (Term) dataAtomsList.remove(0),
					(Term) dataAtomsList.remove(0));
			dataAtomsList.add(0, foldedJoinAtom);
		}

		List<Function> newBodyMapping = new LinkedList<Function>();
		newBodyMapping.add(foldedJoinAtom);
		newBodyMapping.addAll(otherAtomsList);

		CQIE newrule = termFactory.getCQIE(mapping.getHead(), newBodyMapping);

		return newrule;

	}

	/***
	 * Helper method for resolveDataAtom. Do not use anywhere else. This method
	 * returns a list with all the succesfull resolutions againts focusAtom. It
	 * will return a list with 0 ore more elements that result from successfull
	 * resolution steps, or null if there are more than 1 successfull resoluiton
	 * steps but focusAtom is the second atom of a left join (that is,
	 * isSecondAtomOfLeftJoin is true).
	 * 
	 * <p>
	 * Note the meaning of NULL in this method is different than the meaning of
	 * null and empty list in
	 * {@link #resolveDataAtom(Function, CQIE, Stack, int[], boolean)} which is
	 * the caller method. The job of interpreting correctly the output of this
	 * method is done in the caller.
	 * 
	 * 
	 * @param focusAtom
	 * @param rule
	 * @param termidx
	 * @param resolutionCount
	 * @param rulesDefiningTheAtom
	 * @param isSecondAtomOfLeftJoin
	 * @return
	 */
	private List<CQIE> generateResolutionResult(Function focusAtom, CQIE rule, Stack<Integer> termidx, int[] resolutionCount,
			List<CQIE> rulesDefiningTheAtom, boolean isLeftJoin, boolean isSecondAtomOfLeftJoin) {

		List<CQIE> candidateMatches = new LinkedList<CQIE>(rulesDefiningTheAtom);
//		List<CQIE> result = new ArrayList<CQIE>(candidateMatches.size() * 2);
		List<CQIE> result = new LinkedList<CQIE>();

		int rulesGeneratedSoFar = 0;
		for (CQIE candidateRule : candidateMatches) {

			resolutionCount[0] += 1;
			/* getting a rule with unique variables */
			CQIE freshRule = getFreshRule(candidateRule, resolutionCount[0]);

			Map<Variable, Term> mgu = Unifier.getMGU(freshRule.getHead(), focusAtom);

			if (mgu == null) {
				/* Failed attempt */
				resolutionCount[1] += 1;
				// if (resolutionCount[1] % 1000 == 0)
				// System.out.println(resolutionCount[1]);
				continue;
			}

			/*
			 * We have a matching rule, now we prepare for the resolution step
			 */

			// if we are in a left join, we need to make sure the fresh rule
			// has only one data atom
			if (isLeftJoin) {
			CQIE foldedJoinsRule = foldJOIN(freshRule);
			if (foldedJoinsRule != null)
				freshRule = foldedJoinsRule;
			}

			/*
			 * generating the new body of the rule
			 */

			CQIE partialEvalution = rule.clone();
			/*
			 * locating the list that contains the current Function (either body
			 * or inner term) and replacing the current atom, with the body of
			 * the matching rule.
			 */

			List innerAtoms = getNestedList(termidx, partialEvalution);

			innerAtoms.remove((int) termidx.peek());
			innerAtoms.addAll((int) termidx.peek(), freshRule.getBody());

			Unifier.applyUnifier(partialEvalution, mgu, false);

			/***
			 * DONE WITH BASIC RESOLUTION STEP
			 */

			/***
			 * OPTIMIZING
			 */

			int newatomcount = freshRule.getBody().size();
			
			joinEliminationPKBased(termidx, newatomcount, partialEvalution);

			/***
			 * DONE OPTIMIZING RETURN THE RESULT
			 */

			rulesGeneratedSoFar += 1;

			if (isSecondAtomOfLeftJoin && rulesGeneratedSoFar > 1) {
				/*
				 * We had disjunction on the second atom of the lejoin, that is,
				 * more than two rules that unified. LeftJoin is not
				 * distributable on the right component, hence, we cannot simply
				 * generate 2 rules for the seocnd atom.
				 * 
				 * The rules must be untouched, no partial evaluation is
				 * possible. We must return the original rule.
				 */
				return null;

			}

			result.add(partialEvalution);
		}// end for candidate matches

		return result;
	}

	private CQIE generateNullBindingsForLeftJoin(Function focusLiteral, CQIE originalRuleWithLeftJoin, Stack<Integer> termidx) {

		log.debug("Empty evaluation - Data Function {}", focusLiteral);

		CQIE freshRule = originalRuleWithLeftJoin.clone();
		// List<Function> body = freshRule.getBody();

		Stack<Integer> termidx1 = new Stack<Integer>();
		termidx1.addAll(termidx);

		termidx1.pop();
		termidx1.add(0);
		List<Function> innerAtoms = (List<Function>) getNestedList(termidx1, freshRule);

		int ArgumentAtoms = 0;
		List<Function> newbody = new LinkedList<Function>();
		HashSet<Variable> variablesArg1 = new LinkedHashSet<Variable>();
		HashSet<Variable> variablesArg2 = new LinkedHashSet<Variable>();

		// Here we build the new LJ body where we remove the 2nd
		// data atom
		for (Function atom : innerAtoms) {
			if (atom.isDataFunction() || atom.isAlgebraFunction()) {
				ArgumentAtoms++;
				// we found the first argument of the LJ, we need
				// the variables
				if (ArgumentAtoms == 1) {
					variablesArg1 = (HashSet<Variable>) atom.getReferencedVariables();
					newbody.add(atom);
				} else if (ArgumentAtoms != 2) {
					newbody.add(atom);
				} else {
					// Here we keep the variables of the second LJ
					// data argument
					variablesArg2 = (HashSet<Variable>) atom.getReferencedVariables();

					// and we remove the variables that are in both
					// arguments
					for (Variable var : variablesArg1) {
						if (variablesArg2.contains(var)) {
							variablesArg2.remove(var);
						}
					} // end for removing variables
					continue;
				}
			} else {
				newbody.add(atom);
			}

		}// end for rule body

		//freshRule.updateBody(newbody);
		replaceInnerLJ(freshRule, newbody, termidx1);
		HashMap<Variable, Term> unifier = new HashMap<Variable, Term>();

		OBDAVocabulary myNull = new OBDAVocabulary();
		for (Variable var : variablesArg2) {
			unifier.put(var, myNull.NULL);
		}
		// Now I need to add the null to the variables of the second
		// LJ data argument
		freshRule = Unifier.applyUnifier(freshRule, unifier, false);
		return freshRule;
	}
	
	private void replaceInnerLJ(CQIE rule, List<Function> replacementTerms,
			Stack<Integer> termidx) {
		Function parentFunction = null;
		if (termidx.size() > 1) {
			/*
			 * Its a nested term
			 */
			Term nestedTerm = null;
			for (int y = 0; y < termidx.size() - 1; y++) {
				int i = termidx.get(y);
				if (nestedTerm == null)
					nestedTerm = (Function) rule.getBody().get(i);
				else
				{
					parentFunction = (Function) nestedTerm;
					nestedTerm = ((Function) nestedTerm).getTerm(i);
				}
			}
			//Function focusFunction = (Function) nestedTerm;
			if (parentFunction == null) {
				//its just one Left Join, replace rule body directly
				rule.updateBody(replacementTerms);
				return;
			}
			List <Term> tempTerms = parentFunction.getTerms();
			tempTerms.remove(0);
			List <Term> newTerms = new LinkedList<Term>();
			newTerms.addAll(replacementTerms);
			newTerms.addAll(tempTerms);
			parentFunction.updateTerms(newTerms);
		} else {
			throw new RuntimeException("Unexpected OPTIONAL condition!");
		}
	}

	/***
	 * 
	 * We now take into account Primary Key constraints on the database to avoid
	 * adding redundant atoms to the query. This could also be done as an
	 * afterstep, using unification and CQC checks, however, its is much more
	 * expensive that way. Given a primary Key on A, on columns 1,2, and an atom
	 * A(x,y,z) added by the resolution engine (always added at the end of the
	 * CQ body), we will look for another atom A(x,y,z') if the atom exists, we
	 * can unify both atoms, apply the MGU to the query and remove one of the
	 * atoms.
	 * 
	 * 
	 * @param termidx
	 * 
	 * @param newatomcount
	 *            The number of new atoms introduced by this resolution step
	 *            (the body size of the fresh rule used for this resolution
	 *            step)
	 * 
	 * @param partialEvalution
	 *            The CQIE currently being optimized, i.e., the result of the
	 *            resolution step.
	 * 
	 * @param innerAtoms
	 */
	private void joinEliminationPKBased(Stack<Integer> termidx, int newatomcount, CQIE partialEvalution) {

		List innerAtoms = getNestedList(termidx, partialEvalution);

		Function currentAtom = getTerm(termidx, partialEvalution);
		
		
		if (currentAtom == null) {
			/*
			 * Case where the resolution atemt didn't add any atoms, the body was null.
			 */
			
			return;
		}

		int newatomsfirstIndex = termidx.peek();
		if (newatomsfirstIndex <= 0) {
			return;
		}
		for (int newatomidx = newatomsfirstIndex; newatomidx < newatomsfirstIndex + newatomcount; newatomidx++) {

			Function newatom = (Function) innerAtoms.get(newatomidx);
			if (!newatom.isDataFunction())
				continue;

			List<Integer> pkey = primaryKeys.get(newatom.getFunctionSymbol());
			if (!(pkey != null && !pkey.isEmpty())) {
				// no pkeys for this predicate
				continue;
			}
			/*
			 * the predicate has a primary key, looking for candidates for
			 * unification, when we find one we can stop, since the application
			 * of this optimization at each step of the derivation tree
			 * guarantees there wont be any other redundant atom.
			 */
			Function replacement = null;

			Map<Variable, Term> mgu1 = null;
			for (int idx2 = 0; idx2 < termidx.peek(); idx2++) {
				Function tempatom = (Function) innerAtoms.get(idx2);

				if (!tempatom.getFunctionSymbol().equals(newatom.getFunctionSymbol())) {
					/*
					 * predicates are different, atoms cant be unified
					 */
					continue;
				}

				boolean redundant = true;
				for (Integer termidx2 : pkey) {
					if (!newatom.getTerm(termidx2 - 1).equals(tempatom.getTerm(termidx2 - 1))) {
						redundant = false;
						break;
					}
				}

				if (redundant) {
					/* found a candidate replacement atom */
					mgu1 = Unifier.getMGU(newatom, tempatom);
					if (mgu1 != null) {
						replacement = tempatom;
						break;
					}
				}

			}

			if (replacement == null)
				continue;

			if (mgu1 == null)
				throw new RuntimeException("Unexcpected case found while performing JOIN elimination. Contact the authors for debugging.");

			if (currentAtom.isAlgebraFunction() && currentAtom.getFunctionSymbol().equals(OBDAVocabulary.SPARQL_LEFTJOIN)) {
				continue;
			}

			Unifier.applyUnifier(partialEvalution, mgu1, false);
			innerAtoms.remove(newatomidx);
			newatomidx -= 1;
			newatomcount -= 1;

		}

		/***
		 * As the result of optimizing PKs, it can be that JOINs become invalid,
		 * i.e., they contian one single data item (no longer a join). In this
		 * case we need to eliminate the join atom attach the inner atoms to the
		 * parent of the join (body or another join/leftjoin). This is done with
		 * the normalizer.
		 */

		int dataAtoms = DatalogNormalizer.countDataItems(innerAtoms);
		if (dataAtoms == 1)
			cleanJoins(partialEvalution);

	}

	private static void eliminateRedundantISNOTNULL(CQIE partialEvaluation) {
	}

	/***
	 * Goes through a list of atoms (indicated by the termidx) removing any
	 * duplicated ISNOTNULL atoms.
	 * 
	 * @param termidx
	 * @param partialEvalution
	 */
	private static void eliminateRedundantISNOTNULL(Stack<Integer> termidx, CQIE partialEvalution) {
		{
			List innerAtoms = getNestedList(termidx, partialEvalution);
			Function currentAtom = getTerm(termidx, partialEvalution);

			int newatomsfirstIndex = termidx.peek();
			if (newatomsfirstIndex <= 0) {
				return;
			}
			for (int newatomidx = 0; newatomidx < innerAtoms.size(); newatomidx++) {

				Function newatom = (Function) innerAtoms.get(newatomidx);
				if (!newatom.isBooleanFunction())
					continue;

				if (!newatom.getPredicate().equals(OBDAVocabulary.IS_NOT_NULL))
					continue;

				Function replacement = null;

				for (int idx2 = 0; idx2 < termidx.peek(); idx2++) {
					Function tempatom = (Function) innerAtoms.get(idx2);
					if (!tempatom.equals(newatom))
						continue;

					replacement = tempatom;
					break;

				}

				if (replacement == null)
					continue;

				innerAtoms.remove(newatomidx);
				newatomidx -= 1;
			}
		}

	}

	/***
	 * Eliminates Join atoms when they only have one data atom, i.e., they are
	 * not really JOINs
	 */
	private void cleanJoins(CQIE query) {
		DatalogNormalizer.unfoldJoinTrees(query, false);
	}

	/***
	 * Returns the list of terms contained in the nested atom indicated by term
	 * idx. If termidx is empty, then this is the list of atoms in the body of
	 * the rule, otherwise the list correspond to the terms of the nested atom
	 * indicated by termidx viewed as a path of atoms. For example, if termidx =
	 * <2,4> then this atom returns the list of terms of the 4 atom, of the
	 * second atom in the body of the rule.
	 * 
	 * <p>
	 * Example two. IF the rule is q(x):-A(x), Join(R(x,y), Join(P(s),R(x,y) and
	 * termidx = <1,2>, then this method returns the the terms of the second
	 * join atom, ie.,
	 * <P(s),R(x,y)>
	 * ,
	 * 
	 * <p>
	 * note that this list is the actual list of terms of the atom, so
	 * manipulating the list will change the atom.
	 * 
	 * 
	 * @param termidx
	 * @param rule
	 * @return
	 */
	private static List getNestedList(Stack<Integer> termidx, CQIE rule) {
		List innerTerms = null;

		if (termidx.size() > 1) {
			/*
			 * Its a nested term
			 */
			Term nestedTerm = null;
			for (int y = 0; y < termidx.size() - 1; y++) {
				int i = termidx.get(y);
				if (nestedTerm == null)
					nestedTerm = (Function) rule.getBody().get(i);
				else
					nestedTerm = ((Function) nestedTerm).getTerm(i);
			}
			Function newfocusFunction = (Function) nestedTerm;

			innerTerms = newfocusFunction.getTerms();
		} else {
			/*
			 * Its directly on the body of the query
			 */
			innerTerms = rule.getBody();
		}
		return innerTerms;
	}

	private static Function getTerm(Stack<Integer> termidx, CQIE rule) {
		Function atom = null;
		if (termidx.size() > 1) {
			Stack stack = new Stack();
			stack.addAll(termidx.subList(0, termidx.size() - 1));
			List innerTerms = getNestedList(stack, rule);
			atom = (Function) innerTerms.get((Integer) stack.peek());
		} else {
			List<Function> body = rule.getBody();
			Integer peek = (Integer) termidx.peek();
			if (body.size() == 0 || peek >= body.size()) 
				return null;
			
			atom = (Function) body.get(peek);
		}
		return atom;
	}

}
