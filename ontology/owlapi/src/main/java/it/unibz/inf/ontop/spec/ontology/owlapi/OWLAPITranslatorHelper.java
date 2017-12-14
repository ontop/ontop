package it.unibz.inf.ontop.spec.ontology.owlapi;

import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.URIConstant;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyFactoryImpl;
import org.semanticweb.owlapi.model.*;

import java.util.Optional;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;

public class OWLAPITranslatorHelper {

	private final TBoxReasoner reasoner;
	
	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	
	
	public OWLAPITranslatorHelper(TBoxReasoner reasoner) {
		this.reasoner = reasoner;
	}

	
	public ClassAssertion translate(OWLClassAssertionAxiom ax) throws OWLAPITranslatorOWL2QL.TranslationException, InconsistentOntologyException {
		OWLClassExpression classExpression = ax.getClassExpression();
		if (!(classExpression instanceof OWLClass))
			throw new OWLAPITranslatorOWL2QL.TranslationException("complex class expressions are not supported");
		
		OWLClass namedclass = (OWLClass) classExpression;
		OClass concept = reasoner.getVocabulary().getClass(namedclass.getIRI().toString());
		
		URIConstant c = getIndividual(ax.getIndividual());

		return ofac.createClassAssertion(concept, c);
	}
	
	public ObjectPropertyAssertion translate(OWLObjectPropertyAssertionAxiom ax) throws OWLAPITranslatorOWL2QL.TranslationException, InconsistentOntologyException {
		URIConstant c1 = getIndividual(ax.getSubject());
		URIConstant c2 = getIndividual(ax.getObject());

		ObjectPropertyExpression ope = getPropertyExpression(ax.getProperty());

		return ofac.createObjectPropertyAssertion(ope, c1, c2);
	}	
	
	public DataPropertyAssertion translate(OWLDataPropertyAssertionAxiom ax) throws OWLAPITranslatorOWL2QL.TranslationException, InconsistentOntologyException {
		OWLLiteral object = ax.getObject();

		ValueConstant c2;
		if(!object.getLang().isEmpty()) {
			c2 = TERM_FACTORY.getConstantLiteral(object.getLiteral(), object.getLang());
		}
		else {
			Optional<Predicate.COL_TYPE> type = TYPE_FACTORY.getDatatype(object.getDatatype().toStringID());
			if(type.isPresent()){
				c2 = TERM_FACTORY.getConstantLiteral(object.getLiteral(), type.get());
			}
			else {
				c2 = TERM_FACTORY.getConstantLiteral(object.getLiteral());
			}
		}
		URIConstant c1 = getIndividual(ax.getSubject());

		DataPropertyExpression dpe = getPropertyExpression(ax.getProperty());

		return ofac.createDataPropertyAssertion(dpe, c1, c2);
	}

	public AnnotationAssertion translate(OWLAnnotationAssertionAxiom ax) throws OWLAPITranslatorOWL2QL.TranslationException, InconsistentOntologyException {

		URIConstant c1 = getIndividual(ax.getSubject());

		Constant c2 = getValue(ax.getValue());

		AnnotationProperty ap = getPropertyExpression(ax.getProperty());

		return ofac.createAnnotationAssertion(ap, c1, c2);
	}
	
	/**
	 * 
	 * @param clExpression
	 * @return
	 */
	
	public OClass getOClass(OWLClass clExpression) {
		String uri = clExpression.getIRI().toString();
		return reasoner.getVocabulary().getClass(uri);
	}
	
	
	/**
	 * ObjectPropertyExpression := ObjectProperty | InverseObjectProperty
	 * InverseObjectProperty := 'ObjectInverseOf' '(' ObjectProperty ')'
	 * 
	 * @param opeExpression
	 * @return
	 */
	
	public ObjectPropertyExpression getPropertyExpression(OWLObjectPropertyExpression opeExpression) {

		if (opeExpression instanceof OWLObjectProperty) 
			return reasoner.getVocabulary().getObjectProperty(opeExpression.asOWLObjectProperty().getIRI().toString());
	
		else {
			assert(opeExpression instanceof OWLObjectInverseOf);
			
			OWLObjectInverseOf aux = (OWLObjectInverseOf) opeExpression;
			return reasoner.getVocabulary().getObjectProperty(aux.getInverse().asOWLObjectProperty().getIRI().toString()).getInverse();
		} 			
	}

	
	
	/**
	 * DataPropertyExpression := DataProperty
	 * 
	 * @param dpeExpression
	 * @return
	 */
	
	public DataPropertyExpression getPropertyExpression(OWLDataPropertyExpression dpeExpression)  {
		assert (dpeExpression instanceof OWLDataProperty); 
		return reasoner.getVocabulary().getDataProperty(dpeExpression.asOWLDataProperty().getIRI().toString());
	}


	/**
	 * AnnotationProperty
	 *
	 * @param ap
	 * @return
	 */

	public AnnotationProperty getPropertyExpression(OWLAnnotationProperty ap)  {
		return reasoner.getVocabulary().getAnnotationProperty(ap.getIRI().toString());
	}



	private URIConstant getIndividual(OWLIndividual ind) throws OWLAPITranslatorOWL2QL.TranslationException {
		if (ind.isAnonymous()) 
			throw new OWLAPITranslatorOWL2QL.TranslationException("Found anonymous individual, this feature is not supported:" + ind);

		 return TERM_FACTORY.getConstantURI(ind.asOWLNamedIndividual().getIRI().toString());
	}


	/**
	 * Use to translatein URIConstant the subject of an annotation property
	 * @param subject
	 * @return
	 * @throws OWLAPITranslatorOWL2QL.TranslationException
     */
	private URIConstant getIndividual(OWLAnnotationSubject subject) throws OWLAPITranslatorOWL2QL.TranslationException {
		if (subject instanceof IRI) {
			return TERM_FACTORY.getConstantURI( ((IRI) subject).asIRI().get().toString());
		}
		else{
			throw new OWLAPITranslatorOWL2QL.TranslationException("Found anonymous individual, this feature is not supported:" + subject);

		}
	}

	/**
	 * Use to translate in URIConstant or ValueConstant the object of an annotation property
	 * @param value
	 * @return
	 * @throws OWLAPITranslatorOWL2QL.TranslationException
     */
	private Constant getValue (OWLAnnotationValue value)  throws OWLAPITranslatorOWL2QL.TranslationException {
		try {
			if (value instanceof IRI) {
				return TERM_FACTORY.getConstantURI(value.asIRI().get().toString());
			}
			if (value instanceof OWLLiteral) {
				OWLLiteral owlLiteral = value.asLiteral().get();
				if (!owlLiteral.getLang().isEmpty()) {

					return TERM_FACTORY.getConstantLiteral(owlLiteral.getLiteral(), owlLiteral.getLang());
				} else {
					Optional<Predicate.COL_TYPE> type = TYPE_FACTORY.getDatatype(owlLiteral.getDatatype().toStringID());
					if(!type.isPresent()){
						return TERM_FACTORY.getConstantLiteral(owlLiteral.getLiteral());
					}
					return TERM_FACTORY.getConstantLiteral(owlLiteral.getLiteral(), type.get());
				}

			} else {
				throw new OWLAPITranslatorOWL2QL.TranslationException("Found anonymous individual, this feature is not supported:" + value);

			}
		}
		catch(OWLRuntimeException ore){
			throw new OWLAPITranslatorOWL2QL.TranslationException(ore.getMessage());
		}
	}


}
