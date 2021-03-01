package it.unibz.inf.ontop.spec.mapping.parser;

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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.TargetQueryParserException;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.model.vocabulary.RDFS;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import org.apache.commons.rdf.api.IRI;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.utils.MappingTestingTools.*;
import static it.unibz.inf.ontop.utils.MappingTestingTools.TARGET_ATOM_FACTORY;
import static org.junit.Assert.assertEquals;


/**
 * Test syntax of the parser.
 * Added new extension. Define if the mapping column contains a data property with rdfs:Literal ("{column}")
 * or an object property (<{column}>)
 * */

public class TurtleSyntaxParserTest {

	private final TargetQueryParser parser;

	private static final IRI DEFAULT_DATATYPE = RDFS.LITERAL;

	public TurtleSyntaxParserTest() {
		PrefixManager prefixManager = SPECIFICATION_FACTORY.createPrefixManager(ImmutableMap.of(
				PrefixManager.DEFAULT_PREFIX, "http://obda.inf.unibz.it/testcase#",
				"ex:", "http://www.example.org/"));
		parser = TARGET_QUERY_PARSER_FACTORY.createParser(prefixManager);
	}

	private static IRIConstant getConstantIRI(String iri) {
		return TERM_FACTORY.getConstantIRI(iri);
	}

	private static IRIConstant getConstantIRI(IRI iri) {
		return TERM_FACTORY.getConstantIRI(iri);
	}

	private static Variable getVariable(String variable) {
		return TERM_FACTORY.getVariable(variable);
	}

	private static TargetAtom getTripleTargetAtom(ImmutableTerm s, ImmutableTerm p, ImmutableTerm o) {
		return TARGET_ATOM_FACTORY.getTripleTargetAtom(s, p, o);
	}

	private static TargetAtom getQuadTargetAtom(ImmutableTerm s, ImmutableTerm p, ImmutableTerm o, ImmutableTerm g) {
		return TARGET_ATOM_FACTORY.getQuadTargetAtom(s, p, o, g);
	}

	private static ImmutableFunctionalTerm getIRIFunctionalTerm(ImmutableList<Template.Component> template, Variable v1) {
		return TERM_FACTORY.getIRIFunctionalTerm(template,
				ImmutableList.of(TERM_FACTORY.getPartiallyDefinedToStringCast(v1)));
	}

	private static ImmutableFunctionalTerm getIRIFunctionalTerm(ImmutableList<Template.Component> template, Variable v1, Variable v2) {
		return TERM_FACTORY.getIRIFunctionalTerm(template,
				ImmutableList.of(TERM_FACTORY.getPartiallyDefinedToStringCast(v1),
				TERM_FACTORY.getPartiallyDefinedToStringCast(v2)));
	}

	private static ImmutableFunctionalTerm getBnodeFunctionalTerm(ImmutableList<Template.Component> template, Variable v1) {
		return TERM_FACTORY.getBnodeFunctionalTerm(template,
				ImmutableList.of(TERM_FACTORY.getPartiallyDefinedToStringCast(v1)));
	}

	private static ImmutableFunctionalTerm getRDFLiteralFunctionalTerm(ImmutableTerm t, IRI type) {
		return TERM_FACTORY.getRDFLiteralFunctionalTerm(t, type);
	}

	private static ImmutableFunctionalTerm getRDFLiteralFunctionalTerm(ImmutableTerm t, String lang) {
		return TERM_FACTORY.getRDFLiteralFunctionalTerm(t, lang);
	}

	@Test(expected = TargetQueryParserException.class)
	public void test_1_1_empty_placeholder() throws TargetQueryParserException {
		parser.parse(":Person-{} a :Person .");
	}

