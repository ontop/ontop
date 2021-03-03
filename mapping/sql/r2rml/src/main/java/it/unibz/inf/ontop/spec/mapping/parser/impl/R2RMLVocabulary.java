package it.unibz.inf.ontop.spec.mapping.parser.impl;

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
 * Class to represent R2RML standard vocabulary
 */
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.eclipse.rdf4j.common.net.ParsedIRI;


public class R2RMLVocabulary {

    /**
	 * Returns true if the passed string is an absolute IRI (possibly a template).
	 *
     */
    public static boolean isAbsolute(String resource) {
		int index = resource.indexOf('{');
    	String prefix = index >= 0 ? resource.substring(0, index) : resource;
		try {
			return ParsedIRI.create(prefix).isAbsolute();
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * Pre-pends the passed resource string with a default prefix in order
	 * to make it into a valid URI.
	 *
     */
    public static String resolveIri(String resource, String baseIRI) {
		if ( !isAbsolute(resource)) {
			return baseIRI + resource;
		} else {
			return resource;
		}
	}

	public static final RDF4J rdf4j = new RDF4J();
	public static final IRI TriplesMap = rdf4j.createIRI("http://www.w3.org/ns/r2rml#TriplesMap");

	public static final IRI logicalTable = rdf4j.createIRI("http://www.w3.org/ns/r2rml#logicalTable");
	public static final IRI tableName = rdf4j.createIRI("http://www.w3.org/ns/r2rml#tableName");
	public static final IRI baseTableOrView = rdf4j.createIRI("http://www.w3.org/ns/r2rml#baseTableOrView");
	public static final IRI r2rmlView = rdf4j.createIRI("http://www.w3.org/ns/r2rml#R2RMLView");

	public static final IRI subjectMap = rdf4j.createIRI("http://www.w3.org/ns/r2rml#subjectMap");
	public static final IRI subjectMapClass = rdf4j.createIRI("http://www.w3.org/ns/r2rml#SubjectMap");

	public static final IRI subject = rdf4j.createIRI("http://www.w3.org/ns/r2rml#subject");
	public static final IRI predicateObjectMap = rdf4j.createIRI("http://www.w3.org/ns/r2rml#predicateObjectMap");
	public static final IRI predicateMap = rdf4j.createIRI("http://www.w3.org/ns/r2rml#predicateMap");
	public static final IRI objectMap = rdf4j.createIRI("http://www.w3.org/ns/r2rml#objectMap");
	public static final IRI object = rdf4j.createIRI("http://www.w3.org/ns/r2rml#object");
	public static final IRI refObjectMap = rdf4j.createIRI("http://www.w3.org/ns/r2rml#refObjectMap");
	public static final IRI graphMap = rdf4j.createIRI("http://www.w3.org/ns/r2rml#graphMap");
	public static final IRI graph = rdf4j.createIRI("http://www.w3.org/ns/r2rml#graph");
	public static final IRI defaultGraph = rdf4j.createIRI("http://www.w3.org/ns/r2rml#defaultGraph");

	public static final IRI predicate = rdf4j.createIRI("http://www.w3.org/ns/r2rml#predicate");
	public static final IRI template = rdf4j.createIRI("http://www.w3.org/ns/r2rml#template");
	public static final IRI column = rdf4j.createIRI("http://www.w3.org/ns/r2rml#column");
	public static final IRI constant = rdf4j.createIRI("http://www.w3.org/ns/r2rml#constant");
	public static final IRI termType = rdf4j.createIRI("http://www.w3.org/ns/r2rml#termType");
	public static final IRI termMap = rdf4j.createIRI("http://www.w3.org/ns/r2rml#TermMap");
	public static final IRI language = rdf4j.createIRI("http://www.w3.org/ns/r2rml#language");
	public static final IRI datatype = rdf4j.createIRI("http://www.w3.org/ns/r2rml#datatype");
	public static final IRI inverseExpression = rdf4j.createIRI("http://www.w3.org/ns/r2rml#inverseExpression");
	public static final IRI iri = rdf4j.createIRI("http://www.w3.org/ns/r2rml#IRI");
	public static final IRI blankNode = rdf4j.createIRI("http://www.w3.org/ns/r2rml#BlankNode");
	public static final IRI literal = rdf4j.createIRI("http://www.w3.org/ns/r2rml#Literal");
	public static final IRI classUri = rdf4j.createIRI("http://www.w3.org/ns/r2rml#class");
	public static final IRI sqlQuery = rdf4j.createIRI("http://www.w3.org/ns/r2rml#sqlQuery");
	public static final IRI sqlVersion = rdf4j.createIRI("http://www.w3.org/ns/r2rml#sqlVersion");

	public static final IRI parentTriplesMap = rdf4j.createIRI("http://www.w3.org/ns/r2rml#parentTriplesMap");
	public static final IRI joinCondition = rdf4j.createIRI("http://www.w3.org/ns/r2rml#joinCondition");
	public static final IRI child = rdf4j.createIRI("http://www.w3.org/ns/r2rml#child");
	public static final IRI parent = rdf4j.createIRI("http://www.w3.org/ns/r2rml#parent");

}
