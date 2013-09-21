package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.Graphs;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

/** Use to build a DAG and a named DAG.
 * Build on top of the SimpleDirectedGraph
 * 
 *  
 * @author Sarah
 *
 */

public class DAGImpl extends SimpleDirectedGraph <Description,DefaultEdge> implements DAG {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4466539952784531284L;
	
	boolean dag = false;
	boolean namedDAG = false;
	
	private Set<OClass> classes = new LinkedHashSet<OClass> ();
	private Set<Property> roles = new LinkedHashSet<Property> ();
	
	//map between an element  and the representative between the equivalent elements
	private Map<Description, Description> replacements = new HashMap<Description, Description>();
	
	//map of the equivalent elements of an element
	private Map<Description, Set<Description>> equivalencesMap = new HashMap<Description, Set<Description>>();

	public DAGImpl(Class<? extends DefaultEdge> arg0) {
		super(arg0);
		dag=true;
	}

	public DAGImpl(EdgeFactory<Description,DefaultEdge> ef) {
		super(ef);
		dag=true;
	}
	
	
	//set the graph is a dag
	public void setIsaDAG(boolean d){
		
		dag=d;
		namedDAG=!d;

	}

	//set the graph is a named description dag
	public void setIsaNamedDAG(boolean nd){
		
		namedDAG=nd;
		dag=!nd;

	}
	//check if the graph is a dag
	public boolean isaDAG(){
		return dag;

	}

	//check if the graph is a named description dag
	public boolean isaNamedDAG(){
		return namedDAG;


	}
	
	//return all property (not inverse) in the dag
	public Set<Property> getRoles(){
		for (Description r: this.vertexSet()){
			
			//check in the equivalent nodes if there are properties
			if(replacements.containsValue(r)){
			if(equivalencesMap.get(r)!=null){
				for (Description e: equivalencesMap.get(r))	{
					if (e instanceof Property){
//						System.out.println("roles: "+ e +" "+ e.getClass());
						if(!((Property) e).isInverse())
						roles.add((Property)e);
						
				}
				}
			}
			}
			if (r instanceof Property){
//				System.out.println("roles: "+ r +" "+ r.getClass());
				if(!((Property) r).isInverse())
				roles.add((Property)r);
			}

		}
		return roles;

	}

	
	//return all named classes in the dag
	public Set<OClass> getClasses(){
		for (Description c: this.vertexSet()){
			
			//check in the equivalent nodes if there are named classes
			if(replacements.containsValue(c)){
			if(equivalencesMap.get(c)!=null){
				
				for (Description e: equivalencesMap.get(c))	{
					if (e instanceof OClass){
//						System.out.println("classes: "+ e +" "+ e.getClass());
						classes.add((OClass)e);
				}
				}
			}
			}
			
			if (c instanceof OClass){
//				System.out.println("classes: "+ c+ " "+ c.getClass());
				classes.add((OClass)c);
			}

		}
		return classes;

	}


	@Override
	public Map<Description, Set<Description>> getMapEquivalences() {
		
		return equivalencesMap;
	}

	@Override
	public Map<Description, Description> getReplacements() {
		return replacements;
	}

	@Override
	public void setMapEquivalences(Map<Description, Set<Description>> equivalences) {
		this.equivalencesMap= equivalences;
		
	}

	@Override
	public void setReplacements(Map<Description, Description> replacements) {
		this.replacements=replacements;
		
	}
	
