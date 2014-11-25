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

import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.OntologyVocabularyImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.EquivalencesDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Interval;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.SemanticIndexCache;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.SemanticIndexRange;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

/**
 * Store ABox assertions in the DB
 * 
 */

public class RDBMSSIRepositoryManager implements Serializable {

	
	private static final long serialVersionUID = -6494667662327970606L;

	private final static Logger log = LoggerFactory.getLogger(RDBMSSIRepositoryManager.class);

	
	private static final class TableDescription {
		final String tableName;
		final String createCommand;
		final String insertCommand;
		
		final List<String> createIndexCommands = new ArrayList<String>(3);
		final List<String> dropIndexCommands = new ArrayList<String>(3);
		
		final String selectCommand;
		final String dropCommand;
		
		TableDescription(String tableName, String columnDefintions, String insertColumns, String select) {
			this.tableName = tableName;
			this.dropCommand = "DROP TABLE " + tableName;
			this.createCommand = "CREATE TABLE " + tableName + " ( " + columnDefintions + " )";
			this.insertCommand = "INSERT INTO " + tableName + " " + insertColumns;
			this.selectCommand = "SELECT " + select + " FROM " + tableName + " WHERE "; // REQUIRES CONDITION
		}
		
		void indexOn(String indexName, String columns) {
			createIndexCommands.add("CREATE INDEX " + indexName + " ON " + tableName + " (" + columns + ")");
			dropIndexCommands.add("DROP INDEX " + indexName);
		}
	}

	/**
	 * Metadata tables 
	 */
	
	private final static TableDescription indexTable = new TableDescription("IDX", 
			"URI VARCHAR(400), IDX INTEGER, ENTITY_TYPE INTEGER", 
			"(URI, IDX, ENTITY_TYPE) VALUES(?, ?, ?)", "*");   

	private final static TableDescription intervalTable = new TableDescription("IDXINTERVAL",
			"URI VARCHAR(400), IDX_FROM INTEGER, IDX_TO INTEGER, ENTITY_TYPE INTEGER",
			"(URI, IDX_FROM, IDX_TO, ENTITY_TYPE) VALUES(?, ?, ?, ?)", "*");

	private final static TableDescription uriIdTable = new TableDescription("URIID",
			"ID INTEGER, URI VARCHAR(400)",
			"(ID, URI) VALUES(?, ?)", "*");
	
	private final static TableDescription emptinessIndexTable = new TableDescription("NONEMPTYNESSINDEX",
			"TABLEID INTEGER, IDX INTEGER, TYPE1 INTEGER, TYPE2 INTEGER",
			"(TABLEID, IDX, TYPE1, TYPE2) VALUES (?, ?, ?, ?)", "*");
	
	
	
	
	/**
	 *  Data tables
	 */
	
	private final static TableDescription classTable = new TableDescription("QUEST_CLASS_ASSERTION", 
			"\"URI\" INTEGER NOT NULL, \"IDX\"  SMALLINT NOT NULL, ISBNODE BOOLEAN NOT NULL DEFAULT FALSE",
			"(URI, IDX, ISBNODE) VALUES (?, ?, ?)", "\"URI\" as X");
	
	private final static TableDescription roleTable = new TableDescription("QUEST_OBJECT_PROPERTY_ASSERTION", 
			"\"URI1\" INTEGER NOT NULL, \"URI2\" INTEGER NOT NULL, \"IDX\"  SMALLINT NOT NULL, " + 
			"ISBNODE BOOLEAN NOT NULL DEFAULT FALSE, ISBNODE2 BOOLEAN NOT NULL DEFAULT FALSE",
			"(URI1, URI2, IDX, ISBNODE, ISBNODE2) VALUES (?, ?, ?, ?, ?)", "\"URI1\" as X, \"URI2\" as Y");

