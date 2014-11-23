package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SemanticIndexCache {

	private final Map<OClass, SemanticIndexRange> classRanges = new HashMap<>();	
	private final Map<ObjectPropertyExpression, SemanticIndexRange> opeRanges = new HashMap<>();
	private final Map<DataPropertyExpression, SemanticIndexRange> dpeRanges = new HashMap<>();

	private final TBoxReasoner reasonerDag; 
	
	public SemanticIndexCache(TBoxReasoner reasonerDag) {
		this.reasonerDag = reasonerDag;
		
		//create the indexes		
		SemanticIndexBuilder engine = new SemanticIndexBuilder(reasonerDag);
		
		/*
		 * Creating cache of semantic indexes and ranges
		 */
		for (Entry<ClassExpression, SemanticIndexRange> description : engine.getIndexedClasses()) {
			OClass cdesc = (OClass)description.getKey();
			classRanges.put(cdesc, description.getValue());
		} 
		for (Entry<ObjectPropertyExpression, SemanticIndexRange> description : engine.getIndexedObjectProperties()) {
			opeRanges.put(description.getKey(), description.getValue());
		} 
		for (Entry<DataPropertyExpression, SemanticIndexRange> description : engine.getIndexedDataProperties()) {
			dpeRanges.put(description.getKey(), description.getValue());
		} 
	}

	/***
	 * Returns the index (semantic index) for a class or property. 
	 */

	public int getIndex(OClass c) {
		SemanticIndexRange range = classRanges.get(c);
		
		if (range == null) {
			/* direct name is not indexed, maybe there is an equivalent */
			OClass equivalent = (OClass)reasonerDag.getClassDAG().getVertex(c).getRepresentative();
			range = classRanges.get(equivalent);
		}
				
		return range.getIndex();
	}
	
	public int getIndex(ObjectPropertyExpression p) {
		
		SemanticIndexRange range = opeRanges.get(p);
		if (range == null) {
			// direct name is not indexed, maybe there is an equivalent, we need to test
			// with object properties and data properties 
			ObjectPropertyExpression equivalent = reasonerDag.getObjectPropertyDAG().getVertex(p).getRepresentative();
			if (equivalent.isInverse())
				range = opeRanges.get(equivalent.getInverse());			
			else
				range = opeRanges.get(equivalent);			
		}
		return range.getIndex();				
	}
	
	public boolean isIndexedObjectPropertyInverse(ObjectPropertyExpression ope) {
		ObjectPropertyExpression equivalent = reasonerDag.getObjectPropertyDAG().getVertex(ope).getRepresentative();
		return equivalent.isInverse(); 
	}

	public int getIndex(DataPropertyExpression p) {
		
		SemanticIndexRange range = dpeRanges.get(p);
		if (range == null) {
			// direct name is not indexed, maybe there is an equivalent, we need to test
			// with object properties and data properties 
			DataPropertyExpression equivalent = reasonerDag.getDataPropertyDAG().getVertex(p).getRepresentative();		
			range = dpeRanges.get(equivalent);
		}
		
		return range.getIndex();				
	}
	
	/***
	 * Returns the intervals (semantic index) for a class or property. The String
	 * identifies a class if type = 1, identifies a property if type = 2.
	 * 
	 * @param name
	 * @param i
	 * @return
	 */
	public List<Interval> getIntervals(OClass concept)  {
		SemanticIndexRange range = classRanges.get(concept);
		return range.getIntervals();
	}
	
	public List<Interval> getIntervals(ObjectPropertyExpression ope)  {	
		SemanticIndexRange range = opeRanges.get(ope);
		return range.getIntervals();
	}

	public List<Interval> getIntervals(DataPropertyExpression dpe)  {
		SemanticIndexRange range = dpeRanges.get(dpe);
		return range.getIntervals();
	}
	
	
	
	
	
	public void setIntervals(OClass concept, List<Interval> intervals) {
		classRanges.get(concept).setIntervals(intervals);
	}
	
	public void setIntervals(ObjectPropertyExpression ope, List<Interval> intervals) {
		opeRanges.get(ope).setIntervals(intervals);
	}
	
	public void setIntervals(DataPropertyExpression dpe, List<Interval> intervals) {
		dpeRanges.get(dpe).setIntervals(intervals);
	}

	public void setIndex(OClass concept, int idx) {
		classRanges.get(concept).setIndex(idx);
	}
		
	public void setIndex(ObjectPropertyExpression ope, Integer idx) {
		opeRanges.get(ope).setIndex(idx);
	}

	public void setIndex(DataPropertyExpression dpe, Integer idx) {
		dpeRanges.get(dpe).setIndex(idx);
	}
	
	
	public void clear() {
		classRanges.clear();
		opeRanges.clear();
		dpeRanges.clear();
	}


	/*
	 * these three methods are used only by SI Repository to save the metadata
	 */
	
	public Set<Entry<OClass, SemanticIndexRange>> getClassIndexEntries() {
		return classRanges.entrySet();
	}
		
	public Set<Entry<ObjectPropertyExpression, SemanticIndexRange>> getObjectPropertyIndexEntries() {
		return opeRanges.entrySet();
	}
	public Set<Entry<DataPropertyExpression, SemanticIndexRange>> getDataPropertyIndexEntries() {
		return dpeRanges.entrySet();
	}	
}
