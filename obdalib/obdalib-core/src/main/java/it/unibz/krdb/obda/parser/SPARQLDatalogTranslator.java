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
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_GreaterThan;
import com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.expr.E_LessThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_NotEquals;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueBoolean;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDateTime;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDecimal;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDouble;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueFloat;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueInteger;
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
		List<Atom> body = new LinkedList<Atom>();
		for (Element element : elements) {
			body.addAll(getBodyAtoms(element));
		}
		CQIE rule = predicateFactory.getCQIE(head, body);
		datalog.appendRule(rule);
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
		Predicate predicate = predicateFactory.getPredicate(OBDALibConstants.QUERY_HEAD_URI, termSize);

		return predicateFactory.getAtom(predicate, headTerms);
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
		    
		    /// Instantiate the atom components: predicate and terms.
			Predicate predicate = null;
			Vector<Term> terms = new Vector<Term>();
		    
			if (p.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {			    
			    // Subject node
				if (s instanceof Var) {
					Var subject = (Var) s;
					terms.add(termFactory.getVariable(subject.getName()));
				}
				else if (s instanceof Node_Literal) {
					Node_Literal subject = (Node_Literal) s;
					String value = subject.getLiteralValue().toString();
					subjectType = getDataType(subject);
                    terms.add(termFactory.getValueConstant(value, subjectType));
				}
				else if (s instanceof Node_URI) {
					Node_URI subject = (Node_URI) s;
					subjectType = COL_TYPE.OBJECT;
					subjectUri = URI.create(subject.getURI());
					terms.add(termFactory.getURIConstant(subjectUri));
				}
				
				// Object node
				if (o instanceof Var) {
					throw new QueryException("Unsupported query syntax");
				}
				else if (o instanceof Node_Literal) {
					throw new QueryException("Unsupported query syntax");
				}
				else if (o instanceof Node_URI) {
					Node_URI object = (Node_URI) o;
					objectUri = URI.create(object.getURI());
				}
				
				// Construct the predicate
				URI predicateUri = objectUri;
				predicate = predicateFactory.getPredicate(predicateUri, 1, new COL_TYPE[] { subjectType });
			}
			else {
			    // Subject node
				if (s instanceof Var) {
					Var subject = (Var) s;
					terms.add(termFactory.getVariable(subject.getName()));
				}
				else if (s instanceof Node_Literal) {
					Node_Literal subject = (Node_Literal) s;
					String value = subject.getLiteralValue().toString();
					subjectType = getDataType(subject);
					terms.add(termFactory.getValueConstant(value, subjectType));
				}
				else if (s instanceof Node_URI) {
					Node_URI subject = (Node_URI) s;
					subjectType = COL_TYPE.OBJECT;
					subjectUri = URI.create(subject.getURI());
					terms.add(termFactory.getURIConstant(subjectUri));
				}

                // Object node
				if (o instanceof Var) {
					Var object = (Var) o;
					terms.add(termFactory.getVariable(object.getName()));
				}
				else if (o instanceof Node_Literal) {
					Node_Literal object = (Node_Literal) o;
					String value = object.getLiteralValue().toString();
					objectType = getDataType(object);
                    ValueConstant constant = termFactory.getValueConstant(value, objectType);
                    
                    // v1.7: We extend the syntax such that the data type of a constant 
                    // is defined using a functional symbol. 
                    Function dataTypeFunction = termFactory.getFunctionalTerm(getDataTypePredicate(objectType), constant);
                    terms.add(dataTypeFunction);
				}
				else if (o instanceof Node_URI) {
					Node_URI object = (Node_URI) o;
					objectType = COL_TYPE.OBJECT;
					objectUri = URI.create(object.getURI());
					terms.add(termFactory.getURIConstant(objectUri));
				}
				
				// Construct the predicate
				URI predicateUri = URI.create(p.getURI());
				predicate = predicateFactory.getPredicate(predicateUri, 2, new COL_TYPE[] { subjectType, objectType });
			}
			// Construct the atom
			Atom atom = predicateFactory.getAtom(predicate, terms);
			elementTriplesAtoms.add(atom);
		}
		return elementTriplesAtoms;
	}
	
	private List<Atom> collectElementFilterAtom(ElementFilter elementFilter) {
		
		List<Atom> elementFilterAtoms = new LinkedList<Atom>();
		
		Expr expr = elementFilter.getExpr();
		
		if (expr instanceof ExprFunction2) {
			// The expected function should be: (VAR) (OP) (VAR|LITERAL)
			ExprFunction2 function = (ExprFunction2) expr;
			Expr arg1 = function.getArg1();
			Expr arg2 = function.getArg2();
			
			Term term1 = getTermOfRelationalArgument(arg1, 1);
			Term term2 = getTermOfRelationalArgument(arg2, 2);			
			
			if (expr instanceof E_Equals) {
				elementFilterAtoms.add(termFactory.getEQAtom(term1, term2));
			} else if (expr instanceof E_NotEquals) {
				elementFilterAtoms.add(termFactory.getNEQAtom(term1, term2));
			} else if (expr instanceof E_GreaterThan) {
				elementFilterAtoms.add(termFactory.getGTAtom(term1, term2));
			} else if (expr instanceof E_GreaterThanOrEqual) {
				elementFilterAtoms.add(termFactory.getGTEAtom(term1, term2));
			} else if (expr instanceof E_LessThan) {
				elementFilterAtoms.add(termFactory.getLTAtom(term1, term2));
			} else if (expr instanceof E_LessThanOrEqual) {
				elementFilterAtoms.add(termFactory.getLTEAtom(term1, term2));
			} else {
				throw new QueryException("Unsupported query syntax");
			}
		} else {
			throw new QueryException("Unsupported query syntax");
		}
		return elementFilterAtoms;
	}
	
	private Term getTermOfRelationalArgument(Expr arg, int index) {
		Term term = null;
		if (arg instanceof ExprVar) {
			term = termFactory.getVariable(arg.getVarName());
		} else if (arg instanceof NodeValue){
			if (index == 1) {
				// if the first argument is a constant.
				throw new QueryException("Unsupported query syntax");
			}
			NodeValue node = (NodeValue) arg;
			if (node instanceof NodeValueString) {
				term = termFactory.getValueConstant(node.getString(), COL_TYPE.STRING);
			} else if (node instanceof NodeValueInteger) {
				term = termFactory.getValueConstant(node.getInteger()+"", COL_TYPE.INTEGER);
			} else if (node instanceof NodeValueDecimal) {
				// TODO: Change this into COL_TYPE.DECIMAL in the future
				term = termFactory.getValueConstant(node.getDouble()+"", COL_TYPE.DOUBLE);
			} else if (node instanceof NodeValueDouble || node instanceof NodeValueFloat) {
				term = termFactory.getValueConstant(node.getDouble()+"", COL_TYPE.DOUBLE);
			} else if (node instanceof NodeValueDateTime) {
				term = termFactory.getValueConstant(node.getDateTime()+"", COL_TYPE.DATETIME);
			} else if (node instanceof NodeValueBoolean) {
				term = termFactory.getValueConstant(node.getBoolean()+"", COL_TYPE.BOOLEAN);
			} else {
				throw new QueryException("Unsupported data type");
			}
		}
		return term;
	}
	
	private COL_TYPE getDataType(Node_Literal node) {
        COL_TYPE dataType = null;
	    
	    final String dataTypeURI = node.getLiteralDatatypeURI();
	    if (dataTypeURI == null) {
	        dataType = COL_TYPE.LITERAL;
        }
        else {      
            if (dataTypeURI.equalsIgnoreCase(OBDAVocabulary.RDFS_LITERAL_URI)) {
                dataType = COL_TYPE.LITERAL;
            } else if (dataTypeURI.equalsIgnoreCase(OBDAVocabulary.XSD_STRING_URI)) {
                dataType = COL_TYPE.STRING;
            } else if (dataTypeURI.equalsIgnoreCase(OBDAVocabulary.XSD_INT_URI)
                    || dataTypeURI.equalsIgnoreCase(OBDAVocabulary.XSD_INTEGER_URI)) {
                dataType = COL_TYPE.INTEGER;
            } else if (dataTypeURI.equalsIgnoreCase(OBDAVocabulary.XSD_DECIMAL_URI)) {  // special case for decimal
                String value = node.getLiteralValue().toString();
                if (value.contains(".")) {
                    // We "override" the decimal to double.
                    dataType = COL_TYPE.DOUBLE;
                } else {
                    // We "cast" the decimal to integer.
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
                dataType = COL_TYPE.LITERAL; // TODO should throw an exception
            }
        }
	    return dataType;	    	    
	}
	
	private Predicate getDataTypePredicate(COL_TYPE dataType) {
	    Predicate predicate = null;
	    
	    switch (dataType) {
	        case LITERAL: 
	            predicate = termFactory.getPredicate(OBDAVocabulary.RDFS_LITERAL_URI, 1, new COL_TYPE[] { dataType }); break;
	        case STRING:
	            predicate = termFactory.getPredicate(OBDAVocabulary.XSD_STRING_URI, 1, new COL_TYPE[] { dataType }); break;
	        case INTEGER:
                predicate = termFactory.getPredicate(OBDAVocabulary.XSD_INTEGER_URI, 1, new COL_TYPE[] { dataType }); break;
	        case DOUBLE:
                predicate = termFactory.getPredicate(OBDAVocabulary.XSD_DOUBLE_URI, 1, new COL_TYPE[] { dataType }); break;
	        case DATETIME:
                predicate = termFactory.getPredicate(OBDAVocabulary.XSD_DATETIME_URI, 1, new COL_TYPE[] { dataType }); break;
	        case BOOLEAN:
                predicate = termFactory.getPredicate(OBDAVocabulary.XSD_BOOLEAN_URI, 1, new COL_TYPE[] { dataType }); break;
	    }
	    return predicate;
	}
}
