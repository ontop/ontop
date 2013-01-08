package it.unibz.krdb.obda.owlrefplatform.core.translator;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DataTypePredicate;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.OBDAQueryModifiers.OrderCondition;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.Unifier;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.UriTemplateMatcher;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.Op1;
import com.hp.hpl.jena.sparql.algebra.op.Op2;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpSlice;
import com.hp.hpl.jena.sparql.algebra.op.OpTriple;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Add;
import com.hp.hpl.jena.sparql.expr.E_Bound;
import com.hp.hpl.jena.sparql.expr.E_Datatype;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_GreaterThan;
import com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_IsBlank;
import com.hp.hpl.jena.sparql.expr.E_IsIRI;
import com.hp.hpl.jena.sparql.expr.E_IsLiteral;
import com.hp.hpl.jena.sparql.expr.E_IsURI;
import com.hp.hpl.jena.sparql.expr.E_Lang;
import com.hp.hpl.jena.sparql.expr.E_LangMatches;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.expr.E_LessThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalNot;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.E_Multiply;
import com.hp.hpl.jena.sparql.expr.E_NotEquals;
import com.hp.hpl.jena.sparql.expr.E_Regex;
import com.hp.hpl.jena.sparql.expr.E_Str;
import com.hp.hpl.jena.sparql.expr.E_Subtract;
import com.hp.hpl.jena.sparql.expr.E_UnaryMinus;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction1;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprFunctionN;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueBoolean;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDateTime;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDecimal;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDouble;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueFloat;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueInteger;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueNode;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueString;
import com.hp.hpl.jena.sparql.syntax.Template;

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

	private OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

	private NewLiteralComparator comparator = new NewLiteralComparator();

	private final UriTemplateMatcher uriTemplateMatcher;

	public SparqlAlgebraToDatalogTranslator(UriTemplateMatcher templateMatcher) {
		uriTemplateMatcher = templateMatcher;
	}

	protected static org.slf4j.Logger log = LoggerFactory
			.getLogger(SparqlAlgebraToDatalogTranslator.class);

	/**
	 * Translate a given SPARQL query string to datalog program.
	 * 
	 * @param query
	 *            The SPARQL query string.
	 * @return Datalog program that represents the construction of the SPARQL
	 *         query.
	 */
	public DatalogProgram translate(String query) {
		Query arqQuery = QueryFactory.create(query);
		return translate(arqQuery);
	}

	/**
	 * Translate a given SPARQL query object to datalog program.
	 * 
	 * @param query
	 *            The Query object.
	 * @return Datalog program that represents the construction of the SPARQL
	 *         query.
	 */
	public DatalogProgram translate(Query arqQuery) {

		Op op = Algebra.compile(arqQuery);

		log.debug("SPARQL algebra: \n{}", op.toString());

		DatalogProgram result = ofac.getDatalogProgram();

		// Render the variable names in the signature into Variable object
		List<Variable> vars = new LinkedList<Variable>();
		for (String vs : getSignature(arqQuery)) {
			vars.add(ofac.getVariable(vs));
		}

		int[] freshvarcount = { 1 };

		translate(vars, op, result, 1, freshvarcount);
		return result;
	}

	public void translate(List<Variable> vars, Op op, DatalogProgram pr, int i,
			int[] varcount) {

		if (op instanceof OpSlice) {

			// Add LIMIT and OFFSET modifiers, if any
			OpSlice sliceOp = (OpSlice) op;
			translate(vars, sliceOp, pr, i, varcount);

		} else if (op instanceof OpDistinct) {

			// Add DISTINCT modifier, if any
			OpDistinct distinctOp = (OpDistinct) op;
			translate(vars, distinctOp, pr, i, varcount);

		} else if (op instanceof OpProject) {

			// Add PROJECTION modifier, if any
			OpProject projectOp = (OpProject) op;
			translate(vars, projectOp, pr, i, varcount);

		} else if (op instanceof OpOrder) {

			// Add ORDER BY modifier, if any
			OpOrder orderOp = (OpOrder) op;
			translate(vars, orderOp, pr, i, varcount);

		} else if (op instanceof OpFilter) {
			OpFilter filter = (OpFilter) op;
			translate(vars, filter, pr, i, varcount);

		} else if (op instanceof OpBGP) {

			OpBGP bgp = (OpBGP) op;
			translate(vars, bgp, pr, i, varcount);

		} else if (op instanceof OpJoin) {
			OpJoin join = (OpJoin) op;
			translate(vars, join, pr, i, varcount);

		} else if (op instanceof OpUnion) {
			OpUnion union = (OpUnion) op;
			translate(vars, union, pr, i, varcount);

		} else if (op instanceof OpLeftJoin) {
			OpLeftJoin join = (OpLeftJoin) op;
			translate(vars, join, pr, i, varcount);
		} else {
			throw new QueryException("Operation not supported: "
					+ op.toString());
		}
	}

	private void translate(List<Variable> vars, OpUnion union,
			DatalogProgram pr, int i, int[] varcount) {
		Op left = union.getLeft();
		Op right = union.getRight();

		/* Preparing the two atoms */

		Set<Variable> atom1VarsSet = getVariables(left);
		List<NewLiteral> atom1VarsList = new LinkedList<NewLiteral>();
		atom1VarsList.addAll(atom1VarsSet);
		Collections.sort(atom1VarsList, comparator);
		Predicate leftAtomPred = ofac.getPredicate("ans" + (2 * i),
				atom1VarsList.size());
		Atom leftAtom = ofac.getAtom(leftAtomPred, atom1VarsList);

		Set<Variable> atom2VarsSet = getVariables(right);
		List<NewLiteral> atom2VarsList = new LinkedList<NewLiteral>();
		atom2VarsList.addAll(atom2VarsSet);
		Collections.sort(atom2VarsList, comparator);
		Predicate rightAtomPred = ofac.getPredicate("ans" + ((2 * i) + 1),
				atom2VarsList.size());
		Atom rightAtom = ofac.getAtom(rightAtomPred, atom2VarsList);

		/* Preparing the head of the Union rules (2 rules) */
		// Collections.sort(vars, comparator);
		List<NewLiteral> headVars = new LinkedList<NewLiteral>();
		for (Variable var : vars) {
			headVars.add(var);
		}
		Predicate answerPred = ofac.getPredicate("ans" + i, vars.size());
		Atom head = ofac.getAtom(answerPred, headVars);

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
		Map<Variable, NewLiteral> nullifier = new HashMap<Variable, NewLiteral>();
		for (Variable var : nullVars) {
			nullifier.put(var, OBDAVocabulary.NULL);
		}
		// making the rule
		CQIE newrule1 = ofac.getCQIE(head, leftAtom);
		pr.appendRule(Unifier.applyUnifier(newrule1, nullifier));

		// finding out null
		nullVars = new HashSet<Variable>();
		nullVars.addAll(vars);
		nullVars.removeAll(atom2VarsSet); // the remaining variables do not
											// appear in the body assigning
											// null;
		nullifier = new HashMap<Variable, NewLiteral>();
		for (Variable var : nullVars) {
			nullifier.put(var, OBDAVocabulary.NULL);
		}
		// making the rule
		CQIE newrule2 = ofac.getCQIE(head, rightAtom);
		pr.appendRule(Unifier.applyUnifier(newrule2, nullifier));

		/*
		 * Translating the rest
		 */
		{
			List<Variable> vars1 = new LinkedList<Variable>();
			for (NewLiteral var : atom1VarsList)
				vars1.add((Variable) var);
			translate(vars1, left, pr, 2 * i, varcount);
		}
		{
			List<Variable> vars2 = new LinkedList<Variable>();
			for (NewLiteral var : atom2VarsList)
				vars2.add((Variable) var);
			translate(vars2, right, pr, 2 * i + 1, varcount);
		}

	}

	private void translate(List<Variable> vars, OpJoin join, DatalogProgram pr,
			int i, int[] varcount) {
		Op left = join.getLeft();
		Op right = join.getRight();

		/* Preparing the two atoms */

		Set<Variable> atom1VarsSet = getVariables(left);
		List<NewLiteral> atom1VarsList = new LinkedList<NewLiteral>();
		atom1VarsList.addAll(atom1VarsSet);
		Collections.sort(atom1VarsList, comparator);
		Predicate leftAtomPred = ofac.getPredicate("ans" + (2 * i),
				atom1VarsList.size());
		Atom leftAtom = ofac.getAtom(leftAtomPred, atom1VarsList);

		Set<Variable> atom2VarsSet = getVariables(right);
		List<NewLiteral> atom2VarsList = new LinkedList<NewLiteral>();
		atom2VarsList.addAll(atom2VarsSet);
		Collections.sort(atom2VarsList, comparator);
		Predicate rightAtomPred = ofac.getPredicate("ans" + ((2 * i) + 1),
				atom2VarsList.size());
		Atom rightAtom = ofac.getAtom(rightAtomPred, atom2VarsList);

		/* The join */
		Predicate joinp = OBDAVocabulary.SPARQL_JOIN;
		Atom joinAtom = ofac.getAtom(joinp, leftAtom, rightAtom);

		/* Preparing the head of the Join rule */
		// Collections.sort(vars, comparator);
		List<NewLiteral> headVars = new LinkedList<NewLiteral>();
		for (Variable var : vars) {
			headVars.add(var);
		}
		Predicate answerPred = ofac.getPredicate("ans" + i, vars.size());
		Atom head = ofac.getAtom(answerPred, headVars);

		/*
		 * Adding the join to the program
		 */

		CQIE newrule = ofac.getCQIE(head, joinAtom);
		pr.appendRule(newrule);

		/*
		 * Translating the rest
		 */
		{
			List<Variable> vars1 = new LinkedList<Variable>();
			for (NewLiteral var : atom1VarsList)
				vars1.add((Variable) var);
			translate(vars1, left, pr, 2 * i, varcount);
		}
		{
			List<Variable> vars2 = new LinkedList<Variable>();
			for (NewLiteral var : atom2VarsList)
				vars2.add((Variable) var);
			translate(vars2, right, pr, 2 * i + 1, varcount);
		}
	}

	private void translate(List<Variable> vars, OpLeftJoin join,
			DatalogProgram pr, int i, int[] varcount) {
		Op left = join.getLeft();
		Op right = join.getRight();
		ExprList filter = join.getExprs();

		/* Preparing the two atoms */

		Set<Variable> atom1VarsSet = getVariables(left);
		List<NewLiteral> atom1VarsList = new LinkedList<NewLiteral>();
		atom1VarsList.addAll(atom1VarsSet);
		Collections.sort(atom1VarsList, comparator);
		Predicate leftAtomPred = ofac.getPredicate("ans" + (2 * i),
				atom1VarsList.size());
		Atom leftAtom = ofac.getAtom(leftAtomPred, atom1VarsList);

		Set<Variable> atom2VarsSet = getVariables(right);
		List<NewLiteral> atom2VarsList = new LinkedList<NewLiteral>();
		atom2VarsList.addAll(atom2VarsSet);
		Collections.sort(atom2VarsList, comparator);
		Predicate rightAtomPred = ofac.getPredicate("ans" + ((2 * i) + 1),
				atom2VarsList.size());
		Atom rightAtom = ofac.getAtom(rightAtomPred, atom2VarsList);

		/* The join */
		Predicate joinp = OBDAVocabulary.SPARQL_LEFTJOIN;

		Atom joinAtom = ofac.getAtom(joinp, leftAtom, rightAtom);

		/* adding the conditions of the filter for the LeftJoin */
		if (filter != null) {
			List joinTerms = joinAtom.getTerms();
			for (Expr expr : filter.getList()) {
				joinTerms.add(((Function) getBooleanTerm(expr)).asAtom());
			}
		}

		/* Preparing the head of the LeftJoin rule */
		// Collections.sort(vars, comparator);
		List<NewLiteral> headVars = new LinkedList<NewLiteral>();
		for (Variable var : vars) {
			headVars.add(var);
		}
		Predicate answerPred = ofac.getPredicate("ans" + i, vars.size());
		Atom head = ofac.getAtom(answerPred, headVars);

		/*
		 * Adding the join to the program
		 */

		List<Atom> atoms = new LinkedList<Atom>();
		atoms.add(joinAtom);

		CQIE newrule = ofac.getCQIE(head, atoms);
		pr.appendRule(newrule);

		/*
		 * Translating the rest
		 */
		{
			List<Variable> vars1 = new LinkedList<Variable>();
			for (NewLiteral var : atom1VarsList)
				vars1.add((Variable) var);
			translate(vars1, left, pr, 2 * i, varcount);
		}
		{
			List<Variable> vars2 = new LinkedList<Variable>();
			for (NewLiteral var : atom2VarsList)
				vars2.add((Variable) var);
			translate(vars2, right, pr, 2 * i + 1, varcount);
		}
	}

	private void translate(List<Variable> vars, OpProject projectOp,
			DatalogProgram pr, int i, int[] varcount) {

		Op op = projectOp.getSubOp();
		Set<Variable> nestedVars = getVariables(op);

		List<NewLiteral> projectedVariables = new LinkedList<NewLiteral>();
		for (Var var : projectOp.getVars()) {
			projectedVariables.add(ofac.getVariable(var.getVarName()));
		}

		Predicate predicate = ofac.getPredicate("ans" + i,
				projectedVariables.size());
		Atom head = ofac.getAtom(predicate, projectedVariables);

		Predicate pbody = ofac.getPredicate("ans" + (i + 1), nestedVars.size());
		;

		Set<Variable> bodyatomVarsSet = getVariables(op);
		List<NewLiteral> bodyatomVarsList = new LinkedList<NewLiteral>();
		bodyatomVarsList.addAll(bodyatomVarsSet);
		Collections.sort(bodyatomVarsList, comparator);

		Atom bodyAtom = ofac.getAtom(pbody, bodyatomVarsList);
		CQIE cq = ofac.getCQIE(head, bodyAtom);
		pr.appendRule(cq);

		/* Continue the nested tree */

		vars = new LinkedList<Variable>();
		for (NewLiteral var : bodyatomVarsList) {
			vars.add((Variable) var);
		}
		translate(vars, op, pr, i + 1, varcount);
	}

	private void translate(List<Variable> vars, OpSlice sliceOp,
			DatalogProgram pr, int i, int[] varcount) {
		Op op;
		pr.getQueryModifiers().setOffset(sliceOp.getStart());
		pr.getQueryModifiers().setLimit(sliceOp.getLength());
		op = sliceOp.getSubOp(); // narrow down the query
		translate(vars, op, pr, i, varcount);
	}

	private void translate(List<Variable> vars, OpDistinct distinctOp,
			DatalogProgram pr, int i, int[] varcount) {
		Op op;
		pr.getQueryModifiers().setDistinct();
		op = distinctOp.getSubOp(); // narrow down the query
		translate(vars, op, pr, i, varcount);
	}

	private void translate(List<Variable> vars, OpOrder orderOp,
			DatalogProgram pr, int i, int[] varcount) {
		Op op;
		for (SortCondition c : orderOp.getConditions()) {
			Variable var = ofac.getVariable(c.getExpression().getVarName());
			int direction = (c.direction == Query.ORDER_DESCENDING ? OrderCondition.ORDER_DESCENDING
					: OrderCondition.ORDER_ASCENDING);
			pr.getQueryModifiers().addOrderCondition(var, direction);
		}
		op = orderOp.getSubOp(); // narrow down the query
		translate(vars, op, pr, i, varcount);
	}

	public void translate(List<Variable> var, OpFilter op, DatalogProgram pr,
			int i, int varcount[]) {
		ExprList list = op.getExprs();
		List<Expr> exprlist = list.getList();
		List<Atom> filterAtoms = new LinkedList<Atom>();
		Set<Variable> filteredVariables = new LinkedHashSet<Variable>();
		for (Expr expr : exprlist) {
			Function a = null;
			if (expr.isVariable()) {
				a = ofac.getFunctionalTerm(OBDAVocabulary.IS_TRUE, getVariableTerm((ExprVar) expr));
			} else {
				a = (Function) getBooleanTerm(expr);
			}
			if (a != null) {
				Atom filterAtom = ofac.getAtom(a.getFunctionSymbol(),
						a.getTerms());
				filterAtoms.add(filterAtom);
				filteredVariables.addAll(filterAtom.getReferencedVariables());
			}
		}

		Predicate predicate = ofac.getPredicate("ans" + (i), var.size());
		List<NewLiteral> vars = new LinkedList<NewLiteral>();
		vars.addAll(var);
		Atom head = ofac.getAtom(predicate, vars);

		Predicate pbody;
		Atom bodyAtom;

		List<NewLiteral> innerProjection = new LinkedList<NewLiteral>();
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
			bodyAtom = ofac.getAtom(pbody, innerProjection);
		} else {
			pbody = ofac.getPredicate("ans" + (i * 2), vars.size());
			bodyAtom = ofac.getAtom(pbody, vars);
		}

		LinkedList<Atom> body = new LinkedList<Atom>();
		body.add(bodyAtom);
		body.addAll(filterAtoms);

		CQIE cq = ofac.getCQIE(head, body);
		pr.appendRule(cq);

		Op sub = op.getSubOp();

		if (vars.size() == 0 && filteredVariables.size() > 0) {
			List<Variable> newvars = new LinkedList<Variable>();
			for (NewLiteral l : innerProjection)
				newvars.add((Variable) l);
			translate(newvars, sub, pr, (i * 2), varcount);
		} else
			translate(var, sub, pr, (i * 2), varcount);

	}

	public void translate(List<Variable> vars, OpBGP op, DatalogProgram pr,
			int i, int[] varcount) {
		translate(vars, op.getPattern(), pr, i, varcount);
	}

	/*
	 * This method breaks down a BGP into a set of JOINs, so that each join is a
	 * binary operation. This is required to maintain the numbering of each
	 * level of the program.
	 */
	public void translate(List<Variable> vars, BasicPattern bp,
			DatalogProgram pr, int i, int[] varcount) {
		translate(vars, bp.getList(), pr, i, varcount);
	}

	public void translate(List<Variable> vars, List<Triple> triples,
			DatalogProgram pr, int i, int[] varcount) {

		if (triples.size() == 1) {
			translate(vars, triples.get(0), pr, i, varcount);

		} else if (triples.size() >= 2) {
			/* Preparing the head of the Join rule */
			List<NewLiteral> headVars = new LinkedList<NewLiteral>();
			for (Variable var : vars) {
				headVars.add(var);
			}
			Predicate answerPred = ofac.getPredicate("ans" + i, vars.size());
			Atom head = ofac.getAtom(answerPred, headVars);

			/* Preparing the two atoms */
			Set<Variable> atom1VarsSet = getVariables(triples.get(0));
			List<NewLiteral> atom1VarsList = new LinkedList<NewLiteral>();
			atom1VarsList.addAll(atom1VarsSet);
			Collections.sort(atom1VarsList, comparator);
			Predicate leftAtomPred = ofac.getPredicate("ans" + (2 * i),
					atom1VarsList.size());
			Atom leftAtom = ofac.getAtom(leftAtomPred, atom1VarsList);

			Set<Variable> atom2VarsSet = getVariables(triples.subList(1,
					triples.size()));
			List<NewLiteral> atom2VarsList = new LinkedList<NewLiteral>();
			atom2VarsList.addAll(atom2VarsSet);
			Collections.sort(atom2VarsList, comparator);
			Predicate rightAtomPred = ofac.getPredicate("ans" + ((2 * i) + 1),
					atom2VarsList.size());
			Atom rightAtom = ofac.getAtom(rightAtomPred, atom2VarsList);

			/* The join */
			Predicate join = OBDAVocabulary.SPARQL_JOIN;
			Atom joinAtom = ofac.getAtom(join, leftAtom, rightAtom);

			CQIE newrule = ofac.getCQIE(head, joinAtom);
			pr.appendRule(newrule);

			List<Variable> newvars = new LinkedList<Variable>();
			for (NewLiteral var : atom1VarsList) {
				newvars.add((Variable) var);
			}
			translate(newvars, triples.get(0), pr, 2 * i, varcount);
			newvars.clear();
			for (NewLiteral var : atom2VarsList) {
				newvars.add((Variable) var);
			}
			translate(newvars, triples.subList(1, triples.size()), pr,
					(2 * i) + 1, varcount);

		} else {
			throw new RuntimeException("Error tranlsating a BGP, size was 0.");
		}
	}

	/***
	 * This translates a single triple. In most cases it will generate one
	 * single atom, however, if URI's are present, it will generate also
	 * equality atoms.
	 * 
	 * @param triple
	 * @return
	 */
	public void translate(List<Variable> vars, Triple triple,
			DatalogProgram pr, int i, int[] varcount) {
		Node o = triple.getObject();
		Node p = triple.getPredicate();
		Node s = triple.getSubject();

		if (!(p instanceof Node_URI || p instanceof Var)) {
			// if predicate is a variable or literal
			throw new QueryException("Unsupported query syntax");
		}

		LinkedList<Atom> result = new LinkedList<Atom>();

		// Instantiate the subject and object URI
		URI subjectUri = null;
		URI objectUri = null;
		URI propertyUri = null;

		// Instantiate the subject and object data type
		COL_TYPE subjectType = null;
		COL_TYPE objectType = null;

		// / Instantiate the atom components: predicate and terms.
		Predicate predicate = null;
		Vector<NewLiteral> terms = new Vector<NewLiteral>();

		if (p instanceof Node_URI && p.getURI().equals(OBDAVocabulary.RDF_TYPE)) {
			// Subject node
			if (s instanceof Var) {
				Var subject = (Var) s;
				terms.add(ofac.getVariable(subject.getName()));
			} else if (s instanceof Node_Literal) {
				Node_Literal subject = (Node_Literal) s;
				ValueConstant constant = getConstant(subject);
				terms.add(constant);
			} else if (s instanceof Node_URI) {

				/*
				 * Found a URI, here we need to create a variable and add an
				 * equation of the variable to uri("http:....")
				 */

				Node_URI subject = (Node_URI) s;
				subjectType = COL_TYPE.OBJECT;
				subjectUri = URI.create(subject.getURI());

				Function functionURI = uriTemplateMatcher.generateURIFunction(subjectUri);
				terms.add(functionURI);

				// Function functionURI = ofac.getFunctionalTerm(
				// ofac.getUriTemplatePredicate(1),
				// ofac.getURIConstant(subjectUri));
				// Variable freshVariable = getFreshVariable(varcount);
				// Atom eqAtom = ofac.getEQAtom(freshVariable, functionURI);
				// result.add(eqAtom);
				//
				// terms.add(freshVariable);

			}

			// Object node
			if (o instanceof Var) {

				predicate = OBDAVocabulary.QUEST_TRIPLE_PRED;

				Function rdfTypeConstant = ofac.getFunctionalTerm(ofac
						.getUriTemplatePredicate(1), ofac.getURIConstant(URI
						.create(OBDAVocabulary.RDF_TYPE)));
				terms.add(rdfTypeConstant);
				terms.add(ofac.getVariable(((Var) o).getVarName()));

			} else if (o instanceof Node_Literal) {
				throw new QueryException("Unsupported query syntax");
			} else if (o instanceof Node_URI) {
				Node_URI object = (Node_URI) o;
				objectUri = URI.create(object.getURI());
			}

			// Construct the predicate
			URI predicateUri = objectUri;
			if (predicateUri == null) {
				// NO OP, already assigned
			} else if (predicateUri.toString().equals(
					OBDAVocabulary.RDFS_LITERAL_URI)) {
				predicate = OBDAVocabulary.RDFS_LITERAL;
			} else if (predicateUri.toString().equals(
					OBDAVocabulary.XSD_BOOLEAN_URI)) {
				predicate = OBDAVocabulary.XSD_BOOLEAN;
			} else if (predicateUri.toString().equals(
					OBDAVocabulary.XSD_DATETIME_URI)) {
				predicate = OBDAVocabulary.XSD_DATETIME;
			} else if (predicateUri.toString().equals(
					OBDAVocabulary.XSD_DECIMAL_URI)) {
				predicate = OBDAVocabulary.XSD_DECIMAL;
			} else if (predicateUri.toString().equals(
					OBDAVocabulary.XSD_DOUBLE_URI)) {
				predicate = OBDAVocabulary.XSD_DOUBLE;
			} else if (predicateUri.toString().equals(
					OBDAVocabulary.XSD_FLOAT_URI)) {
				predicate = OBDAVocabulary.XSD_DOUBLE;
			} else if (predicateUri.toString().equals(
					OBDAVocabulary.XSD_INT_URI)) {
				predicate = OBDAVocabulary.XSD_INTEGER;
			} else if (predicateUri.toString().equals(
					OBDAVocabulary.XSD_INTEGER_URI)) {
				predicate = OBDAVocabulary.XSD_INTEGER;
			} else if (predicateUri.toString().equals(
					OBDAVocabulary.XSD_STRING_URI)) {
				predicate = OBDAVocabulary.XSD_STRING;
			} else {

				predicate = ofac.getPredicate(predicateUri, 1,
						new COL_TYPE[] { subjectType });

			}

		} else {
			/*
			 * The predicate is NOT rdf:type
			 */

			// Subject node
			if (s instanceof Var) {
				Var subject = (Var) s;
				terms.add(ofac.getVariable(subject.getName()));
			} else if (s instanceof Node_Literal) {
				Node_Literal subject = (Node_Literal) s;
				ValueConstant constant = getConstant(subject);
				terms.add(constant);
			} else if (s instanceof Node_URI) {
				Node_URI subject = (Node_URI) s;
				subjectType = COL_TYPE.OBJECT;
				subjectUri = URI.create(subject.getURI());

				Function functionURI = uriTemplateMatcher.generateURIFunction(subjectUri);
				terms.add(functionURI);

				// Function functionURI = ofac.getFunctionalTerm(
				// ofac.getUriTemplatePredicate(1),
				// ofac.getURIConstant(subjectUri));
				// Variable freshVariable = getFreshVariable(varcount);
				// Atom eqAtom = ofac.getEQAtom(freshVariable, functionURI);
				// result.add(eqAtom);
				//
				// terms.add(freshVariable);
			}

			// Object node
			if (o instanceof Var) {
				Var object = (Var) o;
				terms.add(ofac.getVariable(object.getName()));
			} else if (o instanceof Node_Literal) {
				Node_Literal object = (Node_Literal) o;
				objectType = getDataType(object);

				ValueConstant constant = getConstant(object);

				// v1.7: We extend the syntax such that the data type of a
				// constant
				// is defined using a functional symbol.
				Function dataTypeFunction = null;
				if (objectType == COL_TYPE.LITERAL) {
					// If the object has type LITERAL, check any language
					// tag!
					String lang = object.getLiteralLanguage().toLowerCase();
					Predicate functionSymbol = ofac
							.getDataTypePredicateLiteral();
					Constant languageConstant = null;
					if (lang != null && !lang.equals("")) {
						languageConstant = ofac.getValueConstant(lang,
								COL_TYPE.LITERAL);
						dataTypeFunction = ofac.getFunctionalTerm(
								functionSymbol, constant, languageConstant);
						terms.add(dataTypeFunction);
					} else {
						dataTypeFunction = ofac.getFunctionalTerm(
								functionSymbol, constant);
						terms.add(dataTypeFunction);
					}
				} else {
					// For other supported data-types
					Predicate functionSymbol = getDataTypePredicate(objectType);
					dataTypeFunction = ofac.getFunctionalTerm(functionSymbol,
							constant);
					terms.add(dataTypeFunction);
				}
			} else if (o instanceof Node_URI) {
				Node_URI object = (Node_URI) o;
				objectType = COL_TYPE.OBJECT;
				objectUri = URI.create(object.getURI());

				Function functionURI = uriTemplateMatcher.generateURIFunction(objectUri);
				terms.add(functionURI);

				// Function functionURI = ofac.getFunctionalTerm(
				// ofac.getUriTemplatePredicate(1),
				// ofac.getURIConstant(objectUri));
				// Variable freshVariable = getFreshVariable(varcount);
				// Atom eqAtom = ofac.getEQAtom(freshVariable, functionURI);
				// result.add(eqAtom);
				//
				// terms.add(freshVariable);

			}
			// Construct the predicate

			if (p instanceof Node_URI) {
				URI predicateUri = URI.create(p.getURI());
				predicate = ofac.getPredicate(predicateUri, 2, new COL_TYPE[] {
						subjectType, objectType });
			} else if (p instanceof Var) {
				predicate = OBDAVocabulary.QUEST_TRIPLE_PRED;
				terms.add(1, ofac.getVariable(((Var) p).getVarName()));
			}
		}
		// Construct the atom
		Atom atom = ofac.getAtom(predicate, terms);
		result.addFirst(atom);

		// Collections.sort(vars, comparator);
		List<NewLiteral> newvars = new LinkedList<NewLiteral>();
		for (Variable var : vars) {
			newvars.add(var);
		}

		Predicate answerPred = ofac.getPredicate("ans" + i, vars.size());
		Atom head = ofac.getAtom(answerPred, newvars);

		CQIE newrule = ofac.getCQIE(head, result);
		pr.appendRule(newrule);
	}

	// private class VariableComparator implements Comparator<Variable> {
	//
	// @Override
	// public int compare(Variable arg0, Variable arg1) {
	// return arg0.getName().compareTo(arg1.getName());
	// }
	//
	// }

	private class NewLiteralComparator implements Comparator<NewLiteral> {

		@Override
		public int compare(NewLiteral arg0, NewLiteral arg1) {
			return arg0.toString().compareTo(arg1.toString());
		}

	}

	public Set<Variable> getVariables(List<Triple> triples) {
		Set<Variable> vars = new HashSet<Variable>();
		for (Triple triple : triples) {
			vars.addAll(getVariables(triple));
		}
		return vars;
	}

	public Set<Variable> getVariables(Triple triple) {
		Set<Variable> vars = new HashSet<Variable>();
		Node o = triple.getObject();
		Node p = triple.getPredicate();
		Node s = triple.getSubject();
		if (o instanceof Var) {
			String name = ((Var) o).getVarName();
			Variable var = ofac.getVariable(name);
			vars.add(var);
		}

		if (p instanceof Var) {
			String name = ((Var) p).getVarName();
			Variable var = ofac.getVariable(name);
			vars.add(var);
		}

		if (s instanceof Var) {
			String name = ((Var) s).getVarName();
			Variable var = ofac.getVariable(name);
			vars.add(var);
		}
		return vars;
	}

	public Set<Variable> getVariables(Op op) {
		Set<Variable> result = new LinkedHashSet<Variable>();
		if (op instanceof OpBGP) {
			result.addAll(getVariables(((OpBGP) op).getPattern().getList()));
		} else if (op instanceof OpTriple) {
			result.addAll(getVariables(((OpTriple) op).getTriple()));
		} else if (op instanceof Op2) {
			result.addAll(getVariables(((Op2) op).getLeft()));
			result.addAll(getVariables(((Op2) op).getRight()));
		} else if (op instanceof Op1) {
			result.addAll(getVariables(((Op1) op).getSubOp()));
		} else {
			throw new RuntimeException("Operator not supported: " + op);
		}
		return result;
	}

	private Variable getFreshVariable(int[] count) {
		count[0] += 1;
		return ofac.getVariable("VAR" + count[0]);
	}

	public ValueConstant getConstant(Node_Literal literal) {
		RDFDatatype type = literal.getLiteralDatatype();

		COL_TYPE objectType = getDataType(literal);

		String value = literal.getLiteralLexicalForm();
		ValueConstant constant = ofac.getValueConstant(value, objectType);

		/*
		 * Validating that the value is correct (lexically) with respect to the
		 * specified datatype
		 */
		boolean valid = false;
		if (type == XSDDatatype.XSDanyURI) {
			valid = XSDDatatype.XSDanyURI.isValid(value);
		} else if (type == XSDDatatype.XSDboolean) {
			valid = XSDDatatype.XSDboolean.isValid(value);
		} else if (type == XSDDatatype.XSDdateTime) {
			valid = XSDDatatype.XSDdateTime.isValid(value);
		} else if (type == XSDDatatype.XSDdecimal) {
			valid = XSDDatatype.XSDdecimal.isValid(value);
		} else if (type == XSDDatatype.XSDdouble) {
			valid = XSDDatatype.XSDdouble.isValid(value);
		} else if (type == XSDDatatype.XSDfloat) {
			valid = XSDDatatype.XSDfloat.isValid(value);
		} else if (type == XSDDatatype.XSDint) {
			valid = XSDDatatype.XSDint.isValid(value);
		} else if (type == XSDDatatype.XSDinteger) {
			valid = XSDDatatype.XSDinteger.isValid(value);
		} else if (type == XSDDatatype.XSDlong) {
			valid = XSDDatatype.XSDlong.isValid(value);
		} else if (type == XSDDatatype.XSDshort) {
			valid = XSDDatatype.XSDshort.isValid(value);
		} else if (type == XSDDatatype.XSDstring) {
			valid = XSDDatatype.XSDstring.isValid(value);
		} else if (literal.getLiteralDatatypeURI() == null
				|| literal.getLiteralDatatypeURI().equals(
						OBDAVocabulary.RDFS_LITERAL_URI)) {
			valid = true;
		} else {
			valid = false;
		}
		if (!valid)
			throw new RuntimeException(
					"Invalid lexical form for datatype. Found: " + value);
		return constant;

	}

	private Predicate getDataTypePredicate(COL_TYPE dataType)
			throws QueryException {
		switch (dataType) {
		case STRING:
			return ofac.getDataTypePredicateString();
		case INTEGER:
			return ofac.getDataTypePredicateInteger();
		case DECIMAL:
			return ofac.getDataTypePredicateDecimal();
		case DOUBLE:
			return ofac.getDataTypePredicateDouble();
		case DATETIME:
			return ofac.getDataTypePredicateDateTime();
		case BOOLEAN:
			return ofac.getDataTypePredicateBoolean();
		default:
			throw new QueryException("Unknown data type!");
		}
	}

	private COL_TYPE getDataType(Node_Literal node) {
		COL_TYPE dataType = null;

		final String dataTypeURI = node.getLiteralDatatypeURI();
		if (dataTypeURI == null) {
			dataType = COL_TYPE.LITERAL;
		} else {
			if (dataTypeURI.equalsIgnoreCase(OBDAVocabulary.RDFS_LITERAL_URI)) {
				dataType = COL_TYPE.LITERAL;
			} else if (dataTypeURI
					.equalsIgnoreCase(OBDAVocabulary.XSD_STRING_URI)) {
				dataType = COL_TYPE.STRING;
			} else if (dataTypeURI.equalsIgnoreCase(OBDAVocabulary.XSD_INT_URI)
					|| dataTypeURI
							.equalsIgnoreCase(OBDAVocabulary.XSD_INTEGER_URI)) {
				dataType = COL_TYPE.INTEGER;
			} else if (dataTypeURI
					.equalsIgnoreCase(OBDAVocabulary.XSD_DECIMAL_URI)) {
				// special case for decimal
				String value = node.getLiteralValue().toString();
				if (value.contains(".")) {
					// Put the type as decimal (with fractions).
					dataType = COL_TYPE.DECIMAL;
				} else {
					// Put the type as integer (decimal without fractions).
					dataType = COL_TYPE.INTEGER;
				}
			} else if (dataTypeURI
					.equalsIgnoreCase(OBDAVocabulary.XSD_FLOAT_URI)
					|| dataTypeURI
							.equalsIgnoreCase(OBDAVocabulary.XSD_DOUBLE_URI)) {
				dataType = COL_TYPE.DOUBLE;
			} else if (dataTypeURI
					.equalsIgnoreCase(OBDAVocabulary.XSD_DATETIME_URI)) {
				dataType = COL_TYPE.DATETIME;
			} else if (dataTypeURI
					.equalsIgnoreCase(OBDAVocabulary.XSD_BOOLEAN_URI)) {
				dataType = COL_TYPE.BOOLEAN;
			} else {
				throw new RuntimeException("Unsupported datatype: "
						+ dataTypeURI.toString());
			}
		}
		return dataType;
	}

	private NewLiteral getBooleanTerm(Expr expr) {
		if (expr instanceof ExprVar) {
			return getVariableTerm((ExprVar) expr);
		} else if (expr instanceof NodeValue) {
			return getConstantFunctionTerm((NodeValue) expr);
		} else if (expr instanceof ExprFunction1) {
			return getBuiltinFunctionTerm((ExprFunction1) expr);
		} else if (expr instanceof ExprFunction2) {
			ExprFunction2 function = (ExprFunction2) expr;
			Expr arg1 = function.getArg1(); // get the first argument
			Expr arg2 = function.getArg2(); // get the second argument
			NewLiteral term1 = getBooleanTerm(arg1);
			NewLiteral term2 = getBooleanTerm(arg2);
			// Construct the boolean function
			// TODO Change the method name because ExprFunction2 is not only for
			// boolean functions
			return getBooleanFunction(function, term1, term2);
		} else if (expr instanceof ExprFunctionN) {
			return getOtherFunctionTerm((ExprFunctionN) expr);
		} else {
			throw new RuntimeException("The builtin function "
					+ expr.toString() + " is not supported yet!");
		}
	}

	private Function getVariableTermIntoBoolFunction(ExprVar expr) {
		return ofac.getFunctionalTerm(OBDAVocabulary.IS_TRUE, getVariableTerm(expr));
	}
	
	private Variable getVariableTerm(ExprVar expr) {
		return ofac.getVariable(expr.getVarName());
	}

	private Function getConstantFunctionTerm(NodeValue expr) {
		Function constantFunction = null;
		if (expr instanceof NodeValueString) {
			constantFunction = ofac.getFunctionalTerm(
					ofac.getDataTypePredicateString(),
					ofac.getValueConstant(expr.getString(), COL_TYPE.STRING));
		} else if (expr instanceof NodeValueInteger) {
			constantFunction = ofac.getFunctionalTerm(ofac
					.getDataTypePredicateInteger(), ofac.getValueConstant(
					expr.getInteger() + "", COL_TYPE.INTEGER));
		} else if (expr instanceof NodeValueDecimal) {
			constantFunction = ofac.getFunctionalTerm(ofac
					.getDataTypePredicateDecimal(), ofac.getValueConstant(
					expr.getDecimal() + "", COL_TYPE.DECIMAL));
		} else if (expr instanceof NodeValueDouble
				|| expr instanceof NodeValueFloat) {
			constantFunction = ofac.getFunctionalTerm(ofac
					.getDataTypePredicateDouble(), ofac.getValueConstant(
					expr.getDouble() + "", COL_TYPE.DOUBLE));
		} else if (expr instanceof NodeValueDateTime) {
			constantFunction = ofac.getFunctionalTerm(ofac
					.getDataTypePredicateDateTime(), ofac.getValueConstant(
					expr.getDateTime() + "", COL_TYPE.DATETIME));
		} else if (expr instanceof NodeValueBoolean) {
			constantFunction = ofac.getFunctionalTerm(ofac
					.getDataTypePredicateBoolean(), ofac.getValueConstant(
					expr.getBoolean() + "", COL_TYPE.BOOLEAN));
		} else if (expr instanceof NodeValueNode) {
			NodeValueNode nodeValue = (NodeValueNode) expr;
			Node node = nodeValue.getNode();
			if (node instanceof Node_Literal) {
				constantFunction = ofac.getFunctionalTerm(ofac
						.getDataTypePredicateLiteral(), ofac.getValueConstant(
						node.getLiteralLexicalForm(), COL_TYPE.STRING));
			} else if (node instanceof Node_URI) {
				constantFunction = ofac.getFunctionalTerm(ofac
						.getUriTemplatePredicate(1), ofac.getValueConstant(
						node.toString(), COL_TYPE.OBJECT));
			} else {
				throw new RuntimeException("Unsupported node: "
						+ expr.toString());
			}
		} else {
			throw new QueryException("Unknown data type!");
		}
		return constantFunction;
	}

	private Function getBuiltinFunctionTerm(ExprFunction1 expr) {
		Function builtInFunction = null;
		if (expr instanceof E_LogicalNot) {
			Expr arg = expr.getArg();
			NewLiteral term = getBooleanTerm(arg);
			builtInFunction = ofac.getFunctionalTerm(OBDAVocabulary.NOT, term);
		}
		/*
		 * The following expressions only accept variable as the parameter
		 */
		else if (expr instanceof E_UnaryMinus) {
			Expr arg = expr.getArg();
			NewLiteral term = getBooleanTerm(arg);
			NewLiteral minusOneConstant = ofac.getValueConstant("-1", COL_TYPE.INTEGER);
			builtInFunction = ofac.getFunctionalTerm(OBDAVocabulary.MULTIPLY, minusOneConstant, term);
		} else if (expr instanceof E_Bound) {
			Expr arg = expr.getArg();
			if (arg instanceof ExprVar) {
				builtInFunction = ofac.getFunctionalTerm(
						OBDAVocabulary.IS_NOT_NULL,
						getVariableTerm((ExprVar) arg));
			}
		} else if (expr instanceof E_IsLiteral) {
			builtInFunction = ofac.getFunctionalTerm(OBDAVocabulary.SPARQL_IS_LITERAL, getBooleanTerm( expr.getArg()));
			
		} else if (expr instanceof E_IsBlank) {
			builtInFunction = ofac.getFunctionalTerm(OBDAVocabulary.SPARQL_IS_BLANK, getBooleanTerm( expr.getArg()));
			
		} else if (expr instanceof E_IsURI) {
			builtInFunction = ofac.getFunctionalTerm(OBDAVocabulary.SPARQL_IS_URI, getBooleanTerm( expr.getArg()));
			
		} else if (expr instanceof E_IsIRI) {
			builtInFunction = ofac.getFunctionalTerm(OBDAVocabulary.SPARQL_IS_IRI, getBooleanTerm( expr.getArg()));
			
		} else if (expr instanceof E_Str) {
			builtInFunction = ofac.getFunctionalTerm(OBDAVocabulary.SPARQL_STR, getBooleanTerm( expr.getArg()));
			
		} else if (expr instanceof E_Datatype) {
			builtInFunction = ofac.getFunctionalTerm(OBDAVocabulary.SPARQL_DATATYPE, getBooleanTerm( expr.getArg()));
							
		} else if (expr instanceof E_Lang) {
			Expr arg = expr.getArg();
			if (arg instanceof ExprVar) {
				builtInFunction = ofac.getFunctionalTerm(
						OBDAVocabulary.SPARQL_LANG,
						getVariableTerm((ExprVar) arg));
			}
		} else {
			throw new RuntimeException("The builtin function "
					+ expr.toString() + " is not supported yet!");
		}
		return builtInFunction;
	}

	private Function getBooleanFunction(ExprFunction2 expr, NewLiteral term1,
			NewLiteral term2) {
		Function function = null;
		// The AND and OR expression
		if (expr instanceof E_LogicalAnd) {
			function = ofac.getANDFunction(term1, term2);
		} else if (expr instanceof E_LogicalOr) {
			function = ofac.getORFunction(term1, term2);
		}
		// The Relational expression
		if (expr instanceof E_Equals) {
			function = ofac.getEQFunction(term1, term2);
		} else if (expr instanceof E_NotEquals) {
			function = ofac.getNEQFunction(term1, term2);
		} else if (expr instanceof E_GreaterThan) {
			function = ofac.getGTFunction(term1, term2);
		} else if (expr instanceof E_GreaterThanOrEqual) {
			function = ofac.getGTEFunction(term1, term2);
		} else if (expr instanceof E_LessThan) {
			function = ofac.getLTFunction(term1, term2);
		} else if (expr instanceof E_LessThanOrEqual) {
			function = ofac.getLTEFunction(term1, term2);
		} else if (expr instanceof E_LangMatches) {
			function = ofac.getLANGMATCHESFunction(term1, toLowerCase(term2));
		}
		// The Numerical expression
		if (expr instanceof E_Add) {
			function = ofac.getAddFunction(term1, term2);
		} else if (expr instanceof E_Subtract) {
			function = ofac.getSubstractFunction(term1, term2);
		} else if (expr instanceof E_Multiply) {
			function = ofac.getMultiplyFunction(term1, term2);
		}
		return function;
	}

	private NewLiteral toLowerCase(NewLiteral term) {
		NewLiteral output = term;
		if (term instanceof Function) {
			Function f = (Function) term;
			Predicate functor = f.getFunctionSymbol();
			if (functor instanceof DataTypePredicate) {
				NewLiteral functionTerm = f.getTerm(0);
				if (functionTerm instanceof Constant) {
					Constant c = (Constant) functionTerm;
					output = ofac.getFunctionalTerm(functor, 
							 ofac.getValueConstant(c.getValue().toLowerCase(), 
							 c.getType()));
				}
			}
		}
		return output;
	}

	private NewLiteral getOtherFunctionTerm(ExprFunctionN expr) {
		Function builtInFunction = null;
		if (expr instanceof E_Regex) {
			E_Regex function = (E_Regex) expr;
			Expr arg1 = function.getArg(1); // get the first argument
			Expr arg2 = function.getArg(2); // get the second argument
			Expr arg3 = function.getArg(3); // get the third argument (optional)
			NewLiteral term1 = getBooleanTerm(arg1);
			NewLiteral term2 = getBooleanTerm(arg2);
			NewLiteral term3 = (arg3 != null) ? getBooleanTerm(arg3) : ofac
					.getNULL();
			builtInFunction = ofac.getFunctionalTerm(
					OBDAVocabulary.SPARQL_REGEX, term1, term2, term3);
		} else {
			throw new RuntimeException("The builtin function "
					+ expr.toString() + " is not supported yet!");
		}
		return builtInFunction;
	}

	public List<String> getSignature(Query query) {
		List<String> vars = new ArrayList<String>();
		if (query.isSelectType() || query.isDescribeType()) {
			vars = query.getResultVars();

		} else if (query.isConstructType()) {
			Template constructTemplate = query.getConstructTemplate();
			for (Triple triple : constructTemplate.getTriples()) {
				/*
				 * Check if the subject, predicate, object is a variable.
				 */
				Node subject = triple.getSubject(); // subject
				if (subject instanceof Var) {
					String vs = ((Var) subject).getName();
					vars.add(vs);
				}
				Node predicate = triple.getPredicate(); // predicate
				if (predicate instanceof Var) {
					String vs = ((Var) predicate).getName();
					vars.add(vs);
				}
				Node object = triple.getObject(); // object
				if (object instanceof Var) {
					String vs = ((Var) object).getName();
					vars.add(vs);
				}
			}
		}
		return vars;
	}

	public boolean isBoolean(String query) {
		Query q = QueryFactory.create(query);
		return q.isAskType();
	}

}
