package org.semanticweb.ontop.owlrefplatform.core.abox;

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

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.Predicate.COL_TYPE;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.ontology.Assertion;
import org.semanticweb.ontop.ontology.ClassExpression;
import org.semanticweb.ontop.ontology.DataPropertyAssertion;
import org.semanticweb.ontop.ontology.DataPropertyExpression;
import org.semanticweb.ontop.ontology.ObjectPropertyAssertion;
import org.semanticweb.ontop.ontology.ObjectPropertyExpression;
import org.semanticweb.ontop.ontology.ClassAssertion;
import org.semanticweb.ontop.ontology.OClass;
import org.semanticweb.ontop.ontology.OntologyFactory;
import org.semanticweb.ontop.ontology.impl.OntologyFactoryImpl;
import org.semanticweb.ontop.ontology.impl.OntologyVocabularyImpl;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

/**
 * Store ABox assertions in the DB
 * 
 */

public class RDBMSSIRepositoryManager implements Serializable {

	
	private static final long serialVersionUID = -6494667662327970606L;

	private final static Logger log = LoggerFactory.getLogger(RDBMSSIRepositoryManager.class);
	
	static final class TableDescription {
		final String tableName;
		final String createCommand;
		private final String insertCommand;
		private final String selectCommand;
		
		final List<String> createIndexCommands = new ArrayList<>(3);
		final List<String> dropIndexCommands = new ArrayList<>(3);
		
		final String dropCommand;
		
		TableDescription(String tableName, ImmutableMap<String, String> columnDefintions, String selectColumns) {
			this.tableName = tableName;
			this.dropCommand = "DROP TABLE " + tableName;
			this.createCommand = "CREATE TABLE " + tableName + 
					" ( " + Joiner.on(", ").withKeyValueSeparator(" ").join(columnDefintions) + " )";
			this.insertCommand = "INSERT INTO " + tableName + 
					" (" + Joiner.on(", ").join(columnDefintions.keySet()) + ") VALUES (";
			this.selectCommand = "SELECT " + selectColumns + " FROM " + tableName; 
		}
		
		String getINSERT(String values) {
			return insertCommand + values + ")";
		}
		
		String getSELECT(String filter) {
			return selectCommand +  " WHERE " + filter;
		}
		
		String getSELECT() {
			return selectCommand;
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
			ImmutableMap.of("URI", "VARCHAR(400)", 
					        "IDX", "INTEGER", 
					        "ENTITY_TYPE", "INTEGER"), "*");   

	private final static TableDescription intervalTable = new TableDescription("IDXINTERVAL",
			ImmutableMap.of("URI", "VARCHAR(400)", 
					        "IDX_FROM", "INTEGER", 
					        "IDX_TO", "INTEGER", 
					        "ENTITY_TYPE", "INTEGER"), "*");

	private final static TableDescription uriIdTable = new TableDescription("URIID",
			ImmutableMap.of("ID", "INTEGER", 
					        "URI", "VARCHAR(400)"), "*");
	
	final static TableDescription emptinessIndexTable = new TableDescription("NONEMPTYNESSINDEX",
			ImmutableMap.of("TABLEID", "INTEGER", 
					        "IDX", "INTEGER",
					        "TYPE1", "INTEGER", 
					        "TYPE2", "INTEGER"), "*");
	
	
	
	
	/**
	 *  Data tables
	 */
	
	final static TableDescription classTable = new TableDescription("QUEST_CLASS_ASSERTION", 
			ImmutableMap.of("\"URI\"", "INTEGER NOT NULL", 
					        "\"IDX\"", "SMALLINT NOT NULL", 
					        "ISBNODE", "BOOLEAN NOT NULL DEFAULT FALSE"), "\"URI\" as X");
	
    final static Map<COL_TYPE ,TableDescription> attributeTable = new HashMap<>();
	
	private static final class AttributeTableDescritpion {
		final COL_TYPE type;
		final String tableName;
		final String sqlTypeName;
		final String indexName;
		
		public AttributeTableDescritpion(COL_TYPE type, String tableName, String sqlTypeName, String indexName) {
			this.type = type;
			this.tableName = tableName;
			this.sqlTypeName = sqlTypeName;
			this.indexName = indexName;
		}
	}
	
