package it.unibz.inf.ontop.model.term.functionsymbol;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.COL_TYPE;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInferenceRule;
import it.unibz.inf.ontop.model.type.impl.TermTypeInferenceRules;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;

public enum ExpressionOperation implements OperationPredicate {

	/* Numeric operations */

	MINUS("minus", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, null), // TODO (ROMAN): check -- never used
	ADD("add", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, null, null),
	SUBTRACT("subtract", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, null, null),
	MULTIPLY("multiply", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, null, null),
	DIVIDE("divide", TermTypeInferenceRules.NON_INTEGER_NUMERIC_RULE, null, null),
	ABS("abs", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, null),
	ROUND("round", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, null),
	CEIL("ceil", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, null),
	FLOOR("floor", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, null),
	RAND("RAND", TermTypeInferenceRules.PREDEFINED_DOUBLE_RULE),
	
	/* Boolean operations */

	AND("AND", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, null, null),
	OR("OR", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, null, null),
	NOT("NOT", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, COL_TYPE.BOOLEAN),
	
	EQ("EQ", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, null, null),
	NEQ("NEQ", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, null, null),
	GTE("GTE", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, null, null),
	GT("GT", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, null, null),
	LTE("LTE", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, null, null),
	LT("LT", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, null, null),

	IS_NULL("IS_NULL", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, null),
	IS_NOT_NULL("IS_NOT_NULL", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, null),
	IS_TRUE("IS_TRUE", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, null),

