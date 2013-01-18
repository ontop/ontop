package it.unibz.krdb.obda.owlrefplatform.core.unfolding;

import it.unibz.krdb.obda.model.AlgebraOperatorPredicate;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.BNodePredicate;
import it.unibz.krdb.obda.model.BooleanOperationPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DataTypePredicate;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.NonBooleanOperationPredicate;
import it.unibz.krdb.obda.model.NumericalOperationPredicate;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OperationPredicate;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URITemplatePredicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.Unifier;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.UriTemplateMatcher;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.iri.IRIFactory;

public class ExpressionEvaluator {

	private UriTemplateMatcher uriTemplateMatcher;
	
	private OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	private boolean regexFlag = false;

	public void setUriTemplateMatcher(UriTemplateMatcher matcher) {
		uriTemplateMatcher = matcher;
	}
	
	public void evaluateExpressions(DatalogProgram p) {
		Set<CQIE> toremove = new LinkedHashSet<CQIE>();
		for (CQIE q : p.getRules()) {
			setRegexFlag(false); // reset the ObjectConstant flag
			boolean empty = evaluateExpressions(q);
			if (empty) {
				toremove.add(q);
			}
		}
		p.removeRules(toremove);
	}

	public boolean evaluateExpressions(CQIE q) {
		for (int atomidx = 0; atomidx < q.getBody().size(); atomidx++) {
			Atom atom = q.getBody().get(atomidx);
			NewLiteral newatom = eval(atom);
			if (newatom == fac.getTrue()) {
				q.getBody().remove(atomidx);
				atomidx -= 1;
				continue;
			} else if (newatom == fac.getFalse()) {
				return true;
			}
			q.getBody().remove(atomidx);
			q.getBody().add(atomidx, newatom.asAtom());
		}
		return false;
	}

	public NewLiteral eval(NewLiteral expr) {
		if (expr instanceof Variable) {
			return eval((Variable) expr);
		} else if (expr instanceof Constant) {
			return eval((Constant) expr);
		} else if (expr instanceof Function) {
			return eval((Function) expr);
		} else {
			throw new RuntimeException("Invalid expression");
		}
	}

	public NewLiteral eval(Variable expr) {
		return expr;
	}

	public NewLiteral eval(Constant expr) {
		return expr;
	}

