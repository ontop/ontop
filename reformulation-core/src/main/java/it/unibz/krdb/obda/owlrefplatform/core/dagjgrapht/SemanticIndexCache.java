package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SemanticIndexCache {

	public final static int CLASS_TYPE = 1;
	public final static int ROLE_TYPE = 2;
	
	
	private Map<String, List<Interval>> classIntervals = new HashMap<String, List<Interval>>();
	private Map<String, List<Interval>> roleIntervals = new HashMap<String, List<Interval>>();

	private Map<String, Integer> classIndexes = new HashMap<String, Integer>();
	private Map<String, Integer> roleIndexes = new HashMap<String, Integer>();

	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	private final TBoxReasoner reasonerDag; 
	
	public SemanticIndexCache(TBoxReasoner reasonerDag) {
		this.reasonerDag = reasonerDag;
		
		//create the indexes		
		SemanticIndexBuilder engine = new SemanticIndexBuilder(reasonerDag);
		

		/***
		 * Copying the equivalences that might get lost from the translation
		 * Roman: this section is replaced by accessing DAG
		 */
		{
			// Map<Description,Description> isaEquivalences = pureIsa.getReplacements();
			//for (Description d : dag.getReplacementKeys()) {
				// commented out by Roman -- NamedDag Replacements are inherited from DAG
				// pureIsa.setReplacementFor(d, dag.getReplacementFor(d)); 
			//	equivalentIndex.put(d, dag.getReplacementFor(d));
			//}
			//pureIsa.setReplacements(isaEquivalences);  // commented out by Roman -- no need in resetting the variable
		}
		
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

				String iri = cdesc.getPredicate().getName();
				classIndexes.put(iri, idx);
				classIntervals.put(iri, intervals);

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
	 * Returns the index (semantic index) for a class or property. The String
	 * identifies a class if ype = 1, identifies a property if type = 2.
	 * 
	 * @param name
	 * @param i
	 * @return
	 */
	public int getIndex(String name, int type) {
		if (type == CLASS_TYPE) 
			return classIndexes.get(name);
		else if (type == ROLE_TYPE) 
			return roleIndexes.get(name);
		
		throw new RuntimeException("Could not find index for: String =  " + name + " type = " + type);
	}

	public int getIndex(OClass c) {
		String name = c.getPredicate().getName();
		Integer index = classIndexes.get(name);
		
		if (index == null) {
			/* direct name is not indexed, maybe there is an equivalent */
			OClass equivalent = (OClass)reasonerDag.getClassDAG().getVertex(c).getRepresentative();
			return classIndexes.get(equivalent.getPredicate().getName());
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
	
	public void setIndex(String iri, int type, Integer idx) {
		if (type == CLASS_TYPE) {
			classIndexes.put(iri, idx);
		} else if (type == ROLE_TYPE) {
			roleIndexes.put(iri, idx);
		} else {
			throw new RuntimeException("Error loading semantic index map. String type was " + type
					+ ". Expected 1 for class or 2 for property.");
		}		
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
		List<Interval> intervals = classIntervals.get(concept.getPredicate().getName());
		if (intervals == null)
			throw new RuntimeException("Could not create mapping for predicate: " + concept
					+ ". Couldn not find semantic index intervals for the predicate.");
		return intervals;
	}
	@Deprecated
	public List<Interval> getClassIntervals(String name)  {
		List<Interval> intervals = classIntervals.get(name);
		if (intervals == null)
			throw new RuntimeException("Could not create mapping for predicate: " + name
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
	
	public void setIntervals(String name, int type, List<Interval> intervals) {
		if (type == CLASS_TYPE) 
			classIntervals.put(name, intervals);
		
		else if (type == ROLE_TYPE) 
			roleIntervals.put(name, intervals);
		
		else 
			throw new RuntimeException("Error loading semantic index intervals. String type was " + type
					+ ". Expected 1 for class or 2 for property.");
		
	}
	
	public void clear() {
		classIndexes.clear();
		classIntervals.clear();
		roleIndexes.clear();
		roleIntervals.clear();		
	}


	public Set<String> getIndexKeys(int type) {
		if (type == CLASS_TYPE)
			return classIndexes.keySet();
		
		else if (type == ROLE_TYPE)
			return roleIndexes.keySet();
		
		throw new RuntimeException("Wrong type: " + type);
	}
	
	public Set<String> getIntervalsKeys(int type) {
		if (type == CLASS_TYPE)
			return classIntervals.keySet();
		
		else if (type == ROLE_TYPE)
			return roleIntervals.keySet();
		
		throw new RuntimeException("Wrong type: " + type);
	}
	
}
