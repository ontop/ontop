package org.semanticweb.ontop.owlrefplatform.core.dagjgrapht;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.ontology.*;
import org.semanticweb.ontop.ontology.impl.OntologyFactoryImpl;

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

	public int getIndex(Predicate predicate, int type) {
		String name = predicate.getName();
		if (type == CLASS_TYPE) {
			Integer index = classIndexes.get(name);
		
			if (index == null) {
				/* direct name is not indexed, maybe there is an equivalent */
				OClass c = (OClass)ofac.createClass(name);
				OClass equivalent = (OClass)reasonerDag.getClasses().getVertex(c).getRepresentative();
				return classIndexes.get(equivalent.getPredicate().getName());
			}
					
			return index;
		} else if (type == ROLE_TYPE) {
			Integer index = roleIndexes.get(name);
			
			if (index == null) {
				/* direct name is not indexed, maybe there is an equivalent, we need to test
				 * with object properties and data properties */
				PropertyExpression c = ofac.createObjectProperty(name);
				PropertyExpression equivalent = reasonerDag.getProperties().getVertex(c).getRepresentative();
				
				Integer index1 = roleIndexes.get(equivalent.getPredicate().getName());
				
				if (index1 != null)
					return index1;
				
				/* object property equivalent failed, we now look for data property equivalent */
				
				c = ofac.createDataProperty(name);
				equivalent = reasonerDag.getProperties().getVertex(c).getRepresentative();
				
				index1 = roleIndexes.get(equivalent.getPredicate().getName());
				if (index1 == null) {
					System.out.println(name + " IN " + roleIndexes);
				}
				return index1;
			}
			
			return index;
				
		}
		throw new RuntimeException("Could not find index for: String =  " + name + " type = " + type);
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
	 */
	public List<Interval> getIntervals(String name, int type) {
		if (type == CLASS_TYPE) 
			return classIntervals.get(name);
		
		else if (type == ROLE_TYPE) 
			return roleIntervals.get(name);
		
		throw new RuntimeException("Could not find semantic index intervals for: String =  " + name + " type = " + type);
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
