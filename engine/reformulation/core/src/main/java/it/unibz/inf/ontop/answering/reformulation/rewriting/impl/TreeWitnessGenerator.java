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

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.spec.ontology.*;

import java.util.*;


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