	@Test
	public void test_1_1() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} a :Person .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI(RDF.TYPE),
				getConstantIRI("http://obda.inf.unibz.it/testcase#Person"))), result);
	}

	@Test
	public void test_1_1_equality() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":x={x}&y={y} a :Person .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#x=", 0, "&y=", 1),
						getVariable("x"), getVariable("y")),
				getConstantIRI(RDF.TYPE),
				getConstantIRI("http://obda.inf.unibz.it/testcase#Person"))), result);
	}

	@Test
	public void test_1_1_quotation() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{\"id\"} a :Person .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("\"id\"")),
				getConstantIRI(RDF.TYPE),
				getConstantIRI("http://obda.inf.unibz.it/testcase#Person"))), result);
	}

	@Test
	public void test_1_1_mysql_quotation() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{`id`} a :Person .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("`id`")),
				getConstantIRI(RDF.TYPE),
				getConstantIRI("http://obda.inf.unibz.it/testcase#Person"))), result);
	}

	@Test
	public void test_1_1_quotation_nested() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{\"id\"\"\"} a :Person .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("\"id\"\"\"")),
				getConstantIRI(RDF.TYPE),
				getConstantIRI("http://obda.inf.unibz.it/testcase#Person"))), result);
	}

	@Test(expected = TargetQueryParserException.class)
	public void test_1_2_empty_placeholder() throws TargetQueryParserException {
		parser.parse("<http://example.org/testcase#Person-{}> a :Person .");
	}

	@Test
	public void test_1_2() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				"<http://example.org/testcase#Person-{id}> a :Person .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://example.org/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI(RDF.TYPE),
				getConstantIRI("http://obda.inf.unibz.it/testcase#Person"))), result);
	}

	@Test
	public void test_1_2_quotation() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				"<http://example.org/testcase#Person-{\"id\"}> a :Person .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://example.org/testcase#Person-", 0),
						getVariable("\"id\"")),
				getConstantIRI(RDF.TYPE),
				getConstantIRI("http://obda.inf.unibz.it/testcase#Person"))), result);
	}

	@Test
	public void test_1_2_quotation_nested() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				"<http://example.org/testcase#Person-{\"\"\"id\"\"\"}> a :Person .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://example.org/testcase#Person-", 0),
						getVariable("\"\"\"id\"\"\"")),
				getConstantIRI(RDF.TYPE),
				getConstantIRI("http://obda.inf.unibz.it/testcase#Person"))), result);
	}


	@Test
	public void test_1_3() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				"<http://example.org/testcase#Person-{id}> a <http://example.org/testcase#Person> .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://example.org/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI(RDF.TYPE),
				getConstantIRI("http://example.org/testcase#Person"))), result);
	}

	@Test
	public void test_1_4() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				"<http://example.org/testcase#Person-{id}> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example.org/testcase#Person> .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://example.org/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI(RDF.TYPE),
				getConstantIRI("http://example.org/testcase#Person"))), result);
	}

	@Test
	public void test_2_1() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} :hasFather :Person-{id} .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI("http://obda.inf.unibz.it/testcase#hasFather"),
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("id")))), result);
	}

	@Test
	public void test_2_2() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} :hasFather <http://example.org/testcase#Person-12> .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI("http://obda.inf.unibz.it/testcase#hasFather"),
				getConstantIRI("http://example.org/testcase#Person-12"))), result);
	}

	@Test
	public void test_2_3() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} <http://example.org/testcase#hasFather> <http://example.org/testcase#Person-12> .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI("http://example.org/testcase#hasFather"),
				getConstantIRI("http://example.org/testcase#Person-12"))), result);
	}

	@Test
	public void test_3_1_database() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} :firstName {fname} .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
				getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
						getVariable("fname")), DEFAULT_DATATYPE))), result);
	}

	@Test
	public void test_3_1_database_quotation() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} :firstName {\"fname\"} .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
				getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
						getVariable("\"fname\"")), DEFAULT_DATATYPE))), result);
	}

	@Test
	public void test_3_1_new_literal() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} :firstName \"{fname}\" .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
				getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
						getVariable("fname")), XSD.STRING))), result);
	}

	@Test
	public void test_3_1_new_literal_quotation() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} :firstName \"{\\\"fname\\\"}\" .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
				getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
						getVariable("\"fname\"")), XSD.STRING))), result);
	}

	@Test
	public void test_3_1_empty_literal() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} :firstName \"\" .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
				getRDFLiteralFunctionalTerm(TERM_FACTORY.getDBStringConstant(""), XSD.STRING))), result);
	}

	@Test
	public void test_3_1_new_string() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} :firstName \"{fname}\"^^xsd:string .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
				getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
						getVariable("fname")), XSD.STRING))), result);
	}

	@Test
	public void test_3_1_new_iri() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} :firstName <{fname}> .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
				TERM_FACTORY.getIRIFunctionalTerm(
						TERM_FACTORY.getPartiallyDefinedToStringCast(getVariable("fname"))))), result);
	}

	@Test
	public void test_3_2() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} :firstName {fname}^^xsd:string .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
				getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
						getVariable("fname")), XSD.STRING))), result);
	}

	@Test
	public void test_3_escape_concat() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} rdfs:label \"adres : {Address} \\\\{city:\\\\} {City}{Country}something\"@en-us .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI("http://www.w3.org/2000/01/rdf-schema#label"),
				getRDFLiteralFunctionalTerm(
						TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(ImmutableList.of(
								TERM_FACTORY.getDBStringConstant("adres : "),
								TERM_FACTORY.getPartiallyDefinedToStringCast(getVariable("Address")),
								TERM_FACTORY.getDBStringConstant(" {city:} "),
								TERM_FACTORY.getPartiallyDefinedToStringCast(getVariable("City")),
								TERM_FACTORY.getPartiallyDefinedToStringCast(getVariable("Country")),
								TERM_FACTORY.getDBStringConstant("something"))), "en-us"))), result);
	}

	@Test
	public void test_3_concat() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} :firstName \"hello {fname}\"^^xsd:string .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
				getRDFLiteralFunctionalTerm(
						TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(ImmutableList.of(
								TERM_FACTORY.getDBStringConstant("hello "),
								TERM_FACTORY.getPartiallyDefinedToStringCast(getVariable("fname")))), XSD.STRING))), result);
	}

	@Test
	public void test_3_concat_number() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} :firstName \"hello {fname}\"^^xsd:double .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
				getRDFLiteralFunctionalTerm(
						TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(ImmutableList.of(
								TERM_FACTORY.getDBStringConstant("hello "),
								TERM_FACTORY.getPartiallyDefinedToStringCast(
								getVariable("fname")))), XSD.DOUBLE))), result);
	}

	@Test
	public void test_3_3() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} :firstName {fname}@en-US .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
				getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
						getVariable("fname")), "en-us"))), result);
	}

	@Test
	public void test_4_1_1() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} :firstName \"John\"^^xsd:string .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
				getRDFLiteralFunctionalTerm(
						TERM_FACTORY.getDBStringConstant("John"), XSD.STRING))), result);
	}

	@Test
	public void test_4_1_2() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} <http://example.org/testcase#firstName> \"John\"^^xsd:string .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI("http://example.org/testcase#firstName"),
				getRDFLiteralFunctionalTerm(
						TERM_FACTORY.getDBStringConstant("John"), XSD.STRING))), result);
	}

	@Test
	public void test_4_2_1() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} :firstName \"John\"^^rdfs:Literal .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
				getRDFLiteralFunctionalTerm(
						TERM_FACTORY.getDBStringConstant("John"), RDFS.LITERAL))), result);
	}

	@Test
	public void test_4_2_2() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} :firstName \"John\"@en-US .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
				getRDFLiteralFunctionalTerm(
						TERM_FACTORY.getDBStringConstant("John"), "en-us"))), result);
	}

	@Test
	public void test_5_1_1() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} a :Person ; :firstName {fname} .");

		assertEquals(ImmutableList.of(
				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI(RDF.TYPE),
						getConstantIRI("http://obda.inf.unibz.it/testcase#Person")),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
						getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
								getVariable("fname")), DEFAULT_DATATYPE))), result);
	}

	@Test
	public void test_5_1_2() throws TargetQueryParserException {
		final ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} a :Person ; :firstName {fname} ; :age {age} .");

		assertEquals(ImmutableList.of(
				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI(RDF.TYPE),
						getConstantIRI("http://obda.inf.unibz.it/testcase#Person")),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
						getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
								getVariable("fname")), DEFAULT_DATATYPE)),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI("http://obda.inf.unibz.it/testcase#age"),
						getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
								getVariable("age")), DEFAULT_DATATYPE))), result);
	}

	@Test
	public void test_5_1_3() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} a :Person ; :hasFather :Person-{id} ; :firstName {fname} ; :age {age} .");

		assertEquals(ImmutableList.of(
				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI(RDF.TYPE),
						getConstantIRI("http://obda.inf.unibz.it/testcase#Person")),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI("http://obda.inf.unibz.it/testcase#hasFather"),
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id"))),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
						getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
								getVariable("fname")), DEFAULT_DATATYPE)),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI("http://obda.inf.unibz.it/testcase#age"),
						getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
								getVariable("age")), DEFAULT_DATATYPE))), result);
	}

	@Test
	public void test_5_2_1() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} a :Person ; :firstName {fname}^^xsd:string .");

		assertEquals(ImmutableList.of(
				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI(RDF.TYPE),
						getConstantIRI("http://obda.inf.unibz.it/testcase#Person")),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
						getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
								getVariable("fname")), XSD.STRING))), result);
	}

	@Test
	public void test_5_2_2() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} a :Person ; :firstName {fname}^^xsd:string ; :age {age}^^xsd:integer .");

		assertEquals(ImmutableList.of(
				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI(RDF.TYPE),
						getConstantIRI("http://obda.inf.unibz.it/testcase#Person")),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
						getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
								getVariable("fname")), XSD.STRING)),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI("http://obda.inf.unibz.it/testcase#age"),
						getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
								getVariable("age")), XSD.INTEGER))), result);
	}

	@Test
	public void test_5_2_3() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} a :Person ; :hasFather :Person-{id} ; :firstName {fname}^^xsd:string ; :age {age}^^xsd:integer .");

		assertEquals(ImmutableList.of(
				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI(RDF.TYPE),
						getConstantIRI("http://obda.inf.unibz.it/testcase#Person")),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI("http://obda.inf.unibz.it/testcase#hasFather"),
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id"))),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
						getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
								getVariable("fname")), XSD.STRING)),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI("http://obda.inf.unibz.it/testcase#age"),
						getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
								getVariable("age")), XSD.INTEGER))), result);
	}

	@Test
	public void test_5_2_4() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} a :Person ; :hasFather :Person-{id} ; :firstName {fname}^^xsd:string ; :age {age}^^xsd:integer ; :description {text}@en-US .");

		assertEquals(ImmutableList.of(
				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI(RDF.TYPE),
						getConstantIRI("http://obda.inf.unibz.it/testcase#Person")),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI("http://obda.inf.unibz.it/testcase#hasFather"),
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id"))),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
						getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
								getVariable("fname")), XSD.STRING)),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI("http://obda.inf.unibz.it/testcase#age"),
						getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
								getVariable("age")), XSD.INTEGER)),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI("http://obda.inf.unibz.it/testcase#description"),
						getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
								getVariable("text")), "en-us"))), result);
	}

	@Test
	public void test_5_2_5() throws TargetQueryParserException {
		final ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} a <http://example.org/testcase#Person> ; <http://example.org/testcase:hasFather> :Person-{id} ; <http://example.org/testcase#firstName> {fname}^^xsd:string ; <http://example.org/testcase#age> {age}^^xsd:integer ; <http://example.org/testcase#description> {text}@en-US .");

		assertEquals(ImmutableList.of(
				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI(RDF.TYPE),
						getConstantIRI("http://example.org/testcase#Person")),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI("http://example.org/testcase:hasFather"),
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id"))),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI("http://example.org/testcase#firstName"),
						getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
								getVariable("fname")), XSD.STRING)),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI("http://example.org/testcase#age"),
						getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
								getVariable("age")), XSD.INTEGER)),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI("http://example.org/testcase#description"),
						getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
								getVariable("text")), "en-us"))), result);
	}

	@Ignore("TODO: should we forbid non-recognized datatypes using the XSD prefix?")
	@Test
	public void test_6_1() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} a :Person ; :firstName {fname}^^xsd:String .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_6_1_literal() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} a :Person ; :firstName \"Sarah\" .");

		assertEquals(ImmutableList.of(
				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI(RDF.TYPE),
						getConstantIRI("http://obda.inf.unibz.it/testcase#Person")),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-",  0),
								getVariable("id")),
						getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
						getRDFLiteralFunctionalTerm(
								TERM_FACTORY.getDBStringConstant("Sarah"), XSD.STRING))), result);
	}

	@Test
	public void test_6_2() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} a :Person ; :firstName {fname}^^ex:randomDatatype .");

		assertEquals(ImmutableList.of(
				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI(RDF.TYPE),
						getConstantIRI("http://obda.inf.unibz.it/testcase#Person")),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
								getVariable("id")),
						getConstantIRI("http://obda.inf.unibz.it/testcase#firstName"),
						getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
								getVariable("fname")),
								RDF_FACTORY.createIRI("http://www.example.org/randomDatatype")))), result);
	}

	@Test
	public void test_8_1() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":Person-{id} rdf:type :Person .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#Person-", 0),
						getVariable("id")),
				getConstantIRI(RDF.TYPE),
				getConstantIRI("http://obda.inf.unibz.it/testcase#Person"))), result);
	}

	@Test
	public void test_8_2() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				"ex:Person-{id} rdf:type ex:Person .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://www.example.org/Person-", 0),
						getVariable("id")),
				getConstantIRI(RDF.TYPE),
				getConstantIRI("http://www.example.org/Person"))), result);
	}

	@Test
	public void test_8_3() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				"ex:Person-{id} ex:hasFather ex:Person-123 .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://www.example.org/Person-", 0),
						getVariable("id")),
				getConstantIRI("http://www.example.org/hasFather"),
				getConstantIRI("http://www.example.org/Person-123"))), result);
	}

	@Test
	public void test_8_4() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				"ex:Person/{id}/ ex:hasFather ex:Person/123/ .");

		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.builder().addSeparator("http://www.example.org/Person/").addColumn().addSeparator("/").build(),
						getVariable("id")),
				getConstantIRI("http://www.example.org/hasFather"),
				getConstantIRI("http://www.example.org/Person/123/"))), result);
	}

	// ROMAN 26/11/20: added : to make {first_name} an IRI template
	@Test
	public void test_9_1() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":S_{id} a :Student ; :fname :{first_name} ; :hasCourse :C_{course_id}  .\n" +
				":C_{course_id} a :Course ; :hasProfessor :P_{id} . \n" +
				":P_{id} a :Professor ; :teaches :C_{course_id} .\n" +
				":{first_name} a :Name . ");
		assertEquals(8, result.size());
	}

	// ROMAN 26/11/20: added a few : to make them IRI templates
	@Test
	public void test_9_2() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":{idEmigrante} a  :E21_Person ; :P131_is_identified_by :{nome} ; :P11i_participated_in :{numCM} .\n" +
				":{nome} a :E82_Actor_Appellation ; :P3_has_note {nome}^^xsd:string .\n" +
				":{numCM} a :E9_Move .");
		assertEquals(6, result.size());
	}

	@Test
	public void test_for_value_constant() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} a :Person ; :age 25 ; :hasDegree true ; :averageGrade 28.3 .");
		assertEquals(4, result.size());
	}

	@Test(expected = TargetQueryParserException.class)
	public void test_for_fully_qualified_column() throws TargetQueryParserException {
		parser.parse(":Person-{person.id} a  :Person ;  :age 25 .");
	}

	@Test(expected = TargetQueryParserException.class)
	public void test_for_language_tag_from_a_variable() throws TargetQueryParserException {
		parser.parse(":Person-{id} a :Person ; :firstName {name}@{lang} . ");
	}

	@Test
	public void test_BNODE_object() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse("<http://example.com/emp/{empno}> <http://example.com/emp#c_ref_deptno> _:{deptId} .");
		assertEquals(ImmutableList.of(getTripleTargetAtom(
				getIRIFunctionalTerm(Template.of("http://example.com/emp/", 0),
						getVariable("empno")),
				getConstantIRI("http://example.com/emp#c_ref_deptno"),
				TERM_FACTORY.getBnodeFunctionalTerm(
						TERM_FACTORY.getPartiallyDefinedToStringCast(getVariable("deptId"))))), result);
	}

	// Reproduces Issue #319
	@Test
	public void test_BNODE_function() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse("<http://esricanada.com/gfx_ontology_prototype/{feature_hash}> a <http://ontology.eil.utoronto.ca/icity/LandUse/Parcel> ; <http://ontology.eil.utoronto.ca/icity/LandUse/hasLandUse> _:landuse{feature_hash} .");
		assertEquals(ImmutableList.of(
				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://esricanada.com/gfx_ontology_prototype/", 0),
								getVariable("feature_hash")),
						getConstantIRI(RDF.TYPE),
						getConstantIRI("http://ontology.eil.utoronto.ca/icity/LandUse/Parcel")),
				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://esricanada.com/gfx_ontology_prototype/", 0),
								getVariable("feature_hash")),
						getConstantIRI("http://ontology.eil.utoronto.ca/icity/LandUse/hasLandUse"),
						getBnodeFunctionalTerm(Template.of("landuse", 0),
								getVariable("feature_hash")))), result);
	}

	@Test
	public void test_GRAPH_1() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				"GRAPH <http://www.ciao.it/{id}> { :{id} a :C . }");

		assertEquals(ImmutableList.of(getQuadTargetAtom(
				getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#", 0),
						getVariable("id")),
				getConstantIRI(RDF.TYPE),
				getConstantIRI("http://obda.inf.unibz.it/testcase#C"),
				getIRIFunctionalTerm(Template.of("http://www.ciao.it/", 0),
						getVariable("id")))), result);
	}

	@Test
	public void test_GRAPH_2() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				"GRAPH <http://www.ciao.it/{id}> { :{id} a :C ; :P :{attr1} . }");

		assertEquals(ImmutableList.of(
				getQuadTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#", 0),
								getVariable("id")),
						getConstantIRI(RDF.TYPE),
						getConstantIRI("http://obda.inf.unibz.it/testcase#C"),
						getIRIFunctionalTerm(Template.of("http://www.ciao.it/", 0),
								getVariable("id"))),

				getQuadTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#", 0),
								getVariable("id")),
						getConstantIRI("http://obda.inf.unibz.it/testcase#P"),
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#", 0),
								getVariable("attr1")),
						getIRIFunctionalTerm(Template.of("http://www.ciao.it/", 0),
								getVariable("id")))), result);
	}

	@Test
	public void test_GRAPH_3() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				"GRAPH <http://www.ciao.it/{id}> { :{id} a :C . :{id} :P :{attr1} . }");

		assertEquals(ImmutableList.of(
				getQuadTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#", 0),
								getVariable("id")),
						getConstantIRI(RDF.TYPE),
						getConstantIRI("http://obda.inf.unibz.it/testcase#C"),
						getIRIFunctionalTerm(Template.of("http://www.ciao.it/", 0),
								getVariable("id"))),

				getQuadTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#", 0),
								getVariable("id")),
						getConstantIRI("http://obda.inf.unibz.it/testcase#P"),
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#", 0),
								getVariable("attr1")),
						getIRIFunctionalTerm(Template.of("http://www.ciao.it/", 0),
								getVariable("id")))), result);
	}

	@Test
	public void test_GRAPH_4() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				"GRAPH :uni1 { :uni1/student/{s_id} a :Student ; ex:firstName {first_name}^^xsd:string ; ex:lastName {last_name}^^xsd:string . }");

		assertEquals(ImmutableList.of(
				getQuadTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#uni1/student/", 0),
								getVariable("s_id")),
						getConstantIRI(RDF.TYPE),
						getConstantIRI("http://obda.inf.unibz.it/testcase#Student"),
						getConstantIRI("http://obda.inf.unibz.it/testcase#uni1")),

				getQuadTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#uni1/student/", 0),
								getVariable("s_id")),
						getConstantIRI("http://www.example.org/firstName"),
						getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
								getVariable("first_name")), XSD.STRING),
						getConstantIRI("http://obda.inf.unibz.it/testcase#uni1")),

				getQuadTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#uni1/student/", 0),
								getVariable("s_id")),
						getConstantIRI("http://www.example.org/lastName"),
						getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
								getVariable("last_name")), XSD.STRING),
						getConstantIRI("http://obda.inf.unibz.it/testcase#uni1"))), result);
	}

	@Test
	public void test_GRAPH_5() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				"GRAPH :uni1 { :uni1/student/{s_id} a :Student ; ex:firstName {first_name}^^xsd:string ; ex:lastName {last_name}^^xsd:string . } " +
				"GRAPH :uni2 { :uni2/student/{s_id} a :Student . }");

		assertEquals(ImmutableList.of(
				getQuadTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#uni1/student/", 0),
								getVariable("s_id")),
						getConstantIRI(RDF.TYPE),
						getConstantIRI("http://obda.inf.unibz.it/testcase#Student"),
						getConstantIRI("http://obda.inf.unibz.it/testcase#uni1")),

				getQuadTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#uni1/student/", 0),
								getVariable("s_id")),
						getConstantIRI("http://www.example.org/firstName"),
						getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
								getVariable("first_name")), XSD.STRING),
						getConstantIRI("http://obda.inf.unibz.it/testcase#uni1")),

				getQuadTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#uni1/student/", 0),
								getVariable("s_id")),
						getConstantIRI("http://www.example.org/lastName"),
						getRDFLiteralFunctionalTerm(TERM_FACTORY.getPartiallyDefinedToStringCast(
								getVariable("last_name")), XSD.STRING),
						getConstantIRI("http://obda.inf.unibz.it/testcase#uni1")),

				getQuadTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#uni2/student/", 0),
								getVariable("s_id")),
						getConstantIRI(RDF.TYPE),
						getConstantIRI("http://obda.inf.unibz.it/testcase#Student"),
						getConstantIRI("http://obda.inf.unibz.it/testcase#uni2"))), result);
	}

	@Test
	public void test_GRAPH_6() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				"GRAPH :uni1 { :uni1/student/{s_id} a :Student . } " +
						" :uni3/student/{s_id} a :Student . " +
						"GRAPH :uni2 { :uni2/student/{s_id} a :Student . }");

		assertEquals(ImmutableList.of(
				getQuadTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#uni1/student/", 0),
								getVariable("s_id")),
						getConstantIRI(RDF.TYPE),
						getConstantIRI("http://obda.inf.unibz.it/testcase#Student"),
						getConstantIRI("http://obda.inf.unibz.it/testcase#uni1")),

				getTripleTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#uni3/student/", 0),
								getVariable("s_id")),
						getConstantIRI(RDF.TYPE),
						getConstantIRI("http://obda.inf.unibz.it/testcase#Student")),

				getQuadTargetAtom(
						getIRIFunctionalTerm(Template.of("http://obda.inf.unibz.it/testcase#uni2/student/", 0),
								getVariable("s_id")),
						getConstantIRI(RDF.TYPE),
						getConstantIRI("http://obda.inf.unibz.it/testcase#Student"),
						getConstantIRI("http://obda.inf.unibz.it/testcase#uni2"))), result);
	}


	@Ignore("Anonymous blank nodes are not supported in general")
	@Test
	public void test_qootec() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(
				":observation/{observationID} a ex:Observation ;\n" +
				"\tex:hasFeatureOfInterest :apartment/{apartmentID} ;\n" +
				"\tex:hasResult [\n" +
				"\ta ex:QuantityValue ;\n" +
				"\tex:unit ex:DegreeCelsius ;\n" +
				"\tex:numericValue {observedTemperature}^^xsd:double ] .");
		assertEquals(6, result.size());
	}
}
