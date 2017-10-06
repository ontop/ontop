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

import it.unibz.inf.ontop.evaluator.impl.ExpressionNormalizerImpl;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.model.type.COL_TYPE;
import it.unibz.inf.ontop.datalog.impl.DatalogTools;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.term.TermConstants;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.impl.UnifierUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;


public class ExpressionEvaluator {

	public ExpressionEvaluator() {
	}

	public static class EvaluationResult {
		private final Optional<ImmutableExpression> optionalExpression;
		private final Optional<Boolean> optionalBooleanValue;

		private static final ExpressionNormalizer NORMALIZER = new ExpressionNormalizerImpl();

		private EvaluationResult(ImmutableExpression expression) {
			optionalExpression = Optional.of(NORMALIZER.normalize(expression));
			optionalBooleanValue = Optional.empty();
		}

		private EvaluationResult(boolean value) {
			optionalExpression = Optional.empty();
			optionalBooleanValue = Optional.of(value);
		}

		/**
		 * Evaluated as NULL
		 */
		private EvaluationResult() {
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
						.map(v -> v ? TermConstants.TRUE : TermConstants.FALSE)
						.orElse(TermConstants.NULL);
		}
	}

	public EvaluationResult evaluateExpression(ImmutableExpression expression) {
		Expression mutableExpression = ImmutabilityTools.convertToMutableBooleanExpression(expression);

		Term evaluatedTerm = evalOperation(mutableExpression);

		/**
		 * If a function, convert it into an ImmutableBooleanExpression
		 */
		if (evaluatedTerm instanceof Function) {
			Function evaluatedFunctionalTerm = (Function) evaluatedTerm;

			Predicate predicate = evaluatedFunctionalTerm.getFunctionSymbol();
			if (!(predicate instanceof OperationPredicate)) {
				throw new RuntimeException("Functional term evaluated that does not have a OperationPredicate: "
						+ evaluatedFunctionalTerm);
			}

			return new EvaluationResult(TERM_FACTORY.getImmutableExpression(
					TERM_FACTORY.getExpression((OperationPredicate) predicate,
							evaluatedFunctionalTerm.getTerms())));
		}
		else if (evaluatedTerm instanceof Constant) {
			if (evaluatedTerm == TermConstants.FALSE) {
				return new EvaluationResult(false);
			}
			else if (evaluatedTerm == TermConstants.NULL)
				return new EvaluationResult();
			else {
				return new EvaluationResult(true);
			}
		}
		else if (evaluatedTerm instanceof Variable) {
		    return new EvaluationResult(TERM_FACTORY.getImmutableExpression(ExpressionOperation.IS_TRUE,
                    ImmutabilityTools.convertIntoImmutableTerm(evaluatedTerm)));
        }
		else {
			throw new RuntimeException("Unexpected term returned after evaluation: " + evaluatedTerm);
		}
	}


	private Term eval(Term expr) {
		if (expr instanceof Variable) 
			return expr;

		else if (expr instanceof Constant) 
			return expr;
		
		else if (expr instanceof Function) 
			return eval((Function) expr);
		 
		throw new RuntimeException("Invalid expression");
	}

