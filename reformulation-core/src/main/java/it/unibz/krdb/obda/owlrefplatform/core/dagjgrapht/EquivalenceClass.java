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

	@Override
	public Iterator<T> iterator() {
		return members.iterator();
	}
}
