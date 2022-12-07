package it.unibz.inf.ontop.si.repository.impl;

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

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a set of contiguous intervals (IMMUTABLE)
 */

public class SemanticIndexRange {

	private final ImmutableList<Interval> intervals;
	private final int index;


	/**
	 * creates a Semantic Index range with the specified index 
	 * and a singleton interval [index, index]
	 * 
	 * @param index
	 */
	public SemanticIndexRange(int index) {
		this(index, ImmutableList.of(new Interval(index, index)));
	}

    private SemanticIndexRange(int index, ImmutableList<Interval> intervals) {
        this.index = index;
        this.intervals = intervals;
    }

    public SemanticIndexRange addRange(List<Interval> other) {

        List<Interval> intervals = new ArrayList<>(this.intervals);
        intervals.addAll(other);

        // Sort in ascending order and collapse overlapping intervals
        intervals.sort(Comparator.comparingInt(Interval::getStart));
        List<Interval> new_intervals = new ArrayList<>();

        int min = intervals.get(0).getStart();
        int max = intervals.get(0).getEnd();

        for (int i = 1; i < intervals.size(); ++i) {
            Interval item = intervals.get(i);
            if (item.getEnd() > max + 1 && item.getStart() > max + 1) {
                new_intervals.add(new Interval(min, max));
                min = item.getStart();
            }
            max = Math.max(max, item.getEnd());
        }
        new_intervals.add(new Interval(min, max));
        return new SemanticIndexRange(index, ImmutableList.copyOf(new_intervals));
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

    public ImmutableList<Interval> getIntervals() {
        return intervals;
    }

    public int getIndex() {
    	return index;
    }

	
}
