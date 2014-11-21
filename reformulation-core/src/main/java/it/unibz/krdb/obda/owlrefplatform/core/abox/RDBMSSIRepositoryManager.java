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
import it.unibz.krdb.obda.ontology.impl.OntologyVocabularyImpl;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticIndexRecord.SITable;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.EquivalencesDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Interval;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.SemanticIndexCache;
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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private final static TableDescription attributeTableLiteral = new TableDescription("QUEST_DATA_PROPERTY_LITERAL_ASSERTION",
			"\"URI\" INTEGER NOT NULL, VAL VARCHAR(1000) NOT NULL, LANG VARCHAR(20), \"IDX\"  SMALLINT NOT NULL, " + 
			"ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE ",
			"(URI, VAL, LANG, IDX, ISBNODE) VALUES (?, ?, ?, ?, ?)", "\"URI\" as X, VAL as Y, LANG as Z");
	
	
	static {
		classTable.indexOn("idxclassfull", "URI, IDX, ISBNODE");
		classTable.indexOn("idxclassfull2", "URI, IDX");
		
		roleTable.indexOn("idxrolefull1", "URI1, URI2, IDX, ISBNODE, ISBNODE2");
		roleTable.indexOn("idxrolefull2",  "URI2, URI1, IDX, ISBNODE2, ISBNODE");
		roleTable.indexOn("idxrolefull22", "URI1, URI2, IDX");
		
		attributeTableLiteral.indexOn("IDX_LITERAL_ATTRIBUTE" + "1", "URI");		
		attributeTableLiteral.indexOn("IDX_LITERAL_ATTRIBUTE" + "2", "IDX");
		attributeTableLiteral.indexOn("IDX_LITERAL_ATTRIBUTE" + "3", "VAL");				
	}
	

	/**
	 *  Data tables
	 */
  
	private static final Map<COL_TYPE, String> attribute_table = new HashMap<>();
	
	static {
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
	}
	
	
	
/**
 *  CREATE data tables
 */
	
	private static final String attribute_table_string_create = "CREATE TABLE " + attribute_table.get(COL_TYPE.STRING) + " ( "
			+ "\"URI\" INTEGER  NOT NULL, " + "VAL VARCHAR(1000), " + "\"IDX\"  SMALLINT  NOT NULL"
			+ ", ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE " + ")";
	private static final String attribute_table_integer_create = "CREATE TABLE " + attribute_table.get(COL_TYPE.INTEGER)+ " ( "
			+ "\"URI\" INTEGER  NOT NULL, " + "VAL BIGINT NOT NULL, " + "\"IDX\"  SMALLINT NOT NULL"
			+ ", ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE " + ")";
	private static final String attribute_table_int_create = "CREATE TABLE " + attribute_table.get(COL_TYPE.INT) + " ( "
            + "\"URI\" INTEGER  NOT NULL, " + "VAL INTEGER NOT NULL, " + "\"IDX\"  SMALLINT NOT NULL"
            + ", ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE " + ")";
    private static final String attribute_table_negative_integer_create = "CREATE TABLE " + attribute_table.get(COL_TYPE.NEGATIVE_INTEGER) + " ( "
            + "\"URI\" INTEGER  NOT NULL, " + "VAL BIGINT NOT NULL, " + "\"IDX\"  SMALLINT NOT NULL"
            + ", ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE " + ")";
    private static final String attribute_table_positive_integer_create = "CREATE TABLE " + attribute_table.get(COL_TYPE.POSITIVE_INTEGER) + " ( "
            + "\"URI\" INTEGER  NOT NULL, " + "VAL BIGINT NOT NULL, " + "\"IDX\"  SMALLINT NOT NULL"
            + ", ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE " + ")";
    private static final String attribute_table_unsigned_int_create = "CREATE TABLE " + attribute_table.get(COL_TYPE.UNSIGNED_INT) + " ( "
            + "\"URI\" INTEGER  NOT NULL, " + "VAL INTEGER NOT NULL, " + "\"IDX\"  SMALLINT NOT NULL"
            + ", ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE " + ")";
    private static final String attribute_table_non_positive_integer_create = "CREATE TABLE " + attribute_table.get(COL_TYPE.NON_POSITIVE_INTEGER) + " ( "
            + "\"URI\" INTEGER  NOT NULL, " + "VAL BIGINT NOT NULL, " + "\"IDX\"  SMALLINT NOT NULL"
            + ", ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE " + ")";
    private static final String attribute_table_non_negative_integer_create = "CREATE TABLE " + attribute_table.get(COL_TYPE.NON_NEGATIVE_INTEGER) + " ( "
            + "\"URI\" INTEGER  NOT NULL, " + "VAL BIGINT NOT NULL, " + "\"IDX\"  SMALLINT NOT NULL"
            + ", ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE " + ")";
    private static final String attribute_table_long_create = "CREATE TABLE " + attribute_table.get(COL_TYPE.LONG) + " ( "
            + "\"URI\" INTEGER  NOT NULL, " + "VAL BIGINT NOT NULL, " + "\"IDX\"  SMALLINT NOT NULL"
            + ", ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE " + ")";
    private static final String attribute_table_decimal_create = "CREATE TABLE " + attribute_table.get(COL_TYPE.DECIMAL) + " ( "
			+ "\"URI\" INTEGER NOT NULL, " + "VAL DECIMAL NOT NULL, " + "\"IDX\"  SMALLINT NOT NULL"
			+ ", ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE " + ")";
	private static final String attribute_table_double_create = "CREATE TABLE " + attribute_table.get(COL_TYPE.DOUBLE) + " ( "
			+ "\"URI\" INTEGER NOT NULL, " + "VAL DOUBLE PRECISION NOT NULL, " + "\"IDX\"  SMALLINT NOT NULL"
			+ ", ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE " + ")";
	private static final String attribute_table_float_create = "CREATE TABLE " + attribute_table.get(COL_TYPE.FLOAT) + " ( "
            + "\"URI\" INTEGER NOT NULL, " + "VAL DOUBLE PRECISION NOT NULL, " + "\"IDX\"  SMALLINT NOT NULL"
            + ", ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE " + ")";
    private static final String attribute_table_datetime_create = "CREATE TABLE " + attribute_table.get(COL_TYPE.DATETIME) + " ( "
			+ "\"URI\" INTEGER NOT NULL, " + "VAL TIMESTAMP NOT NULL, " + "\"IDX\"  SMALLINT NOT NULL"
			+ ", ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE " + ")";
	private static final String attribute_table_boolean_create = "CREATE TABLE " + attribute_table.get(COL_TYPE.BOOLEAN) + " ( "
			+ "\"URI\" INTEGER NOT NULL, " + "VAL BOOLEAN NOT NULL, " + "\"IDX\"  SMALLINT NOT NULL"
			+ ", ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE " + ")";


