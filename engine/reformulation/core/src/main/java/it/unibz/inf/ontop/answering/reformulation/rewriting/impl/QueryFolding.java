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
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.spec.ontology.ClassExpression;
import it.unibz.inf.ontop.spec.ontology.ObjectPropertyExpression;
import it.unibz.inf.ontop.answering.reformulation.rewriting.impl.TreeWitnessSet.QueryConnectedComponentCache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibz.inf.ontop.answering.reformulation.rewriting.impl.Intersection.intersectionOf;

public class QueryFolding {
	private final QueryConnectedComponentCache cache;
	
	private Intersection<ObjectPropertyExpression> properties;
	private Set<QueryConnectedComponent.Loop> roots;
	private Intersection<ClassExpression> internalRootConcepts;
	private Set<VariableOrGroundTerm> internalRoots;
	private Set<VariableOrGroundTerm> internalDomain;
	private List<TreeWitness> interior;
	private TreeWitness.TermCover terms;
	private boolean status;

	private static final Logger log = LoggerFactory.getLogger(QueryFolding.class);
		
	@Override
	public String toString() {
		return "Query Folding: " + roots + ", internal roots " + internalRoots + " and domain: " + internalDomain + " with properties: " + properties; 
	}
	
	public QueryFolding(QueryConnectedComponentCache cache) {
		this.cache = cache;
		properties = Intersection.top();
		roots = new HashSet<>();
		internalRootConcepts = Intersection.top();
		internalRoots = new HashSet<>();
		internalDomain = new HashSet<>();
		interior = Collections.emptyList(); // in-place QueryFolding for one-step TreeWitnesses, 
		                                   //             which have no interior TreeWitnesses
		status = true;
	}
	
	public QueryFolding(QueryFolding qf) {
		this.cache = qf.cache;

		properties = qf.properties;
		roots = new HashSet<>(qf.roots);
		internalRootConcepts = qf.internalRootConcepts;
		internalRoots = new HashSet<>(qf.internalRoots);
		internalDomain = new HashSet<>(qf.internalDomain);
		interior = new LinkedList<>(qf.interior);
		status = qf.status;
	}

	
	public QueryFolding extend(TreeWitness tw) {
		assert (status);
		QueryFolding c = new QueryFolding(this);
		c.internalRoots.addAll(tw.getRoots());
		c.internalDomain.addAll(tw.getDomain());
		c.interior.add(tw);
		c.internalRootConcepts = intersectionOf(c.internalRootConcepts, tw.getRootConcepts());
		if (c.internalRootConcepts.isBottom())
			c.status = false;
		return c;
	}
	
	public boolean extend(QueryConnectedComponent.Loop root, QueryConnectedComponent.Edge edge, QueryConnectedComponent.Loop internalRoot) {
		assert(status);

		properties = intersectionOf(properties, cache.getEdgeProperties(edge, root.getTerm(), internalRoot.getTerm()));
		
		if (!properties.isBottom()) {
			internalRootConcepts = intersectionOf(internalRootConcepts, cache.getLoopConcepts(internalRoot));
			if (!internalRootConcepts.isBottom()) {
				roots.add(root);
				return true;
			}
		}
		
		status = false;
		return false;
	}
	
	public void newOneStepFolding(VariableOrGroundTerm t) {
		properties = Intersection.top();
		roots.clear();
		internalRootConcepts = Intersection.top();
		internalDomain = Collections.singleton(t);
		terms = null;
		status = true;		
	}

	public void newQueryFolding(TreeWitness tw) {
		properties = Intersection.top();
		roots.clear(); 
		internalRootConcepts = tw.getRootConcepts();
		internalRoots = new HashSet<>(tw.getRoots());
		internalDomain = new HashSet<>(tw.getDomain());
		interior = new LinkedList<>();
		interior.add(tw);
		terms = null;
		status = true;		
	}

	
	public Intersection<ObjectPropertyExpression> getProperties() {
		return properties;
	}
	
	public boolean isValid() {
		return status;
	}
	
	public Set<QueryConnectedComponent.Loop> getRoots() {
		return roots;
	}
	
	public boolean hasRoot() { 
		return !roots.isEmpty();
	}
	
	public boolean canBeAttachedToAnInternalRoot(QueryConnectedComponent.Loop t0, QueryConnectedComponent.Loop t1) {
		return internalRoots.contains(t0.getTerm()) && !internalDomain.contains(t1.getTerm()); // && !roots.contains(t1);
	}
	
	public Intersection<ClassExpression> getInternalRootConcepts() {
		return internalRootConcepts;
	}
	
	public Collection<TreeWitness> getInteriorTreeWitnesses() {
		return interior;
	}
	
	public TreeWitness.TermCover getTerms() {
		if (terms == null) {
			ImmutableSet<VariableOrGroundTerm> rootNewLiterals = roots.stream()
					.map(QueryConnectedComponent.Loop::getTerm)
					.collect(ImmutableCollectors.toSet());
			ImmutableSet<VariableOrGroundTerm> domain = Stream.concat(
						internalDomain.stream(), rootNewLiterals.stream())
					.collect(ImmutableCollectors.toSet());
			terms = new TreeWitness.TermCover(domain, rootNewLiterals);
		}
		return terms;
	}
	
	public TreeWitness getTreeWitness(ImmutableList<TreeWitnessGenerator> twg, Collection<QueryConnectedComponent.Edge> edges) {
		
		log.debug("NEW TREE WITNESS");
		log.debug("  PROPERTIES {}", properties);
		log.debug("  ENDTYPE {}", internalRootConcepts);

		Intersection<ClassExpression> rootType = Intersection.top();

		Set<DataAtom<RDFAtomPredicate>> rootAtoms = new HashSet<>();
		for (QueryConnectedComponent.Loop root : roots) {
			rootAtoms.addAll(root.getAtoms());
			if (!root.isExistentialVariable()) { // if the variable is not quantified -- not mergeable
				rootType = Intersection.bottom();
				log.debug("  NOT MERGEABLE: {} IS NOT QUANTIFIED", root);				
			}
		}
		
		// EXTEND ROOT ATOMS BY ALL-ROOT EDGES
		for (QueryConnectedComponent.Edge edge : edges) {
			if (roots.contains(edge.getLoop0()) && roots.contains(edge.getLoop1())) {
				rootAtoms.addAll(edge.getBAtoms());
				rootType = Intersection.bottom();
				log.debug("  NOT MERGEABLE: {} IS WITHIN THE ROOTS", edge);				
			}
		}
		
		log.debug("  ROOTTYPE {}", rootAtoms);

		if (!rootType.isBottom()) {
			rootType = intersectionOf(rootType, roots.stream()
					.map(root -> cache.getLoopConcepts(root))
					.collect(Intersection.toIntersection()));
			if (rootType.isBottom())
				log.debug("  NOT MERGEABLE: BOTTOM ROOT CONCEPT");
		}
		
		return new TreeWitness(twg, getTerms(), ImmutableSet.copyOf(rootAtoms), rootType);
	}
}