	static {
				
		classTable.indexOn("idxclassfull", "URI, IDX, ISBNODE");
		classTable.indexOn("idxclassfull2", "URI, IDX");
		
		TableDescription roleTable = new TableDescription("QUEST_OBJECT_PROPERTY_ASSERTION", 
				ImmutableMap.of("\"URI1\"", "INTEGER NOT NULL", 
						        "\"URI2\"", "INTEGER NOT NULL", 
						        "\"IDX\"", "SMALLINT NOT NULL", 
						        "ISBNODE", "BOOLEAN NOT NULL DEFAULT FALSE", 
						        "ISBNODE2", "BOOLEAN NOT NULL DEFAULT FALSE"), "\"URI1\" as X, \"URI2\" as Y");
		attributeTable.put(COL_TYPE.OBJECT, roleTable);

		roleTable.indexOn("idxrolefull1", "URI1, URI2, IDX, ISBNODE, ISBNODE2");
		roleTable.indexOn("idxrolefull2",  "URI2, URI1, IDX, ISBNODE2, ISBNODE");
		roleTable.indexOn("idxrolefull22", "URI1, URI2, IDX");
	
		
		// COL_TYPE.LITERAL is special because of one extra attribute (LANG)
		
		TableDescription attributeTableLiteral = new TableDescription("QUEST_DATA_PROPERTY_LITERAL_ASSERTION",
				ImmutableMap.of("\"URI\"", "INTEGER NOT NULL", 
						        "VAL", "VARCHAR(1000) NOT NULL", 
						        "\"IDX\"", "SMALLINT NOT NULL", 
						        "LANG", "VARCHAR(20)", 
						        "ISBNODE", "BOOLEAN NOT NULL DEFAULT FALSE"), "\"URI\" as X, VAL as Y, LANG as Z");
		attributeTable.put(COL_TYPE.LITERAL, attributeTableLiteral);
			
		attributeTableLiteral.indexOn("IDX_LITERAL_ATTRIBUTE" + "1", "URI");		
		attributeTableLiteral.indexOn("IDX_LITERAL_ATTRIBUTE" + "2", "IDX");
		attributeTableLiteral.indexOn("IDX_LITERAL_ATTRIBUTE" + "3", "VAL");				

		
		// all other datatypes from COL_TYPE are treated similarly
		
		List<AttributeTableDescritpion> attributeDescritions = new ArrayList<>();
		attributeDescritions.add(new AttributeTableDescritpion(   // 1
				COL_TYPE.STRING, "QUEST_DATA_PROPERTY_STRING_ASSERTION", "VARCHAR(1000)", "IDX_STRING_ATTRIBUTE"));
		attributeDescritions.add(new AttributeTableDescritpion(   // 2
				COL_TYPE.INTEGER, "QUEST_DATA_PROPERTY_INTEGER_ASSERTION", "BIGINT", "IDX_INTEGER_ATTRIBUTE"));
		attributeDescritions.add(new AttributeTableDescritpion(   // 3
				COL_TYPE.INT, "QUEST_DATA_PROPERTY_INT_ASSERTION", "INTEGER", "XSD_INT_ATTRIBUTE"));
		attributeDescritions.add(new AttributeTableDescritpion(   // 4
				COL_TYPE.UNSIGNED_INT, "QUEST_DATA_PROPERTY_UNSIGNED_INT_ASSERTION", "INTEGER", "XSD_UNSIGNED_INT_ATTRIBUTE"));
		attributeDescritions.add(new AttributeTableDescritpion(   // 5
				COL_TYPE.NEGATIVE_INTEGER, "QUEST_DATA_PROPERTY_NEGATIVE_INTEGER_ASSERTION", "BIGINT", "XSD_NEGATIVE_INTEGER_ATTRIBUTE"));
		attributeDescritions.add(new AttributeTableDescritpion(   // 6
				COL_TYPE.NON_NEGATIVE_INTEGER, "QUEST_DATA_PROPERTY_NON_NEGATIVE_INTEGER_ASSERTION", "BIGINT", "XSD_NON_NEGATIVE_INTEGER_ATTRIBUTE"));
		attributeDescritions.add(new AttributeTableDescritpion(   // 7
				COL_TYPE.POSITIVE_INTEGER, "QUEST_DATA_PROPERTY_POSITIVE_INTEGER_ASSERTION", "BIGINT", "XSD_POSITIVE_INTEGER_ATTRIBUTE"));
		attributeDescritions.add(new AttributeTableDescritpion(   // 8
				COL_TYPE.NON_POSITIVE_INTEGER, "QUEST_DATA_PROPERTY_NON_POSITIVE_INTEGER_ASSERTION", "BIGINT", "XSD_NON_POSITIVE_INTEGER_ATTRIBUTE"));
		attributeDescritions.add(new AttributeTableDescritpion(   // 9
				COL_TYPE.LONG, "QUEST_DATA_PROPERTY_LONG_ASSERTION", "BIGINT", "IDX_LONG_ATTRIBUTE"));
		attributeDescritions.add(new AttributeTableDescritpion(   // 10
				COL_TYPE.DECIMAL, "QUEST_DATA_PROPERTY_DECIMAL_ASSERTION", "DECIMAL", "IDX_DECIMAL_ATTRIBUTE"));
		attributeDescritions.add(new AttributeTableDescritpion(   // 11
				COL_TYPE.FLOAT, "QUEST_DATA_PROPERTY_FLOAT_ASSERTION", "DOUBLE PRECISION", "XSD_FLOAT_ATTRIBUTE"));
		attributeDescritions.add(new AttributeTableDescritpion(   // 12
				COL_TYPE.DOUBLE, "QUEST_DATA_PROPERTY_DOUBLE_ASSERTION", "DOUBLE PRECISION", "IDX_DOUBLE_ATTRIBUTE"));
		attributeDescritions.add(new AttributeTableDescritpion(   // 13
				COL_TYPE.DATETIME, "QUEST_DATA_PROPERTY_DATETIME_ASSERTION", "TIMESTAMP", "IDX_DATETIME_ATTRIBUTE"));
		attributeDescritions.add(new AttributeTableDescritpion(   // 14
				COL_TYPE.BOOLEAN,  "QUEST_DATA_PROPERTY_BOOLEAN_ASSERTION", "BOOLEAN", "IDX_BOOLEAN_ATTRIBUTE"));
		attributeDescritions.add(new AttributeTableDescritpion(   // 15
				COL_TYPE.DATETIME_STAMP, "QUEST_DATA_PROPERTY_DATETIMESTAMP_ASSERTION", "TIMESTAMP", "IDX_DATETIMESTAMP_ATTRIBUTE"));
		
		for (AttributeTableDescritpion descrtiption : attributeDescritions) {
			TableDescription table = new TableDescription(descrtiption.tableName,
					ImmutableMap.of("\"URI\"", "INTEGER NOT NULL", 
							        "VAL", descrtiption.sqlTypeName, 
							        "\"IDX\"", "SMALLINT  NOT NULL", 
							        "ISBNODE", "BOOLEAN NOT NULL DEFAULT FALSE"), "\"URI\" as X, VAL as Y");
			attributeTable.put(descrtiption.type, table);
			
			table.indexOn(descrtiption.indexName + "1", "URI");		
			table.indexOn(descrtiption.indexName + "2", "IDX");
			table.indexOn(descrtiption.indexName + "3", "VAL");
		}
	}
	

	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	
	private final SemanticIndexURIMap uriMap = new SemanticIndexURIMap();
	
