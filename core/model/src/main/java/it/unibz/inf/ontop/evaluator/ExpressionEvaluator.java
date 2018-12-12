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
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.datalog.impl.DatalogTools;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.AbstractDBIfThenFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.DefaultDBAndFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.RDF;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static it.unibz.inf.ontop.model.term.functionsymbol.BooleanExpressionOperation.*;
import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.*;


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
	private final RDFTermTypeConstant iriConstant, bnodeConstant;
	private final RDF rdfFactory;

	@Inject
	private ExpressionEvaluator(DatalogTools datalogTools, TermFactory termFactory, TypeFactory typeFactory,
								ImmutableUnificationTools unificationTools, ExpressionNormalizer normalizer,
								RDF rdfFactory) {
		this.termFactory = termFactory;
		this.typeFactory = typeFactory;
		this.datalogTools = datalogTools;
		valueFalse = termFactory.getDBBooleanConstant(false);
		valueTrue = termFactory.getDBBooleanConstant(true);
		valueNull = termFactory.getNullConstant();
		this.unificationTools = unificationTools;
		this.normalizer = normalizer;
		this.iriConstant = termFactory.getRDFTermTypeConstant(typeFactory.getIRITermType());
		this.bnodeConstant = termFactory.getRDFTermTypeConstant(typeFactory.getBlankNodeType());
		this.rdfFactory = rdfFactory;
	}

	public static class EvaluationResult {
		private final Optional<ImmutableExpression> optionalExpression;
		private final Optional<Boolean> optionalBooleanValue;

		private final ExpressionNormalizer normalizer;
		private final TermFactory termFactory;

		private EvaluationResult(ImmutableExpression expression, ExpressionNormalizer normalizer, TermFactory termFactory) {
			optionalExpression = Optional.of(normalizer.normalize(expression));
			this.normalizer = normalizer;
			this.termFactory = termFactory;
			optionalBooleanValue = Optional.empty();
		}

		private EvaluationResult(boolean value, ExpressionNormalizer normalizer, TermFactory termFactory) {
			this.normalizer = normalizer;
			this.termFactory = termFactory;
			optionalExpression = Optional.empty();
			optionalBooleanValue = Optional.of(value);
		}

		/**
		 * Evaluated as valueNull
		 * @param normalizer
		 * @param termFactory
		 */
		private EvaluationResult(ExpressionNormalizer normalizer, TermFactory termFactory) {
			this.normalizer = normalizer;
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
		ImmutableTerm evaluatedTerm = evalOperation(expression);

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
				return new EvaluationResult(false, normalizer, termFactory);
			}
			else if (evaluatedTerm == valueNull)
				return new EvaluationResult(normalizer, termFactory);
			else {
				return new EvaluationResult(true, normalizer, termFactory);
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


	private ImmutableTerm eval(ImmutableTerm expr) {
		if (expr instanceof Variable)
			return expr;

		else if (expr instanceof Constant)
			return expr;

		else
			return eval((ImmutableFunctionalTerm) expr);
	}

	private ImmutableTerm eval(ImmutableFunctionalTerm expr) {
		FunctionSymbol functionSymbol = expr.getFunctionSymbol();
		if (functionSymbol instanceof OperationPredicate) {
			return evalOperation(expr);
		}
		else {
			// TODO: should we evaluation non operation?
			return expr;
		}
	}

	private ImmutableTerm evalOperation(ImmutableFunctionalTerm term) {

		FunctionSymbol functionSymbol = term.getFunctionSymbol();
		if (functionSymbol instanceof ExpressionOperation) {
			ExpressionOperation expressionOperation = ExpressionOperation.valueOf(functionSymbol.toString());
			switch (expressionOperation) {

				case ADD:
				case SUBTRACT:
				case MULTIPLY:
				case DIVIDE:
					throw new RuntimeException("Refactor numeric operation evaluation");
				case SPARQL_STR:
					return evalStr(term);
				case SPARQL_DATATYPE:
					return evalDatatype(term);
				case SPARQL_LANG:
					return evalLang(term);
				case UUID:
				case STRUUID:
				case MINUS:
				case ABS:
				case ROUND:
				case CEIL:
				case FLOOR:
				case RAND:
				case STRLEN:
				case LCASE:
				case SUBSTR2:
				case SUBSTR3:
				case STRBEFORE:
				case STRAFTER:
				case REPLACE:
				case ENCODE_FOR_URI:
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
				case QUEST_CAST:
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
				case OR:
					return evalOr(term.getTerm(0), term.getTerm(1));
				case NOT:
					return evalNot(term);
				case EQ:
					return evalEqNeq(term, true);
				case NEQ:
					return evalEqNeq(term, false);
				case IS_NULL:
					return evalIsNullNotNull(term, true);
				case IS_NOT_NULL:
					return evalIsNullNotNull(term, false);
				case IS_TRUE:
					return evalIsTrue(term);
				case IS_NUMERIC:
					return evalIsRDFLiteralNumeric(term);
				case IS_LITERAL:
					return evalIsLiteral(term);
				case IS_IRI:
					return evalIsIri(term);
				case IS_BLANK:
					return evalIsBlank(term);
				case LANGMATCHES:
					return evalLangMatches(term);
				case REGEX:
					return evalRegex(term);
				case GTE:
				case GT:
				case LTE:
				case LT:
				case STR_STARTS:
				case STR_ENDS:
				case CONTAINS:
				case SQL_LIKE:
					return term;
				default:
					throw new RuntimeException(
							"Evaluation of expression not supported: "
									+ term.toString());

			}
		}
		// TODO: remove this temporary hack!
		else if (functionSymbol instanceof DBAndFunctionSymbol) {
			return evalNaryAnd(term.getTerms());
		}
		else {
			throw new RuntimeException(
					"Evaluation of expression not supported: "
							+ term.toString());
		}
	}

	/*
	 * Expression evaluator for isNumeric() function
	 */

	private ImmutableTerm evalIsRDFLiteralNumeric(ImmutableFunctionalTerm term) {
		Optional<TermType> optionalTermType = getTermType(term.getTerm(0));
		if (!optionalTermType.isPresent())
			return term;

		boolean isNumeric = optionalTermType
				.map(t -> t.isA(typeFactory.getAbstractOntopNumericDatatype()))
				.orElse(false);

		return termFactory.getDBBooleanConstant(isNumeric);
	}

	/*
	 * Expression evaluator for isLiteral() function
	 */
	private ImmutableTerm evalIsLiteral(ImmutableFunctionalTerm term) {
		ImmutableTerm innerTerm = term.getTerm(0);
		if (innerTerm instanceof ImmutableFunctionalTerm) {
			ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) innerTerm;
			Optional<TermTypeInference> optionalTypeInference = functionalTerm.inferType();

			if (optionalTypeInference.isPresent()) {
				return optionalTypeInference.get()
						.getTermType()
						.map(t -> t.isA(typeFactory.getAbstractRDFSLiteral()))
						.map(termFactory::getDBBooleanConstant)
						// Non-fatal error
						.orElse(null);
			}
			// Not determined yet
			else
				return term;
		}
		else {
			return term;
		}
	}

	/*
	 * Expression evaluator for isBlank() function
	 */
	private ImmutableTerm evalIsBlank(ImmutableFunctionalTerm term) {
		ImmutableTerm teval = eval(term.getTerm(0));
		if (teval instanceof ImmutableFunctionalTerm) {
			return termFactory.getDBBooleanConstant(isKnownToBeBlank((ImmutableFunctionalTerm) teval));
		}
		return term;
	}

	/*
	 * Expression evaluator for isIRI() and isURI() function
	 */
	private ImmutableTerm evalIsIri(ImmutableFunctionalTerm term) {
		ImmutableTerm teval = eval(term.getTerm(0));
		if (teval instanceof ImmutableFunctionalTerm) {
			return termFactory.getDBBooleanConstant(isKnownToBeIRI((ImmutableFunctionalTerm) teval));
		}
		return term;
	}

	private boolean isKnownToBeIRI(ImmutableFunctionalTerm functionalTerm) {
		return (functionalTerm.getFunctionSymbol() instanceof RDFTermFunctionSymbol)
				&& functionalTerm.getTerm(1).equals(iriConstant);
	}

	private boolean isKnownToBeBlank(ImmutableFunctionalTerm functionalTerm) {
		return (functionalTerm.getFunctionSymbol() instanceof RDFTermFunctionSymbol)
				&& functionalTerm.getTerm(1).equals(bnodeConstant);
	}

	/*
	 * Expression evaluator for str() function
	 */
	private ImmutableTerm evalStr(ImmutableFunctionalTerm topFunctionalTerm) {
		ImmutableTerm innerTerm = topFunctionalTerm.getTerm(0);
		if (innerTerm instanceof ImmutableFunctionalTerm) {
			ImmutableFunctionalTerm functionalInnerTerm = (ImmutableFunctionalTerm) innerTerm;
			FunctionSymbol functionSymbol = functionalInnerTerm.getFunctionSymbol();
			if (functionSymbol instanceof RDFTermFunctionSymbol) {
				ImmutableTerm lexicalTerm = functionalInnerTerm.getTerm(0);
				ImmutableTerm typeTerm = functionalInnerTerm.getTerm(1);

				return (typeTerm.equals(bnodeConstant))
						// B-node are excluded
						? valueNull
						// Lexical term
						: termFactory.getRDFLiteralFunctionalTerm(lexicalTerm, XSD.STRING);
			}
			// TODO: reject if not applied to RDF term
		}
		return topFunctionalTerm;
	}

	/*
	 * Expression evaluator for datatype() function
	 */
	private ImmutableTerm evalDatatype(ImmutableFunctionalTerm functionalTerm) {
		ImmutableTerm innerTerm = functionalTerm.getTerm(0);
		if (innerTerm instanceof ImmutableFunctionalTerm) {
			ImmutableFunctionalTerm innerFunctionalTerm = (ImmutableFunctionalTerm) innerTerm;
			Optional<TermTypeInference> optionalTypeInference = innerFunctionalTerm.inferType();

			if (optionalTypeInference.isPresent()) {
				return optionalTypeInference.get().getTermType()
						.filter(t -> t instanceof RDFDatatype)
						.map(t -> ((RDFDatatype) t).getIRI())
						.map(i -> (ImmutableTerm) termFactory.getConstantIRI(i))
						// Not a Datatype (or a non-fatal error)
						.orElse(null);
			}
			else
				// Not determined yet
				return functionalTerm;
		}
		// No simplification
		return functionalTerm;
	}

	/**
	 * TODO: return an Optional<TermTypeInference> instead
	 */
	private Optional<TermType> getTermType(ImmutableTerm term) {
		if (term instanceof ImmutableFunctionalTerm) {
			return term.inferType()
					.flatMap(TermTypeInference::getTermType);
		}
		else if (term instanceof Constant) {
			return ((Constant) term).getOptionalType();
		}
		// Variable
		else {
			return Optional.empty();
		}
	}

	/*
	 * Expression evaluator for lang() function
	 */
	private ImmutableTerm evalLang(ImmutableFunctionalTerm term) {
		ImmutableTerm innerTerm = term.getTerm(0);

		// Create a default return constant: blank language with literal type.
		// TODO: avoid this constant wrapping thing
		ImmutableFunctionalTerm emptyString = termFactory.getRDFLiteralFunctionalTerm(
				termFactory.getRDFLiteralConstant("", XSD.STRING), XSD.STRING);

        if (innerTerm instanceof Variable) {
            return term;
        }
		/*
		 * TODO: consider the case of constants
		 */
		if (!(innerTerm instanceof ImmutableFunctionalTerm)) {
			return emptyString;
		}
		ImmutableFunctionalTerm function = (ImmutableFunctionalTerm) innerTerm;

		Optional<TermTypeInference> optionalTypeInference = function.inferType();
		if (optionalTypeInference.isPresent()) {
			return optionalTypeInference.get().getTermType()
					.filter(t -> t instanceof RDFDatatype)
					.map(t -> (RDFDatatype) t)
					.flatMap(RDFDatatype::getLanguageTag)
					.map(tag -> termFactory.getRDFLiteralFunctionalTerm(
							termFactory.getRDFLiteralConstant(tag.getFullString(), XSD.STRING),
							XSD.STRING))
					// Not a langstring or non-fatal error
					.orElse(null);
		}
		// Not determined yet
		else {
			return term;
		}
	}

	/*
	 * Expression evaluator for langMatches() function
	 */
	private ImmutableTerm evalLangMatches(ImmutableFunctionalTerm term) {
		final String SELECT_ALL = "*";

		/*
		 * Evaluate the first term
		 */
		ImmutableTerm teval1 = eval(term.getTerm(0));
		if (teval1 == null) {
			return valueNull; // ROMAN (10 Jan 2017): not valueFalse
		}
		/*
		 * Evaluate the second term
		 */
		ImmutableTerm innerTerm2 = term.getTerm(1);
		if (innerTerm2 == null) {
			return valueNull; // ROMAN (10 Jan 2017): not valueFalse
		}

		/*
		 * Term checks
		 */
		if (teval1 instanceof Constant && innerTerm2 instanceof Constant) {
			String lang1 = ((Constant) teval1).getValue();
			String lang2 = ((Constant) innerTerm2).getValue();
			if (lang2.equals(SELECT_ALL)) {
				if (lang1.isEmpty())
					return termFactory.getImmutableFunctionalTerm(IS_NULL, teval1);
				else
					return termFactory.getImmutableFunctionalTerm(IS_NOT_NULL, teval1);
			}
			else {
				return termFactory.getDBBooleanConstant(lang1.equals(lang2));
			}
		}
		else if (teval1 instanceof Variable && innerTerm2 instanceof Constant) {
			Variable var = (Variable) teval1;
			Constant lang = (Constant) innerTerm2;
			if (lang.getValue().equals(SELECT_ALL)) {
				// The char * means to get all languages
				return termFactory.getImmutableFunctionalTerm(IS_NOT_NULL, var);
			} else {
				return termFactory.getImmutableFunctionalTerm(EQ, var, lang);
			}
		}
		else if (teval1 instanceof ImmutableFunctionalTerm && innerTerm2 instanceof ImmutableFunctionalTerm) {
			ImmutableFunctionalTerm f1 = (ImmutableFunctionalTerm) teval1;
			ImmutableFunctionalTerm f2 = (ImmutableFunctionalTerm) innerTerm2;
			if(f1.getFunctionSymbol() instanceof OperationPredicate){
				return term;
			}
			return evalLangMatches(termFactory.getImmutableFunctionalTerm(LANGMATCHES, f1.getTerm(0),
					f2.getTerm(0)));
		}
		else {
			return term;
		}
	}

	private ImmutableTerm evalRegex(ImmutableFunctionalTerm term) {
//
		ImmutableTerm eval1 = term.getTerm(0);
		eval1 = evalRegexSingleExpression(eval1);

        ImmutableTerm eval2 = term.getTerm(1);
		eval2 = evalRegexSingleExpression(eval2);

        ImmutableTerm eval3 = term.getTerm(2);
        eval3 = evalRegexSingleExpression(eval3);

        if(eval1.equals(valueFalse)
                || eval2.equals(valueFalse)
                || eval3.equals(valueFalse))
        {
            return valueFalse;
        }

        return termFactory.getImmutableFunctionalTerm(term.getFunctionSymbol(), eval1, eval2, term.getTerm(2));

	}

	private ImmutableTerm evalRegexSingleExpression(ImmutableTerm expr){

        if (expr instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm function1 = (ImmutableFunctionalTerm) expr;
            FunctionSymbol functionSymbol1 = function1.getFunctionSymbol();
            if((functionSymbol1 instanceof RDFTermFunctionSymbol)
                    && (function1.getTerm(1).equals(iriConstant)
						|| function1.getTerm(1).equals(bnodeConstant))) {
                return valueFalse;
            }
			ImmutableTerm evaluatedExpression = eval(expr);
			return expr.equals(evaluatedExpression)
					? expr
					: evalRegexSingleExpression(evaluatedExpression);
        }
        return expr;

    }

	private ImmutableTerm evalIfElseNull(ImmutableFunctionalTerm term) {
		ImmutableTerm formerCondition = term.getTerm(0);
		ImmutableTerm newCondition = eval(formerCondition);
		if (newCondition.equals(formerCondition))
			return term;
		else if (newCondition.equals(valueFalse))
			return valueNull;
		else if (newCondition.equals(valueTrue))
			return term.getTerm(1);
		else
			return termFactory.getImmutableFunctionalTerm(term.getFunctionSymbol(), newCondition, term.getTerm(1));
	}

	private ImmutableTerm evalIsNullNotNull(ImmutableFunctionalTerm term, boolean isnull) {
		ImmutableTerm innerTerm = term.getTerms().get(0);
		if (innerTerm instanceof ImmutableFunctionalTerm) {
			ImmutableFunctionalTerm functionalInnerTerm = (ImmutableFunctionalTerm) innerTerm;
			FunctionSymbol functionSymbol = functionalInnerTerm.getFunctionSymbol();
			if (functionSymbol instanceof RDFTermType) {

				ImmutableFunctionalTerm isNotNullInnerInnerTerm = termFactory.getImmutableFunctionalTerm(
						isnull ? IS_NULL : IS_NOT_NULL,
						((ImmutableFunctionalTerm) innerTerm).getTerm(0));
				return evalIsNullNotNull(isNotNullInnerInnerTerm , isnull);
			}
		}
		ImmutableTerm result = eval(innerTerm);
		if (result == valueNull) {
			return termFactory.getDBBooleanConstant(isnull);
		}
		else if (result instanceof Constant) {
			return termFactory.getDBBooleanConstant(!isnull);
		}

		if (result instanceof ImmutableFunctionalTerm) {
			ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) result;
			FunctionSymbol functionSymbol = functionalTerm.getFunctionSymbol();
			/*
			 * Special optimization for URI templates
			 */
			if (functionSymbol instanceof IRIStringTemplateFunctionSymbol) {
				return simplifyIsNullorNotNullUriTemplate(functionalTerm, isnull);
			}
			/*
			 * All the functions that accepts null
			 * TODO: add COALESCE
			 */
			else if (functionSymbol != IS_NULL
					&& functionSymbol != IS_NOT_NULL
					// TODO: use something else!
					&& (!(functionSymbol instanceof AbstractDBIfThenFunctionSymbol))) {
				ImmutableExpression notNullExpression = termFactory.getConjunction(
						functionalTerm.getTerms().stream()
								.map(t -> termFactory.getImmutableExpression(IS_NOT_NULL, t))).get();
				return eval(isnull
						? termFactory.getImmutableFunctionalTerm(NOT, notNullExpression)
						: notNullExpression);
			}
		}

		// TODO improve evaluation of is (not) null
		/*
		 * This can be improved by evaluating some of the function, e.g,. URI
		 * and Bnodes never return null
		 */
		if (isnull) {
			return termFactory.getImmutableFunctionalTerm(IS_NULL, result);
		} else {
			return termFactory.getImmutableFunctionalTerm(IS_NOT_NULL, result);
		}
	}

	/**
	 * TODO: make it stronger (in case someone uses complex sub-terms such as IS_NULL(x) inside the URI template...)
	 */
	private ImmutableTerm simplifyIsNullorNotNullUriTemplate(ImmutableFunctionalTerm uriTemplate, boolean isNull) {
		ImmutableList<? extends ImmutableTerm> terms = uriTemplate.getTerms();
		if (isNull) {
			switch (terms.size()) {
				case 0:
					return termFactory.getImmutableFunctionalTerm(IS_NULL, uriTemplate);
				case 1:
					return termFactory.getImmutableFunctionalTerm(IS_NULL, terms.get(0));
				default:
					return terms.stream()
							.reduce(null,
									(e, t) -> e == null
											? termFactory.getImmutableFunctionalTerm(IS_NULL, t)
											: termFactory.getImmutableFunctionalTerm(OR, e, termFactory.getImmutableFunctionalTerm(IS_NULL, t)),
									(e1, e2) -> e1 == null
											? e2
											: (e2 == null) ? e1 : termFactory.getImmutableFunctionalTerm(OR, e1, e2));
			}
		}
		else {
			if (terms.isEmpty())
				return termFactory.getImmutableFunctionalTerm(IS_NOT_NULL, uriTemplate);
			else
				return eval(termFactory.getConjunction(
						terms.stream()
								.map(t -> termFactory.getImmutableExpression(IS_NOT_NULL, t))
				).get());
		}
	}

	private ImmutableTerm evalIsTrue(ImmutableFunctionalTerm term) {
		ImmutableTerm teval = eval(term.getTerm(0));
		if (teval instanceof ImmutableFunctionalTerm) {
			ImmutableFunctionalTerm f = (ImmutableFunctionalTerm) teval;
			FunctionSymbol functionSymbol = f.getFunctionSymbol();
			if (functionSymbol == IS_NOT_NULL) {
				return termFactory.getImmutableFunctionalTerm(IS_NOT_NULL, f.getTerm(0));
			} else if (functionSymbol == IS_NULL) {
				return termFactory.getImmutableFunctionalTerm(IS_NULL, f.getTerm(0));
			} else if (functionSymbol == NEQ) {
				return termFactory.getImmutableFunctionalTerm(NEQ, f.getTerm(0), f.getTerm(1));
			} else if (functionSymbol == EQ) {
				return termFactory.getImmutableFunctionalTerm(EQ, f.getTerm(0), f.getTerm(1));
			}
		} else if (teval instanceof Constant) {
			return teval;
		}
		return term;
	}


	private ImmutableTerm evalNot(ImmutableFunctionalTerm term) {
		ImmutableTerm initialSubTerm = term.getTerm(0);
		ImmutableTerm teval = eval(initialSubTerm);
		if (teval instanceof ImmutableExpression) {
			return ((ImmutableExpression) teval).negate(termFactory);
		} else if (teval instanceof Constant) {
			if (teval == valueFalse)
				return valueTrue;
			else if (teval == valueTrue)
				return valueFalse;
			else if (teval == valueNull)
				return teval;
			// ROMAN (10 Jan 2017): this needs to be revised
			return teval;
		}
		return initialSubTerm.equals(teval)
				? term
				: termFactory.getImmutableFunctionalTerm(NOT, teval);
	}

	private ImmutableTerm evalEqNeq(ImmutableFunctionalTerm term, boolean eq) {
		/*
		 * Evaluate the first term
		 */

		// Do not eval if term is DataTypeFunction, e.g. integer(10)
		ImmutableTerm teval1;
		if (term.getTerm(0) instanceof ImmutableFunctionalTerm) {
			ImmutableFunctionalTerm t1 = (ImmutableFunctionalTerm) term.getTerm(0);
			teval1 = eval(term.getTerm(0));
			if (teval1 == null) {
				return valueFalse;
			}
		}
		// This follows the SQL semantics valueNull != valueNull
		else if (term.getTerm(0).equals(valueNull)) {
			return eq ? valueFalse : valueTrue;
		}
		else {
			teval1 = eval(term.getTerm(0));
		}

		/*
		 * Evaluate the second term
		 */

		ImmutableTerm teval2;
		if (term.getTerm(1) instanceof ImmutableFunctionalTerm) {
			ImmutableFunctionalTerm t2 = (ImmutableFunctionalTerm) term.getTerm(1);
			teval2 = eval(term.getTerm(1));
			if (teval2 == null) {
				return valueFalse;
			}
		}
		// This follows the SQL semantics valueNull != valueNull
		else if (term.getTerm(1).equals(valueNull)) {
			return eq ? valueFalse : valueTrue;
		}
		else {
			teval2 = eval(term.getTerm(1));
		}

		/*
		 * Normalizing the location of terms, functions first
		 */
		ImmutableTerm eval1 = teval1 instanceof ImmutableFunctionalTerm ? teval1 : teval2;
		ImmutableTerm eval2 = teval1 instanceof ImmutableFunctionalTerm ? teval2 : teval1;

		if (eval1 instanceof Variable || eval2 instanceof Variable) {
			// no - op
		}
		else if (eval1 instanceof Constant && eval2 instanceof Constant) {
			if (eval1.equals(eval2))
				return termFactory.getDBBooleanConstant(eq);
			else
				return termFactory.getDBBooleanConstant(!eq);

		}
		else if (eval1 instanceof ImmutableFunctionalTerm) {
			ImmutableFunctionalTerm f1 = (ImmutableFunctionalTerm) eval1;
			FunctionSymbol functionSymbol1 = f1.getFunctionSymbol();

			// TODO: see if we can get rid of it
			if (functionSymbol1 instanceof OperationPredicate) {
				return term;
			}

			// TODO: implement it seriously
			if (!functionSymbol1.isInjective(f1.getTerms(), ImmutableSet.of()))
				return term;


			/*
			 * Evaluate the second term
			 */
			if (eval2 instanceof ImmutableFunctionalTerm) {
				ImmutableFunctionalTerm f2 = (ImmutableFunctionalTerm) eval2;
				FunctionSymbol pred2 = f2.getFunctionSymbol();
//				if (pred2.getTermType(0) == COL_TYPE.UNSUPPORTED) {
//					throw new RuntimeException("Unsupported type: " + pred2);
//				}

				if (functionSymbol1.equals(pred2)) {
					if (functionSymbol1 instanceof IRIStringTemplateFunctionSymbol) {
						return evalUriTemplateEqNeq(f1, f2, eq);
					}
					else {
						if (eq) {
							ImmutableFunctionalTerm neweq = termFactory.getImmutableFunctionalTerm(EQ, f1.getTerm(0), f2.getTerm(0));
							return evalEqNeq(neweq, true);
						}
						else {
							ImmutableFunctionalTerm neweq = termFactory.getImmutableFunctionalTerm(NEQ, f1.getTerm(0), f2.getTerm(0));
							return evalEqNeq(neweq, false);
						}
					}
				}
				else if (!functionSymbol1.equals(pred2)) {
					return termFactory.getDBBooleanConstant(!eq);
				}
				else {
					return term;
				}
			}
		}

		/* eval2 is not a function */
		if (eq) {
			return termFactory.getImmutableFunctionalTerm(EQ, teval1, teval2);
		} else {
			return termFactory.getImmutableFunctionalTerm(NEQ, teval1, teval2);
		}
	}

	private ImmutableTerm evalUriTemplateEqNeq(ImmutableFunctionalTerm uriFunction1, ImmutableFunctionalTerm uriFunction2, boolean isEqual) {
		int arityForFunction1 = uriFunction1.getArity();
		int arityForFunction2 = uriFunction2.getArity();
		if (arityForFunction1 == 1) {
			if (arityForFunction2 == 1) {
				return evalUriFunctionsWithSingleTerm(uriFunction1, uriFunction2, isEqual);
			} else if (arityForFunction2 > 1) {
				// Currently, we assume the arity should be the same (already decomposed URIs)
				return termFactory.getDBBooleanConstant(!isEqual);
			}
		} else if (arityForFunction1 > 1) {
			if (arityForFunction2 == 1) {
				// Currently, we assume the arity should be the same (already decomposed URIs)
				return termFactory.getDBBooleanConstant(!isEqual);
			} else if (arityForFunction2 > 1) {
				return evalUriFunctionsWithMultipleTerms(uriFunction1, uriFunction2, isEqual);
			}
		}
		return null;
	}

	private ImmutableTerm evalUriFunctionsWithSingleTerm(ImmutableFunctionalTerm uriFunction1,
														 ImmutableFunctionalTerm uriFunction2, boolean isEqual) {
		ImmutableTerm term1 = uriFunction1.getTerm(0);
		ImmutableTerm term2 = uriFunction2.getTerm(0);

		if (term2 instanceof Variable) {

			if (isEqual) {
				return termFactory.getImmutableFunctionalTerm(EQ, term2, term1);
			} else {
				if(term1 instanceof Constant){
					if (isEqual)
						return termFactory.getImmutableFunctionalTerm(EQ, term1, term2);
					else
						return termFactory.getImmutableFunctionalTerm(NEQ, term1, term2);
				}
				return termFactory.getImmutableFunctionalTerm(NEQ, term2, term1);
			}

		} else if (term2 instanceof Constant) {

			if (term1.equals(term2))
				return termFactory.getDBBooleanConstant(isEqual);
			else
				{
				if (term1 instanceof Variable) {
					if (isEqual)
						return termFactory.getImmutableFunctionalTerm(EQ, term1, term2);
					else
						return termFactory.getImmutableFunctionalTerm(NEQ, term1, term2);
				}
				return termFactory.getDBBooleanConstant(!isEqual);
			}
		}
		// TODO: try to optimize further on
		return isEqual
				? termFactory.getImmutableFunctionalTerm(EQ,term1, term2)
				: termFactory.getImmutableFunctionalTerm(NEQ, term1, term2);
	}

	private ImmutableTerm evalUriFunctionsWithMultipleTerms(ImmutableFunctionalTerm uriFunction1,
															ImmutableFunctionalTerm uriFunction2, boolean isEqual) {
		if (uriFunction1.equals(uriFunction2))
			return termFactory.getDBBooleanConstant(isEqual);

		Optional<ImmutableSubstitution<ImmutableTerm>> optionalTheta = unificationTools.computeMGU(uriFunction1, uriFunction2);
		if (!optionalTheta.isPresent())
			return termFactory.getDBBooleanConstant(!isEqual);
		else {
			ImmutableSubstitution<ImmutableTerm> theta = optionalTheta.get();

			boolean isEmpty = theta.isEmpty();
			if (isEmpty) {
				return termFactory.getDBBooleanConstant(!isEqual);
			}
			else {
				ImmutableFunctionalTerm result = null;
				List<ImmutableFunctionalTerm> temp = new ArrayList<>();
				Set<Variable> keys = theta.getDomain();
				for (Variable var : keys) {
					if (isEqual)
						result = termFactory.getImmutableFunctionalTerm(EQ, var, theta.get(var));
					else
						result = termFactory.getImmutableFunctionalTerm(NEQ, var, theta.get(var));

					temp.add(result);
					if (temp.size() == 2) {
						if (isEqual){
							result = termFactory.getConjunction((ImmutableExpression) temp.get(0), (ImmutableExpression)temp.get(1));
						}else{
							result = termFactory.getImmutableFunctionalTerm(OR, temp.get(0), temp.get(1));
						}
						temp.clear();
						temp.add(result);
					}
				}
				return result;
			}
		}
	}

	/**
	 * Temporary
	 */
	private ImmutableTerm evalNaryAnd(ImmutableList<? extends ImmutableTerm> terms) {
		return DefaultDBAndFunctionSymbol.computeNewConjunction(
				terms.stream()
						.map(this::eval)
						.map(t -> t == null ? (ImmutableTerm) termFactory.getNullEvaluation() : t)
						.collect(ImmutableCollectors.toList()),
				termFactory);
	}

	private ImmutableTerm evalOr(ImmutableTerm t1, ImmutableTerm t2) {
		ImmutableTerm e1 = eval(t1);
		ImmutableTerm e2 = eval(t2);

		if (e1 == valueTrue || e2 == valueTrue)
			return valueTrue;

		if (e1 == valueFalse)
			return e2;

		if (e2 == valueFalse)
			return e1;

		return termFactory.getImmutableFunctionalTerm(OR, e1, e2);
	}

	@Override
	public ExpressionEvaluator clone() {
		return new ExpressionEvaluator(datalogTools, termFactory, typeFactory, unificationTools, normalizer, rdfFactory);
	}
}
