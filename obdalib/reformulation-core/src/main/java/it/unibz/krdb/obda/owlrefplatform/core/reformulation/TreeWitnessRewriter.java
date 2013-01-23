package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.impl.AnonymousVariable;
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

import com.hp.hpl.jena.iri.IRI;

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
	
	private Atom getExtAtom(Atom a, Set<Predicate> usedExts) {
		if (a.getArity() == 1) {
			Predicate ext = cache.getExtPredicateClass(a.getPredicate(), usedExts);
			return fac.getAtom(ext, a.getTerm(0));
		}
		else {
			NewLiteral t0 = a.getTerm(0);
			NewLiteral t1 = a.getTerm(1);
			if (t0 instanceof AnonymousVariable) {
				Predicate ext = cache.getExtPredicatePropertySome(a.getPredicate(), true, usedExts);
				return fac.getAtom(ext, t1);					
			} 
			else if (t1 instanceof AnonymousVariable) {
				Predicate ext = cache.getExtPredicatePropertySome(a.getPredicate(), false, usedExts);
				return fac.getAtom(ext, t0);					
			} 
			else {
				Predicate ext = cache.getExtPredicateProperty(a.getPredicate(), usedExts);
				return fac.getAtom(ext, t0, t1);					
			}
		}
	}
	
	private List<Atom> getExtAtoms(MinimalCQProducer atoms, Set<Predicate> usedExts) {
		List<Atom> extAtoms = new ArrayList<Atom>(atoms.getAtoms().size() + atoms.getNoCheckAtoms().size());
		extAtoms.addAll(atoms.getNoCheckAtoms());

		for (Atom a : atoms.getAtoms()) 
			extAtoms.add(getExtAtom(a, usedExts));

		return extAtoms;
	}
	
	/*
	 * returns the Ext_E atoms for all atoms E of a given collection of tree witness generators; 
	 * the `free' variable of the generators is replaced by the NewLiteral r0;
	 * saves used Ext_E predicates in usedExts
	 */

	private List<Atom> getAtomsForGenerators(Collection<TreeWitnessGenerator> gens, NewLiteral r0)  {
		Collection<BasicClassDescription> concepts = TreeWitnessGenerator.getMaximalBasicConcepts(gens, reasoner);		
		List<Atom> extAtoms = new ArrayList<Atom>(concepts.size());
		NewLiteral x = fac.getNondistinguishedVariable(); 
		
		for (BasicClassDescription con : concepts) {
			log.debug("  BASIC CONCEPT: " + con);
			if (con instanceof OClass) {
				extAtoms.add(fac.getAtom(((OClass)con).getPredicate(), r0));
			}
			else {
				PropertySomeRestriction some = (PropertySomeRestriction)con;
				extAtoms.add((!some.isInverse()) ?  fac.getAtom(some.getPredicate(), r0, x) : fac.getAtom(some.getPredicate(), x, r0));  						 
			}
		}
		return extAtoms;
	}
	
	/*
	 * returns an atom with given arguments and the predicate name formed by the given URI basis and string fragment
	 */
	
	private static Atom getHeadAtom(IRI headURI, String fragment, List<NewLiteral> arguments) {
		IRI uri = null;
		uri = OBDADataFactoryImpl.getIRI(headURI.toString() + fragment);
		return fac.getAtom(fac.getPredicate(uri, arguments.size()), arguments);
	}
	
	/*
	 * rewrites a given connected CQ with the rules put into output
	 */
	
	private void rewriteCC(QueryConnectedComponent cc, Atom headAtom, DatalogProgram output, Set<Predicate> usedExts, DatalogProgram edgeDP) {
		IRI headURI = headAtom.getPredicate().getName();
		
		TreeWitnessSet tws = TreeWitnessSet.getTreeWitnesses(cc, reasoner);

		if (cc.hasNoFreeTerms()) {  
			for (Atom a : getAtomsForGenerators(tws.getGeneratorsOfDetachedCC(), fac.getNondistinguishedVariable())) {
				output.appendRule(fac.getCQIE(headAtom, getExtAtom(a, usedExts))); 
			}
		}

		// COMPUTE AND STORE TREE WITNESS FORMULAS
		for (TreeWitness tw : tws.getTWs()) {
			log.debug("TREE WITNESS: " + tw);		
			MinimalCQProducer twf = new MinimalCQProducer(reasoner); 
			
			// equality atoms
			Iterator<NewLiteral> i = tw.getRoots().iterator();
			NewLiteral r0 = i.next();
			while (i.hasNext()) 
				twf.addNoCheck(fac.getEQAtom(i.next(), r0));
			
			// root atoms
			for (Atom a : tw.getRootAtoms()) 
				twf.add((a.getArity() == 1) ? fac.getAtom(a.getPredicate(), r0) : fac.getAtom(a.getPredicate(), r0, r0));
			
			List<Atom> genAtoms = getAtomsForGenerators(tw.getGenerators(), r0);			
			boolean subsumes = false;
			for (Atom a : genAtoms) 				
				if (twf.wouldSubsume(a)) {
					subsumes = true;
					log.debug("TWF " + twf.getAtoms() + " SUBSUMES " + a);
					break;
				}

			List<List<Atom>> twfs = new ArrayList<List<Atom>>(subsumes ? 1 : genAtoms.size());			
			if (!subsumes) {
				for (Atom a : genAtoms) {				
					MinimalCQProducer twfa = new MinimalCQProducer(reasoner, twf);
					twfa.add(a); // 
					twfs.add(getExtAtoms(twfa, usedExts));
				}
			}
			else
				twfs.add(getExtAtoms(twf, usedExts));
			
			tw.setFormula(twfs);
		}
				
		if (!cc.isDegenerate()) {			
			if (tws.hasConflicts()) { 
				// there are conflicting tree witnesses
				// use compact exponential rewriting by enumerating all compatible subsets of tree witnesses
				CompatibleTreeWitnessSetIterator iterator = tws.getIterator();
				while (iterator.hasNext()) {
					Collection<TreeWitness> compatibleTWs = iterator.next();
					log.debug("COMPATIBLE: " + compatibleTWs);
					MinimalCQProducer mainbody = new MinimalCQProducer(reasoner); 
					
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
							mainbody.addAll(edge.getAtoms());
						}
					}
					for (TreeWitness tw : compatibleTWs) {
						Atom twAtom = getHeadAtom(headURI, "TW_" + (edgeDP.getRules().size() + 1), cc.getVariables());
						mainbody.addNoCheck(twAtom);				
						for (List<Atom> twfa : tw.getFormula())
							edgeDP.appendRule(fac.getCQIE(twAtom, twfa));
					}	
					output.appendRule(fac.getCQIE(headAtom, getExtAtoms(mainbody, usedExts))); 
				}
			}
			else {
				// no conflicting tree witnesses
				// use polynomial tree witness rewriting by treating each edge independently 
				MinimalCQProducer mainbody = new MinimalCQProducer(reasoner); 		
				for (Edge edge : cc.getEdges()) {
					log.debug("EDGE " + edge);
					MinimalCQProducer edgeAtoms = new MinimalCQProducer(reasoner); 
					edgeAtoms.addAll(edge.getAtoms());
					
					Atom edgeAtom = null;
					for (TreeWitness tw : tws.getTWs())
						if (tw.getDomain().contains(edge.getTerm0()) && tw.getDomain().contains(edge.getTerm1())) {
							if (edgeAtom == null) {
								IRI atomURI = edge.getBAtoms().iterator().next().getPredicate().getName();
								edgeAtom = getHeadAtom(headURI, 
										"EDGE_" + (edgeDP.getRules().size() + 1) + "_" + atomURI.getRawFragment(), cc.getVariables());
								mainbody.addNoCheck(edgeAtom);				
								edgeDP.appendRule(fac.getCQIE(edgeAtom, getExtAtoms(edgeAtoms, usedExts)));													
							}
							
							for (List<Atom> twfa : tw.getFormula())
								edgeDP.appendRule(fac.getCQIE(edgeAtom, twfa));
						}
					
					if (edgeAtom == null) // no tree witnesses -- direct insertion into the main body
						mainbody.addAll(edgeAtoms.getAtoms());
				}
				output.appendRule(fac.getCQIE(headAtom, getExtAtoms(mainbody, usedExts))); 
			}
		}
		else {
			// degenerate connected component -- a single loop
			Loop loop = cc.getLoop();
			log.debug("LOOP " + loop);
			MinimalCQProducer loopbody = new MinimalCQProducer(reasoner);
			loopbody.addAll(loop.getAtoms());
			output.appendRule(fac.getCQIE(headAtom, getExtAtoms(loopbody, usedExts))); 
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
				IRI cqieURI = cqieAtom.getPredicate().getName();
				List<Atom> ccBody = new ArrayList<Atom>(ccs.size());
				for (QueryConnectedComponent cc : ccs) {
					log.debug("CONNECTED COMPONENT (" + cc.getFreeVariables() + ")" + " EXISTS " + cc.getQuantifiedVariables() + " WITH EDGES " + cc.getEdges());
					Atom ccAtom = getHeadAtom(cqieURI, "CC_" + (ccDP.getRules().size() + 1), cc.getFreeVariables());
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
	 * cache for the Ext_E predicates and Datalog programs they are defined by
	 * 
	 * @author Roman Kontchakov
	 *
	 */
	
	private class ExtPredicateCache {
		private Map<Predicate, Predicate> extPredicateMap = new HashMap<Predicate, Predicate>();
		private Map<Predicate, Predicate> extSomePropertyMap = new HashMap<Predicate, Predicate>();
		private Map<Predicate, Predicate> extSomeInvPropertyMap = new HashMap<Predicate, Predicate>();
		private Map<Predicate, List<CQIE>> extDPs = new HashMap<Predicate, List<CQIE>>();
		
		/**
		 * clears the cache (called when a new CBox is set)
		 */
		
		public void clear() {
			extPredicateMap.clear();
			extSomePropertyMap.clear();
			extSomeInvPropertyMap.clear();
			extDPs.clear();
		}
		
		/**
		 * 
		 * @param p: a query predicate E
		 * @param usedExts: a collection of Ext_E predicates that have been used in the rewriting so far
		 * @return the Ext_E predicate or E if Ext_E is trivially defined (i.e., with a single rule Ext_E :- E)
		 */
		
		public Predicate getExtPredicateClass(Predicate p, Set<Predicate> usedExts)  {
			Predicate ext = getEntryFor(p);
			if (ext != null) {
				usedExts.add(ext);
				return ext;
			}
			else
				return p;
		}
		
		public Predicate getExtPredicateProperty(Predicate p, Set<Predicate> usedExts)  {
			Predicate ext = getEntryFor(p);
			if (ext != null) {
				usedExts.add(ext);
				return ext;
			}
			else
				return p;
		}
		
		public Predicate getExtPredicatePropertySome(Predicate p, boolean inverse, Set<Predicate> usedExts) {
			Predicate ext = getEntryFor(p, inverse);
			if (ext != null) {
				usedExts.add(ext);
				return ext;
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
				List<CQIE> extDef = extDPs.get(pred);			 
				extDP.appendRule(extDef);		 
			}
			return extDP;
		}
		
		private final NewLiteral x = fac.getVariable("x");			
		private final NewLiteral y = fac.getVariable("y");
		private final NewLiteral w = fac.getNondistinguishedVariable(); 
		
		private Atom getAtom(BasicClassDescription c) {
			if (c instanceof OClass) 
				return (fac.getAtom(((OClass)c).getPredicate(), x));
			else {     //if (c instanceof PropertySomeRestriction) {
				PropertySomeRestriction some = (PropertySomeRestriction)c;
				return ((!some.isInverse()) ? 
						fac.getAtom(some.getPredicate(), x, w) : fac.getAtom(some.getPredicate(), w, x)); 
			}		
			
		}
		
		private IRI getExtURI(IRI iri, String prefix) {
			return OBDADataFactoryImpl.getIRI(iri.toString()+ prefix);
			//return null;
		}
		
		private ExtDatalogProgramDef getConceptDP(BasicClassDescription b) {
			IRI extURI;
			if (b instanceof OClass) 
				extURI = getExtURI(((OClass)b).getPredicate().getName(), "EXT_");
			else {
				PropertySomeRestriction some = (PropertySomeRestriction)b;
				extURI = getExtURI(some.getPredicate().getName(),  "EXT_E_" + ((!some.isInverse()) ? "" : "INV_"));
			}
			
			ExtDatalogProgramDef dp = new ExtDatalogProgramDef(fac.getAtom(fac.getClassPredicate(extURI), x), getAtom(b));
			for (BasicClassDescription c : reasoner.getSubConcepts(b)) 
				dp.add(getAtom(c));
		
			return dp;
		}
		
		private Predicate getEntryFor(Predicate p) {
			if (extPredicateMap.containsKey(p))
				return extPredicateMap.get(p);
				
			ExtDatalogProgramDef dp = null;
			if (p.getArity() == 1) {
				dp = getConceptDP(reasoner.getOntologyFactory().createClass(p));
			}
			else  {
				IRI extURI = getExtURI(p.getName(), "EXT_");
				dp = new ExtDatalogProgramDef(fac.getAtom(fac.getObjectPropertyPredicate(extURI), x, y), 
											fac.getAtom(p, x, y));
				for (Property sub: reasoner.getSubProperties(p, false))
					dp.add((!sub.isInverse()) ? 
						fac.getAtom(sub.getPredicate(), x, y) : fac.getAtom(sub.getPredicate(), y, x)); 
			}
			log.debug("DP FOR " + p + " IS " + dp.dp);
			if (dp.dp.size() <= 1)
				dp.minimise();			
			if (dp.dp.size() <= 1)
				dp = null;

			Predicate ext = null;
			if (dp != null) {
				ext = dp.extAtom.getPredicate();
				extDPs.put(ext, dp.dp);
			}
				
			extPredicateMap.put(p, ext);
			return ext;
		}	

		private Predicate getEntryFor(Predicate p, boolean inverse) {
			Map<Predicate, Predicate> extMap = inverse ? extSomePropertyMap : extSomeInvPropertyMap;
			
			if (extMap.containsKey(p))
				return extMap.get(p);
				
			ExtDatalogProgramDef dp = getConceptDP(reasoner.getOntologyFactory().createPropertySomeRestriction(p, inverse));

			log.debug("DP FOR " + p + " IS " + dp.dp);
			dp.minimise();			

			Predicate ext = dp.extAtom.getPredicate();
			extDPs.put(ext, dp.dp);
				
			extMap.put(p, ext);
			return ext;
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
			dp.add(mainQuery);
			if (dp.size() > 1) {
				dp = CQCUtilities.removeContainedQueries(dp, true, sigma);
				log.debug("SIMPLIFIED DP FOR " + extAtom + " IS " + dp);
			}
		}
	}	
}
