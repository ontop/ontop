/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */

package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import java.util.Iterator;
import java.util.Set;

public class Equivalences<T> implements Iterable<T> {
	
	final private Set<T> members;
	private T representative;
	private boolean isIndexed;

	public Equivalences(Set<T> members) {
		this(members, null);
	}
	
	public Equivalences(Set<T> members, T representative) {
		this.members = members;
		this.representative = representative;
		this.isIndexed = false;
	}
	
	public void setRepresentative(T representative) {
		this.representative = representative;
	}
	
	public T getRepresentative() {
		return representative;
	}
	
	public boolean isIndexed() {
		return isIndexed;
	}
	
	public void setIndexed() {
		isIndexed = true;
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
		if (o instanceof Equivalences<?>) {
			@SuppressWarnings("unchecked")
			Equivalences<T> other = (Equivalences<T>)o;
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
		return "C[" + (isIndexed ? "SI, " : "") + representative + ": " + members + "]";
	}
}
