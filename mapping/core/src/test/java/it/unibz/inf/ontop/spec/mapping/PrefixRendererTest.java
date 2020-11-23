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

import java.util.*;

import com.google.common.collect.ImmutableMap;
import junit.framework.TestCase;
import org.junit.Ignore;

import static it.unibz.inf.ontop.utils.MappingTestingTools.*;


public class PrefixRendererTest extends TestCase {

	private static final String Q_IRI_STR = "http://obda.org/predicates#q";
	private static final String PERSON_IRI_STR = "http://obda.org/onto.owl#Person";
	private static final String INDIVIDUAL_IRI_STR = "http://obda.org/onto.owl#person-individual";

	/**
	 * Checking that the atoms that use the default namespace are rendered in
	 * short form and those who don't have it are rendered with the full uri
	 */
	@Ignore
	public void testNamespace1() {
        PrefixManager pm;
        Map<String, String> prefixes = new HashMap<>();
		prefixes.put(PrefixManager.DEFAULT_PREFIX, "http://obda.org/onto.owl#");
        pm = MAPPING_FACTORY.createPrefixManager(ImmutableMap.copyOf(prefixes));
		//String name = pm.getShortForm(Q_IRI_STR, true);
		//assertEquals("http://obda.org/predicates#q", name);

		//name = pm.getShortForm(PERSON_IRI_STR, true);
		//assertEquals("&:;Person", name);

		//name = pm.getShortForm(INDIVIDUAL_IRI_STR, true);
		//assertEquals("&:;person-individual", name);

		//prefixes.put(PrefixManager.DEFAULT_PREFIX, "http://obda.org/predicates#");
        //pm = MAPPING_FACTORY.createPrefixManager(ImmutableMap.copyOf(prefixes));
		//name = pm.getShortForm(Q_IRI_STR, true);
		//assertEquals("&:;q", name);

		//name = pm.getShortForm(PERSON_IRI_STR, true);
		//assertEquals("http://obda.org/onto.owl#Person", name);

		//name = pm.getShortForm(INDIVIDUAL_IRI_STR, true);
		//assertEquals("http://obda.org/onto.owl#person-individual", name);
	}

	/**
	 * This test checks if the prefix are properly handled
	 */
	public void testPrefix1() {
        PrefixManager pm;
        Map<String, String> prefixes = new HashMap<>();
		prefixes.put(PrefixManager.DEFAULT_PREFIX, "http://obda.org/onto.owl#");
		prefixes.put("obdap:", "http://obda.org/predicates#");
        pm = MAPPING_FACTORY.createPrefixManager(ImmutableMap.copyOf(prefixes));

		String name = pm.getShortForm(Q_IRI_STR);
		assertEquals("obdap:q", name);

		name = pm.getShortForm(PERSON_IRI_STR);
		assertEquals(":Person", name);

		name = pm.getShortForm(INDIVIDUAL_IRI_STR);
		assertEquals(":person-individual", name);

		prefixes.put(PrefixManager.DEFAULT_PREFIX, "http://obda.org/predicates#");
		prefixes.put("onto:", "http://obda.org/onto.owl#");
        pm = MAPPING_FACTORY.createPrefixManager(ImmutableMap.copyOf(prefixes));
		name = pm.getShortForm(Q_IRI_STR);
		assertEquals(":q", name);

		name = pm.getShortForm(PERSON_IRI_STR);
		assertEquals("onto:Person", name);

		name = pm.getShortForm(INDIVIDUAL_IRI_STR);
		assertEquals("onto:person-individual", name);
	}

	/**
	 * This test checks if the prefix are properly handled. The prefix inside uri should not be modified
	 */
	public void testPrefixInsideURI() {
		Map<String, String> prefixes = new HashMap<>();
		prefixes.put(PrefixManager.DEFAULT_PREFIX, "http://obda.org/onto.owl#");
		prefixes.put("obdap:", "http://obda.org/predicates#");

		PrefixManager pm = MAPPING_FACTORY.createPrefixManager(ImmutableMap.copyOf(prefixes));

		String uri = "http://obda.org/onto.owl#redirect=http://obda.org/predicates#";
		String shortForm = pm.getShortForm(uri);
		System.out.println(shortForm);
		assertEquals(":redirect=http://obda.org/predicates#", shortForm);

		prefixes.put(PrefixManager.DEFAULT_PREFIX, "http://example.com/resource/");
		prefixes.put("movie:", "http://www.movieontology.org/2009/10/01/movieontology.owl/");
		pm = MAPPING_FACTORY.createPrefixManager(ImmutableMap.copyOf(prefixes));

		String uri2 = "http://example.com/resource/?repository=repo&uri=http://www.movieontology.org/2009/10/01/movieontology.owl/China-24951";
		String shortForm2 = pm.getShortForm(uri2);
		System.out.println(shortForm2);
		assertEquals(":?repository=repo&uri=http://www.movieontology.org/2009/10/01/movieontology.owl/China-24951", shortForm2);
	}
}
