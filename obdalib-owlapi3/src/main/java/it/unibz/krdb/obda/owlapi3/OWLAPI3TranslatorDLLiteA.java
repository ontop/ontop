package it.unibz.krdb.obda.owlapi3;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataPropertyRangeExpression;
import it.unibz.krdb.obda.ontology.Datatype;
import it.unibz.krdb.obda.ontology.DisjointClassesAxiom;
import it.unibz.krdb.obda.ontology.DisjointPropertiesAxiom;
import it.unibz.krdb.obda.ontology.FunctionalPropertyAxiom;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;
import it.unibz.krdb.obda.ontology.PropertyAssertion;
import it.unibz.krdb.obda.ontology.PropertyExpression;
import it.unibz.krdb.obda.ontology.SomeValuesFrom;
import it.unibz.krdb.obda.ontology.SubClassOfAxiom;
import it.unibz.krdb.obda.ontology.SubPropertyOfAxiom;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.OntologyVocabularyImpl;

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
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
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
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/* 	RDFS(1), OWL2QL(2), DLLITEA(3); */

public class OWLAPI3TranslatorDLLiteA extends OWLAPI3TranslatorBase {

	
	/*
	 * If we need to construct auxiliary subclass axioms for A ISA exists R.C we
	 * put them in this map to avoid generating too many auxiliary
	 * roles/classes.
	 */
	private final Map<OWLObjectSomeValuesFrom, SomeValuesFrom> auxiliaryClassProperties 
							= new HashMap<OWLObjectSomeValuesFrom, SomeValuesFrom>();
	
	private final Map<OWLDataSomeValuesFrom, SomeValuesFrom> auxiliaryDatatypeProperties 
							= new HashMap<OWLDataSomeValuesFrom, SomeValuesFrom>();

	private int auxRoleCounter = 0;
	
	
	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

	private static final Logger log = LoggerFactory.getLogger(OWLAPI3TranslatorDLLiteA.class);
	
	
	private final Ontology dl_onto = ofac.createOntology();
	
	public OntologyVocabulary getVocabulary() {
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
	public void visit(OWLDeclarationAxiom arg0) {
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
		Set<PropertyExpression> disjointProperties = new HashSet<PropertyExpression>();
		OWLObjectPropertyExpression prop = ax.getProperty(); 
		ObjectPropertyExpression p;
		p = getPropertyExpression(prop);
		disjointProperties.add(p);
		disjointProperties.add(p.getInverse());
		
		DisjointPropertiesAxiom disj = ofac.createDisjointPropertiesAxiom(disjointProperties);				
		dl_onto.add(disj);
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
			Set<ClassExpression> disjointClasses = new HashSet<ClassExpression>();
			for (OWLClassExpression oc : ax.getClassExpressionsAsList()) {
				BasicClassDescription c = getSubclassExpression(oc);
				disjointClasses.add((ClassExpression) c);
			}
			
			DisjointClassesAxiom disj = ofac.createDisjointClassesAxiom(disjointClasses);				
			dl_onto.add(disj);
		} catch (TranslationException e) {
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
			PropertyExpression role = getPropertyExpression(ax.getProperty());
			SomeValuesFrom subclass = ofac.createPropertySomeRestriction(role);
			addSubClassAxioms(subclass, ax.getDomain().asConjunctSet());		
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
			PropertyExpression role = getPropertyExpression(ax.getProperty());
			SomeValuesFrom subclass = ofac.createPropertySomeRestriction(role);
			addSubClassAxioms(subclass, ax.getDomain().asConjunctSet());
		} catch (TranslationException e) {
			log.warn("Axiom not yet supported by Quest: {}", ax.toString());
		}
	}
	
