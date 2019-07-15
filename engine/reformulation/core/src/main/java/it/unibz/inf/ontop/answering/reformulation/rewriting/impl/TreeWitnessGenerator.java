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

import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeWitnessGenerator {
	private final ObjectPropertyExpression property;
//	private final OClass filler;

	private final ImmutableSet<ClassExpression> concepts; // set of generating-concepts

	private static final Logger log = LoggerFactory.getLogger(TreeWitnessGenerator.class);

	public TreeWitnessGenerator(ClassifiedTBox reasoner, ObjectPropertyExpression property/*, OClass filler*/, ImmutableSet<ClassExpression> concepts) {
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
						ImmutableSet<ClassExpression> flatSubClasses = subClasses.stream().flatMap(s -> s.stream())
								.filter(s -> !s.equals(concept))
								.collect(ImmutableCollectors.toSet());

						flatSubClasses.forEach(subConcept -> log.debug("GENERATING CI: {} <= {}", subConcept, concept));
						gens.add(new TreeWitnessGenerator(reasoner, ((ObjectSomeValuesFrom) concept).getProperty(), flatSubClasses));
					}
				}
			}
		}

		return gens.build();
	}
	
	
	public static Set<ClassExpression> getMaximalBasicConcepts(ImmutableList<TreeWitnessGenerator> gens, ClassifiedTBox reasoner) {
		Set<ClassExpression> concepts = new HashSet<>();
		for (TreeWitnessGenerator twg : gens) 
			concepts.addAll(twg.concepts);

		if (concepts.isEmpty())
			return concepts;
		
		if (concepts.size() == 1 && concepts.iterator().next() instanceof OClass)
			return concepts;
		
		log.debug("MORE THAN ONE GENERATING CONCEPT: {}", concepts);
		// add all sub-concepts of all \exists R
		Set<ClassExpression> extension = new HashSet<>();
		for (ClassExpression b : concepts) 
			if (b instanceof ObjectSomeValuesFrom)
				extension.addAll(reasoner.classesDAG().getSubRepresentatives(b));
		concepts.addAll(extension);
		
		// use all concept names to subsume their sub-concepts
		{
			boolean modified = true; 
			while (modified) {
				modified = false;
				for (ClassExpression b : concepts) 
					if (b instanceof OClass) {
						Set<ClassExpression> bsubconcepts = reasoner.classesDAG().getSubRepresentatives(b);
						Iterator<ClassExpression> i = concepts.iterator();
						while (i.hasNext()) {
							ClassExpression bp = i.next();
							if ((b != bp) && bsubconcepts.contains(bp)) { 
								i.remove();
								modified = true;
							}
						}
						if (modified)
							break;
					}
			}
		}
		
		// use all \exists R to subsume their sub-concepts of the form \exists R
		{
			boolean modified = true;
			while (modified) {
				modified = false;
				for (ClassExpression b : concepts) 
					if (b instanceof ObjectSomeValuesFrom) {
						ObjectSomeValuesFrom some = (ObjectSomeValuesFrom)b;
						ObjectPropertyExpression prop = some.getProperty();
						Set<ObjectPropertyExpression> bsubproperties = reasoner.objectPropertiesDAG().getSubRepresentatives(prop);
						Iterator<ClassExpression> i = concepts.iterator();
						while (i.hasNext()) {
							ClassExpression bp = i.next();
							if ((b != bp) && (bp instanceof ObjectSomeValuesFrom)) {
								ObjectSomeValuesFrom somep = (ObjectSomeValuesFrom)bp;
								ObjectPropertyExpression propp = somep.getProperty();
								
								if (bsubproperties.contains(propp)) {
									i.remove();
									modified = true;
								}
							}
						}
						if (modified)
							break;
					}
			}
		}
		
		return concepts;
	}

	public ImmutableSet<ClassExpression> getGeneratingConcepts() {
		return concepts;
	}


	public ObjectPropertyExpression getProperty() {
		return property;
	}
	
	public boolean endPointEntailsAnyOf(ImmutableSet<ClassExpression> subc) {
		return subc.contains(property.getRange()); // || subc.contains(filler);
	}
	
	public boolean endPointEntailsAnyOf(Intersection<ClassExpression> subc) {
		return subc.subsumes(property.getRange()); // || subc.subsumes(filler);
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
