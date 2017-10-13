package it.unibz.inf.ontop.model.term.functionsymbol;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.COL_TYPE;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInferenceRule;
import it.unibz.inf.ontop.model.type.impl.TermTypeInferenceRules;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.Optional;

import static it.unibz.inf.ontop.model.type.impl.TermTypeInferenceRules.*;

public enum ExpressionOperation implements OperationPredicate {

	/* Numeric operations */

	MINUS("minus", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, ONTOP_NUMERIC_DT), // TODO (ROMAN): check -- never used
	ADD("add", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, ONTOP_NUMERIC_DT, ONTOP_NUMERIC_DT),
	SUBTRACT("subtract", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, ONTOP_NUMERIC_DT, ONTOP_NUMERIC_DT),
	MULTIPLY("multiply", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, ONTOP_NUMERIC_DT, ONTOP_NUMERIC_DT),
	DIVIDE("divide", TermTypeInferenceRules.NON_INTEGER_NUMERIC_RULE, ONTOP_NUMERIC_DT, ONTOP_NUMERIC_DT),
	ABS("abs", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, ONTOP_NUMERIC_DT),
	ROUND("round", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, ONTOP_NUMERIC_DT),
	CEIL("ceil", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, ONTOP_NUMERIC_DT),
	FLOOR("floor", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, ONTOP_NUMERIC_DT),
	RAND("RAND", TermTypeInferenceRules.PREDEFINED_DOUBLE_RULE),
	
	/* Boolean operations */

	AND("AND", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, XSD_BOOLEAN_DT, XSD_BOOLEAN_DT),
	OR("OR", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, XSD_BOOLEAN_DT, XSD_BOOLEAN_DT),
	NOT("NOT", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, XSD_BOOLEAN_DT),
	
	EQ("EQ", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDF_TERM_TYPE, RDF_TERM_TYPE),
	NEQ("NEQ", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDF_TERM_TYPE, RDF_TERM_TYPE),
	/*
	 * BC: is it defined for IRIs?
	 */
	GTE("GTE", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT),
	GT("GT", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT),
	LTE("LTE", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT),
	LT("LT", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT),

	IS_NULL("IS_NULL", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDF_TERM_TYPE),
	IS_NOT_NULL("IS_NOT_NULL", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDF_TERM_TYPE),
	IS_TRUE("IS_TRUE", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDF_TERM_TYPE),

	STR_STARTS("STRSTARTS", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT),
	STR_ENDS("STRENDS", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT),
	CONTAINS("CONTAINS", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT),
	
	/* SPARQL String functions */

	STRLEN("STRLEN", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, RDFS_LITERAL_DT),
	UCASE("UCASE", TermTypeInferenceRules.STRING_LANG_RULE, RDFS_LITERAL_DT),
	LCASE("LCASE", TermTypeInferenceRules.STRING_LANG_RULE, RDFS_LITERAL_DT),
	SUBSTR2("SUBSTR", TermTypeInferenceRules.FIRST_STRING_LANG_ARG_RULE, RDFS_LITERAL_DT, XSD_INTEGER_DT),
	SUBSTR3("SUBSTR", TermTypeInferenceRules.FIRST_STRING_LANG_ARG_RULE, RDFS_LITERAL_DT, XSD_INTEGER_DT, XSD_INTEGER_DT),
	STRBEFORE("STRBEFORE", TermTypeInferenceRules.FIRST_STRING_LANG_ARG_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT),
	STRAFTER("STRAFTER", TermTypeInferenceRules.FIRST_STRING_LANG_ARG_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT),
	REPLACE("REPLACE", TermTypeInferenceRules.STRING_LANG_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT, RDFS_LITERAL_DT, RDFS_LITERAL_DT),
	CONCAT("CONCAT", TermTypeInferenceRules.STRING_LANG_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT),
	ENCODE_FOR_URI("ENCODE_FOR_URI", TermTypeInferenceRules.PREDEFINED_LITERAL_RULE, RDFS_LITERAL_DT),

	/* Hash functions */
	
	MD5("MD5", TermTypeInferenceRules.PREDEFINED_LITERAL_RULE, RDFS_LITERAL_DT),
	SHA1("SHA1", TermTypeInferenceRules.PREDEFINED_LITERAL_RULE, RDFS_LITERAL_DT),
	SHA512("SHA521", TermTypeInferenceRules.PREDEFINED_LITERAL_RULE, RDFS_LITERAL_DT),
	SHA256("SHA256", TermTypeInferenceRules.PREDEFINED_LITERAL_RULE, RDFS_LITERAL_DT),

	/* SPARQL Functions on Dates and Times */

	NOW("NOW", TermTypeInferenceRules.PREDEFINED_DATETIME_RULE),
	YEAR("YEAR", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, XSD_DATETIME_DT),
	DAY("DAY", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, XSD_DATETIME_DT),
	MONTH("MONTH", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, XSD_DATETIME_DT),
	HOURS("HOURS", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE,  XSD_DATETIME_DT),
	MINUTES("MINUTES", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, XSD_DATETIME_DT),
	SECONDS("SECONDS", TermTypeInferenceRules.PREDEFINED_DECIMAL_RULE, XSD_DATETIME_DT),
	TZ("TZ", TermTypeInferenceRules.PREDEFINED_LITERAL_RULE, XSD_DATETIME_DT),
	
