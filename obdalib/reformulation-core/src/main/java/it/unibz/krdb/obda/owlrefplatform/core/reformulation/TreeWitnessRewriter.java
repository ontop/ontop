package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.PropertySomeClassRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.SubClassAxiomImpl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.reasoner.Reasoner;

public class TreeWitnessRewriter implements QueryRewriter {
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Ontology tbox;
	private List<SubClassAxiomImpl> generatingAxioms;

	private static OBDADataFactory	fac	= OBDADataFactoryImpl.getInstance();
	private static OntologyFactory ontFactory = OntologyFactoryImpl.getInstance();
	
	private static final Logger log = LoggerFactory.getLogger(TreeWitnessRewriter.class);


	@Override
	public void setTBox(Ontology ontology) 
	{
		this.tbox = ontology;

		// collect generating axioms
		generatingAxioms = new LinkedList<SubClassAxiomImpl>();
		log.debug("AXIOMS");
		for (Axiom ax: tbox.getAssertions())
		{
			if (ax instanceof SubClassAxiomImpl)
			{
				SubClassAxiomImpl sax = (SubClassAxiomImpl)ax;
				ClassDescription sc = sax.getSuper();
				if (sc instanceof PropertySomeClassRestriction)
				{
					PropertySomeClassRestriction some = (PropertySomeClassRestriction)sc;
					log.debug("property " + some.getPredicate() + ", filler " + some.getFiller() + " from " + sax);
					generatingAxioms.add(sax);
				}
				else //if (sc instanceof OClass)
				{
					log.debug("SUBCLASS OF " + sc + ": " + sax + ((sc instanceof OClass) ? "": " UNKNOWN TYPE"));
				}
			}
			else
				log.debug(ax.toString());
		}
	}
	


	@Override
	public void setCBox(Ontology sigma) 
	{
		// TODO Auto-generated method stub	
	}

	@Override
	public void initialize() 
	{
		// TODO Auto-generated method stub	
	}
	
	private static CQIE getCQIE(Atom head, Atom body)
	{
		List<Atom> b = new LinkedList<Atom>();
		b.add(body);
		return fac.getCQIE(head, b);		
	}
	
	private static CQIE getCQIE(Atom head, Atom body1, Atom body2)
	{
		List<Atom> b = new LinkedList<Atom>();
		b.add(body1);
		b.add(body2);
		return fac.getCQIE(head, b);		
	}
	
	private static Atom getAtom(URI name, List<Term> terms) 
	{
		return fac.getAtom(fac.getPredicate(name, terms.size()), terms);
	}
	
	private static Atom getAtom(URI name, Term term) 
	{
		List<Term> terms = new LinkedList<Term>();
		terms.add(term);
		return fac.getAtom(fac.getPredicate(name, 1), terms);
	}
	
	private static Atom getAtom(URI name, Term term1, Term term2) 
	{
		List<Term> terms = new LinkedList<Term>();
		terms.add(term1);
		terms.add(term2);
		return fac.getAtom(fac.getPredicate(name, 1), terms);
	}
	
	private static Atom getRoleAtom(PropertySomeClassRestriction p, Term t1, Term t2) //throws MalformedURIException
	{
		if (!p.isInverse())
			return getAtom(p.getPredicate().getName(), t1, t2);
		else
			return getAtom(p.getPredicate().getName(), t2, t1);
	}

	private static class AdjacentTermsPair 
	{
		private Term t1, t2;
		
		public AdjacentTermsPair(Term t1, Term t2)
		{
			this.t1 = t1;
			this.t2 = t2;
		}
		
		public boolean equals(Object o)
		{
			if (o instanceof AdjacentTermsPair)
			{
				AdjacentTermsPair other = (AdjacentTermsPair)o;
				if (this.t1.equals(other.t1) && this.t2.equals(other.t2))
					return true;
				if (this.t2.equals(other.t1) && this.t1.equals(other.t2))
					return true;
			}
			return false;
		}
		
		public String toString()
		{
			return "term pair: {" + t1 + ", " + t2 + "}";
		}
	}
	
