package it.unibz.krdb.obda.quest.dag;

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

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataRangeExpression;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ObjectSomeValuesFrom;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.EquivalencesDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

/**
 * Reasoning over the TBox using the ontology graph
 * 
 * WARNING: THIS CLASS IS FOR TESTING ONLY
 */
@Deprecated
public class TestTBoxReasonerImpl_OnGraph implements TBoxReasoner {

	private final DefaultDirectedGraph<ObjectPropertyExpression,DefaultEdge> objectPropertyGraph;
	private final DefaultDirectedGraph<DataPropertyExpression,DefaultEdge> dataPropertyGraph;
	private final DefaultDirectedGraph<ClassExpression,DefaultEdge> classGraph;
	private final DefaultDirectedGraph<DataRangeExpression,DefaultEdge> dataRangeGraph;
	
	private final EquivalencesDAGImplOnGraph<ObjectPropertyExpression> objectPropertyDAG;
	private final EquivalencesDAGImplOnGraph<DataPropertyExpression> dataPropertyDAG;
	private final EquivalencesDAGImplOnGraph<ClassExpression> classDAG;
	private final EquivalencesDAGImplOnGraph<DataRangeExpression> dataRangeDAG;

	public TestTBoxReasonerImpl_OnGraph(TBoxReasonerImpl reasoner) {	
		this.objectPropertyGraph = reasoner.getObjectPropertyGraph();
		this.dataPropertyGraph = reasoner.getDataPropertyGraph();
		this.classGraph = reasoner.getClassGraph();
		this.dataRangeGraph = reasoner.getDataRangeGraph();
		
		this.objectPropertyDAG = new EquivalencesDAGImplOnGraph<ObjectPropertyExpression>(objectPropertyGraph);
		this.dataPropertyDAG = new EquivalencesDAGImplOnGraph<DataPropertyExpression>(dataPropertyGraph);
		this.classDAG = new EquivalencesDAGImplOnGraph<ClassExpression>(classGraph);
		this.dataRangeDAG = new EquivalencesDAGImplOnGraph<DataRangeExpression>(dataRangeGraph);
	}
	
	/**
	 * Return the DAG of properties
	 * 
	 * @return DAG 
	 */

	@Override
	public EquivalencesDAG<ObjectPropertyExpression> getObjectPropertyDAG() {
		return objectPropertyDAG;
	}
	
	@Override
	public EquivalencesDAG<DataPropertyExpression> getDataPropertyDAG() {
		return dataPropertyDAG;
	}
	
	/**
	 * Return the DAG of classes
	 * 
	 * @return DAG 
	 */
	
	@Override
	public EquivalencesDAG<ClassExpression> getClassDAG() {
		return classDAG;
	}
	
	@Override
	public EquivalencesDAG<DataRangeExpression> getDataRanges() {
		return dataRangeDAG;
	}

	/**
	 * Reconstruction of the DAG from the ontology graph
	 *
	 * @param <T> Property or BasicClassDescription
	 */
		
	public static final class EquivalencesDAGImplOnGraph<T> implements EquivalencesDAG<T> {

		private DefaultDirectedGraph<T,DefaultEdge> graph;
		
		public EquivalencesDAGImplOnGraph(DefaultDirectedGraph<T, DefaultEdge> graph) {
			this.graph = graph;
		}

		@Override
		public Iterator<Equivalences<T>> iterator() {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();

			for (T vertex : graph.vertexSet()) {
					result.add(getVertex(vertex));
				}

			return result.iterator();
		}

		@Override
		public Equivalences<T> getVertex(T desc) {
			// search for cycles
			StrongConnectivityInspector<T, DefaultEdge> inspector = new StrongConnectivityInspector<T, DefaultEdge>(graph);

			// each set contains vertices which together form a strongly
			// connected component within the given graph
			List<Set<T>> equivalenceSets = inspector.stronglyConnectedSets();

			// I want to find the equivalent node of desc
			for (Set<T> equivalenceSet : equivalenceSets) {
				if (equivalenceSet.size() >= 2) {
					if (equivalenceSet.contains(desc)) {
						/* if (named) {
								Set<Description> equivalences = new LinkedHashSet<Description>();
								for (Description vertex : equivalenceSet) {
									if (namedClasses.contains(vertex)
											| property.contains(vertex)) {
										equivalences.add(vertex);
									}
								}
								return new Equivalences<Description>(equivalences);
							}
						*/
						return new Equivalences<T>(equivalenceSet, equivalenceSet.iterator().next());
					}
				}
			}

			// if there are not equivalent node return the node or nothing
			/* if (named) {
				if (namedClasses.contains(desc) | property.contains(desc)) {
						return new Equivalences<Description>(Collections
								.singleton(desc));
					} else { // return empty set if the node we are considering
								// (desc) is not a named class or propertu
						equivalences = Collections.emptySet();
						return new Equivalences<Description>(equivalences);
					}
			}*/
			return new Equivalences<T>(Collections.singleton(desc), desc);
		}

