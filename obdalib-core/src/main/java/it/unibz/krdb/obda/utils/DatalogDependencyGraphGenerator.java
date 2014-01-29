package it.unibz.krdb.obda.utils;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap; 


/***
 * 
 * This class generates two dependency graphs for the datalog program
 * <ul>
 * <li>Predicate dependency graph with Predicate as nodes</li>
 * <li>Rule dependency graph with Rule as nodes</li>
 * </ul>
 * 
 * add some index
 * <ul>
 * <li>Rule Index by Head Predicate</li>
 * <li>Rule Index by Body Predicate</li>
 * <li>extensionalPredicates</li>
 * </ul>
 * 
 * @author xiao, mrezk
 * 
 */
public class DatalogDependencyGraphGenerator {

	private DirectedGraph<Predicate, DefaultEdge> predicateDependencyGraph = new DefaultDirectedGraph<Predicate, DefaultEdge>(
			DefaultEdge.class);

	private DirectedGraph<CQIE, DefaultEdge> ruleDependencyGraph = new DefaultDirectedGraph<CQIE, DefaultEdge>(
			DefaultEdge.class);

	/***
	 * Map of predicate to the rules defining it
	 */
	private Multimap<Predicate, CQIE> ruleIndex = ArrayListMultimap.create();

	private Multimap<Predicate, CQIE> ruleIndexByBodyPredicate = ArrayListMultimap.create();
	
	
	/**
	 * the predicates in the datalog program without definition
	 */
	private List<Predicate> extensionalPredicates = new ArrayList<Predicate>();
	
	/**
	 * Bottom-up Ordered List of predicates
	 */
	private List<Predicate> predicatesInBottomUp = new ArrayList<Predicate>();
	
	
	public DirectedGraph<Predicate, DefaultEdge> getPredicateDependencyGraph() {
		return predicateDependencyGraph;
	}

	public DirectedGraph<CQIE, DefaultEdge> getRuleDependencyGraph() {
		return ruleDependencyGraph;
	}
	
	/**
	 * Gives you the predicate that is defined using <code>pred</code> 
	 * @param pred 
	 * @return
	 */
	public Predicate getFatherPredicate(Predicate pred){
		Set<DefaultEdge> fatherEdges = predicateDependencyGraph.incomingEdgesOf(pred);
		DefaultEdge fatherEdge = fatherEdges.iterator().next();
		Predicate father = predicateDependencyGraph.getEdgeSource(fatherEdge);
		return father;
	}
	
	/**
	 * Gives all the rules that depends on the input <code>rule</code> 
	 * @param rule 
	 * @return
	 */
	public List<CQIE> getFatherRules(CQIE rule){
		Set<DefaultEdge> fatherEdges = ruleDependencyGraph.incomingEdgesOf(rule);
		
		
		List<CQIE> fatherRules = Lists.newArrayListWithCapacity(fatherEdges.size());
		
		for(DefaultEdge edge : fatherEdges){
			fatherRules.add(ruleDependencyGraph.getEdgeSource(edge));
		}
		
		return fatherRules;
	}
	
	public Multimap<Predicate, CQIE> getRuleIndex() {
		return ruleIndex;
	}

	public List<Predicate> getExtensionalPredicates() {
		return extensionalPredicates;
	}
	
	

	public DatalogDependencyGraphGenerator(DatalogProgram program) {
		this(program.getRules());
	}


	public DatalogDependencyGraphGenerator(List<CQIE> program) {
		for (CQIE rule : program) {

			updateRuleIndexes(rule);
			
			updatePredicateDependencyGraph(rule);
		}

		generateRuleDependencyGraph(program);
		
		generateOrderedDepGraph();

		/**
		 * 
		 * Intuitively, the extenion predicates in the datalog program without
		 * definitions 
		 * 
		 * <pre>
		 * extensionalPredicates = vertices(predicateDependencyGraph) - ruleIndex.keys()
		 * </pre>
		 */
		extensionalPredicates.addAll(predicateDependencyGraph.vertexSet());
		extensionalPredicates.removeAll(ruleIndex.keySet());

	}
	
