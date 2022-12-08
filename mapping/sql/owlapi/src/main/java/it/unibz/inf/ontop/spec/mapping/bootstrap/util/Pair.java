package it.unibz.inf.ontop.spec.mapping.bootstrap.util;

/** @author Davide Lanti **/
public class Pair<T,S> {
    private T first;
    private S second;

    public Pair(T first, S second) {
        this.first = first;
        this.second = second;
    }

    public T first() {
        return this.first;
    }

    public S second() {
        return this.second;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true; // If they are the same object, then fine
        boolean result = false;
        if (other instanceof Pair) {
            Pair that = (Pair) other;
            result = (this.first.equals(that.first) && this.second.equals(that.second));
        }
        return result;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return (this.first.toString() + "->" + this.second.toString());
    }
};