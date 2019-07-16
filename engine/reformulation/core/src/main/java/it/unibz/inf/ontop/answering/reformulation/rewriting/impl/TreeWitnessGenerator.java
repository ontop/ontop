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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeWitnessGenerator {
	private final ObjectPropertyExpression property;
//	private final OClass filler;

	private final ImmutableSet<ClassExpression> concepts; // set of generating-concepts

	private static final Logger log = LoggerFactory.getLogger(TreeWitnessGenerator.class);

	public TreeWitnessGenerator(ObjectPropertyExpression property/*, OClass filler*/, ImmutableSet<ClassExpression> concepts) {
		this.property = property;
//		this.filler = filler;
		this.concepts = concepts;
	}

	// tree witness generators of the ontology (i.e., positive occurrences of \exists R.B)

	public static ImmutableList<TreeWitnessGenerator> getTreeWitnessGenerators(ClassifiedTBox reasoner) {
		
		ImmutableList.Builder<TreeWitnessGenerator> gens = ImmutableList.builder();

		for (Equivalences<ClassExpression> set : reasoner.classesDAG()) {
			Set<Equivalences<ClassExpression>> subClasses = reasoner.classesDAG().getSub(set);
			if (set.size() > 1 || subClasses.size() > 1) { // otherwise cannot give rise to any generator
				for (ClassExpression concept : set) {
					if (concept instanceof ObjectSomeValuesFrom) {
						if (!getMaximalRepresentatives(reasoner, (ObjectSomeValuesFrom) concept).isEmpty())
						gens.add(new TreeWitnessGenerator(((ObjectSomeValuesFrom) concept).getProperty(), ImmutableSet.of(concept)));
					}
				}
			}
		}

		return gens.build();
	}
	
	public static Set<ClassExpression> getMaximalRepresentatives(ClassifiedTBox reasoner, ObjectSomeValuesFrom generatingConcept) {
		Equivalences<ClassExpression> eq = reasoner.classesDAG().getVertex(generatingConcept);
		Stream<ClassExpression> opRep = Stream.of();
		if (eq.getRepresentative() instanceof ObjectSomeValuesFrom) {
			ObjectSomeValuesFrom rep = (ObjectSomeValuesFrom)eq.getRepresentative();
			if (!reasoner.objectPropertiesDAG().getVertex(generatingConcept.getProperty()).contains(rep.getProperty()))
				opRep = Stream.of(rep);
		}
		else
			opRep = Stream.of(eq.getRepresentative());

		ImmutableSet<Equivalences<ClassExpression>> directSubEqs = reasoner.classesDAG().getDirectSub(eq);
		return Stream.concat(directSubEqs.stream().map(e -> e.getRepresentative()), opRep)
				.collect(ImmutableCollectors.toSet());
	}

	public ImmutableSet<ClassExpression> getGeneratingConcepts() {
		return concepts;
	}

	public ObjectPropertyExpression getProperty() {
		return property;
	}

	public boolean endPointEntailsAny(ImmutableList<DownwardSaturatedImmutableSet<ClassExpression>> list) {
		return list.stream().anyMatch(s -> this.endPointEntails(s));
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
