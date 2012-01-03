package it.unibz.krdb.obda.owlrefplatform.core.unfolding;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.BooleanOperationPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OperationPredicate;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.AnonymousVariable;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.VariableImpl;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.ResolutionEngine;
import it.unibz.krdb.obda.utils.QueryUtils;
import it.unibz.krdb.sql.DBMetadata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
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

	private DBMetadata metadata;

	// TODO deal with virtual mode and URI's in a proper way
	public DatalogUnfolder(DatalogProgram unfoldingProgram, DBMetadata metadata) throws Exception {
		this.metadata = metadata;
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
	 * get removed from the result if their bodies contain atoms that have no
	 * matching rule the unfolding program.
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
	 * queries and attempt to unfold atom 0 in the body. The resolution engine
	 * will generate 1 or more CQs as result. The original atom is removed, and
	 * the results are appended to the end of each query (hence its always safe
	 * to unfold atom 0). The new queries are kept for a next cycle. We stop
	 * when no new queries are produced.
	 * 
	 * <p>
	 * We need to extend this procedure optimizations to avoid unnecessary self
	 * joins and redundant CQs.
	 * 
	 * @param inputquery
	 * @return
	 */
	private DatalogProgram unfoldToUCQ(DatalogProgram inputquery) throws Exception {

		LinkedHashSet<CQIE> evaluation = new LinkedHashSet<CQIE>();
		evaluation.addAll(inputquery.getRules());

		LinkedHashSet<CQIE> workingSet = new LinkedHashSet<CQIE>();
		workingSet.addAll(inputquery.getRules());

		int resolutionCount = 0;

		while (true) {

			LinkedHashSet<CQIE> newqueries = new LinkedHashSet<CQIE>();
			LinkedHashSet<CQIE> toRemove = new LinkedHashSet<CQIE>();

			for (CQIE currentQuery : workingSet) {
				resolutionCount += 1;

				/*
				 * We will always try to resolve the first atom in the body of
				 * the query
				 */

				List<CQIE> currentPartialEvaluation = unfoldAtom(0, currentQuery, resolutionCount);
				for (CQIE pe : currentPartialEvaluation) {
					newqueries.add(pe);
				}

				/*
				 * Trying to find out if we can remove the original query. This
				 * will be true if the original query contains a predicate atom
				 * that is not a DB predicate
				 */
				if (!isDBQuery(currentQuery)) {
					toRemove.add(currentQuery);
				}

			}

			LinkedHashSet<CQIE> inserted = new LinkedHashSet<CQIE>();
			for (CQIE newquery : newqueries) {
				boolean isnew = evaluation.add(newquery);
				if (isnew) {
					inserted.add(newquery);
				}
			}
			evaluation.removeAll(toRemove);
			if (!inserted.isEmpty()) {
				workingSet = inserted;
			} else {
				break;
			}
		}

		List<CQIE> result = new LinkedList<CQIE>();
		result.addAll(evaluation);
		return termFactory.getDatalogProgram(result);
	}

	/***
	 * Indicates if this query is a DB query, that is, a query whose body
	 * contains only atoms that are either
	 * <p>
	 * <list>
	 * <li>Boolean operator atoms, e.g., equality, greather than, etc</li><br>
	 * <li>have a corresponding rule in the unfoldingProgram of this
	 * DatalogUnfolder.</li><br/>
	 * </list>
	 * 
	 * @param cqie
	 * @return
	 */
	public boolean isDBQuery(CQIE cqie) {
		boolean isDBQuery = true;
		for (Atom atom : cqie.getBody()) {
			Predicate predicate = atom.getPredicate();
			if (predicate instanceof BooleanOperationPredicate)
				continue;
			if (metadata.getDefinition(predicate.getName().toString()) == null) {
				isDBQuery = false;
				break;
			}

		}
		return isDBQuery;
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
		log.debug("Unfolding size: {}   Time elapsed: {} ms", dp.getRules().size(), timeelapsedseconds);
		return dp;
	}

	/***
	 * Unfolds the atom in position pos. The procedure will remove the atom in
	 * pos from the query, an for each rule in the unfoldingProgram that matches
	 * the predicate of the atom, it will generate a new query in which the body
	 * of the matching rule is appended to the input CQIE.
	 * 
	 * 
	 * @param pos
	 * @param currentQuery
	 * @return
	 */
	private List<CQIE> unfoldAtom(int pos, CQIE currentQuery, int count) {
		LinkedList<CQIE> partialEvaluations = new LinkedList<CQIE>();

		if (pos >= currentQuery.getBody().size())
			return partialEvaluations;

		/* Do not unfold operator atoms */
		Atom atom = (Atom) currentQuery.getBody().get(pos);
		if (atom.getPredicate() instanceof OperationPredicate) {
			currentQuery.getBody().remove(pos);
			currentQuery.getBody().add(atom);
			partialEvaluations.add(currentQuery);
			return partialEvaluations;
		}

		Predicate atomPredicate = atom.getPredicate();

		List<CQIE> ruleList = unfoldingProgram.getRules(atomPredicate);

		for (CQIE mappingRule : ruleList) {
			CQIE freshMappingRule = getFreshRule(mappingRule, count);

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
	 * @param suffix
	 * @return
	 */
	public CQIE getFreshRule(CQIE rule, int suffix) {
		// This method doesn't support nested functional terms
		CQIE freshRule = rule.clone();
		Atom head = freshRule.getHead();
		List<Term> headTerms = head.getTerms();
		for (int i = 0; i < headTerms.size(); i++) {
			Term term = headTerms.get(i);
			Term newTerm = getFreshTerm(term, suffix);
			if (newTerm != null)
				headTerms.set(i, newTerm);
		}

		List<Atom> body = freshRule.getBody();
		for (Atom atom : body) {

			List<Term> atomTerms = ((Atom) atom).getTerms();
			for (int i = 0; i < atomTerms.size(); i++) {
				Term term = atomTerms.get(i);
				Term newTerm = getFreshTerm(term, suffix);
				if (newTerm != null)
					atomTerms.set(i, newTerm);
			}
		}
		return freshRule;

	}

	public Term getFreshTerm(Term term, int suffix) {
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
			Function newFunctionalTerm = (Function) termFactory.getFunctionalTerm(newFunctionSymbol, newInnerTerms);
			newTerm = newFunctionalTerm;
		} else if (term instanceof Constant) {
			newTerm = term.clone();
		} else {
			throw new RuntimeException("Unsupported term: " + term);
		}
		return newTerm;

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
