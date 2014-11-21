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
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataPropertyRangeExpression;
import it.unibz.krdb.obda.ontology.DataRangeExpression;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.Datatype;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.OntologyVocabularyImpl;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticIndexRecord.OBJType;
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

	/**
	 * Metadata tables 
	 */
	private final static String index_table = "IDX";
	private final static String interval_table = "IDXINTERVAL";
	private final static String emptyness_index_table = "NONEMPTYNESSINDEX";
	private final static String uri_id_table = "URIID";

	/**
	 *  Data tables
	 */
	private static final String class_table = "QUEST_CLASS_ASSERTION";
	private static final String role_table = "QUEST_OBJECT_PROPERTY_ASSERTION";
	private static final String attribute_table_literal = "QUEST_DATA_PROPERTY_LITERAL_ASSERTION";
  
	private static final Map<COL_TYPE, String> attribute_table = new HashMap<COL_TYPE, String>();
	
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
 *  CREATE metadata tables
 */
	
	private final static String create_idx = "CREATE TABLE " + index_table + " ( " + "URI VARCHAR(400), "
			+ "IDX INTEGER, ENTITY_TYPE INTEGER" + ")";

	private final static String create_interval = "CREATE TABLE " + interval_table + " ( " + "URI VARCHAR(400), " + "IDX_FROM INTEGER, "
			+ "IDX_TO INTEGER, " + "ENTITY_TYPE INTEGER" + ")";

	private final static String create_emptyness_index = "CREATE TABLE " + emptyness_index_table + " ( TABLEID INTEGER, IDX INTEGER, "
			+ " TYPE1 INTEGER, TYPE2 INTEGER )";
	
	private final static String create_uri_id = "CREATE TABLE " + uri_id_table + " ( " + "ID INTEGER, " + "URI VARCHAR(400) " + ")";


/**
 *  INSERT metadata
 */
	
	private final static String insert_idx_query = "INSERT INTO " + index_table + "(URI, IDX, ENTITY_TYPE) VALUES(?, ?, ?)";
	private final static String uriid_insert = "INSERT INTO " + uri_id_table + "(ID, URI) VALUES(?, ?)";
	private final static String insert_interval_query = "INSERT INTO " + interval_table
			+ "(URI, IDX_FROM, IDX_TO, ENTITY_TYPE) VALUES(?, ?, ?, ?)";
	
/**
 *  CREATE data tables
 */
	
	private static final String class_table_create = "CREATE TABLE " + class_table + " ( " + "\"URI\" INTEGER NOT NULL, "
			+ "\"IDX\"  SMALLINT NOT NULL, " + " ISBNODE BOOLEAN NOT NULL DEFAULT FALSE " + ")";

	private static final String role_table_create = "CREATE TABLE " + role_table + " ( " + "\"URI1\" INTEGER NOT NULL, "
			+ "\"URI2\" INTEGER NOT NULL, " + "\"IDX\"  SMALLINT NOT NULL, " + "ISBNODE BOOLEAN NOT NULL DEFAULT FALSE, "
			+ "ISBNODE2 BOOLEAN NOT NULL DEFAULT FALSE)";

	private static final String attribute_table_literal_create = "CREATE TABLE " + attribute_table_literal + " ( "
			+ "\"URI\" INTEGER NOT NULL, " + "VAL VARCHAR(1000) NOT NULL, " + "LANG VARCHAR(20), " + "\"IDX\"  SMALLINT NOT NULL"
			+ ", ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE " + ")";
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
	
	private static final String class_insert = "INSERT INTO " + class_table + " (URI, IDX, ISBNODE) VALUES (?, ?, ?)";
	private static final String role_insert = "INSERT INTO " + role_table + " (URI1, URI2, IDX, ISBNODE, ISBNODE2) VALUES (?, ?, ?, ?, ?)";

	private static final String attribute_table_literal_insert = "INSERT INTO " + attribute_table_literal
			+ " (URI, VAL, LANG, IDX, ISBNODE) VALUES (?, ?, ?, ?, ?)";
	
	private static final Map<COL_TYPE, String> attribute_table_insert = new HashMap<COL_TYPE, String>();
	
	static {
		for (Entry<COL_TYPE, String> entry : attribute_table.entrySet()) 
			attribute_table_insert.put(entry.getKey(), "INSERT INTO " + entry.getValue() 
											+ " (URI, VAL, IDX, ISBNODE) VALUES (?, ?, ?, ?)");
	}
	

