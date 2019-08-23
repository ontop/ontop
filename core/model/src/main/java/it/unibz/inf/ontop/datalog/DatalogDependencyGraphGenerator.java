package it.unibz.inf.ontop.datalog;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;


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

	private final DirectedGraph<Predicate, DefaultEdge> predicateDependencyGraph
			= new DefaultDirectedGraph<>(DefaultEdge.class);

	private final DirectedGraph<CQIE, DefaultEdge> ruleDependencyGraph
            = new DefaultDirectedGraph<>(DefaultEdge.class);

	/***
	 * Map of predicate to the rules defining it
	 */
	private final Multimap<Predicate, CQIE> ruleIndex = ArrayListMultimap.create();

	private final Multimap<Predicate, CQIE> ruleIndexByBodyPredicate = ArrayListMultimap.create();
	
	
	/**
	 * the predicates in the datalog program without definition
	 */
	private final List<Predicate> extensionalPredicates = new ArrayList<>();
	
	/**
	 * Bottom-up Ordered List of predicates
	 */
	private final List<Predicate> predicatesInBottomUp = new ArrayList<>();
	
	

	public Multimap<Predicate, CQIE> getRuleIndex() {
		return ruleIndex;
	}

	public List<Predicate> getExtensionalPredicates() {
		return extensionalPredicates;
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
		 * Intuitively, the extensional predicates in the datalog program without
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
			} else if (bodyAtom.isAlgebraFunction() || bodyAtom.isOperation()) {
				updateRuleIndexByBodyPredicate_traverseBodyAtom(rule, bodyAtom);
			// BC: should we reintroduce arithmetic functions?
			//} else if (bodyAtom.isArithmeticFunction() || bodyAtom.isDataTypeFunction()){
			} else if (bodyAtom.isDataTypeFunction()){
				continue;
			} else {
				throw new IllegalStateException("Unknown Function");
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

		Queue<Term> queueInAtom = new LinkedList<>();

		queueInAtom.add(bodyAtom);
		while (!queueInAtom.isEmpty()) {
			Term queueHead = queueInAtom.poll();
			if (queueHead instanceof Function) {
				Function funcRoot = (Function) queueHead;
				
				if (funcRoot.isOperation()
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

		List<Predicate> dependencyList = new LinkedList<>();

		for (Function bodyAtom : rule.getBody()) {

			if (bodyAtom.isDataFunction()) {
				dependencyList.add(bodyAtom.getFunctionSymbol());
			} else if (bodyAtom.isAlgebraFunction() || bodyAtom.isOperation()) {
				updatePredicateDependencyGraph_traverseBodyAtom(dependencyList, bodyAtom);
				//} else if (bodyAtom.isArithmeticFunction() || bodyAtom.isDataTypeFunction() {
			} else if (bodyAtom.isDataTypeFunction()) {
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
				
				if (funcRoot.isOperation()
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

}
