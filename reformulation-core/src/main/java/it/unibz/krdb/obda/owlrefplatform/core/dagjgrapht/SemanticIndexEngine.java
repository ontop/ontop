package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Interval;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** 
 * An interface for the class SemanticIndexEngine that builds the indexes for the DAG
 * 
 */

public interface SemanticIndexEngine {


	//given the description returns the index
	public int getIndex(Description d);
	
	//obtain the list of intervals (with indexes of the descendants) for the given description
	public List<Interval> getIntervals(Description d);
	
	//obtain the set with all description with the indexes
	public Set<Description> getIndexed();
	
}
