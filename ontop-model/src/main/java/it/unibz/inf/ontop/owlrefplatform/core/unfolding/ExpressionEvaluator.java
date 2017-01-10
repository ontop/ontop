package it.unibz.inf.ontop.owlrefplatform.core.unfolding;

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

import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.Predicate.COL_TYPE;
import it.unibz.inf.ontop.model.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.UnifierUtilities;
import it.unibz.inf.ontop.owlrefplatform.core.expression.ExpressionNormalizer;
import it.unibz.inf.ontop.owlrefplatform.core.expression.ExpressionNormalizerImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATATYPE_FACTORY;
import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;


public class ExpressionEvaluator {

	private final UriTemplateMatcher uriTemplateMatcher;

	public ExpressionEvaluator(UriTemplateMatcher matcher) {
		uriTemplateMatcher = matcher;
	}

	public static class Evaluation {
		private final Optional<ImmutableExpression> optionalExpression;
		private final Optional<Boolean> optionalBooleanValue;

		private static final ExpressionNormalizer NORMALIZER = new ExpressionNormalizerImpl();

		private Evaluation(ImmutableExpression expression) {
			optionalExpression = Optional.of(NORMALIZER.normalize(expression));
			optionalBooleanValue = Optional.empty();
		}

		private Evaluation(boolean value) {
			optionalExpression = Optional.empty();
			optionalBooleanValue = Optional.of(value);
		}

		public Optional<ImmutableExpression> getOptionalExpression() {
			return optionalExpression;
		}

		public Optional<Boolean> getOptionalBooleanValue() {
			return optionalBooleanValue;
		}

		public boolean isFalse() {
			return optionalBooleanValue
					.filter(v -> !v)
					.isPresent();
		}
	}

