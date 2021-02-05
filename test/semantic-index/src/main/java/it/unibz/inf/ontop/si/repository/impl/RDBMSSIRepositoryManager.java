package it.unibz.inf.ontop.si.repository.impl;

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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQuery;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.IRIDictionary;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.Int2IRIStringFunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.utils.IDGenerator;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * Store ABox assertions in the DB
 * 
 */

public class RDBMSSIRepositoryManager {


	private final static Logger log = LoggerFactory.getLogger(RDBMSSIRepositoryManager.class);

	static final class TableDescription {
		final String tableName;
		final String createCommand;
		private final String insertCommand;
		private final String selectCommand;
		
		private final List<String> createIndexCommands = new ArrayList<>(3);
		private final List<String> dropIndexCommands = new ArrayList<>(3);
		
		//private final String dropCommand;
		
		TableDescription(String tableName, ImmutableMap<String, String> columnDefintions, String selectColumns) {
			this.tableName = tableName;
			//this.dropCommand = "DROP TABLE " + tableName;
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
	
    final static ImmutableList<TableDescription> attributeTables;
    final static TableDescription ROLE_TABLE;
	final static ImmutableMap<IRI, TableDescription> ATTRIBUTE_TABLE_MAP;
	
	private static final class AttributeTableDescritpion {
		final IRI datatypeIRI;
		final String tableName;
		final String sqlTypeName;
		final String indexName;
		
		public AttributeTableDescritpion(IRI datatypeIRI, String tableName, String sqlTypeName, String indexName) {
			this.datatypeIRI = datatypeIRI;
			this.tableName = tableName;
			this.sqlTypeName = sqlTypeName;
			this.indexName = indexName;
		}
	}

	static {
		ImmutableList.Builder<TableDescription> attributeTableBuilder = ImmutableList.builder();

		classTable.indexOn("idxclassfull", "URI, IDX, ISBNODE");
		classTable.indexOn("idxclassfull2", "URI, IDX");
		
		ROLE_TABLE = new TableDescription("QUEST_OBJECT_PROPERTY_ASSERTION",
				ImmutableMap.of("\"URI1\"", "INTEGER NOT NULL", 
						        "\"URI2\"", "INTEGER NOT NULL", 
						        "\"IDX\"", "SMALLINT NOT NULL", 
						        "ISBNODE", "BOOLEAN NOT NULL DEFAULT FALSE", 
						        "ISBNODE2", "BOOLEAN NOT NULL DEFAULT FALSE"), "\"URI1\" as X, \"URI2\" as Y");

		attributeTableBuilder.add(ROLE_TABLE);

		ROLE_TABLE.indexOn("idxrolefull1", "URI1, URI2, IDX, ISBNODE, ISBNODE2");
		ROLE_TABLE.indexOn("idxrolefull2",  "URI2, URI1, IDX, ISBNODE2, ISBNODE");
		ROLE_TABLE.indexOn("idxrolefull22", "URI1, URI2, IDX");

		ImmutableMap.Builder<IRI, TableDescription> datatypeTableMapBuilder = ImmutableMap.builder();
		// LANG_STRING is special because of one extra attribute (LANG)

		TableDescription LANG_STRING_TABLE = new TableDescription("QUEST_DATA_PROPERTY_LITERAL_ASSERTION",
				ImmutableMap.of("\"URI\"", "INTEGER NOT NULL",
						        "VAL", "VARCHAR(1000) NOT NULL",
						        "\"IDX\"", "SMALLINT NOT NULL",
						        "LANG", "VARCHAR(20)",
						        "ISBNODE", "BOOLEAN NOT NULL DEFAULT FALSE"), "\"URI\" as X, VAL as Y, LANG as Z");
		attributeTableBuilder.add(LANG_STRING_TABLE);
		datatypeTableMapBuilder.put(RDF.LANGSTRING, LANG_STRING_TABLE);
			
		LANG_STRING_TABLE.indexOn("IDX_LITERAL_ATTRIBUTE" + "1", "URI");
		LANG_STRING_TABLE.indexOn("IDX_LITERAL_ATTRIBUTE" + "2", "IDX");
		LANG_STRING_TABLE.indexOn("IDX_LITERAL_ATTRIBUTE" + "3", "VAL");

		
		// all other datatypes from COL_TYPE are treated similarly
		List<AttributeTableDescritpion> attributeDescriptions = new ArrayList<>();

		attributeDescriptions.add(new AttributeTableDescritpion(   // 1
				XSD.STRING, "QUEST_DATA_PROPERTY_STRING_ASSERTION", "VARCHAR(1000)", "IDX_STRING_ATTRIBUTE"));
		attributeDescriptions.add(new AttributeTableDescritpion(   // 2
				XSD.INTEGER, "QUEST_DATA_PROPERTY_INTEGER_ASSERTION", "BIGINT", "IDX_INTEGER_ATTRIBUTE"));
		attributeDescriptions.add(new AttributeTableDescritpion(   // 3
				XSD.INT, "QUEST_DATA_PROPERTY_INT_ASSERTION", "INTEGER", "XSD_INT_ATTRIBUTE"));
		attributeDescriptions.add(new AttributeTableDescritpion(   // 4
				XSD.UNSIGNED_INT, "QUEST_DATA_PROPERTY_UNSIGNED_INT_ASSERTION", "INTEGER", "XSD_UNSIGNED_INT_ATTRIBUTE"));
		attributeDescriptions.add(new AttributeTableDescritpion(   // 5
				XSD.NEGATIVE_INTEGER, "QUEST_DATA_PROPERTY_NEGATIVE_INTEGER_ASSERTION", "BIGINT", "XSD_NEGATIVE_INTEGER_ATTRIBUTE"));
		attributeDescriptions.add(new AttributeTableDescritpion(   // 6
				XSD.NON_NEGATIVE_INTEGER, "QUEST_DATA_PROPERTY_NON_NEGATIVE_INTEGER_ASSERTION", "BIGINT", "XSD_NON_NEGATIVE_INTEGER_ATTRIBUTE"));
		attributeDescriptions.add(new AttributeTableDescritpion(   // 7
				XSD.POSITIVE_INTEGER, "QUEST_DATA_PROPERTY_POSITIVE_INTEGER_ASSERTION", "BIGINT", "XSD_POSITIVE_INTEGER_ATTRIBUTE"));
		attributeDescriptions.add(new AttributeTableDescritpion(   // 8
				XSD.NON_POSITIVE_INTEGER, "QUEST_DATA_PROPERTY_NON_POSITIVE_INTEGER_ASSERTION", "BIGINT", "XSD_NON_POSITIVE_INTEGER_ATTRIBUTE"));
		attributeDescriptions.add(new AttributeTableDescritpion(   // 9
				XSD.LONG, "QUEST_DATA_PROPERTY_LONG_ASSERTION", "BIGINT", "IDX_LONG_ATTRIBUTE"));
		attributeDescriptions.add(new AttributeTableDescritpion(   // 10
				XSD.DECIMAL, "QUEST_DATA_PROPERTY_DECIMAL_ASSERTION", "DECIMAL", "IDX_DECIMAL_ATTRIBUTE"));
		attributeDescriptions.add(new AttributeTableDescritpion(   // 11
				XSD.FLOAT, "QUEST_DATA_PROPERTY_FLOAT_ASSERTION", "DOUBLE PRECISION", "XSD_FLOAT_ATTRIBUTE"));
		attributeDescriptions.add(new AttributeTableDescritpion(   // 12
				XSD.DOUBLE, "QUEST_DATA_PROPERTY_DOUBLE_ASSERTION", "DOUBLE PRECISION", "IDX_DOUBLE_ATTRIBUTE"));
		attributeDescriptions.add(new AttributeTableDescritpion(   // 13
				XSD.DATETIME, "QUEST_DATA_PROPERTY_DATETIME_ASSERTION", "TIMESTAMP", "IDX_DATETIME_ATTRIBUTE"));
		attributeDescriptions.add(new AttributeTableDescritpion(   // 14
				XSD.BOOLEAN,  "QUEST_DATA_PROPERTY_BOOLEAN_ASSERTION", "BOOLEAN", "IDX_BOOLEAN_ATTRIBUTE"));
		attributeDescriptions.add(new AttributeTableDescritpion(   // 15
				XSD.DATETIMESTAMP, "QUEST_DATA_PROPERTY_DATETIMESTAMP_ASSERTION", "TIMESTAMP", "IDX_DATETIMESTAMP_ATTRIBUTE"));
		
		for (AttributeTableDescritpion description : attributeDescriptions) {
			TableDescription table = new TableDescription(description.tableName,
					ImmutableMap.of("\"URI\"", "INTEGER NOT NULL", 
							        "VAL", description.sqlTypeName,
							        "\"IDX\"", "SMALLINT  NOT NULL", 
							        "ISBNODE", "BOOLEAN NOT NULL DEFAULT FALSE"), "\"URI\" as X, VAL as Y");
			
			table.indexOn(description.indexName + "1", "URI");
			table.indexOn(description.indexName + "2", "IDX");
			table.indexOn(description.indexName + "3", "VAL");

			attributeTableBuilder.add(table);
			datatypeTableMapBuilder.put(description.datatypeIRI, table);
		}

		attributeTables = attributeTableBuilder.build();
		ATTRIBUTE_TABLE_MAP = datatypeTableMapBuilder.build();
	}
	
	private final SemanticIndexURIMap uriMap;
	
	private final ClassifiedTBox reasonerDag;

	private final SemanticIndexCache cacheSI;

	private final SemanticIndexViewsManager views;
	private final TermFactory termFactory;
	private final TargetAtomFactory targetAtomFactory;
	private final FunctionSymbol int2IRIStringFunctionSymbol;
	private final RDFTermTypeConstant iriTypeConstant;
	private final SQLPPSourceQueryFactory sourceQueryFactory;

	public RDBMSSIRepositoryManager(ClassifiedTBox reasonerDag,
									TermFactory termFactory, TypeFactory typeFactory,
									TargetAtomFactory targetAtomFactory,
									SQLPPSourceQueryFactory sourceQueryFactory) {
		this.reasonerDag = reasonerDag;
		this.termFactory = termFactory;
		this.sourceQueryFactory = sourceQueryFactory;
		views = new SemanticIndexViewsManager(typeFactory);
        cacheSI = new SemanticIndexCache(reasonerDag);
		this.targetAtomFactory = targetAtomFactory;
		cacheSI.buildSemanticIndexFromReasoner();
		uriMap = new SemanticIndexURIMap();

		DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
		int2IRIStringFunctionSymbol = new Int2IRIStringFunctionSymbolImpl(
				dbTypeFactory.getDBTermType("INTEGER"), dbTypeFactory.getDBStringType(), uriMap);
		iriTypeConstant = termFactory.getRDFTermTypeConstant(typeFactory.getIRITermType());
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
			for (TableDescription table : attributeTables)
				st.addBatch(table.createCommand);
			
			st.executeBatch();			
		}
		
		insertMetadata(conn);
	}

