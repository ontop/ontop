package it.unibz.krdb.obda.sesame.r2rml;

/*
 * #%L
 * ontop-obdalib-sesame
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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

/**
 * @author timea bagosi
 * The R2RML parser class that breaks down the responsibility of parsing by case
 */
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DataTypePredicate;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Term;
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
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.GraphUtil;


public class R2RMLParser {

	private ValueFactory fact;
	private OBDADataFactory fac;

	Iterator<Statement> iterator, newiterator;

	List<Predicate> classPredicates; 
	List<Resource> joinPredObjNodes; 

	String parsedString = "";
	String subjectString = "";
	String objectString = "";

	/**
	 * empty constructor 
	 */
	public R2RMLParser() {
		classPredicates = new ArrayList<Predicate>();
		joinPredObjNodes = new ArrayList<Resource>();
		fact = new ValueFactoryImpl();
		fac = OBDADataFactoryImpl.getInstance();
	}
	
	/**
	 * method to get the Resource nodes (TripleMaps) from the given Graph
	 * @param myGraph - the Graph to process
	 * @return Set<Resource> - the Resource nodes of triples map (root node)
	 */
	public Set<Resource> getMappingNodes(Graph myGraph)
	{
		Set<Resource> resources = GraphUtil.getSubjects(myGraph, R2RMLVocabulary.logicalTable, null);
		Set<Resource> nodes = new HashSet<Resource>();
		for (Resource subj : resources) {
			//add resource if it's a triplesMap declaration
			iterator = myGraph.match(subj, fact.createURI(OBDAVocabulary.RDF_TYPE), R2RMLVocabulary.TriplesMap);
			if (iterator.hasNext()) {
				nodes.add(subj);
			}
		}
		return nodes;
	}
		

