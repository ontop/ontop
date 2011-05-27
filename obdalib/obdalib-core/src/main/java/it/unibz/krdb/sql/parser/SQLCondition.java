package it.unibz.krdb.sql.parser;

public class SQLCondition {

	private ISQLTerm term1 = null;
	private ISQLTerm term2 = null;
	private String operator = null;
	
	
	public SQLCondition (ISQLTerm t1, ISQLTerm t2, String op){
		term1 = t1;
		term2 =t2;
		operator = op;
	}
	
	public String toString (){
		
		return term1.toString() + operator + term2.toString();
	}
	
	public int hashCode(){
		return this.toString().hashCode();
	}
	
	public boolean equals(Object o){
		
		if(o instanceof SQLCondition){
			SQLCondition c = (SQLCondition) o;
			if(this.toString().equals(c.toString())){
				return true;
			}else if(operator.equals(c.operator) && (operator.equals("=")|| operator.equals("<>"))){
				if(term1.toString().equals(c.term2.toString()) && term2.toString().equals(c.term1.toString())){
					return true;
				}else{
					return false;
				}
					
			}else{
				return false;
			}
		}
		return false;
	}
}
