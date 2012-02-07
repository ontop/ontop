package it.unibz.krdb.obda.parser;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDALibConstants;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;


/**
 * Provides the translation from a query string to Java objects.
 *
 * @see DatalogProgram
 */
public class SPARQLDatalogTranslator {

	/** A factory to construct the subject and object terms */
	private OBDADataFactory termFactory = OBDADataFactoryImpl.getInstance();

	/** A factory to construct the predicates */
	private OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();

	/**
	 * The default constructor.
	 */
	public SPARQLDatalogTranslator() { }

	/**
	 * Returns the datalog object from the parsing process. The syntax
	 * of the query string has the following form:
	 * <pre>
	 * {@code
	 * <head> :- <body>
	 * } </pre>
	 * where {@code<head>} is a single predicate and the {@code<body>} is a
	 * list of predicates. An example:
	 * <pre>
	 * {@code
	 * result(Name, Code, Phone) :- student(Name, Phone), enrolledIn(Name, Code).
	 * } </pre>
	 *
	 * @param query a string of SPARQL query.
	 * @return the datalog object.
	 * @throws QueryException the syntax is not supported yet.
	 */
	public DatalogProgram parse(String query) throws QueryException {

		Query queryObject = QueryFactory.create(query);

		DatalogProgram datalog = this.predicateFactory.getDatalogProgram();
		if (queryObject.isDistinct())
			datalog.getQueryModifiers().put("distinct", true);

		// Get the head atom.
		Atom head = getHeadAtom(queryObject);

		Element pattern = queryObject.getQueryPattern();
		ElementGroup root = (ElementGroup) pattern;
		List<Element> elements = root.getElements();

		// Iterate for different element groups from the root.
		for (Element element : elements) {
			List<Atom> body = getBodyAtoms(element); // Get the body atoms.

			CQIE rule = predicateFactory.getCQIE(head, body);
			datalog.appendRule(rule);
		}

		return datalog;
	}

	/** Extract the head atom */
	private Atom getHeadAtom(Query query) throws QueryException {

		Vector<Term> headTerms = new Vector<Term>();
		List<String> termNames = query.getResultVars();

		int termSize = termNames.size();
		for (int i = 0; i < termSize; i++) { // iterate the projectors
			String name = termNames.get(i);
			Term term = termFactory.getVariable(name);
			headTerms.add(term);
		}
		Predicate predicate =
			predicateFactory.getPredicate(OBDALibConstants.QUERY_HEAD_URI, termSize);

		return predicateFactory.getAtom(predicate, headTerms);
	}