		@Override
		public Set<Equivalences<T>> getDirectSub(Equivalences<T> v) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();

			// I want to consider also the children of the equivalent nodes
			for (T n : v) {
				Set<DefaultEdge> edges = graph.incomingEdgesOf(n);
				for (DefaultEdge edge : edges) {
					T source = graph.getEdgeSource(edge);

					// I don't want to consider as children the equivalent node
					// of the current node desc
					if (v.contains(source)) 
						continue;
					
					Equivalences<T> equivalences = getVertex(source);
						/* 
						if (named) { // if true I search only for the named nodes

							Equivalences<Description> namedEquivalences = getEquivalences(source, true);

							if (!namedEquivalences.isEmpty())
								result.add(namedEquivalences);
							else {
								for (Description node : equivalences) {
									// I search for the first named description
									if (!namedEquivalences.contains(node)) {

										result.addAll(getNamedChildren(node));
									}
								}
							}
						} */

					//if (!equivalences.isEmpty())
					result.add(equivalences);
				}
			}
		
			return Collections.unmodifiableSet(result);
		}

		@Override
		public Set<Equivalences<T>> getSub(Equivalences<T> v) {
			
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();
			BreadthFirstIterator<T, DefaultEdge> iterator = new BreadthFirstIterator<T, DefaultEdge>(
								new EdgeReversedGraph<T, DefaultEdge>(graph), v.getRepresentative());

			while (iterator.hasNext()) {
				T node = iterator.next();

					/* if (named) { // add only the named classes and property
						if (namedClasses.contains(node) | property.contains(node)) {
							Set<Description> sources = new HashSet<Description>();
							sources.add(node);

							result.add(new Equivalences<Description>(sources));
						}
					} */
				Set<T> sources = new HashSet<T>();
				sources.add(node);
				result.add(new Equivalences<T>(sources));
			}
			// add each of them to the result
			return Collections.unmodifiableSet(result);
		}

		@Override
		public Set<Equivalences<T>> getDirectSuper(Equivalences<T> v) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();


			// I want to consider also the parents of the equivalent nodes
			for (T n : v) {
				Set<DefaultEdge> edges = graph.outgoingEdgesOf(n);
				for (DefaultEdge edge : edges) {
					T target = graph.getEdgeTarget(edge);

					// I don't want to consider as parents the equivalent node
					// of the current node desc
					if (v.contains(target)) 
						continue;
					
					Equivalences<T> equivalences = getVertex(target);

						/* if (named) { // if true I search only for the named nodes

							Equivalences<Description> namedEquivalences = getEquivalences(target, true);
							if (!namedEquivalences.isEmpty())
								result.add(namedEquivalences);
							else {
								for (Description node : equivalences) {
									// I search for the first named description
									if (!namedEquivalences.contains(node)) {

										result.addAll(getNamedParents(node));
									}
								}
							}

						} */
					//if (!equivalences.isEmpty())
					result.add(equivalences);
				}
			}

