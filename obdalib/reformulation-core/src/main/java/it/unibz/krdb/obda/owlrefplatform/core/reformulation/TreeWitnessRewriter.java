package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQCUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryConnectedComponent.Edge;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryConnectedComponent.Loop;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.TreeWitnessSet.CompatibleTreeWitnessSetIterator;
import it.unibz.krdb.obda.utils.QueryUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */

public class TreeWitnessRewriter implements QueryRewriter {
	private static final long serialVersionUID = 1L;

	private static OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	private static final Logger log = LoggerFactory.getLogger(TreeWitnessRewriter.class);

	private TreeWitnessReasonerLite reasoner = new TreeWitnessReasonerLite();
	private ExtPredicateCache cache = new ExtPredicateCache();
	
	private Ontology sigma = null;
	
	@Override
	public void setTBox(Ontology ontology) {
		double startime = System.currentTimeMillis();

		cache.clear();
		reasoner.setTBox(ontology);
		
		double endtime = System.currentTimeMillis();
		double tm = (endtime - startime) / 1000;
		time += tm;
		log.debug(String.format("setTBox time: %.3f s (total %.3f s)", tm, time));
	}
	
	@Override
	public void setCBox(Ontology sigma) {
		log.debug("SET SIGMA");
		this.sigma = sigma;
		for (Axiom ax : sigma.getAssertions()) {
			log.debug("SIGMA: " + ax);
		}
		cache.clear();
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
	}

	/*
	 * adds the Ext_E atoms for all atoms E in a given collection of atoms; saves used Ext_E predicates in usedExts
	 */
	
	private  void addExtAtoms(Collection<Atom> extAtoms, Collection<Atom> atoms, Set<Predicate> usedExts) {
		for (Atom a : atoms) {
			Predicate ext = cache.getCheckedExtPredicate(a.getPredicate(), usedExts);
			extAtoms.add((ext == null) ? a: fac.getAtom(ext, a.getTerms()));
		}
	}

	/*
	 * returns the Ext_E atoms for all atoms E in a given edge; saves used Ext_E predicates in usedExts
	 */
		
	private List<Atom> getExtAtoms(Edge edge, Set<Predicate> usedExts) {
		List<Atom> extAtoms = new ArrayList<Atom>(edge.size());
		addExtAtoms(extAtoms, edge.getBAtoms(), usedExts);
		addExtAtoms(extAtoms, edge.getAtoms0(), usedExts);
		addExtAtoms(extAtoms, edge.getAtoms1(), usedExts);	
		return extAtoms;
	}
	
	/*
	 * returns the Ext_E atoms for all atoms E of a given collection of tree witness generators; 
	 * the `free' variable of the generators is replaced by the term r0;
	 * saves used Ext_E predicates in usedExts
	 */

	private List<Atom> getExtAtomsForGenerators(Collection<TreeWitnessGenerator> gens, Term r0, Set<Predicate> usedExts)  {
		Collection<BasicClassDescription> concepts = TreeWitnessGenerator.getMaximalBasicConcepts(gens);		
		List<Atom> extAtoms = new ArrayList<Atom>(concepts.size());
		Term x = fac.getNondistinguishedVariable(); 
		
		for (BasicClassDescription con : concepts) {
			log.debug("  BASIC CONCEPT: " + con);
			if (con instanceof OClass) {
				Predicate ext = cache.getExtPredicate(((OClass)con).getPredicate(), usedExts);
				extAtoms.add(fac.getAtom(ext, r0));
			}
			else {
				PropertySomeRestriction some = (PropertySomeRestriction)con;
				Predicate ext = cache.getExtPredicate(some.getPredicate(), usedExts);
				extAtoms.add((!some.isInverse()) ?  fac.getAtom(ext, r0, x) : fac.getAtom(ext, x, r0));  						 
			}
		}
		return extAtoms;
	}
	
	/*
	 * returns an atom with given arguments and the predicate name formed by the given URI basis and string fragment
	 */
	