	@Override
	//return the node considering also the equivalent nodes
	public Description getNode(Description node){
		if(replacements.containsKey(node))
			node= replacements.get(node);
		else
		if(!this.vertexSet().contains(node))
			node=null;
		return node;
		
	}

//	
//	/***
//	 * Eliminates redundant edges to ensure that the remaining DAG is the
//	 * transitive reduction of the original DAG.
//	 * 
//	 * <p>
//	 * This is done with an ad-hoc algorithm that functions as follows:
//	 * 
//	 * <p>
//	 * Compute the set of all nodes with more than 2 outgoing edges (these have
//	 * candidate redundant edges.) <br>
//	 */
//	public void eliminateRedundantEdges() {
//		/* Compute the candidate nodes */
//		List<Description> candidates = new LinkedList<Description>();
//		Set<Description> vertexes = this.vertexSet();
//		for (Description vertex : vertexes) {
//			int outdegree = this.outDegreeOf(vertex);
//			if (outdegree > 1) {
//				candidates.add(vertex);
//			}
//		}
//
//		/*
//		 * for each candidate x and each outgoing edge x -> y, we will check if
//		 * y appears in the set of redundant edges
//		 */
//
//		for (Description candidate : candidates) {
//			
//			Set<DefaultEdge> possiblyRedundantEdges = new LinkedHashSet<DefaultEdge>();
//			
//			possiblyRedundantEdges.addAll(this.outgoingEdgesOf(candidate));
//			
//			Set<DefaultEdge> eliminatedEdges = new HashSet<DefaultEdge>();
//			
//			// registering the target of the possible redundant targets for this
//			// node
//			Set<Description> targets = new HashSet<Description>();
//			
//			Map<Description, DefaultEdge> targetEdgeMap = new HashMap<Description, DefaultEdge>();
//			
//			for (DefaultEdge edge : possiblyRedundantEdges) {
//				Description target = this.getEdgeTarget(edge);
//				targets.add(target);
//				targetEdgeMap.put(target, edge);
//			}
//
//			for (DefaultEdge currentPathEdge : possiblyRedundantEdges) {
//				Description currentTarget = this.getEdgeTarget(currentPathEdge);
//				if (eliminatedEdges.contains(currentPathEdge))
//					continue;
//				eliminateRedundantEdge(currentPathEdge, targets, targetEdgeMap, currentTarget, eliminatedEdges);
//			}
//
//		}
//
//	}
//
//	private void eliminateRedundantEdge(DefaultEdge safeEdge, Set<Description> targets, Map<Description, DefaultEdge> targetEdgeMap,
//			Description currentTarget, Set<DefaultEdge> eliminatedEdges) {
//		if (targets.contains(currentTarget)) {
//			DefaultEdge edge = targetEdgeMap.get(currentTarget);
//			if (!edge.equals(safeEdge)) {
//				/*
//				 * This is a redundant edge, removing it.
//				 */
//				this.removeEdge(edge);
//				eliminatedEdges.add(edge);
//			}
//		}
//
//		// continue traversing the dag up
//		Set<DefaultEdge> edgesInPath = this.outgoingEdgesOf(currentTarget);
//		for (DefaultEdge outEdge : edgesInPath) {
//			Description target = this.getEdgeTarget(outEdge);
//			eliminateRedundantEdge(safeEdge, targets, targetEdgeMap, target, eliminatedEdges);
//		}
//
//	}
//
//	/***
//	 * Eliminates all cycles in the graph by computing all strongly connected
//	 * components and eliminating all but one node in each of the components
//	 * from the graph. The result of this transformation is that the graph
//	 * becomes a DAG.
//	 * 
//	 * <p>
//	 * In the process two objects are generated, an 'Equivalence map' and a
//	 * 'replacementMap'. The first can be used to get the implied equivalences
//	 * of the TBox. The second can be used to locate the node that is
//	 * representative of an eliminated node.
//	 * 
//	 * <p>
//	 * Computation of the strongly connected components is done using the
//	 * StrongConnectivityInspector from JGraphT.
//	 * 
//	 */
//	public void eliminateCycles() {
//		
//
//		
//		StrongConnectivityInspector<Description, DefaultEdge> inspector = new StrongConnectivityInspector<Description, DefaultEdge>(this);
//		
//		//each set contains vertices which together form a strongly connected component within the given graph
//		List<Set<Description>> equivalenceSets = inspector.stronglyConnectedSets();
//
//
//		for (Set<Description> equivalenceSet : equivalenceSets) {
//			if (equivalenceSet.size() < 2)
//				continue;
//			Iterator<Description> iterator = equivalenceSet.iterator();
//			Description representative = iterator.next();
//			Description notRepresentative = null;
//			if(representative instanceof PropertySomeRestriction ) //I want to consider a named class as representative element
//				for (Description equivalent: equivalenceSet) {
//					if (equivalent instanceof OClass) {
//						notRepresentative =representative;
//						representative=equivalent;
//						replacements.put(notRepresentative, representative);
//						equivalencesMap.put(notRepresentative, equivalenceSet);
//						break;
//					}
//				}
//			equivalencesMap.put(representative, equivalenceSet);
//
//			while (iterator.hasNext()) {
//				Description eliminatedNode = iterator.next();
//				if(eliminatedNode.equals(representative) & notRepresentative!=null)
//					eliminatedNode=notRepresentative;
//				replacements.put(eliminatedNode, representative);
//				equivalencesMap.put(eliminatedNode, equivalenceSet);
//				
//				/*
//				 * Re-pointing all links to and from the eliminated node to the
//				 * representative node
//				 */
//
//				Set<DefaultEdge> edges = new HashSet<DefaultEdge>(this.incomingEdgesOf(eliminatedNode));
//
//				for (DefaultEdge incEdge : edges) {
//					Description source = this.getEdgeSource(incEdge);
//
//					this.removeAllEdges(source, eliminatedNode);
//
//					if (source.equals(representative))
//						continue;
//
//					this.addEdge(source, representative);
//				}
//
//				edges = new HashSet<DefaultEdge>(this.outgoingEdgesOf(eliminatedNode));
//
//				for (DefaultEdge outEdge : edges) {
//					Description target = this.getEdgeTarget(outEdge);
//
//					this.removeAllEdges(eliminatedNode, target);
//
//					if (target.equals(representative))
//						continue;
//					this.addEdge(representative, target);
//
//				}
//
//				this.removeVertex(eliminatedNode);
//
//			}
//		}
//		
//	}
//
//		






}
