package it.unibz.krdb.obda.owlrefplatform.core.dag;


import java.io.Serializable;

/**
 * Continues interval between 2 points
 *
 * @author Sergejs Pugacs
 */
public class Interval implements Comparable<Interval>, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3982860811012207357L;
	final int start;
	final int end;

    public Interval(int start, int end) {
        this.start = start;
        this.end = end;
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

        return (this.start == otherInterval.start && this.end == otherInterval.end);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result += 37 * result + start;
        result += 37 * result + end;
        return result;
    }

    @Override
    public int compareTo(Interval o) {
        return this.start - o.start;
    }

    @Override
    public String toString() {
        return String.format("[%s:%s]", start, end);
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

}