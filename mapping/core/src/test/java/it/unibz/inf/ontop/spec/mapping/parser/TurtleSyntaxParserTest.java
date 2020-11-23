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
import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.TargetQueryParserException;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.utils.MappingTestingTools.*;
import static org.junit.Assert.assertEquals;


/**
 * Test syntax of the parser.
 * Added new extension. Define if the mapping column contains a data property with rdfs:Literal ("{column}")
 * or an object property (<{column}>)
 * @link {it.unibz.inf.obda.parser.TurtleOBDA.g}
 * */

public class TurtleSyntaxParserTest {

    private final SpecificationFactory specificationFactory;
	private final TargetQueryParser parser;

	public TurtleSyntaxParserTest() {
		OntopMappingConfiguration configuration = OntopMappingConfiguration
				.defaultBuilder().build();
		Injector injector = configuration.getInjector();
        specificationFactory = injector.getInstance(SpecificationFactory.class);
		parser = TARGET_QUERY_PARSER_FACTORY.createParser(
				specificationFactory.createPrefixManager(ImmutableMap.of(
					PrefixManager.DEFAULT_PREFIX, "http://obda.inf.unibz.it/testcase#",
						"ex:", "http://www.example.org/")));
	}

    @Test
	public void test_1_1() throws TargetQueryParserException {
		final ImmutableList<TargetAtom> result = parser.parse(":Person-{id} a :Person .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_1_2() throws TargetQueryParserException {
		final ImmutableList<TargetAtom> result = parser.parse("<http://example.org/testcase#Person-{id}> a :Person .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_1_3() throws TargetQueryParserException {
		final ImmutableList<TargetAtom> result = parser.parse("<http://example.org/testcase#Person-{id}> a <http://example.org/testcase#Person> .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_1_4() throws TargetQueryParserException {
		final ImmutableList<TargetAtom> result = parser.parse("<http://example.org/testcase#Person-{id}> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example.org/testcase#Person> .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_2_1() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} :hasFather :Person-{id} .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_2_2() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} :hasFather <http://example.org/testcase#Person-12> .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_2_3() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} <http://example.org/testcase#hasFather> <http://example.org/testcase#Person-12> .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_3_1_database() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} :firstName {fname} .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_3_1_new_literal() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} :firstName \"{fname}\" .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_3_1_new_string() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} :firstName \"{fname}\"^^xsd:string .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_3_1_new_iri() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} :firstName <{fname}> .");
		assertEquals(1, result.size());
	}
	@Test
	public void test_3_2() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":erson-{id} :firstName {fname}^^xsd:string .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_3_concat() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} :firstName \"hello {fname}\"^^xsd:string .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_3_concat_number() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} :firstName \"hello {fname}\"^^xsd:double .");
		assertEquals(1, result.size());
	}

	public void test_3_3() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} :firstName {fname}@en-US .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_4_1_1() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} :firstName \"John\"^^xsd:string .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_4_1_2() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} <http://example.org/testcase#firstName> \"John\"^^xsd:string .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_4_2_1() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} :firstName \"John\"^^rdfs:Literal .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_4_2_2() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} :firstName \"John\"@en-US .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_5_1_1() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} a :Person ; :firstName {fname} .");
		assertEquals(2, result.size());
	}

	@Test
	public void test_5_1_2() throws TargetQueryParserException {
		final ImmutableList<TargetAtom> result = parser.parse(":Person-{id} a :Person ; :firstName {fname} ; :age {age} .");
		assertEquals(3, result.size());
	}

	@Test
	public void test_5_1_3() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} a :Person ; :hasFather :Person-{id} ; :firstName {fname} ; :age {age} .");
		assertEquals(4, result.size());
	}

	@Test
	public void test_5_2_1() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} a :Person ; :firstName {fname}^^xsd:string .");
		assertEquals(2, result.size());
	}

	@Test
	public void test_5_2_2() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} a :Person ; :firstName {fname}^^xsd:string ; :age {age}^^xsd:integer .");
		assertEquals(3, result.size());
	}

	@Test
	public void test_5_2_3() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} a :Person ; :hasFather :Person-{id} ; :firstName {fname}^^xsd:string ; :age {age}^^xsd:integer .");
		assertEquals(4, result.size());
	}

	@Test
	public void test_5_2_4() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} a :Person ; :hasFather :Person-{id} ; :firstName {fname}^^xsd:string ; :age {age}^^xsd:integer ; :description {text}@en-US .");
		assertEquals(5, result.size());
	}

	@Test
	public void test_5_2_5() throws TargetQueryParserException {
		final ImmutableList<TargetAtom> result = parser.parse(":Person-{id} a <http://example.org/testcase#Person> ; <http://example.org/testcase:hasFather> :Person-{id} ; <http://example.org/testcase#firstName> {fname}^^xsd:string ; <http://example.org/testcase#age> {age}^^xsd:integer ; <http://example.org/testcase#description> {text}@en-US .");
		assertEquals(5, result.size());
	}

	@Ignore("TODO: should we forbid not-recognized datatypes using the XSD prefix?")
	@Test
	public void test_6_1() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} a :Person ; :firstName {fname}^^xsd:String .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_6_1_literal() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} a :Person ; :firstName \"Sarah\" .");
		assertEquals(2, result.size());
	}

	@Test
	public void test_6_2() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} a :Person ; :firstName {fname}^^ex:randomDatatype .");
		assertEquals(2, result.size());
	}

	@Test
	public void test_7_1() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} a :Person .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_7_2() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} :hasFather :Person-{id} .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_8_1() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} rdf:type :Person .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_8_2() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse("ex:Person-{id} rdf:type ex:Person .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_8_3() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse("ex:Person-{id} ex:hasFather ex:Person-123 .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_8_4() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse("ex:Person/{id}/ ex:hasFather ex:Person/123/ .");
		assertEquals(1, result.size());
	}

	@Test
	public void test_9_1() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":S_{id} a :Student ; :fname {first_name} ; :hasCourse :C_{course_id}  .\n" +
				":C_{course_id} a :Course ; :hasProfessor :P_{id} . \n" +
				":P_{id} a :Professor ; :teaches :C_{course_id} .\n" +
				"{first_name} a :Name . ");
		assertEquals(8, result.size());
	}

	@Test
	public void test_9_2() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse("{idEmigrante} a  :E21_Person ; :P131_is_identified_by {nome} ; :P11i_participated_in {numCM} .\n" +
				"{nome} a :E82_Actor_Appellation ; :P3_has_note {nome}^^xsd:string .\n" +
				"{numCM} a :E9_Move .");
		assertEquals(6, result.size());
	}

	@Test
	public void test_for_value_constant() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} a :Person ; :age 25 ; :hasDegree true ; :averageGrade 28.3 .");
		assertEquals(4, result.size());
	}

	@Test(expected = TargetQueryParserException.class)
	public void test_for_fully_qualified_column() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{person.id} a  :Person ;  :age 25 .");
	}

	@Test(expected = TargetQueryParserException.class)
	public void test_for_language_tag_from_a_variable() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse(":Person-{id} a :Person ; :firstName {name}@{lang} . ");
	}

	@Test
	public void test_BNODE_object() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse("<http://example.com/emp/{empno}> <http://example.com/emp#c_ref_deptno> _:{deptId} .");
		assertEquals(1, result.size());
	}

	// Reproduces Issue #319
	@Test
	public void test_BNODE_function() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse("<http://esricanada.com/gfx_ontology_prototype/{feature_hash}> a <http://ontology.eil.utoronto.ca/icity/LandUse/Parcel> ; <http://ontology.eil.utoronto.ca/icity/LandUse/hasLandUse> _:landuse{feature_hash} .");
		assertEquals(2, result.size());
	}

	@Test
	public void test_GRAPH_1() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse("GRAPH <http://www.ciao.it/{id}> { :{id} a :C . }");
		assertEquals(1, result.size());
	}

	@Test
	public void test_GRAPH_2() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse("GRAPH <http://www.ciao.it/{id}> { :{id} a :C ; :P :{attr1} . }");
		assertEquals(2, result.size());
	}

	@Test
	public void test_GRAPH_3() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse("GRAPH <http://www.ciao.it/{id}> { :{id} a :C . :{id} :P :{attr1} . }");
		assertEquals(2, result.size());
	}

	@Test
	public void test_GRAPH_4() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse("GRAPH :uni1 { :uni1/student/{s_id} a :Student ; ex:firstName {first_name}^^xsd:string ; ex:lastName {last_name}^^xsd:string . }");
		assertEquals(3, result.size());
	}

	@Test
	public void test_GRAPH_5() throws TargetQueryParserException {
		ImmutableList<TargetAtom> result = parser.parse("GRAPH :uni1 { :uni1/student/{s_id} a :Student ; ex:firstName {first_name}^^xsd:string ; ex:lastName {last_name}^^xsd:string . } " +
				"GRAPH :uni2 { :uni2/student/{s_id} a :Student ; ex:firstName {first_name}^^xsd:string ; ex:lastName {last_name}^^xsd:string . }");
		assertEquals(6, result.size());
	}
}
