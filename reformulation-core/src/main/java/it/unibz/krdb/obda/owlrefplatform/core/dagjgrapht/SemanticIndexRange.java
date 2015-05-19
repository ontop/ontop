package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
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

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a set of contiguous intervals
 */

public class SemanticIndexRange implements Serializable {

	private static final long serialVersionUID = 8420832314126437803L;
	
	private List<Interval> intervals = new LinkedList<>();
	private int index;

	
	/**
	 * creates a Semantic Index range with the specified index 
	 * and a singleton interval [index, index]
	 * 
	 * @param index
	 */
	public SemanticIndexRange(int index) {
		this.index = index;
		intervals.add(new Interval(index, index));
	}
	
		
    public void addRange(List<Interval> other) {
        intervals.addAll(other);

        /**
         * Sort in ascending order and collapse overlapping intervals
         */

        Collections.sort(intervals);
        List<Interval> new_intervals = new LinkedList<>();

        int min = intervals.get(0).getStart();
        int max = intervals.get(0).getEnd();

        for (int i = 1; i < intervals.size(); ++i) {
            Interval item = intervals.get(i);
            if (item.getEnd() > max + 1 && item.getStart() > max + 1) {
                new_intervals.add(new Interval(min, max));
                min = item.getStart();
            }
            max = (max > item.getEnd()) ? max : item.getEnd();
        }
        new_intervals.add(new Interval(min, max));
        intervals = new_intervals;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SemanticIndexRange) {
        	SemanticIndexRange otherRange = (SemanticIndexRange) other;
        	return this.intervals.equals(otherRange.intervals);
        }
        return false;
    }

    @Override
    public String toString() {
        return intervals.toString();
    }

    public List<Interval> getIntervals() {
        return intervals;
    }
    
    public int getIndex() {
    	return index;
    }

    public boolean contained(SemanticIndexRange other) {
        boolean[] otherContained = new boolean[other.intervals.size()];
        for (int i = 0; i < otherContained.length; ++i) 
            otherContained[i] = false;

        for (Interval it1 : intervals) {
            for (int i = 0; i < other.intervals.size(); ++i) {
                Interval it2 = other.intervals.get(i);
                if ((it1.getStart() <= it2.getStart()) && (it1.getEnd() >= it2.getEnd())) {
                    otherContained[i] = true;
                }
            }
        }

        for (boolean it : otherContained) 
            if (!it) 
                return false;
      
        return true;
    }

	
}
