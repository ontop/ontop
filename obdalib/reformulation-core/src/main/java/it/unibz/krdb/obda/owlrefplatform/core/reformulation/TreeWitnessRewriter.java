package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.PropertySomeClassRestriction;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.xerces.util.URI;
import org.apache.xerces.util.URI.MalformedURIException;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class TreeWitnessRewriter {
	private OWLDataFactory f;
	private CQIE cqie;
	private List<OWLSubClassOfAxiom> axioms;
	private OWLReasoner reasoner;
	private Reasoner r;
	private static OBDADataFactory	fac	= OBDADataFactoryImpl.getInstance();

	public TreeWitnessRewriter(CQIE cqie, List<OWLSubClassOfAxiom> axioms, OWLDataFactory f, OWLReasoner reasoner)
	{
		this.cqie = cqie;
		this.axioms = axioms;
		this.f = f;
		this.reasoner = reasoner;
		r = new Reasoner(f, reasoner);
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

	public DatalogProgram getRewriting(Set<TreeWitness> tws) throws MalformedURIException
	{
		DatalogProgram q = fac.getDatalogProgram(); //(cqie.getHead().getPredicate());
		
		List<Atom> mainbody = new LinkedList<Atom>();
		int Q = 0;
		for (Atom a: cqie.getBody())
			if (a.getArity() == 2)
				mainbody.add(new Atom(getQName(a.getPredicate().getName(), ++Q), a.getTerms()));
		
		// if no binary predicates
		if (mainbody.size() == 0)
			for (Atom a: cqie.getBody())
				mainbody.add(new Atom(getExtName(a.getPredicate().getName()), a.getTerms()));			
		
		q.appendRule(fac.getCQIE(cqie.getHead(), mainbody));
	
		Q = 0;
		for (Atom a: cqie.getBody())
			if (a.getArity() == 2)
			{
				URI Qname = getQName(a.getPredicate().getName(), ++Q);
				// TODO: groups for pairs of terms
				List<Atom> group = new LinkedList<Atom>();
				group.add(new Atom(getExtName(a.getPredicate().getName()), a.getTerms()));
				for (Atom aa: cqie.getBody())
					if ((aa.getArity() == 1) && a.getTerms().contains(aa.getTerm(0)))
						group.add(new Atom(getExtName(aa.getPredicate().getName()), aa.getTerms()));
					
				q.apendRule(fac.getCQIE(fac.getAtom(Qname, a.getTerms()), group));
			
				for (TreeWitness tw: tws)
					if (tw.getDomain().containsAll(a.getTerms()))
					{
						// TREE WITNESS FORMULAS
						List<Atom> twf = new LinkedList<Atom>();
						List<Term> roots = new LinkedList<Term>(tw.getRoots());
						Term r0 = roots.get(0);
						twf.add(fac.getAtom(getExtName(tw.getGenerator()), r0));
						for (Term rt: roots)
							if (!rt.equals(roots.get(0)))
								twf.add(fac.getAtom(new URI("http://EQ"), rt, r0));
						for (OWLClassExpression c: tw.getRootType())
							twf.add(fac.getAtom(getExtName(new URI(c.asOWLClass().getIRI().toString())), r0));
						
						q.appendRule(fac.getCQIE(fac.getAtom(Qname, a.getTerms()), twf));
					}
			}
		
		Set<PropertySomeClassRestriction> gen = new HashSet<PropertySomeClassRestriction>();
		for (TreeWitness tw: tws)
			gen.add(tw.getGenerator());
		
		//System.out.println("\nEXT PREDICATES");
		for (CQIE ext: getExtPredicates(gen))
			q.appendRule(ext);
		
		return q;
	}
	
	
	public List<CQIE> getExtPredicates(Set<PropertySomeClassRestriction> gencon) throws MalformedURIException
	{
		List<CQIE> list = new LinkedList<CQIE>();
		Set<Predicate> exts = new HashSet<Predicate>();

		for (Atom a: cqie.getBody())
			exts.add(a.getPredicate());
				
		reasoner.precomputeInferences();

		
		// GENERATING CONCEPTS
		
		for (PropertySomeClassRestriction some: gencon)
		{
			URI uri = getExtName(some);
			Term x = new Variable("x");
			
			for (OWLClass c: r.getClasses())
				if (!c.isOWLNothing() && r.isSubsumed(c, some))
				{
					//System.out.println("EXT COMPUTE SUBCLASSES: " + c);
					list.add(getCQIE(new Atom(uri, x), new Atom(new URI(c.getIRI().toString()), x)));
				}
			
			{
				Term w = new Variable("w");
				Atom ra = getRoleAtom(some.getProperty(), x, w);
				exts.add(ra.getPredicate());
				ra.setPredicate(new Predicate(getExtName(ra.getPredicate().getName()), 2));
				//System.out.println(ra);
				if(r.isSubsumed(f.getOWLObjectSomeValuesFrom(some.getProperty(), f.getOWLThing()), some))
					list.add(getCQIE(new Atom(uri, x), ra));
				//else
				//{
				//	OWLClass ca = some.getFiller().asOWLClass();
				//	list.add(getCQIE(new Atom(uri, x), ra, new Atom(getExtName(new URI(ca.getIRI().toString())), x)));
				//	exts.add(new Predicate(new URI(ca.getIRI().toString()), 1));
				//}
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
				
		
		for (Predicate pred: exts)
		{
			if (pred.getArity() == 1)
			{
				URI ext = getExtName(pred.getName());
				OWLClass ac = f.getOWLClass(pred.getIRI());
				Term x = new Variable("x");
				//list.add(getCQIE(new Atom(ext, x), new Atom(a.getPredicate().getName(), x)));
				
				for (OWLClass c: r.getClasses())
					if (!c.isOWLNothing() && r.isSubsumed(c, ac))
						list.add(getCQIE(new Atom(ext, x), new Atom(new URI(c.getIRI().toString()), x)));
				
				for (OWLObjectPropertyExpression p: r.getProperties())
					if (!p.isOWLBottomObjectProperty() && r.isSubsumed(f.getOWLObjectSomeValuesFrom(p, f.getOWLThing()), ac))
						list.add(getCQIE(new Atom(ext, x), getRoleAtom(p, x, new Variable("w"))));						
			}
			else if (pred.getArity() == 2)
			{
				URI ext = getExtName(pred.getName());
				OWLObjectProperty pa = f.getOWLObjectProperty(pred.getIRI());
				Term x = new Variable("x");
				Term y = new Variable("y");
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
						list.add(getCQIE(new Atom(ext, x, y), getRoleAtom(p, x, y)));
					}
			}
		}
		
		return list;
	}
	
	private static URI getExtName(URI name) throws MalformedURIException 
	{
		String s = name.toString();
		URI uri = new URI(s);
		uri.setFragment("EXT_" + uri.getFragment());
		return uri;
	}

	private static URI getQName(URI name, int pos) throws MalformedURIException 
	{
		String s = name.toString();
		URI uri = new URI(s);
		uri.setFragment("Q_" + pos + "_" + uri.getFragment());
		return uri;
	}

	private static URI getExtName(PropertySomeClassRestriction some) throws MalformedURIException 
	{
		OWLObjectPropertyExpression p = some.getProperty();
		boolean inverse = false;
		if (p.isAnonymous())
		{
			p = p.getInverseProperty().getSimplified();
			inverse = true;
		}

		//System.out.println(p);
		URI filler = new URI(some.getFiller().asOWLClass().getIRI().toString());
		URI uri = new URI(p.asOWLObjectProperty().getIRI().toString());
		uri.setFragment("EXT_" + uri.getFragment() + (inverse ? "_I_" : "_") + filler.getFragment());
		return uri;
	}

	private static Atom getRoleAtom(OWLObjectPropertyExpression p, Term t1, Term t2) throws MalformedURIException
	{
		if (!p.isAnonymous())
		{
			if (!p.isOWLBottomObjectProperty())
				return new Atom(new URI(p.asOWLObjectProperty().getIRI().toString()), t1, t2);
		}
		else 
		{ 
			OWLObjectPropertyExpression pi = p.getInverseProperty().getSimplified();
			return new Atom(new URI(pi.asOWLObjectProperty().getIRI().toString()), t2, t1);
		}
		return null;
	}
	
	
	
	public Set<TreeWitness> getTreeWitnesses()
	{
		Set<TreeWitness> treewitnesses = new HashSet<TreeWitness>();
		
		for (Term v: cqie.getVariables())
		{
			System.out.println("VARIABLE " + v);
			if (cqie.getHead().getTerms().contains(v))
			{
				System.out.println("   IS FREE, SKIPPING");
				continue;
			}
			List<OWLObjectPropertyExpression> edges = new LinkedList<OWLObjectPropertyExpression>();
			Set<OWLClassExpression> endtype = new HashSet<OWLClassExpression>();
			Set<Term> roots = new HashSet<Term>();
			for (Atom a: cqie.getBody())
			{
				if ((a.getArity() == 1) && (a.getTerm(0).equals(v)))
				{	
					endtype.add(f.getOWLClass(IRI.create(a.getPredicate().getName().toString())));
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

			for (OWLSubClassOfAxiom a: axioms)
			{
				OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom)a.getSuperClass();
				if (isTreeWitness(some, roots, edges, endtype))
				{
					TreeWitness tw = new TreeWitness(some, roots, getRootType(roots), v);
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
		
		return treewitnesses;
	}
	
	
	private void saturateTreeWitnesses(Set<TreeWitness> treewitnesses, Set<TreeWitness> delta, Set<Term> roots, List<OWLObjectPropertyExpression> edges, Set<TreeWitness> tws)
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
			Set<OWLClassExpression> endtype = new HashSet<OWLClassExpression>();
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
				for (OWLSubClassOfAxiom a: axioms)
				{
					OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom)a.getSuperClass();
					if (isTreeWitness(some, roots, edges, endtype))
					{
						TreeWitness tw = new TreeWitness(some, roots, getRootType(roots), nonroots);
						System.out.println(tw);
						delta.add(tw);				
					}
				}
		}
	}
	
	
	private boolean isTreeWitness(PropertySomeClassRestriction some, Set<Term> roots, List<OWLObjectPropertyExpression> edges, Set<OWLClassExpression> endtype)
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

	private Set<OWLClassExpression> getRootType(Set<Term> roots)
	{
		Set<OWLClassExpression> roottype = new HashSet<OWLClassExpression>();
		//System.out.println("         ROOTS " + roots);
		for (Atom a1: cqie.getBody())
			if ((a1.getArity() == 1) && roots.contains(a1.getTerm(0)))
				roottype.add(f.getOWLClass(IRI.create(a1.getPredicate().getName().toString())));

		//System.out.println("         ROOT TYPE " + roottype);
		return roottype;
	}
	
	
	
	public static List<OWLSubClassOfAxiom> getGeneratingAxioms(OWLOntology o) 
	{
		List<OWLSubClassOfAxiom> axioms = new LinkedList<OWLSubClassOfAxiom>();
		System.out.println("AXIOMS");
		for (OWLAxiom ax: o.getAxioms())
		{
			if (ax.isOfType(AxiomType.ANNOTATION_ASSERTION) || 
				ax.isOfType(AxiomType.DECLARATION))
			{
				//System.out.println("Annotation: " + ax);
			}
			else if (ax.isOfType(AxiomType.SUBCLASS_OF))
			{
				OWLSubClassOfAxiom sax = (OWLSubClassOfAxiom)ax;
				OWLClassExpression sc = sax.getSuperClass();
				if (sc.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)
				{
					OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom)sc;
					System.out.println("property " + some.getProperty() + ", filler " + some.getFiller() + " from " + sax);
					axioms.add(sax);
				}
				else if (sc.getClassExpressionType() != ClassExpressionType.OWL_CLASS)
				{
					System.out.println("SUBCLASS OF " + sc + ": " + sax);
				}
			}
			else if (ax.isOfType(AxiomType.EQUIVALENT_CLASSES)) 
			{
				//System.out.println(ax);
				OWLEquivalentClassesAxiom sax = (OWLEquivalentClassesAxiom)ax;
				for (OWLClassExpression sc : sax.getClassExpressionsAsList())
				{
					if (sc.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)
					{
						OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom)sc;
						System.out.println("property " + some.getProperty() + ", filler " + some.getFiller() + " from " + sax);
						System.out.println("TO BE DONE!");
						//axioms.add(sax);
					}
					else if (sc.getClassExpressionType() != ClassExpressionType.OWL_CLASS)
					{
						System.out.println("SUBCLASS OF " + sc + ": " + sax);
					}
				}
			}
			else if (ax.isOfType(AxiomType.OBJECT_PROPERTY_DOMAIN) || 
					 ax.isOfType(AxiomType.OBJECT_PROPERTY_RANGE) ||
					 ax.isOfType(AxiomType.DATA_PROPERTY_DOMAIN) ||
					 ax.isOfType(AxiomType.DATA_PROPERTY_RANGE))
			{
				//System.out.println("DOMAIN/RANGE " + ax);
			}
			else if (ax.isOfType(AxiomType.TRANSITIVE_OBJECT_PROPERTY))
			{
			
			}
			else if (ax.isOfType(AxiomType.SUB_OBJECT_PROPERTY) || 
					 ax.isOfType(AxiomType.INVERSE_OBJECT_PROPERTIES))
			{
				
			}
			else if (ax.isOfType(AxiomType.CLASS_ASSERTION) || 
					 ax.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION))
			{
			}
			else if (ax.isOfType(AxiomType.DISJOINT_CLASSES))
			{
			}
			else
				System.out.println(ax);
		}
		return axioms;
	}
}
