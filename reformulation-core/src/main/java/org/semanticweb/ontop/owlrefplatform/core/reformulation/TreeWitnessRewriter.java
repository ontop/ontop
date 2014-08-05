package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.DatalogProgram;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDAQuery;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.ontology.Axiom;
import org.semanticweb.ontop.ontology.BasicClassDescription;
import org.semanticweb.ontop.ontology.OClass;
import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.ontology.PropertySomeRestriction;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.CQCUtilities;
import org.semanticweb.ontop.owlrefplatform.core.reformulation.QueryConnectedComponent.Edge;
import org.semanticweb.ontop.owlrefplatform.core.reformulation.QueryConnectedComponent.Loop;
import org.semanticweb.ontop.owlrefplatform.core.reformulation.TreeWitnessSet.CompatibleTreeWitnessSetIterator;
import org.semanticweb.ontop.owlrefplatform.core.tboxprocessing.TBoxReasonerToOntology;
import org.semanticweb.ontop.utils.QueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 */

public class TreeWitnessRewriter implements QueryRewriter {
	private static final long serialVersionUID = 1L;

	private static OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	private static final Logger log = LoggerFactory.getLogger(TreeWitnessRewriter.class);

	private TreeWitnessReasonerLite reasoner;
	private ExtDatalogProgram extDP;
	
	private Ontology sigma = null;

	public TreeWitnessRewriter() {
		reasoner = new TreeWitnessReasonerLite();
		extDP = new ExtDatalogProgram(reasoner);
	}
	
	@Override
	public void setTBox(TBoxReasoner reasoner, Ontology sigma) {
		double startime = System.currentTimeMillis();

		Ontology ontology = TBoxReasonerToOntology.getOntology(reasoner);
		this.reasoner.setTBox(ontology);
		
		double endtime = System.currentTimeMillis();
		double tm = (endtime - startime) / 1000;
		time += tm;
		log.debug(String.format("setTBox time: %.3f s (total %.3f s)", tm, time));
		
		log.debug("SET SIGMA");
		for (Axiom ax : sigma.getAssertions()) {
			log.debug("SIGMA: " + ax);
		}
		this.sigma = sigma;
		extDP.setSigma(sigma);
		
	}
	
	
	public static String getIRI(String base, String suffix) {
		return base + suffix;
	}

	
	/*
	 * returns an atom with given arguments and the predicate name formed by the given URI basis and string fragment
	 */
	
	private static Function getHeadAtom(String base, String suffix, List<Term> arguments) {
		Predicate predicate = fac.getPredicate(getIRI(base, suffix), arguments.size(), null);
		return fac.getFunction(predicate, arguments);
	}
	
	/*
	 * returns atoms E of a given collection of tree witness generators; 
	 * the `free' variable of the generators is replaced by the term r0;
	 */

	private List<Function> getAtomsForGenerators(Collection<TreeWitnessGenerator> gens, Term r0)  {
		Collection<BasicClassDescription> concepts = TreeWitnessGenerator.getMaximalBasicConcepts(gens, reasoner);		
		List<Function> genAtoms = new ArrayList<Function>(concepts.size());
		Term x = fac.getVariableNondistinguished(); 
		
		for (BasicClassDescription con : concepts) {
			log.debug("  BASIC CONCEPT: {}", con);
			Function atom; 
			if (con instanceof OClass) {
				atom = fac.getFunction(((OClass)con).getPredicate(), r0);
			}
			else {
				PropertySomeRestriction some = (PropertySomeRestriction)con;
				atom = (!some.isInverse()) ?  fac.getFunction(some.getPredicate(), r0, x) : fac.getFunction(some.getPredicate(), x, r0);  						 
			}
			genAtoms.add(atom);
		}
		return genAtoms;
	}
	
	
	
	/*
	 * rewrites a given connected CQ with the rules put into output
	 */
	
