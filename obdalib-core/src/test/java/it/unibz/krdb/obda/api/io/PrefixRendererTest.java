package it.unibz.krdb.obda.api.io;

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
import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import junit.framework.TestCase;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

//import com.hp.hpl.jena.iri.IRIFactory;

public class PrefixRendererTest extends TestCase {

	private PrefixManager pm;
	private DatalogProgram query;
	private CQIE rule1;
	
	public void setUp() throws Exception {
		pm = new SimplePrefixManager();
		OBDADataFactory pfac = OBDADataFactoryImpl.getInstance();
		OBDADataFactory tfac = OBDADataFactoryImpl.getInstance();
		query = tfac.getDatalogProgram();

		LinkedList<Term> innerterms = new LinkedList<Term>();
		innerterms.add(tfac.getVariable("id"));
		
//		IRIFactory fact = new IRIFactory();

		List<Term> terms = new LinkedList<Term>();
		terms.add(tfac.getFunction(pfac.getPredicate("http://obda.org/onto.owl#person-individual", 1), innerterms));

		Function body = tfac.getFunction(pfac.getClassPredicate("http://obda.org/onto.owl#Person"), terms);

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("id"));
		Function head = tfac.getFunction(pfac.getPredicate("http://obda.org/predicates#q", 1), terms);

		rule1 = tfac.getCQIE(head, Collections.singletonList(body));
		query.appendRule(rule1);
	}

	/**
	 * Checking that the atoms that use the default namespace are renderered in
	 * short form and those who don't have it are renderered with the full uri
	 */
	public void testNamespace1() {
		pm.addPrefix(PrefixManager.DEFAULT_PREFIX, "http://obda.org/onto.owl#");
		String name = pm.getShortForm(query.getRules().get(0).getHead().getFunctionSymbol().toString(), true);
		assertTrue(name, name.equals("http://obda.org/predicates#q"));

		name = pm.getShortForm(((Function) query.getRules().get(0).getBody().get(0)).getFunctionSymbol().toString(), true);
		assertTrue(name, name.equals("&:;Person"));

		Function atom0 = (Function) query.getRules().get(0).getBody().get(0);
		name = pm.getShortForm(((FunctionalTermImpl)atom0.getTerms().get(0)).getFunctionSymbol().toString(), true);
		assertTrue(name, name.equals("&:;person-individual"));

		pm.addPrefix(PrefixManager.DEFAULT_PREFIX, "http://obda.org/predicates#");
		name = pm.getShortForm(query.getRules().get(0).getHead().getFunctionSymbol().toString(), true);
		assertTrue(name, name.equals("&:;q"));

		name = pm.getShortForm(((Function) query.getRules().get(0).getBody().get(0)).getFunctionSymbol().toString(), true);
		assertTrue(name, name.equals("http://obda.org/onto.owl#Person"));

		atom0 = (Function) query.getRules().get(0).getBody().get(0);
		name = pm.getShortForm(((FunctionalTermImpl) atom0.getTerms().get(0)).getFunctionSymbol().toString(), true);
		assertTrue(name, name.equals("http://obda.org/onto.owl#person-individual"));
	}

	/**
	 * This test checks if the prefix are properly handled
	 */
	public void testPrefix1() {
		pm.addPrefix(PrefixManager.DEFAULT_PREFIX, "http://obda.org/onto.owl#");
		pm.addPrefix("obdap:", "http://obda.org/predicates#");

		String name = pm.getShortForm(query.getRules().get(0).getHead().getFunctionSymbol().toString(), false);
		assertTrue(name, name.equals("obdap:q"));

		name = pm.getShortForm(((Function) query.getRules().get(0).getBody().get(0)).getFunctionSymbol().toString(), false);
		assertTrue(name, name.equals(":Person"));

		Function atom0 = (Function) query.getRules().get(0).getBody().get(0);
		name = pm.getShortForm(((FunctionalTermImpl) atom0.getTerms().get(0)).getFunctionSymbol().toString(), false);
		assertTrue(name, name.equals(":person-individual"));

		pm.addPrefix(PrefixManager.DEFAULT_PREFIX, "http://obda.org/predicates#");
		pm.addPrefix("onto:", "http://obda.org/onto.owl#");
		name = pm.getShortForm(query.getRules().get(0).getHead().getFunctionSymbol().toString(), false);
		assertTrue(name, name.equals(":q"));

		name = pm.getShortForm(((Function) query.getRules().get(0).getBody().get(0)).getFunctionSymbol().toString(), false);
		assertTrue(name, name.equals("onto:Person"));

		atom0 = (Function) query.getRules().get(0).getBody().get(0);
		name = pm.getShortForm(((FunctionalTermImpl) atom0.getTerms().get(0)).getFunctionSymbol().toString(), false);
		assertTrue(name, name.equals("onto:person-individual"));

	}

	/**
	 * This test checks if the prefix are properly handled. The prefix inside uri should not be modified
	 */
	public void testPrefixInsideURI() {
		pm.addPrefix(PrefixManager.DEFAULT_PREFIX, "http://obda.org/onto.owl#");
		pm.addPrefix("obdap:", "http://obda.org/predicates#");

		String uri = "http://obda.org/onto.owl#redirect=http://obda.org/predicates#";

		String shortForm = pm.getShortForm(uri, false);
		System.out.println(shortForm);

		assertEquals(":redirect=http://obda.org/predicates#", shortForm);


		pm.addPrefix(PrefixManager.DEFAULT_PREFIX, "http://example.com/resource/");
		pm.addPrefix("movie:", "http://www.movieontology.org/2009/10/01/movieontology.owl/");

		String uri2 = "http://example.com/resource/?repository=repo&uri=http://www.movieontology.org/2009/10/01/movieontology.owl/China-24951";
		String shortForm2 = pm.getShortForm(uri2, false);
		System.out.println(shortForm2);
		assertEquals(":?repository=repo&uri=http://www.movieontology.org/2009/10/01/movieontology.owl/China-24951", shortForm2);



	}
}
