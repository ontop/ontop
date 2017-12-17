package it.unibz.inf.ontop.spec.ontology.owlapi;

import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.URIConstant;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.spec.ontology.*;
import org.semanticweb.owlapi.model.*;

import java.util.Optional;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;

public class OWLAPITranslatorABoxHelper {

	private final OntologyABox abox;
	
	public OWLAPITranslatorABoxHelper(OntologyABox abox) {
		this.abox = abox;
	}
	
	public ClassAssertion translate(OWLClassAssertionAxiom ax) throws OWLAPITranslatorOWL2QL.TranslationException, InconsistentOntologyException {
		OWLClassExpression classExpression = ax.getClassExpression();
		if (!(classExpression instanceof OWLClass))
			throw new OWLAPITranslatorOWL2QL.TranslationException("complex class expressions are not supported");
		
		OClass concept = getOClass((OWLClass) classExpression);
		URIConstant c = getIndividual(ax.getIndividual());

		return abox.createClassAssertion(concept, c);
	}
	
	public ObjectPropertyAssertion translate(OWLObjectPropertyAssertionAxiom ax) throws OWLAPITranslatorOWL2QL.TranslationException, InconsistentOntologyException {
		URIConstant c1 = getIndividual(ax.getSubject());
		URIConstant c2 = getIndividual(ax.getObject());
		ObjectPropertyExpression ope = getPropertyExpression(ax.getProperty());

		return abox.createObjectPropertyAssertion(ope, c1, c2);
	}	
	
	public DataPropertyAssertion translate(OWLDataPropertyAssertionAxiom ax) throws OWLAPITranslatorOWL2QL.TranslationException, InconsistentOntologyException {
		OWLLiteral object = ax.getObject();

		final ValueConstant c2;
		if (!object.getLang().isEmpty()) {
			c2 = TERM_FACTORY.getConstantLiteral(object.getLiteral(), object.getLang());
		}
		else {
			Optional<Predicate.COL_TYPE> type = TYPE_FACTORY.getDatatype(object.getDatatype().toStringID());
			if (type.isPresent()) {
				c2 = TERM_FACTORY.getConstantLiteral(object.getLiteral(), type.get());
			}
			else {
				c2 = TERM_FACTORY.getConstantLiteral(object.getLiteral());
			}
		}
		URIConstant c1 = getIndividual(ax.getSubject());
		DataPropertyExpression dpe = getPropertyExpression(ax.getProperty());

		return abox.createDataPropertyAssertion(dpe, c1, c2);
	}

	public AnnotationAssertion translate(OWLAnnotationAssertionAxiom ax) throws OWLAPITranslatorOWL2QL.TranslationException, InconsistentOntologyException {

		URIConstant c1 = getIndividual(ax.getSubject());
		Constant c2 = getValue(ax.getValue());
		AnnotationProperty ap = getPropertyExpression(ax.getProperty());

		return abox.createAnnotationAssertion(ap, c1, c2);
	}
	
	/**
	 * 
	 * @param clExpression
	 * @return
	 */
	
	public OClass getOClass(OWLClass clExpression) {
		String uri = clExpression.getIRI().toString();
		return abox.getClass(uri);
	}
	
	
	/**
	 * ObjectPropertyExpression := ObjectProperty | InverseObjectProperty
	 * InverseObjectProperty := 'ObjectInverseOf' '(' ObjectProperty ')'
	 * 
	 * @param opeExpression
	 * @return
	 */
	
	public ObjectPropertyExpression getPropertyExpression(OWLObjectPropertyExpression opeExpression) {

		if (opeExpression instanceof OWLObjectProperty) {
            return abox.getObjectProperty(opeExpression.asOWLObjectProperty().getIRI().toString());
        }
		else {
			assert(opeExpression instanceof OWLObjectInverseOf);
			
			OWLObjectInverseOf aux = (OWLObjectInverseOf) opeExpression;
			return abox.getObjectProperty(aux.getInverse().asOWLObjectProperty().getIRI().toString()).getInverse();
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
		return abox.getDataProperty(dpeExpression.asOWLDataProperty().getIRI().toString());
	}


	/**
	 * AnnotationProperty
	 *
	 * @param ap
	 * @return
	 */

	public AnnotationProperty getPropertyExpression(OWLAnnotationProperty ap)  {
		return abox.getAnnotationProperty(ap.getIRI().toString());
	}



	private URIConstant getIndividual(OWLIndividual ind) throws OWLAPITranslatorOWL2QL.TranslationException {
		if (ind.isAnonymous()) 
			throw new OWLAPITranslatorOWL2QL.TranslationException("Found anonymous individual, this feature is not supported:" + ind);

		 return TERM_FACTORY.getConstantURI(ind.asOWLNamedIndividual().getIRI().toString());
	}


	/**
	 * Use to translate URIConstant in the subject of an annotation property
	 * @param subject
	 * @return
	 * @throws OWLAPITranslatorOWL2QL.TranslationException
     */
	private URIConstant getIndividual(OWLAnnotationSubject subject) throws OWLAPITranslatorOWL2QL.TranslationException {
		if (subject instanceof IRI) {
			return TERM_FACTORY.getConstantURI(((IRI)subject).asIRI().get().toString());
		}
		else
			throw new OWLAPITranslatorOWL2QL.TranslationException("Found anonymous individual, this feature is not supported:" + subject);
	}

	/**
	 * Use to translate in URIConstant or ValueConstant the object of an annotation property
	 * @param value
	 * @return
	 * @throws OWLAPITranslatorOWL2QL.TranslationException
     */
	private Constant getValue(OWLAnnotationValue value)  throws OWLAPITranslatorOWL2QL.TranslationException {
		try {
			if (value instanceof IRI) {
				return TERM_FACTORY.getConstantURI(value.asIRI().get().toString());
			}
			else if (value instanceof OWLLiteral) {
				OWLLiteral owlLiteral = value.asLiteral().get();
				if (!owlLiteral.getLang().isEmpty()) {
					return TERM_FACTORY.getConstantLiteral(owlLiteral.getLiteral(), owlLiteral.getLang());
				}
				else {
					Optional<Predicate.COL_TYPE> type = TYPE_FACTORY.getDatatype(owlLiteral.getDatatype().toStringID());
					return !type.isPresent()
						? TERM_FACTORY.getConstantLiteral(owlLiteral.getLiteral())
					    : TERM_FACTORY.getConstantLiteral(owlLiteral.getLiteral(), type.get());
				}
			}
			else
				throw new OWLAPITranslatorOWL2QL.TranslationException("Found anonymous individual, this feature is not supported:" + value);
		}
		catch (OWLRuntimeException ore) {
			throw new OWLAPITranslatorOWL2QL.TranslationException(ore.getMessage());
		}
	}
}
