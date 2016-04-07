package it.unibz.inf.ontop.model;

import it.unibz.inf.ontop.model.type.TermTypeReasoner;

import static it.unibz.inf.ontop.model.type.impl.TermTypeReasoners.*;

public enum ExpressionOperation implements OperationPredicate {

	/* Numeric operations */

	MINUS("minus", STANDARD_NUMERIC_REASONER, null), // TODO (ROMAN): check -- never used
	ADD("add", STANDARD_NUMERIC_REASONER, null, null),
	SUBTRACT("subtract", STANDARD_NUMERIC_REASONER, null, null),
	MULTIPLY("multiply", STANDARD_NUMERIC_REASONER, null, null),
	DIVIDE("divide", NON_INTEGER_NUMERIC_REASONER, null, null),
	ABS("abs", STANDARD_NUMERIC_REASONER, null),
	ROUND("round", STANDARD_NUMERIC_REASONER, null),
	CEIL("ceil", STANDARD_NUMERIC_REASONER, null),
	FLOOR("floor", STANDARD_NUMERIC_REASONER, null),
	RAND("RAND", PREDEFINED_DOUBLE_REASONER),
	
	/* Boolean operations */

	AND("AND", PREDEFINED_BOOLEAN_REASONER, null, null),
	OR("OR", PREDEFINED_BOOLEAN_REASONER, null, null),
	NOT("NOT", PREDEFINED_BOOLEAN_REASONER, COL_TYPE.BOOLEAN),
	
	EQ("EQ", PREDEFINED_BOOLEAN_REASONER, null, null),
	NEQ("NEQ", PREDEFINED_BOOLEAN_REASONER, null, null),
	GTE("GTE", PREDEFINED_BOOLEAN_REASONER, null, null),
	GT("GT", PREDEFINED_BOOLEAN_REASONER, null, null),
	LTE("LTE", PREDEFINED_BOOLEAN_REASONER, null, null),
	LT("LT", PREDEFINED_BOOLEAN_REASONER, null, null),

	IS_NULL("IS_NULL", PREDEFINED_BOOLEAN_REASONER, null),
	IS_NOT_NULL("IS_NOT_NULL", PREDEFINED_BOOLEAN_REASONER, null),
	IS_TRUE("IS_TRUE", PREDEFINED_BOOLEAN_REASONER, null),

