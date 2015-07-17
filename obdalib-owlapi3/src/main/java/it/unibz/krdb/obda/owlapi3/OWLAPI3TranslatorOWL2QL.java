package it.unibz.krdb.obda.owlapi3;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.*;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * 
 * @author Roman Kontchakov
 */

public class OWLAPI3TranslatorOWL2QL extends OWLAPI3TranslatorBase {

	
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
	public void visit(OWLDeclarationAxiom ax) {
		// Declaration Axioms are ignored
	}

	/**
	 * (1)
	 * 
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
			addSubClassAxioms(subDescription, ax.getSuperClass());
		} catch (TranslationException e) {
			log.warn("Axiom not yet supported by Quest: {}", ax.toString());
		}
	}

	
	/**
	 * (2)
	 * 
	 * EquivalentClasses := 'EquivalentClasses' '(' axiomAnnotations 
	 * 						subClassExpression subClassExpression { subClassExpression } ')'
	 * 
	 * replaced by SubClassOfAxiom (rule [R1])
	 */
	
	@Override
	public void visit(OWLEquivalentClassesAxiom ax) {
		try {
			Iterator<OWLClassExpression> it = ax.getClassExpressions().iterator();
			ClassExpression first = getSubclassExpression(it.next());
			ClassExpression previous = first;
			while (it.hasNext()) {
				ClassExpression current = getSubclassExpression(it.next());
				dl_onto.addSubClassOfAxiom(previous, current);
				previous = current;
			}
			dl_onto.addSubClassOfAxiom(previous, first);
		} 
		catch (TranslationException e) {
			log.warn("Error in OWL 2 QL axiom: {} ({})", ax, e);
		}
	}
	

	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom ax) {
		log.warn("Axiom not yet supported by Quest: {}", ax.toString());
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
			dl_onto.addDisjointClassesAxiom(ImmutableSet.copyOf(disjointClasses));
		} 
		catch (TranslationException e) {
			log.warn("Axiom not yet supported by Quest: {}", ax.toString());
		}
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
		Set<DataPropertyExpression> disjointProperties = new HashSet<>();
		for (OWLDataPropertyExpression prop : ax.getProperties()) {
			DataPropertyExpression p = getPropertyExpression(prop);
			disjointProperties.add(p);
		}
		dl_onto.addDisjointDataPropertiesAxiom(ImmutableSet.copyOf(disjointProperties));		
	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom ax) {
		Set<ObjectPropertyExpression> disjointProperties = new HashSet<>();
		for (OWLObjectPropertyExpression prop : ax.getProperties()) {
			ObjectPropertyExpression p = getPropertyExpression(prop);
			disjointProperties.add(p);
		}
		dl_onto.addDisjointObjectPropertiesAxiom(ImmutableSet.copyOf(disjointProperties));		
	}


	@Override
	public void visit(OWLObjectPropertyAssertionAxiom ax) {
		ObjectPropertyAssertion assertion = translate(ax);
		dl_onto.addObjectPropertyAssertion(assertion);
	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom ax) {
		//if (profile.order() < LanguageProfile.OWL2QL.order())
		//	throw new TranslationException();
		
		ObjectPropertyExpression role = getPropertyExpression(ax.getProperty());	
		dl_onto.addFunctionalObjectPropertyAxiom(role);				
	}

	/**
	 * (5)
	 * 
	 * SubObjectPropertyOf := 'SubObjectPropertyOf' '(' axiomAnnotations 
	 * 												ObjectPropertyExpression ObjectPropertyExpression ')'
	 * ObjectPropertyExpression := ObjectProperty | InverseObjectProperty
	 * InverseObjectProperty := 'ObjectInverseOf' '(' ObjectProperty ')'
	 */
	
	@Override
	public void visit(OWLSubObjectPropertyOfAxiom ax) {
		
		ObjectPropertyExpression subrole = getPropertyExpression(ax.getSubProperty());
		ObjectPropertyExpression superrole = getPropertyExpression(ax.getSuperProperty());

		dl_onto.addSubPropertyOfAxiom(subrole, superrole);	
	}

	/**
	 * (6)
	 * 
	 * EquivalentObjectProperties := 'EquivalentObjectProperties' '(' axiomAnnotations 
	 * 								ObjectPropertyExpression ObjectPropertyExpression { ObjectPropertyExpression } ')'
	 * 
	 * replaced by SubObjectPropertyOfAxiom (rule [R1])
	 */

	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom ax) {
		Iterator<OWLObjectPropertyExpression> it = ax.getProperties().iterator();
		ObjectPropertyExpression first = getPropertyExpression(it.next());
		ObjectPropertyExpression previous = first;
		while (it.hasNext()) {
			ObjectPropertyExpression current = getPropertyExpression(it.next());
			dl_onto.addSubPropertyOfAxiom(previous, current);
			previous = current;
		}
		dl_onto.addSubPropertyOfAxiom(previous, first);
	}
	
	/**
	 * (8)
	 * 
	 * InverseObjectProperties := 'InverseObjectProperties' '(' axiomAnnotations 
	 * 									ObjectPropertyExpression ObjectPropertyExpression ')'
	 * 
	 * replaced by SubObjectPropertyOfAxiom (rule [R1])
	 */
	
	@Override
	public void visit(OWLInverseObjectPropertiesAxiom ax) {		
		ObjectPropertyExpression role1 = getPropertyExpression(ax.getFirstProperty());
		ObjectPropertyExpression role2 = getPropertyExpression(ax.getSecondProperty());

		dl_onto.addSubPropertyOfAxiom(role1, role2.getInverse());
		dl_onto.addSubPropertyOfAxiom(role2, role1.getInverse());		
	}
	
	/**
	 * (9)
	 * 
	 * ObjectPropertyDomain := 'ObjectPropertyDomain' '(' axiomAnnotations ObjectPropertyExpression superClassExpression ')'
	 * 
	 * replaced by SubClassOfAxiom (rule [R2])
	 */
	
	@Override
	public void visit(OWLObjectPropertyDomainAxiom ax) {
		try {
			ObjectPropertyExpression role = getPropertyExpression(ax.getProperty());
			addSubClassAxioms(role.getDomain(), ax.getDomain());
		} catch (TranslationException e) {
			log.warn("Error in OWL 2 QL axiom: {} ({})", ax, e);
		}
	}
	
	/**
	 * (10)
	 * 
	 * ObjectPropertyRange := 'ObjectPropertyRange' '(' axiomAnnotations ObjectPropertyExpression superClassExpression ')'
	 * 
	 * replaced by SubClassOfAxiom (rule [R2])
	 */
	
	@Override
	public void visit(OWLObjectPropertyRangeAxiom ax) {
		try {
			ObjectPropertyExpression role = getPropertyExpression(ax.getProperty());
			addSubClassAxioms(role.getRange(), ax.getRange());
		} catch (TranslationException e) {
			log.warn("Error in OWL 2 QL axiom: {} ({})", ax, e);
		}
	}
	
	/**
	 * (13)
	 * 
	 * SymmetricObjectProperty := 'SymmetricObjectProperty' '(' axiomAnnotations ObjectPropertyExpression ')'
	 * 
	 * replaced by SubObjectPropertyOfAxiom (rule [R3])
	 */

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom ax) {
		ObjectPropertyExpression role = getPropertyExpression(ax.getProperty());
		dl_onto.addSubPropertyOfAxiom(role, role.getInverse());
	}

	/**
	 * (14)
	 * 
	 * AsymmetricObjectProperty :='AsymmetricObjectProperty' '(' axiomAnnotations ObjectPropertyExpression ')'
	 * 
	 * replaced by DisjointObjectPropertiesAxiom (rule [R3])
	 */
	
	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom ax) {
		ObjectPropertyExpression p = getPropertyExpression(ax.getProperty());
		ImmutableSet<ObjectPropertyExpression> disjointProperties = ImmutableSet.of(p, p.getInverse());
		dl_onto.addDisjointObjectPropertiesAxiom(disjointProperties);
	}

	
	
	
	@Override
	public void visit(OWLDisjointUnionAxiom ax) {
		log.warn("Axiom not yet supported by Quest: {}", ax.toString());
	}
	
	@Override
	public void visit(OWLDataPropertyRangeAxiom ax) {

		DataPropertyExpression role = getPropertyExpression(ax.getProperty());
		DataPropertyRangeExpression subclass = role.getRange(); // ofac.createDataPropertyRange(role);

		OWLDataRange range = ax.getRange();
		
		if (range.isDatatype()) {
			OWLDatatype rangeDatatype = range.asOWLDatatype();

			if (rangeDatatype.isBuiltIn()) {
				try {
					Predicate.COL_TYPE columnType = OWLTypeMapper.getType(rangeDatatype);
					Datatype datatype = ofac.createDataType(columnType);
					dl_onto.addSubClassOfAxiom(subclass, datatype);
				} catch (TranslationException e) {
					log.warn("Error in " + ax + "(" + e + ")");
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
		
		DataPropertyExpression role = getPropertyExpression(ax.getProperty());
		dl_onto.addFunctionalDataPropertyAxiom(role);		
	}

	
	/**
	 * (17)
	 * 
	 * EquivalentDataProperties := 'EquivalentDataProperties' '(' axiomAnnotations 
	 * 								DataPropertyExpression DataPropertyExpression { DataPropertyExpression } ')'
	 * 
	 * replaced by SubDataPropertyOfAxiom (rule [R1])	
	 */
	
	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom ax) {		
		Iterator<OWLDataPropertyExpression> it = ax.getProperties().iterator();
		DataPropertyExpression first = getPropertyExpression(it.next());
		DataPropertyExpression previous = first;
		while (it.hasNext()) {
			DataPropertyExpression current = getPropertyExpression(it.next());
			dl_onto.addSubPropertyOfAxiom(previous, current);
			previous = current;
		}
		dl_onto.addSubPropertyOfAxiom(previous, first);
	}

	/**
	 * (19)
	 * 
	 * DataPropertyDomain := 'DataPropertyDomain' '(' axiomAnnotations DataPropertyExpression superClassExpression ')'
	 * 
	 * replaced by SubClassOfAxiom (rule [R2])
	 */
	
	@Override
	public void visit(OWLDataPropertyDomainAxiom ax) {
		try {
			DataPropertyExpression role = getPropertyExpression(ax.getProperty());
			addSubClassAxioms(role.getDomain(), ax.getDomain());		
		} catch (TranslationException e) {
			log.warn("Axiom not yet supported by Quest: {}", ax.toString());
		}
	}

	
	
	@Override
	public void visit(OWLClassAssertionAxiom ax) {
		ClassAssertion a = translate(ax);
		if (a != null)
			dl_onto.addClassAssertion(a);
	}

	@Override
	public void visit(OWLDataPropertyAssertionAxiom ax) {
		DataPropertyAssertion assertion = translate(ax);
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
		DataPropertyExpression subrole = getPropertyExpression(ax.getSubProperty());
		DataPropertyExpression superrole = getPropertyExpression(ax.getSuperProperty());

		dl_onto.addSubPropertyOfAxiom(subrole, superrole);	
	}

	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom ax) {
		//if (profile.order() < LanguageProfile.OWL2QL.order())
		//	throw new TranslationException();
		ObjectPropertyExpression role = getPropertyExpression(ax.getProperty());
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
	
	private static ClassExpression getClassExpression(OWLClass rest) {
		String uri = rest.getIRI().toString();
		return ofac.createClass(uri);		
	}
	
	private static ClassExpression getClassExpression(OWLObjectSomeValuesFrom rest) throws TranslationException {
		OWLClassExpression filler = rest.getFiller();

		if (!filler.isOWLThing()) 
			throw new TranslationException();
		
		return getPropertyExpression(rest.getProperty()).getDomain();		
	}

	private static ClassExpression getClassExpression(OWLDataSomeValuesFrom rest) throws TranslationException {
		OWLDataRange filler = rest.getFiller();

		if (!filler.isTopDatatype()) 
			throw new TranslationException();
		
		return getPropertyExpression(rest.getProperty()).getDomain();
	}

	private static ClassExpression getClassExpression(OWLObjectMinCardinality rest) throws TranslationException {
		int cardinatlity = rest.getCardinality();
		OWLClassExpression filler = rest.getFiller();
		if (cardinatlity != 1 || !filler.isOWLThing()) 
			throw new TranslationException();
			
		return getPropertyExpression(rest.getProperty()).getDomain();
	}

	private static ClassExpression getClassExpression(OWLDataMinCardinality rest) throws TranslationException {
		int cardinatlity = rest.getCardinality();
		OWLDataRange range = rest.getFiller();
		if (cardinatlity != 1 || !range.isTopDatatype()) 
			throw new TranslationException();
		
		return getPropertyExpression(rest.getProperty()).getDomain();
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

	
	/**
	 * (CR)
	 * 
	 * superClassExpression := Class | superObjectIntersectionOf | superObjectComplementOf | 
	 * 								superObjectSomeValuesFrom | DataSomeValuesFrom
	 * 
	 * superObjectIntersectionOf := 'ObjectIntersectionOf' '(' superClassExpression superClassExpression { superClassExpression } ')'
	 * superObjectComplementOf := 'ObjectComplementOf' '(' subClassExpression ')'
	 * superObjectSomeValuesFrom := 'ObjectSomeValuesFrom' '(' ObjectPropertyExpression Class ')'
	 * DataSomeValuesFrom := 'DataSomeValuesFrom' '(' DataPropertyExpression DataRange ')'
	 * 
	 * replaces ObjectIntersectionOf by a number of subClassOf axioms (rule [R4])
	 *          superObjectComplementOf by disjointness axioms (rule [R5])
	 */
	
	private void addSubClassAxioms(ClassExpression subDescription, OWLClassExpression superClasses) throws TranslationException {
		
		//System.out.println(superclasses);
		//System.out.println(superclasses.asConjunctSet());
		
		// .asConjunctSet() flattens out the intersections and the loop deals with [R4]
		for (OWLClassExpression superClass : superClasses.asConjunctSet()) {
			if (superClass instanceof OWLClass) {
				dl_onto.addSubClassOfAxiom(subDescription, getClassExpression((OWLClass)superClass));
			} 
			else if (superClass instanceof OWLObjectSomeValuesFrom) {
				OWLObjectSomeValuesFrom someexp = (OWLObjectSomeValuesFrom) superClass;
				OWLClassExpression filler = someexp.getFiller();

				if (filler.isOWLThing()) 
					dl_onto.addSubClassOfAxiom(subDescription, getClassExpression(someexp));
				else 
					// grammar simplifications
					dl_onto.addSubClassOfAxiom(subDescription, getPropertySomeClassRestriction(someexp));
			} 
			else if (superClass instanceof OWLDataSomeValuesFrom) {
				OWLDataSomeValuesFrom someexp = (OWLDataSomeValuesFrom) superClass;
				OWLDataRange filler = someexp.getFiller();

				if (filler.isTopDatatype()) 
					dl_onto.addSubClassOfAxiom(subDescription, getClassExpression(someexp));
				else
					dl_onto.addSubClassOfAxiom(subDescription, getPropertySomeDatatypeRestriction(someexp));
			} 
			else if (superClass instanceof OWLObjectComplementOf) {
				// [R5]
				OWLObjectComplementOf superC = (OWLObjectComplementOf)superClass;
				ClassExpression subDescription2 = getSubclassExpression(superC.getOperand());
				dl_onto.addDisjointClassesAxiom(ImmutableSet.of(subDescription, subDescription2));
			}
			else
				throw new TranslationException("unsupported operation in " + superClass);			
		}
	}
	
	
	// [R5] of the grammar simplification
	
	private ClassExpression getPropertySomeClassRestriction(OWLObjectSomeValuesFrom someexp) throws TranslationException {
		
		ObjectSomeValuesFrom auxclass = auxiliaryClassProperties.get(someexp);
		if (auxclass == null) {
			// no replacement found for this exists R.A, creating a new one
						
			ObjectPropertyExpression role = getPropertyExpression(someexp.getProperty());
			
			OWLClassExpression owlFiller = someexp.getFiller();
			if (!(owlFiller instanceof OWLClass)) 
				throw new TranslationException();
			
			ClassExpression filler = getSubclassExpression(owlFiller);

			ObjectPropertyExpression auxRole = dl_onto.getVocabulary().createAuxiliaryObjectProperty();

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
			
			DataPropertyExpression role = getPropertyExpression(someexp.getProperty());

			// TODO: handle more complex fillers
			// if (filler instanceof OWLDatatype);
			OWLDatatype owlDatatype = (OWLDatatype) someexp.getFiller();
			COL_TYPE datatype = OWLTypeMapper.getType(owlDatatype);
			Datatype filler = ofac.createDataType(datatype);
			
			DataPropertyExpression auxRole = dl_onto.getVocabulary().createAuxiliaryDataProperty();

			auxclass = auxRole.getDomain(); 
			auxiliaryDatatypeProperties.put(someexp, auxclass);

			dl_onto.addSubPropertyOfAxiom(auxRole, role);
			dl_onto.addSubClassOfAxiom(auxRole.getRange(), filler);
		}

		return auxclass;
	}

	
	

	
	
	public static ObjectPropertyAssertion translate(OWLObjectPropertyAssertionAxiom ax) {
		
		URIConstant c1 = getIndividual(ax.getSubject());
		URIConstant c2 = getIndividual(ax.getObject());

		ObjectPropertyExpression prop = getPropertyExpression(ax.getProperty());

		// TODO: check for bottom			
		
		return ofac.createObjectPropertyAssertion(prop, c1, c2);						
	}
	
	
	public static DataPropertyAssertion translate(OWLDataPropertyAssertionAxiom aux) {
		
		try {
			OWLLiteral object = aux.getObject();
			
			Predicate.COL_TYPE type = OWLTypeMapper.getType(object.getDatatype());
			ValueConstant c2 = dfac.getConstantLiteral(object.getLiteral(), type);

			DataPropertyExpression prop = getPropertyExpression(aux.getProperty());

			// TODO: CHECK FOR BOT AND TOP
			
			URIConstant c1 = getIndividual(aux.getSubject());

			return ofac.createDataPropertyAssertion(prop, c1, c2);
		
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
	
	
	

	
	private final Set<String> objectproperties = new HashSet<>();
	private final Set<String> dataproperties = new HashSet<>();
	private final Set<String> punnedPredicates = new HashSet<>();
	
	@Override
	public void declare(OWLClass entity) {
		/* We ignore TOP and BOTTOM (Thing and Nothing) */
		//if (entity.isOWLThing() || entity.isOWLNothing()) 
		//	continue;				
		String uri = entity.getIRI().toString();
		dl_onto.getVocabulary().createClass(uri);
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
			dl_onto.getVocabulary().createObjectProperty(uri);
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
			dl_onto.getVocabulary().createDataProperty(uri);
		}
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
