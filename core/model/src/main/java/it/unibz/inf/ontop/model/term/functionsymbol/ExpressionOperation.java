package it.unibz.inf.ontop.model.term.functionsymbol;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
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

public enum ExpressionOperation implements FunctionSymbol {

	/* SPARQL Functions on Dates and Times */

	NOW("NOW", TermTypeInferenceRules.PREDEFINED_DATETIME_RULE, true),
	TZ("TZ", TermTypeInferenceRules.PREDEFINED_STRING_RULE, XSD_DATETIME_DT, false);


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
                               VariableNullability variableNullability) {
		// TODO: implement seriously later on
		return false;
	}

	@Override
	public TermType getExpectedBaseType(int index) {
		return argumentValidator.getExpectedBaseType(index);
	}

	/**
	 * TODO: implement it?
	 */
	@Override
	public IncrementalEvaluation evaluateStrictEq(ImmutableList<? extends ImmutableTerm> terms, ImmutableTerm otherTerm,
												  TermFactory termFactory, VariableNullability variableNullability) {
		return IncrementalEvaluation.declareSameExpression();
	}

	@Override
	public IncrementalEvaluation evaluateIsNotNull(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory,
												   VariableNullability variableNullability) {
		return IncrementalEvaluation.declareSameExpression();
	}

	/**
	 * TODO: let some of them be post-processed
	 * @param arguments
	 */
	@Override
	public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
		return false;
	}

	@Override
	public boolean isNullable(ImmutableSet<Integer> nullableIndexes) {
		return !nullableIndexes.isEmpty();
	}

	@Override
	public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {

		ImmutableList<Optional<TermTypeInference>> argumentTypes = terms.stream()
				.map(ImmutableTerm::inferType)
				.collect(ImmutableCollectors.toList());

		return inferTypeFromArgumentTypes(argumentTypes);
	}

	/**
	 * TODO: implement it seriously after getting rid of this enum
	 */
	@Override
	public ImmutableTerm simplify(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory, VariableNullability variableNullability) {
		ImmutableList<ImmutableTerm> newTerms = terms.stream()
				.map(t -> (t instanceof ImmutableFunctionalTerm)
						? t.simplify(variableNullability)
						: t)
				.collect(ImmutableCollectors.toList());
		return termFactory.getImmutableFunctionalTerm(this, newTerms);
	}

	private Optional<TermTypeInference> inferTypeFromArgumentTypes(ImmutableList<Optional<TermTypeInference>> argumentTypes) {
		try {
			return termTypeInferenceRule.inferTypeFromArgumentTypes(argumentTypes);
		} catch (FatalTypingException e) {
			// No type could be inferred
			return Optional.empty();
		}
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
