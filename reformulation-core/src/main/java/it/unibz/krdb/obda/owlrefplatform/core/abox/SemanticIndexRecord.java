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

import java.util.HashMap;
import java.util.Map;

import it.unibz.krdb.obda.model.Predicate.COL_TYPE;

/***
 * A record to keep track of which tables in the semantic index tables have rows
 * in it. It allows allows to know the type of rows each has, as in, for which
 * indexes and which type of object.
 * 
 * @author mariano
 * 
 */
public class SemanticIndexRecord {

	public enum SITable {
		CLASS, OPROP, DPROPLite, DPROPStri, DPROPInte, DPROPLong, DPROPDeci, DPROPDoub, DPROPDate, DPROPInt, DPROPUnsignedInt, DPROPNegInte, DPROPNonNegInte, DPROPPosInte, DPROPNonPosInte, DPROPFloat, DPROPBool
	}

	private static final int OBJ_TYPE_URI = 0;
	private static final int OBJ_TYPE_BNode = 1;

	private final int idx;
	private final int type1;
	private final int type2;
	private final SITable table;

	public SemanticIndexRecord(SITable table, COL_TYPE t1, COL_TYPE t2, int idx) {
		this.type1 = (t1 == COL_TYPE.BNODE)  ? OBJ_TYPE_BNode : OBJ_TYPE_URI;
		this.type2 = (t2 == COL_TYPE.BNODE) ? OBJ_TYPE_BNode : OBJ_TYPE_URI;
		
		this.table = table;
		this.idx = idx;
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
		return table.ordinal();
	}
	
	public static SemanticIndexRecord createSIRecord(int table, int t1, int t2, int idx) {
		SITable sitable = null;		
		for (SITable t : SITable.values()) 
			if (t.ordinal() == table) 
				sitable = t;
		if (sitable == null)
			throw new RuntimeException("Unknown table kind: " + table);
		
		COL_TYPE tt2 = (t2 == OBJ_TYPE_BNode) ? COL_TYPE.BNODE : COL_TYPE.OBJECT;
		COL_TYPE tt1 = (t1 == OBJ_TYPE_BNode) ? COL_TYPE.BNODE : COL_TYPE.OBJECT;
		
		return new SemanticIndexRecord(sitable, tt1, tt2, idx);
	}

	/***
	 * This hash will provide no collisions as long as the number of
	 * classes/properties is bellow
	 * 
	 */
	@Override
	public int hashCode() {
		int hash = idx;
		hash += (table.ordinal() + 1) * 10000000;
		hash += (type1 + 1) * 100000000;
		hash += (type2 + 1) * 1000000000;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SemanticIndexRecord))
			return false;
		SemanticIndexRecord r2 = (SemanticIndexRecord) obj;
		return (this.idx == r2.idx) && (this.table == r2.table) && (this.type1 == r2.type1) && (this.type2 == r2.type2);
	}

	public static Map<COL_TYPE, SITable> COLTYPEtoSITable = new HashMap<>();
	
	static {
		COLTYPEtoSITable.put(COL_TYPE.OBJECT, SITable.OPROP);
		COLTYPEtoSITable.put(COL_TYPE.BNODE, SITable.OPROP);
		COLTYPEtoSITable.put(COL_TYPE.LITERAL, SITable.DPROPLite);
		COLTYPEtoSITable.put(COL_TYPE.LITERAL_LANG, SITable.DPROPLite);
		COLTYPEtoSITable.put(COL_TYPE.STRING, SITable.DPROPStri);
		COLTYPEtoSITable.put(COL_TYPE.INTEGER, SITable.DPROPInte);
		COLTYPEtoSITable.put(COL_TYPE.INT, SITable.DPROPInt);
		COLTYPEtoSITable.put(COL_TYPE.UNSIGNED_INT, SITable.DPROPUnsignedInt);
		COLTYPEtoSITable.put(COL_TYPE.NEGATIVE_INTEGER, SITable.DPROPNegInte);
		COLTYPEtoSITable.put(COL_TYPE.NON_NEGATIVE_INTEGER, SITable.DPROPNonNegInte);
		COLTYPEtoSITable.put(COL_TYPE.POSITIVE_INTEGER, SITable.DPROPPosInte);
		COLTYPEtoSITable.put(COL_TYPE.NON_POSITIVE_INTEGER, SITable.DPROPNonPosInte);
		COLTYPEtoSITable.put(COL_TYPE.FLOAT, SITable.DPROPFloat);
		COLTYPEtoSITable.put(COL_TYPE.LONG, SITable.DPROPLong);
		COLTYPEtoSITable.put(COL_TYPE.DECIMAL, SITable.DPROPDeci);
		COLTYPEtoSITable.put(COL_TYPE.DOUBLE, SITable.DPROPDoub);
		COLTYPEtoSITable.put(COL_TYPE.DATETIME, SITable.DPROPDate);
		COLTYPEtoSITable.put(COL_TYPE.BOOLEAN, SITable.DPROPBool);
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
