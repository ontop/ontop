package it.unibz.krdb.obda.owlapi3.directmapping;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.NewLiteral;
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

public class DirectMappingAxiom {
	protected DBMetadata obda_md;
	protected DataDefinition table;
	protected String SQLString;
	protected String baseuri;

	public DirectMappingAxiom() {
	}

	public DirectMappingAxiom(String baseuri, DataDefinition dd,
			DBMetadata obda_md) throws Exception {
		this.table = dd;
		this.SQLString = new String();
		this.obda_md = obda_md;
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

	public Map<String, CQIE> getRefAxioms(OBDADataFactory dfac) {
		HashMap<String, CQIE> refAxioms = new HashMap<String, CQIE>();
		Map<String, List<Attribute>> fks = ((TableDefinition) table)
				.getForeignKeys();
		if (fks.size() > 0) {
			Set<String> keys = fks.keySet();
			for (String key : keys) {
				refAxioms.put(getRefSQL(key), getRefCQ(key, dfac));
			}
		}
		return refAxioms;
	}

	private String getRefSQL(String key) {
		Map<String, List<Attribute>> fks = ((TableDefinition) table)
				.getForeignKeys();

		List<Attribute> pks = ((TableDefinition) table).getPrimaryKeys();

		String SQLStringTempl = new String("SELECT %s FROM %s WHERE %s");

		String table = new String("\"" + this.table.getName() + "\"");
		String Table = table;
		String Column = "";
		String Condition = "";

		for (Attribute pk : pks)
			Column += Table + ".\"" + pk.getName() + "\", ";

		// refferring object
		List<Attribute> attr = fks.get(key);
		for (int i = 0; i < attr.size(); i++) {
			Condition += table + ".\"" + attr.get(i).getName() + "\" = ";

			// get referenced object
			Reference ref = attr.get(i).getReference();
			String tableRef = ref.getTableReference();
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
		return (String.format(SQLStringTempl, Column, Table, Condition));

	}

	public CQIE getCQ(OBDADataFactory df){
		NewLiteral sub = generateSubject(df, (TableDefinition)table, false);
		List<Function> atoms = new ArrayList<Function>();
		
		//Class Atom
		atoms.add(df.getAtom(df.getClassPredicate(generateClassURI(table.getName())), sub));
		
		
		//DataType Atoms
		TypeMapper typeMapper = TypeMapper.getInstance();
		for(int i=0;i<table.countAttribute();i++){
			Attribute att = table.getAttribute(i+1);
			Predicate type = typeMapper.getPredicate(att.getType());
			if (type.equals(OBDAVocabulary.RDFS_LITERAL)) {
				Variable objV = df.getVariable(att.getName());
				atoms.add(df.getAtom(
						df.getDataPropertyPredicate(generateDPURI(
								table.getName(), att.getName())), sub, objV));
			} else {
				Function obj = df.getFunctionalTerm(type,
						df.getVariable(att.getName()));
				atoms.add(df.getAtom(
						df.getDataPropertyPredicate(generateDPURI(
								table.getName(), att.getName())), sub, obj));
			}
		}
	
		//To construct the head, there is no static field about this predicate
		List<NewLiteral> headTerms = new ArrayList<NewLiteral>();
		for(int i=0;i<table.countAttribute();i++){
			headTerms.add(df.getVariable(table.getAttributeName(i+1)));
		}
		Predicate headPredicate = df.getPredicate("http://obda.inf.unibz.it/quest/vocabulary#q", headTerms.size());
		Function head = df.getAtom(headPredicate, headTerms);
		
		
		return df.getCQIE(head, atoms);
	}

	private CQIE getRefCQ(String fk, OBDADataFactory df) {

		NewLiteral sub = generateSubject(df, (TableDefinition) table, true);
		Function atom = null;

		// Object Atoms
		// Foreign key reference
		for (int i = 0; i < table.countAttribute(); i++) {
			if (table.getAttribute(i + 1).isForeignKey()) {
				Attribute att = table.getAttribute(i + 1);
				Reference ref = att.getReference();
				if (ref.getReferenceName().equals(fk)) {
					String pkTableReference = ref.getTableReference();
					TableDefinition tdRef = (TableDefinition) obda_md
							.getDefinition(pkTableReference);
					NewLiteral obj = generateSubject(df, tdRef, true);

					atom = (df.getAtom(
							df.getObjectPropertyPredicate(generateOPURI(
									table.getName(), table.getAttributes())),
							sub, obj));

					// construct the head
					List<NewLiteral> headTerms = new ArrayList<NewLiteral>();
					headTerms.addAll(atom.getReferencedVariables());

					Predicate headPredicate = df.getPredicate(
							"http://obda.inf.unibz.it/quest/vocabulary#q",
							headTerms.size());
					Function head = df.getAtom(headPredicate, headTerms);
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
	private NewLiteral generateSubject(OBDADataFactory df, TableDefinition td,
			boolean ref) {
		String tableName = "";
		if (ref)
			tableName = percentEncode(td.getName()) + "_";

		if (td.getPrimaryKeys().size() > 0) {
			Predicate uritemple = df.getUriTemplatePredicate(td
					.getPrimaryKeys().size() + 1);
			List<NewLiteral> terms = new ArrayList<NewLiteral>();
			terms.add(df.getValueConstant(subjectTemple(td, td.getPrimaryKeys()
					.size())));
			for (int i = 0; i < td.getPrimaryKeys().size(); i++) {
				terms.add(df.getVariable(tableName
						+ td.getPrimaryKeys().get(i).getName()));
			}
			return df.getFunctionalTerm(uritemple, terms);

		} else {
			List<NewLiteral> vars = new ArrayList<NewLiteral>();
			for (int i = 0; i < td.countAttribute(); i++) {
				vars.add(df.getVariable(tableName + td.getAttributeName(i + 1)));
			}

			Predicate bNode = df.getBNodeTemplatePredicate(1);
			return df.getFunctionalTerm(bNode, vars);
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
