package it.unibz.krdb.obda.owlapi3;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataPropertyRangeExpression;
import it.unibz.krdb.obda.ontology.DataSomeValuesFrom;
import it.unibz.krdb.obda.ontology.Datatype;
import it.unibz.krdb.obda.ontology.ImmutableOntologyVocabulary;
import it.unibz.krdb.obda.ontology.InconsistentOntologyException;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ObjectSomeValuesFrom;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;
import it.unibz.krdb.obda.ontology.impl.DatatypeImpl;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;


/* 	RDFS(1), OWL2QL(2), DLLITEA(3); */

public class OWLAPI3TranslatorDLLiteA extends OWLAPI3TranslatorBase {

	
	/*
	 * If we need to construct auxiliary subclass axioms for A ISA exists R.C we
	 * put them in this map to avoid generating too many auxiliary
	 * roles/classes.
	 */
	private final Map<OWLObjectSomeValuesFrom, ObjectSomeValuesFrom> auxiliaryClassProperties = new HashMap<>();
	
	private final Map<OWLDataSomeValuesFrom, DataSomeValuesFrom> auxiliaryDatatypeProperties = new HashMap<>();

	
	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

	private static final Logger log = LoggerFactory.getLogger(OWLAPI3TranslatorDLLiteA.class);
	
	
	private Ontology dl_onto;
	
	public ImmutableOntologyVocabulary getVocabulary() {
		return dl_onto.getVocabulary();
	}
	
	public Ontology getOntology() {
		return dl_onto;
	}
	
	@Override
	public void visit(OWLAnnotationAssertionAxiom arg0) {
		// Annotation Axioms are ignored
	}

	@Override
	public void visit(OWLSubAnnotationPropertyOfAxiom arg0) {
		// Annotation Axioms are ignored
	}

	@Override
	public void visit(OWLAnnotationPropertyDomainAxiom arg0) {
		// Annotation Axioms are ignored
	}

	@Override
	public void visit(OWLAnnotationPropertyRangeAxiom arg0) {
		// Annotation Axioms are ignored
	}

	@Override
	public void visit(OWLDeclarationAxiom ax) {
		// Declaration Axioms are ignored
	}

	/**
	 * SubClassOf := 'SubClassOf' '(' axiomAnnotations subClassExpression superClassExpression ')'
	 * subClassExpression := Class | subObjectSomeValuesFrom | subObjectSomeValuesFrom
	 * subObjectSomeValuesFrom := 'ObjectSomeValuesFrom' '(' ObjectPropertyExpression owl:Thing ')'
	 * DataSomeValuesFrom := 'DataSomeValuesFrom' '(' DataPropertyExpression { DataPropertyExpression } DataRange ')'
	 * superClassExpression := Class | superObjectIntersectionOf | superObjectComplementOf |  
	 *                         superObjectSomeValuesFrom | DataSomeValuesFrom
	 * superObjectIntersectionOf := 'ObjectIntersectionOf' '(' superClassExpression superClassExpression 
	 *                           { superClassExpression } ')'                        
	 * superObjectComplementOf := 'ObjectComplementOf' '(' subClassExpression ')'
	 * superObjectSomeValuesFrom := 'ObjectSomeValuesFrom' '(' ObjectPropertyExpression Class ')'                          
	 */
	
