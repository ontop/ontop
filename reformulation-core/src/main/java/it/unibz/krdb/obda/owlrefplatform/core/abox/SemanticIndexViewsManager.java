package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Interval;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SemanticIndexViewsManager {

	private final Set<SemanticIndexRecord> nonEmptyEntityRecord = new HashSet<>();

	public void setNonEmpty(int idx, COL_TYPE t1, COL_TYPE t2) {
		SemanticIndexRecord record = new SemanticIndexRecord(t1, t2, idx);
		nonEmptyEntityRecord.add(record);
	}
	
	public void setNonEmpty(int idx, COL_TYPE t1) {
		SemanticIndexRecord record = new SemanticIndexRecord(t1, idx);
		nonEmptyEntityRecord.add(record);		
	}
	
	
	public boolean isMappingEmpty(List<Interval> intervals,  COL_TYPE type1)  {
		
		for (Interval interval : intervals) 
			for (int i = interval.getStart(); i <= interval.getEnd(); i++) {
				SemanticIndexRecord record = new SemanticIndexRecord(type1, i);
				if (nonEmptyEntityRecord.contains(record))
					return false;
			}
		
		return true;
	}

	public boolean isMappingEmpty(List<Interval> intervals, COL_TYPE type1, COL_TYPE type2)  {
		
		for (Interval interval : intervals) 
			for (int i = interval.getStart(); i <= interval.getEnd(); i++) {
				SemanticIndexRecord record = new SemanticIndexRecord(type1, type2, i);
				if (nonEmptyEntityRecord.contains(record)) 
					return false;
			}
		
		return true;
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

	
	
	/**
	 * Stores the emptiness index in the database
	 * @throws SQLException 
	 */

	public void store(Connection conn) throws SQLException {
		
		try (PreparedStatement stm = conn.prepareStatement(RDBMSSIRepositoryManager.emptinessIndexTable.insertCommand)) {
			for (SemanticIndexRecord record : nonEmptyEntityRecord) {
				stm.setInt(1, record.getTable());
				stm.setInt(2, record.getIndex());
				stm.setInt(3, record.getType1());
				stm.setInt(4, record.getType2());
				stm.addBatch();
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
				
				SemanticIndexRecord.checkTypeValue(type1);
				SemanticIndexRecord.checkTypeValue(type2);
				SemanticIndexRecord.checkSITableValue(sitable);
				
				SemanticIndexRecord r = new SemanticIndexRecord(sitable, type1, type2, idx);
				nonEmptyEntityRecord.add(r);
			}
		}
	}
	
}