	private final static Map<COL_TYPE ,TableDescription> attributeTable = new HashMap<>();
	
	
	static {
				
		classTable.indexOn("idxclassfull", "URI, IDX, ISBNODE");
		classTable.indexOn("idxclassfull2", "URI, IDX");
		
		roleTable.indexOn("idxrolefull1", "URI1, URI2, IDX, ISBNODE, ISBNODE2");
		roleTable.indexOn("idxrolefull2",  "URI2, URI1, IDX, ISBNODE2, ISBNODE");
		roleTable.indexOn("idxrolefull22", "URI1, URI2, IDX");
	
		
		// COL_TYPE.LITERAL is special because of one extra attribute (LANG)
		
		TableDescription attributeTableLiteral = new TableDescription("QUEST_DATA_PROPERTY_LITERAL_ASSERTION",
				"\"URI\" INTEGER NOT NULL, VAL VARCHAR(1000) NOT NULL, LANG VARCHAR(20), \"IDX\"  SMALLINT NOT NULL, " + 
				"ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE ",
				"(URI, VAL, LANG, IDX, ISBNODE) VALUES (?, ?, ?, ?, ?)", "\"URI\" as X, VAL as Y, LANG as Z");
		attributeTable.put(COL_TYPE.LITERAL, attributeTableLiteral);
			
		attributeTableLiteral.indexOn("IDX_LITERAL_ATTRIBUTE" + "1", "URI");		
		attributeTableLiteral.indexOn("IDX_LITERAL_ATTRIBUTE" + "2", "IDX");
		attributeTableLiteral.indexOn("IDX_LITERAL_ATTRIBUTE" + "3", "VAL");				

		
		// all other datatypes from COL_TYPE are treated similarly
		
		Map<COL_TYPE, String> attribute_table = new HashMap<>();
		attribute_table.put(COL_TYPE.STRING, "QUEST_DATA_PROPERTY_STRING_ASSERTION");    // 1
		attribute_table.put(COL_TYPE.INTEGER, "QUEST_DATA_PROPERTY_INTEGER_ASSERTION");   // 2
		attribute_table.put(COL_TYPE.INT, "QUEST_DATA_PROPERTY_INT_ASSERTION");   // 3
		attribute_table.put(COL_TYPE.UNSIGNED_INT, "QUEST_DATA_PROPERTY_UNSIGNED_INT_ASSERTION");   // 4
		attribute_table.put(COL_TYPE.NEGATIVE_INTEGER, "QUEST_DATA_PROPERTY_NEGATIVE_INTEGER_ASSERTION");  // 5
		attribute_table.put(COL_TYPE.NON_NEGATIVE_INTEGER, "QUEST_DATA_PROPERTY_NON_NEGATIVE_INTEGER_ASSERTION"); // 6
		attribute_table.put(COL_TYPE.POSITIVE_INTEGER, "QUEST_DATA_PROPERTY_POSITIVE_INTEGER_ASSERTION");  // 7
		attribute_table.put(COL_TYPE.NON_POSITIVE_INTEGER, "QUEST_DATA_PROPERTY_NON_POSITIVE_INTEGER_ASSERTION");  // 8
		attribute_table.put(COL_TYPE.LONG, "QUEST_DATA_PROPERTY_LONG_ASSERTION");  // 9
		attribute_table.put(COL_TYPE.DECIMAL, "QUEST_DATA_PROPERTY_DECIMAL_ASSERTION"); // 10
		attribute_table.put(COL_TYPE.FLOAT, "QUEST_DATA_PROPERTY_FLOAT_ASSERTION");  // 11
		attribute_table.put(COL_TYPE.DOUBLE, "QUEST_DATA_PROPERTY_DOUBLE_ASSERTION"); // 12
		attribute_table.put(COL_TYPE.DATETIME, "QUEST_DATA_PROPERTY_DATETIME_ASSERTION"); // 13
		attribute_table.put(COL_TYPE.BOOLEAN,  "QUEST_DATA_PROPERTY_BOOLEAN_ASSERTION");  // 14
		
		
		Map<COL_TYPE, String> attribute_type = new HashMap<>();
		attribute_type.put(COL_TYPE.STRING, "VARCHAR(1000)");
		attribute_type.put(COL_TYPE.INTEGER, "BIGINT");
		attribute_type.put(COL_TYPE.INT, "INTEGER");
		attribute_type.put(COL_TYPE.NEGATIVE_INTEGER, "BIGINT");
		attribute_type.put(COL_TYPE.POSITIVE_INTEGER, "BIGINT");
		attribute_type.put(COL_TYPE.UNSIGNED_INT, "INTEGER");
		attribute_type.put(COL_TYPE.NON_POSITIVE_INTEGER, "BIGINT");
		attribute_type.put(COL_TYPE.NON_NEGATIVE_INTEGER, "BIGINT");
		attribute_type.put(COL_TYPE.LONG, "BIGINT");
		attribute_type.put(COL_TYPE.DECIMAL, "DECIMAL");
		attribute_type.put(COL_TYPE.DOUBLE, "DOUBLE PRECISION");
		attribute_type.put(COL_TYPE.FLOAT, "DOUBLE PRECISION");
		attribute_type.put(COL_TYPE.DATETIME, "TIMESTAMP");
		attribute_type.put(COL_TYPE.BOOLEAN, "BOOLEAN");

		Map<COL_TYPE, String> attribute_index = new HashMap<>();
		attribute_index.put(COL_TYPE.STRING, "IDX_STRING_ATTRIBUTE");
		attribute_index.put(COL_TYPE.INTEGER, "IDX_INTEGER_ATTRIBUTE");
		attribute_index.put(COL_TYPE.INT,  "XSD_INT_ATTRIBUTE");
		attribute_index.put(COL_TYPE.UNSIGNED_INT, "XSD_UNSIGNED_INT_ATTRIBUTE");
		attribute_index.put(COL_TYPE.NEGATIVE_INTEGER, "XSD_NEGATIVE_INTEGER_ATTRIBUTE");
		attribute_index.put(COL_TYPE.NON_NEGATIVE_INTEGER, "XSD_NON_NEGATIVE_INTEGER_ATTRIBUTE");
		attribute_index.put(COL_TYPE.POSITIVE_INTEGER, "XSD_POSITIVE_INTEGER_ATTRIBUTE");
		attribute_index.put(COL_TYPE.NON_POSITIVE_INTEGER, "XSD_NON_POSITIVE_INTEGER_ATTRIBUTE");
		attribute_index.put(COL_TYPE.FLOAT, "XSD_FLOAT_ATTRIBUTE");
		attribute_index.put(COL_TYPE.LONG, "IDX_LONG_ATTRIBUTE");
		attribute_index.put(COL_TYPE.DECIMAL, "IDX_DECIMAL_ATTRIBUTE");
		attribute_index.put(COL_TYPE.DOUBLE, "IDX_DOUBLE_ATTRIBUTE");
		attribute_index.put(COL_TYPE.DATETIME, "IDX_DATETIME_ATTRIBUTE");
		attribute_index.put(COL_TYPE.BOOLEAN, "IDX_BOOLEAN_ATTRIBUTE");

		
		for (COL_TYPE datatype : attribute_table.keySet()) {
			TableDescription table = new TableDescription(attribute_table.get(datatype),
					"\"URI\" INTEGER  NOT NULL, VAL " + attribute_type.get(datatype) + 
					", \"IDX\"  SMALLINT  NOT NULL, ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE",
					"(URI, VAL, IDX, ISBNODE) VALUES (?, ?, ?, ?)", "\"URI\" as X, VAL as Y");
			attributeTable.put(datatype, table);
			
			table.indexOn(attribute_index.get(datatype) + "1", "URI");		
			table.indexOn(attribute_index.get(datatype) + "2", "IDX");
			table.indexOn(attribute_index.get(datatype) + "3", "VAL");
		}
	}
	

	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	
	private final SemanticIndexURIMap uriMap = new SemanticIndexURIMap();
	
	private final TBoxReasoner reasonerDag;
	
	private SemanticIndexCache cacheSI;
	
	private boolean isIndexed;  // database index created

	private final HashSet<SemanticIndexRecord> nonEmptyEntityRecord = new HashSet<>();

	private final List<RepositoryChangedListener> changeList = new LinkedList<>();

	public RDBMSSIRepositoryManager(TBoxReasoner reasonerDag) {
		this.reasonerDag = reasonerDag;
		cacheSI = new SemanticIndexCache(reasonerDag);
		// this is an expensive an unnecessary operation in case the DB stored metadata
		cacheSI.buildSemanticIndexFromReasoner();
	}

	public void addRepositoryChangedListener(RepositoryChangedListener list) {
		this.changeList.add(list);
	}


	public SemanticIndexURIMap getUriMap() {
		return uriMap;
	}
	


	public void createDBSchema(Connection conn) throws SQLException {

		if (isDBSchemaDefined(conn)) {
			log.debug("Schema already exists. Skipping creation");
			return;
		}
		
		log.debug("Creating data tables");

		try (Statement st = conn.createStatement()) {
			st.addBatch(uriIdTable.createCommand);
			
			st.addBatch(indexTable.createCommand);
			st.addBatch(intervalTable.createCommand);
			st.addBatch(emptinessIndexTable.createCommand);

			st.addBatch(classTable.createCommand);
			st.addBatch(roleTable.createCommand);

			for (Entry<COL_TYPE, TableDescription> entry : attributeTable.entrySet())
				st.addBatch(entry.getValue().createCommand);
			
			st.executeBatch();			
		}
	}

	public void createIndexes(Connection conn) throws SQLException {
		log.debug("Creating indexes");
		try (Statement st = conn.createStatement()) {
			for (Entry<COL_TYPE, TableDescription> entry : attributeTable.entrySet())
				for (String s : entry.getValue().createIndexCommands)
					st.addBatch(s);
						
			for (String s : classTable.createIndexCommands)
				st.addBatch(s);
			
			for (String s : roleTable.createIndexCommands)
				st.addBatch(s);
			
			st.executeBatch();
			st.clearBatch();
			
			log.debug("Executing ANALYZE");
			st.addBatch("ANALYZE");
			st.executeBatch();
			
			isIndexed = true;
		}
	}

	
	
