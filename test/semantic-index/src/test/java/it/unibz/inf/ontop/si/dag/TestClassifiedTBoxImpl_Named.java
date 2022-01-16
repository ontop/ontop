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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.impl.ClassifiedTBoxImpl;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Representation of the named part of the property and class DAGs  
 *     based on the DAGs provided by a ClassifiedTBoxImpl
 * 
 * WARNING: THIS CLASS IS FOR TESTING ONLY 
 */
@Deprecated
public class TestClassifiedTBoxImpl_Named implements ClassifiedTBox {

	private final EquivalencesDAGImpl<ClassExpression> classDAG;
	private final EquivalencesDAGImpl<ObjectPropertyExpression> objectPropertyDAG;
	private final EquivalencesDAGImpl<DataPropertyExpression> dataPropertyDAG;
	private final EquivalencesDAGImpl<DataRangeExpression> dataRangeDAG;

	private final ClassifiedTBox reasoner;

	public TestClassifiedTBoxImpl_Named(ClassifiedTBox reasoner) {
		this.objectPropertyDAG = new EquivalencesDAGImpl<>(reasoner.objectPropertiesDAG());
		this.dataPropertyDAG = new EquivalencesDAGImpl<>(reasoner.dataPropertiesDAG());
		this.classDAG = new EquivalencesDAGImpl<>(reasoner.classesDAG());
		this.dataRangeDAG = new EquivalencesDAGImpl<>(reasoner.dataRangesDAG());

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


    /**
	 * Reconstruction of the Named DAG (as EquivalencesDAG) from a DAG
	 *
	 * @param <T> Property or BasicClassDescription
	 */
	
	public static final class EquivalencesDAGImpl<T> implements EquivalencesDAG<T> {

		private final EquivalencesDAG<T> reasonerDAG;
		
		EquivalencesDAGImpl(EquivalencesDAG<T> reasonerDAG) {
			this.reasonerDAG = reasonerDAG;
		}
		
		@Override
		public Iterator<Equivalences<T>> iterator() {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<>();
			
			for (Equivalences<T> e : reasonerDAG) {
				Equivalences<T> nodes = getVertex(e.getRepresentative());
				if (nodes != null)
					result.add(nodes);			
			}
			return result.iterator();
		}

		@Override
		public Equivalences<T> getVertex(T desc) {

			// either all elements of the equivalence set are there or none!
			Equivalences<T> vertex = reasonerDAG.getVertex(desc);
			if (vertex.isIndexed())
				return vertex;
			else
				return null;
		}

		
		@Override
		public ImmutableSet<Equivalences<T>> getDirectSub(Equivalences<T> v) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<>();

			for (Equivalences<T> e : reasonerDAG.getDirectSub(v)) {
				T child = e.getRepresentative();
				
				// get the child node and its equivalent nodes
				Equivalences<T> namedEquivalences = getVertex(child);
				if (namedEquivalences != null)
					result.add(namedEquivalences);
				else 
					result.addAll(getDirectSub(e)); // recursive call if the child is not empty
			}
			return ImmutableSet.copyOf(result);
		}

		@Override
		public ImmutableSet<Equivalences<T>> getSub(Equivalences<T> v) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<>();
			
			for (Equivalences<T> e : reasonerDAG.getSub(v)) {
				Equivalences<T> nodes = getVertex(e.getRepresentative());
				if (nodes != null)
					result.add(nodes);			
			}
			return ImmutableSet.copyOf(result);
		}

		@Override
		public ImmutableSet<T> getSubRepresentatives(T v) {
			Equivalences<T> eq = reasonerDAG.getVertex(v);
			LinkedHashSet<T> result = new LinkedHashSet<>();
			
			for (Equivalences<T> e : reasonerDAG.getSub(eq)) {
				Equivalences<T> nodes = getVertex(e.getRepresentative());
				if (nodes != null)
					result.add(nodes.getRepresentative());			
			}
			return ImmutableSet.copyOf(result);
		}		

		@Override
		public ImmutableSet<Equivalences<T>> getDirectSuper(Equivalences<T> v) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<>();
			
			for (Equivalences<T> e : reasonerDAG.getDirectSuper(v)) {
				T parent = e.getRepresentative();
				
				// get the child node and its equivalent nodes
				Equivalences<T> namedEquivalences = getVertex(parent);
				if (namedEquivalences != null)
					result.add(namedEquivalences);
				else 
					result.addAll(getDirectSuper(e)); // recursive call if the parent is not named
			}
			return ImmutableSet.copyOf(result);
		}
		
		@Override
		public ImmutableSet<Equivalences<T>> getSuper(Equivalences<T> v) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<>();

			for (Equivalences<T> e : reasonerDAG.getSuper(v)) {
				Equivalences<T> nodes = getVertex(e.getRepresentative());
				if (nodes != null)
					result.add(nodes);			
			}
			
			return ImmutableSet.copyOf(result);
		}

		@Override
		public Stream<Equivalences<T>> stream() {
			return null;
		}

		@Override
		public T getCanonicalForm(T v) {
			return null;
		}
	}
}
