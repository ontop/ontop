package it.unibz.krdb.obda.quest.dag;

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


import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OWLAPI3TranslatorUtility;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import java.util.Random;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DAGPerformanceTest extends TestCase {

	Logger log = LoggerFactory.getLogger(DAGPerformanceTest.class);

	int size = 1000;
	int maxdepth = 10;

	private class LevelRange {
		int min = 0;
		int max = 0;
		int width = 0;

		public LevelRange(int min, int max) {
			this.min = min;
			this.max = max;
			this.width = max - min;
		}

		public String toString() {
			return "[" + min + "," + max + "]=" + width;
		}
	}

	/***
	 * Test the performance of classifying an ontology with 500 classes, 1000
	 * subclassAxioms and 2 roles
	 */
	public void testOnto15() throws Exception {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDataFactory fac = man.getOWLDataFactory();
		OWLOntology ont = man.createOntology(IRI.create("http://www.obda.org/krdb/obda/quest/core/dag/test.owl"));

		log.debug("Generating classes");
		String base = "http://www.obda.org/krdb/obda/quest/core/dag/test.owl#";
		OWLClass[] classes = new OWLClass[size];
		for (int i = 0; i < size; i++) {
			OWLClass c = fac.getOWLClass(IRI.create(base + "class" + i));
			classes[i] = c;
			man.addAxiom(ont, fac.getOWLDeclarationAxiom(c));
		}
		log.debug("Generating axioms");
		LevelRange[] ranges = new LevelRange[10];
		for (int depth = 0; depth < maxdepth; depth++) {
			int width = (int) ((Math.log10(depth + 1)) * size);
			LevelRange r = null;
			if (depth == 0) {
				r = new LevelRange(0, width);
			} else {
				r = new LevelRange(ranges[depth - 1].max, width);
			}
			ranges[depth] = r;
		}
		LevelRange[] corrected = new LevelRange[9];
		for (int depth = 0; depth < 9; depth++) {
			LevelRange r2 = null;
			if (depth == 0) {
				r2 = new LevelRange(0, ranges[9 - depth].width);
			} else {
				r2 = new LevelRange(corrected[depth - 1].max, (corrected[depth - 1].max) + ranges[9 - depth].width);
			}
			corrected[depth] = r2;
		}
		log.info("Creating axioms");
		Random r = new Random(10);
		for (int i = 1; i < 9; i++) {
			LevelRange parentRange = corrected[i - 1];
			LevelRange currentRange = corrected[i];
			for (int axiomindex = 0; axiomindex < (currentRange.width*1.5); axiomindex++) {
				int rand1 = r.nextInt(parentRange.width);
				int rand2 = r.nextInt(currentRange.width);
				int parent = parentRange.min + rand1;
				int child = currentRange.min + rand2;
				
				OWLClass c1 = classes[parent];
				OWLClass c2 = classes[child];
				OWLAxiom subc = fac.getOWLSubClassOfAxiom(c1, c2);
				man.addAxiom(ont, subc);
			}
		}
		
    	Random r2 = new Random(10);
    	for (int i =0; i<((ont.getAxiomCount()-size) *0.05);i++){
    		int rand1 = r2.nextInt(size);
			int rand2 = r2.nextInt(size);
			OWLClass c1 = classes[rand1];
			OWLClass c2 = classes[rand2];
			OWLAxiom subc = fac.getOWLSubClassOfAxiom(c2, c1);
			man.addAxiom(ont, subc);
    		
    	}
		

		log.debug("Translating into quest API");
		Ontology o = OWLAPI3TranslatorUtility.translate(ont);

		long start = System.nanoTime();
		log.debug("Creating a DAG out of it");
		//DAGImpl impliedDAG = DAGBuilder.getDAG(o);
		TBoxReasoner reasoner= new TBoxReasonerImpl(o);
		log.debug("{}s", ((System.nanoTime() - start)/1000000));

//		long start = System.nanoTime();
//		log.debug("Optimizing Equivalences");
//		EquivalenceTBoxOptimizer equiOptimizer = new EquivalenceTBoxOptimizer(o);
//		equiOptimizer.optimize();
//		
//		Map<Predicate, Description> equi = equiOptimizer.getEquivalenceMap();
//		log.debug("Equivalences: {}", equi.size());
//		log.debug("{}s", ((System.nanoTime() - start)/1000000));
//		log.debug("Done.");
//		System.out.println(ont);
//		System.out.println(equiOptimizer.getOptimalTBox());
	}
	
	/**
	 * Test the performance of classifying an ontology with 500 classes, 1000
	 * subclassAxioms and 2 roles
	 */
	public void testOnto17() throws Exception {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDataFactory fac = man.getOWLDataFactory();
		OWLOntology ont = man.createOntology(IRI.create("http://www.obda.org/krdb/obda/quest/core/dag/test.owl"));

		log.debug("Generating classes");
		String base = "http://www.obda.org/krdb/obda/quest/core/dag/test.owl#";
		OWLClass[] classes = new OWLClass[size];
		for (int i = 0; i < size; i++) {
			OWLClass c = fac.getOWLClass(IRI.create(base + "class" + i));
			classes[i] = c;
			man.addAxiom(ont, fac.getOWLDeclarationAxiom(c));
		}

		log.debug("Generating axioms");
		LevelRange[] ranges = new LevelRange[10];
		for (int depth = 0; depth < maxdepth; depth++) {
			int width = (int) ((Math.log10(depth + 1)) * size);
			LevelRange r = null;
			if (depth == 0) {
				r = new LevelRange(0, width);
			} else {
				r = new LevelRange(ranges[depth - 1].max, width);
			}
			ranges[depth] = r;
		}
		LevelRange[] corrected = new LevelRange[9];
		for (int depth = 0; depth < 9; depth++) {
			LevelRange r2 = null;
			if (depth == 0) {
				r2 = new LevelRange(0, ranges[9 - depth].width);
			} else {
				r2 = new LevelRange(corrected[depth - 1].max, (corrected[depth - 1].max) + ranges[9 - depth].width);
			}
			corrected[depth] = r2;
		}
		log.info("Creating axioms");
		Random r = new Random(10);
		for (int i = 1; i < 9; i++) {
			LevelRange parentRange = corrected[i - 1];
			LevelRange currentRange = corrected[i];
			for (int axiomindex = 0; axiomindex < (currentRange.width*1.7); axiomindex++) {
				int rand1 = r.nextInt(parentRange.width);
				int rand2 = r.nextInt(currentRange.width);
				int parent = parentRange.min + rand1;
				int child = currentRange.min + rand2;
				
				OWLClass c1 = classes[parent];
				OWLClass c2 = classes[child];
				OWLAxiom subc = fac.getOWLSubClassOfAxiom(c1, c2);
				man.addAxiom(ont, subc);
			}
		}
		
    	Random r2 = new Random(10);
    	for (int i =0; i<((ont.getAxiomCount()-size) *0.05);i++){
    		int rand1 = r2.nextInt(size);
			int rand2 = r2.nextInt(size);
			OWLClass c1 = classes[rand1];
			OWLClass c2 = classes[rand2];
			OWLAxiom subc = fac.getOWLSubClassOfAxiom(c2, c1);
			man.addAxiom(ont, subc);
    		
    	}
		log.debug("Translating into quest API");
		Ontology o = OWLAPI3TranslatorUtility.translate(ont);

		log.debug("Creating a DAG out of it");

		long start = System.nanoTime();
		log.debug("Creating a DAG out of it");
		//DAGImpl impliedDAG = DAGBuilder.getDAG(o);
		TBoxReasoner  reasoner = new TBoxReasonerImpl(o);
		reasoner.getClass();
		log.debug("{}s", ((System.nanoTime() - start)/1000000));

//		long start = System.nanoTime();
//		log.debug("Optimizing Equivalences");
//		EquivalenceTBoxOptimizer equiOptimizer = new EquivalenceTBoxOptimizer(o);
//		equiOptimizer.optimize();
//
//		Map<Predicate, Description> equi = equiOptimizer.getEquivalenceMap();
//		log.debug("Equivalences: {}", equi.size());
//		log.debug("{}s", ((System.nanoTime() - start)/1000000));
//		log.debug("Done.");
//		System.out.println(ont);
//		System.out.println(equiOptimizer.getOptimalTBox());
	}
	
	/**
	 * Test the performance of classifying an ontology with 500 classes, 1000
	 * subclassAxioms and 2 roles
	 */
	public void testOnto19() throws Exception {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDataFactory fac = man.getOWLDataFactory();
		OWLOntology ont = man.createOntology(IRI.create("http://www.obda.org/krdb/obda/quest/core/dag/test.owl"));

		log.debug("Generating classes");
		String base = "http://www.obda.org/krdb/obda/quest/core/dag/test.owl#";
		OWLClass[] classes = new OWLClass[size];
		for (int i = 0; i < size; i++) {
			OWLClass c = fac.getOWLClass(IRI.create(base + "class" + i));
			classes[i] = c;
			man.addAxiom(ont, fac.getOWLDeclarationAxiom(c));
		}

		log.debug("Generating axioms");
		LevelRange[] ranges = new LevelRange[10];
		for (int depth = 0; depth < maxdepth; depth++) {
			int width = (int) ((Math.log10(depth + 1)) * size);
			LevelRange r = null;
			if (depth == 0) {
				r = new LevelRange(0, width);
			} else {
				r = new LevelRange(ranges[depth - 1].max, width);
			}
			ranges[depth] = r;
		}
		LevelRange[] corrected = new LevelRange[9];
		for (int depth = 0; depth < 9; depth++) {
			LevelRange r2 = null;
			if (depth == 0) {
				r2 = new LevelRange(0, ranges[9 - depth].width);
			} else {
				r2 = new LevelRange(corrected[depth - 1].max, (corrected[depth - 1].max) + ranges[9 - depth].width);
			}
			corrected[depth] = r2;
		}

		log.info("Creating axioms");
		Random r = new Random(10);
		for (int i = 1; i < 9; i++) {
			LevelRange parentRange = corrected[i - 1];
			LevelRange currentRange = corrected[i];
			for (int axiomindex = 0; axiomindex < (currentRange.width*1.9); axiomindex++) {
				int rand1 = r.nextInt(parentRange.width);
				int rand2 = r.nextInt(currentRange.width);
				int parent = parentRange.min + rand1;
				int child = currentRange.min + rand2;
				
				OWLClass c1 = classes[parent];
				OWLClass c2 = classes[child];
				OWLAxiom subc = fac.getOWLSubClassOfAxiom(c1, c2);
				man.addAxiom(ont, subc);
			}
		}
		
    	Random r2 = new Random(10);
    	for (int i =0; i<((ont.getAxiomCount()-size) *0.05);i++){
    		int rand1 = r2.nextInt(size);
			int rand2 = r2.nextInt(size);
			OWLClass c1 = classes[rand1];
			OWLClass c2 = classes[rand2];
			OWLAxiom subc = fac.getOWLSubClassOfAxiom(c2, c1);
			man.addAxiom(ont, subc);
    		
    	}
		

		log.debug("Translating into quest API");
		Ontology o = OWLAPI3TranslatorUtility.translate(ont);

		log.debug("Creating a DAG out of it");

		long start = System.nanoTime();
		log.debug("Creating a DAG out of it");
		//DAGImpl impliedDAG = DAGBuilder.getDAG(o);
		TBoxReasoner  reasoner= new TBoxReasonerImpl(o);
		reasoner.getClass();
		log.debug("{}s", ((System.nanoTime() - start)/1000000));

//		long start = System.nanoTime();
//		log.debug("Optimizing Equivalences");
//		EquivalenceTBoxOptimizer equiOptimizer = new EquivalenceTBoxOptimizer(o);
//		equiOptimizer.optimize();
//		
//		Map<Predicate, Description> equi = equiOptimizer.getEquivalenceMap();
//		log.debug("Equivalences: {}", equi.size());
//		log.debug("{}s", ((System.nanoTime() - start)/1000000));
//		log.debug("Done.");
//		System.out.println(ont);
//		System.out.println(equiOptimizer.getOptimalTBox());
	}	
}
