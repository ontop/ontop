/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.translator;

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.io.SimplePrefixManager;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAQueryModifiers;
import it.unibz.krdb.obda.model.OBDAQueryModifiers.OrderCondition;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.TermUtil;
import it.unibz.krdb.obda.owlrefplatform.core.translator.DatalogToSparqlTranslator;
import it.unibz.krdb.obda.owlrefplatform.core.translator.SparqlKeyword;
import it.unibz.krdb.obda.owlrefplatform.core.translator.UnknownArithmeticSymbolException;
import it.unibz.krdb.obda.owlrefplatform.core.translator.UnknownBooleanSymbolException;

import java.util.List;

public class DatalogToSparqlTranslator {

	private static DatalogToSparqlTranslator instance = null;

	private static OBDADataFactory dataFactory = OBDADataFactoryImpl.getInstance();
	
	private static final URIConstant RDF_TYPE = dataFactory.getConstantURI(OBDAVocabulary.RDF_TYPE);

	private PrefixManager prefixManager;

	private OBDAQueryModifiers queryModifiers;

	// Prevent external instantiation
	private DatalogToSparqlTranslator(PrefixManager pm) {
		prefixManager = pm;
	}

	private DatalogToSparqlTranslator() {
		this(new SimplePrefixManager());
	}

	public static void init(PrefixManager pm) {
		instance = new DatalogToSparqlTranslator(pm);
	}

	public static DatalogToSparqlTranslator getInstance() {
		if (instance == null) {
			instance = new DatalogToSparqlTranslator();
		}
		return instance;
	}

	public String toSparql(DatalogProgram datalog) {
		StringBuilder sb = new StringBuilder();
		
		// Print the prefix declaration, if possible
		printPrefixDeclaration(sb);
		sb.append("\n");
		
		queryModifiers = datalog.getQueryModifiers();
		
		// Print the query projection
		printQueryProjection(datalog, sb);
		
		// Print the query body
		printQueryBody(datalog, sb);
		
		// Print the query modifier, if any
		if (datalog.hasModifiers()) {
			printQueryModifier(datalog, sb);
		}
		return sb.toString();
	}

	public String toSparql(Term term) {
		if (term instanceof Variable) {
			return "?" + TermUtil.toString(term);
		} else if (term instanceof URIConstant) {
			return shortenName(TermUtil.toString(term));
		} else if (term instanceof Function) {
			return toSparql((Function) term);
		}
		return TermUtil.toString(term); // for the other types of term
	}

	// TODO: The current OBDA model should be refactored. The current structure prevents 
	// utilizing Java polymorphism in the implementation of the following methods.

	public String toSparql(Function function) {
		StringBuilder sb = new StringBuilder();
		
		Predicate functionSymbol = function.getFunctionSymbol();
		if (functionSymbol.isTriplePredicate()) {
			// For triple predicate
			final Term subject = getTripleSubject(function);
			final Term predicate = getTriplePredicate(function);
			final Term object = getTripleObject(function);
			final String tripleGraph = printTripleGraph(toSparql(subject), toSparql(predicate), toSparql(object));
			sb.append(tripleGraph);
			
		} else {
			// For non-triple predicates
			final Term subject = getSubject(function);
			final Term predicate = (functionSymbol.isClass() ? RDF_TYPE : getPredicate(function));
			final Term object = (functionSymbol.isClass() ? getPredicate(function) : getObject(function));

			// Check the function symbols
			if (functionSymbol.isArithmeticPredicate()) {
				final String expressionGraph = printTripleGraph(toSparql(subject), getArithmeticSymbol(functionSymbol), toSparql(object));
				sb.append(enclosedBrackets(expressionGraph));
			} else if (functionSymbol.isBooleanPredicate()) {
				final String expressionGraph = printTripleGraph(toSparql(subject), getBooleanSymbol(functionSymbol), toSparql(object));
				if (functionSymbol.equals(OBDAVocabulary.AND) || functionSymbol.equals(OBDAVocabulary.OR)) {
					sb.append(enclosedBrackets(expressionGraph));
				} else {
					sb.append(expressionGraph);
				}
			} else {
				final String tripleGraph = printTripleGraph(toSparql(subject), toSparql(predicate), toSparql(object));
				sb.append(tripleGraph);
			}
		}
		return sb.toString();
	}

	protected static String printTripleGraph(String subject, String predicate, String object) {
		return subject + " " + predicate + " " + object;
	}

	protected static String enclosedBrackets(String expression) {
		return "( " + expression + " )";
	}

	/**
	 * Returns the arithmetic symbols for binary operations given its function symbol.
	 */
	public static String getArithmeticSymbol(Predicate functionSymbol) {
		if (functionSymbol.equals(OBDAVocabulary.ADD)) {
			return SparqlKeyword.ADD;
		} else if (functionSymbol.equals(OBDAVocabulary.SUBSTRACT)) {
			return SparqlKeyword.SUBSTRACT;
		} else if (functionSymbol.equals(OBDAVocabulary.MULTIPLY)) {
			return SparqlKeyword.MULTIPLY;
		}
		throw new UnknownArithmeticSymbolException(functionSymbol.getName());
	}

