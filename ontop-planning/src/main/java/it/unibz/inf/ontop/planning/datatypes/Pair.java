package it.unibz.inf.ontop.planning.datatypes;

public class Pair<T,S> {
    public final T first;
    public final S second;

    public Pair(T first, S second){
	this.first = first;
	this.second = second;
    }

    @Override 
    public boolean equals(Object other) {
	boolean result = false;
	if (other instanceof Pair<?,?>) {
	    Pair<?,?> that = (Pair<?,?>) other;
	    result = (this.first == that.first && this.second == that.second);
	}
	return result;
    }

    @Override 
    public int hashCode() {
	return (41 * (41 + this.first.hashCode()) + this.second.hashCode());
    }

    public String toString(){
	return "["+first.toString()+", "+second.toString()+"]";
    }
};