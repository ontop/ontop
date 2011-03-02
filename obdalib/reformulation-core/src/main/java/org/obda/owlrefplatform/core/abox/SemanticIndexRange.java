package org.obda.owlrefplatform.core.abox;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

// FIXME: maybe should be renamed to Range

/**
 * Represents a set of continues intervals
 */
public class SemanticIndexRange {

	private List<Interval> intervals = new LinkedList<Interval>();

	public SemanticIndexRange() {

	}

	public SemanticIndexRange(int from, int to) {
		intervals.add(new Interval(from, to));
	}

	public void addInterval(int from, int to) {
		intervals.add(new Interval(from, to));
		merge();
	}

	public void addRange(SemanticIndexRange other) {
		for (Interval it : other.intervals) {
			intervals.add(it);
		}
		merge();
	}

	/**
	 * Sort in accending order and merge overlapping intervals
	 */
	private void merge() {
		List<Interval> new_intervals = new LinkedList<Interval>();

		Collections.sort(intervals);
		for (int i = 0; i < intervals.size() - 1; ++i) {
			Interval it = intervals.get(i);
			Interval it2 = intervals.get(i + 1);

			if (it.to + 1 >= it2.from) {
				if (it2.to >= it.to) {
					new_intervals.add(new Interval(it.from, it2.to));
				} else {
					new_intervals.add(it);
				}
			}
		}
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

	/**
	 * Continues interval between 2 points
	 * 
	 * @author Sergejs Pugacs
	 * 
	 */
	class Interval implements Comparable<Interval> {

		private int from, to;

		public Interval(int from, int to) {
			this.from = from;
			this.to = to;
		}

		@Override
		public boolean equals(Object other) {

			if (other == null)
				return false;
			if (other == this)
				return true;
			if (this.getClass() != other.getClass())
				return false;
			Interval otherInterval = (Interval) other;

			return (this.from == otherInterval.from && this.to == otherInterval.to);
		}

		@Override
		public int hashCode() {
			int result = 17;
			result += 37 * result + from;
			result += 37 * result + to;
			return result;
		}

		@Override
		public int compareTo(Interval o) {
			return this.from - o.from;
		};

		@Override
		public String toString() {
			return String.format("[%s:%s]", from, to);
		}

	}

}