	/**
	 * This method takes a rule and populates the ruleIndex field.
	 * @param rule
	 */
	private void updateRuleIndexes(CQIE rule) {
		Function head = rule.getHead();
		
		ruleIndex.put(head.getFunctionSymbol(), rule);
		
		updateRuleIndexByBodyPredicate(rule);
	}

	/**
	 * updates 
	 * 
	 * @param rule
	 */
	public void updateRuleIndexByBodyPredicate(CQIE rule) {
		for (Function bodyAtom : rule.getBody()) {

			if (bodyAtom.isDataFunction()) {
				Predicate functionSymbol = bodyAtom.getFunctionSymbol();
				if (!ruleIndexByBodyPredicate.containsEntry(functionSymbol, rule)){
					ruleIndexByBodyPredicate.put(functionSymbol, rule);
				}
			} else if (bodyAtom.isAlgebraFunction() || bodyAtom.isBooleanFunction()) {
				updateRuleIndexByBodyPredicate_traverseBodyAtom(rule, bodyAtom);
			} else if (bodyAtom.isArithmeticFunction() || bodyAtom.isDataTypeFunction()){
				continue;
			} else {
				throw new IllegalStateException("Unknown Function");
			}
		}
	}
	
	
	
	/**
	 * Removes the old indexes given by a rule.
	 * 
	 * @param rule
	 */
	public void removeOldRuleIndexByBodyPredicate(CQIE rule) {
		for (Function bodyAtom : rule.getBody()) {

			if (bodyAtom.isDataFunction()) {
				Predicate functionSymbol = bodyAtom.getFunctionSymbol();
				if (ruleIndexByBodyPredicate.containsEntry(functionSymbol, rule)){
					ruleIndexByBodyPredicate.remove(functionSymbol, rule);
				}
			} else if (bodyAtom.isAlgebraFunction() || bodyAtom.isBooleanFunction()) {
				removeRuleIndexByBodyPredicate_traverseBodyAtom(rule, bodyAtom);
			} else if (bodyAtom.isArithmeticFunction() || bodyAtom.isDataTypeFunction()){
				continue;
			} else {
				throw new IllegalStateException("Unknown Function");
			}
		}
	}

	
	
	/**
	 * 
	 * This is a helper method for {@link #removeRuleIndexByBodyPredicate}.
	 * 
	 * This method traverses in an atom, and removes the predicates  in the
	 * bodyIndex
	 * @param rule 
	 * 
	 * 
	 * @param bodyAtom
	 */
	private void removeRuleIndexByBodyPredicate_traverseBodyAtom(
			CQIE rule, Function bodyAtom) {

		Queue<Term> queueInAtom = new LinkedList<Term>();

		queueInAtom.add(bodyAtom);
		while (!queueInAtom.isEmpty()) {
			Term queueHead = queueInAtom.poll();
			if (queueHead instanceof Function) {
				Function funcRoot = (Function) queueHead;
				
				if (funcRoot.isBooleanFunction() || funcRoot.isArithmeticFunction() 
						|| funcRoot.isDataTypeFunction() || funcRoot.isAlgebraFunction()) {
					for (Term term : funcRoot.getTerms()) {
						queueInAtom.add(term);
					}
				}  else if (funcRoot.isDataFunction()) {
					ruleIndexByBodyPredicate.remove(funcRoot.getFunctionSymbol(), rule);
				}
			} else {
				// NO-OP
			}

		}

	}	
	
	
	
	
	
