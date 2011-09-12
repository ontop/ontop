package it.unibz.krdb.sql.api;

/**
 * This class represents the natural join operation in the
 * SQL query.
 */
public class NaturalJoin extends JoinOperator {

	public static final int JOIN = 0;
	public static final int INNER_JOIN = 1;
	public static final int LEFT_JOIN = 2;
	public static final int RIGHT_JOIN = 3;
	public static final int FULL_JOIN = 4;
	public static final int LEFT_OUTER_JOIN = 5;
	public static final int RIGHT_OUTER_JOIN = 6;
	public static final int FULL_OUTER_JOIN = 7;
	
	private int joinType;

	public NaturalJoin() {
		this(NaturalJoin.JOIN);
	}
	
	public NaturalJoin(int joinType) {
		setType(joinType);
	}
	
	public void setType(int type) {
		joinType = type;
	}
	
	public String getType() {
		switch(joinType) {
			case JOIN: return "join";
			case INNER_JOIN: return "inner join";
			case LEFT_JOIN: return "left join";
			case RIGHT_JOIN: return "right join";
			case FULL_JOIN: return "full join";
			case LEFT_OUTER_JOIN: return "left outer join";
			case RIGHT_OUTER_JOIN: return "right outer join";
			case FULL_OUTER_JOIN: return "full outer join";
			default: return "join";
		}
	}
	
	@Override
	public String toString() {
		String str = "%s " + getType() + " %s";
		
		str += "on";		
		for (Object obj : conditions) {
			str += " ";
			str += obj.toString();
		}		
		return str;
	}
}