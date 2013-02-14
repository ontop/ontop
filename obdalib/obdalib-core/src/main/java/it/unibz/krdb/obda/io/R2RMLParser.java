package it.unibz.krdb.obda.io;

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.GraphUtil;

public class R2RMLParser {

	private GraphUtil util = new GraphUtil();

	public static final ValueFactory fact = new ValueFactoryImpl();
	public final URI logicalTable = fact.createURI("http://www.w3.org/ns/r2rml#logicalTable");
	public final URI tableName = fact.createURI("http://www.w3.org/ns/r2rml#tableName");
	public final URI baseTableOrView = fact.createURI("http://www.w3.org/ns/r2rml#baseTableOrView");
	public final URI r2rmlView = fact.createURI("http://www.w3.org/ns/r2rml#R2RMLView");

	public final URI subjectMap = fact.createURI("http://www.w3.org/ns/r2rml#subjectMap");
	public final URI subject = fact.createURI("http://www.w3.org/ns/r2rml#subject");
	public final URI predicateObjectMap = fact.createURI("http://www.w3.org/ns/r2rml#predicateObjectMap");
	public final URI predicateMap = fact.createURI("http://www.w3.org/ns/r2rml#predicateMap");
	public final URI objectMap = fact.createURI("http://www.w3.org/ns/r2rml#objectMap");
	public final URI object = fact.createURI("http://www.w3.org/ns/r2rml#object");
	public final URI refObjectMap = fact.createURI("http://www.w3.org/ns/r2rml#refObjectMap");
	public final URI graphMap = fact.createURI("http://www.w3.org/ns/r2rml#graphMap");
	public final URI graph = fact.createURI("http://www.w3.org/ns/r2rml#graph");

	public final URI predicate = fact.createURI("http://www.w3.org/ns/r2rml#predicate");
	public final URI template = fact.createURI("http://www.w3.org/ns/r2rml#template");
	public final URI column = fact.createURI("http://www.w3.org/ns/r2rml#column");
	public final URI constant = fact.createURI("http://www.w3.org/ns/r2rml#constant");
	public final URI termType = fact.createURI("http://www.w3.org/ns/r2rml#termType");
	public final URI language = fact.createURI("http://www.w3.org/ns/r2rml#language");
	public final URI datatype = fact.createURI("http://www.w3.org/ns/r2rml#datatype");
	public final URI inverseExpression = fact.createURI("http://www.w3.org/ns/r2rml#inverseExpression");
	public final URI iri = fact.createURI("http://www.w3.org/ns/r2rml#IRI");
	public final URI blankNode = fact.createURI("http://www.w3.org/ns/r2rml#BlankNode");
	public final URI literal = fact.createURI("http://www.w3.org/ns/r2rml#Literal");
	public final URI classUri = fact.createURI("http://www.w3.org/ns/r2rml#class");
	public final URI sqlQuery = fact.createURI("http://www.w3.org/ns/r2rml#sqlQuery");
	public final URI sqlVersion = fact.createURI("http://www.w3.org/ns/r2rml#sqlVersion");

	public final URI parentTriplesMap = fact.createURI("http://www.w3.org/ns/r2rml#parentTriplesMap");
	public final URI joinCondition = fact.createURI("http://www.w3.org/ns/r2rml#joinCondition");
	public final URI child = fact.createURI("http://www.w3.org/ns/r2rml#child");
	public final URI parent = fact.createURI("http://www.w3.org/ns/r2rml#parent");

	private OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	Iterator<Statement> iterator, newiterator;

	List<Predicate> classPredicates = new ArrayList<Predicate>();
	List<Resource> joinPredObjNodes = new ArrayList<Resource>();

	String parsedString = "";
	String subjectString = "";
	String objectString = "";

	public R2RMLParser() {

	}

