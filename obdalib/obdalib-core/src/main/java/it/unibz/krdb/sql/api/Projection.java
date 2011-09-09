package it.unibz.krdb.sql.api;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * 
 **/
public class Projection {
	
	private ArrayList<DerivedColumn> selectList;
	private Type type;
	
	public enum Type {
		DISTINCT, ALL
	};
	
	public Projection() {
		selectList = new ArrayList<DerivedColumn>(); 
	}
	
	public void setType(Type value) {
		type = value;
	}
	
	public String getType() {
		if (type == null) {
			return "";
		}
		switch(type) {
			case DISTINCT: return "distinct";
			case ALL: return "all";
		}
		return "";
	}
	
	public void add(DerivedColumn column) {
		selectList.add(column);
	}
	
	public void addAll(ArrayList<DerivedColumn> columns) {
		selectList.addAll(columns);
	}
	
	/**
	 * Updates the column list in this projection. Any existing
	 * columns are replaced by the new list.
	 * 
	 * @param columns
	 * 			The new column list.
	 */
	public void update(ArrayList<DerivedColumn> columns) {
		selectList.clear();
		addAll(columns);
	}
	
	public String[] getColumns(String table) {
		TreeSet<String> list = new TreeSet<String>();  // use set to avoid duplication.		
		for (DerivedColumn column : selectList) {
			ArrayList<ColumnReference> factors = column.getValueExpression().getAll();
			for (ColumnReference value : factors) {
				String columnOwner = value.getTable();
				if (columnOwner.equals(table)) {
					list.add(value.getColumn());
				}
			}
		}		
		return list.toArray(new String[0]);  // return the set as array.
	}
	
	public int size() {
		return selectList.size();
	}	
	
	@Override
	public String toString() {
		String str = "select";
		
		String type = getType();
		if (type != "") {
			str += " ";
			str += type;
		}
		
		boolean bNeedComma = false;
		for (DerivedColumn column : selectList) {
			if (bNeedComma) {
				str += ",";
			}
			str += " ";
			str += column.toString();
			bNeedComma = true;
		}
		return str + " " + "from";
	}
}