	public NewLiteral eval(Function expr) {
		Predicate p = expr.getFunctionSymbol();
		if (p instanceof BooleanOperationPredicate) {
			return evalBoolean(expr);
		} else if (p instanceof NonBooleanOperationPredicate) {
			return evalNonBoolean(expr);
		} else if (p instanceof NumericalOperationPredicate) {
			return evalNumericalOperation(expr);
		} else if (p instanceof DataTypePredicate) {
			if (p == OBDAVocabulary.XSD_BOOLEAN) {
				if (expr.getTerm(0) instanceof Constant) {
					ValueConstant value = (ValueConstant) expr.getTerm(0);
					String valueString = value.getValue();
					if (valueString.equals("true") || valueString.equals("1")) {
						return fac.getTrue();
					} else if (valueString.equals("false") || valueString.equals("0")) {
						return fac.getFalse();
					}
				} else if (expr.getTerm(0) instanceof Variable) {
					return fac.getFunctionalTerm(OBDAVocabulary.IS_TRUE, expr);
				} 
				else {
					return expr;
				}
			}
			if (p == OBDAVocabulary.XSD_INTEGER) {
				if (expr.getTerm(0) instanceof Constant) {
					ValueConstant value = (ValueConstant) expr.getTerm(0);
					int valueInteger = Integer.parseInt(value.getValue());
					if (valueInteger != 0) {
						return fac.getTrue();
					}
					return fac.getFalse();
				} else if (expr.getTerm(0) instanceof Variable) {
					return fac.getFunctionalTerm(OBDAVocabulary.IS_TRUE, expr);
				} else {
					return expr;
				}
			}
			if (p == OBDAVocabulary.XSD_DOUBLE) {
				if (expr.getTerm(0) instanceof Constant) {
					ValueConstant value = (ValueConstant) expr.getTerm(0);
					double valueD = Double.parseDouble(value.getValue());
					if (valueD > 0) {
						return fac.getTrue();
					}
					return fac.getFalse();
				} else if (expr.getTerm(0) instanceof Variable) {
					return fac.getFunctionalTerm(OBDAVocabulary.IS_TRUE, expr);
				} else {
					return expr;
				}
			}
			if (p == OBDAVocabulary.XSD_STRING) {
				if (expr.getTerm(0) instanceof Constant) {
					ValueConstant value = (ValueConstant) expr.getTerm(0);
					if (value.toString().length() == 0) {
						return fac.getFalse();
					}
					return fac.getTrue();
				} else if (expr.getTerm(0) instanceof Variable) {
					return fac.getFunctionalTerm(OBDAVocabulary.IS_TRUE, expr);
				} else {
					return expr;
				}
			}
			if (p == OBDAVocabulary.RDFS_LITERAL) {
				if (expr.getTerm(0) instanceof Constant) {
					ValueConstant value = (ValueConstant) expr.getTerm(0);
					if (value.toString().length() == 0) {
						return fac.getFalse();
					}
					return fac.getTrue();
				} else if (expr.getTerm(0) instanceof Variable) {
					return fac.getFunctionalTerm(OBDAVocabulary.IS_TRUE, expr);
				} else {
					return expr;
				}
			}
		}
		if (p.getName().toString().equals("QUEST_OBJECT_PROPERTY_ASSERTION")) {
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

	public NewLiteral evalBoolean(Function term) {
		Predicate pred = term.getFunctionSymbol();
		if (pred == OBDAVocabulary.AND) {
			return evalAndOr(term, true);
		} else if (pred == OBDAVocabulary.OR) {
			return evalAndOr(term, false);
		} else if (pred == OBDAVocabulary.EQ) {
			return evalEqNeq(term, true);
		} else if (pred == OBDAVocabulary.GT) {
			return term;
		} else if (pred == OBDAVocabulary.GTE) {
			return term;
		} else if (pred == OBDAVocabulary.IS_NOT_NULL) {
			return evalIsNullNotNull(term, false);
		} else if (pred == OBDAVocabulary.IS_NULL) {
			return evalIsNullNotNull(term, true);
		} else if (pred == OBDAVocabulary.LT) {
			return term;
		} else if (pred == OBDAVocabulary.LTE) {
			return term;
		} else if (pred == OBDAVocabulary.NEQ) {
			return evalEqNeq(term, false);
		} else if (pred == OBDAVocabulary.NOT) {
			return evalNot(term);
		} else if (pred == OBDAVocabulary.IS_TRUE) {
			return evalIsTrue(term);
		} else if (pred == OBDAVocabulary.SPARQL_IS_LITERAL) {
			return evalIsLiteral(term);
		} else if (pred == OBDAVocabulary.SPARQL_IS_BLANK) {
			return evalIsBlank(term);
		} else if (pred == OBDAVocabulary.SPARQL_IS_URI) {
			return evalIsUri(term);
		} else if (pred == OBDAVocabulary.SPARQL_IS_IRI) {
			return evalIsIri(term);
		} else if (pred == OBDAVocabulary.SPARQL_LANGMATCHES) {
			return evalLangMatches(term);
		} else if (pred == OBDAVocabulary.SPARQL_REGEX) {
			return evalRegex(term);
		} else {
			throw new RuntimeException(
					"Evaluation of expression not supported: "
							+ term.toString());
		}
	}

	private NewLiteral evalNonBoolean(Function term) {
		Predicate pred = term.getFunctionSymbol();
		if (pred == OBDAVocabulary.SPARQL_STR) {
			return evalStr(term);
		} else if (pred == OBDAVocabulary.SPARQL_DATATYPE) {
			return evalDatatype(term);
		} else if (pred == OBDAVocabulary.SPARQL_LANG) {
			return evalLang(term);
		} else {
			throw new RuntimeException(
					"Evaluation of expression not supported: "
							+ term.toString());
		}
	}

	private NewLiteral evalNumericalOperation(Function term) {
		Function returnedDatatype = (Function) getDatatype(term.getFunctionSymbol(), term);
		if (returnedDatatype != null && isNumeric((ValueConstant) returnedDatatype.getTerm(0))) {
			Predicate pred = term.getFunctionSymbol();
			if (pred == OBDAVocabulary.ADD
				 || pred == OBDAVocabulary.SUBSTRACT
				 || pred == OBDAVocabulary.MULTIPLY) {
				return term;
			} else {
				throw new RuntimeException(
						"Evaluation of expression not supported: "
								+ term.toString());
			}
		} else {
			return fac.getFalse();
		}
	}

	/*
	 * Expression evaluator for isLiteral() function
	 */
	private NewLiteral evalIsLiteral(Function term) {
		NewLiteral innerTerm = term.getTerm(0);
		if (innerTerm instanceof Function) {
			Function function = (Function) innerTerm;
			Predicate predicate = function.getFunctionSymbol();
			if (predicate instanceof DataTypePredicate) {
				return fac.getTrue();
			} else {
				return fac.getFalse();
			}
		} else {
			return term;
		}
	}

	/*
	 * Expression evaluator for isBlank() function
	 */
	private NewLiteral evalIsBlank(Function term) {
		NewLiteral teval = eval(term.getTerm(0));
		if (teval instanceof Function) {
			Function function = (Function) teval;
			Predicate predicate = function.getFunctionSymbol();
			if (predicate instanceof BNodePredicate) {
				return fac.getTrue();
			} else {
				return fac.getFalse();
			}
		}
		return term;
	}

	/*
	 * Expression evaluator for isURI() function
	 */
	private NewLiteral evalIsUri(Function term) {
		NewLiteral teval = eval(term.getTerm(0));
		if (teval instanceof Function) {
			Function function = (Function) teval;
			Predicate predicate = function.getFunctionSymbol();
			if (predicate instanceof URITemplatePredicate) {
				return fac.getTrue();
			} else {
				return fac.getFalse();
			}
		}
		return term;
	}

	/*
	 * Expression evaluator for isIRI() function
	 */
	private NewLiteral evalIsIri(Function term) {
		NewLiteral teval = eval(term.getTerm(0));
		if (teval instanceof Function) {
			Function function = (Function) teval;
			Predicate predicate = function.getFunctionSymbol();
			if (predicate instanceof URITemplatePredicate) {
				return fac.getTrue();
			} else {
				return fac.getFalse();
			}
		}
		return term;
	}

	/*
	 * Expression evaluator for str() function
	 */
	private NewLiteral evalStr(Function term) {
		NewLiteral innerTerm = term.getTerm(0);
		if (innerTerm instanceof Function) {
			Function function = (Function) innerTerm;
			Predicate predicate = function.getFunctionSymbol();
			NewLiteral parameter = function.getTerm(0);
			if (predicate instanceof DataTypePredicate) {
				String datatype = predicate.toString();
				if (datatype.equals(OBDAVocabulary.RDFS_LITERAL_URI)) {
					return fac.getFunctionalTerm(
							fac.getDataTypePredicateString(),
							fac.getVariable(parameter.toString()));
				} else if (datatype.equals(OBDAVocabulary.XSD_STRING_URI)) {
					return fac.getFunctionalTerm(
							fac.getDataTypePredicateString(),
							fac.getVariable(parameter.toString()));
				} else {
					return fac.getFunctionalTerm(
							fac.getDataTypePredicateString(),
							fac.getFunctionalTerm(
									OBDAVocabulary.QUEST_CAST,
									fac.getVariable(parameter.toString()),
									fac.getValueConstant(OBDAVocabulary.XSD_STRING_URI)));
				}
			} else if (predicate instanceof URITemplatePredicate) {
				return fac.getFunctionalTerm(fac.getDataTypePredicateLiteral(), function.clone());
			} else if (predicate instanceof BNodePredicate) {
				return fac.getNULL();
			}
		}
		return term;
	}

	/*
	 * Expression evaluator for datatype() function
	 */
	private NewLiteral evalDatatype(Function term) {
		NewLiteral innerTerm = term.getTerm(0);
		if (innerTerm instanceof Function) {
			Function function = (Function) innerTerm;
			Predicate predicate = function.getFunctionSymbol();
			return getDatatype(predicate, term.getTerm(0));
		}
		return term;
	}
	
	private NewLiteral getDatatype(Predicate predicate, NewLiteral lit)
	{
		if (predicate instanceof DataTypePredicate) {
			return fac.getFunctionalTerm(fac.getUriTemplatePredicate(1),
					fac.getValueConstant(predicate.toString(),
							COL_TYPE.OBJECT));
		} else if (predicate instanceof BNodePredicate) {
			return null;
		} else if (predicate instanceof URITemplatePredicate) {
			return null;
		} else if (predicate instanceof AlgebraOperatorPredicate){
			return fac.getFunctionalTerm(fac.getUriTemplatePredicate(1),
					fac.getValueConstant(OBDAVocabulary.XSD_BOOLEAN_URI,
							COL_TYPE.OBJECT));
		} else if (predicate instanceof OperationPredicate){
			if (predicate instanceof BooleanOperationPredicate)
			{
				//return boolean uri
				return fac.getFunctionalTerm(fac.getUriTemplatePredicate(1),
						fac.getValueConstant(OBDAVocabulary.XSD_BOOLEAN_URI,
								COL_TYPE.OBJECT));
			}
			else if (predicate instanceof NumericalOperationPredicate)
			{
				//return numerical if arguments have same type
				if (lit instanceof Function) {
					Function func = (Function) lit;
					NewLiteral arg1 = func.getTerm(0);
					Predicate pred1 = getDatatypePredicate(arg1);
					NewLiteral arg2 = func.getTerm(1);
					Predicate pred2 = getDatatypePredicate(arg2);
					if (pred1.equals(pred2) || (isDouble(pred1) && isNumeric(pred2))) {
						return fac.getFunctionalTerm(fac.getUriTemplatePredicate(1),
								fac.getValueConstant(pred1.toString(),
										COL_TYPE.OBJECT));
					} else if (isNumeric(pred1) && isDouble(pred2)) {
						return fac.getFunctionalTerm(fac.getUriTemplatePredicate(1), 
								fac.getValueConstant(pred2.toString(),
										COL_TYPE.OBJECT));
					} else {
						return null;
					}
				}
				else
				{
					return null;
				}
			}
		} else if (predicate instanceof NonBooleanOperationPredicate){
			return null;
		}
		return null;
	}
	
	private Predicate getDatatypePredicate(NewLiteral term) {
		if (term instanceof Function) {
			Function function = (Function) term;
			return function.asAtom().getFunctionSymbol();
		} else if (term instanceof Constant) {
			Constant constant = (Constant) term;
			COL_TYPE type = constant.getType();
			switch(type) {
				case OBJECT:
				case STRING:
					return OBDAVocabulary.XSD_STRING;
				case LITERAL:
				case LITERAL_LANG:
					return OBDAVocabulary.RDFS_LITERAL;
				case INTEGER:
					return OBDAVocabulary.XSD_INTEGER;
				case DECIMAL:
					return OBDAVocabulary.XSD_DECIMAL;
				case DOUBLE:
					return OBDAVocabulary.XSD_DOUBLE;
				case DATETIME:
					return OBDAVocabulary.XSD_DATETIME;
				case BOOLEAN:
					return OBDAVocabulary.XSD_BOOLEAN;
				default:
					// For other data types
					return OBDAVocabulary.XSD_STRING;
			}
		} else {
			throw new RuntimeException("Unknown term type");
		}
	}
	
	private boolean isDouble(Predicate pred) {
		return pred.equals(OBDAVocabulary.XSD_DOUBLE);
	}
	
	private boolean isNumeric(Predicate pred) {
		return (pred.equals(OBDAVocabulary.XSD_INTEGER) || pred.equals(OBDAVocabulary.XSD_DECIMAL) || pred.equals(OBDAVocabulary.XSD_DOUBLE));
	}
	
	private boolean isNumeric(ValueConstant constant) {
		String constantValue = constant.getValue();
		return (constantValue.equals(OBDAVocabulary.XSD_INTEGER_URI) 
				|| constantValue.equals(OBDAVocabulary.XSD_DECIMAL_URI) 
				|| constantValue.equals(OBDAVocabulary.XSD_DOUBLE_URI));
	}

	/*
	 * Expression evaluator for lang() function
	 */
	private NewLiteral evalLang(Function term) {
		NewLiteral innerTerm = term.getTerm(0);

		// Create a default return constant: blank language with literal type.
		NewLiteral emptyconstant = fac.getFunctionalTerm(
				fac.getDataTypePredicateString(), fac.getValueConstant("", COL_TYPE.STRING));

		if (!(innerTerm instanceof Function)) {
			return emptyconstant;
		} 
		Function function = (Function) innerTerm;
		Predicate predicate = function.getFunctionSymbol();

		if (!(predicate instanceof DataTypePredicate)) {
			return null;
		}

		String datatype = predicate.toString();
		if (!(datatype.equals(OBDAVocabulary.RDFS_LITERAL_URI))) {
			return emptyconstant;
		}

		if (function.getTerms().size() != 2) {
			return emptyconstant;
		} else {
			NewLiteral parameter = function.getTerm(1);
			if (parameter instanceof Variable) {
				return fac.getFunctionalTerm(fac.getDataTypePredicateString(),
						parameter.clone());
			} else if (parameter instanceof Constant) {
				return fac.getFunctionalTerm(fac.getDataTypePredicateString(),
						fac.getValueConstant(((Constant) parameter).getValue(),COL_TYPE.STRING));
			}
		}
		return term;
	}

	/*
	 * Expression evaluator for langMatches() function
	 */
	private NewLiteral evalLangMatches(Function term) {
		final String SELECT_ALL = "*";
		
		/*
		 * Evaluate the first term
		 */
		NewLiteral teval1 = eval(term.getTerm(0));
		if (teval1 == null) {
			return fac.getFalse();
		}
		/*
		 * Evaluate the second term
		 */
		NewLiteral innerTerm2 = term.getTerm(1);
		if (innerTerm2 == null) {
			return fac.getFalse();
		}

		/*
		 * Term checks
		 */
		if (teval1 instanceof Constant && innerTerm2 instanceof Constant) {
			String lang1 = ((Constant) teval1).getValue();
			String lang2 = ((Constant) innerTerm2).getValue();	
			if (lang2.equals(SELECT_ALL)) {
				if (lang1.isEmpty()) {
					return fac.getIsNullFunction(teval1);
				} else {
					return fac.getIsNotNullFunction(teval1);
				}
			} else {
				if (lang1.equals(lang2)) {
					return fac.getTrue();
				} else {
					return fac.getFalse();
				}
			}
		} else if (teval1 instanceof Variable && innerTerm2 instanceof Constant) {
			Variable var = (Variable) teval1;
			Constant lang = (Constant) innerTerm2;
			if (lang.getValue().equals(SELECT_ALL)) {
				// The char * means to get all languages
				return fac.getIsNotNullFunction(var);
			} else {
				return fac.getEQFunction(var, lang);
			}
		} else if (teval1 instanceof Function && innerTerm2 instanceof Function) {
			Function f1 = (Function) teval1;
			Function f2 = (Function) innerTerm2;
			return evalLangMatches(fac.getLANGMATCHESFunction(f1.getTerm(0), 
					f2.getTerm(0)));
		} else {
			return term;
		}
	}

	private NewLiteral evalRegex(Function term) {
		NewLiteral innerTerm = term.getTerm(0);
		if (innerTerm instanceof Function) {
			Function f = (Function) innerTerm;
			Predicate functionSymbol = f.getFunctionSymbol();
			if (isObjectConstant()) {
				setRegexFlag(false);
				if (functionSymbol.equals(OBDAVocabulary.SPARQL_STR)) {
					return fac.getTrue();
				} else {
					return fac.getFalse();
				}
			}
		}
		return term;
	}

	public NewLiteral evalIsNullNotNull(Function term, boolean isnull) {
		NewLiteral innerTerm = term.getTerms().get(0);
		if (innerTerm instanceof Function) {
			Function f = (Function) innerTerm;
			if (f.isDataTypeFunction()) return innerTerm;
		}
		NewLiteral result = eval(innerTerm);
		if (result == OBDAVocabulary.NULL) {
			if (isnull) {
				return fac.getTrue();
			} else {
				return fac.getFalse();
			}
		} else if (result instanceof Constant) {
			if (!isnull) {
				return fac.getTrue();
			} else {
				return fac.getFalse();
			}
		}

		// TODO improve evaluation of is (not) null
		/*
		 * This can be inproved by evaluating some of the function, e.g,. URI
		 * and Bnodes never return null
		 */
		if (isnull) {
			return fac.getIsNullFunction(result);
		} else {
			return fac.getIsNotNullFunction(result);
		}
	}

	private NewLiteral evalIsTrue(Function term) {
		NewLiteral teval = eval(term.getTerm(0));
		if (teval instanceof Function) {
			Function f = (Function) teval;
			Predicate predicate = f.getFunctionSymbol();
			if (predicate == OBDAVocabulary.IS_NOT_NULL) {
				return fac.getIsNotNullFunction(f.getTerm(0));
			} else if (predicate == OBDAVocabulary.IS_NULL) {
				return fac.getIsNullFunction(f.getTerm(0));
			} else if (predicate == OBDAVocabulary.NEQ) {
				return fac.getNEQFunction(f.getTerm(0), f.getTerm(1));
			} else if (predicate == OBDAVocabulary.EQ) {
				return fac.getEQFunction(f.getTerm(0), f.getTerm(1));
			}
		} else if (teval instanceof Constant) {
			return teval;
		}
		return term;
	}
	
	
	private NewLiteral evalNot(Function term) {
		NewLiteral teval = eval(term.getTerm(0));
		if (teval instanceof Function) {
			Function f = (Function) teval;
			Predicate predicate = f.getFunctionSymbol();
			if (predicate == OBDAVocabulary.IS_NOT_NULL) {
				return fac.getIsNullFunction(f.getTerm(0));
			} else if (predicate == OBDAVocabulary.IS_NULL) {
				return fac.getIsNotNullFunction(f.getTerm(0));
			} else if (predicate == OBDAVocabulary.NEQ) {
				return fac.getEQFunction(f.getTerm(0), f.getTerm(1));
			} else if (predicate == OBDAVocabulary.EQ) {
				return fac.getNEQFunction(f.getTerm(0), f.getTerm(1));
			}
		} else if (teval instanceof Constant) {
			return teval;
		}
		return term;
	}

	public NewLiteral evalEqNeq(Function term, boolean eq) {
		/*
		 * Evaluate the first term
		 */
		
		// Do not eval if term is DataTypeFunction, e.g. integer(10)
		NewLiteral teval1 = null;
		NewLiteral teval2 = null;
		
		if (term.getTerm(0) instanceof Function) {
			Function t1 = (Function) term.getTerm(0);
			Predicate p1 = t1.getFunctionSymbol();
			if (!(p1 instanceof DataTypePredicate)) {
				teval1 = eval(term.getTerm(0));
				if (teval1 == null) {
					return fac.getFalse();
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

		if (term.getTerm(1) instanceof Function) {
			Function t2 = (Function) term.getTerm(1);
			Predicate p2 = t2.getFunctionSymbol();
			if (!(p2 instanceof DataTypePredicate)) {
				teval2 = eval(term.getTerm(1));
				if (teval2 == null) {
					return fac.getFalse();
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
		NewLiteral eval1 = teval1 instanceof Function ? teval1 : teval2;
		NewLiteral eval2 = teval1 instanceof Function ? teval2 : teval1;

		if (eval1 instanceof Variable || eval2 instanceof Variable) {
			// no - op
		} else if (eval1 instanceof Constant && eval2 instanceof Constant) {
			if (eval1.equals(eval2)) {
				if (eq) {
					return fac.getTrue();
				} else {
					return fac.getFalse();
				}
			} else if (eq) {
				return fac.getFalse();
			} else {
				return fac.getTrue();
			}
		} else if (eval1 instanceof Function) {
			Function f1 = (Function) eval1;
			Predicate pred1 = f1.getFunctionSymbol();
			
			if (pred1 instanceof DataTypePredicate) {
				if (pred1.getType(0) == COL_TYPE.UNSUPPORTED) {
					throw new RuntimeException("Unsupported type: " + pred1);
				}
			} else if (pred1 instanceof NumericalOperationPredicate) {
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
				if (pred1 == OBDAVocabulary.RDFS_LITERAL
						&& pred2 == OBDAVocabulary.RDFS_LITERAL) {
					/*
					 * Special code to handle quality of Literals (plain, and
					 * with language)
					 */
					if (f1.getTerms().size() != f2.getTerms().size()) {
						// case one is with language another without
						if (eq) {
							return fac.getFalse();
						} else {
							return fac.getTrue();
						}
					} else if (f1.getTerms().size() == 2) {
						// SIZE == 2
						// these are literals with languages, we need to
						// return the evaluation of the values and the
						// languages case literals without language, its
						// exactly as normal datatypes.
						// This is copy paste code
						Function eqValues = null;
						Function eqLang = null;
						Function comparison = null;
						if (eq) {
							eqValues = fac.getEQFunction(f1.getTerm(0), f2.getTerm(0));
							eqLang = fac.getEQFunction(f1.getTerm(1), f2.getTerm(1));
							comparison = fac.getANDFunction(eqValues, eqLang);
							return evalAndOr(comparison, true);
						}
						eqValues = fac.getNEQFunction(f1.getTerm(0), f2.getTerm(0));
						eqLang = fac.getNEQFunction(f1.getTerm(1), f2.getTerm(1));
						comparison = fac.getORFunction(eqValues, eqLang);
						return evalAndOr(comparison, false);
					}
					// case literals without language, its exactly as normal
					// datatypes
					// this is copy paste code
					Function neweq = null;
					if (eq) {
						neweq = fac.getEQFunction(f1.getTerm(0), f2.getTerm(0));
						return evalEqNeq(neweq, true);
					} else {
						neweq = fac.getNEQFunction(f1.getTerm(0), f2.getTerm(0));
						return evalEqNeq(neweq, false);
					}
				} else if (pred1.equals(pred2)) {
					Function neweq = null;
					if (pred1 instanceof URITemplatePredicate) {
						return evalUriTemplateEqNeq(f1, f2, eq);
					} else {
						if (eq) {
							neweq = fac.getEQFunction(f1.getTerm(0), f2.getTerm(0));
							return evalEqNeq(neweq, true);
						} else {
							neweq = fac.getNEQFunction(f1.getTerm(0), f2.getTerm(0));
							return evalEqNeq(neweq, false);
						}
					}
				} else if (!pred1.equals(pred2)) {
					if (eq) {
						return fac.getFalse();
					} else {
						return fac.getTrue();
					}
				} else {
					return term;
				}
			}
		}

		/* eval2 is not a function */
		if (eq) {
			return fac.getEQAtom(eval1, eval2);
		} else {
			return fac.getNEQAtom(eval1, eval2);
		}
	}

	private NewLiteral evalUriTemplateEqNeq(Function uriFunction1, Function uriFunction2, boolean isEqual) {
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
	
	private NewLiteral evalUriFunctionsWithSingleTerm(Function uriFunction1, Function uriFunction2, boolean isEqual) {
		NewLiteral term1 = uriFunction1.getTerm(0);
		NewLiteral term2 = uriFunction2.getTerm(0);
		if (!(term1 instanceof ValueConstant)) {
			return null;
		}
		if (term2 instanceof Variable) {
			if (isEqual) {
				return fac.getEQAtom(term2, term1);
			} else {
				return fac.getNEQAtom(term2, term1);
			}
		} else if (term2 instanceof ValueConstant) {
			if (term1.equals(term2)) {
				if (isEqual) {
					return fac.getTrue();
				} else {
					return fac.getFalse();
				}
			} else {
				if (isEqual) {
					return fac.getFalse();
				} else {
					return fac.getTrue();
				}
			}
		}
		return null;
	}

	private NewLiteral evalUriFunctionsWithMultipleTerms(Function uriFunction1, Function uriFunction2, boolean isEqual) {
		Map<Variable, NewLiteral> theta = Unifier.getMGU(uriFunction1, uriFunction2);
		if (theta == null) {
			if (isEqual) {
				return fac.getTrue();
			} else {
				return fac.getFalse();
			}
		} else {
			boolean isEmpty = (theta.size() == 0);
			if (isEmpty) {
				if (isEqual) {
					return fac.getFalse();
				} else {
					return fac.getTrue();
				}
			} else {
				Function result = null;
				List<Function> temp = new ArrayList<Function>();
				Set<Variable> keys = theta.keySet();
				for (Variable var : keys) {
					result = createEqNeqFilter(var, theta.get(var), isEqual);
					temp.add(result);
					if (temp.size() == 2) {
						result = createAndFilter(temp.get(0), temp.get(1));
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
		return uriTemplateMatcher.generateURIFunction(OBDADataFactoryImpl.getIRI(uriString.getValue()));
	}
	
	public Function createEqNeqFilter(Variable var, NewLiteral value, boolean isEqual) {
		if (isEqual) {
			return fac.getEQFunction(var, value);
		} else {
			return fac.getNEQFunction(var, value);
		}
	}
		
	private Function createAndFilter(Function function1, Function function2) {
		return fac.getANDFunction(function1, function2);
	}

	public NewLiteral evalAndOr(Function term, boolean and) {
		NewLiteral teval1 = eval(term.getTerm(0));
		NewLiteral teval2 = eval(term.getTerm(1));

		/*
		 * Normalizing the location of terms, constants first
		 */
		NewLiteral eval1 = teval1 instanceof Constant ? teval1 : teval2;
		NewLiteral eval2 = teval1 instanceof Constant ? teval2 : teval1;

		/*
		 * Implementing boolean logic
		 */
		if (eval1 == fac.getTrue()) {
			if (eval2 == fac.getTrue()) {
				if (and) {
					return fac.getTrue();
				} else {
					return fac.getFalse();
				}
			} else if (eval2 == fac.getFalse()) {
				if (and) {
					return fac.getFalse();
				} else {
					return fac.getTrue();
				}
			} else if (and) {
				/* if its an and we still need to evaluate eval2 */
				return eval2;
			} else {
				/*
				 * Its an Or, and the first was true, so it doesn't matter whats
				 * next.
				 */
				return fac.getTrue();
			}

		} else if (eval1 == fac.getFalse()) {
			if (eval2 == fac.getTrue()) {
				if (and) {
					return fac.getFalse();
				} else {
					return fac.getTrue();
				}
			} else if (eval2 == fac.getFalse()) {
				if (and) {
					return fac.getFalse();
				} else {
					return fac.getFalse();
				}
			} else if (and) {
				/*
				 * Its an And, and the first was false, so it doesn't matter
				 * whats next.
				 */
				return fac.getFalse();
			} else {
				return eval2;
			}
		}
		/*
		 * None of the subnodes evaluated to true or false, we have functions
		 * that need to be evaluated
		 */
		// TODO check if we can further optimize this
		if (and) {
			return fac.getANDAtom(eval1, eval2);
		} else {
			return fac.getORAtom(eval1, eval2);
		}
	}
}
