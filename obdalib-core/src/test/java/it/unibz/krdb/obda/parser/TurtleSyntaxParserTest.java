package it.unibz.krdb.obda.parser;

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

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.io.SimplePrefixManager;
import it.unibz.krdb.obda.model.CQIE;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TurtleSyntaxParserTest extends TestCase {

	final static Logger log = LoggerFactory.getLogger(TurtleSyntaxParserTest.class);
	
	public void test_1_1() {
		final boolean result = parse(":Person-{id} a :Person .");
		assertTrue(result);
	}
	
	public void test_1_2() {
		final boolean result = parse("<http://example.org/testcase#Person-{id}> a :Person .");
		assertTrue(result);
	}
	
	public void test_1_3() {
		final boolean result = parse("<http://example.org/testcase#Person-{id}> a <http://example.org/testcase#Person> .");
		assertTrue(result);
	}
	
	public void test_1_4() {
		final boolean result = parse("<http://example.org/testcase#Person-{id}> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example.org/testcase#Person> .");
		assertTrue(result);
	}
	
	public void test_2_1() {
		final boolean result = parse(":Person-{id} :hasFather :Person-{id} .");
		assertTrue(result);
	}
	
	public void test_2_2() {
		final boolean result = parse(":Person-{id} :hasFather <http://example.org/testcase#Person-12> .");
		assertTrue(result);
	}
	
	public void test_2_3() {
		final boolean result = parse(":Person-{id} <http://example.org/testcase#hasFather> <http://example.org/testcase#Person-12> .");
		assertTrue(result);
	}
	
	public void test_3_1() {
		final boolean result = parse(":Person-{id} :firstName {fname} .");
		assertTrue(result);
	}
	
	public void test_3_2() {
		final boolean result = parse(":Person-{id} :firstName {fname}^^xsd:string .");
		assertTrue(result);
	}
	
	public void test_3_3() {
		final boolean result = parse(":Person-{id} :firstName {fname}@en-US .");
		assertTrue(result);
	}
	
	public void test_4_1_1() {
		final boolean result = parse(":Person-{id} :firstName \"John\"^^xsd:string .");
		assertTrue(result);
	}
	
	public void test_4_1_2() {
		final boolean result = parse(":Person-{id} <http://example.org/testcase#firstName> \"John\"^^xsd:string .");
		assertTrue(result);
	}
	
	public void test_4_2_1() {
		final boolean result = parse(":Person-{id} :firstName \"John\"^^rdfs:Literal .");
		assertTrue(result);
	}
	
	public void test_4_2_2() {
		final boolean result = parse(":Person-{id} :firstName \"John\"@en-US .");
		assertTrue(result);
	}
	
	public void test_5_1_1() {
		final boolean result = parse(":Person-{id} a :Person ; :firstName {fname} .");
		assertTrue(result);
	}
	
	public void test_5_1_2() {
		final boolean result = parse(":Person-{id} a :Person ; :firstName {fname} ; :age {age} .");
		assertTrue(result);
	}
	
	public void test_5_1_3() {
		final boolean result = parse(":Person-{id} a :Person ; :hasFather :Person-{id} ; :firstName {fname} ; :age {age} .");
		assertTrue(result);
	}
	
	public void test_5_2_1() {
		final boolean result = parse(":Person-{id} a :Person ; :firstName {fname}^^xsd:string .");
		assertTrue(result);
	}
	
	public void test_5_2_2() {
		final boolean result = parse(":Person-{id} a :Person ; :firstName {fname}^^xsd:string ; :age {age}^^xsd:integer .");
		assertTrue(result);
	}
	
	public void test_5_2_3() {
		final boolean result = parse(":Person-{id} a :Person ; :hasFather :Person-{id} ; :firstName {fname}^^xsd:string ; :age {age}^^xsd:integer .");
		assertTrue(result);
	}
	
	public void test_5_2_4() {
		final boolean result = parse(":Person-{id} a :Person ; :hasFather :Person-{id} ; :firstName {fname}^^xsd:string ; :age {age}^^xsd:integer ; :description {text}@en-US .");
		assertTrue(result);
	}
	
	public void test_5_2_5() {
		final boolean result = parse(":Person-{id} a <http://example.org/testcase#Person> ; <http://example.org/testcase:hasFather> :Person-{id} ; <http://example.org/testcase#firstName> {fname}^^xsd:string ; <http://example.org/testcase#age> {age}^^xsd:integer ; <http://example.org/testcase#description> {text}@en-US .");
		assertTrue(result);
	}
	
	public void test_6_1() {
		final boolean result = parse(":Person-{id} a :Person ; :firstName {fname}^^xsd:String .");
		assertFalse(result);
	}
	
	public void test_6_2() {
		final boolean result = parse(":Person-{id} a :Person ; :firstName {fname}^^ex:randomDatatype .");
		assertFalse(result);
	}
	
	public void test_7_1() {
		final boolean result = parse(":Person-{id} a :Person .");
		assertTrue(result);
	}
	
	public void test_7_2() {
		final boolean result = parse(":Person-{id} :hasFather :Person-{id} .");
		assertTrue(result);
	}
	
	public void test_8_1() {
		final boolean result = parse(":Person-{id} rdf:type :Person .");
		assertTrue(result);
	}
	
	public void test_8_2() {
		final boolean result = parse("ex:Person-{id} rdf:type ex:Person .");
		assertTrue(result);
	}
	
	public void test_8_3() {
		final boolean result = parse("ex:Person-{id} ex:hasFather ex:Person-123 .");
		assertTrue(result);
	}
	
	public void test_8_4() {
		final boolean result = parse("ex:Person/{id}/ ex:hasFather ex:Person/123/ .");
		assertTrue(result);
	}

	//multiple triples with different subjects
	public void test_9_1(){
		final boolean result = compareCQIE(":S_{id} a :Student ; :fname {first_name} ; :hasCourse :C_{course_id}  .\n" +
				":C_{course_id} a :Course ; :hasProfessor :P_{id} . \n" +
				":P_{id} a :Professor ; :teaches :C_{course_id} .\n" +
				"{first_name} a :Name . ", 8);
				assertTrue(result);

	}


	public void test_9_2(){
		final boolean result = compareCQIE("{idEmigrante} a  :E21_Person ; :P131_is_identified_by {nome} ; :P11i_participated_in {numCM} .\n" +
				"{nome} a :E82_Actor_Appellation ; :P3_has_note {nome}^^xsd:string .\n" +
				"{numCM} a :E9_Move .", 6);
		assertTrue(result);

	}

	private boolean compareCQIE(String input, int countBody) {
		TurtleOBDASyntaxParser parser = new TurtleOBDASyntaxParser();
		parser.setPrefixManager(getPrefixManager());
		CQIE mapping;
		try {
			mapping = parser.parse(input);
		} catch (TargetQueryParserException e) {
			log.debug(e.getMessage());
			return false;
		} catch (Exception e) {
			log.debug(e.getMessage());
			return false;
		}
		return mapping.getBody().size()==countBody;
	}
	private boolean parse(String input) {
		TurtleOBDASyntaxParser parser = new TurtleOBDASyntaxParser();
		parser.setPrefixManager(getPrefixManager());

		try {
			parser.parse(input);
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
		PrefixManager pm = new SimplePrefixManager();
		pm.addPrefix(PrefixManager.DEFAULT_PREFIX, "http://obda.inf.unibz.it/testcase#");
		pm.addPrefix("ex:", "http://www.example.org/");
		return pm;
	}
}
