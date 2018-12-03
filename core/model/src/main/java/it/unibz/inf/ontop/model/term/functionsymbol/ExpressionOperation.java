package it.unibz.inf.ontop.model.term.functionsymbol;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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

	MINUS("minus", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, ONTOP_NUMERIC_DT, false), // TODO (ROMAN): check -- never used
	ADD("add", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, ONTOP_NUMERIC_DT, ONTOP_NUMERIC_DT, false),
	SUBTRACT("subtract", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, ONTOP_NUMERIC_DT, ONTOP_NUMERIC_DT, false),
	MULTIPLY("multiply", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, ONTOP_NUMERIC_DT, ONTOP_NUMERIC_DT, false),
	DIVIDE("divide", TermTypeInferenceRules.NON_INTEGER_NUMERIC_RULE, ONTOP_NUMERIC_DT, ONTOP_NUMERIC_DT, false),
	ABS("abs", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, ONTOP_NUMERIC_DT, false),
	ROUND("round", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, ONTOP_NUMERIC_DT, false),
	CEIL("ceil", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, ONTOP_NUMERIC_DT, false),
	FLOOR("floor", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, ONTOP_NUMERIC_DT, false),
	RAND("RAND", TermTypeInferenceRules.PREDEFINED_DOUBLE_RULE, false),


	/* SPARQL String functions */

	STRLEN("STRLEN", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, XSD_STRING_DT, false),
	LCASE("LCASE", TermTypeInferenceRules.FIRST_ARG_RULE, XSD_STRING_DT, false),
	SUBSTR2("SUBSTR", TermTypeInferenceRules.FIRST_ARG_RULE, XSD_STRING_DT, XSD_INTEGER_DT, false),
	SUBSTR3("SUBSTR", TermTypeInferenceRules.FIRST_ARG_RULE, XSD_STRING_DT, XSD_INTEGER_DT, XSD_INTEGER_DT, false),
	STRBEFORE("STRBEFORE", TermTypeInferenceRules.FIRST_ARG_RULE, COMPATIBLE_STRING_VALIDATOR, false),
	STRAFTER("STRAFTER", TermTypeInferenceRules.FIRST_ARG_RULE, COMPATIBLE_STRING_VALIDATOR, false),
	REPLACE("REPLACE", TermTypeInferenceRules.STRING_LANG_RULE, XSD_STRING_DT, XSD_STRING_DT, RDFS_LITERAL_DT, RDFS_LITERAL_DT, false),
	// TODO: enforce XSD_STRING
	CONCAT("CONCAT", TermTypeInferenceRules.STRING_LANG_RULE, XSD_STRING_DT, RDFS_LITERAL_DT, false),
	ENCODE_FOR_URI("ENCODE_FOR_URI", TermTypeInferenceRules.PREDEFINED_STRING_RULE, XSD_STRING_DT, true),

	/* Hash functions */

	MD5("MD5", TermTypeInferenceRules.PREDEFINED_STRING_RULE, XSD_STRING_DT, false),
	SHA1("SHA1", TermTypeInferenceRules.PREDEFINED_STRING_RULE, XSD_STRING_DT, false),
	SHA512("SHA521", TermTypeInferenceRules.PREDEFINED_STRING_RULE, XSD_STRING_DT, false),
	SHA256("SHA256", TermTypeInferenceRules.PREDEFINED_STRING_RULE, XSD_STRING_DT, false),

	/* SPARQL Functions on Dates and Times */

	NOW("NOW", TermTypeInferenceRules.PREDEFINED_DATETIME_RULE, true),
	YEAR("YEAR", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, XSD_DATETIME_DT, false),
	DAY("DAY", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, XSD_DATETIME_DT, false),
	MONTH("MONTH", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, XSD_DATETIME_DT, false),
	HOURS("HOURS", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE,  XSD_DATETIME_DT, false),
	MINUTES("MINUTES", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, XSD_DATETIME_DT, false),
	SECONDS("SECONDS", TermTypeInferenceRules.PREDEFINED_DECIMAL_RULE, XSD_DATETIME_DT, false),
	TZ("TZ", TermTypeInferenceRules.PREDEFINED_STRING_RULE, XSD_DATETIME_DT, false),

	/* SPARQL built-in functions */

	// NB: str() not defined for blank nodes!!!!
	SPARQL_STR("str", TermTypeInferenceRules.PREDEFINED_STRING_RULE, RDF_TERM_TYPE, false),
	SPARQL_DATATYPE("datatype", TermTypeInferenceRules.PREDEFINED_IRI_RULE, RDFS_LITERAL_DT, false),
	SPARQL_LANG("lang" , TermTypeInferenceRules.PREDEFINED_STRING_RULE, RDFS_LITERAL_DT, false),
	UUID("UUID", TermTypeInferenceRules.PREDEFINED_IRI_RULE, true),
	STRUUID("STRUUID", TermTypeInferenceRules.PREDEFINED_STRING_RULE, true),

	QUEST_CAST("cast", TermTypeInferenceRules.SECOND_ARG_RULE, RDF_TERM_TYPE, RDF_TERM_TYPE, false), // TODO: refactor

	/*
	* Set functions (for aggregation)
	* TODO: consider a non-atomic datatype
	*/

	AVG("AVG", TermTypeInferenceRules.NON_INTEGER_NUMERIC_RULE, RDF_TERM_TYPE, false),
	SUM("SUM", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, RDF_TERM_TYPE, false),
	MAX("MAX", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, RDF_TERM_TYPE, false),
	MIN("MIN", TermTypeInferenceRules.STANDARD_NUMERIC_RULE, RDF_TERM_TYPE, false),
	COUNT("COUNT", TermTypeInferenceRules.PREDEFINED_INTEGER_RULE, RDF_TERM_TYPE, false),

	/*
 	 * Conditional
 	 */
	IF_ELSE_NULL("IF_ELSE_NULL", TermTypeInferenceRules.SECOND_ARG_RULE, XSD_BOOLEAN_DT, RDF_TERM_TYPE, false);


	// 0-ary operations
    ExpressionOperation(@Nonnull String name, @Nonnull TermTypeInferenceRule termTypeInferenceRule, boolean isInjective) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argumentValidator = new SimpleArgumentValidator(ImmutableList.of());
		this.isInjective = isInjective;
	}

	// unary operations
    ExpressionOperation(@Nonnull String name, @Nonnull TermTypeInferenceRule termTypeInferenceRule,
						@Nonnull TermType arg1, boolean isInjective) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argumentValidator = new SimpleArgumentValidator(ImmutableList.of(arg1));
		this.isInjective = isInjective;
	}
	// binary operations
    ExpressionOperation(@Nonnull String name, @Nonnull TermTypeInferenceRule termTypeInferenceRule,
						@Nonnull TermType arg1, @Nonnull TermType arg2, boolean isInjective) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argumentValidator = new SimpleArgumentValidator(ImmutableList.of(arg1, arg2));
		this.isInjective = isInjective;
	}
	// ternary operations
    ExpressionOperation(@Nonnull String name, @Nonnull TermTypeInferenceRule termTypeInferenceRule,
						@Nonnull TermType arg1, @Nonnull TermType arg2, @Nonnull TermType arg3, boolean isInjective) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argumentValidator = new SimpleArgumentValidator(ImmutableList.of(arg1, arg2, arg3));
		this.isInjective = isInjective;
	}
	// Quad operations
	ExpressionOperation(@Nonnull String name, @Nonnull TermTypeInferenceRule termTypeInferenceRule, @Nonnull TermType arg1,
						@Nonnull TermType arg2, @Nonnull TermType arg3, @Nonnull TermType arg4, boolean isInjective) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argumentValidator = new SimpleArgumentValidator(ImmutableList.of(arg1, arg2, arg3, arg4));
		this.isInjective = isInjective;
	}

	ExpressionOperation(@Nonnull String name, @Nonnull TermTypeInferenceRule termTypeInferenceRule,
						@Nonnull ArgumentValidator argumentValidator, boolean isInjective) {
		this.name = name;
		this.termTypeInferenceRule = termTypeInferenceRule;
		this.argumentValidator = argumentValidator;
		this.isInjective = isInjective;
	}

	private final String name;
	private final TermTypeInferenceRule termTypeInferenceRule;
	private final ArgumentValidator argumentValidator;
	private final boolean isInjective;



	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getArity() {
		return argumentValidator.getExpectedBaseArgumentTypes().size();
	}

	@Override
	public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments,
							   ImmutableSet<Variable> nonNullVariables) {
		// TODO: implement seriously later on
		return false;
	}

	@Override
	public TermType getExpectedBaseType(int index) {
		return argumentValidator.getExpectedBaseType(index);
	}

	/**
	 * TODO: let some of them be post-processed
	 */
	@Override
	public boolean canBePostProcessed() {
		return false;
	}

	@Override
	public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {

		ImmutableList<Optional<TermTypeInference>> argumentTypes = terms.stream()
				.map(ImmutableTerm::inferType)
				.collect(ImmutableCollectors.toList());

		return inferTypeFromArgumentTypes(argumentTypes);
	}

	@Override
	public Optional<TermTypeInference> inferAndValidateType(ImmutableList<? extends ImmutableTerm> terms)
			throws FatalTypingException {

		ImmutableList.Builder<Optional<TermTypeInference>> argumentTypeBuilder = ImmutableList.builder();

		for (ImmutableTerm term : terms) {
			argumentTypeBuilder.add(term.inferAndValidateType());
		}

		return inferTypeFromArgumentTypesAndCheckForFatalError(argumentTypeBuilder.build());
	}

	/**
	 * TODO: implement it seriously after getting rid of this enum
	 */
	@Override
	public ImmutableTerm simplify(ImmutableList<? extends ImmutableTerm> terms, boolean isInConstructionNodeInOptimizationPhase, TermFactory termFactory) {
		return termFactory.getImmutableFunctionalTerm(this, terms);
	}

	@Override
	public Optional<TermTypeInference> inferTypeFromArgumentTypes(ImmutableList<Optional<TermTypeInference>> argumentTypes) {
		try {
			return termTypeInferenceRule.inferTypeFromArgumentTypes(argumentTypes);
		} catch (FatalTypingException e) {
			// No type could be inferred
			return Optional.empty();
		}
	}

	@Override
	public Optional<TermTypeInference> inferTypeFromArgumentTypesAndCheckForFatalError(
			ImmutableList<Optional<TermTypeInference>> argumentTypes) throws FatalTypingException {
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
