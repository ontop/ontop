package it.unibz.inf.ontop.spec.ontology;

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


import com.google.common.collect.ImmutableSet;

import java.util.Iterator;
import java.util.stream.Stream;

public class Equivalences<T> implements Iterable<T> {
	
	final private ImmutableSet<T> members;
	// two mutables
	private T representative;
	private boolean isIndexed;

	public Equivalences(ImmutableSet<T> members) {
		this(members, null, false);
	}
	
	public Equivalences(ImmutableSet<T> members, T representative, boolean isIndexed) {
		this.members = members;
		this.representative = representative;
		this.isIndexed = isIndexed;
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
	
	public ImmutableSet<T> getMembers() {
		return members;
	}

	public Stream<T> stream() { return members.stream(); }
	
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
		if (o instanceof Equivalences) {
			Equivalences<?> other = (Equivalences<?>)o;
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
