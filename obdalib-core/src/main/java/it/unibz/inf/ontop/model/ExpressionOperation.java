package it.unibz.inf.ontop.model;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.TermTypeInferenceRule;

import java.util.Optional;

import static it.unibz.inf.ontop.model.type.impl.TermTypeInferenceRules.*;

public enum ExpressionOperation implements OperationPredicate {

	/* Numeric operations */

	MINUS("minus", STANDARD_NUMERIC_RULE, null), // TODO (ROMAN): check -- never used
	ADD("add", STANDARD_NUMERIC_RULE, null, null),
	SUBTRACT("subtract", STANDARD_NUMERIC_RULE, null, null),
	MULTIPLY("multiply", STANDARD_NUMERIC_RULE, null, null),
	DIVIDE("divide", NON_INTEGER_NUMERIC_RULE, null, null),
	ABS("abs", STANDARD_NUMERIC_RULE, null),
	ROUND("round", STANDARD_NUMERIC_RULE, null),
	CEIL("ceil", STANDARD_NUMERIC_RULE, null),
	FLOOR("floor", STANDARD_NUMERIC_RULE, null),
	RAND("RAND", PREDEFINED_DOUBLE_RULE),
	
	/* Boolean operations */

	AND("AND", PREDEFINED_BOOLEAN_RULE, null, null),
	OR("OR", PREDEFINED_BOOLEAN_RULE, null, null),
	NOT("NOT", PREDEFINED_BOOLEAN_RULE, COL_TYPE.BOOLEAN),
	
	EQ("EQ", PREDEFINED_BOOLEAN_RULE, null, null),
	NEQ("NEQ", PREDEFINED_BOOLEAN_RULE, null, null),
	GTE("GTE", PREDEFINED_BOOLEAN_RULE, null, null),
	GT("GT", PREDEFINED_BOOLEAN_RULE, null, null),
	LTE("LTE", PREDEFINED_BOOLEAN_RULE, null, null),
	LT("LT", PREDEFINED_BOOLEAN_RULE, null, null),

	IS_NULL("IS_NULL", PREDEFINED_BOOLEAN_RULE, null),
	IS_NOT_NULL("IS_NOT_NULL", PREDEFINED_BOOLEAN_RULE, null),
	IS_TRUE("IS_TRUE", PREDEFINED_BOOLEAN_RULE, null),

