package it.unibz.krdb.obda.owlapi3.directmapping;

/*
 * #%L
 * ontop-obdalib-owlapi3
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

import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.TermUtils;
import it.unibz.krdb.obda.utils.JdbcTypeMapper;
import it.unibz.krdb.sql.Attribute;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.RelationDefinition;
import it.unibz.krdb.sql.ForeignKeyConstraint;
import it.unibz.krdb.sql.TableDefinition;
import it.unibz.krdb.sql.UniqueConstraint;

import java.util.*;

public class DirectMappingAxiom {
	private final DBMetadata metadata;
	private final RelationDefinition table;
	private String SQLString;
	private final String baseuri;
	private final OBDADataFactory df;
	
	public DirectMappingAxiom(String baseuri, RelationDefinition dd,
			DBMetadata obda_md, OBDADataFactory dfac) {
		this.table = dd;
		this.SQLString = "";
		this.metadata = obda_md;
		this.df = dfac;
		if (baseuri == null)
			throw new IllegalArgumentException("Base uri must be specified!");
		
		this.baseuri = baseuri;
	}


	public String getSQL() {
		String SQLStringTemple = "SELECT * FROM %s";

		SQLString = String.format(SQLStringTemple, "\"" + table.getName() + "\"");
		return SQLString;
	}

	public Map<String, CQIE> getRefAxioms() {
		HashMap<String, CQIE> refAxioms = new HashMap<>();
		List<ForeignKeyConstraint> fks = ((TableDefinition) table).getForeignKeys();
		for (ForeignKeyConstraint fk : fks) {
			refAxioms.put(getRefSQL(fk), getRefCQ(fk));
		}
		return refAxioms;
	}

	private String getRefSQL(ForeignKeyConstraint fk) {
		TableDefinition tableDef = ((TableDefinition) table);

		String SQLStringTempl = "SELECT %s FROM %s WHERE %s";

		String table = "\"" + this.table.getName() + "\"";
		String Table = table;
		String Column = "";
		String Condition = "";
		String tableRef = "";
		
		{
			UniqueConstraint pk = tableDef.getPrimaryKey();
			List<Attribute> attributes;		
			if (pk != null) 
				attributes = pk.getAttributes();
			else 
				attributes = tableDef.getAttributes();

			for (Attribute att : attributes) {
				String attrName = att.getName();
				Column += Table + ".\"" + attrName + "\" AS " + this.table.getName() + "_" + attrName + ", ";
			}
		}

		// referring object
		int count = 0;
		for (ForeignKeyConstraint.Component comp : fk.getComponents()) {
			Condition += table + ".\"" + comp.getAttribute().getName() + "\" = ";

			// get referenced object
			tableRef = comp.getReference().getRelation().getName();
			if (count == 0)
				Table += ", \"" + tableRef + "\"";
			String columnRef = comp.getReference().getName();
			Column += "\"" + tableRef + "\".\"" + columnRef + "\" AS "
					+ tableRef + "_" + columnRef;

			Condition += "\"" + tableRef + "\".\"" + columnRef + "\"";

			if (count < fk.getComponents().size() - 1) {
				Column += ", ";
				Condition += " AND ";
			}
			count++;
		}
		for (TableDefinition tdef : metadata.getTables()) {
			if (tdef.getName().equals(tableRef)) {
				UniqueConstraint pk = tdef.getPrimaryKey();
				if (pk != null) {
					for (Attribute att : pk.getAttributes()) {
						String pki = att.getName();
						String refPki = "\"" + tableRef + "\".\"" + pki + "\"";
						if (!Column.contains(refPki))
							Column += ", " + refPki + " AS " + tableRef + "_" + pki;
					}
				} 
				else {
					for (Attribute att : tdef.getAttributes()) {
						String attrName = att.getName();
						Column += ", \""+ tableRef + "\".\"" + attrName +
								"\" AS " + tableRef+"_" + attrName;
					}
				}
			}
		}
		
		return String.format(SQLStringTempl, Column, Table, Condition);
	}

	public CQIE getCQ(){
		Term sub = generateSubject((TableDefinition)table, false);
		List<Function> atoms = new ArrayList<Function>();
		
		//Class Atom
		atoms.add(df.getFunction(df.getClassPredicate(generateClassURI(table.getName())), sub));
		
		
		//DataType Atoms
		JdbcTypeMapper typeMapper = df.getJdbcTypeMapper();
		for (Attribute att : table.getAttributes()) {
			Predicate.COL_TYPE type = typeMapper.getPredicate(att.getType());
			if (type == COL_TYPE.LITERAL) {
				Variable objV = df.getVariable(att.getName());
				atoms.add(df.getFunction(
						df.getDataPropertyPredicate(generateDPURI(
								table.getName(), att.getName())), sub, objV));
			} 
			else {
				Function obj = df.getTypedTerm(df.getVariable(att.getName()), type);
				atoms.add(df.getFunction(
						df.getDataPropertyPredicate(generateDPURI(
								table.getName(), att.getName())), sub, obj));
			}
		}
	
		//To construct the head, there is no static field about this predicate
		List<Term> headTerms = new ArrayList<>(table.getAttributes().size());
		for (Attribute att : table.getAttributes())
			headTerms.add(df.getVariable(att.getName()));
		
		Predicate headPredicate = df.getPredicate("http://obda.inf.unibz.it/quest/vocabulary#q", headTerms.size());
		Function head = df.getFunction(headPredicate, headTerms);
		
		return df.getCQIE(head, atoms);
	}

	private CQIE getRefCQ(ForeignKeyConstraint fk) {

		Term sub = generateSubject((TableDefinition) table, true);

		ForeignKeyConstraint.Component fkcomp = fk.getComponents().get(0);
		
		// Object Atoms
		// Foreign key reference
		String pkTableReference = fkcomp.getReference().getRelation().getName();
		TableDefinition tdRef = (TableDefinition) metadata
				.getDefinition(pkTableReference);
		Term obj = generateSubject(tdRef, true);

		String opURI = generateOPURI(table.getName(), fkcomp.getAttribute());
		Function atom = df.getFunction(df.getObjectPropertyPredicate(opURI), sub, obj);

		// construct the head
		Set<Variable> headTermsSet = new HashSet<>();
		TermUtils.addReferencedVariablesTo(headTermsSet, atom);
		
		List<Term> headTerms = new ArrayList<>();
		headTerms.addAll(headTermsSet);

		Predicate headPredicate = df.getPredicate(
				"http://obda.inf.unibz.it/quest/vocabulary#q",
				headTerms.size());
		Function head = df.getFunction(headPredicate, headTerms);
		return df.getCQIE(head, atom);
	}

	// Generate an URI for class predicate from a string(name of table)
	private String generateClassURI(String table) {
		return baseuri + table;
	}

	/*
	 * Generate an URI for datatype property from a string(name of column) The
	 * style should be "baseuri/tablename#columnname" as required in Direct
	 * Mapping Definition
	 */
	private String generateDPURI(String table, String column) {
		return baseuri + percentEncode(table) + "#" + percentEncode(column);
	}

	// Generate an URI for object property from a string(name of column)
	private String generateOPURI(String table, Attribute columns) {
//		String columnsInFK = "";
//		for (Attribute a : columns)
//			if (a.isForeignKey() && a.getReference().getTableReference().equals(foreignTable.getName()))
//				columnsInFK += a.getName() + ";";
//		columnsInFK = columnsInFK.substring(0, columnsInFK.length() - 1);
		String columnsInFK = columns.getName();
		return baseuri + percentEncode(table) + "#ref-"  + columnsInFK;
	}

	/*
	 * Generate the subject term of the table
	 * 
	 * 
	 * TODO replace URI predicate to BNode predicate for tables without PKs in
	 * the following method after 'else'
	 */
	private Term generateSubject(TableDefinition td,
			boolean ref) {
		String tableName = "";
		if (ref)
			tableName = percentEncode(td.getName()) + "_";
		
		UniqueConstraint pk = td.getPrimaryKey();	
		if (pk != null) {
			List<Term> terms = new ArrayList<Term>(pk.getAttributes().size() + 1);
			terms.add(df.getConstantLiteral(subjectTemple(td)));
			for (Attribute att : pk.getAttributes()) {
				terms.add(df.getVariable(tableName + att.getName()));
			}
			return df.getUriTemplate(terms);

		} else {
			List<Term> vars = new ArrayList<>(td.getAttributes().size());
			for (Attribute att : td.getAttributes()) 
				vars.add(df.getVariable(tableName + att.getName()));

			return df.getBNodeTemplate(vars);
		}
	}

	
	private String subjectTemple(TableDefinition td) {
		/*
		 * It is hard to generate a uniform temple since the number of PK
		 * differs For example, the subject uri temple with one pk should be
		 * like: baseuri+tablename/PKcolumnname={}('col={}...) For table with
		 * more than one pk columns, there will be a ";" between column names
		 */

		String temp = baseuri + percentEncode(td.getName()) + "/";
		for (Attribute att : td.getPrimaryKey().getAttributes()) {
			//temp += percentEncode("{" + td.getPrimaryKeys().get(i).getName()) + "};";
			temp+=percentEncode(att.getName())+"={};";
		}
		// remove the last "." which is not neccesary
		temp = temp.substring(0, temp.length() - 1);
		// temp="\""+temp+"\"";
		return temp;
	}

	/*
	 * percent encoding for a String
	 */
	private String percentEncode(String pe) {
		pe = pe.replace("#", "%23");
		pe = pe.replace(".", "%2E");
		pe = pe.replace("-", "%2D");
		pe = pe.replace("/", "%2F");

		pe = pe.replace(" ", "%20");
		pe = pe.replace("!", "%21");
		pe = pe.replace("$", "%24");
		pe = pe.replace("&", "%26");
		pe = pe.replace("'", "%27");
		pe = pe.replace("(", "%28");
		pe = pe.replace(")", "%29");
		pe = pe.replace("*", "%2A");
		pe = pe.replace("+", "%2B");
		pe = pe.replace(",", "%2C");
		pe = pe.replace(":", "%3A");
		pe = pe.replace(";", "%3B");
		pe = pe.replace("=", "%3D");
		pe = pe.replace("?", "%3F");
		pe = pe.replace("@", "%40");
		pe = pe.replace("[", "%5B");
		pe = pe.replace("]", "%5D");
		return pe;
	}

}
