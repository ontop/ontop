package it.unibz.krdb.obda.r2rml;

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

import eu.optique.api.mapping.*;
import eu.optique.api.mapping.TermMap.TermMapType;
import eu.optique.api.mapping.impl.InvalidR2RMLMappingException;
import eu.optique.api.mapping.impl.SubjectMapImpl;
import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.DataTypePredicateImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class R2RMLParser {

	private final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	private static final DatatypeFactory dtfac = OBDADataFactoryImpl
			.getInstance().getDatatypeFactory();

	List<Predicate> classPredicates;
	List<Resource> joinPredObjNodes;

	String parsedString = "";
	String subjectString = "";
	String objectString = "";
	R2RMLMappingManager mapManager;
	Logger logger = LoggerFactory.getLogger(R2RMLParser.class);

	/**
	 * empty constructor
	 */
	public R2RMLParser() {
		mapManager = R2RMLMappingManagerFactory.getSesameMappingManager();
		classPredicates = new ArrayList<Predicate>();
		joinPredObjNodes = new ArrayList<Resource>();
	}

	/**
	 * method to get the TriplesMaps from the given Model
	 * 
	 * @param myModel
	 *            - the Model to process
	 * @return Collection<TriplesMap> - the collection of mappings
	 */
	public Collection<TriplesMap> getMappingNodes(Model myModel) {
		Collection<TriplesMap> coll = null;
		try {
			coll = mapManager.importMappings(myModel);
		} catch (InvalidR2RMLMappingException e) {
			e.printStackTrace();
		}
		return coll;
	}

	/**
	 * Get SQL query of the TriplesMap
	 * 
	 * @param tm
	 * @return
	 */
	public String getSQLQuery(TriplesMap tm) {
		return tm.getLogicalTable().getSQLQuery();
	}

	/**
	 * Get classes They can be retrieved only once, after retrieving everything
	 * is cleared.
	 * 
	 * @return
	 */
	public List<Predicate> getClassPredicates() {
		List<Predicate> classes = new ArrayList<Predicate>();
		for (Predicate p : classPredicates)
			classes.add(p);
		classPredicates.clear();
		return classes;
	}

	/**
	 * Get predicates
	 * 
	 * @param tm
	 * @return
	 */
	public Set<Resource> getPredicateObjects(TriplesMap tm) {
		Set<Resource> predobjs = new HashSet<Resource>();
		for (PredicateObjectMap pobj : tm.getPredicateObjectMaps()) {
			for (PredicateMap pm : pobj.getPredicateMaps()) {
				Resource r = (Resource) pm.getResource(Object.class);
				predobjs.add(r);
			}
		}
		return predobjs;
	}

	public Term getSubjectAtom(TriplesMap tm) throws Exception {
		return getSubjectAtom(tm, "");
	}

	/**
	 * Get subject
	 *
	 * @param tm
	 * @param joinCond
	 * @return
	 * @throws Exception
	 */
	public Term getSubjectAtom(TriplesMap tm, String joinCond) throws Exception {
		Term subjectAtom = null;
		String subj = "";
		classPredicates.clear();

		// SUBJECT
		SubjectMap sMap = tm.getSubjectMap();
		SubjectMapImpl sm = (SubjectMapImpl) sMap;
		// process template declaration
		Object termType = sm.getTermType(Object.class);

		TermMapType subjectTermType = sm.getTermMapType();

		// WORKAROUND for:
		// SubjectMap.getTemplateString() throws NullPointerException when
		// template == null
		//
		if (sMap.getTemplate() == null) {
			subj = null;
		} else {
			subj = sMap.getTemplateString();
		}

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
		// subj = sMap.getTermMapType().toString();
		// sMap.getTermType(Object.class);
		// if (subj != null) {
		//
		//
		// }

		// process class declaration
		List<Object> classes = sMap.getClasses(Object.class);
		for (Object o : classes) {
			classPredicates.add(fac.getClassPredicate(o.toString()));
		}

		if (subjectAtom == null)
			throw new Exception("Error in parsing the subjectMap in node "
					+ tm.toString());

		return subjectAtom;

	}

	/**
	 * Get body predicates
	 * 
	 * @param pom
	 * @return
	 */
	public List<Predicate> getBodyPredicates(PredicateObjectMap pom) {
		List<Predicate> bodyPredicates = new ArrayList<Predicate>();

		// process PREDICATEs
		for (PredicateMap pm : pom.getPredicateMaps()) {
			Predicate bodyPredicate = fac.getPredicate(pm.getConstant(), 2);
			bodyPredicates.add(bodyPredicate);
		}
		return bodyPredicates;
	}

	/**
	 * Get body predicates with templates
	 * 
	 * @param pom
	 * @return
	 */
	public List<Function> getBodyURIPredicates(PredicateObjectMap pom) {
		List<Function> predicateAtoms = new ArrayList<>();

		// process PREDICATEMAP
		for (PredicateMap pm : pom.getPredicateMaps()) {
			Template t = pm.getTemplate();
			if (t != null) {
				// craete uri("...",var)
				Function predicateAtom = getURIFunction(t.toString());
				predicateAtoms.add(predicateAtom);
			}

			// process column declaration
			String c = pm.getColumn();
			if (c != null) {
				Function predicateAtom = getURIFunction(c);
				predicateAtoms.add(predicateAtom);
			}
		}
		return predicateAtoms;

	}

	public Term getObjectAtom(PredicateObjectMap pom) {
		return getObjectAtom(pom, "");
	}

	public boolean isConcat(Template t) {
		String st = t.toString();
		int i, j;
		if ((i = st.indexOf("{")) > -1) {
			if ((j = st.lastIndexOf("{")) > i) {
				return true;
			} else if ((i > 0) || (j < (st.length() - 1))) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Get the object atom, it can be a constant, a column or a template
	 * 
	 * @param pom
	 * @param joinCond
	 * @return
	 * @throws Exception
	 */
	public Term getObjectAtom(PredicateObjectMap pom, String joinCond) {
		Term objectAtom = null;
		if (pom.getObjectMaps().isEmpty()) {
			return null;
		}
		ObjectMap om = pom.getObjectMap(0);

		String lan = om.getLanguageTag();
		Object datatype = om.getDatatype(Object.class);

		// we check if the object map is a constant (can be a iri or a literal)
		String obj = om.getConstant();
		if (obj != null) {
			// boolean isURI = false;
			// try {
			// java.net.URI.create(obj);
			// isURI = true;
			// } catch (IllegalArgumentException e){
			//
			// }

			// if the literal has a language property or a datatype property we
			// create the function object later
			if (lan != null || datatype != null) {
				objectAtom = fac.getConstantLiteral(obj);

			} else {
				Term newlit = fac.getConstantLiteral(obj);

				if (obj.startsWith("http://")) {
					objectAtom = fac.getUriTemplate(newlit);
				} else {
					objectAtom = fac.getTypedTerm(newlit, COL_TYPE.LITERAL); // .RDFS_LITERAL;
				}
			}
		}

		// we check if the object map is a column (can be only literal)
		// if it has a datatype or language property we check it later
		String col = om.getColumn();
		if (col != null) {
			col = trim(col);

			if (!joinCond.isEmpty()) {
				col = joinCond + col;
			}

			objectAtom = fac.getVariable(col);

		}

		// we check if the object map is a template (can be a iri, a literal or
		// a blank node)
		Template t = om.getTemplate();
		if (t != null) {
			boolean concat = isConcat(t);

			if (concat) {
				objectAtom = getTypedFunction(t.toString(), 4, joinCond);
			} else {

				// a template can be a rr:IRI, a
				// rr:Literal or rr:BlankNode

				// if the literal has a language property or a datatype property
				// we
				// create the function object later
				if (lan != null || datatype != null) {
					String value = t.getColumnName(0);
					if (!joinCond.isEmpty()) {
						value = joinCond + value;

					}
					objectAtom = fac.getVariable(value);
				} else {

					Object type = om.getTermType(Object.class);

					// we check if the template is a IRI a simple literal or a
					// blank
					// node and create the function object
					objectAtom = getTermTypeAtom(t.toString(), type, joinCond);
				}
			}
		}

		// we check if it is a literal with language tag

		if (lan != null) {
			Term langAtom = fac.getTypedTerm(objectAtom, lan);
			objectAtom = langAtom;
		}

		// we check if it is a typed literal
		if (datatype != null) {
			Predicate.COL_TYPE type = dtfac.getDataType(datatype.toString());
			if (type == null) {
				// throw new RuntimeException("Unsupported datatype: " +
				// datatype.toString());
				logger.warn("Unsupported datatype will not be converted: "
						+ datatype.toString());
			} else {
				Term dtAtom = fac.getTypedTerm(objectAtom, type);
				// Predicate dtype = new
				// DataTypePredicateImpl(datatype.toString(), COL_TYPE.OBJECT);
				// Term dtAtom = fac.getFunction(dtype, objectAtom);
				objectAtom = dtAtom;
			}
		}

		return objectAtom;
	}

	@Deprecated
	private Term getConstantObject(String objectString) {
		if (objectString.startsWith("http:"))
			return getURIFunction(objectString);
		else { // literal
			Constant constt = fac.getConstantLiteral(objectString);
			return fac.getTypedTerm(constt, COL_TYPE.LITERAL);

		}
	}

	@Deprecated
	private Term getExplicitTypedObject(String string) {

		Term typedObject = null;
		String[] strings = string.split("<");
		if (strings.length > 1) {
			String consts = strings[0];
			consts = consts.substring(0, consts.length() - 2);
			consts = trim(consts);
			String type = strings[1];
			if (type.endsWith(">"))
				type = type.substring(0, type.length() - 1);

			DataTypePredicate predicate = new DataTypePredicateImpl(type,
					COL_TYPE.OBJECT);
			Term constant = fac.getConstantLiteral(consts);
			typedObject = fac.getFunction(predicate, constant);
		}
		return typedObject;
	}

	@Deprecated
	public List<Resource> getJoinNodes(TriplesMap tm) {
		List<Resource> joinPredObjNodes = new ArrayList<Resource>();
		// get predicate-object nodes
		Set<Resource> predicateObjectNodes = getPredicateObjects(tm);
		return joinPredObjNodes;
	}

	/**
	 * get a typed atom of a specific type
	 * 
	 * @param type
	 *            - iri, blanknode or literal
	 * @param string
	 *            - the atom as string
	 * @return the contructed Function atom
	 */
	private Function getTermTypeAtom(String string, Object type, String joinCond) {

		if (type.equals(R2RMLVocabulary.iri)) {

			return getURIFunction(string, joinCond);

		} else if (type.equals(R2RMLVocabulary.blankNode)) {

			return getTypedFunction(string, 2, joinCond);

		} else if (type.equals(R2RMLVocabulary.literal)) {

			return getTypedFunction(trim(string), 3, joinCond);
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
	 * 
	 * @param parsedString
	 *            - the content of atom
	 * @param type
	 *            - 0=constant uri, 1=uri or iri, 2=bnode, 3=literal 4=concat
	 * @param joinCond
	 *            - CHILD_ or PARENT_ prefix for variables
	 * @return the constructed Function atom
	 */
	public Function getTypedFunction(String parsedString, int type,
			String joinCond) {

		List<Term> terms = new ArrayList<Term>();
		String string = (parsedString);
		if (!string.contains("{")) {
			if (type < 3) {
				if (!string.startsWith("http://")) {
					string = R2RMLVocabulary.baseuri + "{" + string + "}";
					if (type == 2) {
						string = "\"" + string + "\"";
					}
				} else {
					type = 0;
				}
			}
		}
		if (type == 1 && !string.startsWith("http://")) {
			string = R2RMLVocabulary.baseuri + string;
		}

		string = string.replace("\\{", "[");
		string = string.replace("\\}", "]");
		
		String str = string; //str for concat of constant literal
		String cons;
		int i;
		while (string.contains("{")) {
			int end = string.indexOf("}");
			int begin = string.lastIndexOf("{", end);
			
			// (Concat) if there is constant literal in template, adds it to terms list 
			if (type == 4){
				if ((i = str.indexOf("{")) > 0){
					cons = str.substring(0, i);
					str = str.substring(str.indexOf("}")+1, str.length());
					terms.add(fac.getConstantLiteral(cons));
				}else{
					str = str.substring(str.indexOf("}")+1);
				}
			}

			String var = trim(string.substring(begin + 1, end));

			// trim for making variable
			terms.add(fac.getVariable(joinCond + (var)));

			string = string.replace("{\"" + var + "\"}", "[]");
			string = string.replace("{" + var + "}", "[]");
			
		}
		if(type == 4){
			if((i = str.lastIndexOf("}")) < (str.length())){
				cons = str.substring(i+1,str.length());
				terms.add(fac.getConstantLiteral(cons));
			}
		}
	
		string = string.replace("[", "{");
		string = string.replace("]", "}");

		Term uriTemplate = null;
		switch (type) {
		// constant uri
		case 0:
			uriTemplate = fac.getConstantLiteral(string);
			terms.add(0, uriTemplate); // the URI template is always on the
										// first position in the term list
			return fac.getUriTemplate(terms);
			// URI or IRI
		case 1:
			uriTemplate = fac.getConstantLiteral(string);
			terms.add(0, uriTemplate); // the URI template is always on the
										// first position in the term list
			return fac.getUriTemplate(terms);
			// BNODE
		case 2:
			uriTemplate = fac.getConstantBNode(string);
			terms.add(0, uriTemplate); // the URI template is always on the
										// first position in the term list
			return fac.getBNodeTemplate(terms);
			// simple LITERAL
		case 3:
			uriTemplate = terms.remove(0);
			// pred = dtfac.getTypePredicate(); // OBDAVocabulary.RDFS_LITERAL;
			// the URI template is always on the first position in the term list
			// terms.add(0, uriTemplate);
			return fac.getTypedTerm(uriTemplate, COL_TYPE.LITERAL);
		//concat
		case 4:
			Function f = fac.getFunctionConcat(terms.get(0),terms.get(1));
            for(int j=2;j<terms.size();j++){
                f = fac.getFunctionConcat(f,terms.get(j));
            }
            return f;
		}
		return null;
	}

	/**
	 * method that trims a string of all its double apostrophes from beginning
	 * and end
	 * 
	 * @param string
	 *            - to be trimmed
	 * @return the string without any quotes
	 */
	private String trim(String string) {

		while (string.startsWith("\"") && string.endsWith("\"")) {

			string = string.substring(1, string.length() - 1);
		}
		return string;
	}

	/**
	 * method to trim a string of its leading or trailing quotes but one
	 * 
	 * @param string
	 *            - to be trimmed
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
	 * 
	 * @param myModel
	 *            - the Model of mappings
	 * @param predobjNode
	 *            - the pred obj node containing the join condition
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
	 * 
	 * @param myModel
	 *            - the Model of mappings
	 * @param predobjNode
	 *            - the pred obj node containing the join condition
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
	 * 
	 * @param myModel
	 *            - the Model of mappings
	 * @param predobjNode
	 *            - the pred obj node containing the join condition
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
