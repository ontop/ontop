package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQCUtilities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.iri.IRI;

public class ExtDatalogProgram {
	private static OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	
	private static final Logger log = LoggerFactory.getLogger(ExtDatalogProgram.class);

	private TreeWitnessReasonerLite reasoner;
	private Ontology sigma;

	private Map<Predicate, Predicate> extPredicateMap = new HashMap<Predicate, Predicate>();
	private Map<Predicate, Predicate> extSomePropertyMap = new HashMap<Predicate, Predicate>();
	private Map<Predicate, Predicate> extSomeInvPropertyMap = new HashMap<Predicate, Predicate>();
	private DatalogProgram fullDP;
	//private Map<Predicate, List<CQIE>> extDPs = new HashMap<Predicate, List<CQIE>>();

	private static IRI getIRI(IRI base, String suffix) {
		return OBDADataFactoryImpl.getIRI(base.toString() + suffix);
	}

	public ExtDatalogProgram(TreeWitnessReasonerLite reasoner) {
		this.reasoner = reasoner;
		fullDP = fac.getDatalogProgram();
	}
	
	public TreeWitnessReasonerLite getReasoner() {
		return reasoner;
	}
	
	/**
	 * clears the cache (called when a new CBox is set)
	 */
	
	public void setSigma(Ontology sigma) {
		this.sigma = sigma;
		extPredicateMap.clear();
		extSomePropertyMap.clear();
		extSomeInvPropertyMap.clear();
		fullDP.removeAllRules();
	}
	
	/**
	 * 
	 * @param p: a query predicate E
	 * @param usedExts: a collection of Ext_E predicates that have been used in the rewriting so far
	 * @return the Ext_E predicate or E if Ext_E is trivially defined (i.e., with a single rule Ext_E :- E)
	 */
	
	public Function getExtClassAtom(Function a, Set<Predicate> usedExts)  {
		Predicate ext = getEntryFor(a.getFunctionSymbol());
		if (ext != null) {
			usedExts.add(ext);
			return fac.getAtom(ext, a.getTerm(0));
		}
		else
			return a;
	}
	
	public Function getExtPropertyAtom(Function a, Set<Predicate> usedExts)  {
		Predicate ext = getEntryFor(a.getFunctionSymbol());
		if (ext != null) {
			usedExts.add(ext);
			return fac.getAtom(ext, a.getTerm(0), a.getTerm(1));
		}
		else
			return a;
	}
	
	public Function getExtPropertySomeAtom(Predicate p, boolean inverse, NewLiteral t, Set<Predicate> usedExts) {
		Predicate ext = getEntryFor(p, inverse);
		usedExts.add(ext);
		return fac.getAtom(ext, t);
	}
	
	public DatalogProgram getFullDP() {
		return fullDP;
	}
	
	private final NewLiteral x = fac.getVariable("x");			
	private final NewLiteral y = fac.getVariable("y");
	private final NewLiteral w = fac.getNondistinguishedVariable(); 
	
	private Function getAtom(BasicClassDescription c) {
		if (c instanceof OClass) 
			return (fac.getAtom(((OClass)c).getPredicate(), x));
		else {     //if (c instanceof PropertySomeRestriction) {
			PropertySomeRestriction some = (PropertySomeRestriction)c;
			return ((!some.isInverse()) ? 
					fac.getAtom(some.getPredicate(), x, w) : fac.getAtom(some.getPredicate(), w, x)); 
		}		
		
	}
	
	private ExtDatalogProgramDef getConceptDP(BasicClassDescription b) {
		IRI extURI;
		if (b instanceof OClass) 
			extURI = getIRI(((OClass)b).getPredicate().getName(), "EXT_");
		else {
			PropertySomeRestriction some = (PropertySomeRestriction)b;
			extURI = getIRI(some.getPredicate().getName(), !some.isInverse() ? "EXT_E_" : "EXT_E_INV_");
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
			IRI extURI = getIRI(p.getName(), "EXT_");
			dp = new ExtDatalogProgramDef(fac.getAtom(fac.getObjectPropertyPredicate(extURI), x, y), 
										fac.getAtom(p, x, y));
			for (Property sub: reasoner.getSubProperties(p, false))
				dp.add((!sub.isInverse()) ? 
					fac.getAtom(sub.getPredicate(), x, y) : fac.getAtom(sub.getPredicate(), y, x)); 
		}

		dp.minimise();			
		if (dp.dp.size() <= 1) {
			extPredicateMap.put(p, null);
			return null;
		}
		else {
			fullDP.appendRule(dp.dp);
			Predicate ext = dp.extAtom.getFunctionSymbol();
			extPredicateMap.put(p, ext);
			return ext;	
		}
	}	

	private Predicate getEntryFor(Predicate p, boolean inverse) {
		Map<Predicate, Predicate> extMap = inverse ? extSomePropertyMap : extSomeInvPropertyMap;	
		if (extMap.containsKey(p))
			return extMap.get(p);
			
		ExtDatalogProgramDef dp = getConceptDP(reasoner.getOntologyFactory().createPropertySomeRestriction(p, inverse));

		dp.minimise();			

		fullDP.appendRule(dp.dp);			
		Predicate ext = dp.extAtom.getFunctionSymbol();
		extMap.put(p, ext);
		return ext;
	}	

	/**
	 * class for Datalog program definitions of the Ext_E
	 * implements simplified CQ containment checks
	 * 
	 * @author Roman Kontchakov
	 *
	 */
	
	private class ExtDatalogProgramDef {
		private final Function extAtom;
		private final Predicate mainPredicate;
		private final CQIE mainQuery;
		private List<CQIE> dp = new LinkedList<CQIE>();
		
		public ExtDatalogProgramDef(Function extAtom, Function mainAtom) {
			this.extAtom = extAtom;
			this.mainPredicate = mainAtom.getFunctionSymbol();
			this.mainQuery = fac.getCQIE(extAtom, mainAtom);
		}
		
		public void add(Function body) {
			if (body.getFunctionSymbol().equals(mainPredicate))
				return;
			
			CQIE query = fac.getCQIE(extAtom, body);
			CQCUtilities cqc = new CQCUtilities(query, sigma);
			if (!cqc.isContainedIn(mainQuery)) 
				dp.add(query);
			else
				log.debug("    CQC CONTAINMENT: {} IN {}", query, mainQuery);
		}
		
		public void minimise() {
			log.debug("DP FOR {} IS {}", extAtom, dp);
			dp.add(mainQuery);
			if (dp.size() > 1) {
				dp = CQCUtilities.removeContainedQueries(dp, true, sigma);
				log.debug("SIMPLIFIED DP FOR {} IS {}", extAtom, dp);
			}
		}
	}	
}