	public Evaluation evaluateExpression(ImmutableExpression expression) {
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

			return new Evaluation(DATA_FACTORY.getImmutableExpression(
					DATA_FACTORY.getExpression((OperationPredicate) predicate,
							evaluatedFunctionalTerm.getTerms())));
		}
		else if (evaluatedTerm instanceof Constant) {
			if (evaluatedTerm.equals(OBDAVocabulary.FALSE)) {
				return new Evaluation(false);
			}
			else {
				return new Evaluation(true);
			}
		}
		else if (evaluatedTerm instanceof Variable) {
		    return new Evaluation(DATA_FACTORY.getImmutableExpression(ExpressionOperation.IS_TRUE,
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
		if (expr.isAlgebraFunction()) { // p == OBDAVocabulary.SPARQL_JOIN || p == OBDAVocabulary.SPARQL_LEFTJOIN
			List<Term> terms = expr.getTerms();
			for (int i=0; i<terms.size(); i++) {
				Term old = terms.get(i);
				if (old instanceof Function) {
					Term newterm = eval((Function)old);
					if (!newterm.equals(old))
						if (newterm == OBDAVocabulary.FALSE) {
							//
							terms.set(i, DATA_FACTORY.getTypedTerm(OBDAVocabulary.FALSE, COL_TYPE.BOOLEAN));
						} else if (newterm == OBDAVocabulary.TRUE) {
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
				if (DATATYPE_FACTORY.isBoolean(p)) { // OBDAVocabulary.XSD_BOOLEAN
					if (valueString.equals("true") || valueString.equals("1")) {
						return OBDAVocabulary.TRUE;
					} 
					else if (valueString.equals("false") || valueString.equals("0")) {
						return OBDAVocabulary.FALSE;
					}
				}
				else if (DATATYPE_FACTORY.isInteger(p)) {
					long valueInteger = Long.parseLong(valueString);
					return DATA_FACTORY.getBooleanConstant(valueInteger != 0);
				} 
				else if (DATATYPE_FACTORY.isFloat(p)) {
					double valueD = Double.parseDouble(valueString);
					return DATA_FACTORY.getBooleanConstant(valueD > 0);
				} 
				else if (DATATYPE_FACTORY.isString(p)) {
					// ROMAN (18 Dec 2015): toString() was wrong -- it contains "" and so is never empty
					return DATA_FACTORY.getBooleanConstant(valueString.length() != 0);
				} 
				else if (DATATYPE_FACTORY.isLiteral(p)) { // R: a bit wider than p == DATA_FACTORY.getDataTypePredicateLiteral()
									// by taking LANG into account
					// ROMAN (18 Dec 2015): toString() was wrong -- it contains "" and so is never empty
					return DATA_FACTORY.getBooleanConstant(valueString.length() != 0);
				} 
				// TODO (R): year, date and time are not covered?
			} 
			else if (t0 instanceof Variable) {
				return DATA_FACTORY.getFunctionIsTrue(expr);
			} 
			else {
				return expr;
			}
		}
		return expr;
	}

	private Term evalOperation(Function term) {
		Predicate pred = term.getFunctionSymbol();
		if (pred == ExpressionOperation.AND) {
			return evalAnd(term.getTerm(0), term.getTerm(1));
		} else if (pred == ExpressionOperation.OR) {
			return evalOr(term.getTerm(0), term.getTerm(1));
		} else if (pred == ExpressionOperation.EQ) {
			return evalEqNeq(term, true);
		} else if (pred == ExpressionOperation.GT) {
			return term;
		} else if (pred == ExpressionOperation.GTE) {
			return term;
		} else if (pred == ExpressionOperation.IS_NOT_NULL) {
			return evalIsNullNotNull(term, false);
		} else if (pred == ExpressionOperation.IS_NULL) {
			return evalIsNullNotNull(term, true);
		} else if (pred == ExpressionOperation.LT) {
			return term;
		} else if (pred == ExpressionOperation.LTE) {
			return term;
		} else if (pred == ExpressionOperation.NEQ) {
			return evalEqNeq(term, false);
		} else if (pred == ExpressionOperation.NOT) {
			return evalNot(term);
		} else if (pred == ExpressionOperation.IS_TRUE) {
			return evalIsTrue(term);
		} else if (pred == ExpressionOperation.IS_LITERAL) {
			return evalIsLiteral(term);
		} else if (pred == ExpressionOperation.IS_BLANK) {
			return evalIsBlank(term);
		} else if (pred == ExpressionOperation.IS_IRI) {
			return evalIsIri(term);
		}
		else if (pred == ExpressionOperation.LANGMATCHES) {
			return evalLangMatches(term);
		} else if (pred == ExpressionOperation.REGEX) {
			return evalRegex(term);
		} else if (pred == ExpressionOperation.SQL_LIKE) {
				return term;	
		} else if (pred == ExpressionOperation.STR_STARTS) {
			return term;
		} else if (pred == ExpressionOperation.STR_ENDS) {
			return term;
		} else if (pred == ExpressionOperation.CONTAINS) {
			return term;
		} else if (pred == ExpressionOperation.SPARQL_STR) {
			return evalStr(term);
		} 
		else if (pred == ExpressionOperation.SPARQL_DATATYPE) {
			return evalDatatype(term);
		} 
		else if (pred == ExpressionOperation.SPARQL_LANG) {
			return evalLang(term);
		} 
		else if (pred == ExpressionOperation.ADD || pred == ExpressionOperation.SUBTRACT
				 || pred == ExpressionOperation.MULTIPLY || pred == ExpressionOperation.DIVIDE) {
			
			Function returnedDatatype = getDatatype(term);
            //expression has not been removed
            if(returnedDatatype != null &&
                    (returnedDatatype.getFunctionSymbol().equals(pred) || isNumeric((ValueConstant) returnedDatatype.getTerm(0)))){
                return term;
            }
			else
				return OBDAVocabulary.FALSE;
			
		} 
		else if (pred == ExpressionOperation.QUEST_CAST) {
			return term;
		}	
		else {
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
			return DATA_FACTORY.getBooleanConstant(function.isDataTypeFunction());
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
			return DATA_FACTORY.getBooleanConstant(predicate instanceof BNodePredicate);
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
			return DATA_FACTORY.getBooleanConstant(predicate instanceof URITemplatePredicate);
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
				if (DATATYPE_FACTORY.isLiteral(predicate)) { // R: was datatype.equals(OBDAVocabulary.RDFS_LITERAL_URI)
					return DATA_FACTORY.getTypedTerm(
							DATA_FACTORY.getVariable(parameter.toString()), COL_TYPE.STRING);
				} 
				else if (DATATYPE_FACTORY.isString(predicate)) { // R: was datatype.equals(OBDAVocabulary.XSD_STRING_URI)) {
					return DATA_FACTORY.getTypedTerm(
							DATA_FACTORY.getVariable(parameter.toString()), COL_TYPE.STRING);
				} 
				else {
					return DATA_FACTORY.getTypedTerm(
							DATA_FACTORY.getFunctionCast(DATA_FACTORY.getVariable(parameter.toString()),
									DATA_FACTORY.getConstantLiteral(DATATYPE_FACTORY.getDatatypeURI(COL_TYPE.STRING).stringValue())),
										COL_TYPE.STRING);
				}
			} 
			else if (predicate instanceof URITemplatePredicate) {
				return DATA_FACTORY.getTypedTerm(function.clone(), COL_TYPE.STRING);
			} 
			else if (predicate instanceof BNodePredicate) {
				return OBDAVocabulary.NULL;
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
			return DATA_FACTORY.getUriTemplateForDatatype(predicate.toString());
		} 
		else if (predicate instanceof BNodePredicate) {
			return null;
		} 
		else if (predicate instanceof URITemplatePredicate) {
			return null;
		} 
		else if (function.isAlgebraFunction()) {
			return DATA_FACTORY.getUriTemplateForDatatype(DATATYPE_FACTORY.getDatatypeURI(COL_TYPE.BOOLEAN).stringValue());
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
				return DATA_FACTORY.getUriTemplateForDatatype(pred1.toString());
			} 
			else if (isNumeric(pred1) && isDouble(pred2)) {
				return DATA_FACTORY.getUriTemplateForDatatype(pred2.toString());
			} 
			else {
				return null;
			}
		}
		else if (function.isOperation()) {
			//return boolean uri
			return DATA_FACTORY.getUriTemplateForDatatype(DATATYPE_FACTORY.getDatatypeURI(COL_TYPE.BOOLEAN).stringValue());
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
			Predicate pred = DATATYPE_FACTORY.getTypePredicate(type);
			if (pred == null)
				pred = DATATYPE_FACTORY.getTypePredicate(COL_TYPE.STRING); // .XSD_STRING;
			return pred;
		} 
		else {
			throw new RuntimeException("Unknown term type");
		}
	}
	
	private boolean isDouble(Predicate pred) {
		return (pred.equals(DATATYPE_FACTORY.getTypePredicate(COL_TYPE.DOUBLE)) || pred.equals(DATATYPE_FACTORY.getTypePredicate(COL_TYPE.FLOAT)));
	}
	
	private boolean isNumeric(Predicate pred) {
		return (DATATYPE_FACTORY.isInteger(pred) || DATATYPE_FACTORY.isFloat(pred));
	}
	
	private boolean isNumeric(ValueConstant constant) {
		String constantValue = constant.getValue();
		Predicate.COL_TYPE type = DATATYPE_FACTORY.getDatatype(constantValue);
		if (type != null) {
			Predicate p = DATATYPE_FACTORY.getTypePredicate(type);
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
		Term emptyconstant = DATA_FACTORY.getTypedTerm(DATA_FACTORY.getConstantLiteral("", COL_TYPE.STRING), COL_TYPE.STRING);

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
		if (!DATATYPE_FACTORY.isLiteral(predicate)) { // (datatype.equals(OBDAVocabulary.RDFS_LITERAL_URI))
			return emptyconstant;
		}

		if (function.getTerms().size() != 2) {
			return emptyconstant;
		} 
		else { // rdfs:Literal(text, lang)
			Term parameter = function.getTerm(1);
			if (parameter instanceof Variable) {
				return DATA_FACTORY.getTypedTerm(parameter.clone(), COL_TYPE.STRING);
			} 
			else if (parameter instanceof Constant) {
				return DATA_FACTORY.getTypedTerm(
						DATA_FACTORY.getConstantLiteral(((Constant) parameter).getValue(),COL_TYPE.STRING), COL_TYPE.STRING);
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
			return OBDAVocabulary.FALSE;
		}
		/*
		 * Evaluate the second term
		 */
		Term innerTerm2 = term.getTerm(1);
		if (innerTerm2 == null) {
			return OBDAVocabulary.FALSE;
		}

		/*
		 * Term checks
		 */
		if (teval1 instanceof Constant && innerTerm2 instanceof Constant) {
			String lang1 = ((Constant) teval1).getValue();
			String lang2 = ((Constant) innerTerm2).getValue();	
			if (lang2.equals(SELECT_ALL)) {
				if (lang1.isEmpty()) 
					return DATA_FACTORY.getFunctionIsNull(teval1);
				else 
					return DATA_FACTORY.getFunctionIsNotNull(teval1);
			} 
			else {
				return DATA_FACTORY.getBooleanConstant(lang1.equals(lang2));
			}
		} 
		else if (teval1 instanceof Variable && innerTerm2 instanceof Constant) {
			Variable var = (Variable) teval1;
			Constant lang = (Constant) innerTerm2;
			if (lang.getValue().equals(SELECT_ALL)) {
				// The char * means to get all languages
				return DATA_FACTORY.getFunctionIsNotNull(var);
			} else {
				return DATA_FACTORY.getFunctionEQ(var, lang);
			}
		} 
		else if (teval1 instanceof Function && innerTerm2 instanceof Function) {
			Function f1 = (Function) teval1;
			Function f2 = (Function) innerTerm2;
			if(f1.isOperation()){
				return term;
			}
			return evalLangMatches(DATA_FACTORY.getLANGMATCHESFunction(f1.getTerm(0),
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

        if(eval1.equals(OBDAVocabulary.FALSE)
                || eval2.equals(OBDAVocabulary.FALSE)
                || eval3.equals(OBDAVocabulary.FALSE))
        {
            return OBDAVocabulary.FALSE;
        }

        return DATA_FACTORY.getFunction(term.getFunctionSymbol(), eval1, eval2, term.getTerm(2));

	}

	private Term evalRegexSingleExpression(Term expr){

        if (expr instanceof Function) {
            Function function1 = (Function) expr;
            Predicate predicate1 = function1.getFunctionSymbol();
            if((predicate1 instanceof URITemplatePredicate)
                    ||(predicate1 instanceof BNodePredicate)) {
                return OBDAVocabulary.FALSE;
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
				Function isNotNullInnerInnerTerm = DATA_FACTORY.getFunction(ExpressionOperation.IS_NOT_NULL,
						((Function) innerTerm).getTerms());
				return evalIsNullNotNull(isNotNullInnerInnerTerm , isnull);
			}
		}
		Term result = eval(innerTerm);
		if (result == OBDAVocabulary.NULL) {
			return DATA_FACTORY.getBooleanConstant(isnull);
		} 
		else if (result instanceof Constant) {
			return DATA_FACTORY.getBooleanConstant(!isnull);
		}

		// TODO improve evaluation of is (not) null
		/*
		 * This can be improved by evaluating some of the function, e.g,. URI
		 * and Bnodes never return null
		 */
		if (isnull) {
			return DATA_FACTORY.getFunctionIsNull(result);
		} else {
			return DATA_FACTORY.getFunctionIsNotNull(result);
		}
	}

	private Term evalIsTrue(Function term) {
		Term teval = eval(term.getTerm(0));
		if (teval instanceof Function) {
			Function f = (Function) teval;
			Predicate predicate = f.getFunctionSymbol();
			if (predicate == ExpressionOperation.IS_NOT_NULL) {
				return DATA_FACTORY.getFunctionIsNotNull(f.getTerm(0));
			} else if (predicate == ExpressionOperation.IS_NULL) {
				return DATA_FACTORY.getFunctionIsNull(f.getTerm(0));
			} else if (predicate == ExpressionOperation.NEQ) {
				return DATA_FACTORY.getFunctionNEQ(f.getTerm(0), f.getTerm(1));
			} else if (predicate == ExpressionOperation.EQ) {
				return DATA_FACTORY.getFunctionEQ(f.getTerm(0), f.getTerm(1));
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
				return DATA_FACTORY.getFunctionIsNull(f.getTerm(0));
			} else if (predicate == ExpressionOperation.IS_NULL) {
				return DATA_FACTORY.getFunctionIsNotNull(f.getTerm(0));
			} else if (predicate == ExpressionOperation.NEQ) {
				return DATA_FACTORY.getFunctionEQ(f.getTerm(0), f.getTerm(1));
			} else if (predicate == ExpressionOperation.EQ) {
				return DATA_FACTORY.getFunctionNEQ(f.getTerm(0), f.getTerm(1));
			}
		} else if (teval instanceof Constant) {
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
					return OBDAVocabulary.FALSE;
				}
			} else {
				teval1 = term.getTerm(0);
			}
		}
		// This follows the SQL semantics NULL != NULL
		else if (term.getTerm(0).equals(OBDAVocabulary.NULL)) {
			return eq ? OBDAVocabulary.FALSE : OBDAVocabulary.TRUE;
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
					return OBDAVocabulary.FALSE;
				}
			} else {
				teval2 = term.getTerm(1);
			}
		}
		// This follows the SQL semantics NULL != NULL
		else if (term.getTerm(1).equals(OBDAVocabulary.NULL)) {
			return eq ? OBDAVocabulary.FALSE : OBDAVocabulary.TRUE;
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
				return DATA_FACTORY.getBooleanConstant(eq);
			else 
				return DATA_FACTORY.getBooleanConstant(!eq);
			
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
				if (DATATYPE_FACTORY.isLiteral(pred1) && DATATYPE_FACTORY.isLiteral(pred2)) { // R: replaced incorrect check
																		//  pred1 == DATA_FACTORY.getDataTypePredicateLiteral()
																		// && pred2 == DATA_FACTORY.getDataTypePredicateLiteral())
																	    // which does not work for LITERAL_LANG
					/*
					 * Special code to handle quality of Literals (plain, and
					 * with language)
					 */
					if (f1.getTerms().size() != f2.getTerms().size()) {
						// case one is with language another without
						return DATA_FACTORY.getBooleanConstant(!eq);
					} 
					else if (f1.getTerms().size() == 2) {
						// SIZE == 2
						// these are literals with languages, we need to
						// return the evaluation of the values and the
						// languages case literals without language, its
						// exactly as normal datatypes.
						// This is copy paste code
						if (eq) {
							Function eqValues = DATA_FACTORY.getFunctionEQ(f1.getTerm(0), f2.getTerm(0));
							Function eqLang = DATA_FACTORY.getFunctionEQ(f1.getTerm(1), f2.getTerm(1));
							return evalAnd(eqValues, eqLang);
						}
						Function eqValues = DATA_FACTORY.getFunctionNEQ(f1.getTerm(0), f2.getTerm(0));
						Function eqLang = DATA_FACTORY.getFunctionNEQ(f1.getTerm(1), f2.getTerm(1));
						return evalOr(eqValues, eqLang);
					}
					// case literals without language, its exactly as normal
					// datatypes
					// this is copy paste code
					if (eq) {
						Function neweq = DATA_FACTORY.getFunctionEQ(f1.getTerm(0), f2.getTerm(0));
						return evalEqNeq(neweq, true);
					} 
					else {
						Function neweq = DATA_FACTORY.getFunctionNEQ(f1.getTerm(0), f2.getTerm(0));
						return evalEqNeq(neweq, false);
					}
				} 
				else if (pred1.equals(pred2)) {
					if (pred1 instanceof URITemplatePredicate) {
						return evalUriTemplateEqNeq(f1, f2, eq);
					} 
					else {
						if (eq) {
							Function neweq = DATA_FACTORY.getFunctionEQ(f1.getTerm(0), f2.getTerm(0));
							return evalEqNeq(neweq, true);
						} 
						else {
							Function neweq = DATA_FACTORY.getFunctionNEQ(f1.getTerm(0), f2.getTerm(0));
							return evalEqNeq(neweq, false);
						}
					}
				} 
				else if (!pred1.equals(pred2)) {
					return DATA_FACTORY.getBooleanConstant(!eq);
				} 
				else {
					return term;
				}
			}
		}

		/* eval2 is not a function */
		if (eq) {
			return DATA_FACTORY.getFunctionEQ(eval1, eval2);
		} else {
			return DATA_FACTORY.getFunctionNEQ(eval1, eval2);
		}
	}

	private Term evalUriTemplateEqNeq(Function uriFunction1, Function uriFunction2, boolean isEqual) {
		int arityForFunction1 = uriFunction1.getArity();
		int arityForFunction2 = uriFunction2.getArity();		
		if (arityForFunction1 == 1) {
			if (arityForFunction2 == 1) {
				return evalUriFunctionsWithSingleTerm(uriFunction1, uriFunction2, isEqual);
			} else if (arityForFunction2 > 1) {
				Function newUriFunction1 = getUriFunctionWithParameters(uriFunction1);
				return evalUriFunctionsWithMultipleTerms(newUriFunction1, uriFunction2, isEqual);
			}
		} else if (arityForFunction1 > 1) {
			if (arityForFunction2 == 1) {
				Function newUriFunction2 = getUriFunctionWithParameters(uriFunction2);
				return evalUriFunctionsWithMultipleTerms(newUriFunction2, uriFunction1, isEqual);
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
				return DATA_FACTORY.getFunctionEQ(term2, term1);
			} else {
				if(term1 instanceof ValueConstant){
					if (isEqual)
						return DATA_FACTORY.getFunctionEQ(term1, term2);
					else
						return DATA_FACTORY.getFunctionNEQ(term1, term2);
				}
				return DATA_FACTORY.getFunctionNEQ(term2, term1);
			}

		} else if (term2 instanceof ValueConstant) {

			if (term1.equals(term2))
				return DATA_FACTORY.getBooleanConstant(isEqual);
			else
				{
				if (term1 instanceof Variable) {
					if (isEqual)
						return DATA_FACTORY.getFunctionEQ(term1, term2);
					else
						return DATA_FACTORY.getFunctionNEQ(term1, term2);
				}
				return DATA_FACTORY.getBooleanConstant(!isEqual);
			}
		}
		return null;
	}

	private Term evalUriFunctionsWithMultipleTerms(Function uriFunction1, Function uriFunction2, boolean isEqual) {
		Substitution theta = UnifierUtilities.getMGU(uriFunction1, uriFunction2);
		if (theta == null) {
			return DATA_FACTORY.getBooleanConstant(!isEqual);
		} 
		else {
			boolean isEmpty = theta.isEmpty();
			if (isEmpty) {
				return DATA_FACTORY.getBooleanConstant(!isEqual);
			} 
			else {
				Function result = null;
				List<Function> temp = new ArrayList<>();
				Set<Variable> keys = theta.getMap().keySet();
				for (Variable var : keys) {
					if (isEqual) 
						result = DATA_FACTORY.getFunctionEQ(var, theta.get(var));
					else 
						result = DATA_FACTORY.getFunctionNEQ(var, theta.get(var));
					
					temp.add(result);
					if (temp.size() == 2) {
						result = DATA_FACTORY.getFunctionAND(temp.get(0), temp.get(1));
						temp.clear();
						temp.add(result);
					}
				}
				return result;
			}				
		}
	}

	private Function getUriFunctionWithParameters(Function uriFunction) {
		ValueConstant uriString = (ValueConstant) uriFunction.getTerm(0);
		return uriTemplateMatcher.generateURIFunction(uriString.getValue());
	}
		
	
	private Term evalAnd(Term t1, Term t2) {
		Term e1 = eval(t1);
		Term e2 = eval(t2);
	
		if (e1 == OBDAVocabulary.FALSE || e2 == OBDAVocabulary.FALSE)
			return OBDAVocabulary.FALSE;
		
		if (e1 == OBDAVocabulary.TRUE)
			return e2;
		
		if (e2 == OBDAVocabulary.TRUE)
			return e1;
		
		return DATA_FACTORY.getFunctionAND(e1, e2);
	}

	private Term evalOr(Term t1, Term t2) {
		Term e1 = eval(t1);
		Term e2 = eval(t2);
	
		if (e1 == OBDAVocabulary.TRUE || e2 == OBDAVocabulary.TRUE)
			return OBDAVocabulary.TRUE;
		
		if (e1 == OBDAVocabulary.FALSE)
			return e2;
		
		if (e2 == OBDAVocabulary.FALSE)
			return e1;
		
		return DATA_FACTORY.getFunctionOR(e1, e2);
	}
}
