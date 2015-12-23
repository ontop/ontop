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

import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;

public class OBDAVocabulary {

	/* Constants */

	public static final ValueConstant NULL = new ValueConstantImpl("null",
			COL_TYPE.STRING);
	public static final ValueConstant TRUE = new ValueConstantImpl("true",
			COL_TYPE.BOOLEAN);
	public static final ValueConstant FALSE = new ValueConstantImpl("false",
			COL_TYPE.BOOLEAN);

	/* Numeric operation predicates */

	public static final NonBooleanOperationPredicate MINUS = new NonBooleanOperationPredicateImpl(
			"minus", null); // TODO (ROMAN): check -- never used
	public static final NonBooleanOperationPredicate ADD = new NonBooleanOperationPredicateImpl(
			"add", null, null);
	public static final NonBooleanOperationPredicate SUBTRACT = new NonBooleanOperationPredicateImpl(
			"subtract", null, null);
	public static final NonBooleanOperationPredicate MULTIPLY = new NonBooleanOperationPredicateImpl(
			"multiply", null, null);
	public static final NonBooleanOperationPredicate DIVIDE = new NonBooleanOperationPredicateImpl(
			"divide", null, null);
	public static final NonBooleanOperationPredicate ABS = new NonBooleanOperationPredicateImpl(
			"abs", null);
	public static final NonBooleanOperationPredicate ROUND = new NonBooleanOperationPredicateImpl(
			"round", null);
	public static final NonBooleanOperationPredicate CEIL = new NonBooleanOperationPredicateImpl(
			"ceil", null);
	public static final NonBooleanOperationPredicate FLOOR = new NonBooleanOperationPredicateImpl(
			"floor", null);
	public static final NonBooleanOperationPredicate RAND = new NonBooleanOperationPredicateImpl(
			"RAND");


	/* Boolean predicates */

	public static final BooleanOperationPredicate AND = new BooleanOperationPredicateImpl(
			"AND", 2);
	public static final BooleanOperationPredicate OR = new BooleanOperationPredicateImpl(
			"OR", 2);
	public static final BooleanOperationPredicate NOT = new BooleanOperationPredicateImpl(
			"NOT", 1);

	public static final BooleanOperationPredicate EQ = new BooleanOperationPredicateImpl(
			"EQ", 2);
	public static final BooleanOperationPredicate NEQ = new BooleanOperationPredicateImpl(
			"NEQ", 2);

	public static final BooleanOperationPredicate GTE = new BooleanOperationPredicateImpl(
			"GTE", 2);
	public static final BooleanOperationPredicate GT = new BooleanOperationPredicateImpl(
			"GT", 2);
	public static final BooleanOperationPredicate LTE = new BooleanOperationPredicateImpl(
			"LTE", 2);
	public static final BooleanOperationPredicate LT = new BooleanOperationPredicateImpl(
			"LT", 2);

	public static final BooleanOperationPredicate IS_NULL = new BooleanOperationPredicateImpl(
			"IS_NULL", 1);
	public static final BooleanOperationPredicate IS_NOT_NULL = new BooleanOperationPredicateImpl(
			"IS_NOT_NULL", 1);
	public static final BooleanOperationPredicate IS_TRUE = new BooleanOperationPredicateImpl(
			"IS_TRUE", 1);

	public static final BooleanOperationPredicate STR_STARTS = new BooleanOperationPredicateImpl(
			"STRSTARTS", 2);
	public static final BooleanOperationPredicate STR_ENDS = new BooleanOperationPredicateImpl(
			"STRENDS", 2);
	public static final BooleanOperationPredicate CONTAINS = new BooleanOperationPredicateImpl(
			"CONTAINS", 2);


	/*SPARQL String predicates */

	public static final NonBooleanOperationPredicate UCASE = new NonBooleanOperationPredicateImpl(
			"UCASE", COL_TYPE.LITERAL ); 

	public static final NonBooleanOperationPredicate LCASE = new NonBooleanOperationPredicateImpl(
			"LCASE", COL_TYPE.LITERAL ); 

	public static final NonBooleanOperationPredicate STRLEN = new NonBooleanOperationPredicateImpl(
			"STRLEN", COL_TYPE.LITERAL ); 

	public static final NonBooleanOperationPredicate SUBSTR = new NonBooleanOperationPredicateImpl(
			"SUBSTR", COL_TYPE.LITERAL, COL_TYPE.INTEGER, COL_TYPE.INTEGER); 

	public static final NonBooleanOperationPredicate STRBEFORE = new NonBooleanOperationPredicateImpl(
			"STRBEFORE", COL_TYPE.LITERAL, COL_TYPE.LITERAL); 

	public static final NonBooleanOperationPredicate STRAFTER = new NonBooleanOperationPredicateImpl(
			"STRAFTER", COL_TYPE.LITERAL, COL_TYPE.LITERAL ); 

	public static final NonBooleanOperationPredicate REPLACE = new NonBooleanOperationPredicateImpl(
			"REPLACE", COL_TYPE.LITERAL, COL_TYPE.LITERAL, COL_TYPE.LITERAL);

