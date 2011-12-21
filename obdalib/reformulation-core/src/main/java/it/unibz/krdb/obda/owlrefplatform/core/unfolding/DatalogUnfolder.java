package it.unibz.krdb.obda.owlrefplatform.core.unfolding;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.AnonymousVariable;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.VariableImpl;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQCUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.ResolutionEngine;
import it.unibz.krdb.obda.utils.QueryUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatalogUnfolder implements UnfoldingMechanism {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6088558456135748487L;

	private DatalogProgram unfoldingProgram;

	private static final OBDADataFactory termFactory = OBDADataFactoryImpl.getInstance();

	private static final ResolutionEngine resolutionEngine = new ResolutionEngine();

	private static final Logger log = LoggerFactory.getLogger(ComplexMappingUnfolder.class);

	URIToFunctionMatcher uriFunctorMatcher;

	private enum UnfoldingMode {
		UCQ, DATALOG
	};

	private UnfoldingMode unfoldingMode = UnfoldingMode.UCQ;

	private HashMap<String, Function> functTermMap;

	// TODO deal with virtual mode and URI's in a proper way
	public DatalogUnfolder(DatalogProgram unfoldingProgram) throws Exception {



		this.unfoldingProgram = unfoldingProgram;

		
		/* Collecting all the function symbols in the auxiliary program */
		functTermMap = new HashMap<String, Function>();
		for (CQIE mappingrule : unfoldingProgram.getRules()) {
			/* Collecting functional terms */
			List<Term> headTerms = mappingrule.getHead().getTerms();
			for (Term term : headTerms) {
				if (term instanceof FunctionalTermImpl) {
					FunctionalTermImpl function = (FunctionalTermImpl) term;
					String predicateName = function.getFunctionSymbol().getName().toString();
					if (functTermMap.containsKey(predicateName)) {
						/*
						 * We found an existing fucntional term that has the
						 * same predicate, these must have the same airity,
						 * otherwise we cannt match URI constants with
						 * functional terms.
						 */
						Function existing = functTermMap.get(predicateName);
						if (function.getTerms().size() != existing.getTerms().size())
							throw new Exception("The model contains two URI building functions with the same base but different arity");
					}
					functTermMap.put(predicateName, function);
				}
			}
		}
		uriFunctorMatcher = new URIToFunctionMatcher(functTermMap);

	}

	/***
	 * Computes a UCQ unfolding. This is will resolve each atom of each CQ in
	 * the input with the rules of the unfolding program. The original queries
	 * get removed from the result, and instead the result of the resolution
	 * step is kept. For example
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
	 * Unfolding:<br>
	 * A(x1) :- table1(x1,y1),table3(x1,z2)<br>
	 * A(x1) :- table2(x1,z1),table3(x1,z2)<br>
	 * 
	 * @param inputquery
	 * @return
	 */
	private DatalogProgram unfoldToUCQ(DatalogProgram inputquery) throws Exception {

		LinkedList<CQIE> evaluation = new LinkedList<CQIE>();
		Iterator<CQIE> qit = inputquery.getRules().iterator();
		int maxlenght = 0;

		/*
		 * Finding the longest query, we will try to unfold from the atom that
		 * is farther away
		 */

		while (qit.hasNext()) {
			CQIE query = qit.next();
			if (query.getBody().size() > maxlenght) {// find longest query
				maxlenght = query.getBody().size();
			}
			evaluation.add(query);
		}

		int pos = 0;

		LinkedList<CQIE> partialEvaluation = new LinkedList<CQIE>();
		partialEvaluation.addAll(inputquery.getRules());
		LinkedList<CQIE> newPartialEvaluation = null;
		while (pos < maxlenght) {
			newPartialEvaluation = new LinkedList<CQIE>();
			// TODO change so that only queries of relevant size are unfolded
			for (CQIE currentQuery : partialEvaluation) {
				if (pos < currentQuery.getBody().size()) {
					List<CQIE> currentPartialEvaluation = unfoldAtom(pos, currentQuery);
					// newPartialEvaluation.addAll(CQCUtilities.removeDuplicateAtoms(currentPartialEvaluation));
					newPartialEvaluation.addAll(currentPartialEvaluation);
				} else {
					newPartialEvaluation.add(currentQuery);
				}
			}
			pos++;
			partialEvaluation.clear();
			partialEvaluation.addAll(newPartialEvaluation);
			// CQCUtilities.removeContainedQueriesSyntacticSorter(partialEvaluation,
			// false);

		}
		HashSet<CQIE> cleanset = CQCUtilities.removeDuplicateAtoms(partialEvaluation);
		partialEvaluation.clear();
		partialEvaluation.addAll(cleanset);
		CQCUtilities.removeContainedQueriesSyntacticSorter(partialEvaluation, true);

		return termFactory.getDatalogProgram(partialEvaluation);
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
			for (Atom atom : cq.getBody()) {
				for (CQIE rule : unfoldingProgram.getRules(atom.getPredicate())) {
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
	public DatalogProgram unfold(DatalogProgram inputquery) throws Exception {

		log.debug("Unfolding mode: {}. Initial query size: {}", unfoldingMode, inputquery.getRules().size());

		inputquery = replaceURIsForFunctions(inputquery);

		long startime = System.nanoTime();

		// log.debug("Computing partial evaluation for: \n{}", inputquery);
		deAnonymize(inputquery);

		DatalogProgram partialEvaluation = null;
		if (unfoldingMode == UnfoldingMode.UCQ) {
			partialEvaluation = unfoldToUCQ(inputquery);
		} else if (unfoldingMode == UnfoldingMode.DATALOG) {
			partialEvaluation = unfoldToDatalog(inputquery);
		} else {
			throw new RuntimeException("Unknown unfolding mode");
		}

		DatalogProgram dp = termFactory.getDatalogProgram();
		QueryUtils.copyQueryModifiers(inputquery, dp);
		dp.appendRule(partialEvaluation.getRules());

		// log.debug("Computed partial evaluation: \n{}", dp);
		long endtime = System.nanoTime();
		long timeelapsedseconds = (endtime - startime) / 1000000;
		log.debug("Result size: {}   Time elapsed: {} ms", dp.getRules().size(), timeelapsedseconds);
		return dp;
	}

	private List<CQIE> unfoldAtom(int pos, CQIE currentQuery) {
		LinkedList<CQIE> partialEvaluations = new LinkedList<CQIE>();

		if (pos >= currentQuery.getBody().size())
			return partialEvaluations;

		Atom atom = (Atom) currentQuery.getBody().get(pos);

		Predicate atomPredicate = atom.getPredicate();

		List<CQIE> ruleList = unfoldingProgram.getRules(atomPredicate);

		for (CQIE mappingRule : ruleList) {
			CQIE freshMappingRule = getFreshRule(mappingRule, pos);

			CQIE pev = resolutionEngine.resolve(freshMappingRule, currentQuery, pos);
			if (pev != null) {
				partialEvaluations.add(pev.clone());
			}
		}

		return partialEvaluations;
	}

	/***
	 * Replaces each variable 'v' in the query for a new variable constructed
	 * using the name of the original variable plus the counter. For example
	 * 
	 * q(x) :- C(x)
	 * 
	 * results in
	 * 
	 * q(x_1) :- C(x_1)
	 * 
	 * if counter = 1.
	 * 
	 * This method can be used to generate "fresh" rules from a datalog program
	 * that is going to be used during a resolution procedure.
	 * 
	 * @param rule
	 * @param count
	 * @return
	 */
	public CQIE getFreshRule(CQIE rule, int count) {
		// This method doesn't support nested functional terms
		CQIE freshRule = rule.clone();
		Atom head = freshRule.getHead();
		List<Term> headTerms = head.getTerms();
		for (int i = 0; i < headTerms.size(); i++) {
			Term term = headTerms.get(i);
			Term newTerm = null;
			if (term instanceof VariableImpl) {
				VariableImpl variable = (VariableImpl) term;
				newTerm = termFactory.getVariable(variable.getName() + "_" + count);
			} else if (term instanceof FunctionalTermImpl) {
				FunctionalTermImpl functionalTerm = (FunctionalTermImpl) term;
				List<Term> innerTerms = functionalTerm.getTerms();
				List<Term> newInnerTerms = new LinkedList<Term>();
				for (int j = 0; j < innerTerms.size(); j++) {
					Term innerTerm = innerTerms.get(j);
					if (innerTerm instanceof VariableImpl) {
						newInnerTerms.add(termFactory.getVariable(((Variable) innerTerm).getName() + "_" + count));
					} else {
						newInnerTerms.add(innerTerm.clone());
					}
				}
				Predicate newFunctionSymbol = functionalTerm.getFunctionSymbol();
				FunctionalTermImpl newFunctionalTerm = (FunctionalTermImpl) termFactory.getFunctionalTerm(newFunctionSymbol, newInnerTerms);
				newTerm = newFunctionalTerm;
			}
			if (newTerm != null)
				headTerms.set(i, newTerm);
		}

		List<Atom> body = freshRule.getBody();
		for (Atom atom : body) {

			List<Term> atomTerms = ((Atom) atom).getTerms();
			for (int i = 0; i < atomTerms.size(); i++) {
				Term term = atomTerms.get(i);
				Term newTerm = null;
				if (term instanceof VariableImpl) {
					VariableImpl variable = (VariableImpl) term;
					newTerm = termFactory.getVariable(variable.getName() + "_" + count);
				} else if (term instanceof Function) {
					Function functionalTerm = (Function) term;
					List<Term> innerTerms = functionalTerm.getTerms();
					List<Term> newInnerTerms = new LinkedList<Term>();
					for (int j = 0; j < innerTerms.size(); j++) {
						Term innerTerm = innerTerms.get(j);
						if (innerTerm instanceof Variable) {
							newInnerTerms.add(termFactory.getVariable(((Variable) innerTerm).getName() + "_" + count));
						} else {
							newInnerTerms.add(innerTerm.clone());
						}
					}
					Predicate newFunctionSymbol = functionalTerm.getFunctionSymbol();
					Function newFunctionalTerm = (Function) termFactory.getFunctionalTerm(newFunctionSymbol, newInnerTerms);
					newTerm = newFunctionalTerm;
				}
				if (newTerm != null)
					atomTerms.set(i, newTerm);
			}
		}
		return freshRule;

	}

	/**
	 * method that enumerates all undistinguished variables in the given data
	 * log program. This will also remove any instances of
	 * UndisinguishedVariable and replace them by instance of Variable
	 * (enumerated as mentioned before). This step is needed to ensure that the
	 * algorithm treats each undistinguished variable as a unique variable.
	 * 
	 * @param dp
	 */
	private void deAnonymize(DatalogProgram dp) {

		Iterator<CQIE> it = dp.getRules().iterator();
		while (it.hasNext()) {
			CQIE query = it.next();
			Atom head = query.getHead();
			Iterator<Term> hit = head.getTerms().iterator();
			OBDADataFactory factory = OBDADataFactoryImpl.getInstance();
			int coutner = 1;
			int i = 0;
			LinkedList<Term> newTerms = new LinkedList<Term>();
			while (hit.hasNext()) {
				Term t = hit.next();
				if (t instanceof AnonymousVariable) {
					String newName = "_uv-" + coutner;
					coutner++;
					Term newT = factory.getVariable(newName);
					newTerms.add(newT);
				} else {
					newTerms.add(t.clone());
				}
				i++;
			}
			head.updateTerms(newTerms);

			Iterator<Atom> bit = query.getBody().iterator();
			while (bit.hasNext()) {
				Atom a = (Atom) bit.next();
				Iterator<Term> hit2 = a.getTerms().iterator();
				i = 0;
				LinkedList<Term> vec = new LinkedList<Term>();
				while (hit2.hasNext()) {
					Term t = hit2.next();
					if (t instanceof AnonymousVariable) {
						String newName = "_uv-" + coutner;
						coutner++;
						Term newT = factory.getVariable(newName);
						vec.add(newT);
					} else {
						vec.add(t.clone());
					}
					i++;
				}
				a.updateTerms(vec);
			}
		}
	}

	/***
	 * Produces a new program in which all URI constnats have been replaced by
	 * grounded functions that match the URI. Any URI constant that was not
	 * matched will remain in the output.
	 * 
	 * @param dp
	 */
	private DatalogProgram replaceURIsForFunctions(DatalogProgram dp) {
		List<CQIE> rules = dp.getRules();
		List<CQIE> newrules = new LinkedList<CQIE>();

		for (CQIE rule : rules) {
			List<Atom> body = rule.getBody();
			List<Atom> newbody = new LinkedList<Atom>();
			for (int i = 0; i < body.size(); i++) {
				Atom atom = (Atom) body.get(i);
				List<Term> terms = atom.getTerms();
				List<Term> newTerms = new LinkedList<Term>();
				for (Term t : terms) {
					if (t instanceof URIConstant) {
						URIConstant uri = (URIConstant) t;
						Function matchedFunction = this.uriFunctorMatcher.getPossibleFunctionalTermMatch(uri);
						if (matchedFunction != null)
							newTerms.add(matchedFunction);
						else
							newTerms.add(t);
					} else {
						newTerms.add(t);
					}
				}
				Atom newatom = termFactory.getAtom(atom.getPredicate(), newTerms);
				newbody.add(newatom);
			}
			CQIE newrule = termFactory.getCQIE(rule.getHead(), newbody);
			newrules.add(newrule);
		}
		DatalogProgram newprogram = termFactory.getDatalogProgram();
		newprogram.appendRule(newrules);
		QueryUtils.copyQueryModifiers(dp, newprogram);
		return newprogram;
	}
}