	STR_STARTS("STRSTARTS", PREDEFINED_BOOLEAN_REASONER, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	STR_ENDS("STRENDS", PREDEFINED_BOOLEAN_REASONER, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	CONTAINS("CONTAINS", PREDEFINED_BOOLEAN_REASONER, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	
	/* SPARQL String functions */

	STRLEN("STRLEN", PREDEFINED_INTEGER_REASONER, COL_TYPE.LITERAL),
	UCASE("UCASE", STRING_LANG_REASONER, COL_TYPE.LITERAL),
	LCASE("LCASE", STRING_LANG_REASONER, COL_TYPE.LITERAL),
	SUBSTR("SUBSTR", FIRST_ARG_STRING_LANG_REASONER, COL_TYPE.LITERAL, COL_TYPE.INTEGER, COL_TYPE.INTEGER),
	STRBEFORE("STRBEFORE", FIRST_ARG_STRING_LANG_REASONER, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	STRAFTER("STRAFTER", FIRST_ARG_STRING_LANG_REASONER, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	REPLACE("REPLACE", PREDEFINED_LITERAL_REASONER, COL_TYPE.LITERAL, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	CONCAT("CONCAT", STRING_LANG_REASONER, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	ENCODE_FOR_URI("ENCODE_FOR_URI", PREDEFINED_LITERAL_REASONER, COL_TYPE.LITERAL),

	/* Hash functions */
	
	MD5("MD5", PREDEFINED_LITERAL_REASONER, COL_TYPE.LITERAL),
	SHA1("SHA1", PREDEFINED_LITERAL_REASONER, COL_TYPE.LITERAL),
	SHA512("SHA521", PREDEFINED_LITERAL_REASONER, COL_TYPE.LITERAL),
	SHA256("SHA256", PREDEFINED_LITERAL_REASONER, COL_TYPE.LITERAL),

	/* SPARQL Functions on Dates and Times */

	NOW("NOW", PREDEFINED_DATETIME_STAMP_REASONER),
	YEAR("YEAR", PREDEFINED_INTEGER_REASONER, COL_TYPE.DATETIME_STAMP),
	DAY("DAY", PREDEFINED_INTEGER_REASONER, COL_TYPE.DATETIME_STAMP),
	MONTH("MONTH", PREDEFINED_INTEGER_REASONER, COL_TYPE.DATETIME_STAMP),
	HOURS("HOURS", PREDEFINED_INTEGER_REASONER,  COL_TYPE.DATETIME_STAMP),
	MINUTES("MINUTES", PREDEFINED_INTEGER_REASONER, COL_TYPE.DATETIME_STAMP),
	SECONDS("SECONDS", PREDEFINED_DECIMAL_REASONER, COL_TYPE.DATETIME_STAMP),
	TZ("TZ", PREDEFINED_LITERAL_REASONER, COL_TYPE.DATETIME_STAMP),
	
	/* SPARQL built-in functions */

	SPARQL_STR("str", PREDEFINED_LITERAL_REASONER, null),
	SPARQL_DATATYPE("datatype", PREDEFINED_OBJECT_REASONER, COL_TYPE.LITERAL),
	SPARQL_LANG("lang" , PREDEFINED_LITERAL_REASONER, COL_TYPE.LITERAL),
	UUID("UUID", PREDEFINED_OBJECT_REASONER),
	STRUUID("STRUUID", PREDEFINED_LITERAL_REASONER),

	/* SPARQL built-in predicates */

	IS_LITERAL("isLiteral", PREDEFINED_BOOLEAN_REASONER, null),
	IS_IRI("isIRI", PREDEFINED_BOOLEAN_REASONER, null),
	IS_BLANK("isBlank", PREDEFINED_BOOLEAN_REASONER, null),
	LANGMATCHES("LangMatches", PREDEFINED_BOOLEAN_REASONER, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	REGEX("regex", PREDEFINED_BOOLEAN_REASONER, COL_TYPE.LITERAL, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	
	// ROMAN (23 Dec 2015) THIS COMES ONLY FROM MAPPINGS
	SQL_LIKE("like", PREDEFINED_BOOLEAN_REASONER, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	
	QUEST_CAST("cast", SECOND_ARG_REASONER, null, null),

	/*
	* Set functions (for aggregation)
	*/

	AVG("AVG", NON_INTEGER_NUMERIC_REASONER, null),
	SUM("SUM", STANDARD_NUMERIC_REASONER, null),
	MAX("MAX", STANDARD_NUMERIC_REASONER, null),
	MIN("MIN", STANDARD_NUMERIC_REASONER, null),
	COUNT("COUNT", PREDEFINED_INTEGER_REASONER, null);


	// 0-ary operations
    ExpressionOperation(String name, TermTypeReasoner termTypeReasoner) {
		this.name = name;
		this.termTypeReasoner = termTypeReasoner;
		this.argTypes = new COL_TYPE[] { };
	}
	// unary operations
    ExpressionOperation(String name, TermTypeReasoner termTypeReasoner, COL_TYPE arg1) {
		this.name = name;
		this.termTypeReasoner = termTypeReasoner;
		this.argTypes = new COL_TYPE[] { arg1 };
	}
	// binary operations
    ExpressionOperation(String name, TermTypeReasoner termTypeReasoner, COL_TYPE arg1, COL_TYPE arg2) {
		this.name = name;
		this.termTypeReasoner = termTypeReasoner;
		this.argTypes = new COL_TYPE[] { arg1, arg2 };
	}
	// ternary operations
    ExpressionOperation(String name, TermTypeReasoner termTypeReasoner, COL_TYPE arg1, COL_TYPE arg2, COL_TYPE arg3) {
		this.name = name;
		this.termTypeReasoner = termTypeReasoner;
		this.argTypes = new COL_TYPE[] { arg1, arg2, arg3 };
	}


	private final String name;
	private final TermTypeReasoner termTypeReasoner;
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

	@Override
	public TermTypeReasoner getTermTypeReasoner() {
		return termTypeReasoner;
	}

	@Override
	public COL_TYPE[] getArgumentTypes() {
		return argTypes;
	}
}
