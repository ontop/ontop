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
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterConceptInclusionImpl;
import it.unibz.krdb.obda.utils.QueryUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeWitnessReformulator implements QueryRewriter {

	private Set<Assertion>				assertions		= null;
	private DAG  conceptDAG = null;
	
	private OBDADataFactory				fac				= OBDADataFactoryImpl.getInstance();
	Logger								log				= LoggerFactory.getLogger(TreeWitnessReformulator.class);
	
	public TreeWitnessReformulator(Set<Assertion> set) {
		this.assertions = set;
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
			log.debug("atom: \n{}", a);
			boolean addToComponent = terms.isEmpty();

			for (Term t: a.getTerms()) {
				// handle "undistinguished" variables, i.e., underscores?
				log.debug("term: {}\n", t + " of " + t.getClass().getName());
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
				log.debug("THE QUERY IS NOT CONNECTED\n");
			}
		}
		log.debug("component terms: {}\n", terms);			
		log.debug("existentially quntified: {}\n", variables);
		log.debug("binary predicates: {}\n", predicates);					
		
		
		// compute witness functions
		List<TreeWitness> tws = new ArrayList<TreeWitness>(); 
		for (Term t: terms) {
			for (Predicate p: predicates) {
				for (int d = 1; d <= 2; d++) {
					TreeWitness tw = new TreeWitness(t, new PredicatePosition(p, d));
					if (tw.extendWithAtoms(q.getRules().get(0).getBody())) 	{
						log.debug("TREE WITNESS {}\n", tw);
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
				Predicate np = fac.getPredicate(URI.create("A" + n1), 1);
				body.add(fac.getAtom(np, a.getTerm(0)));
				n1++;
			}
			if (a.getArity() == 2) {
				Predicate np = fac.getPredicate(URI.create("P" + n2), variables.size());
				
				body.add(fac.getAtom(np, variables));
					
				out.appendRule(fac.getCQIE(fac.getAtom(np, variables), 
							Collections.singletonList((Atom)fac.getAtom(a.getPredicate(), 
									a.getTerm(0), a.getTerm(1)))));
					
				for (TreeWitness tw: tws) {
					// check whether both terms are in the tree witness domain
					if (!tw.isInDomain(a.getTerm(0)) || !tw.isInDomain(a.getTerm(1)))
						continue;
					
					// check whether all non-roots are existentially quantified
					boolean nonrootsOK = true;
					for (Term t: tw.getNonRoots()) 
						if (!evars.contains(t)) {
							nonrootsOK = false;
							log.debug("non roots are not all existentially quantified: " + t);
							break;
						}
					if (!nonrootsOK)
						continue;

					List<Term> roots = tw.getRoots();
					Term x = roots.get(0);
							
					List<Atom> wb = new ArrayList<Atom>();
							
					// Extension Atom ext_{\exists R} 
					wb.add(fac.getAtom(fac.getPredicate(URI.create("EXT" + 
								tw.getDirection().getPosition() + "E" +
								tw.getDirection().getPredicate().getName().getFragment()), 1), x)); // ext
							
					// Negated Atom \neg \exists z R(x,z)
					List<Term> naterms = new ArrayList<Term>();
					naterms.add(null); naterms.add(null);
					naterms.set(2 - tw.getDirection().getPosition(), x);
					naterms.set(tw.getDirection().getPosition() - 1, fac.getVariable("z"));
					wb.add(fac.getAtom(tw.getDirection().getPredicate(),naterms));
					//wb.add(fac.getNOTAtom((Term)negatedatom)); // IMPORTANT: type conversion fails 
						
					// Tree-structure atom
					// TBD
							
					// Equality Atoms for all terms that are equivalent to the root
					for (Term v : roots) 
						if (!v.equals(x))
							wb.add(fac.getEQAtom(x, v));
													
 
					out.appendRule(fac.getCQIE(fac.getAtom(np, variables), wb));
				}
						
				n2++;				
			}
		}
		out.appendRule(fac.getCQIE(cqie.getHead(), body));
				
		log.debug("CONCEPT DAG"); // descendants = subclasses
		for (DAGNode node: conceptDAG.getClasses()) {
			log.debug(node.toString());
			log.debug(node.descendans.toString());
		}
		
		return out;	
	}
	
	
	@Override
	public Query rewrite(Query input) throws Exception {
		if (!(input instanceof DatalogProgram)) {
			throw new Exception("Rewriting exception: The input must be a DatalogProgram instance");
		}

		DatalogProgram prog = (DatalogProgram) input;

		log.debug("Starting query rewrting. Received query: \n{}", prog);

		if (!prog.isUCQ()) {
			throw new Exception("Rewriting exception: The input is not a valid union of conjuctive queries");
		}

		/* Query preprocessing */
		log.debug("Reformulating");
		DatalogProgram reformulation = reformulate(prog);
		log.debug("Done reformulating. Output: \n{}", reformulation);

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
