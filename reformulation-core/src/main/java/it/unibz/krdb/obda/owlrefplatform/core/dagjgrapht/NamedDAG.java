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

import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataRangeExpression;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

/** 
 * Used to represent a DAG and a named DAG.
 * Extend SimpleDirectedGraph from the JGrapht library
 * It can be constructed using the class DAGBuilder.
 * 
 * 
 */

public class NamedDAG  {
	
	private final SimpleDirectedGraph <ObjectPropertyExpression,DefaultEdge> objectPropertyDAG;
	private final SimpleDirectedGraph <DataPropertyExpression,DefaultEdge> dataPropertyDAG;
	private final SimpleDirectedGraph <ClassExpression,DefaultEdge> classDAG;
	private final SimpleDirectedGraph <DataRangeExpression,DefaultEdge> dataRangeDAG;
	
	public NamedDAG(TBoxReasoner reasoner) {			
		objectPropertyDAG = getNamedDAG(reasoner.getObjectPropertyDAG());
		dataPropertyDAG = getNamedDAG(reasoner.getDataPropertyDAG());
		classDAG = getNamedDAG(reasoner.getClassDAG());
		dataRangeDAG = getNamedDAG(reasoner.getDataRangeDAG());
	}
	
	@Override
	public String toString() {
		return objectPropertyDAG.toString() + dataPropertyDAG.toString() + classDAG.toString();
	}
	
	
	
	public SimpleDirectedGraph <ObjectPropertyExpression,DefaultEdge> getObjectPropertyDag() {
		return objectPropertyDAG;
	}
	public SimpleDirectedGraph <DataPropertyExpression,DefaultEdge> getDataPropertyDag() {
		return dataPropertyDAG;
	}
	public SimpleDirectedGraph <ClassExpression,DefaultEdge> getClassDag() {
		return classDAG;
	}
	public SimpleDirectedGraph <DataRangeExpression,DefaultEdge> getDataRangeDag() {
		return dataRangeDAG;
	}
	
		
	
	
	/**
	 * Constructor for the NamedDAGBuilder
	 * @param dag the DAG from which we want to maintain only the named descriptions
	 */

	private static <T> SimpleDirectedGraph <T,DefaultEdge> getNamedDAG(EquivalencesDAG<T> dag) {
		
		SimpleDirectedGraph<T,DefaultEdge> namedDAG = new SimpleDirectedGraph<>(DefaultEdge.class); 

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
			
}
