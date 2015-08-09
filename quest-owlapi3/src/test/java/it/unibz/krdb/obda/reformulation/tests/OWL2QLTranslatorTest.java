package it.unibz.krdb.obda.reformulation.tests;

/**
 * 
 * @author Roman Kontchakov
 */

import it.unibz.krdb.obda.ontology.BinaryAxiom;
import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataSomeValuesFrom;
import it.unibz.krdb.obda.ontology.NaryAxiom;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ObjectSomeValuesFrom;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OWLAPI3TranslatorUtility;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

public class OWL2QLTranslatorTest extends TestCase {

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
		
		Ontology dlliteonto = OWLAPI3TranslatorUtility.translate(onto);
		
		List<BinaryAxiom<ClassExpression>> axs = dlliteonto.getSubClassAxioms();
		assertEquals(3, axs.size());
		
		for (BinaryAxiom<ClassExpression> a : axs) {
			OClass subC = (OClass)a.getSub();
			OClass superC = (OClass)a.getSuper();
			if (subC.getPredicate().getName().equals("http://example/A"))
				assertEquals("http://example/B", superC.getPredicate().getName());				
			else if (subC.getPredicate().getName().equals("http://example/B"))
				assertEquals("http://example/C", superC.getPredicate().getName());				
			else { 
				assertEquals("http://example/C", subC.getPredicate().getName());				
				assertEquals("http://example/A", superC.getPredicate().getName());				
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
		
		Ontology dlliteonto = OWLAPI3TranslatorUtility.translate(onto);
		
		List<BinaryAxiom<ObjectPropertyExpression>> axs = dlliteonto.getSubObjectPropertyAxioms();
		assertEquals(3, axs.size());
		
		for (BinaryAxiom<ObjectPropertyExpression> a : axs) {
			ObjectPropertyExpression subC = a.getSub();
			ObjectPropertyExpression superC = a.getSuper();
			if (subC.getPredicate().getName().equals("http://example/P"))
				assertEquals("http://example/Q", superC.getPredicate().getName());				
			else if (subC.getPredicate().getName().equals("http://example/Q"))
				assertEquals("http://example/R", superC.getPredicate().getName());				
			else { 
				assertEquals("http://example/R", subC.getPredicate().getName());				
				assertEquals("http://example/P", superC.getPredicate().getName());				
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
		
		Ontology dlliteonto = OWLAPI3TranslatorUtility.translate(onto);
		
		List<BinaryAxiom<ObjectPropertyExpression>> axs = dlliteonto.getSubObjectPropertyAxioms();
		assertEquals(2, axs.size());
		
		for (BinaryAxiom<ObjectPropertyExpression> a : axs) {
			ObjectPropertyExpression subC = a.getSub();
			ObjectPropertyExpression superC = a.getSuper();
			if (subC.getPredicate().getName().equals("http://example/P")) {
				assertEquals("http://example/Q", superC.getPredicate().getName());
				assertEquals(true, superC.isInverse() != subC.isInverse());
			}
			else { 
				assertEquals("http://example/Q", subC.getPredicate().getName());				
				assertEquals("http://example/P", superC.getPredicate().getName());				
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
		
		Ontology dlliteonto = OWLAPI3TranslatorUtility.translate(onto);
		
		List<BinaryAxiom<DataPropertyExpression>> axs = dlliteonto.getSubDataPropertyAxioms();
		assertEquals(3, axs.size());
		
		for (BinaryAxiom<DataPropertyExpression> a : axs) {
			DataPropertyExpression subC = a.getSub();
			DataPropertyExpression superC = a.getSuper();
			if (subC.getPredicate().getName().equals("http://example/P"))
				assertEquals("http://example/Q", superC.getPredicate().getName());				
			else if (subC.getPredicate().getName().equals("http://example/Q"))
				assertEquals("http://example/R", superC.getPredicate().getName());				
			else { 
				assertEquals("http://example/R", subC.getPredicate().getName());				
				assertEquals("http://example/P", superC.getPredicate().getName());				
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
		
		Ontology dlliteonto = OWLAPI3TranslatorUtility.translate(onto);
		
		List<BinaryAxiom<ClassExpression>> axs = dlliteonto.getSubClassAxioms();
		assertEquals(1, axs.size());
		
		for (BinaryAxiom<ClassExpression> a : axs) {
			ObjectSomeValuesFrom subC = (ObjectSomeValuesFrom) a.getSub();
			OClass superC = (OClass) a.getSuper();
			assertEquals("http://example/P", subC.getProperty().getPredicate().getName());				
			assertEquals(false, subC.getProperty().isInverse());				
			assertEquals("http://example/A", superC.getPredicate().getName());				
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
		
		Ontology dlliteonto = OWLAPI3TranslatorUtility.translate(onto);
		
		List<BinaryAxiom<ClassExpression>> axs = dlliteonto.getSubClassAxioms();
		assertEquals(1, axs.size());
		
		for (BinaryAxiom<ClassExpression> a : axs) {
			ObjectSomeValuesFrom subC = (ObjectSomeValuesFrom) a.getSub();
			OClass superC = (OClass) a.getSuper();
			assertEquals("http://example/P", subC.getProperty().getPredicate().getName());				
			assertEquals(true, subC.getProperty().isInverse());				
			assertEquals("http://example/A", superC.getPredicate().getName());				
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
		
		Ontology dlliteonto = OWLAPI3TranslatorUtility.translate(onto);
		
		List<BinaryAxiom<ClassExpression>> axs = dlliteonto.getSubClassAxioms();
		assertEquals(1, axs.size());
		
		for (BinaryAxiom<ClassExpression> a : axs) {
			DataSomeValuesFrom subC = (DataSomeValuesFrom) a.getSub();
			OClass superC = (OClass) a.getSuper();
			assertEquals("http://example/P", subC.getProperty().getPredicate().getName());				
			assertEquals("http://example/A", superC.getPredicate().getName());				
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
		
		Ontology dlliteonto = OWLAPI3TranslatorUtility.translate(onto);
		
		List<BinaryAxiom<ObjectPropertyExpression>> axs = dlliteonto.getSubObjectPropertyAxioms();
		assertEquals(1, axs.size());
		
		for (BinaryAxiom<ObjectPropertyExpression> a : axs) {
			ObjectPropertyExpression subC = a.getSub();
			ObjectPropertyExpression superC = a.getSuper();
			assertEquals("http://example/P", subC.getPredicate().getName());				
			assertEquals("http://example/P", superC.getPredicate().getName());				
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
		
		Ontology dlliteonto = OWLAPI3TranslatorUtility.translate(onto);
		
		List<NaryAxiom<ObjectPropertyExpression>> axs = dlliteonto.getDisjointObjectPropertiesAxioms();
		assertEquals(1, axs.size());
		
		for (NaryAxiom<ObjectPropertyExpression> a : axs) {
			assertEquals(2, a.getComponents().size());						
			Iterator<ObjectPropertyExpression> it = a.getComponents().iterator();
			ObjectPropertyExpression subC = it.next();
			ObjectPropertyExpression superC = it.next();
			assertEquals("http://example/P", subC.getPredicate().getName());				
			assertEquals("http://example/P", superC.getPredicate().getName());				
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
		
		Ontology dlliteonto = OWLAPI3TranslatorUtility.translate(onto);
		
		List<BinaryAxiom<ClassExpression>> axs = dlliteonto.getSubClassAxioms();
		assertEquals(3, axs.size());
		Set<String> classNames = new HashSet<String>();
		
		for (BinaryAxiom<ClassExpression> a : axs) {
			OClass subC = (OClass)a.getSub();
			OClass superC = (OClass)a.getSuper();
			assertEquals("http://example/A", subC.getPredicate().getName());
			String name = superC.getPredicate().getName();
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
		
		Ontology dlliteonto = OWLAPI3TranslatorUtility.translate(onto);
		
		List<BinaryAxiom<ClassExpression>> axs = dlliteonto.getSubClassAxioms();
		assertEquals(2, axs.size());
		Set<String> classNames = new HashSet<String>();
		
		for (BinaryAxiom<ClassExpression> a : axs) {
			OClass subC = (OClass)a.getSub();
			OClass superC = (OClass)a.getSuper();
			assertEquals("http://example/A", subC.getPredicate().getName());
			String name = superC.getPredicate().getName();
			classNames.add(name);
			assertTrue(name.equals("http://example/B") || name.equals("http://example/D"));				
		}
		assertEquals(2, classNames.size());
		
		List<NaryAxiom<ClassExpression>> axs2 = dlliteonto.getDisjointClassesAxioms();
		assertEquals(1, axs2.size());
		
		for (NaryAxiom<ClassExpression> a : axs2) {
			assertEquals(2, a.getComponents().size());						
			Iterator<ClassExpression> it = a.getComponents().iterator();
			OClass subC = (OClass) it.next();
			OClass superC = (OClass) it.next();
			assertEquals("http://example/A", subC.getPredicate().getName());				
			assertEquals("http://example/C", superC.getPredicate().getName());				
		}
	}	
	

	@Test
	public void test_R6() throws Exception {
		OntologyFactory factory = OntologyFactoryImpl.getInstance(); 
		
		ObjectPropertyExpression top = factory.createObjectProperty("http://www.w3.org/2002/07/owl#topObjectProperty");
		ObjectPropertyExpression topInv = top.getInverse();
		ObjectPropertyExpression topInvInv = topInv.getInverse();
		assertTrue(topInv == topInvInv);

		ObjectPropertyExpression top2 = factory.createObjectProperty("http://www.w3.org/2002/07/owl#topObjectProperty");
		assertEquals(top2, topInv);
		
		ObjectPropertyExpression bot = factory.createObjectProperty("http://www.w3.org/2002/07/owl#bottomObjectProperty");
		ObjectPropertyExpression botInv = bot.getInverse();
		ObjectPropertyExpression botInvInv = botInv.getInverse();
		assertTrue(botInv == botInvInv);
		
		ObjectPropertyExpression bot2 = factory.createObjectProperty("http://www.w3.org/2002/07/owl#bottomObjectProperty");
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
	
		Ontology dlliteonto = OWLAPI3TranslatorUtility.translate(onto);		
	}
	
}
