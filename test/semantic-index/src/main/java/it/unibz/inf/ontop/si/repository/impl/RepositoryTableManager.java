package it.unibz.inf.ontop.si.repository.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.RDFLiteralConstant;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class RepositoryTableManager {

	public static final String IDX_COLUMN = "IDX";
	public static final String ISBNODE_COLUMN = "ISBNODE";
	public static final String ISBNODE2_COLUMN = "ISBNODE2";
	public static final String URI_COLUMN = "\"URI\"";
	public static final String URI2_COLUMN = "\"URI2\"";
	public static final String LANG_COLUMN = "LANG";
	public static final String VAL_COLUMN = "VAL";


	private final static RepositoryTable CLASS_TABLE = new RepositoryTable("QUEST_CLASS_ASSERTION",
			ImmutableMap.of(URI_COLUMN, "INTEGER NOT NULL",
					IDX_COLUMN, "SMALLINT NOT NULL",
					ISBNODE_COLUMN, "BOOLEAN NOT NULL DEFAULT FALSE"), URI_COLUMN + " as X");

	private final static RepositoryTable OBJECT_PROPERTY_TABLE = new RepositoryTable("QUEST_OBJECT_PROPERTY_ASSERTION",
			ImmutableMap.of(URI_COLUMN, "INTEGER NOT NULL",
					URI2_COLUMN, "INTEGER NOT NULL",
					IDX_COLUMN, "SMALLINT NOT NULL",
					ISBNODE_COLUMN, "BOOLEAN NOT NULL DEFAULT FALSE",
					ISBNODE2_COLUMN, "BOOLEAN NOT NULL DEFAULT FALSE"), URI_COLUMN + " as X, " + URI2_COLUMN + " as Y");

	private static RepositoryTable getDataPropertyTable(String tableNameFragment, String sqlTypeName) {
		return new RepositoryTable(String.format("QUEST_DATA_PROPERTY_%s_ASSERTION", tableNameFragment),
				ImmutableMap.of(URI_COLUMN, "INTEGER NOT NULL",
						VAL_COLUMN, sqlTypeName,
						IDX_COLUMN, "SMALLINT  NOT NULL",
						ISBNODE_COLUMN, "BOOLEAN NOT NULL DEFAULT FALSE"), URI_COLUMN + " as X, " + VAL_COLUMN + " as Y");
	}

	private final static ImmutableMap<IRI, RepositoryTable> DATA_PROPERTY_TABLE_MAP = ImmutableMap.<IRI, RepositoryTable>builder()
			// LANG_STRING is special because of one extra attribute (LANG)
			.put(RDF.LANGSTRING,
					new RepositoryTable("QUEST_DATA_PROPERTY_LITERAL_ASSERTION",
							ImmutableMap.of(URI_COLUMN, "INTEGER NOT NULL",
									VAL_COLUMN, "VARCHAR(1000) NOT NULL",
									IDX_COLUMN, "SMALLINT NOT NULL",
									LANG_COLUMN, "VARCHAR(20)",
									ISBNODE_COLUMN, "BOOLEAN NOT NULL DEFAULT FALSE"),
							URI_COLUMN + " as X, " + VAL_COLUMN + " as Y, " + LANG_COLUMN + " as Z"))

			// all other datatypes from COL_TYPE are treated similarly
			.put(XSD.STRING, getDataPropertyTable("STRING", "VARCHAR(1000)"))
			.put(XSD.INTEGER, getDataPropertyTable("INTEGER", "BIGINT"))
			.put(XSD.INT, getDataPropertyTable("INT", "INTEGER"))
			.put(XSD.UNSIGNED_INT, getDataPropertyTable("UNSIGNED_INT", "INTEGER"))
			.put(XSD.NEGATIVE_INTEGER, getDataPropertyTable("NEGATIVE_INTEGER", "BIGINT"))
			.put(XSD.NON_NEGATIVE_INTEGER, getDataPropertyTable("NON_NEGATIVE_INTEGER", "BIGINT"))
			.put(XSD.POSITIVE_INTEGER, getDataPropertyTable("POSITIVE_INTEGER", "BIGINT"))
			.put(XSD.NON_POSITIVE_INTEGER, getDataPropertyTable("NON_POSITIVE_INTEGER", "BIGINT"))
			.put(XSD.LONG, getDataPropertyTable("LONG", "BIGINT"))
			.put(XSD.DECIMAL, getDataPropertyTable("DECIMAL", "DECIMAL"))
			.put(XSD.FLOAT, getDataPropertyTable("FLOAT", "DOUBLE PRECISION"))
			.put(XSD.DOUBLE, getDataPropertyTable("DOUBLE", "DOUBLE PRECISION"))
			.put(XSD.DATETIME, getDataPropertyTable("DATETIME", "TIMESTAMP"))
			.put(XSD.BOOLEAN,  getDataPropertyTable("BOOLEAN", "BOOLEAN"))
			.put(XSD.DATETIMESTAMP, getDataPropertyTable("DATETIMESTAMP", "TIMESTAMP"))
			.build();
	private static final ImmutableList<RepositoryTable> ABOX_TABLES = Stream.concat(Stream.of(CLASS_TABLE, OBJECT_PROPERTY_TABLE), DATA_PROPERTY_TABLE_MAP.values().stream()).collect(ImmutableCollectors.toList());

	@FunctionalInterface
	public interface PreparedStatementInsertAction {
		void setValue(PreparedStatement stm, RDFLiteralConstant o) throws SQLException;
	}

	public static final ImmutableMap<IRI, PreparedStatementInsertAction> DATA_PROPERTY_TABLE_INSERT_STM_MAP = ImmutableMap.<IRI, PreparedStatementInsertAction>builder()
			.put(RDF.LANGSTRING, (stm, o) -> {
				stm.setString(2, o.getValue());
				stm.setString(4, o.getType().getLanguageTag().get().getFullString()); })
			.put(XSD.STRING, (stm, o) -> stm.setString(2, o.getValue()))
			.put(XSD.INTEGER, (stm, o) -> stm.setLong(2, Long.parseLong(o.getValue())))
			.put(XSD.INT, (stm, o) -> stm.setInt(2, Integer.parseInt(o.getValue())))
			.put(XSD.UNSIGNED_INT, (stm, o) -> stm.setInt(2, Integer.parseInt(o.getValue())))
			.put(XSD.NEGATIVE_INTEGER, (stm, o) -> stm.setLong(2, Long.parseLong(o.getValue())))
			.put(XSD.NON_NEGATIVE_INTEGER, (stm, o) -> stm.setLong(2, Long.parseLong(o.getValue())))
			.put(XSD.POSITIVE_INTEGER, (stm, o) -> stm.setLong(2, Long.parseLong(o.getValue())))
			.put(XSD.NON_POSITIVE_INTEGER, (stm, o) -> stm.setLong(2, Long.parseLong(o.getValue())))
			.put(XSD.LONG, (stm, o) -> stm.setLong(2, Long.parseLong(o.getValue())))
			.put(XSD.DECIMAL, (stm, o) -> stm.setBigDecimal(2, new BigDecimal(o.getValue())))
			.put(XSD.FLOAT, (stm, o) -> stm.setDouble(2, Float.parseFloat(o.getValue())))
			.put(XSD.DOUBLE, (stm, o) -> stm.setDouble(2, Double.parseDouble(o.getValue())))
			.put(XSD.DATETIME, (stm, o) -> stm.setTimestamp(2, XsdDatatypeConverter.parseXsdDateTime(o.getValue())))
			.put(XSD.BOOLEAN, (stm, o) -> stm.setBoolean(2, XsdDatatypeConverter.parseXsdBoolean(o.getValue())))
			.put(XSD.DATETIMESTAMP, (stm, o) -> stm.setTimestamp(2, XsdDatatypeConverter.parseXsdDateTime(o.getValue())))
			.build();


	private final static RepositoryTable EMPTINESS_INDEX_TABLE = new RepositoryTable("NONEMPTYNESSINDEX",
			ImmutableMap.of("TABLEID", "INTEGER",
					IDX_COLUMN, "INTEGER",
					"TYPE1", "INTEGER",
					"TYPE2", "INTEGER"), "*");

	private final Map<RepositoryTableSlice.Identifier, RepositoryTableSlice> views = new HashMap<>(); // fully mutable - see getView

	private final ImmutableMap<TermType, Integer> colTypetoSITable;

	public RepositoryTableManager(TypeFactory typeFactory) {

		ImmutableList<ObjectRDFType> objectTypes = ImmutableList.of(typeFactory.getIRITermType(),
				typeFactory.getBlankNodeType());

		ImmutableList<IRI> datatypeIRIs = DATA_PROPERTY_TABLE_MAP.keySet().stream()
				.filter(i1 -> !i1.equals(RDF.LANGSTRING)) // exclude as it depends on a particular language
				.collect(ImmutableCollectors.toList());

		for (ObjectRDFType type1 : objectTypes) {
			initClass(type1);

			for (ObjectRDFType type2 : objectTypes)
				initObjectProperty(type1, type2);

			for (IRI iriType2 : datatypeIRIs)
				initDataProperty(type1, typeFactory.getDatatype(iriType2));
		}

		IRI[] SITableToIRI = {
				XSD.STRING, XSD.INTEGER, // TODO: why these two skipped below?
				XSD.LONG, XSD.DECIMAL, XSD.DOUBLE, XSD.DATETIME, XSD.INT, XSD.UNSIGNED_INT,
				XSD.NEGATIVE_INTEGER, XSD.NON_NEGATIVE_INTEGER, XSD.POSITIVE_INTEGER, XSD.NON_POSITIVE_INTEGER,
				XSD.FLOAT, XSD.BOOLEAN, XSD.DATETIMESTAMP };

		// special case of COL_TYPE.OBJECT and COL_TYPE.BNODE (both are mapped to 1)
		ImmutableMap.Builder<TermType, Integer> colTypetoSITableBuilder = ImmutableMap.builder();
		colTypetoSITableBuilder.put(typeFactory.getBlankNodeType(), 1);
		colTypetoSITableBuilder.put(typeFactory.getIRITermType(), 1);
		// Class SITable has value 0 (skip it)
		for (int i = 2; i < SITableToIRI.length; i++)
			colTypetoSITableBuilder.put(typeFactory.getDatatype(SITableToIRI[i]), i);
		colTypetoSITable = colTypetoSITableBuilder.build();
	}

	public ImmutableList<RepositoryTableSlice> getViews() {
		return ImmutableList.copyOf(views.values());
	}

	public RepositoryTableSlice getView(ObjectRDFType type) {
		RepositoryTableSlice.Identifier viewId = new RepositoryTableSlice.Identifier(type);
		return views.get(viewId);
	}
	
	public RepositoryTableSlice getView(ObjectRDFType type1, RDFTermType type2) {
		RepositoryTableSlice.Identifier viewId = new RepositoryTableSlice.Identifier(type1, type2);
		/*
		 * For language tags (need to know the concrete one)
		 */
		if (!views.containsKey(viewId))
			if (type2 instanceof RDFDatatype)
				initDataProperty(type1, (RDFDatatype) type2);
			else
				throw new UnexpectedRDFTermTypeException(type2);

		return views.get(viewId);
	}

	private void initClass(ObjectRDFType type1) {
		String value = getBooleanValue(type1.isBlankNode());
		String filter = String.format("%s = %s AND ", ISBNODE_COLUMN, value);

		String select = CLASS_TABLE.getSELECT(filter);
		String insert = CLASS_TABLE.getINSERT("?, ?, " + value);
		RepositoryTableSlice view = new RepositoryTableSlice(type1, select, insert);
		views.put(view.getId(), view);
	}

	private void initObjectProperty(ObjectRDFType type1, ObjectRDFType type2) {
		String value1 = getBooleanValue(type1.isBlankNode());
		String value2 = getBooleanValue(type2.isBlankNode());
		String filter = String.format("%s = %s AND %s = %s AND ", ISBNODE_COLUMN, value1, ISBNODE2_COLUMN, value2);

		String select = OBJECT_PROPERTY_TABLE.getSELECT(filter);
		String insert = OBJECT_PROPERTY_TABLE.getINSERT("?, ?, ?, " + value1 + ", " + value2);
		RepositoryTableSlice view = new RepositoryTableSlice(type1, type2, select, insert);
		views.put(view.getId(), view);
	}

	private void initDataProperty(ObjectRDFType type1, RDFDatatype type2) {
		String value = getBooleanValue(type1.isBlankNode());
		String filter = String.format("%s = %s AND ", ISBNODE_COLUMN, value);

		final String select, insert;
		if (type2.getLanguageTag().isPresent()) {
			LanguageTag languageTag = type2.getLanguageTag().get();
			RepositoryTable table = DATA_PROPERTY_TABLE_MAP.get(RDF.LANGSTRING);
			select = table.getSELECT(filter + String.format("%s = '%s' AND ", LANG_COLUMN, languageTag.getFullString()));
			insert = table.getINSERT("?, ?, ?, ?, " + value);
		}
		else {
			RepositoryTable table = DATA_PROPERTY_TABLE_MAP.get(type2.getIRI());
			select = table.getSELECT(filter);
			insert = table.getINSERT("?, ?, ?, " + value);
		}

		RepositoryTableSlice view = new RepositoryTableSlice(type1, type2, select, insert);
		views.put(view.getId(), view);
	}

	private static String getBooleanValue(boolean b) {
		return b ? "TRUE" : "FALSE";
	}

	// these two values distinguish between COL_TYPE.OBJECT and COL_TYPE.BNODE
	private static final int OBJ_TYPE_URI = 0;
	private static final int OBJ_TYPE_BNode = 1;

	private static int COLTYPEtoInt(ObjectRDFType t) {
		return t.isBlankNode()  ? OBJ_TYPE_BNode : OBJ_TYPE_URI;
	}


	public void init(Statement st) throws SQLException {
		for (RepositoryTable table : ABOX_TABLES)
			st.addBatch(table.getCREATE());

		st.addBatch(EMPTINESS_INDEX_TABLE.getCREATE());
	}

	public boolean isDBSchemaDefined(Connection conn)  {

		try (Statement st = conn.createStatement()) {
			for (RepositoryTable table : ABOX_TABLES)
				st.executeQuery(table.getEXISTS());

			return true; // everything is fine if we get to this point
		}
		catch (Exception e) {
			return false;
		}
	}

	/**
	 * Stores the emptiness index in the database
	 * @throws SQLException 
	 */
	public void store(Connection conn) throws SQLException {

		try(Statement st = conn.createStatement()) {
			st.executeUpdate(EMPTINESS_INDEX_TABLE.getDELETE());
		}

		try (PreparedStatement stm = conn.prepareStatement(EMPTINESS_INDEX_TABLE.getINSERT("?, ?, ?, ?"))) {
			for (RepositoryTableSlice view : views.values()) {
				RepositoryTableSlice.Identifier viewId = view.getId();
				for (Integer idx : view.getIndexes()) {
					if (viewId.getType2() == null) {
						// class view (only type1 is relevant)
						stm.setInt(1, 0); //
						stm.setInt(2, idx);
						stm.setInt(3, COLTYPEtoInt(viewId.getType1()));
						stm.setInt(4, OBJ_TYPE_BNode);
					}
					else {
						// property view
						stm.setInt(1, colTypetoSITable.get(viewId.getType2()));
						stm.setInt(2, idx);
						stm.setInt(3, COLTYPEtoInt(viewId.getType1()));
						stm.setInt(4, COLTYPEtoInt((ObjectRDFType) viewId.getType2()));
					}
					
					stm.addBatch();
				}
			}
			stm.executeBatch();
		}
	}


	private static class UnexpectedRDFTermTypeException extends OntopInternalBugException {

		private UnexpectedRDFTermTypeException(RDFTermType termType) {
			super("Unexpected RDF term type used as property object: " + termType);
		}
	}

}
