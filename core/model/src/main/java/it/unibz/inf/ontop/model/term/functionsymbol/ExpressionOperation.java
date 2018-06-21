package it.unibz.inf.ontop.model.term.functionsymbol;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonFunctionalTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.impl.FunctionalTermNullabilityImpl;
import it.unibz.inf.ontop.model.type.ArgumentValidator;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.type.TermTypeInferenceRule;
import it.unibz.inf.ontop.model.type.impl.SimpleArgumentValidator;
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

	
	/* SPARQL String functions */

	STRLEN("STRLEN", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, XSD_STRING_DT),
	UCASE("UCASE", TermTypeInferenceRules.FIRST_ARG_RULE, XSD_STRING_DT),
	LCASE("LCASE", TermTypeInferenceRules.FIRST_ARG_RULE, XSD_STRING_DT),
	SUBSTR2("SUBSTR", TermTypeInferenceRules.FIRST_ARG_RULE, XSD_STRING_DT, XSD_INTEGER_DT),
	SUBSTR3("SUBSTR", TermTypeInferenceRules.FIRST_ARG_RULE, XSD_STRING_DT, XSD_INTEGER_DT, XSD_INTEGER_DT),
	STRBEFORE("STRBEFORE", TermTypeInferenceRules.FIRST_ARG_RULE, COMPATIBLE_STRING_VALIDATOR),
	STRAFTER("STRAFTER", TermTypeInferenceRules.FIRST_ARG_RULE, COMPATIBLE_STRING_VALIDATOR),
	REPLACE("REPLACE", TermTypeInferenceRules.STRING_LANG_RULE, XSD_STRING_DT, XSD_STRING_DT, RDFS_LITERAL_DT, RDFS_LITERAL_DT),
	// TODO: enforce XSD_STRING
	CONCAT("CONCAT", TermTypeInferenceRules.STRING_LANG_RULE, XSD_STRING_DT, RDFS_LITERAL_DT),
	ENCODE_FOR_URI("ENCODE_FOR_URI", TermTypeInferenceRules.PREDEFINED_STRING_RULE, XSD_STRING_DT),

	/* Hash functions */
	
	MD5("MD5", TermTypeInferenceRules.PREDEFINED_STRING_RULE, XSD_STRING_DT),
	SHA1("SHA1", TermTypeInferenceRules.PREDEFINED_STRING_RULE, XSD_STRING_DT),
	SHA512("SHA521", TermTypeInferenceRules.PREDEFINED_STRING_RULE, XSD_STRING_DT),
	SHA256("SHA256", TermTypeInferenceRules.PREDEFINED_STRING_RULE, XSD_STRING_DT),

	/* SPARQL Functions on Dates and Times */

	NOW("NOW", TermTypeInferenceRules.PREDEFINED_DATETIME_RULE),
	YEAR("YEAR", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, XSD_DATETIME_DT),
	DAY("DAY", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, XSD_DATETIME_DT),
	MONTH("MONTH", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, XSD_DATETIME_DT),
	HOURS("HOURS", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE,  XSD_DATETIME_DT),
	MINUTES("MINUTES", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, XSD_DATETIME_DT),
	SECONDS("SECONDS", TermTypeInferenceRules.PREDEFINED_DECIMAL_RULE, XSD_DATETIME_DT),
	TZ("TZ", TermTypeInferenceRules.PREDEFINED_STRING_RULE, XSD_DATETIME_DT),
	
	/* SPARQL built-in functions */

	// NB: str() not defined for blank nodes!!!!
	SPARQL_STR("str", TermTypeInferenceRules.PREDEFINED_STRING_RULE, RDF_TERM_TYPE),
	SPARQL_DATATYPE("datatype", TermTypeInferenceRules.PREDEFINED_IRI_RULE, RDFS_LITERAL_DT),
	SPARQL_LANG("lang" , TermTypeInferenceRules.PREDEFINED_STRING_RULE, RDFS_LITERAL_DT),
	UUID("UUID", TermTypeInferenceRules.PREDEFINED_IRI_RULE),
	STRUUID("STRUUID", TermTypeInferenceRules.PREDEFINED_STRING_RULE),

	QUEST_CAST("cast", TermTypeInferenceRules.SECOND_ARG_RULE, RDF_TERM_TYPE, RDF_TERM_TYPE), // TODO: refactor

	/*
	* Set functions (for aggregation)
	* TODO: consider a non-atomic datatype
	*/

	AVG("AVG", TermTypeInferenceRules.NON_INTEGER_NUMERIC_RULE, RDF_TERM_TYPE),
	SUM("SUM", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, RDF_TERM_TYPE),
	MAX("MAX", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, RDF_TERM_TYPE),
	MIN("MIN", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, RDF_TERM_TYPE),
	COUNT("COUNT", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, RDF_TERM_TYPE),

	/*
 	 * Conditional
 	 */
	IF_ELSE_NULL("IF_ELSE_NULL", TermTypeInferenceRules.SECOND_ARG_RULE, XSD_BOOLEAN_DT, RDF_TERM_TYPE);


	// 0-ary operations
    ExpressionOperation(@Nonnull String name, @Nonnull TermTypeInferenceRule termTypeInferenceRule) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argumentValidator = new SimpleArgumentValidator(ImmutableList.of());
	}

	// unary operations
    ExpressionOperation(@Nonnull String name, @Nonnull TermTypeInferenceRule termTypeInferenceRule,
						@Nonnull TermType arg1) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argumentValidator = new SimpleArgumentValidator(ImmutableList.of(arg1));
	}
	// binary operations
    ExpressionOperation(@Nonnull String name, @Nonnull TermTypeInferenceRule termTypeInferenceRule,
						@Nonnull TermType arg1, @Nonnull TermType arg2) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argumentValidator = new SimpleArgumentValidator(ImmutableList.of(arg1, arg2));
	}
	// ternary operations
    ExpressionOperation(@Nonnull String name, @Nonnull TermTypeInferenceRule termTypeInferenceRule,
						@Nonnull TermType arg1, @Nonnull TermType arg2, @Nonnull TermType arg3) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argumentValidator = new SimpleArgumentValidator(ImmutableList.of(arg1, arg2, arg3));
	}
	// Quad operations
	ExpressionOperation(@Nonnull String name, @Nonnull TermTypeInferenceRule termTypeInferenceRule, @Nonnull TermType arg1,
						@Nonnull TermType arg2, @Nonnull TermType arg3, @Nonnull TermType arg4) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argumentValidator = new SimpleArgumentValidator(ImmutableList.of(arg1, arg2, arg3, arg4));
	}

	ExpressionOperation(@Nonnull String name, @Nonnull TermTypeInferenceRule termTypeInferenceRule,
						@Nonnull ArgumentValidator argumentValidator) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argumentValidator = argumentValidator;
	}

	private final String name;
	private final TermTypeInferenceRule termTypeInferenceRule;
	private final ArgumentValidator argumentValidator;



	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getArity() {
		return argumentValidator.getExpectedBaseArgumentTypes().size();
	}

	@Override
	public TermType getExpectedBaseType(int index) {
		return argumentValidator.getExpectedBaseType(index);
	}

	@Override
	public ImmutableList<TermType> getExpectedBaseArgumentTypes() {
		return argumentValidator.getExpectedBaseArgumentTypes();
	}

	@Override
	public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) throws FatalTypingException {

		ImmutableList<Optional<TermTypeInference>> argumentTypes = terms.stream()
				.map(ImmutableTerm::inferType)
				.collect(ImmutableCollectors.toList());

		return inferTypeFromArgumentTypes(argumentTypes);
	}

	/**
	 * TODO: implement it seriously after getting rid of this enum
	 */
	@Override
	public ImmutableTerm evaluate(ImmutableList<? extends ImmutableTerm> terms, boolean isInConstructionNodeInOptimizationPhase, TermFactory termFactory) {
		return termFactory.getImmutableFunctionalTerm(this, terms);
	}

	@Override
	public Optional<TermTypeInference> inferTypeFromArgumentTypes(ImmutableList<Optional<TermTypeInference>> argumentTypes) {
		argumentValidator.validate(argumentTypes);

		return termTypeInferenceRule.inferTypeFromArgumentTypes(argumentTypes);
	}


	/**
	 * TODO: IMPLEMENT IT SERIOUSLY
	 */
	@Override
	public FunctionalTermNullability evaluateNullability(ImmutableList<? extends NonFunctionalTerm> arguments,
														 VariableNullability childNullability) {
		boolean isNullable = arguments.stream()
				.filter(a -> a instanceof Variable)
				.anyMatch(a -> childNullability.isPossiblyNullable((Variable) a));
		return new FunctionalTermNullabilityImpl(isNullable);
	}
}
