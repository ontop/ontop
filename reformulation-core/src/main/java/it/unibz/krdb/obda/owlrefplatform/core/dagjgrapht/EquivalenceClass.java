package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import java.util.Iterator;
import java.util.Set;

public class EquivalenceClass<T> implements Iterable<T> {
	
	final private Set<T> members;
	private T representative;

	public EquivalenceClass(Set<T> members) {
		this.members = members;
		this.representative = null;
	}
	
	public EquivalenceClass(Set<T> members, T representative) {
		this.members = members;
		this.representative = representative;
	}
	
	public void setRepresentative(T representative) {
		this.representative = representative;
	}
	
	public T getRepresentative() {
		return representative;
	}
	
	public Set<T> getMembers() {
		return members;
	}
	
	public int size() {
		return members.size();
	}
	
	public boolean isEmpty() {
		return members.isEmpty();
	}
	
	public boolean contains(T v) {
		return members.contains(v);
	}

	@Override
	public Iterator<T> iterator() {
		return members.iterator();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof EquivalenceClass<?>) {
			@SuppressWarnings("unchecked")
			EquivalenceClass<T> other = (EquivalenceClass<T>)o;
			return this.members.equals(other.members);
		}
		return false;
	}
	
	@Override 
	public int hashCode() {
		return members.hashCode();
	}
	
	@Override
	public String toString() {
		return "C[" + representative + ": " + members + "]";
	}
}
