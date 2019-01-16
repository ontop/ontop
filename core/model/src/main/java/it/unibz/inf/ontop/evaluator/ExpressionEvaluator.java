package it.unibz.inf.ontop.evaluator;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.datalog.impl.DatalogTools;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBAndFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.DefaultDBIfElseNullFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.AbstractDBIfThenFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.DefaultDBAndFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.RDF;

import java.util.Optional;

import static it.unibz.inf.ontop.model.term.functionsymbol.BooleanExpressionOperation.*;


/**
 * WARNING: NOT immutable!!!!!
 */
public class ExpressionEvaluator {

	private final DatalogTools datalogTools;
	private final TermFactory termFactory;
	private final TypeFactory typeFactory;
	private final DBConstant valueFalse;
	private final DBConstant valueTrue;
	private final Constant valueNull;
	private final ImmutableUnificationTools unificationTools;
	private final ExpressionNormalizer normalizer;
	private final CoreUtilsFactory coreUtilsFactory;
	private final RDFTermTypeConstant iriConstant, bnodeConstant;
	private final RDF rdfFactory;

	@Inject
	private ExpressionEvaluator(DatalogTools datalogTools, TermFactory termFactory, TypeFactory typeFactory,
								ImmutableUnificationTools unificationTools, ExpressionNormalizer normalizer,
								CoreUtilsFactory coreUtilsFactory, RDF rdfFactory) {
		this.termFactory = termFactory;
		this.typeFactory = typeFactory;
		this.datalogTools = datalogTools;
		valueFalse = termFactory.getDBBooleanConstant(false);
		valueTrue = termFactory.getDBBooleanConstant(true);
		valueNull = termFactory.getNullConstant();
		this.unificationTools = unificationTools;
		this.normalizer = normalizer;
		this.coreUtilsFactory = coreUtilsFactory;
		this.iriConstant = termFactory.getRDFTermTypeConstant(typeFactory.getIRITermType());
		this.bnodeConstant = termFactory.getRDFTermTypeConstant(typeFactory.getBlankNodeType());
		this.rdfFactory = rdfFactory;
	}

	public static class EvaluationResult {
		private final Optional<ImmutableExpression> optionalExpression;
		private final Optional<Boolean> optionalBooleanValue;

		private final TermFactory termFactory;

		private EvaluationResult(ImmutableExpression expression, ExpressionNormalizer normalizer, TermFactory termFactory) {
			optionalExpression = Optional.of(normalizer.normalize(expression));
			this.termFactory = termFactory;
			optionalBooleanValue = Optional.empty();
		}

		private EvaluationResult(boolean value, TermFactory termFactory) {
			this.termFactory = termFactory;
			optionalExpression = Optional.empty();
			optionalBooleanValue = Optional.of(value);
		}

		/**
		 * Evaluated as valueNull
		 * @param termFactory
		 */
		private EvaluationResult(TermFactory termFactory) {
			this.termFactory = termFactory;
			optionalExpression = Optional.empty();
			optionalBooleanValue = Optional.empty();
		}

		public Optional<ImmutableExpression> getOptionalExpression() {
			return optionalExpression;
		}

		public boolean isEffectiveTrue() {
			return optionalBooleanValue
					.filter(v -> v)
					.isPresent();
		}

		public boolean isNull() {
			return ! (optionalBooleanValue.isPresent() || optionalExpression.isPresent());
		}

		public boolean isEffectiveFalse() {
			return isFalse() || isNull();
		}

		private boolean isFalse() {
			return optionalBooleanValue
					.filter(v -> !v)
					.isPresent();
		}

		public ImmutableTerm getTerm() {
			if (optionalExpression.isPresent())
				return optionalExpression.get();
			else
				return optionalBooleanValue
						.map(b -> (Constant) termFactory.getDBBooleanConstant(b))
						.orElseGet(termFactory::getNullConstant);
		}
	}

	public EvaluationResult evaluateExpression(ImmutableExpression expression) {
		return evaluateExpression(expression, coreUtilsFactory.createDummyVariableNullability(expression));
	}

	public EvaluationResult evaluateExpression(ImmutableExpression expression, VariableNullability variableNullability) {
		ImmutableTerm evaluatedTerm = eval(expression, variableNullability);

		/**
		 * If a function, convert it into an ImmutableBooleanExpression
		 */
		if (evaluatedTerm instanceof ImmutableFunctionalTerm) {
			ImmutableFunctionalTerm evaluatedFunctionalTerm = (ImmutableFunctionalTerm) evaluatedTerm;

			FunctionSymbol functionSymbol = evaluatedFunctionalTerm.getFunctionSymbol();
			if (!(functionSymbol instanceof BooleanFunctionSymbol)) {
				throw new RuntimeException("Functional term evaluated that does not have a BooleanFunctionSymbol: "
						+ evaluatedFunctionalTerm);
			}

			return new EvaluationResult(termFactory.getImmutableExpression((BooleanFunctionSymbol) functionSymbol,
							evaluatedFunctionalTerm.getTerms()), normalizer, termFactory);
		}
		else if (evaluatedTerm instanceof Constant) {
			if (evaluatedTerm == valueFalse) {
				return new EvaluationResult(false, termFactory);
			}
			else if (evaluatedTerm == valueNull)
				return new EvaluationResult(termFactory);
			else {
				return new EvaluationResult(true, termFactory);
			}
		}
		else if (evaluatedTerm instanceof Variable) {
		    return new EvaluationResult(
		    		termFactory.getImmutableExpression(BooleanExpressionOperation.IS_TRUE, evaluatedTerm),
					normalizer, termFactory);
        }
		else {
			throw new RuntimeException("Unexpected term returned after evaluation: " + evaluatedTerm);
		}
	}


