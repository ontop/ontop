package it.unibz.krdb.obda.owlrefplatform.core.unfolding;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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

import it.unibz.krdb.obda.model.AlgebraOperatorPredicate;
import it.unibz.krdb.obda.model.BooleanOperationPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.VariableImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.DatalogNormalizer;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.QueryAnonymizer;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.Unifier;
import it.unibz.krdb.obda.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator;
import it.unibz.krdb.obda.utils.DatalogDependencyGraphGenerator;
import it.unibz.krdb.obda.utils.QueryUtils;

import java.util.ArrayList;
import java.util.Collection;
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

import com.google.common.collect.Multimap;

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
	
	private final List<CQIE> emptyList = Collections.unmodifiableList(new LinkedList<CQIE>());
	
	private enum UnfoldingMode {
		UCQ, DATALOG
	};

	private UnfoldingMode unfoldingMode = UnfoldingMode.UCQ;

	private Map<Predicate, List<Integer>> primaryKeys = new HashMap<Predicate, List<Integer>>();

	private Multimap<Predicate, CQIE> ruleIndex;
	private Multimap<Predicate, CQIE> ruleIndexByBody;

	private Map<Predicate, List<CQIE>> mappings = new LinkedHashMap<Predicate, List<CQIE>>();
	
	/**
	 * This field will contain the number of mappings that an ans predicate is related to.
	 * If it is more than 1, we will not unfold it in the left join case.
	 */
	private Map<Predicate, Integer> mapCount = new HashMap<Predicate,Integer>();
	
	
	/***
	 * Leaf predicates are those that do not appear in the head of any rule. If
	 * a predicate is a leaf predicate, it should not be unfolded, they indicate
	 * stop points to get partial evaluations.
	 * <p>
	 * Any atom that is not a leaf, and that cannot be unified with a rule
	 * (either cause of lack of MGU, or because of a rule for the predicate of
	 * the atom) is logically empty w.r.t. to the program.
	 */
	private List<Predicate> extensionalPredicates = new LinkedList<Predicate>();
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

		//TODO: this should not be mappings when working with the query!!
		for (CQIE mappingrule : unfoldingProgram.getRules()) {
			Function head = mappingrule.getHead();

			List<CQIE> rules = mappings.get(head.getFunctionSymbol());
			if (rules == null) {
				rules = new LinkedList<CQIE>();
				mappings.put(head.getFunctionSymbol(), rules);
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
		allPredicates.removeAll(mappings.keySet());
		extensionalPredicates.addAll(allPredicates);
	}
	
	/**
	 * Since we need the mappings here to treat correctly the unfolding of the leftjoin even when we unfold with respect to the program alone
	 * @param unfoldingProgram
	 * @param hashMap
	 * @param mappings2
	 */
/*	public DatalogUnfolder(DatalogProgram unfoldingProgram,
			HashMap<Predicate, List<Integer>> hashMap,
			Hashtable<URI, ArrayList<OBDAMappingAxiom>> mappings2) {
		// TODO Auto-generated constructor stub
		
		this(unfoldingProgram, new HashMap<Predicate, List<Integer>>());
		
		
		
	}*/

	@Override
	/***
	 * Generates a partial evaluation of the rules in <b>inputquery</b> with respect to the
	 * with respect to the program given when this unfolder was initialized. 
	 * 
	 */
	public DatalogProgram unfold(DatalogProgram inputquery, String targetPredicate, String strategy, boolean includeMappings) {
		/*
		 * Needed because the rewriter might generate query bodies like this
		 * B(x,_), R(x,_), underscores reperesnt uniquie anonymous varaibles.
		 * However, the SQL generator needs them to be explicitly unique.
		 * replacing B(x,newvar1), R(x,newvar2)
		 */
		inputquery = QueryAnonymizer.deAnonymize(inputquery);

		DatalogProgram partialEvaluation = flattenUCQ(inputquery, targetPredicate, strategy,  includeMappings);

		DatalogProgram dp = termFactory.getDatalogProgram();
		
		QueryUtils.copyQueryModifiers(inputquery, dp);
		
		dp.appendRule(partialEvaluation.getRules());

		//TODO: SEE WHAT TO DO WITH THIS METHOD !!
		for (CQIE rule: partialEvaluation.getRules()){
			DatalogNormalizer.pullOutEqualities(rule);
			System.out.println(rule);
		}
		
		
		return dp;
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
	 * @param includeMappings 
	 * @return
	 */
	private DatalogProgram flattenUCQ(DatalogProgram inputquery, String targetPredicate, String strategy, boolean includeMappings) {

		List<CQIE> workingSet = new LinkedList<CQIE>();
		workingSet.addAll(inputquery.getRules());

		for (CQIE query : workingSet) {	
			DatalogNormalizer.enforceEqualities(query, false);
		}

		if (includeMappings){
			computePartialEvaluationWRTMappings(workingSet);
		} else{
			if (QuestConstants.BUP.equals(strategy)){
				computePartialEvaluationBUP(workingSet,includeMappings);
			}else if (QuestConstants.TDOWN.equals(strategy)){
				computePartialEvaluationTDown(workingSet,includeMappings);
			}
		}
		
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
	 * @param includeMappings 
	 * 
	 * @param resolvent
	 * @param term
	 * @param termidx
	 * @return
//	 */
	private int computePartialEvaluationTDown(List<CQIE> workingList, boolean includeMappings) {

		int[] rcount = { 0, 0 };

		for (int queryIdx = 0; queryIdx < workingList.size(); queryIdx++) {

			CQIE rule = workingList.get(queryIdx);

			Stack<Integer> termidx = new Stack<Integer>();

			List<Term> tempList = getBodyTerms(rule);

			List<CQIE> result = computePartialEvaluation(null, tempList, rule, rcount, termidx, false, includeMappings);

			if (result == null) {

				/*
				 * If the result is null the rule is logically empty
				 */
				workingList.remove(queryIdx);
				queryIdx -= 1;
				continue;
			} else if (result.size() == 0) {

				/*
				 * This rule is already a partial evaluation
				 */
				continue;
			}

			/*
			 * One more step in the partial evaluation was computed, we need to
			 * remove the old query and add the result instead. Each of the new
			 * queries could still require more steps of evaluation, so we
			 * decrease the index.
			 */
			workingList.remove(queryIdx);
			for (CQIE newquery : result) {
				if (!workingList.contains(newquery)) {
					workingList.add(queryIdx, newquery);
				}
			}
			queryIdx -= 1;
		}
		return rcount[1];
	}
	
/**
 * TODO: ADD COMMENT !!
 * 
 * @param workingList
 * @param includeMappings
 * @return
 */
		private int computePartialEvaluationBUP(List<CQIE> workingList, boolean includeMappings) {

			int[] rcount = { 0, 0 }; //int queryIdx = 0;
			
		
			
			DatalogDependencyGraphGenerator depGraph = new DatalogDependencyGraphGenerator(workingList);
			
			List<Predicate> predicatesInBottomUp = depGraph.getPredicatesInBottomUp();		
			List<Predicate> extensionalPredicates = depGraph.getExtensionalPredicates();
			List<CQIE> fatherCollection = new LinkedList<CQIE>();

			for (int predIdx = 0; predIdx < predicatesInBottomUp.size() -1; predIdx++) {

				Predicate pred = predicatesInBottomUp.get(predIdx);
				Predicate preFather =  depGraph.getFatherPredicate(pred);
				
				if (!extensionalPredicates.contains(pred)) {// it is a defined  predicate, like ans2,3.. etc

					ruleIndex = depGraph.getRuleIndex();
					Multimap<Predicate, CQIE> ruleIndexByBody = depGraph.getRuleIndexByBodyPredicate();
					Collection<CQIE> workingRules = ruleIndex.get(pred);
				
					
					fatherCollection.clear();
					Collection<CQIE> ruleCollection = ruleIndexByBody.get(pred);
	
					
					
					
					for (CQIE fatherRule:  ruleCollection) {
						CQIE copyruleCqie = fatherRule.clone();
						fatherCollection.add(copyruleCqie);
					}
					
					for (CQIE fatherRule : fatherCollection) {
						
						int queryIdx=workingList.indexOf(fatherRule);
						Stack<Integer> termidx = new Stack<Integer>();

						List<Term> fatherTerms = getBodyTerms(fatherRule);
						List<CQIE> result = computePartialEvaluation( pred, fatherTerms, fatherRule, rcount, termidx, false,includeMappings);
				

						
					
						
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
						workingList.remove(queryIdx);
						workingList.removeAll(workingRules);

						for (CQIE newquery : result) {
							if (!workingList.contains(newquery)) {
								
								//Here we update the index head arom -> rule
								depGraph.removeRuleFromRuleIndex(preFather,fatherRule);
								depGraph.addRuleToRuleIndex(preFather, newquery);
							
								
								//Delete the rules from workingList that have been touched
								workingList.add(queryIdx, newquery);

							
								//Here we update the index body atom -> rule

								
								//I remove all the old indexes	with the old rule
								depGraph.removeOldRuleIndexByBodyPredicate(fatherRule);
								
								
								//I update the new indexes with the new rule
								depGraph.updateRuleIndexByBodyPredicate(newquery);
								
							} //end if
						}// end for result
						
						
						
						
						
					} // end for workingRules

				} //else { // it is an extensional predicate
					
					//int numMap= mappings.get(pred).size();
					//mapCount.put(preFather, numMap);
					//continue;
				//}

			} // end for over the ordered predicates
			return rcount[1];
		}
	
		
		
		/**
		 * This method does the partial evaluation w.r.t. the mappings. 
		 * It first iterates over the extensional predicates that need to be undolded w.r.t. the mappings.
		 * Then does the partial evaluation for each extensional atom. and updates the rules in working list.
		 * 
		 * @param workingList
		 */
		private void computePartialEvaluationWRTMappings(List<CQIE> workingList) {

			int[] rcount = { 0, 0 }; //int queryIdx = 0;
		
			
			DatalogDependencyGraphGenerator depGraph = new DatalogDependencyGraphGenerator(workingList);

		//	List<Predicate> predicatesInBottomUp = depGraph.getPredicatesInBottomUp();		
			List<Predicate> extensionalPredicates = depGraph.getExtensionalPredicates();

			boolean includeMappings=true;
			boolean keepLooping=true;
			extensionalPredicates =  depGraph.getExtensionalPredicates();
			ruleIndex = depGraph.getRuleIndex();
			ruleIndexByBody = depGraph.getRuleIndexByBodyPredicate();

			for (int predIdx = 0; predIdx < extensionalPredicates.size() ; predIdx++) {

				Predicate pred = extensionalPredicates.get(predIdx);
				Predicate preFather =  depGraph.getFatherPredicate(pred);

			
				List<CQIE> result = new LinkedList<CQIE>();
				List<CQIE> fatherCollection = new LinkedList<CQIE>();
		
				//The next loop unfold all the atoms of the predicate pred
				do
				{
		
					result.clear();
					fatherCollection.clear();
					
					Collection<CQIE> ruleCollection = ruleIndexByBody.get(pred);
					
					
					for (CQIE fatherRule:  ruleCollection) {
						CQIE copyruleCqie = fatherRule.clone();
						fatherCollection.add(copyruleCqie);
					}
					
					for (CQIE fatherRule:  fatherCollection) {
						List<Term> ruleTerms = getBodyTerms(fatherRule);
						Stack<Integer> termidx = new Stack<Integer>();
						
						//here we perform the partial evaluation
						List<CQIE> partialEvaluation = computePartialEvaluation(pred,  ruleTerms, fatherRule, rcount, termidx, false, includeMappings);
						
						if (partialEvaluation != null){
							addDistinctList(result, partialEvaluation);
							//updating indexes with intermediate results
							keepLooping = updateIndexes(depGraph, pred, preFather, result, fatherRule,  workingList);
						} else{
							keepLooping = updateNullIndexes(depGraph, pred, preFather,  fatherRule,  workingList);
						}
					} //end for father collection
					
					
				if (result.isEmpty() )
				{
					keepLooping = false;
				}
					
				}while(keepLooping);
			}
			
			

			// I add to the working list all the rules touched by the unfolder!
			for (int predIdx = 0; predIdx < extensionalPredicates.size() ; predIdx++) {
				Predicate pred = extensionalPredicates.get(predIdx);
				Predicate preFather =  depGraph.getFatherPredicate(pred);

				Collection<CQIE> rulesToAdd= ruleIndex.get(preFather);

				for (CQIE resultingRule: rulesToAdd){
					if (!workingList.contains(resultingRule)){
						workingList.add(resultingRule);
					}
				}

			}
			

		}
		/**
		 * This method will update the indexes when the result of unfolding fatherRule is null.
		 * 
		 * @param depGraph
		 * @param pred
		 * @param preFather
		 * @param fatherRule
		 * @param workingList
		 * @return
		 */
		private boolean updateNullIndexes(DatalogDependencyGraphGenerator depGraph, Predicate pred, Predicate preFather, CQIE fatherRule, List<CQIE> workingList) {
			
			
			depGraph.removeRuleFromRuleIndex(preFather,fatherRule);

			//Delete the rules from workingList that have been touched
			if (workingList.contains(fatherRule)){
				workingList.remove(fatherRule);
			}




			//Update the bodyIndex
	
			depGraph.removeOldRuleIndexByBodyPredicate(fatherRule);
			
			return true;
		
	

		}

		/**
		 * This method just copy the contain of one list into another without repeating elements
		 * @param result
		 * @param inputList
		 */
		private void addDistinctList(List<CQIE> result, List<CQIE> inputList) {
			for (CQIE resultingRule: inputList){
					if (!result.contains(resultingRule)){
						result.add(resultingRule);
					}
			}
		}

		/**
		 * This method has several tasks:
		 * <ul>
		 * <li> Update the ruleIndex
		 * <li> Update the bodyIndex
		 * <li> Delete keys from the body index that have alreay been unfolded and therefore not needed
		 * <li> Determine if there is a need to keep unfolding pred
		 * <li> Delete the rules from workingList that have been touched
		 * </ul>
		 * @param depGraph
		 * @param pred
		 * @param preFather
		 * @param result
		 * @param fatherRule
		 * @param workingList
		 * @return
		 */
		private boolean updateIndexes(DatalogDependencyGraphGenerator depGraph, Predicate pred,
				Predicate preFather, List<CQIE> result, CQIE fatherRule, List<CQIE> workingList) {
			//boolean hasPred = false;
			
			for (CQIE newquery : result) {
				//Update the ruleIndex
				depGraph.removeRuleFromRuleIndex(preFather,fatherRule);
				depGraph.addRuleToRuleIndex(preFather, newquery);

				//Delete the rules from workingList that have been touched
				if (workingList.contains(fatherRule)){
					workingList.remove(fatherRule);
				}



				//List<Term> bodyTerms = getBodyTerms(newquery);

				//Update the bodyIndex
				
				depGraph.removeOldRuleIndexByBodyPredicate(fatherRule);
				depGraph.updateRuleIndexByBodyPredicate(newquery);

				
				
/*				for (Term termPredicate: bodyTerms){
					if (termPredicate instanceof Function){
						Predicate mypred = ((Function) termPredicate).getFunctionSymbol(); 
						if (extensionalPredicates.contains(mypred)){
							depGraph.removeRuleFromBodyIndex(mypred, fatherRule);
							depGraph.addRuleToBodyIndex(mypred, newquery);

						}
						
						if (mypred.equals(pred)){
							hasPred=true;
						}
					}
				} //end for terms in rule
			*/
			} // end for queries in result
			
			
			
			//TODO: check what happens here when the concept has several mappings and it appears several times in the rule
			
			//Determine if there is a need to keep unfolding pred
			if (ruleIndexByBody.get(pred).isEmpty()){
				return false; //I finish with pred I can move on
			}else if (result.contains(fatherRule)){ 
				return false; // otherwise it will loop for ever. I am in the case when a concept in the LJ has several mappings
			}else{
				return true; // keep doing the loop
			}
			
		}

		
		
		
	

		/**
		 * Returns the list of terms inside the functions atoms in the body
		 * @param rule
		 * @return
		 */
		private List<Term> getBodyTerms(CQIE rule) {
			List<Function> currentTerms = rule.getBody();
			List<Term> tempList = new LinkedList<Term>();

			for (Function a : currentTerms) {
					tempList.add(a);
			}
			return tempList;
		}			
					
					
					
					
					
					
				
					
		
		
		
		
		
		
		
		
		
		

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
		 * @param includeMappings 
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

		
		private List<CQIE> resolveDataAtom(Predicate resolvPred, Function focusAtom, CQIE rule, Stack<Integer> termidx, int[] resolutionCount, boolean isLeftJoin,
				boolean isSecondAtomInLeftJoin, boolean includeMappings) {

			if (!focusAtom.isDataFunction())
				throw new RuntimeException("Cannot unfold a non-data atom: " + focusAtom);

			/*
			 * Leaf predicates are ignored (as boolean or algebra predicates)
			 */
			Predicate pred = focusAtom.getFunctionSymbol();
			if (extensionalPredicates.contains(pred) && !includeMappings) {
				// The atom is a leaf, that means that is a data atom that
				// has no resolvent rule, and marks the end points to compute
				// partial evaluations

				return emptyList;
			}
			
			Collection<CQIE> rulesDefiningTheAtom = new LinkedList<CQIE>();

			if (!includeMappings){
			//therefore we are doing bottom-up of the query	
				if (ruleIndex.keySet().contains(pred) && !pred.equals(resolvPred)) {
					//Since it is not resolvPred, we are are not going to resolve it
					return emptyList;
				} else if (pred.equals(resolvPred)) {
					// I am doing bottom up here
					rulesDefiningTheAtom = ruleIndex.get(pred);
				}
			} else  if (pred.equals(resolvPred)) {
					// I am doing top-down
					//I am using the mappings 
					rulesDefiningTheAtom = mappings.get(pred);
				
			} else if (!pred.equals(resolvPred)){
				return emptyList;
			}
			
			
			
            boolean hasOneMapping = true ;
            if (includeMappings && mappings.containsKey(resolvPred)){
                hasOneMapping = mappings.get(resolvPred).size()<2;
            }  
            
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
			} else if (hasOneMapping || !isSecondAtomInLeftJoin){
				
				//result = generateResolutionResultParent(parentRule, focusAtom, rule, termidx, resolutionCount, rulesDefiningTheAtom, isLeftJoin, isSecondAtomInLeftJoin);
				result = generateResolutionResult(focusAtom, rule, termidx, resolutionCount, rulesDefiningTheAtom, isLeftJoin, isSecondAtomInLeftJoin);
			} else if (!hasOneMapping && isSecondAtomInLeftJoin) {
				// This case takes place when ans has only 1 definition, but the extensional atom have more than 1 mapping, and 
				result = new LinkedList<CQIE>();
				result.add(rule);
				result.addAll(rulesDefiningTheAtom);
			}
			
			
			//TODO: improve this... looks awkward 
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
//	private DatalogProgram unfoldToDatalog(DatalogProgram inputquery) {
//		HashSet<CQIE> relevantrules = new HashSet<CQIE>();
//		for (CQIE cq : inputquery.getRules()) {
//			for (Function atom : cq.getBody()) {
//				for (CQIE rule : unfoldingProgram.getRules(atom.getFunctionSymbol())) {
//					/*
//					 * No repeteatin is assured by the HashSet and the hashing
//					 * implemented in each CQIE
//					 */
//					relevantrules.add(rule);
//				}
//			}
//		}
//		/**
//		 * Done collecting relevant rules, appending the original query.
//		 */
//		LinkedList<CQIE> result = new LinkedList<CQIE>();
//		result.addAll(inputquery.getRules());
//		result.addAll(relevantrules);
//		return termFactory.getDatalogProgram(result);
//	}


	

	

	

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
	// TODO REMOVE?????????? Private?????????????????
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

	private static Term getFreshTerm(Term term, int suffix) {
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
	 * @param includeMappings 
	 * @return
	 */

    private List<CQIE> computePartialEvaluation(Predicate resolvPred, List<Term> currentTerms, CQIE rule, int[] resolutionCount, Stack<Integer> termidx,
            boolean parentIsLeftJoin, boolean includeMappings) {

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
                    result = computePartialEvaluation(resolvPred,  focusLiteral.getTerms(), rule, resolutionCount, termidx, focusAtomIsLeftJoin, includeMappings);

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
                    List<CQIE> result = new LinkedList<CQIE>();
                    Predicate pred = focusLiteral.getFunctionSymbol();
                     if (pred.equals(resolvPred)) {
                             result = resolveDataAtom(resolvPred, focusLiteral, rule, termidx, resolutionCount, parentIsLeftJoin,
                                    isLeftJoinSecondArgument,includeMappings);
                     }
                     
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
			Collection<CQIE> rulesDefiningTheAtom, boolean isLeftJoin, boolean isSecondAtomOfLeftJoin) {

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
			
			while (innerAtoms.contains(focusAtom)){
				innerAtoms.remove(focusAtom);
			}
			
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



			
			if (isSecondAtomOfLeftJoin && rulesGeneratedSoFar > 1 ) {
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
		boolean containsVar=false;
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
				} else if (ArgumentAtoms == 2){
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
				//TODO: is that correct?? what if there is a complex expression?????
			} else if (atom.isBooleanFunction()&& ArgumentAtoms >=2){
				for (Variable var: variablesArg2){
					containsVar = atom.getReferencedVariables().contains(var);
					if (containsVar){
							innerAtoms.remove(atom);
							break;
						}
				}
			}

		}// end for rule body

		//freshRule.updateBody(newbody);
		replaceInnerLJ(freshRule, newbody, termidx1);
		HashMap<Variable, Term> unifier = new HashMap<Variable, Term>();

		for (Variable var : variablesArg2) {
			unifier.put(var, OBDAVocabulary.NULL);
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

	@Override
	@Deprecated
	/**
	 * This method is deprecated and does the unfolding of the program assuming that the strategy is TOP-DOWN
	 */
	public DatalogProgram unfold(DatalogProgram query, String targetPredicate)
			throws OBDAException {
		unfold(query,targetPredicate,QuestConstants.TDOWN, false);
		return null;
	}



}