	private boolean isDBSchemaDefined(Connection conn)  {
		
		try (Statement st = conn.createStatement()) {
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", classTable.tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", ROLE_TABLE.tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", ATTRIBUTE_TABLE_MAP.get(RDF.LANGSTRING).tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", ATTRIBUTE_TABLE_MAP.get(XSD.STRING).tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", ATTRIBUTE_TABLE_MAP.get(XSD.INTEGER).tableName));
            st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", ATTRIBUTE_TABLE_MAP.get(XSD.LONG).tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", ATTRIBUTE_TABLE_MAP.get(XSD.DECIMAL).tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", ATTRIBUTE_TABLE_MAP.get(XSD.DOUBLE).tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", ATTRIBUTE_TABLE_MAP.get(XSD.DATETIME).tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", ATTRIBUTE_TABLE_MAP.get(XSD.BOOLEAN).tableName));
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", ATTRIBUTE_TABLE_MAP.get(XSD.DATETIMESTAMP).tableName));

			return true; // everything is fine if we get to this point
		} 
		catch (Exception e) {
			// ignore all exceptions
		}
		return false; // there was an exception if we have got here
	}
	
/*
	public void dropDBSchema(Connection conn) throws SQLException {

		try (Statement st = conn.createStatement()) {
			st.addBatch(indexTable.dropCommand);
			st.addBatch(intervalTable.dropCommand);
			st.addBatch(emptinessIndexTable.dropCommand);

			st.addBatch(classTable.dropCommand);
			for (TableDescription table : attributeTables)
				st.addBatch(table.dropCommand);
			
			st.addBatch(uriIdTable.dropCommand);

			st.executeBatch();
		}
		catch (BatchUpdateException e) {
			// no-op: ignore all exceptions here
		}
	}
*/

