package it.unibz.krdb.obda.owlrefplatform.core.unfolding;

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

import it.unibz.krdb.obda.model.DatatypeFactory;
import it.unibz.krdb.obda.model.ExpressionOperation;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.BNodePredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URITemplatePredicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.Substitution;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.UnifierUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.UriTemplateMatcher;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class ExpressionEvaluator {

	private final UriTemplateMatcher uriTemplateMatcher;
	
	private final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	
	private final DatatypeFactory dtfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();
	
	private boolean regexFlag = false;

	public ExpressionEvaluator(UriTemplateMatcher matcher) {
		uriTemplateMatcher = matcher;
	}
	
	public void evaluateExpressions(DatalogProgram p) {
		List<CQIE> toremove = new LinkedList<>();
		for (CQIE q : p.getRules()) {
			setRegexFlag(false); // reset the ObjectConstant flag
			boolean empty = evaluateExpressions(q.getBody());
			if (empty) 
				toremove.add(q);
		}
		p.removeRules(toremove);
	}

	private boolean evaluateExpressions(List<Function> body) {
		for (int atomidx = 0; atomidx < body.size(); atomidx++) {
			Function atom = body.get(atomidx);
			Term newatom = eval(atom);
			if (newatom == OBDAVocabulary.TRUE) {
				body.remove(atomidx);
				atomidx -= 1;
				continue;
			} 
			else if (newatom == OBDAVocabulary.FALSE) {
				return true;
			}
			body.set(atomidx, (Function)newatom);
		}
		return false;
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
							terms.set(i, fac.getTypedTerm(OBDAVocabulary.FALSE, COL_TYPE.BOOLEAN));
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
				if (dtfac.isBoolean(p)) { // OBDAVocabulary.XSD_BOOLEAN
					if (valueString.equals("true") || valueString.equals("1")) {
						return OBDAVocabulary.TRUE;
					} 
					else if (valueString.equals("false") || valueString.equals("0")) {
						return OBDAVocabulary.FALSE;
					}
				}
				else if (dtfac.isInteger(p)) {
					long valueInteger = Long.parseLong(valueString);
					return fac.getBooleanConstant(valueInteger != 0);
				} 
				else if (dtfac.isFloat(p)) {
					double valueD = Double.parseDouble(valueString);
					return fac.getBooleanConstant(valueD > 0); 
				} 
				else if (dtfac.isString(p)) {
					// ROMAN (18 Dec 2015): toString() was wrong -- it contains "" and so is never empty
					return fac.getBooleanConstant(valueString.length() != 0);
				} 
				else if (dtfac.isLiteral(p)) { // R: a bit wider than p == fac.getDataTypePredicateLiteral() 
									// by taking LANG into account
					// ROMAN (18 Dec 2015): toString() was wrong -- it contains "" and so is never empty
					return fac.getBooleanConstant(valueString.length() != 0);
				} 
				// TODO (R): year, date and time are not covered?
			} 
			else if (t0 instanceof Variable) {
				return fac.getFunctionIsTrue(expr);
			} 
			else {
				return expr;
			}
		}
		if (p.getName().toUpperCase().contains("QUEST_OBJECT_PROPERTY_ASSERTION")) {
			// ROMAN (26 Sep 2015): what exactly is the meaning of this check?
			setRegexFlag(true);
		}
		return expr;
	}

	private void setRegexFlag(boolean b) {
		regexFlag = b;
	}
	
	private boolean isObjectConstant() {
		return regexFlag;
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
		} else if (pred == ExpressionOperation.LANGMATCHES) {
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
			if (returnedDatatype != null && isNumeric((ValueConstant) returnedDatatype.getTerm(0))) 
				return term;
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
			return fac.getBooleanConstant(function.isDataTypeFunction());
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
			return fac.getBooleanConstant(predicate instanceof BNodePredicate);
		}
		return term;
	}

	/*
	 * Expression evaluator for isURI() function
	 */
	private Term evalIsUri(Function term) {
		Term teval = eval(term.getTerm(0));
		if (teval instanceof Function) {
			Function function = (Function) teval;
			Predicate predicate = function.getFunctionSymbol();
			return fac.getBooleanConstant(predicate instanceof URITemplatePredicate);
		}
		return term;
	}

	/*
	 * Expression evaluator for isIRI() function
	 */
	private Term evalIsIri(Function term) {
		Term teval = eval(term.getTerm(0));
		if (teval instanceof Function) {
			Function function = (Function) teval;
			Predicate predicate = function.getFunctionSymbol();
			return fac.getBooleanConstant(predicate instanceof URITemplatePredicate);
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
				if (dtfac.isLiteral(predicate)) { // R: was datatype.equals(OBDAVocabulary.RDFS_LITERAL_URI)
					return fac.getTypedTerm(
							fac.getVariable(parameter.toString()), COL_TYPE.LITERAL);
				} 
				else if (dtfac.isString(predicate)) { // R: was datatype.equals(OBDAVocabulary.XSD_STRING_URI)) {
					return fac.getTypedTerm(
							fac.getVariable(parameter.toString()), COL_TYPE.LITERAL);
				} 
				else {
					return fac.getTypedTerm(
							fac.getFunctionCast(fac.getVariable(parameter.toString()),
									fac.getConstantLiteral(dtfac.getDatatypeURI(COL_TYPE.LITERAL).stringValue())),
										COL_TYPE.LITERAL);
				}
			} 
			else if (predicate instanceof URITemplatePredicate) {
				return fac.getTypedTerm(function.clone(), COL_TYPE.LITERAL);
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
			return fac.getUriTemplateForDatatype(predicate.toString());
		} 
		else if (predicate instanceof BNodePredicate) {
			return null;
		} 
		else if (predicate instanceof URITemplatePredicate) {
			return null;
		} 
		else if (function.isAlgebraFunction()) {
			return fac.getUriTemplateForDatatype(dtfac.getDatatypeURI(COL_TYPE.BOOLEAN).stringValue());
		} 
		else if (predicate == ExpressionOperation.ADD || predicate == ExpressionOperation.SUBTRACT || 
				predicate == ExpressionOperation.MULTIPLY || predicate == ExpressionOperation.DIVIDE)
		{
			//return numerical if arguments have same type
			Term arg1 = function.getTerm(0);
			Predicate pred1 = getDatatypePredicate(arg1);
			Term arg2 = function.getTerm(1);
			Predicate pred2 = getDatatypePredicate(arg2);
			if (pred1.equals(pred2) || (isDouble(pred1) && isNumeric(pred2))) {
				return fac.getUriTemplateForDatatype(pred1.toString());
			} 
			else if (isNumeric(pred1) && isDouble(pred2)) {
				return fac.getUriTemplateForDatatype(pred2.toString());
			} 
			else {
				return null;
			}
		}
		else if (function.isOperation()) {
			//return boolean uri
			return fac.getUriTemplateForDatatype(dtfac.getDatatypeURI(COL_TYPE.BOOLEAN).stringValue());
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
			Predicate pred = dtfac.getTypePredicate(type);
			if (pred == null)
				pred = dtfac.getTypePredicate(COL_TYPE.STRING); // .XSD_STRING;
			return pred;
		} 
		else {
			throw new RuntimeException("Unknown term type");
		}
	}
	
	private boolean isDouble(Predicate pred) {
		return (pred.equals(dtfac.getTypePredicate(COL_TYPE.DOUBLE)) || pred.equals(dtfac.getTypePredicate(COL_TYPE.FLOAT)));
	}
	
	private boolean isNumeric(Predicate pred) {
		return (dtfac.isInteger(pred) || dtfac.isFloat(pred));
	}
	
	private boolean isNumeric(ValueConstant constant) {
		String constantValue = constant.getValue();
		Predicate.COL_TYPE type = dtfac.getDatatype(constantValue);
		if (type != null) {
			Predicate p = dtfac.getTypePredicate(type);
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
		Term emptyconstant = fac.getTypedTerm(fac.getConstantLiteral("", COL_TYPE.LITERAL), COL_TYPE.LITERAL);

		if (!(innerTerm instanceof Function)) {
			return emptyconstant;
		} 
		Function function = (Function) innerTerm;

		if (!function.isDataTypeFunction()) {
			return null;
		}

		Predicate predicate = function.getFunctionSymbol();
		//String datatype = predicate.toString();
		if (!dtfac.isLiteral(predicate)) { // (datatype.equals(OBDAVocabulary.RDFS_LITERAL_URI))
			return emptyconstant;
		}

		if (function.getTerms().size() != 2) {
			return emptyconstant;
		} 
		else { // rdfs:Literal(text, lang)
			Term parameter = function.getTerm(1);
			if (parameter instanceof Variable) {
				return fac.getTypedTerm(parameter.clone(), COL_TYPE.LITERAL);
			} 
			else if (parameter instanceof Constant) {
				return fac.getTypedTerm(
						fac.getConstantLiteral(((Constant) parameter).getValue(),COL_TYPE.LITERAL), COL_TYPE.LITERAL);
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
					return fac.getFunctionIsNull(teval1); 
				else 
					return fac.getFunctionIsNotNull(teval1);
			} 
			else {
				return fac.getBooleanConstant(lang1.equals(lang2));
			}
		} 
		else if (teval1 instanceof Variable && innerTerm2 instanceof Constant) {
			Variable var = (Variable) teval1;
			Constant lang = (Constant) innerTerm2;
			if (lang.getValue().equals(SELECT_ALL)) {
				// The char * means to get all languages
				return fac.getFunctionIsNotNull(var);
			} else {
				return fac.getFunctionEQ(var, lang);
			}
		} 
		else if (teval1 instanceof Function && innerTerm2 instanceof Function) {
			Function f1 = (Function) teval1;
			Function f2 = (Function) innerTerm2;
			return evalLangMatches(fac.getLANGMATCHESFunction(f1.getTerm(0), 
					f2.getTerm(0)));
		} 
		else {
			return term;
		}
	}

	private Term evalRegex(Function term) {
		Term innerTerm = term.getTerm(0);
		if (innerTerm instanceof Function) {
			Function f = (Function) innerTerm;
			Predicate functionSymbol = f.getFunctionSymbol();
			if (isObjectConstant()) {
				setRegexFlag(false);
				return fac.getBooleanConstant(functionSymbol.equals(ExpressionOperation.SPARQL_STR));
			}
		}
		return term;
	}

	private Term evalIsNullNotNull(Function term, boolean isnull) {
		Term innerTerm = term.getTerms().get(0);
		if (innerTerm instanceof Function) {
			Function f = (Function) innerTerm;
			if (f.isDataTypeFunction()) return innerTerm;
		}
		Term result = eval(innerTerm);
		if (result == OBDAVocabulary.NULL) {
			return fac.getBooleanConstant(isnull);
		} 
		else if (result instanceof Constant) {
			return fac.getBooleanConstant(!isnull);
		}

		// TODO improve evaluation of is (not) null
		/*
		 * This can be improved by evaluating some of the function, e.g,. URI
		 * and Bnodes never return null
		 */
		if (isnull) {
			return fac.getFunctionIsNull(result);
		} else {
			return fac.getFunctionIsNotNull(result);
		}
	}

	private Term evalIsTrue(Function term) {
		Term teval = eval(term.getTerm(0));
		if (teval instanceof Function) {
			Function f = (Function) teval;
			Predicate predicate = f.getFunctionSymbol();
			if (predicate == ExpressionOperation.IS_NOT_NULL) {
				return fac.getFunctionIsNotNull(f.getTerm(0));
			} else if (predicate == ExpressionOperation.IS_NULL) {
				return fac.getFunctionIsNull(f.getTerm(0));
			} else if (predicate == ExpressionOperation.NEQ) {
				return fac.getFunctionNEQ(f.getTerm(0), f.getTerm(1));
			} else if (predicate == ExpressionOperation.EQ) {
				return fac.getFunctionEQ(f.getTerm(0), f.getTerm(1));
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
				return fac.getFunctionIsNull(f.getTerm(0));
			} else if (predicate == ExpressionOperation.IS_NULL) {
				return fac.getFunctionIsNotNull(f.getTerm(0));
			} else if (predicate == ExpressionOperation.NEQ) {
				return fac.getFunctionEQ(f.getTerm(0), f.getTerm(1));
			} else if (predicate == ExpressionOperation.EQ) {
				return fac.getFunctionNEQ(f.getTerm(0), f.getTerm(1));
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
		} else {
			teval1 = eval(term.getTerm(0));
		}

		/*
		 * Evaluate the second term
		 */

		Term teval2;
		if (term.getTerm(1) instanceof Function) {
			Function t2 = (Function) term.getTerm(1);
			Predicate p2 = t2.getFunctionSymbol();
			if (!(t2.isDataTypeFunction())) {
				teval2 = eval(term.getTerm(1));
				if (teval2 == null) {
					return OBDAVocabulary.FALSE;
				}
			} else {
				teval2 = term.getTerm(1);
			}
		} else {
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
				return fac.getBooleanConstant(eq);
			else 
				return fac.getBooleanConstant(!eq);
			
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
				if (dtfac.isLiteral(pred1) && dtfac.isLiteral(pred2)) { // R: replaced incorrect check 
																		//  pred1 == fac.getDataTypePredicateLiteral()
																		// && pred2 == fac.getDataTypePredicateLiteral())
																	    // which does not work for LITERAL_LANG
					/*
					 * Special code to handle quality of Literals (plain, and
					 * with language)
					 */
					if (f1.getTerms().size() != f2.getTerms().size()) {
						// case one is with language another without
						return fac.getBooleanConstant(!eq);
					} 
					else if (f1.getTerms().size() == 2) {
						// SIZE == 2
						// these are literals with languages, we need to
						// return the evaluation of the values and the
						// languages case literals without language, its
						// exactly as normal datatypes.
						// This is copy paste code
						if (eq) {
							Function eqValues = fac.getFunctionEQ(f1.getTerm(0), f2.getTerm(0));
							Function eqLang = fac.getFunctionEQ(f1.getTerm(1), f2.getTerm(1));
							return evalAnd(eqValues, eqLang);
						}
						Function eqValues = fac.getFunctionNEQ(f1.getTerm(0), f2.getTerm(0));
						Function eqLang = fac.getFunctionNEQ(f1.getTerm(1), f2.getTerm(1));
						return evalOr(eqValues, eqLang);
					}
					// case literals without language, its exactly as normal
					// datatypes
					// this is copy paste code
					if (eq) {
						Function neweq = fac.getFunctionEQ(f1.getTerm(0), f2.getTerm(0));
						return evalEqNeq(neweq, true);
					} 
					else {
						Function neweq = fac.getFunctionNEQ(f1.getTerm(0), f2.getTerm(0));
						return evalEqNeq(neweq, false);
					}
				} 
				else if (pred1.equals(pred2)) {
					if (pred1 instanceof URITemplatePredicate) {
						return evalUriTemplateEqNeq(f1, f2, eq);
					} 
					else {
						if (eq) {
							Function neweq = fac.getFunctionEQ(f1.getTerm(0), f2.getTerm(0));
							return evalEqNeq(neweq, true);
						} 
						else {
							Function neweq = fac.getFunctionNEQ(f1.getTerm(0), f2.getTerm(0));
							return evalEqNeq(neweq, false);
						}
					}
				} 
				else if (!pred1.equals(pred2)) {
					return fac.getBooleanConstant(!eq);
				} 
				else {
					return term;
				}
			}
		}

		/* eval2 is not a function */
		if (eq) {
			return fac.getFunctionEQ(eval1, eval2);
		} else {
			return fac.getFunctionNEQ(eval1, eval2);
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
//		if (!(term1 instanceof ValueConstant)) {
//			return null;
//		}
		if (term2 instanceof Variable) {
			if (isEqual) {
				return fac.getFunctionEQ(term2, term1);
			} else {
				return fac.getFunctionNEQ(term2, term1);
			}
		} else if (term2 instanceof ValueConstant) {
			if (term1.equals(term2)) 
				return fac.getBooleanConstant(isEqual);
			else 
				return fac.getBooleanConstant(!isEqual);
		}
		return null;
	}

	private Term evalUriFunctionsWithMultipleTerms(Function uriFunction1, Function uriFunction2, boolean isEqual) {
		Substitution theta = UnifierUtilities.getMGU(uriFunction1, uriFunction2);
		if (theta == null) {
			return fac.getBooleanConstant(isEqual);
		} 
		else {
			boolean isEmpty = theta.isEmpty();
			if (isEmpty) {
				return fac.getBooleanConstant(!isEqual);
			} 
			else {
				Function result = null;
				List<Function> temp = new ArrayList<>();
				Set<Variable> keys = theta.getMap().keySet();
				for (Variable var : keys) {
					if (isEqual) 
						result = fac.getFunctionEQ(var, theta.get(var));
					else 
						result = fac.getFunctionNEQ(var, theta.get(var));
					
					temp.add(result);
					if (temp.size() == 2) {
						result = fac.getFunctionAND(temp.get(0), temp.get(1));
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
		
		return fac.getFunctionAND(e1, e2);
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
		
		return fac.getFunctionOR(e1, e2);
	}
}
