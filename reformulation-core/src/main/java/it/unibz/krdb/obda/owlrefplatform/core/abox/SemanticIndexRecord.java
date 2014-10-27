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

import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.PropertyAssertion;
import it.unibz.krdb.obda.ontology.ClassAssertion;

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

	public enum OBJType {
		URI, BNode
	}

	public int idx;

	public OBJType type1 = null;

	public OBJType type2 = OBJType.URI;

	public SITable table = null;

	public SemanticIndexRecord(SITable table, OBJType t1, int idx) {
		this(table, t1, null, idx);
	}

	public SemanticIndexRecord(SITable table, OBJType t1, OBJType t2, int idx) {
		this.table = table;
		this.type1 = t1;
		this.type2 = t2;
		this.idx = idx;

	}

	public SemanticIndexRecord(int table, int t1, int t2, int idx) {

		for (SITable t : SITable.values()) {
			if (t.ordinal() == table) {
				this.table = t;
			}
		}
		if (this.table == null)
			throw new RuntimeException("Unknown table kind: " + table);
		
		
		for (OBJType t : OBJType.values()) {
			if (t.ordinal() == t1) {
				this.type1 = t;
			}
			if (t.ordinal() == t2) {
				this.type2 = t;
			}
		}
		if (this.type1 == null)
			throw new RuntimeException("Unknown object type1: " + t1);
				
		if (this.type2 == null)
			throw new RuntimeException("Unknown object type2: " + t2);
				
		this.idx = idx;

	}

	/***
	 * This hash will provide no colisions as long as the number of
	 * classes/properties is bellow
	 * 
	 */
	@Override
	public int hashCode() {
		int hash = 0;
		hash += (idx);
		hash += (table.ordinal() + 1 * 10000000);
		hash += (type1.ordinal() + 1 * 100000000);
		hash += (type2.ordinal() + 1 * 1000000000);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SemanticIndexRecord))
			return false;
		SemanticIndexRecord r2 = (SemanticIndexRecord) obj;
		return (this.idx == r2.idx) && (this.table == r2.table) && (this.type1 == r2.type1) && (this.type2 == r2.type2);
	}

	/**
	 * Gets the semantic index record for this assertion (that is, the kind of
	 * row that is inserted in the database).
	 * 
	 * @param aboxAssertion
	 * @return
	 */
	public static SemanticIndexRecord getRecord(Assertion assertion, int index) {
		OBJType t1 = null;
		OBJType t2 = OBJType.URI;
		SITable table = null;

		if (assertion instanceof ClassAssertion) {
			ClassAssertion ca = (ClassAssertion) assertion;
			table = SITable.CLASS;
			COL_TYPE atype1 = ca.getObject().getType();

			if (atype1 == COL_TYPE.BNODE) {
				t1 = OBJType.BNode;
			} else {
				t1 = OBJType.URI;
			}

		} else {
			PropertyAssertion ba = (PropertyAssertion) assertion;
			COL_TYPE atype1 = ba.getValue1().getType();

			if (atype1 == COL_TYPE.BNODE) {
				t1 = OBJType.BNode;
			} else {
				t1 = OBJType.URI;
			}

			COL_TYPE atype2 = ba.getValue2().getType();
			switch (atype2) {
			case OBJECT:
				t2 = OBJType.URI;
				table = SITable.OPROP;
				break;
			case BNODE:
				t2 = OBJType.BNode;
				table = SITable.OPROP;
				break;
			case LITERAL:
				table = SITable.DPROPLite;
				break;
			case LITERAL_LANG:
				table = SITable.DPROPLite;
				break;
			case STRING:
				table = SITable.DPROPStri;
				break;
			case INTEGER:
				table = SITable.DPROPInte;
				break;
            case INT:
                table = SITable.DPROPInt;
                break;
            case UNSIGNED_INT:
                table = SITable.DPROPUnsignedInt;
                break;
            case NEGATIVE_INTEGER:
                table = SITable.DPROPNegInte;
                break;
            case NON_NEGATIVE_INTEGER:
                table = SITable.DPROPNonNegInte;
                break;
            case POSITIVE_INTEGER:
                table = SITable.DPROPPosInte;
                break;
            case NON_POSITIVE_INTEGER:
                table = SITable.DPROPNonPosInte;
                break;
            case FLOAT:
                table = SITable.DPROPFloat;
                break;
            case LONG:
                table = SITable.DPROPLong;
                break;
			case DECIMAL:
				table = SITable.DPROPDeci;
				break;
			case DOUBLE:
				table = SITable.DPROPDoub;
				break;
			case DATETIME:
				table = SITable.DPROPDate;
				break;
			case BOOLEAN:
				table = SITable.DPROPBool;
				break;

			}

		}
		return new SemanticIndexRecord(table, t1, t2, index);
	}

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