	/* SPARQL built-in functions */

	// NB: str() not defined for blank nodes!!!!
	SPARQL_STR("str", TermTypeInferenceRules.PREDEFINED_LITERAL_RULE, RDF_TERM_TYPE),
	SPARQL_DATATYPE("datatype", TermTypeInferenceRules.PREDEFINED_OBJECT_RULE, RDFS_LITERAL_DT),
	SPARQL_LANG("lang" , TermTypeInferenceRules.PREDEFINED_LITERAL_RULE, RDFS_LITERAL_DT),
	UUID("UUID", TermTypeInferenceRules.PREDEFINED_OBJECT_RULE),
	STRUUID("STRUUID", TermTypeInferenceRules.PREDEFINED_LITERAL_RULE),

	/* SPARQL built-in predicates */

	IS_NUMERIC("isNumeric", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDF_TERM_TYPE),
	IS_LITERAL("isLiteral", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDF_TERM_TYPE),
	IS_IRI("isIRI", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDF_TERM_TYPE),
	IS_BLANK("isBlank", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDF_TERM_TYPE),
	LANGMATCHES("LangMatches", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT),
	REGEX("regex", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT, RDFS_LITERAL_DT),
	
	// ROMAN (23 Dec 2015) THIS COMES ONLY FROM MAPPINGS
	SQL_LIKE("like", TermTypeInferenceRules.PREDEFINED_BOOLEAN_RULE, RDFS_LITERAL_DT, RDFS_LITERAL_DT),

	QUEST_CAST("cast", TermTypeInferenceRules.SECOND_ARG_RULE, RDF_TERM_TYPE, RDF_TERM_TYPE), // TODO: refactor

	/*
	* Set functions (for aggregation)
	* TODO: consider a non-atomic datatype
	*/

	AVG("AVG", TermTypeInferenceRules.NON_INTEGER_NUMERIC_RULE, RDF_TERM_TYPE),
	SUM("SUM", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, RDF_TERM_TYPE),
	MAX("MAX", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, RDF_TERM_TYPE),
	MIN("MIN", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, RDF_TERM_TYPE),
	COUNT("COUNT", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, RDF_TERM_TYPE);


	// 0-ary operations
    ExpressionOperation(@Nonnull String name, @Nonnull TermTypeInferenceRule termTypeInferenceRule) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argTypes = ImmutableList.of();
		this.argColTypes = ImmutableList.of();
	}

	// unary operations
    ExpressionOperation(@Nonnull String name, @Nonnull TermTypeInferenceRule termTypeInferenceRule, @Nonnull TermType arg1) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argTypes = ImmutableList.of(arg1);
		this.argColTypes = convertArgTypes(argTypes);
	}
	// binary operations
    ExpressionOperation(@Nonnull String name, @Nonnull TermTypeInferenceRule termTypeInferenceRule, @Nonnull TermType arg1, @Nonnull TermType arg2) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argTypes = ImmutableList.of(arg1, arg2);
		this.argColTypes = convertArgTypes(argTypes);
	}
	// ternary operations
    ExpressionOperation(@Nonnull String name, @Nonnull TermTypeInferenceRule termTypeInferenceRule, @Nonnull TermType arg1, @Nonnull TermType arg2,
						@Nonnull TermType arg3) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argTypes = ImmutableList.of(arg1, arg2, arg3);
		this.argColTypes = convertArgTypes(argTypes);
	}
	// Quad operations
	ExpressionOperation(@Nonnull String name, @Nonnull TermTypeInferenceRule termTypeInferenceRule, @Nonnull TermType arg1,
						@Nonnull TermType arg2, @Nonnull TermType arg3, @Nonnull TermType arg4) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argTypes = ImmutableList.of(arg1, arg2, arg3, arg4);
		this.argColTypes = convertArgTypes(argTypes);
	}

	private final String name;
	private final TermTypeInferenceRule termTypeInferenceRule;
	// Immutable
	private final ImmutableList<Optional<COL_TYPE>> argColTypes;
	private final ImmutableList<TermType> argTypes;
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getArity() {
		return argColTypes.size();
	}

	@Override
	public COL_TYPE getColType(int column) {
		return argColTypes.get(column).orElse(null);
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
	public TermType getExpectedBaseType(int index) {
		return argTypes.get(index);
	}

	@Override
	public ImmutableList<TermType> getExpectedBaseArgumentTypes() {
		return argTypes;
	}

	private static ImmutableList<Optional<COL_TYPE>> convertArgTypes(ImmutableList<TermType> argColTypes) {
		return argColTypes.stream()
				.map(TermType::getOptionalColType)
				.collect(ImmutableCollectors.toList());
	}
}