	/**
	 * Returns the boolean symbols for binary operations given its function symbol.
	 */
	public static String getBooleanSymbol(Predicate functionSymbol) {
		if (functionSymbol.equals(OBDAVocabulary.AND)) {
			return SparqlKeyword.AND;
		} else if (functionSymbol.equals(OBDAVocabulary.OR)) {
			return SparqlKeyword.OR;
		} else if (functionSymbol.equals(OBDAVocabulary.EQ)) {
			return SparqlKeyword.EQUALS;
		} else if (functionSymbol.equals(OBDAVocabulary.NEQ)) {
			return SparqlKeyword.NOT_EQUALS;
		} else if (functionSymbol.equals(OBDAVocabulary.GT)) {
			return SparqlKeyword.GREATER_THAN;
		} else if (functionSymbol.equals(OBDAVocabulary.GTE)) {
			return SparqlKeyword.GREATER_THAN_AND_EQUALS;
		} else if (functionSymbol.equals(OBDAVocabulary.LT)) {
			return SparqlKeyword.LESS_THAN;
		} else if (functionSymbol.equals(OBDAVocabulary.LTE)) {
			return SparqlKeyword.LESS_THAN_AND_EQUALS;
		}
		throw new UnknownBooleanSymbolException(functionSymbol.getName());
	}

	/*
	 * Makes the input string short by creating a prefixed value, if possible.
	 */
	private String shortenName(String value) {
		return prefixManager.getShortForm(value);
	}

	// TODO: Create a distinct class for Triple/Class/Property function to have the 
	// following methods specialized. The current implementation requires the
	// distinction are selected using IF condition in client code.
	
	/*
	 * For Triple function, i.e., triple(s, p, o)
	 */
	public static Term getTripleSubject(Function function) {
		return function.getTerm(0);
	}

	public static Term getTriplePredicate(Function function) {
		return function.getTerm(1);
	}

	public static Term getTripleObject(Function function) {
		return function.getTerm(2);
	}

	/*
	 * For binary function, e.g., Person(x), hasName(x, y)
	 */
	public static Term getSubject(Function function) {
		return function.getTerm(0);
	}

	public static Term getPredicate(Function function) {
		Predicate predicate = function.getFunctionSymbol();
		return dataFactory.getConstantURI(predicate.getName());
	}

	public static Term getObject(Function function) {
		return function.getTerm(1);
	}

	/*
	 * Private methods for printing SPARQL and formatting.
	 */

	private void printPrefixDeclaration(StringBuilder sb) {
		for (String prefix : prefixManager.getPrefixMap().keySet()) {
			String iri = prefixManager.getURIDefinition(prefix);
			sb.append(SparqlKeyword.PREFIX + " " + prefix);
			sb.append("\t");
			sb.append("<" + iri + ">");
			sb.append("\n");
		}
	}

	private void printQueryProjection(DatalogProgram datalog, StringBuilder sb) {
		sb.append(SparqlKeyword.SELECT + " ");
		if (queryModifiers.isDistinct()) {
			sb.append(SparqlKeyword.DISTINCT + " ");
		}
		CQIE mainQuery = datalog.getRules().get(0);  // assume the first rule contains the projection
		for (Term term : mainQuery.getHead().getTerms()) {
			sb.append(toSparql(term));
			sb.append(" ");
		}
		sb.append("\n");
	}

	private void printQueryBody(DatalogProgram datalog, StringBuilder sb) {
		sb.append(SparqlKeyword.WHERE + " {");
		sb.append("\n");
		List<CQIE> mainQueries = getMainQueries(datalog);
		
		if (hasMultipleDefintion(mainQueries)) {
			printUnionGraphPattern(mainQueries, datalog, sb, 1);
		} else {
			printGraphPattern(mainQueries.get(0), datalog, sb, 1);
		}
		sb.append("}");
	}

	private void printGraphPattern(CQIE query, DatalogProgram datalog, StringBuilder sb, int indentLevel) {
		List<Function> queryBody = query.getBody();
		for (Function graph : queryBody) {
			Predicate graphPredicate = graph.getFunctionSymbol();
			if (graphPredicate.isAlgebraPredicate()) {
				printJoinExpression(graph, datalog, sb, indentLevel);
			} else if (graphPredicate.isBooleanPredicate()) {
				printBooleanFilter(graph, sb, indentLevel);
			} else {
				printGraph(graph, datalog, sb, indentLevel);
			}
		}
	}