	/*
	 * method to return an sql string from a given Resource node in the Graph
	 */
	public String getSQLQuery(Graph myGraph, Resource subj) {
		// System.out.println(subj.toString());
		Iterator<Statement> tableit;
		String sqlString;

		// search for logicalTable declaration
		Set<Value> objects = util.getObjects(myGraph, subj, logicalTable,	(Resource) null);
		Resource object = (Resource) objects.toArray()[0];
		if (object instanceof BNode) {
			
			//look for tableName declaration
			String tableName =  getSQLTableName(myGraph, object);
			if (!tableName.isEmpty())
				return ("SELECT * FROM " +tableName);
			
			// search for sqlQuery declarations
			sqlString = getSQLQueryString(myGraph, object);
			if (!sqlString.isEmpty())
			return sqlString;
			

			// search for r2rmlview declaration
			tableit = myGraph.match(object, r2rmlView, null, (Resource) null);
			if (tableit.hasNext()) {
				Resource objectt = (Resource) tableit.next().getObject();
				
				//look for sqlquery declaration
				return getSQLQueryString(myGraph, objectt);
				
			}

			// search for basetableorview declaration
			tableit = myGraph.match(object, baseTableOrView, null,
					(Resource) null);
			if (tableit.hasNext()) {
				Resource objectt = (Resource) tableit.next().getObject();
				//seach table name in basetableview definition
				return ("SELECT * FROM " + getSQLTableName(myGraph, objectt));
			}
			
			
		} else {
			
			//logicalTable not a node, only sqlquery
			return getSQLQueryString(myGraph, object);
		}
		return "";
	}
	
	private String getSQLQueryString(Graph myGraph, Resource object) {
		
		// search for sqlQuery declarations
		Iterator<Statement> tableit = myGraph.match(object, sqlQuery, null, (Resource) null);
		if (tableit.hasNext()) {
			String sqlString = tableit.next().getObject().toString();
			// System.out.println(sqlString);
			return trim(sqlString);
		}
		return "";
	}
	
	private String getSQLTableName(Graph myGraph, Resource object){
		
		//look for tableName declaration
		Iterator<Statement> newiterator = myGraph.match(object, tableName, null, (Resource) null);
		if (newiterator.hasNext()) {
			String sqlString = newiterator.next().getObject().toString();
			// System.out.println(sqlString);
			return trim(sqlString);
		}
		return "";
	}

	public List<Predicate> getClassPredicates() {
		return classPredicates;
	}

	public Set<Resource> getPredicateObjects(Graph myGraph, Resource subj) {
		// process PREDICATEOBJECTs
		Set<Resource> predobjs = new HashSet<Resource>();
		Set<Value> objectss = util.getObjects(myGraph, subj, predicateObjectMap, (Resource) null);
		for (Value objectVal : objectss) {
			Resource object = (Resource) objectVal;
			predobjs.add(object);
		}
		return predobjs;
	}

	public NewLiteral getSubjectAtom(Graph myGraph, Resource subj)
			throws Exception {
		NewLiteral subjectAtom = null;

		// process SUBJECT
		Set<Value> objects = util.getObjects(myGraph, subj, subjectMap,	(Resource) null);
		Resource object = (Resource) objects.toArray()[0];

		// process template declaration
		iterator = myGraph.match(object, template, null, (Resource) null);
		if (iterator.hasNext()) {
			parsedString = iterator.next().getObject().toString();
			// System.out.println(parsedString);
			subjectString = trim(parsedString);
			// craete uri("...",var)
			subjectAtom = getURIFunction(subjectString);
		}

		// process column declaration
		iterator = myGraph.match(object, column, null, (Resource) null);
		if (iterator.hasNext()) {
			parsedString = iterator.next().getObject().toString();
			// System.out.println(parsedString);
			subjectString = trim(parsedString);
			subjectAtom = getURIFunction(subjectString);
		}
		
		// process constant declaration
		iterator = myGraph.match(object, constant, null, (Resource) null);
		if (iterator.hasNext()) {
			parsedString = iterator.next().getObject().toString();
			// System.out.println(parsedString);
			subjectString = trim(parsedString);
			subjectAtom = getURIFunction(subjectString);
		}
		
		// process termType declaration
		iterator = myGraph.match(object, termType, null, (Resource) null);
		if (iterator.hasNext()) {
			parsedString = iterator.next().getObject().toString();
			// System.out.println(parsedString);
			subjectAtom = getTermTypeAtom(parsedString, subjectString);
		}
		
		iterator = myGraph.match(object, inverseExpression, null,
				(Resource) null);
		if (iterator.hasNext()) {
			parsedString = iterator.next().getObject().toString();
			// System.out.println(parsedString);
		}
		
		// process class declaration
		iterator = myGraph.match(object, classUri, null, (Resource) null);
		while (iterator.hasNext()) {
			parsedString = iterator.next().getObject().toString();
			// System.out.println(parsedString);

			// create class(uri("...", var)) and add it to the body
			classPredicates.add(fac.getClassPredicate(parsedString));
		}

		if (subjectAtom == null)
			throw new Exception("Error in parsing the subjectMap in node "
					+ subj.stringValue());

		// System.out.println("subjectatom = " +subjectAtom.toString());
		return subjectAtom;

	}