	public int insertData(Connection conn, Iterator<RDFFact> data, int commitLimit, int batchLimit) throws SQLException {
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
		Map<IRI, Integer> failures = new HashMap<>();

		int batchCount = 0;
		int commitCount = 0;

		try {
			while (data.hasNext()) {
				RDFFact ax = data.next();

				// log.debug("Inserting statement: {}", ax);
				batchCount++;
				commitCount++;

				try {
					process(conn, ax, uriidStm, stmMap);
					success++;
				}
				catch (Exception e) {
					IRI iri = Optional.of(ax.getClassOrProperty())
							.filter(c -> c instanceof IRIConstant)
							.map(c -> (IRIConstant) c)
							.orElseGet(ax::getProperty)
							.getIRI();
					Integer counter = failures.get(iri);
					if (counter == null)
						counter = 0;
					failures.put(iri, counter + 1);
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
		for (Map.Entry<IRI, Integer> entry : failures.entrySet()) {
			log.warn("Failed to insert data for predicate {} ({} tuples).", entry.getKey(), entry.getValue());
			totalFailures += entry.getValue();
		}
		if (totalFailures > 0) {
			log.warn("Total failed insertions: " + totalFailures + ". (REASON: datatype mismatch between the ontology and database).");
		}

		return success;
	}

	private void process(Connection conn, RDFFact ax, PreparedStatement uriidStm,
						 Map<SemanticIndexViewID, PreparedStatement> stmMap) throws SQLException {
		if (ax.isClassAssertion() && (ax.getObject() instanceof IRIConstant)) {
			IRI classIRI = ((IRIConstant) ax.getObject()).getIRI();
			OClass cls = reasonerDag.classes().get(classIRI);
			process(conn, ax, cls, uriidStm, stmMap);
		}
		else {
			RDFConstant object = ax.getObject();
			IRI propertyIri = ax.getProperty().getIRI();

			if (object instanceof ObjectConstant) {
				ObjectPropertyExpression ope = reasonerDag.objectProperties().get(propertyIri);
				process(conn, ax, ope, uriidStm, stmMap);
			}
			else if (object instanceof RDFLiteralConstant) {
				DataPropertyExpression dpe = reasonerDag.dataProperties().get(propertyIri);
				process(conn, ax, dpe, uriidStm, stmMap);
			}
		}
	}

	private void process(Connection conn, RDFFact assertion, ObjectPropertyExpression ope0, PreparedStatement uriidStm,
						 Map<SemanticIndexViewID, PreparedStatement> stmMap) throws SQLException {

		if (ope0.isInverse()) 
			throw new RuntimeException("INVERSE PROPERTIES ARE NOT SUPPORTED IN ABOX:" + assertion);
		
		ObjectPropertyExpression ope = reasonerDag.objectPropertiesDAG().getCanonicalForm(ope0);
				
		ObjectConstant o1, o2;
		if (ope.isInverse()) {
			// the canonical representative is inverse
			// and so, it is not indexed -- swap the arguments 
			// and replace the representative with its inverse 
			// (which must be indexed)
			o1 = (ObjectConstant) assertion.getObject();
			o2 = assertion.getSubject();
			ope = ope.getInverse();
		}
		else {
			o1 = assertion.getSubject();
			o2 = (ObjectConstant) assertion.getObject();
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

	private void process(Connection conn, RDFFact assertion, DataPropertyExpression dpe0, PreparedStatement uriidStm, Map<SemanticIndexViewID, PreparedStatement> stmMap) throws SQLException {

		// replace the property by its canonical representative
		DataPropertyExpression dpe = reasonerDag.dataPropertiesDAG().getCanonicalForm(dpe0);
		int idx = cacheSI.getEntry(dpe).getIndex();
		
		ObjectConstant subject = assertion.getSubject();
		int uri_id = getObjectConstantUriId(subject, uriidStm);

		RDFLiteralConstant object = (RDFLiteralConstant) assertion.getObject();

		// ROMAN (28 June 2016): quite fragile because objectType is UNSUPPORTED for SHORT, BYTE, etc.
		//                       a a workaround, obtain the URI ID first, without triggering an exception here
		SemanticIndexView view =  views.getView(subject.getType(), object.getType());
		PreparedStatement stm = stmMap.get(view.getId());
		if (stm == null) {
			stm = conn.prepareStatement(view.getINSERT());
			stmMap.put(view.getId(), stm);
		}

		stm.setInt(1, uri_id);
		
		String value = object.getValue();
		
		switch (COL_TYPE.getColType(object.getType().getIRI())) {
			case LANG_STRING:  // -3
				stm.setString(2, value);
				stm.setString(4, object.getType().getLanguageTag().get().getFullString());
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
				Timestamp timestamp = XsdDatatypeConverter.parseXsdDateTime(value);
				stm.setTimestamp(2, timestamp);
				break;
			case BOOLEAN: // 14				
				stm.setBoolean(2, XsdDatatypeConverter.parseXsdBoolean(value));
				break;
			default:
				// UNSUPPORTED DATATYPE
				log.warn("Ignoring assertion: {}", assertion);
				return;				
		}
		
		stm.setInt(3, idx);
		stm.addBatch();
		
		// register non-emptiness
		view.addIndex(idx);
	}
	
		
	private void process(Connection conn, RDFFact assertion, OClass concept0, PreparedStatement uriidStm, Map<SemanticIndexViewID, PreparedStatement> stmMap) throws SQLException {
		
		// replace concept by the canonical representative (which must be a concept name)
		OClass concept = (OClass)reasonerDag.classesDAG().getCanonicalForm(concept0);
		int conceptIndex = cacheSI.getEntry(concept).getIndex();	

		ObjectConstant c1 = assertion.getSubject();

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
		String uri = (c instanceof BNode) ? ((BNode) c).getInternalLabel() : ((IRIConstant) c).getIRI().getIRIString();

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

	public final static int CLASS_TYPE = 1;
	public final static int ROLE_TYPE = 2;
	
/* ROMAN: commented out dead code

	private void setIndex(String iri, int type, int idx) {
		if (type == CLASS_TYPE) {
			OClass c = reasonerDag.classes().get(iri);
			if (reasonerDag.classesDAG().getVertex(c) == null)
				throw new RuntimeException("UNKNOWN CLASS: " + iri);
			
			if (cacheSI.getEntry(c) != null)
				throw new RuntimeException("DUPLICATE CLASS INDEX: " + iri);
			
			cacheSI.setIndex(c, idx);
		}
		else {
			if (reasonerDag.objectProperties().contains(iri)) {
				//
				// a bit elaborate logic is a consequence of using the same type for
				// both object and data properties (which can have the same name)
				// according to the SemanticIndexBuilder, object properties are indexed first 
				// (and have lower indexes), and only then data properties are indexed
				// so, the first occurrence is an object property, 
				// and the second occurrence is a datatype property
				// (here we use the fact that the query result is sorted by idx)
				//
				ObjectPropertyExpression ope = reasonerDag.objectProperties().get(iri);
				if (cacheSI.getEntry(ope) != null)  {
					DataPropertyExpression dpe = reasonerDag.dataProperties().get(iri);
					if (reasonerDag.dataPropertiesDAG().getVertex(dpe) != null) {
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
				DataPropertyExpression dpe = reasonerDag.dataProperties().get(iri);
				if (reasonerDag.dataPropertiesDAG().getVertex(dpe) != null) {
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
			OClass c = reasonerDag.classes().get(iri);
			range = cacheSI.getEntry(c);
		}
		else {
			Interval interval = intervals.get(0);
			// if the first interval is within object property indexes
			if (interval.getEnd() <= maxObjectPropertyIndex) {
				ObjectPropertyExpression ope = reasonerDag.objectProperties().get(iri);
				range = cacheSI.getEntry(ope);
			}
			else {
				DataPropertyExpression dpe = reasonerDag.dataProperties().get(iri);
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
*/

	public ImmutableList<SQLPPTriplesMap> getMappings() {

		List<SQLPPTriplesMap> result = new LinkedList<>();

		/*
		 * PART 2: Creating the mappings
		 * 
		 * Note, at every step we always use the pureIsa dag to get the indexes
		 * and ranges for each class.
		 */


		for (Equivalences<ObjectPropertyExpression> set : reasonerDag.objectPropertiesDAG()) {

			ObjectPropertyExpression ope = set.getRepresentative();
			// only named roles are mapped
			if (ope.isInverse()) 
				continue;
			
			// no mappings for auxiliary roles, which are introduced by the ontology translation process
			if (!reasonerDag.objectProperties().contains(ope.getIRI()))
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
				
				SQLPPSourceQuery sourceQuery = sourceQueryFactory.createSourceQuery(view.getSELECT(intervalsSqlFilter));
				ImmutableList<TargetAtom> targetQuery = constructTargetQuery(termFactory.getConstantIRI(ope.getIRI()),
						view.getId().getType1(), view.getId().getType2(), getUriMap());
				SQLPPTriplesMap basicmapping = new OntopNativeSQLPPTriplesMap(
						IDGenerator.getNextUniqueID("MAPID-"), sourceQuery, targetQuery);
				result.add(basicmapping);		
			}
		}

		for (Equivalences<DataPropertyExpression> set : reasonerDag.dataPropertiesDAG()) {

			DataPropertyExpression dpe = set.getRepresentative();
			
			// no mappings for auxiliary roles, which are introduced by the ontology translation process
			if (!reasonerDag.dataProperties().contains(dpe.getIRI()))
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
				
				SQLPPSourceQuery sourceQuery = sourceQueryFactory.createSourceQuery(view.getSELECT(intervalsSqlFilter));
				ImmutableList<TargetAtom> targetQuery = constructTargetQuery(
						termFactory.getConstantIRI(dpe.getIRI()) ,
						view.getId().getType1(), view.getId().getType2(), getUriMap());
				SQLPPTriplesMap basicmapping = new OntopNativeSQLPPTriplesMap(
						IDGenerator.getNextUniqueID("MAPID-"), sourceQuery, targetQuery);
				result.add(basicmapping);
			}
		}
		
		/*
		 * Creating mappings for each concept
		 */

		for (Equivalences<ClassExpression> set : reasonerDag.classesDAG()) {
			
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
				
				SQLPPSourceQuery sourceQuery = sourceQueryFactory.createSourceQuery(view.getSELECT(intervalsSqlFilter));
				ImmutableList<TargetAtom> targetQuery = constructTargetQuery(
						termFactory.getConstantIRI(classNode.getIRI()), view.getId().getType1());
				SQLPPTriplesMap basicmapping = new OntopNativeSQLPPTriplesMap(
						IDGenerator.getNextUniqueID("MAPID-"), sourceQuery, targetQuery);
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
				newSQL.append(((SQLPPSourceQuery) currentMappings.get(0).getSourceQuery()).toString());
				for (int mapi = 1; mapi < currentMappings.size(); mapi++) {
					newSQL.append(" UNION ALL ");
					newSQL.append(((SQLPPSourceQuery) currentMappings.get(mapi).getSourceQuery()).toString());
				}

				// Replacing the old mappings 
				OBDAMappingAxiom mergedMapping = nativeQLFactory.create(newSQL.toString(), targetQuery);
				currentMappings.clear();
				currentMappings.add(mergedMapping);
			}
		}
		*/
		log.debug("Total: {} mappings", result.size());
		return ImmutableList.copyOf(result);
	}

	
	private ImmutableList<TargetAtom> constructTargetQuery(ImmutableTerm classTerm, ObjectRDFType type) {

		Variable X = termFactory.getVariable("X");

		ImmutableFunctionalTerm subjectTerm;
		if (!type.isBlankNode())
			subjectTerm = getEncodedIRIFunctionalTerm(X);
		else {
			subjectTerm = termFactory.getRDFFunctionalTerm(X, termFactory.getRDFTermTypeConstant(type));
		}

		ImmutableTerm predTerm = termFactory.getConstantIRI(RDF.TYPE);

		TargetAtom targetAtom = targetAtomFactory.getTripleTargetAtom(subjectTerm, predTerm ,classTerm);
		return ImmutableList.of(targetAtom);
	}
	
	
	private ImmutableList<TargetAtom> constructTargetQuery(ImmutableTerm iriTerm, ObjectRDFType type1,
																		RDFTermType type2, IRIDictionary iriDictionary) {

		Variable X = termFactory.getVariable("X");
		Variable Y = termFactory.getVariable("Y");

		ImmutableFunctionalTerm subjectTerm;
		if (!type1.isBlankNode())
			subjectTerm = getEncodedIRIFunctionalTerm(X);
		else {
			subjectTerm = termFactory.getRDFFunctionalTerm(X, termFactory.getRDFTermTypeConstant(type1));
		}

		ImmutableFunctionalTerm objectTerm;
		if (type2 instanceof ObjectRDFType) {
			objectTerm = ((ObjectRDFType)type2).isBlankNode()
					? termFactory.getRDFFunctionalTerm(Y, termFactory.getRDFTermTypeConstant(type2))
					: getEncodedIRIFunctionalTerm(Y);
		}
		else {
			RDFDatatype datatype = (RDFDatatype) type2;
			if (datatype.getLanguageTag().isPresent()) {
				LanguageTag languageTag = datatype.getLanguageTag().get();
				objectTerm = termFactory.getRDFLiteralFunctionalTerm(Y, languageTag.getFullString());
			}
			else {
				objectTerm = termFactory.getRDFLiteralFunctionalTerm(Y, datatype);
			}
		}

		TargetAtom targetAtom = targetAtomFactory.getTripleTargetAtom(subjectTerm,iriTerm,objectTerm);

		return ImmutableList.of(targetAtom);
	}

	public ImmutableFunctionalTerm getEncodedIRIFunctionalTerm(ImmutableTerm dbIntegerTerm) {
		ImmutableFunctionalTerm lexicalValue = termFactory.getImmutableFunctionalTerm(
				int2IRIStringFunctionSymbol, dbIntegerTerm);
		return termFactory.getRDFFunctionalTerm(lexicalValue, iriTypeConstant);
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

		log.debug("Inserting semantic index metadata.");

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
					stm.setString(1, concept.getKey().getIRI().getIRIString());
					stm.setInt(2, concept.getValue().getIndex());
					stm.setInt(3, CLASS_TYPE);
					stm.addBatch();
				}
				for (Entry<ObjectPropertyExpression, SemanticIndexRange> role : cacheSI.getObjectPropertyIndexEntries()) {
					stm.setString(1, role.getKey().getIRI().getIRIString());
					stm.setInt(2, role.getValue().getIndex());
					stm.setInt(3, ROLE_TYPE);
					stm.addBatch();
				}
				for (Entry<DataPropertyExpression, SemanticIndexRange> role : cacheSI.getDataPropertyIndexEntries()) {
					stm.setString(1, role.getKey().getIRI().getIRIString());
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
						stm.setString(1, concept.getKey().getIRI().getIRIString());
						stm.setInt(2, it.getStart());
						stm.setInt(3, it.getEnd());
						stm.setInt(4, CLASS_TYPE);
						stm.addBatch();
					}
				}
				for (Entry<ObjectPropertyExpression, SemanticIndexRange> role : cacheSI.getObjectPropertyIndexEntries()) {
					for (Interval it : role.getValue().getIntervals()) {
						stm.setString(1, role.getKey().getIRI().getIRIString());
						stm.setInt(2, it.getStart());
						stm.setInt(3, it.getEnd());
						stm.setInt(4, ROLE_TYPE);
						stm.addBatch();
					}
				}
				for (Entry<DataPropertyExpression, SemanticIndexRange> role : cacheSI.getDataPropertyIndexEntries()) {
					for (Interval it : role.getValue().getIntervals()) {
						stm.setString(1, role.getKey().getIRI().getIRIString());
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

	
	
	
/* dead code
	public void createIndexes(Connection conn) throws SQLException {
		log.debug("Creating indexes");
		try (Statement st = conn.createStatement()) {
			for (TableDescription table : attributeTables)
				for (String s : table.createIndexCommands)
					st.addBatch(s);
						
			for (String s : classTable.createIndexCommands)
				st.addBatch(s);
			
			st.executeBatch();
			st.clearBatch();
			
			log.debug("Executing ANALYZE");
			st.addBatch("ANALYZE");
			st.executeBatch();
		}
	}

	public void dropIndexes(Connection conn) throws SQLException {
		log.debug("Dropping indexes");

		try (Statement st = conn.createStatement()) {
			for (String s : classTable.dropIndexCommands)
				st.addBatch(s);	

			for (TableDescription table : attributeTables)
				for (String s : table.dropIndexCommands)
					st.addBatch(s);
			
			st.executeBatch();
		}
	}
 */
}