	public void dropDBSchema(Connection conn) throws SQLException {

		try (Statement st = conn.createStatement()) {
			st.addBatch(indexTable.dropCommand);
			st.addBatch(intervalTable.dropCommand);
			st.addBatch(emptinessIndexTable.dropCommand);

			st.addBatch(classTable.dropCommand);
			st.addBatch(roleTable.dropCommand);
			
			for (Entry<COL_TYPE, TableDescription> entry : attributeTable.entrySet())
				st.addBatch(entry.getValue().dropCommand); 
			
			st.addBatch(uriIdTable.dropCommand);

			st.executeBatch();
		}
	}

	public int insertData(Connection conn, Iterator<Assertion> data, int commitLimit, int batchLimit) throws SQLException {
		log.debug("Inserting data into DB");

		// The precondition for the limit number must be greater or equal to one.
		commitLimit = (commitLimit < 1) ? 1 : commitLimit;
		batchLimit = (batchLimit < 1) ? 1 : batchLimit;

		boolean oldAutoCommit = conn.getAutoCommit();
		conn.setAutoCommit(false);

		// Create the insert statement for all assertions (i.e, concept, role
		// and attribute)
		PreparedStatement uriidStm = conn.prepareStatement(uriIdTable.insertCommand);
		PreparedStatement classStm = conn.prepareStatement(classTable.insertCommand);
		PreparedStatement roleStm = conn.prepareStatement(roleTable.insertCommand);
		
		Map<COL_TYPE, PreparedStatement> attributeStm = new HashMap<COL_TYPE, PreparedStatement>();
		for (Entry<COL_TYPE, TableDescription> entry : attributeTable.entrySet()) {
			PreparedStatement stm = conn.prepareStatement(entry.getValue().insertCommand);
			attributeStm.put(entry.getKey(), stm);
		}
		
		// For counting the insertion
		int success = 0;
		Map<Predicate, Integer> failures = new HashMap<Predicate, Integer>();

		int batchCount = 0;
		int commitCount = 0;

		while (data.hasNext()) {
			Assertion ax = data.next();

			// log.debug("Inserting statement: {}", ax);
			batchCount++;
			commitCount++;


			if (ax instanceof ClassAssertion) {
				ClassAssertion ca = (ClassAssertion) ax; 
				try {
					process(ca, uriidStm, classStm);
				}
				catch (Exception e) {
					Predicate predicate = ca.getConcept().getPredicate();
					Integer counter = failures.get(predicate);
					if (counter == null) 
						counter = 0;
					failures.put(predicate, counter + 1);					
				}
			} 
			else if (ax instanceof ObjectPropertyAssertion) {
				ObjectPropertyAssertion opa = (ObjectPropertyAssertion)ax;
				try {
					process(opa, uriidStm, roleStm);	
				}
				catch (Exception e) {
					Predicate predicate = opa.getProperty().getPredicate();
					Integer counter = failures.get(predicate);
					if (counter == null) 
						counter = 0;
					failures.put(predicate, counter + 1);					
				}
			}
			else /* (ax instanceof DataPropertyAssertion) */ {
				DataPropertyAssertion dpa = (DataPropertyAssertion)ax;
				try {
					process(dpa, uriidStm, attributeStm);										
				}
				catch (Exception e) {
					Predicate predicate = dpa.getProperty().getPredicate();
					Integer counter = failures.get(predicate);
					if (counter == null) 
						counter = 0;
					failures.put(predicate, counter + 1);					
				}
			}

			// Check if the batch count is already in the batch limit
			if (batchCount == batchLimit) {
				uriidStm.executeBatch();
				uriidStm.clearBatch();
				roleStm.executeBatch();
				roleStm.clearBatch();;
				for (PreparedStatement stm : attributeStm.values()) {
					stm.executeBatch();
					stm.clearBatch();
				}
				classStm.executeBatch();
				classStm.clearBatch();
				batchCount = 0; // reset the counter
			}

			// Check if the commit count is already in the commit limit
			if (commitCount == commitLimit) {
				conn.commit();
				commitCount = 0; // reset the counter
			}
		}

		// Execute the rest of the batch
		uriidStm.executeBatch();
		uriidStm.clearBatch();
		roleStm.executeBatch();
		roleStm.clearBatch();;
		for (PreparedStatement stm : attributeStm.values()) {
			stm.executeBatch();
			stm.clearBatch();
		}
		classStm.executeBatch();
		classStm.clearBatch();

	
		// Close all open statements
		uriidStm.close();
		roleStm.close();
		for (PreparedStatement stm : attributeStm.values())
			stm.close();
		classStm.close();

		// Commit the rest of the batch insert
		conn.commit();

		conn.setAutoCommit(oldAutoCommit);

		// Print the monitoring log
		log.debug("Total successful insertions: " + success + ".");
		int totalFailures = 0;
		for (Predicate predicate : failures.keySet()) {
			int failure = failures.get(predicate);
			log.warn("Failed to insert data for predicate {} ({} tuples).", predicate, failure);
			totalFailures += failure;
		}
		if (totalFailures > 0) {
			log.warn("Total failed insertions: " + totalFailures + ". (REASON: datatype mismatch between the ontology and database).");
		}

		fireRepositoryChanged();

		return success;
	}

	private void fireRepositoryChanged() {
		for (RepositoryChangedListener listener : changeList) {
			listener.repositoryChanged();
		}
	}

	private void process(ObjectPropertyAssertion ax, PreparedStatement uriidStm, PreparedStatement roleStm) throws SQLException {

		ObjectPropertyExpression ope0 = ax.getProperty();
		if (ope0.isInverse()) 
			throw new RuntimeException("INVERSE PROPERTIES ARE NOT SUPPORTED IN ABOX:" + ax);
		
		// TODO: could use EquivalentTriplePredicateIterator instead
		
		ObjectPropertyExpression ope = reasonerDag.getObjectPropertyDAG().getVertex(ope0).getRepresentative();
				
		ObjectConstant o1, o2;
		if (ope.isInverse()) {
			// the canonical representative is inverse
			// and so, it is not indexed -- swap the arguments 
			// and replace the representative with its inverse 
			// (which must be indexed)
			o1 = ax.getObject();
			o2 = ax.getSubject();
			ope = ope.getInverse();
		}
		else {
			o1 = ax.getSubject();			
			o2 = ax.getObject();
		}

		int idx = cacheSI.getEntry(ope).getIndex();
		
		int uri_id = getObjectConstantUriId(o1, uriidStm);
		int uri2_id = getObjectConstantUriId(o2, uriidStm);
		
		// Construct the database INSERT statements		
		roleStm.setInt(1, uri_id);
		roleStm.setInt(2, uri2_id);		
		roleStm.setInt(3, idx);
		roleStm.setBoolean(4, o1 instanceof BNode);
		roleStm.setBoolean(5, o2 instanceof BNode);
		roleStm.addBatch();
		
		// Register non emptiness
		COL_TYPE t1 = o1.getType();
		COL_TYPE t2 = o2.getType();		
		SemanticIndexRecord record = new SemanticIndexRecord(t1, t2, idx);
		nonEmptyEntityRecord.add(record);
	} 