			return Collections.unmodifiableSet(result);
		}

		@Override
		public Set<Equivalences<T>> getSuper(Equivalences<T> v) {
			
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();
			BreadthFirstIterator<T, DefaultEdge> iterator = new BreadthFirstIterator<T, DefaultEdge>(graph, v.getRepresentative());

			while (iterator.hasNext()) {
				T node = iterator.next();

					/* if (named) { // add only the named classes and property
						if (namedClasses.contains(node) | property.contains(node)) {
							Set<Description> sources = new HashSet<Description>();
							sources.add(node);

							result.add(new Equivalences<Description>(sources));
						}
					} */
				Set<T> sources = new HashSet<T>();
				sources.add(node);
				result.add(new Equivalences<T>(sources));
			}
			// add each of them to the result
			return Collections.unmodifiableSet(result);
		}

		@Override
		public Set<T> getSubRepresentatives(T v) {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	
	/***
	 * Modifies the DAG so that \exists R = \exists R-, so that the reachability
	 * relation of the original DAG gets extended to the reachability relation
	 * of T and Sigma chains.
	 * 
	 */
	
	public void convertIntoChainDAG() {

		Collection<ClassExpression> nodes = new HashSet<ClassExpression>(classGraph.vertexSet());
		OntologyFactory fac = OntologyFactoryImpl.getInstance();
		HashSet<ClassExpression> processedNodes = new HashSet<ClassExpression>();
		for (ClassExpression node : nodes) {
			if ((!(node instanceof ObjectSomeValuesFrom) /*&& !(node instanceof DataSomeValuesFrom)*/)
					|| processedNodes.contains(node)) {
				continue;
			}

			/*
			 * Adding a cycle between exists R and exists R- for each R.
			 */

			ClassExpression existsRin;
			
			//if (node instanceof ObjectSomeValuesFrom) {
				ObjectSomeValuesFrom existsR = (ObjectSomeValuesFrom) node;
				ObjectPropertyExpression exists = existsR.getProperty();
				existsRin = exists.getInverse().getDomain();
			//}
/*				
			else {
				DataSomeValuesFrom existsR = (DataSomeValuesFrom) node;
				DataPropertyExpression exists = existsR.getProperty();
				existsRin = fac.createPropertySomeRestriction(exists.getInverse());
					// TODO: fix DataRange
//					existsRin = fac.createDataPropertyRange((DataPropertyExpression)exists);
			}
*/				
//			ClassExpression existsR = node;
			
			Equivalences<ClassExpression> existsNode = classDAG.getVertex(existsR);
			Equivalences<ClassExpression> existsInvNode = classDAG.getVertex(existsRin);
			
			Set<Equivalences<ClassExpression>> childrenExist 
					= new HashSet<Equivalences<ClassExpression>>(classDAG.getDirectSub(existsNode));
			Set<Equivalences<ClassExpression>> childrenExistInv 
					= new HashSet<Equivalences<ClassExpression>>(classDAG.getDirectSub(existsInvNode));

			for (Equivalences<ClassExpression> children : childrenExist) {
				for (ClassExpression child : children) 
					classGraph.addEdge(child, existsRin);
			}
			for (Equivalences<ClassExpression> children : childrenExistInv) {
				for (ClassExpression child : children) 
					classGraph.addEdge(child, existsR);
			}

			Set<Equivalences<ClassExpression>> parentExist 
					= new HashSet<Equivalences<ClassExpression>>(classDAG.getDirectSuper(existsNode));
			Set<Equivalences<ClassExpression>> parentsExistInv 
					= new HashSet<Equivalences<ClassExpression>>(classDAG.getDirectSuper(existsInvNode));

			for (Equivalences<ClassExpression> parents : parentExist) {
				for (ClassExpression parent : parents) 
					classGraph.addEdge(existsRin, parent);
			}

			for (Equivalences<ClassExpression> parents : parentsExistInv) {
				for (ClassExpression parent : parents) 
					classGraph.addEdge(existsR, parent);
			}

			processedNodes.add(existsRin);
			processedNodes.add(existsR);
		}
	}

	public int vertexSetSize() {
		return objectPropertyGraph.vertexSet().size() + dataPropertyGraph.vertexSet().size() + classGraph.vertexSet().size();
	}

	public int edgeSetSize() {
		return objectPropertyGraph.edgeSet().size() + dataPropertyGraph.edgeSet().size() +  classGraph.edgeSet().size();
	}

	public DefaultDirectedGraph<ObjectPropertyExpression, DefaultEdge> getObjectPropertyGraph() {
		return objectPropertyGraph;
	}
	public DefaultDirectedGraph<DataPropertyExpression, DefaultEdge> getDataPropertyGraph() {
		return dataPropertyGraph;
	}
	public DefaultDirectedGraph<ClassExpression, DefaultEdge> getClassGraph() {
		return classGraph;
	}

	@Override
	public OClass getClassRepresentative(OClass p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectPropertyExpression getObjectPropertyRepresentative(ObjectPropertyExpression p) {
		// TODO Auto-generated method stub
		return null;
	}	
	
	@Override
	public DataPropertyExpression getDataPropertyRepresentative(DataPropertyExpression p) {
		// TODO Auto-generated method stub
		return null;
	}	
}