	/**
	 * EquivalentObjectProperties := 'EquivalentObjectProperties' '(' axiomAnnotations ObjectPropertyExpression ObjectPropertyExpression 
	 * 						{ ObjectPropertyExpression } ')'
	 */

	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom ax) {

		// TODO: avoid using intermediate list 
		
		List<ObjectPropertyExpression> result = new LinkedList<ObjectPropertyExpression>();
		for (OWLObjectPropertyExpression rolExpression : ax.getProperties()) 
			result.add(getPropertyExpression(rolExpression));

		for (int i = 0; i < result.size() - 1; i++) {
			SubPropertyOfAxiom inclusion1 = ofac.createSubPropertyAxiom(result.get(i), result.get(i + 1));
			dl_onto.add(inclusion1);
		}
		SubPropertyOfAxiom inclusion1 = ofac.createSubPropertyAxiom(result.get(result.size() - 1), result.get(0));
		dl_onto.add(inclusion1);
	}

	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom ax) {
		log.warn("Axiom not yet supported by Quest: {}", ax.toString());
	}

	@Override
	public void visit(OWLDifferentIndividualsAxiom ax) {
		log.warn("Axiom not yet supported by Quest: {}", ax.toString());
	}

	@Override
	public void visit(OWLDisjointDataPropertiesAxiom ax) {
		Set<PropertyExpression> disjointProperties = new HashSet<PropertyExpression>();
		for (OWLDataPropertyExpression prop : ax.getProperties()) {
			PropertyExpression p = getPropertyExpression(prop);
			disjointProperties.add(p);
		}
		DisjointPropertiesAxiom disj = ofac.createDisjointPropertiesAxiom(disjointProperties);
		dl_onto.add(disj);		
	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom ax) {
		Set<PropertyExpression> disjointProperties = new HashSet<PropertyExpression>();
		for (OWLObjectPropertyExpression prop : ax.getProperties()) {
			PropertyExpression p = getPropertyExpression(prop);
			disjointProperties.add(p);
		}
		DisjointPropertiesAxiom disj = ofac.createDisjointPropertiesAxiom(disjointProperties);				
		dl_onto.add(disj);		
	}

	@Override
	public void visit(OWLObjectPropertyRangeAxiom ax) {
		ObjectPropertyExpression role = getPropertyExpression(ax.getProperty());
		ObjectPropertyExpression inv = role.getInverse();		
		SomeValuesFrom subclass = ofac.createPropertySomeRestriction(inv);
		
		try {
			addSubClassAxioms(subclass, ax.getRange().asConjunctSet());
		} catch (TranslationException e) {
			log.warn("Axiom not yet supported by Quest: {}", ax.toString());
		}
	}

	@Override
	public void visit(OWLObjectPropertyAssertionAxiom ax) {
		PropertyAssertion assertion = translate(ax);
		dl_onto.add(assertion);
	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom ax) {
		//if (profile.order() < LanguageProfile.OWL2QL.order())
		//	throw new TranslationException();
		
		PropertyExpression role = getPropertyExpression(ax.getProperty());
		
		FunctionalPropertyAxiom funct = ofac.createPropertyFunctionalAxiom(role);
		dl_onto.add(funct);				
	}

	@Override
	public void visit(OWLSubObjectPropertyOfAxiom ax) {
		
		ObjectPropertyExpression subrole = getPropertyExpression(ax.getSubProperty());
		ObjectPropertyExpression superrole = getPropertyExpression(ax.getSuperProperty());

		SubPropertyOfAxiom roleinc = ofac.createSubPropertyAxiom(subrole, superrole);
		dl_onto.add(roleinc);	
	}

	@Override
	public void visit(OWLDisjointUnionAxiom ax) {
		log.warn("Axiom not yet supported by Quest: {}", ax.toString());
	}

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom ax) {
		//if (profile.order() < LanguageProfile.OWL2QL.order())
		//	throw new TranslationException();
		
		OWLObjectPropertyExpression exp1 = ax.getProperty();
		ObjectPropertyExpression role = getPropertyExpression(exp1);

		SubPropertyOfAxiom symm = ofac.createSubPropertyAxiom(role, role.getInverse());
		dl_onto.add(symm);
	}

	@Override
	public void visit(OWLDataPropertyRangeAxiom ax) {

		DataPropertyExpression role = getPropertyExpression(ax.getProperty());
		DataPropertyRangeExpression subclass = ofac.createDataPropertyRange(role);

		OWLDataRange range = ax.getRange();
		
		if (range.isDatatype()) {
			OWLDatatype rangeDatatype = range.asOWLDatatype();

			if (rangeDatatype.isBuiltIn()) {
				try {
					Predicate.COL_TYPE columnType = getColumnType(rangeDatatype);
					Datatype datatype = ofac.createDataType(dfac.getTypePredicate(columnType));
					SubClassOfAxiom inc = ofac.createSubClassAxiom(subclass, datatype);
					dl_onto.add(inc);
				} catch (TranslationException e) {
					log.warn("Error in " + ax);
				}
			} else {
				log.warn("Ignoring range axiom since it refers to a non-supported datatype: " + ax);
			}
		} else {
			log.warn("Ignoring range axiom since it is not a datatype: " + ax);
		}
	}

	@Override
	public void visit(OWLFunctionalDataPropertyAxiom ax) {
		//if (profile.order() < LanguageProfile.DLLITEA.order())
		//	throw new TranslationException();
		
		PropertyExpression role = getPropertyExpression(ax.getProperty());
		FunctionalPropertyAxiom funct = ofac.createPropertyFunctionalAxiom(role);
		dl_onto.add(funct);		
	}

	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom ax) {
		//if (profile.order() < LanguageProfile.OWL2QL.order())
		//	throw new TranslationException();

		List<DataPropertyExpression> result = new LinkedList<DataPropertyExpression>();
		for (OWLDataPropertyExpression rolExpression : ax.getProperties()) 
			result.add(getPropertyExpression(rolExpression));
		
		for (int i = 0; i < result.size() - 1; i++) {
			SubPropertyOfAxiom inclusion1 = ofac.createSubPropertyAxiom(result.get(i), result.get(i + 1));
			dl_onto.add(inclusion1);
		}
		SubPropertyOfAxiom inclusion1 = ofac.createSubPropertyAxiom(result.get(result.size() - 1), result.get(0));
		dl_onto.add(inclusion1);
	}

	@Override
	public void visit(OWLClassAssertionAxiom ax) {
		ClassAssertion a = translate(ax);
		if (a != null)
			dl_onto.add(a);
	}

	@Override
	public void visit(OWLEquivalentClassesAxiom ax) {
		//if (profile.order() < LanguageProfile.OWL2QL.order())
		//	throw new TranslationException();

		try {
			Set<OWLClassExpression> equivalents = ax.getClassExpressions();		
			List<ClassExpression> result = new LinkedList<ClassExpression>();
			for (OWLClassExpression OWLClassExpression : equivalents)
				result.add(getSubclassExpression(OWLClassExpression));
			
			for (int i = 0; i < result.size() - 1; i++) {
				SubClassOfAxiom inclusion1 = ofac.createSubClassAxiom(result.get(i), result.get(i + 1));
				dl_onto.add(inclusion1);
			}
			SubClassOfAxiom inclusion1 = ofac.createSubClassAxiom(result.get(result.size() - 1), result.get(0));
			dl_onto.add(inclusion1);
			
		} catch (TranslationException e) {
			log.warn("Error in " + ax);
		}
	}

	@Override
	public void visit(OWLDataPropertyAssertionAxiom ax) {
		PropertyAssertion assertion = translate(ax);
		dl_onto.add(assertion);
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
		DataPropertyExpression subrole = getPropertyExpression(ax.getSubProperty());
		DataPropertyExpression superrole = getPropertyExpression(ax.getSuperProperty());

		SubPropertyOfAxiom roleinc = ofac.createSubPropertyAxiom(subrole, superrole);
		dl_onto.add(roleinc);	
	}

	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom ax) {
		//if (profile.order() < LanguageProfile.OWL2QL.order())
		//	throw new TranslationException();
		ObjectPropertyExpression role = getPropertyExpression(ax.getProperty());
		ObjectPropertyExpression invrole = role.getInverse();
		FunctionalPropertyAxiom funct = ofac.createPropertyFunctionalAxiom(invrole);

		dl_onto.add(funct);
	}

	@Override
	public void visit(OWLSameIndividualAxiom ax) {
		log.warn("Axiom not yet supported by Quest: {}", ax.toString());
	}

	@Override
	public void visit(OWLSubPropertyChainOfAxiom ax) {
		log.warn("Axiom not yet supported by Quest: {}", ax.toString());
	}

	@Override
	public void visit(OWLInverseObjectPropertiesAxiom ax) {
		//if (profile.order() < LanguageProfile.OWL2QL.order())
		//	throw new TranslationException();

		ObjectPropertyExpression role1 = getPropertyExpression(ax.getFirstProperty());
		ObjectPropertyExpression role2 = getPropertyExpression(ax.getSecondProperty());

		SubPropertyOfAxiom inc1 = ofac.createSubPropertyAxiom(role1, role2.getInverse());
		dl_onto.add(inc1);

		SubPropertyOfAxiom inc2 = ofac.createSubPropertyAxiom(role2, role1.getInverse());
		dl_onto.add(inc2);		
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
	
	
	
	private static ClassExpression getSubclassExpression0(OWLObjectSomeValuesFrom rest) throws TranslationException {
		OWLClassExpression filler = rest.getFiller();

		if (!filler.isOWLThing()) 
			throw new TranslationException();
		
		return ofac.createPropertySomeRestriction(getPropertyExpression(rest.getProperty()));		
	}

	private static ClassExpression getSubclassExpression0(OWLDataSomeValuesFrom rest) throws TranslationException {
		OWLDataRange filler = rest.getFiller();

		if (!filler.isTopDatatype()) 
			throw new TranslationException();
		
		return ofac.createPropertySomeRestriction(getPropertyExpression(rest.getProperty()));
	}

	private static ClassExpression getSubclassExpression0(OWLObjectMinCardinality rest) throws TranslationException {
		int cardinatlity = rest.getCardinality();
		OWLClassExpression filler = rest.getFiller();
		if (cardinatlity != 1 || !filler.isOWLThing()) 
			throw new TranslationException();
			
		return ofac.createPropertySomeRestriction(getPropertyExpression(rest.getProperty()));
	}

	private static ClassExpression getSubclassExpression0(OWLDataMinCardinality rest) throws TranslationException {
		int cardinatlity = rest.getCardinality();
		OWLDataRange range = rest.getFiller();
		if (cardinatlity != 1 || !range.isTopDatatype()) 
			throw new TranslationException();
		
		return ofac.createPropertySomeRestriction(getPropertyExpression(rest.getProperty()));
	}
	
	
	/**
	 * subClassExpression := Class | subObjectSomeValuesFrom | subObjectSomeValuesFrom
	 * 
	 * @param owlExpression
	 * @return
	 * @throws TranslationException
	 */
	
	private static ClassExpression getSubclassExpression(OWLClassExpression owlExpression) throws TranslationException {

		if (owlExpression instanceof OWLClass) {
			String uri = ((OWLClass) owlExpression).getIRI().toString();
			return ofac.createClass(uri);
		} 
		else if (owlExpression instanceof OWLObjectSomeValuesFrom) {
			//if (profile.order() < LanguageProfile.OWL2QL.order())
			//	throw new TranslationException();
			return getSubclassExpression0((OWLObjectSomeValuesFrom)owlExpression);
		} 
		else if (owlExpression instanceof OWLDataSomeValuesFrom) {
			//if (profile.order() < LanguageProfile.OWL2QL.order())
			//	throw new TranslationException();
			return getSubclassExpression0((OWLDataSomeValuesFrom) owlExpression);
		}
		else if (owlExpression instanceof OWLObjectMinCardinality) {
			//if (profile.order() < LanguageProfile.DLLITEA.order())
			//	throw new TranslationException();
			return getSubclassExpression0((OWLObjectMinCardinality) owlExpression);
		} 
		else if (owlExpression instanceof OWLDataMinCardinality) {
			//if (profile.order() < LanguageProfile.DLLITEA.order())
			//	throw new TranslationException();
			return getSubclassExpression0((OWLDataMinCardinality) owlExpression);
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
	
	private static DataPropertyExpression getPropertyExpression(OWLDataPropertyExpression rolExpression)  {
		assert (rolExpression instanceof OWLDataProperty); 
		return ofac.createDataProperty((rolExpression.asOWLDataProperty().getIRI().toString()));
	}
	
	/**
	 * ObjectPropertyExpression := ObjectProperty | InverseObjectProperty
	 * InverseObjectProperty := 'ObjectInverseOf' '(' ObjectProperty ')'
	 * 
	 * @param rolExpression
	 * @return
	 */
	
	private static ObjectPropertyExpression getPropertyExpression(OWLObjectPropertyExpression rolExpression) {

		if (rolExpression instanceof OWLObjectProperty) 
			return ofac.createObjectProperty(rolExpression.asOWLObjectProperty().getIRI().toString());
	
		else {
			assert(rolExpression instanceof OWLObjectInverseOf);
			
		//	if (profile.order() < LanguageProfile.OWL2QL.order())
		//		throw new TranslationException();
			
			OWLObjectInverseOf aux = (OWLObjectInverseOf) rolExpression;
			return ofac.createObjectProperty(aux.getInverse().asOWLObjectProperty().getIRI().toString()).getInverse();
		} 			
	}

	private void addSubClassAxioms(ClassExpression subDescription, Set<OWLClassExpression> superclasses) throws TranslationException {
		for (OWLClassExpression superClass : superclasses) {
			// We ignore owl:Thing  
			if (!superClass.isOWLThing()) {				
				BasicClassDescription superDescription; 
				
				if (superClass instanceof OWLObjectSomeValuesFrom) {
					//if (profile.order() < LanguageProfile.OWL2QL.order()) {
					//	throw new TranslationException();
					//}
					OWLObjectSomeValuesFrom someexp = (OWLObjectSomeValuesFrom) superClass;
					OWLClassExpression filler = someexp.getFiller();
					if (!(filler instanceof OWLClass)) 
						throw new TranslationException();
					
					if (filler.isOWLThing()) 
						superDescription =  getSubclassExpression0(someexp);
					else 
						superDescription = getPropertySomeClassRestriction(someexp);
				} 
				else if (superClass instanceof OWLDataSomeValuesFrom) {
					//if (profile.order() < LanguageProfile.OWL2QL.order()) {
					//	throw new TranslationException();
					//}
					OWLDataSomeValuesFrom someexp = (OWLDataSomeValuesFrom) superClass;
					OWLDataRange filler = someexp.getFiller();

					if (filler.isTopDatatype()) 
						superDescription =  getSubclassExpression0(someexp);
					
					else
						superDescription = getPropertySomeDatatypeRestriction(someexp);	
				} 
				else 
					superDescription = getSubclassExpression(superClass);
				
				SubClassOfAxiom inc = ofac.createSubClassAxiom(subDescription, superDescription);
				dl_onto.add(inc);
			}
		}	
	}
	
	

	private BasicClassDescription getPropertySomeClassRestriction(OWLObjectSomeValuesFrom someexp) throws TranslationException {
		
		SomeValuesFrom auxclass = auxiliaryClassProperties.get(someexp);
		if (auxclass == null) {
			/*
			 * no auxiliary subclass assertions found for this exists R.A,
			 * creating a new one
			 */
			
			OWLObjectPropertyExpression owlProperty = someexp.getProperty();
			OWLClassExpression owlFiller = someexp.getFiller();
			
			ObjectPropertyExpression role = getPropertyExpression(owlProperty);
			BasicClassDescription filler = getSubclassExpression(owlFiller);

			PropertyExpression auxRole = ofac.createObjectProperty(OntologyVocabularyImpl.AUXROLEURI + auxRoleCounter);
			auxRoleCounter += 1;

			// if \exists R.C then exp = P, auxclass = \exists P, P <= R, \exists P^- <= C
			// if \exists R^-.C then exp = P^-, auxclass = \exists P^-, P <= R, \exists P <= C
			
			ObjectPropertyExpression exp = ofac.createObjectProperty(auxRole.getPredicate().getName());
			if (role.isInverse())
				exp = exp.getInverse();
			
			auxclass = ofac.createPropertySomeRestriction(exp);
			auxiliaryClassProperties.put(someexp, auxclass);

			/* Creating the new subrole assertions */
			SubPropertyOfAxiom subrole = ofac.createSubPropertyAxiom(exp, role);
			dl_onto.add(subrole);
			
			/* Creating the range assertion */
			PropertyExpression expInv = exp.getInverse();
			SomeValuesFrom propertySomeRestrictionInv = ofac.createPropertySomeRestriction(expInv);
			SubClassOfAxiom subclass = ofac.createSubClassAxiom(propertySomeRestrictionInv, filler);
			dl_onto.add(subclass);
		}

		return auxclass;
	}

	private BasicClassDescription getPropertySomeDatatypeRestriction(OWLDataSomeValuesFrom someexp) throws TranslationException {
		
		SomeValuesFrom auxclass = auxiliaryDatatypeProperties.get(someexp);
		if (auxclass == null) {
			/*
			 * no auxiliary subclass assertions found for this exists R.A,
			 * creating a new one
			 */
			
			OWLDataPropertyExpression owlProperty = someexp.getProperty();
			DataPropertyExpression role = getPropertyExpression(owlProperty);

			// TODO: handle more complex fillers
			// if (filler instanceof OWLDatatype);
			OWLDatatype owlDatatype = (OWLDatatype) someexp.getFiller();
			COL_TYPE datatype = getColumnType(owlDatatype);
			Datatype filler = ofac.createDataType(dfac.getTypePredicate(datatype));
			
			DataPropertyExpression auxRole = ofac.createDataProperty(OntologyVocabularyImpl.AUXROLEURI + auxRoleCounter);
			auxRoleCounter += 1;

			DataPropertyExpression exp = ofac.createDataProperty(auxRole.getPredicate().getName());
			auxclass = ofac.createPropertySomeRestriction(exp);
			auxiliaryDatatypeProperties.put(someexp, auxclass);

			/* Creating the new subrole assertions */
			SubPropertyOfAxiom subrole = ofac.createSubPropertyAxiom(auxRole, role);
			dl_onto.add(subrole);
			
			/* Creating the range assertion */
			DataPropertyRangeExpression propertySomeRestrictionInv = ofac.createDataPropertyRange(exp);
			SubClassOfAxiom subclass = ofac.createSubClassAxiom(propertySomeRestrictionInv, filler);
			dl_onto.add(subclass);
		}

		return auxclass;
	}

	
	

	
	
	public static PropertyAssertion translate(OWLObjectPropertyAssertionAxiom ax) {
		
		URIConstant c1 = getIndividual(ax.getSubject());
		URIConstant c2 = getIndividual(ax.getObject());

		PropertyExpression prop = getPropertyExpression(ax.getProperty());

		// TODO: check for bottom			
		
		if (prop.isInverse()) 
			return ofac.createPropertyAssertion(prop.getInverse(), c2, c1);			
		else 
			return ofac.createPropertyAssertion(prop, c1, c2);						
	}
	
	
	public static PropertyAssertion translate(OWLDataPropertyAssertionAxiom aux) {
		
		try {
			OWLLiteral object = aux.getObject();
			
			Predicate.COL_TYPE type = getColumnType(object.getDatatype());
			ValueConstant c2 = dfac.getConstantLiteral(object.getLiteral(), type);

			PropertyExpression prop = getPropertyExpression(aux.getProperty());

			// TODO: CHECK FOR BOT AND TOP
			
			URIConstant c1 = getIndividual(aux.getSubject());

			return ofac.createPropertyAssertion(prop, c1, c2);
		
		} catch (TranslationException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	
	public static ClassAssertion translate(OWLClassAssertionAxiom aux) {

		OWLClassExpression classExpression = aux.getClassExpression();
		if (!(classExpression instanceof OWLClass))
			throw new RuntimeException("Found complex class in assertion, this feature is not supported");
		
		if (classExpression.isOWLThing())
			return null;
		
		if (classExpression.isOWLNothing())
			throw new RuntimeException("Unsatisfiable class assertion: " + aux);

		OWLClass namedclass = (OWLClass) classExpression;

		OClass concept = ofac.createClass(namedclass.getIRI().toString());
		URIConstant c = getIndividual(aux.getIndividual());

		return ofac.createClassAssertion(concept, c);
	}
	
	private static URIConstant getIndividual(OWLIndividual ind) {
		if (ind.isAnonymous()) 
			throw new RuntimeException("Found anonymous individual, this feature is not supported:" + ind);

		 return dfac.getConstantURI(ind.asOWLNamedIndividual().getIRI().toString());
	}
	
	
	
	// TODO: merge with OWLAPI3IndividualTranslator
	
	private static Predicate.COL_TYPE getColumnType(OWLDatatype datatype) throws TranslationException {
		if (datatype == null) {
			return COL_TYPE.LITERAL;
		}
		if (datatype.isString() || datatype.getBuiltInDatatype() == OWL2Datatype.XSD_STRING) { // xsd:string
			return COL_TYPE.STRING;
		} else if (datatype.isRDFPlainLiteral() || datatype.getBuiltInDatatype() == OWL2Datatype.RDF_PLAIN_LITERAL // rdf:PlainLiteral
				|| datatype.getBuiltInDatatype() == OWL2Datatype.RDFS_LITERAL) { // rdfs:Literal
			return COL_TYPE.LITERAL;
		} else if (datatype.isInteger()
				|| datatype.getBuiltInDatatype() == OWL2Datatype.XSD_INTEGER) {
            return COL_TYPE.INTEGER;
        } else if ( datatype.getBuiltInDatatype() == OWL2Datatype.XSD_NON_NEGATIVE_INTEGER) {
            return COL_TYPE.NON_NEGATIVE_INTEGER;
        } else if (datatype.getBuiltInDatatype() == OWL2Datatype.XSD_INT) { // xsd:int
            System.err.println(datatype.getBuiltInDatatype() + " is not in OWL2QL profile");
            return COL_TYPE.INT;
        } else if  (datatype.getBuiltInDatatype() == OWL2Datatype.XSD_POSITIVE_INTEGER){
            System.err.println(datatype.getBuiltInDatatype() + " is not in OWL2QL profile");
            return COL_TYPE.POSITIVE_INTEGER;
        } else if  (datatype.getBuiltInDatatype() == OWL2Datatype.XSD_NEGATIVE_INTEGER) {
            System.err.println(datatype.getBuiltInDatatype() + " is not in OWL2QL profile");
            return COL_TYPE.NEGATIVE_INTEGER;
        } else if  (datatype.getBuiltInDatatype() == OWL2Datatype.XSD_NON_POSITIVE_INTEGER){
            System.err.println(datatype.getBuiltInDatatype() + " is not in OWL2QL profile");
            return COL_TYPE.NON_POSITIVE_INTEGER;
        } else if  (datatype.getBuiltInDatatype() == OWL2Datatype.XSD_UNSIGNED_INT) {
            System.err.println(datatype.getBuiltInDatatype() + " is not in OWL2QL profile");
            return COL_TYPE.UNSIGNED_INT;
		} else if (datatype.getBuiltInDatatype() == OWL2Datatype.XSD_DECIMAL) { // xsd:decimal
			return Predicate.COL_TYPE.DECIMAL;
        } else if (datatype.isFloat() || datatype.isDouble() || datatype.getBuiltInDatatype() == OWL2Datatype.XSD_DOUBLE) { // xsd:double
            System.err.println(datatype.getBuiltInDatatype() + " is not in OWL2QL profile");
			return Predicate.COL_TYPE.DOUBLE;
        } else if (datatype.isFloat() || datatype.getBuiltInDatatype() == OWL2Datatype.XSD_FLOAT) { // xsd:float
            System.err.println(datatype.getBuiltInDatatype() + " is not in OWL2QL profile");
            return Predicate.COL_TYPE.FLOAT;
		} else if (datatype.getBuiltInDatatype() == OWL2Datatype.XSD_DATE_TIME || datatype.getBuiltInDatatype() == OWL2Datatype.XSD_DATE_TIME_STAMP ) {
			return Predicate.COL_TYPE.DATETIME;
        } else if (datatype.getBuiltInDatatype() == OWL2Datatype.XSD_LONG) {
            System.err.println(datatype.getBuiltInDatatype() + " is not in OWL2QL profile");
            return Predicate.COL_TYPE.LONG;
		} else if (datatype.isBoolean() || datatype.getBuiltInDatatype() == OWL2Datatype.XSD_BOOLEAN) { // xsd:boolean
            System.err.println(datatype.getBuiltInDatatype() + " is not in OWL2QL profile");
			return Predicate.COL_TYPE.BOOLEAN;
		} else {
			throw new TranslationException("Unsupported data range: " + datatype.toString());
		}
	}

	
	final Set<String> objectproperties = new HashSet<String>();
	final Set<String> dataproperties = new HashSet<String>();
	final Set<String> punnedPredicates = new HashSet<String>();
	
	@Override
	public void declare(OWLClass entity) {
		/* We ignore TOP and BOTTOM (Thing and Nothing) */
		//if (entity.isOWLThing() || entity.isOWLNothing()) 
		//	continue;				
		String uri = entity.getIRI().toString();
		dl_onto.getVocabulary().declareClass(uri);
	}

	@Override
	public void declare(OWLObjectProperty prop) {
		//if (prop.isOWLTopObjectProperty() || prop.isOWLBottomObjectProperty()) 
		//	continue;
		String uri = prop.getIRI().toString();
		if (dataproperties.contains(uri))  {
			punnedPredicates.add(uri); 
			log.warn("Quest can become unstable with properties declared as both data and object. Offending property: " + uri);
		}
		else {
			objectproperties.add(uri);
			dl_onto.getVocabulary().declareObjectProperty(uri);
		}
	}

	@Override
	public void declare(OWLDataProperty prop) {
		//if (prop.isOWLTopDataProperty() || prop.isOWLBottomDataProperty()) 
		//	continue;
		String uri = prop.getIRI().toString();
		if (objectproperties.contains(uri)) {
			punnedPredicates.add(uri);
			log.warn("Quest can become unstable with properties declared as both data and object. Offending property: " + uri);
		}
		else {
			dataproperties.add(uri);
			dl_onto.getVocabulary().declareDataProperty(uri);
		}
	}
	
	

	
	
	

	// public Predicate getDataTypePredicate(Predicate.COL_TYPE type) {
	// switch (type) {
	// case LITERAL:
	// return dfac.getDataTypePredicateLiteral();
	// case STRING:
	// return dfac.getDataTypePredicateString();
	// case INTEGER:
	// return dfac.getDataTypePredicateInteger();
	// case DECIMAL:
	// return dfac.getDataTypePredicateDecimal();
	// case DOUBLE:
	// return dfac.getDataTypePredicateDouble();
	// case DATETIME:
	// return dfac.getDataTypePredicateDateTime();
	// case BOOLEAN:
	// return dfac.getDataTypePredicateBoolean();
	// default:
	// return dfac.getDataTypePredicateLiteral();
	// }
	// }

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
