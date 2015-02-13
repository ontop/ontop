package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Interval;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SemanticIndexViewsManager {

	private final Map<SemanticIndexViewID, Set<Integer>> nonEmptyEntityRecord = new HashMap<>();

	public static SemanticIndexViewID getViewId(COL_TYPE t1, COL_TYPE t2) {
		SemanticIndexViewID viewId = new SemanticIndexViewID(COLTYPEtoSITable.get(t2).ordinal(), COLTYPEtoInt(t1), COLTYPEtoInt(t2));
		return viewId;
	}
	
	public static SemanticIndexViewID getViewId(COL_TYPE t1) {
		SemanticIndexViewID viewId = new SemanticIndexViewID(SITable.CLASS.ordinal(), COLTYPEtoInt(t1), OBJ_TYPE_BNode);
		return viewId;
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
	
	
	public String constructSqlSource(COL_TYPE type) {
		
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
		
		return sql.toString();		
	}	
	
	public String constructSqlSource(COL_TYPE type1, COL_TYPE type2) {
		
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
		
		return sql.toString();		
	}	

	
	
	// the order provides datatype codes that are stored in DB (starts with 0) 
	private static enum SITable {
		CLASS, 
		OPROP, 
		DPROPLite, 
		DPROPStri, 
		DPROPInte, 
		DPROPLong, 
		DPROPDeci, 
		DPROPDoub, 
		DPROPDate, 
		DPROPInt, 
		DPROPUnsignedInt, 
		DPROPNegInte, 
		DPROPNonNegInte, 
		DPROPPosInte, 
		DPROPNonPosInte, 
		DPROPFloat, 
		DPROPBool,
		DPROPDateStamp,
		DPROPLiteLang
	}

	private static final int OBJ_TYPE_URI = 0;
	private static final int OBJ_TYPE_BNode = 1;

	private static int COLTYPEtoInt(COL_TYPE t) {
		return (t == COL_TYPE.BNODE)  ? OBJ_TYPE_BNode : OBJ_TYPE_URI;
	}
	
	private static Map<COL_TYPE, SITable> COLTYPEtoSITable = new HashMap<>();
	
	static {
		COLTYPEtoSITable.put(COL_TYPE.OBJECT, SITable.OPROP);
		COLTYPEtoSITable.put(COL_TYPE.BNODE, SITable.OPROP);
		COLTYPEtoSITable.put(COL_TYPE.LITERAL, SITable.DPROPLite);
		COLTYPEtoSITable.put(COL_TYPE.LITERAL_LANG, SITable.DPROPLiteLang);
		COLTYPEtoSITable.put(COL_TYPE.STRING, SITable.DPROPStri);
		COLTYPEtoSITable.put(COL_TYPE.INTEGER, SITable.DPROPInte);
		COLTYPEtoSITable.put(COL_TYPE.INT, SITable.DPROPInt);
		COLTYPEtoSITable.put(COL_TYPE.UNSIGNED_INT, SITable.DPROPUnsignedInt);
		COLTYPEtoSITable.put(COL_TYPE.NEGATIVE_INTEGER, SITable.DPROPNegInte);
		COLTYPEtoSITable.put(COL_TYPE.NON_NEGATIVE_INTEGER, SITable.DPROPNonNegInte);
		COLTYPEtoSITable.put(COL_TYPE.POSITIVE_INTEGER, SITable.DPROPPosInte);
		COLTYPEtoSITable.put(COL_TYPE.NON_POSITIVE_INTEGER, SITable.DPROPNonPosInte);
		COLTYPEtoSITable.put(COL_TYPE.FLOAT, SITable.DPROPFloat);
		COLTYPEtoSITable.put(COL_TYPE.LONG, SITable.DPROPLong);
		COLTYPEtoSITable.put(COL_TYPE.DECIMAL, SITable.DPROPDeci);
		COLTYPEtoSITable.put(COL_TYPE.DOUBLE, SITable.DPROPDoub);
		COLTYPEtoSITable.put(COL_TYPE.DATETIME, SITable.DPROPDate);
		COLTYPEtoSITable.put(COL_TYPE.BOOLEAN, SITable.DPROPBool);
		COLTYPEtoSITable.put(COL_TYPE.DATETIME_STAMP, SITable.DPROPDateStamp);
	}
	
	
	/**
	 * checks that type is one of the two valid values
	 * 
	 * @param type
	 * @throws RuntimeException if type is not valid
	 */
	
	private static void checkTypeValue(int type) { 
		if (type != OBJ_TYPE_URI && type != OBJ_TYPE_BNode)
			throw new RuntimeException("Unknown OBJ_TYPE:" + type);
	}
	
	/**
	 * checks that table is one of the valid values
	 * 
	 * @param table
	 * @throws RuntimeException if table is not valid
	 */

	private static void checkSITableValue(int table) {
		SITable sitable = null;		
		for (SITable t : SITable.values()) 
			if (t.ordinal() == table) 
				sitable = t;
		
		if (sitable == null)
			throw new RuntimeException("Unknown SITable: " + table);
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
						stm.setInt(1, viewId.getTable());
						stm.setInt(2, idx);
						stm.setInt(3, viewId.getType1());
						stm.setInt(4, viewId.getType2());
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
				
				checkTypeValue(type1);
				checkTypeValue(type2);
				checkSITableValue(sitable);
				
				SemanticIndexViewID viewId = new SemanticIndexViewID(sitable, type1, type2);
				addIndexToView(viewId, idx);
			}
		}
	}
		
}