	@Override
	public void visit(OWLSubClassOfAxiom ax) {
		try {
			ClassExpression subDescription = getSubclassExpression(ax.getSubClass());
			addSubClassAxioms(subDescription, ax.getSuperClass().asConjunctSet());
		} catch (TranslationException e) {
			log.warn("Axiom not yet supported by Quest: {}", ax.toString());
		}
	}

	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom ax) {
		log.warn("Axiom not yet supported by Quest: {}", ax.toString());
	}

	/**
	 * AsymmetricObjectProperty :='AsymmetricObjectProperty' '(' axiomAnnotations ObjectPropertyExpression ')'
	 */
	
	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom ax) {
		ObjectPropertyExpression p = getPropertyExpression(dl_onto.getVocabulary(), ax.getProperty());
		
		// [R3] of the grammar simplifications
		Set<ObjectPropertyExpression> disjointProperties = new HashSet<>();
		disjointProperties.add(p);
		disjointProperties.add(p.getInverse());
		dl_onto.addDisjointObjectPropertiesAxiom(ImmutableList.copyOf(disjointProperties));
	}

	@Override
	public void visit(OWLReflexiveObjectPropertyAxiom ax) {
		log.warn("Axiom not yet supported by Quest: {}", ax.toString());
	}

	/**
	 * DisjointClasses := 'DisjointClasses' '(' axiomAnnotations subClassExpression subClassExpression 
	 *                     { subClassExpression } ')'
	 */
	
	@Override
	public void visit(OWLDisjointClassesAxiom ax) {
		try {
			Set<ClassExpression> disjointClasses = new HashSet<>();
			for (OWLClassExpression oc : ax.getClassExpressionsAsList()) {
				ClassExpression c = getSubclassExpression(oc);
				disjointClasses.add(c);
			}			
			dl_onto.addDisjointClassesAxiom(ImmutableList.copyOf(disjointClasses));
		} 
		catch (TranslationException e) {
			log.warn("Axiom not yet supported by Quest: {}", ax.toString());
		}
	}

	/**
	 * DataPropertyDomain := 'DataPropertyDomain' '(' axiomAnnotations DataPropertyExpression superClassExpression ')'
	 * 
	 */
	
	@Override
	public void visit(OWLDataPropertyDomainAxiom ax) {
		try {
			DataPropertyExpression role = getPropertyExpression(dl_onto.getVocabulary(), ax.getProperty());
			addSubClassAxioms(role.getDomainRestriction(DatatypeImpl.rdfsLiteral), ax.getDomain().asConjunctSet());		
		} catch (TranslationException e) {
			log.warn("Axiom not yet supported by Quest: {}", ax.toString());
		}
	}

	/**
	 * ObjectPropertyDomain := 'ObjectPropertyDomain' '(' axiomAnnotations ObjectPropertyExpression superClassExpression ')'
	 * 
	 */
	
	@Override
	public void visit(OWLObjectPropertyDomainAxiom ax) {
		try {
			ObjectPropertyExpression role = getPropertyExpression(dl_onto.getVocabulary(), ax.getProperty());
			
			// [R3] of the grammar simplifications
			addSubClassAxioms(role.getDomain(), ax.getDomain().asConjunctSet());
		} catch (TranslationException e) {
			log.warn("Axiom not yet supported by Quest: {}", ax.toString());
		}
	}
	
	/**
	 * EquivalentObjectProperties := 'EquivalentObjectProperties' '(' axiomAnnotations 
	 * 								ObjectPropertyExpression ObjectPropertyExpression { ObjectPropertyExpression } ')'
	 */

	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom ax) {

		// TODO: avoid using intermediate list 		
		List<ObjectPropertyExpression> result = new LinkedList<>();
		for (OWLObjectPropertyExpression rolExpression : ax.getProperties()) 
			result.add(getPropertyExpression(dl_onto.getVocabulary(), rolExpression));

		// [R2] from grammar simplifications		
		for (int i = 0; i < result.size() - 1; i++) 
			dl_onto.addSubPropertyOfAxiom(result.get(i), result.get(i + 1));
		
		dl_onto.addSubPropertyOfAxiom(result.get(result.size() - 1), result.get(0));
	}

	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom ax) {
		log.warn("Axiom not yet supported by Quest: {}", ax.toString());
	}

	@Override
	public void visit(OWLDifferentIndividualsAxiom ax) {
		// NO-OP: DifferentInfividuals has no effect in OWL 2 QL and DL-Lite_A
	}

	@Override
	public void visit(OWLDisjointDataPropertiesAxiom ax) {
		try {
			DataPropertyExpression[] disjointProperties = new DataPropertyExpression[ax.getProperties().size()];
			int i = 0;
			for (OWLDataPropertyExpression prop : ax.getProperties()) {
				DataPropertyExpression p = getPropertyExpression(dl_onto.getVocabulary(), prop);
				disjointProperties[i++] = p;
			}
			dl_onto.addDisjointDataPropertiesAxiom(disjointProperties);		
		}
		catch (InconsistentOntologyException e) {
			throw new RuntimeException("InconsistentOntologyException:" + ax);
		}
	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom ax) {
		Set<ObjectPropertyExpression> disjointProperties = new HashSet<>();
		for (OWLObjectPropertyExpression prop : ax.getProperties()) {
			ObjectPropertyExpression p = getPropertyExpression(dl_onto.getVocabulary(), prop);
			disjointProperties.add(p);
		}
		dl_onto.addDisjointObjectPropertiesAxiom(ImmutableList.copyOf(disjointProperties));		
	}

	/**
	 * ObjectPropertyRange := 'ObjectPropertyRange' '(' axiomAnnotations ObjectPropertyExpression superClassExpression ')'
	 */
	
	@Override
	public void visit(OWLObjectPropertyRangeAxiom ax) {
		
		try {
			ObjectPropertyExpression role = getPropertyExpression(dl_onto.getVocabulary(), ax.getProperty());
			
			// [R3] of the grammar simplifications			
			addSubClassAxioms(role.getRange(), ax.getRange().asConjunctSet());
		} catch (TranslationException e) {
			log.warn("Axiom not yet supported by Quest: {}", ax.toString());
		}
	}

	@Override
	public void visit(OWLObjectPropertyAssertionAxiom ax) {
		ObjectPropertyAssertion assertion = translate(dl_onto.getVocabulary(), ax);
		dl_onto.addObjectPropertyAssertion(assertion);
	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom ax) {
		//if (profile.order() < LanguageProfile.OWL2QL.order())
		//	throw new TranslationException();
		
		ObjectPropertyExpression role = getPropertyExpression(dl_onto.getVocabulary(), ax.getProperty());	
		dl_onto.addFunctionalObjectPropertyAxiom(role);				
	}

	@Override
	public void visit(OWLSubObjectPropertyOfAxiom ax) {
		
		ObjectPropertyExpression subrole = getPropertyExpression(dl_onto.getVocabulary(), ax.getSubProperty());
		ObjectPropertyExpression superrole = getPropertyExpression(dl_onto.getVocabulary(), ax.getSuperProperty());

		dl_onto.addSubPropertyOfAxiom(subrole, superrole);	
	}

	@Override
	public void visit(OWLDisjointUnionAxiom ax) {
		log.warn("Axiom not yet supported by Quest: {}", ax.toString());
	}
	
	/**
	 * SymmetricObjectProperty := 'SymmetricObjectProperty' '(' axiomAnnotations ObjectPropertyExpression ')'
	 */

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom ax) {
		//if (profile.order() < LanguageProfile.OWL2QL.order())
		//	throw new TranslationException();
		
		// [R3] of the grammar simplifications
		ObjectPropertyExpression role = getPropertyExpression(dl_onto.getVocabulary(), ax.getProperty());
		dl_onto.addSubPropertyOfAxiom(role, role.getInverse());
	}

	@Override
	public void visit(OWLDataPropertyRangeAxiom ax) {

		DataPropertyExpression role = getPropertyExpression(dl_onto.getVocabulary(), ax.getProperty());
		DataPropertyRangeExpression subclass = role.getRange(); // ofac.createDataPropertyRange(role);

		OWLDataRange range = ax.getRange();
		
		if (range.isDatatype()) {
			OWLDatatype rangeDatatype = range.asOWLDatatype();

			if (rangeDatatype.isBuiltIn()) {
				//Predicate.COL_TYPE columnType = OWLTypeMapper.getType(rangeDatatype);
				Datatype datatype = dl_onto.getVocabulary().getDatatype(rangeDatatype.getIRI().toString());
				dl_onto.addSubClassOfAxiom(subclass, datatype);
			} 
			else {
				log.warn("Ignoring range axiom since it refers to a non-supported datatype: " + ax);
			}
		} 
		else {
			log.warn("Ignoring range axiom since it is not a datatype: " + ax);
		}
	}

	@Override
	public void visit(OWLFunctionalDataPropertyAxiom ax) {
		//if (profile.order() < LanguageProfile.DLLITEA.order())
		//	throw new TranslationException();
		
		DataPropertyExpression role = getPropertyExpression(dl_onto.getVocabulary(), ax.getProperty());
		dl_onto.addFunctionalDataPropertyAxiom(role);		
	}

	
	/**
	 * EquivalentDataProperties := 'EquivalentDataProperties' '(' axiomAnnotations 
	 * 								DataPropertyExpression DataPropertyExpression { DataPropertyExpression } ')'
	 */
	
	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom ax) {
		//if (profile.order() < LanguageProfile.OWL2QL.order())
		//	throw new TranslationException();

		List<DataPropertyExpression> result = new LinkedList<>();
		for (OWLDataPropertyExpression rolExpression : ax.getProperties()) 
			result.add(getPropertyExpression(dl_onto.getVocabulary(), rolExpression));

		// [R2] of the grammar simplifictaions
		for (int i = 0; i < result.size() - 1; i++) 
			dl_onto.addSubPropertyOfAxiom(result.get(i), result.get(i + 1));
		
		dl_onto.addSubPropertyOfAxiom(result.get(result.size() - 1), result.get(0));
	}

	@Override
	public void visit(OWLClassAssertionAxiom ax) {
		ClassAssertion a = translate(dl_onto.getVocabulary(), ax);
		if (a != null)
			dl_onto.addClassAssertion(a);
	}

	/**
	 * EquivalentClasses := 'EquivalentClasses' '(' axiomAnnotations 
	 * 						subClassExpression subClassExpression { subClassExpression } ')'
	 */
	
	@Override
	public void visit(OWLEquivalentClassesAxiom ax) {
		//if (profile.order() < LanguageProfile.OWL2QL.order())
		//	throw new TranslationException();

		try {
			List<ClassExpression> result = new LinkedList<>();
			for (OWLClassExpression OWLClassExpression : ax.getClassExpressions())
				result.add(getSubclassExpression(OWLClassExpression));

			// [R2] from grammar simplifications
			for (int i = 0; i < result.size() - 1; i++) 
				dl_onto.addSubClassOfAxiom(result.get(i), result.get(i + 1));
			
			dl_onto.addSubClassOfAxiom(result.get(result.size() - 1), result.get(0));	
		} 
		catch (TranslationException e) {
			log.warn("Error in " + ax);
		}
	}

	@Override
	public void visit(OWLDataPropertyAssertionAxiom ax) {
		DataPropertyAssertion assertion = translate(dl_onto.getVocabulary(), ax);
		dl_onto.addDataPropertyAssertion(assertion);
	}

	@Override
	public void visit(OWLTransitiveObjectPropertyAxiom ax) {
		log.warn("Axiom not yet supported by Quest: {}", ax.toString());
	}

	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom ax) {
		log.warn("Axiom not yet supported by Quest: {}", ax.toString());
	}

	@Override
	public void visit(OWLSubDataPropertyOfAxiom ax) {
		DataPropertyExpression subrole = getPropertyExpression(dl_onto.getVocabulary(), ax.getSubProperty());
		DataPropertyExpression superrole = getPropertyExpression(dl_onto.getVocabulary(), ax.getSuperProperty());

		dl_onto.addSubPropertyOfAxiom(subrole, superrole);	
	}

	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom ax) {
		//if (profile.order() < LanguageProfile.OWL2QL.order())
		//	throw new TranslationException();
		ObjectPropertyExpression role = getPropertyExpression(dl_onto.getVocabulary(), ax.getProperty());
		dl_onto.addFunctionalObjectPropertyAxiom(role.getInverse());
	}

	@Override
	public void visit(OWLSameIndividualAxiom ax) {
		log.warn("Axiom not yet supported by Quest: {}", ax.toString());
	}

	@Override
	public void visit(OWLSubPropertyChainOfAxiom ax) {
		log.warn("Axiom not yet supported by Quest: {}", ax.toString());
	}

	/**
	 * InverseObjectProperties := 'InverseObjectProperties' '(' axiomAnnotations 
	 * 									ObjectPropertyExpression ObjectPropertyExpression ')'
	 */
	
	@Override
	public void visit(OWLInverseObjectPropertiesAxiom ax) {
		//if (profile.order() < LanguageProfile.OWL2QL.order())
		//	throw new TranslationException();

		ObjectPropertyExpression role1 = getPropertyExpression(dl_onto.getVocabulary(), ax.getFirstProperty());
		ObjectPropertyExpression role2 = getPropertyExpression(dl_onto.getVocabulary(), ax.getSecondProperty());

		// [R2] of the grammar simplifications
		
		if (!role1.equals(role2.getInverse())) {
			dl_onto.addSubPropertyOfAxiom(role1, role2.getInverse());
			dl_onto.addSubPropertyOfAxiom(role2, role1.getInverse());		
		}
	}

	@Override
	public void visit(OWLHasKeyAxiom ax) {
		log.warn("Axiom not yet supported by Quest: {}", ax.toString());
	}

	@Override
	public void visit(OWLDatatypeDefinitionAxiom ax) {
		log.warn("Axiom not yet supported by Quest: {}", ax.toString());
	}

	@Override
	public void visit(SWRLRule ax) {
		log.warn("Axiom not yet supported by Quest: {}", ax.toString());
	}


	
	/**
	 * 
	 * SERVICE METHODS
	 * 
	 */
	
	private ClassExpression getClassExpression(OWLClass rest) {
		String uri = rest.getIRI().toString();
		return dl_onto.getVocabulary().getClass(uri);		
	}
	
	private ClassExpression getClassExpression(OWLObjectSomeValuesFrom rest) throws TranslationException {
		OWLClassExpression filler = rest.getFiller();

		if (!filler.isOWLThing()) 
			throw new TranslationException();
		
		return getPropertyExpression(dl_onto.getVocabulary(), rest.getProperty()).getDomain();		
	}

	private ClassExpression getClassExpression(OWLDataSomeValuesFrom rest) throws TranslationException {
		OWLDataRange filler = rest.getFiller();

		if (!filler.isTopDatatype()) 
			throw new TranslationException();
		
		return getPropertyExpression(dl_onto.getVocabulary(), rest.getProperty()).getDomainRestriction(DatatypeImpl.rdfsLiteral);
	}

	private  ClassExpression getClassExpression(OWLObjectMinCardinality rest) throws TranslationException {
		int cardinatlity = rest.getCardinality();
		OWLClassExpression filler = rest.getFiller();
		if (cardinatlity != 1 || !filler.isOWLThing()) 
			throw new TranslationException();
			
		return getPropertyExpression(dl_onto.getVocabulary(), rest.getProperty()).getDomain();
	}

	private ClassExpression getClassExpression(OWLDataMinCardinality rest) throws TranslationException {
		int cardinatlity = rest.getCardinality();
		OWLDataRange range = rest.getFiller();
		if (cardinatlity != 1 || !range.isTopDatatype()) 
			throw new TranslationException();
		
		return getPropertyExpression(dl_onto.getVocabulary(), rest.getProperty()).getDomainRestriction(DatatypeImpl.rdfsLiteral);
	}
	
	
	/**
	 * subClassExpression := Class | subObjectSomeValuesFrom | subObjectSomeValuesFrom
	 * 
	 * @param owlExpression
	 * @return
	 * @throws TranslationException
	 */
	
	private ClassExpression getSubclassExpression(OWLClassExpression owlExpression) throws TranslationException {

		if (owlExpression instanceof OWLClass) {
			return getClassExpression((OWLClass)owlExpression);
		} 
		else if (owlExpression instanceof OWLObjectSomeValuesFrom) {
			//if (profile.order() < LanguageProfile.OWL2QL.order())
			//	throw new TranslationException();
			return getClassExpression((OWLObjectSomeValuesFrom)owlExpression);
		} 
		else if (owlExpression instanceof OWLDataSomeValuesFrom) {
			//if (profile.order() < LanguageProfile.OWL2QL.order())
			//	throw new TranslationException();
			return getClassExpression((OWLDataSomeValuesFrom) owlExpression);
		}
		else if (owlExpression instanceof OWLObjectMinCardinality) {
			//if (profile.order() < LanguageProfile.DLLITEA.order())
			//	throw new TranslationException();
			return getClassExpression((OWLObjectMinCardinality) owlExpression);
		} 
		else if (owlExpression instanceof OWLDataMinCardinality) {
			//if (profile.order() < LanguageProfile.DLLITEA.order())
			//	throw new TranslationException();
			return getClassExpression((OWLDataMinCardinality) owlExpression);
		} 
		else
			throw new TranslationException();
	}

	/**
	 * DataPropertyExpression := DataProperty
	 * 
	 * @param rolExpression
	 * @return
	 */
	
	private static DataPropertyExpression getPropertyExpression(ImmutableOntologyVocabulary voc, OWLDataPropertyExpression rolExpression)  {
		assert (rolExpression instanceof OWLDataProperty); 
		return voc.getDataProperty((rolExpression.asOWLDataProperty().getIRI().toString()));
	}
	
	/**
	 * ObjectPropertyExpression := ObjectProperty | InverseObjectProperty
	 * InverseObjectProperty := 'ObjectInverseOf' '(' ObjectProperty ')'
	 * 
	 * @param rolExpression
	 * @return
	 */
	
	private static ObjectPropertyExpression getPropertyExpression(ImmutableOntologyVocabulary voc, OWLObjectPropertyExpression rolExpression) {

		if (rolExpression instanceof OWLObjectProperty) 
			return voc.getObjectProperty(rolExpression.asOWLObjectProperty().getIRI().toString());
	
		else {
			assert(rolExpression instanceof OWLObjectInverseOf);
			
		//	if (profile.order() < LanguageProfile.OWL2QL.order())
		//		throw new TranslationException();
			
			OWLObjectInverseOf aux = (OWLObjectInverseOf) rolExpression;
			return voc.getObjectProperty(aux.getInverse().asOWLObjectProperty().getIRI().toString()).getInverse();
		} 			
	}

	
	/**
	 * superClassExpression := Class | superObjectIntersectionOf | superObjectComplementOf | 
	 * 								superObjectSomeValuesFrom | DataSomeValuesFrom
	 * 
	 * superObjectIntersectionOf := 'ObjectIntersectionOf' '(' superClassExpression superClassExpression { superClassExpression } ')'
	 * superObjectComplementOf := 'ObjectComplementOf' '(' subClassExpression ')'
	 * superObjectSomeValuesFrom := 'ObjectSomeValuesFrom' '(' ObjectPropertyExpression Class ')'
	 * DataSomeValuesFrom := 'DataSomeValuesFrom' '(' DataPropertyExpression DataRange ')'
	 */
	
	// Set<OWLClassExpression> is for [R4] in the grammar simplification
	
	private void addSubClassAxioms(ClassExpression subDescription, Set<OWLClassExpression> superclasses) throws TranslationException {
		
		for (OWLClassExpression superClass : superclasses) {
			if (superClass instanceof OWLClass) {
				dl_onto.addSubClassOfAxiom(subDescription, getClassExpression((OWLClass)superClass));
			} 
			else if (superClass instanceof OWLObjectSomeValuesFrom) {
				//if (profile.order() < LanguageProfile.OWL2QL.order()) {
				//	throw new TranslationException();
				//}
				OWLObjectSomeValuesFrom someexp = (OWLObjectSomeValuesFrom) superClass;
				OWLClassExpression filler = someexp.getFiller();

				if (filler.isOWLThing()) 
					dl_onto.addSubClassOfAxiom(subDescription, getClassExpression(someexp));
				else 
					// [R5] of the grammar simplifications
					dl_onto.addSubClassOfAxiom(subDescription, getPropertySomeClassRestriction(someexp));
			} 
			else if (superClass instanceof OWLDataSomeValuesFrom) {
				//if (profile.order() < LanguageProfile.OWL2QL.order()) {
				//	throw new TranslationException();
				//}
				OWLDataSomeValuesFrom someexp = (OWLDataSomeValuesFrom) superClass;
				OWLDataRange filler = someexp.getFiller();

				if (filler.isTopDatatype()) 
					dl_onto.addSubClassOfAxiom(subDescription, getClassExpression(someexp));
				else
					dl_onto.addSubClassOfAxiom(subDescription, getPropertySomeDatatypeRestriction(someexp));
			} 
			else if (superClass instanceof OWLObjectComplementOf) {
				// TODO: handle negation via disjointness
			}
			else if (superClass instanceof OWLObjectMinCardinality) {
				//if (profile.order() < LanguageProfile.DLLITEA.order())
				//	throw new TranslationException();
				dl_onto.addSubClassOfAxiom(subDescription, getClassExpression((OWLObjectMinCardinality) superClass));
			} 
			else if (superClass instanceof OWLDataMinCardinality) {
				//if (profile.order() < LanguageProfile.DLLITEA.order())
				//	throw new TranslationException();
				dl_onto.addSubClassOfAxiom(subDescription, getClassExpression((OWLDataMinCardinality) superClass));
			} 
			else
				throw new TranslationException();			
		}
	}
	
	
	// [R5] of the grammar simplifcation
	
	private ClassExpression getPropertySomeClassRestriction(OWLObjectSomeValuesFrom someexp) throws TranslationException {
		
		ObjectSomeValuesFrom auxclass = auxiliaryClassProperties.get(someexp);
		if (auxclass == null) {
			// no replacement found for this exists R.A, creating a new one
						
			ObjectPropertyExpression role = getPropertyExpression(dl_onto.getVocabulary(), someexp.getProperty());
			
			OWLClassExpression owlFiller = someexp.getFiller();
			if (!(owlFiller instanceof OWLClass)) 
				throw new TranslationException();
			
			ClassExpression filler = getSubclassExpression(owlFiller);

			ObjectPropertyExpression auxRole = dl_onto.createAuxiliaryObjectProperty();

			// if \exists R.C then auxRole = P, auxclass = \exists P, P <= R, \exists P^- <= C
			// if \exists R^-.C then auxRole = P^-, auxclass = \exists P^-, P^- <= R^-, \exists P <= C
			
			if (role.isInverse())
				auxRole = auxRole.getInverse();
			
			auxclass = auxRole.getDomain();
			auxiliaryClassProperties.put(someexp, auxclass);

			dl_onto.addSubPropertyOfAxiom(auxRole, role);
			dl_onto.addSubClassOfAxiom(auxRole.getRange(), filler);
		}

		return auxclass;
	}

	private ClassExpression getPropertySomeDatatypeRestriction(OWLDataSomeValuesFrom someexp) throws TranslationException {
		
		DataSomeValuesFrom auxclass = auxiliaryDatatypeProperties.get(someexp);
		if (auxclass == null) {			
			// no replacement found for this exists R.A, creating a new one
			
			DataPropertyExpression role = getPropertyExpression(dl_onto.getVocabulary(), someexp.getProperty());

			// TODO: handle more complex fillers
			// if (filler instanceof OWLDatatype);
			OWLDatatype owlDatatype = (OWLDatatype) someexp.getFiller();
			//COL_TYPE datatype = OWLTypeMapper.getType(owlDatatype);
			Datatype filler = dl_onto.getVocabulary().getDatatype(owlDatatype.getIRI().toString());
			
			DataPropertyExpression auxRole = dl_onto.createAuxiliaryDataProperty();

			auxclass = auxRole.getDomainRestriction(DatatypeImpl.rdfsLiteral); 
			auxiliaryDatatypeProperties.put(someexp, auxclass);

			dl_onto.addSubPropertyOfAxiom(auxRole, role);
			dl_onto.addSubClassOfAxiom(auxRole.getRange(), filler);
		}

		return auxclass;
	}

	
	

	
	
	public static ObjectPropertyAssertion translate(ImmutableOntologyVocabulary voc, OWLObjectPropertyAssertionAxiom ax) {
		
		try {
			URIConstant c1 = getIndividual(ax.getSubject());
			URIConstant c2 = getIndividual(ax.getObject());

			ObjectPropertyExpression prop = getPropertyExpression(voc, ax.getProperty());

			return ofac.createObjectPropertyAssertion(prop, c1, c2);
		} 
		catch (InconsistentOntologyException e) {
			throw new RuntimeException("InconsistentOntologyException: " + ax);
		}						
	}
	
	
	public static DataPropertyAssertion translate(ImmutableOntologyVocabulary voc, OWLDataPropertyAssertionAxiom aux) {
		
		try {
			OWLLiteral object = aux.getObject();
			
			Predicate.COL_TYPE type = OWLTypeMapper.getType(object.getDatatype());
			ValueConstant c2 = dfac.getConstantLiteral(object.getLiteral(), type);

			URIConstant c1 = getIndividual(aux.getSubject());

			DataPropertyExpression prop = getPropertyExpression(voc, aux.getProperty());
			
			return ofac.createDataPropertyAssertion(prop, c1, c2);
		} 
		catch (InconsistentOntologyException e) {
			throw new RuntimeException("InconsistentOntologyException: " + aux);
		}		
		catch (TranslationException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	
	public static ClassAssertion translate(ImmutableOntologyVocabulary voc, OWLClassAssertionAxiom aux) {

		try {
			OWLClassExpression classExpression = aux.getClassExpression();
			if (!(classExpression instanceof OWLClass))
				throw new RuntimeException("Found complex class in assertion, this feature is not supported");
			
			OWLClass namedclass = (OWLClass) classExpression;
			OClass concept = voc.getClass(namedclass.getIRI().toString());
			
			URIConstant c = getIndividual(aux.getIndividual());

			return ofac.createClassAssertion(concept, c);
		}
		catch (InconsistentOntologyException e) {
			throw new RuntimeException("InconsistentOntologyException: " + aux);
		}				
	}
	
	private static URIConstant getIndividual(OWLIndividual ind) {
		if (ind.isAnonymous()) 
			throw new RuntimeException("Found anonymous individual, this feature is not supported:" + ind);

		 return dfac.getConstantURI(ind.asOWLNamedIndividual().getIRI().toString());
	}
	
	
	private final Set<String> objectproperties = new HashSet<>();
	private final Set<String> dataproperties = new HashSet<>();
	private final Set<String> punnedPredicates = new HashSet<>();
		
	
	@Override
	public void prepare(OWLOntology owl) {
		
		OntologyVocabulary vb = OntologyFactoryImpl.getInstance().createVocabulary();
		
		// add all definitions for classes and roles		
		
		for (OWLClass entity : owl.getClassesInSignature())  {
			/* We ignore TOP and BOTTOM (Thing and Nothing) */
			//if (entity.isOWLThing() || entity.isOWLNothing()) 
			//	continue;				
			String uri = entity.getIRI().toString();
			vb.createClass(uri);			
		}

		for (OWLObjectProperty prop : owl.getObjectPropertiesInSignature()) {
			//if (prop.isOWLTopObjectProperty() || prop.isOWLBottomObjectProperty()) 
			//	continue;
			String uri = prop.getIRI().toString();
			if (dataproperties.contains(uri))  {
				punnedPredicates.add(uri); 
				log.warn("Quest can become unstable with properties declared as both data and object. Offending property: " + uri);
			}
			else {
				objectproperties.add(uri);
				vb.createObjectProperty(uri);
			}
		}
		
		for (OWLDataProperty prop : owl.getDataPropertiesInSignature())  {
			//if (prop.isOWLTopDataProperty() || prop.isOWLBottomDataProperty()) 
			//	continue;
			String uri = prop.getIRI().toString();
			if (objectproperties.contains(uri)) {
				punnedPredicates.add(uri);
				log.warn("Quest can become unstable with properties declared as both data and object. Offending property: " + uri);
			}
			else {
				dataproperties.add(uri);
				vb.createDataProperty(uri);
			}
		}
		dl_onto = ofac.createOntology(vb);
	}

	

	
	

	
	
	

/*		
	OWL2QLProfile owlprofile = new OWL2QLProfile();
	OWLProfileReport report = owlprofile.checkOntology(owl);
	if (!report.isInProfile()) {
		log.warn("WARNING. The current ontology is not in the OWL 2 QL profile.");
		try {
			File profileReport = new File("quest-profile-report.log");
			if (profileReport.canWrite()) {
				BufferedWriter bf = new BufferedWriter(new FileWriter(profileReport));
				bf.write(report.toString());
				bf.flush();
				bf.close();
			}
		} catch (Exception e) {

		}
	}
*/
	
	
}
