package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;

import java.util.HashMap;
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
	}
	
	public void buildSemanticIndexFromReasoner() {
		
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
	 * Returns the intervals (semantic index) for a class or property.
	 * 
	 * @param name
	 * @param i
	 * @return
	 */
	public SemanticIndexRange getEntry(OClass concept)  {
		return classRanges.get(concept);
	}
	
	public SemanticIndexRange getEntry(ObjectPropertyExpression ope)  {	
		return opeRanges.get(ope);
	}

	public SemanticIndexRange getEntry(DataPropertyExpression dpe)  {
		return dpeRanges.get(dpe);
	}
	
	
	
	
	public void setIndex(OClass concept, int idx) {
		SemanticIndexRange range = new SemanticIndexRange(idx);
		classRanges.put(concept, range);
	}
		
	public void setIndex(ObjectPropertyExpression ope, Integer idx) {
		SemanticIndexRange range = new SemanticIndexRange(idx);
		opeRanges.put(ope, range);
	}

	public void setIndex(DataPropertyExpression dpe, Integer idx) {
		SemanticIndexRange range = new SemanticIndexRange(idx);
		dpeRanges.put(dpe, range);
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
