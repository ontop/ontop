package it.unibz.krdb.obda.model;

public enum ExpressionOperation implements OperationPredicate {

	/* Numeric operations */

	MINUS("minus", null, null), // TODO (ROMAN): check -- never used
	ADD("add", null, null, null),
	SUBTRACT("subtract", null, null, null),
	MULTIPLY("multiply", null, null, null),
	DIVIDE("divide", null, null, null),
	ABS("abs", null, null),
	ROUND("round", null, null),
	CEIL("ceil", null, null),
	FLOOR("floor", null, null),
	RAND("RAND", COL_TYPE.DOUBLE),
	
	/* Boolean operations */

	AND("AND", COL_TYPE.BOOLEAN, null, null),
	OR("OR", COL_TYPE.BOOLEAN, null, null),
	NOT("NOT", COL_TYPE.BOOLEAN, COL_TYPE.BOOLEAN),
	
	EQ("EQ", COL_TYPE.BOOLEAN, null, null),
	NEQ("NEQ", COL_TYPE.BOOLEAN, null, null),
	GTE("GTE", COL_TYPE.BOOLEAN, null, null),
	GT("GT", COL_TYPE.BOOLEAN, null, null),
	LTE("LTE", COL_TYPE.BOOLEAN, null, null),
	LT("LT", COL_TYPE.BOOLEAN, null, null),

	IS_NULL("IS_NULL", COL_TYPE.BOOLEAN, null),
	IS_NOT_NULL("IS_NOT_NULL", COL_TYPE.BOOLEAN, null),
	IS_TRUE("IS_TRUE", COL_TYPE.BOOLEAN, null),

	STR_STARTS("STRSTARTS", COL_TYPE.BOOLEAN, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	STR_ENDS("STRENDS", COL_TYPE.BOOLEAN, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	CONTAINS("CONTAINS", COL_TYPE.BOOLEAN, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	
	/* SPARQL String functions */

	STRLEN("STRLEN", COL_TYPE.INTEGER, COL_TYPE.LITERAL), 
	UCASE("UCASE", COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	LCASE("LCASE", COL_TYPE.LITERAL, COL_TYPE.LITERAL), 
	SUBSTR("SUBSTR", COL_TYPE.LITERAL, COL_TYPE.LITERAL, COL_TYPE.INTEGER, COL_TYPE.INTEGER),
	STRBEFORE("STRBEFORE", COL_TYPE.LITERAL, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	STRAFTER("STRAFTER", COL_TYPE.LITERAL, COL_TYPE.LITERAL, COL_TYPE.LITERAL), 
	REPLACE("REPLACE", COL_TYPE.LITERAL, COL_TYPE.LITERAL, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	CONCAT("CONCAT", COL_TYPE.LITERAL, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	ENCODE_FOR_URI("ENCODE_FOR_URI", COL_TYPE.LITERAL, COL_TYPE.LITERAL),

	/* Hash functions */
	
	MD5("MD5", COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	SHA1("SHA1", COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	SHA512("SHA521", COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	SHA256("SHA256", COL_TYPE.LITERAL, COL_TYPE.LITERAL),

	/* SPARQL Functions on Dates and Times */

	NOW("NOW", COL_TYPE.DATETIME_STAMP),
	YEAR("YEAR", COL_TYPE.INTEGER, COL_TYPE.DATETIME_STAMP),
	DAY("DAY", COL_TYPE.INTEGER, COL_TYPE.DATETIME_STAMP),
	MONTH("MONTH", COL_TYPE.INTEGER, COL_TYPE.DATETIME_STAMP),
	HOURS("HOURS",COL_TYPE.INTEGER,  COL_TYPE.DATETIME_STAMP),
	MINUTES("MINUTES", COL_TYPE.INTEGER, COL_TYPE.DATETIME_STAMP),
	SECONDS("SECONDS", COL_TYPE.DECIMAL, COL_TYPE.DATETIME_STAMP),
	TZ("TZ", COL_TYPE.LITERAL, COL_TYPE.DATETIME_STAMP),
	
	/* SPARQL built-in functions */

	SPARQL_STR("str", COL_TYPE.LITERAL, null),
	SPARQL_DATATYPE("datatype", COL_TYPE.OBJECT, COL_TYPE.LITERAL),
	SPARQL_LANG("lang" , COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	UUID("UUID", COL_TYPE.OBJECT),
	STRUUID("STRUUID", COL_TYPE.LITERAL),

	/* SPARQL built-in predicates */

	IS_LITERAL("isLiteral", COL_TYPE.BOOLEAN, null),
	IS_IRI("isIRI", COL_TYPE.BOOLEAN, null),
	IS_BLANK("isBlank", COL_TYPE.BOOLEAN, null),
	LANGMATCHES("LangMatches", COL_TYPE.BOOLEAN, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	REGEX("regex", COL_TYPE.BOOLEAN, COL_TYPE.LITERAL, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	
	// ROMAN (23 Dec 2015) THIS COMES ONLY FROM MAPPINGS
	SQL_LIKE("like", COL_TYPE.BOOLEAN, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	
	QUEST_CAST("cast", null, null, null);



	// 0-ary operations
    ExpressionOperation(String name, COL_TYPE exprType) {
		this.name = name;
		this.exprType = exprType;
		this.argTypes = new COL_TYPE[] { };
	}
	// unary operations
    ExpressionOperation(String name, COL_TYPE exprType, COL_TYPE arg1) {
		this.name = name;
		this.exprType = exprType;
		this.argTypes = new COL_TYPE[] { arg1 };
	}
	// binary operations
    ExpressionOperation(String name, COL_TYPE exprType, COL_TYPE arg1, COL_TYPE arg2) {
		this.name = name;
		this.exprType = exprType;
		this.argTypes = new COL_TYPE[] { arg1, arg2 };
	}
	// ternary operations
    ExpressionOperation(String name, COL_TYPE exprType, COL_TYPE arg1, COL_TYPE arg2, COL_TYPE arg3) {
		this.name = name;
		this.exprType = exprType;
		this.argTypes = new COL_TYPE[] { arg1, arg2, arg3 };
	}


	private final String name;
	private final COL_TYPE exprType;
	private final COL_TYPE argTypes[];
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getArity() {
		return argTypes.length;
	}

	@Override
	public COL_TYPE getType(int column) {
		return argTypes[column];
	}

	public COL_TYPE getExpressionType() {
		return exprType;
	}
	
	@Override
	public boolean isClass() {
		return false;
	}

	@Override
	public boolean isObjectProperty() {
		return false;
	}

	@Override
	public boolean isDataProperty() {
		return false;
	}

	@Override
	public boolean isTriplePredicate() {
		return false;
	}
	
	@Override
	public boolean isAnnotationProperty() {
		return false;
	}	
}
