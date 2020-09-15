package it.unibz.inf.ontop.owlapi.utils;

/*
 * #%L
 * ontop-quest-owlapi
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
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.OntologyBuilder;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import junit.framework.TestCase;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import static it.unibz.inf.ontop.utils.SITestingTools.*;

public class OWLAPIABoxIteratorTest extends TestCase {

	private ClassifiedTBox tbox;
	
	protected void setUp() {
		final String prefix = "http://it.unibz.inf/obda/ontologies/test/translation/onto2.owl#";
		OntologyBuilder builder = OntologyBuilderImpl.builder(RDF_FACTORY);
		builder.declareObjectProperty(getIRI(prefix, "P"));
		builder.declareObjectProperty(getIRI(prefix, "R"));
		builder.declareDataProperty(getIRI(prefix, "age"));
		builder.declareClass(getIRI(prefix, "Man"));
		builder.declareClass(getIRI(prefix, "Person"));
		builder.declareClass(getIRI(prefix, "Woman"));
		tbox = builder.build().tbox();
	}

	public void testAssertionIterator() throws Exception {
		String owlfile = "src/test/resources/test/ontologies/translation/onto2.owl";

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology owl = manager.loadOntologyFromOntologyDocument(new File(owlfile));

        int count = count(ImmutableList.of(owl));
		assertEquals(9, count);
	}

	public void testAssertionIterable() throws Exception {
		String owlfile = "src/test/resources/test/ontologies/translation/onto2.owl";

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology owl = manager.loadOntologyFromOntologyDocument(new File(owlfile));

        int count = count(ImmutableList.of(owl));
		assertEquals(9, count);
	}
	
	public void testAssertionEmptyIterable() throws Exception {

        int count = count(ImmutableList.of());
		assertEquals(0, count);
	}

	
	public void testAssertionOntology() throws Exception {
		String owlfile = "src/test/resources/test/ontologies/translation/onto2.owl";

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology owl = manager.loadOntologyFromOntologyDocument(new File(owlfile));

        int count = count(ImmutableList.of(owl));
		assertEquals(9, count);
	}
	
	public void testAssertionEmptyOntology() throws Exception {
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology owl = manager.createOntology();

        int count = count(ImmutableList.of(owl));
		assertEquals(0, count);
	}
	
	public void testAssertionOntologies() throws Exception {
		String owlfile1 = "src/test/resources/test/ontologies/translation/onto1.owl";
		String owlfile2 = "src/test/resources/test/ontologies/translation/onto2.owl";
		String owlfile3 = "src/test/resources/test/ontologies/translation/onto3.owl";

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		manager.loadOntologyFromOntologyDocument(new File(owlfile1));
		manager.loadOntologyFromOntologyDocument(new File(owlfile2));
		manager.loadOntologyFromOntologyDocument(new File(owlfile3));

        int count = count(manager.getOntologies());
		assertEquals(9, count);
	}
	
	public void testAssertionEmptyOntologySet() {

		int count = count(ImmutableList.of());
		assertEquals(0, count);
	}


	private int count(Collection<OWLOntology> ontologies) {
        Iterator<RDFFact> aboxit = new OWLAPIABoxIterator(ontologies, tbox, OWLAPI_TRANSLATOR);
        int count = 0;
        while (aboxit.hasNext()) {
            count += 1;
            aboxit.next();
        }
        return count;
    }
}
