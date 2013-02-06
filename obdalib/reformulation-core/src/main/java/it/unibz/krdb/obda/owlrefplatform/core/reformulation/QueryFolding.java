package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryConnectedComponent.Edge;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryConnectedComponent.Loop;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.TreeWitnessReasonerLite.IntersectionOfConceptSets;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.TreeWitnessReasonerLite.IntersectionOfProperties;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.TreeWitnessSet.PropertiesCache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryFolding {
	private final PropertiesCache propertiesCache;
	
	private IntersectionOfProperties properties; 
	private Set<Loop> roots; 
	private IntersectionOfConceptSets internalRootConcepts;
	private Set<NewLiteral> internalRoots;
	private Set<NewLiteral> internalDomain;
	private List<TreeWitness> interior;
	private TreeWitness.NewLiteralCover NewLiterals;
	private boolean status;

	private static final Logger log = LoggerFactory.getLogger(QueryFolding.class);
		
	public QueryFolding(PropertiesCache propertiesCache) {
		this.propertiesCache = propertiesCache;
		properties = new IntersectionOfProperties(); 
		roots = new HashSet<Loop>(); 
		internalRootConcepts = new IntersectionOfConceptSets(); 
		internalRoots = new HashSet<NewLiteral>();
		internalDomain = new HashSet<NewLiteral>();
		interior = Collections.EMPTY_LIST; // in-place QueryFolding for one-step TreeWitnesses, 
		                                   //             which have no interior TreeWitnesses
		status = true;
	}
	
	public QueryFolding(QueryFolding qf) {
		this.propertiesCache = qf.propertiesCache;

		properties = new IntersectionOfProperties(qf.properties.get()); 
		roots = new HashSet<Loop>(qf.roots); 
		internalRootConcepts = new IntersectionOfConceptSets(qf.internalRootConcepts.get()); 
		internalRoots = new HashSet<NewLiteral>(qf.internalRoots);
		internalDomain = new HashSet<NewLiteral>(qf.internalDomain);
		interior = new LinkedList<TreeWitness>(qf.interior);
		status = qf.status;
	}

	
	public QueryFolding extend(TreeWitness tw) {
		assert (status);
		QueryFolding c = new QueryFolding(this);
		c.internalRoots.addAll(tw.getRoots());
		c.internalDomain.addAll(tw.getDomain());
		c.interior.add(tw);
		if (!c.internalRootConcepts.intersect(tw.getRootConcepts()))
			c.status = false;
		return c;
	}
	
	public boolean extend(Loop root, Edge edge, Loop internalRoot) {
		assert(status);

		if (properties.intersect(propertiesCache.getEdgeProperties(edge, root.getTerm(), internalRoot.getTerm()))) 
			if (internalRootConcepts.intersect(propertiesCache.getLoopConcepts(internalRoot))) {
				roots.add(root);
				return true;
			}
			
		status = false;
		return false;
	}
	
	public void newOneStepFolding(NewLiteral t) {
		properties.clear();
		roots.clear();
		internalRootConcepts.clear(); 
		internalDomain = Collections.singleton(t);
		NewLiterals = null;
		status = true;		
	}

	public void newQueryFolding(TreeWitness tw) {
		properties.clear(); 
		roots.clear(); 
		internalRootConcepts = new IntersectionOfConceptSets(tw.getRootConcepts()); 
		internalRoots = new HashSet<NewLiteral>(tw.getRoots());
		internalDomain = new HashSet<NewLiteral>(tw.getDomain());
		interior = new LinkedList<TreeWitness>();
		interior.add(tw);
		NewLiterals = null;
		status = true;		
	}

	
	public Set<Property> getProperties() {
		return properties.get();
	}
	
	public boolean isValid() {
		return status;
	}
	
	public boolean hasRoot() { 
		return !roots.isEmpty();
	}
	
	public boolean canBeAttachedToAnInternalRoot(Loop t0, Loop t1) {
		return internalRoots.contains(t0.getTerm()) && !internalDomain.contains(t1.getTerm()) && !roots.contains(t1);
	}
	
	public Set<BasicClassDescription> getInternalRootConcepts() {
		return internalRootConcepts.get();
	}
	
	public Collection<TreeWitness> getInteriorTreeWitnesses() {
		return interior;
	}
	
	public TreeWitness.NewLiteralCover getTerms() {
		if (NewLiterals == null) {
			Set<NewLiteral> domain = new HashSet<NewLiteral>(internalDomain);
			Set<NewLiteral> rootNewLiterals = new HashSet<NewLiteral>();
			for (Loop l : roots)
				rootNewLiterals.add(l.getTerm());
			domain.addAll(rootNewLiterals);
			NewLiterals = new TreeWitness.NewLiteralCover(domain, rootNewLiterals);
		}
		return NewLiterals;
	}
	
	public TreeWitness getTreeWitness(Collection<TreeWitnessGenerator> twg, Collection<Edge> edges) {
		
		log.debug("NEW TREE WITNESS");
		log.debug("  PROPERTIES " + properties);
		log.debug("  ENDTYPE " + internalRootConcepts);

		IntersectionOfConceptSets rootType = new IntersectionOfConceptSets();

		Set<Function> rootAtoms = new HashSet<Function>();
		for (Loop root : roots) {
			rootAtoms.addAll(root.getAtoms());
			if (!root.isExistentialVariable()) { // if the variable is not quantified -- not mergeable
				rootType = IntersectionOfConceptSets.EMPTY;
				log.debug("  NOT MERGEABLE: " + root + " IS NOT QUANTIFIED");				
			}
		}
		
		// EXTEND ROOT ATOMS BY ALL-ROOT EDGES
		for (Edge edge : edges) {
			if (roots.contains(edge.getLoop0()) && roots.contains(edge.getLoop1())) {
				rootAtoms.addAll(edge.getBAtoms());
				rootType = IntersectionOfConceptSets.EMPTY;
				log.debug("  NOT MERGEABLE: " + edge + " IS WITHIN THE ROOTS");				
			}
		}
		
		log.debug("  ROOTTYPE " + rootAtoms);

		if (rootType.get() == null) // not empty 
			for (Loop root : roots) {
				if (!rootType.intersect(propertiesCache.getLoopConcepts(root))) { // empty intersection -- not mergeable
					log.debug("  NOT MERGEABLE: EMPTY ROOT CONCEPT");
					break;
				}
			}
		
		return new TreeWitness(twg, getTerms(), rootAtoms, rootType); 	
	}
}
