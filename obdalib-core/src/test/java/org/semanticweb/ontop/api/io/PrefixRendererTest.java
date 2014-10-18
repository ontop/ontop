package org.semanticweb.ontop.api.io;

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

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.injection.OBDACoreModule;
import org.semanticweb.ontop.injection.OBDAProperties;
import org.semanticweb.ontop.io.PrefixManager;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.DatalogProgram;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.impl.FunctionalTermImpl;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;

import junit.framework.TestCase;

//import com.hp.hpl.jena.iri.IRIFactory;

public class PrefixRendererTest extends TestCase {

	private DatalogProgram query;
	private CQIE rule1;
    private final NativeQueryLanguageComponentFactory factory;

    public PrefixRendererTest() {
        Injector injector = Guice.createInjector(new OBDACoreModule(new OBDAProperties()));
        factory = injector.getInstance(NativeQueryLanguageComponentFactory.class);
    }
	
	public void setUp() throws Exception {
		OBDADataFactory pfac = OBDADataFactoryImpl.getInstance();
		OBDADataFactory tfac = OBDADataFactoryImpl.getInstance();
		query = tfac.getDatalogProgram();

		LinkedList<Term> innerterms = new LinkedList<Term>();
		innerterms.add(tfac.getVariable("id"));
		
//		IRIFactory fact = new IRIFactory();

		List<Term> terms = new LinkedList<Term>();
		terms.add(tfac.getFunction(pfac.getPredicate("http://obda.org/onto.owl#person-individual", 1), innerterms));

		Function body = tfac.getFunction(pfac.getPredicate("http://obda.org/onto.owl#Person", 1), terms);

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
        PrefixManager pm;
        Map<String, String> prefixes = new HashMap<>();
		prefixes.put(PrefixManager.DEFAULT_PREFIX, "http://obda.org/onto.owl#");
        pm = factory.create(prefixes);
		String name = pm.getShortForm(query.getRules().get(0).getHead().getFunctionSymbol().toString(), true);
		assertTrue(name, name.equals("http://obda.org/predicates#q"));

		name = pm.getShortForm(((Function) query.getRules().get(0).getBody().get(0)).getFunctionSymbol().toString(), true);
		assertTrue(name, name.equals("&:;Person"));

		Function atom0 = (Function) query.getRules().get(0).getBody().get(0);
		name = pm.getShortForm(((FunctionalTermImpl)atom0.getTerms().get(0)).getFunctionSymbol().toString(), true);
		assertTrue(name, name.equals("&:;person-individual"));

		prefixes.put(PrefixManager.DEFAULT_PREFIX, "http://obda.org/predicates#");
        pm = factory.create(prefixes);
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
        PrefixManager pm;
        Map<String, String> prefixes = new HashMap<>();
		prefixes.put(PrefixManager.DEFAULT_PREFIX, "http://obda.org/onto.owl#");
		prefixes.put("obdap:", "http://obda.org/predicates#");
        pm = factory.create(prefixes);

		String name = pm.getShortForm(query.getRules().get(0).getHead().getFunctionSymbol().toString(), false);
		assertTrue(name, name.equals("obdap:q"));

		name = pm.getShortForm(((Function) query.getRules().get(0).getBody().get(0)).getFunctionSymbol().toString(), false);
		assertTrue(name, name.equals(":Person"));

		Function atom0 = (Function) query.getRules().get(0).getBody().get(0);
		name = pm.getShortForm(((FunctionalTermImpl) atom0.getTerms().get(0)).getFunctionSymbol().toString(), false);
		assertTrue(name, name.equals(":person-individual"));

		prefixes.put(PrefixManager.DEFAULT_PREFIX, "http://obda.org/predicates#");
		prefixes.put("onto:", "http://obda.org/onto.owl#");
        pm = factory.create(prefixes);
		name = pm.getShortForm(query.getRules().get(0).getHead().getFunctionSymbol().toString(), false);
		assertTrue(name, name.equals(":q"));

		name = pm.getShortForm(((Function) query.getRules().get(0).getBody().get(0)).getFunctionSymbol().toString(), false);
		assertTrue(name, name.equals("onto:Person"));

		atom0 = (Function) query.getRules().get(0).getBody().get(0);
		name = pm.getShortForm(((FunctionalTermImpl) atom0.getTerms().get(0)).getFunctionSymbol().toString(), false);
		assertTrue(name, name.equals("onto:person-individual"));
	}
}
