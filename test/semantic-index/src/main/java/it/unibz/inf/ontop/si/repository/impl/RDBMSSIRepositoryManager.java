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
import it.unibz.inf.ontop.model.term.functionsymbol.impl.Int2IRIStringFunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.utils.IDGenerator;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Store ABox assertions in the DB
 */

public class RDBMSSIRepositoryManager {

	private final static Logger log = LoggerFactory.getLogger(RDBMSSIRepositoryManager.class);

	static final class TableDescription {
		private final String tableName;
		private final String createCommand;
		private final String insertCommand;
		private final String selectCommand;

		TableDescription(String tableName, ImmutableMap<String, String> columnDefinitions, String selectColumns) {
			this.tableName = tableName;
			this.createCommand = "CREATE TABLE " + tableName +
					" ( " + Joiner.on(", ").withKeyValueSeparator(" ").join(columnDefinitions) + " )";
			this.insertCommand = "INSERT INTO " + tableName + 
					" (" + Joiner.on(", ").join(columnDefinitions.keySet()) + ") VALUES ";
			this.selectCommand = "SELECT " + selectColumns + " FROM " + tableName; 
		}
		
		String getINSERT(String values) {
			return insertCommand + "(" + values + ")";
		}
		
		String getSELECT(String filter) {
			return selectCommand +  " WHERE " + filter;
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
	
	final static TableDescription CLASS_TABLE = new TableDescription("QUEST_CLASS_ASSERTION",
			ImmutableMap.of("\"URI\"", "INTEGER NOT NULL", 
					        "\"IDX\"", "SMALLINT NOT NULL", 
					        "ISBNODE", "BOOLEAN NOT NULL DEFAULT FALSE"), "\"URI\" as X");
	
    final static TableDescription ROLE_TABLE = new TableDescription("QUEST_OBJECT_PROPERTY_ASSERTION",
			ImmutableMap.of("\"URI1\"", "INTEGER NOT NULL",
					"\"URI2\"", "INTEGER NOT NULL",
					"\"IDX\"", "SMALLINT NOT NULL",
					"ISBNODE", "BOOLEAN NOT NULL DEFAULT FALSE",
					"ISBNODE2", "BOOLEAN NOT NULL DEFAULT FALSE"), "\"URI1\" as X, \"URI2\" as Y");

	private static TableDescription getAttributeTableDescription(String tableNameFragment, String sqlTypeName) {
		return new TableDescription(String.format("QUEST_DATA_PROPERTY_%s_ASSERTION", tableNameFragment),
				ImmutableMap.of("\"URI\"", "INTEGER NOT NULL",
						"VAL", sqlTypeName,
						"\"IDX\"", "SMALLINT  NOT NULL",
						"ISBNODE", "BOOLEAN NOT NULL DEFAULT FALSE"), "\"URI\" as X, VAL as Y");
	}

	final static ImmutableMap<IRI, TableDescription> ATTRIBUTE_TABLE_MAP = ImmutableMap.<IRI, TableDescription>builder()
			// LANG_STRING is special because of one extra attribute (LANG)
			.put(RDF.LANGSTRING,
					new TableDescription("QUEST_DATA_PROPERTY_LITERAL_ASSERTION",
							ImmutableMap.of("\"URI\"", "INTEGER NOT NULL",
									"VAL", "VARCHAR(1000) NOT NULL",
									"\"IDX\"", "SMALLINT NOT NULL",
									"LANG", "VARCHAR(20)",
									"ISBNODE", "BOOLEAN NOT NULL DEFAULT FALSE"), "\"URI\" as X, VAL as Y, LANG as Z"))

			// all other datatypes from COL_TYPE are treated similarly
			.put(XSD.STRING, getAttributeTableDescription("STRING", "VARCHAR(1000)"))
			.put(XSD.INTEGER, getAttributeTableDescription("INTEGER", "BIGINT"))
			.put(XSD.INT, getAttributeTableDescription("INT", "INTEGER"))
			.put(XSD.UNSIGNED_INT, getAttributeTableDescription("UNSIGNED_INT", "INTEGER"))
			.put(XSD.NEGATIVE_INTEGER, getAttributeTableDescription("NEGATIVE_INTEGER", "BIGINT"))
			.put(XSD.NON_NEGATIVE_INTEGER, getAttributeTableDescription("NON_NEGATIVE_INTEGER", "BIGINT"))
			.put(XSD.POSITIVE_INTEGER, getAttributeTableDescription("POSITIVE_INTEGER", "BIGINT"))
			.put(XSD.NON_POSITIVE_INTEGER, getAttributeTableDescription("NON_POSITIVE_INTEGER", "BIGINT"))
			.put(XSD.LONG, getAttributeTableDescription("LONG", "BIGINT"))
			.put(XSD.DECIMAL, getAttributeTableDescription("DECIMAL", "DECIMAL"))
			.put(XSD.FLOAT, getAttributeTableDescription("FLOAT", "DOUBLE PRECISION"))
			.put(XSD.DOUBLE, getAttributeTableDescription("DOUBLE", "DOUBLE PRECISION"))
			.put(XSD.DATETIME, getAttributeTableDescription("DATETIME", "TIMESTAMP"))
			.put(XSD.BOOLEAN,  getAttributeTableDescription("BOOLEAN", "BOOLEAN"))
			.put(XSD.DATETIMESTAMP, getAttributeTableDescription("DATETIMESTAMP", "TIMESTAMP"))
			.build();

	private final SemanticIndexURIMap uriMap;
	
	private final ClassifiedTBox reasonerDag;

	private final SemanticIndex semanticIndex;

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
		this.targetAtomFactory = targetAtomFactory;

		views = new SemanticIndexViewsManager(typeFactory);
        semanticIndex = new SemanticIndex(reasonerDag);
		uriMap = new SemanticIndexURIMap();

		DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
		int2IRIStringFunctionSymbol = new Int2IRIStringFunctionSymbolImpl(
				dbTypeFactory.getDBTermType("INTEGER"), dbTypeFactory.getDBStringType(), uriMap);
		iriTypeConstant = termFactory.getRDFTermTypeConstant(typeFactory.getIRITermType());
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

			st.addBatch(CLASS_TABLE.createCommand);
			st.addBatch(ROLE_TABLE.createCommand);
			for (TableDescription table : ATTRIBUTE_TABLE_MAP.values())
				st.addBatch(table.createCommand);
			
			st.executeBatch();			
		}
		
