package org.obda.owlrefplatform.core.unfolding;

import inf.unibz.it.obda.model.Atom;
import inf.unibz.it.obda.model.CQIE;
import inf.unibz.it.obda.model.DatalogProgram;
import inf.unibz.it.obda.model.Function;
import inf.unibz.it.obda.model.OBDADataFactory;
import inf.unibz.it.obda.model.OBDAMappingAxiom;
import inf.unibz.it.obda.model.Predicate;
import inf.unibz.it.obda.model.Term;
import inf.unibz.it.obda.model.URIConstant;
import inf.unibz.it.obda.model.impl.AtomImpl;
import inf.unibz.it.obda.model.impl.CQIEImpl;
import inf.unibz.it.obda.model.impl.DatalogProgramImpl;
import inf.unibz.it.obda.model.impl.FunctionalTermImpl;
import inf.unibz.it.obda.model.impl.OBDADataFactoryImpl;
import inf.unibz.it.obda.model.impl.OperatorAtom;
import inf.unibz.it.obda.model.impl.RDBMSOBDAMappingAxiom;
import inf.unibz.it.obda.model.impl.UndistinguishedVariable;
import inf.unibz.it.obda.model.impl.VariableImpl;
import inf.unibz.it.obda.utils.QueryUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obda.owlrefplatform.core.basicoperations.CQCUtilities;
import org.obda.owlrefplatform.core.basicoperations.ResolutionEngine;
import org.obda.owlrefplatform.core.viewmanager.AuxSQLMapping;
import org.obda.owlrefplatform.core.viewmanager.MappingViewManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements the partial evaluation algorithm with obda mappings
 * 
 * @author Manfred Gerstgrasser
 * 
 */

public class ComplexMappingUnfolder implements UnfoldingMechanism {

	List<OBDAMappingAxiom>			mappings			= null;
	Set<OBDAMappingAxiom>			splitMappings		= null;

	// private Map<String, LinkedList<OBDAMappingAxiom>> mappingsIndex = null;

	private MappingViewManager		viewManager			= null;
	private OBDADataFactory			termFactory			= null;
	private ResolutionEngine		resolutionEngine	= null;

	/*
	 * A program that has rules of the form C(p(x)) :- Aux(x) to be used for the
	 * computation of partial evaluations
	 */
	DatalogProgram					compilationOfM		= null;

	Logger							log					= LoggerFactory.getLogger(ComplexMappingUnfolder.class);

	private Map<String, Function>	functTermMap		= null;

	URIToFunctionMatcher			uriFunctorMatcher;

