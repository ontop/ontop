package org.semanticweb.ontop.owlrefplatform.core.unfolding;

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

import java.util.*;

import org.semanticweb.ontop.model.AlgebraOperatorPredicate;
import org.semanticweb.ontop.model.BooleanOperationPredicate;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Constant;
import org.semanticweb.ontop.model.DatalogProgram;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.*;
import org.semanticweb.ontop.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator;
import org.semanticweb.ontop.utils.DatalogDependencyGraphGenerator;
import org.semanticweb.ontop.utils.QueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
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
	private static final Logger log = LoggerFactory.getLogger(DatalogUnfolder.class);
	private static final OBDADataFactory termFactory = OBDADataFactoryImpl.getInstance();

	private final Map<Predicate, List<Integer>> primaryKeys;
	private final Map<Predicate, List<CQIE>> mappings;

	/***
	 * Leaf predicates are those that do not appear in the head of any rule. If
	 * a predicate is a leaf predicate, it should not be unfolded, they indicate
	 * stop points to get partial evaluations.
	 * <p>
	 * Any atom that is not a leaf, and that cannot be unified with a rule
	 * (either cause of lack of MGU, or because of a rule for the predicate of
	 * the atom) is logically empty w.r.t. to the program.
	 */
	private final List<Predicate> extensionalPredicates;

	public DatalogUnfolder(DatalogProgram unfoldingProgram) {
		this(unfoldingProgram, new HashMap<Predicate, List<Integer>>());
	}

	public DatalogUnfolder(DatalogProgram unfoldingProgram, Map<Predicate, List<Integer>> primaryKeys) {
		this(unfoldingProgram.getRules(), primaryKeys);
	}
	public DatalogUnfolder(List<CQIE> unfoldingProgram, Map<Predicate, List<Integer>> primaryKeys) {
		/**
		 * Creating a local index for the rules according to their predicate
		 */
		//TODO: Do we really need to use a LinkedHashMap here?
		Map<Predicate, List<CQIE>> ruleIndex = new LinkedHashMap<>();
		Set<Predicate> leafPredicates = new HashSet<>();

		//TODO: this should not be mappings when working with the query!!
		for (CQIE mappingRule : unfoldingProgram) {
			Function head = mappingRule.getHead();

			List<CQIE> rules = ruleIndex.get(head.getFunctionSymbol());
			if (rules == null) {
				rules = new LinkedList<CQIE>();
				ruleIndex.put(head.getFunctionSymbol(), rules);
			}
			rules.add(mappingRule);

			/*
			 * Collecting the leafPredicates that appear in the body of rules.
			 */
			for (Function atom : mappingRule.getBody()) {
				leafPredicates.addAll(getPredicates(atom));
			}
		}

		/**
		 * the leafPredicates that do not appear in the head of rules are leaf
		 * leafPredicates
		 */
		leafPredicates.removeAll(ruleIndex.keySet());

		/**
		 * These collections are saved as final and unmodifiable attributes.
		 */
		this.primaryKeys = Collections.unmodifiableMap(primaryKeys);
		this.mappings = Collections.unmodifiableMap(ruleIndex);
		this.extensionalPredicates = Collections.unmodifiableList(new ArrayList<>(leafPredicates));
	}

	/**
	 * Since we need the mappings here to treat correctly the unfolding of the leftjoin even when we unfold with respect to the program alone
	 */