	private void rewriteCC(QueryConnectedComponent cc, Function headAtom, DatalogProgram output, ExtPredicateCache cache, DatalogProgram edgeDP) {
		String headURI = headAtom.getFunctionSymbol().getName();
		
		TreeWitnessSet tws = TreeWitnessSet.getTreeWitnesses(cc, reasoner);

		if (cc.hasNoFreeTerms()) {  
			for (Function a : getAtomsForGenerators(tws.getGeneratorsOfDetachedCC(), fac.getVariableNondistinguished())) {
				output.appendRule(fac.getCQIE(headAtom, cache.getExtAtom(a))); 
			}
		}

		// COMPUTE AND STORE TREE WITNESS FORMULAS
		for (TreeWitness tw : tws.getTWs()) {
			log.debug("TREE WITNESS: {}", tw);		
			MinimalCQProducer twf = new MinimalCQProducer(reasoner); 
			
			// equality atoms
			Iterator<Term> i = tw.getRoots().iterator();
			Term r0 = i.next();
			while (i.hasNext()) 
				twf.addNoCheck(fac.getFunctionEQ(i.next(), r0));
			
			// root atoms
			for (Function a : tw.getRootAtoms()) {
				Predicate predicate = a.getFunctionSymbol();
				twf.add((predicate.getArity() == 1) ? fac.getFunction(predicate, r0) : fac.getFunction(predicate, r0, r0));
			}
			
			List<Function> genAtoms = getAtomsForGenerators(tw.getGenerators(), r0);			
			boolean subsumes = false;
			for (Function a : genAtoms) 				
				if (twf.subsumes(a)) {
					subsumes = true;
					log.debug("TWF {} SUBSUMES {}", twf.getAtoms(), a);
					break;
				}

			List<List<Function>> twfs = new ArrayList<List<Function>>(subsumes ? 1 : genAtoms.size());			
			if (!subsumes) {
				for (Function a : genAtoms) {				
					MinimalCQProducer twfa = new MinimalCQProducer(twf);
					twfa.add(a); // 
					twfs.add(cache.getExtAtoms(twfa));
				}
			}
			else
				twfs.add(cache.getExtAtoms(twf));
			
			tw.setFormula(twfs);
		}
				
		if (!cc.isDegenerate()) {			
			if (tws.hasConflicts()) { 
				// there are conflicting tree witnesses
				// use compact exponential rewriting by enumerating all compatible subsets of tree witnesses
				CompatibleTreeWitnessSetIterator iterator = tws.getIterator();
				while (iterator.hasNext()) {
					Collection<TreeWitness> compatibleTWs = iterator.next();
					log.debug("COMPATIBLE: {}", compatibleTWs);
					MinimalCQProducer mainbody = new MinimalCQProducer(reasoner); 
					
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
						Function twAtom = getHeadAtom(headURI, "_TW_" + (edgeDP.getRules().size() + 1), cc.getVariables());
						mainbody.addNoCheck(twAtom);				
						for (List<Function> twfa : tw.getFormula())
							edgeDP.appendRule(fac.getCQIE(twAtom, twfa));
					}	
					mainbody.addAllNoCheck(cc.getNonDLAtoms());					
					output.appendRule(fac.getCQIE(headAtom, cache.getExtAtoms(mainbody))); 
				}
			}
			else {
				// no conflicting tree witnesses
				// use polynomial tree witness rewriting by treating each edge independently 
				MinimalCQProducer mainbody = new MinimalCQProducer(reasoner); 		
				for (Edge edge : cc.getEdges()) {
					log.debug("EDGE {}", edge);
					
					Function edgeAtom = null;
					for (TreeWitness tw : tws.getTWs())
						if (tw.getDomain().contains(edge.getTerm0()) && tw.getDomain().contains(edge.getTerm1())) {
							if (edgeAtom == null) {
								//IRI atomURI = edge.getBAtoms().iterator().next().getPredicate().getName();
								edgeAtom = getHeadAtom(headURI, 
										"_EDGE_" + (edgeDP.getRules().size() + 1) /*+ "_" + atomURI.getRawFragment()*/, cc.getVariables());
								mainbody.addNoCheck(edgeAtom);				
								
								MinimalCQProducer edgeAtoms = new MinimalCQProducer(reasoner); 
								edgeAtoms.addAll(edge.getAtoms());
								edgeDP.appendRule(fac.getCQIE(edgeAtom, cache.getExtAtoms(edgeAtoms)));													
							}
							
							for (List<Function> twfa : tw.getFormula())
								edgeDP.appendRule(fac.getCQIE(edgeAtom, twfa));
						}
					
					if (edgeAtom == null) // no tree witnesses -- direct insertion into the main body
						mainbody.addAll(edge.getAtoms());
				}
				mainbody.addAllNoCheck(cc.getNonDLAtoms());
				output.appendRule(fac.getCQIE(headAtom, cache.getExtAtoms(mainbody))); 
			}
		}
		else {
			// degenerate connected component
			MinimalCQProducer loopbody = new MinimalCQProducer(reasoner);
			Loop loop = cc.getLoop();
			log.debug("LOOP {}", loop);
			if (loop != null)
				loopbody.addAll(loop.getAtoms());
			loopbody.addAllNoCheck(cc.getNonDLAtoms());
			output.appendRule(fac.getCQIE(headAtom, cache.getExtAtoms(loopbody))); 
		}
	}
	
	private double time = 0;
	
	@Override
	public OBDAQuery rewrite(OBDAQuery input) {
		
		double startime = System.currentTimeMillis();
		
		DatalogProgram dp = (DatalogProgram) input;
		DatalogProgram output = fac.getDatalogProgram();
		DatalogProgram ccDP = null;
		DatalogProgram edgeDP = fac.getDatalogProgram();

		ExtPredicateCache cache = new ExtPredicateCache(extDP);
		
		for (CQIE cqie : dp.getRules()) {
			List<QueryConnectedComponent> ccs = QueryConnectedComponent.getConnectedComponents(cqie);	
			Function cqieAtom = cqie.getHead();
		
			if (ccs.size() == 1) {
				QueryConnectedComponent cc = ccs.iterator().next();
				log.debug("CONNECTED COMPONENT ({})" + " EXISTS {}", cc.getFreeVariables(), cc.getQuantifiedVariables());
				log.debug("     WITH EDGES {} AND LOOP {}", cc.getEdges(), cc.getLoop());
				log.debug("     NON-DL ATOMS {}", cc.getNonDLAtoms());
				rewriteCC(cc, cqieAtom, output, cache, edgeDP); 				
			}
			else {
				if (ccDP == null)
					ccDP = fac.getDatalogProgram();
				String cqieURI = cqieAtom.getFunctionSymbol().getName();
				List<Function> ccBody = new ArrayList<Function>(ccs.size());
				for (QueryConnectedComponent cc : ccs) {
					log.debug("CONNECTED COMPONENT ({})" + " EXISTS {}", cc.getFreeVariables(), cc.getQuantifiedVariables());
					log.debug("     WITH EDGES {} AND LOOP {}", cc.getEdges(), cc.getLoop());
					log.debug("     NON-DL ATOMS {}", cc.getNonDLAtoms());
					Function ccAtom = getHeadAtom(cqieURI, "_CC_" + (ccDP.getRules().size() + 1), cc.getFreeVariables());
					rewriteCC(cc, ccAtom, ccDP, cache, edgeDP); 
					ccBody.add(ccAtom);
				}
				output.appendRule(fac.getCQIE(cqieAtom, ccBody));
			}
		}
		
		log.debug("REWRITTEN PROGRAM\n{}CC DEFS\n{}", output, ccDP);
		if (!edgeDP.getRules().isEmpty()) {
			log.debug("EDGE DEFS\n{}", edgeDP);			
			output = DatalogQueryServices.plugInDefinitions(output, edgeDP);
			if (ccDP != null)
				ccDP = DatalogQueryServices.plugInDefinitions(ccDP, edgeDP);
			log.debug("INLINE EDGE PROGRAM\n{}CC DEFS\n{}", output, ccDP);
		}
		if (ccDP != null) {
			output = DatalogQueryServices.plugInDefinitions(output, ccDP);
			log.debug("INLINE CONNECTED COMPONENTS PROGRAM\n{}", output);
		}
		DatalogProgram extDP = cache.getExtDP();
		if (extDP != null) {
			output = DatalogQueryServices.plugInDefinitions(output, extDP);
			log.debug("INLINE EXT PROGRAM\n{}", output);
		}
	
		// extra CQC 
		if (output.getRules().size() > 1)
			output = fac.getDatalogProgram(CQCUtilities.removeContainedQueries(output.getRules(), true, sigma));
		
		QueryUtils.copyQueryModifiers(input, output);

		double endtime = System.currentTimeMillis();
		double tm = (endtime - startime) / 1000;
		time += tm;
		log.debug(String.format("Rewriting time: %.3f s (total %.3f s)", tm, time));
		log.debug("Final rewriting:\n{}", output);
		return output;
	}
	

	/**
	 * cache for the Ext_E predicates and Datalog programs they are defined by
	 * 
	 * @author Roman Kontchakov
	 *
	 */
	private static class ExtPredicateCache {
		//private Set<Predicate> usedExts = new HashSet<Predicate>();
		private boolean usedExts = false;
		final private ExtDatalogProgram extDP;
		
		public ExtPredicateCache(ExtDatalogProgram extDP) {
			this.extDP = extDP;
		}

		public List<Function> getExtAtoms(MinimalCQProducer atoms) {
			List<Function> extAtoms = new ArrayList<Function>(atoms.getAtoms().size() + atoms.getNoCheckAtoms().size());
			extAtoms.addAll(atoms.getNoCheckAtoms());

			for (Function a : atoms.getAtoms()) 
				extAtoms.add(getExtAtom(a));

			return extAtoms;
		}

				
		public Function getExtAtom(Function a) {
			Predicate ext = extDP.getEntryForPredicate(a.getFunctionSymbol());
			
			if (ext != null) {
				usedExts = true;        // usedExts.add(ext);
				if (a.getArity() == 1) 
					return fac.getFunction(ext, a.getTerm(0));
				else {
					assert (a.getArity() == 2);
					return fac.getFunction(ext, a.getTerm(0), a.getTerm(1));
				}
			}
			return a;
		}
		
		/**
		 * @return Datalog program containing definitions for all Ext_E in usedExts
		 */

		public DatalogProgram getExtDP() {
			if (!usedExts) 
				return null;
			
			//DatalogProgram dp = fac.getDatalogProgram();		
			//for (Predicate pred : usedExts) { 
			//	List<CQIE> extDef = extDP.getDP(pred);			 
			//	dp.appendRule(extDef);		 
			//}
			return extDP.getFullDP(); 
		}
	}
	
}