	public static final NonBooleanOperationPredicate CONCAT = new NonBooleanOperationPredicateImpl(
			"CONCAT", COL_TYPE.LITERAL, COL_TYPE.LITERAL );

	public static final NonBooleanOperationPredicate ENCODE_FOR_URI = new NonBooleanOperationPredicateImpl(
			"ENCODE_FOR_URI", COL_TYPE.LITERAL);

	/*Hash functions*/
	
	public static final NonBooleanOperationPredicate MD5 = new NonBooleanOperationPredicateImpl(
			"MD5", COL_TYPE.LITERAL);
	
	public static final NonBooleanOperationPredicate SHA1 = new NonBooleanOperationPredicateImpl(
			"SHA1", COL_TYPE.LITERAL);
	
	public static final NonBooleanOperationPredicate SHA512 = new NonBooleanOperationPredicateImpl(
			"SHA521", COL_TYPE.LITERAL);
	
	public static final NonBooleanOperationPredicate SHA256 = new NonBooleanOperationPredicateImpl(
			"SHA256", COL_TYPE.LITERAL);


	/* SPARQL Functions on Dates and Times */

	public static final NonBooleanOperationPredicate NOW = new NonBooleanOperationPredicateImpl(
			"NOW");

	public static final NonBooleanOperationPredicate YEAR = new NonBooleanOperationPredicateImpl(
			"YEAR", COL_TYPE.DATETIME_STAMP);
	
	public static final NonBooleanOperationPredicate DAY = new NonBooleanOperationPredicateImpl(
			"DAY", COL_TYPE.DATETIME_STAMP);
	
	public static final NonBooleanOperationPredicate MONTH = new NonBooleanOperationPredicateImpl(
			"MONTH", COL_TYPE.DATETIME_STAMP);
	
	public static final NonBooleanOperationPredicate HOURS = new NonBooleanOperationPredicateImpl(
			"HOURS", COL_TYPE.DATETIME_STAMP);
	
	public static final NonBooleanOperationPredicate MINUTES = new NonBooleanOperationPredicateImpl(
			"MINUTES", COL_TYPE.DATETIME_STAMP);
	
	public static final NonBooleanOperationPredicate SECONDS = new NonBooleanOperationPredicateImpl(
			"SECONDS", COL_TYPE.DATETIME_STAMP);
	
	public static final NonBooleanOperationPredicate TZ = new NonBooleanOperationPredicateImpl(
			"TZ", COL_TYPE.DATETIME_STAMP);

	public static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";


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

	// TODO: to be removed
	public static final String RDFS_LITERAL_URI = "http://www.w3.org/2000/01/rdf-schema#Literal";

	/* Built-in function URIs */

	// The name of the function that creates URI's in Quest
	public static final String QUEST_URI = "URI";

	public static final Predicate QUEST_CAST = new PredicateImpl("cast", 2,
			new COL_TYPE[2]);

	public static final String QUEST_QUERY = "ans1";

	/* SPARQL algebra operations */

	public static final Predicate SPARQL_JOIN = new AlgebraOperatorPredicateImpl(
			"Join");
	public static final Predicate SPARQL_LEFTJOIN = new AlgebraOperatorPredicateImpl(
			"LeftJoin");

	/* SPARQL built-in functions */

	public static final NonBooleanOperationPredicate SPARQL_STR = new NonBooleanOperationPredicateImpl(
			"str", COL_TYPE.LITERAL );
	public static final NonBooleanOperationPredicate SPARQL_DATATYPE = new NonBooleanOperationPredicateImpl(
			"datatype", COL_TYPE.LITERAL );
	public static final NonBooleanOperationPredicate SPARQL_LANG = new NonBooleanOperationPredicateImpl(
			"lang" , COL_TYPE.LITERAL );
	public static final NonBooleanOperationPredicate UUID = new NonBooleanOperationPredicateImpl(
			"UUID");
	public static final NonBooleanOperationPredicate STRUUID = new NonBooleanOperationPredicateImpl(
			"STRUUID");

	/* SPARQL built-in predicates */

	public static final BooleanOperationPredicate SPARQL_IS_LITERAL = new BooleanOperationPredicateImpl(
			"isLiteral", 1);
	public static final BooleanOperationPredicate SPARQL_IS_URI = new BooleanOperationPredicateImpl(
			"isURI", 1);
	public static final BooleanOperationPredicate SPARQL_IS_IRI = new BooleanOperationPredicateImpl(
			"isIRI", 1);
	public static final BooleanOperationPredicate SPARQL_IS_BLANK = new BooleanOperationPredicateImpl(
			"isBlank", 1);
	public static final BooleanOperationPredicate SPARQL_LANGMATCHES = new BooleanOperationPredicateImpl(
			"LangMatches", 2);
	public static final BooleanOperationPredicate SPARQL_REGEX = new BooleanOperationPredicateImpl(
			"regex", 3);
	
	// ROMAN (23 Dec 2015) THIS COMES ONLY FROM MAPPINGS
	public static final BooleanOperationPredicate SQL_LIKE = new BooleanOperationPredicateImpl(
			"like", 2);

}
