package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SemanticIndexCache {

	public final static int CLASS_TYPE = 1;
	public final static int ROLE_TYPE = 2;
	
	
	private Map<OClass, List<Interval>> classIntervals = new HashMap<>();
	private Map<String, List<Interval>> roleIntervals = new HashMap<>();

	private Map<OClass, Integer> classIndexes = new HashMap<>();
	private Map<String, Integer> roleIndexes = new HashMap<>();

	private final TBoxReasoner reasonerDag; 
	
	public SemanticIndexCache(TBoxReasoner reasonerDag) {
		this.reasonerDag = reasonerDag;
		
		//create the indexes		
		SemanticIndexBuilder engine = new SemanticIndexBuilder(reasonerDag);
		
		
		/*
		 * Creating cache of semantic indexes and ranges
		 */
		// HACKY WAY -- TO BE CHANGED
		Set<Description> descriptions = engine.getIndexed(); // ((SemanticIndexEngineImpl)engine).getNamedDAG().vertexSet();
		for (Description description : descriptions) {
			if (description instanceof OClass) {

				OClass cdesc = (OClass) description;
				int idx = engine.getIndex(cdesc);
				List<Interval> intervals = engine.getIntervals(cdesc);

				classIndexes.put(cdesc, idx);
				classIntervals.put(cdesc, intervals);
			} 
			else if (description instanceof ObjectPropertyExpression) {
				ObjectPropertyExpression cdesc = (ObjectPropertyExpression) description;

				if (cdesc.isInverse()) {
					/* Inverses don't get indexes or intervals */
					continue;
				}

				int idx = engine.getIndex(cdesc);
				List<Interval> intervals = engine.getIntervals(cdesc);

				String iri = cdesc.getPredicate().getName();
				roleIndexes.put(iri, idx);
				roleIntervals.put(iri, intervals);
			} 
			else if (description instanceof DataPropertyExpression) {
				DataPropertyExpression cdesc = (DataPropertyExpression) description;

				int idx = engine.getIndex(cdesc);
				List<Interval> intervals = engine.getIntervals(cdesc);

				String iri = cdesc.getPredicate().getName();
				roleIndexes.put(iri, idx);
				roleIntervals.put(iri, intervals);
			} 
		}
	}

	/***
	 * Returns the index (semantic index) for a class or property. 
	 */

	public int getIndex(OClass c) {
		Integer index = classIndexes.get(c);
		
		if (index == null) {
			/* direct name is not indexed, maybe there is an equivalent */
			OClass equivalent = (OClass)reasonerDag.getClassDAG().getVertex(c).getRepresentative();
			return classIndexes.get(equivalent);
		}
				
		return index;
	}
	
	public int getIndex(ObjectPropertyExpression p) {
		String name = p.getPredicate().getName();
		Integer index = roleIndexes.get(name);
		
		if (index == null) {
			// direct name is not indexed, maybe there is an equivalent, we need to test
			// with object properties and data properties 
			ObjectPropertyExpression equivalent = reasonerDag.getObjectPropertyDAG().getVertex(p).getRepresentative();
			
			Integer index1 = roleIndexes.get(equivalent.getPredicate().getName());
			
			if (index1 != null)
				return index1;
			
			// TODO: object property equivalent failed, we now look for data property equivalent 
			
			System.out.println(name + " IN " + roleIndexes);
		}
		
		return index;				
	}

	public int getIndex(DataPropertyExpression p) {
		String name = p.getPredicate().getName();
		Integer index = roleIndexes.get(name);
		
		if (index == null) {
			// direct name is not indexed, maybe there is an equivalent, we need to test
			// with object properties and data properties 
			DataPropertyExpression equivalent = reasonerDag.getDataPropertyDAG().getVertex(p).getRepresentative();
			
			Integer index1 = roleIndexes.get(equivalent.getPredicate().getName());
			
			if (index1 != null)
				return index1;
			
			// TODO: object property equivalent failed, we now look for data property equivalent 
			
			System.out.println(name + " IN " + roleIndexes);
		}
		
		return index;				
	}
	
	public void setIndex(OClass concept, Integer idx) {
		classIndexes.put(concept, idx);
	}
		
	public void setRoleIndex(String iri, Integer idx) {
		roleIndexes.put(iri, idx);
	}

	/***
	 * Returns the intervals (semantic index) for a class or property. The String
	 * identifies a class if type = 1, identifies a property if type = 2.
	 * 
	 * @param name
	 * @param i
	 * @return
	 * @throws OBDAException 
	 */
	public List<Interval> getIntervals(OClass concept)  {
		List<Interval> intervals = classIntervals.get(concept);
		if (intervals == null)
			throw new RuntimeException("Could not create mapping for predicate: " + concept
					+ ". Couldn not find semantic index intervals for the predicate.");
		return intervals;
	}
	
	public List<Interval> getRoleIntervals(String name)  {
		
		List<Interval> intervals = roleIntervals.get(name);
		if (intervals == null)
			throw new RuntimeException("Could not create mapping for predicate: " + name
					+ ". Couldn not find semantic index intervals for the predicate.");
		return intervals;
		
	}
	
	public void setIntervals(OClass concept, List<Interval> intervals) {
		classIntervals.put(concept, intervals);
	}
	
	public void setRoleIntervals(String name, List<Interval> intervals) {
		roleIntervals.put(name, intervals);
	}
	
	public void clear() {
		classIndexes.clear();
		classIntervals.clear();
		roleIndexes.clear();
		roleIntervals.clear();		
	}


	/*
	 * these four methods are used only by SI Repository to save the metadata
	 */
	
	public Set<Entry<OClass, Integer>> getClassIndexEntries() {
		return classIndexes.entrySet();
	}
		
	public Set<Entry<String,Integer>> getRoleIndexEntries() {
		return roleIndexes.entrySet();
	}
	
	public Set<Entry<OClass, List<Interval>>> getClassIntervalsEntries() {
		return classIntervals.entrySet();
	}
	
	public Set<Entry<String, List<Interval>>> getRoleIntervalsEntries() {
		return roleIntervals.entrySet();
	}
}
