package it.unibz.inf.ontop.si.repository.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.apache.commons.rdf.api.IRI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;

public class SemanticIndexViewsManager {

	private final Map<SemanticIndexViewID, SemanticIndexView> views = new HashMap<>();
	
	private final List<SemanticIndexView> propertyViews = new LinkedList<>();
	private final List<SemanticIndexView> classViews = new LinkedList<>();
	
	public SemanticIndexViewsManager() {
		init();
	}

	public List<SemanticIndexView> getPropertyViews() {
		return Collections.unmodifiableList(propertyViews);
	}
	
	public List<SemanticIndexView> getClassViews() {
		return Collections.unmodifiableList(classViews);
	}

	public SemanticIndexView getView(ObjectRDFType type) {
		SemanticIndexViewID viewId = new SemanticIndexViewID(type);
		return views.get(viewId);
	}
	
	public SemanticIndexView getView(ObjectRDFType type1, RDFTermType type2) {
		SemanticIndexViewID viewId = new SemanticIndexViewID(type1, type2);
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
	
	private final void init() {

		ImmutableList<ObjectRDFType> objectTypes = ImmutableList.of(TYPE_FACTORY.getIRITermType(),
				TYPE_FACTORY.getBlankNodeType());


		IRI[] datatypeIRIs = { XSD.BOOLEAN, XSD.DATETIME, XSD.DATETIMESTAMP, XSD.DECIMAL, XSD.DOUBLE, XSD.INTEGER,
				XSD.INT, XSD.UNSIGNED_INT, XSD.NEGATIVE_INTEGER, XSD.NON_NEGATIVE_INTEGER,
				XSD.POSITIVE_INTEGER, XSD.NON_POSITIVE_INTEGER, XSD.FLOAT,  XSD.LONG,
				XSD.STRING };

		for (ObjectRDFType type1 : objectTypes) {

			String value =  type1.isBlankNode() ? "TRUE" : "FALSE";
			String filter = "ISBNODE = " + value + " AND ";
			
			{
				String select = RDBMSSIRepositoryManager.classTable.getSELECT(filter);
				String insert = RDBMSSIRepositoryManager.classTable.getINSERT("?, ?, " + value);
				
				SemanticIndexViewID viewId = new SemanticIndexViewID(type1);
				SemanticIndexView view = new SemanticIndexView(viewId, select, insert);
				views.put(view.getId(), view);		
				classViews.add(view);
			}

			for (ObjectRDFType type2 : objectTypes) {
				initObjectProperty(type1, type2);
			}
			
			for (IRI iriType2 : datatypeIRIs) {
				initDataProperty(type1, TYPE_FACTORY.getDatatype(iriType2));
			}
		}
	}

	private void initDataProperty(ObjectRDFType type1, RDFDatatype type2) {
		String value =  type1.isBlankNode() ? "TRUE" : "FALSE";
		String filter = "ISBNODE = " + value + " AND ";

		String select, insert;

		if (type2.getLanguageTag().isPresent()) {
			/*
			 * If the mapping is for something of type Literal we need to add IS
			 * NULL or IS NOT NULL to the language column. IS NOT NULL might be
			 * redundant since we have another stage in Quest where we add IS NOT
			 * NULL for every variable in the head of a mapping.
			 */
			LanguageTag languageTag = type2.getLanguageTag().get();
			// Hack: use the RDFS Literal table to get the table description
			RDBMSSIRepositoryManager.TableDescription tableDescription = RDBMSSIRepositoryManager.ATTRIBUTE_TABLE_MAP
					.get(RDF.LANGSTRING);

			select = tableDescription.getSELECT("LANG = '" + languageTag.getFullString() +  "' AND " + filter);
			insert = tableDescription.getINSERT("?, ?, ?, ?, " + value);
		}
		else {
			RDBMSSIRepositoryManager.TableDescription tableDescription = RDBMSSIRepositoryManager.ATTRIBUTE_TABLE_MAP
					.get(type2.getIRI());

			select = tableDescription.getSELECT(filter);
			insert = tableDescription.getINSERT("?, ?, ?, " + value);
		}

		createViews(type1, type2, select, insert);
	}

	private void initObjectProperty(ObjectRDFType type1, ObjectRDFType type2) {
		String value1 =  type1.isBlankNode() ? "TRUE" : "FALSE";
		String value2 =  type2.isBlankNode() ? "TRUE" : "FALSE";
		String filter = "ISBNODE = " + value1 + " AND ";

		RDBMSSIRepositoryManager.TableDescription tableDescription = RDBMSSIRepositoryManager.ROLE_TABLE;

		String select = tableDescription.getSELECT(filter + "ISBNODE2 = " + value2 + " AND ");
		String insert = tableDescription.getINSERT("?, ?, ?, " + value1 + ", " + value2);

		createViews(type1, type2, select, insert);
	}

	private void createViews(ObjectRDFType type1, RDFTermType type2, String select, String insert) {
		SemanticIndexViewID viewId = new SemanticIndexViewID(type1, type2);
		SemanticIndexView view = new SemanticIndexView(viewId, select, insert);
		views.put(view.getId(), view);
		propertyViews.add(view);
	}
	
	
	
	// view id codes that are stored in DB (starts with 0)

	private static final IRI[] SITableToIRI = {
		XSD.STRING, XSD.INTEGER,
		XSD.LONG, XSD.DECIMAL, XSD.DOUBLE, XSD.DATETIME, 
		XSD.INT, XSD.UNSIGNED_INT, XSD.NEGATIVE_INTEGER, 
		XSD.NON_NEGATIVE_INTEGER, XSD.POSITIVE_INTEGER, XSD.NON_POSITIVE_INTEGER,
		XSD.FLOAT, XSD.BOOLEAN, XSD.DATETIMESTAMP};
	
	private static final Map<TermType, Integer> COLTYPEtoSITable = new HashMap<>();
	
	static {
		// special case of COL_TYPE.OBJECT and COL_TYPE.BNODE (both are mapped to 1)
		COLTYPEtoSITable.put(TYPE_FACTORY.getBlankNodeType(), 1);
		COLTYPEtoSITable.put(TYPE_FACTORY.getIRITermType(), 1);
		// Class SITable has value 0 (skip it)
		for (int i = 2; i < SITableToIRI.length; i++)
			COLTYPEtoSITable.put(TYPE_FACTORY.getDatatype(SITableToIRI[i]), i);
	}
	
	// these two values distinguish between COL_TYPE.OBJECT and COL_TYPE.BNODE
	private static final int OBJ_TYPE_URI = 0;
	private static final int OBJ_TYPE_BNode = 1;
	
	private static int COLTYPEtoInt(ObjectRDFType t) {
		return t.isBlankNode()  ? OBJ_TYPE_BNode : OBJ_TYPE_URI;
	}
	
	private static COL_TYPE IntToCOLTYPE(int t) {
		return (t == OBJ_TYPE_BNode) ? COL_TYPE.BNODE : COL_TYPE.OBJECT;
	}

	
	
	/**
	 * Stores the emptiness index in the database
	 * @throws SQLException 
	 */

	public void store(Connection conn) throws SQLException {
		
		try (PreparedStatement stm = conn.prepareStatement(RDBMSSIRepositoryManager.emptinessIndexTable.getINSERT("?, ?, ?, ?"))) {
			for (SemanticIndexView view : views.values()) {
				SemanticIndexViewID viewId = view.getId();
				for (Integer idx : view.getIndexes()) {
					if (viewId.getType2() == null) {
						// class view (only type1 is relevant)
						stm.setInt(1, 0); // SITable.CLASS.ordinal()
						stm.setInt(2, idx);
						stm.setInt(3, COLTYPEtoInt(viewId.getType1()));
						stm.setInt(4, OBJ_TYPE_BNode);
					}
					else {
						// property view
						stm.setInt(1, COLTYPEtoSITable.get(viewId.getType2()));
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
