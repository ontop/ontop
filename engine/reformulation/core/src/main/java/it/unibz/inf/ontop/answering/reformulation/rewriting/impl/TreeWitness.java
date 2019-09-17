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

import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.spec.ontology.ClassExpression;

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
 * @author Roman Kontchakov
 *
 */

public class TreeWitness {
	private final TermCover terms;
	
	private final ImmutableSet<DataAtom<RDFAtomPredicate>> rootAtoms; // atoms of the query that contain only the roots of the tree witness
	                            // these atoms must hold true for this tree witness to be realised
	private final ImmutableList<TreeWitnessGenerator> generators; // the \exists R.B concepts that realise the tree witness
	                                          // in the canonical model of the TBox
	
	private final DownwardSaturatedImmutableSet<ClassExpression> rootConcepts; // store concept for merging tree witnesses
	
	public TreeWitness(ImmutableList<TreeWitnessGenerator> generators, TermCover terms, ImmutableSet<DataAtom<RDFAtomPredicate>> rootAtoms, DownwardSaturatedImmutableSet<ClassExpression> rootConcepts) {
		this.generators = generators;
		this.terms = terms;
		this.rootAtoms = rootAtoms;
		this.rootConcepts = rootConcepts;
	}

	public DownwardSaturatedImmutableSet<ClassExpression> getRootConcepts() {
		return rootConcepts;
	}
	
	/**
	 * Set<Term> getRoots()
	 * 
	 * @return set of roots of the tree witness
	 */
	public ImmutableSet<VariableOrGroundTerm> getRoots() {
		return terms.roots;
	}
	
	/**
	 * boolean isMergeable()
	 * @return true if all root terms are quantified variables
	 *                  (otherwise, the constructing code sets the rootConcepts to BOT)
	 *                 and the intersection of root concepts is non-empty
	 */
	public boolean isMergeable() {
		return !rootConcepts.isBottom();
	}
	
	/**
	 * ImmutableSet<Term> getDomain()
	 * 
	 * @return the domain (set of terms) of the tree witness
	 */
	
	public ImmutableSet<VariableOrGroundTerm> getDomain() {
		return terms.domain;
	}

	public TermCover getTerms() { return terms; }
	
	/**
	 * ImmutableList<TreeWitnessGenerator> getGenerator()
	 * 
	 * @return the tree witness generators \exists R.B
	 */
	
	public ImmutableList<TreeWitnessGenerator> getGenerators() {
		return generators;
	}

	
	/**
	 * ImmutableSet<DataAtom<RDFAtomPredicate>> getRootAtoms()
	 * 
	 * @return query atoms with all terms among the roots of tree witness
	 */
	
	public ImmutableSet<DataAtom<RDFAtomPredicate>> getRootAtoms() {
		return rootAtoms;
	}

	/**
	 * boolean isCompatible(TreeWitness tw1, TreeWitness tw2)
	 * 
	 * distinct tree witnesses are compatible iff their domains intersect on their **common** roots
	 * 
	 * @param tw1: a tree witness
	 * @param tw2: a tree witness
	 * @return true if tw1 is compatible with tw2
	 */
	
	private static boolean isCompatible(TreeWitness tw1, TreeWitness tw2) {
		Set<VariableOrGroundTerm> commonTerms = new HashSet<>(tw1.getDomain());
		commonTerms.retainAll(tw2.getDomain());
		return commonTerms.isEmpty() /* an optimisation - not necessary */ ||
				(tw1.getRoots().containsAll(commonTerms) && tw2.getRoots().containsAll(commonTerms));
	}

	public static boolean isCompatible(ImmutableList<TreeWitness> tws) {
		for (ListIterator<TreeWitness> i = tws.listIterator(tws.size()); i.hasPrevious(); ) {
			TreeWitness tw0 = i.previous();
			for (ListIterator<TreeWitness> j = tws.listIterator(); j.nextIndex() <= i.previousIndex(); )
				if (!isCompatible(tw0, j.next()))
					return false;
		}
		return true;
	}



	@Override
	public String toString() {
		return "tree witness generated by " + generators + "\n    with domain " + terms + " and root atoms " + rootAtoms;
	}

	/**
	 * TermCover stores the domain and the set of roots of a tree witness
	 * 
	 * implements methods for efficient comparison and hashing
	 * 
	 * @author Roman Kontchakov
	 *
	 */
	
	public static final class TermCover {
		private final ImmutableSet<VariableOrGroundTerm> domain; // terms that are covered by the tree witness
		private final ImmutableSet<VariableOrGroundTerm> roots;  // terms that are mapped onto the root of the tree witness
		
		public TermCover(ImmutableSet<VariableOrGroundTerm> domain, ImmutableSet<VariableOrGroundTerm> roots) {
			this.domain = domain;
			this.roots = roots;
		}
		
		public ImmutableSet<VariableOrGroundTerm> getDomain() {
			return domain;
		}
		
		public ImmutableSet<VariableOrGroundTerm> getRoots() {
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
				return this.roots.equals(other.roots) && this.domain.equals(other.domain);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return roots.hashCode() ^ domain.hashCode(); 
		}
	}

}