	/**
	 * 
	 * This is a helper method for {@link #updateRuleIndexByBodyPredicate}.
	 * 
	 * This method traverses in an atom, and put the predicates "ansi" to the
	 * dependentList
	 * @param rule 
	 * 
	 * 
	 * @param bodyAtom
	 */
	private void updateRuleIndexByBodyPredicate_traverseBodyAtom(
			CQIE rule, Function bodyAtom) {

		Queue<Term> queueInAtom = new LinkedList<Term>();

		queueInAtom.add(bodyAtom);
		while (!queueInAtom.isEmpty()) {
			Term queueHead = queueInAtom.poll();
			if (queueHead instanceof Function) {
				Function funcRoot = (Function) queueHead;
				
				if (funcRoot.isBooleanFunction() || funcRoot.isArithmeticFunction() 
						|| funcRoot.isDataTypeFunction() || funcRoot.isAlgebraFunction()) {
					for (Term term : funcRoot.getTerms()) {
						queueInAtom.add(term);
					}
				}  else if (funcRoot.isDataFunction()) {
					if (!ruleIndexByBodyPredicate.containsEntry(funcRoot.getFunctionSymbol(), rule)){
						ruleIndexByBodyPredicate.put(funcRoot.getFunctionSymbol(), rule);
					}
				}
			} else {
				// NO-OP
			}

		}

	}

	/***
	 * 
	 * generates the {@link #ruleDependencyGraph}
	 * 
	 * 
	 * @param program
	 */
	private void generateRuleDependencyGraph(List<CQIE> program) {
		for (CQIE rule : program) {
			ruleDependencyGraph.addVertex(rule);
			Predicate headPred = rule.getHead().getFunctionSymbol();
			Set<DefaultEdge> ontgoingEdges = predicateDependencyGraph
					.outgoingEdgesOf(headPred);

			for (DefaultEdge e : ontgoingEdges) {
				Predicate edgeTarget = predicateDependencyGraph
						.getEdgeTarget(e);
				Collection<CQIE> rules = ruleIndex.get(edgeTarget);
				if (rules != null) {
					for (CQIE dependentRule : rules) {
						ruleDependencyGraph.addVertex(dependentRule);
						ruleDependencyGraph.addEdge(rule, dependentRule);
					}
				}
			}
		}
	}
	
	/**
	 * It removes all rules of a given predicate from the <code>ruleIndex</code>.
	 * Be careful. This method may cause a mismatch between the graph and the indexes.
	 * @return 
	 */
	public void removeAllPredicateFromRuleIndex(Predicate pred) {
		
		ruleIndex.removeAll(pred);
		
	}
	
	/**
	 * It removes a single given rule <code>rule<code> mapped to the predicate <code>pred</code>
	 * Be careful. This method may cause a mismatch between the graph and the indexes.
	 * @param pred
	 * @param rule
	 */
	public void removeRuleFromRuleIndex(Predicate pred, CQIE rule) {
		if (ruleIndex.containsEntry(pred, rule)){
			ruleIndex.remove(pred, rule);
		}
	}
	

	
	/**
	 * Adds a rule to the <code>ruleIndex</code>
	 * Be careful. This method may cause a mismatch between the graph and the indexes.
	 * @return 
	 */
	public void addRuleToRuleIndex(Predicate pred, CQIE rule) {
		if (!ruleIndex.containsEntry(pred, rule)){
			ruleIndex.put(pred, rule);
		}
	}
	
	
	
	

	
	/**
	 * Updates the {@link #predicateDependencyGraph} by the input rule.
	 * 
	 * It adds all the edges <rule.head.pred, p> to
	 * {@link #predicateDependencyGraph}, for all the p in the predicates of the
	 * rule body
	 * 
	 * 
	 * @param rule
	 */
	private void updatePredicateDependencyGraph(CQIE rule) {

		List<Predicate> dependencyList = new LinkedList<Predicate>();

		for (Function bodyAtom : rule.getBody()) {

			if (bodyAtom.isDataFunction()) {
				dependencyList.add(bodyAtom.getFunctionSymbol());
			} else if (bodyAtom.isAlgebraFunction() || bodyAtom.isBooleanFunction()) {
				updatePredicateDependencyGraph_traverseBodyAtom(dependencyList, bodyAtom);
			} else if (bodyAtom.isArithmeticFunction() || bodyAtom.isDataTypeFunction()){
				continue;
			} else {
				throw new IllegalStateException("Unknown Function");
			}
		}

		Predicate headPred = rule.getHead().getFunctionSymbol();
		predicateDependencyGraph.addVertex(headPred);

		for (Predicate dependentPred : dependencyList) {
			predicateDependencyGraph.addVertex(dependentPred);
			predicateDependencyGraph.addEdge(headPred, dependentPred);
		}
	}

	
	
	
	/**
	 * This method will 
	 * <ul>
	 * <li>Order the {@link #predicateDependencyGraph} using a top down approach</li>
	 * <li>Then reverse the list and add them into the field {@link #predicatesInBottomUp} </li>
	 * </ul>
	 */
	private void generateOrderedDepGraph() {
		TopologicalOrderIterator<Predicate, DefaultEdge> iter =
				new TopologicalOrderIterator<Predicate, DefaultEdge>(predicateDependencyGraph);
		
		while (iter.hasNext()){
			Predicate pred = iter.next();
			predicatesInBottomUp.add(pred);
		}
		Collections.reverse(predicatesInBottomUp);
	}

