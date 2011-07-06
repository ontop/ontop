package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.PredicateAtom;
import it.unibz.krdb.obda.model.Query;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.VariableImpl;
import it.unibz.krdb.obda.owlrefplatform.core.abox.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.abox.DAGNode;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.AtomicConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ExistentialConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicDescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterConceptInclusionImpl;
import it.unibz.krdb.obda.utils.QueryUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeWitnessReformulator implements QueryRewriter {

	private Set<Assertion>				assertions;
	private DAG  conceptDAG = null;
	private Map<ConceptDescription, Predicate> views;
	
	private OBDADataFactory				fac				= OBDADataFactoryImpl.getInstance();

	private static final DescriptionFactory descFactory = new BasicDescriptionFactory();

	private static final Logger log = LoggerFactory.getLogger(TreeWitnessReformulator.class);
	
	public TreeWitnessReformulator(Set<Assertion> set) {
		this.assertions = set;
		this.views = new HashMap<ConceptDescription, Predicate>();
	}
	
	public void setConceptDAG(DAG conceptDAG) {
		this.conceptDAG = conceptDAG;
	}
	
	/***
	 * Reformulates the query. 
	 * 
	 * 
	 * @param q
	 * @return
	 * @throws Exception
	 */
	
	private DatalogProgram reformulate(DatalogProgram q) throws Exception {
		
		// collect terms and binary predicate names for computing the tree witnesses
		Set<Term> terms = new HashSet<Term>(); // terms of the current connected component
		List<Term> variables = new ArrayList<Term>(); // all variables
		HashSet<Term> evars = new HashSet<Term>(); // all variables
		
		Set<Predicate> predicates = new HashSet<Predicate>(); // binary predicates
		
		if (q.getRules().size() != 1)
			log.debug("ONLY CQ ARE ALLOWED\n");		
		CQIE cqie = q.getRules().get(0);

		for (Atom a0: cqie.getBody()) {
			PredicateAtom a = (PredicateAtom) a0;
			log.debug("atom: " + a);
			boolean addToComponent = terms.isEmpty();

			for (Term t: a.getTerms()) {
				// handle "undistinguished" variables, i.e., underscores?
				log.debug("term: " + t + " of " + t.getClass().getName());
				if (terms.contains(t)) {
					addToComponent = true;
					break;
				}
			}

			log.debug("in the same component: {}\n", addToComponent);
			if (addToComponent) {
				if (a.getPredicate().getArity() >= 2) 
					predicates.add(a.getPredicate());
				
				for (Term t: a.getTerms()) {
					terms.add(t);
					
					if ((t instanceof VariableImpl) && !variables.contains(t)) 
						variables.add(t);
					
					
					if ((t instanceof VariableImpl) && !cqie.getHead().getTerms().contains(t)) {
						log.debug("existentially quantified " + t);
						evars.add(t);
					}
				}
			}
			else { 
				log.debug("THE QUERY IS NOT CONNECTED\n"); // WRONG!
			}
		}
		log.debug("component terms: " + terms);			
		log.debug("existentially quantified: " + variables);
		log.debug("binary predicates: " + predicates);					
		
		
		// compute witness functions
		List<TreeWitness> tws = new ArrayList<TreeWitness>(); 
		for (Term t: terms) {
			for (Predicate p: predicates) {
				for (int d = 1; d <= 2; d++) {
					TreeWitness tw = new TreeWitness(t, new PredicatePosition(p, d));
					if (tw.extendWithAtoms(q.getRules().get(0).getBody())) 	{
						log.debug("TREE WITNESS " + tw);
						if (tw.getNonRoots().isEmpty()) {
							log.debug("TRIVIAL, IGNORING");
							continue;
						}
							
						boolean found = false;
						for (TreeWitness twc: tws) 
							if (twc.getRoots().contains(t) && twc.getDirection().equals(tw.getDirection())) {						
								found = true;
								break;
							}
						
						if (!found)
							tws.add(tw);
						else
							log.debug("DUPLICATING");
					}
					else
						log.debug("NO TREE WITNESS {}\n", tw);
				}
			}
		}
		
		
		// generate the output datalog programme
		DatalogProgram out = fac.getDatalogProgram();
		QueryUtils.copyQueryModifiers(q, out);
		int n1 = 1;
		int n2 = 1;
			
		List<Atom> body = new ArrayList<Atom>();
			
		for (Atom a0 : cqie.getBody()) {
			PredicateAtom a = (PredicateAtom)a0;
			if (a.getArity() == 1) {
				Predicate np = fac.getPredicate(URI.create("A" + n1), variables.size());
				body.add(fac.getAtom(np, variables));

				Predicate pa = fac.getPredicate(URI.create("EXT" + 
						a.getPredicate().getName().getFragment()), 1);
				out.appendRule(fac.getCQIE(fac.getAtom(np, variables), 
						Collections.singletonList((Atom)fac.getAtom(pa, a.getTerm(0)))));
				views.put(descFactory.getAtomicConceptDescription(a.getPredicate()), pa);

				
				for (TreeWitness tw: tws) {
					log.debug("atom " + a + " on tree witness " + tw + " with term " + a.getTerm(0));
					if (!tw.isInDomain(a.getTerm(0))) 
						continue;
					log.debug("in the domain");
					
					if (!nonRootsAreExistential(tw, evars))
						continue;

					List<Atom> wb = getRuleBodyForTreeWitness(tw, cqie);								
 
					if (wb != null)
						out.appendRule(fac.getCQIE(fac.getAtom(np, variables), wb));
				}

				n1++;
			}
			if (a.getArity() == 2) {
				Predicate np = fac.getPredicate(URI.create("P" + n2), variables.size());
				
				body.add(fac.getAtom(np, variables));
					
				out.appendRule(fac.getCQIE(fac.getAtom(np, variables), 
							Collections.singletonList((Atom)fac.getAtom(a.getPredicate(), 
									a.getTerm(0), a.getTerm(1)))));
					
				for (TreeWitness tw: tws) {
					if (!tw.isInDomain(a.getTerm(0)) || !tw.isInDomain(a.getTerm(1)))
						continue;
					
					if (!nonRootsAreExistential(tw, evars))
						continue;

					List<Atom> wb = getRuleBodyForTreeWitness(tw, cqie);								
 
					if (wb != null)
						out.appendRule(fac.getCQIE(fac.getAtom(np, variables), wb));
				}
						
				n2++;				
			}
		}
		out.appendRule(fac.getCQIE(cqie.getHead(), body));

		for (ConceptDescription C: views.keySet()) {
			Term z = fac.getVariable("z");
			PredicateAtom head = fac.getAtom(views.get(C), z);
			log.debug("CREATING VIEWS FOR " + C);
			Set<DAGNode> subclasses = conceptDAG.getClassNode(C).descendans;  // CAREFUL here
			subclasses.add(conceptDAG.getClassNode(C));
			for (DAGNode node: subclasses) {
				Description D = node.getDescription();
				//log.debug("subclass " + D + " of " + C);
				Atom p = null;
				if (D instanceof AtomicConceptDescription) {
					p = fac.getAtom(((AtomicConceptDescription)D).getPredicate(), z);
				}
				else if (D instanceof ExistentialConceptDescription) {
					Term w = fac.getVariable("w");
					ExistentialConceptDescription DD = (ExistentialConceptDescription)D;
					if (!DD.isInverse())
						p = fac.getAtom(DD.getPredicate(), z, w);
					else
						p = fac.getAtom(DD.getPredicate(), w, z);						
				}	
				out.appendRule(fac.getCQIE(head, Collections.singletonList(p)));
			}
				
			//  {
		}
		
		return out;	
	}
	// check whether all non-roots are existentially quantified
	
	private static boolean nonRootsAreExistential(TreeWitness tw, Set<Term> evars) {
		boolean nonrootsOK = true;
		for (Term t: tw.getNonRoots()) 
			if (!evars.contains(t)) {
				nonrootsOK = false;
				log.debug("non roots are not all existentially quantified: " + t);
				break;
			}
		return nonrootsOK;
	}
	
	private boolean checkTree(TreeWitness tw, Term t, ConceptDescription C) {
		if (tw.isInDomain(t) && !tw.isRoot(t)) {
			PredicatePosition pp = tw.getLabelTail(t);
			log.debug("checking tree for predicate position: " + pp + " for " + t);
			ConceptDescription ETi = descFactory.getExistentialConceptDescription(pp.getPredicate(), pp.getPosition() == 2);
			if (!ETi.equals(C) && !conceptDAG.getClassNode(C).descendans.contains(conceptDAG.getClassNode(ETi))) {
				log.debug("falsum in " + C + " " + ETi);								
				return false;
			}
		}
		return true;
	}
	
	// returns null if no rule should be produced for the tree witness
	
	private List<Atom> getRuleBodyForTreeWitness(TreeWitness tw, CQIE cqie) {
		List<Term> roots = tw.getRoots();
		Term x = roots.get(0);
				
		List<Atom> wb = new ArrayList<Atom>();

		// Tree-structure atoms
		for (Atom ca0: cqie.getBody()) {
			PredicateAtom ca = (PredicateAtom)ca0;
			if ((ca.getArity() == 1) && tw.isInDomain(ca.getTerm(0))) {
				if (tw.isRoot(ca.getTerm(0))) {
					Predicate pa = fac.getPredicate(URI.create("EXT" + 
							ca.getPredicate().getName().getFragment()), 1);
					wb.add(fac.getAtom(pa, x)); 
					views.put(descFactory.getAtomicConceptDescription(ca.getPredicate()), pa);
				}
				else if (!checkTree(tw, ca.getTerm(0), descFactory.getAtomicConceptDescription(
						ca.getPredicate()))) 
					return null;
			}
			else if (ca.getArity() == 2) {
				if (!checkTree(tw, ca.getTerm(0), descFactory.getExistentialConceptDescription(
						ca.getPredicate(), false))) 
					return null;
				
				if (!checkTree(tw, ca.getTerm(1), descFactory.getExistentialConceptDescription(
						ca.getPredicate(), true))) 
					return null;								
			}
		}
						
		// Extension Atom ext_{\exists R}
		Predicate p = fac.getPredicate(URI.create("EXT" + 
				tw.getDirection().getPosition() + "E" +
				tw.getDirection().getPredicate().getName().getFragment()), 1);
		wb.add(fac.getAtom(p, x)); 
		views.put(descFactory.getExistentialConceptDescription(
				tw.getDirection().getPredicate(), tw.getDirection().getPosition() == 1), p);
		
		//DAGNode node = conceptDAG.getClassNode(C);
		// Negated Atom \neg \exists z R(x,z)
		//List<Term> naterms = new ArrayList<Term>();
		//naterms.add(null); naterms.add(null);
		//naterms.set(2 - tw.getDirection().getPosition(), x);
		//naterms.set(tw.getDirection().getPosition() - 1, fac.getVariable("z"));
		//wb.add(fac.getAtom(tw.getDirection().getPredicate(),naterms));
		//wb.add(fac.getNOTAtom((Term)negatedatom)); // IMPORTANT: type conversion fails 
			
		// Equality Atoms for all terms that are equivalent to the root
		for (Term v : roots) 
			if (!v.equals(x))
				wb.add(fac.getEQAtom(x, v));
		
		return wb;		
	}
	
	
	@Override
	public Query rewrite(Query input) throws Exception {
		if (!(input instanceof DatalogProgram)) {
			throw new Exception("Rewriting exception: The input must be a DatalogProgram instance");
		}

		DatalogProgram prog = (DatalogProgram) input;

		log.debug("Starting query rewriting. Received query: \n{}", prog);

		if (!prog.isUCQ()) {
			throw new Exception("Rewriting exception: The input is not a valid union of conjuctive queries");
		}

		/* Query preprocessing */
		//log.debug("Reformulating");
		DatalogProgram reformulation = reformulate(prog);
		//log.debug("Done reformulating. Output: \n{}", reformulation);

		return reformulation;	
	}

	@Override
	public void setTBox(Ontology ontology) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setABoxDependencies(Ontology sigma) {
		// TODO Auto-generated method stub
		
	}


}
