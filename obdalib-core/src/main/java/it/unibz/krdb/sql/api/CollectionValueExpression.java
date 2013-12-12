package it.unibz.krdb.sql.api;

public class CollectionValueExpression extends AbstractValueExpression {

	private static final long serialVersionUID = -8519994184134506857L;
	
	/**
	 * The name of the function operation.
	 */
	private String functionOp = "";
	
	@Override
	public void putSpecification(Object obj) {
		functionOp = (String)obj;
	}
	
	public String getSpecification() {
		return functionOp;
	}

	@Override
	public String toString() {
		return functionOp + "(" + factors.get(0) + ")";
	}
}
