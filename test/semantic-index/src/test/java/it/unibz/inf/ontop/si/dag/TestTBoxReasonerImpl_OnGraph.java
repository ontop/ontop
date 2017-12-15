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

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.impl.EquivalencesDAGImpl;
import it.unibz.inf.ontop.spec.ontology.impl.TBoxReasonerImpl;

import java.util.*;

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
public class TestTBoxReasonerImpl_OnGraph implements TBoxReasoner {

	private final TBoxReasonerImpl.ClassifiedOntologyVocabularyCategoryImpl<ObjectPropertyExpression, ObjectPropertyExpression> objectPropertyDAG;
	private final TBoxReasonerImpl.ClassifiedOntologyVocabularyCategoryImpl<DataPropertyExpression, DataPropertyExpression> dataPropertyDAG;
	private final TBoxReasonerImpl.ClassifiedOntologyVocabularyCategoryImpl<ClassExpression, OClass> classDAG;
	private final TBoxReasonerImpl.ClassifiedOntologyVocabularyCategoryImpl<DataRangeExpression, Datatype> dataRangeDAG;
	private final TBoxReasonerImpl reasoner;

	public TestTBoxReasonerImpl_OnGraph(TBoxReasonerImpl reasoner) {	
		this.objectPropertyDAG = new TBoxReasonerImpl.ClassifiedOntologyVocabularyCategoryImpl<>(ImmutableMap.of(),
				new EquivalencesDAGImplOnGraph<>(reasoner.getObjectPropertyGraph()));
		this.dataPropertyDAG = new TBoxReasonerImpl.ClassifiedOntologyVocabularyCategoryImpl<>(ImmutableMap.of(),
				new EquivalencesDAGImplOnGraph<>(reasoner.getDataPropertyGraph()));
		this.classDAG = new TBoxReasonerImpl.ClassifiedOntologyVocabularyCategoryImpl<>(ImmutableMap.of(),
				new EquivalencesDAGImplOnGraph<>(reasoner.getClassGraph()));
		this.dataRangeDAG = new TBoxReasonerImpl.ClassifiedOntologyVocabularyCategoryImpl<>(ImmutableMap.of(),
				new EquivalencesDAGImplOnGraph<>(reasoner.getDataRangeGraph()));
		this.reasoner = reasoner;
	}

    @Override
    public ClassifiedOntologyVocabularyCategory<ObjectPropertyExpression, ObjectPropertyExpression> objectProperties() {
        return objectPropertyDAG;
    }

    @Override
    public ClassifiedOntologyVocabularyCategory<DataPropertyExpression, DataPropertyExpression> dataProperties() {
        return dataPropertyDAG;
    }

    @Override
    public ClassifiedOntologyVocabularyCategory<ClassExpression, OClass> classes() {
	    return classDAG;
    }

    @Override
    public ClassifiedOntologyVocabularyCategory<DataRangeExpression, Datatype> dataRanges() {
        return dataRangeDAG;
    }


    // DUMMY

    @Override
    public ClassifiedOntologyVocabularyCategory<AnnotationProperty, AnnotationProperty> annotationProperties() {
        return null;
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
			return new Equivalences<T>(ImmutableSet.of(desc), desc, false);
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
				result.add(new Equivalences<T>(ImmutableSet.of(node)));
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
				result.add(new Equivalences<T>(ImmutableSet.of(node)));
			}
			// add each of them to the result
			return Collections.unmodifiableSet(result);
		}

		@Override
		public Set<T> getSubRepresentatives(T v) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public T getCanonicalForm(T v) {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	

	public int vertexSetSize() {
		return ((EquivalencesDAGImplOnGraph<ObjectPropertyExpression>)objectPropertyDAG.dag()).graph.vertexSet().size()
                + ((EquivalencesDAGImplOnGraph<DataPropertyExpression>)dataPropertyDAG.dag()).graph.vertexSet().size()
                + ((EquivalencesDAGImplOnGraph<ClassExpression>)classDAG.dag()).graph.vertexSet().size();
	}

	public int edgeSetSize() {
		return ((EquivalencesDAGImplOnGraph<ObjectPropertyExpression>)objectPropertyDAG.dag()).graph.edgeSet().size()
                + ((EquivalencesDAGImplOnGraph<DataPropertyExpression>)dataPropertyDAG.dag()).graph.edgeSet().size()
                + ((EquivalencesDAGImplOnGraph<ClassExpression>)classDAG.dag()).graph.edgeSet().size();
	}

	public DefaultDirectedGraph<ObjectPropertyExpression, DefaultEdge> getObjectPropertyGraph() {
		return ((EquivalencesDAGImplOnGraph<ObjectPropertyExpression>)objectPropertyDAG.dag()).graph;
	}
	public DefaultDirectedGraph<DataPropertyExpression, DefaultEdge> getDataPropertyGraph() {
		return ((EquivalencesDAGImplOnGraph<DataPropertyExpression>)dataPropertyDAG.dag()).graph;
	}
	public DefaultDirectedGraph<ClassExpression, DefaultEdge> getClassGraph() {
		return ((EquivalencesDAGImplOnGraph<ClassExpression>)classDAG.dag()).graph;
	}
}
