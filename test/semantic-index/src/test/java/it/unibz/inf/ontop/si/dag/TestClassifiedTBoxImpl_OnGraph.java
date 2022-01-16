package it.unibz.inf.ontop.si.dag;

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

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.impl.ClassifiedTBoxImpl;

import java.util.*;
import java.util.stream.Stream;

import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import com.google.common.collect.ImmutableSet;

/**
 * Reasoning over the TBox using the ontology graph
 * 
 * WARNING: THIS CLASS IS FOR TESTING ONLY
 */
@Deprecated
public class TestClassifiedTBoxImpl_OnGraph implements ClassifiedTBox {

    private final EquivalencesDAGImplOnGraph<ClassExpression> classDAG;
    private final EquivalencesDAGImplOnGraph<ObjectPropertyExpression> objectPropertyDAG;
    private final EquivalencesDAGImplOnGraph<DataPropertyExpression> dataPropertyDAG;
    private final EquivalencesDAGImplOnGraph<DataRangeExpression> dataRangeDAG;

	private final ClassifiedTBoxImpl reasoner;

	public TestClassifiedTBoxImpl_OnGraph(ClassifiedTBoxImpl reasoner) {
		this.objectPropertyDAG = new EquivalencesDAGImplOnGraph<>(reasoner.getObjectPropertyGraph());
		this.dataPropertyDAG = new EquivalencesDAGImplOnGraph<>(reasoner.getDataPropertyGraph());
		this.classDAG = new EquivalencesDAGImplOnGraph<>(reasoner.getClassGraph());
		this.dataRangeDAG = new EquivalencesDAGImplOnGraph<>(reasoner.getDataRangeGraph());
		this.reasoner = reasoner;
	}

    @Override
    public OntologyVocabularyCategory<ObjectPropertyExpression> objectProperties() { return reasoner.objectProperties(); }

    @Override
    public OntologyVocabularyCategory<DataPropertyExpression> dataProperties() { return reasoner.dataProperties(); }

    @Override
    public OntologyVocabularyCategory<OClass> classes() {
	    return reasoner.classes();
    }



    // DUMMIES
	@Override
	public ImmutableList<NaryAxiom<ClassExpression>> disjointClasses() { return null; }

	@Override
	public ImmutableList<NaryAxiom<ObjectPropertyExpression>> disjointObjectProperties() { return null; }

	@Override
	public ImmutableList<NaryAxiom<DataPropertyExpression>> disjointDataProperties() { return null; }

	@Override
	public ImmutableSet<ObjectPropertyExpression> reflexiveObjectProperties() { return null; }

	@Override
	public ImmutableSet<ObjectPropertyExpression> irreflexiveObjectProperties() { return null; }

	@Override
	public ImmutableSet<ObjectPropertyExpression> functionalObjectProperties() { return null; }

	@Override
	public ImmutableSet<DataPropertyExpression> functionalDataProperties() { return null; }

	@Override
    public EquivalencesDAG<ClassExpression> classesDAG() {
        return classDAG;
    }

    @Override
    public EquivalencesDAG<ObjectPropertyExpression> objectPropertiesDAG() {
        return objectPropertyDAG;
    }

    @Override
    public EquivalencesDAG<DataPropertyExpression> dataPropertiesDAG() {
        return dataPropertyDAG;
    }

    @Override
    public EquivalencesDAG<DataRangeExpression> dataRangesDAG() {
        return dataRangeDAG;
    }


	/**
	 * Reconstruction of the DAG from the ontology graph
	 *
	 * @param <T> Property or BasicClassDescription
	 */
		
	public static final class EquivalencesDAGImplOnGraph<T> implements EquivalencesDAG<T> {

		private final DefaultDirectedGraph<T,DefaultEdge> graph;
		
		public EquivalencesDAGImplOnGraph(DefaultDirectedGraph<T, DefaultEdge> graph) {
			this.graph = graph;
		}

		@Override
		public Iterator<Equivalences<T>> iterator() {
			Set<Equivalences<T>> result = new LinkedHashSet<>();

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
						return new Equivalences<T>(ImmutableSet.copyOf(equivalenceSet), equivalenceSet.iterator().next(), false);
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
			return new Equivalences<>(ImmutableSet.of(desc), desc, false);
		}

		@Override
		public ImmutableSet<Equivalences<T>> getDirectSub(Equivalences<T> v) {
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
		
			return ImmutableSet.copyOf(result);
		}

		@Override
		public ImmutableSet<Equivalences<T>> getSub(Equivalences<T> v) {
			
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
				result.add(new Equivalences<T>(ImmutableSet.of(node)));
			}
			// add each of them to the result
			return ImmutableSet.copyOf(result);
		}

		@Override
		public ImmutableSet<Equivalences<T>> getDirectSuper(Equivalences<T> v) {
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

			return ImmutableSet.copyOf(result);
		}

		@Override
		public ImmutableSet<Equivalences<T>> getSuper(Equivalences<T> v) {
			
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
				result.add(new Equivalences<T>(ImmutableSet.of(node)));
			}
			// add each of them to the result
			return ImmutableSet.copyOf(result);
		}

		@Override
		public Stream<Equivalences<T>> stream() {
			return null;
		}

		@Override
		public ImmutableSet<T> getSubRepresentatives(T v) {
			return null;
		}

		@Override
		public T getCanonicalForm(T v) {
			return null;
		}
	}
	
	

	public int vertexSetSize() {
		return objectPropertyDAG.graph.vertexSet().size() + dataPropertyDAG.graph.vertexSet().size() + classDAG.graph.vertexSet().size();
	}

	public int edgeSetSize() {
		return objectPropertyDAG.graph.edgeSet().size() + dataPropertyDAG.graph.edgeSet().size() + classDAG.graph.edgeSet().size();
	}

	public DefaultDirectedGraph<ObjectPropertyExpression, DefaultEdge> getObjectPropertyGraph() { return objectPropertyDAG.graph; }

	public DefaultDirectedGraph<DataPropertyExpression, DefaultEdge> getDataPropertyGraph() { return dataPropertyDAG.graph; }

	public DefaultDirectedGraph<ClassExpression, DefaultEdge> getClassGraph() { return classDAG.graph; }
}
