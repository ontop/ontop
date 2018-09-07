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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.reformulation.rewriting.ExistentialQueryRewriter;
import it.unibz.inf.ontop.answering.reformulation.rewriting.LinearInclusionDependencyTools;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.TriplePredicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.datalog.impl.CQCUtilities;
import it.unibz.inf.ontop.datalog.impl.CQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.answering.reformulation.rewriting.impl.QueryConnectedComponent.Edge;
import it.unibz.inf.ontop.answering.reformulation.rewriting.impl.QueryConnectedComponent.Loop;
import it.unibz.inf.ontop.answering.reformulation.rewriting.impl.TreeWitnessSet.CompatibleTreeWitnessSetIterator;

import java.util.*;

import it.unibz.inf.ontop.substitution.impl.SubstitutionUtilities;
import it.unibz.inf.ontop.substitution.impl.UnifierUtilities;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 */

public class TreeWitnessRewriter implements ExistentialQueryRewriter {

	private static final Logger log = LoggerFactory.getLogger(TreeWitnessRewriter.class);

	private ClassifiedTBox reasoner;
	private CQContainmentCheckUnderLIDs dataDependenciesCQC;
	private ImmutableMultimap<Predicate, LinearInclusionDependency> sigma;

	private Collection<TreeWitnessGenerator> generators;
	private final AtomFactory atomFactory;
	private final TermFactory termFactory;
	private final DatalogFactory datalogFactory;
	private final DatalogQueryServices datalogQueryServices;
	private final UnifierUtilities unifierUtilities;
	private final SubstitutionUtilities substitutionUtilities;
	private final CQCUtilities cqcUtilities;
	private final ImmutabilityTools immutabilityTools;
	private final LinearInclusionDependencyTools inclusionDependencyTools;

	@Inject
	private TreeWitnessRewriter(AtomFactory atomFactory, TermFactory termFactory, DatalogFactory datalogFactory,
								DatalogQueryServices datalogQueryServices, UnifierUtilities unifierUtilities,
								SubstitutionUtilities substitutionUtilities, CQCUtilities cqcUtilities,
								ImmutabilityTools immutabilityTools, LinearInclusionDependencyTools inclusionDependencyTools) {
		this.atomFactory = atomFactory;
		this.termFactory = termFactory;
		this.datalogFactory = datalogFactory;
		this.datalogQueryServices = datalogQueryServices;
		this.unifierUtilities = unifierUtilities;
		this.substitutionUtilities = substitutionUtilities;
		this.cqcUtilities = cqcUtilities;
		this.immutabilityTools = immutabilityTools;
		this.inclusionDependencyTools = inclusionDependencyTools;
	}

	@Override
	public void setTBox(ClassifiedTBox reasoner) {
		double startime = System.currentTimeMillis();

		this.reasoner = reasoner;

		ImmutableList<LinearInclusionDependency> s = inclusionDependencyTools.getABoxDependencies(reasoner, true);
		sigma = LinearInclusionDependency.toMultimap(s);

		dataDependenciesCQC = new CQContainmentCheckUnderLIDs(s, datalogFactory, unifierUtilities,
				substitutionUtilities, termFactory);
		
		generators = TreeWitnessGenerator.getTreeWitnessGenerators(reasoner);
		
//		log.debug("SET SIGMA");
//		for (Axiom ax : sigma.getAssertions()) {
//			log.debug("SIGMA: " + ax);
//		}
		
		double endtime = System.currentTimeMillis();
		double tm = (endtime - startime) / 1000;
		time += tm;
		log.debug(String.format("setTBox time: %.3f s (total %.3f s)", tm, time));		
	}
	
	
	@Override
    public ImmutableMultimap<Predicate, LinearInclusionDependency> getSigma() {
	    return sigma;
    }

	/*
	 * returns an atom with given arguments and the predicate name formed by the given URI basis and string fragment
	 */
	