	/** Extract the body atoms */
	private LinkedList<Atom> getBodyAtoms(Element element) throws QueryException {

		ElementTriplesBlock triplesBlock = null; // initiate the triples

		if (element instanceof ElementTriplesBlock)
			triplesBlock = (ElementTriplesBlock) element;
		else
			// OPTIONAL, UNION and FILTER constraints are currently unsupported.
			throw new QueryException("Unsupported query syntax");

		BasicPattern triples = triplesBlock.getTriples();

		LinkedList<Atom> body = new LinkedList<Atom>();
		for (int j = 0; j < triples.size(); j++) {
			Vector<Term> terms = new Vector<Term>();
			Triple triple = triples.get(j);
			Node o = triple.getObject();
			Node p = triple.getPredicate();
			Node s = triple.getSubject();

			if (!(p instanceof Node_URI)) { // predicate is a variable or literal
				throw new QueryException("Unsupported query");
			}

			URI subjectUri = null;
			URI objectUri = null;
			Predicate predicate = null;
			if (p.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
				if (s instanceof Var) { // Subject is a variable
					Var subject = (Var) s;
					terms.add(termFactory.getVariable(subject.getName()));
				}
				else if (s instanceof Node_Literal) { // Subject is a node literal
					Node_Literal subject = (Node_Literal) s;
					terms.add(getValueConstant(subject));
				}
				else if (s instanceof Node_URI) { // Subject is a node URI
					Node_URI subject = (Node_URI) s;
					subjectUri = URI.create(subject.getURI());
					terms.add(termFactory.getURIConstant(subjectUri));
				}

				if (o instanceof Var) { // Object is a variable
					throw new QueryException("Unsupported query syntax");
				}
				else if (o instanceof Node_Literal) { // Object is a node literal
					throw new QueryException("Use class URI instead of node literal");
				}
				else if (o instanceof Node_URI) { // Object is a node URI
					Node_URI object = (Node_URI) o;
					objectUri = URI.create(object.getURI());
				}
				predicate = predicateFactory.getPredicate(objectUri, 1);
			}
			else { // not equal to "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
				if (s instanceof Var) { // Subject is a variable
					Var subject = (Var) s;
					terms.add(termFactory.getVariable(subject.getName()));
				}
				else if (s instanceof Node_Literal) { // Subject is a node literal
					Node_Literal subject = (Node_Literal) s;
					terms.add(getValueConstant(subject));
				}
				else if (s instanceof Node_URI) { // Subject is a node URI
					Node_URI subject = (Node_URI) s;
					subjectUri = URI.create(subject.getURI());
					terms.add(termFactory.getURIConstant(subjectUri));
				}

				if (o instanceof Var) { // Object is a variable
					Var object = (Var) o;
					terms.add(termFactory.getVariable(object.getName()));
				}
				else if (o instanceof Node_Literal) { // Object is a node literal
					Node_Literal object = (Node_Literal) o;
					terms.add(getValueConstant(object));
				}
				else if (o instanceof Node_URI) { // Object is a node URI
					Node_URI object = (Node_URI) o;
					objectUri = URI.create(object.getURI());
					terms.add(termFactory.getURIConstant(objectUri));
				}
				URI predicateUri = URI.create(p.getURI());
				predicate = predicateFactory.getPredicate(predicateUri, 2);
			}
			Atom atom = predicateFactory.getAtom(predicate, terms);
			body.add(atom);
		}
		return body;
	}
	
	private ValueConstant getValueConstant(Node_Literal subject) {
		final String dataTypeURI = subject.getLiteralDatatypeURI();
		final String value = subject.getLiteralValue().toString();
		
		ValueConstant constant = null;
		
		if (dataTypeURI == null) {
			return termFactory.getValueConstant(value, COL_TYPE.LITERAL);
		}
		else {		
			if (dataTypeURI.equalsIgnoreCase(OBDAVocabulary.RDFS_LITERAL_URI)) {
				constant = termFactory.getValueConstant(value, COL_TYPE.LITERAL);
			} else if (dataTypeURI.equalsIgnoreCase(OBDAVocabulary.XSD_STRING_URI)) {
				constant = termFactory.getValueConstant(value, COL_TYPE.STRING);
			} else if (dataTypeURI.equalsIgnoreCase(OBDAVocabulary.XSD_INT_URI)
					|| dataTypeURI.equalsIgnoreCase(OBDAVocabulary.XSD_INTEGER_URI)) {
				constant = termFactory.getValueConstant(value, COL_TYPE.INTEGER);
			} else if (dataTypeURI.equalsIgnoreCase(OBDAVocabulary.XSD_DECIMAL_URI)) {
				if (value.contains(".")) {
					// We "override" the decimal to datatype double.
					constant = termFactory.getValueConstant(value, COL_TYPE.DOUBLE);
				} else {
					// We "cast" the decimal to datatype integer.
					constant = termFactory.getValueConstant(value, COL_TYPE.INTEGER);
				}				
			} else if (dataTypeURI.equalsIgnoreCase(OBDAVocabulary.XSD_FLOAT_URI)
					|| dataTypeURI.equalsIgnoreCase(OBDAVocabulary.XSD_DOUBLE_URI)) {
				constant = termFactory.getValueConstant(value, COL_TYPE.DOUBLE);
			} else if (dataTypeURI.equalsIgnoreCase(OBDAVocabulary.XSD_DATETIME_URI)) {
				constant = termFactory.getValueConstant(value, COL_TYPE.DATETIME);
			} else if (dataTypeURI.equalsIgnoreCase(OBDAVocabulary.XSD_BOOLEAN_URI)) {
				constant = termFactory.getValueConstant(value, COL_TYPE.BOOLEAN);
			} else {
				constant = termFactory.getValueConstant(value, COL_TYPE.LITERAL); // TODO should throw an exception
			}
		}
		return constant;
	}
}
