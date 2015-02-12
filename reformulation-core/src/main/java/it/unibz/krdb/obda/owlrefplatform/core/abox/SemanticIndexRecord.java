package it.unibz.krdb.obda.owlrefplatform.core.abox;

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
 * 
 * @author mariano
 * 
 */
public class SemanticIndexRecord {

	private final int idx;
	private final int type1;
	private final int type2;
	private final int table;
	private final int hashCode;

	
	/**
	 * Constructor for SI Records taken from the database
	 * 
	 * NOTE: use the @cite{checkTypeValue} and @cite{checkSITableValue} functions 
	 *       to ensure that type1, type2 and table have valid values
	 * 
	 * @param table
	 * @param type1
	 * @param type2
	 * @param idx
	 */
	
	public SemanticIndexRecord(int table, int type1, int type2, int idx) {
		this.type1 = type1; 	
		this.type2 = type2;  
		this.table = table;
		this.idx = idx;
		this.hashCode = idx + (table + 1) * 10000000 + (type1 + 1) * 100000000 + (type2 + 1) * 1000000000;
	}
	
	
	public int getIndex() {
		return idx;
	}
	
	public int getType1() {
		return type1;
	}

	public int getType2() {
		return type2;
	}
	
	public int getTable() {
		return table;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SemanticIndexRecord))
			return false;
		
		SemanticIndexRecord r2 = (SemanticIndexRecord) obj;
		return (this.idx == r2.idx) && (this.table == r2.table) && (this.type1 == r2.type1) && (this.type2 == r2.type2);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("T: ");
		b.append(table);
		b.append(" IDX: ");
		b.append(idx);
		b.append(" T1: ");
		b.append(type1);
		b.append(" T2: ");
		b.append(type2);
		return b.toString();
	}
}