	private Function getHeadAtom(String base, String suffix, List<Term> arguments) {
		Predicate predicate = datalogFactory.getSubqueryPredicate(base + suffix, arguments.size());
		return termFactory.getFunction(predicate, arguments);
	}
	
	private int freshVarIndex = 0;
	
	private Variable getFreshVariable() {
		freshVarIndex++;
		return termFactory.getVariable("twr" + freshVarIndex);
	}
	
	/*
	 * returns atoms E of a given collection of tree witness generators; 
	 * the `free' variable of the generators is replaced by the term r0;
	 */

	private ImmutableList<Function> getAtomsForGenerators(Collection<TreeWitnessGenerator> gens, Term r0)  {
		return TreeWitnessGenerator.getMaximalBasicConcepts(gens, reasoner).stream()
				.map(con -> {
					log.debug("  BASIC CONCEPT: {}", con);
					if (con instanceof OClass) {
						return atomFactory.getMutableTripleBodyAtom(r0, ((OClass) con).getIRI());
					}
					else if (con instanceof ObjectSomeValuesFrom) {
						ObjectPropertyExpression ope = ((ObjectSomeValuesFrom)con).getProperty();
						return (!ope.isInverse())
								? atomFactory.getMutableTripleBodyAtom(r0, ope.getIRI(), getFreshVariable())
								: atomFactory.getMutableTripleBodyAtom(getFreshVariable(), ope.getIRI(), r0);
					}
					else {
						DataPropertyExpression dpe = ((DataSomeValuesFrom)con).getProperty();
						return atomFactory.getMutableTripleBodyAtom(r0, dpe.getIRI(), getFreshVariable());
					}
				})
				.collect(ImmutableCollectors.toList());
	}
	
	
	
	/*
	 * rewrites a given connected CQ with the rules put into output
	 */
	
