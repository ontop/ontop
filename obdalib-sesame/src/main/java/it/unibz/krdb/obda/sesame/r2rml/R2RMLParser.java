package it.unibz.krdb.obda.sesame.r2rml;

/*
 * #%L
 * ontop-obdalib-sesame
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
import it.unibz.krdb.obda.sesame.r2rml.R2RMLVocabulary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import eu.optique.api.mapping.ObjectMap;
import eu.optique.api.mapping.PredicateMap;
import eu.optique.api.mapping.PredicateObjectMap;
import eu.optique.api.mapping.R2RMLMappingManager;
import eu.optique.api.mapping.R2RMLMappingManagerFactory;
import eu.optique.api.mapping.SubjectMap;
import eu.optique.api.mapping.Template;
import eu.optique.api.mapping.TriplesMap;
import eu.optique.api.mapping.impl.InvalidR2RMLMappingException;
import eu.optique.api.mapping.impl.SubjectMapImpl;


public class R2RMLParser {

	private ValueFactory fact;
	private OBDADataFactory fac;

	List<Predicate> classPredicates; 
	List<Resource> joinPredObjNodes; 

	String parsedString = "";
	String subjectString = "";
	String objectString = "";
	R2RMLMappingManager mapManager;

	/**
	 * empty constructor 
	 */
	public R2RMLParser() {
		mapManager = R2RMLMappingManagerFactory.getSesameMappingManager();
		classPredicates = new ArrayList<Predicate>();
		joinPredObjNodes = new ArrayList<Resource>();
		fact = new ValueFactoryImpl();
		fac = OBDADataFactoryImpl.getInstance();
	}

	/**
	 * method to get the TriplesMaps from the given Model
	 * @param myModel - the Model to process
	 * @return Collection<TriplesMap> - the collection of mappings
	 */
	public Collection <TriplesMap> getMappingNodes(Model myModel)
	{
		Collection <TriplesMap> coll = null;
		try {
			coll = mapManager.importMappings(myModel);
		} catch (InvalidR2RMLMappingException e) {
			e.printStackTrace();
		}		
		return coll;
	}

	public String getSQLQuery(TriplesMap tm) {
		return tm.getLogicalTable().getSQLQuery();
	}

	public List<Predicate> getClassPredicates() {
		List<Predicate> classes = new ArrayList<Predicate>();
		for (Predicate p: classPredicates)
			classes.add(p);
		classPredicates.clear();
		return classes;
	}

	public Set<Resource> getPredicateObjects(TriplesMap tm) {
		Set<Resource> predobjs = new HashSet<Resource>();
		for (PredicateObjectMap pobj : tm.getPredicateObjectMaps()) {
			for (PredicateMap pm : pobj.getPredicateMaps()) {
				Resource r = (Resource)pm.getResource(Object.class);	
				predobjs.add(r);
			}
		}
		return predobjs;
	}

	public Term getSubjectAtom(TriplesMap tm)
			throws Exception {
		return getSubjectAtom(tm, "");
	}

	public Term getSubjectAtom(TriplesMap tm, String joinCond)
			throws Exception {
		Term subjectAtom = null;
		String subj = "";

		// SUBJECT
		SubjectMap sMap = tm.getSubjectMap();
		SubjectMapImpl sm = (SubjectMapImpl) sMap;
		// process template declaration
		subj = sMap.getTemplateString();
		if (subj != null) {
			// craete uri("...",var)
			subjectAtom = getURIFunction((subj), joinCond);
		}

		// process column declaration
		subj = sMap.getColumn();
		if (subj != null) {
			// craete uri("...",var)
			subjectAtom = getURIFunction((subj), joinCond);
		}

		// process constant declaration
		subj = sMap.getConstant();
		if (subj != null) {
			// craete uri("...",var)
			subjectAtom = getURIFunction((subj), joinCond);
		}

		// process termType declaration
		//		subj = sMap.getTermMapType().toString();
		//		sMap.getTermType(Object.class);
		//		if (subj != null) {
		//			
		//			
		//		}

		// process class declaration
		List<Object> classes = sMap.getClasses(Object.class);
		for (Object o : classes)
		{	
			classPredicates.add(fac.getClassPredicate(o.toString()));
		}


		if (subjectAtom == null)
			throw new Exception("Error in parsing the subjectMap in node "
					+ tm.toString());

		return subjectAtom;

	}

	public List<Predicate> getBodyPredicates(PredicateObjectMap pom)
			throws Exception {
		List<Predicate> bodyPredicates = new ArrayList<Predicate>();
		Predicate bodyPredicate = null;

		// process PREDICATEs
		for (PredicateMap pm : pom.getPredicateMaps()) {
			bodyPredicate = fac.getPredicate(pm.getConstant(), 2);
			bodyPredicates.add(bodyPredicate);
		}
		return bodyPredicates;
	}

	public List<Function> getBodyURIPredicates(PredicateObjectMap pom)
			throws Exception {
		List<Function> predicateAtoms = new ArrayList<Function>();
		Function predicateAtom;

		// process PREDICATEMAP
		for (PredicateMap pm : pom.getPredicateMaps()) {
			Template t = pm.getTemplate();
			if(t != null) 
			{
				// craete uri("...",var)
				predicateAtom = getURIFunction(t.toString());
				predicateAtoms.add(predicateAtom);
			}

			// process column declaration
			String c = pm.getColumn();
			if (c != null) {
				predicateAtom = getURIFunction(c);
				predicateAtoms.add(predicateAtom);
			}
		}
		return predicateAtoms;

	}

	public Term getObjectAtom(PredicateObjectMap pom)
			throws Exception {
		return getObjectAtom(pom, "");
	}

	public Term getObjectAtom(PredicateObjectMap pom, String joinCond)
			throws Exception {
		Term objectAtom = null;
		if (pom.getObjectMaps().isEmpty()) {
			return null;
		}
		ObjectMap om = pom.getObjectMap(0);
		String obj = om.getConstant();
		if (obj != null) {
			Predicate pred = fac.getUriTemplatePredicate(1);
			Term newlit = fac.getConstantLiteral(obj);
			objectAtom = fac.getFunction(pred, newlit);
		}

		String col = om.getColumn();
		if (col != null) {
			if (!joinCond.isEmpty())
				col = joinCond + col;
			objectAtom = fac.getVariable(col);
		}

		Template t = om.getTemplate();
		if (t != null) {
			objectAtom = getTypedFunction(t.toString(), 1, joinCond);
		}

		String lan = om.getLanguageTag(); 
		if (lan != null) {
			Term lang = fac.getConstantLiteral(lan.toLowerCase());
			Predicate literal = OBDAVocabulary.RDFS_LITERAL_LANG;
			Term langAtom = fac.getFunction(literal, objectAtom, lang);
			objectAtom = langAtom;
		}
		Object type = om.getDatatype(Object.class);
		if (type != null)
		{
			Predicate dtype =  new DataTypePredicateImpl(type.toString(), COL_TYPE.OBJECT);
			Term dtAtom = fac.getFunction(dtype, objectAtom);
			objectAtom = dtAtom;
		}

		return objectAtom;
	}


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
			Term constant = fac.getConstantLiteral(consts);
			typedObject = fac.getFunction(predicate, constant);
		}
		return typedObject;
	}

	public List<Resource> getJoinNodes(TriplesMap tm)
	{
		List<Resource> joinPredObjNodes = new ArrayList<Resource>();
		// get predicate-object nodes
		Set<Resource> predicateObjectNodes = getPredicateObjects(tm);
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
	 * @param myModel - the Model of mappings
	 * @param predobjNode - the pred obj node containing the join condition
	 * @return the Resource node refferred to in the condition
	 */
	public Resource getReferencedTripleMap(Model myModel, Resource predobjNode) {

		// process OBJECTMAP
		Model m = myModel.filter(predobjNode, R2RMLVocabulary.objectMap, null);
		if (!m.isEmpty()) {
			Resource object = m.objectResource();

			// look for parentTriplesMap declaration
			m = myModel.filter(object, R2RMLVocabulary.parentTriplesMap, null);
			if (!m.isEmpty()) {
				return m.objectResource();
			}
		}
		return null;
	}

	/**
	 * method to get the child column in a join condition
	 * @param myModel - the Model of mappings
	 * @param predobjNode - the pred obj node containing the join condition
	 * @return the child column condition as a string
	 */
	public String getChildColumn(Model myModel, Resource predobjNode) {

		// process OBJECTMAP
		Model m = myModel.filter(predobjNode, R2RMLVocabulary.objectMap, null);
		if (!m.isEmpty()) {
			Resource object = m.objectResource();

			// look for joincondition declaration
			m = myModel.filter(object, R2RMLVocabulary.joinCondition, null);
			if (!m.isEmpty()) {
				Resource objectt = m.objectResource();

				// look for child declaration
				m = myModel.filter(objectt, R2RMLVocabulary.child, null);
				if (!m.isEmpty()) {
					return trimTo1(m.objectString());
				}
			}
		}
		return null;
	}

	/**
	 * method to get the parent column in a join condition
	 * @param myModel - the Model of mappings
	 * @param predobjNode - the pred obj node containing the join condition
	 * @return the parent column condition as a string
	 */
	public String getParentColumn(Model myModel, Resource predobjNode) {
		// process OBJECTMAP
		Model m = myModel.filter(predobjNode, R2RMLVocabulary.objectMap, null);
		if (!m.isEmpty()) {
			Resource object = m.objectResource();

			// look for joincondition declaration
			m = myModel.filter(object, R2RMLVocabulary.joinCondition, null);
			if (!m.isEmpty()) {
				Resource objectt = m.objectResource();

				// look for parent declaration
				m = myModel.filter(objectt, R2RMLVocabulary.parent, null);
				if (!m.isEmpty()) {
					return trimTo1(m.objectString());
				}
			}
		}
		return null;
	}

}
