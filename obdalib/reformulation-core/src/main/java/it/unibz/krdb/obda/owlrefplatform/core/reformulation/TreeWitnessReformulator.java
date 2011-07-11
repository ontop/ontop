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
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQCUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.AtomicConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DLLiterOntology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ExistentialConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicDescriptionFactory;
import it.unibz.krdb.obda.utils.QueryUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeWitnessReformulator implements QueryRewriter {

	// private Set<Assertion> assertions;
	private DAG									conceptDAG			= null;

	private OBDADataFactory						fac					= OBDADataFactoryImpl.getInstance();

	private static final DescriptionFactory		descFactory			= new BasicDescriptionFactory();

	private static final Logger					log					= LoggerFactory.getLogger(TreeWitnessReformulator.class);

	private DLLiterOntology						aboxDependencies	= null;

	public TreeWitnessReformulator(Set<Assertion> set) {
		// this.assertions = set;
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

	private DatalogProgram reformulate(CQIE cqie) throws Exception {
		
		// compute the list of binary predicates, terms, variables and
		// existential variables
		Set<Term> terms = new HashSet<Term>(); // terms
		Set<Predicate> predicates = new HashSet<Predicate>(); // binary
																// predicates
		List<Term> variables = new ArrayList<Term>(); // all variables (needed
														// as a List)
		Set<Term> evars = new HashSet<Term>(); // all existential variables

		for (Atom a0 : cqie.getBody()) {
			PredicateAtom a = (PredicateAtom) a0;
			log.debug("atom: " + a);

			if (a.getPredicate().getArity() >= 2) {
				predicates.add(a.getPredicate());

				for (Term t : a.getTerms()) {
					// handle "undistinguished" variables, i.e., underscores?
					log.debug("term: " + t + " of " + t.getClass().getName());
					terms.add(t);

					if ((t instanceof VariableImpl) && !variables.contains(t)) {
						variables.add(t);

						if (!cqie.getHead().getTerms().contains(t)) {
							log.debug("existentially quantified " + t);
							evars.add(t);
						}
					}
				}
			}
		}
		log.debug("terms: " + terms);
		log.debug("variables: " + variables);
		log.debug("existentially quantified variables: " + evars);
		log.debug("binary predicates: " + predicates);

		// compute tree witness functions
		Set<TreeWitness> tws = new HashSet<TreeWitness>();
		for (Term t : terms) {
			for (Predicate p : predicates) {
				for (int d = 1; d <= 2; d++) {
					TreeWitness tw = new TreeWitness(t, new PredicatePosition(p, d));
					if (tw.extendWithAtoms(cqie.getBody())) {
						log.debug(tw.toString());
						List<Term> nonroots = tw.getNonRoots();
						// check whether the tree witness contains at least one
						// non-root
						if (nonroots.isEmpty()) {
							log.debug("trivial, ignoring");
							continue;
						}

						// check whether all non-roots are existentially
						// quantified
						boolean nonrootsOK = true;
						for (Term tt : nonroots)
							if (!evars.contains(tt)) {
								nonrootsOK = false;
								log.debug("non-root " + tt + " is not existentially quantified");
								break;
							}
						if (!nonrootsOK)
							continue;

						tws.add(tw);
					} else
						log.debug(tw.toString());
				}
			}
		}
		log.debug("TREE WITNESSES");
		for (TreeWitness tw : tws) {
			log.debug(tw.toString());
		}

		// generate the output datalog programme
		DatalogProgram out = fac.getDatalogProgram();
		int n = 1;

		Map<ConceptDescription, Predicate>	views = new HashMap<ConceptDescription, Predicate>();
		List<Atom> body = new ArrayList<Atom>();

		for (Atom a0 : cqie.getBody()) {
			PredicateAtom a = (PredicateAtom) a0;
			if (a.getArity() == 1) {
				Predicate pa = fac.getPredicate(URI.create("EXT" + a.getPredicate().getName().getFragment()), 1);
				views.put(descFactory.getAtomicConceptDescription(a.getPredicate()), pa);
				
				body.add(rewriteAtom(out, tws, a, cqie, 
						fac.getAtom(fac.getPredicate(URI.create("A" + n), variables.size()), variables), 
						fac.getAtom(pa, a.getTerm(0)), views));
			}
			else if (a.getArity() == 2) {
				body.add(rewriteAtom(out, tws, a, cqie, 
						fac.getAtom(fac.getPredicate(URI.create("P" + n), variables.size()), variables), 
						fac.getAtom(a.getPredicate(), a.getTerm(0), a.getTerm(1)), views));
			}
			n++;
		}
		out.appendRule(fac.getCQIE(cqie.getHead(), body));

		for (ConceptDescription C : views.keySet()) {
			Term z = fac.getVariable("z");
			PredicateAtom head = fac.getAtom(views.get(C), z);
			log.debug("CREATING VIEWS FOR " + C);
			log.debug("subclass " + C + " of " + C);
			out.appendRule(fac.getCQIE(head, Collections.singletonList(getConceptAtom(C,z))));

			DAGNode cnode = conceptDAG.getClassNode(C);
			if (cnode != null) {
				for (DAGNode node : cnode.descendans) {
					Description D = node.getDescription();
					log.debug("subclass " + D + " of " + C);
					out.appendRule(fac.getCQIE(head, Collections.singletonList(getConceptAtom(D,z))));
				}				
			}
			else
				log.debug("NO CLASS NODE FOR " + C);
		}
	 	
		return out;
	}

	private Atom getConceptAtom(Description D, Term z) {

		if (D instanceof AtomicConceptDescription) {
			return fac.getAtom(((AtomicConceptDescription) D).getPredicate(), z);
		} else if (D instanceof ExistentialConceptDescription) {
			Term w = fac.getVariable("w");
			ExistentialConceptDescription DD = (ExistentialConceptDescription) D;
			if (!DD.isInverse())
				return fac.getAtom(DD.getPredicate(), z, w);
			else
				return fac.getAtom(DD.getPredicate(), w, z);
		}		
		log.debug("UNKNOWN CONCEPT " + D);
		return null;
	}
	
	
	// check whether the term t of the tree witness tw can be an instance of C
	// in the anonymous part

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

	// return the atom that replaces a in the query (either head-atom or ext-atom)
	
	private PredicateAtom rewriteAtom(DatalogProgram out, Set<TreeWitness> tws, PredicateAtom a, CQIE cqie, PredicateAtom head, PredicateAtom ext, Map<ConceptDescription, Predicate> views) {
		
		boolean nontrivialRule = false;
		for (TreeWitness tw : tws) {
			log.debug("atom " + a + " on " + tw + " with term " + a.getTerms());
			
			boolean inDomain = true;
			for (Term t: a.getTerms())
				if (!tw.isInDomain(t)) {
					inDomain = false;
					log.debug("not in the domain " + t);
					break;
				}
			if (!inDomain)
				continue;

			if ((a.getArity() == 1) && tw.getRoots().contains(a.getTerm(0))) {
				log.debug("absorbed by the default rule of unary atom");
				continue;
			}

			List<Atom> wb = getRuleBodyForTreeWitness(tw, cqie, views);
			if (wb != null) {
				out.appendRule(fac.getCQIE(head, wb));
				nontrivialRule = true; 
			}
		}
		if (nontrivialRule) {
			out.appendRule(fac.getCQIE(head, Collections.singletonList((Atom)ext)));
			return head;			
		}
		else
			return ext;
	}
	
	
	// returns null if no rule should be produced for the tree witness

	private List<Atom> getRuleBodyForTreeWitness(TreeWitness tw, CQIE cqie, Map<ConceptDescription, Predicate> views) {
		List<Term> roots = tw.getRoots();
		Term x = roots.get(0);

		List<Atom> wb = new ArrayList<Atom>();

		// Tree-structure atoms
		for (Atom ca0 : cqie.getBody()) {
			PredicateAtom ca = (PredicateAtom) ca0;
			if ((ca.getArity() == 1) && tw.isInDomain(ca.getTerm(0))) {
				if (tw.isRoot(ca.getTerm(0))) {
					Predicate pa = fac.getPredicate(URI.create("EXT" + ca.getPredicate().getName().getFragment()), 1);
					Atom ua = fac.getAtom(pa, x);
					if (!wb.contains(ua))
						wb.add(fac.getAtom(pa, x));
					else 
						log.debug("duplicating atom " + ua);
					views.put(descFactory.getAtomicConceptDescription(ca.getPredicate()), pa);
				} else if (!checkTree(tw, ca.getTerm(0), descFactory.getAtomicConceptDescription(ca.getPredicate())))
					return null;
			} else if (ca.getArity() == 2) {
				if (!checkTree(tw, ca.getTerm(0), descFactory.getExistentialConceptDescription(ca.getPredicate(), false)))
					return null;

				if (!checkTree(tw, ca.getTerm(1), descFactory.getExistentialConceptDescription(ca.getPredicate(), true)))
					return null;
			}
		}

		// Extension Atom ext_{\exists R}
		Predicate p = fac.getPredicate(
				URI.create("EXT" + tw.getDirection().getPosition() + "E" + tw.getDirection().getPredicate().getName().getFragment()), 1);
		wb.add(fac.getAtom(p, x));
		views.put(descFactory.getExistentialConceptDescription(tw.getDirection().getPredicate(), tw.getDirection().getPosition() == 1), p);

		// DAGNode node = conceptDAG.getClassNode(C);
		// Negated Atom \neg \exists z R(x,z)
		// List<Term> naterms = new ArrayList<Term>();
		// naterms.add(null); naterms.add(null);
		// naterms.set(2 - tw.getDirection().getPosition(), x);
		// naterms.set(tw.getDirection().getPosition() - 1,
		// fac.getVariable("z"));
		// wb.add(fac.getAtom(tw.getDirection().getPredicate(),naterms));
		// wb.add(fac.getNOTAtom((Term)negatedatom)); // IMPORTANT: type
		// conversion fails

		// Equality Atoms for all terms that are equivalent to the root
		for (Term v : roots)
			if (!v.equals(x))
				wb.add(fac.getEQAtom(x, v));

		return wb;
	}

	@Override
	public Query rewrite(Query input) throws Exception {
		if (!(input instanceof DatalogProgram)) 
			throw new Exception("Rewriting exception: The input must be a DatalogProgram instance");

		DatalogProgram prog = (DatalogProgram) input;
		log.debug("Starting query rewriting. Received query: \n{}", prog);

		if (!prog.isUCQ()) 
			throw new Exception("Rewriting exception: The input is not a valid union of conjuctive queries");

		// TBD: CHECK WHETHER THE QUERY IS CONNECTED

		/* Query preprocessing */
		// log.debug("Reformulating");
		DatalogProgram reformulation = reformulate(prog.getRules().get(0));
		QueryUtils.copyQueryModifiers(prog, reformulation);
		// log.debug("Done reformulating. Output: \n{}", reformulation);
		
		log.debug("########## Basic Tree Witness Reformulation ##########: {}", reformulation.toString());
		log.debug("Main reformulation finished. Size: {}", reformulation.getRules().size());
		if (aboxDependencies!= null) {
			CQCUtilities.removeContainedQueriesSorted(reformulation, true, aboxDependencies);
			log.debug("########## Optimized Tree Witness Reformulation ##########: {}", reformulation.toString());
			log.debug("Done optimizing w.r.t. ABox dependencies. Resulting size: {}", reformulation.getRules().size());
		}
		
		return reformulation;
	}

	@Override
	public void setTBox(Ontology ontology) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setABoxDependencies(Ontology sigma) {
		aboxDependencies = (DLLiterOntology) sigma;

	}

}
