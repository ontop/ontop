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

import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.spec.ontology.ClassExpression;
import it.unibz.inf.ontop.spec.ontology.ObjectPropertyExpression;
import it.unibz.inf.ontop.answering.reformulation.rewriting.impl.TreeWitnessSet.QueryConnectedComponentCache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryFolding {
	private final QueryConnectedComponentCache cache;
	
	private Intersection<ObjectPropertyExpression> properties;
	private Set<QueryConnectedComponent.Loop> roots;
	private Intersection<ClassExpression> internalRootConcepts;
	private Set<Term> internalRoots;
	private Set<Term> internalDomain;
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
		properties = cache.getTopProperty(); 
		roots = new HashSet<>();
		internalRootConcepts = cache.getTopClass(); 
		internalRoots = new HashSet<>();
		internalDomain = new HashSet<>();
		interior = Collections.emptyList(); // in-place QueryFolding for one-step TreeWitnesses, 
		                                   //             which have no interior TreeWitnesses
		status = true;
	}
	
	public QueryFolding(QueryFolding qf) {
		this.cache = qf.cache;

		properties = new Intersection<>(qf.properties);
		roots = new HashSet<>(qf.roots);
		internalRootConcepts = new Intersection<>(qf.internalRootConcepts);
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
		c.internalRootConcepts.intersectWith(tw.getRootConcepts());
		if (c.internalRootConcepts.isBottom())
			c.status = false;
		return c;
	}
	
	public boolean extend(QueryConnectedComponent.Loop root, QueryConnectedComponent.Edge edge, QueryConnectedComponent.Loop internalRoot) {
		assert(status);

		properties.intersectWith(cache.getEdgeProperties(edge, root.getTerm(), internalRoot.getTerm()));
		
		if (!properties.isBottom()) {
			internalRootConcepts.intersectWith(cache.getLoopConcepts(internalRoot));
			if (!internalRootConcepts.isBottom()) {
				roots.add(root);
				return true;
			}
		}
		
		status = false;
		return false;
	}
	
	public void newOneStepFolding(Term t) {
		properties.setToTop();
		roots.clear();
		internalRootConcepts.setToTop(); 
		internalDomain = Collections.singleton(t);
		terms = null;
		status = true;		
	}

	public void newQueryFolding(TreeWitness tw) {
		properties.setToTop(); 
		roots.clear(); 
		internalRootConcepts = new Intersection<>(tw.getRootConcepts());
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
			Set<Term> domain = new HashSet<Term>(internalDomain);
			Set<Term> rootNewLiterals = new HashSet<Term>();
			for (QueryConnectedComponent.Loop l : roots)
				rootNewLiterals.add(l.getTerm());
			domain.addAll(rootNewLiterals);
			terms = new TreeWitness.TermCover(domain, rootNewLiterals);
		}
		return terms;
	}
	
	public TreeWitness getTreeWitness(Collection<TreeWitnessGenerator> twg, Collection<QueryConnectedComponent.Edge> edges) {
		
		log.debug("NEW TREE WITNESS");
		log.debug("  PROPERTIES {}", properties);
		log.debug("  ENDTYPE {}", internalRootConcepts);

		Intersection<ClassExpression> rootType = cache.getTopClass();

		Set<Function> rootAtoms = new HashSet<>();
		for (QueryConnectedComponent.Loop root : roots) {
			rootAtoms.addAll(root.getAtoms());
			if (!root.isExistentialVariable()) { // if the variable is not quantified -- not mergeable
				rootType.setToBottom();
				log.debug("  NOT MERGEABLE: {} IS NOT QUANTIFIED", root);				
			}
		}
		
		// EXTEND ROOT ATOMS BY ALL-ROOT EDGES
		for (QueryConnectedComponent.Edge edge : edges) {
			if (roots.contains(edge.getLoop0()) && roots.contains(edge.getLoop1())) {
				rootAtoms.addAll(edge.getBAtoms());
				rootType.setToBottom();
				log.debug("  NOT MERGEABLE: {} IS WITHIN THE ROOTS", edge);				
			}
		}
		
		log.debug("  ROOTTYPE {}", rootAtoms);

		if (!rootType.isBottom()) // not empty 
			for (QueryConnectedComponent.Loop root : roots) {
				rootType.intersectWith(cache.getLoopConcepts(root));
				if (rootType.isBottom()) { // empty intersection -- not mergeable
					log.debug("  NOT MERGEABLE: BOTTOM ROOT CONCEPT");
					break;
				}
			}
		
		return new TreeWitness(twg, getTerms(), rootAtoms, rootType); 	
	}
}