	STR_STARTS("STRSTARTS", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	STR_ENDS("STRENDS", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	CONTAINS("CONTAINS", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	
	/* SPARQL String functions */

	STRLEN("STRLEN", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, COL_TYPE.LITERAL),
	UCASE("UCASE", TermTypeInferenceRules.STRING_LANG_RULE, COL_TYPE.LITERAL),
	LCASE("LCASE", TermTypeInferenceRules.STRING_LANG_RULE, COL_TYPE.LITERAL),
	SUBSTR2("SUBSTR", TermTypeInferenceRules.FIRST_STRING_LANG_ARG_RULE, COL_TYPE.LITERAL, COL_TYPE.INTEGER),
	SUBSTR3("SUBSTR", TermTypeInferenceRules.FIRST_STRING_LANG_ARG_RULE, COL_TYPE.LITERAL, COL_TYPE.INTEGER, COL_TYPE.INTEGER),
	STRBEFORE("STRBEFORE", TermTypeInferenceRules.FIRST_STRING_LANG_ARG_RULE, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	STRAFTER("STRAFTER", TermTypeInferenceRules.FIRST_STRING_LANG_ARG_RULE, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	REPLACE("REPLACE", TermTypeInferenceRules.PREDEFINED_LITERAL_RULE, COL_TYPE.LITERAL, COL_TYPE.LITERAL, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	CONCAT("CONCAT", TermTypeInferenceRules.STRING_LANG_RULE, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	ENCODE_FOR_URI("ENCODE_FOR_URI", TermTypeInferenceRules.PREDEFINED_LITERAL_RULE, COL_TYPE.LITERAL),

	/* Hash functions */
	
	MD5("MD5", TermTypeInferenceRules.PREDEFINED_LITERAL_RULE, COL_TYPE.LITERAL),
	SHA1("SHA1", TermTypeInferenceRules.PREDEFINED_LITERAL_RULE, COL_TYPE.LITERAL),
	SHA512("SHA521", TermTypeInferenceRules.PREDEFINED_LITERAL_RULE, COL_TYPE.LITERAL),
	SHA256("SHA256", TermTypeInferenceRules.PREDEFINED_LITERAL_RULE, COL_TYPE.LITERAL),

	/* SPARQL Functions on Dates and Times */

	NOW("NOW", TermTypeInferenceRules.PREDEFINED_DATETIME_RULE),
	YEAR("YEAR", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, COL_TYPE.DATETIME),
	DAY("DAY", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, COL_TYPE.DATETIME),
	MONTH("MONTH", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, COL_TYPE.DATETIME),
	HOURS("HOURS", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE,  COL_TYPE.DATETIME),
	MINUTES("MINUTES", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, COL_TYPE.DATETIME),
	SECONDS("SECONDS", TermTypeInferenceRules.PREDEFINED_DECIMAL_RULE, COL_TYPE.DATETIME),
	TZ("TZ", TermTypeInferenceRules.PREDEFINED_LITERAL_RULE, COL_TYPE.DATETIME),
	
	/* SPARQL built-in functions */

	SPARQL_STR("str", TermTypeInferenceRules.PREDEFINED_LITERAL_RULE, null),
	SPARQL_DATATYPE("datatype", TermTypeInferenceRules.PREDEFINED_OBJECT_RULE, COL_TYPE.LITERAL),
	SPARQL_LANG("lang" , TermTypeInferenceRules.PREDEFINED_LITERAL_RULE, COL_TYPE.LITERAL),
	UUID("UUID", TermTypeInferenceRules.PREDEFINED_OBJECT_RULE),
	STRUUID("STRUUID", TermTypeInferenceRules.PREDEFINED_LITERAL_RULE),

	/* SPARQL built-in predicates */

	IS_LITERAL("isLiteral", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, null),
	IS_IRI("isIRI", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, null),
	IS_BLANK("isBlank", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, null),
	LANGMATCHES("LangMatches", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	REGEX("regex", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, COL_TYPE.LITERAL, COL_TYPE.LITERAL, COL_TYPE.LITERAL),
	
	// ROMAN (23 Dec 2015) THIS COMES ONLY FROM MAPPINGS
	SQL_LIKE("like", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, COL_TYPE.LITERAL, COL_TYPE.LITERAL),

	QUEST_CAST("cast", TermTypeInferenceRules.SECOND_ARG_RULE, null, null), // TODO: check

	/*
	* Set functions (for aggregation)
	*/

	AVG("AVG", TermTypeInferenceRules.NON_INTEGER_NUMERIC_RULE, null),
	SUM("SUM", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, null),
	MAX("MAX", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, null),
	MIN("MIN", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, null),
	COUNT("COUNT", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, null);


	// 0-ary operations
    ExpressionOperation(String name, TermTypeInferenceRule termTypeInferenceRule) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argColTypes = ImmutableList.of();
		this.argTypes = convertArgTypes(argColTypes);
	}

	// unary operations
    ExpressionOperation(String name, TermTypeInferenceRule termTypeInferenceRule, COL_TYPE arg1) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argColTypes = ImmutableList.of(Optional.ofNullable(arg1));
		this.argTypes = convertArgTypes(argColTypes);
	}
	// binary operations
    ExpressionOperation(String name, TermTypeInferenceRule termTypeInferenceRule, COL_TYPE arg1, COL_TYPE arg2) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argColTypes = ImmutableList.of(Optional.ofNullable(arg1), Optional.ofNullable(arg2));
		this.argTypes = convertArgTypes(argColTypes);
	}
	// ternary operations
    ExpressionOperation(String name, TermTypeInferenceRule termTypeInferenceRule, COL_TYPE arg1, COL_TYPE arg2, COL_TYPE arg3) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argColTypes = ImmutableList.of(Optional.ofNullable(arg1), Optional.ofNullable(arg2), Optional.ofNullable(arg3));
		this.argTypes = convertArgTypes(argColTypes);
	}
	// Quad operations
	ExpressionOperation(String name, TermTypeInferenceRule termTypeInferenceRule, COL_TYPE arg1, COL_TYPE arg2,
						COL_TYPE arg3, COL_TYPE arg4) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argColTypes = ImmutableList.of(Optional.ofNullable(arg1), Optional.ofNullable(arg2),
				Optional.ofNullable(arg3), Optional.ofNullable(arg4));
		this.argTypes = convertArgTypes(argColTypes);
	}



	private final String name;
	private final TermTypeInferenceRule termTypeInferenceRule;
	// Immutable
	private final ImmutableList<Optional<COL_TYPE>> argColTypes;
	private final ImmutableList<Optional<TermType>> argTypes;
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getArity() {
		return argColTypes.size();
	}

	@Override
	public COL_TYPE getType(int column) {
		return argColTypes.get(column).orElse(null);
	}

	@Override
	public COL_TYPE[] getTypes() {
		return (COL_TYPE[]) argColTypes.toArray();
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
	public boolean isSameAsProperty() {
		return false;
	}

	@Override
	public boolean isCanonicalIRIProperty() {
		return false;
	}

	public TermTypeInferenceRule getTermTypeInferenceRule() {
		return termTypeInferenceRule;
	}

	@Override
	public ImmutableList<Optional<TermType>> getArgumentTypes() {
		return argTypes;
	}

	private static ImmutableList<Optional<TermType>> convertArgTypes(ImmutableList<Optional<COL_TYPE>> argColTypes) {
		return argColTypes.stream()
				.map(o -> o.map(TYPE_FACTORY::getTermType))
				.collect(ImmutableCollectors.toList());
	}
}
