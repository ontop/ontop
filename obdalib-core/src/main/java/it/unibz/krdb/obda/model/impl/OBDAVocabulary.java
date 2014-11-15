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

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;

public class OBDAVocabulary {
	
	/* Constants */

	public static final Constant NULL = new ValueConstantImpl("null", COL_TYPE.STRING);
	public static final Constant TRUE = new ValueConstantImpl("t", COL_TYPE.BOOLEAN);
	public static final Constant FALSE = new ValueConstantImpl("f", COL_TYPE.BOOLEAN);
	
	/* Numeric operation predicates */
	
	public static final Predicate MINUS = new NumericalOperationPredicateImpl("minus", 1);
	public static final Predicate ADD = new NumericalOperationPredicateImpl("add", 2);
	public static final Predicate SUBSTRACT = new NumericalOperationPredicateImpl("substract", 2);
	public static final Predicate MULTIPLY = new NumericalOperationPredicateImpl("multiply", 2);
	
		
	/* Boolean predicates */

	public static final Predicate AND = new BooleanOperationPredicateImpl("AND", 2);
	public static final Predicate OR = new BooleanOperationPredicateImpl("OR", 2);
	public static final Predicate NOT = new BooleanOperationPredicateImpl("NOT", 1);

	public static final Predicate EQ = new BooleanOperationPredicateImpl("EQ", 2);
	public static final Predicate NEQ = new BooleanOperationPredicateImpl("NEQ", 2);

	public static final Predicate GTE = new BooleanOperationPredicateImpl("GTE", 2);
	public static final Predicate GT = new BooleanOperationPredicateImpl("GT", 2);
	public static final Predicate LTE = new BooleanOperationPredicateImpl("LTE", 2);
	public static final Predicate LT = new BooleanOperationPredicateImpl("LT", 2);

	public static final Predicate IS_NULL = new BooleanOperationPredicateImpl("IS_NULL", 1);
	public static final Predicate IS_NOT_NULL = new BooleanOperationPredicateImpl("IS_NOT_NULL", 1);
	
	public static final Predicate IS_TRUE = new BooleanOperationPredicateImpl("IS_TRUE", 1);
	
	

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

	public static final String QUEST_TRIPLE_STR = "triple";
	public static final Predicate QUEST_TRIPLE_PRED = new PredicateImpl(QUEST_TRIPLE_STR, 3, new COL_TYPE[3]);

	public static final String QUEST_CAST_STR = "cast";
	public static final Predicate QUEST_CAST = new PredicateImpl(QUEST_CAST_STR, 2, new COL_TYPE[2]);
	
	public static final String QUEST_QUERY = "ans1";

	/* SPARQL algebra predicates */

	public static final Predicate SPARQL_JOIN = new AlgebraOperatorPredicateImpl("Join", COL_TYPE.STRING);
	public static final Predicate SPARQL_LEFTJOIN = new AlgebraOperatorPredicateImpl("LeftJoin", COL_TYPE.STRING);

	/* Boolean operations */
	
	public static final Predicate SPARQL_IS_LITERAL = new BooleanOperationPredicateImpl("isLiteral");
	public static final Predicate SPARQL_IS_URI = new BooleanOperationPredicateImpl("isURI");
	public static final Predicate SPARQL_IS_IRI = new BooleanOperationPredicateImpl("isIRI");
	public static final Predicate SPARQL_IS_BLANK = new BooleanOperationPredicateImpl("isBlank");
	public static final Predicate SPARQL_LANGMATCHES = new BooleanOperationPredicateImpl("LangMatches", 2);
	public static final Predicate SPARQL_STR = new NonBooleanOperationPredicateImpl("str");
	public static final Predicate SPARQL_DATATYPE = new NonBooleanOperationPredicateImpl("datatype");
	public static final Predicate SPARQL_LANG = new NonBooleanOperationPredicateImpl("lang");
	public static final Predicate SPARQL_REGEX = new BooleanOperationPredicateImpl("regex", 3);
	public static final Predicate SPARQL_LIKE = new BooleanOperationPredicateImpl("like", 2);

}
