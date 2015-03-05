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

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatatypePredicate;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.DatatypeFactory;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDAQueryModifiers.OrderCondition;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.Substitution;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.SubstitutionUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.UriTemplateMatcher;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.BinaryValueOperator;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Datatype;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.IsBNode;
import org.openrdf.query.algebra.IsLiteral;
import org.openrdf.query.algebra.IsURI;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.Lang;
import org.openrdf.query.algebra.LangMatches;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Str;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.UnaryValueOperator;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private final TermComparator comparator = new TermComparator();

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

	public DatalogProgram translate(ParsedQuery pq, List<String> signature) {
		TupleExpr te = pq.getTupleExpr();

		log.debug("SPARQL algebra: \n{}", te);
		DatalogProgram result = ofac.getDatalogProgram();

		// Render the variable names in the signature into Variable object
		List<Term> vars = new LinkedList<>();
		for (String vs : signature) 
			vars.add(ofac.getVariable(vs));

		Predicate answerPred = ofac.getPredicate(OBDAVocabulary.QUEST_QUERY, vars.size());
		Function headAtom = ofac.getFunction(answerPred, vars);
		
		translateTupleExpr(headAtom, te, result, 1);
		return result;
	}


	/**
	 * main translation method -- a switch over all possible types of subexpressions
	 * 
	 * @param vars
	 * @param te
	 * @param pr
	 * @param i
	 * @param varcount
	 */
	
	private void translateTupleExpr(Function headAtom, TupleExpr te, DatalogProgram pr, long i) {
		if (te instanceof Slice) {
			// Add LIMIT and OFFSET modifiers, if any
			Slice slice = (Slice)te;
			pr.getQueryModifiers().setOffset(slice.getOffset());
			pr.getQueryModifiers().setLimit(slice.getLimit());
			translateTupleExpr(headAtom, slice.getArg(), pr, i); // narrow down the query
		} 
		else if (te instanceof Distinct) {
			// Add DISTINCT modifier, if any
			Distinct distinct = (Distinct) te;
			pr.getQueryModifiers().setDistinct();
			translateTupleExpr(headAtom, distinct.getArg(), pr, i); // narrow down the query
		} 
		else if (te instanceof Projection) {
			// Add PROJECTION modifier, if any
			translate(headAtom, (Projection) te, pr, i);
		} 
		else if (te instanceof Order) {
			// Add ORDER BY modifier, if any
			translate(headAtom, (Order) te, pr, i);
		} 
		else if (te instanceof Filter) {
			translate(headAtom, (Filter) te, pr, i);
		} 
		else if (te instanceof StatementPattern) {
			translate(headAtom, (StatementPattern) te, pr, i);
		} 
		else if (te instanceof Join) {
			translate(headAtom, (Join) te, pr, i);
		} 
		else if (te instanceof Union) {
			translate(headAtom, (Union) te, pr, i);
		} 
		else if (te instanceof LeftJoin) {
			translate(headAtom, (LeftJoin) te, pr, i);
		} 
		else if (te instanceof Reduced) {
			Reduced reduced = (Reduced)te;
			translateTupleExpr(headAtom, reduced.getArg(), pr, i);
		} 
		else if (te instanceof Extension) { 
			translate(headAtom, (Extension) te, pr, i);
		} 
		else {
			try {
				throw new QueryEvaluationException("Operation not supported: " + te);
			} catch (QueryEvaluationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
	}

	private Function getAnsAtom(TupleExpr subte, long i) {
		Set<Variable> varSet = getVariables(subte);
		List<Term> varList = new LinkedList<>();
		varList.addAll(varSet);
		Collections.sort(varList, comparator);
		Predicate pred = ofac.getPredicate("ans" + i, varList.size());
		Function atom = ofac.getFunction(pred, varList);
		return atom;
	}
	
	private void translate(Function headAtom, Extension extend, DatalogProgram pr, long i) {
		
		List<Term> headTerms = headAtom.getTerms();
		List<Term> atom2VarList = new LinkedList<>();
		atom2VarList.addAll(headTerms);
		for (ExtensionElem el: extend.getElements()) {
			String name = el.getName();
			Variable var = ofac.getVariable(name);
			int indexOfvar = headTerms.indexOf(var);
			
			ValueExpr vexp = el.getExpr();
			Term term = getBooleanTerm(vexp);			
			atom2VarList.set(indexOfvar,term);
		}
		Function newHeadAtom = ofac.getFunction(headAtom.getFunctionSymbol(), atom2VarList);
		
		TupleExpr subte = extend.getArg();		
		Function atom = getAnsAtom(subte, 2 * i);
		
		CQIE newrule = ofac.getCQIE(newHeadAtom, atom);
		pr.appendRule(newrule);
		
		// Translating the rest
		translateTupleExpr(atom, subte, pr, 2 * i);
	}		    

	
	private void translate(Function headAtom, Union union, DatalogProgram pr, long i) {
		TupleExpr left = union.getLeftArg();
		TupleExpr right = union.getRightArg();

		// Preparing the two atoms 
		Function leftAtom = getAnsAtom(left, 2 * i);
		Function rightAtom = getAnsAtom(right, (2 * i) + 1); 

		/*
		 * Adding the UNION to the program, i.e., two rules Note, we need to
		 * make null any head variables that do not appear in the body of the
		 * components of the union, e.g,
		 * 
		 * q(x,y,z) <- Union(R(x,y), R(x,z))
		 * 
		 * results in
		 * 
		 * q(x,y,null) :- ... R(x,y) ... q(x,null,z) :- ... R(x,z) ...
		 */
		Substitution nullifier1 = getNullifierForUnion(headAtom.getTerms(), leftAtom.getTerms());
		CQIE newrule1 = ofac.getCQIE(headAtom, leftAtom);
		CQIE newrule1n = SubstitutionUtilities.applySubstitution(newrule1, nullifier1);
		pr.appendRule(newrule1n);

		Substitution nullifier2 = getNullifierForUnion(headAtom.getTerms(), rightAtom.getTerms());
		CQIE newrule2 = ofac.getCQIE(headAtom, rightAtom);
		CQIE newrule2n = SubstitutionUtilities.applySubstitution(newrule2, nullifier2);
		pr.appendRule(newrule2n);
			
		// Translating the rest
		translateTupleExpr(leftAtom, left, pr, 2 * i);
		translateTupleExpr(rightAtom, right, pr, 2 * i + 1);
	}

	private static Substitution getNullifierForUnion(List<Term> terms, List<Term> nonNulls) {
		// finding out null
		Set<Variable> nullVars = new HashSet<>();
		// terms in the head are always variables
		for (Term t : terms)
			nullVars.add((Variable)t); 
		nullVars.removeAll(nonNulls); // the remaining variables do not
											// appear in the body assigning
											// null;
		Substitution nullifier = SubstitutionUtilities.getNullifier(nullVars);
		return nullifier;
	}
	
	private void translate(Function headAtom, Join join, DatalogProgram pr, long i) {
		TupleExpr left = join.getLeftArg();
		TupleExpr right = join.getRightArg();

		// Preparing the two atoms 
		Function leftAtom = getAnsAtom(left, 2 * i); 
		Function rightAtom = getAnsAtom(right, (2 * i) + 1); 

		// Adding the join to the program
		// We  avoid explicit join as much as possible, just use comma 
		CQIE newrule = ofac.getCQIE(headAtom, leftAtom, rightAtom);
		pr.appendRule(newrule);

		// Translating the rest
		translateTupleExpr(leftAtom, left, pr, 2 * i);
		translateTupleExpr(rightAtom, right, pr, 2 * i + 1);
	}

	private void translate(Function headAtom, LeftJoin join, DatalogProgram pr, long i) {
		TupleExpr left = join.getLeftArg();
		TupleExpr right = join.getRightArg();
		ValueExpr filter = join.getCondition();

		// Preparing the two atoms 
		Function leftAtom = getAnsAtom(left, 2 * i); 
		Function rightAtom = getAnsAtom(right, 2 * i + 1); 

		// The join 
		Function joinAtom = ofac.getSPARQLLeftJoin(leftAtom, rightAtom);

		/* adding the conditions of the filter for the LeftJoin */
		if (filter != null) {
		
			List<Term> joinTerms = joinAtom.getTerms();
			joinTerms.add(((Function) getBooleanTerm(filter)));
//			for (Expr expr : filter.getList()) {
//				joinTerms.add(((Function) getBooleanTerm(expr)));
//			}		
		}

		// Preparing the head of the LeftJoin rule and create the rule
		CQIE newrule = ofac.getCQIE(headAtom, joinAtom);
		pr.appendRule(newrule);

		// Translating the rest
		translateTupleExpr(leftAtom, left, pr, 2 * i);
		translateTupleExpr(rightAtom, right, pr, 2 * i + 1);
	}

	private void translate(Function headAtom, Projection project, DatalogProgram pr, long i) {

		TupleExpr te = project.getArg();
		Set<Variable> nestedVars = getVariables(te);

		List<Term> projectedVariables = new LinkedList<>();
		for (ProjectionElem var : project.getProjectionElemList().getElements()) {
			projectedVariables.add(ofac.getVariable(var.getSourceName()));
		}

		Predicate predicate = ofac.getPredicate("ans" + i,
				projectedVariables.size());
		Function head = ofac.getFunction(predicate, projectedVariables);

		Predicate pbody = ofac.getPredicate("ans" + (i + 1), nestedVars.size());

		Set<Variable> bodyatomVarsSet = getVariables(te);
		List<Term> bodyatomVarsList = new LinkedList<>();
		bodyatomVarsList.addAll(bodyatomVarsSet);
		Collections.sort(bodyatomVarsList, comparator);

		Function bodyAtom = ofac.getFunction(pbody, bodyatomVarsList);
		CQIE cq = ofac.getCQIE(head, bodyAtom);
		pr.appendRule(cq);

		// Continue the nested tree
		translateTupleExpr(bodyAtom, te, pr, i + 1);
	}

	private void translate(Function headAtom, Order order, DatalogProgram pr, long i) {
		
		for (OrderElem c : order.getElements()) {	
			ValueExpr expression = c.getExpr();
			if (!(expression instanceof Var)) {
				throw new IllegalArgumentException("Error translating ORDER BY. The current implementation can only sort by variables, this query has a more complex expression. Offending expression: '"+expression+"'");
			}
			Var v = (Var) expression;
			Variable var = ofac.getVariable(v.getName());
			int direction =  c.isAscending() ? OrderCondition.ORDER_ASCENDING : OrderCondition.ORDER_DESCENDING; 
			pr.getQueryModifiers().addOrderCondition(var, direction);
		}
		translateTupleExpr(headAtom, order.getArg(), pr, i); // narrow down the query
	}

	private void translate(Function headAtom, Filter filter, DatalogProgram pr, long i) {
		ValueExpr condition = filter.getCondition();
		List<Function> filterAtoms = new LinkedList<>();
		Set<Variable> filteredVariables = new LinkedHashSet<>();

		Function a = null;
		if (condition instanceof Var) {
			a = ofac.getFunctionIsTrue(getVariableTerm((Var) condition));
		} 
		else {
			a = (Function) getBooleanTerm(condition);
		}
		if (a != null) {
			Function filterAtom = ofac.getFunction(a.getFunctionSymbol(),
					a.getTerms());
			filterAtoms.add(filterAtom);
			filteredVariables.addAll(filterAtom.getReferencedVariables());
		}

		List<Term> innerProjection = new LinkedList<>();
		innerProjection.addAll(filteredVariables);
		Collections.sort(innerProjection, comparator);

		/***
		 * This is necessary because some filters might apply to variables that
		 * have not been projected yet, for example:
		 * <p>
		 * (filter (= ?x 99) <br>
		 * <t> (bgp (triple <http://example/x> <http://example/p> ?x)))
		 * <p>
		 * in this cases we must project at least the filtered variables from
		 * the nested expressions, otherwise we end up with free variables.
		 * 
		 */
		List<Term> vars;

		// TODO here we might be missing the case where there is a filter
		// on a variable that has not been projected out of the inner
		// expressions
		if (headAtom.getTerms().isEmpty() && !filteredVariables.isEmpty()) {
			vars = innerProjection;
		} 
		else {
			vars = new LinkedList<>();
			// filter out only variables, skip the complex terms
			for (Term t : headAtom.getTerms())
				 if (t instanceof Variable)
					 vars.add(t);
		}
		Predicate pbody = ofac.getPredicate("ans" + (i * 2), vars.size());
		Function bodyAtom = ofac.getFunction(pbody, vars);

		LinkedList<Function> body = new LinkedList<>();
		body.add(bodyAtom);
		body.addAll(filterAtoms);

		CQIE cq = ofac.getCQIE(headAtom, body);
		pr.appendRule(cq);

		translateTupleExpr(bodyAtom, filter.getArg(), pr, (i * 2));
	}

	/***
	 * This translates a single triple. In most cases it will generate one
	 * single atom, however, if URI's are present, it will generate also
	 * equality atoms.
	 * 
	 * @param triple
	 * @return
	 */
	private void translate(Function headAtom, StatementPattern triple, DatalogProgram pr, long i) {
		
		Var pred = triple.getPredicateVar();		
		Value p = pred.getValue();
		
		if (!(p instanceof URIImpl || (p == null))) {
			// if predicate is a variable or literal
			throw new RuntimeException("Unsupported query syntax");
		}

		Var subj = triple.getSubjectVar();
		Value s = subj.getValue();
		Var obj = triple.getObjectVar();
		Value o = obj.getValue();
		
		// / Instantiate the atom components: predicate and terms.
		Function atom = null;

		// Subject node		
		Term sTerm = getOntopTerm(subj, s);
		
		if ((p != null) && p.toString().equals(RDF.TYPE.stringValue())) {

			// Object node
			if (o == null) {
				Function rdfTypeConstant = ofac.getUriTemplate(ofac.getConstantLiteral(OBDAVocabulary.RDF_TYPE));
				atom = ofac.getTripleAtom(sTerm, rdfTypeConstant, ofac.getVariable(obj.getName()));
			} 
			else if (o instanceof URIImpl) {
				URI objectUri = (URI)o; 
				Predicate.COL_TYPE type = dtfac.getDatatype(objectUri);
				if (type != null) {
					Predicate predicate = dtfac.getTypePredicate(type);
					atom = ofac.getFunction(predicate, sTerm);
				}
	            else {
	        		COL_TYPE subjectType = null; // are never changed
					Predicate predicate = ofac.getPredicate(objectUri.stringValue(), new COL_TYPE[] { subjectType });
					atom = ofac.getFunction(predicate, sTerm);
				}
			}
			else /* if (o instanceof LiteralImpl)*/ {
				throw new RuntimeException("Unsupported query syntax");
			} 
		} 
		else {
			/*
			 * The predicate is NOT rdf:type
			 */

			Term oTerm = getOntopTerm(obj, o); 
			
			if (p != null) {
        		COL_TYPE subjectType = null; // are never changed
				COL_TYPE objectType = null;
				Predicate predicate = ofac.getPredicate(p.stringValue(), new COL_TYPE[] { subjectType, objectType });
				atom = ofac.getFunction(predicate, sTerm, oTerm);
			} 
			else {
				atom = ofac.getTripleAtom(sTerm, ofac.getVariable(pred.getName()), oTerm);
			}
		}
		
		CQIE newrule = ofac.getCQIE(headAtom, atom);
		pr.appendRule(newrule);
	}
	
	private Term getOntopTerm(Var subj, Value s) {
		Term result = null;
		if (s == null) {
			result = ofac.getVariable(subj.getName());
		} else if (s instanceof LiteralImpl) {
			LiteralImpl object = (LiteralImpl) s;
			COL_TYPE objectType = getDataType(object);
			ValueConstant constant = getConstant(object);

			// v1.7: We extend the syntax such that the data type of a
			// constant
			// is defined using a functional symbol.
			if (objectType == COL_TYPE.LITERAL) {
				// If the object has type LITERAL, check any language
				// tag!
				String lang = object.getLanguage();
				if (lang != null && !lang.equals("")) {
					result = ofac.getTypedTerm(constant, lang.toLowerCase());
				} 
				else {
					result =  ofac.getTypedTerm(constant, COL_TYPE.LITERAL);
				}
			} 
			else {
				// For other supported data-types
				result = ofac.getTypedTerm(constant, objectType);
			}
		} 
		else if (s instanceof URIImpl) {
			URIImpl subject = (URIImpl) s;
			//COL_TYPE subjectType = COL_TYPE.OBJECT;
			
			String subject_URI = subject.stringValue();
			subject_URI = decodeURIEscapeCodes(subject_URI);
			

			if (uriRef != null) {
				/* if in the Semantic Index mode */
				int id = uriRef.getId(s.stringValue());
				
				result = ofac.getUriTemplate(ofac.getConstantLiteral(String.valueOf(id), COL_TYPE.INTEGER));
			} else {
				result = uriTemplateMatcher.generateURIFunction(subject_URI);
			}
		}
		
		return result;
	}
	
	/***
	 * Given a string representing a URI, this method will return a new String in which all percent encoded characters (e.g., %20) will
	 * be restored to their original characters (e.g., ' '). This is necessary to transform some URIs into the original dtabase values.
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

			// now we check if they match any of our escape wodes, if
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
	
	private static class TermComparator implements Comparator<Term> {

		@Override
		public int compare(Term arg0, Term arg1) {
			return arg0.toString().compareTo(arg1.toString());
		}

	}

	private Set<Variable> getVariables(List<org.openrdf.query.algebra.Var> list) {
		Set<Variable> vars = new HashSet<Variable>();
		for (org.openrdf.query.algebra.Var variable : list) {
			if (!variable.hasValue()) { // if it has value, then its a constant
				String name = variable.getName();
				Variable var = ofac.getVariable(name);
				vars.add(var);
			}
		}
		return vars;
	}
	
	private Set<Variable> getBindVariables(List<ExtensionElem> elements) {
		Set<Variable> vars = new HashSet<>();
		for (ExtensionElem el : elements) {
				String name = el.getName();
				Variable var = ofac.getVariable(name);
				vars.add(var);
			}
		return vars;
	}
	
	private Set<Variable> getVariables(TupleExpr te) {
		Set<Variable> result = new LinkedHashSet<>();
		if (te instanceof StatementPattern) {
			result.addAll(getVariables(((StatementPattern) te).getVarList()));
		} 
		else if (te instanceof BinaryTupleOperator) {
			result.addAll(getVariables(((BinaryTupleOperator) te).getLeftArg()));
			result.addAll(getVariables(((BinaryTupleOperator) te).getRightArg()));
		} 
		else if (te instanceof UnaryTupleOperator) {
			if (te instanceof Extension) {
				result.addAll(getBindVariables(((Extension) te).getElements()));
			}
			result.addAll(getVariables(((UnaryTupleOperator) te).getArg()));
		} 
		else {
			throw new RuntimeException("Operator not supported: " + te);
		}
		return result;
	}
	
	private ValueConstant getConstant(LiteralImpl literal) {
		URI type = literal.getDatatype();
		COL_TYPE objectType = getDataType(literal);
		String value = literal.getLabel();
		ValueConstant constant = ofac.getConstantLiteral(value, objectType);

		/*
		 * Validating that the value is correct (lexically) with respect to the
		 * specified datatype
		 */
		
		if (type != null) {
			boolean valid = XMLDatatypeUtil.isValidValue(value, type);
			if (!valid)
				throw new RuntimeException(
						"Invalid lexical form for datatype. Found: " + value);
		}
		return constant;

	}


	private COL_TYPE getDataType(LiteralImpl node) {

		URI typeURI = node.getDatatype();
		// if null return literal, and avoid exception
		if (typeURI == null) 
			return COL_TYPE.LITERAL;
		
		COL_TYPE dataType = dtfac.getDatatype(typeURI);
        if (dataType == null) 
			throw new RuntimeException("Unsupported datatype: " + typeURI.stringValue());
		
        if (dataType == COL_TYPE.DECIMAL) { 
			// special case for decimal
			String value = node.getLabel().toString();
			if (!value.contains(".")) {
				// Put the type as integer (decimal without fractions).
				dataType = COL_TYPE.INTEGER;
			}
		} 
		return dataType;
	}

	private Term getBooleanTerm(ValueExpr expr) {
		if (expr instanceof Var) {
			return getVariableTerm((Var) expr);
		} else if (expr instanceof org.openrdf.query.algebra.ValueConstant) {
			return getConstantFunctionTerm((org.openrdf.query.algebra.ValueConstant) expr);
		} else if (expr instanceof UnaryValueOperator) {
			return getBuiltinFunctionTerm((UnaryValueOperator) expr);
		} else if (expr instanceof BinaryValueOperator) {
			if (expr instanceof Regex) { // sesame regex is Binary, Jena N-ary
				Regex reg = (Regex) expr;
				ValueExpr arg1 = reg.getLeftArg(); 
				ValueExpr arg2 = reg.getRightArg(); 
				ValueExpr flags = reg.getFlagsArg();
				Term term1 = getBooleanTerm(arg1);
				Term term2 = getBooleanTerm(arg2);
				Term term3 = (flags != null) ? getBooleanTerm(flags) : OBDAVocabulary.NULL;
				return ofac.getFunction(
						OBDAVocabulary.SPARQL_REGEX, term1, term2, term3);
			}
			BinaryValueOperator function = (BinaryValueOperator) expr;
			ValueExpr arg1 = function.getLeftArg(); // get the first argument
			ValueExpr arg2 = function.getRightArg(); // get the second argument
			Term term1 = getBooleanTerm(arg1);
			Term term2 = getBooleanTerm(arg2);
			// Construct the boolean function
			// TODO Change the method name because ExprFunction2 is not only for
			// boolean functions
			return getBooleanFunction(function, term1, term2);
		} else if (expr instanceof Bound){
			
			return ofac.getFunctionIsNotNull(getVariableTerm(((Bound) expr).getArg()));
		} else {
			throw new RuntimeException("The builtin function "
					+ expr.toString() + " is not supported yet!");
		}
	}
	
	private Term getVariableTerm(Var expr) {
		return getOntopTerm(expr, expr.getValue());
		
	}

	private Function getConstantFunctionTerm(org.openrdf.query.algebra.ValueConstant expr) {
		Function constantFunction = null;
		Value v = expr.getValue();

		if (v instanceof LiteralImpl) {
			LiteralImpl lit = (LiteralImpl)v;
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
					throw new RuntimeException("Undefiend datatype: " + tp);
			}
			ValueConstant constant = ofac.getConstantLiteral(constantString, tp);
			constantFunction = ofac.getTypedTerm(constant, tp);	
		} 
		else if (v instanceof URIImpl) {
            constantFunction = uriTemplateMatcher.generateURIFunction(v.stringValue());
            if (constantFunction.getArity() == 1)
                constantFunction = ofac.getUriTemplateForDatatype(((URIImpl) v).stringValue());
		}
		
		return constantFunction;
	}

	private Function getBuiltinFunctionTerm(UnaryValueOperator expr) {
		Function builtInFunction = null;
		if (expr instanceof Not) {
			ValueExpr arg = expr.getArg();
			Term term = getBooleanTerm(arg);
			builtInFunction = ofac.getFunctionNOT(term);
		}
		/*
		 * The following expressions only accept variable as the parameter
		 */

		else if (expr instanceof IsLiteral) {
			builtInFunction = ofac.getFunction(OBDAVocabulary.SPARQL_IS_LITERAL, getBooleanTerm( expr.getArg()));
			
		} else if (expr instanceof IsURI) {
			builtInFunction = ofac.getFunction(OBDAVocabulary.SPARQL_IS_URI, getBooleanTerm( expr.getArg()));
			
		} else if (expr instanceof Str) {
			builtInFunction = ofac.getFunction(OBDAVocabulary.SPARQL_STR, getBooleanTerm( expr.getArg()));
			
		} else if (expr instanceof Datatype) {
			builtInFunction = ofac.getFunction(OBDAVocabulary.SPARQL_DATATYPE, getBooleanTerm( expr.getArg()));
		
		} else if (expr instanceof IsBNode) {
			builtInFunction = ofac.getFunction(OBDAVocabulary.SPARQL_IS_BLANK, getBooleanTerm( expr.getArg()));
							
		} else if (expr instanceof Lang) {
			ValueExpr arg = expr.getArg();
			if (arg instanceof Var) {
				builtInFunction = ofac.getFunction(
						OBDAVocabulary.SPARQL_LANG,
						getVariableTerm((Var) arg));
			}
		} else {
			throw new RuntimeException("The builtin function "
					+ expr.toString() + " is not supported yet!");
		}
		return builtInFunction;
	}

	private Function getBooleanFunction(BinaryValueOperator expr, Term term1, Term term2) {
		// The AND and OR expression
		if (expr instanceof And) {
			return ofac.getFunctionAND(term1, term2);
		} 
		else if (expr instanceof Or) {
			return ofac.getFunctionOR(term1, term2);
		}
		// The other expressions
		else if (expr instanceof SameTerm){
			return ofac.getFunctionEQ(term1, term2);
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
		
		throw new IllegalStateException("getBooleanFunction does not understand the expression " + expr);
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
	
	public List<String> getSignature(ParsedQuery query) {
		List<String> signatureContainer = new LinkedList<>();
		if (query instanceof ParsedTupleQuery || query instanceof ParsedGraphQuery) {
			TupleExpr te = query.getTupleExpr();
			signatureContainer.addAll(te.getBindingNames());
		}
		return signatureContainer;
	}
	
//	public void getSignature(Query query, List<String> signatureContainer) {
//		signatureContainer.clear();
//		if (query.isSelectType() || query.isDescribeType()) {
//			signatureContainer.addAll(query.getResultVars());
//
//		} else if (query.isConstructType()) {
//			Template constructTemplate = query.getConstructTemplate();
//			for (Triple triple : constructTemplate.getTriples()) {
//				/*
//				 * Check if the subject, predicate, object is a variable.
//				 */
//				Node subject = triple.getSubject(); // subject
//				if (subject instanceof com.hp.hpl.jena.sparql.core.Var) {
//					String vs = ((com.hp.hpl.jena.sparql.core.Var) subject).getName();
//					signatureContainer.add(vs);
//				}
//				Node predicate = triple.getPredicate(); // predicate
//				if (predicate instanceof com.hp.hpl.jena.sparql.core.Var) {
//					String vs = ((com.hp.hpl.jena.sparql.core.Var) predicate).getName();
//					signatureContainer.add(vs);
//				}
//				Node object = triple.getObject(); // object
//				if (object instanceof com.hp.hpl.jena.sparql.core.Var) {
//					String vs = ((com.hp.hpl.jena.sparql.core.Var) object).getName();
//					signatureContainer.add(vs);
//				}
//			}
//		}
//	}

}
