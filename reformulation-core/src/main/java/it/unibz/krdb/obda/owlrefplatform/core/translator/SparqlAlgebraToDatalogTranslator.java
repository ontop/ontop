package it.unibz.krdb.obda.owlrefplatform.core.translator;

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

import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.OBDAQueryModifiers.OrderCondition;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.TermUtils;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.UriTemplateMatcher;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.*;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/***
 * Translate a SPARQL algebra expression into a Datalog program that has the
 * same semantics. We use the built-in predicates Join and Left join. The rules
 * in the program have always 1 or 2 operator atoms, plus (in)equality atoms
 * (due to filters).
 * 
 * This programs needs to be flattened by another procedure later.
 * 
 * @author mariano
 */
public class SparqlAlgebraToDatalogTranslator {

	
	private final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
	
	private final DatatypeFactory dtfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();

	private final UriTemplateMatcher uriTemplateMatcher;
	private final SemanticIndexURIMap uriRef;  
	
	private static final Logger log = LoggerFactory.getLogger(SparqlAlgebraToDatalogTranslator.class);
	
	/**
	 * 
	 * @param templateMatcher
	 * @param uriRef is used only in the Semantic Index mode
	 */
	
	public SparqlAlgebraToDatalogTranslator(UriTemplateMatcher templateMatcher, SemanticIndexURIMap uriRef) {
		uriTemplateMatcher = templateMatcher;
		this.uriRef = uriRef;
	}
	
	/**
	 * Translate a given SPARQL query object to datalog program.
	 * 
	 *
	 *            The Query object.
	 * @return Datalog program that represents the construction of the SPARQL
	 *         query.
	 */

	public DatalogProgram translate(ParsedQuery pq) {
		
		TupleExpr te = pq.getTupleExpr();
		log.debug("SPARQL algebra: \n{}", te);

		List<Term> vars;
		if (pq instanceof ParsedTupleQuery || pq instanceof ParsedGraphQuery) {
			Set<String> signature = te.getBindingNames();
			vars = new ArrayList<>(signature.size());
			for (String vs : signature) 
				vars.add(ofac.getVariable(vs));
		}
		else
			vars = Collections.emptyList(); 		// the signature of ASK queries is EMPTY
		
		DatalogProgram result = ofac.getDatalogProgram();
		Function bodyAtom = translateTupleExpr(te, result, OBDAVocabulary.QUEST_QUERY + "0");
		createRule(result, OBDAVocabulary.QUEST_QUERY, vars, bodyAtom); // appends rule to the result
		
		return result;
	}


	/**
	 * main translation method -- a recursive switch over all possible types of subexpressions
	 * 
	 * @param te
	 * @param pr
	 * @param newHeadName
	 */
	