	private Collection<CQIE> rewriteCC(QueryConnectedComponent cc, Function headAtom,  Multimap<Predicate, CQIE> edgeDP) {
		
		List<CQIE> outputRules = new LinkedList<>();	

		TreeWitnessSet tws = TreeWitnessSet.getTreeWitnesses(cc, reasoner, generators, immutabilityTools);

		if (cc.hasNoFreeTerms()) {  
			if (!cc.isDegenerate() || cc.getLoop() != null) 
				for (Function a : getAtomsForGenerators(tws.getGeneratorsOfDetachedCC(), getFreshVariable())) {
					outputRules.add(datalogFactory.getCQIE(headAtom, a));
				}
		}

		// COMPUTE AND STORE TREE WITNESS FORMULAS
		for (TreeWitness tw : tws.getTWs()) {
			log.debug("TREE WITNESS: {}", tw);		
			List<Function> twf = new LinkedList<>();
			
			// equality atoms
			Iterator<Term> i = tw.getRoots().iterator();
			Term r0 = i.next();
			while (i.hasNext()) 
				twf.add(termFactory.getFunctionEQ(i.next(), r0));
			
			// root atoms
			for (Function a : tw.getRootAtoms()) {
				if (!(a.getFunctionSymbol() instanceof TriplePredicate))
					throw new MinorOntopInternalBugException("A triple atom was expected: " + a);

				boolean isClass = ((TriplePredicate) a.getFunctionSymbol()).getClassIRI(
						a.getTerms().stream()
								.map(immutabilityTools::convertIntoImmutableTerm)
								.collect(ImmutableCollectors.toList()))
						.isPresent();

				twf.add(isClass
						? atomFactory.getMutableTripleAtom(r0, a.getTerm(1), a.getTerm(2))
						: atomFactory.getMutableTripleAtom(r0, a.getTerm(1), r0));
			}
			
			List<Function> genAtoms = getAtomsForGenerators(tw.getGenerators(), r0);			
			boolean subsumes = false;
//			for (Function a : genAtoms) 				
//				if (twf.subsumes(a)) {
//					subsumes = true;
//					log.debug("TWF {} SUBSUMES {}", twf.getAllAtoms(), a);
//					break;
//				}

			List<List<Function>> twfs = new ArrayList<>(subsumes ? 1 : genAtoms.size());
//			if (!subsumes) {
				for (Function a : genAtoms) {				
					LinkedList<Function> twfa = new LinkedList<>(twf);
					twfa.add(a); // 
					twfs.add(twfa);
				}
//			}
//			else
//				twfs.add(twf.getAllAtoms());
			
			tw.setFormula(twfs);
		}

		final String headURI = headAtom.getFunctionSymbol().getName();
		if (!cc.isDegenerate()) {
			if (tws.hasConflicts()) { 
				// there are conflicting tree witnesses
				// use compact exponential rewriting by enumerating all compatible subsets of tree witnesses
				CompatibleTreeWitnessSetIterator iterator = tws.getIterator();
				while (iterator.hasNext()) {
					Collection<TreeWitness> compatibleTWs = iterator.next();
					log.debug("COMPATIBLE: {}", compatibleTWs);
					LinkedList<Function> mainbody = new LinkedList<Function>(); 
					
					for (Edge edge : cc.getEdges()) {
						boolean contained = false;
						for (TreeWitness tw : compatibleTWs)
							if (tw.getDomain().contains(edge.getTerm0()) && tw.getDomain().contains(edge.getTerm1())) {
								contained = true;
								log.debug("EDGE {} COVERED BY {}", edge, tw);
								break;
							}
						if (!contained) {
							log.debug("EDGE {} NOT COVERED BY ANY TW",  edge);
							mainbody.addAll(edge.getAtoms());
						}
					}
					for (TreeWitness tw : compatibleTWs) {
						Function twAtom = getHeadAtom(headURI, "_TW_" + (edgeDP.size() + 1), cc.getVariables());
						mainbody.add(twAtom);				
						for (List<Function> twfa : tw.getFormula())
							edgeDP.put(twAtom.getFunctionSymbol(), datalogFactory.getCQIE(twAtom, twfa));
					}	
					mainbody.addAll(cc.getNonDLAtoms());					
					outputRules.add(datalogFactory.getCQIE(headAtom, mainbody));
				}
			}
			else {
				// no conflicting tree witnesses
				// use polynomial tree witness rewriting by treating each edge independently 
				LinkedList<Function> mainbody = new LinkedList<>();
				for (Edge edge : cc.getEdges()) {
					log.debug("EDGE {}", edge);
					
					Function edgeAtom = null;
					for (TreeWitness tw : tws.getTWs())
						if (tw.getDomain().contains(edge.getTerm0()) && tw.getDomain().contains(edge.getTerm1())) {
							if (edgeAtom == null) {
								//IRI atomURI = edge.getBAtoms().iterator().next().getIRI().getName();
								edgeAtom = getHeadAtom(headURI, 
										"_EDGE_" + (edgeDP.size() + 1) /*+ "_" + atomURI.getRawFragment()*/, cc.getVariables());
								mainbody.add(edgeAtom);				
								
								LinkedList<Function> edgeAtoms = new LinkedList<>();
								edgeAtoms.addAll(edge.getAtoms());
								edgeDP.put(edgeAtom.getFunctionSymbol(), datalogFactory.getCQIE(edgeAtom, edgeAtoms));
							}
							
							for (List<Function> twfa : tw.getFormula())
								edgeDP.put(edgeAtom.getFunctionSymbol(), datalogFactory.getCQIE(edgeAtom, twfa));
						}
					
					if (edgeAtom == null) // no tree witnesses -- direct insertion into the main body
						mainbody.addAll(edge.getAtoms());
				}
				mainbody.addAll(cc.getNonDLAtoms());
				outputRules.add(datalogFactory.getCQIE(headAtom, mainbody));
			}
		}
		else {
			// degenerate connected component
			LinkedList<Function> loopbody = new LinkedList<>();
			Loop loop = cc.getLoop();
			log.debug("LOOP {}", loop);
			if (loop != null)
				loopbody.addAll(loop.getAtoms());
			loopbody.addAll(cc.getNonDLAtoms());
			outputRules.add(datalogFactory.getCQIE(headAtom, loopbody));
		}
		return outputRules;
	}
	
