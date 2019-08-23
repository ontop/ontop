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

import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.datalog.impl.DatalogTools;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.NumericRDFDatatype;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.OntopInternal;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.impl.UnifierUtilities;
import org.apache.commons.rdf.api.RDF;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.*;


/**
 * WARNING: NOT immutable!!!!!
 */
public class ExpressionEvaluator {

	private final DatalogTools datalogTools;
	private final TermFactory termFactory;
	private final TypeFactory typeFactory;
	private final ValueConstant valueFalse;
	private final ValueConstant valueTrue;
	private final ValueConstant valueNull;
	private final UnifierUtilities unifierUtilities;
	private final ExpressionNormalizer normalizer;
	private final ImmutabilityTools immutabilityTools;
	private final RDF rdfFactory;

	@Inject
	private ExpressionEvaluator(DatalogTools datalogTools, TermFactory termFactory, TypeFactory typeFactory,
								UnifierUtilities unifierUtilities, ExpressionNormalizer normalizer,
								ImmutabilityTools immutabilityTools, RDF rdfFactory) {
		this.termFactory = termFactory;
		this.typeFactory = typeFactory;
		this.datalogTools = datalogTools;
		valueFalse = termFactory.getBooleanConstant(false);
		valueTrue = termFactory.getBooleanConstant(true);
		valueNull = termFactory.getNullConstant();
		this.unifierUtilities = unifierUtilities;
		this.normalizer = normalizer;
		this.immutabilityTools = immutabilityTools;
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
						.map(termFactory::getBooleanConstant)
						.orElseGet(termFactory::getNullConstant);
		}
	}

	public EvaluationResult evaluateExpression(ImmutableExpression expression) {
		Expression mutableExpression = immutabilityTools.convertToMutableBooleanExpression(expression);

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

			return new EvaluationResult(termFactory.getImmutableExpression(
					termFactory.getExpression((OperationPredicate) predicate,
							evaluatedFunctionalTerm.getTerms())), normalizer, termFactory);
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
		    return new EvaluationResult(termFactory.getImmutableExpression(ExpressionOperation.IS_TRUE,
                    immutabilityTools.convertIntoImmutableTerm(evaluatedTerm)), normalizer, termFactory);
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
						if (newterm == valueFalse) {
							//
							terms.set(i, termFactory.getTypedTerm(valueFalse, typeFactory.getXsdBooleanDatatype()));
						} else if (newterm == valueTrue) {
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

				RDFDatatype datatype = value.getType();

				if (datatype.isA(XSD.BOOLEAN)) { // OBDAVocabulary.XSD_BOOLEAN
					if (valueString.equals("true") || valueString.equals("1")) {
						return valueTrue;
					} 
					else if (valueString.equals("false") || valueString.equals("0")) {
						return valueFalse;
					}
				}
				else if (isNumeric(p)) {
					BigDecimal valueDecimal = new BigDecimal(valueString);
					return termFactory.getBooleanConstant(!valueDecimal.equals(BigDecimal.ZERO));
				}
				else if (datatype.isA(XSD.STRING)) {
					// ROMAN (18 Dec 2015): toString() was wrong -- it contains "" and so is never empty
					return termFactory.getBooleanConstant(valueString.length() != 0);
				}
				// TODO (R): year, date and time are not covered?
			} 
			else if (t0 instanceof Variable) {
				return termFactory.getFunctionIsTrue(expr);
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
				return valueFalse;
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
			case IS_NUMERIC:
				return evalIsNumeric(term);
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
			case IF_ELSE_NULL:
				return evalIfElseNull(term);
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
	 * Expression evaluator for isNumeric() function
	 */

	private Term evalIsNumeric(Function term) {
		Term innerTerm = term.getTerm(0);
		if (innerTerm instanceof Function) {
			Function function = (Function) innerTerm;
			return termFactory.getBooleanConstant(function.isDataTypeFunction() && isNumeric(function.getFunctionSymbol()));
		}
		else {
			return term;
		}
	}

	/*
	 * Expression evaluator for isLiteral() function
	 */
	private Term evalIsLiteral(Function term) {
		Term innerTerm = term.getTerm(0);
		if (innerTerm instanceof Function) {
			Function function = (Function) innerTerm;
			return termFactory.getBooleanConstant(function.isDataTypeFunction());
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
			return termFactory.getBooleanConstant(predicate instanceof BNodePredicate);
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
			return termFactory.getBooleanConstant(predicate instanceof URITemplatePredicate);
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
				if (isXsdString(predicate) ) { // R: was datatype.equals(OBDAVocabulary.RDFS_LITERAL_URI)
					return termFactory.getTypedTerm(
							termFactory.getVariable(parameter.toString()), typeFactory.getXsdStringDatatype());
				} 

				else {
					return termFactory.getTypedTerm(
							termFactory.getFunctionCast(termFactory.getVariable(parameter.toString()),
									termFactory.getConstantLiteral(typeFactory.getXsdStringDatatype().getIRI().getIRIString())),
										typeFactory.getXsdStringDatatype());
				}
			} 
			else if (predicate instanceof URITemplatePredicate) {
				return termFactory.getTypedTerm(function.clone(), typeFactory.getXsdStringDatatype());
			} 
			else if (predicate instanceof BNodePredicate) {
				return valueNull;
			}
		}
		return term;
	}

	private static boolean isXsdString(Predicate predicate) {
		return (predicate instanceof DatatypePredicate)
				&& ((DatatypePredicate) predicate).getReturnedType().isA(XSD.STRING);
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
			return termFactory.getUriTemplateForDatatype(predicate.toString());
		} 
		else if (predicate instanceof BNodePredicate) {
			return null;
		} 
		else if (predicate instanceof URITemplatePredicate) {
			return null;
		} 
		else if (function.isAlgebraFunction()) {
			return termFactory.getUriTemplateForDatatype(typeFactory.getXsdBooleanDatatype().getIRI().getIRIString());
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
				return termFactory.getUriTemplateForDatatype(pred1.toString());
			} 
			else if (isNumeric(pred1) && isDouble(pred2)) {
				return termFactory.getUriTemplateForDatatype(pred2.toString());
			} 
			else {
				return null;
			}
		}
		else if (function.isOperation()) {
			//return boolean uri
			return termFactory.getUriTemplateForDatatype(XSD.BOOLEAN.getIRIString());
		}
		return null;
	}
	
	private Predicate getDatatypePredicate(Term term) {
		if (term instanceof Function) {
			Function function = (Function) term;
			return function.getFunctionSymbol();
		} 
		else if (term instanceof ValueConstant) {
			ValueConstant constant = (ValueConstant) term;
			RDFDatatype type = constant.getType();
			Predicate pred = termFactory.getRequiredTypePredicate(type);
			if (pred == null)
				pred = termFactory.getRequiredTypePredicate(XSD.STRING); // .XSD_STRING;
			return pred;
		} 
		else {
			throw new RuntimeException("Unexpected term type: " + term);
		}
	}
	
	private boolean isDouble(Predicate pred) {
		return (pred.equals(termFactory.getRequiredTypePredicate(XSD.DOUBLE))
				|| pred.equals(termFactory.getRequiredTypePredicate(XSD.FLOAT)));
	}
	
	private boolean isNumeric(Predicate pred) {
		return (pred instanceof DatatypePredicate)
				&& (((DatatypePredicate) pred).getReturnedType() instanceof NumericRDFDatatype);
	}
	
	private boolean isNumeric(ValueConstant constant) {
		String constantValue = constant.getValue();
		RDFDatatype type = typeFactory.getDatatype(rdfFactory.createIRI(constantValue));
		return type.isA(OntopInternal.NUMERIC);
	}

	/*
	 * Expression evaluator for lang() function
	 */
	private Term evalLang(Function term) {
		Term innerTerm = term.getTerm(0);

		// Create a default return constant: blank language with literal type.
		// TODO: avoid this constant wrapping thing
		Function emptyString = termFactory.getTypedTerm(
				termFactory.getConstantLiteral("", XSD.STRING), XSD.STRING);

        if (innerTerm instanceof Variable) {
            return term;
        }
		/*
		 * TODO: consider the case of constants
		 */
		if (!(innerTerm instanceof Function)) {
			return emptyString;
		}
		Function function = (Function) innerTerm;

		if (!function.isDataTypeFunction()) {
			return null;
		}

		Predicate predicate = function.getFunctionSymbol();
		if (predicate instanceof DatatypePredicate) {
			RDFDatatype datatype = ((DatatypePredicate) predicate).getReturnedType();

			return datatype.getLanguageTag()
					// TODO: avoid this constant wrapping thing
					.map(tag -> termFactory.getTypedTerm(
							termFactory.getConstantLiteral(tag.getFullString(), XSD.STRING),
							XSD.STRING))
					.orElse(emptyString);

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
			return valueNull; // ROMAN (10 Jan 2017): not valueFalse
		}
		/*
		 * Evaluate the second term
		 */
		Term innerTerm2 = term.getTerm(1);
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
					return termFactory.getFunctionIsNull(teval1);
				else 
					return termFactory.getFunctionIsNotNull(teval1);
			} 
			else {
				return termFactory.getBooleanConstant(lang1.equals(lang2));
			}
		} 
		else if (teval1 instanceof Variable && innerTerm2 instanceof Constant) {
			Variable var = (Variable) teval1;
			Constant lang = (Constant) innerTerm2;
			if (lang.getValue().equals(SELECT_ALL)) {
				// The char * means to get all languages
				return termFactory.getFunctionIsNotNull(var);
			} else {
				return termFactory.getFunctionEQ(var, lang);
			}
		} 
		else if (teval1 instanceof Function && innerTerm2 instanceof Function) {
			Function f1 = (Function) teval1;
			Function f2 = (Function) innerTerm2;
			if(f1.isOperation()){
				return term;
			}
			return evalLangMatches(termFactory.getLANGMATCHESFunction(f1.getTerm(0),
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

        if(eval1.equals(valueFalse)
                || eval2.equals(valueFalse)
                || eval3.equals(valueFalse))
        {
            return valueFalse;
        }

        return termFactory.getFunction(term.getFunctionSymbol(), eval1, eval2, term.getTerm(2));

	}

	private Term evalRegexSingleExpression(Term expr){

        if (expr instanceof Function) {
            Function function1 = (Function) expr;
            Predicate predicate1 = function1.getFunctionSymbol();
            if((predicate1 instanceof URITemplatePredicate)
                    ||(predicate1 instanceof BNodePredicate)) {
                return valueFalse;
            }
            if (!function1.isDataTypeFunction()){
				Term evaluatedExpression = eval(expr);
				return expr.equals(evaluatedExpression)
						? expr
						: evalRegexSingleExpression(evaluatedExpression);
            }
        }
        return expr;

    }

	private Term evalIfElseNull(Function term) {
		Term formerCondition = term.getTerm(0);
		Term newCondition = eval(formerCondition);
		if (newCondition.equals(formerCondition))
			return term;
		else if (newCondition.equals(valueFalse))
			return valueNull;
		else if (newCondition.equals(valueTrue))
			return term.getTerm(1);
		else
			return termFactory.getFunction(term.getFunctionSymbol(), newCondition, term.getTerm(1));
	}

	private Term evalIsNullNotNull(Function term, boolean isnull) {
		Term innerTerm = term.getTerms().get(0);
		if (innerTerm instanceof Function) {
			Function f = (Function) innerTerm;
			if (f.isDataTypeFunction()) {
				Function isNotNullInnerInnerTerm = termFactory.getFunction(ExpressionOperation.IS_NOT_NULL,
						((Function) innerTerm).getTerms());
				return evalIsNullNotNull(isNotNullInnerInnerTerm , isnull);
			}
		}
		Term result = eval(innerTerm);
		if (result == valueNull) {
			return termFactory.getBooleanConstant(isnull);
		} 
		else if (result instanceof Constant) {
			return termFactory.getBooleanConstant(!isnull);
		}

		if (result instanceof Function) {
			Function functionalTerm = (Function) result;
			Predicate functionSymbol = functionalTerm.getFunctionSymbol();
			/*
			 * Special optimization for URI templates
			 */
			if (functionSymbol instanceof URITemplatePredicate) {
				return simplifyIsNullorNotNullUriTemplate(functionalTerm, isnull);
			}
			/*
			 * All the functions that accepts null
			 * TODO: add COALESCE
			 */
			else if (functionSymbol != IS_NULL
					&& functionSymbol != IS_NOT_NULL
					&& functionSymbol != IF_ELSE_NULL) {
				Expression notNullExpression = datalogTools.foldBooleanConditions(
						functionalTerm.getTerms().stream()
								.map(t -> termFactory.getFunction(IS_NOT_NULL, t))
								.collect(Collectors.toList()));
				return eval(isnull
						? termFactory.getFunction(NOT, notNullExpression)
						: notNullExpression);
			}
		}

		// TODO improve evaluation of is (not) null
		/*
		 * This can be improved by evaluating some of the function, e.g,. URI
		 * and Bnodes never return null
		 */
		if (isnull) {
			return termFactory.getFunctionIsNull(result);
		} else {
			return termFactory.getFunctionIsNotNull(result);
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
					return termFactory.getFunctionIsNull(uriTemplate);
				case 1:
					return termFactory.getFunctionIsNull(variables.iterator().next());
				default:
					return variables.stream()
							.reduce(null,
									(e, v) -> e == null
											? termFactory.getFunctionIsNull(v)
											: termFactory.getFunctionOR(e, termFactory.getFunctionIsNull(v)),
									(e1, e2) -> e1 == null
											? e2
											: (e2 == null) ? e1 : termFactory.getFunctionOR(e1, e2));
			}
		}
		else {
			if (variables.isEmpty())
				return termFactory.getFunctionIsNotNull(uriTemplate);
			else
				return datalogTools.foldBooleanConditions(
						variables.stream()
								.map(termFactory::getFunctionIsNotNull)
								.collect(Collectors.toList()));
		}
	}

	private Term evalIsTrue(Function term) {
		Term teval = eval(term.getTerm(0));
		if (teval instanceof Function) {
			Function f = (Function) teval;
			Predicate predicate = f.getFunctionSymbol();
			if (predicate == ExpressionOperation.IS_NOT_NULL) {
				return termFactory.getFunctionIsNotNull(f.getTerm(0));
			} else if (predicate == ExpressionOperation.IS_NULL) {
				return termFactory.getFunctionIsNull(f.getTerm(0));
			} else if (predicate == ExpressionOperation.NEQ) {
				return termFactory.getFunctionNEQ(f.getTerm(0), f.getTerm(1));
			} else if (predicate == ExpressionOperation.EQ) {
				return termFactory.getFunctionEQ(f.getTerm(0), f.getTerm(1));
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
				return termFactory.getFunctionIsNull(f.getTerm(0));
			} else if (predicate == ExpressionOperation.IS_NULL) {
				return termFactory.getFunctionIsNotNull(f.getTerm(0));
			} else if (predicate == ExpressionOperation.NEQ) {
				return termFactory.getFunctionEQ(f.getTerm(0), f.getTerm(1));
			} else if (predicate == ExpressionOperation.EQ) {
				return termFactory.getFunctionNEQ(f.getTerm(0), f.getTerm(1));
			}
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
					return valueFalse;
				}
			} else {
				teval1 = term.getTerm(0);
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

		Term teval2;
		if (term.getTerm(1) instanceof Function) {
			Function t2 = (Function) term.getTerm(1);
			if (!(t2.isDataTypeFunction())) {
				teval2 = eval(term.getTerm(1));
				if (teval2 == null) {
					return valueFalse;
				}
			} else {
				teval2 = term.getTerm(1);
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
		Term eval1 = teval1 instanceof Function ? teval1 : teval2;
		Term eval2 = teval1 instanceof Function ? teval2 : teval1;

		if (eval1 instanceof Variable || eval2 instanceof Variable) {
			// no - op
		} 
		else if (eval1 instanceof Constant && eval2 instanceof Constant) {
			if (eval1.equals(eval2)) 
				return termFactory.getBooleanConstant(eq);
			else 
				return termFactory.getBooleanConstant(!eq);
			
		} 
		else if (eval1 instanceof Function) {
			Function f1 = (Function) eval1;
			Predicate pred1 = f1.getFunctionSymbol();
			
			if (f1.isDataTypeFunction()) {
//				if (pred1.getTermType(0) == COL_TYPE.UNSUPPORTED) {
//					throw new RuntimeException("Unsupported type: " + pred1);
//				}
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
//				if (pred2.getTermType(0) == COL_TYPE.UNSUPPORTED) {
//					throw new RuntimeException("Unsupported type: " + pred2);
//				}

				if (pred1.equals(pred2)) {
					if (pred1 instanceof URITemplatePredicate) {
						return evalUriTemplateEqNeq(f1, f2, eq);
					} 
					else {
						if (eq) {
							Function neweq = termFactory.getFunctionEQ(f1.getTerm(0), f2.getTerm(0));
							return evalEqNeq(neweq, true);
						} 
						else {
							Function neweq = termFactory.getFunctionNEQ(f1.getTerm(0), f2.getTerm(0));
							return evalEqNeq(neweq, false);
						}
					}
				} 
				else if (!pred1.equals(pred2)) {
					/*
					 * TEMPORARY HOT FIX!
					 */
					if ((pred1.equals(SPARQL_LANG) || pred2.equals(SPARQL_LANG))
							&& (isXsdString(pred1) || isXsdString(pred2))) {
						return term;
					}
					else
						return termFactory.getBooleanConstant(!eq);
				} 
				else {
					return term;
				}
			}
		}

		/* eval2 is not a function */
		if (eq) {
			return termFactory.getFunctionEQ(eval1, eval2);
		} else {
			return termFactory.getFunctionNEQ(eval1, eval2);
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
				return termFactory.getBooleanConstant(!isEqual);
			}
		} else if (arityForFunction1 > 1) {
			if (arityForFunction2 == 1) {
				// Currently, we assume the arity should be the same (already decomposed URIs)
				return termFactory.getBooleanConstant(!isEqual);
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
				return termFactory.getFunctionEQ(term2, term1);
			} else {
				if(term1 instanceof ValueConstant){
					if (isEqual)
						return termFactory.getFunctionEQ(term1, term2);
					else
						return termFactory.getFunctionNEQ(term1, term2);
				}
				return termFactory.getFunctionNEQ(term2, term1);
			}

		} else if (term2 instanceof ValueConstant) {

			if (term1.equals(term2))
				return termFactory.getBooleanConstant(isEqual);
			else
				{
				if (term1 instanceof Variable) {
					if (isEqual)
						return termFactory.getFunctionEQ(term1, term2);
					else
						return termFactory.getFunctionNEQ(term1, term2);
				}
				return termFactory.getBooleanConstant(!isEqual);
			}
		}
		return null;
	}

	private Term evalUriFunctionsWithMultipleTerms(Function uriFunction1, Function uriFunction2, boolean isEqual) {
		if (uriFunction1.equals(uriFunction2))
			return termFactory.getBooleanConstant(isEqual);

		Substitution theta = unifierUtilities.getMGU(uriFunction1, uriFunction2);
		if (theta == null) {
			return termFactory.getBooleanConstant(!isEqual);
		} 
		else {
			boolean isEmpty = theta.isEmpty();
			if (isEmpty) {
				return termFactory.getBooleanConstant(!isEqual);
			} 
			else {
				Function result = null;
				List<Function> temp = new ArrayList<>();
				Set<Variable> keys = theta.getMap().keySet();
				for (Variable var : keys) {
					if (isEqual) 
						result = termFactory.getFunctionEQ(var, theta.get(var));
					else 
						result = termFactory.getFunctionNEQ(var, theta.get(var));
					
					temp.add(result);
					if (temp.size() == 2) {
						if (isEqual){
							result = termFactory.getFunctionAND(temp.get(0), temp.get(1));
						}else{
							result = termFactory.getFunctionOR(temp.get(0), temp.get(1));
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
	
		if (e1 == valueFalse || e2 == valueFalse)
			return valueFalse;
		
		if (e1 == valueTrue)
			return e2;
		
		if (e2 == valueTrue)
			return e1;
		
		return termFactory.getFunctionAND(e1, e2);
	}

	private Term evalOr(Term t1, Term t2) {
		Term e1 = eval(t1);
		Term e2 = eval(t2);
	
		if (e1 == valueTrue || e2 == valueTrue)
			return valueTrue;
		
		if (e1 == valueFalse)
			return e2;
		
		if (e2 == valueFalse)
			return e1;
		
		return termFactory.getFunctionOR(e1, e2);
	}

	@Override
	public ExpressionEvaluator clone() {
		return new ExpressionEvaluator(datalogTools, termFactory, typeFactory, unifierUtilities, normalizer,
				immutabilityTools, rdfFactory);
	}
}
