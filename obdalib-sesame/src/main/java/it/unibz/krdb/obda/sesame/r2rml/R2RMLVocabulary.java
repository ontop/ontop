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
 * Class to represent R2RML standard vocabulary
 */
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

public class R2RMLVocabulary {

	public static final String baseuri = "http://example.com/base/";
	
	public static final ValueFactory fact = new ValueFactoryImpl();
	public static final URI TriplesMap = fact.createURI("http://www.w3.org/ns/r2rml#TriplesMap");
	
	public static final URI logicalTable = fact.createURI("http://www.w3.org/ns/r2rml#logicalTable");
	public static final URI tableName = fact.createURI("http://www.w3.org/ns/r2rml#tableName");
	public static final URI baseTableOrView = fact.createURI("http://www.w3.org/ns/r2rml#baseTableOrView");
	public static final URI r2rmlView = fact.createURI("http://www.w3.org/ns/r2rml#R2RMLView");

	public static final URI subjectMap = fact.createURI("http://www.w3.org/ns/r2rml#subjectMap");
	public static final URI subjectMapClass = fact.createURI("http://www.w3.org/ns/r2rml#SubjectMap");

	public static final URI subject = fact.createURI("http://www.w3.org/ns/r2rml#subject");
	public static final URI predicateObjectMap = fact.createURI("http://www.w3.org/ns/r2rml#predicateObjectMap");
	public static final URI predicateMap = fact.createURI("http://www.w3.org/ns/r2rml#predicateMap");
	public static final URI objectMap = fact.createURI("http://www.w3.org/ns/r2rml#objectMap");
	public static final URI object = fact.createURI("http://www.w3.org/ns/r2rml#object");
	public static final URI refObjectMap = fact.createURI("http://www.w3.org/ns/r2rml#refObjectMap");
	public static final URI graphMap = fact.createURI("http://www.w3.org/ns/r2rml#graphMap");
	public static final URI graph = fact.createURI("http://www.w3.org/ns/r2rml#graph");

	public static final URI predicate = fact.createURI("http://www.w3.org/ns/r2rml#predicate");
	public static final URI template = fact.createURI("http://www.w3.org/ns/r2rml#template");
	public static final URI column = fact.createURI("http://www.w3.org/ns/r2rml#column");
	public static final URI constant = fact.createURI("http://www.w3.org/ns/r2rml#constant");
	public static final URI termType = fact.createURI("http://www.w3.org/ns/r2rml#termType");
	public static final URI termMap = fact.createURI("http://www.w3.org/ns/r2rml#TermMap");
	public static final URI language = fact.createURI("http://www.w3.org/ns/r2rml#language");
	public static final URI datatype = fact.createURI("http://www.w3.org/ns/r2rml#datatype");
	public static final URI inverseExpression = fact.createURI("http://www.w3.org/ns/r2rml#inverseExpression");
	public static final URI iri = fact.createURI("http://www.w3.org/ns/r2rml#IRI");
	public static final URI blankNode = fact.createURI("http://www.w3.org/ns/r2rml#BlankNode");
	public static final URI literal = fact.createURI("http://www.w3.org/ns/r2rml#Literal");
	public static final URI classUri = fact.createURI("http://www.w3.org/ns/r2rml#class");
	public static final URI sqlQuery = fact.createURI("http://www.w3.org/ns/r2rml#sqlQuery");
	public static final URI sqlVersion = fact.createURI("http://www.w3.org/ns/r2rml#sqlVersion");

	public static final URI parentTriplesMap = fact.createURI("http://www.w3.org/ns/r2rml#parentTriplesMap");
	public static final URI joinCondition = fact.createURI("http://www.w3.org/ns/r2rml#joinCondition");
	public static final URI child = fact.createURI("http://www.w3.org/ns/r2rml#child");
	public static final URI parent = fact.createURI("http://www.w3.org/ns/r2rml#parent");

}
