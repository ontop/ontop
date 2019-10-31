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

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.ontology.ClassExpression;
import it.unibz.inf.ontop.spec.ontology.ObjectPropertyExpression;
import it.unibz.inf.ontop.answering.reformulation.rewriting.impl.QueryConnectedComponent.Edge;
import it.unibz.inf.ontop.answering.reformulation.rewriting.impl.QueryConnectedComponent.Loop;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeWitnessSet implements Iterable<ImmutableCollection<TreeWitness>> {
	private final List<TreeWitness> tws = new LinkedList<>();

	private final QueryConnectedComponent cc;
	private final CachedClassifiedTBoxWrapper cache;

	// working lists (may all be nulls)
	private List<TreeWitness> mergeable;
	private Queue<TreeWitness> delta;
	private Map<TreeWitness.TermCover, TreeWitness> twsCache;

	private static final Logger log = LoggerFactory.getLogger(TreeWitnessSet.class);

	private TreeWitnessSet(QueryConnectedComponent cc, TreeWitnessRewriterReasoner reasoner) {
		this.cc = cc;
		this.cache = new CachedClassifiedTBoxWrapper(reasoner);
	}
	
	public ImmutableList<TreeWitness> getTWs() {
		return ImmutableList.copyOf(tws);
	}
	
	public static TreeWitnessSet getTreeWitnesses(QueryConnectedComponent cc, TreeWitnessRewriterReasoner reasoner) {
		TreeWitnessSet treewitnesses = new TreeWitnessSet(cc, reasoner);
		
		if (!cc.isDegenerate())
			treewitnesses.computeTreeWitnesses();
				
		return treewitnesses;
	}

	private void computeTreeWitnesses() {		
		QueryFolding qf = new QueryFolding(cache); // in-place query folding, so copying is required when creating a tree witness
		
		for (Loop loop : cc.getQuantifiedVariables()) {
			VariableOrGroundTerm v = loop.getTerm();
			log.debug("QUANTIFIED VARIABLE {}", v); 
			qf.newOneStepFolding(v);
			
			for (Edge edge : cc.getEdges()) { // loop.getAdjacentEdges()
				if (edge.getTerm0().equals(v)) {
					if (!qf.extend(edge.getLoop1(), edge, loop))
						break;
				}
				else if (edge.getTerm1().equals(v)) {
					if (!qf.extend(edge.getLoop0(), edge, loop))
						break;
				}
			}
			
			if (qf.isValid()) {
				// tws cannot contain duplicates by construction, so no caching (even negative)
				ImmutableList<TreeWitnessGenerator> twg = getTreeWitnessGenerators(qf);
				if (!twg.isEmpty()) {
					// no need to copy the query folding: it creates all temporary objects anyway (including NewLiterals)
					tws.add(qf.getTreeWitness(twg, cc.getEdges()));
				}
			}
		}		
		
		if (!tws.isEmpty()) {
			mergeable = new ArrayList<>();
			Queue<TreeWitness> working = new LinkedList<>();

			for (TreeWitness tw : tws) 
				if (tw.isMergeable())  {
					working.add(tw);			
					mergeable.add(tw);
				}
			
			delta = new LinkedList<>();
			twsCache = new HashMap<>();

			while (!working.isEmpty()) {
				while (!working.isEmpty()) {
					TreeWitness tw = working.poll(); 
					qf.newQueryFolding(tw);
					saturateTreeWitnesses(qf); 					
				}
				
				while (!delta.isEmpty()) {
					TreeWitness tw = delta.poll();
					tws.add(tw);
					if (tw.isMergeable())  {
						working.add(tw);			
						mergeable.add(tw);
					}
				}
			}				
		}
		
		log.debug("TREE WITNESSES FOUND: {}", tws.size());
	}
	
	private void saturateTreeWitnesses(QueryFolding qf) {
		boolean saturated = true; 
		
		for (Edge edge : cc.getEdges()) { 
			Loop rootLoop, internalLoop;
			if (qf.canBeAttachedToAnInternalRoot(edge.getLoop0(), edge.getLoop1())) {
				rootLoop = edge.getLoop0();
				internalLoop = edge.getLoop1();
			}
			else if (qf.canBeAttachedToAnInternalRoot(edge.getLoop1(), edge.getLoop0())) { 
				rootLoop = edge.getLoop1();
				internalLoop = edge.getLoop0();
			}
			else
				continue;
			
			log.debug("EDGE {} IS ADJACENT TO THE TREE WITNESS {}", edge, qf); 

			if (qf.getRoots().contains(internalLoop)) {
				if (qf.extend(internalLoop, edge, rootLoop)) {
					log.debug("    RE-ATTACHING A HANDLE {}", edge);
					continue;
				}	
				else {
					log.debug("    FAILED TO RE-ATTACH A HANDLE {}", edge);
					return;					
				}
			}

			saturated = false;

			VariableOrGroundTerm rootNewLiteral = rootLoop.getTerm();
			VariableOrGroundTerm internalNewLiteral = internalLoop.getTerm();
			for (TreeWitness tw : mergeable)  
				if (tw.getRoots().contains(rootNewLiteral) && tw.getDomain().contains(internalNewLiteral)) {
					log.debug("    ATTACHING A TREE WITNESS {}", tw);
					saturateTreeWitnesses(qf.extend(tw)); 
				} 
			
			QueryFolding qf2 = new QueryFolding(qf);
			if (qf2.extend(internalLoop, edge, rootLoop)) {
				log.debug("    ATTACHING A HANDLE {}", edge);
				saturateTreeWitnesses(qf2);  
			}	
		}

		if (saturated && qf.hasRoot())  {
			if (!twsCache.containsKey(qf.getTerms())) {
				ImmutableList<TreeWitnessGenerator> twg = getTreeWitnessGenerators(qf);
				if (!twg.isEmpty()) {
					TreeWitness tw = qf.getTreeWitness(twg, cc.getEdges()); 
					delta.add(tw);
					twsCache.put(tw.getTerms(), tw);
				}
				else
					twsCache.put(qf.getTerms(), null); // cache negative
			}
			else {
				log.debug("TWS CACHE HIT {}", qf.getTerms());
			}
		}
	}
	
	// can return null if there are no applicable generators!
	
	private ImmutableList<TreeWitnessGenerator> getTreeWitnessGenerators(QueryFolding qf) {
		ImmutableList.Builder<TreeWitnessGenerator> twg = ImmutableList.builder();
		log.debug("CHECKING WHETHER THE FOLDING {} CAN BE GENERATED: ", qf); 
		for (TreeWitnessGenerator g : cache.reasoner.getTreeWitnessGenerators()) {
			DownwardSaturatedImmutableSet<ObjectPropertyExpression> subp = qf.getProperties();
			if (!subp.subsumes(g.getProperty())) {
				log.debug("      NEGATIVE PROPERTY CHECK {}", g.getProperty());
				continue;
			}
			else
				log.debug("      POSITIVE PROPERTY CHECK {}", g.getProperty());

			DownwardSaturatedImmutableSet<ClassExpression> subc = qf.getInternalRootConcepts();
			if (!g.endPointEntails(subc)) {
				 log.debug("        ENDTYPE TOO SPECIFIC: {} FOR {}", subc, g);
				 continue;			
			}
			else
				 log.debug("        ENDTYPE IS FINE: TOP FOR {}", g);

			boolean failed = false;
			for (TreeWitness tw : qf.getInteriorTreeWitnesses()) {
				if (!g.endPointEntailsAny(tw.getGenerators())) {
					log.debug("        ENDTYPE TOO SPECIFIC: {} FOR {}", tw, g);
					failed = true;
					break;
				}
				else
					log.debug("        ENDTYPE IS FINE: {} FOR {}", tw, g);
			}
			if (failed)
				continue;
			
			twg.add(g);
			log.debug("        OK");
		}
		return twg.build();
	}
	
	@Override
	public Iterator<ImmutableCollection<TreeWitness>> iterator() {
		return new CompatibleTreeWitnessSetIterator(ImmutableList.copyOf(tws));
	}


	static class CachedClassifiedTBoxWrapper {
		private final Map<Map.Entry<VariableOrGroundTerm, VariableOrGroundTerm>, DownwardSaturatedImmutableSet<ObjectPropertyExpression>> propertiesCache = new HashMap<>();
		private final Map<VariableOrGroundTerm, DownwardSaturatedImmutableSet<ClassExpression>> conceptsCache = new HashMap<>();

		private final TreeWitnessRewriterReasoner reasoner;

		private CachedClassifiedTBoxWrapper(TreeWitnessRewriterReasoner reasoner) {
			this.reasoner = reasoner;
		}

		public DownwardSaturatedImmutableSet<ClassExpression> getLoopConcepts(Loop loop) {
			return conceptsCache.computeIfAbsent(loop.getTerm(),
					t -> reasoner.getSubConcepts(loop.getAtoms()));
		}

		public DownwardSaturatedImmutableSet<ObjectPropertyExpression> getEdgeProperties(Edge edge, VariableOrGroundTerm root, VariableOrGroundTerm nonroot) {
			return propertiesCache.computeIfAbsent(new AbstractMap.SimpleImmutableEntry<>(root, nonroot),
					idx -> reasoner.getSubProperties(edge.getBAtoms(), idx));
		}
	}


	public Set<TreeWitnessGenerator> getGeneratorsOfDetachedCC() {		
		Set<TreeWitnessGenerator> generators = new HashSet<>();

		if (cc.isDegenerate()) { // do not remove the curly brackets -- dangling else otherwise
			DownwardSaturatedImmutableSet<ClassExpression> subc = cache.getLoopConcepts(cc.getLoop().get());
			log.debug("DEGENERATE DETACHED COMPONENT: {}", cc);
			if (!subc.isBottom()) // (subc == null) || 
				for (TreeWitnessGenerator twg : cache.reasoner.getTreeWitnessGenerators()) {
					if (twg.endPointEntails(subc)) {
						log.debug("        ENDTYPE IS FINE: {} FOR {}", subc, twg);
						generators.add(twg);					
					}
					else 
						log.debug("        ENDTYPE TOO SPECIFIC: {} FOR {}", subc, twg);
				}
		} 
		else {
			for (TreeWitness tw : tws) 
				if (tw.getDomain().containsAll(cc.getVariables())) {
					log.debug("TREE WITNESS {} COVERS THE QUERY",  tw);
					DownwardSaturatedImmutableSet<ClassExpression> subc = cache.reasoner.getSubConcepts(tw.getRootAtoms());
					if (!subc.isBottom())
						for (TreeWitnessGenerator twg : cache.reasoner.getTreeWitnessGenerators())
							if (twg.endPointEntails(subc)) {
								log.debug("        ENDTYPE IS FINE: {} FOR {}",  subc, twg);
								if (twg.endPointEntailsAny(tw.getGenerators())) {
									log.debug("        ENDTYPE IS FINE: {} FOR {}",  tw, twg);
									generators.add(twg);					
								}
								else  
									log.debug("        ENDTYPE TOO SPECIFIC: {} FOR {}", tw, twg);
							}
							else 
								 log.debug("        ENDTYPE TOO SPECIFIC: {} FOR {}", subc, twg);
				}
		}
		
		if (!generators.isEmpty()) {
			boolean saturated = false;
			while (!saturated) {
				saturated = true;
				for (TreeWitnessGenerator g : cache.reasoner.getTreeWitnessGenerators())
					if (g.endPointEntailsAny(generators)) {
						if (generators.add(g))
							saturated = false;
					}		 		
			} 									
		}	
		return generators;
	}

}
