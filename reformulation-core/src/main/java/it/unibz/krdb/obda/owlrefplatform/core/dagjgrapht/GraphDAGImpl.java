package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;


import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultEdge;

/** 
 * Starting from a graph build a DAG. 
 * Consider equivalences, redundancies and transitive reduction */

public class GraphDAGImpl implements GraphDAG{

	//contains the representative node with the set of equivalent mappings.
	private Map<Description, Set<Description>> equivalencesMap = new HashMap<Description, Set<Description>>();

	/***
	 * A map to keep the relationship between 'eliminated' and 'remaining'
	 * nodes, created while computing equivalences by eliminating cycles in the
	 * graph.
	 */
	private Map<Description, Description> replacements = new HashMap<Description, Description>();

	private DAGImpl dag= new DAGImpl(DefaultEdge.class);
	
	//graph transformed in a dag
	private GraphImpl modifiedGraph;

	
	public GraphDAGImpl (Graph graph){
		
		
		modifiedGraph= new GraphImpl( DefaultEdge.class);
		
		
		//clone all the vertex and edges from dag
		
		for (Description v: ((GraphImpl)graph).vertexSet()){
			modifiedGraph.addVertex(v);
			
		}
		 for (DefaultEdge e : ((GraphImpl)graph).edgeSet()) {
	            Description s = ((GraphImpl)graph).getEdgeSource(e);
	            Description t = ((GraphImpl)graph).getEdgeTarget(e);
	            
	           
	            modifiedGraph.addEdge(s, t, e);
	        }
		eliminateCycles(graph);
		eliminateRedundantEdges();
		
//		System.out.println("modified graph "+modifiedGraph);
		
		dag= new DAGImpl( DefaultEdge.class);
		
		//change the graph in a dag
		Graphs.addGraph(dag, modifiedGraph);

		dag.setMapEquivalences(equivalencesMap);
		dag.setReplacements(replacements);
		dag.setIsaDAG(true);
		
	}


	@Override
	public DAG getDAG() {

		return dag;
	}


	/***
	 * Eliminates redundant edges to ensure that the remaining DAG is the
	 * transitive reduction of the original DAG.
	 * 
	 * <p>
	 * This is done with an ad-hoc algorithm that functions as follows:
	 * 
	 * <p>
	 * Compute the set of all nodes with more than 2 outgoing edges (these have
	 * candidate redundant edges.) <br>
	 */
	private void eliminateRedundantEdges() {
		/* Compute the candidate nodes */
		List<Description> candidates = new LinkedList<Description>();
		Set<Description> vertexes = modifiedGraph.vertexSet();
		for (Description vertex : vertexes) {
			int outdegree = modifiedGraph.outDegreeOf(vertex);
			if (outdegree > 1) {
				candidates.add(vertex);
			}
		}

		/*
		 * for each candidate x and each outgoing edge x -> y, we will check if
		 * y appears in the set of redundant edges
		 */

		for (Description candidate : candidates) {
			
			Set<DefaultEdge> possiblyRedundantEdges = new LinkedHashSet<DefaultEdge>();
			
			possiblyRedundantEdges.addAll(modifiedGraph.outgoingEdgesOf(candidate));
			
			Set<DefaultEdge> eliminatedEdges = new HashSet<DefaultEdge>();
			
			// registering the target of the possible redundant targets for this
			// node
			Set<Description> targets = new HashSet<Description>();
			
			Map<Description, DefaultEdge> targetEdgeMap = new HashMap<Description, DefaultEdge>();
			
			for (DefaultEdge edge : possiblyRedundantEdges) {
				Description target = modifiedGraph.getEdgeTarget(edge);
				targets.add(target);
				targetEdgeMap.put(target, edge);
			}

			for (DefaultEdge currentPathEdge : possiblyRedundantEdges) {
				Description currentTarget = modifiedGraph.getEdgeTarget(currentPathEdge);
				if (eliminatedEdges.contains(currentPathEdge))
					continue;
				eliminateRedundantEdge(currentPathEdge, targets, targetEdgeMap, currentTarget, eliminatedEdges);
			}

		}

	}