	public List<Predicate> getBodyPredicates(Graph myGraph, Resource object)
			throws Exception {

		List<Predicate> bodyPredicates = new ArrayList<Predicate>();
		Predicate bodyPredicate = null;

		// process PREDICATE
		// look for the predicate
		iterator = myGraph.match(object, predicate, null, (Resource) null);
		while (iterator.hasNext()) {
			parsedString = iterator.next().getObject().toString();
			// System.out.println(parsedString);
			bodyPredicate = fac.getPredicate(parsedString, 2);
			bodyPredicates.add(bodyPredicate);
		}

		// process PREDICATEMAP
		// look for the predicateMap
		iterator = myGraph.match(object, predicateMap, null, (Resource) null);
		while (iterator.hasNext()) {
			Resource objectt = (Resource) (iterator.next().getObject());

			// process constant declaration
			Iterator<Statement> newiterator = myGraph.match(objectt, constant, null, (Resource) null);
			if (newiterator.hasNext()) {
				parsedString = newiterator.next().getObject().toString();
				// System.out.println(parsedString);
				bodyPredicate = fac.getPredicate(parsedString, 2);
				bodyPredicates.add(bodyPredicate);

			}
		}

		if (bodyPredicate == null)
			throw new Exception("Error in parsing the predicate");

		return bodyPredicates;
	}
	
	

	public NewLiteral getObjectAtom(Graph myGraph, Resource objectt)
			throws Exception {
		NewLiteral objectAtom = null;

		// process OBJECT
		// look for the object
		iterator = myGraph.match(objectt, this.object, null, (Resource) null);
		if (iterator.hasNext()) {
			parsedString = iterator.next().getObject().toString();
			// System.out.println(parsedString);
			objectAtom = fac.getURIConstant(OBDADataFactoryImpl.getIRI(parsedString));
		}

		// process OBJECTMAP
		iterator = myGraph.match(objectt, objectMap, null, (Resource) null);
		if (iterator.hasNext()) {
			Resource object = (Resource) (iterator.next().getObject());

			// look for column declaration
			newiterator = myGraph.match(object, column, null, (Resource) null);
			if (newiterator.hasNext()) {
				parsedString = newiterator.next().getObject().toString();
				// System.out.println(parsedString);
				objectAtom = fac.getVariable(trim(parsedString));
			}

			// look for constant declaration
			newiterator = myGraph
					.match(object, constant, null, (Resource) null);
			if (newiterator.hasNext()) {
				parsedString = newiterator.next().getObject().toString();
				// System.out.println(parsedString);
				objectAtom = fac.getValueConstant(trim(parsedString));
			}

			// look for template declaration
			newiterator = myGraph
					.match(object, template, null, (Resource) null);
			if (newiterator.hasNext()) {
				parsedString = newiterator.next().getObject().toString();

				// craete uri("...",var)
				objectString = trim(parsedString);
				objectAtom = getURIFunction(objectString);

			}
			// process termType declaration
			newiterator = myGraph
					.match(object, termType, null, (Resource) null);
			if (newiterator.hasNext()) {
				parsedString = newiterator.next().getObject().toString();
				// System.out.println(parsedString);
				objectAtom = getTermTypeAtom(parsedString, objectString);

			}

			
		}

		return objectAtom;
	}
	
	
	public List<Resource> getJoinNodes(Graph myGraph, Resource termMap)
	{
		List<Resource> joinPredObjNodes = new ArrayList<Resource>();
		// get predicate-object nodes
		Set<Resource> predicateObjectNodes = getPredicateObjects(myGraph, termMap);

		for (Resource predobj : predicateObjectNodes) {
			// for each predicate object map

			// process OBJECTMAP
			iterator = myGraph.match(predobj, objectMap, null, (Resource) null);
			if (iterator.hasNext()) {
				Resource objectt = (Resource) (iterator.next().getObject());
				
				// look for parentTriplesMap declaration
				newiterator = myGraph.match(objectt, parentTriplesMap, null, (Resource) null);
				if (newiterator.hasNext()) {
					// found a join condition, add the predicateobject node to the list
					joinPredObjNodes.add(predobj);
				}
			}
		}
		return joinPredObjNodes;
	}

	
	private Function getTermTypeAtom(String type, String string) {
		
		if (type.contentEquals(iri.stringValue())) {
			
			return getURIFunction(string);
			
		} else if (type.contentEquals(blankNode.stringValue())) {
			
			return getTypedFunction(string, 2);
			
		} else if (type.contentEquals(literal.stringValue())) {
			
			return getTypedFunction(string, 3);
		}
		return null;
	}

	
	private Function getURIFunction(String string) {
		
		return getTypedFunction(string, 1);
	}

	
	public Function getTypedFunction(String parsedString, int type) {

		List<NewLiteral> terms = new ArrayList<NewLiteral>();
		String string = (parsedString);
		if (!string.contains("{") && !string.startsWith("http://")) {
			string = "{" + string + "}";
			if (type == 2)
				string = "\"" + string + "\"";
		}
		while (string.contains("{")) {
			int begin = string.indexOf("{");
			int end = string.indexOf("}");
			String var = string.substring(begin + 1, end);
			terms.add(fac.getVariable(trim(var)));
			string = string.replace("{" + var + "}", "[]");
		}
		string = string.replace("[]", "{}");

		NewLiteral uriTemplate = null;
		Predicate pred = null;
		switch (type) {
		// URI or IRI
		case 1:
			uriTemplate = fac.getValueConstant(string);
			pred = fac.getUriTemplatePredicate(terms.size());
			break;
		// BNODE
		case 2:
			uriTemplate = fac.getBNodeConstant(string);
			pred = fac.getBNodeTemplatePredicate(terms.size());
			break;
		// LITERAL
		case 3:
			uriTemplate = fac.getValueConstant(string);
			pred = fac.getTypePredicate(COL_TYPE.LITERAL);
			break;
		}

		// the URI template is always on the first position in the term list
		terms.add(0, uriTemplate);
		return fac.getFunctionalTerm(pred, terms);

	}

