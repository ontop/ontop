package org.semanticweb.ontop.owlrefplatform.core.reformulation;

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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.ontology.BasicClassDescription;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.Intersection;

/**
 * TreeWitness: universal tree witnesses as in the KR 2012 paper
 *     each tree witness is determined by its domain, root terms and a set of \exists R.B concept 
 *           that generate a tree in the TBox canonical model to embed the tree witness part of the query
 *           
 *           roots are the terms that are mapped to the root of that tree
 *           
 *           the "tree witness part of the query" consists of all atoms in the query 
 *                       with terms in the tw domain and at least one of the terms not being a tw root
 *                       
 *     each instance also stores those atoms of the query with all terms among the tw roots
 *      
 *     this information is enough to produce the tree witness formula tw_f 
 *     
 * @author Roman Kontchakov
 *
 */

public class TreeWitness {
	private final TermCover terms;
	
	private final Set<Function> rootAtoms; // atoms of the query that contain only the roots of the tree witness
	                            // these atoms must hold true for this tree witness to be realised
	private final Collection<TreeWitnessGenerator> gens; // the \exists R.B concepts that realise the tree witness 
	                                          // in the canonical model of the TBox
	
	private final Intersection<BasicClassDescription> rootConcepts; // store concept for merging tree witnesses
	
	private List<List<Function>> twfs;  // tw-formula: disjunction of conjunctions of atoms

	public TreeWitness(Collection<TreeWitnessGenerator> gens, TermCover terms, Set<Function> rootAtoms, Intersection<BasicClassDescription> rootConcepts) {
		this.gens = gens;
		this.terms = terms;
		this.rootAtoms = rootAtoms;
		this.rootConcepts = rootConcepts;
		//this.domain = domain; // new HashSet<term>(roots); domain.addAll(nonroots);
	}
	
	void setFormula(List<List<Function>> twfs) {
		this.twfs = twfs;
	}
	
	public List<List<Function>> getFormula() {
		return twfs;
	}
	
	public Intersection<BasicClassDescription> getRootConcepts() {
		return rootConcepts;
	}
	
	/**
	 * Set<Term> getRoots()
	 * 
	 * @return set of roots of the tree witness
	 */
	public Set<Term> getRoots() {
		return terms.roots;
	}
	
	/**
	 * boolean isMergeable()
	 * 
	 * @return true if all root terms are quantified variables and there is the intersection of root concepts is non-empty
	 */
	public boolean isMergeable() {
		return !rootConcepts.isBottom();
	}
	
	/**
	 * Set<Term> getDomain()
	 * 
	 * @return the domain (set of terms) of the tree witness
	 */
	
	public Set<Term> getDomain() {
		return terms.domain;
	}
	
	public TermCover getTerms() {
		return terms;
	}
	
	/**
	 * Set<TreeWitnessGenerator> getGenerator()
	 * 
	 * @return the tree witness generators \exists R.B
	 */
	
	public Collection<TreeWitnessGenerator> getGenerators() {
		return gens;
	}

	/**
	 * getGeneratorSubConcepts
	 * 
	 * @return the set of all sub-concepts for all of the tree witness generators
	 */
	
	
	public Set<BasicClassDescription> getGeneratorSubConcepts() {
		if (gens.size() == 1)
			return gens.iterator().next().getSubConcepts();
		
		Set<BasicClassDescription> all = new HashSet<BasicClassDescription>();		
		for (TreeWitnessGenerator twg : gens) 
			all.addAll(twg.getSubConcepts());
		return all;
	}
	
	
	/**
	 * Set<Function> getRootAtoms()
	 * 
	 * @return query atoms with all terms among the roots of tree witness
	 */
	
	public Set<Function> getRootAtoms() {
		return rootAtoms;
	}

	/**
	 * boolean isCompatibleWith(TreeWitness tw1, TreeWitness tw2)
	 * 
	 * tree witnesses are consistent iff their domains intersect on their **common** roots
	 * 
	 * @param tw1: a tree witness
	 * @return true if tw1 is compatible with the given tree witness
	 */
	
	public static boolean isCompatible(TreeWitness tw1, TreeWitness tw2) {
		Set<Term> commonTerms = new HashSet<Term>(tw1.getDomain());
		commonTerms.retainAll(tw2.getDomain());
		if (!commonTerms.isEmpty()) {
			if (!tw1.getRoots().containsAll(commonTerms) || !tw2.getRoots().containsAll(commonTerms))
				return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "tree witness generated by " + gens + "\n    with domain " + terms + " and root atoms " + rootAtoms;
	}

	/**
	 * TermCover stores the domain and the set of roots of a tree witness
	 * 
	 * implements methods for efficient comparison and hashing
	 * 
	 * @author Roman Kontchakov
	 *
	 */
	
	public static class TermCover {
		private final Set<Term> domain; // terms that are covered by the tree witness
		private final Set<Term> roots;   // terms that are mapped onto the root of the tree witness
		
		public TermCover(Set<Term> domain, Set<Term> roots) {
			this.domain = domain;
			this.roots = roots;
		}
		
		public Set<Term> getDomain() {
			return domain;
		}
		
		public Set<Term> getRoots() {
			return roots;
		}
		
		@Override
		public String toString() {
			return "tree witness domain " + domain + " with roots " + roots;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof TermCover) {
				TermCover other = (TermCover)obj;
				return this.roots.equals(other.roots) && 
					   this.domain.equals(other.domain);			
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return roots.hashCode() ^ domain.hashCode(); 
		}
	}

}
