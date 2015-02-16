package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.Predicate.COL_TYPE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

	public SemanticIndexView getView(COL_TYPE type) {
		SemanticIndexViewID viewId = new SemanticIndexViewID(type);
		return views.get(viewId);
	}
	
	public SemanticIndexView getView(COL_TYPE type1, COL_TYPE type2) {
		SemanticIndexViewID viewId = new SemanticIndexViewID(type1, type2);
		return views.get(viewId);
	}
	
	
	
	private static final COL_TYPE[] objectTypes = { COL_TYPE.OBJECT, COL_TYPE.BNODE };

	private static final COL_TYPE[] typesAndObjectTypes = { COL_TYPE.OBJECT, COL_TYPE.BNODE, 
		COL_TYPE.LITERAL, COL_TYPE.LITERAL_LANG, COL_TYPE.BOOLEAN, 
		COL_TYPE.DATETIME, COL_TYPE.DATETIME_STAMP, COL_TYPE.DECIMAL, COL_TYPE.DOUBLE, COL_TYPE.INTEGER, COL_TYPE.INT,
		COL_TYPE.UNSIGNED_INT, COL_TYPE.NEGATIVE_INTEGER, COL_TYPE.NON_NEGATIVE_INTEGER, 
		COL_TYPE.POSITIVE_INTEGER, COL_TYPE.NON_POSITIVE_INTEGER, COL_TYPE.FLOAT,  COL_TYPE.LONG, 
		COL_TYPE.STRING };
	
	private final void init() {
		
		for (COL_TYPE type1 : objectTypes) {

			String value =  (type1 == COL_TYPE.BNODE) ? "TRUE" : "FALSE";
			String filter = "ISBNODE = " + value + " AND ";
			
			{
				String select = RDBMSSIRepositoryManager.classTable.getSELECT(filter);
				String insert = RDBMSSIRepositoryManager.classTable.getINSERT("?, ?, " + value);
				
				SemanticIndexViewID viewId = new SemanticIndexViewID(type1);
				SemanticIndexView view = new SemanticIndexView(viewId, select, insert);
				views.put(view.getId(), view);		
				classViews.add(view);
			}
			
			
			for (COL_TYPE type2 : typesAndObjectTypes) {
				String select, insert;
				
				switch (type2) {
					case OBJECT:
						select = RDBMSSIRepositoryManager.attributeTable.get(type2).getSELECT(filter + "ISBNODE2 = FALSE AND ");
						insert = RDBMSSIRepositoryManager.attributeTable.get(type2).getINSERT("?, ?, ?, " + value + ", FALSE");
						break;
					case BNODE:
						select = RDBMSSIRepositoryManager.attributeTable.get(COL_TYPE.OBJECT).getSELECT(filter + "ISBNODE2 = TRUE AND ");
						insert = RDBMSSIRepositoryManager.attributeTable.get(COL_TYPE.OBJECT).getINSERT("?, ?, ?, " + value + ", TRUE");
						break;
					case LITERAL:
						select = RDBMSSIRepositoryManager.attributeTable.get(type2).getSELECT("LANG IS NULL AND " + filter);
						insert = RDBMSSIRepositoryManager.attributeTable.get(type2).getINSERT("?, ?, ?, NULL, " + value);
						break;
					case LITERAL_LANG:
						/*
						 * If the mapping is for something of type Literal we need to add IS
						 * NULL or IS NOT NULL to the language column. IS NOT NULL might be
						 * redundant since we have another stage in Quest where we add IS NOT
						 * NULL for every variable in the head of a mapping.
						 */
						select = RDBMSSIRepositoryManager.attributeTable.get(COL_TYPE.LITERAL).getSELECT("LANG IS NOT NULL AND " + filter);
						insert = RDBMSSIRepositoryManager.attributeTable.get(COL_TYPE.LITERAL).getINSERT("?, ?, ?, ?, " + value);
						break;
					default:
						select = RDBMSSIRepositoryManager.attributeTable.get(type2).getSELECT(filter);
						insert = RDBMSSIRepositoryManager.attributeTable.get(type2).getINSERT("?, ?, ?, " + value);
				}

				SemanticIndexViewID viewId = new SemanticIndexViewID(type1, type2);
				SemanticIndexView view = new SemanticIndexView(viewId, select, insert);
				views.put(view.getId(), view);					
				propertyViews.add(view);
			}
		}		
	}
	
	
	
	// view id codes that are stored in DB (starts with 0)

	private static COL_TYPE[] SITableToCOLTYPE = { 
		null, // Class SITable 
		COL_TYPE.OBJECT, COL_TYPE.LITERAL, COL_TYPE.STRING, COL_TYPE.INTEGER,
		COL_TYPE.LONG, COL_TYPE.DECIMAL, COL_TYPE.DOUBLE, COL_TYPE.DATETIME, 
		COL_TYPE.INT, COL_TYPE.UNSIGNED_INT, COL_TYPE.NEGATIVE_INTEGER, 
		COL_TYPE.NON_NEGATIVE_INTEGER, COL_TYPE.POSITIVE_INTEGER, COL_TYPE.NON_POSITIVE_INTEGER,
		COL_TYPE.FLOAT, COL_TYPE.BOOLEAN, COL_TYPE.DATETIME_STAMP, COL_TYPE.LITERAL_LANG
	};
	
	private static Map<COL_TYPE, Integer> COLTYPEtoSITable = new HashMap<>();
	
	static {
		// special case of COL_TYPE.OBJECT and COL_TYPE.BNODE (both are mapped to 1)
		COLTYPEtoSITable.put(COL_TYPE.BNODE, 1);
		// Class SITable has value 0 (skip it)
		for (int i = 1; i < SITableToCOLTYPE.length; i++)
			COLTYPEtoSITable.put(SITableToCOLTYPE[i], i);
	}
	
	// these two values distinguish between COL_TYPE.OBJECT and COL_TYPE.BNODE
	private static final int OBJ_TYPE_URI = 0;
	private static final int OBJ_TYPE_BNode = 1;
	
	private static int COLTYPEtoInt(COL_TYPE t) {
		return (t == COL_TYPE.BNODE)  ? OBJ_TYPE_BNode : OBJ_TYPE_URI;
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
						stm.setInt(4, COLTYPEtoInt(viewId.getType2()));
					}
					
					stm.addBatch();
				}
			}
			stm.executeBatch();
		}
	}

	/**
	 * Restoring the emptiness index from the database
	 * @throws SQLException 
	 */
	
	public void load(Connection conn) throws SQLException {
		
		try (Statement st = conn.createStatement()) {
			ResultSet res = st.executeQuery(RDBMSSIRepositoryManager.emptinessIndexTable.getSELECT());
			while (res.next()) {
				int sitable = res.getInt(1);
				int type1 = res.getInt(3);
				int type2 = res.getInt(4);
				int idx = res.getInt(2);
				
				COL_TYPE coltype = SITableToCOLTYPE[sitable];
				SemanticIndexView view;
				if (coltype == null) {
					// class view
					view = getView(IntToCOLTYPE(type1));
				}
				else {
					// property view
					if (coltype ==  COL_TYPE.OBJECT)
						coltype = IntToCOLTYPE(type2);
					view = getView(IntToCOLTYPE(type1), coltype);
				}
				view.addIndex(idx);
			}
		}
	}
		
}