	private Term eval(Function expr) {
		Predicate p = expr.getFunctionSymbol();
		if (expr.isAlgebraFunction()) { // p == DatalogAlgebraOperatorPredicates.SPARQL_JOIN || p == DatalogAlgebraOperatorPredicates.SPARQL_LEFTJOIN
			List<Term> terms = expr.getTerms();
			for (int i=0; i<terms.size(); i++) {
				Term old = terms.get(i);
				if (old instanceof Function) {
					Term newterm = eval((Function)old);
					if (!newterm.equals(old))
						if (newterm == TermConstants.FALSE) {
							//
							terms.set(i, TERM_FACTORY.getTypedTerm(TermConstants.FALSE, COL_TYPE.BOOLEAN));
						} else if (newterm == TermConstants.TRUE) {
							//remove
							terms.remove(i);
							i--;
							continue;
						}
						else {
							terms.set(i, newterm);
						}
				}
			}
			return expr;
		}
		else if (expr.isOperation()) {
			return evalOperation(expr);
		} 
		else if (expr.isDataTypeFunction()) {
			Term t0 = expr.getTerm(0);
			if (t0 instanceof Constant) {
				ValueConstant value = (ValueConstant) t0;
				String valueString = value.getValue();
				if (TYPE_FACTORY.isBoolean(p)) { // OBDAVocabulary.XSD_BOOLEAN
					if (valueString.equals("true") || valueString.equals("1")) {
						return TermConstants.TRUE;
					} 
					else if (valueString.equals("false") || valueString.equals("0")) {
						return TermConstants.FALSE;
					}
				}
				else if (TYPE_FACTORY.isInteger(p)) {
					long valueInteger = Long.parseLong(valueString);
					return TERM_FACTORY.getBooleanConstant(valueInteger != 0);
				} 
				else if (TYPE_FACTORY.isFloat(p)) {
					double valueD = Double.parseDouble(valueString);
					return TERM_FACTORY.getBooleanConstant(valueD > 0);
				} 
				else if (TYPE_FACTORY.isString(p)) {
					// ROMAN (18 Dec 2015): toString() was wrong -- it contains "" and so is never empty
					return TERM_FACTORY.getBooleanConstant(valueString.length() != 0);
				}
				// TODO (R): year, date and time are not covered?
			} 
			else if (t0 instanceof Variable) {
				return TERM_FACTORY.getFunctionIsTrue(expr);
			} 
			else {
				return expr;
			}
		}
		return expr;
	}

