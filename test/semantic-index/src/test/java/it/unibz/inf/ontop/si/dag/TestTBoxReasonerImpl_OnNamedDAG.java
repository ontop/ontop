package it.unibz.inf.ontop.si.dag;

/*
 * #%L
 * ontop-quest-owlapi
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
import it.unibz.inf.ontop.si.repository.impl.SemanticIndexBuilder;
import it.unibz.inf.ontop.spec.ontology.impl.TBoxReasonerImpl;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Representation of the named part of the property and class DAGs  
 *     based on the NamedDAG abstraction
 * 
 * WARNING: THIS CLASS IS FOR TESTING ONLY 
 */

@Deprecated
public class TestTBoxReasonerImpl_OnNamedDAG implements TBoxReasoner {

	private final TBoxReasonerImpl.ClassifiedOntologyVocabularyCategoryImpl<ObjectPropertyExpression, ObjectPropertyExpression> objectPropertyDAG;
	private final TBoxReasonerImpl.ClassifiedOntologyVocabularyCategoryImpl<DataPropertyExpression, DataPropertyExpression> dataPropertyDAG;
	private final TBoxReasonerImpl.ClassifiedOntologyVocabularyCategoryImpl<ClassExpression, OClass> classDAG;
	private final TBoxReasonerImpl.ClassifiedOntologyVocabularyCategoryImpl<DataRangeExpression, Datatype> dataRangeDAG;
	private final TBoxReasoner reasoner;

	/**
	 * Constructor using a DAG or a named DAG
	 * @param reasoner DAG to be used for reasoning
	 */
	public TestTBoxReasonerImpl_OnNamedDAG(TBoxReasoner reasoner) {

		this.objectPropertyDAG =
				new TBoxReasonerImpl.ClassifiedOntologyVocabularyCategoryImpl<>(ImmutableMap.of(), new EquivalencesDAGImpl<>(
				SemanticIndexBuilder.getNamedDAG(reasoner.objectProperties().dag()), reasoner.objectProperties().dag()));
		this.dataPropertyDAG =
				new TBoxReasonerImpl.ClassifiedOntologyVocabularyCategoryImpl<>(ImmutableMap.of(), new EquivalencesDAGImpl<>(
				SemanticIndexBuilder.getNamedDAG(reasoner.dataProperties().dag()), reasoner.dataProperties().dag()));
		this.classDAG =
				new TBoxReasonerImpl.ClassifiedOntologyVocabularyCategoryImpl<>(ImmutableMap.of(), new EquivalencesDAGImpl<>(
				SemanticIndexBuilder.getNamedDAG(reasoner.classes().dag()), reasoner.classes().dag()));
		this.dataRangeDAG =
				new TBoxReasonerImpl.ClassifiedOntologyVocabularyCategoryImpl<>(ImmutableMap.of(), new EquivalencesDAGImpl<>(
					SemanticIndexBuilder.getNamedDAG(reasoner.dataRanges().dag()), reasoner.dataRanges().dag()));
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
	 * Reconstruction of the Named DAG (as EquivalencesDAG) from a NamedDAG
	 *
	 * @param <T> Property or BasicClassDescription
	 */
	
	public static final class EquivalencesDAGImpl<T> implements EquivalencesDAG<T> {

		private SimpleDirectedGraph <T,DefaultEdge> dag;
		private EquivalencesDAG<T> reasonerDAG;
		
		public EquivalencesDAGImpl(SimpleDirectedGraph<T, DefaultEdge> dag, EquivalencesDAG<T> reasonerDAG) {
			this.dag = dag;
			this.reasonerDAG = reasonerDAG;
		}

		@Override
		public Iterator<Equivalences<T>> iterator() {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();

			for (T vertex : dag.vertexSet()) 
				result.add(getVertex(vertex));
			
			return result.iterator();
		}

		@Override
		public Equivalences<T> getVertex(T v) {		
			// either all or none
			Equivalences<T> vertex = reasonerDAG.getVertex(v);
			if (dag.containsVertex(vertex.getRepresentative()))
				return vertex;
			else
				return null;
		}

		@Override
		public Set<Equivalences<T>> getDirectSub(Equivalences<T> v) {
			return getDirectSub(v.getRepresentative());
		}
		public Set<Equivalences<T>> getDirectSub(T node) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();

			for (T source : Graphs.predecessorListOf(dag, node)) {

				// get the child node and its equivalent nodes
				Equivalences<T> namedEquivalences = getVertex(source);
				if (namedEquivalences != null)
					result.add(namedEquivalences);
				else 
					result.addAll(getDirectSub(source));
			}

			return result;
		}

		@Override
		public Set<Equivalences<T>> getSub(Equivalences<T> v) {
			
			T node = v.getRepresentative();
			
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();
			// reverse the dag
			DirectedGraph<T, DefaultEdge> reversed = new EdgeReversedGraph<T, DefaultEdge>(dag);
			BreadthFirstIterator<T, DefaultEdge>  iterator = new BreadthFirstIterator<T, DefaultEdge>(reversed, node);

			while (iterator.hasNext()) {
				T child = iterator.next();

				// add the node and its equivalent nodes
				Equivalences<T> sources = getVertex(child);
				if (sources != null)
					result.add(sources);
			}
			
			return result;
		}

		@Override
		public Set<Equivalences<T>> getDirectSuper(Equivalences<T> v) {
			return getDirectSuper(v.getRepresentative());
		}
		
		public Set<Equivalences<T>> getDirectSuper(T node) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();
			
			for (T target : Graphs.successorListOf(dag, node)) {

				// get the child node and its equivalent nodes
				Equivalences<T> namedEquivalences = getVertex(target);
				if (namedEquivalences != null)
					result.add(namedEquivalences);
				else 
					result.addAll(getDirectSuper(target));
			}

			return result;
		}

		@Override
		public Set<Equivalences<T>> getSuper(Equivalences<T> v) {

			T node = v.getRepresentative();
			
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();
			BreadthFirstIterator<T, DefaultEdge>  iterator = new BreadthFirstIterator<T, DefaultEdge>(dag, node);

			while (iterator.hasNext()) {
				T parent = iterator.next();

				// add the node and its equivalent nodes
				Equivalences<T> sources = getVertex(parent);
				if (sources != null)
					result.add(sources);
			}

			return result;
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
}
