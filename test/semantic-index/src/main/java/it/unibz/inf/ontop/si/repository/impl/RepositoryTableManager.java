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

	// LANG_STRING is special because of one extra attribute (LANG)
	private final static RepositoryTable LANGSTRING_DATA_PROPERTY_TABLE = new RepositoryTable("QUEST_DATA_PROPERTY_LITERAL_ASSERTION",
			ImmutableMap.of(URI_COLUMN, "INTEGER NOT NULL",
					VAL_COLUMN, "VARCHAR(1000) NOT NULL",
					IDX_COLUMN, "SMALLINT NOT NULL",
					LANG_COLUMN, "VARCHAR(20)",
					ISBNODE_COLUMN, "BOOLEAN NOT NULL DEFAULT FALSE"),
			URI_COLUMN + " as X, " + VAL_COLUMN + " as Y, " + LANG_COLUMN + " as Z");

	// all other datatypes from COL_TYPE are treated similarly
	private final static ImmutableMap<IRI, RepositoryTable> DATA_PROPERTY_TABLE_MAP = ImmutableMap.<IRI, RepositoryTable>builder()
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
			.put(XSD.BOOLEAN, getDataPropertyTable("BOOLEAN", "BOOLEAN"))
			.put(XSD.DATETIMESTAMP, getDataPropertyTable("DATETIMESTAMP", "TIMESTAMP"))
			.build();
	private static final ImmutableList<RepositoryTable> ABOX_TABLES = Stream.concat(
			Stream.of(CLASS_TABLE, OBJECT_PROPERTY_TABLE, LANGSTRING_DATA_PROPERTY_TABLE), DATA_PROPERTY_TABLE_MAP.values().stream())
			.collect(ImmutableCollectors.toList());

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

	private final Map<RepositoryTableSlice.Identifier, RepositoryTableSlice> views = new HashMap<>(); // fully mutable - see getView


	public RepositoryTableManager(TypeFactory typeFactory) {

		ImmutableList<ObjectRDFType> objectTypes =
				ImmutableList.of(typeFactory.getIRITermType(), typeFactory.getBlankNodeType());

		for (ObjectRDFType type1 : objectTypes) {
			addView(initClass(type1));

			for (ObjectRDFType type2 : objectTypes)
				addView(initObjectProperty(type1, type2));

			// LANGSTRING is treated differently as it depends on a particular language - see getView
			for (Map.Entry<IRI, RepositoryTable> e : DATA_PROPERTY_TABLE_MAP.entrySet())
				addView(initDataProperty(type1, typeFactory.getDatatype(e.getKey()), e.getValue()));
		}
	}

	private void addView(RepositoryTableSlice view) {
		views.put(view.getId(), view);
	}
	public Stream<RepositoryTableSlice> getViewsStream() {
		return views.values().stream();
	}

	public RepositoryTableSlice getView(ObjectRDFType type) {
		RepositoryTableSlice.Identifier viewId = new RepositoryTableSlice.Identifier(type);
		return views.get(viewId);
	}
	
	public RepositoryTableSlice getView(ObjectRDFType type1, RDFTermType type2) {
		RepositoryTableSlice.Identifier viewId = new RepositoryTableSlice.Identifier(type1, type2);

		if (!views.containsKey(viewId))
			if (type2 instanceof RDFDatatype)
				addView(initLangStringDataProperty(type1, (RDFDatatype) type2));
			else
				throw new UnexpectedRDFTermTypeException(type2);

		return views.get(viewId);
	}

	private RepositoryTableSlice initClass(ObjectRDFType type1) {
		String value = getBooleanValue(type1.isBlankNode());
		String filter = String.format("%s = %s AND ", ISBNODE_COLUMN, value);

		String select = CLASS_TABLE.getSELECT(filter);
		String insert = CLASS_TABLE.getINSERT("?, ?, " + value);
		return new RepositoryTableSlice(type1, select, insert);
	}

	private RepositoryTableSlice initObjectProperty(ObjectRDFType type1, ObjectRDFType type2) {
		String value1 = getBooleanValue(type1.isBlankNode());
		String value2 = getBooleanValue(type2.isBlankNode());
		String filter = String.format("%s = %s AND %s = %s AND ", ISBNODE_COLUMN, value1, ISBNODE2_COLUMN, value2);

		String select = OBJECT_PROPERTY_TABLE.getSELECT(filter);
		String insert = OBJECT_PROPERTY_TABLE.getINSERT("?, ?, ?, " + value1 + ", " + value2);
		return new RepositoryTableSlice(type1, type2, select, insert);
	}

	private RepositoryTableSlice initDataProperty(ObjectRDFType type1, RDFDatatype type2, RepositoryTable table) {
		String value = getBooleanValue(type1.isBlankNode());
		String filter = String.format("%s = %s AND ", ISBNODE_COLUMN, value);

		String select = table.getSELECT(filter);
		String insert = table.getINSERT("?, ?, ?, " + value);
		return new RepositoryTableSlice(type1, type2, select, insert);
	}

	private RepositoryTableSlice initLangStringDataProperty(ObjectRDFType type1, RDFDatatype type2) {
		String value = getBooleanValue(type1.isBlankNode());
		String filter = String.format("%s = %s AND ", ISBNODE_COLUMN, value);

		if (!type2.getLanguageTag().isPresent())
			throw new UnexpectedRDFTermTypeException(type2);

		LanguageTag languageTag = type2.getLanguageTag().get();
		String select = LANGSTRING_DATA_PROPERTY_TABLE.getSELECT(filter + String.format("%s = '%s' AND ", LANG_COLUMN, languageTag.getFullString()));
		String insert = LANGSTRING_DATA_PROPERTY_TABLE.getINSERT("?, ?, ?, ?, " + value);
		return new RepositoryTableSlice(type1, type2, select, insert);
	}

	private static String getBooleanValue(boolean b) {
		return b ? "TRUE" : "FALSE";
	}


	public void init(Statement st) throws SQLException {
		for (RepositoryTable table : ABOX_TABLES)
			st.addBatch(table.getCREATE());
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

	}


	private static class UnexpectedRDFTermTypeException extends OntopInternalBugException {

		private UnexpectedRDFTermTypeException(RDFTermType termType) {
			super("Unexpected RDF term type used as property object: " + termType);
		}
	}

}