	/**
	 * 
	 * This is a helper method for {@link #updatePredicateDependencyGraph}.
	 * 
	 * This method traverses in an atom, and put the predicates "ansi" to the
	 * dependentList
	 * 
	 * 
	 * @param dependentList
	 *            dependentList will be updated in the mothod
	 * 
	 * @param bodyAtom
	 */
	private void updatePredicateDependencyGraph_traverseBodyAtom(
			List<Predicate> dependentList, Function bodyAtom) {

		Queue<Term> queueInAtom = new LinkedList<Term>();

		queueInAtom.add(bodyAtom);
		while (!queueInAtom.isEmpty()) {
			Term queueHead = queueInAtom.poll();
			if (queueHead instanceof Function) {
				Function funcRoot = (Function) queueHead;
				
				if (funcRoot.isBooleanFunction() || funcRoot.isArithmeticFunction() 
						|| funcRoot.isDataTypeFunction() || funcRoot.isAlgebraFunction()) {
					for (Term term : funcRoot.getTerms()) {
						queueInAtom.add(term);
					}
				}  else if (funcRoot.isDataFunction()) {
					dependentList.add(funcRoot.getFunctionSymbol());
				}
			} else {
				// NO-OP
			}

		}

	}
	
	/**
	 * Returns the Bottom-up Ordered List of predicates
	 * 
	 * @return List with Predicate elements
	 */
	public List<Predicate> getPredicatesInBottomUp() {
		return predicatesInBottomUp;
	}

	/**
	 * @return the ruleIndexByBodyPredicate
	 */
	public Multimap<Predicate, CQIE> getRuleIndexByBodyPredicate() {
		return ruleIndexByBodyPredicate;
	}
	

	/**
	 * Adds a rule to the <code>ruleIndexByBodyPredicate</code>
	 * Be careful. This method may cause a missmatch between the graph and the indexes.
	 * @return 
	 */
	public void addRuleToBodyIndex(Predicate pred, CQIE rule) {
		if (!ruleIndexByBodyPredicate.containsEntry(pred, rule)){
			ruleIndexByBodyPredicate.put(pred, rule);		
		}
	}

	
	
	/**
	 * It removes a single given rule <code>rule<code> mapped to the predicate <code>pred</code> from <code>ruleIndexByBodyPredicate</code>
	 * Be careful. This method may cause a mismatch between the graph and the indexes.
	 * @param pred
	 * @param rule
	 */
	public void removeRuleFromBodyIndex(Predicate pred, CQIE rule) {
		if (ruleIndexByBodyPredicate.containsKey(pred)){
			ruleIndexByBodyPredicate.remove(pred, rule);
		}else {
			System.err.println("No Such Key as"+pred);
		}
	}
	



}
