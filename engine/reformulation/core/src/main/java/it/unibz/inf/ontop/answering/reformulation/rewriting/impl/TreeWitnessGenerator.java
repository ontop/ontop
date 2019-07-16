package it.unibz.inf.ontop.answering.reformulation.rewriting.impl;

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
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;

import java.util.*;
import java.util.stream.Stream;

import it.unibz.inf.ontop.utils.ImmutableCollectors;

public class TreeWitnessGenerator {
	private final ObjectPropertyExpression property;
//	private final OClass filler;

	private final DownwardSaturatedImmutableSet<ClassExpression> concepts;
	private final ImmutableSet<ClassExpression> maximalRepresentatives;


	public TreeWitnessGenerator(ObjectPropertyExpression property/*, OClass filler*/, DownwardSaturatedImmutableSet<ClassExpression> concepts, ImmutableSet<ClassExpression> maximalRepresentatives) {
		this.property = property;
//		this.filler = filler;
		this.concepts = concepts;
		this.maximalRepresentatives = maximalRepresentatives;
	}

	// tree witness generators of the ontology (i.e., positive occurrences of \exists R.B)

	public static ImmutableList<TreeWitnessGenerator> getTreeWitnessGenerators(ClassifiedTBox reasoner, TreeWitnessSet.ClassifiedTBoxWrapper reasonerWrapper) {
		
		ImmutableList.Builder<TreeWitnessGenerator> gens = ImmutableList.builder();

		for (Equivalences<ClassExpression> eq : reasoner.classesDAG()) {
				ImmutableSet<ClassExpression> maximalRepresentatives = getMaximalRepresentatives(reasoner, eq);
				if (!maximalRepresentatives.isEmpty()) {
					DownwardSaturatedImmutableSet<ClassExpression> generators = reasonerWrapper.getSubConcepts(eq.getRepresentative());
					eq.stream()
							.filter(ce -> ce instanceof ObjectSomeValuesFrom)
							.map(ce -> reasoner.objectPropertiesDAG().getVertex(((ObjectSomeValuesFrom) ce).getProperty()))
							.distinct()
							.map(v -> v.getRepresentative())
							.map(r -> new TreeWitnessGenerator(r, generators, maximalRepresentatives))
							.forEach(twg -> gens.add(twg));
				}
		}

		return gens.build();
	}
	
	private static ImmutableSet<ClassExpression> getMaximalRepresentatives(ClassifiedTBox reasoner, Equivalences<ClassExpression> eq) {
		Stream<ClassExpression> opRep;
		if (eq.getRepresentative() instanceof ObjectSomeValuesFrom) {
			// in particular, there are no class names in eq,
			// but the property is a representative its equivalence class
			ObjectPropertyExpression property = ((ObjectSomeValuesFrom)eq.getRepresentative()).getProperty();
			// collect domains of all distinct representatives different from the property
			opRep = eq.stream()
					.filter(ce -> ce instanceof ObjectSomeValuesFrom)
					.map(ce -> reasoner.objectPropertiesDAG().getVertex(((ObjectSomeValuesFrom)ce).getProperty()))
					.distinct()
					.map(v -> v.getRepresentative())
					.filter(r -> r != property)
					.map(r -> r.getDomain());
		}
		else
			opRep = Stream.of(eq.getRepresentative());

		ImmutableSet<Equivalences<ClassExpression>> directSubEqs = reasoner.classesDAG().getDirectSub(eq);
		return Stream.concat(directSubEqs.stream().map(e -> e.getRepresentative()), opRep)
				.collect(ImmutableCollectors.toSet());
	}

	public DownwardSaturatedImmutableSet<ClassExpression> getGeneratorConcepts() {
		return concepts;
	}

	public ImmutableSet<ClassExpression> getMaximalGeneratorRepresentatives() {
		return maximalRepresentatives;
	}

	public ObjectPropertyExpression getProperty() {
		return property;
	}

	public boolean endPointEntailsAny(Collection<TreeWitnessGenerator> twgs) {
		return twgs.stream().anyMatch(twg -> this.endPointEntails(twg.getGeneratorConcepts()));
	}

	public boolean endPointEntails(DownwardSaturatedImmutableSet<ClassExpression> s) {
		return s.subsumes(property.getRange()); // || s.subsumes(filler);
	}
	
	@Override 
	public String toString() {
		return "tw-generator E" + property.toString(); // + "." + filler.toString();
	}
	
	@Override
	public int hashCode() {
		return property.hashCode(); // ^ filler.hashCode();
	}
	
	@Override 
	public boolean equals(Object other) {
		if (other instanceof TreeWitnessGenerator) {
			TreeWitnessGenerator o = (TreeWitnessGenerator)other;
			return this.property.equals(o.property); // && this.filler.equals(o.filler));		
		}
		return false;
	}
}