	private Function translateTupleExpr(TupleExpr te, DatalogProgram pr, String newHeadName) {
		if (te instanceof Slice) {
			// Add LIMIT and OFFSET modifiers, if any
			Slice slice = (Slice)te;
			pr.getQueryModifiers().setOffset(slice.getOffset());
			pr.getQueryModifiers().setLimit(slice.getLimit());
			return translateTupleExpr(slice.getArg(), pr, newHeadName); // narrow down the query
		} 
		else if (te instanceof Distinct) {
			// Add DISTINCT modifier, if any
			Distinct distinct = (Distinct) te;
			pr.getQueryModifiers().setDistinct();
			return translateTupleExpr(distinct.getArg(), pr, newHeadName); // narrow down the query
		} 
		else if (te instanceof Order) {
			// Add ORDER BY modifier, if any
			Order order = (Order) te;
			for (OrderElem c : order.getElements()) {	
				ValueExpr expression = c.getExpr();
				if (!(expression instanceof Var)) {
					throw new IllegalArgumentException("Error translating ORDER BY. "
							+ "The current implementation can only sort by variables, this query has a more complex expression. Offending expression: '"+expression+"'");
				}
				Var v = (Var) expression;
				Variable var = ofac.getVariable(v.getName());
				int direction =  c.isAscending() ? OrderCondition.ORDER_ASCENDING : OrderCondition.ORDER_DESCENDING; 
				pr.getQueryModifiers().addOrderCondition(var, direction);
			}
			return translateTupleExpr(order.getArg(), pr, newHeadName); // narrow down the query
		} 
		else if (te instanceof Projection) {
			return translate((Projection) te, pr, newHeadName);
		} 
		else if (te instanceof Filter) {
			return translate((Filter) te, pr, newHeadName);
		} 
		else if (te instanceof StatementPattern) {
			return translate((StatementPattern) te);		
		} 
		else if (te instanceof Join) {
			return translate((Join) te, pr, newHeadName);
		} 
		else if (te instanceof Union) {
			return translate((Union) te, pr, newHeadName);
		} 
		else if (te instanceof LeftJoin) {
			return translate((LeftJoin) te, pr, newHeadName);
		} 
		else if (te instanceof Reduced) {
			Reduced reduced = (Reduced)te;
			return translateTupleExpr(reduced.getArg(), pr, newHeadName);
		} 
		else if (te instanceof Extension) { 
			return translate((Extension) te, pr, newHeadName);
		} 
		
		try {
			throw new QueryEvaluationException("Operation not supported: " + te);
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	private Set<Variable> getVariables(Function atom) {
		Set<Variable> set = new HashSet<>();
		for (Term t : atom.getTerms())
			if (t instanceof Variable)
				set.add((Variable)t);
		return set;
	}
	
	private List<Term> getUnion(Set<Variable> s1, Set<Variable> s2) {
		// take the union of the *sets* of variables
		Set<Term> vars = new HashSet<>();
		vars.addAll(s1);
		vars.addAll(s2);
		// order is chosen arbitrarily but this is not a problem
		// because it is chosen once and for all
		List<Term> varList = new ArrayList<>(vars);
		return varList;
	}

	private List<Term> getUnionOfVariables(Function a1, Function a2) {
		// take the union of the *sets* of variables
		Set<Term> vars = new HashSet<>();
		for (Term t : a1.getTerms())
			if (t instanceof Variable)
				vars.add(t);
		for (Term t : a2.getTerms())
			if (t instanceof Variable)
				vars.add(t);
		// order is chosen arbitrarily but this is not a problem
		// because it is chosen once and for all
		List<Term> varList = new ArrayList<>(vars);
		return varList;
	}
	
	
	private CQIE createRule(DatalogProgram pr, String headName, List<Term> headParameters, Function... body) {
		Predicate pred = ofac.getPredicate(headName, headParameters.size());
		Function head = ofac.getFunction(pred, headParameters);
		CQIE rule = ofac.getCQIE(head, body);
		pr.appendRule(rule);
		return rule;
	}
	
	/**
	 * EXTEND { (T_j AS V_j) } EXPR
	 * 
	 * where the T_j are built from the variables X of EXPR,
	 *   
	 * adds the following rule:
	 * 
	 *   ans_i(X * T) :- ans_{i.0}(X)
	 * 
	 * @param extend
	 * @param pr
	 * @param newHeadName
	 * @return 
	 */
	
	private Function translate(Extension extend, DatalogProgram pr, String newHeadName) {

		Function subAtom = translateTupleExpr(extend.getArg(), pr, newHeadName + "0");
		Set<Variable> subVarSet = getVariables(subAtom);
		
		int sz = subVarSet.size() + extend.getElements().size();
		List<Term> varList = new ArrayList<>(sz);
		varList.addAll(subVarSet);
		List<Term> termList = new ArrayList<>(sz);
		termList.addAll(varList);
		
		for (ExtensionElem el: extend.getElements()) {
			Variable var = ofac.getVariable(el.getName());
			varList.add(var);
			
			Term term = getExpression(el.getExpr());			
			termList.add(term);
		}
		CQIE rule = createRule(pr, newHeadName, termList, subAtom);
		
		Function newHeadAtom = ofac.getFunction(rule.getHead().getFunctionSymbol(), varList);
		return newHeadAtom;
	}		    

	/**
	 * EXPR_1 UNION EXPR_2
	 * 
	 * adds the following rules
	 * 
	 * ans_i(X * NULL_1) :- ans_{i.0}(X_1)
	 * ans_i(X * NULL_2) :- ans_{i.1}(X_2)
	 * 
		 * Adding the UNION to the program, i.e., two rules Note, we need to
		 * make null any head variables that do not appear in the body of the
		 * components of the union, e.g,
		 * 
		 * q(x,y,z) <- Union(R(x,y), R(x,z))
		 * 
		 * results in
		 * 
		 * q(x,y,null) :- ... R(x,y) ... 
		 * q(x,null,z) :- ... R(x,z) ...
	 * 
	 * @param union
	 * @param pr
	 * @param newHeadName
	 * @return 
	 */
	
	private Function translate(Union union, DatalogProgram pr, String  newHeadName) {
		
		Function leftAtom = translateTupleExpr(union.getLeftArg(), pr, newHeadName + "0");
		Set<Variable> leftVars = getVariables(leftAtom);
		
		Function rightAtom = translateTupleExpr(union.getRightArg(), pr, newHeadName + "1");
		Set<Variable> rightVars = getVariables(rightAtom);

		List<Term> varList = getUnion(leftVars, rightVars);
		
		// left atom rule
		List<Term> leftTermList = new ArrayList<>(varList.size());
		for (Term t : varList) {
			Term lt =  (leftVars.contains(t)) ? t : OBDAVocabulary.NULL;
			leftTermList.add(lt);
		}
		CQIE leftRule = createRule(pr, newHeadName, leftTermList, leftAtom);

		// right atom rule
		List<Term> rightTermList = new ArrayList<>(varList.size());
		for (Term t : varList) {
			Term lt =  (rightVars.contains(t)) ? t : OBDAVocabulary.NULL;
			rightTermList.add(lt);
		}
		CQIE rightRule = createRule(pr, newHeadName, rightTermList, rightAtom);
		
		Function atom = ofac.getFunction(rightRule.getHead().getFunctionSymbol(), varList);
		return atom;
	}

	
	/**
	 * EXPR_1 JOIN EXPR_2
	 * 
	 * adds the following rule 
	 * 
	 * ans_i(X_1 U X_2) :- ans_{i.0}(X_1), ans_{i.1}(X_2)
	 * 
	 * @param join
	 * @param pr
	 * @param newHeadName
	 * @return 
	 */
	
	private Function translate(Join join, DatalogProgram pr, String  newHeadName)  {
		
		Function leftAtom = translateTupleExpr(join.getLeftArg(), pr, newHeadName + "0");
		Function rightAtom = translateTupleExpr(join.getRightArg(), pr, newHeadName + "1");

		List<Term> varList = getUnionOfVariables(leftAtom, rightAtom);
		CQIE rule = createRule(pr, newHeadName, varList, leftAtom, rightAtom);
		return rule.getHead();
	}
	
	/**
	 * EXPR_1 OPT EXPR_2 FILTER F
	 * 
	 * ans_i(X_1 U X_2) :- LEFTJOIN(ans_{i.0}(X_1), ans_{i.1}(X_2), F(X_1 U X_2))
	 * 
	 * @param leftjoin
	 * @param pr
	 * @param newHeadName
	 * @return 
	 */

	private Function translate(LeftJoin leftjoin, DatalogProgram pr, String  newHeadName) {
		
		Function leftAtom = translateTupleExpr(leftjoin.getLeftArg(), pr, newHeadName + "0");
		Function rightAtom = translateTupleExpr(leftjoin.getRightArg(), pr, newHeadName + "1");

		// the left join atom
		Function joinAtom = ofac.getSPARQLLeftJoin(leftAtom, rightAtom);
		// adding the conditions of the filter for the LeftJoin 
		ValueExpr filter = leftjoin.getCondition();
		if (filter != null) {
			List<Term> joinTerms = joinAtom.getTerms();
			joinTerms.add((Function) getExpression(filter));
		}
		
		List<Term> varList = getUnionOfVariables(leftAtom, rightAtom);
		CQIE rule = createRule(pr, newHeadName, varList, joinAtom);
		return rule.getHead();
	}
	
	/**
	 * PROJECT { V_j } EXPR
	 * 
	 * adds the following rule
	 * 
	 * ans_i(V) :- ans_{i.0}(X)
	 * 
	 * @param project
	 * @param pr
	 * @param newHeadName
	 * @return 
	 */

	private Function translate(Projection project, DatalogProgram pr, String  newHeadName) {

		Function atom = translateTupleExpr(project.getArg(), pr, newHeadName + "0");
		
		List<ProjectionElem> projectionElements = project.getProjectionElemList().getElements();
		List<Term> varList = new  ArrayList<>(projectionElements.size());
		for (ProjectionElem var : projectionElements)  {
			// we assume here that the target name is "introduced" as one of the arguments of atom
			// (this is normally done by an EXTEND inside the PROJECTION)
			// first, we check whether this assumption can be made
			if (!var.getSourceName().equals(var.getTargetName())) {
				boolean found = false;
				for (Term a : atom.getTerms())
					if ((a instanceof Variable) && ((Variable)a).getName().equals(var.getSourceName())) {
						found = true;
						break;
					}
				if (!found)
					throw new RuntimeException("Projection target of " + var + " not found in " + project.getArg());
			}
			varList.add(ofac.getVariable(var.getTargetName()));
		}

		CQIE rule = createRule(pr, newHeadName, varList, atom);
		return rule.getHead();
	}

	/**
	 * FILTER EXPR F
	 * 
	 * adds the following rule
	 * 
	 * ans_i(X U X') :- ans_{i.0}(X), F(X')
	 * 
	 * @param filter
	 * @param pr
	 * @param newHeadName
	 * @return
	 */
	private Function translate(Filter filter, DatalogProgram pr, String  newHeadName) {

		Function atom = translateTupleExpr(filter.getArg(), pr, newHeadName + "0");		
		Set<Variable> atomVars = getVariables(atom);
		
		ValueExpr condition = filter.getCondition();
		Function filterAtom;
		if (condition instanceof Var) 
			filterAtom = ofac.getFunctionIsTrue(getOntopTerm((Var) condition));
		else 
			filterAtom = (Function) getExpression(condition);
		
		Set<Variable> filterVars = new HashSet<>();
		TermUtils.addReferencedVariablesTo(filterVars, filterAtom);
		
		List<Term> vars = getUnion(atomVars, filterVars);	
		CQIE rule = createRule(pr, newHeadName, vars, atom, filterAtom);
		return rule.getHead();
	}

	/***
	 * This translates a single triple. 
	 * 
	 * @param triple
	 * @return
	 */
	private Function translate(StatementPattern triple) {
		
		Var pred = triple.getPredicateVar();		
		Value p = pred.getValue();
		
		if (!(p instanceof URI || (p == null))) {
			// if predicate is a variable or literal
			throw new RuntimeException("Unsupported query syntax");
		}

		Var subj = triple.getSubjectVar();
		Var obj = triple.getObjectVar();
		
		// Subject node		
		Term sTerm = getOntopTerm(subj);
		
		if ((p != null) && p.toString().equals(RDF.TYPE.stringValue())) {

			Value o = obj.getValue();
			// Object node
			if (o == null) {
				Function rdfTypeConstant = ofac.getUriTemplate(ofac.getConstantLiteral(OBDAVocabulary.RDF_TYPE));
				return ofac.getTripleAtom(sTerm, rdfTypeConstant, ofac.getVariable(obj.getName()));
			} 
			else if (o instanceof URI) {
				URI objectUri = (URI)o; 
				Predicate.COL_TYPE type = dtfac.getDatatype(objectUri);
				if (type != null) {
					Predicate predicate = dtfac.getTypePredicate(type);
					return ofac.getFunction(predicate, sTerm);
				}
	            else {
	        		COL_TYPE subjectType = null; // are never changed
					Predicate predicate = ofac.getPredicate(objectUri.stringValue(), new COL_TYPE[] { subjectType });
					return ofac.getFunction(predicate, sTerm);
				}
			}
			else  
				throw new RuntimeException("Unsupported query syntax");
		} 
		else {			
			// The predicate is NOT rdf:type
			Term oTerm = getOntopTerm(obj); 
			
			if (p != null) {
        		COL_TYPE subjectType = null; // are never changed
				COL_TYPE objectType = null;
				Predicate predicate = ofac.getPredicate(p.stringValue(), new COL_TYPE[] { subjectType, objectType });
				return ofac.getFunction(predicate, sTerm, oTerm);
			} 
			else 
				return ofac.getTripleAtom(sTerm, ofac.getVariable(pred.getName()), oTerm);
		}
	}
	
	private Term getOntopTerm(Var subj) {
		Value s = subj.getValue();
		Term result = null;
		if (s == null) {
			result = ofac.getVariable(subj.getName());
		} 
		else if (s instanceof Literal) {
			Literal object = (Literal) s;
			URI type = object.getDatatype();
			String value = object.getLabel();
	
			// Validating that the value is correct (lexically) with respect to the
			// specified datatype
			if (type != null) {
				boolean valid = XMLDatatypeUtil.isValidValue(value, type);
				if (!valid)
					throw new RuntimeException("Invalid lexical form for datatype. Found: " + value);
			}
			
			COL_TYPE objectType; 			
			if (type == null) 
				objectType = COL_TYPE.LITERAL;
			else {
				objectType = dtfac.getDatatype(type);
		        if (objectType == null) 
					throw new RuntimeException("Unsupported datatype: " + type.stringValue());
			}
			
			// special case for decimal
	        if ((objectType == COL_TYPE.DECIMAL) && !value.contains("."))  { 
				// put the type as integer (decimal without fractions)
				objectType = COL_TYPE.INTEGER;
			} 
	        
			ValueConstant constant = ofac.getConstantLiteral(value, objectType);

			// v1.7: We extend the syntax such that the data type of a
			// constant is defined using a functional symbol.
			if (objectType == COL_TYPE.LITERAL) {
				// if the object has type LITERAL, check any language tag!
				String lang = object.getLanguage();
				if (lang != null && !lang.equals("")) {
					result = ofac.getTypedTerm(constant, lang.toLowerCase());
				} 
				else {
					result =  ofac.getTypedTerm(constant, objectType);
				}
			} 
			else {
				result = ofac.getTypedTerm(constant, objectType);
			}
		} 
		else if (s instanceof URI) {
			if (uriRef != null) {
				// if in the Semantic Index mode 
				int id = uriRef.getId(s.stringValue());
				result = ofac.getUriTemplate(ofac.getConstantLiteral(String.valueOf(id), COL_TYPE.INTEGER));
			} 
			else {
				String subject_URI = decodeURIEscapeCodes(s.stringValue());
				result = uriTemplateMatcher.generateURIFunction(subject_URI);
			}
		}
		
		return result;
	}
	
	/***
	 * Given a string representing a URI, this method will return a new String 
	 * in which all percent encoded characters (e.g., %20) will
	 * be restored to their original characters (e.g., ' '). 
	 * This is necessary to transform some URIs into the original database values.
	 * 
	 * @param encodedURI
	 * @return
	 */
	
	private String decodeURIEscapeCodes(String encodedURI) {
		int length = encodedURI.length();
		StringBuilder strBuilder = new StringBuilder(length+20);
		
		char[] codeBuffer = new char[2];
		
		for (int ci = 0; ci < length; ci++) {
			char c = encodedURI.charAt(ci);

			if (c != '%') {
				// base case, the character is a normal character, just
				// append
				strBuilder.append(c);
				continue;
			}

			/*
			 * found a escape, processing the code and replacing it by
			 * the original value that should be found on the DB. This
			 * should not be used all the time, only when working in
			 * virtual mode... we need to fix this with a FLAG.
			 */

			// First we get the 2 chars next to %
			codeBuffer[0] = encodedURI.charAt(ci + 1);
			codeBuffer[1] = encodedURI.charAt(ci + 2);

			// now we check if they match any of our escape codes, if
			// they do the char to be inserted is put in codeBuffer
			// otherwise
			String code = String.copyValueOf(codeBuffer);
			if (code.equals("%20")) {
				strBuilder.append(' ');
			} else if (code.equals("%21")) {
				strBuilder.append('!');
			} else if (code.equals("%40")) {
				strBuilder.append('@');
			} else if (code.equals("%23")) {
				strBuilder.append('#');
			} else if (code.equals("%24")) {
				strBuilder.append('$');
			} else if (code.equals("%26")) {
				strBuilder.append('&');
			} else if (code.equals("%42")) {
				strBuilder.append('*');
			} else if (code.equals("%28")) {
				strBuilder.append('(');
			} else if (code.equals("%29")) {
				strBuilder.append(')');
			} else if (code.equals("%5B")) {
				strBuilder.append('[');
			} else if (code.equals("%5C")) {
				strBuilder.append(']');
			} else if (code.equals("%2C")) {
				strBuilder.append(',');
			} else if (code.equals("%3B")) {
				strBuilder.append(';');
			} else if (code.equals("%3A")) {
				strBuilder.append(':');
			} else if (code.equals("%3F")) {
				strBuilder.append('?');
			} else if (code.equals("%3D")) {
				strBuilder.append('=');
			} else if (code.equals("%2B")) {
				strBuilder.append('+');
			} else if (code.equals("%22")) {
				strBuilder.append('\'');
			} else if (code.equals("%2F")) {
				strBuilder.append('/');
			} else {
				// This was not an escape code, so we just append the
				// characters and continue;
				strBuilder.append(codeBuffer);
			}
			ci += 2;

		}
		return strBuilder.toString();

	}
	


	private Term getExpression(ValueExpr expr) {
		if (expr instanceof Var) {
			return getOntopTerm((Var) expr);
		} 
		else if (expr instanceof org.openrdf.query.algebra.ValueConstant) {
			return getConstantExpression(((org.openrdf.query.algebra.ValueConstant) expr).getValue());
		} 
		else if (expr instanceof UnaryValueOperator) {
			return getUnaryExpression((UnaryValueOperator) expr);
		} 
		else if (expr instanceof BinaryValueOperator) {
			return getBinaryExpression((BinaryValueOperator) expr);
		} 
		else if (expr instanceof Bound) {	
			return ofac.getFunctionIsNotNull(getOntopTerm(((Bound) expr).getArg()));
		} 
		else if (expr instanceof FunctionCall) {
            return getFunctionCallTerm((FunctionCall)expr);
		} 
		throw new RuntimeException("The expression " + expr + " is not supported yet!");
	}

	private Term getConcat(List<ValueExpr> values) {
        Iterator<ValueExpr> iterator = values.iterator();

        ValueExpr first = iterator.next();
        Term topConcat = getExpression(first);
        
        if (!iterator.hasNext())
            throw new UnsupportedOperationException("Wrong number of arguments (found " + values.size() + 
            					", at least 1) of SQL function CONCAT");
 	  	
        while (iterator.hasNext()) {
            ValueExpr second = iterator.next();
            Term second_string = getExpression(second);

            topConcat = ofac.getFunctionConcat(topConcat, second_string);                	
        }
        
        return topConcat;		
	}
	
	private Term getLength(List<ValueExpr> args) {
		if (args.size() != 1){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 1 supported) for SQL LENGTH function");					
		}
		ValueExpr arg = args.get(0); 
		Term term = getExpression(arg);
		term = ofac.getFunctionLength(term);
		return term;
	}
	
	private Term getSubstring(List<ValueExpr> args) {
		Term term = null;

		switch (args.size()){
			case 2 :
				ValueExpr string = args.get(0);
				ValueExpr start = args.get(1);
				Term str = getExpression(string);
				Term st = getExpression(start);
				term  = ofac.getFunctionSubstring(str, st);
				break;

			case 3 :
				string = args.get(0);
				start = args.get(1);
				ValueExpr end = args.get(2);
				str = getExpression(string);
				st = getExpression(start);
				Term en = getExpression(end);
				term = ofac.getFunctionSubstring(str, st, en);
				break;
			default:
				throw new UnsupportedOperationException("Wrong number of arguments (found "
						+ args.size() + ", only 2 or 3 supported) for SQL SUBSTRING function");

		}

		return term;
	}
	
	private Term getLower(List<ValueExpr> args) {
		if (args.size() != 1){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 1 supported) for SQL LOWER function");					
		}
		ValueExpr arg = args.get(0); 
		Term term = getExpression(arg);
		term = ofac.getFunctionLower(term);
		return term;
	}

