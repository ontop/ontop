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

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.utils.TypeMapper;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DataDefinition;
import it.unibz.krdb.sql.Reference;
import it.unibz.krdb.sql.TableDefinition;
import it.unibz.krdb.sql.api.Attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DirectMappingAxiom {
	protected DBMetadata metadata;
	protected DataDefinition table;
	protected String SQLString;
	protected String baseuri;
	private OBDADataFactory df;

	public DirectMappingAxiom() {
	}

	public DirectMappingAxiom(String baseuri, DataDefinition dd,
			DBMetadata obda_md, OBDADataFactory dfac) throws Exception {
		this.table = dd;
		this.SQLString = new String();
		this.metadata = obda_md;
		this.df = dfac;
		if (baseuri != null)
			this.baseuri = baseuri;
		else
		{
			throw new Exception("Base uri must be specified!");
		}
	}

	public DirectMappingAxiom(DirectMappingAxiom dmab) {
		this.table = dmab.table;
		this.SQLString = new String(dmab.getSQL());
		this.baseuri = new String(dmab.getbaseuri());
	}

	public String getSQL() {
		String SQLStringTemple = new String("SELECT * FROM %s");

		SQLString = String.format(SQLStringTemple, "\"" + this.table.getName()
				+ "\"");
		return new String(SQLString);
	}

	public Map<String, CQIE> getRefAxioms() {
		HashMap<String, CQIE> refAxioms = new HashMap<String, CQIE>();
		Map<String, List<Attribute>> fks = ((TableDefinition) table)
				.getForeignKeys();
		if (fks.size() > 0) {
			Set<String> keys = fks.keySet();
			for (String key : keys) {
				refAxioms.put(getRefSQL(key), getRefCQ(key));
			}
		}
		return refAxioms;
	}

	private String getRefSQL(String key) {
		TableDefinition tableDef = ((TableDefinition) table);
		Map<String, List<Attribute>> fks = tableDef.getForeignKeys();

		List<Attribute> pks = tableDef.getPrimaryKeys();
		
		String SQLStringTempl = new String("SELECT %s FROM %s WHERE %s");

		String table = new String("\"" + this.table.getName() + "\"");
		String Table = table;
		String Column = "";
		String Condition = "";
		String tableRef = "";
		
		if (pks.size() > 0) {
		for (Attribute pk : pks)
			Column += Table + ".\"" + pk.getName() + "\" AS "+this.table.getName()+"_"+pk.getName()+", ";
		} else {
			for (int i = 0; i < tableDef.getNumOfAttributes(); i++) {
				String attrName = tableDef.getAttributeName(i + 1);
				Column += Table + ".\"" + attrName + "\" AS " + this.table.getName()+"_"+attrName +", ";
			}
		}

		// refferring object
		List<Attribute> attr = fks.get(key);
		for (int i = 0; i < attr.size(); i++) {
			Condition += table + ".\"" + attr.get(i).getName() + "\" = ";

			// get referenced object
			Reference ref = attr.get(i).getReference();
			tableRef = ref.getTableReference();
			if (i == 0)
				Table += ", \"" + tableRef + "\"";
			String columnRef = ref.getColumnReference();
			Column += "\"" + tableRef + "\".\"" + columnRef + "\" AS "
					+ tableRef + "_" + columnRef;

			Condition += "\"" + tableRef + "\".\"" + columnRef + "\"";

			if (i < attr.size() - 1) {
				Column += ", ";
				Condition += " AND ";
			}
		}
		for (TableDefinition tdef : metadata.getTableList()) {
			if (tdef.getName().equals(tableRef)) {
				int pknumber = tdef.getPrimaryKeys().size();
				if (pknumber > 0) {
					for (int i = 0; i < pknumber; i++) {
						String pki = tdef.getPrimaryKeys().get(i).getName();
						String refPki = "\"" + tableRef + "\".\"" + pki + "\"";
						if (!Column.contains(refPki))
							Column += ", " + refPki + " AS " + tableRef + "_" + pki;
					}
				} else {
					for (int i = 0; i < tdef.getNumOfAttributes(); i++) {
						String attrName = tdef.getAttributeName(i + 1);
						Column += ", \""+ tableRef + "\".\"" + attrName +
								"\" AS " + tableRef+"_"+attrName;
					}
				}
			}
		}
		
		return (String.format(SQLStringTempl, Column, Table, Condition));

	}

	public CQIE getCQ(){
		Term sub = generateSubject((TableDefinition)table, false);
		List<Function> atoms = new ArrayList<Function>();
		
		//Class Atom
		atoms.add(df.getFunction(df.getClassPredicate(generateClassURI(table.getName())), sub));
		
		
		//DataType Atoms
		TypeMapper typeMapper = TypeMapper.getInstance();
		for(int i=0;i<table.getNumOfAttributes();i++){
			Attribute att = table.getAttribute(i+1);
			Predicate type = typeMapper.getPredicate(att.getType());
			if (type.equals(df.getDataTypePredicateLiteral())) {
				Variable objV = df.getVariable(att.getName());
				atoms.add(df.getFunction(
						df.getDataPropertyPredicate(generateDPURI(
								table.getName(), att.getName())), sub, objV));
			} else {
				Function obj = df.getFunction(type,
						df.getVariable(att.getName()));
				atoms.add(df.getFunction(
						df.getDataPropertyPredicate(generateDPURI(
								table.getName(), att.getName())), sub, obj));
			}
		}
	
		//To construct the head, there is no static field about this predicate
		List<Term> headTerms = new ArrayList<Term>();
		for(int i=0;i<table.getNumOfAttributes();i++){
			headTerms.add(df.getVariable(table.getAttributeName(i+1)));
		}
		Predicate headPredicate = df.getPredicate("http://obda.inf.unibz.it/quest/vocabulary#q", headTerms.size());
		Function head = df.getFunction(headPredicate, headTerms);
		
		
		return df.getCQIE(head, atoms);
	}

	private CQIE getRefCQ(String fk) {

		Term sub = generateSubject((TableDefinition) table, true);
		Function atom = null;

		// Object Atoms
		// Foreign key reference
		for (int i = 0; i < table.getNumOfAttributes(); i++) {
			if (table.getAttribute(i + 1).isForeignKey()) {
				Attribute att = table.getAttribute(i + 1);
				Reference ref = att.getReference();
				if (ref.getReferenceName().equals(fk)) {
					String pkTableReference = ref.getTableReference();
					TableDefinition tdRef = (TableDefinition) metadata
							.getDefinition(pkTableReference);
					Term obj = generateSubject(tdRef, true);

					atom = (df.getFunction(
							df.getObjectPropertyPredicate(generateOPURI(
									table.getName(), table.getAttributes())),
							sub, obj));

					// construct the head
					List<Term> headTerms = new ArrayList<Term>();
					headTerms.addAll(atom.getReferencedVariables());

					Predicate headPredicate = df.getPredicate(
							"http://obda.inf.unibz.it/quest/vocabulary#q",
							headTerms.size());
					Function head = df.getFunction(headPredicate, headTerms);
					return df.getCQIE(head, atom);
				}
			}
		}
		return null;
	}

	// Generate an URI for class predicate from a string(name of table)
	private String generateClassURI(String table) {
		return new String(baseuri + table);

	}

	/*
	 * Generate an URI for datatype property from a string(name of column) The
	 * style should be "baseuri/tablename#columnname" as required in Direct
	 * Mapping Definition
	 */
	private String generateDPURI(String table, String column) {
		return new String(baseuri + percentEncode(table) + "#"
				+ percentEncode(column));
	}

	// Generate an URI for object property from a string(name of column)
	private String generateOPURI(String table, ArrayList<Attribute> columns) {
		String column = "";
		for (Attribute a : columns)
			if (a.isForeignKey())
				column += a.getName() + "_";
		column = column.substring(0, column.length() - 1);
		return new String(baseuri + percentEncode(table) + "#ref-" + column);
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

		if (td.getPrimaryKeys().size() > 0) {
			Predicate uritemple = df.getUriTemplatePredicate(td
					.getPrimaryKeys().size() + 1);
			List<Term> terms = new ArrayList<Term>();
			terms.add(df.getConstantLiteral(subjectTemple(td, td.getPrimaryKeys()
					.size())));
			for (int i = 0; i < td.getPrimaryKeys().size(); i++) {
				terms.add(df.getVariable(tableName
						+ td.getPrimaryKeys().get(i).getName()));
			}
			return df.getFunction(uritemple, terms);

		} else {
			List<Term> vars = new ArrayList<Term>();
			for (int i = 0; i < td.getNumOfAttributes(); i++) {
				vars.add(df.getVariable(tableName + td.getAttributeName(i + 1)));
			}

			Predicate bNode = df.getBNodeTemplatePredicate(1);
			return df.getFunction(bNode, vars);
		}
	}

	private String subjectTemple(TableDefinition td, int numPK) {
		/*
		 * It is hard to generate a uniform temple since the number of PK
		 * differs For example, the subject uri temple with one pk should be
		 * like: baseuri+tablename/PKcolumnname={}('col={}...) For table with
		 * more than one pk columns, there will be a ";" between column names
		 */

		String temp = new String(baseuri);
		temp += percentEncode(td.getName());
		temp += "/";
		for (int i = 0; i < numPK; i++) {
			//temp += percentEncode("{" + td.getPrimaryKeys().get(i).getName()) + "};";
			temp+=percentEncode(td.getPrimaryKeys().get(i).getName())+"={};";

		}
		// remove the last "." which is not neccesary
		temp = temp.substring(0, temp.length() - 1);
		// temp="\""+temp+"\"";
		return temp;
	}

	public String getbaseuri() {
		return baseuri;
	}

	public void setbaseuri(String uri) {
		if (uri != null)
			baseuri = new String(uri);
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
		return new String(pe);
	}

}
