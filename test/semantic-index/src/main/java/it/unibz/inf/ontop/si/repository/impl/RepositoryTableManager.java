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

import static it.unibz.inf.ontop.si.repository.impl.RepositoryTable.*;
import static it.unibz.inf.ontop.si.repository.impl.RepositoryTable.getNotNull;

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
			ImmutableMap.of(IDX_COLUMN, getNotNull(IDX_COLUMN_TYPE),
					URI_COLUMN, getNotNull(URI_COLUMN_TYPE),
					ISBNODE_COLUMN, getNotNull(ISBNODE_COLUMN_TYPE)),  getSelectListOf(URI_COLUMN));

	private final static RepositoryTable OBJECT_PROPERTY_TABLE = new RepositoryTable("QUEST_OBJECT_PROPERTY_ASSERTION",
			ImmutableMap.of(IDX_COLUMN, getNotNull(IDX_COLUMN_TYPE),
					URI_COLUMN, getNotNull(URI_COLUMN_TYPE),
					ISBNODE_COLUMN, getNotNull(ISBNODE_COLUMN_TYPE),
					URI2_COLUMN, getNotNull(URI_COLUMN_TYPE),
					ISBNODE2_COLUMN, getNotNull(ISBNODE_COLUMN_TYPE)),  getSelectListOf(URI_COLUMN, URI2_COLUMN));

	// LANG_STRING is special because of one extra attribute (LANG)
	private final static RepositoryTable LANGSTRING_DATA_PROPERTY_TABLE = new RepositoryTable("QUEST_DATA_PROPERTY_LITERAL_ASSERTION",
			ImmutableMap.of(IDX_COLUMN, getNotNull(IDX_COLUMN_TYPE),
					URI_COLUMN, getNotNull(URI_COLUMN_TYPE),
					ISBNODE_COLUMN, getNotNull(ISBNODE_COLUMN_TYPE),
					VAL_COLUMN, getNotNull(VAL_COLUMN_TYPE),
					LANG_COLUMN, getNotNull("VARCHAR(20)")),  getSelectListOf(URI_COLUMN, VAL_COLUMN));

	private final static RepositoryTable DEFAULT_TYPE_DATA_PROPERTY_TABLE = new RepositoryTable("QUEST_DATA_PROPERTY_DEFAULT_TYPE_ASSERTION",
			ImmutableMap.of(IDX_COLUMN, getNotNull(IDX_COLUMN_TYPE),
					URI_COLUMN, getNotNull(URI_COLUMN_TYPE),
					ISBNODE_COLUMN, getNotNull(ISBNODE_COLUMN_TYPE),
					VAL_COLUMN, getNotNull(VAL_COLUMN_TYPE),
					TYPE_COLUMN, getNotNull(IRI_COLUMN_TYPE)), getSelectListOf(URI_COLUMN, VAL_COLUMN));
	private static final ImmutableList<RepositoryTable> ABOX_TABLES = ImmutableList.of(
			CLASS_TABLE, OBJECT_PROPERTY_TABLE, LANGSTRING_DATA_PROPERTY_TABLE, DEFAULT_TYPE_DATA_PROPERTY_TABLE);

	@FunctionalInterface
	public interface PreparedStatementInsertAction {
		void setValue(PreparedStatement stm, RDFLiteralConstant o) throws SQLException;
	}

	private static final int VAL_PARAM_INDEX = 3;

	private static final PreparedStatementInsertAction DEFAULT_TYPE_TABLE_INSERT_STM = (stm, o) -> stm.setString(VAL_PARAM_INDEX, o.getValue());

	private final Map<ImmutableList<RDFTermType>, RepositoryTableSlice> views = new HashMap<>(); // fully mutable - see getView


	public Stream<RepositoryTableSlice> getViewsStream() {
		return views.values().stream();
	}

	public RepositoryTableSlice getView(ObjectRDFType type) {
		return views.computeIfAbsent(ImmutableList.of(type), RepositoryTableManager::initClass);
	}
	
	public RepositoryTableSlice getView(ObjectRDFType type1, RDFTermType type2) {
		ImmutableList<RDFTermType> viewId = ImmutableList.of(type1, type2);
		if (type2 instanceof RDFDatatype) {
			RDFDatatype t2 = (RDFDatatype)type2;
			if (t2.getLanguageTag().isPresent())
				return views.computeIfAbsent(viewId, RepositoryTableManager::initLangStringDataProperty);
			else
				return views.computeIfAbsent(viewId, RepositoryTableManager::initDefaultTypeDataProperty);
		}
		else if (type2 instanceof ObjectRDFType)
			return views.computeIfAbsent(viewId, RepositoryTableManager::initObjectProperty);
		else
			throw new UnexpectedRDFTermTypeException(type2);
	}

	private static RepositoryTableSlice initClass(ImmutableList<RDFTermType> id) {
		String isBlankNode = getBooleanLiteral(((ObjectRDFType) id.get(0)).isBlankNode());

		String select = CLASS_TABLE.getSELECT(ImmutableList.of(getEq(ISBNODE_COLUMN, isBlankNode)));
		String insert = CLASS_TABLE.getINSERT(String.format("?, ?, %s", isBlankNode));
		return new RepositoryTableSlice(id, select, insert, null);
	}

	private static RepositoryTableSlice initObjectProperty(ImmutableList<RDFTermType> id) {
		String isBlankNode1 = getBooleanLiteral(((ObjectRDFType) id.get(0)).isBlankNode());
		String isBlankNode2 = getBooleanLiteral(((ObjectRDFType) id.get(1)).isBlankNode());

		String select = OBJECT_PROPERTY_TABLE.getSELECT(ImmutableList.of(getEq(ISBNODE_COLUMN, isBlankNode1), getEq(ISBNODE2_COLUMN, isBlankNode2)));
		String insert = OBJECT_PROPERTY_TABLE.getINSERT(String.format("?, ?, %s, ?, %s", isBlankNode1, isBlankNode2));
		return new RepositoryTableSlice(id, select, insert, null);
	}

	private static RepositoryTableSlice initLangStringDataProperty(ImmutableList<RDFTermType> id) {
		String isBlankNode = getBooleanLiteral(((ObjectRDFType) id.get(0)).isBlankNode());
		String languageTag = getStringLiteral(((RDFDatatype) id.get(1)).getLanguageTag().get().getFullString());

		String select = LANGSTRING_DATA_PROPERTY_TABLE.getSELECT(ImmutableList.of(getEq(ISBNODE_COLUMN, isBlankNode), getEq(LANG_COLUMN, languageTag)));
		String insert = LANGSTRING_DATA_PROPERTY_TABLE.getINSERT(String.format("?, ?, %s, ?, %s", isBlankNode, languageTag));
		return new RepositoryTableSlice(id, select, insert, DEFAULT_TYPE_TABLE_INSERT_STM);
	}

	private static RepositoryTableSlice initDefaultTypeDataProperty(ImmutableList<RDFTermType> id) {
		String isBlankNode = getBooleanLiteral(((ObjectRDFType) id.get(0)).isBlankNode());
		String typeIri = getStringLiteral(((RDFDatatype) id.get(1)).getIRI().getIRIString());

		String select = DEFAULT_TYPE_DATA_PROPERTY_TABLE.getSELECT(ImmutableList.of(getEq(ISBNODE_COLUMN, isBlankNode), getEq(TYPE_COLUMN, typeIri)));
		String insert = DEFAULT_TYPE_DATA_PROPERTY_TABLE.getINSERT(String.format("?, ?, %s, ?, %s", isBlankNode, typeIri));
		return new RepositoryTableSlice(id, select, insert, DEFAULT_TYPE_TABLE_INSERT_STM);
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