	private void process(DataPropertyAssertion ax, PreparedStatement uriidStm, Map<COL_TYPE, PreparedStatement> attributeStatement) throws SQLException {
		
		ObjectConstant subject = ax.getSubject();
		int uri_id = getObjectConstantUriId(subject, uriidStm);
		
		// replace the property by its canonical representative 
		DataPropertyExpression dpe0 = ax.getProperty();
		DataPropertyExpression dpe = reasonerDag.getDataPropertyDAG().getVertex(dpe0).getRepresentative();		
		int idx = cacheSI.getEntry(dpe).getIndex();

		ValueConstant object = ax.getValue();
		Predicate.COL_TYPE attributeType = object.getType();
		// special treatment for LITERAL_LANG
		if (attributeType == COL_TYPE.LITERAL_LANG)
			attributeType = COL_TYPE.LITERAL;
		
		// Construct the database INSERT statements
		PreparedStatement stm = attributeStatement.get(attributeType);
		if (stm == null) {
			// UNSUPPORTED DATATYPE
			log.warn("Ignoring assertion: {}", ax);			
			return;
		}
		stm.setInt(1, uri_id);
		
		String value = object.getValue();
		
		switch (attributeType) {
			case LITERAL:  // 0
				stm.setString(2, value);
				stm.setString(3, object.getLanguage());
				break;  
			case STRING:   // 1
				stm.setString(2, value);
				break;
	        case INT:   // 3
	            if (value.charAt(0) == '+')
	                value = value.substring(1, value.length());
	        	stm.setInt(2, Integer.parseInt(value));
	            break;
	        case UNSIGNED_INT:  // 4
	        	stm.setInt(2, Integer.parseInt(value));
	            break;
	        case INTEGER:  // 2
	        case NEGATIVE_INTEGER:   // 5
	        case POSITIVE_INTEGER:   // 6
	        case NON_NEGATIVE_INTEGER: // 7
	        case NON_POSITIVE_INTEGER: // 8
	        case LONG: // 10
	            if (value.charAt(0) == '+')
	                value = value.substring(1, value.length());
	            stm.setLong(2, Long.parseLong(value));
	            break;
	        case FLOAT: // 9
				stm.setDouble(2, Float.parseFloat(value));
	            break;
			case DOUBLE: // 12
				stm.setDouble(2, Double.parseDouble(value));
				break;
			case DECIMAL: // 11
				stm.setBigDecimal(2, new BigDecimal(value));
				break;
			case DATETIME: // 13
				stm.setTimestamp(2, parseTimestamp(value));
				break;
			case BOOLEAN: // 14
				// PostgreSQL abbreviates the boolean value to 't' and 'f'
				if (value.equalsIgnoreCase("t") || value.equals("1")) 
					value = "true";
				else if (value.equalsIgnoreCase("f") || value.equals("0")) 
					value= "false";
			
				stm.setBoolean(2, Boolean.parseBoolean(value));
				break;
			default:
		}
		
		boolean c1isBNode = subject instanceof BNode;
		if (attributeType == COL_TYPE.LITERAL) {
			stm.setInt(4, idx);
			stm.setBoolean(5, c1isBNode);			
		}
		else {
			stm.setInt(3, idx);
			stm.setBoolean(4, c1isBNode);			
		}
		stm.addBatch();
		
		// register non-emptiness
		COL_TYPE t1 = subject.getType();
		COL_TYPE t2 = object.getType();		
		SemanticIndexRecord record = new SemanticIndexRecord(t1, t2, idx);
		nonEmptyEntityRecord.add(record);
	}
	
		
	private void process(ClassAssertion ax, PreparedStatement uriidStm, PreparedStatement classStm) throws SQLException {
		
		ObjectConstant c1 = ax.getIndividual();

		int uri_id = getObjectConstantUriId(c1, uriidStm); 

		// replace concept by the canonical representative (which must be a concept name)
		OClass concept0 = ax.getConcept();
		OClass concept = (OClass)reasonerDag.getClassDAG().getVertex(concept0).getRepresentative();	
		int conceptIndex = cacheSI.getEntry(concept).getIndex();	

		// Construct the database INSERT statements
		classStm.setInt(1, uri_id);
		classStm.setInt(2, conceptIndex);
		classStm.setBoolean(3, c1 instanceof BNode);
		classStm.addBatch();
	
		// Register non emptiness
		COL_TYPE t1 = c1.getType();
		SemanticIndexRecord record = new SemanticIndexRecord(t1, conceptIndex);
		nonEmptyEntityRecord.add(record);
	}

	// TODO: big issue -- URI map is incomplete -- it is never read back from the DB
	
	// TODO: use database to get the maximum URIId
	private int maxURIId = -1;
	
	private int getObjectConstantUriId(ObjectConstant c, PreparedStatement uriidStm) throws SQLException {
		
		// TODO (ROMAN): I am not sure this is entirely correct for blank nodes
		String uri = (c instanceof BNode) ? ((BNode) c).getName() : ((URIConstant) c).getURI().toString();

		int uri_id = uriMap.getId(uri);
		if (uri_id < 0) {
			uri_id = maxURIId + 1;
			uriMap.set(uri, uri_id);			
			maxURIId++;		
			
			// Construct the database INSERT statement
			uriidStm.setInt(1, uri_id);
			uriidStm.setString(2, uri);
			uriidStm.addBatch();								
		}
			
		return uri_id;
	}
	





	private static final String[] formatStrings = { 
				"yyyy-MM-dd HH:mm:ss.SS", 
				"yyyy-MM-dd HH:mm:ss.S", 
				"yyyy-MM-dd HH:mm:ss", 
				"yyyy-MM-dd",
				"yyyy-MM-dd'T'HH:mm:ssz" };
	
	private static Timestamp parseTimestamp(String lit) {

		for (String formatString : formatStrings) {
			try {
				long time = new SimpleDateFormat(formatString).parse(lit).getTime();
				Timestamp ts = new Timestamp(time);
				return ts;
			} 
			catch (ParseException e) {
			}
		}
		return null; // the string can't be parsed to one of the datetime
						// formats.
	}

	// Attribute datatype from TBox
	/*
	private COL_TYPE getAttributeType(Predicate attribute) {
		DataPropertyExpression prop = ofac.createDataProperty(attribute.getName());
		DataPropertyRangeExpression role = prop.getRange(); 
		Equivalences<DataRangeExpression> roleNode = reasonerDag.getDataRangeDAG().getVertex(role);
		Set<Equivalences<DataRangeExpression>> ancestors = reasonerDag.getDataRangeDAG().getSuper(roleNode);

		for (Equivalences<DataRangeExpression> node : ancestors) {
			for(DataRangeExpression desc: node)
			{
				if (desc instanceof Datatype) {
					Datatype datatype = (Datatype) desc;
					return datatype.getPredicate().getType(0); // TODO Put some
																// check for
																// multiple types
				}
			}
		}
		return COL_TYPE.LITERAL;
	}
	*/

	
	public final static int CLASS_TYPE = 1;
	public final static int ROLE_TYPE = 2;
	
