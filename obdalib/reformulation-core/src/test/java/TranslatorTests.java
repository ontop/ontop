import java.net.URI;
import java.util.List;

import junit.framework.TestCase;

import org.obda.owlrefplatform.core.ontology.Assertion;
import org.obda.owlrefplatform.core.ontology.ConceptDescription;
import org.obda.owlrefplatform.core.ontology.DLLiterOntology;
import org.obda.owlrefplatform.core.ontology.RoleDescription;
import org.obda.owlrefplatform.core.ontology.imp.AtomicConceptDescriptionImpl;
import org.obda.owlrefplatform.core.ontology.imp.DLLiterConceptInclusionImpl;
import org.obda.owlrefplatform.core.ontology.imp.DLLiterRoleInclusionImpl;
import org.obda.owlrefplatform.core.ontology.imp.ExistentialConceptDescriptionImpl;
import org.obda.owlrefplatform.core.ontology.imp.OWLAPITranslator;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;


public class TranslatorTests extends TestCase {

	public void test_1() throws Exception{
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLClass class1 = factory.getOWLClass(URI.create("A"));
		OWLObjectProperty prop =  factory.getOWLObjectProperty(URI.create("prop1"));
		
		OWLObjectPropertyRangeAxiom ax = factory.getOWLObjectPropertyRangeAxiom(prop, class1);
		
		OWLOntology onto = manager.createOntology(URI.create("testonto"));
		manager.addAxiom(onto, ax);
		
		OWLAPITranslator translator = new OWLAPITranslator();
		DLLiterOntology dlliteonto = translator.translate(onto);
		
		List<Assertion> ass = dlliteonto.getAssertions();
		
		assertEquals(1, ass.size());
		DLLiterConceptInclusionImpl a = (DLLiterConceptInclusionImpl) ass.get(0);
		ExistentialConceptDescriptionImpl ex = (ExistentialConceptDescriptionImpl) a.getIncluded();
		assertEquals(true, ex.isInverse());
		AtomicConceptDescriptionImpl con = (AtomicConceptDescriptionImpl) a.getIncluding();
		assertEquals(false, con.isInverse());
		
	}
	
	public void test_2() throws Exception{
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLClass class1 = factory.getOWLClass(URI.create("A"));
		OWLObjectProperty prop =  factory.getOWLObjectProperty(URI.create("prop1"));
		
		OWLObjectPropertyDomainAxiom ax = factory.getOWLObjectPropertyDomainAxiom(prop, class1);
		
		OWLOntology onto = manager.createOntology(URI.create("testonto"));
		manager.addAxiom(onto, ax);
		
		OWLAPITranslator translator = new OWLAPITranslator();
		DLLiterOntology dlliteonto = translator.translate(onto);
		
		List<Assertion> ass = dlliteonto.getAssertions();
		
		assertEquals(1, ass.size());
		DLLiterConceptInclusionImpl a = (DLLiterConceptInclusionImpl) ass.get(0);
		ExistentialConceptDescriptionImpl ex = (ExistentialConceptDescriptionImpl) a.getIncluded();
		assertEquals(false, ex.isInverse());
		AtomicConceptDescriptionImpl con = (AtomicConceptDescriptionImpl) a.getIncluding();
		assertEquals(false, con.isInverse());
		
	}
	
	public void test_3() throws Exception{
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLObjectProperty prop =  factory.getOWLObjectProperty(URI.create("R"));
		OWLObjectProperty invofprop =  factory.getOWLObjectProperty(URI.create("S"));
		
		OWLInverseObjectPropertiesAxiom ax = factory.getOWLInverseObjectPropertiesAxiom(prop, invofprop);
		
		OWLOntology onto = manager.createOntology(URI.create("testonto"));
		manager.addAxiom(onto, ax);
		
		OWLAPITranslator translator = new OWLAPITranslator();
		DLLiterOntology dlliteonto = translator.translate(onto);
		
		List<Assertion> ass = dlliteonto.getAssertions();
		
		assertEquals(2, ass.size());
		DLLiterRoleInclusionImpl a = (DLLiterRoleInclusionImpl) ass.get(0);
		DLLiterRoleInclusionImpl b = (DLLiterRoleInclusionImpl) ass.get(1);
		
		
		RoleDescription included = (RoleDescription) a.getIncluded();
		assertEquals(false, included.isInverse());
		assertEquals("R", included.getPredicate().getName().toString());
		RoleDescription indlucing = (RoleDescription) a.getIncluding();
		assertEquals(true, indlucing.isInverse());
		assertEquals("S", indlucing.getPredicate().getName().toString());
		
		included = (RoleDescription) b.getIncluded();
		assertEquals(false, included.isInverse());
		assertEquals("S", included.getPredicate().getName().toString());
		indlucing = (RoleDescription) b.getIncluding();
		assertEquals(true, indlucing.isInverse());
		assertEquals("R", indlucing.getPredicate().getName().toString());
		
	}
	
	public void test_4() throws Exception{
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		OWLClass clsA = factory.getOWLClass(URI.create("A"));
		OWLClass clsB = factory.getOWLClass(URI.create("B"));
		
		OWLEquivalentClassesAxiom ax = factory.getOWLEquivalentClassesAxiom(clsA, clsB);
				
		OWLOntology onto = manager.createOntology(URI.create("testonto"));
		manager.addAxiom(onto, ax);
		
		OWLAPITranslator translator = new OWLAPITranslator();
		DLLiterOntology dlliteonto = translator.translate(onto);
		
		List<Assertion> ass = dlliteonto.getAssertions();
		
		assertEquals(2, ass.size());
		DLLiterConceptInclusionImpl c1 = (DLLiterConceptInclusionImpl) ass.get(0);
		DLLiterConceptInclusionImpl c2 = (DLLiterConceptInclusionImpl) ass.get(1);
		
		ConceptDescription included = (ConceptDescription) c1.getIncluded();
		assertEquals("A", included.getPredicate().getName().toString());
		ConceptDescription indlucing = (ConceptDescription) c1.getIncluding();
		assertEquals("B", indlucing.getPredicate().getName().toString());
		
		included = (ConceptDescription) c2.getIncluded();
		assertEquals("B", included.getPredicate().getName().toString());
		indlucing = (ConceptDescription) c2.getIncluding();
		assertEquals("A", indlucing.getPredicate().getName().toString());
		
	}
	
}
