package it.unibz.krdb.sql.parser;

import java.util.Iterator;
import java.util.List;

public class SQLQuery {

	private List<SQLSelection> select = null;
	private List<SQLTable> tables = null;
	private List<SQLCondition> conditions = null;
	private boolean isDistinct = false;
	
	public SQLQuery(List<SQLSelection> select, List<SQLTable> tables, List<SQLCondition> condition, boolean isDistinct){
		this.conditions = condition;
		this.select = select;
		this.tables = tables;
		this.isDistinct = isDistinct;
	}
	
	
	
	public List<SQLSelection> getSelect() {
		return select;
	}



	public List<SQLTable> getTables() {
		return tables;
	}



	public List<SQLCondition> getConditions() {
		return conditions;
	}



	public boolean isDistinct() {
		return isDistinct;
	}



	public String toString(){
		
		String s="SELECT ";
		if(isDistinct){
			s = s +"DISTINCT ";
		}
		
		Iterator<SQLSelection> it1 = select.iterator();
		String selectstatement = "";
		while(it1.hasNext()){
			if(selectstatement.length() >0){
				selectstatement = selectstatement + ", ";
			}
			SQLSelection sel = it1.next();
			selectstatement = selectstatement + sel.toString();
		}
		
		s = s + selectstatement + " FROM ";
		
		Iterator<SQLTable> it2 = tables.iterator();
		String fromstatement = "";
		while(it2.hasNext()){
			if(fromstatement.length() > 0){
				fromstatement = fromstatement + ", ";
			}
			SQLTable t = it2.next();
	
			fromstatement = fromstatement + t.toString();
		}
		
		s = s + fromstatement; 
		if(conditions.size() >0){
			s = s+ " WHERE ";
			Iterator<SQLCondition> it3 = conditions.iterator();
			String wherestatement = "";
			while(it3.hasNext()){
				if(wherestatement.length() > 0){
					wherestatement = wherestatement +" AND ";
				}
				SQLCondition c = it3.next();
				wherestatement = wherestatement + c.toString();
				
			}
			
			s = s+ wherestatement ;
		}
		return s;
	}
}