	private void setIndex(String iri, int type, int idx) {
		if (type == CLASS_TYPE) {
			OClass c = ofac.createClass(iri);
			if (reasonerDag.getClassDAG().getVertex(c) == null) 
				throw new RuntimeException("UNKNOWN CLASS: " + iri);
			
			if (cacheSI.getEntry(c) != null)
				throw new RuntimeException("DUPLICATE CLASS INDEX: " + iri);
			
			cacheSI.setIndex(c, idx);
		}
		else {
			ObjectPropertyExpression ope = ofac.createObjectProperty(iri);
			if (reasonerDag.getObjectPropertyDAG().getVertex(ope) != null) {
				//
				// a bit elaborate logic is a consequence of using the same type for
				// both object and data properties (which can have the same name)
				// according to the SemanticIndexBuilder, object properties are indexed first 
				// (and have lower indexes), and only then data properties are indexed
				// so, the first occurrence is an object property, 
				// and the second occurrence is a datatype property
				// (here we use the fact that the query result is sorted by idx)
				//
				if (cacheSI.getEntry(ope) != null)  {
					DataPropertyExpression dpe = ofac.createDataProperty(iri);
					if (reasonerDag.getDataPropertyDAG().getVertex(dpe) != null) {
						if (cacheSI.getEntry(dpe) != null)
							throw new RuntimeException("DUPLICATE PROPERTY: " + iri);
						
						cacheSI.setIndex(dpe, idx);
					}	
					else
						throw new RuntimeException("UNKNOWN PROPERTY: " + iri);
				}
				else 
					cacheSI.setIndex(ope, idx);
			}
			else {
				DataPropertyExpression dpe = ofac.createDataProperty(iri);
				if (reasonerDag.getDataPropertyDAG().getVertex(dpe) != null) {
					if (cacheSI.getEntry(dpe) != null)
						throw new RuntimeException("DUPLICATE PROPERTY: " + iri);
					
					cacheSI.setIndex(dpe, idx);
				}	
				else
					throw new RuntimeException("UNKNOWN PROPERTY: " + iri);
			}
		}		
	}
	
	private void setIntervals(String iri, int type, List<Interval> intervals, int maxObjectPropertyIndex) {
		
		SemanticIndexRange range;
		if (type == CLASS_TYPE) {
			OClass c = ofac.createClass(iri);
			range = cacheSI.getEntry(c);
		}
		else {
			Interval interval = intervals.get(0);
			// if the first interval is within object property indexes
			if (interval.getEnd() <= maxObjectPropertyIndex) {
				ObjectPropertyExpression ope = ofac.createObjectProperty(iri);
				range = cacheSI.getEntry(ope);
			}
			else {
				DataPropertyExpression dpe = ofac.createDataProperty(iri);
				range = cacheSI.getEntry(dpe);
			}
		}
		int idx = range.getIndex();
		boolean idxInIntervals = false;
		for (Interval interval : intervals) {
			if (idx >= interval.getStart() && idx <= interval.getEnd()) {
				idxInIntervals = true;
				break;
			}
		}
		if (!idxInIntervals)
			throw new RuntimeException("INTERVALS " + intervals + " FOR " + iri + "(" + type + ") DO NOT CONTAIN " + range.getIndex());

		range.addRange(intervals);	
	}
	
	public void loadMetadata(Connection conn) throws SQLException {
		log.debug("Loading semantic index metadata from the database *");

		cacheSI = new SemanticIndexCache(reasonerDag);	
		nonEmptyEntityRecord.clear();

		// Fetching the index data 
		Statement st = conn.createStatement();
		ResultSet res = st.executeQuery("SELECT * FROM " + indexTable.tableName + " ORDER BY IDX");
		while (res.next()) {
			String iri = res.getString(1);
			if (iri.startsWith("file:/"))  // ROMAN: what exactly is this?!
				continue;
			
			int idx = res.getInt(2);
			int type = res.getInt(3);
			setIndex(iri, type, idx);
		}
		res.close();

		
		// compute the maximum object property index 
		// (all data property indexes must be above)
		int maxObjectPropertyIndex = 0;
		for (Entry<ObjectPropertyExpression, SemanticIndexRange> entry : cacheSI.getObjectPropertyIndexEntries()) {
			maxObjectPropertyIndex = Math.max(maxObjectPropertyIndex, entry.getValue().getIndex());
		}
		
		
		// fetching the intervals data, note that a given String can have one ore
		// more intervals (a set) hence we need to go through several rows to
		// collect all of them. To do this we sort the table by URI (to get all
		// the intervals for a given String in sequence), then we collect all the
		// intervals row by row until we change URI, at that switch we store the
		// interval

		res = st.executeQuery("SELECT * FROM " + intervalTable.tableName + " ORDER BY URI, ENTITY_TYPE");

		List<Interval> currentSet = null;
		int previousType = 0;
		String previousString = null;
		while (res.next()) {
			String iri = res.getString(1);
			if (iri.startsWith("file:/"))   // ROMAN: what is this?
				continue;

			int type = res.getInt(4);

			if (previousString == null) { // very first row
				currentSet = new LinkedList<Interval>();
				previousType = type;
				previousString = iri;
			}

			if ((!iri.equals(previousString) || previousType != type)) {
				 // we switched URI or type, time to store the collected
				 // intervals and clear the set
				setIntervals(previousString, previousType, currentSet, maxObjectPropertyIndex);

				currentSet = new LinkedList<Interval>();
				previousType = type;
				previousString = iri;
			}

			int low = res.getInt(2);
			int high = res.getInt(3);
			currentSet.add(new Interval(low, high));
		}

		setIntervals(previousString, previousType, currentSet, maxObjectPropertyIndex);

		res.close();

		/**
		 * Restoring the emptiness index
		 */
		res = st.executeQuery("SELECT * FROM " + emptinessIndexTable.tableName);
		while (res.next()) {
			int sitable = res.getInt(1);
			int type1 = res.getInt(3);
			int type2 = res.getInt(4);
			int idx = res.getInt(2);
			
			SemanticIndexRecord.checkTypeValue(type1);
			SemanticIndexRecord.checkTypeValue(type2);
			SemanticIndexRecord.checkSITableValue(sitable);
			
			SemanticIndexRecord r = new SemanticIndexRecord(sitable, type1, type2, idx);
			nonEmptyEntityRecord.add(r);
		}

		res.close();

	}

