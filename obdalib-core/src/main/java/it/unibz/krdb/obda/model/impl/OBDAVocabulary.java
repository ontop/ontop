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

import it.unibz.krdb.obda.model.BooleanOperationPredicate;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.StringOperationPredicate;
import it.unibz.krdb.obda.model.ValueConstant;

public class OBDAVocabulary {

	/* Constants */

	public static final ValueConstant NULL = new ValueConstantImpl("null",
			COL_TYPE.STRING);
	public static final ValueConstant TRUE = new ValueConstantImpl("true",
			COL_TYPE.BOOLEAN);
	public static final ValueConstant FALSE = new ValueConstantImpl("false",
			COL_TYPE.BOOLEAN);

	/* Numeric operation predicates */

	public static final Predicate MINUS = new NumericalOperationPredicateImpl(
			"minus", 1); // TODO (ROMAN): check -- never used
	public static final Predicate ADD = new NumericalOperationPredicateImpl(
			"add", 2);
	public static final Predicate SUBTRACT = new NumericalOperationPredicateImpl(
			"subtract", 2);
	public static final Predicate MULTIPLY = new NumericalOperationPredicateImpl(
			"multiply", 2);
	public static final Predicate ABS = new NumericalOperationPredicateImpl(
			"abs", 1);
	public static final Predicate ROUND = new NumericalOperationPredicateImpl(
			"round", 1);
	public static final Predicate CEIL = new NumericalOperationPredicateImpl(
			"ceil", 1);
	public static final Predicate FLOOR = new NumericalOperationPredicateImpl(
			"floor", 1);
	public static final Predicate RAND = new NumericalOperationPredicateImpl(
			"RAND", 0);

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
			"STR_ENDS", 2);
	public static final BooleanOperationPredicate CONTAINS = new BooleanOperationPredicateImpl(
			"CONTAINS", 2);


	/* String predicates */

	public static final StringOperationPredicate UCASE = new StringOperationPredicateImpl(
			"UCASE", 1, new COL_TYPE[] { COL_TYPE.LITERAL }); // added by Nika

	public static final StringOperationPredicate LCASE = new StringOperationPredicateImpl(
			"LCASE", 1, new COL_TYPE[] { COL_TYPE.LITERAL }); // added by Nika

	public static final StringOperationPredicate STRLEN = new StringOperationPredicateImpl(
			"STRLEN", 1, new COL_TYPE[] { COL_TYPE.LITERAL }); // added by Nika

	public static final StringOperationPredicate SUBSTR = new StringOperationPredicateImpl(
			"SUBSTR", 3, new COL_TYPE[] { COL_TYPE.LITERAL, COL_TYPE.INTEGER,
					COL_TYPE.INTEGER }); // Nika

	public static final StringOperationPredicate STRBEFORE = new StringOperationPredicateImpl(
			"STRBEFORE", 2,
			new COL_TYPE[] { COL_TYPE.LITERAL, COL_TYPE.LITERAL }); // Nika

	public static final StringOperationPredicate STRAFTER = new StringOperationPredicateImpl(
			"STRAFTER", 2,
			new COL_TYPE[] { COL_TYPE.LITERAL, COL_TYPE.LITERAL }); // Nika

	public static final StringOperationPredicate REPLACE = new StringOperationPredicateImpl(
			"REPLACE", 3, new COL_TYPE[] { COL_TYPE.LITERAL, COL_TYPE.LITERAL,
					COL_TYPE.LITERAL });

	public static final StringOperationPredicate CONCAT = new StringOperationPredicateImpl(
			"CONCAT", 2, new COL_TYPE[] { COL_TYPE.LITERAL, COL_TYPE.LITERAL });

	public static final StringOperationPredicate ENCODE_FOR_URI = new StringOperationPredicateImpl(
			"ENCODE_FOR_URI", 1, new COL_TYPE[]{COL_TYPE.LITERAL});

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

	public static final Predicate SPARQL_STR = new NonBooleanOperationPredicateImpl(
			"str");
	public static final Predicate SPARQL_DATATYPE = new NonBooleanOperationPredicateImpl(
			"datatype");
	public static final Predicate SPARQL_LANG = new NonBooleanOperationPredicateImpl(
			"lang");

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
	public static final BooleanOperationPredicate SPARQL_LIKE = new BooleanOperationPredicateImpl(
			"like", 2);

}
