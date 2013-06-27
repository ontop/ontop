package it.unibz.krdb.obda.obda.quest.dag;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.EquivalenceTBoxOptimizer;

import java.util.Map;
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

	int size = 10000;
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
		System.out.println(ont);

		log.debug("Translating into quest API");
		OWLAPI3Translator t = new OWLAPI3Translator();
		Ontology o = t.translate(ont);

		log.debug("Creating a DAG out of it");

		long start = System.nanoTime();
		log.debug("Optimizing Equivalences");
		EquivalenceTBoxOptimizer equiOptimizer = new EquivalenceTBoxOptimizer(o);
		equiOptimizer.optimize();
		
		Map<Predicate, Description> equi = equiOptimizer.getEquivalenceMap();
		log.debug("Equivalences: {}", equi.size());
		log.debug("{}s", ((System.nanoTime() - start)/1000000000));
		log.debug("Done.");
	}
	
	/**
	 * Test the performance of classifying an ontology with 500 classes, 1000
	 * subclassAxioms and 2 roles
	 */
	public void disabledtestOnto17() throws Exception {
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
		log.debug("Translating into quest API");
		OWLAPI3Translator t = new OWLAPI3Translator();
		Ontology o = t.translate(ont);

		log.debug("Creating a DAG out of it");

		long start = System.nanoTime();
		log.debug("Optimizing Equivalences");
		EquivalenceTBoxOptimizer equiOptimizer = new EquivalenceTBoxOptimizer(o);
		equiOptimizer.optimize();

		Map<Predicate, Description> equi = equiOptimizer.getEquivalenceMap();
		log.debug("Equivalences: {}", equi.size());
		log.debug("{}s", ((System.nanoTime() - start)/1000000000));
		log.debug("Done.");
	}
	
	/**
	 * Test the performance of classifying an ontology with 500 classes, 1000
	 * subclassAxioms and 2 roles
	 */
	public void disabledtestOnto19() throws Exception {
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
		log.debug("Translating into quest API");
		OWLAPI3Translator t = new OWLAPI3Translator();
		Ontology o = t.translate(ont);

		log.debug("Creating a DAG out of it");

		long start = System.nanoTime();
		log.debug("Optimizing Equivalences");
		EquivalenceTBoxOptimizer equiOptimizer = new EquivalenceTBoxOptimizer(o);
		equiOptimizer.optimize();
		
		Map<Predicate, Description> equi = equiOptimizer.getEquivalenceMap();
		log.debug("Equivalences: {}", equi.size());
		log.debug("{}s", ((System.nanoTime() - start)/1000000000));
		log.debug("Done.");
	}	
}