	private void eliminateRedundantEdge(DefaultEdge safeEdge, Set<Description> targets, Map<Description, DefaultEdge> targetEdgeMap,
			Description currentTarget, Set<DefaultEdge> eliminatedEdges) {
		if (targets.contains(currentTarget)) {
			DefaultEdge edge = targetEdgeMap.get(currentTarget);
			if (!edge.equals(safeEdge)) {
				/*
				 * This is a redundant edge, removing it.
				 */
				modifiedGraph.removeEdge(edge);
				eliminatedEdges.add(edge);
			}
		}

		// continue traversing the dag up
		Set<DefaultEdge> edgesInPath = modifiedGraph.outgoingEdgesOf(currentTarget);
		for (DefaultEdge outEdge : edgesInPath) {
			Description target = modifiedGraph.getEdgeTarget(outEdge);
			eliminateRedundantEdge(safeEdge, targets, targetEdgeMap, target, eliminatedEdges);
		}

	}

	/***
	 * Eliminates all cycles in the graph by computing all strongly connected
	 * components and eliminating all but one node in each of the components
	 * from the graph. The result of this transformation is that the graph
	 * becomes a DAG.
	 * 
	 * <p>
	 * In the process two objects are generated, an 'Equivalence map' and a
	 * 'replacementMap'. The first can be used to get the implied equivalences
	 * of the TBox. The second can be used to locate the node that is
	 * representative of an eliminated node.
	 * 
	 * <p>
	 * Computation of the strongly connected components is done using the
	 * StrongConnectivityInspector from JGraphT.
	 * 
	 */
	private void eliminateCycles(Graph graph) {
		
		
		StrongConnectivityInspector<Description, DefaultEdge> inspector = new StrongConnectivityInspector<Description, DefaultEdge>(modifiedGraph);
		
		//each set contains vertices which together form a strongly connected component within the given graph
		List<Set<Description>> equivalenceSets = inspector.stronglyConnectedSets();


//		List<Set<Description>> equivalenceClassSets = new LinkedList<Set<Description>>();
		OntologyFactory fac = OntologyFactoryImpl.getInstance();

		
		/*
		 * A set with all the nodes that have been proceesed as participating in
		 * an equivalence cycle. If a component contains any of these nodes, the
		 * component should be ignored, since a cycle involving the same nodes
		 * or nodes for inverse descriptions has already been processed.
		 */
		Set<Description> processedNodes = new HashSet<Description>();

		for (Set<Description> equivalenceSet : equivalenceSets) {

			if (equivalenceSet.size() < 2)
				continue;


			/*
			 * Avoiding processing nodes two times, but assign the equivalentMap
			 */
			boolean ignore = false;
			for (Description node : equivalenceSet) {
				
				if(node.equals(fac.createObjectProperty("http://www.owl-ontologies.com/Ontology1207768242.owl#involvesInstrument")))
					System.out.println("Here!");
				
				if(node.equals(fac.createObjectProperty("http://www.owl-ontologies.com/Ontology1207768242.owl#isTradedIn", true)))
					System.out.println("Here!");
				
				if( node.equals(fac.createPropertySomeRestriction(fac.createObjectProperty("http://www.owl-ontologies.com/Ontology1207768242.owl#involvesInstrument").getPredicate(), false)))
					System.out.println("Here!");
				
				if( node.equals(fac.createPropertySomeRestriction(fac.createObjectProperty("http://www.owl-ontologies.com/Ontology1207768242.owl#isTradedIn").getPredicate(), true)))
					System.out.println("Here!");
					
				if (!ignore && processedNodes.contains(node)) {
					ignore = true;
					
				
//					break;
					
					
				}
				
				if(!(node instanceof Property)){
					ignore = true;
//					break;
				}

			}
			if (ignore)
				continue;

			Iterator<Description> iterator = equivalenceSet.iterator();
			Description representative = iterator.next();
			Description notRepresentative = null;


				
					//if it is inverse I search a not inverse element as representative
					if(((Property) representative).isInverse()){
						boolean notInverse=false;
						for (Description equivalent: equivalenceSet) {
							if (equivalent instanceof Property && !((Property)equivalent).isInverse()) {
								notRepresentative =representative;
								representative=equivalent;
								notInverse=true;
//								replacements.put(notRepresentative, representative);
//								equivalencesMap.put(notRepresentative, equivalenceSet);
								break;
							}
					}
						if(!notInverse)
						representative=null; //if all inverse they will be added later in another set with property
					}
					
								
								


			equivalencesMap.put(representative, equivalenceSet);
			

			if(representative==null)
				continue;
			
			Property prop = (Property) representative;

			Description inverse = fac.createProperty(prop.getPredicate(), !prop.isInverse());
			
			
			Description domain = fac.createPropertySomeRestriction(prop.getPredicate(), prop.isInverse());
			
			
			Description range = fac.createPropertySomeRestriction(prop.getPredicate(), !prop.isInverse());
			
			processedNodes.add(inverse);
			processedNodes.add(domain);
			processedNodes.add(range);
			
			processedNodes.add(representative);
			
			Set<Description> inverseMap= new HashSet<Description>();
			Set<Description> existMap= new HashSet<Description>();
			Set<Description> existInverseMap= new HashSet<Description>();
			existInverseMap.add(range);
			existMap.add(domain);
			inverseMap.add(inverse);
			
			while (iterator.hasNext() ) {
				Description eliminatedNode = iterator.next();
				if(eliminatedNode.equals(representative) & notRepresentative!=null)
					eliminatedNode=notRepresentative;
				replacements.put(eliminatedNode, representative);

				equivalencesMap.put(eliminatedNode, equivalenceSet);
				
				
				/*
				 * Re-pointing all links to and from the eliminated node to the
				 * representative node
				 */

				Set<DefaultEdge> edges = new HashSet<DefaultEdge>(modifiedGraph.incomingEdgesOf(eliminatedNode));

				for (DefaultEdge incEdge : edges) {
					Description source = modifiedGraph.getEdgeSource(incEdge);

					modifiedGraph.removeAllEdges(source, eliminatedNode);

					if (source.equals(representative))
						continue;

					modifiedGraph.addEdge(source, representative);
				}

				edges = new HashSet<DefaultEdge>(modifiedGraph.outgoingEdgesOf(eliminatedNode));

				for (DefaultEdge outEdge : edges) {
					Description target = modifiedGraph.getEdgeTarget(outEdge);

					modifiedGraph.removeAllEdges(eliminatedNode, target);

					if (target.equals(representative))
						continue;
					modifiedGraph.addEdge(representative, target);

				}

				modifiedGraph.removeVertex(eliminatedNode);
				
				processedNodes.add(eliminatedNode);

				
				if(eliminatedNode instanceof Property){
					
					/*
					 * we are dealing with properties, so we need to also
					 * collapse the inverses and existentials
					 */
						
					
					
					Property equiprop = (Property) eliminatedNode;

						Description equivinverseNode = fac.createProperty(equiprop.getPredicate(), !equiprop.isInverse());
//						Description i= replacements.get(equivinverseNode);
//						replacements.put(equivinverseNode, inverse);
//						if(i!=null)
//							equivinverseNode=i;
						
						Description equivDomainNode = fac.createPropertySomeRestriction(equiprop.getPredicate(), equiprop.isInverse());
//						Description d= replacements.get(equivDomainNode);
//						replacements.put(equivDomainNode, domain);
//						if(d!=null)
//							equivDomainNode=  d;
						if( equivDomainNode.equals(fac.createPropertySomeRestriction(fac.createObjectProperty("http://www.owl-ontologies.com/Ontology1207768242.owl#involvesInstrument").getPredicate(), false)))
							System.out.println("Here!");
						
						Description equivRangeNode = fac.createPropertySomeRestriction(equiprop.getPredicate(), !equiprop.isInverse());
//						Description r= replacements.get(equivRangeNode);
//						replacements.put(equivRangeNode, range);
//						if(r!=null)
//							equivRangeNode=  r;
						if( equivRangeNode.equals(fac.createPropertySomeRestriction(fac.createObjectProperty("http://www.owl-ontologies.com/Ontology1207768242.owl#involvesInstrument").getPredicate(), false)))
							System.out.println("Here!");
//						
						processedNodes.add(equivRangeNode);
						existInverseMap.add(equivRangeNode);
						
						processedNodes.add(equivDomainNode);
						existMap.add(equivDomainNode);
						
						processedNodes.add(equivinverseNode);
						inverseMap.add(equivinverseNode);
						
						
						//this for inverse
						
					Set<DefaultEdge> edgesInverse = new HashSet<DefaultEdge>(modifiedGraph.incomingEdgesOf(equivinverseNode));

					for (DefaultEdge incEdge : edgesInverse) {
						Description source = modifiedGraph.getEdgeSource(incEdge);

						modifiedGraph.removeAllEdges(source, equivinverseNode);

						if (source.equals(inverse))
							continue;

						modifiedGraph.addEdge(source, inverse);
					}

					edgesInverse = new HashSet<DefaultEdge>(modifiedGraph.outgoingEdgesOf(equivinverseNode));

					for (DefaultEdge outEdge : edgesInverse) {
						Description target = modifiedGraph.getEdgeTarget(outEdge);

						modifiedGraph.removeAllEdges(equivinverseNode, target);

						if (target.equals(inverse))
							continue;
						modifiedGraph.addEdge(inverse, target);

					}

					modifiedGraph.removeVertex(equivinverseNode);
					
					
					
					//this for domain
					Set<DefaultEdge> edgesDomain = new HashSet<DefaultEdge>(modifiedGraph.incomingEdgesOf(equivDomainNode));

					for (DefaultEdge incEdge : edgesDomain) {
						Description source = modifiedGraph.getEdgeSource(incEdge);

						modifiedGraph.removeAllEdges(source, equivDomainNode);

						if (source.equals(domain))
							continue;

						modifiedGraph.addEdge(source, domain);
					}

					edgesDomain = new HashSet<DefaultEdge>(modifiedGraph.outgoingEdgesOf(equivDomainNode));

					for (DefaultEdge outEdge : edgesDomain) {
						Description target = modifiedGraph.getEdgeTarget(outEdge);

						modifiedGraph.removeAllEdges(equivDomainNode, target);

						if (target.equals(domain))
							continue;
						modifiedGraph.addEdge(domain, target);

					}

					modifiedGraph.removeVertex(equivDomainNode);
					

					
					//this for range
					Set<DefaultEdge> edgesRange = new HashSet<DefaultEdge>(modifiedGraph.incomingEdgesOf(equivRangeNode));

					for (DefaultEdge incEdge : edgesRange) {
						Description source = modifiedGraph.getEdgeSource(incEdge);

						modifiedGraph.removeAllEdges(source, equivRangeNode);

						if (source.equals(range))
							continue;

						modifiedGraph.addEdge(source, range);
					}
					
					edgesRange = new HashSet<DefaultEdge>(modifiedGraph.outgoingEdgesOf(equivRangeNode));

					for (DefaultEdge outEdge : edgesRange) {
						Description target = modifiedGraph.getEdgeTarget(outEdge);

						modifiedGraph.removeAllEdges(equivRangeNode, target);

						if (target.equals(range))
							continue;
						modifiedGraph.addEdge(range, target);

					}

					modifiedGraph.removeVertex(equivRangeNode);
					
			
					replacements.put(equivinverseNode, inverse);
					replacements.put(equivDomainNode, domain);
					replacements.put(equivRangeNode, range);

					}
				

			}
			
			//equivalentMap for the inverse
//			for(Description nodeInverse:inverseMap ){
//				equivalencesMap.put(nodeInverse, inverseMap);
//			}
			
			for(Description nodeExist:existMap ){
				equivalencesMap.put(nodeExist, existMap);
			}
			
			for(Description nodeExistInverse:existInverseMap){
				equivalencesMap.put(nodeExistInverse, existInverseMap);
			}
		}

//		StrongConnectivityInspector<Description, DefaultEdge> inspector2 = new StrongConnectivityInspector<Description, DefaultEdge>(modifiedGraph);
		
		//each set contains vertices which together form a strongly connected component within the given graph
		List<Set<Description>> equivalenceClassSets = inspector.stronglyConnectedSets();
		processedNodes.clear();
		
		for (Set<Description> equivalenceClassSet : equivalenceClassSets) {
			
			if (equivalenceClassSet.size() < 2)
				continue;
			/*
			 * Avoiding processing nodes two times, but assign the equivalentMap
			 */
			boolean ignore = false;
			for (Description node : equivalenceClassSet) {
				if( node.equals(fac.createPropertySomeRestriction(fac.createObjectProperty("http://www.owl-ontologies.com/Ontology1207768242.owl#involvesInstrument").getPredicate(), false)))
					System.out.println("Here!");
				

				if (!ignore 
						&& processedNodes.contains(node)
						|| node instanceof Property) {
					ignore = true;
					break;
					
					
				}

			}
			if (ignore)
				continue;
			
			Iterator<Description> iterator = equivalenceClassSet.iterator();
			Description node = iterator.next();
//			Description representative=replacements.get(node);
//			if(representative==null)
//				representative=node;
			Description representative= node;
			Description notRepresentative = null;
			
			//I want to consider a named class as representative element
			if(representative instanceof PropertySomeRestriction ) {
				boolean namedElement=false;
				for (Description equivalent: equivalenceClassSet) {
//					Description equivalent=replacements.get(e);
//					if(equivalent==null)
//						equivalent=e;
					if (equivalent instanceof OClass) {
						notRepresentative =representative;
						representative=equivalent;
						namedElement=true;
//						replacements.put(notRepresentative, representative);
//						equivalencesMap.put(notRepresentative, equivalenceSet);
						break;
					}
				}
				if(!namedElement)
				representative = null; //if all propertysomerestriction they will be added later with another set with property
			}
			
			if(representative==null)
				continue;

			processedNodes.add(representative);
			equivalencesMap.put(representative, equivalenceClassSet);
			
			while (iterator.hasNext()) {
				
				
				Description eliminatedNode = iterator.next();
				if( eliminatedNode.equals(fac.createPropertySomeRestriction(fac.createObjectProperty("http://www.owl-ontologies.com/Ontology1207768242.owl#involvesInstrument").getPredicate(), false)))
					System.out.println("Here!");
				
				if( eliminatedNode.equals(fac.createPropertySomeRestriction(fac.createObjectProperty("http://www.owl-ontologies.com/Ontology1207768242.owl#isTradedIn").getPredicate(), true)))
					System.out.println("Here!");
//				Description eliminatedNode=replacements.get(equivalent);
//				if(eliminatedNode==null)
//					eliminatedNode=equivalent;
				if(eliminatedNode.equals(representative) & notRepresentative!=null)
					eliminatedNode=notRepresentative;
				replacements.put(eliminatedNode, representative);

				equivalencesMap.put(eliminatedNode, equivalenceClassSet);
				
				
				if(!modifiedGraph.containsVertex(eliminatedNode)){
					System.out.println(eliminatedNode);
					continue;
				}
				/*
				 * Re-pointing all links to and from the eliminated node to the
				 * representative node
				 */

				Set<DefaultEdge> edges = new HashSet<DefaultEdge>(modifiedGraph.incomingEdgesOf(eliminatedNode));

				for (DefaultEdge incEdge : edges) {
					Description source = modifiedGraph.getEdgeSource(incEdge);

					modifiedGraph.removeAllEdges(source, eliminatedNode);

					if (source.equals(representative))
						continue;
					if(!modifiedGraph.containsVertex(representative))
					System.out.println(source+ " "+ representative);
					modifiedGraph.addEdge(source, representative);
				}

				edges = new HashSet<DefaultEdge>(modifiedGraph.outgoingEdgesOf(eliminatedNode));

				for (DefaultEdge outEdge : edges) {
					Description target = modifiedGraph.getEdgeTarget(outEdge);
					

					modifiedGraph.removeAllEdges(eliminatedNode, target);

					if (target.equals(representative))
						continue;
					
					Description value=replacements.get(target);
					if(value!= null)
						if(value.equals(eliminatedNode)){
						replacements.put(target, representative);

						equivalencesMap.put(target, equivalenceClassSet);
					}
					modifiedGraph.addEdge(representative, target);

				}

				modifiedGraph.removeVertex(eliminatedNode);
				
				processedNodes.add(eliminatedNode);
			}
		}
		
	}

		


}