	/***
	 * 
	 * Exception if it recieves a list of mappigs in which two function terms
	 * are defined such that they have the same predicate name, but different
	 * arities.
	 * 
	 * @param mappings
	 * @param man
	 */
	public ComplexMappingUnfolder(List<OBDAMappingAxiom> mappings, MappingViewManager man) throws Exception {

		log.debug("Setting up ComplexMappingUnfolder");
		log.debug("Mappings recreived: {}", mappings.size());

		this.mappings = mappings;
		this.termFactory = OBDADataFactoryImpl.getInstance();
		this.viewManager = man;
		resolutionEngine = new ResolutionEngine();
		functTermMap = new HashMap<String, Function>();

		splitMappings = new HashSet<OBDAMappingAxiom>();
		// mappingsIndex = new HashMap<String, LinkedList<OBDAMappingAxiom>>();

		Iterator<OBDAMappingAxiom> mappingsIterator = mappings.iterator();
		int counter = 0;

		log.debug("Splitting mappings");
		// splitting mappings
		while (mappingsIterator.hasNext()) {
			OBDAMappingAxiom map = mappingsIterator.next();
			CQIE cq = (CQIEImpl) map.getTargetQuery();
			Atom head = cq.getHead();
			List<Atom> body = cq.getBody();
			Iterator<Atom> bit = body.iterator();
			while (bit.hasNext()) {
				Atom atom = bit.next();
				List<Atom> newBody = new LinkedList<Atom>();
				newBody.add(atom);
				CQIE newCQ = new CQIEImpl(head, newBody, false);
				RDBMSOBDAMappingAxiom newMap = new RDBMSOBDAMappingAxiom(map.getId() + "_" + counter);
				newMap.setSourceQuery(map.getSourceQuery());
				newMap.setTargetQuery(newCQ);
				splitMappings.add(newMap);
			}
		}

		log.debug("Mappings after split: {}", splitMappings.size());

		/* Creating the compilation of the mappings */
		log.debug("Computing the compilation of the mappings");
		DatalogProgram compilationOfM = new DatalogProgramImpl();
		for (OBDAMappingAxiom mapping : splitMappings) {
			CQIE mappingrule = getRule(mapping);
			compilationOfM.appendRule(mappingrule);
			log.debug("Rule generated: {}", mappingrule);

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
							throw new Exception("The model contains to function terms that have the same base but different arity");
					}
					functTermMap.put(predicateName, function);
				}
			}
		}

		uriFunctorMatcher = new URIToFunctionMatcher(functTermMap);

		/* Collecting all the function symbols in the auxiliary program */

		this.compilationOfM = compilationOfM;

	}

	/***
	 * Creates a rule for a mapping at can be used to create the program to be
	 * used for the computatino of partial evaluations.
	 * 
	 * Given a Mapping sql1 -> C(p(x))
	 * 
	 * This method will return a rule
	 * 
	 * C(p(aux_1) :- Aux(aux_1, aux_2)
	 * 
	 * Where Aux is the auxiliary predicate assocaited to the view for the SQL
	 * query sql1.
	 * 
	 * @param mapping
	 * @return
	 */
	public CQIE getRule(OBDAMappingAxiom mapping) {
		Predicate viewPredicate = viewManager.getViewName(mapping.getSourceQuery().toString());
		AuxSQLMapping auxmap = viewManager.getAuxSQLMapping(viewPredicate.getName());

		/* Creating the atom for the body */
		LinkedList<Term> bodyTerms = new LinkedList<Term>();
		String name = viewPredicate.getName().getFragment().toLowerCase() + "_";
		for (int i = 0; i < auxmap.getNrOfVariables(); i++) {
			bodyTerms.add(termFactory.createVariable(name + i));
		}

		Atom ruleBodyAtom = new AtomImpl(viewPredicate, bodyTerms);
		LinkedList<Atom> ruleBody = new LinkedList<Atom>();
		ruleBody.add(ruleBodyAtom);

		/* Creating the atom for the head */

		CQIEImpl mappingsTargetQuery = (CQIEImpl) mapping.getTargetQuery();
		Atom mappingsBodyAtom = mappingsTargetQuery.getBody().get(0);
		/*
		 * Example: Person (c(x))
		 */

		LinkedList<Term> headTerms = new LinkedList<Term>();
		for (int i = 0; i < mappingsBodyAtom.getTerms().size(); i++) {
			Term currentTerm = mappingsBodyAtom.getTerms().get(i);
			if (currentTerm instanceof FunctionalTermImpl) {
				FunctionalTermImpl ft = (FunctionalTermImpl) currentTerm;
				List<Term> innerTerms = ft.getTerms();
				Iterator<Term> innerTermsIterator = innerTerms.iterator();
				LinkedList<Term> funvec = new LinkedList<Term>();
				while (innerTermsIterator.hasNext()) {
					Term v = innerTermsIterator.next();
					int pos = auxmap.getPosOf(v.getName());
					Term t = termFactory.createVariable(ruleBodyAtom.getTerms().get(pos).getName());
					funvec.add(t);
				}
				Term t = termFactory.createFunctionalTerm(ft.getFunctionSymbol(), funvec);
				headTerms.add(t);
			} else {
				String n = currentTerm.getName();
				int pos = auxmap.getPosOf(n);
				Term t = termFactory.createVariable(ruleBodyAtom.getTerms().get(pos).getName());
				headTerms.add(t);
			}
		}

		Atom ruleHead = new AtomImpl(mappingsBodyAtom.getPredicate(), headTerms);

		CQIE mappingRule = new CQIEImpl(ruleHead, ruleBody, false);

		return mappingRule;
	}

	@Override
	public DatalogProgram unfold(DatalogProgram inputquery) throws Exception {
		log.info("Computing unfolding for query of size: {}", inputquery.getRules().size());
		long startime = System.currentTimeMillis();

		log.debug("Computing partial evaluation for: \n{}", inputquery);
		deAnonymize(inputquery);

		log.debug("Replacing any URI constants for function symbols if there are matching ones.");
		inputquery = replaceURIsForFunctions(inputquery);

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

		DatalogProgram dp = new DatalogProgramImpl();

		QueryUtils.copyQueryModifiers(inputquery, dp);

		dp.appendRule(partialEvaluation);

		log.debug("Computed partial evaluation: \n{}", dp);
		long endtime = System.currentTimeMillis();
		long timeelapsedseconds = (endtime - startime) / 1000;
		log.info("Final size of unfolding: {}   Time elapsed: {}s", dp.getRules().size(), timeelapsedseconds);
		return dp;
	}

	private List<CQIE> unfoldAtom(int pos, CQIE currentQuery) {
		LinkedList<CQIE> partialEvaluations = new LinkedList<CQIE>();

		if (pos >= currentQuery.getBody().size())
			return partialEvaluations;

		Atom atom = currentQuery.getBody().get(pos);

		if (atom instanceof OperatorAtom)
			throw new RuntimeException("ComplexMappingUnfolder: Attempting to unfold an operator atom");

		Predicate atomPredicate = atom.getPredicate();

		List<CQIE> ruleList = compilationOfM.getRules(atomPredicate);

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
				newTerm = termFactory.createVariable(variable.getName() + "_" + count);
			} else if (term instanceof FunctionalTermImpl) {
				FunctionalTermImpl functionalTerm = (FunctionalTermImpl) term;
				List<Term> innerTerms = functionalTerm.getTerms();
				List<Term> newInnerTerms = new LinkedList<Term>();
				for (int j = 0; j < innerTerms.size(); j++) {
					Term innerTerm = innerTerms.get(j);
					if (innerTerm instanceof VariableImpl) {
						newInnerTerms.add(termFactory.createVariable(innerTerm.getName() + "_" + count));
					} else {
						newInnerTerms.add(innerTerm.copy());
					}
				}
				Predicate newFunctionSymbol = functionalTerm.getFunctionSymbol();
				FunctionalTermImpl newFunctionalTerm = (FunctionalTermImpl) termFactory.createFunctionalTerm(newFunctionSymbol,
						newInnerTerms);
				newTerm = newFunctionalTerm;
			}
			if (newTerm != null)
				headTerms.set(i, newTerm);
		}

		List<Atom> body = freshRule.getBody();
		for (Atom atom : body) {

			List<Term> atomTerms = atom.getTerms();
			for (int i = 0; i < atomTerms.size(); i++) {
				Term term = atomTerms.get(i);
				Term newTerm = null;
				if (term instanceof VariableImpl) {
					VariableImpl variable = (VariableImpl) term;
					newTerm = termFactory.createVariable(variable.getName() + "_" + count);
				} else if (term instanceof FunctionalTermImpl) {
					FunctionalTermImpl functionalTerm = (FunctionalTermImpl) term;
					List<Term> innerTerms = functionalTerm.getTerms();
					List<Term> newInnerTerms = new LinkedList<Term>();
					for (int j = 0; j < innerTerms.size(); j++) {
						Term innerTerm = innerTerms.get(j);
						if (innerTerm instanceof VariableImpl) {
							newInnerTerms.add(termFactory.createVariable(innerTerm.getName() + "_" + count));
						} else {
							newInnerTerms.add(innerTerm.copy());
						}
					}
					Predicate newFunctionSymbol = functionalTerm.getFunctionSymbol();
					FunctionalTermImpl newFunctionalTerm = (FunctionalTermImpl) termFactory.createFunctionalTerm(newFunctionSymbol,
							newInnerTerms);
					newTerm = newFunctionalTerm;
				}
				if (newTerm != null)
					atomTerms.set(i, newTerm);
			}
		}
		return freshRule;

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
				Atom atom = body.get(i);
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
				Atom newatom = new AtomImpl(atom.getPredicate(), newTerms);
				newbody.add(newatom);
			}
			CQIE newrule = new CQIEImpl(rule.getHead(), newbody, rule.isBoolean());
			newrules.add(newrule);
		}
		DatalogProgram newprogram = new DatalogProgramImpl();
		newprogram.appendRule(newrules);
		QueryUtils.copyQueryModifiers(dp, newprogram);
		return newprogram;
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
				if (t instanceof UndistinguishedVariable) {
					String newName = "_uv-" + coutner;
					coutner++;
					Term newT = factory.createVariable(newName);
					newTerms.add(newT);
				} else {
					newTerms.add(t.copy());
				}
				i++;
			}
			head.updateTerms(newTerms);

			Iterator<Atom> bit = query.getBody().iterator();
			while (bit.hasNext()) {
				Atom a = bit.next();
				Iterator<Term> hit2 = a.getTerms().iterator();
				i = 0;
				LinkedList<Term> vec = new LinkedList<Term>();
				while (hit2.hasNext()) {
					Term t = hit2.next();
					if (t instanceof UndistinguishedVariable) {
						String newName = "_uv-" + coutner;
						coutner++;
						Term newT = factory.createVariable(newName);
						vec.add(newT);
					} else {
						vec.add(t.copy());
					}
					i++;
				}
				a.updateTerms(vec);
			}
		}
	}

}