	/**
	 * Method to return an sql string from a given Resource node in the Graph
	 * @param myGraph - the Graph of mappings
	 * @param subj - the Resource node to process
	 * @return String - sql query parsed
	 */
	public String getSQLQuery(Graph myGraph, Resource subj) {
		// System.out.println(subj.toString());
		Iterator<Statement> tableit;
		String sqlString;

		// search for logicalTable declaration
		Set<Value> objects = GraphUtil.getObjects(myGraph, subj, R2RMLVocabulary.logicalTable);
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
			tableit = myGraph.match(object, R2RMLVocabulary.r2rmlView, null);
			if (tableit.hasNext()) {
				Resource objectt = (Resource) tableit.next().getObject();
				
				//look for sqlquery declaration
				return getSQLQueryString(myGraph, objectt);
				
			}

			// search for basetableorview declaration
			tableit = myGraph.match(object, R2RMLVocabulary.baseTableOrView, null,
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
	
	/**
	 * Get sql query string from sqlquery definition
	 * @param myGraph - the Graph of mappings
	 * @param object - the Resource node containing the actual sql query 
	 * @return String of sql query
	 */
	private String getSQLQueryString(Graph myGraph, Resource object) {
		
		// search for sqlQuery declarations
		Iterator<Statement> tableit = myGraph.match(object, R2RMLVocabulary.sqlQuery, null);
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
	
	/**
	 * Get sql table name from node with tableName definition
	 * @param myGraph - the Graph of mappings
	 * @param object - the Resource node containing the table name
	 * @return String of table name
	 */
	private String getSQLTableName(Graph myGraph, Resource object){
		
		//look for tableName declaration
		Iterator<Statement> newiterator = myGraph.match(object, R2RMLVocabulary.tableName, null);
		if (newiterator.hasNext()) {
			String sqlString = newiterator.next().getObject().toString();
			// System.out.println(sqlString);
			return trimTo1(sqlString);
		}
		return "";
	}

	/**
	 * get class predicates after processing one triples map, 
	 * called by manager iteratively 
	 * to introduce class predicates
	 * @return List of class predicates
	 */
	public List<Predicate> getClassPredicates() {
		 List<Predicate> classes = new ArrayList<Predicate>();
		 for (Predicate p: classPredicates)
			 classes.add(p);
		 classPredicates.clear();
		 return classes;
	}

	/**
	 * List to process predicate objects
	 * @param myGraph - the Graph of mappings
	 * @param subj - the Resource node to process
	 * @return set of Resource nodes that are predicate objects 
	 */
	public Set<Resource> getPredicateObjects(Graph myGraph, Resource subj) {
		// process PREDICATEOBJECTs
		Set<Resource> predobjs = new HashSet<Resource>();
		Set<Value> objectss = GraphUtil.getObjects(myGraph, subj, R2RMLVocabulary.predicateObjectMap);
		for (Value objectVal : objectss) {
			Resource object = (Resource) objectVal;
			predobjs.add(object);
		}
		return predobjs;
	}
	
	/**
	 * get the subject atom as a term
	 * @param myGraph - the Graph of mappings
	 * @param subj - the Resource node to process
	 * @return the term
	 * @throws Exception
	 */
	public Term getSubjectAtom(Graph myGraph, Resource subj)
			throws Exception {
		return getSubjectAtom(myGraph, subj, "");
	}
	
	/**
	 * get subject atom of join condition mapping
	 * @param myGraph - the Graph of mappings
	 * @param subj - the Resource node to process
	 * @param joinCond - CHILD_ or PARENT_ prefix
	 * @return the subject Term
	 * @throws Exception
	 */
	public Term getSubjectAtom(Graph myGraph, Resource subj, String joinCond)
			throws Exception {
		Term subjectAtom = null;

		// process SUBJECT
		Set<Value> objects = GraphUtil.getObjects(myGraph, subj, R2RMLVocabulary.subjectMap);
		Resource object = (Resource) objects.toArray()[0];

		// process template declaration
		iterator = myGraph.match(object, R2RMLVocabulary.template, null);
		if (iterator.hasNext()) {
			parsedString = iterator.next().getObject().toString();
			// System.out.println(parsedString);
			subjectString = trim(parsedString);
			// craete uri("...",var)
			subjectAtom = getURIFunction((subjectString), joinCond);
		}

		// process column declaration
		iterator = myGraph.match(object, R2RMLVocabulary.column, null);
		if (iterator.hasNext()) {
			parsedString = iterator.next().getObject().toString();
			// System.out.println(parsedString);
			subjectString = trim(parsedString);
			subjectAtom = getURIFunction((subjectString), joinCond);
		}
		
		// process constant declaration
		iterator = myGraph.match(object, R2RMLVocabulary.constant, null);
		if (iterator.hasNext()) {
			parsedString = iterator.next().getObject().toString();
			// System.out.println(parsedString);
			subjectString = trim(parsedString);
			subjectAtom = getURIFunction((subjectString), joinCond);
		}
		
		// process termType declaration
		iterator = myGraph.match(object, R2RMLVocabulary.termType, null);
		if (iterator.hasNext()) {
			parsedString = iterator.next().getObject().toString();
			// System.out.println(parsedString);
			subjectAtom = getTermTypeAtom(parsedString, (subjectString));
		}
		
		// process class declaration
		iterator = myGraph.match(object, R2RMLVocabulary.classUri, null);
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

	/**
	 * Get the predicates present in the body
	 * @param myGraph - the Graph of mappings
	 * @param object - the Resource node to process
	 * @return the list of predicates
	 * @throws Exception
	 */
	public List<Predicate> getBodyPredicates(Graph myGraph, Resource object)
			throws Exception {

		List<Predicate> bodyPredicates = new ArrayList<Predicate>();
		Predicate bodyPredicate = null;

		// process PREDICATE
		// look for the predicate
		iterator = myGraph.match(object, R2RMLVocabulary.predicate, null);
		while (iterator.hasNext()) {
			parsedString = iterator.next().getObject().toString();
			// System.out.println(parsedString);
			bodyPredicate = fac.getPredicate(parsedString, 2);
			bodyPredicates.add(bodyPredicate);
		}

//		if (bodyPredicate == null)
//			throw new Exception("Error in parsing the predicate");

		return bodyPredicates;
	}
	
	/**
	 * get the uri predicates in the body
	 * @param myGraph - the Graph of mappings
	 * @param object - the Resource node to process
	 * @return list of function, the predicate atoms
	 * @throws Exception
	 */
	public List<Function> getBodyURIPredicates(Graph myGraph, Resource object)
			throws Exception {
		List<Function> predicateAtoms = new ArrayList<Function>();
		Function predicateAtom;
		String predicateString = "";

		// process PREDICATEMAP
		// look for the predicateMap
		iterator = myGraph.match(object, R2RMLVocabulary.predicateMap, null);
		while (iterator.hasNext()) {
			Resource objectt = (Resource) (iterator.next().getObject());

			// process template declaration
			iterator = myGraph.match(objectt, R2RMLVocabulary.template, null);
			if (iterator.hasNext()) {
				parsedString = iterator.next().getObject().toString();
				// System.out.println(parsedString);
				predicateString = trim(parsedString);
				// craete uri("...",var)
				predicateAtom = getURIFunction((predicateString));
				predicateAtoms.add(predicateAtom);
			}

			// process column declaration
			iterator = myGraph.match(objectt, R2RMLVocabulary.column, null);
			if (iterator.hasNext()) {
				parsedString = iterator.next().getObject().toString();
				// System.out.println(parsedString);
				predicateString = trim(parsedString);
				predicateAtom = getURIFunction((predicateString));
				predicateAtoms.add(predicateAtom);
			}
			
			// process constant declaration
			iterator = myGraph.match(objectt, R2RMLVocabulary.constant, null);
			if (iterator.hasNext()) {
				parsedString = iterator.next().getObject().toString();
				// System.out.println(parsedString);
				predicateString = trim(parsedString);
				// craete uri("...",var)
				predicateAtom = getURIFunction((predicateString));
				predicateAtoms.add(predicateAtom);
			}
		}
		return predicateAtoms;

	}
	
	/**
	 * get the object atom
	 * @param myGraph - the graph of mappings
	 * @param objectt - the Resource node to process
	 * @return the object term
	 * @throws Exception
	 */
	public Term getObjectAtom(Graph myGraph, Resource objectt)
			throws Exception {
		return getObjectAtom(myGraph, objectt, "");
	}
	
	/**
	 * get the object atom of a join condition triples map
	 * @param myGraph - the graph of mappings
	 * @param objectt - the Resource node to process
	 * @param joinCond - the string CHILD_ or PARENT_
	 * @return the object atom as a term
	 * @throws Exception
	 */
	public Term getObjectAtom(Graph myGraph, Resource objectt, String joinCond)
			throws Exception {
		Term objectAtom = null;

		// process OBJECT
		// look for the object
		iterator = myGraph.match(objectt, R2RMLVocabulary.object, null);
		if (iterator.hasNext()) {
			parsedString = iterator.next().getObject().toString();
			// System.out.println(parsedString);
			//uriconstant
				//valueconstant
				Predicate pred = fac.getUriTemplatePredicate(1);
				Term newlit = fac.getConstantLiteral(trim(parsedString));
				objectAtom = fac.getFunction(pred, newlit);
		}

		// process OBJECTMAP
		iterator = myGraph.match(objectt, R2RMLVocabulary.objectMap, null);
		if (iterator.hasNext()) {
			Resource object = (Resource) (iterator.next().getObject());

			// look for column declaration
			newiterator = myGraph.match(object, R2RMLVocabulary.column, null);
			if (newiterator.hasNext()) {
				parsedString = newiterator.next().getObject().toString();
				objectString = trim(parsedString);
				// System.out.println(parsedString);
				if (!joinCond.isEmpty())
					objectString = joinCond+(objectString);
				objectAtom = fac.getVariable(objectString);
			}
			

			// look for constant declaration
			newiterator = myGraph.match(object, R2RMLVocabulary.constant, null);
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
			newiterator = myGraph.match(object, R2RMLVocabulary.template, null);
			if (newiterator.hasNext()) {
				parsedString = newiterator.next().getObject().toString();

				// craete uri("...",var)
				objectString = trimTo1(parsedString);
				objectAtom = getTypedFunction(trim(objectString), 1, joinCond);

			}
			// process termType declaration
			newiterator = myGraph.match(object, R2RMLVocabulary.termType, null);
			if (newiterator.hasNext()) {
				parsedString = newiterator.next().getObject().toString();
				// System.out.println(parsedString);
				objectAtom = getTermTypeAtom(parsedString, (objectString));

			}
			
			// look for language declaration
			newiterator = myGraph.match(object, R2RMLVocabulary.language, null);
			if (newiterator.hasNext()) {
				parsedString = newiterator.next().getObject().toString();
				// System.out.println(parsedString);
				Term lang = fac.getConstantLiteral(trim(parsedString.toLowerCase()));
				//create literal(object, lang) atom
				Predicate literal = OBDAVocabulary.RDFS_LITERAL_LANG;
				Term langAtom = fac.getFunction(literal, objectAtom, lang);
				objectAtom = langAtom;
			}
			
			// look for datatype declaration
			newiterator = myGraph.match(object, R2RMLVocabulary.datatype, null);
			if (newiterator.hasNext()) {
				parsedString = newiterator.next().getObject().toString();
				// System.out.println(parsedString);
				
				//create datatype(object) atom
				Predicate dtype =  new DataTypePredicateImpl(parsedString, COL_TYPE.OBJECT);
				Term dtAtom = fac.getFunction(dtype, objectAtom);
				objectAtom = dtAtom;
			}
		}

		return objectAtom;
	}
	
	
	/**
	 * get constant object of an object
	 * @param objectString - the string of object
	 * @return the object term
	 */
	private Term getConstantObject(String objectString) {
		if (objectString.startsWith("http:"))
			return getURIFunction(objectString);
		else
		{	//literal
			Constant constt = fac.getConstantLiteral(objectString);
			Predicate pred = fac.getDataTypePredicateLiteral();
			return fac.getFunction(pred, constt);
		
		}
	}

	/**
	 * get a term object from a string
	 * @param string - representing the object
	 * @return the Term explicitly typed 
	 */
	private Term getExplicitTypedObject(String string) {
		
		Term typedObject = null;
		String[] strings = string.split("<");
		if (strings.length > 1) {
			String consts = strings[0];
			consts = consts.substring(0, consts.length()-2);
			consts = trim(consts);
			String type = strings[1];
			if (type.endsWith(">"))
				type = type.substring(0, type.length() - 1);

			DataTypePredicate predicate = new DataTypePredicateImpl(type, COL_TYPE.OBJECT);
					//fac.getDataPropertyPredicate(OBDADataFactoryImpl.getIRI(type));
			Term constant = fac.getConstantLiteral(consts);
			typedObject = fac.getFunction(predicate, constant);
		}
		return typedObject;
	}

	/**
	 * Get the list of triples maps that contain joins
	 * @param myGraph - the graph of mappings
	 * @param termMap - the Resource node of term maps
	 * @return the lis of Resource nodes containing joins
	 */
	public List<Resource> getJoinNodes(Graph myGraph, Resource termMap)
	{
		List<Resource> joinPredObjNodes = new ArrayList<Resource>();
		// get predicate-object nodes
		Set<Resource> predicateObjectNodes = getPredicateObjects(myGraph, termMap);

		for (Resource predobj : predicateObjectNodes) {
			// for each predicate object map

			// process OBJECTMAP
			iterator = myGraph.match(predobj, R2RMLVocabulary.objectMap, null);
			if (iterator.hasNext()) {
				Resource objectt = (Resource) (iterator.next().getObject());
				
				// look for parentTriplesMap declaration
				newiterator = myGraph.match(objectt, R2RMLVocabulary.parentTriplesMap, null);
				if (newiterator.hasNext()) {
					// found a join condition, add the predicateobject node to the list
					joinPredObjNodes.add(predobj);
				}
			}
		}
		return joinPredObjNodes;
	}

	/**
	 * get a typed atom of a specific type
	 * @param type - iri, blanknode or literal
	 * @param string - the atom as string
	 * @return the contructed Function atom
	 */
	private Function getTermTypeAtom(String type, String string) {
		
		if (type.contentEquals(R2RMLVocabulary.iri.stringValue())) {
			
			return getURIFunction(string);
			
		} else if (type.contentEquals(R2RMLVocabulary.blankNode.stringValue())) {
			
			return getTypedFunction(string, 2);
			
		} else if (type.contentEquals(R2RMLVocabulary.literal.stringValue())) {
			
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
	
	/**
	 * get a typed atom 
	 * @param parsedString - the content of atom
	 * @param type - 0=constant uri, 1=uri or iri, 2=bnode, 3=literal
	 * @param joinCond - CHILD_ or PARENT_ prefix for variables
	 * @return the constructed Function atom
	 */
	public Function getTypedFunction(String parsedString, int type, String joinCond) {

		List<Term> terms = new ArrayList<Term>();
		String string = (parsedString);
		if (!string.contains("{"))
			if (type<3)
			if(!string.startsWith("http://")) 
			{	string = R2RMLVocabulary.baseuri + "{" + string + "}";
				if (type == 2)
					string = "\"" + string + "\"";
			}
			else
			{
				type = 0;
			}
		if (type == 1 && !string.startsWith("http://"))
			string = R2RMLVocabulary.baseuri + string;
		
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
	

		Term uriTemplate = null;
		Predicate pred = null;
		switch (type) {
		//constant uri
		case 0:
			uriTemplate = fac.getConstantLiteral(string);
			pred = fac.getUriTemplatePredicate(terms.size());
			break;
		// URI or IRI
		case 1:
			uriTemplate = fac.getConstantLiteral(string);
			pred = fac.getUriTemplatePredicate(terms.size());
			break;
		// BNODE
		case 2:
			uriTemplate = fac.getConstantBNode(string);
			pred = fac.getBNodeTemplatePredicate(terms.size());
			break;
		// LITERAL
		case 3:
			uriTemplate = fac.getVariable(string);
			pred = OBDAVocabulary.RDFS_LITERAL_LANG;//lang?
			terms.add(OBDAVocabulary.NULL);
			break;
		}

		// the URI template is always on the first position in the term list
		terms.add(0, uriTemplate);
		return fac.getFunction(pred, terms);

	}

	/**
	 * method that trims a string of all its double apostrophes
	 * from beginning and end
	 * @param string - to be trimmed
	 * @return the string without any quotes
	 */
	private String trim(String string) {
		
		while (string.startsWith("\"") && string.endsWith("\"")) {
			
			string = string.substring(1, string.length() - 1);
		}
		return string;
	}
	
	/**
	 * method to trim a string of its leading or trailing quotes
	 * but one
	 * @param string - to be trimmed
	 * @return the string left with one leading and trailing quote
	 */
	private String trimTo1(String string) {
		
		while (string.startsWith("\"\"") && string.endsWith("\"\"")) {
			
			string = string.substring(1, string.length() - 1);
		}
		return string;
	}

	/**
	 * method to find the triplesmap node referenced in a parent join condition
	 * @param myGraph - the graph of mappings
	 * @param predobjNode - the pred obj node containing the join condition
	 * @return the Resource node refferred to in the condition
	 */
	public Resource getReferencedTripleMap(Graph myGraph, Resource predobjNode) {
	
		// process OBJECTMAP
		iterator = myGraph.match(predobjNode, R2RMLVocabulary.objectMap, null);
		if (iterator.hasNext()) {
			Resource object = (Resource) (iterator.next().getObject());
			
			// look for parentTriplesMap declaration
			newiterator = myGraph.match(object, R2RMLVocabulary.parentTriplesMap, null);
			if (newiterator.hasNext()) {
				return (Resource)newiterator.next().getObject();
			}
		}
		return null;
	}

	/**
	 * method to get the child column in a join condition
	 * @param myGraph - the graph of mappings
	 * @param predobjNode - the pred obj node containing the join condition
	 * @return the child column condition as a string
	 */
	public String getChildColumn(Graph myGraph, Resource predobjNode) {
		
		// process OBJECTMAP
		iterator = myGraph.match(predobjNode, R2RMLVocabulary.objectMap, null);
		if (iterator.hasNext()) {
			Resource object = (Resource) (iterator.next().getObject());

			// look for joincondition declaration
			newiterator = myGraph.match(object, R2RMLVocabulary.joinCondition, null);
			if (newiterator.hasNext()) {
				Resource objectt = (Resource) (newiterator.next().getObject());
				
				// look for child declaration
				Iterator<Statement> newiterator2 = myGraph.match(objectt, R2RMLVocabulary.child, null);
				if (newiterator2.hasNext()) {
					return trimTo1(newiterator2.next().getObject().stringValue());
				}
			}
		}
		return null;
	}

	/**
	 * method to get the parent column in a join condition
	 * @param myGraph - the graph of mappings
	 * @param predobjNode - the pred obj node containing the join condition
	 * @return the parent column condition as a string
	 */
	public String getParentColumn(Graph myGraph, Resource predobjNode) {
		// process OBJECTMAP
		iterator = myGraph.match(predobjNode, R2RMLVocabulary.objectMap, null);
		if (iterator.hasNext()) {
			Resource object = (Resource) (iterator.next().getObject());
			
			// look for joincondition declaration
			newiterator = myGraph.match(object, R2RMLVocabulary.joinCondition, null);
			if (newiterator.hasNext()) {
				Resource objectt = (Resource) (newiterator.next().getObject());
							
				// look for parent declaration
				Iterator<Statement> newiterator2 = myGraph.match(objectt, R2RMLVocabulary.parent, null);
				if (newiterator2.hasNext()) {
					return trimTo1(newiterator2.next().getObject().stringValue());
				}
			}
		}
		return null;
	}


	

}
