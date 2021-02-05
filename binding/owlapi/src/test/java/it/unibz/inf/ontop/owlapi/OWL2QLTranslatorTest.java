package it.unibz.inf.ontop.owlapi;


import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyImpl;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.OWLAPI_TRANSLATOR;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.*;

/**
 * Test for OWLAPI3TranslatorOWL2QL
 * 
 * @author Roman Kontchakov
 * 
 */

public class OWL2QLTranslatorTest {

	private static final String owl = "http://www.w3.org/2002/07/owl#";
	private static final String xsd = "http://www.w3.org/2001/XMLSchema#";
	private static final String rdfs = "http://www.w3.org/2000/01/rdf-schema#";

	private static final RDF rdfFactory = new SimpleRDF();
	private static final TermFactory TERM_FACTORY = OntopModelConfiguration.defaultBuilder().build().getTermFactory();
	
	@Test
	public void test_R1_2() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
		
		OWLClass class1 = factory.getOWLClass(IRI.create("http://example/A"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class1));
		OWLClass class2 = factory.getOWLClass(IRI.create("http://example/B"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class2));
		OWLClass class3 = factory.getOWLClass(IRI.create("http://example/C"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class3));
		
		manager.addAxiom(onto, factory.getOWLEquivalentClassesAxiom(class1, class2, class3));

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);
		
		Collection<BinaryAxiom<ClassExpression>> axs = dlliteonto.getSubClassAxioms();
		assertEquals(3, axs.size());
		
		for (BinaryAxiom<ClassExpression> a : axs) {
			OClass subC = (OClass)a.getSub();
			OClass superC = (OClass)a.getSuper();
			if (subC.getIRI().getIRIString().equals("http://example/A"))
				assertEquals("http://example/B", superC.getIRI().getIRIString());
			else if (subC.getIRI().getIRIString().equals("http://example/B"))
				assertEquals("http://example/C", superC.getIRI().getIRIString());
			else { 
				assertEquals("http://example/C", subC.getIRI().getIRIString());
				assertEquals("http://example/A", superC.getIRI().getIRIString());
			}
		}
	}	

	@Test
	public void test_R1_6() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
		
		OWLObjectProperty class1 = factory.getOWLObjectProperty(IRI.create("http://example/P"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class1));
		OWLObjectProperty class2 = factory.getOWLObjectProperty(IRI.create("http://example/Q"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class2));
		OWLObjectProperty class3 = factory.getOWLObjectProperty(IRI.create("http://example/R"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class3));
		
		manager.addAxiom(onto, factory.getOWLEquivalentObjectPropertiesAxiom(class1, class2, class3));

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);
		
		Collection<BinaryAxiom<ObjectPropertyExpression>> axs = dlliteonto.getSubObjectPropertyAxioms();
		assertEquals(3, axs.size());
		
		for (BinaryAxiom<ObjectPropertyExpression> a : axs) {
			ObjectPropertyExpression subC = a.getSub();
			ObjectPropertyExpression superC = a.getSuper();
			if (subC.getIRI().getIRIString().equals("http://example/P"))
				assertEquals("http://example/Q", superC.getIRI().getIRIString());
			else if (subC.getIRI().getIRIString().equals("http://example/Q"))
				assertEquals("http://example/R", superC.getIRI().getIRIString());
			else { 
				assertEquals("http://example/R", subC.getIRI().getIRIString());
				assertEquals("http://example/P", superC.getIRI().getIRIString());
			}
		}
	}	

	@Test
	public void test_R1_8() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
		
		OWLObjectProperty class1 = factory.getOWLObjectProperty(IRI.create("http://example/P"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class1));
		OWLObjectProperty class2 = factory.getOWLObjectProperty(IRI.create("http://example/Q"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class2));
		
		manager.addAxiom(onto, factory.getOWLInverseObjectPropertiesAxiom(class1, class2));

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);
		
		Collection<BinaryAxiom<ObjectPropertyExpression>> axs = dlliteonto.getSubObjectPropertyAxioms();
		assertEquals(2, axs.size());
		
		for (BinaryAxiom<ObjectPropertyExpression> a : axs) {
			ObjectPropertyExpression subC = a.getSub();
			ObjectPropertyExpression superC = a.getSuper();
			if (subC.getIRI().getIRIString().equals("http://example/P")) {
				assertEquals("http://example/Q", superC.getIRI().getIRIString());
				assertEquals(true, superC.isInverse() != subC.isInverse());
			}
			else { 
				assertEquals("http://example/Q", subC.getIRI().getIRIString());
				assertEquals("http://example/P", superC.getIRI().getIRIString());
				assertEquals(true, superC.isInverse() != subC.isInverse());
			}
		}
	}	

	@Test
	public void test_R1_17() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
		
		OWLDataProperty class1 = factory.getOWLDataProperty(IRI.create("http://example/P"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class1));
		OWLDataProperty class2 = factory.getOWLDataProperty(IRI.create("http://example/Q"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class2));
		OWLDataProperty class3 = factory.getOWLDataProperty(IRI.create("http://example/R"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class3));
		
		manager.addAxiom(onto, factory.getOWLEquivalentDataPropertiesAxiom(class1, class2, class3));

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);
		
		Collection<BinaryAxiom<DataPropertyExpression>> axs = dlliteonto.getSubDataPropertyAxioms();
		assertEquals(3, axs.size());
		
		for (BinaryAxiom<DataPropertyExpression> a : axs) {
			DataPropertyExpression subC = a.getSub();
			DataPropertyExpression superC = a.getSuper();
			if (subC.getIRI().getIRIString().equals("http://example/P"))
				assertEquals("http://example/Q", superC.getIRI().getIRIString());
			else if (subC.getIRI().getIRIString().equals("http://example/Q"))
				assertEquals("http://example/R", superC.getIRI().getIRIString());
			else { 
				assertEquals("http://example/R", subC.getIRI().getIRIString());
				assertEquals("http://example/P", superC.getIRI().getIRIString());
			}
		}
	}	
	
	@Test
	public void test_R2_9() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
		
		OWLObjectProperty class1 = factory.getOWLObjectProperty(IRI.create("http://example/P"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class1));
		OWLClass class2 = factory.getOWLClass(IRI.create("http://example/A"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class2));
		
		manager.addAxiom(onto, factory.getOWLObjectPropertyDomainAxiom(class1, class2));

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);
		
		Collection<BinaryAxiom<ClassExpression>> axs = dlliteonto.getSubClassAxioms();
		assertEquals(1, axs.size());
		
		for (BinaryAxiom<ClassExpression> a : axs) {
			ObjectSomeValuesFrom subC = (ObjectSomeValuesFrom) a.getSub();
			OClass superC = (OClass) a.getSuper();
			assertEquals("http://example/P", subC.getProperty().getIRI().getIRIString());
			assertEquals(false, subC.getProperty().isInverse());				
			assertEquals("http://example/A", superC.getIRI().getIRIString());
		}
	}	

	@Test
	public void test_R2_10() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
		
		OWLObjectProperty class1 = factory.getOWLObjectProperty(IRI.create("http://example/P"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class1));
		OWLClass class2 = factory.getOWLClass(IRI.create("http://example/A"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class2));
		
		manager.addAxiom(onto, factory.getOWLObjectPropertyRangeAxiom(class1, class2));

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);
		
		Collection<BinaryAxiom<ClassExpression>> axs = dlliteonto.getSubClassAxioms();
		assertEquals(1, axs.size());
		
		for (BinaryAxiom<ClassExpression> a : axs) {
			ObjectSomeValuesFrom subC = (ObjectSomeValuesFrom) a.getSub();
			OClass superC = (OClass) a.getSuper();
			assertEquals("http://example/P", subC.getProperty().getIRI().getIRIString());
			assertEquals(true, subC.getProperty().isInverse());				
			assertEquals("http://example/A", superC.getIRI().getIRIString());
		}
	}	

	@Test
	public void test_R2_19() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
		
		OWLDataProperty class1 = factory.getOWLDataProperty(IRI.create("http://example/P"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class1));
		OWLClass class2 = factory.getOWLClass(IRI.create("http://example/A"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class2));
		
		manager.addAxiom(onto, factory.getOWLDataPropertyDomainAxiom(class1, class2));

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);
		
		Collection<BinaryAxiom<ClassExpression>> axs = dlliteonto.getSubClassAxioms();
		assertEquals(1, axs.size());
		
		for (BinaryAxiom<ClassExpression> a : axs) {
			DataSomeValuesFrom subC = (DataSomeValuesFrom) a.getSub();
			OClass superC = (OClass) a.getSuper();
			assertEquals("http://example/P", subC.getProperty().getIRI().getIRIString());
			assertEquals("http://example/A", superC.getIRI().getIRIString());
		}
	}	
	
	@Test
	public void test_R3_13() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
		
		OWLObjectProperty class1 = factory.getOWLObjectProperty(IRI.create("http://example/P"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class1));
		
		manager.addAxiom(onto, factory.getOWLSymmetricObjectPropertyAxiom(class1));

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);
		
		Collection<BinaryAxiom<ObjectPropertyExpression>> axs = dlliteonto.getSubObjectPropertyAxioms();
		assertEquals(1, axs.size());
		
		for (BinaryAxiom<ObjectPropertyExpression> a : axs) {
			ObjectPropertyExpression subC = a.getSub();
			ObjectPropertyExpression superC = a.getSuper();
			assertEquals("http://example/P", subC.getIRI().getIRIString());
			assertEquals("http://example/P", superC.getIRI().getIRIString());
			assertEquals(true, subC.isInverse() != superC.isInverse());				
		}
	}	

	@Test
	public void test_R3_14() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
		
		OWLObjectProperty class1 = factory.getOWLObjectProperty(IRI.create("http://example/P"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class1));
		
		manager.addAxiom(onto, factory.getOWLAsymmetricObjectPropertyAxiom(class1));

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);
		
		Collection<NaryAxiom<ObjectPropertyExpression>> axs = dlliteonto.getDisjointObjectPropertiesAxioms();
		assertEquals(1, axs.size());
		
		for (NaryAxiom<ObjectPropertyExpression> a : axs) {
			assertEquals(2, a.getComponents().size());						
			Iterator<ObjectPropertyExpression> it = a.getComponents().iterator();
			ObjectPropertyExpression subC = it.next();
			ObjectPropertyExpression superC = it.next();
			assertEquals("http://example/P", subC.getIRI().getIRIString());
			assertEquals("http://example/P", superC.getIRI().getIRIString());
			assertEquals(true, subC.isInverse() != superC.isInverse());				
		}
	}	

	@Test
	public void test_R4() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
		
		OWLClass class1 = factory.getOWLClass(IRI.create("http://example/A"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class1));
		OWLClass class2 = factory.getOWLClass(IRI.create("http://example/B"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class2));
		OWLClass class3 = factory.getOWLClass(IRI.create("http://example/C"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class3));
		OWLClass class4 = factory.getOWLClass(IRI.create("http://example/D"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class4));
		
		OWLClassExpression expr = factory.getOWLObjectIntersectionOf(class2, 
				factory.getOWLObjectIntersectionOf(class3, class4));
		manager.addAxiom(onto, factory.getOWLSubClassOfAxiom(class1, expr));

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);
		
		Collection<BinaryAxiom<ClassExpression>> axs = dlliteonto.getSubClassAxioms();
		assertEquals(3, axs.size());
		Set<String> classNames = new HashSet<String>();
		
		for (BinaryAxiom<ClassExpression> a : axs) {
			OClass subC = (OClass)a.getSub();
			OClass superC = (OClass)a.getSuper();
			assertEquals("http://example/A", subC.getIRI().getIRIString());
			String name = superC.getIRI().getIRIString();
			classNames.add(name);
			assertTrue(name.equals("http://example/B") || name.equals("http://example/C") || name.equals("http://example/D"));				
		}
		assertEquals(3, classNames.size());
	}	

	@Test
	public void test_R5() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
		
		OWLClass class1 = factory.getOWLClass(IRI.create("http://example/A"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class1));
		OWLClass class2 = factory.getOWLClass(IRI.create("http://example/B"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class2));
		OWLClass class3 = factory.getOWLClass(IRI.create("http://example/C"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class3));
		OWLClass class4 = factory.getOWLClass(IRI.create("http://example/D"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class4));
		
		OWLClassExpression expr = factory.getOWLObjectIntersectionOf(class2, 
				factory.getOWLObjectIntersectionOf(factory.getOWLObjectComplementOf(class3), class4));
		manager.addAxiom(onto, factory.getOWLSubClassOfAxiom(class1, expr));

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);

		Collection<BinaryAxiom<ClassExpression>> axs = dlliteonto.getSubClassAxioms();
		assertEquals(2, axs.size());
		Set<String> classNames = new HashSet<String>();
		
		for (BinaryAxiom<ClassExpression> a : axs) {
			OClass subC = (OClass)a.getSub();
			OClass superC = (OClass)a.getSuper();
			assertEquals("http://example/A", subC.getIRI().getIRIString());
			String name = superC.getIRI().getIRIString();
			classNames.add(name);
			assertTrue(name.equals("http://example/B") || name.equals("http://example/D"));				
		}
		assertEquals(2, classNames.size());
		
		Collection<NaryAxiom<ClassExpression>> axs2 = dlliteonto.getDisjointClassesAxioms();
		assertEquals(1, axs2.size());
		
		for (NaryAxiom<ClassExpression> a : axs2) {
			assertEquals(2, a.getComponents().size());						
			Iterator<ClassExpression> it = a.getComponents().iterator();
			OClass subC = (OClass) it.next();
			OClass superC = (OClass) it.next();
			assertEquals("http://example/A", subC.getIRI().getIRIString());
			assertEquals("http://example/C", superC.getIRI().getIRIString());
		}
	}




    @Test
	public void test_R6() {
		OntologyBuilder builder = OntologyBuilderImpl.builder(rdfFactory, TERM_FACTORY);
		
		ObjectPropertyExpression top = builder.declareObjectProperty(rdfFactory.createIRI("http://www.w3.org/2002/07/owl#topObjectProperty"));
		ObjectPropertyExpression topInv = top.getInverse();
		ObjectPropertyExpression topInvInv = topInv.getInverse();
		assertTrue(topInv == topInvInv);

		ObjectPropertyExpression top2 = builder.declareObjectProperty(rdfFactory.createIRI("http://www.w3.org/2002/07/owl#topObjectProperty"));
		assertEquals(top2, topInv);
		
		ObjectPropertyExpression bot = builder.declareObjectProperty(rdfFactory.createIRI("http://www.w3.org/2002/07/owl#bottomObjectProperty"));
		ObjectPropertyExpression botInv = bot.getInverse();
		ObjectPropertyExpression botInvInv = botInv.getInverse();
		assertTrue(botInv == botInvInv);
		
		ObjectPropertyExpression bot2 = builder.declareObjectProperty(rdfFactory.createIRI("http://www.w3.org/2002/07/owl#bottomObjectProperty"));
		assertEquals(bot2, botInv);		
		
		assertFalse(bot.equals(top));
	}	
	
	@Test
	public void test_DT() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
		
		OWLDatatype dt2 = factory.getOWLDatatype(IRI.create("http://example/datatype2"));
		OWLDataRange dr2 = factory.getOWLDataIntersectionOf(factory.getIntegerOWLDatatype(), OWL2Datatype.XSD_NON_NEGATIVE_INTEGER.getDatatype(factory));
		manager.addAxiom(onto, factory.getOWLDatatypeDefinitionAxiom(dt2, dr2));

		OWLDatatype dt = factory.getOWLDatatype(IRI.create("http://example/datatype"));
		OWLDataRange dr = factory.getOWLDataIntersectionOf(factory.getIntegerOWLDatatype(), dt2);
		OWLDataRange drp = factory.getOWLDataIntersectionOf(dr, OWL2Datatype.XSD_STRING.getDatatype(factory));
		manager.addAxiom(onto, factory.getOWLDatatypeDefinitionAxiom(dt, drp));

		OWLDatatype dt3 = factory.getOWLDatatype(IRI.create("http://example/datatype3"));
		OWLDataRange dr3 = factory.getOWLDataIntersectionOf(OWL2Datatype.XSD_DECIMAL.XSD_INTEGER.getDatatype(factory), OWL2Datatype.OWL_REAL.getDatatype(factory));
		manager.addAxiom(onto, factory.getOWLDatatypeDefinitionAxiom(dt3, dr3));

		OWLDatatype dt4 = factory.getOWLDatatype(IRI.create("http://example/datatype3"));
		OWLDataRange dr4 = factory.getOWLDataIntersectionOf(OWL2Datatype.XSD_DECIMAL.XSD_INTEGER.getDatatype(factory), OWL2Datatype.XSD_BOOLEAN.getDatatype(factory));
		manager.addAxiom(onto, factory.getOWLDatatypeDefinitionAxiom(dt4, dr4));
	
		Ontology dlliteonto = OWLAPI_TRANSLATOR.translateAndClassify(onto);
	}
	
	@Test
	public void test_C1() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
		
		OWLClass class1 = factory.getOWLClass(IRI.create("http://example/A"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class1));
		OWLClass class2 = factory.getOWLClass(IRI.create("http://example/B"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class2));
		OWLClass class3 = factory.getOWLClass(IRI.create("http://example/C"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class3));
		OWLClass owlThing = factory.getOWLClass(IRI.create(owl + "Thing"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(owlThing));
		OWLClass owlNothing = factory.getOWLClass(IRI.create(owl + "Nothing"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(owlNothing));
		
		manager.addAxiom(onto, factory.getOWLSubClassOfAxiom(owlNothing, class1));
		manager.addAxiom(onto, factory.getOWLSubClassOfAxiom(class2, owlThing));
		manager.addAxiom(onto, factory.getOWLSubClassOfAxiom(class3, owlNothing));

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);
		
		Collection<BinaryAxiom<ClassExpression>> axs = dlliteonto.getSubClassAxioms();
		assertEquals(0, axs.size());
		Collection<NaryAxiom<ClassExpression>> axs1 = dlliteonto.getDisjointClassesAxioms();
		assertEquals(1, axs1.size());
		
		NaryAxiom<ClassExpression> ax = axs1.iterator().next();
		assertEquals(2, ax.getComponents().size());
		Iterator<ClassExpression> it = ax.getComponents().iterator();
		assertEquals("http://example/C", it.next().toString());
		assertEquals("http://example/C", it.next().toString());
	}	
	
	@Test
	public void test_D1() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
		
		OWLDataProperty dpe1 = factory.getOWLDataProperty(IRI.create("http://example/A"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(dpe1));
		OWLDataProperty dpe2 = factory.getOWLDataProperty(IRI.create("http://example/B"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(dpe2));
		OWLDataProperty dpe3 = factory.getOWLDataProperty(IRI.create("http://example/C"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(dpe3));
		OWLDataProperty owlTop = factory.getOWLDataProperty(IRI.create(owl + "topDataProperty"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(owlTop));
		OWLDataProperty owlBottom = factory.getOWLDataProperty(IRI.create(owl + "bottomDataProperty"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(owlBottom));
		
		manager.addAxiom(onto, factory.getOWLSubDataPropertyOfAxiom(owlBottom, dpe1));
		manager.addAxiom(onto, factory.getOWLSubDataPropertyOfAxiom(dpe2, owlTop));
		manager.addAxiom(onto, factory.getOWLSubDataPropertyOfAxiom(dpe3, owlBottom));

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);
		
		Collection<BinaryAxiom<DataPropertyExpression>> axs = dlliteonto.getSubDataPropertyAxioms();
		assertEquals(0, axs.size());
		Collection<NaryAxiom<DataPropertyExpression>> axs1 = dlliteonto.getDisjointDataPropertiesAxioms();
		assertEquals(1, axs1.size());
		
		NaryAxiom<DataPropertyExpression> ax = axs1.iterator().next();
		assertEquals(2, ax.getComponents().size());
		UnmodifiableIterator<DataPropertyExpression> it = ax.getComponents().iterator();
		assertEquals("http://example/C", it.next().toString());
		assertEquals("http://example/C", it.next().toString());
	}	
	
	@Test
	public void test_O1() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
		
		OWLObjectProperty ope1 = factory.getOWLObjectProperty(IRI.create("http://example/A"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ope1));
		OWLObjectProperty ope2 = factory.getOWLObjectProperty(IRI.create("http://example/B"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ope2));
		OWLObjectProperty ope3 = factory.getOWLObjectProperty(IRI.create("http://example/C"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ope3));
		OWLObjectProperty owlTop = factory.getOWLObjectProperty(IRI.create(owl + "topObjectProperty"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(owlTop));
		OWLObjectProperty owlBottom = factory.getOWLObjectProperty(IRI.create(owl + "bottomObjectProperty"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(owlBottom));
		
		manager.addAxiom(onto, factory.getOWLSubObjectPropertyOfAxiom(owlBottom, ope1));
		manager.addAxiom(onto, factory.getOWLSubObjectPropertyOfAxiom(ope2, owlTop));
		manager.addAxiom(onto, factory.getOWLSubObjectPropertyOfAxiom(ope3, owlBottom));

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);
		
		Collection<BinaryAxiom<ObjectPropertyExpression>> axs = dlliteonto.getSubObjectPropertyAxioms();
		assertEquals(0, axs.size());
		Collection<NaryAxiom<ObjectPropertyExpression>> axs1 = dlliteonto.getDisjointObjectPropertiesAxioms();
		assertEquals(1, axs1.size());
		
		NaryAxiom<ObjectPropertyExpression> ax = axs1.iterator().next();
		assertEquals(2, ax.getComponents().size());
		UnmodifiableIterator<ObjectPropertyExpression> it = ax.getComponents().iterator();
		assertEquals("http://example/C", it.next().toString());
		assertEquals("http://example/C", it.next().toString());
	}	

	@Test
	public void test_D2() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
		
		OWLDataProperty dpe1 = factory.getOWLDataProperty(IRI.create("http://example/A"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(dpe1));
		OWLDataProperty dpe2 = factory.getOWLDataProperty(IRI.create("http://example/B"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(dpe2));
		OWLDataProperty dpe3 = factory.getOWLDataProperty(IRI.create("http://example/C"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(dpe3));
		OWLDataProperty dpe4 = factory.getOWLDataProperty(IRI.create("http://example/D"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(dpe4));
		OWLDataProperty dpe5 = factory.getOWLDataProperty(IRI.create("http://example/E"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(dpe5));
		OWLDataProperty owlTop = factory.getOWLDataProperty(IRI.create(owl + "topDataProperty"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(owlTop));
		OWLDataProperty owlBottom = factory.getOWLDataProperty(IRI.create(owl + "bottomDataProperty"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(owlBottom));
		
		manager.addAxiom(onto, factory.getOWLDisjointDataPropertiesAxiom(owlBottom, dpe1)); // nothing
		manager.addAxiom(onto, factory.getOWLDisjointDataPropertiesAxiom(dpe2, owlTop, owlBottom)); // empty
		//manager.addAxiom(onto, factory.getOWLDisjointDataPropertiesAxiom(dpe3, owlBottom, owlTop, owlTop)); // inconsistent
		manager.addAxiom(onto, factory.getOWLDisjointDataPropertiesAxiom(dpe4, owlBottom, dpe5)); // normal

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);
		
		Collection<BinaryAxiom<DataPropertyExpression>> axs = dlliteonto.getSubDataPropertyAxioms();
		assertEquals(0, axs.size());
		Collection<NaryAxiom<DataPropertyExpression>> axs1 = dlliteonto.getDisjointDataPropertiesAxioms();
		assertEquals(2, axs1.size());
		
		Iterator<NaryAxiom<DataPropertyExpression>> axIt = axs1.iterator();
		NaryAxiom<DataPropertyExpression> ax = axIt.next();
		assertEquals(2, ax.getComponents().size()); // dpe2 (B) is empty
		Iterator<DataPropertyExpression> it = ax.getComponents().iterator();
		assertEquals("http://example/D", it.next().toString());
		assertEquals("http://example/E", it.next().toString());
		
		ax = axIt.next();
		assertEquals(2, ax.getComponents().size()); // dpe4, dpe5 (D, E) are disjoint
		it = ax.getComponents().iterator();
		assertEquals("http://example/B", it.next().toString());
		assertEquals("http://example/B", it.next().toString());
	}	

	@Test
	public void test_O2() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
		
		OWLObjectProperty dpe1 = factory.getOWLObjectProperty(IRI.create("http://example/A"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(dpe1));
		OWLObjectProperty dpe2 = factory.getOWLObjectProperty(IRI.create("http://example/B"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(dpe2));
		OWLObjectProperty dpe3 = factory.getOWLObjectProperty(IRI.create("http://example/C"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(dpe3));
		OWLObjectProperty dpe4 = factory.getOWLObjectProperty(IRI.create("http://example/D"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(dpe4));
		OWLObjectProperty dpe5 = factory.getOWLObjectProperty(IRI.create("http://example/E"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(dpe5));
		OWLObjectProperty owlTop = factory.getOWLObjectProperty(IRI.create(owl + "topObjectProperty"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(owlTop));
		OWLObjectProperty owlBottom = factory.getOWLObjectProperty(IRI.create(owl + "bottomObjectProperty"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(owlBottom));
		
		manager.addAxiom(onto, factory.getOWLDisjointObjectPropertiesAxiom(owlBottom, dpe1)); // nothing
		manager.addAxiom(onto, factory.getOWLDisjointObjectPropertiesAxiom(dpe2, owlTop, owlBottom)); // empty
		//manager.addAxiom(onto, factory.getOWLDisjointDataPropertiesAxiom(dpe3, owlBottom, owlTop, owlTop)); // inconsistent
		manager.addAxiom(onto, factory.getOWLDisjointObjectPropertiesAxiom(dpe4, owlBottom, dpe5)); // normal

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);
		
		Collection<BinaryAxiom<ObjectPropertyExpression>> axs = dlliteonto.getSubObjectPropertyAxioms();
		assertEquals(0, axs.size());
		Collection<NaryAxiom<ObjectPropertyExpression>> axs1 = dlliteonto.getDisjointObjectPropertiesAxioms();
		assertEquals(2, axs1.size());
		
		Iterator<NaryAxiom<ObjectPropertyExpression>> axIt = axs1.iterator();
		NaryAxiom<ObjectPropertyExpression> ax = axIt.next();
		assertEquals(2, ax.getComponents().size()); // dpe2 (B) is empty
		Iterator<ObjectPropertyExpression> it = ax.getComponents().iterator();
		assertEquals("http://example/D", it.next().toString());
		assertEquals("http://example/E", it.next().toString());
		
		ax = axIt.next();
		assertEquals(2, ax.getComponents().size()); // dpe4, dpe5 (D, E) are disjoint
		it = ax.getComponents().iterator();
		assertEquals("http://example/B", it.next().toString());
		assertEquals("http://example/B", it.next().toString());
	}	
	
	
	@Test
	public void test_C2() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
		
		OWLClass dpe1 = factory.getOWLClass(IRI.create("http://example/A"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(dpe1));
		OWLClass dpe2 = factory.getOWLClass(IRI.create("http://example/B"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(dpe2));
		OWLClass dpe3 = factory.getOWLClass(IRI.create("http://example/C"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(dpe3));
		OWLClass dpe4 = factory.getOWLClass(IRI.create("http://example/D"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(dpe4));
		OWLClass dpe5 = factory.getOWLClass(IRI.create("http://example/E"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(dpe5));
		OWLClass owlTop = factory.getOWLClass(IRI.create(owl + "Thing"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(owlTop));
		OWLClass owlBottom = factory.getOWLClass(IRI.create(owl + "Nothing"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(owlBottom));
		
		manager.addAxiom(onto, factory.getOWLDisjointClassesAxiom(owlBottom, dpe1)); // nothing
		manager.addAxiom(onto, factory.getOWLDisjointClassesAxiom(dpe2, owlTop, owlBottom)); // empty
		//manager.addAxiom(onto, factory.getOWLDisjointDataPropertiesAxiom(dpe3, owlBottom, owlTop, owlTop)); // inconsistent
		manager.addAxiom(onto, factory.getOWLDisjointClassesAxiom(dpe4, owlBottom, dpe5)); // normal

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);
		
		Collection<BinaryAxiom<ClassExpression>> axs = dlliteonto.getSubClassAxioms();
		assertEquals(0, axs.size());

		Collection<NaryAxiom<ClassExpression>> axs1 = dlliteonto.getDisjointClassesAxioms();
		assertEquals(2, axs1.size());
		
		axs1.iterator().forEachRemaining(ax -> {
            Set<String> clsExpressions = ax.getComponents().stream().map(Object::toString).collect(toSet());
            if (clsExpressions.size() == 1) {
                assertEquals(ImmutableSet.of("http://example/B"), clsExpressions);
            } else if (clsExpressions.size() == 2) {
                assertEquals(ImmutableSet.of("http://example/D", "http://example/E"), clsExpressions);
            } else {
                fail();
            }
        });

	}	
	
	@Test
	public void test_D3() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
		
		OWLDataProperty dpe1 = factory.getOWLDataProperty(IRI.create("http://example/A"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(dpe1));
		OWLDataProperty owlTop = factory.getOWLDataProperty(IRI.create(owl + "topDataProperty"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(owlTop));
		OWLDataProperty owlBottom = factory.getOWLDataProperty(IRI.create(owl + "bottomDataProperty"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(owlBottom));
		
		OWLDataRange integer = factory.getOWLDatatype(IRI.create(xsd + "integer"));
		OWLDataRange literal = factory.getOWLDatatype(IRI.create(rdfs + "Literal"));
		
		manager.addAxiom(onto, factory.getOWLDataPropertyRangeAxiom(owlBottom, integer)); // nothing
		manager.addAxiom(onto, factory.getOWLDataPropertyRangeAxiom(dpe1, literal)); // empty

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);
		
		Collection<BinaryAxiom<DataRangeExpression>> axs = dlliteonto.getSubDataRangeAxioms();
		assertEquals(0, axs.size());
	}	
	
	@Test
	public void test_D5() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));

		OWLClass ce1 = factory.getOWLClass(IRI.create("http://example/C"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ce1));
		
		OWLClass ce2 = factory.getOWLClass(IRI.create("http://example/D"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ce1));
		
		OWLDataProperty owlTop = factory.getOWLDataProperty(IRI.create(owl + "topDataProperty"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(owlTop));
		OWLDataProperty owlBottom = factory.getOWLDataProperty(IRI.create(owl + "bottomDataProperty"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(owlBottom));
		
		OWLDataRange literal = factory.getOWLDatatype(IRI.create(rdfs + "Literal"));
		
		manager.addAxiom(onto, factory.getOWLSubClassOfAxiom(ce1, 
							factory.getOWLDataSomeValuesFrom(owlTop, literal))); 
		manager.addAxiom(onto, factory.getOWLSubClassOfAxiom(
							factory.getOWLDataSomeValuesFrom(owlBottom, literal), ce2));

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);
		
		Collection<BinaryAxiom<ClassExpression>> axs = dlliteonto.getSubClassAxioms();
		assertEquals(0, axs.size());
		Collection<NaryAxiom<ClassExpression>> axs1 = dlliteonto.getDisjointClassesAxioms();
		assertEquals(0, axs1.size());
	}	

	
	@Test
	public void test_O3() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
		
		OWLObjectProperty owlTop = factory.getOWLObjectProperty(IRI.create(owl + "topObjectProperty"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(owlTop));
		OWLObjectProperty owlBottom = factory.getOWLObjectProperty(IRI.create(owl + "bottomObjectProperty"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(owlBottom));
		
		manager.addAxiom(onto, factory.getOWLReflexiveObjectPropertyAxiom(owlTop)); // nothing
		manager.addAxiom(onto, factory.getOWLIrreflexiveObjectPropertyAxiom(owlBottom)); // nothing

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);
		
		Collection<ObjectPropertyExpression> axs = dlliteonto.getReflexiveObjectPropertyAxioms();
		assertEquals(0, axs.size());
		Collection<ObjectPropertyExpression> axs1 = dlliteonto.getIrreflexiveObjectPropertyAxioms();
		assertEquals(0, axs1.size());

		{
			boolean flag = false;
			try {
				manager.addAxiom(onto, factory.getOWLReflexiveObjectPropertyAxiom(owlBottom));
				OWLAPI_TRANSLATOR.translateAndClassify(onto);
			}
			catch (RuntimeException e) {
				if (e.getMessage().startsWith("Incon"))
					flag = true;
			}
			assertTrue(flag);
		}
		{
			boolean flag = false;
			try {
				OWLOntology onto2 = manager.createOntology(IRI.create("http://example/testonto2"));
				manager.addAxiom(onto2, factory.getOWLIrreflexiveObjectPropertyAxiom(owlTop));
				OWLAPI_TRANSLATOR.translateAndClassify(onto);
			}
			catch (RuntimeException e) {
				if (e.getMessage().startsWith("Incon"))
					flag = true;
			}
			assertTrue(flag);
		}
	}	
	
	
	@Test
	public void test_O5() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));

		OWLClass ce1 = factory.getOWLClass(IRI.create("http://example/C"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ce1));
		OWLClass ce2 = factory.getOWLClass(IRI.create("http://example/D"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ce1));
		
		OWLObjectProperty owlTop = factory.getOWLObjectProperty(IRI.create(owl + "topObjectProperty"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(owlTop));
		OWLObjectProperty owlBottom = factory.getOWLObjectProperty(IRI.create(owl + "bottomObjectProperty"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(owlBottom));
		
		OWLClass thing = factory.getOWLClass(IRI.create(owl + "Thing"));
		
		manager.addAxiom(onto, factory.getOWLSubClassOfAxiom(ce1, 
							factory.getOWLObjectSomeValuesFrom(owlTop, thing))); 
		manager.addAxiom(onto, factory.getOWLSubClassOfAxiom(
							factory.getOWLObjectSomeValuesFrom(owlBottom, thing), ce2));

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);
		
		Collection<BinaryAxiom<ClassExpression>> axs = dlliteonto.getSubClassAxioms();
		assertEquals(0, axs.size());
		Collection<NaryAxiom<ClassExpression>> axs1 = dlliteonto.getDisjointClassesAxioms();
		assertEquals(0, axs1.size());
	}	

	@Test
	public void test_O0() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));

		OWLClass ce1 = factory.getOWLClass(IRI.create("http://example/A"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ce1));
		OWLClass ce2 = factory.getOWLClass(IRI.create("http://example/B"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ce1));
		OWLClass ce3 = factory.getOWLClass(IRI.create("http://example/C"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ce3));
		
		OWLObjectProperty ope1 = factory.getOWLObjectProperty(IRI.create("http://example/R"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ope1));
		OWLObjectProperty ope2 = factory.getOWLObjectProperty(IRI.create("http://example/S"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ope2));
		
		manager.addAxiom(onto, factory.getOWLSubClassOfAxiom(ce1, 
							factory.getOWLObjectSomeValuesFrom(factory.getOWLObjectInverseOf(ope1), ce2))); 
		manager.addAxiom(onto, factory.getOWLSubClassOfAxiom(ce3, 
				factory.getOWLObjectSomeValuesFrom(factory.getOWLObjectInverseOf(ope1), ce2)));

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);

		ObjectPropertyExpression ope = null;
		
		Collection<BinaryAxiom<ClassExpression>> axs = dlliteonto.getSubClassAxioms();
		//System.out.println(axs);
		assertEquals(3, axs.size()); // surrogates for existential restrictions are re-used
		// first pass - find ope
		for (BinaryAxiom<ClassExpression> ax : axs) {
			if (ax.getSuper().equals(dlliteonto.classes().get(rdfFactory.createIRI("http://example/B")))) {
				assertEquals(ax.getSub() instanceof ObjectSomeValuesFrom, true);
				ObjectSomeValuesFrom e = (ObjectSomeValuesFrom)ax.getSub();
				ope = e.getProperty();
			}
		}
		assertNotNull(ope);
		// second pass - verify the axioms
		for (BinaryAxiom<ClassExpression> ax : axs) {
			if (ax.getSuper().equals(dlliteonto.classes().get(rdfFactory.createIRI("http://example/B")))) {
				assertEquals(ax.getSub() instanceof ObjectSomeValuesFrom, true);
				ObjectSomeValuesFrom e = (ObjectSomeValuesFrom)ax.getSub();
				assertEquals(e.getProperty(), ope);
				assertEquals(ope.isInverse(), false);
			}
			else if (ax.getSub().equals(dlliteonto.classes().get(rdfFactory.createIRI("http://example/C")))) {
				assertEquals(ax.getSuper(), ope.getInverse().getDomain());
			}
			else {
				assertEquals(ax.getSub(), dlliteonto.classes().get(rdfFactory.createIRI("http://example/A")));
				assertEquals(ax.getSuper(), ope.getInverse().getDomain());
			}
		}
		
		Collection<BinaryAxiom<ObjectPropertyExpression>> axs1 = dlliteonto.getSubObjectPropertyAxioms();
		assertEquals(1, axs1.size());
		
		BinaryAxiom<ObjectPropertyExpression> ax1 = axs1.iterator().next();
		assertEquals(ax1.getSub(), ope.getInverse());
		assertEquals(ax1.getSuper(), dlliteonto.objectProperties().get(rdfFactory.createIRI("http://example/R")).getInverse());
	}	

	@Test
	public void test_O0nested() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));

		OWLClass ce1 = factory.getOWLClass(IRI.create("http://example/A"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ce1));
		OWLClass ce2 = factory.getOWLClass(IRI.create("http://example/B"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ce1));
		OWLClass ce3 = factory.getOWLClass(IRI.create("http://example/C"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ce3));
		OWLClass ce4 = factory.getOWLClass(IRI.create("http://example/D"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ce4));
		
		OWLObjectProperty ope1 = factory.getOWLObjectProperty(IRI.create("http://example/R"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ope1));
		OWLObjectProperty ope2 = factory.getOWLObjectProperty(IRI.create("http://example/S"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ope2));
		
		manager.addAxiom(onto, factory.getOWLSubClassOfAxiom(ce1, 
							factory.getOWLObjectSomeValuesFrom(factory.getOWLObjectInverseOf(ope1), 
									factory.getOWLObjectIntersectionOf(
											factory.getOWLObjectSomeValuesFrom(ope2, ce2), 
											factory.getOWLObjectComplementOf(ce3), ce4))));

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);

		Collection<BinaryAxiom<ClassExpression>> axs = dlliteonto.getSubClassAxioms();
		assertEquals(4, axs.size()); // surrogates for existential restrictions are re-used
		Iterator<BinaryAxiom<ClassExpression>> it = axs.iterator();

        it.forEachRemaining(ax -> {
            if (ax.getSuper().equals(dlliteonto.classes().get(rdfFactory.createIRI("http://example/B")))){
                // E AUX.ROLE1^- ISA http://example/B
                assertEquals(ax.getSub() instanceof ObjectSomeValuesFrom, true);
                ObjectPropertyExpression opep = ((ObjectSomeValuesFrom) ax.getSub()).getProperty();
                assertTrue(opep.isInverse());
            } else if (ax.getSuper().equals(dlliteonto.classes().get(rdfFactory.createIRI("http://example/D")))){
                // E AUX.ROLE0 ISA http://example/D
                ObjectPropertyExpression ope = ((ObjectSomeValuesFrom)ax.getSub()).getProperty();
                assertFalse(ope.isInverse());
            } else if (ax.getSub().equals(dlliteonto.classes().get(rdfFactory.createIRI("http://example/A")))){
                // http://example/A ISA E AUX.ROLE0^-
                ObjectPropertyExpression ope = ((ObjectSomeValuesFrom)ax.getSuper()).getProperty();
                assertTrue(ope.isInverse());
            } else {
                // E AUX.ROLE0 ISA E AUX.ROLE1
                assertEquals(ax.getSub() instanceof ObjectSomeValuesFrom, true);
                ObjectPropertyExpression ope = ((ObjectSomeValuesFrom)ax.getSub()).getProperty();
                assertFalse(ope.isInverse());
                ObjectPropertyExpression opep = ((ObjectSomeValuesFrom) ax.getSuper()).getProperty();
                assertFalse(opep.isInverse());
            }
        });


        //BinaryAxiom<ClassExpression> ax = it.next();

//        ObjectPropertyExpression ope = ((ObjectSomeValuesFrom)ax.getSub()).getProperty(); // aux
//        ObjectPropertyExpression opep = ((ObjectSomeValuesFrom) ax.getSub()).getProperty(); // aux1^-

        Collection<BinaryAxiom<ObjectPropertyExpression>> axs1 = dlliteonto.getSubObjectPropertyAxioms();
		assertEquals(2, axs1.size());

        axs1.iterator().forEachRemaining(ax -> {
            if(ax.getSuper().equals(dlliteonto.objectProperties().get(rdfFactory.createIRI("http://example/S")))){
                // AUX.ROLE1 ISA http://example/S
                assertFalse(ax.getSub().isInverse());
            } else if(ax.getSuper().equals(dlliteonto.objectProperties().get(rdfFactory.createIRI("http://example/R")).getInverse())){
                // AUX.ROLE0^- ISA http://example/R^-
                assertTrue(ax.getSub().isInverse());
            } else {
                fail();
            }
        });

//
//		Iterator<BinaryAxiom<ObjectPropertyExpression>> it1 = axs1.iterator();
//		BinaryAxiom<ObjectPropertyExpression> ax1 = it1.next();
//		assertEquals(ax1.getSub(), opep.getInverse());
//		assertEquals(ax1.getSuper(), voc.getObjectProperty("http://example/S"));
//
//		ax1 = it1.next();
//		assertEquals(ax1.getSub(), ope.getInverse());
//		assertEquals(ax1.getSuper(), voc.getObjectProperty("http://example/R").getInverse());
//
		Collection<NaryAxiom<ClassExpression>> axs2 = dlliteonto.getDisjointClassesAxioms();
		assertEquals(axs2.size(), 1);
	}	
	
	@Test
	public void test_O0min() throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));

		OWLClass ce1 = factory.getOWLClass(IRI.create("http://example/A"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ce1));
		OWLClass ce2 = factory.getOWLClass(IRI.create("http://example/B"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ce1));
		OWLClass ce3 = factory.getOWLClass(IRI.create("http://example/C"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ce3));
		
		OWLObjectProperty ope1 = factory.getOWLObjectProperty(IRI.create("http://example/R"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ope1));
		OWLObjectProperty ope2 = factory.getOWLObjectProperty(IRI.create("http://example/S"));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(ope2));
		
		manager.addAxiom(onto, factory.getOWLSubClassOfAxiom(ce1, 
							factory.getOWLObjectSomeValuesFrom(factory.getOWLObjectInverseOf(ope1), ce2))); 
		manager.addAxiom(onto, factory.getOWLSubClassOfAxiom(ce3, 
				factory.getOWLObjectMinCardinality(1, factory.getOWLObjectInverseOf(ope1), ce2)));

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);

		ObjectPropertyExpression ope = null;
		
		Collection<BinaryAxiom<ClassExpression>> axs = dlliteonto.getSubClassAxioms();
		//System.out.println(axs);
		assertEquals(3, axs.size()); // surrogates for existential restrictions are re-used
		// first pass - find ope
		for (BinaryAxiom<ClassExpression> ax : axs) {
			if (ax.getSuper().equals(dlliteonto.classes().get(rdfFactory.createIRI("http://example/B")))) {
				assertEquals(ax.getSub() instanceof ObjectSomeValuesFrom, true);
				ObjectSomeValuesFrom e = (ObjectSomeValuesFrom)ax.getSub();
				ope = e.getProperty();
			}
		}
		assertNotNull(ope);
		// second pass - verify the axioms
		for (BinaryAxiom<ClassExpression> ax : axs) {
			if (ax.getSuper().equals(dlliteonto.classes().get(rdfFactory.createIRI("http://example/B")))) {
				assertEquals(ax.getSub() instanceof ObjectSomeValuesFrom, true);
				ObjectSomeValuesFrom e = (ObjectSomeValuesFrom)ax.getSub();
				assertEquals(e.getProperty(), ope);
				assertEquals(ope.isInverse(), false);
			}
			else if (ax.getSub().equals(dlliteonto.classes().get(rdfFactory.createIRI("http://example/C")))) {
				assertEquals(ax.getSuper(), ope.getInverse().getDomain());
			}
			else {
				assertEquals(ax.getSub(), dlliteonto.classes().get(rdfFactory.createIRI("http://example/A")));
				assertEquals(ax.getSuper(), ope.getInverse().getDomain());
			}
		}
		
		Collection<BinaryAxiom<ObjectPropertyExpression>> axs1 = dlliteonto.getSubObjectPropertyAxioms();
		assertEquals(1, axs1.size());
		
		BinaryAxiom<ObjectPropertyExpression> ax1 = axs1.iterator().next();
		assertEquals(ax1.getSub(), ope.getInverse());
		assertEquals(ax1.getSuper(), dlliteonto.objectProperties().get(rdfFactory.createIRI("http://example/R")).getInverse());
	}


    @Test
    public void test_1() throws Exception{
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();

        OWLClass class1 = factory.getOWLClass(IRI.create("http://example/A"));
        OWLObjectProperty prop =  factory.getOWLObjectProperty(IRI.create("http://example/prop1"));

        OWLObjectPropertyRangeAxiom ax = factory.getOWLObjectPropertyRangeAxiom(prop, class1);

        OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
        manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class1));
        manager.addAxiom(onto, factory.getOWLDeclarationAxiom(prop));
        manager.addAxiom(onto, ax);

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);

        Collection<BinaryAxiom<ClassExpression>> ass = dlliteonto.getSubClassAxioms();
        Iterator<BinaryAxiom<ClassExpression>> assit = ass.iterator();
        assertEquals(1, ass.size());

        BinaryAxiom<ClassExpression> a = assit.next();
        ObjectSomeValuesFrom ex = (ObjectSomeValuesFrom) a.getSub();
        assertEquals(true, ex.getProperty().isInverse());
    }

    @Test
    public void test_2() throws Exception{
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();

        OWLClass class1 = factory.getOWLClass(IRI.create("http://example/A"));
        OWLObjectProperty prop =  factory.getOWLObjectProperty(IRI.create("http://example/prop1"));

        OWLObjectPropertyDomainAxiom ax = factory.getOWLObjectPropertyDomainAxiom(prop, class1);

        OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
        manager.addAxiom(onto, factory.getOWLDeclarationAxiom(class1));

        manager.addAxiom(onto, ax);

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);

        Collection<BinaryAxiom<ClassExpression>> ass = dlliteonto.getSubClassAxioms();
        Iterator<BinaryAxiom<ClassExpression>> assit = ass.iterator();
        assertEquals(1, ass.size());

        BinaryAxiom<ClassExpression> a = assit.next();
        ObjectSomeValuesFrom ex = (ObjectSomeValuesFrom) a.getSub();
        assertEquals(false, ex.getProperty().isInverse());
    }

    @Test
    public void test_3() throws Exception{
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();

        OWLObjectProperty prop =  factory.getOWLObjectProperty(IRI.create("http://example/R"));
        OWLObjectProperty invofprop =  factory.getOWLObjectProperty(IRI.create("http://example/S"));

        OWLInverseObjectPropertiesAxiom ax = factory.getOWLInverseObjectPropertiesAxiom(prop, invofprop);

        OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
        manager.addAxiom(onto, ax);

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);

        Collection<BinaryAxiom<ObjectPropertyExpression>> ass = dlliteonto.getSubObjectPropertyAxioms();
        Iterator<BinaryAxiom<ObjectPropertyExpression>> assit = ass.iterator();
        assertEquals(2, ass.size());

        BinaryAxiom<ObjectPropertyExpression> a = assit.next();
        BinaryAxiom<ObjectPropertyExpression> b = assit.next();
        ObjectPropertyExpression included =a.getSub();
        assertEquals(false, included.isInverse());
        assertEquals("http://example/R", included.getIRI().getIRIString());

        ObjectPropertyExpression indlucing = a.getSuper();
        assertEquals(true, indlucing.isInverse());
        assertEquals("http://example/S", indlucing.getIRI().getIRIString());

        included = b.getSub();
        assertEquals(false, included.isInverse());
        assertEquals("http://example/S", included.getIRI().getIRIString());

        indlucing = b.getSuper();
        assertEquals(true, indlucing.isInverse());
        assertEquals("http://example/R", indlucing.getIRI().getIRIString());
    }

    @Test
    public void test_4() throws Exception{
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();

        OWLClass clsA = factory.getOWLClass(IRI.create("http://example/A"));
        OWLClass clsB = factory.getOWLClass(IRI.create("http://example/B"));

        OWLEquivalentClassesAxiom ax = factory.getOWLEquivalentClassesAxiom(clsA, clsB);

        OWLOntology onto = manager.createOntology(IRI.create("http://example/testonto"));
        manager.addAxiom(onto, ax);

        OntologyImpl.UnclassifiedOntologyTBox dlliteonto = translateTBox(onto);

        Collection<BinaryAxiom<ClassExpression>> ass = dlliteonto.getSubClassAxioms();
        Iterator<BinaryAxiom<ClassExpression>> assit = ass.iterator();
        assertEquals(2, ass.size());

        BinaryAxiom<ClassExpression> c1 = assit.next();
        BinaryAxiom<ClassExpression> c2 = assit.next();
        OClass included = (OClass) c1.getSub();
        assertEquals("http://example/A", included.getIRI().getIRIString());

        OClass indlucing = (OClass) c1.getSuper();
        assertEquals("http://example/B", indlucing.getIRI().getIRIString());

        included = (OClass) c2.getSub();
        assertEquals("http://example/B", included.getIRI().getIRIString());

        indlucing = (OClass) c2.getSuper();
        assertEquals("http://example/A", indlucing.getIRI().getIRIString());
    }



	public static OntologyImpl.UnclassifiedOntologyTBox translateTBox(OWLOntology owl) {
		return ((OntologyImpl)OWLAPI_TRANSLATOR.translateAndClassify(owl)).unclassifiedTBox();
	}

    /**
     * USE FOR TESTS ONLY
     *
     * @param filename
     * @return
     * @throws OWLOntologyCreationException
     */

    public static ClassifiedTBox loadOntologyFromFileAndClassify(String filename) throws OWLOntologyCreationException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology owl = man.loadOntologyFromOntologyDocument(new File(filename));
        Ontology onto = OWLAPI_TRANSLATOR.translateAndClassify(owl);
        return onto.tbox();
    }

    public static org.apache.commons.rdf.api.IRI getIRI(String prefix, String suffix) {
    	return rdfFactory.createIRI(prefix + suffix);
	}
}