	private Term evalOperation(Function term) {

		Predicate pred = term.getFunctionSymbol();
		ExpressionOperation expressionOperation = ExpressionOperation.valueOf(pred.toString());
		switch(expressionOperation){

			case ADD:
			case SUBTRACT:
			case MULTIPLY:
			case DIVIDE:
				Function returnedDatatype = getDatatype(term);
            //expression has not been removed
            if(returnedDatatype != null &&
                    (returnedDatatype.getFunctionSymbol().equals(pred) || isNumeric((ValueConstant) returnedDatatype.getTerm(0)))){
                return term;
            }
			else
				return TermConstants.FALSE;
			case AND :
				return evalAnd(term.getTerm(0), term.getTerm(1));
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
			case SPARQL_STR:
				return evalStr(term);
			case SPARQL_DATATYPE:
				return evalDatatype(term);
			case SPARQL_LANG:
				return evalLang(term);
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
			case UUID:
			case STRUUID:
			case MINUS:
			case ABS:
			case ROUND:
			case CEIL:
			case FLOOR:
			case RAND:
			case GTE:
			case GT:
			case LTE:
			case LT:
			case STR_STARTS:
			case STR_ENDS:
			case CONTAINS:
			case STRLEN:
			case UCASE:
			case LCASE:
			case SUBSTR2:
			case SUBSTR3:
			case STRBEFORE:
			case STRAFTER:
			case REPLACE:
			case CONCAT:
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
			case SQL_LIKE:
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

	/*
	 * Expression evaluator for isLiteral() function
	 */
	private Term evalIsLiteral(Function term) {
		Term innerTerm = term.getTerm(0);
		if (innerTerm instanceof Function) {
			Function function = (Function) innerTerm;
			return TERM_FACTORY.getBooleanConstant(function.isDataTypeFunction());
		} 
		else {
			return term;
		}
	}

	/*
	 * Expression evaluator for isBlank() function
	 */
	private Term evalIsBlank(Function term) {
		Term teval = eval(term.getTerm(0));
		if (teval instanceof Function) {
			Function function = (Function) teval;
			Predicate predicate = function.getFunctionSymbol();
			return TERM_FACTORY.getBooleanConstant(predicate instanceof BNodePredicate);
		}
		return term;
	}

	/*
	 * Expression evaluator for isIRI() and isURI() function
	 */
	private Term evalIsIri(Function term) {
		Term teval = eval(term.getTerm(0));
		if (teval instanceof Function) {
			Function function = (Function) teval;
			Predicate predicate = function.getFunctionSymbol();
			return TERM_FACTORY.getBooleanConstant(predicate instanceof URITemplatePredicate);
		}
		return term;
	}

	/*
	 * Expression evaluator for str() function
	 */
	private Term evalStr(Function term) {
		Term innerTerm = term.getTerm(0);
		if (innerTerm instanceof Function) {
			Function function = (Function) innerTerm;
			Predicate predicate = function.getFunctionSymbol();
			Term parameter = function.getTerm(0);
			if (function.isDataTypeFunction()) {
				if (TYPE_FACTORY.isString(predicate) ) { // R: was datatype.equals(OBDAVocabulary.RDFS_LITERAL_URI)
					return TERM_FACTORY.getTypedTerm(
							TERM_FACTORY.getVariable(parameter.toString()), COL_TYPE.STRING);
				} 

				else {
					return TERM_FACTORY.getTypedTerm(
							TERM_FACTORY.getFunctionCast(TERM_FACTORY.getVariable(parameter.toString()),
									TERM_FACTORY.getConstantLiteral(TYPE_FACTORY.getDatatypeURI(COL_TYPE.STRING).stringValue())),
										COL_TYPE.STRING);
				}
			} 
			else if (predicate instanceof URITemplatePredicate) {
				return TERM_FACTORY.getTypedTerm(function.clone(), COL_TYPE.STRING);
			} 
			else if (predicate instanceof BNodePredicate) {
				return TermConstants.NULL;
			}
		}
		return term;
	}

	/*
	 * Expression evaluator for datatype() function
	 */
	private Term evalDatatype(Function term) {
		Term innerTerm = term.getTerm(0);
		if (innerTerm instanceof Function) {
			Function function = (Function) innerTerm;
			return getDatatype(function);
		}
		return term;
	}
	
	private Function getDatatype(Function function) {
		Predicate predicate = function.getFunctionSymbol();
		if (function.isDataTypeFunction()) {
			return TERM_FACTORY.getUriTemplateForDatatype(predicate.toString());
		} 
		else if (predicate instanceof BNodePredicate) {
			return null;
		} 
		else if (predicate instanceof URITemplatePredicate) {
			return null;
		} 
		else if (function.isAlgebraFunction()) {
			return TERM_FACTORY.getUriTemplateForDatatype(TYPE_FACTORY.getDatatypeURI(COL_TYPE.BOOLEAN).stringValue());
		} 
		else if (predicate == ExpressionOperation.ADD || predicate == ExpressionOperation.SUBTRACT || 
				predicate == ExpressionOperation.MULTIPLY || predicate == ExpressionOperation.DIVIDE)
		{
			//return numerical if arguments have same type
			Term arg1 = function.getTerm(0);
			Term arg2 = function.getTerm(1);

			if (arg1 instanceof Variable|| arg2 instanceof Variable){
				return function;
			}
			Predicate pred1 = getDatatypePredicate(arg1);

			Predicate pred2 = getDatatypePredicate(arg2);
			if (pred1.equals(pred2) || (isDouble(pred1) && isNumeric(pred2))) {
				return TERM_FACTORY.getUriTemplateForDatatype(pred1.toString());
			} 
			else if (isNumeric(pred1) && isDouble(pred2)) {
				return TERM_FACTORY.getUriTemplateForDatatype(pred2.toString());
			} 
			else {
				return null;
			}
		}
		else if (function.isOperation()) {
			//return boolean uri
			return TERM_FACTORY.getUriTemplateForDatatype(TYPE_FACTORY.getDatatypeURI(COL_TYPE.BOOLEAN).stringValue());
		}
		return null;
	}
	
	private Predicate getDatatypePredicate(Term term) {
		if (term instanceof Function) {
			Function function = (Function) term;
			return function.getFunctionSymbol();
		} 
		else if (term instanceof Constant) {
			Constant constant = (Constant) term;
			COL_TYPE type = constant.getType();
			Predicate pred = TYPE_FACTORY.getTypePredicate(type);
			if (pred == null)
				pred = TYPE_FACTORY.getTypePredicate(COL_TYPE.STRING); // .XSD_STRING;
			return pred;
		} 
		else {
			throw new RuntimeException("Unknown term type");
		}
	}
	
	private boolean isDouble(Predicate pred) {
		return (pred.equals(TYPE_FACTORY.getTypePredicate(COL_TYPE.DOUBLE)) || pred.equals(TYPE_FACTORY.getTypePredicate(COL_TYPE.FLOAT)));
	}
	
	private boolean isNumeric(Predicate pred) {
		return (TYPE_FACTORY.isInteger(pred) || TYPE_FACTORY.isFloat(pred));
	}
	
	private boolean isNumeric(ValueConstant constant) {
		String constantValue = constant.getValue();
		Optional<COL_TYPE> type = TYPE_FACTORY.getDatatype(constantValue);
		if (type.isPresent()) {
			Predicate p = TYPE_FACTORY.getTypePredicate(type.get());
			return isNumeric(p);
		}
		return false;	
	}

	/*
	 * Expression evaluator for lang() function
	 */
	private Term evalLang(Function term) {
		Term innerTerm = term.getTerm(0);

		// Create a default return constant: blank language with literal type.
		Term emptyconstant = TERM_FACTORY.getTypedTerm(TERM_FACTORY.getConstantLiteral("", COL_TYPE.STRING), COL_TYPE.STRING);

        if (innerTerm instanceof Variable) {
            return term;
        }

        if (!(innerTerm instanceof Function)) {
			return emptyconstant;
		} 
		Function function = (Function) innerTerm;

		if (!function.isDataTypeFunction()) {
			return null;
		}

		Predicate predicate = function.getFunctionSymbol();
		//String datatype = predicate.toString();
		if (!TYPE_FACTORY.isString(predicate)) { // (datatype.equals(OBDAVocabulary.RDFS_LITERAL_URI))
			return emptyconstant;
		}

		if (function.getTerms().size() != 2) {
			return emptyconstant;
		} 
		else { // rdfs:Literal(text, lang)
			Term parameter = function.getTerm(1);
			if (parameter instanceof Variable) {
				return TERM_FACTORY.getTypedTerm(parameter.clone(), COL_TYPE.STRING);
			} 
			else if (parameter instanceof Constant) {
				return TERM_FACTORY.getTypedTerm(
						TERM_FACTORY.getConstantLiteral(((Constant) parameter).getValue(),COL_TYPE.STRING), COL_TYPE.STRING);
			}
		}
		return term;
	}

	/*
	 * Expression evaluator for langMatches() function
	 */
	private Term evalLangMatches(Function term) {
		final String SELECT_ALL = "*";
		
		/*
		 * Evaluate the first term
		 */
		Term teval1 = eval(term.getTerm(0));
		if (teval1 == null) {
			return TermConstants.NULL; // ROMAN (10 Jan 2017): not FALSE
		}
		/*
		 * Evaluate the second term
		 */
		Term innerTerm2 = term.getTerm(1);
		if (innerTerm2 == null) {
			return TermConstants.NULL; // ROMAN (10 Jan 2017): not FALSE
		}

		/*
		 * Term checks
		 */
		if (teval1 instanceof Constant && innerTerm2 instanceof Constant) {
			String lang1 = ((Constant) teval1).getValue();
			String lang2 = ((Constant) innerTerm2).getValue();	
			if (lang2.equals(SELECT_ALL)) {
				if (lang1.isEmpty()) 
					return TERM_FACTORY.getFunctionIsNull(teval1);
				else 
					return TERM_FACTORY.getFunctionIsNotNull(teval1);
			} 
			else {
				return TERM_FACTORY.getBooleanConstant(lang1.equals(lang2));
			}
		} 
		else if (teval1 instanceof Variable && innerTerm2 instanceof Constant) {
			Variable var = (Variable) teval1;
			Constant lang = (Constant) innerTerm2;
			if (lang.getValue().equals(SELECT_ALL)) {
				// The char * means to get all languages
				return TERM_FACTORY.getFunctionIsNotNull(var);
			} else {
				return TERM_FACTORY.getFunctionEQ(var, lang);
			}
		} 
		else if (teval1 instanceof Function && innerTerm2 instanceof Function) {
			Function f1 = (Function) teval1;
			Function f2 = (Function) innerTerm2;
			if(f1.isOperation()){
				return term;
			}
			return evalLangMatches(TERM_FACTORY.getLANGMATCHESFunction(f1.getTerm(0),
					f2.getTerm(0)));
		} 
		else {
			return term;
		}
	}

	private Term evalRegex(Function term) {
//
		Term eval1 = term.getTerm(0);
		eval1 = evalRegexSingleExpression(eval1);

        Term eval2 = term.getTerm(1);
		eval2 = evalRegexSingleExpression(eval2);

        Term eval3 = term.getTerm(2);
        eval3 = evalRegexSingleExpression(eval3);

        if(eval1.equals(TermConstants.FALSE)
                || eval2.equals(TermConstants.FALSE)
                || eval3.equals(TermConstants.FALSE))
        {
            return TermConstants.FALSE;
        }

        return TERM_FACTORY.getFunction(term.getFunctionSymbol(), eval1, eval2, term.getTerm(2));

	}

	private Term evalRegexSingleExpression(Term expr){

        if (expr instanceof Function) {
            Function function1 = (Function) expr;
            Predicate predicate1 = function1.getFunctionSymbol();
            if((predicate1 instanceof URITemplatePredicate)
                    ||(predicate1 instanceof BNodePredicate)) {
                return TermConstants.FALSE;
            }
            if (!function1.isDataTypeFunction()){
                return evalRegexSingleExpression( eval(expr));

            }
        }
        return expr;

    }

	private Term evalIsNullNotNull(Function term, boolean isnull) {
		Term innerTerm = term.getTerms().get(0);
		if (innerTerm instanceof Function) {
			Function f = (Function) innerTerm;
			if (f.isDataTypeFunction()) {
				Function isNotNullInnerInnerTerm = TERM_FACTORY.getFunction(ExpressionOperation.IS_NOT_NULL,
						((Function) innerTerm).getTerms());
				return evalIsNullNotNull(isNotNullInnerInnerTerm , isnull);
			}
		}
		Term result = eval(innerTerm);
		if (result == TermConstants.NULL) {
			return TERM_FACTORY.getBooleanConstant(isnull);
		} 
		else if (result instanceof Constant) {
			return TERM_FACTORY.getBooleanConstant(!isnull);
		}

		/*
		 * Special optimization for URI templates
		 */
		if (result instanceof Function) {
			Function functionalTerm = (Function) result;
			if (functionalTerm.getFunctionSymbol() instanceof URITemplatePredicate) {
				return simplifyIsNullorNotNullUriTemplate(functionalTerm, isnull);
			}
		}

		// TODO improve evaluation of is (not) null
		/*
		 * This can be improved by evaluating some of the function, e.g,. URI
		 * and Bnodes never return null
		 */
		if (isnull) {
			return TERM_FACTORY.getFunctionIsNull(result);
		} else {
			return TERM_FACTORY.getFunctionIsNotNull(result);
		}
	}

	/**
	 * TODO: make it stronger (in case someone uses complex sub-terms such as IS_NULL(x) inside the URI template...)
	 */
	private Function simplifyIsNullorNotNullUriTemplate(Function uriTemplate, boolean isNull) {
		Set<Variable> variables = uriTemplate.getVariables();
		if (isNull) {
			switch (variables.size()) {
				case 0:
					return TERM_FACTORY.getFunctionIsNull(uriTemplate);
				case 1:
					return TERM_FACTORY.getFunctionIsNull(variables.iterator().next());
				default:
					return variables.stream()
							.reduce(null,
									(e, v) -> e == null
											? TERM_FACTORY.getFunctionIsNull(v)
											: TERM_FACTORY.getFunctionOR(e, TERM_FACTORY.getFunctionIsNull(v)),
									(e1, e2) -> e1 == null
											? e2
											: (e2 == null) ? e1 : TERM_FACTORY.getFunctionOR(e1, e2));
			}
		}
		else {
			if (variables.isEmpty())
				return TERM_FACTORY.getFunctionIsNotNull(uriTemplate);
			else
				return DatalogTools.foldBooleanConditions(
						variables.stream()
								.map(TERM_FACTORY::getFunctionIsNotNull)
								.collect(Collectors.toList()));
		}
	}

	private Term evalIsTrue(Function term) {
		Term teval = eval(term.getTerm(0));
		if (teval instanceof Function) {
			Function f = (Function) teval;
			Predicate predicate = f.getFunctionSymbol();
			if (predicate == ExpressionOperation.IS_NOT_NULL) {
				return TERM_FACTORY.getFunctionIsNotNull(f.getTerm(0));
			} else if (predicate == ExpressionOperation.IS_NULL) {
				return TERM_FACTORY.getFunctionIsNull(f.getTerm(0));
			} else if (predicate == ExpressionOperation.NEQ) {
				return TERM_FACTORY.getFunctionNEQ(f.getTerm(0), f.getTerm(1));
			} else if (predicate == ExpressionOperation.EQ) {
				return TERM_FACTORY.getFunctionEQ(f.getTerm(0), f.getTerm(1));
			}
		} else if (teval instanceof Constant) {
			return teval;
		}
		return term;
	}
	
	
	private Term evalNot(Function term) {
		Term teval = eval(term.getTerm(0));
		if (teval instanceof Function) {
			Function f = (Function) teval;
			Predicate predicate = f.getFunctionSymbol();
			if (predicate == ExpressionOperation.IS_NOT_NULL) {
				return TERM_FACTORY.getFunctionIsNull(f.getTerm(0));
			} else if (predicate == ExpressionOperation.IS_NULL) {
				return TERM_FACTORY.getFunctionIsNotNull(f.getTerm(0));
			} else if (predicate == ExpressionOperation.NEQ) {
				return TERM_FACTORY.getFunctionEQ(f.getTerm(0), f.getTerm(1));
			} else if (predicate == ExpressionOperation.EQ) {
				return TERM_FACTORY.getFunctionNEQ(f.getTerm(0), f.getTerm(1));
			}
		} else if (teval instanceof Constant) {
			if (teval == TermConstants.FALSE)
				return TermConstants.TRUE;
			else if (teval == TermConstants.TRUE)
				return TermConstants.FALSE;
			else if (teval == TermConstants.NULL)
				return teval;
			// ROMAN (10 Jan 2017): this needs to be revised
			return teval;
		}
		return term;
	}

	private Term evalEqNeq(Function term, boolean eq) {
		/*
		 * Evaluate the first term
		 */
		
		// Do not eval if term is DataTypeFunction, e.g. integer(10)
		Term teval1;
		if (term.getTerm(0) instanceof Function) {
			Function t1 = (Function) term.getTerm(0);
			if (!(t1.isDataTypeFunction())) {
				teval1 = eval(term.getTerm(0));
				if (teval1 == null) {
					return TermConstants.FALSE;
				}
			} else {
				teval1 = term.getTerm(0);
			}
		}
		// This follows the SQL semantics NULL != NULL
		else if (term.getTerm(0).equals(TermConstants.NULL)) {
			return eq ? TermConstants.FALSE : TermConstants.TRUE;
		}
		else {
			teval1 = eval(term.getTerm(0));
		}

		/*
		 * Evaluate the second term
		 */

		Term teval2;
		if (term.getTerm(1) instanceof Function) {
			Function t2 = (Function) term.getTerm(1);
			if (!(t2.isDataTypeFunction())) {
				teval2 = eval(term.getTerm(1));
				if (teval2 == null) {
					return TermConstants.FALSE;
				}
			} else {
				teval2 = term.getTerm(1);
			}
		}
		// This follows the SQL semantics NULL != NULL
		else if (term.getTerm(1).equals(TermConstants.NULL)) {
			return eq ? TermConstants.FALSE : TermConstants.TRUE;
		}
		else {
			teval2 = eval(term.getTerm(1));
		}

		/* 
		 * Normalizing the location of terms, functions first 
		 */
		Term eval1 = teval1 instanceof Function ? teval1 : teval2;
		Term eval2 = teval1 instanceof Function ? teval2 : teval1;

		if (eval1 instanceof Variable || eval2 instanceof Variable) {
			// no - op
		} 
		else if (eval1 instanceof Constant && eval2 instanceof Constant) {
			if (eval1.equals(eval2)) 
				return TERM_FACTORY.getBooleanConstant(eq);
			else 
				return TERM_FACTORY.getBooleanConstant(!eq);
			
		} 
		else if (eval1 instanceof Function) {
			Function f1 = (Function) eval1;
			Predicate pred1 = f1.getFunctionSymbol();
			
			if (f1.isDataTypeFunction()) {
				if (pred1.getType(0) == COL_TYPE.UNSUPPORTED) {
					throw new RuntimeException("Unsupported type: " + pred1);
				}
			} 
			else if (f1.isOperation()) {
				return term;
			}
			
			/*
			 * Evaluate the second term
			 */
			if (eval2 instanceof Function) {
				Function f2 = (Function) eval2;
				Predicate pred2 = f2.getFunctionSymbol();
				if (pred2.getType(0) == COL_TYPE.UNSUPPORTED) {
					throw new RuntimeException("Unsupported type: " + pred2);
				}

				/*
				 * Evaluate both terms by comparing their datatypes
				 */
				if (TYPE_FACTORY.isString(pred1) && TYPE_FACTORY.isString(pred2)) { // R: replaced incorrect check
																		//  pred1 == TERM_FACTORY.getDataTypePredicateLiteral()
																		// && pred2 == TERM_FACTORY.getDataTypePredicateLiteral())
																	    // which does not work for LANG_STRING
					/*
					 * Special code to handle quality of Literals (plain, and
					 * with language)
					 */
					if (f1.getTerms().size() != f2.getTerms().size()) {
						// case one is with language another without
						return TERM_FACTORY.getBooleanConstant(!eq);
					} 
					else if (f1.getTerms().size() == 2) {
						// SIZE == 2
						// these are literals with languages, we need to
						// return the evaluation of the values and the
						// languages case literals without language, its
						// exactly as normal datatypes.
						// This is copy paste code
						if (eq) {
							Function eqValues = TERM_FACTORY.getFunctionEQ(f1.getTerm(0), f2.getTerm(0));
							Function eqLang = TERM_FACTORY.getFunctionEQ(f1.getTerm(1), f2.getTerm(1));
							return evalAnd(eqValues, eqLang);
						}
						Function eqValues = TERM_FACTORY.getFunctionNEQ(f1.getTerm(0), f2.getTerm(0));
						Function eqLang = TERM_FACTORY.getFunctionNEQ(f1.getTerm(1), f2.getTerm(1));
						return evalOr(eqValues, eqLang);
					}
					// case literals without language, its exactly as normal
					// datatypes
					// this is copy paste code
					if (eq) {
						Function neweq = TERM_FACTORY.getFunctionEQ(f1.getTerm(0), f2.getTerm(0));
						return evalEqNeq(neweq, true);
					} 
					else {
						Function neweq = TERM_FACTORY.getFunctionNEQ(f1.getTerm(0), f2.getTerm(0));
						return evalEqNeq(neweq, false);
					}
				} 
				else if (pred1.equals(pred2)) {
					if (pred1 instanceof URITemplatePredicate) {
						return evalUriTemplateEqNeq(f1, f2, eq);
					} 
					else {
						if (eq) {
							Function neweq = TERM_FACTORY.getFunctionEQ(f1.getTerm(0), f2.getTerm(0));
							return evalEqNeq(neweq, true);
						} 
						else {
							Function neweq = TERM_FACTORY.getFunctionNEQ(f1.getTerm(0), f2.getTerm(0));
							return evalEqNeq(neweq, false);
						}
					}
				} 
				else if (!pred1.equals(pred2)) {
					return TERM_FACTORY.getBooleanConstant(!eq);
				} 
				else {
					return term;
				}
			}
		}

		/* eval2 is not a function */
		if (eq) {
			return TERM_FACTORY.getFunctionEQ(eval1, eval2);
		} else {
			return TERM_FACTORY.getFunctionNEQ(eval1, eval2);
		}
	}

	private Term evalUriTemplateEqNeq(Function uriFunction1, Function uriFunction2, boolean isEqual) {
		int arityForFunction1 = uriFunction1.getArity();
		int arityForFunction2 = uriFunction2.getArity();		
		if (arityForFunction1 == 1) {
			if (arityForFunction2 == 1) {
				return evalUriFunctionsWithSingleTerm(uriFunction1, uriFunction2, isEqual);
			} else if (arityForFunction2 > 1) {
				// Currently, we assume the arity should be the same (already decomposed URIs)
				return TERM_FACTORY.getBooleanConstant(!isEqual);
			}
		} else if (arityForFunction1 > 1) {
			if (arityForFunction2 == 1) {
				// Currently, we assume the arity should be the same (already decomposed URIs)
				return TERM_FACTORY.getBooleanConstant(!isEqual);
			} else if (arityForFunction2 > 1) {
				return evalUriFunctionsWithMultipleTerms(uriFunction1, uriFunction2, isEqual);
			}
		}
		return null;
	}
	
	private Term evalUriFunctionsWithSingleTerm(Function uriFunction1, Function uriFunction2, boolean isEqual) {
		Term term1 = uriFunction1.getTerm(0);
		Term term2 = uriFunction2.getTerm(0);

		if (term2 instanceof Variable) {

			if (isEqual) {
				return TERM_FACTORY.getFunctionEQ(term2, term1);
			} else {
				if(term1 instanceof ValueConstant){
					if (isEqual)
						return TERM_FACTORY.getFunctionEQ(term1, term2);
					else
						return TERM_FACTORY.getFunctionNEQ(term1, term2);
				}
				return TERM_FACTORY.getFunctionNEQ(term2, term1);
			}

		} else if (term2 instanceof ValueConstant) {

			if (term1.equals(term2))
				return TERM_FACTORY.getBooleanConstant(isEqual);
			else
				{
				if (term1 instanceof Variable) {
					if (isEqual)
						return TERM_FACTORY.getFunctionEQ(term1, term2);
					else
						return TERM_FACTORY.getFunctionNEQ(term1, term2);
				}
				return TERM_FACTORY.getBooleanConstant(!isEqual);
			}
		}
		return null;
	}

	private Term evalUriFunctionsWithMultipleTerms(Function uriFunction1, Function uriFunction2, boolean isEqual) {
		Substitution theta = UnifierUtilities.getMGU(uriFunction1, uriFunction2);
		if (theta == null) {
			return TERM_FACTORY.getBooleanConstant(!isEqual);
		} 
		else {
			boolean isEmpty = theta.isEmpty();
			if (isEmpty) {
				return TERM_FACTORY.getBooleanConstant(!isEqual);
			} 
			else {
				Function result = null;
				List<Function> temp = new ArrayList<>();
				Set<Variable> keys = theta.getMap().keySet();
				for (Variable var : keys) {
					if (isEqual) 
						result = TERM_FACTORY.getFunctionEQ(var, theta.get(var));
					else 
						result = TERM_FACTORY.getFunctionNEQ(var, theta.get(var));
					
					temp.add(result);
					if (temp.size() == 2) {
						if (isEqual){
							result = TERM_FACTORY.getFunctionAND(temp.get(0), temp.get(1));
						}else{
							result = TERM_FACTORY.getFunctionOR(temp.get(0), temp.get(1));
						}
						temp.clear();
						temp.add(result);
					}
				}
				return result;
			}				
		}
	}
		
	
	private Term evalAnd(Term t1, Term t2) {
		Term e1 = eval(t1);
		Term e2 = eval(t2);
	
		if (e1 == TermConstants.FALSE || e2 == TermConstants.FALSE)
			return TermConstants.FALSE;
		
		if (e1 == TermConstants.TRUE)
			return e2;
		
		if (e2 == TermConstants.TRUE)
			return e1;
		
		return TERM_FACTORY.getFunctionAND(e1, e2);
	}

	private Term evalOr(Term t1, Term t2) {
		Term e1 = eval(t1);
		Term e2 = eval(t2);
	
		if (e1 == TermConstants.TRUE || e2 == TermConstants.TRUE)
			return TermConstants.TRUE;
		
		if (e1 == TermConstants.FALSE)
			return e2;
		
		if (e2 == TermConstants.FALSE)
			return e1;
		
		return TERM_FACTORY.getFunctionOR(e1, e2);
	}
}