	private String trim(String string) {
		
		while (string.startsWith("\"") && string.endsWith("\"")) {
			
			string = string.substring(1, string.length() - 1);
		}
		return string;
	}

	public Resource getReferencedTripleMap(Graph myGraph, Resource predobjNode) {
	
		// process OBJECTMAP
		iterator = myGraph.match(predobjNode, objectMap, null, (Resource) null);
		if (iterator.hasNext()) {
			Resource object = (Resource) (iterator.next().getObject());
			
			// look for parentTriplesMap declaration
			newiterator = myGraph.match(object, parentTriplesMap, null,	(Resource) null);
			if (newiterator.hasNext()) {
				return (Resource)newiterator.next().getObject();
			}
		}
		return null;
	}

	public String getChildColumn(Graph myGraph, Resource predobjNode) {
		
		// process OBJECTMAP
		iterator = myGraph.match(predobjNode, objectMap, null, (Resource) null);
		if (iterator.hasNext()) {
			Resource object = (Resource) (iterator.next().getObject());

			// look for joincondition declaration
			newiterator = myGraph.match(object, joinCondition, null, (Resource) null);
			if (newiterator.hasNext()) {
				Resource objectt = (Resource) (newiterator.next().getObject());
				
				// look for child declaration
				Iterator<Statement> newiterator2 = myGraph.match(objectt, child, null, (Resource) null);
				if (newiterator2.hasNext()) {
					return trim(newiterator2.next().getObject().stringValue());
				}
			}
		}
		return null;
	}

	public String getParentColumn(Graph myGraph, Resource predobjNode) {
		// process OBJECTMAP
		iterator = myGraph.match(predobjNode, objectMap, null, (Resource) null);
		if (iterator.hasNext()) {
			Resource object = (Resource) (iterator.next().getObject());
			
			// look for joincondition declaration
			newiterator = myGraph.match(object, joinCondition, null, (Resource) null);
			if (newiterator.hasNext()) {
				Resource objectt = (Resource) (newiterator.next().getObject());
							
				// look for parent declaration
				Iterator<Statement> newiterator2 = myGraph.match(objectt, parent, null, (Resource) null);
				if (newiterator2.hasNext()) {
					return trim(newiterator2.next().getObject().stringValue());
				}
			}
		}
		return null;
	}

}