/*	public DatalogUnfolder(DatalogProgram unfoldingProgram,
			HashMap<Predicate, List<Integer>> hashMap,
			Hashtable<URI, ArrayList<OBDAMappingAxiom>> mappings2) {
		// TODO Auto-generated constructor stub
		
		this(unfoldingProgram, new HashMap<Predicate, List<Integer>>());
		
	}*/

	/***
	 * Given a query q, this method will try
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
	 * queries and attempt to resolve atom 0 in the body against the rules in
	 * unfolding program. The resolution engine will generate 1 or more CQs as
	 * result. The original atom is removed, and the results are appended to the
	 * end of each query (hence its always safe to unfold atom 0). The new
	 * queries are kept for a next cycle. We stop when no new queries are
	 * produced.
	 * <p>
	 * The right side of left joins will never be touched.
	 * <p>
	 * Currently the method also is aware of functional dependencies (Primary
	 * keys) and will produce queries in which redundant joins w.r.t. to these
	 * are avoided.
	 *
	 * TODO: this method has been erased in version2. Should we keep it??
	 *
	 * @param inputquery
	 * @param includeMappings
	 * @return
	 */

	@Override
	/***
	 * Generates a partial evaluation of the rules in <b>inputquery</b> with respect to the
	 * with respect to the program given when this unfolder was initialized.
	 *
	 * multiplePredIdx is needed because the rewriter might generate query bodies like this
	 * B(x,_), R(x,_), underscores represent unique anonymous variables.
	 * However, the SQL generator needs them to be explicitly unique.
	 * replacing B(x,newvar1), R(x,newvar2)
	 *
	 * TODO: comment other parameters
	 * @param multiTypedFunctionSymbolIndex
	 *          Index of predicates having multiple types
	 *
	 */
	public DatalogProgram unfold(DatalogProgram inputquery, String targetPredicate, String strategy, boolean includeMappings,
								 Multimap<Predicate,Integer> multiTypedFunctionSymbolIndex) {

		List<CQIE> workingSet = new LinkedList<CQIE>();
		for (CQIE query : inputquery.getRules())
			workingSet.add(QueryAnonymizer.deAnonymize(query));

		if (includeMappings){
			computePartialEvaluationWRTMappings(workingSet, multiTypedFunctionSymbolIndex);
		} else{
			if (QuestConstants.BUP.equals(strategy)){
				computePartialEvaluationBUP(workingSet,includeMappings, multiTypedFunctionSymbolIndex);
			}else if (QuestConstants.TDOWN.equals(strategy)){
				computePartialEvaluationTDown(workingSet,includeMappings, multiTypedFunctionSymbolIndex);
			}
		}

		/**
		 * We need to enforce equality again, because at this point it is
		 *  possible that there is still some EQ(...) in the Program resultdp
		 *
		 */
		for (CQIE query : workingSet)
			EQNormalizer.enforceEqualities(query);

		//TODO: remove this noise
		DatalogProgram dp = termFactory.getDatalogProgram();
		QueryUtils.copyQueryModifiers(inputquery, dp);
		dp.appendRule(workingSet);

		return dp;
	}


	/***
	 * This method assumes that the inner term (termidx) of term is a data atom,
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
	 * @return
	//	 */
	private int computePartialEvaluationTDown(List<CQIE> workingList, boolean includeMappings,
											  Multimap<Predicate, Integer> multiTypedFunctionSymbolIndex) {


		/**
		 * Rule indexing: copied from computePartialEvaluationBUP()
		 */
		DatalogDependencyGraphGenerator depGraph = new DatalogDependencyGraphGenerator(workingList);
		Multimap<Predicate, CQIE> ruleIndex = depGraph.getRuleIndex();


		int[] rcount = { 0, 0 };

		for (int queryIdx = 0; queryIdx < workingList.size(); queryIdx++) {

			CQIE rule = workingList.get(queryIdx);

			Stack<Integer> termidx = new Stack<Integer>();

			List<Term> tempList = getBodyTerms(rule);

			List<CQIE> result = computePartialEvaluation(null, tempList, rule, rcount, termidx, false,
					includeMappings, ruleIndex, multiTypedFunctionSymbolIndex);

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
	 * <p>
	 * TODO: This method compute the partial evaluation of the query in a bottom-up fashion without considering the mappings.
	 * It will start the leaves parents (ans predicates) and start going up. If there is a leftjoin it will stop unfolding
	 * </p>
	 *
	 * <p>
	 * Consider the program
	 * <ul>
	 * <li> Ans1(x) :- Ans(2)
	 * <li> Ans2(x) :- Emploee
	 * </ul>
	 * It will start by considering Ans2(x)---pred--- use the index to find the father atom, Ans1(x), and the father rule.
	 * It gives all this information to  computePartialEvaluation and returns the unfolding program afterwards since Ans1 has no father atom.
	 * </p>
	 *
	 * @param workingList
	 * @param includingMappings
	 * @return
	 */
	private int computePartialEvaluationBUP(List<CQIE> workingList, boolean includingMappings,
											Multimap<Predicate, Integer> multiTypedFunctionSymbolIndex) {

		int[] rcount = { 0, 0 }; //int queryIdx = 0;

		DatalogDependencyGraphGenerator depGraph = new DatalogDependencyGraphGenerator(workingList);

		List<Predicate> predicatesInBottomUp = depGraph.getPredicatesInBottomUp();
		List<Predicate> extensionalPredicates = depGraph.getExtensionalPredicates();
		List<CQIE> fatherCollection = new LinkedList<CQIE>();

		//We iterate over the ordered predicates in the program according to the dependencies
		for (int predIdx = 0; predIdx < predicatesInBottomUp.size() -1; predIdx++) {

			//get the predicate
			Predicate pred = predicatesInBottomUp.get(predIdx);
			//get the father predicate

			if (!extensionalPredicates.contains(pred)) {// it is a defined  predicate, like ans2,3.. etc

				//get all the indexes we need
				Multimap<Predicate, CQIE> ruleIndex = depGraph.getRuleIndex();
				Multimap<Predicate, CQIE> ruleIndexByBody = depGraph.getRuleIndexByBodyPredicate();


				fatherCollection.clear();

				//The rules USING pred
				Collection<CQIE> ruleCollection = ruleIndexByBody.get(pred);

				//The rules DEFINING pred
				Collection<CQIE> workingRules = ruleIndex.get(pred);


				cloneRules(fatherCollection, ruleCollection);

				//We unfold every rule of the father atom that contains pred
				for (CQIE fatherRule : fatherCollection) {

					Predicate preFather =  fatherRule.getHead().getFunctionSymbol();

					int queryIdx=workingList.indexOf(fatherRule);
					Stack<Integer> termidx = new Stack<Integer>();

					List<Term> fatherTerms = getBodyTerms(fatherRule);

					//This is to search for aggregates in the head of the
					//father rule and stop unfolding.
					boolean hasAggregates = false;
					hasAggregates = detectAggregatesHead(workingRules);
						
						

						/*
						 * This we compute the partial evaluation. The variable parentIsLeftJoin is false because here we do not process 
						 * the atom itself, but we delegate this task to computePartialEvaluation. Inside that method we check if the atom
						 * is a leftjoin, in case that it is an algebra atom.
						 */
					boolean parentIsLeftJoin = false;

					List<CQIE> result = new LinkedList<CQIE>();
					if (!hasAggregates){
						result = computePartialEvaluation( pred, fatherTerms, fatherRule, rcount, termidx, parentIsLeftJoin,
								includingMappings, ruleIndex, multiTypedFunctionSymbolIndex);
					}else{
						// result is the empty list
						//TODO: This can be optmised I think. Here we could still unfold atoms in the body that are not
						//affected by the aggregate
					}

					if (result == null) {
							/*
							 * If the result is null the rule is logically empty
							 */
						workingList.remove(queryIdx);
						continue;

					} else if (result.size() == 0) {
							/*
							 * This rule is already a partial evaluation
							 */
						continue;
					}else if (result.size() >= 2) {
						multiTypedFunctionSymbolIndex = detectMissmatchArgumentType(result, false, ruleIndex, multiTypedFunctionSymbolIndex);
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


							//adding the rules from workingList that have been touched
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
	 * This is a helper method to find aggregate functions in the head of the rule.
	 *
	 * @param workingRules
	 * @return
	 */
	private boolean detectAggregatesHead(Collection<CQIE> workingRules) {
		boolean hasAggregates = false;

		for (CQIE rule: workingRules){
			hasAggregates = detectAggregateinSingleRule( rule);
		}
		return hasAggregates;
	}

	private boolean detectAggregateinSingleRule( CQIE rule) {
		Function fatherHead = rule.getHead();

		List<Term> headArgs = fatherHead.getTerms();
		for (Term arg: headArgs) {
			if (detectAggregateInArgument(arg)) {
				return true;
			}
		}
		return false;
	}

	protected static boolean detectAggregateInArgument(Term arg) {
		if (arg instanceof Function) {
			Function compositeTerm = ((Function) arg);
			Predicate functionSymbol = compositeTerm.getFunctionSymbol();

			if (functionSymbol.isAggregationPredicate()) {
				return true;
			}

			/**
			 * Looks recursively at the sub(-sub)-terms
			 */
			return detectAggregateInArgument(compositeTerm.getTerm(0));
		}

		return false;
	}

	/**
	 * Clones the rules in ruleCollection into fatherCollection
	 *
	 * @param fatherCollection
	 * @param ruleCollection
	 */
	private void cloneRules(List<CQIE> fatherCollection,
							Collection<CQIE> ruleCollection) {
		for (CQIE fatherRule:  ruleCollection) {
			CQIE copyruleCqie = fatherRule.clone();
			fatherCollection.add(copyruleCqie);
		}
	}



	/**
	 * This method does the partial evaluation w.r.t. the mappings.
	 * It first iterates over the extensional predicates that need to be undolded w.r.t. the mappings.
	 * Then does the partial evaluation for each extensional atom. and updates the rules in working list.
	 *
	 * @param workingList
	 */
	private void computePartialEvaluationWRTMappings(List<CQIE> workingList,
													 Multimap<Predicate, Integer> multiTypedFunctionSymbolIndex) {

		int[] rcount = { 0, 0 }; //int queryIdx = 0;

		log.debug("Unfolding w.r.t. Mappings:");

		log.debug("Generating Dependency Graph!");
		DatalogDependencyGraphGenerator depGraph = new DatalogDependencyGraphGenerator(workingList);
		//	List<Predicate> predicatesInBottomUp = depGraph.getPredicatesInBottomUp();		
		List<Predicate> extensionalPredicates = depGraph.getExtensionalPredicates();
		List<Predicate> predicatesMightGotEmpty = new LinkedList<Predicate>();

		boolean includeMappings=true;
		boolean keepLooping=true;
		extensionalPredicates =  depGraph.getExtensionalPredicates();
		Multimap<Predicate, CQIE> ruleIndex = depGraph.getRuleIndex();
		Multimap<Predicate, CQIE> ruleIndexByBody = depGraph.getRuleIndexByBodyPredicate();

		for (int predIdx = 0; predIdx < extensionalPredicates.size() ; predIdx++) {

			Predicate pred = extensionalPredicates.get(predIdx);



			List<CQIE> result = new LinkedList<CQIE>();
			List<CQIE> fatherCollection = new LinkedList<CQIE>();

			//The next loop unfold all the atoms of the predicate pred
			do
			{

				result.clear();
				fatherCollection.clear();

				Collection<CQIE> ruleCollection = ruleIndexByBody.get(pred);


				cloneRules(fatherCollection, ruleCollection);

				boolean parentIsLeftJoin = false;

				for (CQIE fatherRule:  fatherCollection) {

					Predicate preFather =  fatherRule.getHead().getFunctionSymbol();
					List<Term> ruleTerms = getBodyTerms(fatherRule);
					Stack<Integer> termidx = new Stack<Integer>();

					//here we perform the partial evaluation
					List<CQIE> partialEvaluation = computePartialEvaluation(pred,  ruleTerms, fatherRule, rcount, termidx,
							parentIsLeftJoin,  includeMappings, ruleIndex, multiTypedFunctionSymbolIndex);



					if (partialEvaluation != null){

//							System.out.print("Result: ");
//							for (CQIE rule: partialEvaluation){
//								System.out.println(rule);
//							}

						addDistinctList(result, partialEvaluation);
						//updating indexes with intermediate results


						keepLooping = updateIndexes(pred, preFather, result, fatherRule,
								workingList, depGraph, multiTypedFunctionSymbolIndex);

						log.debug(pred + " : " + ruleIndex.get(preFather).toString() );


					} else{
						log.debug("Empty: "+pred);
						if (!predicatesMightGotEmpty.contains(preFather)){
							predicatesMightGotEmpty.add(preFather);

						}
						keepLooping = updateNullIndexes(pred, preFather,  fatherRule,  workingList, depGraph);

						//System.out.println(ruleIndex.get(preFather).size());
//							System.out.println(ruleIndexByBody.get(pred).size());

					}
					if (result.size() >= 2) {
						multiTypedFunctionSymbolIndex = detectMissmatchArgumentType(result,false, ruleIndex, multiTypedFunctionSymbolIndex);
					}

				} //end for father collection


				if (result.isEmpty() )
				{
					keepLooping = parentIsLeftJoin;
				}

			}while(keepLooping);
		}

		List<Predicate> touchedPredicates = new LinkedList<Predicate>();
		boolean noRewriting = false ;
		Predicate ans1 = termFactory.getClassPredicate("ans1");

		while (!predicatesMightGotEmpty.isEmpty()){
			predicatesMightGotEmpty=updateRulesWithEmptyAnsPredicates(workingList, predicatesMightGotEmpty, touchedPredicates,
					depGraph, multiTypedFunctionSymbolIndex);

			//if I deleted ans1 I dont return anything later
			if (predicatesMightGotEmpty.contains(ans1)){
				noRewriting = true;
				break;
			}
		}

		if (!noRewriting){
			// I add to the working list all the rules touched by the unfolder!
			addNewRules2WorkingListFromBodyAtoms(workingList, extensionalPredicates, ruleIndex, depGraph);
			addNewRules2WorkingListFromHeadAtoms(workingList, touchedPredicates, ruleIndex);
		}


	}

	/**
	 * It search for the empty predicates that got empty during the unfolding of the extensional
	 * predicates, and either generate the right rule for the LJ, or delete the rules.
	 * Returns the predicates that have been deleted.
	 *
	 */
	private List<Predicate> updateRulesWithEmptyAnsPredicates(List<CQIE> workingList,
															  List<Predicate> predicatesMightGotEmpty, List<Predicate> touchedPredicates,
															  DatalogDependencyGraphGenerator dependencyGraph,
															  Multimap<Predicate, Integer> multiTypedFunctionSymbolIndex) {
		//TODO: this is not optimal. The best would be that the generateNullBinding takes care of this

		//This loop is to update the ans rules that could be affected by the elimination of
		//some rule in the bottom part of the problem because of the lack of mappings for instance.

		//in the next variable we keep the head of the rules that we delete
		List<Predicate> deletedPredicates = new LinkedList<Predicate>();

		Multimap<Predicate, CQIE> ruleIndex = dependencyGraph.getRuleIndex();
		Multimap<Predicate, CQIE> ruleIndexByBody = dependencyGraph.getRuleIndexByBodyPredicate();

		for (Predicate predEmpty : predicatesMightGotEmpty ) {

			Collection<CQIE> predRules = ruleIndex.get(predEmpty);

			//if it is not empty I do nothing
			if (predRules.isEmpty()){

				Collection<CQIE> ruleCollection = ruleIndexByBody.get(predEmpty);

				List<CQIE> fatherCollection = new LinkedList<CQIE>();
				cloneRules(fatherCollection, ruleCollection);

				for (CQIE fatherRule:  fatherCollection) {

					List<Function>currentTerms = fatherRule.getBody();


					//I have to find the stack pointing to the atom
					Stack<Integer> termidx = new Stack<Integer>();

					boolean isLeftJoinSecondArgument[] = {false};

					termidx = getStackfromPredicate(predEmpty,currentTerms, termidx, false, isLeftJoinSecondArgument);

					Predicate fatherpred = fatherRule.getHead().getFunctionSymbol();

					//if it is the second argument of a LJ, it add the null bindings and produce the new rule
					if (isLeftJoinSecondArgument[0]){
						CQIE newrule = generateNullBindingsForLeftJoin(fatherRule,termidx);
						List<CQIE> result = new LinkedList<CQIE>();
						//System.out.println(newrule);
						result.add(newrule);
						updateIndexes(predEmpty, fatherpred, result, fatherRule,  workingList, dependencyGraph,
								multiTypedFunctionSymbolIndex);
						touchedPredicates.add(fatherpred);
					} else{
						//here I remove fatherRule, since it is either a join, or it is the first argument of the LJ
						//System.out.println("deleting"+fatherpred);

						updateNullIndexes( predEmpty, fatherpred,  fatherRule,  workingList, dependencyGraph);
						deletedPredicates.add(fatherpred);

					}
				}
			}


		}//end for
		return deletedPredicates;
	}

	/**
	 * This Method will search in the rule trying to find  where predEmpty is, and return the stack
	 *
	 * @param predEmpty
	 * @param currentTerms
	 * @param termidx
	 * @return
	 */
	private Stack<Integer> getStackfromPredicate(Predicate predEmpty, List<Function> currentTerms, Stack<Integer> termidx, boolean parentIsLeftJoin, boolean[] isLeftJoinSecondArgument) {
		int nonBooleanAtomCounter = 0;


		for (int atomIdx = 0; atomIdx < currentTerms.size(); atomIdx++) {
			Function focusedLiteral=currentTerms.get(atomIdx);

			if (focusedLiteral.isBooleanFunction() || focusedLiteral.isArithmeticFunction() || focusedLiteral.isDataTypeFunction()) {
				continue;

			} else if (focusedLiteral.isAlgebraFunction()) {
				nonBooleanAtomCounter += 1;
					/*
					 * These may contain data atoms that need to be unfolded, we
					 * need to recursively unfold each term.
					 */

				Predicate predicate = focusedLiteral.getFunctionSymbol();
				boolean focusedAtomIsLeftJoin = predicate.equals(OBDAVocabulary.SPARQL_LEFTJOIN);



				List<Function> mylist = new LinkedList<Function>();
				//TODO: Try to remove this!!!!
				for (Term t:focusedLiteral.getTerms()){
					mylist.add((Function) t);
				}
				termidx.push(atomIdx);
				termidx  = getStackfromPredicate(predEmpty, mylist, termidx, focusedAtomIsLeftJoin, isLeftJoinSecondArgument );

			} else if (focusedLiteral.isDataFunction()) {
				nonBooleanAtomCounter += 1;

	                    /*
	                     * This is a data atom, it should be unfolded with the usual
	                     * resolution algorithm.
	                     */

				Predicate pred = focusedLiteral.getFunctionSymbol();

				if (pred.equals(predEmpty)) {
					isLeftJoinSecondArgument[0] = (nonBooleanAtomCounter == 2) && parentIsLeftJoin;
					termidx.push(atomIdx);

					return termidx;

				}
			}//end if

		}//end for
		return termidx;
	}

	/**
	 * I add to the working list all the rules touched by the unfolder w.r.t. mappings
	 */
	private void addNewRules2WorkingListFromBodyAtoms(List<CQIE> workingList,
													  List<Predicate> predicatesToAdd, Multimap<Predicate, CQIE> ruleIndex,
													  DatalogDependencyGraphGenerator depGraph) {
		for (int predIdx = 0; predIdx < predicatesToAdd.size() ; predIdx++) {
			Predicate pred = predicatesToAdd.get(predIdx);


			//Adding the  predicate
			Collection<CQIE> rulesToAdd2 = ruleIndex.get(pred);

			for (CQIE resultingRule: rulesToAdd2){
				if (!workingList.contains(resultingRule)){
					workingList.add(resultingRule);
				}
			}

			//Adding the  fathers
			List<Predicate> preFatherList =  depGraph.getFatherPredicates(pred);

			for (Predicate predFa: preFatherList){
				Collection<CQIE> rulesToAdd= ruleIndex.get(predFa);

				for (CQIE resultingRule: rulesToAdd){
					if (!workingList.contains(resultingRule)){
						workingList.add(resultingRule);
					}
				}
			}
		}
	}

	/**
	 * I add to the working list all the rules touched by the unfolder w.r.t. mappings
	 */
	private void addNewRules2WorkingListFromHeadAtoms(List<CQIE> workingList,
													  List<Predicate> predicatesToAdd, Multimap<Predicate, CQIE> ruleIndex) {
		for (int predIdx = 0; predIdx < predicatesToAdd.size() ; predIdx++) {
			Predicate pred = predicatesToAdd.get(predIdx);

			Collection<CQIE> rulesToAdd= ruleIndex.get(pred);

			for (CQIE resultingRule: rulesToAdd){
				if (!workingList.contains(resultingRule)){
					workingList.add(resultingRule);
				}
			}

		}
	}














	/**
	 * This method will update the indexes when the result of unfolding fatherRule is null.
	 */
	private boolean updateNullIndexes(Predicate pred, Predicate preFather, CQIE fatherRule, List<CQIE> workingList,
									  DatalogDependencyGraphGenerator depGraph) {


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
	 */
	private boolean updateIndexes(Predicate pred,
								  Predicate preFather, List<CQIE> result, CQIE fatherRule, List<CQIE> workingList,
								  DatalogDependencyGraphGenerator depGraph, Multimap<Predicate, Integer> multiTypedFunctionSymbolIndex) {
		//boolean hasPred = false;

		Multimap<Predicate, CQIE> ruleIndexByBody = depGraph.getRuleIndexByBodyPredicate();

		//Update MultipleTemplateList
		if (multiTypedFunctionSymbolIndex.containsKey(pred))
		{
			Function head = fatherRule.getHead();
			List<Function> ruleBody = fatherRule.getBody();
			Function oldFun = getAtomFromBody(pred, ruleBody);

			int oldIdx = multiTypedFunctionSymbolIndex.get(pred).iterator().next();
			Term t = oldFun.getTerm(oldIdx);

			//here I find the new index in the head
			int newIdx = -1;
			int count = 0;
			for (Term nt: head.getTerms()){
				if (t.equals(nt)){
					newIdx = count;
				}
				count++;
			}

			multiTypedFunctionSymbolIndex.removeAll(pred);
			//if the problematic term was in the head
			if (newIdx>-1){
				multiTypedFunctionSymbolIndex.put(preFather,newIdx);
			}

		}




		for (CQIE newquery : result) {
			Predicate ruleHead = newquery.getHead().getFunctionSymbol();
			boolean sameHeadFromFather = ruleHead.equals(preFather);
			if (!fatherRule.equals(newquery) && sameHeadFromFather){
				//Update the ruleIndex
				depGraph.removeRuleFromRuleIndex(preFather,fatherRule);
				depGraph.addRuleToRuleIndex(preFather, newquery);

				//Delete the rules from workingList that have been touched
				if (workingList.contains(fatherRule) && !result.contains(fatherRule)){
					workingList.remove(fatherRule);
				}

				//List<Term> bodyTerms = getBodyTerms(newquery);
				//Update the bodyIndex
				depGraph.removeOldRuleIndexByBodyPredicate(fatherRule);
				depGraph.updateRuleIndexByBodyPredicate(newquery);
			} else if (depGraph.getExtensionalPredicates().contains(ruleHead)){ //it means I added mappings to the result !!
				depGraph.addRuleToRuleIndex(pred, newquery);
			}


		} // end for queries in result
		//Determine if there is a need to keep unfolding pred




		if (ruleIndexByBody.get(pred).isEmpty()){
			return false; //I finish with pred I can move on
		}else if (result.contains(fatherRule)){
			return false; // otherwise it will loop for ever. I am in the case when a concept in the LJ has several mappings, or aggregates
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



	/**
	 * Returns the list functions inside the functions atoms
	 */
	private List<Function> getAtomFunctions(Function func) {

		List<Term> currentTerms = func.getTerms();
		List<Function> tempList = new LinkedList<Function>();

		for (Term a : currentTerms) {
			if (a instanceof Function){
				tempList.add((Function)a);
			}
		}
		return tempList;
	}


	/***
	 * Applies a resolution step over a non-boolean/non-algebra atom (i.e. data
	 * atoms). The resolution step will try to match the <strong>focused atom
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
	 * @param focusedAtom
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
	 * @param includingMappings
	 * @param parentIsAggr
	 * @param termidx
	 *            The location of the focustAtom in the currentlist
	 * @return <ul>
	 *         <li>null if there is no s whos head unifies with a, we return
	 *         null. </li><li>An empty list if the atom a is <strong>extensional
	 *         predicate (those that have no defining rules)</strong> or the
	 *         second data atom in a left join </li><li>a list with one ore more
	 *         rules otherwise</li>
	 *         <ul>
	 *
	 * @see org.semanticweb.ontop.owlrefplatform.core.basicoperations.UnifierUtilities
	 */
	private List<CQIE> resolveDataAtom(Predicate resolvPred, Function focusedAtom, CQIE rule, Stack<Integer> termidx,
									   int[] resolutionCount, boolean isLeftJoin, boolean isSecondAtomInLeftJoin,
									   boolean includingMappings, boolean parentIsAggr, Multimap<Predicate, CQIE> ruleIndex,
									   Multimap<Predicate, Integer> multiTypedFunctionSymbolIndex) {

		if (!focusedAtom.isDataFunction())
			throw new IllegalArgumentException("Cannot unfold a non-data atom: " + focusedAtom);

			/*
			 * Leaf predicates are ignored (as boolean or algebra predicates)
			 */
		Predicate pred = focusedAtom.getFunctionSymbol();
		if (extensionalPredicates.contains(pred) && !includingMappings) {
			// The atom is a leaf, that means that is a data atom that
			// has no resolvent rule, and marks the end points to compute
			// partial evaluations

			return Collections.emptyList();
		}

		Collection<CQIE> rulesDefiningTheAtom = new LinkedList<CQIE>();

		if (!includingMappings){
			// therefore we are doing bottom-up of the query

			if (ruleIndex.containsKey(pred) && !pred.equals(resolvPred)) {
				//Since it is not resolvPred, we are are not going to resolve it
				return Collections.emptyList();
			} else if (pred.equals(resolvPred)) {
				// I am doing bottom up here
				rulesDefiningTheAtom = ruleIndex.get(pred);
			}
		} else  if (pred.equals(resolvPred)) {
			// I am doing top-down
			//I am using the mappings
			rulesDefiningTheAtom = mappings.get(pred);

		} else if (!pred.equals(resolvPred)){
			return Collections.emptyList();
		}



		boolean hasOneMapping = true ;
		if (includingMappings && mappings.containsKey(resolvPred)){
			//hasOneMapping = mappings.get(resolvPred).size() < 2;
			hasOneMapping = mappings.get(resolvPred).size() == 1;
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
				CQIE newRuleWithNullBindings = generateNullBindingsForLeftJoin( rule, termidx);

				result = Lists.newArrayList(newRuleWithNullBindings);

//					result = new LinkedList<CQIE>();
//					result.add(newRuleWithNullBindings);
			}
		} else {
			boolean freeToFlatten = !isSecondAtomInLeftJoin && !parentIsAggr;
			if (hasOneMapping || freeToFlatten){

				//result = generateResolutionResultParent(parentRule, focusAtom, rule, termidx, resolutionCount,
				// rulesDefiningTheAtom, isLeftJoin, isSecondAtomInLeftJoin);
				result = generateResolutionResult(focusedAtom, rule, termidx, resolutionCount, rulesDefiningTheAtom,
						isLeftJoin, isSecondAtomInLeftJoin, multiTypedFunctionSymbolIndex);
			} else {
				boolean noUnfoldingNeededAddMappings = isSecondAtomInLeftJoin || parentIsAggr;
				if (!hasOneMapping && noUnfoldingNeededAddMappings) {
					// This case takes place when ans has only 1 definition, but the extensional atom have more than 1 mapping, and
					result = Lists.newArrayListWithExpectedSize(1 + rulesDefiningTheAtom.size() + 1);
					//result = new LinkedList<CQIE>();
					result.add(rule);
					result.addAll(rulesDefiningTheAtom);
				}
			}
		}


		//TODO: improve this... looks awkward
		if (result == null) {
			// this is the case for second atom in left join generating more
			// than one rull, we
			// must reutrn an empty result i ndicating its already a partial
			// evaluation.
			result = Collections.emptyList();
		} else if (result.size() == 0) {
			if (!isSecondAtomInLeftJoin)
				return null;
			else {
				CQIE newRuleWithNullBindings = generateNullBindingsForLeftJoin( rule, termidx);
//					result = new LinkedList<CQIE>();
//					result.add(newRuleWithNullBindings);
				result = Lists.newArrayList(newRuleWithNullBindings);

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
			Function newFunctionalTerm = termFactory.getFunction(newFunctionSymbol, newInnerTerms);
			newTerm = newFunctionalTerm;
		} else if (term instanceof Constant) {
			newTerm = term.clone();
		} else {
			throw new RuntimeException("Unsupported term: " + term);
		}
		return newTerm;

	}








	/***
	 * Goes through each term, and recursively each inner term trying to resolve
	 * each atom. Returns an empty list if the partial evaluation is completed
	 * (no atoms can be resolved and each atom is a leaf atom), null if there is
	 * at least one atom that is not leaf and can't be resolved, or a list with
	 * one or more queries if there was one atom that could be resolved against
	 * one or more rules. The list contains the result of the resolution steps
	 * against those rules.
	 * @param resolvPred
	 *
	 * @param currentTerms
	 * @param rule
	 * @param resolutionCount
	 * @param termidx
	 * 			a stack used to track the depth first searching (DFS) 
	 * @param includingMappings
	 * @return
	 */

	private List<CQIE> computePartialEvaluation(Predicate resolvPred, List<Term> currentTerms, CQIE rule, int[] resolutionCount, Stack<Integer> termidx,
												boolean parentIsLeftJoin, boolean includingMappings, Multimap<Predicate, CQIE> ruleIndex,
												Multimap<Predicate, Integer> multiTypedFunctionSymbolIndex) {

		int nonBooleanAtomCounter = 0;

		for (int atomIdx = 0; atomIdx < currentTerms.size(); atomIdx++) {
			termidx.push(atomIdx);

			Function focusedLiteral = (Function) currentTerms.get(atomIdx);

			if (focusedLiteral.isBooleanFunction() || focusedLiteral.isArithmeticFunction() || focusedLiteral.isDataTypeFunction()) {
				termidx.pop();
				continue;
			} else if (focusedLiteral.isAlgebraFunction()) {
				nonBooleanAtomCounter += 1;
                    /*
                     * These may contain data atoms that need to be unfolded, we
                     * need to recursively unfold each term.
                     */

				// for (int i = 0; i < focusLiteral.getTerms().size(); i++) {

				// TODO: check
				//parentIsLeftJoin = focusedLiteral.getFunctionSymbol().equals(OBDAVocabulary.SPARQL_LEFTJOIN);

				Predicate predicate = focusedLiteral.getFunctionSymbol();
				boolean isLeftJoinSecondArgument = (nonBooleanAtomCounter == 2) && parentIsLeftJoin;
				boolean focusedAtomIsLeftJoin = predicate == OBDAVocabulary.SPARQL_LEFTJOIN;


//                    if(focusedAtomIsLeftJoin && parentIsLeftJoin){
//                    	return emptyList;
//                    }

				List<CQIE> result = computePartialEvaluation(resolvPred, focusedLiteral.getTerms(),
						rule, resolutionCount, termidx, focusedAtomIsLeftJoin, includingMappings, ruleIndex,
						multiTypedFunctionSymbolIndex);

				if (result == null)
					if (!isLeftJoinSecondArgument){
						return null;
					}else{
						termidx.pop();
						log.debug("Empty evaluation - Data Function {}", focusedLiteral);
						CQIE newRuleWithNullBindings = generateNullBindingsForLeftJoin( rule, termidx);
						result = new LinkedList<CQIE>();
						result.add(newRuleWithNullBindings);
						return result;
					}
				if (result.size() > 0) {
					return result;
				}

			} else if (focusedLiteral.isDataFunction()) {
				nonBooleanAtomCounter += 1;

                    /*
                     * This is a data atom, it should be unfolded with the usual
                     * resolution algorithm.
                     */

				boolean isLeftJoinSecondArgument = (nonBooleanAtomCounter == 2) && parentIsLeftJoin;
				List<CQIE> result = null;
				Predicate pred = focusedLiteral.getFunctionSymbol();
				boolean parentIsAggr = false;

				//Here we check if the head has an aggregate, if it does, it should not be distributed over the union of mappings
				if (pred.equals(resolvPred)) {
					if (includingMappings){
						parentIsAggr= detectAggregateinSingleRule(rule);
					}
					result = resolveDataAtom(resolvPred, focusedLiteral, rule, termidx, resolutionCount, parentIsLeftJoin,
							isLeftJoinSecondArgument, includingMappings, parentIsAggr, ruleIndex,
							multiTypedFunctionSymbolIndex);
					if (result == null)
						return null;

					if (result.size() > 0)
						return result;
				}


			} else {
				throw new IllegalArgumentException(
						"Error during unfolding, trying to unfold a non-algrbra/non-data function. Offending atom: "
								+ focusedLiteral.toString());
			}
			termidx.pop();
		}

		return new LinkedList<CQIE>();
	}






	/***
	 * * Normalizes a rule that has multiple data atoms to a rule in which there
	 * is one single Join atom by creating a nested Join structure. Required to
	 * resolve atoms in LeftJoins (any, left or right) to avoid ending up with
	 * left joins with more than 2 table definitions. If the body contains only
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
			foldedJoinAtom = termFactory.getSPARQLJoin(dataAtomsList.remove(0), dataAtomsList.remove(0));
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
												Collection<CQIE> rulesDefiningTheAtom, boolean isLeftJoin, boolean isSecondAtomOfLeftJoin,
												Multimap<Predicate, Integer> multiTypedFunctionSymbolIndex) {

		List<CQIE> candidateMatches = new LinkedList<CQIE>(rulesDefiningTheAtom);
//		List<CQIE> result = new ArrayList<CQIE>(candidateMatches.size() * 2);
		List<CQIE> result = new LinkedList<CQIE>();

		int rulesGeneratedSoFar = 0;
		for (CQIE candidateRule : candidateMatches) {

			resolutionCount[0] += 1;
			/* getting a rule with unique variables */
			CQIE freshRule = getFreshRule(candidateRule, resolutionCount[0]);

			Substitution mgu = UnifierUtilities.getMGU(freshRule.getHead(), focusAtom);

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

			SubstitutionUtilities.applySubstitution(partialEvalution, mgu, false);

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




			//if (isSecondAtomOfLeftJoin && rulesGeneratedSoFar > 1 ) {
			//if (isSecondAtomOfLeftJoin ) {
			if (isLeftJoin) {
				// guohui: I changed it to not unfold inside the leftjoin, regardless of the position 
				
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


		if (result.size()<=1){
			return result;
		}else if (result.size()>1){ // THIS case is to check weather we cannot push this type
			boolean templateProblem = false;

			CQIE firstRule = (CQIE) result.get(0);
			Function newfocusAtom= firstRule.getHead();
			Predicate focusPred = newfocusAtom.getFunctionSymbol();

			for (int i=1; i<result.size(); i++){
				Function pickedAtom = null;
				CQIE tgt = (CQIE) result.get(i);
				pickedAtom=  tgt.getHead();

				//TODO: is this if needed??
				boolean found = false;
				for (int p=0;p<newfocusAtom.getArity();p++){

					Term t1 = newfocusAtom.getTerm(p);
					Term t2 = pickedAtom.getTerm(p);


					if ((t1 instanceof Function) && (t2 instanceof  Function )){

						templateProblem = checkFunctionTemplates(
								templateProblem, t1, t2);
					}else if ((t1 instanceof Variable) && (t2 instanceof  Variable )){
						continue;
					}else if ((t1 instanceof Constant) && (t2 instanceof  Constant )){
						continue;
					}else if ((t1 instanceof Function) && (t2 instanceof  Constant )){
						templateProblem = true;
					}else if ((t1 instanceof Constant ) && (t2 instanceof  Function )){
						templateProblem = true;
					}else if ((t1 instanceof Variable) && (t2 instanceof  Constant )){ // TODO: Maybe these 2 are too strict !!
						templateProblem = true;
					}else if ((t1 instanceof Constant ) && (t2 instanceof  Variable )){
						templateProblem = true;
					}


					if (templateProblem){
						if (!multiTypedFunctionSymbolIndex.containsEntry(focusPred,p)) {
							multiTypedFunctionSymbolIndex.put(focusPred,p);
							found = true;

						}
					}
				}//end for terms



			}//end for rules
		}



		return result;
	}

	/**
	 * @param templateProblem
	 * @param t1
	 * @param t2
	 * @return
	 */
	private boolean checkFunctionTemplates(boolean templateProblem, Term t1,
										   Term t2) {
		Function funct1 = (Function) t1;
		Function funct2 = (Function) t2;

		//it has different types
		if (!funct1.getFunctionSymbol().equals(funct2.getFunctionSymbol())){
			templateProblem = true;
		}

		//it has different templates regarding the variable


		//TODO: FIX ME!! super slow!! Literals have arity 1 always, even when they have 2 arguments!
		//See Test COmplex Optional Semantics

		//int arity = t1.getArity();
		int arity = funct1.getTerms().size();

		//						int arity2 = t2.getArity();
		int arity2 = funct2.getTerms().size();

		if (arity !=  arity2){
			templateProblem = true;
		}

		//it has different templates regarding the uri
		if (funct1.getFunctionSymbol().getName().equals(OBDAVocabulary.QUEST_URI)){
			Term string1 = funct1.getTerm(0);
			Term string2 = funct2.getTerm(0);
			if (!string1.equals(string2)){
				templateProblem = true;
			}
		}
		return templateProblem;
	}

	/**
	 * This will generate a new rule when the second argument of the LJ is empty.
	 *
	 */
	private CQIE generateNullBindingsForLeftJoin(CQIE originalRuleWithLeftJoin, Stack<Integer> termidx) {


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
		Substitution unifier = SubstitutionUtilities.getNullifier(variablesArg2);

		// Now I need to add the null to the variables of the second
		// LJ data argument
		freshRule = SubstitutionUtilities.applySubstitution(freshRule, unifier, false);
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

			Substitution mgu1 = null;
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
					mgu1 = UnifierUtilities.getMGU(newatom, tempatom);
					if (mgu1 != null) {
						replacement = tempatom;
						break;
					}
				}

			}

			if (replacement == null)
				continue;

			if (mgu1 == null)
				throw new RuntimeException("Unexpected case found while performing JOIN elimination. Contact the authors for debugging.");

			if (currentAtom.isAlgebraFunction() && currentAtom.getFunctionSymbol() == OBDAVocabulary.SPARQL_LEFTJOIN) {
				continue;
			}

			SubstitutionUtilities.applySubstitution(partialEvalution, mgu1, false);
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
		Multimap<Predicate,Integer> multiplePredIdx = ArrayListMultimap.create();
		unfold(query,targetPredicate,QuestConstants.TDOWN, false,multiplePredIdx);
		return null;
	}


	/**
	 * This method returns the predicates that define two different templates
	 */
	public Multimap<Predicate,Integer> processMultipleTemplatePredicates(List<CQIE> mappings) {

		Multimap<Predicate,Integer> multiTypedFunctionSymbolIndex = ArrayListMultimap.create();

		Multimap<Predicate, CQIE> ruleIndex;
		DatalogDependencyGraphGenerator depGraph;
		depGraph = new DatalogDependencyGraphGenerator(mappings);
		ruleIndex = depGraph.getRuleIndex();



		Predicate triple = termFactory.getPredicate("triple", 3);

		Set<Predicate> keySet = ruleIndex.keySet();
		//Not interested in triple predicates
		keySet.remove(triple);

		for(Predicate focusPred: keySet){

			List<CQIE> rules = (List<CQIE>) ruleIndex.get(focusPred);

			//if there is only one rule there cannot be two templates
			if (rules.size()==1){
				continue;
			}

			//There is more than 1 rule, we need to see if it uses different templates


			multiTypedFunctionSymbolIndex = detectMissmatchArgumentType(rules,true, ruleIndex, multiTypedFunctionSymbolIndex);

		} //end for predicates

		return multiTypedFunctionSymbolIndex;

	}


	/**
	 * This is a helper method that takes a set of rules and calculate wich argument have different types
	 * @param rules
	 */
	private Multimap<Predicate,Integer> detectMissmatchArgumentType(List<CQIE> rules, boolean isComputingMappings,
																	Multimap<Predicate, CQIE> ruleIndex,
																	Multimap<Predicate,Integer> multiTypedFunctionSymbolIndex) {

		List<Predicate> predList= new LinkedList<Predicate>();

		//I check all the different predicates in the head of each result. Usually it will be only 1
		//TODO: This can be done more efficiently by having a flag that is true when I add mappings to the result

		if (!isComputingMappings){
			for (CQIE rule: rules){
				Function focusAtom= rule.getHead();
				Predicate focusPred = focusAtom.getFunctionSymbol();
				if (!predList.contains(focusPred)){
					predList.add(focusPred);
				}
			}
		} else{
			CQIE firstRule = (CQIE) rules.get(0);
			Function focusAtom= firstRule.getHead();
			Predicate focusPred = focusAtom.getFunctionSymbol();
			predList.add(focusPred);
		}
		//TODO: Inefficeint !!!!! :( Fix!


		for (Predicate pred: predList){
			// I pick the pred atom in the body of the first rule

			List<CQIE> rulesubset =new LinkedList<CQIE>();
			if (!isComputingMappings){
				rulesubset = (List<CQIE> ) ruleIndex.get(pred);
			}else{
				rulesubset= rules;
			}

			CQIE firstRule = (CQIE) rulesubset.get(0);
			Function focusAtom= firstRule.getHead();
			Predicate focusPred = focusAtom.getFunctionSymbol();

			for (int i=1; i<rules.size(); i++){
				Function pickedAtom = null;
				CQIE tgt = (CQIE) rules.get(i);
				pickedAtom= tgt.getHead();
				Predicate focusPred2 = pickedAtom.getFunctionSymbol();
				if (!focusPred2.equals(pred)){
					continue;
				}




				//TODO: is this if needed??
				boolean found = false;
				for (int p=0;p<focusAtom.getArity();p++){
					boolean templateProblem = false;
					Term term1 = focusAtom.getTerm(p);
					Term term2 =  pickedAtom.getTerm(p);

					if ((term1 instanceof Function ) &&  (term2 instanceof Function )){
						Function t1 = (Function)term1;
						Function t2 = (Function)term2;




						//it has different types
						if (!t1.getFunctionSymbol().equals(t2.getFunctionSymbol())){
							templateProblem = true;
						}

						//it has different templates regarding the variable


						//TODO: FIX ME!! super slow!! Literals have arity 1 always, even when they have 2 arguments!
						//See Test COmplex Optional Semantics

						//int arity = t1.getArity();
						int arity = t1.getTerms().size();

						//						int arity2 = t2.getArity();
						int arity2 = t2.getTerms().size();

						if (arity !=  arity2){
							templateProblem = true;
						}

						//it has different templates regarding the uri
						if (t1.getFunctionSymbol().getName().equals(OBDAVocabulary.QUEST_URI)){
							Term string1 = t1.getTerm(0);
							Term string2 = t2.getTerm(0);
							if (!string1.equals(string2)){
								templateProblem = true;
							}
						}
					}
					else if ((term1 instanceof Variable ) &&  (term2 instanceof Constant)){
						templateProblem = true;
					}else if ((term2 instanceof Variable ) &&  (term1 instanceof Constant)){
						templateProblem = true;
					}
					/**
					 * Checks the constants
					 */
					else if ((term1 instanceof Constant) && (term2 instanceof Constant)) {
						/**
						 * They should be equal
						 */
						if (! term1.equals(term2))
							templateProblem = true;
					}

					if (templateProblem){
						if (!multiTypedFunctionSymbolIndex.containsEntry(focusPred,p)) {
							multiTypedFunctionSymbolIndex.put(focusPred,p);
							found = true;
							break;
						}
					}
				}//end for terms	



				if (found ==true){
					continue;
				}
			}//end for rules
		}
		return multiTypedFunctionSymbolIndex;
	}

	/**
	 * This is a helper method for processMultiplePredicates. 
	 * It returns the atom of a given predicate in a body of a rule
	 * @param bodyPred
	 * @param ruleBody
	 */
	private Function getAtomFromBody(Predicate bodyPred, List<Function> ruleBody) {
		Function focusAtom = null;
		for (Function atom: ruleBody ){
			if (atom.getFunctionSymbol().equals(bodyPred))
			{
				focusAtom = atom;
			}
		}
		return focusAtom;
	}

}
