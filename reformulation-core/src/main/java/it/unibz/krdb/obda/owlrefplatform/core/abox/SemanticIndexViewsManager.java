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
	
	
	
	private static final COL_TYPE objectTypes[] = new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.BNODE };

	private static final COL_TYPE typesAndObjectTypes[] = new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.BNODE, 
		COL_TYPE.LITERAL, COL_TYPE.LITERAL_LANG, COL_TYPE.BOOLEAN, 
		COL_TYPE.DATETIME, COL_TYPE.DATETIME_STAMP, COL_TYPE.DECIMAL, COL_TYPE.DOUBLE, COL_TYPE.INTEGER, COL_TYPE.INT,
		COL_TYPE.UNSIGNED_INT, COL_TYPE.NEGATIVE_INTEGER, COL_TYPE.NON_NEGATIVE_INTEGER, 
		COL_TYPE.POSITIVE_INTEGER, COL_TYPE.NON_POSITIVE_INTEGER, COL_TYPE.FLOAT,  COL_TYPE.LONG, 
		COL_TYPE.STRING };
	
	private final void init() {
		
		for (COL_TYPE type1 : objectTypes) {
			for (COL_TYPE type2 : typesAndObjectTypes) {
				StringBuilder select = new StringBuilder();
				StringBuilder insert = new StringBuilder();
				
				switch (type2) {
					case OBJECT:
						select.append(RDBMSSIRepositoryManager.attributeTable.get(type2).selectCommand)
							  .append("ISBNODE2 = FALSE AND ");
						insert.append("INSERT INTO ")
							  .append(RDBMSSIRepositoryManager.attributeTable.get(type2).tableName)
							  .append("(URI1, URI2, IDX, ISBNODE2, ISBNODE) VALUES (?, ?, ?, FALSE, ");
						break;
					case BNODE:
						select.append(RDBMSSIRepositoryManager.attributeTable.get(COL_TYPE.OBJECT).selectCommand)
						      .append("ISBNODE2 = TRUE AND ");
						insert.append("INSERT INTO ")
						      .append(RDBMSSIRepositoryManager.attributeTable.get(COL_TYPE.OBJECT).tableName)
						      .append("(URI1, URI2, IDX, ISBNODE2, ISBNODE) VALUES (?, ?, ?, TRUE, ");
						break;
					case LITERAL:
						select.append(RDBMSSIRepositoryManager.attributeTable.get(type2).selectCommand)
							  .append("LANG IS NULL AND ");
						insert.append("INSERT INTO ")
					      	  .append(RDBMSSIRepositoryManager.attributeTable.get(type2).tableName)
					          .append("(URI, VAL, IDX, LANG, ISBNODE) VALUES (?, ?, ?, NULL, ");
						break;
					case LITERAL_LANG:
						select.append(RDBMSSIRepositoryManager.attributeTable.get(COL_TYPE.LITERAL).selectCommand)
							  .append("LANG IS NOT NULL AND ");
						insert.append("INSERT INTO ")
				      	  	  .append(RDBMSSIRepositoryManager.attributeTable.get(COL_TYPE.LITERAL).tableName)
				      	  	  .append("(URI, VAL, IDX, LANG, ISBNODE) VALUES (?, ?, ?, ?, ");
						break;
					default:
						select.append(RDBMSSIRepositoryManager.attributeTable.get(type2).selectCommand);
						insert.append("INSERT INTO ")
				      	  	  .append(RDBMSSIRepositoryManager.attributeTable.get(type2).tableName)
				      	  	  .append("(URI, VAL, IDX, ISBNODE) VALUES (?, ?, ?, ");				
				}

				/*
				 * If the mapping is for something of type Literal we need to add IS
				 * NULL or IS NOT NULL to the language column. IS NOT NULL might be
				 * redundant since we have another stage in Quest where we add IS NOT
				 * NULL for every variable in the head of a mapping.
				 */

				if (type1 == COL_TYPE.BNODE) { 
					select.append("ISBNODE = TRUE AND ");
					insert.append("TRUE)");
				}
				else {
					assert (type1 == COL_TYPE.OBJECT);
					select.append("ISBNODE = FALSE AND ");
					insert.append("FALSE)");
				}
				
				SemanticIndexViewID viewId = new SemanticIndexViewID(type1, type2);
				SemanticIndexView view = new SemanticIndexView(viewId, select.toString(), insert.toString());
				views.put(view.getId(), view);					
				propertyViews.add(view);
			}
		}

		for (COL_TYPE type : objectTypes) {
			StringBuilder select = new StringBuilder();
			StringBuilder insert = new StringBuilder();
			
			select.append(RDBMSSIRepositoryManager.classTable.selectCommand);
			insert.append("INSERT INTO ")
				.append(RDBMSSIRepositoryManager.classTable.tableName)
				.append(" (URI, IDX, ISBNODE) VALUES (?, ?, ");


			/*
			 * If the mapping is for something of type Literal we need to add IS
			 * NULL or IS NOT NULL to the language column. IS NOT NULL might be
			 * redundant since we have another stage in Quest where we add IS NOT
			 * NULL for every variable in the head of a mapping.
			 */

			if (type == COL_TYPE.BNODE) { 
				select.append("ISBNODE = TRUE AND ");
				insert.append("TRUE)");
			}
			else {
				assert (type == COL_TYPE.OBJECT);
				select.append("ISBNODE = FALSE AND ");
				insert.append("FALSE)");
			}
			
			SemanticIndexViewID viewId = new SemanticIndexViewID(type);
			SemanticIndexView view = new SemanticIndexView(viewId, select.toString(), insert.toString());
			views.put(view.getId(), view);		
			classViews.add(view);
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
		
		try (PreparedStatement stm = conn.prepareStatement(RDBMSSIRepositoryManager.emptinessIndexTable.insertCommand)) {
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
			ResultSet res = st.executeQuery("SELECT * FROM " + RDBMSSIRepositoryManager.emptinessIndexTable.tableName);
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