	STR_STARTS("STRSTARTS", PREDEFINED_BOOLEAN_RULE, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	STR_ENDS("STRENDS", PREDEFINED_BOOLEAN_RULE, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	CONTAINS("CONTAINS", PREDEFINED_BOOLEAN_RULE, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	
	/* SPARQL String functions */

	STRLEN("STRLEN", PREDEFINED_INTEGER_RULE, COL_TYPE.LITERAL),
	UCASE("UCASE", STRING_LANG_RULE, COL_TYPE.LITERAL),
	LCASE("LCASE", STRING_LANG_RULE, COL_TYPE.LITERAL),
	SUBSTR("SUBSTR", FIRST_STRING_LANG_ARG_RULE, COL_TYPE.LITERAL, COL_TYPE.INTEGER, COL_TYPE.INTEGER),
	STRBEFORE("STRBEFORE", FIRST_STRING_LANG_ARG_RULE, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	STRAFTER("STRAFTER", FIRST_STRING_LANG_ARG_RULE, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	REPLACE("REPLACE", PREDEFINED_LITERAL_RULE, COL_TYPE.LITERAL, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	CONCAT("CONCAT", STRING_LANG_RULE, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	ENCODE_FOR_URI("ENCODE_FOR_URI", PREDEFINED_LITERAL_RULE, COL_TYPE.LITERAL),

	/* Hash functions */
	
	MD5("MD5", PREDEFINED_LITERAL_RULE, COL_TYPE.LITERAL),
	SHA1("SHA1", PREDEFINED_LITERAL_RULE, COL_TYPE.LITERAL),
	SHA512("SHA521", PREDEFINED_LITERAL_RULE, COL_TYPE.LITERAL),
	SHA256("SHA256", PREDEFINED_LITERAL_RULE, COL_TYPE.LITERAL),

	/* SPARQL Functions on Dates and Times */

	NOW("NOW", PREDEFINED_DATETIME_RULE),
	YEAR("YEAR", PREDEFINED_INTEGER_RULE, COL_TYPE.DATETIME),
	DAY("DAY", PREDEFINED_INTEGER_RULE, COL_TYPE.DATETIME),
	MONTH("MONTH", PREDEFINED_INTEGER_RULE, COL_TYPE.DATETIME),
	HOURS("HOURS", PREDEFINED_INTEGER_RULE,  COL_TYPE.DATETIME),
	MINUTES("MINUTES", PREDEFINED_INTEGER_RULE, COL_TYPE.DATETIME),
	SECONDS("SECONDS", PREDEFINED_DECIMAL_RULE, COL_TYPE.DATETIME),
	TZ("TZ", PREDEFINED_LITERAL_RULE, COL_TYPE.DATETIME),
	
	/* SPARQL built-in functions */

	SPARQL_STR("str", PREDEFINED_LITERAL_RULE, null),
	SPARQL_DATATYPE("datatype", PREDEFINED_OBJECT_RULE, COL_TYPE.LITERAL),
	SPARQL_LANG("lang" , PREDEFINED_LITERAL_RULE, COL_TYPE.LITERAL),
	UUID("UUID", PREDEFINED_OBJECT_RULE),
	STRUUID("STRUUID", PREDEFINED_LITERAL_RULE),

	/* SPARQL built-in predicates */

	IS_LITERAL("isLiteral", PREDEFINED_BOOLEAN_RULE, null),
	IS_IRI("isIRI", PREDEFINED_BOOLEAN_RULE, null),
	IS_BLANK("isBlank", PREDEFINED_BOOLEAN_RULE, null),
	LANGMATCHES("LangMatches", PREDEFINED_BOOLEAN_RULE, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	REGEX("regex", PREDEFINED_BOOLEAN_RULE, COL_TYPE.LITERAL, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	
	// ROMAN (23 Dec 2015) THIS COMES ONLY FROM MAPPINGS
	SQL_LIKE("like", PREDEFINED_BOOLEAN_RULE, COL_TYPE.LITERAL, COL_TYPE.LITERAL),

	QUEST_CAST("cast", SECOND_ARG_RULE, null, null), // TODO: check

	/*
	* Set functions (for aggregation)
	*/

	AVG("AVG", NON_INTEGER_NUMERIC_RULE, null),
	SUM("SUM", STANDARD_NUMERIC_RULE, null),
	MAX("MAX", STANDARD_NUMERIC_RULE, null),
	MIN("MIN", STANDARD_NUMERIC_RULE, null),
	COUNT("COUNT", PREDEFINED_INTEGER_RULE, null);


	// 0-ary operations
    ExpressionOperation(String name, TermTypeInferenceRule termTypeInferenceRule) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argTypes = ImmutableList.of();
	}
	// unary operations
    ExpressionOperation(String name, TermTypeInferenceRule termTypeInferenceRule, COL_TYPE arg1) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argTypes = ImmutableList.of(Optional.ofNullable(arg1));
	}
	// binary operations
    ExpressionOperation(String name, TermTypeInferenceRule termTypeInferenceRule, COL_TYPE arg1, COL_TYPE arg2) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argTypes = ImmutableList.of(Optional.ofNullable(arg1), Optional.ofNullable(arg2));
	}
	// ternary operations
    ExpressionOperation(String name, TermTypeInferenceRule termTypeInferenceRule, COL_TYPE arg1, COL_TYPE arg2, COL_TYPE arg3) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argTypes = ImmutableList.of(Optional.ofNullable(arg1), Optional.ofNullable(arg2), Optional.ofNullable(arg3));
	}


	private final String name;
	private final TermTypeInferenceRule termTypeInferenceRule;
	// Immutable
	private final ImmutableList<Optional<COL_TYPE>> argTypes;
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getArity() {
		return argTypes.size();
	}

	@Override
	public COL_TYPE getType(int column) {
		return argTypes.get(column).orElse(null);
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

	public TermTypeInferenceRule getTermTypeInferenceRule() {
		return termTypeInferenceRule;
	}

	@Override
	public ImmutableList<Optional<COL_TYPE>> getArgumentTypes() {
		return argTypes;
	}
}