	private Term getUpper(List<ValueExpr> args) {
		if (args.size() != 1){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 1 supported) for SQL UPPER function");					
		}
		ValueExpr arg = args.get(0); 
		Term term = getExpression(arg);
		term = ofac.getFunctionUpper(term);
		return term;
	}
	
	private Term getEncodeForUri(List<ValueExpr> args) {
		ValueExpr arg = args.get(0); 
		Term term = getExpression(arg);
		term = ofac.getFunctionEncodeForUri(term);
		return term;
	} 
	
	private Term getStrBefore(List<ValueExpr> args) {
		if (args.size() != 2){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 2 supported) for SQL STRBEFORE function");					
		}
		ValueExpr string = args.get(0); 
		ValueExpr before = args.get(1); 
		Term str = getExpression(string);
		Term be = getExpression(before);
		Term term = ofac.getFunctionStrBefore(str, be);
		return term;
	}
	
	private Term getStrAfter(List<ValueExpr> args) {
		if (args.size() != 2){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 2 supported) for SQL STRAFTER function");					
		}
		ValueExpr string = args.get(0); 
		ValueExpr after = args.get(1); 
		Term str = getExpression(string);
		Term af = getExpression(after);
		Term term = ofac.getFunctionStrAfter(str, af);
		return term;
	}
	
	private Term getStrStarts(List<ValueExpr> args) {
		if (args.size() != 2){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 2 supported) for SQL STRSTARTS function");					
		}
		ValueExpr string = args.get(0); 
		ValueExpr start = args.get(1); 
		Term str = getExpression(string);
		Term sta = getExpression(start);
		Term term = ofac.getFunctionStrStarts(str, sta);
		return term;
	}
	
	private Term getStrEnds(List<ValueExpr> args) {
		if (args.size() != 2){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 2 supported) for SQL STRENDS function");					
		}
		ValueExpr string = args.get(0); 
		ValueExpr start = args.get(1); 
		Term str = getExpression(string);
		Term sta = getExpression(start);
		Term term = ofac.getFunctionStrEnds(str, sta);
		return term;
	}
	
	private Term getContains(List<ValueExpr> args) {
		if (args.size() != 2){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 2 supported) for SQL CONTAINS function");					
		}
		ValueExpr string = args.get(0); 
		ValueExpr start = args.get(1); 
		Term str = getExpression(string);
		Term sta = getExpression(start);
		Term term = ofac.getFunctionContains(str, sta);
		return term;
	}
	
	private Term getAbs(List<ValueExpr> args) {	
		if (args.size() != 1){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 1 supported) for SQL ABS function");					
		}
		ValueExpr argument = args.get(0);
		Term arg = getExpression(argument);
		Term term = ofac.getFunctionAbs(arg);
		return term;	
	}
	
	private Term getCeil(List<ValueExpr> args) {
		if (args.size() != 1){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 1 supported) for SQL CEIL function");					
		}
		ValueExpr argument = args.get(0);
		Term arg = getExpression(argument);
		Term term = ofac.getFunctionCeil(arg);
		return term;	
	}
	
	private Term getFloor(List<ValueExpr> args) {
		if (args.size() != 1){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 1 supported) for SQL FLOOR function");					
		}
		ValueExpr argument = args.get(0);
		Term arg = getExpression(argument);
		Term term = ofac.getFunctionFloor(arg);
		return term;	
	}
	
	private Term getRound(List<ValueExpr> args) {
		if (args.size() != 1){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 1 supported) for SQL ROUND function");					
		}
		ValueExpr argument = args.get(0);
		Term arg = getExpression(argument);
		Term term = ofac.getFunctionRound(arg);
		return term;	
	}
	

	
	private Term getRand() {
		Term term = ofac.getFunctionRand();
		return term;	
	}

	private Term getUUID() {
		Term term = ofac.getFunctionUUID();
		return term;	
	}

	private Term getstrUUID() {
		Term term = ofac.getFunctionstrUUID();
		return term;
	}
	
	private Term getNow() {	
		Term term = ofac.getFunctionNow();
		return term;	
	}
	
	private Term getYear(List<ValueExpr> args) {
		if (args.size() != 1){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 1 supported) for SQL YEAR function");					
		}
		ValueExpr argument = args.get(0);
		Term arg = getExpression(argument);
		Term term = ofac.getFunctionYear(arg);
		return term;	
	}
	
	private Term getDay(List<ValueExpr> args) {
		if (args.size() != 1){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 1 supported) for SQL DAY function");					
		}
		ValueExpr argument = args.get(0);
		Term arg = getExpression(argument);
		Term term = ofac.getFunctionDay(arg);
		return term;	
	}
	
	private Term getMonth(List<ValueExpr> args) {
		if (args.size() != 1){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 1 supported) for SQL MONTH function");					
		}
		ValueExpr argument = args.get(0);
		Term arg = getExpression(argument);
		Term term = ofac.getFunctionMonth(arg);
		return term;	
	}
	
	private Term getHours(List<ValueExpr> args) {
		if (args.size() != 1){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 1 supported) for SQL HOURS function");					
		}
		ValueExpr argument = args.get(0);
		Term arg = getExpression(argument);
		Term term = ofac.getFunctionHours(arg);
		return term;	
	}
	
	
	private Term getMinutes(List<ValueExpr> args) {
		if (args.size() != 1){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 1 supported) for SQL MINUTES function");					
		}
		ValueExpr argument = args.get(0);
		Term arg = getExpression(argument);
		Term term = ofac.getFunctionMinutes(arg);
		return term;	
	}
	
	private Term getSeconds(List<ValueExpr> args) {
		if (args.size() != 1){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 1 supported) for SQL SECONDS function");					
		}
		ValueExpr argument = args.get(0);
		Term arg = getExpression(argument);
		Term term = ofac.getFunctionSeconds(arg);
		return term;	
	}
	
	private Term getTZ(List<ValueExpr> args) {
		if (args.size() != 1){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 1 supported) for SQL TZ function");
		}
		ValueExpr argument = args.get(0);
		Term arg = getExpression(argument);
		Term term = ofac.getFunctionTZ(arg);
		return term;	
	}
	
	private Term getMD5(List<ValueExpr> args) {	
		if (args.size() != 1){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 1 supported) for SQL hash function");					
		}
		ValueExpr argument = args.get(0);
		Term arg = getExpression(argument);
		Term term = ofac.getFunctionMD5(arg);
		return term;	
	}
	
	private Term getSHA1(List<ValueExpr> args) {	
		if (args.size() != 1){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 1 supported) for SQL hash function");					
		}
		ValueExpr argument = args.get(0);
		Term arg = getExpression(argument);
		Term term = ofac.getFunctionSHA1(arg);
		return term;	
	}
	
	private Term getSHA256(List<ValueExpr> args) {	
		if (args.size() != 1){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 1 supported) for SQL hash function");					
		}
		ValueExpr argument = args.get(0);
		Term arg = getExpression(argument);
		Term term = ofac.getFunctionSHA256(arg);
		return term;	
	}
	
	private Term getSHA512(List<ValueExpr> args) {
		if (args.size() != 1){
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
		+ args.size() + ", only 1 supported) for SQL hash function");					
		}
		ValueExpr argument = args.get(0);
		Term arg = getExpression(argument);
		Term term = ofac.getFunctionSHA512(arg);
		return term;	
	}
		
	private Term getReplace(List<ValueExpr> expressions) {
        if (expressions.size() == 2 || expressions.size() == 3) {

            // first parameter is a function expression
            ValueExpr first = expressions.get(0);
            Term t1 = getExpression(first);

            // second parameter is a string
            ValueExpr second = expressions.get(1);
            Term out_string = getExpression(second);

            /*
             * Term t3 is optional: no string means delete occurrences of second param
	         */
            Term in_string;
            if (expressions.size() == 3) {
                ValueExpr third = expressions.get(2);
                in_string = getExpression(third);
            } 
            else {
                in_string = ofac.getConstantLiteral("");
            }

            return ofac.getFunctionReplace(t1, out_string, in_string);
        } 
        else
            throw new UnsupportedOperationException("Wrong number of arguments (found " + expressions.size() + ", only 2 or 3 supported) to sql function REPLACE");		
	}
	
	
	
    /** Return the Functions supported at the moment only
     * concat and replace
     * @param expr
     * @return
     */
    private Term getFunctionCallTerm(FunctionCall expr) {
    	
        switch(expr.getURI()){
         
            case "http://www.w3.org/2005/xpath-functions#concat":
                return getConcat(expr.getArgs());

            case "http://www.w3.org/2005/xpath-functions#replace":
                return getReplace(expr.getArgs());
                
            case "http://www.w3.org/2005/xpath-functions#string-length":
                return getLength(expr.getArgs()); 
                
            case "http://www.w3.org/2005/xpath-functions#substring":
            	return getSubstring(expr.getArgs()); 
            	
            case "http://www.w3.org/2005/xpath-functions#upper-case":
            	return getUpper(expr.getArgs());    
            	
            case "http://www.w3.org/2005/xpath-functions#lower-case":
            	return getLower(expr.getArgs());  
            	
            case "http://www.w3.org/2005/xpath-functions#substring-before":
            	return getStrBefore(expr.getArgs()); 
            	
            case "http://www.w3.org/2005/xpath-functions#substring-after":
            	return getStrAfter(expr.getArgs()); 
            	
            case "http://www.w3.org/2005/xpath-functions#starts-with":
            	return getStrStarts(expr.getArgs()); 
            	
            case "http://www.w3.org/2005/xpath-functions#ends-with":
            	return getStrEnds(expr.getArgs()); 
            	
            case "http://www.w3.org/2005/xpath-functions#contains":
            	return getContains(expr.getArgs());
            	
            case "http://www.w3.org/2005/xpath-functions#encode-for-uri":
            	return getEncodeForUri(expr.getArgs());
            	
            case "http://www.w3.org/2005/xpath-functions#numeric-abs":
            	return getAbs(expr.getArgs());
            	
            case "http://www.w3.org/2005/xpath-functions#numeric-ceil":
            	return getCeil(expr.getArgs());
            	
            case "http://www.w3.org/2005/xpath-functions#numeric-floor":
            	return getFloor(expr.getArgs());
            	
            case "http://www.w3.org/2005/xpath-functions#numeric-round":
            	return getRound(expr.getArgs());
            	
            case "RAND":
            	return getRand();
            	
            case "UUID":
            	return getUUID();

			case "STRUUID":
				return getstrUUID();
                        
            case "MD5":
            	return getMD5(expr.getArgs()); 
           
            case "SHA1":
            	return getSHA1(expr.getArgs()); 
            	
            case "SHA256":
            	return getSHA256(expr.getArgs()); 
            	
            case "SHA512":
            	return getSHA512(expr.getArgs());
            	
            case "NOW":
            	return getNow();	
            
            case "http://www.w3.org/2005/xpath-functions#year-from-dateTime":
            	return getYear(expr.getArgs());
            	
            case "http://www.w3.org/2005/xpath-functions#day-from-dateTime":
            	return getDay(expr.getArgs());
            	
            case "http://www.w3.org/2005/xpath-functions#month-from-dateTime":
            	return getMonth(expr.getArgs());	
           
            case "http://www.w3.org/2005/xpath-functions#hours-from-dateTime":
            	return getHours(expr.getArgs());
	
            case "http://www.w3.org/2005/xpath-functions#minutes-from-dateTime":
            	return getMinutes(expr.getArgs());
               	
            case "http://www.w3.org/2005/xpath-functions#seconds-from-dateTime":
            	return getSeconds(expr.getArgs());	
            	
            case "TZ":
            	return getTZ(expr.getArgs());
            	
            default:
                throw new RuntimeException("The builtin function " + expr.getURI() + " is not supported yet!");
        }
    }


	private Term getConstantExpression(Value v) {

		if (v instanceof Literal) {
			Literal lit = (Literal)v;
			URI type = lit.getDatatype();
			COL_TYPE tp;
			if (type == null) {
				tp = COL_TYPE.LITERAL;
			}
			else {
				tp = dtfac.getDatatype(type);
				if (tp == null) {
					return ofac.getUriTemplateForDatatype(type.stringValue());
				}				
			}
			
			String constantString;
			switch (tp) {
				case INTEGER:
				case NEGATIVE_INTEGER:
				case NON_POSITIVE_INTEGER:
				case POSITIVE_INTEGER:
				case NON_NEGATIVE_INTEGER:
					constantString = lit.integerValue().toString();
					break;
				case LONG:
					constantString = lit.longValue() + "";
					break;
				case DECIMAL:
					constantString = lit.decimalValue().toString();
					break;
				case FLOAT:
					constantString = lit.floatValue() + "";
					break;
				case DOUBLE:
					constantString = lit.doubleValue() + "";
					break;
				case INT:
				case UNSIGNED_INT:
					constantString = lit.intValue() + "";
					break;
				case DATETIME_STAMP:
				case DATETIME:
				case YEAR:
				case DATE:
				case TIME:
					constantString = lit.calendarValue().toString();
					break;
				case BOOLEAN:
					constantString = lit.booleanValue() + "";
					break;
				case STRING:
				case LITERAL:
					constantString = lit.stringValue() + "";
					break;
				default:
					throw new RuntimeException("Undefined datatype: " + tp);
			}
			ValueConstant constant = ofac.getConstantLiteral(constantString, tp);
			return ofac.getTypedTerm(constant, tp);	
		} 
		else if (v instanceof URI) {
            Function constantFunction = uriTemplateMatcher.generateURIFunction(v.stringValue());
            if (constantFunction.getArity() == 1)
                constantFunction = ofac.getUriTemplateForDatatype(v.stringValue());
            return constantFunction;
		}
		
		throw new RuntimeException("The value " + v + " is not supported yet!");
	}

	private Term getUnaryExpression(UnaryValueOperator expr) {

		Term term = getExpression(expr.getArg());

		if (expr instanceof Not) {
			return ofac.getFunctionNOT(term);
		}
		else if (expr instanceof IsLiteral) {
			return ofac.getFunction(OBDAVocabulary.SPARQL_IS_LITERAL, term);	
		} 
		else if (expr instanceof IsURI) {
			return ofac.getFunction(OBDAVocabulary.SPARQL_IS_URI, term);
		} 
		else if (expr instanceof Str) {
			return ofac.getFunction(OBDAVocabulary.SPARQL_STR, term);
		} 
		else if (expr instanceof Datatype) {
			return ofac.getFunction(OBDAVocabulary.SPARQL_DATATYPE, term);
		} 
		else if (expr instanceof IsBNode) {
			return ofac.getFunction(OBDAVocabulary.SPARQL_IS_BLANK, term);
		} 
		
		else if (expr instanceof Lang) {
			ValueExpr arg = expr.getArg();
			if (arg instanceof Var) 
				return ofac.getFunction(OBDAVocabulary.SPARQL_LANG, term);
			else
				throw new RuntimeException("A variable or a value is expected in " + expr);
		}
		
		throw new RuntimeException("The expression " + expr + " is not supported yet!");
	}

	private Term getBinaryExpression(BinaryValueOperator expr) {
		
		ValueExpr arg1 = expr.getLeftArg(); // get the first argument
		Term term1 = getExpression(arg1);
		
		ValueExpr arg2 = expr.getRightArg(); // get the second argument
		Term term2 = getExpression(arg2);
		
		if (expr instanceof And) {
			return ofac.getFunctionAND(term1, term2);
		} 
		else if (expr instanceof Or) {
			return ofac.getFunctionOR(term1, term2);
		}
		else if (expr instanceof SameTerm) {
			return ofac.getFunctionEQ(term1, term2);
		} 
		else if (expr instanceof Regex) { // sesame regex is Binary, Jena N-ary
			Regex reg = (Regex) expr;
			ValueExpr flags = reg.getFlagsArg();
			Term term3 = (flags != null) ? getExpression(flags) : OBDAVocabulary.NULL;
			return ofac.getFunction(OBDAVocabulary.SPARQL_REGEX, term1, term2, term3);
		}
		else if (expr instanceof Compare) {
			switch (((Compare) expr).getOperator()) {
				case EQ:
					return ofac.getFunctionEQ(term1, term2);
				case GE:
					return ofac.getFunctionGTE(term1, term2);
				case GT:
					return ofac.getFunctionGT(term1, term2);
				case LE:
					return ofac.getFunctionLTE(term1, term2);
				case LT:
					return ofac.getFunctionLT(term1, term2);
				case NE:
					return ofac.getFunctionNEQ(term1, term2);
			}
		} 
		else if (expr instanceof MathExpr) {
			switch (((MathExpr)expr).getOperator()) {
				case PLUS:
					return ofac.getFunctionAdd(term1, term2);
				case MINUS:
					return ofac.getFunctionSubstract(term1, term2);
				case MULTIPLY: 
					return ofac.getFunctionMultiply(term1, term2);
				case DIVIDE:
					// TODO: NOT SUPPORTED?
					break;
			}
		} 
		else if (expr instanceof LangMatches) {
			return ofac.getLANGMATCHESFunction(term1, toLowerCase(term2));
		} 
		
		throw new RuntimeException("The expression " + expr + " is not supported yet!");
	}

	private Term toLowerCase(Term term) {
		Term output = term;
		if (term instanceof Function) {
			Function f = (Function) term;
			Predicate functor = f.getFunctionSymbol();
			if (functor instanceof DatatypePredicate) {
				Term functionTerm = f.getTerm(0);
				if (functionTerm instanceof Constant) {
					Constant c = (Constant) functionTerm;
					output = ofac.getFunction(functor, 
							 ofac.getConstantLiteral(c.getValue().toLowerCase(), 
							 c.getType()));
				}
			}
		}
		return output;
	}
	
	/**
	 * Used only in QuestStatement
	 *  
	 * TODO: to be removed
	 * 
	 * @param query
	 * @return
	 */
	
	public List<String> getSignature(ParsedQuery query) {
		if (query instanceof ParsedTupleQuery || query instanceof ParsedGraphQuery) {
			TupleExpr te = query.getTupleExpr();
			List<String> signatureContainer = new ArrayList<>(te.getBindingNames());
			return signatureContainer;
		}
		return Collections.emptyList();
	}
}
