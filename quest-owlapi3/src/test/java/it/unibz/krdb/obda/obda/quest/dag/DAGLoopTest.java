package it.unibz.krdb.obda.obda.quest.dag;

/*
 * #%L
 * ontop-quest-owlapi3
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


import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.PunningException;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAGBuilder;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAGBuilderImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.GraphBuilder;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.GraphBuilderImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.GraphImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Interval;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.NamedDAGBuilderImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.SemanticIndexEngine;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.SemanticIndexEngineImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class DAGLoopTest {

	Ontology onto;
	@Before
	public void setUp() {
		
		OWLAPI3Translator t = new OWLAPI3Translator();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owlonto;
		try {
			owlonto = man.loadOntologyFromOntologyDocument(new File("src/test/resources/test/dag/final_project_original.owl"));
		
		onto = t.translate(owlonto);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PunningException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

	@Test
	public void testLoop() throws Exception {
		
		// generate Graph
		GraphBuilder change = new GraphBuilderImpl(onto);

		GraphImpl graph = (GraphImpl) change.getGraph();

		// generate DAG
		DAGBuilder change2 = new DAGBuilderImpl(graph);

		DAG dag = change2.getDAG();
		// generate named DAG
		NamedDAGBuilderImpl namedchange = new NamedDAGBuilderImpl(dag);

		DAG pureIsa = namedchange.getDAG();

		TBoxReasoner namedReasoner = new TBoxReasonerImpl(pureIsa);
		OntologyFactory ofac = OntologyFactoryImpl.getInstance();

		
	}


}