	private static final COL_TYPE objectTypes[] = new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.BNODE };

	private static final COL_TYPE types[] = new COL_TYPE[] { COL_TYPE.LITERAL, COL_TYPE.LITERAL_LANG, COL_TYPE.BOOLEAN, 
		COL_TYPE.DATETIME, COL_TYPE.DECIMAL, COL_TYPE.DOUBLE, COL_TYPE.INTEGER, COL_TYPE.INT,
		COL_TYPE.UNSIGNED_INT, COL_TYPE.NEGATIVE_INTEGER, COL_TYPE.NON_NEGATIVE_INTEGER, 
		COL_TYPE.POSITIVE_INTEGER, COL_TYPE.NON_POSITIVE_INTEGER, COL_TYPE.FLOAT,  COL_TYPE.LONG, 
		COL_TYPE.STRING };


	
	public Collection<OBDAMappingAxiom> getMappings() throws OBDAException {

		List<OBDAMappingAxiom> result = new LinkedList<>();

		/*
		 * PART 2: Creating the mappings
		 * 
		 * Note, at every step we always use the pureIsa dag to get the indexes
		 * and ranges for each class.
		 */


		for (Equivalences<ObjectPropertyExpression> set: reasonerDag.getObjectPropertyDAG()) {

			ObjectPropertyExpression node = set.getRepresentative();
			// only named roles are mapped
			if (node.isInverse()) 
				continue;
			
			// We need to make sure we make no mappings for Auxiliary roles
			// introduced by the Ontology translation process.
			if (OntologyVocabularyImpl.isAuxiliaryProperty(node)) 
				continue;
			
			/***
			 * Generating one mapping for each supported cases, i.e., the second
			 * component is an object, or one of the supported datatypes. For
			 * each case we will construct 1 target query, one source query and
			 * the mapping axiom.
			 * 
			 * The resulting mapping will be added to the list. In this way,
			 * each property can act as an object or data property of any type.
			 */
			
			for (COL_TYPE obType1 : objectTypes) {
				for (COL_TYPE obType2 : objectTypes) {
					if (!isMappingEmpty(node, obType1, obType2)) {
						CQIE targetQuery = constructTargetQuery(node.getPredicate(), obType1, obType2);
						String sourceQuery = constructSourceQuery(node, obType1, obType2);
						if (sourceQuery == null)
							continue;
						OBDAMappingAxiom basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
						result.add(basicmapping);		
					}
				}
			}

			for (COL_TYPE obType1 : objectTypes) {
				for (COL_TYPE type2 : types) {			
					if (!isMappingEmpty(node, obType1, type2)) {
						CQIE targetQuery = constructTargetQuery(node.getPredicate(), obType1, type2);
						String sourceQuery = constructSourceQuery(node, obType1, type2);
						if (sourceQuery == null)
							continue;
						OBDAMappingAxiom basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
						result.add(basicmapping);
					}
				}	
			}
		}

		for (Equivalences<DataPropertyExpression> set: reasonerDag.getDataPropertyDAG()) {

			DataPropertyExpression node = set.getRepresentative();
			
			// We need to make sure we make no mappings for Auxiliary roles
			// introduced by the Ontology translation process.
			if (OntologyVocabularyImpl.isAuxiliaryProperty(node)) 
				continue;
			

			/***
			 * Generating one mapping for each supported cases, i.e., the second
			 * component is an object, or one of the supported datatypes. For
			 * each case we will construct 1 target query, one source query and
			 * the mapping axiom.
			 * 
			 * The resulting mapping will be added to the list. In this way,
			 * each property can act as an object or data property of any type.
			 */
			
			for (COL_TYPE obType1 : objectTypes) {
				for (COL_TYPE obType2 : objectTypes) {
					if (!isMappingEmpty(node, obType1, obType2)) {
						CQIE targetQuery = constructTargetQuery(node.getPredicate(), obType1, obType2);
						String sourceQuery = constructSourceQuery(node, obType1, obType2);
						if (sourceQuery == null)
							continue;
						OBDAMappingAxiom basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
						result.add(basicmapping);			
					}
				}
			}

			for (COL_TYPE obType1 : objectTypes) {
				for (COL_TYPE type2 : types) {			
					if (!isMappingEmpty(node, obType1, type2)) {
						CQIE targetQuery = constructTargetQuery(node.getPredicate(), obType1, type2);
						String sourceQuery = constructSourceQuery(node, obType1, type2);
						if (sourceQuery == null)
							continue;
						OBDAMappingAxiom basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
						result.add(basicmapping);
					}
				}	
			}
		}
		
		/*
		 * Creating mappings for each concept
		 */

		EquivalencesDAG<ClassExpression> classes = reasonerDag.getClassDAG();		
		for (Equivalences<ClassExpression> set : classes) {
			
			ClassExpression node = set.getRepresentative();
			
			if (!(node instanceof OClass))
				continue;
						
			OClass classNode = (OClass)node;
			SemanticIndexRange range = cacheSI.getEntry(classNode);
			if (range == null) {
				log.debug("Class: " + classNode + " has no SemanticIndexRange");
				continue;
			}
			List<Interval> intervals = range.getIntervals();

			// Mapping head
			Predicate predicate = dfac.getPredicate("m", new COL_TYPE[] { COL_TYPE.OBJECT });
			Function head = dfac.getFunction(predicate, dfac.getVariable("X"));
			
			if (!isMappingEmpty(classNode, COL_TYPE.OBJECT)) {
				/* FOR URI */
				Function body1 = dfac.getFunction(classNode.getPredicate(), dfac.getUriTemplate(dfac.getVariable("X")));
				CQIE targetQuery1 = dfac.getCQIE(head, body1);

				StringBuilder sql1 = new StringBuilder();
				sql1.append(classTable.selectCommand);
				sql1.append(" ISBNODE = FALSE AND ");
				appendIntervalString(sql1, intervals);

				OBDAMappingAxiom basicmapping = dfac.getRDBMSMappingAxiom(sql1.toString(), targetQuery1);
				result.add(basicmapping);
			}
			if (!isMappingEmpty(classNode, COL_TYPE.BNODE)) {
				/* FOR BNODE */
				
				Function body2 = dfac.getFunction(classNode.getPredicate(), dfac.getBNodeTemplate(dfac.getVariable("X")));
				CQIE targetQuery2 = dfac.getCQIE(head, body2);
				
				StringBuilder sql2 = new StringBuilder();
				sql2.append(classTable.selectCommand);
				sql2.append(" ISBNODE = TRUE AND ");
				appendIntervalString(sql2, intervals);

				OBDAMappingAxiom  basicmapping = dfac.getRDBMSMappingAxiom(sql2.toString(), targetQuery2);
				result.add(basicmapping);
			}
		}

		/*
		 * PART 4: Optimizing.
		 */

		// Merging multiple mappings into 1 with UNION ALL to minimize the
		// number of the mappings.
        /* ROMAN: mergeUniions was final = false;
		if (mergeUniions) {
			for (Predicate predicate : mappings.keySet()) {

				List<OBDAMappingAxiom> currentMappings = mappings.get(predicate);

				// Getting the current head 
				CQIE targetQuery = (CQIE) currentMappings.get(0).getTargetQuery();

				// Computing the merged SQL 
				StringBuilder newSQL = new StringBuilder();
				newSQL.append(((OBDASQLQuery) currentMappings.get(0).getSourceQuery()).toString());
				for (int mapi = 1; mapi < currentMappings.size(); mapi++) {
					newSQL.append(" UNION ALL ");
					newSQL.append(((OBDASQLQuery) currentMappings.get(mapi).getSourceQuery()).toString());
				}

				// Replacing the old mappings 
				OBDAMappingAxiom mergedMapping = dfac.getRDBMSMappingAxiom(newSQL.toString(), targetQuery);
				currentMappings.clear();
				currentMappings.add(mergedMapping);
			}
		}
		*/
		log.debug("Total: {} mappings", result.size());
		return result;
	}

	/***
	 * @param iri
	 * @param type1
	 * @return
	 * @throws OBDAException 
	 */
	private boolean isMappingEmpty(OClass concept, COL_TYPE type1)  {
		
		for (Interval interval : cacheSI.getEntry(concept).getIntervals()) 
			for (int i = interval.getStart(); i <= interval.getEnd(); i++) {
				SemanticIndexRecord record = new SemanticIndexRecord(type1, i);
				if (nonEmptyEntityRecord.contains(record))
					return false;
			}
		
		return true;
	}

	/***
	 * @param iri
	 * @param type1
	 * @param type2
	 * @return
	 * @throws OBDAException 
	 */
	private boolean isMappingEmpty(ObjectPropertyExpression ope, COL_TYPE type1, COL_TYPE type2)  {

		for (Interval interval : cacheSI.getEntry(ope).getIntervals()) 
			for (int i = interval.getStart(); i <= interval.getEnd(); i++) {
				SemanticIndexRecord record = new SemanticIndexRecord(type1, type2, i);
				if (nonEmptyEntityRecord.contains(record)) 
					return false;
			}

		return true;
	}

	/***
	 * @param iri
	 * @param type1
	 * @param type2
	 * @return
	 * @throws OBDAException 
	 */
	private boolean isMappingEmpty(DataPropertyExpression dpe, COL_TYPE type1, COL_TYPE type2)  {

		for (Interval interval : cacheSI.getEntry(dpe).getIntervals()) 
			for (int i = interval.getStart(); i <= interval.getEnd(); i++) {
				SemanticIndexRecord record = new SemanticIndexRecord(type1, type2, i);
				if (nonEmptyEntityRecord.contains(record))
					return false;
			}

		return true;
	}
	
	
	private CQIE constructTargetQuery(Predicate predicate, COL_TYPE type1, COL_TYPE type2) {

		Variable X = dfac.getVariable("X");
		Variable Y = dfac.getVariable("Y");

		Predicate headPredicate = dfac.getPredicate("m", new COL_TYPE[] { COL_TYPE.STRING, COL_TYPE.OBJECT });
		Function head = dfac.getFunction(headPredicate, X, Y);

		Function subjectTerm;
		if (type1 == COL_TYPE.OBJECT) 
			subjectTerm = dfac.getUriTemplate(X);
		else {
			assert (type1 == COL_TYPE.BNODE); 
			subjectTerm = dfac.getBNodeTemplate(X);
		}
		
		Function objectTerm;
		switch (type2) {
			case BNODE:
				objectTerm = dfac.getBNodeTemplate(Y); 
				break;
			case OBJECT:
				objectTerm = dfac.getUriTemplate(Y);
				break;
			case LITERAL_LANG:	
				objectTerm = dfac.getTypedTerm(Y, dfac.getVariable("Z"));
				break;
			case DATE:
			case TIME:
			case YEAR:
				// R: these three types were not covered by the old switch
				throw new RuntimeException("Unsuported type: " + type2);
			default:
				objectTerm = dfac.getTypedTerm(Y, type2);
		}

		Function body = dfac.getFunction(predicate, subjectTerm, objectTerm);
		return dfac.getCQIE(head, body);
	}

	
	
	private String constructSourceQuery(ObjectPropertyExpression ope, COL_TYPE type1, COL_TYPE type2)  {
		StringBuilder sql = new StringBuilder();
		switch (type2) {
			case OBJECT:
			case BNODE:
				sql.append(roleTable.selectCommand);
				break;
			case LITERAL:
			case LITERAL_LANG:
				sql.append(attributeTable.get(COL_TYPE.LITERAL).selectCommand);
				break;
			default:
				sql.append(attributeTable.get(type2).selectCommand);
		}

		/*
		 * If the mapping is for something of type Literal we need to add IS
		 * NULL or IS NOT NULL to the language column. IS NOT NULL might be
		 * redundant since we have another stage in Quest where we add IS NOT
		 * NULL for every variable in the head of a mapping.
		 */

		if (type1 == COL_TYPE.BNODE) 
			sql.append("ISBNODE = TRUE AND ");
		else {
			assert (type1 == COL_TYPE.OBJECT);
			sql.append("ISBNODE = FALSE AND ");
		}

		if (type2 == COL_TYPE.BNODE) 
			sql.append("ISBNODE2 = TRUE AND ");
		else if (type2 == COL_TYPE.OBJECT) 
			sql.append("ISBNODE2 = FALSE AND ");
		else if (type2 == COL_TYPE.LITERAL) 
			sql.append("LANG IS NULL AND ");
		else if (type2 == COL_TYPE.LITERAL_LANG)
			sql.append("LANG IS NOT NULL AND ");
		

		/*
		 * Generating the interval conditions for semantic index
		 */

		SemanticIndexRange range = cacheSI.getEntry(ope);
		if (range == null) {
			log.debug("Object property " + ope + " has no SemanticIndexRange");
			return null;
		}
		List<Interval> intervals = range.getIntervals();	
		appendIntervalString(sql, intervals);

		return sql.toString();
	}
	
	private String constructSourceQuery(DataPropertyExpression dpe, COL_TYPE type1, COL_TYPE type2)  {
		StringBuilder sql = new StringBuilder();
		switch (type2) {
			case OBJECT:
			case BNODE:
				sql.append(roleTable.selectCommand);
				break;
			case LITERAL:
			case LITERAL_LANG:
				sql.append(attributeTable.get(COL_TYPE.LITERAL).selectCommand);
				break;
			default:
				sql.append(attributeTable.get(type2).selectCommand);
		}

		/*
		 * If the mapping is for something of type Literal we need to add IS
		 * NULL or IS NOT NULL to the language column. IS NOT NULL might be
		 * redundant since we have another stage in Quest where we add IS NOT
		 * NULL for every variable in the head of a mapping.
		 */

		if (type1 == COL_TYPE.BNODE) 
			sql.append("ISBNODE = TRUE AND ");
		else {
			assert (type1 == COL_TYPE.OBJECT);
			sql.append("ISBNODE = FALSE AND ");
		}

		if (type2 == COL_TYPE.BNODE) 
			sql.append("ISBNODE2 = TRUE AND ");
		else if (type2 == COL_TYPE.OBJECT) 
			sql.append("ISBNODE2 = FALSE AND ");
		else if (type2 == COL_TYPE.LITERAL) 
			sql.append("LANG IS NULL AND ");
		else if (type2 == COL_TYPE.LITERAL_LANG)
			sql.append("LANG IS NOT NULL AND ");
		

		/*
		 * Generating the interval conditions for semantic index
		 */

		SemanticIndexRange range = cacheSI.getEntry(dpe);
		if (range == null) {
			log.debug("Data property " + dpe + " has no SemanticIndexRange");
			return null;
		}
		List<Interval> intervals = range.getIntervals();	
		appendIntervalString(sql, intervals);

		return sql.toString();
	}
	

	private void appendIntervalString(StringBuilder sql, final List<Interval> intervals) {
		
		Joiner.on(" OR ").appendTo(sql, new Iterator<String>() {		
			private final Iterator<Interval> it = intervals.iterator();

			@Override
			public boolean hasNext() { return it.hasNext(); }

			@Override
			public String next() {
				Interval interval = it.next();
				if (interval.getStart() == interval.getEnd()) 
					return String.format("IDX = %d", interval.getStart());
				else 
					return String.format("IDX >= %d AND IDX <= %d", interval.getStart(), interval.getEnd());

			}

			@Override
			public void remove() { }
		});
		
		if (intervals.size() > 1) {
			sql.insert(0, "(");
			sql.append(")");
		}
	}
	

	public void collectStatistics(Connection conn) throws SQLException {

		try (Statement st = conn.createStatement()) {
			st.addBatch("ANALYZE");
			st.executeBatch();
		}
	}


	/***
	 * Inserts the metadata about semantic indexes and intervals into the
	 * database. The metadata is later used to reconstruct a semantic index
	 * repository.
	 * @throws  
	 */
	public void insertMetadata(Connection conn) throws SQLException {

		log.debug("Inserting semantic index metadata. This will allow the repository to reconstruct itself afterwards.");

		boolean commitval = conn.getAutoCommit();
		conn.setAutoCommit(false);

		try {
			// dropping previous metadata 
			try (Statement st = conn.createStatement()) {
				st.executeUpdate("DELETE FROM " + indexTable.tableName);
				st.executeUpdate("DELETE FROM " + intervalTable.tableName);
				st.executeUpdate("DELETE FROM " + emptinessIndexTable.tableName);
			}

			// inserting index data for classes and properties 
			try (PreparedStatement stm = conn.prepareStatement(indexTable.insertCommand)) {
				for (Entry<OClass,SemanticIndexRange> concept : cacheSI.getClassIndexEntries()) {
					stm.setString(1, concept.getKey().getPredicate().getName());
					stm.setInt(2, concept.getValue().getIndex());
					stm.setInt(3, CLASS_TYPE);
					stm.addBatch();
				}
				for (Entry<ObjectPropertyExpression, SemanticIndexRange> role : cacheSI.getObjectPropertyIndexEntries()) {
					stm.setString(1, role.getKey().getPredicate().getName());
					stm.setInt(2, role.getValue().getIndex());
					stm.setInt(3, ROLE_TYPE);
					stm.addBatch();
				}
				for (Entry<DataPropertyExpression, SemanticIndexRange> role : cacheSI.getDataPropertyIndexEntries()) {
					stm.setString(1, role.getKey().getPredicate().getName());
					stm.setInt(2, role.getValue().getIndex());
					stm.setInt(3, ROLE_TYPE);
					stm.addBatch();
				}
				stm.executeBatch();
			}

			// Inserting interval metadata
			try (PreparedStatement stm = conn.prepareStatement(intervalTable.insertCommand)) {
				for (Entry<OClass,SemanticIndexRange> concept : cacheSI.getClassIndexEntries()) {
					for (Interval it : concept.getValue().getIntervals()) {
						stm.setString(1, concept.getKey().getPredicate().getName());
						stm.setInt(2, it.getStart());
						stm.setInt(3, it.getEnd());
						stm.setInt(4, CLASS_TYPE);
						stm.addBatch();
					}
				}
				for (Entry<ObjectPropertyExpression, SemanticIndexRange> role : cacheSI.getObjectPropertyIndexEntries()) {
					for (Interval it : role.getValue().getIntervals()) {
						stm.setString(1, role.getKey().getPredicate().getName());
						stm.setInt(2, it.getStart());
						stm.setInt(3, it.getEnd());
						stm.setInt(4, ROLE_TYPE);
						stm.addBatch();
					}
				}
				for (Entry<DataPropertyExpression, SemanticIndexRange> role : cacheSI.getDataPropertyIndexEntries()) {
					for (Interval it : role.getValue().getIntervals()) {
						stm.setString(1, role.getKey().getPredicate().getName());
						stm.setInt(2, it.getStart());
						stm.setInt(3, it.getEnd());
						stm.setInt(4, ROLE_TYPE);
						stm.addBatch();
					}
				}
				stm.executeBatch();
			}

			// Inserting emptiness index metadata 
			try (PreparedStatement stm = conn.prepareStatement(emptinessIndexTable.insertCommand)) {
				for (SemanticIndexRecord record : nonEmptyEntityRecord) {
					stm.setInt(1, record.getTable());
					stm.setInt(2, record.getIndex());
					stm.setInt(3, record.getType1());
					stm.setInt(4, record.getType2());
					stm.addBatch();
				}
				stm.executeBatch();
			}

			conn.commit();
		} 
		catch (SQLException e) {
			// If there is a big error, restore everything as it was
			conn.rollback();
		}
		finally {
			conn.setAutoCommit(commitval);			
		}
	}

	
	/**
	 *  DROP indexes	
	 */
		

	public void dropIndexes(Connection conn) throws SQLException {
		log.debug("Droping indexes");

		try (Statement st = conn.createStatement()) {
			for (String s : classTable.dropIndexCommands)
				st.addBatch(s);	

			for (String s : roleTable.dropIndexCommands)
				st.addBatch(s);	

			for (Entry<COL_TYPE, TableDescription> entry : attributeTable.entrySet())
				for (String s : entry.getValue().dropIndexCommands)
					st.addBatch(s);
			
			st.executeBatch();

			isIndexed = false;
		}
	}

	public boolean isIndexed(Connection conn) {
		return isIndexed;
	}

	public boolean isDBSchemaDefined(Connection conn) throws SQLException {
		
		boolean exists = false; // initially pessimistic
		
		try (Statement st = conn.createStatement()) {
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", classTable.tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", roleTable.tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attributeTable.get(COL_TYPE.LITERAL).tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attributeTable.get(COL_TYPE.STRING).tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attributeTable.get(COL_TYPE.INTEGER).tableName));
            st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attributeTable.get(COL_TYPE.LONG).tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attributeTable.get(COL_TYPE.DECIMAL).tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attributeTable.get(COL_TYPE.DOUBLE).tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attributeTable.get(COL_TYPE.DATETIME).tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attributeTable.get(COL_TYPE.BOOLEAN).tableName));

			exists = true; // everything is fine if we get to this point
		} 
		catch (Exception e) {
			// ignore all exceptions
		}
		return exists;
	}

}
