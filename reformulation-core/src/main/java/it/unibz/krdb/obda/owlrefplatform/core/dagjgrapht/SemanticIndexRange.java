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

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a set of continues intervals
 */

public class SemanticIndexRange implements Serializable {

  
	private static final long serialVersionUID = 8420832314126437803L;
	private List<Interval> intervals = new LinkedList<Interval>();

    public SemanticIndexRange() {
    }

    public SemanticIndexRange(SemanticIndexRange range) {
        intervals = new LinkedList<Interval>(range.getIntervals());
    }

    public SemanticIndexRange(int start, int end) {
        intervals.add(new Interval(start, end));
    }

    public SemanticIndexRange addInterval(int start, int end) {
        intervals.add(new Interval(start, end));
        merge();

        return this;
    }

    public SemanticIndexRange addRange(SemanticIndexRange other) {

        if (this.intervals == other.intervals)
            return this;

        for (Interval it : other.intervals) {
            this.intervals.add(it);
        }
        merge();
        return this;
    }

    /**
     * Sort in ascending order and collapse overlapping intervals
     */
    private void merge() {

        Collections.sort(intervals);
        List<Interval> new_intervals = new LinkedList<Interval>();

        int min = intervals.get(0).start;
        int max = intervals.get(0).end;

        for (int i = 1; i < intervals.size(); ++i) {
            Interval item = intervals.get(i);
            if (item.end > max + 1 && item.start > max + 1) {
                new_intervals.add(new Interval(min, max));
                min = item.start;
            }
            max = (max > item.end) ? max : item.end;
        }
        new_intervals.add(new Interval(min, max));
        intervals = new_intervals;
    }

    @Override
    public boolean equals(Object other) {

        if (other == null)
            return false;
        if (other == this)
            return true;
        if (this.getClass() != other.getClass())
            return false;
        SemanticIndexRange otherRange = (SemanticIndexRange) other;

        return this.intervals.equals(otherRange.intervals);
    }

    @Override
    public String toString() {
        return intervals.toString();
    }

    public List<Interval> getIntervals() {
        return intervals;
    }

    public boolean contained(SemanticIndexRange other) {
        boolean[] otherContained = new boolean[other.intervals.size()];
        for (int i = 0; i < otherContained.length; ++i) {
            otherContained[i] = false;
        }

        for (Interval it1 : this.intervals) {

            for (int i = 0; i < other.intervals.size(); ++i) {
                Interval it2 = other.intervals.get(i);
                if ((it1.start <= it2.start) && (it1.end >= it2.end)) {
                    otherContained[i] = true;
                }
            }

        }

        for (boolean it : otherContained) {
            if (!it) {
                return false;
            }
        }
        return true;
    }

}
