package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

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

import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.PropertyExpression;

import java.util.List;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;

/** 
 * Used to represent a DAG and a named DAG.
 * Extend SimpleDirectedGraph from the JGrapht library
 * It can be constructed using the class DAGBuilder.
 * 
 * 
 */

public class NamedDAG  {
	
	private final SimpleDirectedGraph <PropertyExpression,DefaultEdge> propertyDAG;
	private final SimpleDirectedGraph <BasicClassDescription,DefaultEdge> classDAG;
	
	public NamedDAG(TBoxReasoner reasoner) {			
		propertyDAG = getNamedDAG(reasoner.getProperties());
		classDAG = getNamedDAG(reasoner.getClasses());
	}
	
	@Override
	public String toString() {
		return propertyDAG.toString() + classDAG.toString();
	}
	
	
	
	@Deprecated // USED ONLY IN TESTS (3 calls)
	public SimpleDirectedGraph <PropertyExpression,DefaultEdge> getPropertyDag() {
		return propertyDAG;
	}
	@Deprecated // USED ONLY IN TESTS (3 calls)
	public SimpleDirectedGraph <BasicClassDescription,DefaultEdge> getClassDag() {
		return classDAG;
	}
	
	public List<PropertyExpression> getSuccessors(PropertyExpression desc) {
		return Graphs.successorListOf(propertyDAG, desc);		
	}
	public List<BasicClassDescription> getSuccessors(BasicClassDescription desc) {
		return Graphs.successorListOf(classDAG, desc);		
	}
	
	public List<PropertyExpression> getPredecessors(PropertyExpression desc) {
		return Graphs.predecessorListOf(propertyDAG, desc);		
	}
	public List<BasicClassDescription> getPredecessors(BasicClassDescription desc) {
		return Graphs.predecessorListOf(classDAG, desc);		
	}
	
	
	/**
	 * Constructor for the NamedDAGBuilder
	 * @param dag the DAG from which we want to maintain only the named descriptions
	 */

	private static <T> SimpleDirectedGraph <T,DefaultEdge> getNamedDAG(EquivalencesDAG<T> dag) {
		
		SimpleDirectedGraph<T,DefaultEdge>  namedDAG = new SimpleDirectedGraph <T,DefaultEdge> (DefaultEdge.class); 

		for (Equivalences<T> v : dag) 
			namedDAG.addVertex(v.getRepresentative());

		for (Equivalences<T> s : dag) 
			for (Equivalences<T> t : dag.getDirectSuper(s)) 
				namedDAG.addEdge(s.getRepresentative(), t.getRepresentative());

		for (Equivalences<T> v : dag) 
			if (!v.isIndexed()) {
				// eliminate node
				for (DefaultEdge incEdge : namedDAG.incomingEdgesOf(v.getRepresentative())) { 
					T source = namedDAG.getEdgeSource(incEdge);

					for (DefaultEdge outEdge : namedDAG.outgoingEdgesOf(v.getRepresentative())) {
						T target = namedDAG.getEdgeTarget(outEdge);

						namedDAG.addEdge(source, target);
					}
				}
				namedDAG.removeVertex(v.getRepresentative());		// removes all adjacent edges as well				
			}
		return namedDAG;
	}
	

	public DirectedGraph<Description, DefaultEdge> getReversedDag() {
		SimpleDirectedGraph <Description,DefaultEdge>  dag 
			= new SimpleDirectedGraph <Description,DefaultEdge> (DefaultEdge.class); 
		Graphs.addGraph(dag, propertyDAG);
		Graphs.addGraph(dag, classDAG);
		DirectedGraph<Description, DefaultEdge> reversed =
				new EdgeReversedGraph<Description, DefaultEdge>(dag); // WOULD IT NOT BE BETTER TO CACHE?
		return reversed;
	}
		
}
