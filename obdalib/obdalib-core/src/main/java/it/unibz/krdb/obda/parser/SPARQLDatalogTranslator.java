package it.unibz.krdb.obda.parser;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDALibConstants;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.VariableImpl;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_GreaterThan;
import com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_Lang;
import com.hp.hpl.jena.sparql.expr.E_LangMatches;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.expr.E_LessThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.E_NotEquals;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction1;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprFunctionN;
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
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

/**
 * Provides the translation from a query string to Java objects.
 * 
 * @see DatalogProgram
 */
public class SPARQLDatalogTranslator {

	/** A factory to construct the predicates */
	private static OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

	/** A utility map to store substitution rule */
	private Map<Variable, Term> mgu = new HashMap<Variable, Term>();

	/**
	 * The default constructor.
	 */
	public SPARQLDatalogTranslator() {
	}

	/**
	 * Returns the datalog object from the parsing process. The syntax of the
	 * query string has the following form:
	 * 
	 * <pre>
	 * {@code
	 * <head> :- <body>
	 * }
	 * </pre>
	 * 
	 * where {@code<head>} is a single predicate and the {@code<body>} is a list
	 * of predicates. An example:
	 * 
	 * <pre>
	 * {@code
	 * result(Name, Code, Phone) :- student(Name, Phone), enrolledIn(Name, Code).
	 * }
	 * </pre>
	 * 
	 * @param query
	 *            a string of SPARQL query.
	 * @return the datalog object.
	 * @throws QueryException
	 *             the syntax is not supported yet.
	 */
	public DatalogProgram parse(String query) throws QueryException {

		Query queryObject = QueryFactory.create(query);

		DatalogProgram datalog = ofac.getDatalogProgram();
		if (queryObject.isDistinct())
			datalog.getQueryModifiers().put("distinct", true);

		// Get the head atom.
		Atom head = getHeadAtom(queryObject);

		Element pattern = queryObject.getQueryPattern();
		ElementGroup root = (ElementGroup) pattern;
		List<Element> elements = root.getElements();

		// Iterate for different element groups from the root.
		List<Atom> body = new LinkedList<Atom>();
		for (Element element : elements) {
			body.addAll(getBodyAtoms(element));
		}
		CQIE rule = ofac.getCQIE(head, body);
		CQIE pev = Util.applyUnifier(rule, mgu);
		datalog.appendRule(pev);
		return datalog;
	}

	public List<String> getSignature(String query) {
		Query queryObject = QueryFactory.create(query);
		return queryObject.getResultVars();
	}

	/** Extract the head atom */
	private Atom getHeadAtom(Query query) throws QueryException {

		Vector<Term> headTerms = new Vector<Term>();
		List<String> termNames = query.getResultVars();

		int termSize = termNames.size();
		for (int i = 0; i < termSize; i++) { // iterate the projectors
			String name = termNames.get(i);
			Term term = ofac.getVariable(name);
			headTerms.add(term);
		}
		Predicate predicate = ofac.getPredicate(OBDALibConstants.QUERY_HEAD_URI, termSize);

		return ofac.getAtom(predicate, headTerms);
	}

	/** Extract the body atoms */
	private List<Atom> getBodyAtoms(Element element) throws QueryException {
		if (element instanceof ElementTriplesBlock) {
			return collectElementTriplesAtom((ElementTriplesBlock) element);
		} else if (element instanceof ElementFilter) {
			return collectElementFilterAtom((ElementFilter) element);
		} else {
			// OPTIONAL and UNION constraints are currently unsupported.
			throw new QueryException("Unsupported query syntax");
		}
	}

