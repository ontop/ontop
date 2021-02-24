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


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.si.repository.impl.SemanticIndexBuilder;
import it.unibz.inf.ontop.spec.ontology.impl.ClassifiedTBoxImpl;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Representation of the named part of the property and class DAGs  
 *     based on the NamedDAG abstraction
 * 
 * WARNING: THIS CLASS IS FOR TESTING ONLY 
 */

@Deprecated
public class TestClassifiedTBoxImpl_OnNamedDAG implements ClassifiedTBox {

	private final EquivalencesDAGImpl<ClassExpression> classDAG;
	private final EquivalencesDAGImpl<ObjectPropertyExpression> objectPropertyDAG;
	private final EquivalencesDAGImpl<DataPropertyExpression> dataPropertyDAG;
	private final EquivalencesDAGImpl<DataRangeExpression> dataRangeDAG;

	private final ClassifiedTBox reasoner;

	/**
	 * Constructor using a DAG or a named DAG
	 * @param reasoner DAG to be used for reasoning
	 */
	public TestClassifiedTBoxImpl_OnNamedDAG(ClassifiedTBox reasoner) {

		this.objectPropertyDAG = new EquivalencesDAGImpl<>(
				SemanticIndexBuilder.getNamedDAG(reasoner.objectPropertiesDAG()), reasoner.objectPropertiesDAG());
		this.dataPropertyDAG = new EquivalencesDAGImpl<>(
				SemanticIndexBuilder.getNamedDAG(reasoner.dataPropertiesDAG()), reasoner.dataPropertiesDAG());
		this.classDAG = new EquivalencesDAGImpl<>(
				SemanticIndexBuilder.getNamedDAG(reasoner.classesDAG()), reasoner.classesDAG());
		this.dataRangeDAG = new EquivalencesDAGImpl<>(
					SemanticIndexBuilder.getNamedDAG(reasoner.dataRangesDAG()), reasoner.dataRangesDAG());
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
		public ImmutableSet<Equivalences<T>> getDirectSub(Equivalences<T> v) {
			return getDirectSub(v.getRepresentative());
		}
		public ImmutableSet<Equivalences<T>> getDirectSub(T node) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<>();

			for (T source : Graphs.predecessorListOf(dag, node)) {

				// get the child node and its equivalent nodes
				Equivalences<T> namedEquivalences = getVertex(source);
				if (namedEquivalences != null)
					result.add(namedEquivalences);
				else 
					result.addAll(getDirectSub(source));
			}

			return ImmutableSet.copyOf(result);
		}

		@Override
		public ImmutableSet<Equivalences<T>> getSub(Equivalences<T> v) {
			
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
			
			return ImmutableSet.copyOf(result);
		}

		@Override
		public ImmutableSet<Equivalences<T>> getDirectSuper(Equivalences<T> v) {
			return getDirectSuper(v.getRepresentative());
		}

		public ImmutableSet<Equivalences<T>> getDirectSuper(T node) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<>();
			
			for (T target : Graphs.successorListOf(dag, node)) {

				// get the child node and its equivalent nodes
				Equivalences<T> namedEquivalences = getVertex(target);
				if (namedEquivalences != null)
					result.add(namedEquivalences);
				else 
					result.addAll(getDirectSuper(target));
			}

			return ImmutableSet.copyOf(result);
		}

		@Override
		public ImmutableSet<Equivalences<T>> getSuper(Equivalences<T> v) {

			T node = v.getRepresentative();
			
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<>();
			BreadthFirstIterator<T, DefaultEdge>  iterator = new BreadthFirstIterator<>(dag, node);

			while (iterator.hasNext()) {
				T parent = iterator.next();

				// add the node and its equivalent nodes
				Equivalences<T> sources = getVertex(parent);
				if (sources != null)
					result.add(sources);
			}

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
}