/**
 *  INSERT data 	
 */
	
	private static final Map<COL_TYPE, String> attribute_table_insert = new HashMap<>();
	
	static {
		for (Entry<COL_TYPE, String> entry : attribute_table.entrySet()) 
			attribute_table_insert.put(entry.getKey(), "INSERT INTO " + entry.getValue() 
											+ " (URI, VAL, IDX, ISBNODE) VALUES (?, ?, ?, ?)");
	}
	

/**
 *  Indexes
 */
	

	private static final Map<COL_TYPE, String> attribute_index = new HashMap<>();
	
	static {
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
	}
	

	


	
	
	

	private static final Map<COL_TYPE, String> select_mapping_attribute = new HashMap<>();
	
	static {
		// two special cases
		select_mapping_attribute.put(COL_TYPE.OBJECT, roleTable.selectCommand);
		select_mapping_attribute.put(COL_TYPE.LITERAL, attributeTableLiteral.selectCommand);
		//
		for (Entry<COL_TYPE, String> entry : attribute_table.entrySet()) 
			select_mapping_attribute.put(entry.getKey(),  "SELECT \"URI\" as X, VAL as Y FROM " + entry.getValue() + " WHERE ");  			
	}
	
	
	private static final String whereSingleCondition = "IDX = %d";
	private static final String whereIntervalCondition = "IDX >= %d AND IDX <= %d";
	

	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
	
	private final SemanticIndexURIMap uriMap = new SemanticIndexURIMap();
	
	private final TBoxReasoner reasonerDag;
	private final SemanticIndexCache cacheSI;
	
	private boolean isIndexed;  // database index created

	private final HashSet<SemanticIndexRecord> nonEmptyEntityRecord = new HashSet<SemanticIndexRecord>();

	private final List<RepositoryChangedListener> changeList = new LinkedList<RepositoryChangedListener>();

	public RDBMSSIRepositoryManager(TBoxReasoner reasonerDag) {
		this.reasonerDag = reasonerDag;
		cacheSI = new SemanticIndexCache(reasonerDag);
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

		Statement st = conn.createStatement();

		st.addBatch(uriIdTable.createCommand);
		
		st.addBatch(indexTable.createCommand);
		st.addBatch(intervalTable.createCommand);
		st.addBatch(emptinessIndexTable.createCommand);

		st.addBatch(classTable.createCommand);
		st.addBatch(roleTable.createCommand);

		st.addBatch(attributeTableLiteral.createCommand);
		st.addBatch(attribute_table_string_create);
		st.addBatch(attribute_table_integer_create);
        st.addBatch(attribute_table_int_create);
        st.addBatch(attribute_table_unsigned_int_create);
        st.addBatch(attribute_table_negative_integer_create);
        st.addBatch(attribute_table_non_negative_integer_create);
        st.addBatch(attribute_table_positive_integer_create);
        st.addBatch(attribute_table_non_positive_integer_create);
        st.addBatch(attribute_table_float_create);
        st.addBatch(attribute_table_long_create);
		st.addBatch(attribute_table_decimal_create);
		st.addBatch(attribute_table_double_create);
		st.addBatch(attribute_table_datetime_create);
		st.addBatch(attribute_table_boolean_create);

		
		
		
		st.executeBatch();
		st.close();
	}

	public void createIndexes(Connection conn) throws SQLException {
		log.debug("Creating indexes");
		Statement st = conn.createStatement();

//		st.addBatch(indexclass1);
//		st.addBatch(indexclass2);
//		st.addBatch(indexrole1);
//		st.addBatch(indexrole2);
//		st.addBatch(indexrole3);
		
		for (String s : attributeTableLiteral.createIndexCommands)
			st.addBatch(s);

		for (Entry<COL_TYPE, String> entry : attribute_index.entrySet()) {
			st.addBatch("CREATE INDEX " + entry.getValue() + "1 ON " + attribute_table.get(entry.getKey()) + " (URI)");		
			st.addBatch("CREATE INDEX " + entry.getValue() + "2 ON " + attribute_table.get(entry.getKey()) + " (IDX)");
			st.addBatch("CREATE INDEX " + entry.getValue() + "3 ON " + attribute_table.get(entry.getKey()) + " (VAL)");
		}
		
		for (String s : classTable.createIndexCommands)
			st.addBatch(s);
		
		for (String s : roleTable.createIndexCommands)
			st.addBatch(s);
		
		st.executeBatch();
		
		log.debug("Executing ANALYZE");
		st.addBatch("ANALYZE");
		st.executeBatch();
		
		st.close();

		isIndexed = true;
	}

	
	
	public void dropDBSchema(Connection conn) throws SQLException {

		Statement st = conn.createStatement();

		st.addBatch(indexTable.dropCommand);
		st.addBatch(intervalTable.dropCommand);
		st.addBatch(emptinessIndexTable.dropCommand);

		st.addBatch(classTable.dropCommand);
		st.addBatch(roleTable.dropCommand);
		st.addBatch(attributeTableLiteral.dropCommand);
		
		for (Entry<COL_TYPE, String> entry : attribute_table.entrySet())
			st.addBatch("DROP TABLE " + entry.getValue()); 
		
		st.addBatch(uriIdTable.dropCommand);

		st.executeBatch();
		st.close();
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
		PreparedStatement attributeLiteralStm = conn.prepareStatement(attributeTableLiteral.insertCommand);
		
		Map<COL_TYPE, PreparedStatement> attributeStm = new HashMap<COL_TYPE, PreparedStatement>();
		for (Entry<COL_TYPE, String> entry : attribute_table_insert.entrySet()) {
			PreparedStatement stm = conn.prepareStatement(entry.getValue());
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


			int index = 0;
			if (ax instanceof ClassAssertion) {
				ClassAssertion ca = (ClassAssertion) ax; 
				try {
					addPreparedStatement(uriidStm, classStm, ca);
					index = cacheSI.getIndex(ca.getConcept()); // WOW ! NullPointerException here
					// Register non emptiness
					COL_TYPE t1 = ca.getIndividual().getType();
					SemanticIndexRecord record = new SemanticIndexRecord(SITable.CLASS, t1, COL_TYPE.OBJECT, index);
					nonEmptyEntityRecord.add(record);
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
					addPreparedStatement(uriidStm, roleStm, opa);	
					index = cacheSI.getIndex(opa.getProperty());
					// Register non emptiness
					COL_TYPE t1 = opa.getSubject().getType();
					COL_TYPE t2 = opa.getObject().getType();		
					SemanticIndexRecord record = new SemanticIndexRecord(SITable.OPROP, t1, t2, index);
					nonEmptyEntityRecord.add(record);
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
					addPreparedStatement(uriidStm, attributeLiteralStm, attributeStm, dpa);										
					index = cacheSI.getIndex(dpa.getProperty());
					COL_TYPE t1 = dpa.getSubject().getType();
					COL_TYPE t2 = dpa.getValue().getType();		
					SITable table = SemanticIndexRecord.COLTYPEtoSITable.get(t2);	
					if (table != null) { // IMPORTANT: can be null is the datatype is not recognised
						SemanticIndexRecord record = new SemanticIndexRecord(table, t1, t2, index);
						nonEmptyEntityRecord.add(record);
					}
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
				executeBatch(uriidStm);
				executeBatch(roleStm);
				executeBatch(attributeLiteralStm);
				for (PreparedStatement stm : attributeStm.values())
					executeBatch(stm);
				executeBatch(classStm);
				batchCount = 0; // reset the counter
			}

			// Check if the commit count is already in the commit limit
			if (commitCount == commitLimit) {
				conn.commit();
				commitCount = 0; // reset the counter
			}
		}

		// Execute the rest of the batch
		executeBatch(uriidStm);
		executeBatch(roleStm);
		executeBatch(attributeLiteralStm);
		for (PreparedStatement stm : attributeStm.values())
			executeBatch(stm);
		executeBatch(classStm);

	
		// Close all open statements
		uriidStm.close();
		roleStm.close();;
		attributeLiteralStm.close();;
		for (PreparedStatement stm : attributeStm.values())
			stm.close();;
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

	private void addPreparedStatement(PreparedStatement uriidStm, PreparedStatement roleStm, ObjectPropertyAssertion ax) throws SQLException {

		ObjectPropertyExpression prop = ax.getProperty();

		ObjectConstant object = ax.getObject();

		// Construct the database INSERT statements
		ObjectConstant subject = ax.getSubject();

		String uri = subject.getName();
		int uri_id = uriMap.idOfURI(uri);
		uriidStm.setInt(1, uri_id);
		uriidStm.setString(2, uri);
		uriidStm.addBatch();
		
		boolean c1isBNode = subject instanceof BNode;

		int idx = cacheSI.getIndex(prop);

		// Get the object property assertion
		String uri2 = object.getName();
		boolean c2isBNode = object instanceof BNode;

		if (isInverse(prop)) {

			/* Swapping values */

			String tmp = uri;
			uri = uri2;
			uri2 = tmp;

			boolean tmpb = c1isBNode;
			c1isBNode = c2isBNode;
			c2isBNode = tmpb;
		}

		// Construct the database INSERT statement
		// replace URIs with their ids
		
		uri_id = uriMap.idOfURI(uri);
		uriidStm.setInt(1, uri_id);
		uriidStm.setString(2, uri);
		uriidStm.addBatch();

		int uri2_id = uriMap.idOfURI(uri2);
		uriidStm.setInt(1, uri2_id);
		uriidStm.setString(2, uri2);
		uriidStm.addBatch();
		
		roleStm.setInt(1, uri_id);
		roleStm.setInt(2, uri2_id);		
		roleStm.setInt(3, idx);
		roleStm.setBoolean(4, c1isBNode);
		roleStm.setBoolean(5, c2isBNode);
		roleStm.addBatch();

		// log.debug("role");

		// log.debug("inserted: {} {}", uri, uri2);
		// log.debug("inserted: {} property", idx);
	} 

	private void addPreparedStatement(PreparedStatement uriidStm, PreparedStatement attributeLiteralStm,
			Map<COL_TYPE, PreparedStatement> attributeStatement, DataPropertyAssertion ax) throws SQLException {
		

		// Construct the database INSERT statements
		ObjectConstant subject = ax.getSubject();
		String uri = subject.getName();
		int uri_id = uriMap.idOfURI(uri);
		uriidStm.setInt(1, uri_id);
		uriidStm.setString(2, uri);
		uriidStm.addBatch();
		
		boolean c1isBNode = subject instanceof BNode;

		DataPropertyExpression prop = ax.getProperty();
		int idx = cacheSI.getIndex(prop);

		// The insertion is based on the datatype from TBox
		ValueConstant object = ax.getValue();
		Predicate.COL_TYPE attributeType = object.getType();
		String value = object.getValue();
		String lang = object.getLanguage();

		switch (attributeType) {
		case BNODE:
		case OBJECT:
			throw new RuntimeException("Data property cannot have a URI as object");
		case LITERAL:
		case LITERAL_LANG:
			setInputStatement(attributeLiteralStm, uri_id, value, lang, idx, c1isBNode);
			// log.debug("literal");
			break;
		case STRING:
			setInputStatement(attributeStatement.get(attributeType), uri_id, value, idx, c1isBNode);
			// log.debug("string");
			break;
        case INTEGER:
            if (value.charAt(0) == '+')
                value = value.substring(1, value.length());
            setInputStatement(attributeStatement.get(attributeType), uri_id, Long.parseLong(value), idx, c1isBNode);
            // log.debug("Integer");
            break;
        case INT:
            if (value.charAt(0) == '+')
                value = value.substring(1, value.length());
            setInputStatement(attributeStatement.get(attributeType), uri_id, Integer.parseInt(value), idx, c1isBNode);
            // log.debug("Int");
            break;
        case UNSIGNED_INT:
            setInputStatement(attributeStatement.get(attributeType), uri_id, Integer.parseInt(value), idx, c1isBNode);
            // log.debug("Int");
            break;
        case NEGATIVE_INTEGER:
            setInputStatement(attributeStatement.get(attributeType), uri_id, Long.parseLong(value), idx, c1isBNode);
            // log.debug("Integer");
            break;
        case POSITIVE_INTEGER:
            if (value.charAt(0) == '+')
                value = value.substring(1, value.length());
            setInputStatement(attributeStatement.get(attributeType), uri_id, Long.parseLong(value), idx, c1isBNode);
            // log.debug("Integer");
            break;
        case NON_NEGATIVE_INTEGER:
            if (value.charAt(0) == '+')
                value = value.substring(1, value.length());
            setInputStatement(attributeStatement.get(attributeType), uri_id, Long.parseLong(value), idx, c1isBNode);
            // log.debug("Integer");
            break;
        case NON_POSITIVE_INTEGER:
                value = value.substring(1, value.length());
            setInputStatement(attributeStatement.get(attributeType), uri_id, Long.parseLong(value), idx, c1isBNode);
            // log.debug("Integer");
            break;
        case FLOAT:
            setInputStatement(attributeStatement.get(attributeType), uri_id, Float.parseFloat(value), idx, c1isBNode);
            // log.debug("Float");
            break;
        case LONG:
			if (value.charAt(0) == '+')
				value = value.substring(1, value.length());
			setInputStatement(attributeStatement.get(attributeType), uri_id, Long.parseLong(value), idx, c1isBNode);
			// log.debug("Long");
			break;
		case DECIMAL:
			setInputStatement(attributeStatement.get(attributeType), uri_id, parseBigDecimal(value), idx, c1isBNode);
			// log.debug("BigDecimal");
			break;
		case DOUBLE:
			setInputStatement(attributeStatement.get(attributeType), uri_id, Double.parseDouble(value), idx, c1isBNode);
			// log.debug("Double");
			break;
		case DATETIME:
			setInputStatement(attributeStatement.get(attributeType), uri_id, parseTimestamp(value), idx, c1isBNode);
			// log.debug("Date");
			break;
		case BOOLEAN:
			value = getBooleanString(value); // PostgreSQL
												// abbreviates the
												// boolean value to
												// 't' and 'f'
			setInputStatement(attributeStatement.get(attributeType), uri_id, Boolean.parseBoolean(value), idx, c1isBNode);
			// log.debug("boolean");
			break;
		case UNSUPPORTED:
		default:
			log.warn("Ignoring assertion: {}", ax);
		}
	}
	
	
	private void addPreparedStatement(PreparedStatement uriidStm, PreparedStatement classStm, ClassAssertion ax) throws SQLException {

		// Construct the database INSERT statements
		ObjectConstant c1 = ax.getIndividual();

		boolean c1isBNode = c1 instanceof BNode;

		String uri;
		if (c1isBNode)
			uri = ((BNode) c1).getName();
		else
			uri = ((URIConstant) c1).getURI().toString();

		// Construct the database INSERT statement
		int uri_id = uriMap.idOfURI(uri);
		uriidStm.setInt(1, uri_id);
		uriidStm.setString(2, uri);
		uriidStm.addBatch();			

		OClass concept = ax.getConcept();
		int conceptIndex = cacheSI.getIndex(concept);
	
		classStm.setInt(1, uri_id);
		classStm.setInt(2, conceptIndex);
		classStm.setBoolean(3, c1isBNode);
		classStm.addBatch();

		// log.debug("inserted: {} {} class", uri, conceptIndex);
	}

	private void executeBatch(PreparedStatement statement) throws SQLException {
		statement.executeBatch();
		statement.clearBatch();
	}

	private boolean isInverse(ObjectPropertyExpression property) {
	
		ObjectPropertyExpression desc = reasonerDag.getObjectPropertyDAG().getVertex(property).getRepresentative();
		if (!property.equals(desc)) {
			if (desc.isInverse()) 
				return true;
		}
		return false; // representative is never an inverse
	}


	private String getBooleanString(String value) {
		if (value.equalsIgnoreCase("t") || value.equals("1")) {
			return "true";
		} else if (value.equalsIgnoreCase("f") || value.equals("0")) {
			return "false";
		} else {
			return value; // nothing change
		}
	}

	private void setInputStatement(PreparedStatement stm, int uri, String value, String lang, int idx, boolean isBnode)
			throws SQLException {

		// log.debug("inserted: {} {}", uri, value);
		// log.debug("inserted: {} {}", lang, idx);
		stm.setInt(1, uri);
		stm.setString(2, value);
		stm.setString(3, lang);
		stm.setInt(4, idx);
		stm.setBoolean(5, isBnode);

		stm.addBatch();
	}

	private void setInputStatement(PreparedStatement stm, int uri, String value, int idx, boolean isBnode) throws SQLException {
		// log.debug("inserted: {} {}", uri, value);
		// log.debug("inserted: {}", idx);
		stm.setInt(1, uri);
		stm.setString(2, value);
		stm.setInt(3, idx);
		stm.setBoolean(4, isBnode);

		stm.addBatch();
	}

	private void setInputStatement(PreparedStatement stm, int uri, int value, int idx, boolean isBnode) throws SQLException {
		// log.debug("inserted: {} {}", uri, value);
		// log.debug("inserted: {}", idx);
		stm.setInt(1, uri);
		stm.setInt(2, value);
		stm.setInt(3, idx);
		stm.setBoolean(4, isBnode);

		stm.addBatch();
	}

	private void setInputStatement(PreparedStatement stm, int uri, BigDecimal value, int idx, boolean isBnode) throws SQLException {
		// log.debug("inserted: {} {}", uri, value);
		// log.debug("inserted: {}", idx);
		stm.setInt(1, uri);
		stm.setBigDecimal(2, value);
		stm.setInt(3, idx);
		stm.setBoolean(4, isBnode);

		stm.addBatch();
	}

	private void setInputStatement(PreparedStatement stm, int uri, double value, int idx, boolean isBnode) throws SQLException {
		// log.debug("inserted: {} {}", uri, value);
		// log.debug("inserted: {}", idx);
		stm.setInt(1, uri);
		stm.setDouble(2, value);
		stm.setInt(3, idx);
		stm.setBoolean(4, isBnode);

		stm.addBatch();
	}

	private void setInputStatement(PreparedStatement stm, int uri, Timestamp value, int idx, boolean isBnode) throws SQLException {
		// log.debug("inserted: {} {}", uri, value);
		// log.debug("inserted: {}", idx);
		stm.setInt(1, uri);
		stm.setTimestamp(2, value);
		stm.setInt(3, idx);
		stm.setBoolean(4, isBnode);

		stm.addBatch();
	}

	private void setInputStatement(PreparedStatement stm, int uri, boolean value, int idx, boolean isBnode) throws SQLException {
		// log.debug("inserted: {} {}", uri, value);
		// log.debug("inserted: {}", idx);
		stm.setInt(1, uri);
		stm.setBoolean(2, value);
		stm.setInt(3, idx);
		stm.setBoolean(4, isBnode);

		stm.addBatch();
	}

	private static BigDecimal parseBigDecimal(String value) {
		return new BigDecimal(value);
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

	public void loadMetadata(Connection conn) throws SQLException {
		log.debug("Loading semantic index metadata from the database *");

		cacheSI.clear();
		
		nonEmptyEntityRecord.clear();


		/* Fetching the index data */
		Statement st = conn.createStatement();
		ResultSet res = st.executeQuery("SELECT * FROM " + indexTable.tableName);
		while (res.next()) {
			String string = res.getString(1);
			if (string.startsWith("file:/"))
				continue;
			String iri = string;
			int idx = res.getInt(2);
			int type = res.getInt(3);
			cacheSI.setIndex(iri, type, idx);
		}
		res.close();

		/*
		 * fetching the intervals data, note that a given String can have one ore
		 * more intervals (a set) hence we need to go through several rows to
		 * collect all of them. To do this we sort the table by URI (to get all
		 * the intervals for a given String in sequence), then we collect all the
		 * intervals row by row until we change URI, at that switch we store the
		 * interval
		 */

		res = st.executeQuery("SELECT * FROM " + intervalTable.tableName + " ORDER BY URI, ENTITY_TYPE");

		List<Interval> currentSet = new LinkedList<Interval>();
		String previousStringStr = null;
		int previousType = 0;
		String previousString = null;
		while (res.next()) {

			String iristr = res.getString(1);
			if (iristr.startsWith("file:/"))
				continue;

			String iri = iristr;
			int low = res.getInt(2);
			int high = res.getInt(3);
			int type = res.getInt(4);

			if (previousStringStr == null) {
				currentSet = new LinkedList<Interval>();
				previousStringStr = iristr;
				previousType = type;
				previousString = iri;
			}

			if ((!iristr.equals(previousStringStr) || previousType != type)) {
				/*
				 * we switched URI or type, time to store the collected
				 * intervals and clear the set
				 */
				cacheSI.setIntervals(previousString, previousType, currentSet);

				currentSet = new LinkedList<Interval>();
				previousStringStr = iristr;
				previousType = type;
				previousString = iri;
			}

			currentSet.add(new Interval(low, high));

		}

		cacheSI.setIntervals(previousString, previousType, currentSet);

		res.close();

		/**
		 * Restoring the emptyness index
		 */
		res = st.executeQuery("SELECT * FROM " + emptinessIndexTable.tableName);
		while (res.next()) {
			int table = res.getInt(1);
			int type1 = res.getInt(3);
			int type2 = res.getInt(4);
			int idx = res.getInt(2);
			SemanticIndexRecord r = SemanticIndexRecord.createSIRecord(table, type1, type2, idx);
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

		Set<Predicate> roleNodes = new HashSet<Predicate>();

		for (Equivalences<ObjectPropertyExpression> set: reasonerDag.getObjectPropertyDAG()) {

			ObjectPropertyExpression node = set.getRepresentative();
			// only named roles are mapped
			if (node.isInverse()) 
				continue;
			// We need to make sure we make no mappings for Auxiliary roles
			// introduced by the Ontology translation process.
			if (OntologyVocabularyImpl.isAuxiliaryProperty(node)) 
				continue;
			
			
			roleNodes.add(node.getPredicate());
		}
		
		for (Equivalences<DataPropertyExpression> set: reasonerDag.getDataPropertyDAG()) {

			DataPropertyExpression node = set.getRepresentative();
			
			// We need to make sure we make no mappings for Auxiliary roles
			// introduced by the Ontology translation process.
			if (OntologyVocabularyImpl.isAuxiliaryProperty(node)) 
				continue;
			
			roleNodes.add(node.getPredicate());
			
		}

		/*
		 * Collecting relevant nodes for each class, that is, the Node itself,
		 * and each exists R such that there is no other exists P, such that R
		 * isa P
		 * 
		 * Here we cannot collect only the top most, so we do it in two passes.
		 * First we collect all exists R children, then we remove redundant ones.
		 */

		// TODO this part can be optimized if we know some existing dependencies
		// (e.g., coming from given mappings)

		Set<OClass> classNodesMaps = new HashSet<OClass>();
		EquivalencesDAG<ClassExpression> classes = reasonerDag.getClassDAG();
		
		for (Equivalences<ClassExpression> set : classes) {
			
			ClassExpression node = set.getRepresentative();
			
			if (!(node instanceof OClass))
				continue;
						
			classNodesMaps.add((OClass)node);
		}

		/*
		 * We collected all classes and properties that need mappings, and the
		 * nodes that are relevant for each of their mappings
		 */

		/*
		 * PART 2: Creating the mappings
		 * 
		 * Note, at every step we always use the pureIsa dag to get the indexes
		 * and ranges for each class.
		 */

		// Creating the mappings for each role

		Map<Predicate, List<OBDAMappingAxiom>> mappings = new HashMap<Predicate, List<OBDAMappingAxiom>>();

		for (Predicate role : roleNodes) {

			// Get the indexed node (from the pureIsa dag)
			List<OBDAMappingAxiom> currentMappings = new LinkedList<OBDAMappingAxiom>();
			mappings.put(role, currentMappings);

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
					CQIE targetQuery = constructTargetQuery(role, obType1, obType2);
					String sourceQuery = constructSourceQuery(role, obType1, obType2);
					OBDAMappingAxiom basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
					if (!isMappingEmpty(role.getName(), obType1, obType2))
						currentMappings.add(basicmapping);					
				}
			}

			for (COL_TYPE obType1 : objectTypes) {
				for (COL_TYPE type2 : types) {			
					CQIE targetQuery = constructTargetQuery(role, obType1, type2);
					String sourceQuery = constructSourceQuery(role, obType1, type2);
					OBDAMappingAxiom basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
					if (!isMappingEmpty(role.getName(), obType1, type2))
						currentMappings.add(basicmapping);
				}	
			}
		}

		/*
		 * Creating mappings for each concept
		 */


		for (OClass classNode : classNodesMaps) {

			Predicate classuri = classNode.getPredicate();
			List<Interval> intervals = cacheSI.getIntervals(classNode);
			if (intervals == null) {
				log.warn("Found URI with no mappings, the ontology might not match the respository. Ill URI: {}", classuri.getName());
				continue;
			}

			List<OBDAMappingAxiom> currentMappings = new LinkedList<OBDAMappingAxiom>();
			mappings.put(classuri, currentMappings);

			// Mapping head
			Function head = dfac.getFunction(dfac.getPredicate("m", 1), dfac.getVariable("X"));
			
			{
				/* FOR URI */
				Function body1 = dfac.getFunction(classuri, dfac.getUriTemplate(dfac.getVariable("X")));
				CQIE targetQuery1 = dfac.getCQIE(head, body1);

				StringBuilder sql1 = new StringBuilder();
				sql1.append(classTable.selectCommand);
				sql1.append(" ISBNODE = FALSE AND ");
				appendIntervalString(intervals, sql1);

				OBDAMappingAxiom basicmapping = dfac.getRDBMSMappingAxiom(sql1.toString(), targetQuery1);
				if (!isMappingEmpty(classNode, COL_TYPE.OBJECT))
					currentMappings.add(basicmapping);
			}
			{
				/* FOR BNODE */
				
				Function body2 = dfac.getFunction(classuri, dfac.getBNodeTemplate(dfac.getVariable("X")));
				CQIE targetQuery2 = dfac.getCQIE(head, body2);
				
				StringBuilder sql2 = new StringBuilder();
				sql2.append(classTable.selectCommand);
				sql2.append(" ISBNODE = TRUE AND ");
				appendIntervalString(intervals, sql2);

				OBDAMappingAxiom  basicmapping = dfac.getRDBMSMappingAxiom(sql2.toString(), targetQuery2);
				if (!isMappingEmpty(classNode, COL_TYPE.BNODE))
					currentMappings.add(basicmapping);
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
		/*
		 * Collecting the result
		 */
		Collection<OBDAMappingAxiom> result = new LinkedList<OBDAMappingAxiom>();
		for (Predicate predicate : mappings.keySet()) {
			log.debug("Predicate: {} Mappings: {}", predicate, mappings.get(predicate).size());
			result.addAll(mappings.get(predicate));
		}
		log.debug("Total: {} mappings", result.size());
		return result;
	}

	/***
	 * @param iri
	 * @param type1
	 * @return
	 */
	private boolean isMappingEmpty(OClass concept, COL_TYPE type1) {

		SITable table = SITable.CLASS;

		boolean empty = true;
		List<Interval> intervals = cacheSI.getIntervals(concept);

		for (Interval interval : intervals) {
			for (int i = interval.getStart(); i <= interval.getEnd(); i++) {

				SemanticIndexRecord record = new SemanticIndexRecord(table, type1, COL_TYPE.OBJECT, i);
				empty = empty && !nonEmptyEntityRecord.contains(record);
				if (!empty)
					break;
			}
			if (!empty)
				break;
		}

		return empty;
	}

	/***
	 * @param iri
	 * @param type1
	 * @param type2
	 * @return
	 */
	private boolean isMappingEmpty(String iri, COL_TYPE type1, COL_TYPE type2) {

		SITable table = SemanticIndexRecord.COLTYPEtoSITable.get(type2);			

		boolean empty = true;
		List<Interval> intervals = cacheSI.getRoleIntervals(iri);

		for (Interval interval : intervals) {
			for (int i = interval.getStart(); i <= interval.getEnd(); i++) {

				SemanticIndexRecord record = new SemanticIndexRecord(table, type1, type2, i);
				empty = empty && !nonEmptyEntityRecord.contains(record);
				if (!empty)
					break;
			}
			if (!empty)
				break;
		}

		return empty;
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
		if (type2 == COL_TYPE.BNODE) 
			objectTerm = dfac.getBNodeTemplate(Y); 
		else if (type2 == COL_TYPE.OBJECT) 
			objectTerm = dfac.getUriTemplate(Y);
		else if (type2 == COL_TYPE.LITERAL_LANG) { 
			objectTerm = dfac.getTypedTerm(Y, dfac.getVariable("Z"));
		} 
		else {
			if (type2 == COL_TYPE.DATE || type2 == COL_TYPE.TIME || type2 == COL_TYPE.YEAR) {
				// R: the three types below were not covered by the switch
				throw new RuntimeException("Unsuported type: " + type2);
			}
			objectTerm = dfac.getTypedTerm(Y, type2);
		}

		Function body = dfac.getFunction(predicate, subjectTerm, objectTerm);
		return dfac.getCQIE(head, body);
	}

	private String constructSourceQuery(Predicate predicate, COL_TYPE type1, COL_TYPE type2) throws OBDAException {
		StringBuilder sql = new StringBuilder();
		switch (type2) {
			case OBJECT:
			case BNODE:
				sql.append(select_mapping_attribute.get(COL_TYPE.OBJECT));
				break;
			case LITERAL:
			case LITERAL_LANG:
				sql.append(select_mapping_attribute.get(COL_TYPE.LITERAL));
				break;
			default:
				sql.append(select_mapping_attribute.get(type2));
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

		List<Interval> intervals = cacheSI.getRoleIntervals(predicate.getName());
		if (intervals == null)
			throw new OBDAException("Could not create mapping for predicate: " + predicate.getName()
					+ ". Couldn not find semantic index intervals for the predicate.");
		
		appendIntervalString(intervals, sql);

		return sql.toString();
	}
	

	private void appendIntervalString(List<Interval> intervals, StringBuilder sql) {
		if (intervals.size() > 1)
			sql.append("(");
		sql.append(getIntervalString0(intervals.get(0)));

		for (int intervali = 1; intervali < intervals.size(); intervali++) {
			sql.append(" OR ");
			sql.append(getIntervalString0(intervals.get(intervali)));
		}
		if (intervals.size() > 1)
			sql.append(")");	
	}
	
	private String getIntervalString0(Interval interval) {
		if (interval.getStart() == interval.getEnd()) {
			return String.format(whereSingleCondition, interval.getStart());
		} else {
			return String.format(whereIntervalCondition, interval.getStart(), interval.getEnd());
		}
	}

	public void collectStatistics(Connection conn) throws SQLException {

		Statement st = conn.createStatement();

		st.addBatch("ANALYZE");

		st.executeBatch();
		st.close();

	}


	/***
	 * Inserts the metadata about semantic indexes and intervals into the
	 * database. The metadata is later used to reconstruct a semantic index
	 * repository.
	 */
	public void insertMetadata(Connection conn) throws SQLException {

		log.debug("Inserting semantic index metadata. This will allow the repository to reconstruct itself afterwards.");

		boolean commitval = conn.getAutoCommit();

		conn.setAutoCommit(false);

		PreparedStatement stm = conn.prepareStatement(indexTable.insertCommand);
		Statement st = conn.createStatement();

		try {

			/* dropping previous metadata */

			st.executeUpdate("DELETE FROM " + indexTable.tableName);
			st.executeUpdate("DELETE FROM " + intervalTable.tableName);
			st.executeUpdate("DELETE FROM " + emptinessIndexTable.tableName);

			/* inserting index data for classes and roles */

			for (String concept : cacheSI.getIndexKeys(SemanticIndexCache.CLASS_TYPE)) {
				stm.setString(1, concept.toString());
				stm.setInt(2, cacheSI.getIndex(concept, SemanticIndexCache.CLASS_TYPE));
				stm.setInt(3, SemanticIndexCache.CLASS_TYPE);
				stm.addBatch();
			}
			stm.executeBatch();

			for (String role : cacheSI.getIndexKeys(SemanticIndexCache.ROLE_TYPE)) {
				stm.setString(1, role.toString());
				stm.setInt(2, cacheSI.getIndex(role, SemanticIndexCache.ROLE_TYPE));
				stm.setInt(3, SemanticIndexCache.ROLE_TYPE);
				stm.addBatch();
			}
			stm.executeBatch();
			stm.clearBatch();
			stm.close();

			/*
			 * Inserting interval metadata
			 */

			stm = conn.prepareStatement(intervalTable.insertCommand);
			for (String concept : cacheSI.getIntervalsKeys(SemanticIndexCache.CLASS_TYPE)) {
				for (Interval it : cacheSI.getClassIntervals(concept)) {
					stm.setString(1, concept.toString());
					stm.setInt(2, it.getStart());
					stm.setInt(3, it.getEnd());
					stm.setInt(4, SemanticIndexCache.CLASS_TYPE);
					stm.addBatch();
				}
			}
			stm.executeBatch();

			for (String role : cacheSI.getIntervalsKeys(SemanticIndexCache.ROLE_TYPE)) {
				for (Interval it : cacheSI.getRoleIntervals(role)) {
					stm.setString(1, role.toString());
					stm.setInt(2, it.getStart());
					stm.setInt(3, it.getEnd());
					stm.setInt(4, SemanticIndexCache.ROLE_TYPE);
					stm.addBatch();
				}
			}
			stm.executeBatch();

			/* Inserting emptyness index metadata */

			stm = conn.prepareStatement(emptinessIndexTable.insertCommand);
			for (SemanticIndexRecord record : nonEmptyEntityRecord) {
				stm.setInt(1, record.getTable());
				stm.setInt(2, record.getIndex());
				stm.setInt(3, record.getType1());
				stm.setInt(4, record.getType2());
				stm.addBatch();
			}
			stm.executeBatch();

			stm.close();

			conn.commit();

		} catch (SQLException e) {
			try {
				st.close();
			} catch (Exception e2) {

			}

			try {
				stm.close();
			} catch (Exception e2) {

			}
			// If there is a big error, restore everything as it was
			try {
				conn.rollback();
			} catch (Exception e2) {

			}
			conn.setAutoCommit(commitval);
			throw e;
		}

	}

	/*
	 * Utilities
	 */

	
	/**
	 *  DROP indexes	
	 */
		
	private static final String dropindexclass1 = "DROP INDEX \"idxclass1\"";
	private static final String dropindexclass2 = "DROP INDEX \"idxclass2\"";

	private static final String dropindexrole1 = "DROP INDEX \"idxrole1\"";
	private static final String dropindexrole2 = "DROP INDEX \"idxrole2\"";
	private static final String dropindexrole3 = "DROP INDEX \"idxrole3\"";
	

	public void dropIndexes(Connection conn) throws SQLException {
		log.debug("Droping indexes");

		Statement st = conn.createStatement();

		st.addBatch(dropindexclass1);
		st.addBatch(dropindexclass2);

		st.addBatch(dropindexrole1);
		st.addBatch(dropindexrole2);
		st.addBatch(dropindexrole3);

		for (String s : attributeTableLiteral.dropIndexCommands)
			st.addBatch(s);	
		
		
		for (Entry<COL_TYPE, String> entry : attribute_index.entrySet()) {
			st.addBatch("DROP INDEX " + entry.getValue() + "1");
			st.addBatch("DROP INDEX " + entry.getValue() + "2");
			st.addBatch("DROP INDEX " + entry.getValue() + "3");
		}
		
		st.executeBatch();
		st.close();

		isIndexed = false;
	}

	public boolean isIndexed(Connection conn) {
		return isIndexed;
	}

	public boolean isDBSchemaDefined(Connection conn) throws SQLException {
		Statement st = conn.createStatement();
		boolean exists = true;
		try {
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", classTable.tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", roleTable.tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attributeTableLiteral.tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attribute_table.get(COL_TYPE.STRING)));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attribute_table.get(COL_TYPE.INTEGER)));
            st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attribute_table.get(COL_TYPE.LONG)));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attribute_table.get(COL_TYPE.DECIMAL)));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attribute_table.get(COL_TYPE.DOUBLE)));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attribute_table.get(COL_TYPE.DATETIME)));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attribute_table.get(COL_TYPE.BOOLEAN)));
		} catch (SQLException e) {
			exists = false;
			log.debug(e.getMessage());
		} finally {
			try {
				st.close();
			} catch (SQLException e) {

			}
		}
		return exists;
	}

	//
	class InsertionMonitor {




	}

	

}