	private double time = 0;
	
	@Override
	public List<CQIE> rewrite(List<CQIE> ucq) {
		
		double startime = System.currentTimeMillis();

		List<CQIE> outputRules = new LinkedList<>();
		Multimap<Predicate, CQIE> ccDP = null;
		Multimap<Predicate, CQIE> edgeDP = ArrayListMultimap.create();

		for (CQIE cqie : ucq) {
			List<QueryConnectedComponent> ccs = QueryConnectedComponent.getConnectedComponents(reasoner, cqie,
					atomFactory, immutabilityTools);
			Function cqieAtom = cqie.getHead();
		
			if (ccs.size() == 1) {
				QueryConnectedComponent cc = ccs.iterator().next();
				log.debug("CONNECTED COMPONENT ({}) EXISTS {}", cc.getFreeVariables(), cc.getQuantifiedVariables());
				log.debug("     WITH EDGES {} AND LOOP {}", cc.getEdges(), cc.getLoop());
				log.debug("     NON-DL ATOMS {}", cc.getNonDLAtoms());
				outputRules.addAll(rewriteCC(cc, cqieAtom, edgeDP)); 				
			}
			else {
				if (ccDP == null)
					ccDP = ArrayListMultimap.create();
				String cqieURI = cqieAtom.getFunctionSymbol().getName();
				List<Function> ccBody = new ArrayList<>(ccs.size());
				for (QueryConnectedComponent cc : ccs) {
					log.debug("CONNECTED COMPONENT ({}) EXISTS {}", cc.getFreeVariables(), cc.getQuantifiedVariables());
					log.debug("     WITH EDGES {} AND LOOP {}", cc.getEdges(), cc.getLoop());
					log.debug("     NON-DL ATOMS {}", cc.getNonDLAtoms());
					Function ccAtom = getHeadAtom(cqieURI, "_CC_" + (ccDP.size() + 1), cc.getFreeVariables());
					for (CQIE cq : rewriteCC(cc, ccAtom, edgeDP))
					    ccDP.put(cq.getHead().getFunctionSymbol(), cq);
					ccBody.add(ccAtom);
				}
				outputRules.add(datalogFactory.getCQIE(cqieAtom, ccBody));
			}
		}
		
		log.debug("REWRITTEN PROGRAM\n{}CC DEFS\n{}", outputRules, ccDP);
		if (!edgeDP.isEmpty()) {
			log.debug("EDGE DEFS\n{}", edgeDP);			
			outputRules = datalogQueryServices.plugInDefinitions(outputRules, edgeDP);
			if (ccDP != null) {
			    Multimap<Predicate, CQIE> ccDP2 = ArrayListMultimap.create();
			    for (Predicate p : ccDP.keys())
			        for (CQIE cq : datalogQueryServices.plugInDefinitions(ccDP.get(p), edgeDP))
                        ccDP2.put(p, cq);
			    ccDP = ccDP2;
            }
			log.debug("INLINE EDGE PROGRAM\n{}CC DEFS\n{}", outputRules, ccDP);
		}
		if (ccDP != null) {
			outputRules = datalogQueryServices.plugInDefinitions(outputRules, ccDP);
			log.debug("INLINE CONNECTED COMPONENTS PROGRAM\n{}", outputRules);
		}
	
		// extra CQC 
		if (outputRules.size() > 1) 
			cqcUtilities.removeContainedQueries(outputRules, dataDependenciesCQC);
		
		for (CQIE cq : outputRules)
            cqcUtilities.optimizeQueryWithSigmaRules(cq.getBody(), dataDependenciesCQC.dependencies());

		double endtime = System.currentTimeMillis();
		double tm = (endtime - startime) / 1000;
		time += tm;
		log.debug(String.format("Rewriting time: %.3f s (total %.3f s)", tm, time));
		log.debug("Final rewriting:\n{}", outputRules);
		return outputRules;
	}
}