	/**
	 * Collects the triple atoms from the WHERE expression.
	 * 
	 * @param elementTriples
	 *            The Triple elements from Jena.
	 */
	private List<Atom> collectElementTriplesAtom(ElementTriplesBlock elementTriples) {
		// Get the triples object.
		BasicPattern triples = elementTriples.getTriples();

		// Instantiate the atom list to stores the known triples
		List<Atom> elementTriplesAtoms = new LinkedList<Atom>();
		for (int j = 0; j < triples.size(); j++) {
			Triple triple = triples.get(j);
			Node o = triple.getObject();
			Node p = triple.getPredicate();
			Node s = triple.getSubject();

			if (!(p instanceof Node_URI)) {
				// if predicate is a variable or literal
				throw new QueryException("Unsupported query syntax");
			}

			// Instantiate the subject and object URI
			URI subjectUri = null;
			URI objectUri = null;

			// Instantiate the subject and object data type
			COL_TYPE subjectType = null;
			COL_TYPE objectType = null;

			// / Instantiate the atom components: predicate and terms.
			Predicate predicate = null;
			Vector<Term> terms = new Vector<Term>();

			if (p.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
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
					terms.add(ofac.getURIConstant(subjectUri));
				}

				// Object node
				if (o instanceof Var) {
					throw new QueryException("Unsupported query syntax");
				} else if (o instanceof Node_Literal) {
					throw new QueryException("Unsupported query syntax");
				} else if (o instanceof Node_URI) {
					Node_URI object = (Node_URI) o;
					objectUri = URI.create(object.getURI());
				}

				// Construct the predicate
				URI predicateUri = objectUri;

				if (predicateUri.toString().equals(OBDAVocabulary.RDFS_LITERAL_URI)) {
					predicate = OBDAVocabulary.RDFS_LITERAL;
				} else if (predicateUri.toString().equals(OBDAVocabulary.XSD_BOOLEAN_URI)) {
					predicate = OBDAVocabulary.XSD_BOOLEAN;
				} else if (predicateUri.toString().equals(OBDAVocabulary.XSD_DATETIME_URI)) {
					predicate = OBDAVocabulary.XSD_DATETIME;
				} else if (predicateUri.toString().equals(OBDAVocabulary.XSD_DECIMAL_URI)) {
					predicate = OBDAVocabulary.XSD_DECIMAL;
				} else if (predicateUri.toString().equals(OBDAVocabulary.XSD_DOUBLE_URI)) {
					predicate = OBDAVocabulary.XSD_DOUBLE;
				} else if (predicateUri.toString().equals(OBDAVocabulary.XSD_FLOAT_URI)) {
					predicate = OBDAVocabulary.XSD_DOUBLE;
				} else if (predicateUri.toString().equals(OBDAVocabulary.XSD_INT_URI)) {
					predicate = OBDAVocabulary.XSD_INTEGER;
				} else if (predicateUri.toString().equals(OBDAVocabulary.XSD_INTEGER_URI)) {
					predicate = OBDAVocabulary.XSD_INTEGER;
				} else if (predicateUri.toString().equals(OBDAVocabulary.XSD_STRING_URI)) {
					predicate = OBDAVocabulary.XSD_STRING;
				} else {
					predicate = ofac.getPredicate(predicateUri, 1, new COL_TYPE[] { subjectType });
				}

			} else {
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
					terms.add(ofac.getURIConstant(subjectUri));
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
						String lang = (object.getLiteralLanguage() == null) ? "" : object.getLiteralLanguage();
						Predicate functionSymbol = ofac.getDataTypePredicateLiteral();
						ValueConstant languageConstant = ofac.getValueConstant(lang, COL_TYPE.LITERAL);
						dataTypeFunction = ofac.getFunctionalTerm(functionSymbol, constant, languageConstant);
					} else {
						// For other supported data-types
						Predicate functionSymbol = getDataTypePredicate(objectType);
						dataTypeFunction = ofac.getFunctionalTerm(functionSymbol, constant);
					}
					terms.add(dataTypeFunction);
				} else if (o instanceof Node_URI) {
					Node_URI object = (Node_URI) o;
					objectType = COL_TYPE.OBJECT;
					objectUri = URI.create(object.getURI());
					terms.add(ofac.getURIConstant(objectUri));
				}
				// Construct the predicate
				URI predicateUri = URI.create(p.getURI());
				predicate = ofac.getPredicate(predicateUri, 2, new COL_TYPE[] { subjectType, objectType });
			}
			// Construct the atom
			Atom atom = ofac.getAtom(predicate, terms);
			elementTriplesAtoms.add(atom);
		}
		return elementTriplesAtoms;
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
		} else if (literal.getLiteralDatatypeURI() == null || literal.getLiteralDatatypeURI().equals(OBDAVocabulary.RDFS_LITERAL_URI)) {
			valid = true;
		} else {
			valid = false;
		}
		if (!valid)
			throw new RuntimeException("Invalid lexical form for datatype. Found: " + value);
		return constant;
		
	}

	/**
	 * Collects the atom from the FILTER expression.
	 * 
	 * @param elementFilter
	 *            The FILTER element from Jena parser.
	 */
	private List<Atom> collectElementFilterAtom(ElementFilter elementFilter) throws QueryException {
		List<Atom> elementFilterAtoms = new LinkedList<Atom>();
		Expr expr = elementFilter.getExpr();
		if (expr instanceof ExprFunction1) {
			// NO-OP
		} else if (expr instanceof ExprFunction2) {
			ExprFunction2 function = (ExprFunction2) expr;
			Expr arg1 = function.getArg1(); // get the first argument
			Expr arg2 = function.getArg2(); // get the second argument
			if (expr instanceof E_LangMatches) {
				// If we find a build-in function LANGMATCHES
				Variable variable = getVariableTerm((ExprVar) ((E_Lang) arg1).getArg());
				ValueConstant languageTag = ofac.getValueConstant(arg2.getConstant().getString(), COL_TYPE.LITERAL);
				Function langMatches = ofac.getFunctionalTerm(ofac.getDataTypePredicateLiteral(), variable, languageTag);
				mgu.put(variable, langMatches);
			} else {
				Term term1 = getBooleanTerm(arg1);
				Term term2 = getBooleanTerm(arg2);
				// Construct the boolean atom
				elementFilterAtoms.add(getBooleanAtom(function, term1, term2));
			}
		} else if (expr instanceof ExprFunctionN) {
			// NO-OP
		}
		return elementFilterAtoms;
	}

	private Term getBooleanTerm(Expr expr) {
		Term term = null;
		if (expr instanceof ExprVar) {
			return getVariableTerm((ExprVar) expr);
		} else if (expr instanceof NodeValue) {
			return getConstantFunctionTerm((NodeValue) expr);
		}

		if (expr instanceof ExprFunction1) {
			// NO-OP
		} else if (expr instanceof ExprFunction2) {
			ExprFunction2 function = (ExprFunction2) expr;
			Expr arg1 = function.getArg1(); // get the first argument
			Expr arg2 = function.getArg2(); // get the second argument
			if (expr instanceof E_LangMatches) {
				// If we find a build-in function LANGMATCHES
				Variable variable = getVariableTerm((ExprVar) ((E_Lang) arg1).getArg());
				ValueConstant languageTag = ofac.getValueConstant(arg2.getConstant().getString(), COL_TYPE.LITERAL);
				Function langMatches = ofac.getFunctionalTerm(ofac.getDataTypePredicateLiteral(), variable, languageTag);
				mgu.put(variable, langMatches);
			} else {
				Term term1 = getBooleanTerm(arg1);
				Term term2 = getBooleanTerm(arg2);
				// Construct the boolean function
				term = getBooleanFunction(function, term1, term2);
			}
		} else if (expr instanceof ExprFunctionN) {
			// NO-OP
		}
		return term;
	}

	private Variable getVariableTerm(ExprVar expr) {
		return ofac.getVariable(expr.getVarName());
	}

	private Function getConstantFunctionTerm(NodeValue expr) {
		Function constantFunction = null;
		if (expr instanceof NodeValueString) {
			constantFunction = ofac.getFunctionalTerm(ofac.getDataTypePredicateString(),
					ofac.getValueConstant(expr.getString(), COL_TYPE.STRING));
		} else if (expr instanceof NodeValueInteger) {
			constantFunction = ofac.getFunctionalTerm(ofac.getDataTypePredicateInteger(),
					ofac.getValueConstant(expr.getInteger() + "", COL_TYPE.INTEGER));
		} else if (expr instanceof NodeValueDecimal) {
			constantFunction = ofac.getFunctionalTerm(ofac.getDataTypePredicateDecimal(),
					ofac.getValueConstant(expr.getDecimal() + "", COL_TYPE.DECIMAL));
		} else if (expr instanceof NodeValueDouble || expr instanceof NodeValueFloat) {
			constantFunction = ofac.getFunctionalTerm(ofac.getDataTypePredicateDouble(),
					ofac.getValueConstant(expr.getDouble() + "", COL_TYPE.DOUBLE));
		} else if (expr instanceof NodeValueDateTime) {
			constantFunction = ofac.getFunctionalTerm(ofac.getDataTypePredicateDateTime(),
					ofac.getValueConstant(expr.getDateTime() + "", COL_TYPE.DATETIME));
		} else if (expr instanceof NodeValueBoolean) {
			constantFunction = ofac.getFunctionalTerm(ofac.getDataTypePredicateBoolean(),
					ofac.getValueConstant(expr.getBoolean() + "", COL_TYPE.BOOLEAN));
		} else if (expr instanceof NodeValueNode) {
			constantFunction = ofac.getFunctionalTerm(ofac.getDataTypePredicateLiteral(),
					ofac.getValueConstant(expr.getNode().getLiteralLexicalForm() + "", COL_TYPE.LITERAL));
		} else {
			throw new QueryException("Unknown data type!");
		}
		return constantFunction;
	}

	private Atom getBooleanAtom(ExprFunction2 expr, Term term1, Term term2) {
		Atom atom = null;
		// The AND and OR expression
		if (expr instanceof E_LogicalAnd) {
			atom = ofac.getANDAtom(term1, term2);
		} else if (expr instanceof E_LogicalOr) {
			atom = ofac.getORAtom(term1, term2);
		}
		// The Relational expression
		if (expr instanceof E_Equals) {
			atom = ofac.getEQAtom(term1, term2);
		} else if (expr instanceof E_NotEquals) {
			atom = ofac.getNEQAtom(term1, term2);
		} else if (expr instanceof E_GreaterThan) {
			atom = ofac.getGTAtom(term1, term2);
		} else if (expr instanceof E_GreaterThanOrEqual) {
			atom = ofac.getGTEAtom(term1, term2);
		} else if (expr instanceof E_LessThan) {
			atom = ofac.getLTAtom(term1, term2);
		} else if (expr instanceof E_LessThanOrEqual) {
			atom = ofac.getLTEAtom(term1, term2);
		}
		return atom;
	}

	private Function getBooleanFunction(ExprFunction2 expr, Term term1, Term term2) {
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
		}
		return function;
	}

	private COL_TYPE getDataType(Node_Literal node) {
		COL_TYPE dataType = null;

		final String dataTypeURI = node.getLiteralDatatypeURI();
		if (dataTypeURI == null) {
			dataType = COL_TYPE.LITERAL;
		} else {
			if (dataTypeURI.equalsIgnoreCase(OBDAVocabulary.RDFS_LITERAL_URI)) {
				dataType = COL_TYPE.LITERAL;
			} else if (dataTypeURI.equalsIgnoreCase(OBDAVocabulary.XSD_STRING_URI)) {
				dataType = COL_TYPE.STRING;
			} else if (dataTypeURI.equalsIgnoreCase(OBDAVocabulary.XSD_INT_URI)
					|| dataTypeURI.equalsIgnoreCase(OBDAVocabulary.XSD_INTEGER_URI)) {
				dataType = COL_TYPE.INTEGER;
			} else if (dataTypeURI.equalsIgnoreCase(OBDAVocabulary.XSD_DECIMAL_URI)) {
				// special case for decimal
				String value = node.getLiteralValue().toString();
				if (value.contains(".")) {
					// Put the type as decimal (with fractions).
					dataType = COL_TYPE.DECIMAL;
				} else {
					// Put the type as integer (decimal without fractions).
					dataType = COL_TYPE.INTEGER;
				}
			} else if (dataTypeURI.equalsIgnoreCase(OBDAVocabulary.XSD_FLOAT_URI)
					|| dataTypeURI.equalsIgnoreCase(OBDAVocabulary.XSD_DOUBLE_URI)) {
				dataType = COL_TYPE.DOUBLE;
			} else if (dataTypeURI.equalsIgnoreCase(OBDAVocabulary.XSD_DATETIME_URI)) {
				dataType = COL_TYPE.DATETIME;
			} else if (dataTypeURI.equalsIgnoreCase(OBDAVocabulary.XSD_BOOLEAN_URI)) {
				dataType = COL_TYPE.BOOLEAN;
			} else {
				throw new RuntimeException("Unsupported datatype: " + dataTypeURI.toString());
			}
		}
		return dataType;
	}

	private Predicate getDataTypePredicate(COL_TYPE dataType) throws QueryException {
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

	private static class Util {

		/***
		 * This method will return a new query, resulting from the application
		 * of the unifier to the original query q. To do this, we will call the
		 * clone() method of the original query and then will call applyUnifier
		 * to each atom of the cloned query.
		 */
		static CQIE applyUnifier(CQIE q, Map<Variable, Term> unifier) {
			CQIE newq = q.clone();

			/* applying the unifier to every term in the head */
			Atom head = newq.getHead();
			applyUnifier(head, unifier);
			for (Atom bodyatom : newq.getBody()) {
				applyUnifier(bodyatom, unifier);
			}
			return newq;
		}

		private static void applyUnifier(Atom atom, Map<Variable, Term> unifier) {
			applyUnifier(atom.getTerms(), unifier);
		}

		private static void applyUnifier(List<Term> terms, Map<Variable, Term> unifier) {
			for (int i = 0; i < terms.size(); i++) {
				Term t = terms.get(i);
				if (t instanceof VariableImpl) {
					Term replacement = unifier.get(t);
					if (replacement != null) {
						terms.set(i, replacement);
					}
				} else if (t instanceof Function) {
					applyUnifier((Function) t, unifier);
				}
			}
		}

		private static void applyUnifier(Function term, Map<Variable, Term> unifier) {
			List<Term> terms = term.getTerms();
			applyUnifier(terms, unifier);
		}
	}
}
