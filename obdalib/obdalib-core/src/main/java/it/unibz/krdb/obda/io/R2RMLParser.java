package it.unibz.krdb.obda.io;

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DataTypePredicate;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.DataTypePredicateImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

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

	private final String baseuri = "http://example.com/base/";
	
	public static final ValueFactory fact = new ValueFactoryImpl();
	public final URI TriplesMap = fact.createURI("http://www.w3.org/ns/r2rml#TriplesMap");
	
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
	String basePrefix = "";

	public R2RMLParser() {

	}
	
	/*
	 * method to get the Resource nodes (TripleMaps) from the given Graph
	 */
	public Set<Resource> getMappingNodes(Graph myGraph)
	{
		Set<Resource> resources = GraphUtil.getSubjects(myGraph, logicalTable, null);
		Set<Resource> nodes = new HashSet<Resource>();
		for (Resource subj : resources) {
			//add resource if it's a triplesMap declaration
			iterator = myGraph.match(subj, fact.createURI(OBDAVocabulary.RDF_TYPE), TriplesMap);
			if (iterator.hasNext()) {
				nodes.add(subj);
			}
		}
		return nodes;
	}
		

	/*
	 * method to return an sql string from a given Resource node in the Graph
	 */
	public String getSQLQuery(Graph myGraph, Resource subj) {
		// System.out.println(subj.toString());
		Iterator<Statement> tableit;
		String sqlString;

		// search for logicalTable declaration
		Set<Value> objects = GraphUtil.getObjects(myGraph, subj, logicalTable);
		if (objects.size() > 0){
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
			tableit = myGraph.match(object, r2rmlView, null);
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
		}
		return "";
	}
	
	private String getSQLQueryString(Graph myGraph, Resource object) {
		
		// search for sqlQuery declarations
		Iterator<Statement> tableit = myGraph.match(object, sqlQuery, null);
		if (tableit.hasNext()) {
			String sqlString = tableit.next().getObject().toString();
			// System.out.println(sqlString);
			sqlString = trim(sqlString).trim();
			if (sqlString.endsWith(";"))
				sqlString = sqlString.substring(0, sqlString.length()-1);
			return (sqlString);
		}
		return "";
	}
	
	private String getSQLTableName(Graph myGraph, Resource object){
		
		//look for tableName declaration
		Iterator<Statement> newiterator = myGraph.match(object, tableName, null);
		if (newiterator.hasNext()) {
			String sqlString = newiterator.next().getObject().toString();
			// System.out.println(sqlString);
			return trimTo1(sqlString);
		}
		return "";
	}

	public List<Predicate> getClassPredicates() {
		 List<Predicate> classes = new ArrayList<Predicate>();
		 for (Predicate p: classPredicates)
			 classes.add(p);
		 classPredicates.clear();
		 return classes;
	}

	public Set<Resource> getPredicateObjects(Graph myGraph, Resource subj) {
		// process PREDICATEOBJECTs
		Set<Resource> predobjs = new HashSet<Resource>();
		Set<Value> objectss = GraphUtil.getObjects(myGraph, subj, predicateObjectMap);
		for (Value objectVal : objectss) {
			Resource object = (Resource) objectVal;
			predobjs.add(object);
		}
		return predobjs;
	}
	
	public NewLiteral getSubjectAtom(Graph myGraph, Resource subj)
			throws Exception {
		return getSubjectAtom(myGraph, subj, "");
	}
	
	public NewLiteral getSubjectAtom(Graph myGraph, Resource subj, String joinCond)
			throws Exception {
		NewLiteral subjectAtom = null;

		// process SUBJECT
		Set<Value> objects = GraphUtil.getObjects(myGraph, subj, subjectMap);
		Resource object = (Resource) objects.toArray()[0];

		// process template declaration
		iterator = myGraph.match(object, template, null);
		if (iterator.hasNext()) {
			parsedString = iterator.next().getObject().toString();
			// System.out.println(parsedString);
			subjectString = trim(parsedString);
			// craete uri("...",var)
			subjectAtom = getURIFunction((subjectString), joinCond);
		}

		// process column declaration
		iterator = myGraph.match(object, column, null);
		if (iterator.hasNext()) {
			parsedString = iterator.next().getObject().toString();
			// System.out.println(parsedString);
			subjectString = trim(parsedString);
			subjectAtom = getURIFunction((subjectString), joinCond);
		}
		
		// process constant declaration
		iterator = myGraph.match(object, constant, null);
		if (iterator.hasNext()) {
			parsedString = iterator.next().getObject().toString();
			// System.out.println(parsedString);
			subjectString = trim(parsedString);
			subjectAtom = getURIFunction((subjectString), joinCond);
		}
		
		// process termType declaration
		iterator = myGraph.match(object, termType, null);
		if (iterator.hasNext()) {
			parsedString = iterator.next().getObject().toString();
			// System.out.println(parsedString);
			subjectAtom = getTermTypeAtom(parsedString, (subjectString));
		}
		
		// process class declaration
		iterator = myGraph.match(object, classUri, null);
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
		iterator = myGraph.match(object, predicate, null);
		while (iterator.hasNext()) {
			parsedString = iterator.next().getObject().toString();
			// System.out.println(parsedString);
			bodyPredicate = fac.getPredicate(parsedString, 2);
			bodyPredicates.add(bodyPredicate);
		}

		// process PREDICATEMAP
		// look for the predicateMap
		iterator = myGraph.match(object, predicateMap, null);
		while (iterator.hasNext()) {
			Resource objectt = (Resource) (iterator.next().getObject());

			// process constant declaration
			Iterator<Statement> newiterator = myGraph.match(objectt, constant, null);
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
		return getObjectAtom(myGraph, objectt, "");
	}
	
	public NewLiteral getObjectAtom(Graph myGraph, Resource objectt, String joinCond)
			throws Exception {
		NewLiteral objectAtom = null;

		// process OBJECT
		// look for the object
		iterator = myGraph.match(objectt, this.object, null);
		if (iterator.hasNext()) {
			parsedString = iterator.next().getObject().toString();
			// System.out.println(parsedString);
			//uriconstant
			if(parsedString.startsWith("http://"))
				objectAtom = fac.getURIConstant(parsedString);
			else
			{
				//valueconstant
				Predicate pred = fac.getUriTemplatePredicate(1);
				NewLiteral newlit = fac.getValueConstant(trim(parsedString));
				objectAtom = fac.getFunctionalTerm(pred, newlit);
			}
				
			
		}

		// process OBJECTMAP
		iterator = myGraph.match(objectt, objectMap, null);
		if (iterator.hasNext()) {
			Resource object = (Resource) (iterator.next().getObject());

			// look for column declaration
			newiterator = myGraph.match(object, column, null);
			if (newiterator.hasNext()) {
				parsedString = newiterator.next().getObject().toString();
				objectString = trim(parsedString);
				// System.out.println(parsedString);
				if (!joinCond.isEmpty())
					objectString = joinCond+(objectString);
				objectAtom = fac.getVariable(objectString);
			}
			

			// look for constant declaration
			newiterator = myGraph.match(object, constant, null);
			if (newiterator.hasNext()) {
				parsedString = newiterator.next().getObject().toString();
				// System.out.println(parsedString);
				objectString = trim(parsedString);
				if (objectString.contains("^^"))
					objectAtom = getExplicitTypedObject(objectString);
				else
					objectAtom = getConstantObject(objectString);
			}

			// look for template declaration
			newiterator = myGraph.match(object, template, null);
			if (newiterator.hasNext()) {
				parsedString = newiterator.next().getObject().toString();

				// craete uri("...",var)
				objectString = trimTo1(parsedString);
				objectAtom = getTypedFunction(trim(objectString), 1, joinCond);

			}
			// process termType declaration
			newiterator = myGraph.match(object, termType, null);
			if (newiterator.hasNext()) {
				parsedString = newiterator.next().getObject().toString();
				// System.out.println(parsedString);
				objectAtom = getTermTypeAtom(parsedString, (objectString));

			}
			
			// look for language declaration
			newiterator = myGraph.match(object, language, null);
			if (newiterator.hasNext()) {
				parsedString = newiterator.next().getObject().toString();
				// System.out.println(parsedString);
				NewLiteral lang = fac.getValueConstant(trim(parsedString.toLowerCase()));
				//create literal(object, lang) atom
				Predicate literal = OBDAVocabulary.RDFS_LITERAL_LANG;
				NewLiteral langAtom = fac.getFunctionalTerm(literal, objectAtom, lang);
				objectAtom = langAtom;
			}
			
			// look for datatype declaration
			newiterator = myGraph.match(object, datatype, null);
			if (newiterator.hasNext()) {
				parsedString = newiterator.next().getObject().toString();
				// System.out.println(parsedString);
				
				//create datatype(object) atom
				Predicate dtype =  new DataTypePredicateImpl(OBDADataFactoryImpl.getIRI(parsedString), COL_TYPE.OBJECT);
				NewLiteral dtAtom = fac.getFunctionalTerm(dtype, objectAtom);
				objectAtom = dtAtom;
			}
		}

		return objectAtom;
	}
	
	
	private NewLiteral getConstantObject(String objectString) {
		if (objectString.startsWith("http:"))
			return getURIFunction(objectString);
		else
		{	//literal
			Constant constt = fac.getValueConstant(objectString);
			Predicate pred = fac.getDataTypePredicateLiteral();
			return fac.getFunctionalTerm(pred, constt);
		
		}
	}

	private NewLiteral getExplicitTypedObject(String string) {
		
		NewLiteral typedObject = null;
		String[] strings = string.split("<");
		if (strings.length > 1) {
			String consts = strings[0];
			consts = consts.substring(0, consts.length()-2);
			consts = trim(consts);
			String type = strings[1];
			if (type.endsWith(">"))
				type = type.substring(0, type.length() - 1);

			DataTypePredicate predicate = new DataTypePredicateImpl(OBDADataFactoryImpl.getIRI(type), COL_TYPE.OBJECT);
					//fac.getDataPropertyPredicate(OBDADataFactoryImpl.getIRI(type));
			NewLiteral constant = fac.getValueConstant(consts);
			typedObject = fac.getFunctionalTerm(predicate, constant);
		}
		return typedObject;
	}

	public List<Resource> getJoinNodes(Graph myGraph, Resource termMap)
	{
		List<Resource> joinPredObjNodes = new ArrayList<Resource>();
		// get predicate-object nodes
		Set<Resource> predicateObjectNodes = getPredicateObjects(myGraph, termMap);

		for (Resource predobj : predicateObjectNodes) {
			// for each predicate object map

			// process OBJECTMAP
			iterator = myGraph.match(predobj, objectMap, null);
			if (iterator.hasNext()) {
				Resource objectt = (Resource) (iterator.next().getObject());
				
				// look for parentTriplesMap declaration
				newiterator = myGraph.match(objectt, parentTriplesMap, null);
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
			
			return getTypedFunction(trim(string), 3);
		}
		return null;
	}

	private Function getURIFunction(String string, String joinCond) {
		return getTypedFunction(string, 1, joinCond);
	}
	
	private Function getURIFunction(String string) {
		
		return getTypedFunction(string, 1);
	}

	public Function getTypedFunction(String parsedString, int type) {
		return getTypedFunction(parsedString, type, "");
	}
	
	public Function getTypedFunction(String parsedString, int type, String joinCond) {

		List<NewLiteral> terms = new ArrayList<NewLiteral>();
		String string = (parsedString);
		if (!string.contains("{"))
			if (!string.startsWith("http://")) 
			{	string = baseuri + "{" + string + "}";
				if (type == 2)
					string = "\"" + string + "\"";
			}
			else
			{
				type = 0;
			}
		if (type == 1 && !string.startsWith("http://"))
			string = baseuri + string;
		
		string = string.replace("\\{", "[");
		string = string.replace("\\}", "]");
		
		while (string.contains("{") ) {
			int end = string.indexOf("}");
			int begin = string.lastIndexOf("{", end);
			
			String var = trim(string.substring(begin + 1, end));
			
			//trim for making variable
			terms.add(fac.getVariable(joinCond+(var)));
			
			
			string = string.replace("{\"" + var + "\"}", "[]");
			string = string.replace("{" + var + "}", "[]");
		}
		string = string.replace("[", "{");
		string = string.replace("]", "}");
	

		NewLiteral uriTemplate = null;
		Predicate pred = null;
		switch (type) {
		//constant uri
		case 0:
			uriTemplate = fac.getURIConstant(string);
			pred = fac.getUriTemplatePredicate(terms.size());
			break;
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
			pred = OBDAVocabulary.RDFS_LITERAL_LANG;//lang?
			terms.add(OBDAVocabulary.NULL);
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
	
	private String trimTo1(String string) {
		
		while (string.startsWith("\"\"") && string.endsWith("\"\"")) {
			
			string = string.substring(1, string.length() - 1);
		}
		return string;
	}

	public Resource getReferencedTripleMap(Graph myGraph, Resource predobjNode) {
	
		// process OBJECTMAP
		iterator = myGraph.match(predobjNode, objectMap, null);
		if (iterator.hasNext()) {
			Resource object = (Resource) (iterator.next().getObject());
			
			// look for parentTriplesMap declaration
			newiterator = myGraph.match(object, parentTriplesMap, null);
			if (newiterator.hasNext()) {
				return (Resource)newiterator.next().getObject();
			}
		}
		return null;
	}

	public String getChildColumn(Graph myGraph, Resource predobjNode) {
		
		// process OBJECTMAP
		iterator = myGraph.match(predobjNode, objectMap, null);
		if (iterator.hasNext()) {
			Resource object = (Resource) (iterator.next().getObject());

			// look for joincondition declaration
			newiterator = myGraph.match(object, joinCondition, null);
			if (newiterator.hasNext()) {
				Resource objectt = (Resource) (newiterator.next().getObject());
				
				// look for child declaration
				Iterator<Statement> newiterator2 = myGraph.match(objectt, child, null);
				if (newiterator2.hasNext()) {
					return trimTo1(newiterator2.next().getObject().stringValue());
				}
			}
		}
		return null;
	}

	public String getParentColumn(Graph myGraph, Resource predobjNode) {
		// process OBJECTMAP
		iterator = myGraph.match(predobjNode, objectMap, null);
		if (iterator.hasNext()) {
			Resource object = (Resource) (iterator.next().getObject());
			
			// look for joincondition declaration
			newiterator = myGraph.match(object, joinCondition, null);
			if (newiterator.hasNext()) {
				Resource objectt = (Resource) (newiterator.next().getObject());
							
				// look for parent declaration
				Iterator<Statement> newiterator2 = myGraph.match(objectt, parent, null);
				if (newiterator2.hasNext()) {
					return trimTo1(newiterator2.next().getObject().stringValue());
				}
			}
		}
		return null;
	}


	

}
