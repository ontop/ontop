package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Interval;

import java.util.List;
import java.util.Map;

/** 
 * An interface for the class SemanticIndexEngine that builds the indexes for the DAG
 * 
 */

public interface SemanticIndexEngine {


	//given the description returns the index
	public int getIndex(Description d);
	
	public void setIndex(Description d, int index);
	
	//obtain the list of intervals (with indexes of the descendants) for the given description
	public List<Interval> getIntervals(Description d);
	
	public void setRange(Description d, SemanticIndexRange range);
	
	//obtain the map with all the indexes
	public Map<Description, Integer> getIndexes();
	
	//obtain the map with all the intervals
	public Map<Description, SemanticIndexRange> getIntervals();

}