/**
 *  Indexes
 */
	
	private static final String indexclass_composite = "CREATE INDEX idxclassfull ON " + class_table + " (URI, IDX, ISBNODE)";
	private static final String indexrole_composite1 = "CREATE INDEX idxrolefull1 ON " + role_table + " (URI1, URI2, IDX, ISBNODE, ISBNODE2)";
	private static final String indexrole_composite2 = "CREATE INDEX idxrolefull2 ON " + role_table + " (URI2, URI1, IDX, ISBNODE2, ISBNODE)";

	private static final String indexclassfull2 = "CREATE INDEX idxclassfull2 ON " + class_table + " (URI, IDX)";
	private static final String indexrolefull22 = "CREATE INDEX idxrolefull22 ON " + role_table + " (URI1, URI2, IDX)";

	private static final String attribute_index_literal = "IDX_LITERAL_ATTRIBUTE";

	private static final Map<COL_TYPE, String> attribute_index = new HashMap<COL_TYPE, String>();
	
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
	

	


	
	
	
	private static final String select_mapping_class = "SELECT \"URI\" as X FROM " + class_table;

	private static final Map<COL_TYPE, String> select_mapping_attribute = new HashMap<COL_TYPE, String>();
	
	static {
		// two special cases
		select_mapping_attribute.put(COL_TYPE.OBJECT, "SELECT \"URI1\" as X, \"URI2\" as Y FROM " + role_table);
		select_mapping_attribute.put(COL_TYPE.LITERAL, "SELECT \"URI\" as X, VAL as Y, LANG as Z FROM " + attribute_table_literal);
		//
		for (Entry<COL_TYPE, String> entry : attribute_table.entrySet()) 
			select_mapping_attribute.put(entry.getKey(),  "SELECT \"URI\" as X, VAL as Y FROM " + entry.getValue());  			
	}
	
	
	private static final String whereSingleCondition = "IDX = %d";
	private static final String whereIntervalCondition = "IDX >= %d AND IDX <= %d";
	

	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

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
	


	public void createDBSchema(Connection conn, boolean dropExisting) throws SQLException {

		log.debug("Creating data tables");

		Statement st = conn.createStatement();

		if (dropExisting) {
			try {
				log.debug("Droping existing tables");
				dropDBSchema(conn);
			} catch (SQLException e) {
				log.debug(e.getMessage(), e);
			}
		}

		if (isDBSchemaDefined(conn)) {
			log.debug("Schema already exists. Skipping creation");
			return;
		}
		st.addBatch(create_uri_id);
		
		st.addBatch(create_idx);
		st.addBatch(create_interval);
		st.addBatch(create_emptyness_index);

		st.addBatch(class_table_create);
		st.addBatch(role_table_create);

		st.addBatch(attribute_table_literal_create);
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
		
		st.addBatch("CREATE INDEX " + attribute_index_literal + "1 ON " + attribute_table_literal + " (URI)");		
		st.addBatch("CREATE INDEX " + attribute_index_literal + "2 ON " + attribute_table_literal + " (IDX)");
		st.addBatch("CREATE INDEX " + attribute_index_literal + "3 ON " + attribute_table_literal + " (VAL)");			
		

		for (Entry<COL_TYPE, String> entry : attribute_index.entrySet()) {
			st.addBatch("CREATE INDEX " + entry.getValue() + "1 ON " + attribute_table.get(entry.getKey()) + " (URI)");		
			st.addBatch("CREATE INDEX " + entry.getValue() + "2 ON " + attribute_table.get(entry.getKey()) + " (IDX)");
			st.addBatch("CREATE INDEX " + entry.getValue() + "3 ON " + attribute_table.get(entry.getKey()) + " (VAL)");
		}
		
		st.addBatch(indexclass_composite);
		st.addBatch(indexrole_composite1);
		st.addBatch(indexrole_composite2);
		
		st.addBatch(indexclassfull2);
		st.addBatch(indexrolefull22);
		
		st.executeBatch();
		
		log.debug("Executing ANALYZE");
		st.addBatch("ANALYZE");
		st.executeBatch();
		
		st.close();

		isIndexed = true;
	}

	
	private final static String drop_idx = "DROP TABLE " + index_table;
	private final static String drop_interval = "DROP TABLE " + interval_table;
	private final static String drop_emptyness = "DROP TABLE " + emptyness_index_table;
	private final static String drop_uri_id = "DROP TABLE " + uri_id_table;
	
	private static final String class_table_drop = "DROP TABLE " + class_table;
	private static final String role_table_drop = "DROP TABLE " + role_table;
	private static final String attribute_table_literal_drop = "DROP TABLE " + attribute_table_literal;
		
	
	public void dropDBSchema(Connection conn) throws SQLException {

		Statement st = conn.createStatement();

		st.addBatch(drop_idx);
		st.addBatch(drop_interval);
		st.addBatch(drop_emptyness);

		st.addBatch(class_table_drop);
		st.addBatch(role_table_drop);
		st.addBatch(attribute_table_literal_drop);
		
		for (Entry<COL_TYPE, String> entry : attribute_table.entrySet())
			st.addBatch("DROP TABLE " + entry.getValue()); 
		
		st.addBatch(drop_uri_id);

		st.executeBatch();
		st.close();
	}

	public int insertData(Connection conn, Iterator<Assertion> data, int commitLimit, int batchLimit) throws SQLException {
		log.debug("Inserting data into DB");

		// The precondition for the limit number must be greater or equal to
		// one.
		commitLimit = (commitLimit < 1) ? 1 : commitLimit;
		batchLimit = (batchLimit < 1) ? 1 : batchLimit;

		boolean oldAutoCommit = conn.getAutoCommit();
		conn.setAutoCommit(false);

		// Create the insert statement for all assertions (i.e, concept, role
		// and attribute)
		PreparedStatement uriidStm = conn.prepareStatement(uriid_insert);
		PreparedStatement classStm = conn.prepareStatement(class_insert);
		PreparedStatement roleStm = conn.prepareStatement(role_insert);
		PreparedStatement attributeLiteralStm = conn.prepareStatement(attribute_table_literal_insert);
		
		Map<COL_TYPE, PreparedStatement> attributeStm = new HashMap<COL_TYPE, PreparedStatement>();
		for (Entry<COL_TYPE, String> entry : attribute_table_insert.entrySet()) {
			PreparedStatement stm = conn.prepareStatement(entry.getValue());
			attributeStm.put(entry.getKey(), stm);
		}
		
		// For counting the insertion
		InsertionMonitor monitor = new InsertionMonitor();

		int batchCount = 0;
		int commitCount = 0;

		while (data.hasNext()) {
			Assertion ax = data.next();

			try {

				// log.debug("Inserting statement: {}", ax);
				batchCount += 1;
				commitCount += 1;

				addPreparedStatement(uriidStm, classStm, roleStm, attributeLiteralStm, attributeStm, monitor, ax);

				/*
				 * Register non emptyness
				 */
				int index = 0;
				if (ax instanceof ClassAssertion) {
					index = cacheSI.getIndex(((ClassAssertion) ax).getConcept());
				} 
				else if (ax instanceof ObjectPropertyAssertion) {
					index = cacheSI.getIndex(((ObjectPropertyAssertion)ax).getProperty());
				}
				else /* (ax instanceof DataPropertyAssertion) */ {
					index = cacheSI.getIndex(((DataPropertyAssertion)ax).getProperty());
				}
				SemanticIndexRecord record = SemanticIndexRecord.getRecord(ax, index);
				nonEmptyEntityRecord.add(record);

			} catch (Exception e) {
				if (ax instanceof ClassAssertion) {
					monitor.fail(((ClassAssertion)ax).getConcept().getPredicate());
				} 
				else if (ax instanceof ObjectPropertyAssertion) {
					monitor.fail(((ObjectPropertyAssertion)ax).getProperty().getPredicate());
				}
				else /* if (ax instanceof DataPropertyAssertion)*/ {
					monitor.fail(((DataPropertyAssertion)ax).getProperty().getPredicate());
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
		monitor.printLog();

		fireRepositoryChanged();

		return monitor.getSuccessCount();
	}

	private void fireRepositoryChanged() {
		for (RepositoryChangedListener listener : changeList) {
			listener.repositoryChanged();
		}
	}

	private void addPreparedStatement(PreparedStatement uriidStm, PreparedStatement classStm, PreparedStatement roleStm, PreparedStatement attributeLiteralStm,
			Map<COL_TYPE, PreparedStatement> attributeStatement,
			InsertionMonitor monitor, Assertion ax) throws SQLException {
		int uri_id = 0;
		int uri2_id = 0;
//		boolean newUri = false;
		if (ax instanceof ObjectPropertyAssertion) {
			// Get the data property assertion
			ObjectPropertyAssertion attributeAssertion = (ObjectPropertyAssertion) ax;
			ObjectPropertyExpression prop = attributeAssertion.getProperty();

			ObjectConstant object = attributeAssertion.getObject();

			// Construct the database INSERT statements
			ObjectConstant subject = attributeAssertion.getSubject();

			String uri = subject.getName();
			 uri_id = uriMap.idOfURI(uri);
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

				uri2_id = uriMap.idOfURI(uri2);
				uriidStm.setInt(1, uri2_id);
				uriidStm.setString(2, uri2);
				uriidStm.addBatch();
				
				//roleStm.setString(1, uri);
				//roleStm.setString(2, uri2);
				roleStm.setInt(1, uri_id);
				roleStm.setInt(2, uri2_id);
				
				
				roleStm.setInt(3, idx);
				roleStm.setBoolean(4, c1isBNode);
				roleStm.setBoolean(5, c2isBNode);
				roleStm.addBatch();

				// log.debug("role");

				// log.debug("inserted: {} {}", uri, uri2);
				// log.debug("inserted: {} property", idx);

			monitor.success(); // advanced the success counter

		} 
		else if (ax instanceof DataPropertyAssertion) {
			// Get the data property assertion
			DataPropertyAssertion attributeAssertion = (DataPropertyAssertion) ax;
			DataPropertyExpression prop = attributeAssertion.getProperty();

			ValueConstant object = attributeAssertion.getValue();
			Predicate.COL_TYPE attributeType = object.getType();

			// Construct the database INSERT statements
			ObjectConstant subject = attributeAssertion.getSubject();

			String uri = subject.getName();
			 uri_id = uriMap.idOfURI(uri);
				uriidStm.setInt(1, uri_id);
				uriidStm.setString(2, uri);
				uriidStm.addBatch();
			
			boolean c1isBNode = subject instanceof BNode;

			int idx = cacheSI.getIndex(prop);

			// The insertion is based on the datatype from TBox
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
			monitor.success(); // advanced the success counter

		} 
		else if (ax instanceof ClassAssertion) {
			// Get the class assertion
			ClassAssertion classAssertion = (ClassAssertion) ax;
			OClass concept = classAssertion.getConcept();

			// Construct the database INSERT statements
			ObjectConstant c1 = classAssertion.getIndividual();

			String uri;

			boolean c1isBNode = c1 instanceof BNode;

			if (c1isBNode)
				uri = ((BNode) c1).getName();
			else
				uri = ((URIConstant) c1).getURI().toString();

			// Construct the database INSERT statement
			uri_id = uriMap.idOfURI(uri);
			uriidStm.setInt(1, uri_id);
			uriidStm.setString(2, uri);
			uriidStm.addBatch();			

			classStm.setInt(1, uri_id);
			int conceptIndex = cacheSI.getIndex(concept);
			classStm.setInt(2, conceptIndex);
			classStm.setBoolean(3, c1isBNode);
			classStm.addBatch();

			// log.debug("inserted: {} {} class", uri, conceptIndex);

			monitor.success(); // advanced the success counter

			// log.debug("Class");
		}
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

	private BigDecimal parseBigDecimal(String value) {
		return new BigDecimal(value);
	}

	private Timestamp parseTimestamp(String lit) {
		final String[] formatStrings = { "yyyy-MM-dd HH:mm:ss.SS", "yyyy-MM-dd HH:mm:ss.S", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd",
				"yyyy-MM-dd'T'HH:mm:ssz", };

		for (String formatString : formatStrings) {
			try {
				long time = new SimpleDateFormat(formatString).parse(lit).getTime();
				Timestamp ts = new Timestamp(time);
				return ts;
			} catch (ParseException e) {
			}
		}
		return null; // the string can't be parsed to one of the datetime
						// formats.
	}

	// Attribute datatype from TBox
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

	public void loadMetadata(Connection conn) throws SQLException {
		log.debug("Loading semantic index metadata from the database *");

		cacheSI.clear();
		
		nonEmptyEntityRecord.clear();


		/* Fetching the index data */
		Statement st = conn.createStatement();
		ResultSet res = st.executeQuery("SELECT * FROM " + index_table);
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
		 * fecthing the intervals data, note that a given String can have one ore
		 * more intervals (a set) hence we need to go through several rows to
		 * collect all of them. To do this we sort the table by URI (to get all
		 * the intervals for a given String in sequence), then we collect all the
		 * intervals row by row until we change URI, at that switch we store the
		 * interval
		 */

		res = st.executeQuery("SELECT * FROM " + interval_table + " ORDER BY URI, ENTITY_TYPE");

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
		res = st.executeQuery("SELECT * FROM " + emptyness_index_table);
		while (res.next()) {
			int table = res.getInt(1);
			int type1 = res.getInt(3);
			int type2 = res.getInt(4);
			int idx = res.getInt(2);
			SemanticIndexRecord r = new SemanticIndexRecord(table, type1, type2, idx);
			nonEmptyEntityRecord.add(r);
		}

		res.close();

	}

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

			COL_TYPE type = role.getType(1);

			CQIE targetQuery;
			String sourceQuery;
			OBDAMappingAxiom basicmapping;

			/***
			 * Generating one mapping for each supported cases, i.e., the second
			 * component is an object, or one of the supported datatypes. For
			 * each case we will construct 1 target query, one source query and
			 * the mapping axiom.
			 * 
			 * The resulting mapping will be added to the list. In this way,
			 * each property can act as an object or data property of any type.
			 */

			targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.OBJECT);
			sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.OBJECT);

			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
			if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.OBJECT, 2))
				currentMappings.add(basicmapping);

			/*
			 * object, bnode object, object bnode, object bnode, bnode
			 */

			targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.BNODE);
			sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.BNODE);

			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
			if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.BNODE, 2))
				currentMappings.add(basicmapping);

			targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.OBJECT);
			sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.OBJECT);

			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
			if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.OBJECT, 2))
				currentMappings.add(basicmapping);

			targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.BNODE);
			sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.BNODE);

			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
			if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.BNODE, 2))
				currentMappings.add(basicmapping);

			/*
			 * object, type
			 */

			targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.LITERAL);
			sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.LITERAL);
			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
			if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.LITERAL, 2))
				currentMappings.add(basicmapping);

			targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.LITERAL_LANG);
			sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.LITERAL_LANG);
			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
			if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.LITERAL_LANG, 2))
				currentMappings.add(basicmapping);

			targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.BOOLEAN);
			sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.BOOLEAN);
			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
			if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.BOOLEAN, 2))
				currentMappings.add(basicmapping);

			targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.DATETIME);
			sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.DATETIME);
			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
			if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.DATETIME, 2))
				currentMappings.add(basicmapping);

			targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.DECIMAL);
			sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.DECIMAL);
			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
			if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.DECIMAL, 2))
				currentMappings.add(basicmapping);

			targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.DOUBLE);
			sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.DOUBLE);
			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
			if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.DOUBLE, 2))
				currentMappings.add(basicmapping);

			targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.INTEGER);
			sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.INTEGER);
			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
			if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.INTEGER, 2))
				currentMappings.add(basicmapping);

            targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.INT);
            sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.INT);
            basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
            if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.INT, 2))
                currentMappings.add(basicmapping);

            targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.UNSIGNED_INT);
            sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.UNSIGNED_INT);
            basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
            if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.UNSIGNED_INT, 2))
                currentMappings.add(basicmapping);

            targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.NEGATIVE_INTEGER);
            sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.NEGATIVE_INTEGER);
            basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
            if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.NEGATIVE_INTEGER, 2))
                currentMappings.add(basicmapping);

            targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.NON_NEGATIVE_INTEGER);
            sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.NON_NEGATIVE_INTEGER);
            basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
            if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.NON_NEGATIVE_INTEGER, 2))
                currentMappings.add(basicmapping);

            targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.POSITIVE_INTEGER);
            sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.POSITIVE_INTEGER);
            basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
            if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.POSITIVE_INTEGER, 2))
                currentMappings.add(basicmapping);

            targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.NON_POSITIVE_INTEGER);
            sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.NON_POSITIVE_INTEGER);
            basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
            if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.NON_POSITIVE_INTEGER, 2))
                currentMappings.add(basicmapping);

            targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.FLOAT);
            sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.FLOAT);
            basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
            if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.FLOAT, 2))
                currentMappings.add(basicmapping);

            targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.LONG);
            sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.LONG);
            basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
            if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.LONG, 2))
                currentMappings.add(basicmapping);

			targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.STRING);
			sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.STRING);
			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
			if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.STRING, 2))
				currentMappings.add(basicmapping);

			/*
			 * bnode, type
			 */

			targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.LITERAL);
			sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.LITERAL);
			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
			if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.LITERAL, 2))
				currentMappings.add(basicmapping);
			;

			targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.LITERAL_LANG);
			sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.LITERAL_LANG);
			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
			if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.LITERAL_LANG, 2))
				currentMappings.add(basicmapping);
			;

			targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.BOOLEAN);
			sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.BOOLEAN);
			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
			if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.BOOLEAN, 2))
				currentMappings.add(basicmapping);
			;

			targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.DATETIME);
			sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.DATETIME);
			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
			if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.DATETIME, 2))
				currentMappings.add(basicmapping);
			;

			targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.DECIMAL);
			sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.DECIMAL);
			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
			if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.DECIMAL, 2))
				currentMappings.add(basicmapping);
			;

			targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.DOUBLE);
			sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.DOUBLE);
			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
			if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.DOUBLE, 2))
				currentMappings.add(basicmapping);
			;

			targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.INTEGER);
			sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.INTEGER);
			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
			if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.INTEGER, 2))
				currentMappings.add(basicmapping);
			;

            targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.INT);
            sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.INT);
            basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
            if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.INT, 2))
                currentMappings.add(basicmapping);

            targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.UNSIGNED_INT);
            sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.UNSIGNED_INT);
            basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
            if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.UNSIGNED_INT, 2))
                currentMappings.add(basicmapping);

            targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.NEGATIVE_INTEGER);
            sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.NEGATIVE_INTEGER);
            basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
            if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.NEGATIVE_INTEGER, 2))
                currentMappings.add(basicmapping);

            targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.NON_NEGATIVE_INTEGER);
            sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.NON_NEGATIVE_INTEGER);
            basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
            if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.NON_NEGATIVE_INTEGER, 2))
                currentMappings.add(basicmapping);

            targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.POSITIVE_INTEGER);
            sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.POSITIVE_INTEGER);
            basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
            if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.POSITIVE_INTEGER, 2))
                currentMappings.add(basicmapping);

            targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.NON_POSITIVE_INTEGER);
            sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.NON_POSITIVE_INTEGER);
            basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
            if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.NON_POSITIVE_INTEGER, 2))
                currentMappings.add(basicmapping);

            targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.FLOAT);
            sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.FLOAT);
            basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
            if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.FLOAT, 2))
                currentMappings.add(basicmapping);
            
            targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.LONG);
            sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.LONG);
            basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
            if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.LONG, 2))
                currentMappings.add(basicmapping);
            ;

			targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.STRING);
			sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.STRING);
			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
			if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.STRING, 2))
				currentMappings.add(basicmapping);
			;

		}

		/*
		 * Creating mappings for each concept
		 */

		

		for (OClass classNode : classNodesMaps) {

			Predicate classuri = classNode.getPredicate();

			List<OBDAMappingAxiom> currentMappings = new LinkedList<OBDAMappingAxiom>();

			mappings.put(classuri, currentMappings);

			// Mapping head

			Function head = dfac.getFunction(dfac.getPredicate("m", 1), dfac.getVariable("X"));
			Function body1 = dfac.getFunction(classuri, dfac.getUriTemplate(dfac.getVariable("X")));
			Function body2 = dfac.getFunction(classuri, dfac.getBNodeTemplate(dfac.getVariable("X")));
			
			/*
			 * This target query is shared by all mappings for this class
			 */

			/* FOR URI */
			CQIE targetQuery1 = dfac.getCQIE(head, body1);

			/* FOR BNODE */
			CQIE targetQuery2 = dfac.getCQIE(head, body2);

			/*
			 * First mapping: Getting the SQL for the *BASIC* mapping using
			 * ranges
			 */

			StringBuilder sql1 = new StringBuilder();
			sql1.append(select_mapping_class);
			sql1.append(" WHERE ");
			sql1.append(" ISBNODE = FALSE AND ");

			/* FOR BNODE */

			StringBuilder sql2 = new StringBuilder();
			sql2.append(select_mapping_class);
			sql2.append(" WHERE ");
			sql2.append(" ISBNODE = TRUE AND ");


			List<Interval> intervals = cacheSI.getIntervals(classuri.getName(), 1);
			if (intervals == null) {
				log.warn("Found URI with no mappings, the ontology might not match the respository. Ill URI: {}", classuri.getName());
				continue;
			}
			appendIntervalString(intervals, sql1);


			OBDAMappingAxiom basicmapping = dfac.getRDBMSMappingAxiom(sql1.toString(), targetQuery1);
			if (!isMappingEmpty(classuri.getName(), COL_TYPE.OBJECT, COL_TYPE.OBJECT, 1))
				currentMappings.add(basicmapping);

			/* FOR BNODE */
			
			appendIntervalString(intervals, sql2);

			basicmapping = dfac.getRDBMSMappingAxiom(sql2.toString(), targetQuery2);
			if (!isMappingEmpty(classuri.getName(), COL_TYPE.BNODE, COL_TYPE.OBJECT, 1))
				currentMappings.add(basicmapping);
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
	 * We use 1 for classes 2 for properties. Tells you if there has been
	 * inserts that can make this mapping non empty.
	 * 
	 * @param iri
	 * @param type1
	 * @param type2
	 * @param classPredicate
	 * @return
	 */
	private boolean isMappingEmpty(String iri, COL_TYPE type1, COL_TYPE type2, int classPredicate) {

		OBJType t1 = null;
		OBJType t2 = OBJType.URI;

		if (type1 == COL_TYPE.OBJECT) {
			t1 = OBJType.URI;
		} else {
			t1 = OBJType.BNode;
		}

		SITable table = null;
		if (classPredicate == 1) {
			table = SITable.CLASS;
		} else if (type2 == COL_TYPE.OBJECT) {
			table = SITable.OPROP;
		} else if (type2 == COL_TYPE.BNODE) {
			table = SITable.OPROP;
			t2 = OBJType.BNode;
		} else if (type2 == COL_TYPE.BOOLEAN)
			table = SITable.DPROPBool;
		else if (type2 == COL_TYPE.DATETIME)
			table = SITable.DPROPDate;
		else if (type2 == COL_TYPE.DECIMAL)
			table = SITable.DPROPDeci;
		else if (type2 == COL_TYPE.DOUBLE)
			table = SITable.DPROPDoub;
		else if (type2 == COL_TYPE.INTEGER)
			table = SITable.DPROPInte;
        else if (type2 == COL_TYPE.INT)
            table = SITable.DPROPInt;
        else if (type2 == COL_TYPE.UNSIGNED_INT)
            table = SITable.DPROPUnsignedInt;
        else if (type2 == COL_TYPE.NEGATIVE_INTEGER)
            table = SITable.DPROPNegInte;
        else if (type2 == COL_TYPE.NON_NEGATIVE_INTEGER)
            table = SITable.DPROPNonNegInte;
        else if (type2 == COL_TYPE.POSITIVE_INTEGER)
            table = SITable.DPROPPosInte;
        else if (type2 == COL_TYPE.NON_POSITIVE_INTEGER)
            table = SITable.DPROPNonPosInte;
        else if (type2 == COL_TYPE.FLOAT)
            table = SITable.DPROPFloat;
        else if (type2 == COL_TYPE.LONG)
            table = SITable.DPROPLong;
		else if (type2 == COL_TYPE.LITERAL || type2 == COL_TYPE.LITERAL_LANG)
			table = SITable.DPROPLite;
		else if (type2 == COL_TYPE.STRING)
			table = SITable.DPROPStri;

		boolean empty = true;
		List<Interval> intervals = cacheSI.getIntervals(iri, classPredicate);

		for (Interval interval : intervals) {
			for (int i = interval.getStart(); i <= interval.getEnd(); i++) {

				SemanticIndexRecord record = new SemanticIndexRecord(table, t1, t2, i);
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
		// Initialize the predicate and term objects.
		Predicate headPredicate, bodyPredicate = null;
		List<Term> headTerms = new ArrayList<Term>();
		List<Term> bodyTerms = new ArrayList<Term>();

		List<Function> bodyAtoms = new LinkedList<Function>();

		headPredicate = dfac.getPredicate("m", new COL_TYPE[] { COL_TYPE.STRING, COL_TYPE.OBJECT });
		headTerms.add(dfac.getVariable("X"));
		headTerms.add(dfac.getVariable("Y"));

		bodyPredicate = predicate; // the body

		Function subjectTerm;
		if (type1 == COL_TYPE.OBJECT) {

			subjectTerm = dfac.getUriTemplate(dfac.getVariable("X"));

		} else if (type1 == COL_TYPE.BNODE) {

			subjectTerm = dfac.getBNodeTemplate(dfac.getVariable("X"));

		} else {
			throw new RuntimeException("Unsupported object type: " + type1);
		}
		bodyTerms.add(subjectTerm);

		Function objectTerm;
		if (type2 == COL_TYPE.BNODE) {

			objectTerm = dfac.getBNodeTemplate(dfac.getVariable("Y"));

		} 
		else if (type2 == COL_TYPE.OBJECT) {

			objectTerm = dfac.getUriTemplate(dfac.getVariable("Y"));

		} 
		else if (type2 == COL_TYPE.LITERAL_LANG) { 
			objectTerm = dfac.getTypedTerm(dfac.getVariable("Y"), dfac.getVariable("Z"));
		} 
		else {
			if (type2 == COL_TYPE.DATE || type2 == COL_TYPE.TIME || type2 == COL_TYPE.YEAR) {
				// R: the three types below were not covered by the switch
				throw new RuntimeException("Unsuported type: " + type2);
			}
			
			objectTerm = dfac.getTypedTerm(dfac.getVariable("Y"), type2);
		}
		bodyTerms.add(objectTerm);

		Function head = dfac.getFunction(headPredicate, headTerms);
		Function body = dfac.getFunction(bodyPredicate, bodyTerms);
		bodyAtoms.add(0, body);
		return dfac.getCQIE(head, bodyAtoms);
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

		sql.append(" WHERE ");

		/*
		 * If the mapping is for something of type Literal we need to add IS
		 * NULL or IS NOT NULL to the language column. IS NOT NULL might be
		 * redundant since we have another stage in Quest where we add IS NOT
		 * NULL for every variable in the head of a mapping.
		 */

		if (type1 == COL_TYPE.BNODE) {
			sql.append("ISBNODE = TRUE AND ");
		} else if (type1 == COL_TYPE.OBJECT) {
			sql.append("ISBNODE = FALSE AND ");
		}

		if (type2 == COL_TYPE.BNODE) {
			sql.append("ISBNODE2 = TRUE AND ");
		} else if (type2 == COL_TYPE.OBJECT) {
			sql.append("ISBNODE2 = FALSE AND ");
		} else if (type2 == COL_TYPE.LITERAL) {
			sql.append("LANG IS NULL AND ");
		} else if (type2 == COL_TYPE.LITERAL_LANG) {
			sql.append("LANG IS NOT NULL AND ");
		}

		/*
		 * Generating the interval conditions for semantic index
		 */

		List<Interval> intervals = cacheSI.getIntervals(predicate.getName(), 2);
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

		PreparedStatement stm = conn.prepareStatement(insert_idx_query);
		Statement st = conn.createStatement();

		try {

			/* dropping previous metadata */

			st.executeUpdate("DELETE FROM " + index_table);
			st.executeUpdate("DELETE FROM " + interval_table);
			st.executeUpdate("DELETE FROM " + emptyness_index_table);

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

			stm = conn.prepareStatement(insert_interval_query);
			for (String concept : cacheSI.getIntervalsKeys(SemanticIndexCache.CLASS_TYPE)) {
				for (Interval it : cacheSI.getIntervals(concept, SemanticIndexCache.CLASS_TYPE)) {
					stm.setString(1, concept.toString());
					stm.setInt(2, it.getStart());
					stm.setInt(3, it.getEnd());
					stm.setInt(4, SemanticIndexCache.CLASS_TYPE);
					stm.addBatch();
				}
			}
			stm.executeBatch();

			for (String role : cacheSI.getIntervalsKeys(SemanticIndexCache.ROLE_TYPE)) {
				for (Interval it : cacheSI.getIntervals(role, SemanticIndexCache.ROLE_TYPE)) {
					stm.setString(1, role.toString());
					stm.setInt(2, it.getStart());
					stm.setInt(3, it.getEnd());
					stm.setInt(4, SemanticIndexCache.ROLE_TYPE);
					stm.addBatch();
				}
			}
			stm.executeBatch();

			/* Inserting emptyness index metadata */

			stm = conn.prepareStatement("INSERT INTO " + emptyness_index_table + " (TABLEID, IDX, TYPE1, TYPE2) VALUES (?, ?, ?, ?)");
			for (SemanticIndexRecord record : nonEmptyEntityRecord) {
				stm.setInt(1, record.table.ordinal());
				stm.setInt(2, record.idx);
				stm.setInt(3, record.type1.ordinal());
				stm.setInt(4, record.type2.ordinal());
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

		st.addBatch("DROP INDEX " + attribute_index_literal + "1");	
		st.addBatch("DROP INDEX " + attribute_index_literal + "2");	
		st.addBatch("DROP INDEX " + attribute_index_literal + "3");	
		
		
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
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", class_table));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", role_table));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attribute_table_literal));
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

		private int success = 0;
		private Map<Predicate, Integer> failures = new HashMap<Predicate, Integer>();

		void success() {
			success++;
		}

		void fail(Predicate predicate) {
			Integer counter = failures.get(predicate);
			if (counter == null) {
				counter = 0;
			}
			failures.put(predicate, counter + 1);
		}

		int getSuccessCount() {
			return success;
		}

		public void printLog() {
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
		}
	}

	

}
