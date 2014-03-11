package it.unibz.krdb.obda.reformulation.owlapi3;

import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlapi3.SWRLTranslator;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLObjectVisitor;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.util.AutoIRIMapper;

import junit.framework.TestCase;

public class SWRLTest extends TestCase {
	private OWLOntology owlontology;
	OWLDataFactory fac;
	
	public void setUp() throws Exception {
		

		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		owlontology = man.loadOntologyFromOntologyDocument(new File("src/test/resources/test/swrl/exampleSWRL.owl"));
		fac = man.getOWLDataFactory();
	}
	
	
	@Test
	public void testInformation() {
		
		Set<OWLEntity> entities = owlontology.getSignature();
		Iterator<OWLEntity> eit = entities.iterator();

		while (eit.hasNext()) {
			OWLEntity entity = eit.next();
			System.out.println(entity);
		}
		
		
		//get the rules
		for (OWLAxiom a: owlontology.getAxioms()){
			
			if(a.getAxiomType().equals(AxiomType.SWRL_RULE)){
				
				System.out.println(a);
				SWRLRule rule =(SWRLRule) a;
				
				Set<SWRLAtom> body =rule.getBody();
				
				
				Set<SWRLAtom> head =rule.getHead();
				Set<SWRLVariable> variables= rule.getVariables();
				
				System.out.println("body: "+body);
				System.out.println("head: "+head);
				System.out.println("variables: "+ variables);
				
				for (SWRLAtom one:  body){
					System.out.println("predicate: "+one.getPredicate());
					System.out.println("arguments: "+one.getAllArguments());
				}
			}
		}
		
		
		
	}
	
	@Test
	public void testVisitor() throws Exception {
		
		SWRLTranslator trans= new SWRLTranslator(owlontology);
		trans.getDatalog();
		
		

		
	}

}
