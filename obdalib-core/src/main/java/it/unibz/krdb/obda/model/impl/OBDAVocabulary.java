package it.unibz.krdb.obda.model.impl;

/*
 * #%L
 * ontop-obdalib-core
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

import java.util.HashMap;
import java.util.Map;

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;

public class OBDAVocabulary {
	

	/* Constants */

	public static final Constant NULL = new ValueConstantImpl("null", COL_TYPE.STRING);

	public static final Constant TRUE = new ValueConstantImpl("t", COL_TYPE.BOOLEAN);

	public static final Constant FALSE = new ValueConstantImpl("f", COL_TYPE.BOOLEAN);

	/* Numeric operations */

	public static final String MINUS_STR = "minus";
	
	public static final String ADD_STR = "add";
	
	public static final String SUBSTRACT_STR = "substract";
	
	public static final String MULTIPLY_STR = "multiply";
	
	/* Numeric operation predicates */
	
	public static final Predicate MINUS = new NumericalOperationPredicateImpl(
			MINUS_STR, 1);
	
	public static final Predicate ADD = new NumericalOperationPredicateImpl(
			ADD_STR, 2);
	
	public static final Predicate SUBSTRACT = new NumericalOperationPredicateImpl(
			SUBSTRACT_STR, 2);
	
	public static final Predicate MULTIPLY = new NumericalOperationPredicateImpl(
			MULTIPLY_STR, 2);
	
	/* Boolean predicate URIs */

	public static final String strAND = "AND";

	public static final String strEQ = "EQ";

	public static final String strGTE = "GTE";

	public static final String strGT = "GT";

	public static final String strLTE = "LTE";

	public static final String strLT = "LT";

	public static final String strNEQ = "NEQ";

	public static final String strNOT = "NOT";

	public static final String strOR = "OR";

	public static final String strIS_NULL = "IS_NULL";

	public static final String strIS_NOT_NULL = "IS_NOT_NULL";

	public static final String strIS_TRUE = "IS_TRUE";
	
	/* Boolean predicates */

	public static final Predicate AND = new BooleanOperationPredicateImpl(
			strAND, 2);

	public static final Predicate EQ = new BooleanOperationPredicateImpl(
			strEQ, 2);

	public static final Predicate GTE = new BooleanOperationPredicateImpl(
			strGTE, 2);

	public static final Predicate GT = new BooleanOperationPredicateImpl(
			strGT, 2);

	public static final Predicate LTE = new BooleanOperationPredicateImpl(
			strLTE, 2);

	public static final Predicate LT = new BooleanOperationPredicateImpl(
			strLT, 2);

	public static final Predicate NEQ = new BooleanOperationPredicateImpl(
			strNEQ, 2);

	public static final Predicate NOT = new BooleanOperationPredicateImpl(
			strNOT, 1);

	public static final Predicate OR = new BooleanOperationPredicateImpl(
			strOR, 2);

	public static final Predicate IS_NULL = new BooleanOperationPredicateImpl(
			strIS_NULL, 1);

	public static final Predicate IS_NOT_NULL = new BooleanOperationPredicateImpl(
			strIS_NOT_NULL, 1);
	
	public static final Predicate IS_TRUE = new BooleanOperationPredicateImpl(
			strIS_TRUE, 1);

	public static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

	/* Data type predicate URIs */

	public static final String RDFS_LITERAL_URI = "http://www.w3.org/2000/01/rdf-schema#Literal";

	public static final String XSD_STRING_URI = "http://www.w3.org/2001/XMLSchema#string";

	public static final String XSD_INT_URI = "http://www.w3.org/2001/XMLSchema#int";

    public static final String XSD_POSITIVE_INTEGER_URI = "http://www.w3.org/2001/XMLSchema#positiveInteger";

    public static final String XSD_NEGATIVE_INTEGER_URI = "http://www.w3.org/2001/XMLSchema#negativeInteger";

    public static final String XSD_NON_POSITIVE_INTEGER_URI = "http://www.w3.org/2001/XMLSchema#nonPositiveInteger";

    public static final String XSD_UNSIGNED_INT_URI = "http://www.w3.org/2001/XMLSchema#unsignedInt";

    public static final String XSD_NON_NEGATIVE_INTEGER_URI = "http://www.w3.org/2001/XMLSchema#nonNegativeInteger";

    public static final String XSD_INTEGER_URI = "http://www.w3.org/2001/XMLSchema#integer";

    public static final String XSD_LONG_URI = "http://www.w3.org/2001/XMLSchema#long";

	public static final String XSD_DECIMAL_URI = "http://www.w3.org/2001/XMLSchema#decimal";

	public static final String XSD_FLOAT_URI = "http://www.w3.org/2001/XMLSchema#float";

	public static final String XSD_DOUBLE_URI = "http://www.w3.org/2001/XMLSchema#double";

	public static final String XSD_DATETIME_URI = "http://www.w3.org/2001/XMLSchema#dateTime";

	public static final String XSD_BOOLEAN_URI = "http://www.w3.org/2001/XMLSchema#boolean";
	
	public static final String XSD_DATE_URI = "http://www.w3.org/2001/XMLSchema#date";
	
	public static final String XSD_TIME_URI = "http://www.w3.org/2001/XMLSchema#time";

	public static final String XSD_YEAR_URI = "http://www.w3.org/2001/XMLSchema#gYear";

	private static final Map<String, COL_TYPE> mapURItoCOLTYPE;
	private static final Map<COL_TYPE, String> mapCOLTYPEtoURI;
	
	static {
		mapURItoCOLTYPE = new HashMap<String, COL_TYPE>();
		
		mapURItoCOLTYPE.put(RDFS_LITERAL_URI, COL_TYPE.LITERAL); // 1
		mapURItoCOLTYPE.put(XSD_STRING_URI, COL_TYPE.STRING);  // 2
		mapURItoCOLTYPE.put(XSD_INT_URI, COL_TYPE.INT);  // 3
		mapURItoCOLTYPE.put(XSD_POSITIVE_INTEGER_URI, COL_TYPE.POSITIVE_INTEGER); // 4
		mapURItoCOLTYPE.put(XSD_NEGATIVE_INTEGER_URI, COL_TYPE.NEGATIVE_INTEGER); // 5
		mapURItoCOLTYPE.put(XSD_NON_POSITIVE_INTEGER_URI, COL_TYPE.NON_POSITIVE_INTEGER); // 6
		mapURItoCOLTYPE.put(XSD_UNSIGNED_INT_URI, COL_TYPE.UNSIGNED_INT);   // 7
		mapURItoCOLTYPE.put(XSD_NON_NEGATIVE_INTEGER_URI, COL_TYPE.NEGATIVE_INTEGER); // 8
		mapURItoCOLTYPE.put(XSD_INTEGER_URI, COL_TYPE.INTEGER);  // 9
		mapURItoCOLTYPE.put(XSD_LONG_URI, COL_TYPE.LONG);  // 10
		mapURItoCOLTYPE.put(XSD_DECIMAL_URI, COL_TYPE.DECIMAL);  // 11
		mapURItoCOLTYPE.put(XSD_FLOAT_URI, COL_TYPE.FLOAT); // 12
		mapURItoCOLTYPE.put(XSD_DOUBLE_URI, COL_TYPE.DOUBLE);  // 13
		mapURItoCOLTYPE.put(XSD_DATETIME_URI, COL_TYPE.DATETIME); // 14
		mapURItoCOLTYPE.put(XSD_BOOLEAN_URI, COL_TYPE.BOOLEAN);  // 15
		mapURItoCOLTYPE.put(XSD_DATE_URI, COL_TYPE.DATE);  // 16
		mapURItoCOLTYPE.put(XSD_TIME_URI, COL_TYPE.TIME);  // 17
		mapURItoCOLTYPE.put(XSD_YEAR_URI, COL_TYPE.YEAR);  // 18

		mapCOLTYPEtoURI = new HashMap<COL_TYPE, String>();	

		mapCOLTYPEtoURI.put(COL_TYPE.LITERAL, RDFS_LITERAL_URI); // 1
		mapCOLTYPEtoURI.put(COL_TYPE.STRING, XSD_STRING_URI);  // 2
		mapCOLTYPEtoURI.put(COL_TYPE.INT, XSD_INT_URI);  // 3
		mapCOLTYPEtoURI.put(COL_TYPE.POSITIVE_INTEGER, XSD_POSITIVE_INTEGER_URI); // 4
		mapCOLTYPEtoURI.put(COL_TYPE.NEGATIVE_INTEGER, XSD_NEGATIVE_INTEGER_URI); // 5
		mapCOLTYPEtoURI.put(COL_TYPE.NON_POSITIVE_INTEGER, XSD_NON_POSITIVE_INTEGER_URI); // 6
		mapCOLTYPEtoURI.put(COL_TYPE.UNSIGNED_INT, XSD_UNSIGNED_INT_URI);   // 7
		mapCOLTYPEtoURI.put(COL_TYPE.NEGATIVE_INTEGER, XSD_NON_NEGATIVE_INTEGER_URI); // 8
		mapCOLTYPEtoURI.put(COL_TYPE.INTEGER, XSD_INTEGER_URI);  // 9
		mapCOLTYPEtoURI.put(COL_TYPE.LONG, XSD_LONG_URI);  // 10
		mapCOLTYPEtoURI.put(COL_TYPE.DECIMAL, XSD_DECIMAL_URI);  // 11
		mapCOLTYPEtoURI.put(COL_TYPE.FLOAT, XSD_FLOAT_URI); // 12
		mapCOLTYPEtoURI.put(COL_TYPE.DOUBLE, XSD_DOUBLE_URI);  // 13
		mapCOLTYPEtoURI.put(COL_TYPE.DATETIME, XSD_DATETIME_URI); // 14
		mapCOLTYPEtoURI.put(COL_TYPE.BOOLEAN, XSD_BOOLEAN_URI);  // 15
		mapCOLTYPEtoURI.put(COL_TYPE.DATE, XSD_DATE_URI);  // 16
		mapCOLTYPEtoURI.put(COL_TYPE.TIME, XSD_TIME_URI);  // 17
		mapCOLTYPEtoURI.put(COL_TYPE.YEAR, XSD_YEAR_URI);  // 18		
	}
	
	public static COL_TYPE getDataType(String uri) {
		return mapURItoCOLTYPE.get(uri);
	}
	
	public static String getDataTypeURI(COL_TYPE type) {
		return mapCOLTYPEtoURI.get(type);
	}

	/* Common namespaces and prefixes */

	public static final String NS_XSD = "http://www.w3.org/2001/XMLSchema#";

	public static final String NS_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	public static final String NS_RDFS = "http://www.w3.org/2000/01/rdf-schema#";

	public static final String NS_OWL = "http://www.w3.org/2002/07/owl#";

	public static final String NS_QUEST = "http://obda.org/quest#";

	public static final String PREFIX_XSD = "xsd:";

	public static final String PREFIX_RDF = "rdf:";

	public static final String PREFIX_RDFS = "rdfs:";

	public static final String PREFIX_OWL = "owl:";

	public static final String PREFIX_QUEST = "quest:";

	/* Built-in function URIs */

	// The name of the function that creates URI's in Quest
	public static final String QUEST_URI = "URI";

	// The name of the function that creates URI's in Quest
	public static final String QUEST_BNODE = "BNODE";

	public static final String QUEST_TRIPLE_STR = "triple";

	public static final Predicate QUEST_TRIPLE_PRED = new PredicateImpl(
			QUEST_TRIPLE_STR, 3, new COL_TYPE[3]);

	public static final String QUEST_CAST_STR = "cast";

	public static final Predicate QUEST_CAST = new PredicateImpl(
			QUEST_CAST_STR, 2, new COL_TYPE[2]);
	
	public static final String QUEST_QUERY = "ans1";

	/* SPARQL Algebra vocabulary */

	public static final String SPARQL_JOIN_URI = "Join";

	public static final String SPARQL_LEFTJOIN_URI = "LeftJoin";

	public static final String SPARQL_LANGMATCHES_URI = "LangMatches";

	public static final String SPARQL_IS_LITERAL_URI = "isLiteral";

	public static final String SPARQL_IS_URI_URI = "isURI";

	public static final String SPARQL_IS_IRI_URI = "isIRI";

	public static final String SPARQL_IS_BLANK_URI = "isBlank";

	public static final String SPARQL_STR_URI = "str";

	public static final String SPARQL_DATATYPE_URI = "datatype";

	public static final String SPARQL_LANG_URI = "lang";

	public static final String SPARQL_REGEX_URI = "regex";
	
	public static final String SPARQL_LIKE_URI = "like";

	/* SPARQL Algebra predicate */

	public static final Predicate SPARQL_JOIN = new AlgebraOperatorPredicateImpl(
			SPARQL_JOIN_URI, COL_TYPE.STRING);

	public static final Predicate SPARQL_LEFTJOIN = new AlgebraOperatorPredicateImpl(
			SPARQL_LEFTJOIN_URI, COL_TYPE.STRING);

	public static final Predicate SPARQL_IS_LITERAL = new BooleanOperationPredicateImpl(
			SPARQL_IS_LITERAL_URI);

	public static final Predicate SPARQL_IS_URI = new BooleanOperationPredicateImpl(
			SPARQL_IS_URI_URI);

	public static final Predicate SPARQL_IS_IRI = new BooleanOperationPredicateImpl(
			SPARQL_IS_IRI_URI);

	public static final Predicate SPARQL_IS_BLANK = new BooleanOperationPredicateImpl(
			SPARQL_IS_BLANK_URI);

	public static final Predicate SPARQL_LANGMATCHES = new BooleanOperationPredicateImpl(
			SPARQL_LANGMATCHES_URI, 2);

	public static final Predicate SPARQL_STR = new NonBooleanOperationPredicateImpl(
			SPARQL_STR_URI);

	public static final Predicate SPARQL_DATATYPE = new NonBooleanOperationPredicateImpl(
			SPARQL_DATATYPE_URI);

	public static final Predicate SPARQL_LANG = new NonBooleanOperationPredicateImpl(
			SPARQL_LANG_URI);

	public static final Predicate SPARQL_REGEX = new BooleanOperationPredicateImpl(
			SPARQL_REGEX_URI, 3);
	
	public static final Predicate SPARQL_LIKE = new BooleanOperationPredicateImpl(
			SPARQL_LIKE_URI, 2);

	
}
