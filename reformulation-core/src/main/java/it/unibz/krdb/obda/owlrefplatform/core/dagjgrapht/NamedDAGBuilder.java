package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

/** 
 * Build a DAG with only the named descriptions 
 * 
 * */

public class NamedDAGBuilder {



	/**
	 * Constructor for the NamedDAGBuilder
	 * @param dag the DAG from which we want to maintain only the named descriptions
	 */
	
	public static DAGImpl getNamedDAG(DAGImpl dag) {

		Map<Description, Set<Description>> equivalencesMap = new HashMap<Description, Set<Description>>();
		Map<Description, Description> replacements = new HashMap<Description, Description>();;

		SimpleDirectedGraph <Description,DefaultEdge>  namedDag = 
				new SimpleDirectedGraph <Description,DefaultEdge>(DefaultEdge.class);

		// clone all the vertexes and edges from dag

		for (Description v : dag.vertexSet()) {
			namedDag.addVertex(v);
		}
		for (DefaultEdge e : dag.edgeSet()) {
			Description s = dag.getEdgeSource(e);
			Description t = dag.getEdgeTarget(e);

			namedDag.addEdge(s, t, e);
		}

		TBoxReasonerImpl reasoner = new TBoxReasonerImpl(dag);
		OntologyFactory descFactory = OntologyFactoryImpl.getInstance();
		
		// take classes, roles, equivalences map and replacements from the DAG
		Set<OClass> namedClasses = dag.getClasses();
		Set<Property> property = dag.getRoles();

		// clone the equivalences and replacements map
		Map<Description, Set<Description>> equivalencesDag = dag
				.getMapEquivalences();
		Map<Description, Description> replacementsDag = dag.getReplacements();
		for (Description vertex : equivalencesDag.keySet()) {
			

				HashSet<Description> equivalents = new HashSet<Description>();
				for (Description equivalent : equivalencesDag.get(vertex)) {
					equivalents.add(equivalent);
				}
				equivalencesMap.put(vertex, new HashSet<Description>(
						equivalents));
			

		}
		for (Description eliminateNode : replacementsDag.keySet()) {

			Description referent = replacementsDag.get(eliminateNode);
			replacements.put(eliminateNode, referent);

		}

		GraphIterator<Description, DefaultEdge> orderIterator;

		/*
		 * Test with a reversed graph so that the incoming edges 
		 * represent the parents of the node
		 */
		
		DirectedGraph<Description, DefaultEdge> reversed = dag.getReversedDag();

		LinkedList<Description> roots = new LinkedList<Description>();
		for (Description n : reversed.vertexSet()) {
			if ((reversed.incomingEdgesOf(n)).isEmpty()) {
				roots.add(n);
			}
		}
		
		Set<Description> processedNodes= new HashSet<Description>();
		
		for (Description root: roots){
		
		/* 
		 * A depth first sort from each root of the DAG.
		 * If the node is named we keep it, otherwise we remove it and 
		 * we connect all its descendants to all its ancestors.
		 * 
		 * 
		 */
		orderIterator =
				new DepthFirstIterator<Description, DefaultEdge>(reversed, root);
		
		
		while (orderIterator.hasNext()) 
		{
			Description node= orderIterator.next();
			
			if(processedNodes.contains(node))
				continue;
			
			if (namedClasses.contains(node) | property.contains(node)) {
				
				processedNodes.add(node);
				continue;
			}
			
			if(node instanceof Property)
			{
				Property posNode = descFactory.createProperty(((Property)node).getPredicate(), false);
				if(processedNodes.contains(posNode))
				{
					Set<DefaultEdge> incomingEdges = new HashSet<DefaultEdge>(
							namedDag.incomingEdgesOf(node));

					// I do a copy of the dag not to remove edges that I still need to
					// consider in the loops
					SimpleDirectedGraph <Description,DefaultEdge> copyDAG = (SimpleDirectedGraph <Description,DefaultEdge>)namedDag.clone();
					Set<DefaultEdge> outgoingEdges = new HashSet<DefaultEdge>(
							copyDAG.outgoingEdgesOf(node));
					for (DefaultEdge incEdge : incomingEdges) {

						Description source = namedDag.getEdgeSource(incEdge);
						namedDag.removeAllEdges(source, node);

						for (DefaultEdge outEdge : outgoingEdges) {
							Description target = copyDAG.getEdgeTarget(outEdge);
							namedDag.removeAllEdges(node, target);

							if (source.equals(target))
								continue;
							namedDag.addEdge(source, target);
						}

					}

					namedDag.removeVertex(node);
					processedNodes.add(node);
					
					
				
					continue;
				}
			}
			
			Set<Description> namedEquivalences = reasoner.getEquivalences(node,true);
			if(!namedEquivalences.isEmpty())
			{
				Description newReference= namedEquivalences.iterator().next();
				 replacements.remove(newReference);
				 namedDag.addVertex(newReference);
				
				 Set<Description> allEquivalences = reasoner.getEquivalences(node,
							false);
				 Iterator<Description> e= allEquivalences.iterator();
				 while(e.hasNext()){
					 Description vertex =e.next();
				 
					 if(vertex.equals(newReference))
						 continue;
				 
				 replacements.put(vertex, newReference);
				 }
				 
				 /*
					 * Re-pointing all links to and from the eliminated node to the
					 new
					 * representative node
					 */
					
					 Set<DefaultEdge> edges = new
					 HashSet<DefaultEdge>(namedDag.incomingEdgesOf(node));
					 for (DefaultEdge incEdge : edges) {
					 Description source = namedDag.getEdgeSource(incEdge);
					 namedDag.removeAllEdges(source, node);
					
					 if (source.equals(newReference))
					 continue;
					
					 namedDag.addEdge(source, newReference);
					 }
					
					 edges = new
					 HashSet<DefaultEdge>(namedDag.outgoingEdgesOf(node));
					 for (DefaultEdge outEdge : edges) {
					 Description target = namedDag.getEdgeTarget(outEdge);
					 namedDag.removeAllEdges(node, target);
					
					 if (target.equals(newReference))
					 continue;
					 namedDag.addEdge(newReference, target);
					 }
					
					 namedDag.removeVertex(node);
					 processedNodes.add(node);
					 
					 /*remove the invertex*/
					 
					 if(node instanceof Property)
						{
							Property posNode = descFactory.createProperty(((Property)node).getPredicate(), false);
							
								Set<DefaultEdge> incomingEdges = new HashSet<DefaultEdge>(
										namedDag.incomingEdgesOf(posNode));

								// I do a copy of the dag not to remove edges that I still need to
								// consider in the loops
								SimpleDirectedGraph <Description,DefaultEdge> copyDAG = (SimpleDirectedGraph <Description,DefaultEdge>) namedDag.clone();
								Set<DefaultEdge> outgoingEdges = new HashSet<DefaultEdge>(
										copyDAG.outgoingEdgesOf(posNode));
								for (DefaultEdge incEdge : incomingEdges) {

									Description source = namedDag.getEdgeSource(incEdge);
									namedDag.removeAllEdges(source, posNode);

									for (DefaultEdge outEdge : outgoingEdges) {
										Description target = copyDAG.getEdgeTarget(outEdge);
										namedDag.removeAllEdges(posNode, target);

										if (source.equals(target))
											continue;
										namedDag.addEdge(source, target);
									}

								}

								namedDag.removeVertex(posNode);
								
								
							
								continue;
							}
					 
					 	
			}
			else{
				Set<DefaultEdge> incomingEdges = new HashSet<DefaultEdge>(
						namedDag.incomingEdgesOf(node));

				// I do a copy of the dag not to remove edges that I still need to
				// consider in the loops
				SimpleDirectedGraph <Description,DefaultEdge> copyDAG = (SimpleDirectedGraph <Description,DefaultEdge>) namedDag.clone();
				Set<DefaultEdge> outgoingEdges = new HashSet<DefaultEdge>(
						copyDAG.outgoingEdgesOf(node));
				for (DefaultEdge incEdge : incomingEdges) {

					Description source = namedDag.getEdgeSource(incEdge);
					namedDag.removeAllEdges(source, node);

					for (DefaultEdge outEdge : outgoingEdges) {
						Description target = copyDAG.getEdgeTarget(outEdge);
						namedDag.removeAllEdges(node, target);

						if (source.equals(target))
							continue;
						namedDag.addEdge(source, target);
					}

				}

				namedDag.removeVertex(node);
				processedNodes.add(node);
			}
			
		}
		
		}
		
		DAGImpl dagImpl = new DAGImpl(namedDag, equivalencesMap, replacements, false);
		return dagImpl;
	}
}
