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

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.UnsignedInteger;
import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.OBDAQueryModifiers.OrderCondition;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.TermUtils;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.UriTemplateMatcher;
import it.unibz.krdb.obda.parser.EncodeForURI;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
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
 * same semantics. We use the built-in predicate LeftJoin. The rules
 * in the program have always 1 or 2 operator atoms, plus (in)equality atoms
 * (due to filters).
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
	 * @param uriTemplateMatcher
	 * @param uriRef is used only in the Semantic Index mode
	 */
	
	public SparqlAlgebraToDatalogTranslator(UriTemplateMatcher uriTemplateMatcher, SemanticIndexURIMap uriRef) {
		this.uriTemplateMatcher = uriTemplateMatcher;
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

	public SparqlQuery translate(ParsedQuery pq) {
		
		TupleExpr te = pq.getTupleExpr();
		log.debug("SPARQL algebra: \n{}", te);

		List<String> signature;
		if (pq instanceof ParsedTupleQuery || pq instanceof ParsedGraphQuery)
			// order elements of the set in some way by converting it into the list
			signature = new ArrayList<>(te.getBindingNames());
		else
			signature = Collections.emptyList(); // ASK queries have no answer variables

		List<Term> answerVariables = new ArrayList<>(signature.size());
		for (String variable : signature)
			answerVariables.add(ofac.getVariable(variable));

		DatalogProgram program = ofac.getDatalogProgram();
		SubExpression body = translateTupleExpr(te, program, OBDAVocabulary.QUEST_QUERY + "0");

		CQIE top = createRule(program, OBDAVocabulary.QUEST_QUERY, answerVariables, body.atoms); // appends rule to the result

		SPARQLQueryFlattener flattener = new SPARQLQueryFlattener(program);
		List<CQIE> flattened = flattener.flatten(top);

		DatalogProgram result = ofac.getDatalogProgram(program.getQueryModifiers(), flattened);
		return new SparqlQuery(result, signature);
	}

	private static final class SubExpression {
		final Set<Variable> variables;
		final Set<Variable> nullableVariables;
		final List<Function> atoms;

		SubExpression(List<Function> atoms, Set<Variable> variables, Set<Variable> nullableVariables) {
			this.atoms = atoms;
			this.variables = variables;
			this.nullableVariables = nullableVariables;
		}
	}


	/**
	 * main translation method -- a recursive switch over all possible types of subexpressions
	 * 
	 * @param te
	 * @param pr
	 * @param newHeadName
	 */
	
	private SubExpression translateTupleExpr(TupleExpr te, DatalogProgram pr, String newHeadName) {
		if (te instanceof Slice) {
			// add LIMIT and OFFSET modifiers
			Slice slice = (Slice)te;
			pr.getQueryModifiers().setOffset(slice.getOffset());
			pr.getQueryModifiers().setLimit(slice.getLimit());
			return translateTupleExpr(slice.getArg(), pr, newHeadName); // narrow down the query
		} 
		else if (te instanceof Distinct) {
			// add DISTINCT modifier
			Distinct distinct = (Distinct) te;
			pr.getQueryModifiers().setDistinct();
			return translateTupleExpr(distinct.getArg(), pr, newHeadName); // narrow down the query
		}
		else if (te instanceof Reduced) {
			// ignore REDUCED modifier 
			Reduced reduced = (Reduced)te;
			return translateTupleExpr(reduced.getArg(), pr, newHeadName);
		}
		else if (te instanceof Order) {
			// add ORDER BY modifier
			Order order = (Order) te;
			for (OrderElem c : order.getElements()) {	
				ValueExpr expression = c.getExpr();
				if (!(expression instanceof Var)) {
					throw new IllegalArgumentException("Error translating ORDER BY. "
							+ "The current implementation can only sort by variables, this query has a more complex expression '" + expression + "'");
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
			Function atom = translate((StatementPattern) te);
			Set<Variable> vars = new HashSet<>();
			TermUtils.addReferencedVariablesTo(vars, atom);
			return new SubExpression(Collections.singletonList(atom), vars, Collections.emptySet());
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
		else if (te instanceof Extension) {
			return translate((Extension) te, pr, newHeadName);
		}
		else if (te instanceof BindingSetAssignment) {
			return getValuesFilter((BindingSetAssignment)te);
		}
		
		try {
			throw new QueryEvaluationException("Operation not supported: " + te);
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}


	private CQIE createRule(DatalogProgram pr, String headName, List<Term> headParameters, Function... body) {
		Predicate pred = ofac.getPredicate(headName, headParameters.size());
		Function head = ofac.getFunction(pred, headParameters);
		CQIE rule = ofac.getCQIE(head, body);
		pr.appendRule(rule);
		return rule;
	}

	private CQIE createRule(DatalogProgram pr, String headName, List<Term> headParameters, List<Function> body) {
		Predicate pred = ofac.getPredicate(headName, headParameters.size());
		Function head = ofac.getFunction(pred, headParameters);
		CQIE rule = ofac.getCQIE(head, body);
		pr.appendRule(rule);
		return rule;
	}

	private static Set<Variable> union(Set<Variable> s1, Set<Variable> s2) {
		Set<Variable> r = new HashSet<>();
		r.addAll(s1);
		r.addAll(s2);
		return r;
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
	
	private SubExpression translate(Extension extend, DatalogProgram pr, String newHeadName) {

		SubExpression subAtom = translateTupleExpr(extend.getArg(), pr, newHeadName + "0");

		int sz = subAtom.variables.size() + extend.getElements().size();
		List<Term> varList = new ArrayList<>(sz);
		varList.addAll(subAtom.variables);
		Set<Variable> varSet = new HashSet<>();
		varSet.addAll(subAtom.variables);
		List<Term> termList = new ArrayList<>(sz);
		termList.addAll(varList);
		
		for (ExtensionElem el: extend.getElements()) {
			Variable var = ofac.getVariable(el.getName());
			varList.add(var);
			varSet.add(var);
			
			Term term = getExpression(el.getExpr());			
			termList.add(term);
		}
		CQIE rule = createRule(pr, newHeadName, termList, subAtom.atoms);
		
		Function newHeadAtom = ofac.getFunction(rule.getHead().getFunctionSymbol(), varList);
		// TODO: double check nullable variables
		return new SubExpression(Collections.singletonList(newHeadAtom), varSet, subAtom.nullableVariables);
	}


	/**
	 * EXPR_1 UNION EXPR_2
	 * 
	 * adds the following two rules
	 * 
	 * ans_i(X * X_1 * NULL_2) :- ans_{i.0}(X * X_1)
	 * ans_i(X * NULL_1 * X_2) :- ans_{i.1}(X * X_2)
	 * 
	 * where NULL_i is the padding of X_i with NULLs 
	 * 
	 * @param union
	 * @param pr
	 * @param newHeadName
	 * @return 
	 */
	
	private SubExpression translate(Union union, DatalogProgram pr, String  newHeadName) {

		SubExpression left = translateTupleExpr(union.getLeftArg(), pr, newHeadName + "0");
		SubExpression right = translateTupleExpr(union.getRightArg(), pr, newHeadName + "1");

		Set<Variable> vars = union(left.variables, right.variables);
		List<Term> varList = new ArrayList<>(vars);

		List<Term> leftTermList = new ArrayList<>(varList.size());
		List<Term> rightTermList = new ArrayList<>(varList.size());
		for (Term v : varList) {
			Term ltl =  left.variables.contains(v) ? v : OBDAVocabulary.NULL;
			leftTermList.add(ltl);

			Term ltr =  right.variables.contains(v) ? v : OBDAVocabulary.NULL;
			rightTermList.add(ltr);
		}
		CQIE leftRule = createRule(pr, newHeadName, leftTermList, left.atoms);
		CQIE rightRule = createRule(pr, newHeadName, rightTermList, right.atoms);
		
		Function atom = ofac.getFunction(rightRule.getHead().getFunctionSymbol(), varList);
		// TODO: double-check nullable variables
		return new SubExpression(Collections.singletonList(atom), vars, Collections.emptySet());
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
	
	private SubExpression translate(Join join, DatalogProgram pr, String  newHeadName)  {

		SubExpression left = translateTupleExpr(join.getLeftArg(), pr, newHeadName + "0");
		SubExpression right = translateTupleExpr(join.getRightArg(), pr, newHeadName + "1");

		Set<Variable> vars = union(left.variables, right.variables);

		//CQIE rule = createRule(pr, newHeadName, new ArrayList<>(vars), left.atoms, right.atoms);
		LinkedList<Function> list = new LinkedList<>();
		list.addAll(left.atoms);
		list.addAll(right.atoms);

		// TODO: double-check nullable variables
		return new SubExpression(list, vars, Collections.emptySet());
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

	private SubExpression translate(LeftJoin leftjoin, DatalogProgram pr, String  newHeadName) {

		SubExpression left = translateTupleExpr(leftjoin.getLeftArg(), pr, newHeadName + "0");
		SubExpression right = translateTupleExpr(leftjoin.getRightArg(), pr, newHeadName + "1");

		Function leftAtom;
		if (left.atoms.size() > 1 || left.atoms.get(0).isAlgebraFunction())
			leftAtom = createRule(pr, newHeadName + "0", new ArrayList<Term>(left.variables), left.atoms).getHead();
		else
			leftAtom = left.atoms.get(0);

		Function rightAtom;
		if (right.atoms.size() > 1 || right.atoms.get(0).isAlgebraFunction())
			rightAtom = createRule(pr, newHeadName + "1", new ArrayList<Term>(right.variables), right.atoms).getHead();
		else
			rightAtom = right.atoms.get(0);

		// the left join atom
		Function joinAtom = ofac.getSPARQLLeftJoin(leftAtom, rightAtom);

		Set<Variable> vars = union(left.variables, right.variables);

		// adding the conditions of the filter for the LeftJoin 
		ValueExpr filter = leftjoin.getCondition();
		if (filter != null) {
			SubExpression filterSub = getFilterExpression(filter, vars);
			joinAtom.getTerms().addAll(filterSub.atoms);
			vars = filterSub.variables;
		}

		// TODO: double-check nullable variables
		return new SubExpression(Collections.singletonList(joinAtom), vars, Collections.emptySet());
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

	private SubExpression translate(Projection project, DatalogProgram pr, String  newHeadName) {

		SubExpression sub = translateTupleExpr(project.getArg(), pr, newHeadName + "0");
		Set<Variable> vars = new HashSet<>();

		List<ProjectionElem> projectionElements = project.getProjectionElemList().getElements();
		for (ProjectionElem pe : projectionElements)  {
			// we assume here that the target name is "introduced" as one of the arguments of atom
			// (this is normally done by an EXTEND inside the PROJECTION)
			// first, we check whether this assumption can be made
			if (!pe.getSourceName().equals(pe.getTargetName())) {
				Variable t = ofac.getVariable(pe.getSourceName());
				if (!sub.variables.contains(t))
					throw new RuntimeException("Projection source of " + pe + " not found in " + project.getArg());
			}
			vars.add(ofac.getVariable(pe.getTargetName()));
		}

		CQIE rule = createRule(pr, newHeadName, new  ArrayList<>(vars), sub.atoms);
		// TODO: double-check nullable variables
		return new SubExpression(Collections.singletonList(rule.getHead()), vars, Collections.emptySet());
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
	private SubExpression translate(Filter filter, DatalogProgram pr, String  newHeadName) {

		SubExpression sub = translateTupleExpr(filter.getArg(), pr, newHeadName + "0");

		SubExpression filterSub  = getFilterExpression(filter.getCondition(), new HashSet<>(sub.variables));

		//CQIE rule = createRule(pr, newHeadName, new ArrayList<>(filterSub.variables), sub.atom, filterSub.atom);
		List<Function> atoms = new LinkedList<>();
		atoms.addAll(sub.atoms);
		atoms.addAll(filterSub.atoms);
		// TODO: double-check nullable variables
		return new SubExpression(atoms, filterSub.variables, Collections.emptySet());
	}

	private SubExpression getFilterExpression(ValueExpr condition, Set<Variable> vars) {
		Function filterAtom;
		if (condition instanceof Var)
			filterAtom = ofac.getFunctionIsTrue(getOntopTerm((Var) condition));
		else
			filterAtom = (Function) getExpression(condition);

		TermUtils.addReferencedVariablesTo(vars, filterAtom);
		return new SubExpression(Collections.singletonList(filterAtom), vars, Collections.emptySet());
	}

	/***
	 * This translates a single triple. 
	 * 
	 * @param triple
	 * @return
	 */
	private Function translate(StatementPattern triple) {
		
		Term sTerm = getOntopTerm(triple.getSubjectVar());

		Value p = triple.getPredicateVar().getValue();
		if (p == null) {
			// term variable term .
			Term oTerm = getOntopTerm(triple.getObjectVar());
			return ofac.getTripleAtom(sTerm, ofac.getVariable(triple.getPredicateVar().getName()), oTerm);
		}
		else if (p instanceof URI) {
			if (p.equals(RDF.TYPE)) {
				Value o = triple.getObjectVar().getValue();
				if (o == null) {
					// term a variable .
					Function rdfTypeConstant = ofac.getUriTemplate(ofac.getConstantLiteral(OBDAVocabulary.RDF_TYPE));
					return ofac.getTripleAtom(sTerm, rdfTypeConstant, ofac.getVariable(triple.getObjectVar().getName()));
				}
				else if (o instanceof URI) {
					// term a uri .
					Predicate.COL_TYPE type = dtfac.getDatatype((URI)o);
					if (type != null) // datatype
						return ofac.getFunction(dtfac.getTypePredicate(type), sTerm);
					else // class
						return ofac.getFunction(ofac.getClassPredicate(o.stringValue()), sTerm);
				}
				else
					throw new RuntimeException("Unsupported query syntax");
			}
			else {
				// term uri term . (where uri is either an object or a datatype property)
				Term oTerm = getOntopTerm(triple.getObjectVar());
				Predicate predicate = ofac.getPredicate(p.stringValue(), new COL_TYPE[] { null, null });
				return ofac.getFunction(predicate, sTerm, oTerm);
			}
		}
		else {
			// if predicate is a variable or literal
			throw new RuntimeException("Unsupported query syntax");
		}
	}
	
	private Term getOntopTerm(Var term) {
		Value s = term.getValue();
		if (s == null) {
			return ofac.getVariable(term.getName());
		} 
		else if (s instanceof Literal) {
			Literal literal = (Literal) s;
			URI typeURI = literal.getDatatype();
			String value = literal.getLabel();
	
			COL_TYPE type;
			if (typeURI == null)
				type = COL_TYPE.LITERAL;
			else {
				type = dtfac.getDatatype(typeURI);
		        if (type == null)
					throw new RuntimeException("Unsupported datatype: " + typeURI.stringValue());

				// check if the value is (lexically) correct for the specified datatype
				if (!XMLDatatypeUtil.isValidValue(value, typeURI))
					throw new RuntimeException("Invalid lexical form for datatype. Found: " + value);

				// special case for decimal
				if ((type == COL_TYPE.DECIMAL) && !value.contains("."))
					// put the type as integer (decimal without fractions)
					type = COL_TYPE.INTEGER;
			}
			

			ValueConstant constant = ofac.getConstantLiteral(value, type);

			// v1.7: We extend the syntax such that the data type of a
			// constant is defined using a functional symbol.
			if (type == COL_TYPE.LITERAL) {
				// if the object has type LITERAL, check the language tag
				String lang = literal.getLanguage();
				if (lang != null && !lang.equals(""))
					return ofac.getTypedTerm(constant, lang);
				else
					return ofac.getTypedTerm(constant, type);
			} 
			else {
				return ofac.getTypedTerm(constant, type);
			}
		} 
		else if (s instanceof URI) {
			if (uriRef != null) {
				// if in the Semantic Index mode 
				int id = uriRef.getId(s.stringValue());
				return ofac.getUriTemplate(ofac.getConstantLiteral(String.valueOf(id), COL_TYPE.INTEGER));
			} 
			else {
				String subject_URI = EncodeForURI.decodeURIEscapeCodes(s.stringValue());
				return uriTemplateMatcher.generateURIFunction(subject_URI);
			}
		}
		throw new RuntimeException("Unsupported term " + term);
	}

	/**
	 * Creates a "FILTER" atom out of VALUES bindings.
	 */
	private SubExpression getValuesFilter(BindingSetAssignment expression) {
		Set<Variable> vars = new HashSet<>();

		Function valuesFilter = null;
		for (BindingSet bindingSet : expression.getBindingSets()) {

			Function bindingFilter = null;
			for (Binding binding : bindingSet) {
				Variable variable = ofac.getVariable(binding.getName());
				vars.add(variable);
				Term value = getConstantExpression(binding.getValue());
				Function eqFilter = ofac.getFunctionEQ(variable, value);
				bindingFilter = (bindingFilter == null) ?
						eqFilter :
						ofac.getFunctionAND(bindingFilter, eqFilter);
			}

			// the empty conjunction is TRUE
			if (bindingFilter == null)
				bindingFilter = ofac.getFunctionIsTrue(ofac.getBooleanConstant(true));

			valuesFilter = (valuesFilter == null) ?
					bindingFilter :
					ofac.getFunctionOR(valuesFilter, bindingFilter);
		}

		// the empty disjunction is FALSE
		if (valuesFilter == null)
			valuesFilter = ofac.getFunctionIsTrue(ofac.getBooleanConstant(false));

		// TODO: double-check nullable variables
		return new SubExpression(Collections.singletonList(valuesFilter), vars, Collections.emptySet());
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
        Term topConcat = getExpression(iterator.next());
        
        if (!iterator.hasNext())
            throw new UnsupportedOperationException("Wrong number of arguments (found " + values.size() + 
            					", at least 1) of SQL function CONCAT");
 	  	
        while (iterator.hasNext()) {
            Term second_string = getExpression(iterator.next());
            topConcat = ofac.getFunctionConcat(topConcat, second_string);                	
        }
        
        return topConcat;		
	}
	
	private Term getSubstring(List<ValueExpr> args) {

		if (args.size() == 2) {
			Term str = getExpression(args.get(0));
			Term st = getExpression(args.get(1));
			return ofac.getFunctionSubstring(str, st);
		}
		else if (args.size() == 3) {
			Term str = getExpression(args.get(0));
			Term st = getExpression(args.get(1));
			Term en = getExpression(args.get(2));
			return ofac.getFunctionSubstring(str, st, en);
		}
		else 
			throw new UnsupportedOperationException("Wrong number of arguments (found "
					+ args.size() + ", only 2 or 3 supported) for SPARQL function SUBSTRING");
	}
		
	
	private Term getReplace(List<ValueExpr> args) {
		
		if (args.size() == 2) {
            Term t1 = getExpression(args.get(0));
            Term out_string = getExpression(args.get(1));
            Term in_string = ofac.getConstantLiteral("");
            return ofac.getFunctionReplace(t1, out_string, in_string);
		}
		else if (args.size() == 3) {
            Term t1 = getExpression(args.get(0));
            Term out_string = getExpression(args.get(1));
            Term in_string = getExpression(args.get(2));
            return ofac.getFunctionReplace(t1, out_string, in_string);
		}
        else
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
            		+ args.size() + ", only 2 or 3 supported) to SPARQL function REPLACE");
	}
	
	// XPath 1.0 functions (XPath 1.1 has variants with more arguments)
	private static final ImmutableMap<String, OperationPredicate> XPathFunctions =
				new ImmutableMap.Builder<String, OperationPredicate>()
						.put("http://www.w3.org/2005/xpath-functions#upper-case", ExpressionOperation.UCASE)
						.put("http://www.w3.org/2005/xpath-functions#lower-case", ExpressionOperation.LCASE)
						.put("http://www.w3.org/2005/xpath-functions#string-length", ExpressionOperation.STRLEN) 
						.put("http://www.w3.org/2005/xpath-functions#substring-before", ExpressionOperation.STRBEFORE) 
						.put("http://www.w3.org/2005/xpath-functions#substring-after", ExpressionOperation.STRAFTER) 
						.put("http://www.w3.org/2005/xpath-functions#starts-with", ExpressionOperation.STR_STARTS) 
						.put("http://www.w3.org/2005/xpath-functions#ends-with", ExpressionOperation.STR_ENDS) 
						.put("http://www.w3.org/2005/xpath-functions#encode-for-uri", ExpressionOperation.ENCODE_FOR_URI) 
						.put("http://www.w3.org/2005/xpath-functions#contains", ExpressionOperation.CONTAINS) 
						.put("UUID", ExpressionOperation.UUID) 
						.put("STRUUID", ExpressionOperation.STRUUID) 

						.put("http://www.w3.org/2005/xpath-functions#numeric-abs", ExpressionOperation.ABS) 
						.put("http://www.w3.org/2005/xpath-functions#numeric-ceil", ExpressionOperation.CEIL) 
						.put("http://www.w3.org/2005/xpath-functions#numeric-floor", ExpressionOperation.FLOOR) 
						.put("http://www.w3.org/2005/xpath-functions#numeric-round", ExpressionOperation.ROUND) 
						.put("RAND", ExpressionOperation.RAND) 
						
						.put("http://www.w3.org/2005/xpath-functions#year-from-dateTime", ExpressionOperation.YEAR) 
						.put("http://www.w3.org/2005/xpath-functions#day-from-dateTime", ExpressionOperation.DAY) 
						.put("http://www.w3.org/2005/xpath-functions#month-from-dateTime", ExpressionOperation.MONTH) 
						.put("http://www.w3.org/2005/xpath-functions#hours-from-dateTime", ExpressionOperation.HOURS) 
						.put("http://www.w3.org/2005/xpath-functions#minutes-from-dateTime", ExpressionOperation.MINUTES) 
						.put("http://www.w3.org/2005/xpath-functions#seconds-from-dateTime", ExpressionOperation.SECONDS) 
						.put("NOW", ExpressionOperation.NOW) 
						.put("TZ", ExpressionOperation.TZ) 
					
						.put("MD5", ExpressionOperation.MD5) 
						.put("SHA1", ExpressionOperation.SHA1) 
						.put("SHA256", ExpressionOperation.SHA256) 
						.put("SHA512", ExpressionOperation.SHA512) 
						.build();
	
	
	
	
	
    /** Return the Functions supported at the moment only
     * concat and replace
     * @param expr
     * @return
     */
    private Term getFunctionCallTerm(FunctionCall expr) {
    	
    	OperationPredicate p = XPathFunctions.get(expr.getURI());
    	if (p != null) {
    		List<ValueExpr> args = expr.getArgs();
    		if (args.size() != p.getArity()) {
                throw new UnsupportedOperationException(
                		"Wrong number of arguments (found " + args.size() + ", only " +
                			 p.getArity() + "supported) for SPARQL " + expr.getURI() + "function");					
    		}
    		List<Term> terms = new ArrayList<>(args.size());
    		for (ValueExpr a : args)
    			terms.add(getExpression(a));
    		Term fun = ofac.getFunction(p, terms);
    		return fun;   		
    	}
    	
    	// these are all special cases with **variable** number of arguments
  
        switch (expr.getURI()) {
        	// at least one argument 
            case "http://www.w3.org/2005/xpath-functions#concat":
                return getConcat(expr.getArgs());

            // 3 or 4 arguments (ROMAN 16 Dec 2015): check the actual implementation
            case "http://www.w3.org/2005/xpath-functions#replace":
                return getReplace(expr.getArgs());
                
            // 2 or 3 arguments    
            case "http://www.w3.org/2005/xpath-functions#substring":
            	return getSubstring(expr.getArgs()); 
            	
            default:
                throw new RuntimeException("Function " + expr.getURI() + " is not supported yet!");
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
				if (tp == null)
					return ofac.getUriTemplateForDatatype(type.stringValue());
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
				case DECIMAL:
					constantString = lit.decimalValue().toString();
					break;
				case INT:
				case UNSIGNED_INT:
					constantString = Integer.toString(lit.intValue());
					break;
				case LONG:
					constantString = Long.toString(lit.longValue());
					break;
				case FLOAT:
					constantString = Float.toString(lit.floatValue());
					break;
				case DOUBLE:
					constantString = Double.toString(lit.doubleValue());
					break;
				case BOOLEAN:
					constantString = Boolean.toString(lit.booleanValue());
					break;
				case DATETIME_STAMP:
				case DATETIME:
				case YEAR:
				case DATE:
				case TIME:
					constantString = lit.calendarValue().toString();
					break;
				case STRING:
				case LITERAL:
					constantString = lit.stringValue();
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
			return ofac.getFunction(ExpressionOperation.IS_LITERAL, term);	
		} 
		else if (expr instanceof IsURI) {
			return ofac.getFunction(ExpressionOperation.IS_IRI, term);
		} 
		else if (expr instanceof Str) {
			return ofac.getFunction(ExpressionOperation.SPARQL_STR, term);
		} 
		else if (expr instanceof Datatype) {
			return ofac.getFunction(ExpressionOperation.SPARQL_DATATYPE, term);
		} 
		else if (expr instanceof IsBNode) {
			return ofac.getFunction(ExpressionOperation.IS_BLANK, term);
		} 	
		else if (expr instanceof Lang) {
			ValueExpr arg = expr.getArg();
			if (arg instanceof Var) 
				return ofac.getFunction(ExpressionOperation.SPARQL_LANG, term);
			else
				throw new RuntimeException("A variable or a value is expected in " + expr);
		}
		
		throw new RuntimeException("The expression " + expr + " is not supported yet!");
	}
	
	private static final ImmutableMap<Compare.CompareOp, ExpressionOperation> relationalOperations = 
			new ImmutableMap.Builder<Compare.CompareOp, ExpressionOperation>()
				.put(Compare.CompareOp.EQ, ExpressionOperation.EQ)
				.put(Compare.CompareOp.GE, ExpressionOperation.GTE)
				.put(Compare.CompareOp.GT, ExpressionOperation.GT)
				.put(Compare.CompareOp.LE, ExpressionOperation.LTE)
				.put(Compare.CompareOp.LT, ExpressionOperation.LT)
				.put(Compare.CompareOp.NE, ExpressionOperation.NEQ)
				.build();

	private static final ImmutableMap<MathExpr.MathOp, ExpressionOperation> numericalOperations = 
			new ImmutableMap.Builder<MathExpr.MathOp, ExpressionOperation>()
			.put(MathExpr.MathOp.PLUS, ExpressionOperation.ADD)
			.put(MathExpr.MathOp.MINUS, ExpressionOperation.SUBTRACT)
			.put(MathExpr.MathOp.MULTIPLY, ExpressionOperation.MULTIPLY)
			.put(MathExpr.MathOp.DIVIDE, ExpressionOperation.DIVIDE)
			.build();
			
	
	private Term getBinaryExpression(BinaryValueOperator expr) {
		
		Term term1 = getExpression(expr.getLeftArg());
		Term term2 = getExpression(expr.getRightArg());
		
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
			return ofac.getFunction(ExpressionOperation.REGEX, term1, term2, term3);
		}
		else if (expr instanceof Compare) {
			ExpressionOperation p = relationalOperations.get(((Compare) expr).getOperator());
			return ofac.getFunction(p, term1, term2);
		} 
		else if (expr instanceof MathExpr) {
			ExpressionOperation p = numericalOperations.get(((MathExpr)expr).getOperator());
			return ofac.getFunction(p, term1, term2);
		} 
		else if (expr instanceof LangMatches) {
			if (term2 instanceof Function) {
				Function f = (Function) term2;
				if (f.isDataTypeFunction()) {
					Term functionTerm = f.getTerm(0);
					if (functionTerm instanceof Constant) {
						Constant c = (Constant) functionTerm;
						term2 = ofac.getFunction(f.getFunctionSymbol(),
								ofac.getConstantLiteral(c.getValue().toLowerCase(),
										c.getType()));
					}
				}
			}

			return ofac.getLANGMATCHESFunction(term1, term2);
		} 
		
		throw new RuntimeException("The expression " + expr + " is not supported yet!");
	}
}
