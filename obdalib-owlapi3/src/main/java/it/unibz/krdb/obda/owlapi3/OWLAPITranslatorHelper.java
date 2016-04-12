package it.unibz.krdb.obda.owlapi3;

import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.*;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OWLAPITranslatorOWL2QL.TranslationException;
import org.semanticweb.owlapi.model.*;

public class OWLAPITranslatorHelper {

	private final ImmutableOntologyVocabulary voc;
	
	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
	
	
	OWLAPITranslatorHelper(ImmutableOntologyVocabulary voc) {
		this.voc = voc;
	}

	
	public ClassAssertion translate(OWLClassAssertionAxiom ax) throws TranslationException, InconsistentOntologyException {
		OWLClassExpression classExpression = ax.getClassExpression();
		if (!(classExpression instanceof OWLClass))
			throw new TranslationException("complex class expressions are not supported");
		
		OWLClass namedclass = (OWLClass) classExpression;
		OClass concept = voc.getClass(namedclass.getIRI().toString());
		
		URIConstant c = getIndividual(ax.getIndividual());

		return ofac.createClassAssertion(concept, c);
	}
	
	public ObjectPropertyAssertion translate(OWLObjectPropertyAssertionAxiom ax) throws TranslationException, InconsistentOntologyException {
		URIConstant c1 = getIndividual(ax.getSubject());
		URIConstant c2 = getIndividual(ax.getObject());

		ObjectPropertyExpression ope = getPropertyExpression(ax.getProperty());

		return ofac.createObjectPropertyAssertion(ope, c1, c2);
	}	
	
	public DataPropertyAssertion translate(OWLDataPropertyAssertionAxiom ax) throws TranslationException, InconsistentOntologyException {
		OWLLiteral object = ax.getObject();

		ValueConstant c2;
		if(!object.getLang().isEmpty()) {
			c2 = dfac.getConstantLiteral(object.getLiteral(), object.getLang());
		}
		else {
			Predicate.COL_TYPE type = OWLTypeMapper.getType(object.getDatatype());
			c2 = dfac.getConstantLiteral(object.getLiteral(), type);
		}
		URIConstant c1 = getIndividual(ax.getSubject());

		DataPropertyExpression dpe = getPropertyExpression(ax.getProperty());

		return ofac.createDataPropertyAssertion(dpe, c1, c2);
	}

	public AnnotationAssertion translate(OWLAnnotationAssertionAxiom ax) throws TranslationException, InconsistentOntologyException {

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
		return voc.getClass(uri);		
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
			return voc.getObjectProperty(opeExpression.asOWLObjectProperty().getIRI().toString());
	
		else {
			assert(opeExpression instanceof OWLObjectInverseOf);
			
			OWLObjectInverseOf aux = (OWLObjectInverseOf) opeExpression;
			return voc.getObjectProperty(aux.getInverse().asOWLObjectProperty().getIRI().toString()).getInverse();
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
		return voc.getDataProperty(dpeExpression.asOWLDataProperty().getIRI().toString());
	}


	/**
	 * AnnotationProperty
	 *
	 * @param ap
	 * @return
	 */

	public AnnotationProperty getPropertyExpression(OWLAnnotationProperty ap)  {
		return voc.getAnnotationProperty(ap.getIRI().toString());
	}



	private URIConstant getIndividual(OWLIndividual ind) throws TranslationException {
		if (ind.isAnonymous()) 
			throw new TranslationException("Found anonymous individual, this feature is not supported:" + ind);

		 return dfac.getConstantURI(ind.asOWLNamedIndividual().getIRI().toString());
	}


	/**
	 * Use to translatein URIConstant the subject of an annotation property
	 * @param subject
	 * @return
	 * @throws TranslationException
     */
	private URIConstant getIndividual(OWLAnnotationSubject subject) throws TranslationException {
		if (subject instanceof IRI) {
			return dfac.getConstantURI( ((IRI) subject).asIRI().get().toString());
		}
		else{
			throw new TranslationException("Found anonymous individual, this feature is not supported:" + subject);

		}
	}

	/**
	 * Use to translate in URIConstant or ValueConstant the object of an annotation property
	 * @param value
	 * @return
	 * @throws TranslationException
     */
	private Constant getValue (OWLAnnotationValue value)  throws TranslationException {
		try {
			if (value instanceof IRI) {
				return dfac.getConstantURI(value.asIRI().get().toString());
			}
			if (value instanceof OWLLiteral) {
				OWLLiteral owlLiteral = value.asLiteral().get();
				if (!owlLiteral.getLang().isEmpty()) {

					return dfac.getConstantLiteral(owlLiteral.getLiteral(), owlLiteral.getLang());
				} else {
					Predicate.COL_TYPE type = OWLTypeMapper.getType(owlLiteral.getDatatype());
					return dfac.getConstantLiteral(owlLiteral.getLiteral(), type);
				}

			} else {
				throw new TranslationException("Found anonymous individual, this feature is not supported:" + value);

			}
		}
		catch(OWLRuntimeException ore){
			throw new TranslationException(ore.getMessage());
		}
	}


}