	private void printJoinExpression(Function expression, DatalogProgram datalog, StringBuilder sb, int indentLevel) {
		Predicate joinPredicate = expression.getFunctionSymbol();
		if (joinPredicate.equals(OBDAVocabulary.SPARQL_JOIN)) {
			printGraph((Function) expression.getTerm(0), datalog, sb, indentLevel);
			printGraph((Function) expression.getTerm(1), datalog, sb, indentLevel);
		} else if (joinPredicate.equals(OBDAVocabulary.SPARQL_LEFTJOIN)) {
			printGraph((Function) expression.getTerm(0), datalog, sb, indentLevel);
			sb.append(indent(indentLevel));
			sb.append(SparqlKeyword.OPTIONAL + " {\n");
			printGraph((Function) expression.getTerm(1), datalog, sb, indentLevel+1);
			sb.append(indent(indentLevel));
			sb.append("}");
			sb.append("\n");
		}
	}

	private void printGraph(Function graph, DatalogProgram datalog, StringBuilder sb, int indentLevel) {
		Predicate graphPredicate = graph.getFunctionSymbol();
		List<CQIE> rules = datalog.getRules(graphPredicate);
		if (hasDefinition(rules)) {
			if (hasMultipleDefintion(rules)) {
				printUnionGraphPattern(rules, datalog, sb, indentLevel);
			} else {
				printGraphPattern(rules.get(0), datalog, sb, indentLevel);
			}
		} else {
			printTriple(graph, sb, indentLevel);
		}
	}
	
	private void printTriple(Function graph, StringBuilder sb, int indentLevel) {
		sb.append(indent(indentLevel));
		sb.append(toSparql(graph));
		sb.append(" .");
		sb.append("\n");
	}

	private void printBooleanFilter(Function graph, StringBuilder sb, int indentLevel) {
		sb.append(indent(indentLevel));
		sb.append(SparqlKeyword.FILTER + " ");
		sb.append("( ");
		sb.append(toSparql(graph));
		sb.append(" ) .");
		sb.append("\n");
	}

	private void printUnionGraphPattern(List<CQIE> queries, DatalogProgram datalog, StringBuilder sb, int indentLevel) {
		boolean needUnion = false;
		for (CQIE query : queries) {
			if (needUnion) {
				sb.append(indent(indentLevel) + SparqlKeyword.UNION);
				sb.append("\n");
			}
			sb.append(indent(1) + "{");
			sb.append("\n");
			
			printGraphPattern(query, datalog, sb, indentLevel+1);
			
			sb.append(indent(indentLevel) + "}");
			sb.append("\n");
			
			needUnion = true;
		}
	}

	private void printQueryModifier(DatalogProgram datalog, StringBuilder sb) {
		sb.append("\n");
		if (queryModifiers.hasLimit()) {
			sb.append(SparqlKeyword.LIMIT + " " + queryModifiers.getLimit());
			sb.append("\n");
		}
		if (queryModifiers.hasOffset()) {
			sb.append(SparqlKeyword.OFFSET + " " + queryModifiers.getOffset());
			sb.append("\n");
		}
		if (queryModifiers.hasOrder()) {
			sb.append(SparqlKeyword.ORDER_BY + " ");
			for (OrderCondition condition : queryModifiers.getSortConditions()) {
				String var = toSparql(condition.getVariable());
				switch (condition.getDirection()) {
					case OrderCondition.ORDER_ASCENDING: sb.append(SparqlKeyword.ASCENDING + " (" + var + ")"); break;
					case OrderCondition.ORDER_DESCENDING: sb.append(SparqlKeyword.DESCENDING + " (" + var + ")"); break;
					default: sb.append(var);
				}
				sb.append(" ");
			}
		}
	}

	private boolean hasDefinition(List<CQIE> rules) {
		return rules.size() == 0 ? false : true;
	}

	private boolean hasMultipleDefintion(List<CQIE> rules) {
		return rules.size() > 1 ? true : false;
	}

	private List<CQIE> getMainQueries(DatalogProgram datalog) {
		// TODO: Predicate and DatalogProgram need a code refactor.
		
		/* Unusual method implementation. Getting rules from DatalogProgram
		 * requires a Predicate object. And creating a Predicate object requires
		 * the arity number and types in advanced, which are sometimes impossible
		 * to get.
		 * 
		 * This method uses a way around such that it gets the first rule
		 * (Assumption: the first position is always the main query). And
		 * then getting the predicate to get the other main queries, if available.
		 */
		CQIE mainQuery = datalog.getRules().get(0);
		Predicate mainQueryPredicate = mainQuery.getHead().getFunctionSymbol();
		return datalog.getRules(mainQueryPredicate);
	}
	
	private static final String INDENT = "   ";
	
	private String indent(int indentLevel) {
		String indent = "";
		if (indentLevel > 0) {
			// repeat text
			indent = new String(new char[indentLevel]).replace("\0", INDENT);
		}
		return indent;
	}
}
