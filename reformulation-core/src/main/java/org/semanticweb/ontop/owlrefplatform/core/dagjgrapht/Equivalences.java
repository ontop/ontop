package org.semanticweb.ontop.owlrefplatform.core.dagjgrapht;

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
