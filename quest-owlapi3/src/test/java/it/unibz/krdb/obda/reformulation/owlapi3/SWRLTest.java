package it.unibz.krdb.obda.reformulation.owlapi3;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.owlapi3.swrl.SWRLAPITranslator;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SWRLTest extends TestCase {
	
	private OWLOntology owlontology;
	OWLDataFactory fac;
	Logger log = LoggerFactory.getLogger(SWRLTest.class);
	ArrayList<String> input= new ArrayList<String>();
	
	public void setUp() {
			
		input.add("src/test/resources/test/swrl/exampleSWRL.owl");
		input.add("src/test/resources/test/swrl/complex_example.owl");
		input.add("src/test/resources/test/swrl/propertyExample.owl");

		
	}
	
	
	@Test
	public void testInformation() throws OWLOntologyCreationException {
		
		for (int i=0; i<input.size(); i++){
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			owlontology = man.loadOntologyFromOntologyDocument(new File(input.get(i)));	
			fac = man.getOWLDataFactory();
			
		Set<OWLEntity> entities = owlontology.getSignature();
		Iterator<OWLEntity> eit = entities.iterator();

		while (eit.hasNext()) {
			OWLEntity entity = eit.next();
			log.info(entity.toString());
		}
		
		
		//get the rules
		for (OWLAxiom a: owlontology.getAxioms()){
			
			if(a.getAxiomType().equals(AxiomType.SWRL_RULE)){
				
				System.out.println(a);
				SWRLRule rule =(SWRLRule) a;
				
				Set<SWRLAtom> body =rule.getBody();

				Set<SWRLAtom> head =rule.getHead();
				Set<SWRLVariable> variables= rule.getVariables();
				
				log.info("head: "+head);
				log.info("body: "+body);
				for (SWRLAtom one:  body){
					log.info("predicate: "+one.getPredicate());
					log.info("arguments: "+one.getAllArguments());
				}
				log.info("variables: "+ variables);
				
				
			}
		}
		
		}
		
				
	}
	
	@Test
	public void testVisitorSimpleExample() throws Exception {
		
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		owlontology = man.loadOntologyFromOntologyDocument(new File(input.get(0)));	
		fac = man.getOWLDataFactory();
		
		SWRLAPITranslator trans= new SWRLAPITranslator(owlontology);
		log.info(trans.getDatalog().toString());
		List<CQIE> rules= trans.getDatalog().getRules();
		assertEquals(4,rules.size());
		
		for(CQIE rule: rules){
			log.info(rule.toString());
			assertNotNull(rule.getHead());
			assertNotNull(rule.getBody());
			
		}
		
		
		

		
	}
	
	@Test
	public void testVisitorComplexExample() throws Exception {
		
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		owlontology = man.loadOntologyFromOntologyDocument(new File(input.get(1)));	
		fac = man.getOWLDataFactory();
		
		SWRLAPITranslator trans= new SWRLAPITranslator(owlontology);
		log.info(trans.getDatalog().toString());
		List<CQIE> rules= trans.getDatalog().getRules();
		assertEquals(5,rules.size());
		
		for(CQIE rule: rules){
			log.info(rule.toString());
			assertNotNull(rule.getHead());
			assertNotNull(rule.getBody());
		}
		
		
		

		
	}
	
public void testVisitorPropertyExample() throws Exception {
		
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		owlontology = man.loadOntologyFromOntologyDocument(new File(input.get(2)));	
		fac = man.getOWLDataFactory();
		
		SWRLAPITranslator trans= new SWRLAPITranslator(owlontology);
		log.info(trans.getDatalog().toString());
		List<CQIE> rules= trans.getDatalog().getRules();
		assertEquals(2,rules.size());
		
		for(CQIE rule: rules){
			log.info(rule.toString());
			assertNotNull(rule.getHead());
			assertNotNull(rule.getBody());
		}
		
		
		

		
	}
	
	

}
