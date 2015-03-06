package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.Predicate.COL_TYPE;

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


/***
 * A record to keep track of which tables in the semantic index tables have rows
 * in it. It allows allows to know the type of rows each has, as in, for which
 * indexes and which type of object.
 */

public class SemanticIndexViewID {
	
	private final COL_TYPE type1, type2;
	private final int hashCode;

	public SemanticIndexViewID(COL_TYPE type1, COL_TYPE type2) {
		this.type1 = type1;
		this.type2 = type2;
		this.hashCode  = type2.hashCode() ^ (type1.hashCode() << 16);
	}
	
	public SemanticIndexViewID(COL_TYPE type1) {
		this.type1 = type1;
		this.type2 = null;
		this.hashCode  = type1.hashCode();
	}

	public COL_TYPE getType1() {
		return type1;
	}

	public COL_TYPE getType2() {
		return type2;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SemanticIndexViewID))
			return false;
		
		SemanticIndexViewID r2 = (SemanticIndexViewID) obj;
		return this.type1 == r2.type1 && this.type2 == r2.type2;
	}
	

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(" T1: ");
		b.append(type1);
		if (type2 != null) {
			b.append(" T2: ");
			b.append(type2);
		}
		return b.toString();
	}
}
