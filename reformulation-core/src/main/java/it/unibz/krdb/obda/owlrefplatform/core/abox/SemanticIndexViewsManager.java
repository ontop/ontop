package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Interval;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;

public class SemanticIndexViewsManager {

	private final Map<SemanticIndexViewID, Set<Integer>> nonEmptyEntityRecord = new HashMap<>();

	public SemanticIndexViewsManager() {
		init();
	}
	
	
	
	public boolean isViewEmpty(SemanticIndexViewID viewId, List<Interval> intervals) {
		
		Set<Integer> set = nonEmptyEntityRecord.get(viewId);
		if (set == null) 
			return true; // the set if empty
		
		for (Interval interval : intervals) 
			for (Integer i = interval.getStart(); i <= interval.getEnd(); i++) 
				if (set.contains(i)) 
					return false;
		
		return true;
	}

	public void addIndexToView(SemanticIndexViewID viewId, Integer idx) {
		Set<Integer> set = nonEmptyEntityRecord.get(viewId);
		if (set == null) {
			set = new HashSet<>();
			nonEmptyEntityRecord.put(viewId, set);
		}
		set.add(idx);			
	}
	
	
	private static final COL_TYPE objectTypes[] = new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.BNODE };

	private static final COL_TYPE typesAndObjectTypes[] = new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.BNODE, 
		COL_TYPE.LITERAL, COL_TYPE.LITERAL_LANG, COL_TYPE.BOOLEAN, 
		COL_TYPE.DATETIME, COL_TYPE.DATETIME_STAMP, COL_TYPE.DECIMAL, COL_TYPE.DOUBLE, COL_TYPE.INTEGER, COL_TYPE.INT,
		COL_TYPE.UNSIGNED_INT, COL_TYPE.NEGATIVE_INTEGER, COL_TYPE.NON_NEGATIVE_INTEGER, 
		COL_TYPE.POSITIVE_INTEGER, COL_TYPE.NON_POSITIVE_INTEGER, COL_TYPE.FLOAT,  COL_TYPE.LONG, 
		COL_TYPE.STRING };
	
	private final Map<SemanticIndexViewID, String> selectCommand = new HashMap<>();
	
	private final List<SemanticIndexViewID> propertyViewIds = new LinkedList<>();
	private final List<SemanticIndexViewID> classViewIds = new LinkedList<>();
	
	public List<SemanticIndexViewID> getPropertyViewIDs() {
		return Collections.unmodifiableList(propertyViewIds);
	}
	
	public List<SemanticIndexViewID> getClassViewIDs() {
		return Collections.unmodifiableList(classViewIds);
	}
	
	private final void init() {
		
		for (COL_TYPE type1 : objectTypes) {
			for (COL_TYPE type2 : typesAndObjectTypes) {
				StringBuilder sql = new StringBuilder();
				
				switch (type2) {
					case OBJECT:
					case BNODE:
						sql.append(RDBMSSIRepositoryManager.roleTable.selectCommand);
						break;
					case LITERAL:
					case LITERAL_LANG:
						sql.append(RDBMSSIRepositoryManager.attributeTable.get(COL_TYPE.LITERAL).selectCommand);
						break;
					default:
						sql.append(RDBMSSIRepositoryManager.attributeTable.get(type2).selectCommand);
				}

				/*
				 * If the mapping is for something of type Literal we need to add IS
				 * NULL or IS NOT NULL to the language column. IS NOT NULL might be
				 * redundant since we have another stage in Quest where we add IS NOT
				 * NULL for every variable in the head of a mapping.
				 */

				if (type1 == COL_TYPE.BNODE) 
					sql.append("ISBNODE = TRUE AND ");
				else {
					assert (type1 == COL_TYPE.OBJECT);
					sql.append("ISBNODE = FALSE AND ");
				}

				if (type2 == COL_TYPE.BNODE) 
					sql.append("ISBNODE2 = TRUE AND ");
				else if (type2 == COL_TYPE.OBJECT) 
					sql.append("ISBNODE2 = FALSE AND ");
				else if (type2 == COL_TYPE.LITERAL) 
					sql.append("LANG IS NULL AND ");
				else if (type2 == COL_TYPE.LITERAL_LANG)
					sql.append("LANG IS NOT NULL AND ");
				
				SemanticIndexViewID viewId = new SemanticIndexViewID(type1, type2);
				selectCommand.put(viewId, sql.toString());		
				
				propertyViewIds.add(viewId);
			}
		}

		for (COL_TYPE type : objectTypes) {
			StringBuilder sql = new StringBuilder();
			
			sql.append(RDBMSSIRepositoryManager.classTable.selectCommand);

			/*
			 * If the mapping is for something of type Literal we need to add IS
			 * NULL or IS NOT NULL to the language column. IS NOT NULL might be
			 * redundant since we have another stage in Quest where we add IS NOT
			 * NULL for every variable in the head of a mapping.
			 */

			if (type == COL_TYPE.BNODE) 
				sql.append("ISBNODE = TRUE AND ");
			else {
				assert (type == COL_TYPE.OBJECT);
				sql.append("ISBNODE = FALSE AND ");
			}
			
			SemanticIndexViewID viewId = new SemanticIndexViewID(type);
			selectCommand.put(viewId, sql.toString());	
			
			classViewIds.add(viewId);			
		}
		
	}
	
	
	public String getSqlSource(SemanticIndexViewID viewId) {
		return selectCommand.get(viewId);
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
			for (Entry<SemanticIndexViewID, Set<Integer>> record : nonEmptyEntityRecord.entrySet()) {
				if (record.getValue() != null) {
					SemanticIndexViewID viewId = record.getKey();
					for (Integer idx : record.getValue()) {
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
				if (coltype == null) {
					// class view
					SemanticIndexViewID viewId = new SemanticIndexViewID(IntToCOLTYPE(type1), null);
					addIndexToView(viewId, idx);
				}
				else {
					// property view
					if (coltype ==  COL_TYPE.OBJECT)
						coltype = IntToCOLTYPE(type2);
					SemanticIndexViewID viewId = new SemanticIndexViewID(IntToCOLTYPE(type1), coltype);
					addIndexToView(viewId, idx);					
				}
			}
		}
	}
		
}
