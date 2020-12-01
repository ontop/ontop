package it.unibz.inf.ontop.spec.mapping;

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

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.injection.OntopOBDAConfiguration;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.utils.MappingTestingTools.SPECIFICATION_FACTORY;
import static org.junit.Assert.assertEquals;


public class PrefixManagerTest {

	private static final String Q_IRI_STR = "http://obda.org/predicates#q";
	private static final String PERSON_IRI_STR = "http://obda.org/onto.owl#Person";
	private static final String INDIVIDUAL_IRI_STR = "http://obda.org/onto.owl#person-individual";

	/**
	 * Checking that the atoms that use the default namespace are rendered in
	 * short form and those who don't have it are rendered with the full uri
	 */
	@Ignore
	@Test
	public void testNamespace1() {
		//PrefixManager pm = MAPPING_FACTORY.createPrefixManager(ImmutableMap.of(
		//		PrefixManager.DEFAULT_PREFIX, "http://obda.org/onto.owl#"));

		//assertEquals("http://obda.org/predicates#q", pm.getShortForm(Q_IRI_STR, true));

		//assertEquals("&:;Person", pm.getShortForm(PERSON_IRI_STR, true));

		//assertEquals("&:;person-individual", pm.getShortForm(INDIVIDUAL_IRI_STR, true));

        //pm = MAPPING_FACTORY.createPrefixManager(ImmutableMap.of(
		//           PrefixManager.DEFAULT_PREFIX, "http://obda.org/predicates#"));

		//assertEquals("&:;q", pm.getShortForm(Q_IRI_STR, true));

		//assertEquals("http://obda.org/onto.owl#Person", pm.getShortForm(PERSON_IRI_STR, true));

		//assertEquals("http://obda.org/onto.owl#person-individual", pm.getShortForm(INDIVIDUAL_IRI_STR, true));
	}

	@Test
	public void testPrefix1() {
		PrefixManager pm = SPECIFICATION_FACTORY.createPrefixManager(ImmutableMap.of(
				PrefixManager.DEFAULT_PREFIX, "http://obda.org/onto.owl#",
				"obdap:", "http://obda.org/predicates#"));

		assertEquals("obdap:q", pm.getShortForm(Q_IRI_STR));

		assertEquals(":Person", pm.getShortForm(PERSON_IRI_STR));

		assertEquals(":person-individual", pm.getShortForm(INDIVIDUAL_IRI_STR));
	}

	@Test
	public void testPrefix1a() {
		PrefixManager pm = SPECIFICATION_FACTORY.createPrefixManager(ImmutableMap.of(
				PrefixManager.DEFAULT_PREFIX, "http://obda.org/predicates#",
				"onto:", "http://obda.org/onto.owl#"));

		assertEquals(":q", pm.getShortForm(Q_IRI_STR));

		assertEquals("onto:Person", pm.getShortForm(PERSON_IRI_STR));

		assertEquals("onto:person-individual", pm.getShortForm(INDIVIDUAL_IRI_STR));
	}

	/**
	 * This test checks if the prefix are properly handled. The prefix inside uri should not be modified
	 */
	@Test
	public void testPrefixInsideURI() {
		PrefixManager pm = SPECIFICATION_FACTORY.createPrefixManager(ImmutableMap.of(
				PrefixManager.DEFAULT_PREFIX, "http://obda.org/onto.owl#",
				"obdap:", "http://obda.org/predicates#"));

		String uri = "http://obda.org/onto.owl#redirect=http://obda.org/predicates#";
		assertEquals(":redirect=http://obda.org/predicates#", pm.getShortForm(uri));
	}

	@Test
	public void testPrefixInsideURI2() {
		PrefixManager pm = SPECIFICATION_FACTORY.createPrefixManager(ImmutableMap.of(
				PrefixManager.DEFAULT_PREFIX, "http://example.com/resource/",
				"movie:", "http://www.movieontology.org/2009/10/01/movieontology.owl/"));

		String uri2 = "http://example.com/resource/?repository=repo&uri=http://www.movieontology.org/2009/10/01/movieontology.owl/China-24951";
		assertEquals(":?repository=repo&uri=http://www.movieontology.org/2009/10/01/movieontology.owl/China-24951", pm.getShortForm(uri2));
	}
}