	@Override
	public OBDAQuery rewrite(OBDAQuery input) throws OBDAException 
	{	
		// public DatalogProgram getRewriting(Set<TreeWitness> tws) //throws MalformedURIException
		CQIE cqie = (CQIE)input;
		
		Set<TreeWitness> tws = getReducedSetOfTreeWitnesses(cqie);
		
		DatalogProgram output = fac.getDatalogProgram(); //(cqie.getHead().getPredicate());
		
		try {
		int Q = 0;
		{
			List<Atom> mainbody = new LinkedList<Atom>();
			for (Atom a: cqie.getBody())
				if (a.getArity() == 2)
						mainbody.add(getAtom(getQName(a.getPredicate().getName(), ++Q), a.getTerms()));
			
			// if no binary predicates
			if (mainbody.size() == 0)
				for (Atom a: cqie.getBody())
					mainbody.add(getAtom(getExtName(a.getPredicate().getName()), a.getTerms()));			
			
			output.appendRule(fac.getCQIE(cqie.getHead(), mainbody));
		}
		Q = 0;
		for (Atom a: cqie.getBody())
			if (a.getArity() == 2)
			{
				URI Qname = getQName(a.getPredicate().getName(), ++Q);
				// TODO: groups for pairs of terms
				List<Atom> group = new LinkedList<Atom>();
				group.add(getAtom(getExtName(a.getPredicate().getName()), a.getTerms()));
				for (Atom aa: cqie.getBody())
					if ((aa.getArity() == 1) && a.getTerms().contains(aa.getTerm(0)))
						group.add(getAtom(getExtName(aa.getPredicate().getName()), aa.getTerms()));
					
				output.appendRule(fac.getCQIE(getAtom(Qname, a.getTerms()), group));
			
				for (TreeWitness tw: tws)
					if (tw.getDomain().containsAll(a.getTerms()))
					{
						// TREE WITNESS FORMULAS
						List<Atom> twf = new LinkedList<Atom>();
						List<Term> roots = new LinkedList<Term>(tw.getRoots());
						Term r0 = roots.get(0);
						twf.add(getAtom(getExtName(tw.getGenerator()), r0));
						for (Term rt: roots)
							if (!rt.equals(roots.get(0)))
								twf.add(getAtom(new URI("http://EQ"), rt, r0));
						for (ClassDescription c: tw.getRootType())
						{
							OClass cl = (OClass)c;
							twf.add(getAtom(getExtName(cl.getPredicate().getName()), r0));
						}
						
						output.appendRule(fac.getCQIE(getAtom(Qname, a.getTerms()), twf));
					}
			}
		
		Set<PropertySomeClassRestriction> gen = new HashSet<PropertySomeClassRestriction>();
		for (TreeWitness tw: tws)
			gen.add(tw.getGenerator());
		
		//System.out.println("\nEXT PREDICATES");
		for (CQIE ext: getExtPredicates(cqie, gen))
			output.appendRule(ext);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return output;
	}
	
	
	public List<CQIE> getExtPredicates(CQIE cqie, Set<PropertySomeClassRestriction> gencon) throws URISyntaxException //throws MalformedURIException
	{
		List<CQIE> list = new LinkedList<CQIE>();
		Set<Predicate> exts = new HashSet<Predicate>();

		for (Atom a: cqie.getBody())
			exts.add(a.getPredicate());
				
		
		// GENERATING CONCEPTS
		
		for (PropertySomeClassRestriction some: gencon)
		{
			URI uri = getExtName(some);
			Term x = fac.getVariable("x");
			
			for (Predicate c: tbox.getConcepts())
				//COMM if (!c.isOWLNothing() && r.isSubsumed(c, some))
				{
					//System.out.println("EXT COMPUTE SUBCLASSES: " + c);
					list.add(getCQIE(getAtom(uri, x), getAtom(c.getName(), x)));
				}
			
			{
				Term w = fac.getVariable("w");
				Atom ra = getRoleAtom(some, x, w);
				exts.add(ra.getPredicate());
				ra.setPredicate(fac.getPredicate(getExtName(ra.getPredicate().getName()), 2));
				//System.out.println(ra);
				//COMM if(r.isSubsumed(f.getOWLObjectSomeValuesFrom(some.getProperty(), f.getOWLThing()), some))
					list.add(getCQIE(getAtom(uri, x), ra));
			}
			/*
			for (OWLObjectPropertyExpression p: getProperties())
				if (!p.isOWLBottomObjectProperty())
				{
					if(isSubsumed(f.getOWLObjectSomeValuesFrom(p, f.getOWLThing()), some))
					{
						list.add(getCQIE(new Atom(uri, x), getRoleAtom(p, x, w)));
						continue;
					}
					
					for (OWLClassExpression c: getClasses())
						if (!c.isOWLNothing() && isSubsumed(f.getOWLObjectSomeValuesFrom(p, c), some))
							list.add(getCQIE(new Atom(uri, x), getRoleAtom(p, x, w), new Atom(new URI(c.asOWLClass().getIRI().toString()), w)));						
				}
			*/
		}
				
	/* COMM	
		for (Predicate pred: exts)
		{
			if (pred.getArity() == 1)
			{
				URI ext = getExtName(pred.getName());
				OWLClass ac = f.getOWLClass(pred.getIRI());
				Term x = fac.getVariable("x");
				//list.add(getCQIE(new Atom(ext, x), new Atom(a.getPredicate().getName(), x)));
				
				for (OWLClass c: r.getClasses())
					if (!c.isOWLNothing() && r.isSubsumed(c, ac))
						list.add(getCQIE(getAtom(ext, x), getAtom(new URI(c.getIRI().toString()), x)));
				
				for (OWLObjectPropertyExpression p: r.getProperties())
					if (!p.isOWLBottomObjectProperty() && r.isSubsumed(f.getOWLObjectSomeValuesFrom(p, f.getOWLThing()), ac))
						list.add(getCQIE(getAtom(ext, x), getRoleAtom(p, x, fac.getVariable("w"))));						
			}
			else if (pred.getArity() == 2)
			{
				URI ext = getExtName(pred.getName());
				OWLObjectProperty pa = f.getOWLObjectProperty(pred.getIRI());
				Term x = fac.getVariable("x");
				Term y = fac.getVariable("y");
				//list.add(getCQIE(new Atom(ext, x, y), new Atom(a.getPredicate().getName(), x, y)));
				
				for (OWLObjectPropertyExpression p: r.getProperties())
					if (!p.isOWLBottomObjectProperty() && r.isSubsumed(p, pa))
					{
						//OWLObjectPropertyExpression pi = p.getInverseProperty().getSimplified();
						//if (!pi.isAnonymous() && isSubsumed(pa, pi.getInverseProperty()))
						//{
						//	System.out.println("INVERSE " + pi + " OF " + a.getPredicate() + ": NO EXTRA RULE GENERATED");
						//	continue;
						//}
						list.add(getCQIE(getAtom(ext, x, y), getRoleAtom(p, x, y)));
					}
			}
		}
	*/	
		return list;
	}
	
	private static URI getExtName(URI name) throws URISyntaxException
	{
		return new URI(name.getScheme(), name.getSchemeSpecificPart(), 
				"EXT_" + name.getFragment());
	}

	private static URI getExtName(PropertySomeClassRestriction some) throws URISyntaxException //throws MalformedURIException 
	{
		URI property = some.getPredicate().getName();
		return new URI(property.getScheme(), property.getSchemeSpecificPart(), 
					"EXT_" + property.getFragment() + (some.isInverse() ? "_I_" : "_") 
					+ some.getFiller().getPredicate().getName().getFragment());
	}

	private static URI getQName(URI name, int pos) throws URISyntaxException  
	{
		return new URI(name.getScheme(), name.getSchemeSpecificPart(), 
					"Q_" + pos + "_" + name.getFragment());
	}

	
	private Set<TreeWitness> getReducedSetOfTreeWitnesses(CQIE cqie)
	{
		Set<TreeWitness> treewitnesses = getTreeWitnesses(cqie);

		Set<TreeWitness> subtws = new HashSet<TreeWitness>();
		for (TreeWitness tw: treewitnesses)
		{
			boolean subsumed = false;
			for (TreeWitness tw1: treewitnesses)
				if (!tw.equals(tw1) && tw.getDomain().equals(tw1.getDomain()) && tw.getRoots().equals(tw1.getRoots()))
					// COMM if (reasoner.isEntailed(f.getOWLSubClassOfAxiom(tw.getGenerator(), tw1.getGenerator())))
					{
						System.out.println("SUBSUMED: " + tw + " BY " + tw1);
						subsumed = true;
						//break;
					}
			if (!subsumed)
				subtws.add(tw);
		}
		return subtws;
	}
	
		
	private Set<TreeWitness> getTreeWitnesses(CQIE cqie)
	{
		Set<TreeWitness> treewitnesses = new HashSet<TreeWitness>();
/* COMM		
		for (Term v: cqie.getVariables())
		{
			System.out.println("VARIABLE " + v);
			if (cqie.getHead().getTerms().contains(v))
			{
				System.out.println("   IS FREE, SKIPPING");
				continue;
			}
			List<OWLObjectPropertyExpression> edges = new LinkedList<OWLObjectPropertyExpression>();
			Set<ClassDescription> endtype = new HashSet<ClassDescription>();
			Set<Term> roots = new HashSet<Term>();
			for (Atom a: cqie.getBody())
			{
				if ((a.getArity() == 1) && (a.getTerm(0).equals(v)))
				{	
					endtype.add(ontFactory.createClass(a.getPredicate().getName()));
				}
				else if ((a.getArity() == 2) && (a.getTerm(0).equals(v)))
				{
					edges.add(f.getOWLObjectProperty(IRI.create(a.getPredicate().getName().toString()))
							.getInverseProperty());
					roots.add(a.getTerm(1));
				}
				else if ((a.getArity() == 2) && (a.getTerm(1).equals(v)))
				{
					edges.add(f.getOWLObjectProperty(IRI.create(a.getPredicate().getName().toString())));
					roots.add(a.getTerm(0));
				}
			}
			System.out.println("  EDGES " + edges);
			System.out.println("  ENDTYPE " + endtype);

			for (SubClassAxiomImpl a: generatingAxioms)
			{
				PropertySomeClassRestriction some = (PropertySomeClassRestriction)a.getSuper();
				if (isTreeWitness(some, roots, edges, endtype))
				{
					TreeWitness tw = new TreeWitness(some, roots, getRootType(cqie, roots), v);
					System.out.println(tw);
					treewitnesses.add(tw);				
				}
			}
		}
		
		Set<TreeWitness> delta = new HashSet<TreeWitness>();
		do
			for (TreeWitness tw: treewitnesses)
			{
				Set<TreeWitness> twa = new HashSet<TreeWitness>();
				twa.add(tw);
				saturateTreeWitnesses(treewitnesses, delta, new HashSet<Term>(), new LinkedList<OWLObjectPropertyExpression>(), twa);
			}
		while (treewitnesses.addAll(delta));
*/		
		return treewitnesses;
	}
	
/* COMM	
	private void saturateTreeWitnesses(CQIE cqie, Set<TreeWitness> treewitnesses, Set<TreeWitness> delta, Set<Term> roots, List<OWLObjectPropertyExpression> edges, Set<TreeWitness> tws)
	{
		boolean saturated = true;
		for (Atom a: cqie.getBody())
		{
			if (a.getArity() == 2) 
			{
				for (TreeWitness tw: tws)
				{
					Term r = null;
					Term nonr = null;
					OWLObjectPropertyExpression edge = null;
					if (tw.getRoots().contains(a.getTerm(0)) && !tw.getDomain().contains(a.getTerm(1)) && !roots.contains(a.getTerm(1)))
					{
						r = a.getTerm(0);
						nonr = a.getTerm(1);
						edge = f.getOWLObjectProperty(IRI.create(a.getPredicate().getName().toString()))
									.getInverseProperty();
					}
					else if (tw.getRoots().contains(a.getTerm(1)) && !tw.getDomain().contains(a.getTerm(0)) && !roots.contains(a.getTerm(0)))
					{
						r = a.getTerm(1);
						nonr = a.getTerm(0);
						edge = f.getOWLObjectProperty(IRI.create(a.getPredicate().getName().toString()));
					}
					else
						continue;
					
					System.out.println("ATOM " + a + " IS ADJACENT TO THE TREE WITNESS " + tw);
					saturated = false;
					for (TreeWitness twa: treewitnesses)
					{
						if (twa.getRoots().contains(r) && tw.getDomain().contains(nonr))
						{
							Set<TreeWitness> tws2 = new HashSet<TreeWitness>(tws);
							tws2.add(twa);
							System.out.println("    ATTACHING THE TREE WITNESS " + twa);
							saturateTreeWitnesses(treewitnesses, delta, roots, edges, tws2);
						}
					}
					Set<Term> roots2 = new HashSet<Term>(roots);
					roots2.add(nonr); 
					List<OWLObjectPropertyExpression> edges2 = new LinkedList<OWLObjectPropertyExpression>(edges);
					edges2.add(edge);
					System.out.println("    ATTACHING THE HANDLE " + edge);
					saturateTreeWitnesses(treewitnesses, delta, roots2, edges2, tws);
				}
			}
		}
		if (saturated)
		{
			System.out.println("CHEKCING WHETHER THE ROOTS " + roots + " WITH EDGES " + edges + " CAN BE ATTACHED TO THE FOLLOWING: ");
			for (TreeWitness tw: tws)
				System.out.println(tw);
			
			// collect the type of the root
			Set<ClassDescription> endtype = new HashSet<ClassDescription>();
			Set<Term> nonroots = new HashSet<Term>();
			boolean nonrootsbound = true;
			for (TreeWitness tw: tws)
			{
				endtype.add(tw.getGenerator());
				nonroots.addAll(tw.getDomain());
				// check whether the variables are bound
				for (Term t: tw.getRoots())
					if (cqie.getHead().getTerms().contains(t))
						nonrootsbound = false;
			}
			
			System.out.println("      NON-ROOTS ARE " + (nonrootsbound ? "" : "NOT") + " BOUND");
			if (nonrootsbound)
				for (SubClassAxiomImpl a: generatingAxioms)
				{
					PropertySomeClassRestriction some = (PropertySomeClassRestriction)a.getSuper();
					if (isTreeWitness(some, roots, edges, endtype))
					{
						TreeWitness tw = new TreeWitness(some, roots, getRootType(cqie, roots), nonroots);
						System.out.println(tw);
						delta.add(tw);				
					}
				}
		}
		
	}
	*/
	/* COMM
	private boolean isTreeWitness(PropertySomeClassRestriction some, Set<Term> roots, List<OWLObjectPropertyExpression> edges, Set<ClassDescription> endtype)
	{
		System.out.println("      CHECKING " + some);
		boolean match = false;
		if (r.isSubsumed(some.getFiller(), f.getOWLObjectIntersectionOf(endtype)))
		{
			System.out.println("         ENDTYPE MATCH " + some.getFiller() + " <= " + f.getOWLObjectIntersectionOf(endtype));
			match = true;
		}
		if (r.isSubsumed(f.getOWLObjectSomeValuesFrom(some.getProperty().getInverseProperty(), f.getOWLThing()), f.getOWLObjectIntersectionOf(endtype)))
		{
			System.out.println("         ENDARROW MATCH " + some.getProperty().getInverseProperty() + " <= " + f.getOWLObjectIntersectionOf(endtype));
			match = true;
		}
		if (!match)
			return false;
		
		for (OWLObjectPropertyExpression p: edges)
		{
			if (r.isSubsumed(some.getProperty(), p))
				System.out.println("         ROLE MATCH " + some.getProperty() + " <= " + p);
			else
			{
				System.out.println("         ROLE NOT MATCHED " + some.getProperty() + " !<= " + p);
				return false;
			}
		}
		System.out.println("         ALL MATCHED");
		return true;
	}
*/
	private Set<ClassDescription> getRootType(CQIE cqie, Set<Term> roots)
	{
		Set<ClassDescription> roottype = new HashSet<ClassDescription>();
		//System.out.println("         ROOTS " + roots);
		for (Atom a: cqie.getBody())
			if ((a.getArity() == 1) && roots.contains(a.getTerm(0)))
				roottype.add(ontFactory.createClass(a.getPredicate()));

		// TODO: reflexivity stuff
		
		//System.out.println("         ROOT TYPE " + roottype);
		return roottype;
	}
	
	
	
}