	private final TBoxReasoner reasonerDag;
	
	private SemanticIndexCache cacheSI;
	
	private boolean isIndexed;  // database index created

	private SemanticIndexViewsManager views = new SemanticIndexViewsManager();
	
	private final List<RepositoryChangedListener> changeList = new LinkedList<>();

	public RDBMSSIRepositoryManager(TBoxReasoner reasonerDag) {
		this.reasonerDag = reasonerDag;
	}

	public void generateMetadata() {
		cacheSI = new SemanticIndexCache(reasonerDag);
		cacheSI.buildSemanticIndexFromReasoner();		
	}
	
	public void addRepositoryChangedListener(RepositoryChangedListener list) {
		this.changeList.add(list);
	}


	public SemanticIndexURIMap getUriMap() {
		return uriMap;
	}
	


	
	
	public void createDBSchemaAndInsertMetadata(Connection conn) throws SQLException {

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
			for (Entry<COL_TYPE, TableDescription> entry : attributeTable.entrySet())
				st.addBatch(entry.getValue().createCommand);
			
			st.executeBatch();			
		}
		
		insertMetadata(conn);
	}

	private boolean isDBSchemaDefined(Connection conn) throws SQLException {
		
		try (Statement st = conn.createStatement()) {
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", classTable.tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attributeTable.get(COL_TYPE.OBJECT).tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attributeTable.get(COL_TYPE.LITERAL).tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attributeTable.get(COL_TYPE.STRING).tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attributeTable.get(COL_TYPE.INTEGER).tableName));
            st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attributeTable.get(COL_TYPE.LONG).tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attributeTable.get(COL_TYPE.DECIMAL).tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attributeTable.get(COL_TYPE.DOUBLE).tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attributeTable.get(COL_TYPE.DATETIME).tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attributeTable.get(COL_TYPE.BOOLEAN).tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attributeTable.get(COL_TYPE.DATETIME_STAMP).tableName));

