package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.SubDescriptionAxiom;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.AxiomToRuleTranslator;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.Unifier;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.SigmaTBoxOptimizer;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.TBoxReasonerToOntology;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.DatalogUnfolder;

public class DataDependencies {
	
	private final Ontology sigma;
	/* TBox axioms translated into rules (used by QuestStatement) */ 
	private final Map<Predicate, List<CQIE>> rulesIndex;
	private final List<CQIE> rules;

	private static final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
	private static final Logger log = LoggerFactory.getLogger(DataDependencies.class);
	
	public DataDependencies(TBoxReasoner reasoner) {
		sigma = TBoxReasonerToOntology.getOntology(reasoner, true);
		sigma.saturate();

		rules = createSigmaRules(sigma);
		rulesIndex = createSigmaRulesIndex(sigma);				
	}
	
	// TEST ONLY 
	@Deprecated 
	public DataDependencies(Ontology sigma) {
		this.sigma = sigma;
		sigma.saturate();
		
		rules = createSigmaRules(sigma);
		rulesIndex = createSigmaRulesIndex(sigma);				
	}

	public TBoxReasoner getReasoner() {
		return new TBoxReasonerImpl(sigma);		
	}
	
	@Deprecated
	public Ontology getOntology() {
		return sigma;
	}
	
	@Deprecated
	public Set<Axiom> getAssertions() {
		return sigma.getAssertions();
	}

	public Set<SubDescriptionAxiom> getByIncluded(Predicate pred) {
		return sigma.getByIncluded(pred);
	}
	
	private static Map<Predicate, List<CQIE>> createSigmaRulesIndex(Ontology sigma) {
		Ontology saturatedSigma = sigma.clone();
		saturatedSigma.saturate();
		
		Map<Predicate, List<CQIE>> sigmaRulesMap = new HashMap<Predicate, List<CQIE>>();
		
		for (Axiom assertion : saturatedSigma.getAssertions()) {
			try {
				CQIE rule = AxiomToRuleTranslator.translate(assertion);
                if(rule != null) {
        			Function atom = rule.getBody().get(0); // The rule always has one
					// body atom
        			Predicate predicate = atom.getFunctionSymbol();
        			List<CQIE> rules = sigmaRulesMap.get(predicate);
        			if (rules == null) {
        				rules = new LinkedList<CQIE>();
        				sigmaRulesMap.put(predicate, rules);
        			}
        			rules.add(rule);
                }
			} 
			catch (UnsupportedOperationException e) {
				log.warn(e.getMessage());
			}
		}
		return sigmaRulesMap;
	}

	private static List<CQIE> createSigmaRules(Ontology sigma) {
		Ontology saturatedSigma = sigma.clone();
		saturatedSigma.saturate();
		
		List<CQIE> sigmaRules = new LinkedList<CQIE>();
		
		for (Axiom assertion : saturatedSigma.getAssertions()) {
			try {
				CQIE rule = AxiomToRuleTranslator.translate(assertion);
				// ESSENTIAL: FRESH RULES
				if (rule != null) {
					rule = DatalogUnfolder.getFreshRule(rule, 4022013); // Random suffix number
					sigmaRules.add(rule);
				}
			} 
			catch (UnsupportedOperationException e) {
				log.warn(e.getMessage());
			}
		}
		return sigmaRules;
	}
	
	public List<CQIE> getRules() {
		return rules;
	}
	
	/**
	 * 
	 * @param program
	 * @param rules
	 */
	public static void optimizeQueryWithSigmaRules(DatalogProgram program, DataDependencies sigma) {
		List<CQIE> unionOfQueries = new LinkedList<CQIE>(program.getRules());
		// for each rule in the query
		for (int qi = 0; qi < unionOfQueries.size(); qi++) {
			CQIE query = unionOfQueries.get(qi);
			// get query head, body
			Function queryHead = query.getHead();
			List<Function> queryBody = query.getBody();
			// for each atom in query body
			for (int i = 0; i < queryBody.size(); i++) {
				Set<Function> removedAtom = new HashSet<Function>();
				Function atomQuery = queryBody.get(i);
				Predicate predicate = atomQuery.getPredicate();

				// for each tbox rule
				List<CQIE> rules = sigma.rulesIndex.get(predicate);
				if (rules == null || rules.isEmpty()) {
					continue;
				}
				for (CQIE rule : rules) {
					// try to unify current query body atom with tbox rule body
					// atom
					rule = DatalogUnfolder.getFreshRule(rule, 4022013); // Random
																		// suffix
																		// number
					Function ruleBody = rule.getBody().get(0);
					Map<Variable, Term> theta = Unifier.getMGU(ruleBody, atomQuery);
					if (theta == null || theta.isEmpty()) {
						continue;
					}
					// if unifiable, apply to head of tbox rule
					Function ruleHead = rule.getHead();
					Function copyRuleHead = (Function) ruleHead.clone();
					Unifier.applyUnifier(copyRuleHead, theta);

					removedAtom.add(copyRuleHead);
				}

				for (int j = 0; j < queryBody.size(); j++) {
					if (j == i) {
						continue;
					}
					Function toRemove = queryBody.get(j);
					if (removedAtom.contains(toRemove)) {
						queryBody.remove(j);
						j -= 1;
						if (j < i) {
							i -= 1;
						}
					}
				}
			}
			// update query datalog program
			unionOfQueries.remove(qi);
			unionOfQueries.add(qi, ofac.getCQIE(queryHead, queryBody));
		}
		program.removeAllRules();
		program.appendRule(unionOfQueries);
	}
	
}
