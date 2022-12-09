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
	public static final String TYPE_COLUMN = "TYPE";

	private static final String IDX_COLUMN_TYPE = "SMALLINT";
	private static final String URI_COLUMN_TYPE = "VARCHAR(200)";
	private static final String ISBNODE_COLUMN_TYPE = "BOOLEAN";
	private static final String VAL_COLUMN_TYPE = "VARCHAR(1000)";
	private static final String IRI_COLUMN_TYPE = "VARCHAR(200)";

	private final static RepositoryTable CLASS_TABLE = new RepositoryTable("QUEST_CLASS_ASSERTION",
			ImmutableMap.of(IDX_COLUMN, IDX_COLUMN_TYPE,
					URI_COLUMN, URI_COLUMN_TYPE,
					ISBNODE_COLUMN, ISBNODE_COLUMN_TYPE),  RepositoryTable.getSelectListOf(URI_COLUMN));

	private final static RepositoryTable OBJECT_PROPERTY_TABLE = new RepositoryTable("QUEST_OBJECT_PROPERTY_ASSERTION",
			ImmutableMap.of(IDX_COLUMN, IDX_COLUMN_TYPE,
					URI_COLUMN, URI_COLUMN_TYPE,
					ISBNODE_COLUMN, ISBNODE_COLUMN_TYPE,
					URI2_COLUMN, URI_COLUMN_TYPE,
					ISBNODE2_COLUMN, ISBNODE_COLUMN_TYPE),  RepositoryTable.getSelectListOf(URI_COLUMN, URI2_COLUMN));

	// LANG_STRING is special because of one extra attribute (LANG)
	private final static RepositoryTable LANGSTRING_DATA_PROPERTY_TABLE = new RepositoryTable("QUEST_DATA_PROPERTY_LITERAL_ASSERTION",
			ImmutableMap.of(IDX_COLUMN, IDX_COLUMN_TYPE,
					URI_COLUMN, URI_COLUMN_TYPE,
					ISBNODE_COLUMN, ISBNODE_COLUMN_TYPE,
					VAL_COLUMN, VAL_COLUMN_TYPE,
					LANG_COLUMN, "VARCHAR(20)"),  RepositoryTable.getSelectListOf(URI_COLUMN, VAL_COLUMN));

	private final static RepositoryTable DEFAULT_TYPE_DATA_PROPERTY_TABLE = new RepositoryTable("QUEST_DATA_PROPERTY_DEFAULT_TYPE_ASSERTION",
			ImmutableMap.of(IDX_COLUMN, IDX_COLUMN_TYPE,
					URI_COLUMN, URI_COLUMN_TYPE,
					ISBNODE_COLUMN, ISBNODE_COLUMN_TYPE,
					VAL_COLUMN, VAL_COLUMN_TYPE,
					TYPE_COLUMN, IRI_COLUMN_TYPE), RepositoryTable.getSelectListOf(URI_COLUMN, VAL_COLUMN));
	private static final ImmutableList<RepositoryTable> ABOX_TABLES = ImmutableList.of(
			CLASS_TABLE, OBJECT_PROPERTY_TABLE, LANGSTRING_DATA_PROPERTY_TABLE, DEFAULT_TYPE_DATA_PROPERTY_TABLE);

	@FunctionalInterface
	public interface PreparedStatementInsertAction {
		void setValue(PreparedStatement stm, RDFLiteralConstant o) throws SQLException;
	}

	private static final int VAL_PARAM_INDEX = 3;

	private static final PreparedStatementInsertAction DEFAULT_TYPE_TABLE_INSERT_STM = (stm, o) -> stm.setString(VAL_PARAM_INDEX, o.getValue());

	private final Map<RepositoryTableSlice.Identifier, RepositoryTableSlice> views = new HashMap<>(); // fully mutable - see getView


	public RepositoryTableManager(TypeFactory typeFactory) {

		ImmutableList<ObjectRDFType> objectTypes =
				ImmutableList.of(typeFactory.getIRITermType(), typeFactory.getBlankNodeType());

		for (ObjectRDFType type1 : objectTypes) {
			addView(initClass(type1));

			for (ObjectRDFType type2 : objectTypes)
				addView(initObjectProperty(type1, type2));
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
			if (type2 instanceof RDFDatatype) {
				RDFDatatype t2 = (RDFDatatype)type2;
				if (t2.getLanguageTag().isPresent())
					addView(initLangStringDataProperty(type1, t2));
				else
					addView(initDefaultTypeDataProperty(type1, t2));
			}
			else
				throw new UnexpectedRDFTermTypeException(type2);

		return views.get(viewId);
	}

	private RepositoryTableSlice initClass(ObjectRDFType type1) {
		String isBlankNode = getBooleanValue(type1.isBlankNode());
		String filter = String.format("%s = %s AND ", ISBNODE_COLUMN, isBlankNode);

		String select = CLASS_TABLE.getSELECT(filter);
		String insert = CLASS_TABLE.getINSERT(String.format("?, ?, %s", isBlankNode));
		return new RepositoryTableSlice(type1, select, insert);
	}

	private RepositoryTableSlice initObjectProperty(ObjectRDFType type1, ObjectRDFType type2) {
		String isBlankNode1 = getBooleanValue(type1.isBlankNode());
		String isBlankNode2 = getBooleanValue(type2.isBlankNode());
		String filter = String.format("%s = %s AND %s = %s AND ", ISBNODE_COLUMN, isBlankNode1, ISBNODE2_COLUMN, isBlankNode2);

		String select = OBJECT_PROPERTY_TABLE.getSELECT(filter);
		String insert = OBJECT_PROPERTY_TABLE.getINSERT(String.format("?, ?, %s, ?, %s", isBlankNode1, isBlankNode2));
		return new RepositoryTableSlice(type1, type2, select, insert, null);
	}

	private RepositoryTableSlice initLangStringDataProperty(ObjectRDFType type1, RDFDatatype type2) {
		String isBlankNode = getBooleanValue(type1.isBlankNode());
		String filter = String.format("%s = %s AND ", ISBNODE_COLUMN, isBlankNode);

		String languageTag = type2.getLanguageTag().get().getFullString();
		String select = LANGSTRING_DATA_PROPERTY_TABLE.getSELECT(filter + String.format("%s = '%s' AND ", LANG_COLUMN, languageTag));
		String insert = LANGSTRING_DATA_PROPERTY_TABLE.getINSERT(String.format("?, ?, %s, ?, '%s'", isBlankNode, languageTag));
		return new RepositoryTableSlice(type1, type2, select, insert, DEFAULT_TYPE_TABLE_INSERT_STM);
	}

	private RepositoryTableSlice initDefaultTypeDataProperty(ObjectRDFType type1, RDFDatatype type2) {
		String isBlankNode = getBooleanValue(type1.isBlankNode());
		String filter = String.format("%s = %s AND ", ISBNODE_COLUMN, isBlankNode);

		String typeIri = type2.getIRI().getIRIString();
		String select = DEFAULT_TYPE_DATA_PROPERTY_TABLE.getSELECT(filter + String.format("%s = '%s' AND ", TYPE_COLUMN, typeIri));
		String insert = DEFAULT_TYPE_DATA_PROPERTY_TABLE.getINSERT(String.format("?, ?, %s, ?, '%s'", isBlankNode, typeIri));
		return new RepositoryTableSlice(type1, type2, select, insert, DEFAULT_TYPE_TABLE_INSERT_STM);
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