		insertMetadata(conn);
	}

	private boolean isDBSchemaDefined(Connection conn)  {
		
		try (Statement st = conn.createStatement()) {
			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", CLASS_TABLE.tableName));
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
			return false;
		}
	}

	public int insertData(Connection conn, Iterator<RDFFact> data, int commitLimit, int batchLimit) throws SQLException {
		log.debug("Inserting data into DB");

		// The precondition for the limit number must be greater or equal to one.
		commitLimit = Math.max(commitLimit, 1);
		batchLimit = Math.max(batchLimit, 1);

		boolean oldAutoCommit = conn.getAutoCommit();
		conn.setAutoCommit(false);

		// For counting the insertion
		int success = 0;
		Map<IRI, Integer> failures = new HashMap<>();

		int batchCount = 0;
		int commitCount = 0;

		try (BatchProcessor batch = new BatchProcessor(conn)) {
			while (data.hasNext()) {
				RDFFact ax = data.next();

				batchCount++;
				commitCount++;

				try {
					batch.process(ax);
					success++;
				}
				catch (Exception e) {
					IRI iri = Optional.of(ax.getClassOrProperty())
							.filter(c -> c instanceof IRIConstant)
							.map(c -> (IRIConstant) c)
							.orElseGet(ax::getProperty)
							.getIRI();
					int counter = failures.getOrDefault(iri, 0);
					failures.put(iri, counter + 1);
				}

				// Check if the batch count is already in the batch limit
				if (batchCount == batchLimit) {
					batch.execute();
					batchCount = 0; // reset the counter
				}

				// Check if the commit count is already in the commit limit
				if (commitCount == commitLimit) {
					conn.commit();
					commitCount = 0; // reset the counter
				}
			}

			// Execute the rest of the batch
			batch.execute();
			// Commit the rest of the batch insert
			conn.commit();
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

	// TODO: big issue -- URI map is incomplete -- it is never read back from the DB
	// TODO: use database to get the maximum URIId
	private int maxUriId = -1;

	private final class BatchProcessor implements AutoCloseable {
		private final Connection conn;
		private final PreparedStatement uriIdStm;
		private final Map<SemanticIndexViewID, PreparedStatement> stmMap;

		BatchProcessor(Connection conn) throws SQLException {
			this.conn = conn;
			uriIdStm = conn.prepareStatement(uriIdTable.getINSERT("?, ?"));
			stmMap = new HashMap<>();
		}

		void process(RDFFact ax) throws SQLException {
			if (ax.isClassAssertion() && (ax.getObject() instanceof IRIConstant)) {
				IRI classIRI = ((IRIConstant) ax.getObject()).getIRI();
				OClass cls0 = reasonerDag.classes().get(classIRI);
				// replace concept by the canonical representative (which must be a concept name)
				OClass cls = (OClass)reasonerDag.classesDAG().getCanonicalForm(cls0);
				process(cls, ax.getSubject());
			}
			else {
				RDFConstant object = ax.getObject();
				IRI propertyIri = ax.getProperty().getIRI();

				if (object instanceof ObjectConstant) {
					ObjectPropertyExpression ope0 = reasonerDag.objectProperties().get(propertyIri);
					if (ope0.isInverse())
						throw new RuntimeException("INVERSE PROPERTIES ARE NOT SUPPORTED IN ABOX:" + ax);
					ObjectPropertyExpression ope = reasonerDag.objectPropertiesDAG().getCanonicalForm(ope0);
					if (ope.isInverse())
						process(ope.getInverse(), (ObjectConstant) object, ax.getSubject());
					else
						process(ope, ax.getSubject(), (ObjectConstant) object);
				}
				else if (object instanceof RDFLiteralConstant) {
					DataPropertyExpression dpe0 = reasonerDag.dataProperties().get(propertyIri);
					// replace the property by its canonical representative
					DataPropertyExpression dpe = reasonerDag.dataPropertiesDAG().getCanonicalForm(dpe0);
					process(dpe, ax.getSubject(), (RDFLiteralConstant) ax.getObject());
				}
			}
		}

		private void process(OClass cls, ObjectConstant c1) throws SQLException {
			int idx = semanticIndex.getRange(cls).getIndex();

			int uriId = getObjectConstantUriId(c1);

			SemanticIndexView view =  views.getView(c1.getType());
			PreparedStatement stm = getPreparedStatement(view);
			stm.setInt(1, uriId);
			stm.setInt(2, idx);
			stm.addBatch();

			// Register non emptiness
			view.addIndex(idx);
		}

		private void process(ObjectPropertyExpression ope, ObjectConstant o1, ObjectConstant o2) throws SQLException {
			int	idx = semanticIndex.getRange(ope).getIndex();

			int uriId1 = getObjectConstantUriId(o1);
			int uriId2 = getObjectConstantUriId(o2);

			SemanticIndexView view = views.getView(o1.getType(), o2.getType());
			PreparedStatement stm = getPreparedStatement(view);
			stm.setInt(1, uriId1);
			stm.setInt(2, uriId2);
			stm.setInt(3, idx);
			stm.addBatch();

			// Register non emptiness
			view.addIndex(idx);
		}

		private void process(DataPropertyExpression dpe, ObjectConstant subject, RDFLiteralConstant object) throws SQLException {

			int idx = semanticIndex.getRange(dpe).getIndex();

			int uriId = getObjectConstantUriId(subject);

			// ROMAN (28 June 2016): quite fragile because objectType is UNSUPPORTED for SHORT, BYTE, etc.
			//                       a a workaround, obtain the URI ID first, without triggering an exception here
			SemanticIndexView view =  views.getView(subject.getType(), object.getType());
			PreparedStatement stm = getPreparedStatement(view);
			stm.setInt(1, uriId);

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
					log.warn("Ignoring assertion: {} {} {}", dpe, subject, object);
					return;
			}

			stm.setInt(3, idx);
			stm.addBatch();

			// register non-emptiness
			view.addIndex(idx);
		}

		PreparedStatement getPreparedStatement(SemanticIndexView view) throws SQLException {
			PreparedStatement stm = stmMap.get(view.getId());
			if (stm == null) {
				stm = conn.prepareStatement(view.getINSERT());
				stmMap.put(view.getId(), stm);
			}
			return stm;
		}

		private int getObjectConstantUriId(ObjectConstant c) throws SQLException {
			// TODO (ROMAN): I am not sure this is entirely correct for blank nodes
			String uri = (c instanceof BNode) ? ((BNode) c).getInternalLabel() : ((IRIConstant) c).getIRI().getIRIString();

			int uriId = uriMap.getId(uri);
			if (uriId < 0) {
				uriId = ++maxUriId;
				uriMap.set(uri, uriId);

				// Construct the database INSERT statement
				uriIdStm.setInt(1, uriId);
				uriIdStm.setString(2, uri);
				uriIdStm.addBatch();
			}

			return uriId;
		}

		void execute() throws SQLException {
			uriIdStm.executeBatch();
			uriIdStm.clearBatch();
			for (PreparedStatement stm : stmMap.values()) {
				stm.executeBatch();
				stm.clearBatch();
			}
		}

		@Override
		public void close() throws SQLException {
			uriIdStm.close();
			for (PreparedStatement stm : stmMap.values())
				stm.close();
		}
	}




	private static String getIntervalString(Interval interval) {
		if (interval.getStart() == interval.getEnd())
			return String.format("IDX = %d", interval.getStart());
		else
			return String.format("IDX >= %d AND IDX <= %d", interval.getStart(), interval.getEnd());
	}

	private Stream<SQLPPTriplesMap> getTripleMaps(SemanticIndexRange range, IRI iri, BiFunction<IRI, SemanticIndexView, TargetAtom> transformer) {
		List<Interval> intervals = range.getIntervals();
		String intervalsSqlFilter = intervals.stream()
				.map(RDBMSSIRepositoryManager::getIntervalString)
				.collect(Collectors.joining(" OR "));

		return views.getViews().stream()
				.filter(v -> !v.isEmptyForIntervals(intervals))
				.map(v -> {
					SQLPPSourceQuery sourceQuery = sourceQueryFactory.createSourceQuery(v.getSELECT(intervalsSqlFilter));
					TargetAtom targetAtom = transformer.apply(iri, v);
					return new OntopNativeSQLPPTriplesMap(
							IDGenerator.getNextUniqueID("MAPID-"), sourceQuery, ImmutableList.of(targetAtom));
				});
	}

	public ImmutableList<SQLPPTriplesMap> getMappings() {

		ImmutableList<SQLPPTriplesMap> result = Stream.concat(Stream.concat(
				reasonerDag.objectPropertiesDAG().stream()
						.map(Equivalences::getRepresentative)
						.filter(ope -> !ope.isInverse())
						.filter(ope -> reasonerDag.objectProperties().contains(ope.getIRI())) 	// no mappings for auxiliary roles, which are introduced by the ontology translation process
						.flatMap(ope -> getTripleMaps(semanticIndex.getRange(ope), ope.getIRI(), this::constructPropertyTargetQuery)),

				reasonerDag.dataPropertiesDAG().stream()
						.map(Equivalences::getRepresentative)
						.filter(dpe -> reasonerDag.dataProperties().contains(dpe.getIRI())) 	// no mappings for auxiliary roles, which are introduced by the ontology translation process
						.flatMap(dpe -> getTripleMaps(semanticIndex.getRange(dpe), dpe.getIRI(), this::constructPropertyTargetQuery))),

				reasonerDag.classesDAG().stream()
						.map(Equivalences::getRepresentative)
						.filter(cle -> cle instanceof OClass) // only named classes are mapped
						.map(cle -> (OClass)cle)
						.flatMap(cls -> getTripleMaps(semanticIndex.getRange(cls), cls.getIRI(), this::constructClassTargetQuery)))

						.collect(ImmutableCollectors.toList());

		log.debug("Total: {} mappings", result.size());
		return result;
	}

	private ImmutableFunctionalTerm getTerm(ObjectRDFType type, Variable var) {
		if (!type.isBlankNode()) {
			ImmutableFunctionalTerm lexicalValue = termFactory.getImmutableFunctionalTerm(
					int2IRIStringFunctionSymbol, var);
			return termFactory.getRDFFunctionalTerm(lexicalValue, iriTypeConstant);
		}
		else {
			return termFactory.getRDFFunctionalTerm(var, termFactory.getRDFTermTypeConstant(type));
		}
	}
	
	private TargetAtom constructClassTargetQuery(IRI iri, SemanticIndexView view) {
		Variable X = termFactory.getVariable("X");

		ImmutableFunctionalTerm subjectTerm = getTerm(view.getId().getType1(), X);
		ImmutableTerm predTerm = termFactory.getConstantIRI(RDF.TYPE);
		IRIConstant classTerm = termFactory.getConstantIRI(iri);

		return targetAtomFactory.getTripleTargetAtom(subjectTerm, predTerm, classTerm);
	}

	private TargetAtom constructPropertyTargetQuery(IRI iri, SemanticIndexView view) {
		Variable X = termFactory.getVariable("X");
		Variable Y = termFactory.getVariable("Y");

		ImmutableFunctionalTerm subjectTerm = getTerm(view.getId().getType1(), X);
		IRIConstant iriTerm = termFactory.getConstantIRI(iri);

		RDFTermType type2 = view.getId().getType2();
		final ImmutableFunctionalTerm objectTerm;
		if (type2 instanceof ObjectRDFType) {
			objectTerm = getTerm((ObjectRDFType)type2, Y);
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

		return targetAtomFactory.getTripleTargetAtom(subjectTerm, iriTerm, objectTerm);
	}



	public final static int CLASS_TYPE = 1;
	public final static int ROLE_TYPE = 2;

	/***
	 * Inserts the metadata about semantic indexes and intervals into the
	 * database. The metadata is later used to reconstruct a semantic index
	 * repository.
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

			try (PreparedStatement stm = conn.prepareStatement(indexTable.getINSERT("?, ?, ?"))) {
				for (Entry<OClass,SemanticIndexRange> e : semanticIndex.getIndexedClasses())
					insertIndexData(stm, e.getKey().getIRI(), e.getValue(), CLASS_TYPE);

				for (Entry<ObjectPropertyExpression, SemanticIndexRange> e : semanticIndex.getIndexedObjectProperties())
					insertIndexData(stm, e.getKey().getIRI(), e.getValue(), ROLE_TYPE);

				for (Entry<DataPropertyExpression, SemanticIndexRange> e : semanticIndex.getIndexedDataProperties())
					insertIndexData(stm, e.getKey().getIRI(), e.getValue(), ROLE_TYPE);

				stm.executeBatch();
			}

			try (PreparedStatement stm = conn.prepareStatement(intervalTable.getINSERT("?, ?, ?, ?"))) {
				for (Entry<OClass,SemanticIndexRange> e : semanticIndex.getIndexedClasses())
					insertIntervalMetadata(stm,  e.getKey().getIRI(), e.getValue(), CLASS_TYPE);

				for (Entry<ObjectPropertyExpression, SemanticIndexRange> e : semanticIndex.getIndexedObjectProperties())
					insertIntervalMetadata(stm,  e.getKey().getIRI(), e.getValue(), ROLE_TYPE);

				for (Entry<DataPropertyExpression, SemanticIndexRange> e : semanticIndex.getIndexedDataProperties())
					insertIntervalMetadata(stm,  e.getKey().getIRI(), e.getValue(), ROLE_TYPE);

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

	private void insertIntervalMetadata(PreparedStatement stm, IRI iri, SemanticIndexRange range, int type) throws SQLException {
		for (Interval it : range.getIntervals()) {
			stm.setString(1, iri.getIRIString());
			stm.setInt(2, it.getStart());
			stm.setInt(3, it.getEnd());
			stm.setInt(4, type);
			stm.addBatch();
		}
	}

	private void insertIndexData(PreparedStatement stm, IRI iri, SemanticIndexRange range, int type) throws SQLException {
		stm.setString(1, iri.getIRIString());
		stm.setInt(2, range.getIndex());
		stm.setInt(3, type);
		stm.addBatch();
	}
}