			return true; // everything is fine if we get to this point
		} 
		catch (Exception e) {
			// ignore all exceptions
		}
		return false; // there was an exception if we have got here
	}
	
	
	public void dropDBSchema(Connection conn) throws SQLException {

		try (Statement st = conn.createStatement()) {
			st.addBatch(indexTable.dropCommand);
			st.addBatch(intervalTable.dropCommand);
			st.addBatch(emptinessIndexTable.dropCommand);

			st.addBatch(classTable.dropCommand);
			for (Entry<COL_TYPE, TableDescription> entry : attributeTable.entrySet())
				st.addBatch(entry.getValue().dropCommand); 
			
			st.addBatch(uriIdTable.dropCommand);

			st.executeBatch();
		}
		catch (BatchUpdateException e) {
			// no-op: ignore all exceptions here
		}
	}

	public int insertData(Connection conn, Iterator<Assertion> data, int commitLimit, int batchLimit) throws SQLException {
		log.debug("Inserting data into DB");

		// The precondition for the limit number must be greater or equal to one.
		commitLimit = (commitLimit < 1) ? 1 : commitLimit;
		batchLimit = (batchLimit < 1) ? 1 : batchLimit;

		boolean oldAutoCommit = conn.getAutoCommit();
		conn.setAutoCommit(false);

		PreparedStatement uriidStm = conn.prepareStatement(uriIdTable.getINSERT("?, ?"));
		Map<SemanticIndexViewID, PreparedStatement> stmMap = new HashMap<>();
		
		// For counting the insertion
		int success = 0;
		Map<Predicate, Integer> failures = new HashMap<>();

		int batchCount = 0;
		int commitCount = 0;

		try {
			while (data.hasNext()) {
				Assertion ax = data.next();

				// log.debug("Inserting statement: {}", ax);
				batchCount++;
				commitCount++;

				if (ax instanceof ClassAssertion) {
					ClassAssertion ca = (ClassAssertion) ax; 
					try {
						process(conn, ca, uriidStm, stmMap);
						success++;
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
						process(conn, opa, uriidStm, stmMap);	
						success++;
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
						process(conn, dpa, uriidStm, stmMap);				
						success++;					
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
					for (PreparedStatement stm : stmMap.values()) {
						stm.executeBatch();
						stm.clearBatch();
					}
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
			for (PreparedStatement stm : stmMap.values()) {
				stm.executeBatch();
				stm.clearBatch();
			}
			// Commit the rest of the batch insert
			conn.commit();
		}
		finally {
			// Close all open statements
			uriidStm.close();
			for (PreparedStatement stm : stmMap.values()) 
				stm.close();
		}

		conn.setAutoCommit(oldAutoCommit);

		// Print the monitoring log
		log.debug("Total successful insertions: " + success + ".");
		int totalFailures = 0;
		for (Map.Entry<Predicate, Integer> entry : failures.entrySet()) {
			log.warn("Failed to insert data for predicate {} ({} tuples).", entry.getKey(), entry.getValue());
			totalFailures += entry.getValue();
		}
		if (totalFailures > 0) {
			log.warn("Total failed insertions: " + totalFailures + ". (REASON: datatype mismatch between the ontology and database).");
		}

		/*
		 * fired ONLY when new data is inserted and emptiness index is updated
		 * (this is done in order to update T-mappings)
		 */

		for (RepositoryChangedListener listener : changeList) 
			listener.repositoryChanged();

		return success;
	}


	private void process(Connection conn, ObjectPropertyAssertion ax, PreparedStatement uriidStm, Map<SemanticIndexViewID, PreparedStatement> stmMap) throws SQLException {

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
		

		SemanticIndexView view = views.getView(o1.getType(), o2.getType());
		
		PreparedStatement stm = stmMap.get(view.getId());
		if (stm == null) {
			stm = conn.prepareStatement(view.getINSERT());
			stmMap.put(view.getId(), stm);
		}
		
		int uri_id = getObjectConstantUriId(o1, uriidStm);
		int uri2_id = getObjectConstantUriId(o2, uriidStm);
		
		// Construct the database INSERT statements		
		stm.setInt(1, uri_id);
		stm.setInt(2, uri2_id);		
		stm.setInt(3, idx);
		stm.addBatch();
		
		// Register non emptiness
		view.addIndex(idx);
	} 

	private void process(Connection conn, DataPropertyAssertion ax, PreparedStatement uriidStm, Map<SemanticIndexViewID, PreparedStatement> stmMap) throws SQLException {

		// replace the property by its canonical representative 
		DataPropertyExpression dpe0 = ax.getProperty();
		DataPropertyExpression dpe = reasonerDag.getDataPropertyDAG().getVertex(dpe0).getRepresentative();		
		int idx = cacheSI.getEntry(dpe).getIndex();
		
		ObjectConstant subject = ax.getSubject();
		
		ValueConstant object = ax.getValue();
		COL_TYPE objectType = object.getType();
		
		SemanticIndexView view =  views.getView(subject.getType(), objectType);
		PreparedStatement stm = stmMap.get(view.getId());
		if (stm == null) {
			stm = conn.prepareStatement(view.getINSERT());
			stmMap.put(view.getId(), stm);
		}

		int uri_id = getObjectConstantUriId(subject, uriidStm);
		stm.setInt(1, uri_id);
		
		String value = object.getValue();
		
		switch (objectType) {
			case LITERAL:  // 0
				stm.setString(2, value);
				break;  
			case LITERAL_LANG:  // -3
				stm.setString(2, value);
				stm.setString(4, object.getLanguage());
				break;  
			case STRING:   // 1
				stm.setString(2, value);
				break;
	        case INT:   // 3
	            //if (value.charAt(0) == '+') // ROMAN: not needed in Java 7
	            //    value = value.substring(1, value.length());
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
	            //if (value.charAt(0) == '+')  // ROMAN: not needed in Java 7
	            //    value = value.substring(1, value.length());
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
			case DATETIME_STAMP: // 15
			case DATETIME: // 13
				stm.setTimestamp(2, XsdDatatypeConverter.parseXsdDateTime(value));
				break;
			case BOOLEAN: // 14				
				stm.setBoolean(2, XsdDatatypeConverter.parseXsdBoolean(value));
				break;
			default:
				// UNSUPPORTED DATATYPE
				log.warn("Ignoring assertion: {}", ax);			
				return;				
		}
		
		stm.setInt(3, idx);
		stm.addBatch();
		
		// register non-emptiness
		view.addIndex(idx);
	}
	
		
	private void process(Connection conn, ClassAssertion ax, PreparedStatement uriidStm, Map<SemanticIndexViewID, PreparedStatement> stmMap) throws SQLException {
		
		// replace concept by the canonical representative (which must be a concept name)
		OClass concept0 = ax.getConcept();
		OClass concept = (OClass)reasonerDag.getClassDAG().getVertex(concept0).getRepresentative();	
		int conceptIndex = cacheSI.getEntry(concept).getIndex();	

		ObjectConstant c1 = ax.getIndividual();

		SemanticIndexView view =  views.getView(c1.getType());
	
		PreparedStatement stm = stmMap.get(view.getId());
		if (stm == null) {
			stm = conn.prepareStatement(view.getINSERT());
			stmMap.put(view.getId(), stm);
		}

		int uri_id = getObjectConstantUriId(c1, uriidStm); 
		
		// Construct the database INSERT statements
		stm.setInt(1, uri_id);
		stm.setInt(2, conceptIndex);
		stm.addBatch();
	
		// Register non emptiness
		view.addIndex(conceptIndex);
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
		views = new SemanticIndexViewsManager();

		// Fetching the index data 
		Statement st = conn.createStatement();
		ResultSet res = st.executeQuery(indexTable.getSELECT() + " ORDER BY IDX");
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
		
		
		// Fetching the intervals data, note that a given String can have one or
		// more intervals (a set) hence we need to go through several rows to
		// collect all of them. To do this we sort the table by URI (to get all
		// the intervals for a given String in sequence), then we collect all the
		// intervals row by row until we change URI, at that switch we store the
		// interval

		res = st.executeQuery(intervalTable.getSELECT() + " ORDER BY URI, ENTITY_TYPE");

		List<Interval> currentSet = null;
		int previousType = 0;
		String previousString = null;
		while (res.next()) {
			String iri = res.getString(1);
			if (iri.startsWith("file:/"))   // ROMAN: what is this?
				continue;

			int type = res.getInt(4);

			if (previousString == null) { // very first row
				currentSet = new LinkedList<>();
				previousType = type;
				previousString = iri;
			}

			if ((!iri.equals(previousString) || previousType != type)) {
				 // we switched URI or type, time to store the collected
				 // intervals and clear the set
				setIntervals(previousString, previousType, currentSet, maxObjectPropertyIndex);

				currentSet = new LinkedList<>();
				previousType = type;
				previousString = iri;
			}

			int low = res.getInt(2);
			int high = res.getInt(3);
			currentSet.add(new Interval(low, high));
		}

		setIntervals(previousString, previousType, currentSet, maxObjectPropertyIndex);

		res.close();

		views.load(conn);
	}

	
	public ImmutableList<OBDAMappingAxiom> getMappings() throws OBDAException {

		List<OBDAMappingAxiom> result = new LinkedList<>();

		/*
		 * PART 2: Creating the mappings
		 * 
		 * Note, at every step we always use the pureIsa dag to get the indexes
		 * and ranges for each class.
		 */


		for (Equivalences<ObjectPropertyExpression> set: reasonerDag.getObjectPropertyDAG()) {

			ObjectPropertyExpression ope = set.getRepresentative();
			// only named roles are mapped
			if (ope.isInverse()) 
				continue;
			
			// We need to make sure we make no mappings for Auxiliary roles
			// introduced by the Ontology translation process.
			if (OntologyVocabularyImpl.isAuxiliaryProperty(ope)) 
				continue;

			SemanticIndexRange range = cacheSI.getEntry(ope);
			if (range == null) {
				log.debug("Object property " + ope + " has no SemanticIndexRange");
				return null;
			}
			List<Interval> intervals = range.getIntervals();	
			String intervalsSqlFilter = getIntervalString(intervals);
			
			
			/***
			 * Generating one mapping for each supported cases, i.e., the second
			 * component is an object, or one of the supported datatypes. For
			 * each case we will construct 1 target query, one source query and
			 * the mapping axiom.
			 * 
			 * The resulting mapping will be added to the list. In this way,
			 * each property can act as an object or data property of any type.
			 */
			
			for (SemanticIndexView view : views.getPropertyViews()) {
				if (view.isEmptyForIntervals(intervals))
					continue;
				
				String sourceQuery = view.getSELECT(intervalsSqlFilter);
				CQIE targetQuery = constructTargetQuery(ope.getPredicate(), view.getId().getType1(), view.getId().getType2());
				OBDAMappingAxiom basicmapping = dfac.getMappingAxiom(dfac.getSQLQuery(sourceQuery), targetQuery);
				result.add(basicmapping);		
			}
		}

		for (Equivalences<DataPropertyExpression> set: reasonerDag.getDataPropertyDAG()) {

			DataPropertyExpression dpe = set.getRepresentative();
			
			// We need to make sure we make no mappings for Auxiliary roles
			// introduced by the Ontology translation process.
			if (OntologyVocabularyImpl.isAuxiliaryProperty(dpe)) 
				continue;
			
			SemanticIndexRange range = cacheSI.getEntry(dpe);
			if (range == null) {
				log.debug("Data property " + dpe + " has no SemanticIndexRange");
				return null;
			}
			List<Interval> intervals = range.getIntervals();
			String intervalsSqlFilter = getIntervalString(intervals);
			
		
			/***
			 * Generating one mapping for each supported cases, i.e., the second
			 * component is an object, or one of the supported datatypes. For
			 * each case we will construct 1 target query, one source query and
			 * the mapping axiom.
			 * 
			 * The resulting mapping will be added to the list. In this way,
			 * each property can act as an object or data property of any type.
			 */
			
			for (SemanticIndexView view : views.getPropertyViews()) {
				if (view.isEmptyForIntervals(intervals))
					continue;
				
				String sourceQuery = view.getSELECT(intervalsSqlFilter);
				CQIE targetQuery = constructTargetQuery(dpe.getPredicate(), view.getId().getType1(), view.getId().getType2());
				OBDAMappingAxiom basicmapping = dfac.getMappingAxiom(dfac.getSQLQuery(sourceQuery), targetQuery);
				result.add(basicmapping);			
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
			String intervalsSqlFilter = getIntervalString(intervals);

			for (SemanticIndexView view : views.getClassViews()) {
				if (view.isEmptyForIntervals(intervals))
					continue;
				
				String sourceQuery = view.getSELECT(intervalsSqlFilter);
				CQIE targetQuery = constructTargetQuery(classNode.getPredicate(), view.getId().getType1());
				OBDAMappingAxiom basicmapping = dfac.getMappingAxiom(dfac.getSQLQuery(sourceQuery), targetQuery);
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
				OBDAMappingAxiom mergedMapping = dfac.getMappingAxiom(newSQL.toString(), targetQuery);
				currentMappings.clear();
				currentMappings.add(mergedMapping);
			}
		}
		*/
		log.debug("Total: {} mappings", result.size());
		return ImmutableList.copyOf(result);
	}

	
	private CQIE constructTargetQuery(Predicate predicate, COL_TYPE type) {

		Variable X = dfac.getVariable("X");

		Predicate headPredicate = dfac.getPredicate("m", new COL_TYPE[] { COL_TYPE.OBJECT });
		Function head = dfac.getFunction(headPredicate, X);

		Function subjectTerm;
		if (type == COL_TYPE.OBJECT) 
			subjectTerm = dfac.getUriTemplate(X);
		else {
			assert (type == COL_TYPE.BNODE); 
			subjectTerm = dfac.getBNodeTemplate(X);
		}
		
		Function body = dfac.getFunction(predicate, subjectTerm);
		return dfac.getCQIE(head, body);
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

	
	
	/**
	 * Generating the interval conditions for semantic index
	 */
	
	private static String getIntervalString(final List<Interval> intervals) {
		
		if (intervals.size() == 1)
			return getIntervalString(intervals.iterator().next());
		
		else if (intervals.size() > 1) { 
			StringBuilder sql = new StringBuilder();
			
			sql.append("(");
			Joiner.on(" OR ").appendTo(sql, new Iterator<String>() {		
				private final Iterator<Interval> it = intervals.iterator();

				@Override
				public boolean hasNext() { return it.hasNext(); }

				@Override
				public String next() {
					Interval interval = it.next();
					return getIntervalString(interval);
				}

				@Override
				public void remove() { }
			});
			sql.append(")");
			
			return sql.toString();
		}
		else // if the size is 0
			return "";
	}
	

	private static String getIntervalString(Interval interval) {
		if (interval.getStart() == interval.getEnd()) 
			return String.format("IDX = %d", interval.getStart());
		else 
			return String.format("IDX >= %d AND IDX <= %d", interval.getStart(), interval.getEnd());		
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
			try (PreparedStatement stm = conn.prepareStatement(indexTable.getINSERT("?, ?, ?"))) {
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
			try (PreparedStatement stm = conn.prepareStatement(intervalTable.getINSERT("?, ?, ?, ?"))) {
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

			views.store(conn);
			
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

	
	
	
	
	public void createIndexes(Connection conn) throws SQLException {
		log.debug("Creating indexes");
		try (Statement st = conn.createStatement()) {
			for (Entry<COL_TYPE, TableDescription> entry : attributeTable.entrySet())
				for (String s : entry.getValue().createIndexCommands)
					st.addBatch(s);
						
			for (String s : classTable.createIndexCommands)
				st.addBatch(s);
			
			st.executeBatch();
			st.clearBatch();
			
			log.debug("Executing ANALYZE");
			st.addBatch("ANALYZE");
			st.executeBatch();
			
			isIndexed = true;
		}
	}
	
	/**
	 *  DROP indexes	
	 */
		

	public void dropIndexes(Connection conn) throws SQLException {
		log.debug("Dropping indexes");

		try (Statement st = conn.createStatement()) {
			for (String s : classTable.dropIndexCommands)
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
}
