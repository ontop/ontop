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
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.Substitution;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.SubstitutionUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.UriTemplateMatcher;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.MathExpr.MathOp;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/***
 * Translate a SPARQL algebra expression into a Datalog program that has the
 * same semantics. We use the built-int predicates Join and Left join. The rules
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
	

	public DatalogProgram translate(ParsedQuery pq, List<String> signature) {
		TupleExpr te = pq.getTupleExpr();

		log.debug("SPARQL algebra: \n{}", te);
		DatalogProgram result = ofac.getDatalogProgram();

		// Render the variable names in the signature into Variable object
		List<Variable> vars = new LinkedList<Variable>();
		for (String vs : signature) {
			vars.add(ofac.getVariable(vs));
		}
		int[] freshvarcount = { 1 };

		translate(vars, te, result, 1, freshvarcount);
		return result;
	}
	
	/**
	 * Translate a given SPARQL query object to datalog program.
	 * 
	 *
	 *            The Query object.
	 * @return Datalog program that represents the construction of the SPARQL
	 *         query.
	 */
//	public DatalogProgram translate(Query arqQuery, List<String> signature) {
//
//		Op op = Algebra.compile(arqQuery);
//
//		log.debug("SPARQL algebra: \n{}", op);
//
//		DatalogProgram result = ofac.getDatalogProgram();
//
//		// Render the variable names in the signature into Variable object
//		List<Variable> vars = new LinkedList<Variable>();
//		for (String vs : signature) {
//			vars.add(ofac.getVariable(vs));
//		}
//
//		int[] freshvarcount = { 1 };
//
//		//translate(vars, op, result, 1, freshvarcount);
//		return result;
//	}

	private void translate(List<Variable> vars, TupleExpr te,
			DatalogProgram pr, long i, int[] varcount) {
		if (te instanceof Slice) {

			// Add LIMIT and OFFSET modifiers, if any
			Slice slice = (Slice) te;
			translate(vars, slice, pr, i, varcount);

		} else if (te instanceof Distinct) {

			// Add DISTINCT modifier, if any
			Distinct distinct = (Distinct) te;
			translate(vars, distinct, pr, i, varcount);

		} else if (te instanceof Projection) {

			// Add PROJECTION modifier, if any
			Projection project = (Projection) te;
			translate(vars, project, pr, i, varcount);

		} else if (te instanceof Order) {

			// Add ORDER BY modifier, if any
			Order order = (Order) te;
			translate(vars, order, pr, i, varcount);

		} else if (te instanceof Filter) {
			Filter filter = (Filter) te;
			translate(vars, filter, pr, i, varcount);

		} else if (te instanceof StatementPattern) {

			StatementPattern stmp = (StatementPattern) te;
			translate(vars, stmp, pr, i, varcount);

		} else if (te instanceof Join) {
			Join join = (Join) te;
			translate(vars, join, pr, i, varcount);

		} else if (te instanceof Union) {
			Union union = (Union) te;
			translate(vars, union, pr, i, varcount);

		} else if (te instanceof LeftJoin) {
			LeftJoin join = (LeftJoin) te;
			translate(vars, join, pr, i, varcount);
		
		} else if (te instanceof Reduced) {
			translate(vars, ((Reduced) te).getArg(), pr, i, varcount);
		
		} else if (te instanceof Extension) { 
			Extension extend = (Extension) te;
			translate(vars, extend, pr, i, varcount);

       } else {

			try {
				throw new QueryEvaluationException("Operation not supported: "
						+ te.toString());
			} catch (QueryEvaluationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void translate(List<Variable> vars, Extension extend,
			DatalogProgram pr, long i, int[] varcount) {
		TupleExpr subte = extend.getArg();
		List<ExtensionElem> elements = extend.getElements();
		Set<Variable> atom2VarsSet = null;
        List<Term> atom2VarsList = new LinkedList<Term>();
		for (ExtensionElem el: elements) {
			Variable var = null;
			
			String name = el.getName();
			ValueExpr vexp = el.getExpr();
			var = ofac.getVariable(name);
			
			Term term = getBooleanTerm(vexp);

			Set<Variable> atom1VarsSet = getVariables(subte);
			List<Term> atom1VarsList = new LinkedList<Term>();

            atom1VarsSet.add(var);
			atom1VarsList.addAll(atom1VarsSet);
			Collections.sort(atom1VarsList, comparator);
			int indexOfvar = atom1VarsList.indexOf(var);
			atom1VarsList.set(indexOfvar,term);
			Predicate leftAtomPred = ofac.getPredicate("ans" + (i),
					atom1VarsList.size());
			Function head = ofac.getFunction(leftAtomPred, atom1VarsList);
		
			atom2VarsSet = getVariables(subte);

			atom2VarsList.addAll(atom2VarsSet);
			Collections.sort(atom2VarsList, comparator);
			Predicate rightAtomPred = ofac.getPredicate("ans" + ((2 * i)),
					atom2VarsList.size());
			Function rightAtom = ofac.getFunction(rightAtomPred, atom2VarsList);

			CQIE newrule = ofac.getCQIE(head, rightAtom);
			pr.appendRule(newrule);
		}
		/*
		 * Translating the rest
		 */
	
		{
			List<Variable> vars1 = new LinkedList<Variable>();
			for (Term var1 : atom2VarsList)
				vars1.add((Variable) var1);
			translate(vars1, subte, pr, 2 * i, varcount);
		}
	}		    
//		    VarExprList extendExpr = extend.getVarExprList();
//		    Map<Var, Expr> varmap = extendExpr.getExprs();
//		    Variable var = null;
//		    Expr exp = null;
//		    
//		    for (Var v: varmap.keySet()){
//		      String name = v.getVarName();
//		      var = ofac.getVariable(name);
//		      exp = varmap.get(v);
//		    }
//		    
//		    Term term = getTermFromExpression(exp);
//		    
//		    Set<Variable> atom1VarsSet = getVariables(subop);
//		    List<Term> atom1VarsList = new LinkedList<Term>();
//		    atom1VarsList.addAll(atom1VarsSet);
//		    atom1VarsList.add(var);
//		    Collections.sort(atom1VarsList, comparator);
//		    int indexOfvar = atom1VarsList.indexOf(var);
//		    atom1VarsList.set(indexOfvar,term);
//		    Predicate leftAtomPred = ofac.getPredicate("ans" + (i),
//		        atom1VarsList.size());
//		    Function head = ofac.getFunction(leftAtomPred, atom1VarsList);
//		
//		    
//		    Set<Variable> atom2VarsSet = getVariables(subop);
//		    List<Term> atom2VarsList = new LinkedList<Term>();
//		    atom2VarsList.addAll(atom2VarsSet);
//		    Collections.sort(atom2VarsList, comparator);
//		    Predicate rightAtomPred = ofac.getPredicate("ans" + ((2 * i)),
//		        atom2VarsList.size());
//		    Function rightAtom = ofac.getFunction(rightAtomPred, atom2VarsList);
//		    
//		    
//		    
//		    CQIE newrule = ofac.getCQIE(head, rightAtom);
//		    pr.appendRule(newrule);
//		    
//		    /*
//		     * Translating the rest
//		     */
//		    {
//		      List<Variable> vars1 = new LinkedList<Variable>();
//		      for (Term var1 : atom2VarsSet)
//		        vars1.add((Variable) var1);
//		      translate(vars1, subop, pr, 2 * i, varcount);
//		    }

	private void translate(List<Variable> vars, Union union,
			DatalogProgram pr, long i, int[] varcount) {
		TupleExpr left = union.getLeftArg();
		TupleExpr right = union.getRightArg();

		/* Preparing the two atoms */

		Set<Variable> atom1VarsSet = getVariables(left);
		List<Term> atom1VarsList = new LinkedList<Term>();
		atom1VarsList.addAll(atom1VarsSet);
		Collections.sort(atom1VarsList, comparator);
		Predicate leftAtomPred = ofac.getPredicate("ans" + (2 * i),
				atom1VarsList.size());
		Function leftAtom = ofac.getFunction(leftAtomPred, atom1VarsList);

		Set<Variable> atom2VarsSet = getVariables(right);
		List<Term> atom2VarsList = new LinkedList<Term>();
		atom2VarsList.addAll(atom2VarsSet);
		Collections.sort(atom2VarsList, comparator);
		Predicate rightAtomPred = ofac.getPredicate("ans" + ((2 * i) + 1),
				atom2VarsList.size());
		Function rightAtom = ofac.getFunction(rightAtomPred, atom2VarsList);

		/* Preparing the head of the Union rules (2 rules) */
		// Collections.sort(vars, comparator);
		List<Term> headVars = new LinkedList<Term>();
//		for (Variable var : vars) {
			headVars.addAll(vars);
//		}

		Predicate answerPred = ofac.getPredicate("ans" + i, vars.size());
		Function head = ofac.getFunction(answerPred, headVars);

		/*
		 * Adding the UNION to the program, i.e., two rules Note, we need to
		 * make null any head variables that do not appear in the body of the
		 * uniones, e.g,
		 * 
		 * q(x,y,z) <- Union(R(x,y), R(x,z))
		 * 
		 * results in
		 * 
		 * q(x,y,null) :- ... R(x,y) ... q(x,null,z) :- ... R(x,z) ...
		 */

		// finding out null
		Set<Variable> nullVars = new HashSet<Variable>();
		nullVars.addAll(vars);
		nullVars.removeAll(atom1VarsSet); // the remaining variables do not
											// appear in the body assigning
											// null;
		Substitution nullifier = SubstitutionUtilities.getNullifier(nullVars);
		// making the rule
		CQIE newrule1 = ofac.getCQIE(head, leftAtom);
		pr.appendRule(SubstitutionUtilities.applySubstitution(newrule1, nullifier));

		// finding out null
		nullVars = new HashSet<Variable>();
		nullVars.addAll(vars);
		nullVars.removeAll(atom2VarsSet); // the remaining variables do not
											// appear in the body assigning
											// null;
		nullifier = SubstitutionUtilities.getNullifier(nullVars);
		// making the rule
		CQIE newrule2 = ofac.getCQIE(head, rightAtom);
		pr.appendRule(SubstitutionUtilities.applySubstitution(newrule2, nullifier));

		/*
		 * Translating the rest
		 */
		{
			List<Variable> vars1 = new LinkedList<Variable>();
			for (Term var : atom1VarsList)
				vars1.add((Variable) var);
			translate(vars1, left, pr, 2 * i, varcount);
		}
		{
			List<Variable> vars2 = new LinkedList<Variable>();
			for (Term var : atom2VarsList)
				vars2.add((Variable) var);
			translate(vars2, right, pr, 2 * i + 1, varcount);
		}

	}

	private void translate(List<Variable> vars, Join join, DatalogProgram pr,
			long i, int[] varcount) {
		TupleExpr left = join.getLeftArg();
		TupleExpr right = join.getRightArg();

		/* Preparing the two atoms */

		Set<Variable> atom1VarsSet = getVariables(left);
		List<Term> atom1VarsList = new LinkedList<Term>();
		atom1VarsList.addAll(atom1VarsSet);
		Collections.sort(atom1VarsList, comparator);
		Predicate leftAtomPred = ofac.getPredicate("ans" + (2 * i),
				atom1VarsList.size());
		Function leftAtom = ofac.getFunction(leftAtomPred, atom1VarsList);

		Set<Variable> atom2VarsSet = getVariables(right);
		List<Term> atom2VarsList = new LinkedList<Term>();
		atom2VarsList.addAll(atom2VarsSet);
		Collections.sort(atom2VarsList, comparator);
		Predicate rightAtomPred = ofac.getPredicate("ans" + ((2 * i) + 1),
				atom2VarsList.size());
		Function rightAtom = ofac.getFunction(rightAtomPred, atom2VarsList);
		/* The join, this is no longer necessary, we will try to avoid explicit joins
		as much as poosible, just use comma */
//		Predicate joinp = OBDAVocabulary.SPARQL_JOIN;
//		Function joinAtom = ofac.getFunction(joinp, leftAtom, rightAtom);

		/* Preparing the head of the Join rule */

		List<Term> headVars = new LinkedList<Term>();
//		for (Variable var : vars) {
			headVars.addAll(vars);
//		}

		Predicate answerPred = ofac.getPredicate("ans" + i, vars.size());
		Function head = ofac.getFunction(answerPred, headVars);

		/*
		 * Adding the join to the program
		 */

		CQIE newrule = ofac.getCQIE(head, leftAtom, rightAtom);
		pr.appendRule(newrule);

		/*
		 * Translating the rest
		 */
		{
			List<Variable> vars1 = new LinkedList<Variable>();
			for (Term var : atom1VarsList)
				vars1.add((Variable) var);
			translate(vars1, left, pr, 2 * i, varcount);
		}
		{
			List<Variable> vars2 = new LinkedList<Variable>();
			for (Term var : atom2VarsList)
				vars2.add((Variable) var);
			translate(vars2, right, pr, 2 * i + 1, varcount);
		}
	}

	private void translate(List<Variable> vars, LeftJoin join,
			DatalogProgram pr, long i, int[] varcount) {
		TupleExpr left = join.getLeftArg();
		TupleExpr right = join.getRightArg();
		ValueExpr filter = join.getCondition();

		/* Preparing the two atoms */

		Set<Variable> atom1VarsSet = getVariables(left);
		List<Term> atom1VarsList = new LinkedList<Term>();
		atom1VarsList.addAll(atom1VarsSet);
		Collections.sort(atom1VarsList, comparator);
		Predicate leftAtomPred = ofac.getPredicate("ans" + (2 * i),
				atom1VarsList.size());
		Function leftAtom = ofac.getFunction(leftAtomPred, atom1VarsList);

		Set<Variable> atom2VarsSet = getVariables(right);
		List<Term> atom2VarsList = new LinkedList<Term>();
		atom2VarsList.addAll(atom2VarsSet);
		Collections.sort(atom2VarsList, comparator);
		Predicate rightAtomPred = ofac.getPredicate("ans" + ((2 * i) + 1),
				atom2VarsList.size());
		Function rightAtom = ofac.getFunction(rightAtomPred, atom2VarsList);

		/* The join */
		Function joinAtom = ofac.getSPARQLLeftJoin(leftAtom, rightAtom);

		/* adding the conditions of the filter for the LeftJoin */
		if (filter != null) {
		
			List<Term> joinTerms = joinAtom.getTerms();
			joinTerms.add(((Function) getBooleanTerm(filter)));
//			for (Expr expr : filter.getList()) {
//				joinTerms.add(((Function) getBooleanTerm(expr)));
//			}
			
		}

		/* Preparing the head of the LeftJoin rule */
		// Collections.sort(vars, comparator);
		List<Term> headVars = new LinkedList<Term>();
//		for (Variable var : vars) {
			headVars.addAll(vars);
//		}
		Predicate answerPred = ofac.getPredicate("ans" + i, vars.size());
		Function head = ofac.getFunction(answerPred, headVars);

		/*
		 * Adding the join to the program
		 */

		List<Function> atoms = new LinkedList<Function>();
		atoms.add(joinAtom);

		CQIE newrule = ofac.getCQIE(head, atoms);
		pr.appendRule(newrule);

		/*
		 * Translating the rest
		 */
		{
			List<Variable> vars1 = new LinkedList<Variable>();
			for (Term var : atom1VarsList)
				vars1.add((Variable) var);
			translate(vars1, left, pr, 2 * i, varcount);
		}
		{
			List<Variable> vars2 = new LinkedList<Variable>();
			for (Term var : atom2VarsList)
				vars2.add((Variable) var);
			translate(vars2, right, pr, 2 * i + 1, varcount);
		}
	}

	private void translate(List<Variable> vars, Projection project,
			DatalogProgram pr, long i, int[] varcount) {

		TupleExpr te = project.getArg();
		Set<Variable> nestedVars = getVariables(te);

		List<Term> projectedVariables = new LinkedList<Term>();
		for (ProjectionElem var : project.getProjectionElemList().getElements()) {
			projectedVariables.add(ofac.getVariable(var.getSourceName()));
		}

		Predicate predicate = ofac.getPredicate("ans" + i,
				projectedVariables.size());
		Function head = ofac.getFunction(predicate, projectedVariables);

		Predicate pbody = ofac.getPredicate("ans" + (i + 1), nestedVars.size());
		;

		Set<Variable> bodyatomVarsSet = getVariables(te);
		List<Term> bodyatomVarsList = new LinkedList<Term>();
		bodyatomVarsList.addAll(bodyatomVarsSet);
		Collections.sort(bodyatomVarsList, comparator);

		Function bodyAtom = ofac.getFunction(pbody, bodyatomVarsList);
		CQIE cq = ofac.getCQIE(head, bodyAtom);
		pr.appendRule(cq);

		/* Continue the nested tree */

		vars = new LinkedList<Variable>();
		for (Term var : bodyatomVarsList) {
			vars.add((Variable) var);
		}
		translate(vars, te, pr, i + 1, varcount);
	}

	private void translate(List<Variable> vars, Slice slice,
			DatalogProgram pr, long i, int[] varcount) {
		TupleExpr te;
		pr.getQueryModifiers().setOffset(slice.getOffset());
		pr.getQueryModifiers().setLimit(slice.getLimit());
		te = slice.getArg(); // narrow down the query
		translate(vars, te, pr, i, varcount);
	}

	private void translate(List<Variable> vars, Distinct distinct,
			DatalogProgram pr, long i, int[] varcount) {
		TupleExpr te;
		pr.getQueryModifiers().setDistinct();
		te = distinct.getArg(); // narrow down the query
		translate(vars, te, pr, i, varcount);
	}

	private void translate(List<Variable> vars, Order order,
			DatalogProgram pr, long i, int[] varcount) {
		TupleExpr te;
		for (OrderElem c : order.getElements()) {
			
			ValueExpr expression = c.getExpr();
			if (!(expression instanceof Var)) {
				throw new IllegalArgumentException("Error translating ORDER BY. The current implementation can only sort by variables, this query has a more complex expression. Offending expression: '"+expression+"'");
			}
			Var v = (Var) expression;
			Variable var = ofac.getVariable(v.getName());
			int direction = 0;
			if (c.isAscending()) direction = 1;
            else direction = 2;
			pr.getQueryModifiers().addOrderCondition(var, direction);
		}
		te = order.getArg(); // narrow down the query
		translate(vars, te, pr, i, varcount);
	}

	public void translate(List<Variable> var, Filter filter, DatalogProgram pr,
			long i, int varcount[]) {
		ValueExpr condition = filter.getCondition();
		List<Function> filterAtoms = new LinkedList<Function>();
		Set<Variable> filteredVariables = new LinkedHashSet<Variable>();

			Function a = null;
			if (condition instanceof Var) {
				a = ofac.getFunctionIsTrue(getVariableTerm((Var) condition));
			} else {
				a = (Function) getBooleanTerm(condition);
			}
			if (a != null) {
				Function filterAtom = ofac.getFunction(a.getFunctionSymbol(),
						a.getTerms());
				filterAtoms.add(filterAtom);
				filteredVariables.addAll(filterAtom.getReferencedVariables());
			}

		Predicate predicate = ofac.getPredicate("ans" + (i), var.size());
		List<Term> vars = new LinkedList<Term>();
		vars.addAll(var);
//        Collections.sort(vars, comparator);
		Function head = ofac.getFunction(predicate, vars);

		Predicate pbody;
		Function bodyAtom;

		List<Term> innerProjection = new LinkedList<Term>();
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
		 * the nested expressions, otherwise we endup with free variables.
		 * 
		 */

		// TODO here we might be missing the case where there is a filter
		// on a variable that has not been projected out of the inner
		// expressions
		if (vars.size() == 0 && filteredVariables.size() > 0) {
			pbody = ofac.getPredicate("ans" + (i * 2), innerProjection.size());
			bodyAtom = ofac.getFunction(pbody, innerProjection);
		} else {
			pbody = ofac.getPredicate("ans" + (i * 2), vars.size());
			bodyAtom = ofac.getFunction(pbody, vars);
		}

		LinkedList<Function> body = new LinkedList<Function>();
		body.add(bodyAtom);
		body.addAll(filterAtoms);

		CQIE cq = ofac.getCQIE(head, body);
		pr.appendRule(cq);

		TupleExpr sub = filter.getArg();

		if (vars.size() == 0 && filteredVariables.size() > 0) {
			List<Variable> newvars = new LinkedList<Variable>();
			for (Term l : innerProjection)
				newvars.add((Variable) l);
			translate(newvars, sub, pr, (i * 2), varcount);
		} else
			translate(var, sub, pr, (i * 2), varcount);

	}

	/***
	 * This translates a single triple. In most cases it will generate one
	 * single atom, however, if URI's are present, it will generate also
	 * equality atoms.
	 * 
	 * @param triple
	 * @return
	 */
	private void translate(List<Variable> vars, StatementPattern triple,
			DatalogProgram pr, long i, int[] varcount) {

        Var obj = triple.getObjectVar();
		Var pred = triple.getPredicateVar();
        Var subj = triple.getSubjectVar();

        Value o = obj.getValue();
        Value p = pred.getValue();
        Value s = subj.getValue();
		
		if (!(p instanceof URIImpl || (p == null))) {
			// if predicate is a variable or literal
			throw new RuntimeException("Unsupported query syntax");
		}


		
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
				Predicate.COL_TYPE type = dtfac.getDataType(objectUri);
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
		
		LinkedList<Function> result = new LinkedList<>();
		result.addFirst(atom);

		// Collections.sort(vars, comparator);
		List<Term> newvars = new LinkedList<>();
		for (Variable var : vars) {
			newvars.add(var);
		}
//        Collections.sort(newvars, comparator);

		Predicate answerPred = ofac.getPredicate("ans" + i, vars.size());
		Function head = ofac.getFunction(answerPred, newvars);

		CQIE newrule = ofac.getCQIE(head, result);
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
			Function dataTypeFunction = null;
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
				dataTypeFunction = ofac.getTypedTerm(constant, objectType);
				result= dataTypeFunction;
			}
		} 
		else if (s instanceof URIImpl) {
			URIImpl subject = (URIImpl) s;
			COL_TYPE subjectType = COL_TYPE.OBJECT;
			
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

	public Set<Variable> getVariables(List<org.openrdf.query.algebra.Var> list) {
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
	
	public Set<Variable> getBindVariables(List<ExtensionElem> elements) {
		Set<Variable> vars = new HashSet<Variable>();
		for (ExtensionElem el : elements) {
				String name = el.getName();
				Variable var = ofac.getVariable(name);
				vars.add(var);
			}
		return vars;
	}
	
	public Set<Variable> getVariables(TupleExpr te) {
		Set<Variable> result = new LinkedHashSet<Variable>();
		if (te instanceof StatementPattern) {
			result.addAll(getVariables(((StatementPattern) te).getVarList()));
		} else if (te instanceof BinaryTupleOperator) {
			result.addAll(getVariables(((BinaryTupleOperator) te).getLeftArg()));
			result.addAll(getVariables(((BinaryTupleOperator) te).getRightArg()));
		} else if (te instanceof UnaryTupleOperator) {
				if (te instanceof Extension) {
					result.addAll(getBindVariables(((Extension) te).getElements()));
				}
			result.addAll(getVariables(((UnaryTupleOperator) te).getArg()));
		} else {
			throw new RuntimeException("Operator not supported: " + te);
		}
		return result;
	}
	
	//private Variable getFreshVariable(int[] count) {
	//	count[0] += 1;
	//	return ofac.getVariable("VAR" + count[0]);
	//}

	public ValueConstant getConstant(LiteralImpl literal) {
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
		
		COL_TYPE dataType = dtfac.getDataType(typeURI);
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
		} else if (expr instanceof Bound) {

            return ofac.getFunctionIsNotNull(getVariableTerm(((Bound) expr).getArg()));
        } else if (expr instanceof FunctionCall){

            return getFunctionCallTerm(expr);

		} else {
			throw new RuntimeException("The builtin function "
					+ expr.toString() + " is not supported yet!");
		}
	}

    /** Return the Functions supported at the moment only
     * concat and replace
     * @param expr
     * @return
     */
    private Term getFunctionCallTerm(ValueExpr expr) {

        switch(((FunctionCall) expr).getURI()){
            case "http://www.w3.org/2005/xpath-functions#concat":

                List<ValueExpr> values = ((FunctionCall) expr).getArgs();

                Function topConcat = null;
                for (int i= 0; i<values.size(); i+=2) {

                    Term first_string, second_string;

                    if(topConcat == null){

                        ValueExpr first = values.get(i);
                        first_string = getBooleanTerm(first);

                        ValueExpr second = values.get(i+1);
                        second_string = getBooleanTerm(second);

                        topConcat = ofac.getFunctionSPARQLConcat(first_string, second_string);
                    }
                    else{

                        ValueExpr second = values.get(i);
                        second_string = getBooleanTerm(second);

                        topConcat = ofac.getFunctionSPARQLConcat(topConcat, second_string);
                    }

                }
                return topConcat;



            case "http://www.w3.org/2005/xpath-functions#replace":
                List<ValueExpr> expressions = ((FunctionCall) expr).getArgs();
                if (expressions.size() == 2 || expressions.size() == 3) {

                    Term t1; // first parameter is a function expression
                    ValueExpr first = expressions.get(0);
                    t1 = getBooleanTerm(first);


                    // second parameter is a string
                    Term out_string;
                    ValueExpr second = expressions.get(1);
                    out_string = getBooleanTerm(second);


                    /*
                     * Term t3 is optional: no string means delete occurrences of second param
			         */
                    Term in_string;
                    if (expressions.size() == 3) {
                        ValueExpr third = expressions.get(2);
                        in_string = getBooleanTerm(third);
                    } else {
                        in_string = ofac.getConstantLiteral("");
                    }

                    return ofac.getFunctionReplace(t1, out_string, in_string);
                } else

                    throw new UnsupportedOperationException("Wrong number of arguments (found " + expressions.size() + ", only 2 or 3 supported) to sql function REPLACE");



            default:
                throw new RuntimeException("The builtin function "
                        + ((FunctionCall) expr).getURI().toString() + " is not supported yet!");
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
				tp = dtfac.getDataType(type);
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

	private Function getBooleanFunction(BinaryValueOperator expr, Term term1,
			Term term2) {
		Function function = null;
		// The AND and OR expression
		if (expr instanceof And) {
			function = ofac.getFunctionAND(term1, term2);
		} else if (expr instanceof Or) {
			function = ofac.getFunctionOR(term1, term2);
		}
		// The other expressions
		else if (expr instanceof SameTerm){
			function = ofac.getFunctionEQ(term1, term2);
		} else if (expr instanceof Compare) {
			CompareOp operator = ((Compare) expr).getOperator();
			if (operator == Compare.CompareOp.EQ)
				function = ofac.getFunctionEQ(term1, term2);
			else if (operator == Compare.CompareOp.GE)
				function = ofac.getFunctionGTE(term1, term2);
			else if (operator == Compare.CompareOp.GT)
				function = ofac.getFunctionGT(term1, term2);
			else if (operator == Compare.CompareOp.LE)
				function = ofac.getFunctionLTE(term1, term2);
			else if (operator == Compare.CompareOp.LT)
				function = ofac.getFunctionLT(term1, term2);
			else if (operator == Compare.CompareOp.NE)
				function = ofac.getFunctionNEQ(term1, term2);
		} else if (expr instanceof MathExpr) {
			MathOp mop = ((MathExpr)expr).getOperator();
			if (mop == MathOp.PLUS) 
				function = ofac.getFunctionAdd(term1, term2);
			else if (mop == MathOp.MINUS)
				function = ofac.getFunctionSubstract(term1, term2);
			else if (mop == MathOp.MULTIPLY) 
				function = ofac.getFunctionMultiply(term1, term2);
		} else if (expr instanceof LangMatches) {
			function = ofac.getLANGMATCHESFunction(term1, toLowerCase(term2));
		} else {
			throw new IllegalStateException("getBooleanFunction does not understand the expression " + expr);
		}
		return function;
	}

	private Term toLowerCase(Term term) {
		Term output = term;
		if (term instanceof Function) {
			Function f = (Function) term;
			Predicate functor = f.getFunctionSymbol();
			if (functor instanceof DataTypePredicate) {
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
