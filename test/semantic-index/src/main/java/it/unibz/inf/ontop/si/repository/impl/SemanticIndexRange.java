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
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Comparator;
import java.util.stream.Stream;

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

    public ImmutableList<Interval> getIntervals() {
        return intervals;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return intervals.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SemanticIndexRange) {
            SemanticIndexRange other = (SemanticIndexRange) obj;
            return this.index == other.index && this.intervals.equals(other.intervals);
        }
        return false;
    }

    public SemanticIndexRange extendWith(SemanticIndexRange other) {

        ImmutableList<Interval> intervals = Stream.concat(this.intervals.stream(), other.getIntervals().stream())
                .sorted(Comparator.comparingInt(Interval::getStart))
                .collect(ImmutableCollectors.toList());

        // collapse overlapping intervals
        ImmutableList.Builder<Interval> builder = ImmutableList.builder();
        int min = intervals.get(0).getStart();
        int max = intervals.get(0).getEnd();
        for (int i = 1; i < intervals.size(); i++) {
            Interval item = intervals.get(i);
            if (item.getStart() > max + 1) {
                builder.add(new Interval(min, max));
                min = item.getStart();
            }
            max = Math.max(max, item.getEnd());
        }
        builder.add(new Interval(min, max));
        return new SemanticIndexRange(index, builder.build());
    }
}
