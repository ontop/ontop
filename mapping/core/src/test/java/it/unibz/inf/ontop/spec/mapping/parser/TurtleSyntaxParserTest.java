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
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.parser.impl.TurtleOBDASQLParser;
import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibz.inf.ontop.utils.MappingTestingTools.*;


/**
 * Test syntax of the parser.
 * Added new extension. Define if the mapping column contains a data property with rdfs:Literal ("{column}")
 * or an object property (<{column}>)
 * @link {it.unibz.inf.obda.parser.TurtleOBDA.g}
 * */

public class TurtleSyntaxParserTest {

	private final static Logger log = LoggerFactory.getLogger(TurtleSyntaxParserTest.class);
    private final SpecificationFactory specificationFactory;

    public TurtleSyntaxParserTest() {
		OntopMappingConfiguration configuration = OntopMappingConfiguration
				.defaultBuilder().build();
		Injector injector = configuration.getInjector();
        specificationFactory = injector.getInstance(SpecificationFactory.class);
    }

    @Test
	public void test_1_1() {
		final boolean result = parse(":Person-{id} a :Person .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_1_2() {
		final boolean result = parse("<http://example.org/testcase#Person-{id}> a :Person .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_1_3() {
		final boolean result = parse("<http://example.org/testcase#Person-{id}> a <http://example.org/testcase#Person> .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_1_4() {
		final boolean result = parse("<http://example.org/testcase#Person-{id}> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example.org/testcase#Person> .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_2_1() {
		final boolean result = parse(":Person-{id} :hasFather :Person-{id} .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_2_2() {
		final boolean result = parse(":Person-{id} :hasFather <http://example.org/testcase#Person-12> .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_2_3() {
		final boolean result = parse(":Person-{id} <http://example.org/testcase#hasFather> <http://example.org/testcase#Person-12> .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_3_1_database() {
		final boolean result = parse(":Person-{id} :firstName {fname} .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_3_1_new_literal() {
		final boolean result = parse(":Person-{id} :firstName \"{fname}\" .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_3_1_new_string() {
		final boolean result = parse(":Person-{id} :firstName \"{fname}\"^^xsd:string .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_3_1_new_iri() {
		final boolean result = parse(":Person-{id} :firstName <{fname}> .");
		TestCase.assertTrue(result);
	}
	@Test
	public void test_3_2() {
		final boolean result = parse(":Person-{id} :firstName {fname}^^xsd:string .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_3_concat() {
		final boolean result = parse(":Person-{id} :firstName \"hello {fname}\"^^xsd:string .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_3_concat_number() {
		final boolean result = parse(":Person-{id} :firstName \"hello {fname}\"^^xsd:double .");
		TestCase.assertTrue(result);
	}

	public void test_3_3() {
		final boolean result = parse(":Person-{id} :firstName {fname}@en-US .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_4_1_1() {
		final boolean result = parse(":Person-{id} :firstName \"John\"^^xsd:string .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_4_1_2() {
		final boolean result = parse(":Person-{id} <http://example.org/testcase#firstName> \"John\"^^xsd:string .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_4_2_1() {
		final boolean result = parse(":Person-{id} :firstName \"John\"^^rdfs:Literal .");
		TestCase.assertFalse(result);
	}

	@Test
	public void test_4_2_2() {
		final boolean result = parse(":Person-{id} :firstName \"John\"@en-US .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_5_1_1() {
		final boolean result = parse(":Person-{id} a :Person ; :firstName {fname} .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_5_1_2() {
		final boolean result = parse(":Person-{id} a :Person ; :firstName {fname} ; :age {age} .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_5_1_3() {
		final boolean result = parse(":Person-{id} a :Person ; :hasFather :Person-{id} ; :firstName {fname} ; :age {age} .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_5_2_1() {
		final boolean result = parse(":Person-{id} a :Person ; :firstName {fname}^^xsd:string .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_5_2_2() {
		final boolean result = parse(":Person-{id} a :Person ; :firstName {fname}^^xsd:string ; :age {age}^^xsd:integer .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_5_2_3() {
		final boolean result = parse(":Person-{id} a :Person ; :hasFather :Person-{id} ; :firstName {fname}^^xsd:string ; :age {age}^^xsd:integer .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_5_2_4() {
		final boolean result = parse(":Person-{id} a :Person ; :hasFather :Person-{id} ; :firstName {fname}^^xsd:string ; :age {age}^^xsd:integer ; :description {text}@en-US .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_5_2_5() {
		final boolean result = parse(":Person-{id} a <http://example.org/testcase#Person> ; <http://example.org/testcase:hasFather> :Person-{id} ; <http://example.org/testcase#firstName> {fname}^^xsd:string ; <http://example.org/testcase#age> {age}^^xsd:integer ; <http://example.org/testcase#description> {text}@en-US .");
		TestCase.assertTrue(result);
	}

	@Ignore("TODO: should we forbid not-recognized datatypes using the XSD prefix?")
	@Test
	public void test_6_1() {
		final boolean result = parse(":Person-{id} a :Person ; :firstName {fname}^^xsd:String .");
		TestCase.assertFalse(result);
	}

	@Test
	public void test_6_1_literal() {
		final boolean result = parse(":Person-{id} a :Person ; :firstName \"Sarah\" .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_6_2() {
		final boolean result = parse(":Person-{id} a :Person ; :firstName {fname}^^ex:randomDatatype .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_7_1() {
		final boolean result = parse(":Person-{id} a :Person .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_7_2() {
		final boolean result = parse(":Person-{id} :hasFather :Person-{id} .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_8_1() {
		final boolean result = parse(":Person-{id} rdf:type :Person .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_8_2() {
		final boolean result = parse("ex:Person-{id} rdf:type ex:Person .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_8_3() {
		final boolean result = parse("ex:Person-{id} ex:hasFather ex:Person-123 .");
		TestCase.assertTrue(result);
	}

	@Test
	public void test_8_4() {
		final boolean result = parse("ex:Person/{id}/ ex:hasFather ex:Person/123/ .");
		TestCase.assertTrue(result);
	}

	//multiple triples with different subjects
	@Test
	public void test_9_1(){
		final boolean result = compareCQIE(":S_{id} a :Student ; :fname {first_name} ; :hasCourse :C_{course_id}  .\n" +
				":C_{course_id} a :Course ; :hasProfessor :P_{id} . \n" +
				":P_{id} a :Professor ; :teaches :C_{course_id} .\n" +
				"{first_name} a :Name . ", 8);
		TestCase.assertTrue(result);
	}

	@Test
	public void test_BNODE_object(){
		final boolean result = compareCQIE("<http://example.com/emp/{empno}> <http://example.com/emp#c_ref_deptno> _:{deptId} .", 1);
		TestCase.assertTrue(result);
	}

	@Test
	public void test_9_2(){
		final boolean result = compareCQIE("{idEmigrante} a  :E21_Person ; :P131_is_identified_by {nome} ; :P11i_participated_in {numCM} .\n" +
				"{nome} a :E82_Actor_Appellation ; :P3_has_note {nome}^^xsd:string .\n" +
				"{numCM} a :E9_Move .", 6);
		TestCase.assertTrue(result);
	}

		//Test for value constant
		@Test
	public void test10() {
		final boolean result = parse(":Person-{id} a :Person ; :age 25 ; :hasDegree true ; :averageGrade 28.3 .");
		TestCase.assertTrue(result);
	}


	//Test for fully identified column
	@Test
	public void test_11_1(){
		final boolean result = parse(":Person-{person.id} a  :Person ;  :age 25 .");
		TestCase.assertFalse(result);

	}

	//Test for language tag from a variable (FORBIDDEN)
	@Test
	public void test_12_1(){
		final boolean result = parse(":Person-{id} a :Person ; :firstName {name}@{lang} . ");
		TestCase.assertFalse(result);

	}

	private boolean compareCQIE(String input, int countBody) {
		TargetQueryParser parser = new TurtleOBDASQLParser(getPrefixManager().getPrefixMap(),
                TERM_FACTORY, TARGET_ATOM_FACTORY, RDF_FACTORY);
		ImmutableList<TargetAtom> mapping;
		try {
			mapping = parser.parse(input);
		} catch (TargetQueryParserException e) {
			log.debug(e.getMessage());
			return false;
		} catch (Exception e) {
			log.debug(e.getMessage());
			return false;
		}
		return mapping.size()==countBody;
	}


	private boolean parse(String input) {
		TargetQueryParser parser = new TurtleOBDASQLParser(getPrefixManager().getPrefixMap(),
                TERM_FACTORY, TARGET_ATOM_FACTORY, RDF_FACTORY);

		ImmutableList<TargetAtom> mapping;
		try {
			mapping = parser.parse(input);
			log.debug("mapping " + mapping);
		} catch (TargetQueryParserException e) {
			log.debug(e.getMessage());
			return false;
		} catch (Exception e) {
			log.debug(e.getMessage());
			return false;
		}
		return true;
	}

	private PrefixManager getPrefixManager() {
        return specificationFactory.createPrefixManager(ImmutableMap.of(
				PrefixManager.DEFAULT_PREFIX, "http://obda.inf.unibz.it/testcase#",
				"ex:", "http://www.example.org/"
		));
	}
}