	private static Atom getHeadAtom(URI basis, String fragment, List<Term> arguments) {
		URI uri = null;
		try {
			uri = new URI(basis.getScheme(), basis.getSchemeSpecificPart(), fragment);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fac.getAtom(fac.getPredicate(uri, arguments.size()), arguments);
	}
	
	/*
	 * rewrites a given connected CQ with the rules put into output
	 */
	
	private void rewriteCC(QueryConnectedComponent cc, Atom headAtom, DatalogProgram output, Set<Predicate> usedExts, DatalogProgram edgeDP) {
		TreeWitnessSet tws = TreeWitnessSet.getTreeWitnesses(cc, reasoner);

		if (cc.hasNoFreeTerms()) {  
			for (Atom a : getExtAtomsForGenerators(tws.getGeneratorsOfDetachedCC(), fac.getNondistinguishedVariable(), usedExts))
				output.appendRule(fac.getCQIE(headAtom, a)); 
		}

		// COMPUTE AND STORE TREE WITNESS FORMULAS
		for (TreeWitness tw : tws.getTWs()) {
			log.debug("TREE WITNESS: " + tw);		
			MinimalCQProducer twf = new MinimalCQProducer(reasoner, cc.getVariables()); 
			
			// equality atoms
			Iterator<Term> i = tw.getRoots().iterator();
			Term r0 = i.next();
			while (i.hasNext()) 
				twf.add(fac.getEQAtom(i.next(), r0));
			
			// root atoms
			for (Atom a : tw.getRootAtoms()) {
				Predicate ext = cache.getExtPredicate(a.getPredicate(), usedExts);
				twf.add((a.getArity() == 1) ? fac.getAtom(ext, r0) : fac.getAtom(ext, r0, r0));
			}
			
			List<Atom> genAtoms = getExtAtomsForGenerators(tw.getGenerators(), r0, usedExts);			
			boolean subsumes = false;
			for (Atom a : genAtoms) 				
				if (twf.wouldSubsume(a)) {
					subsumes = true;
					log.debug("TWF " + twf.getBody() + " SUBSUMES " + a);
					break;
				}

			List<List<Atom>> twfs = new ArrayList<List<Atom>>(subsumes ? 1 : genAtoms.size());			
			if (!subsumes) {
				List<Atom> twfbody = twf.getBody();		
				for (Atom a : genAtoms) {				
					List<Atom> twfa = new ArrayList<Atom>(twfbody.size() + 1); 
					twfa.add(a); // 
					twfa.addAll(twfbody);
					twfs.add(twfa);
				}
			}
			else
				twfs.add(twf.getBody());
			
			tw.setFormula(twfs);
		}
				
		if (!cc.isDegenerate()) {			
			if (tws.hasConflicts()) { 
				// there are conflicting tree witnesses
				// use compact exponential rewriting by enumerating all compatible subsets of tree witnesses
				URI atomURI = headAtom.getPredicate().getName();
				CompatibleTreeWitnessSetIterator iterator = tws.getIterator();
				while (iterator.hasNext()) {
					Collection<TreeWitness> compatibleTWs = iterator.next();
					log.debug("COMPATIBLE: " + compatibleTWs);
					MinimalCQProducer mainbody = new MinimalCQProducer(reasoner, cc.getFreeVariables()); 
					
					for (Edge edge : cc.getEdges()) {
						boolean contained = false;
						for (TreeWitness tw : compatibleTWs)
							if (tw.getDomain().contains(edge.getTerm0()) && tw.getDomain().contains(edge.getTerm1())) {
								contained = true;
								log.debug("EDGE " + edge + " COVERED BY " + tw);
								break;
							}
						if (!contained) {
							log.debug("EDGE " + edge + " NOT COVERED BY ANY TW");
							for (Atom a : getExtAtoms(edge, usedExts))
								mainbody.add(a);
						}
					}
					for (TreeWitness tw : compatibleTWs) {
						Atom twAtom = getHeadAtom(atomURI, "TW_" + (edgeDP.getRules().size() + 1), cc.getVariables());
						mainbody.addNoCheck(twAtom);				
						for (List<Atom> twfa : tw.getFormula())
							edgeDP.appendRule(fac.getCQIE(twAtom, twfa));
					}	
					output.appendRule(fac.getCQIE(headAtom, mainbody.getBody())); 
				}
			}
			else {
				// no conflicting tree witnesses
				// use polynomial tree witness rewriting by treating each edge independently 
				MinimalCQProducer mainbody = new MinimalCQProducer(reasoner, cc.getFreeVariables()); 		
				for (Edge edge : cc.getEdges()) {
					log.debug("EDGE " + edge);
					List<Atom> extAtoms = getExtAtoms(edge, usedExts);
					
					Atom edgeAtom = null;
					for (TreeWitness tw : tws.getTWs())
						if (tw.getDomain().contains(edge.getTerm0()) && tw.getDomain().contains(edge.getTerm1())) {
							if (edgeAtom == null) {
								URI atomURI = edge.getBAtoms().iterator().next().getPredicate().getName();
								edgeAtom = getHeadAtom(atomURI, 
										"E_" + (edgeDP.getRules().size() + 1) + "_" + atomURI.getFragment(), cc.getVariables());
								mainbody.addNoCheck(edgeAtom);				
								edgeDP.appendRule(fac.getCQIE(edgeAtom, extAtoms));													
							}
							
							for (List<Atom> twfa : tw.getFormula())
								edgeDP.appendRule(fac.getCQIE(edgeAtom, twfa));
						}
					
					if (edgeAtom == null) // no tree witnesses -- direct insertion into the main body
						for (Atom a : extAtoms)
							mainbody.add(a);
				}
				output.appendRule(fac.getCQIE(headAtom, mainbody.getBody())); 
			}
		}
		else {
			// degenerate connected component -- a single loop
			Loop loop = cc.getLoop();
			log.debug("LOOP " + loop);
			List<Atom> extAtoms = new ArrayList<Atom>(loop.getAtoms().size());			
			addExtAtoms(extAtoms, loop.getAtoms(), usedExts);
			output.appendRule(fac.getCQIE(headAtom, extAtoms)); 
		}
	}
	
	private double time = 0;
	
	@Override
	public OBDAQuery rewrite(OBDAQuery input) {
		
		double startime = System.currentTimeMillis();
		
		DatalogProgram dp = (DatalogProgram) input;
		DatalogProgram output = fac.getDatalogProgram();
		DatalogProgram ccDP = fac.getDatalogProgram();
		DatalogProgram edgeDP = fac.getDatalogProgram();

		Set<Predicate> exts = new HashSet<Predicate>();
		
		for (CQIE cqie : dp.getRules()) {
			List<QueryConnectedComponent> ccs = QueryConnectedComponent.getConnectedComponents(cqie);	
			Atom cqieAtom = cqie.getHead();
		
			if (ccs.size() == 1) {
				QueryConnectedComponent cc = ccs.iterator().next();
				log.debug("CONNECTED COMPONENT (" + cc.getFreeVariables() + ")" + " EXISTS " + cc.getQuantifiedVariables() + " WITH EDGES " + cc.getEdges() + " AND LOOP " + cc.getLoop());
				rewriteCC(cc, cqieAtom, output, exts, edgeDP); 				
			}
			else {
				URI cqieURI = cqieAtom.getPredicate().getName();
				List<Atom> ccBody = new ArrayList<Atom>(ccs.size());
				for (QueryConnectedComponent cc : ccs) {
					log.debug("CONNECTED COMPONENT (" + cc.getFreeVariables() + ")" + " EXISTS " + cc.getQuantifiedVariables() + " WITH EDGES " + cc.getEdges());
					Atom ccAtom = getHeadAtom(cqieURI, 
							cqieURI.getFragment() + "_CC_" + (ccDP.getRules().size() + 1), cc.getFreeVariables());
					rewriteCC(cc, ccAtom, ccDP, exts, edgeDP); 
					ccBody.add(ccAtom);
				}
				output.appendRule(fac.getCQIE(cqieAtom, ccBody));
			}
		}
		
		DatalogProgram extDP = cache.getExtDP(exts);
			
		log.debug("REWRITTEN PROGRAM\n" + output + "CC DEFS\n"+ ccDP + "EDGE DEFS\n" + edgeDP + "EXT DEFS\n" + extDP);			
		if (!edgeDP.getRules().isEmpty()) {
			output = DatalogQueryServices.plugInDefinitions(output, edgeDP);
			if (!ccDP.getRules().isEmpty())
				ccDP = DatalogQueryServices.plugInDefinitions(ccDP, edgeDP);
			log.debug("INLINE EDGE PROGRAM\n" + output + "CC DEFS\n" + ccDP);
		}
		if (!ccDP.getRules().isEmpty()) {
			output = DatalogQueryServices.plugInDefinitions(output, ccDP);
			log.debug("INLINE CONNECTED COMPONENTS PROGRAM\n" + output);
		}
		if (extDP != null) {
			output = DatalogQueryServices.plugInDefinitions(output, extDP);
			log.debug("INLINE EXT PROGRAM\n" + output);
		}
		QueryUtils.copyQueryModifiers(input, output);

		double endtime = System.currentTimeMillis();
		double tm = (endtime - startime) / 1000;
		time += tm;
		log.debug(String.format("Rewriting time: %.3f s (total %.3f s)", tm, time));
		
		return output;
	}
	

	/**
	 * Ext predicates cache entry
	 * 		ext: the Ext_E predicate for a query predicate E
	 * 		dp:  list of CQs to represent the Datalog program for this ext
	 * note that both dp and ext can be null (at the same time) meaning that the definition of Ext_E is trivial
	 *  
	 * @author Roman Kontchakov
	 *
	 */
	
	private static class ExtPredicateCacheEntry {
		private final Predicate ext;
		private final List<CQIE> dp;
		
		public ExtPredicateCacheEntry(Predicate ext, List<CQIE> dp) {
			this.ext = ext;
			this.dp = dp;
		}
	}
	
	/**
	 * cache for the Ext_E predicates and Datalog programs they are defined by
	 * 
	 * @author Roman Kontchakov
	 *
	 */
	
	private class ExtPredicateCache {
		private Map<Predicate, ExtPredicateCacheEntry> extPredicateMap = new HashMap<Predicate, ExtPredicateCacheEntry>();
		
		/**
		 * clears the cache (called when a new CBox is set)
		 */
		
		public void clear() {
			extPredicateMap.clear();
		}
		
		/**
		 * 
		 * @param p: a query predicate E
		 * @param usedExts: a collection of Ext_E predicates that have been used in the rewriting so far
		 * @return the Ext_E predicate or null if Ext_E is trivially defined (i.e., with a single rule Ext_E :- E)
		 */
		
		public Predicate getCheckedExtPredicate(Predicate p, Set<Predicate> usedExts)  {
			ExtPredicateCacheEntry e = getEntryFor(p);
			if (e.ext != null)
				usedExts.add(p);
			return e.ext;
		}
		
		/**
		 * 
		 * @param p: a query predicate E
		 * @param usedExts: a collection of Ext_E predicates that have been used in the rewriting so far
		 * @return the Ext_E predicate or E if Ext_E is trivially defined (i.e., with a single rule Ext_E :- E)
		 */
		
		public Predicate getExtPredicate(Predicate p, Set<Predicate> usedExts)  {
			ExtPredicateCacheEntry e = getEntryFor(p);
			if (e.ext != null) {
				usedExts.add(p);
				return e.ext;
			}
			else
				return p;
		}
		
		/**
		 * 
		 * @param usedExts: a collection of Ext_E predicates that have been used in the rewriting so far
		 * @return Datalog program containing definitions for all Ext_E in usedExts
		 */
		
		public DatalogProgram getExtDP(Set<Predicate> usedExts) {
			if (usedExts.isEmpty())
				return null;
			
			DatalogProgram extDP = fac.getDatalogProgram();		
			for (Predicate pred : usedExts) { 
				List<CQIE> extDef = extPredicateMap.get(pred).dp;			 
				extDP.appendRule(extDef); // NEED TO CLONE?				 
			}
			return extDP;
		}
		
		private final Term x = fac.getVariable("x");			
		private final Term y = fac.getVariable("y");
		private final Term w = fac.getNondistinguishedVariable(); 
		
		private ExtPredicateCacheEntry getEntryFor(Predicate p) {
			ExtPredicateCacheEntry entry = extPredicateMap.get(p);
			if (entry != null) 
				return entry;
				
			URI extURI = null;
			try {
				URI pURI = p.getName();
				extURI = new URI(pURI.getScheme(), pURI.getSchemeSpecificPart(), "EXT_" + pURI.getFragment());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ExtDatalogProgramDef dp = null;
			if (p.getArity() == 1) {
				Collection<BasicClassDescription> subc = reasoner.getSubConcepts(p);
				if (subc.size() > 1) {
					dp = new ExtDatalogProgramDef(fac.getAtom(fac.getClassPredicate(extURI), x), fac.getAtom(p, x));
					for (BasicClassDescription c : subc) 
						if (c instanceof OClass) 
							dp.add(fac.getAtom(((OClass)c).getPredicate(), x));
						else {     //if (c instanceof PropertySomeRestriction) {
							PropertySomeRestriction some = (PropertySomeRestriction)c;
							dp.add((!some.isInverse()) ? 
									fac.getAtom(some.getPredicate(), x, w) : fac.getAtom(some.getPredicate(), w, x)); 
						}		
				}
			}
			else  {
				Collection<Property> subp = reasoner.getSubProperties(p, false); 
				if (subp.size() > 1) {
					dp = new ExtDatalogProgramDef(fac.getAtom(fac.getObjectPropertyPredicate(extURI), x, y), 
												fac.getAtom(p, x, y));
					for (Property sub: subp)
						dp.add((!sub.isInverse()) ? 
							fac.getAtom(sub.getPredicate(), x, y) : fac.getAtom(sub.getPredicate(), y, x)); 
				}
			}
			if (dp == null) 
				entry = new ExtPredicateCacheEntry(null, null);
			else {
				log.debug("DP FOR " + p + " IS " + dp.dp);
				dp.minimise();			
				entry = (dp.dp.size() <= 1)  
						? new ExtPredicateCacheEntry(null, null) : new ExtPredicateCacheEntry(dp.extAtom.getPredicate(), dp.dp);
			}
			extPredicateMap.put(p, entry);
			return entry;
		}
	
	}
	
	/**
	 * class for Datalog program definitions of the Ext_E
	 * implements simplified CQ containment checks
	 * 
	 * @author Roman Kontchakov
	 *
	 */
	
	private class ExtDatalogProgramDef {
		private final Atom extAtom;
		private final Predicate mainPredicate;
		private final CQIE mainQuery;
		private List<CQIE> dp = new LinkedList<CQIE>();
		
		public ExtDatalogProgramDef(Atom extAtom, Atom mainAtom) {
			this.extAtom = extAtom;
			this.mainPredicate = mainAtom.getPredicate();
			this.mainQuery = fac.getCQIE(extAtom, mainAtom);
		}
		
		public void add(Atom body) {
			if (body.getPredicate().equals(mainPredicate))
				return;
			
			CQIE query = fac.getCQIE(extAtom, body);
			CQCUtilities cqc = new CQCUtilities(query, sigma);
			if (!cqc.isContainedIn(mainQuery)) 
				dp.add(query);
			else
				log.debug("    CQC CONTAINMENT: " + query +  " IN " + mainQuery);
		}
		
		public void minimise() {
			if (dp.size() > 0) {
				dp.add(mainQuery);
				dp = CQCUtilities.removeContainedQueries(dp, true, sigma, false);
				log.debug("SIMPLIFIED DP FOR " + extAtom + " IS " + dp);
			}
		}
	}
	
}
