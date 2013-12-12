package it.unibz.krdb.sql.api;

public class NullPredicate implements IPredicate, ICondition {
	
	private static final long serialVersionUID = -5825503995391673078L;

	private ReferenceValueExpression rowValueExpression;
	
	private boolean useIsNullOperator;
	
	public NullPredicate(ColumnReference column, boolean useIsNullOperator) {
		rowValueExpression = new ReferenceValueExpression();
		rowValueExpression.add(column);
		
		this.useIsNullOperator = useIsNullOperator;
	}
	
	public boolean useIsNullOperator() {
		return useIsNullOperator;
	}
	
	public IValueExpression getValueExpression() {
		return rowValueExpression;
	}
	
	@Override
	public String toString() {
		String str = rowValueExpression.toString();
		str += " IS";
		if (!useIsNullOperator) {
			str += " NOT";
		}
		str += " NULL";
		return str;
	}
}