	private ImmutableTerm eval(ImmutableTerm expr, VariableNullability variableNullability) {
		if (expr instanceof Variable)
			return expr;

		else if (expr instanceof Constant)
			return expr;

		else
			return eval((ImmutableFunctionalTerm) expr, variableNullability);
	}

	private ImmutableTerm eval(ImmutableFunctionalTerm term, VariableNullability variableNullability) {

		FunctionSymbol functionSymbol = term.getFunctionSymbol();
		if (functionSymbol instanceof ExpressionOperation) {
			ExpressionOperation expressionOperation = ExpressionOperation.valueOf(functionSymbol.toString());
			switch (expressionOperation) {

				case ADD:
				case SUBTRACT:
				case MULTIPLY:
				case DIVIDE:
					throw new RuntimeException("Refactor numeric operation evaluation");
				case MINUS:
				case ABS:
				case ROUND:
				case CEIL:
				case FLOOR:
				case RAND:
				case MD5:
				case SHA1:
				case SHA512:
				case SHA256:
				case NOW:
				case YEAR:
				case DAY:
				case MONTH:
				case HOURS:
				case MINUTES:
				case SECONDS:
				case TZ:
				case AVG:
				case SUM:
				case MAX:
				case MIN:
				case COUNT:
					return term;
				default:
					throw new RuntimeException(
							"Evaluation of expression not supported: "
									+ term.toString());

			}
		}
		else if (functionSymbol instanceof BooleanExpressionOperation) {
			switch((BooleanExpressionOperation) functionSymbol){
				case IS_TRUE:
					return evalIsTrue(term, variableNullability);
				case GTE:
				case GT:
				case LTE:
				case LT:
					return term;
				default:
					throw new RuntimeException(
							"Evaluation of expression not supported: "
									+ term.toString());

			}
		}
		// TODO: remove this temporary hack!
		else if (functionSymbol instanceof DBAndFunctionSymbol) {
			return evalNaryAnd(term.getTerms(), variableNullability);
		}
		// TODO: remove this temporary hack!
		else if (functionSymbol instanceof DefaultDBIfElseNullFunctionSymbol) {
			return evalIfElseNull(term.getTerms(), variableNullability);
		}
		else {
			// isInConstructionNodeInOptimizationPhase is CURRENTLY set to true
			// to exploit unification techniques for simplifying equalities
			return term.simplify(true, variableNullability);
		}
	}

	/**
	 * Temporary: allows to use eval() on the condition
	 */
	private ImmutableTerm evalIfElseNull(ImmutableList<? extends ImmutableTerm> terms, VariableNullability variableNullability) {
		ImmutableTerm newCondition = eval(terms.get(0), variableNullability);
		if (newCondition.equals(valueFalse))
			return valueNull;
		else if (newCondition.equals(valueTrue))
			return terms.get(1);
		else if (newCondition.equals(valueNull))
			return valueNull;
		else if (newCondition instanceof ImmutableExpression)
			return termFactory.getIfElseNull((ImmutableExpression) newCondition, terms.get(1))
					.simplify(false, variableNullability);
		else
			throw new MinorOntopInternalBugException("The new condition was expected " +
					"to be a ImmutableExpression, not " + newCondition);
	}

	private ImmutableTerm evalIsTrue(ImmutableFunctionalTerm term, VariableNullability variableNullability) {
		ImmutableTerm teval = eval(term.getTerm(0), variableNullability);
		if (teval instanceof Constant) {
			return teval;
		}
		return term;
	}

	/**
	 * Temporary
	 */
	private ImmutableTerm evalNaryAnd(ImmutableList<? extends ImmutableTerm> terms, VariableNullability variableNullability) {
		return DefaultDBAndFunctionSymbol.computeNewConjunction(
				terms.stream()
						.map(t -> eval(t, variableNullability))
						.map(t -> t == null ? (ImmutableTerm) termFactory.getNullEvaluation() : t)
						.collect(ImmutableCollectors.toList()),
				termFactory);
	}

	@Override
	public ExpressionEvaluator clone() {
		return new ExpressionEvaluator(datalogTools, termFactory, typeFactory, unificationTools, normalizer,
				coreUtilsFactory, rdfFactory);
	}
}
