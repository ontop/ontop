package it.unibz.krdb.obda.owlapi3;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.*;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OWLAPI3TranslatorOWL2QL.TranslationException;
import org.semanticweb.owlapi.model.*;

public class OWLAPI3TranslatorHelper {

	private final ImmutableOntologyVocabulary voc;
	
	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
	
	
	OWLAPI3TranslatorHelper(ImmutableOntologyVocabulary voc) {
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
	
	public ObjectPropertyAssertion translate(OWLObjectPropertyAssertionAxiom ax) throws InconsistentOntologyException {
		URIConstant c1 = getIndividual(ax.getSubject());
		URIConstant c2 = getIndividual(ax.getObject());

		ObjectPropertyExpression ope = getPropertyExpression(ax.getProperty());

		return ofac.createObjectPropertyAssertion(ope, c1, c2);
	}	
	
	public DataPropertyAssertion translate(OWLDataPropertyAssertionAxiom ax) throws TranslationException, InconsistentOntologyException {
		OWLLiteral object = ax.getObject();		
		Predicate.COL_TYPE type = OWLTypeMapper.getType(object.getDatatype());
		ValueConstant c2 = dfac.getConstantLiteral(object.getLiteral(), type);
		
		URIConstant c1 = getIndividual(ax.getSubject());

		DataPropertyExpression dpe = getPropertyExpression(ax.getProperty());
		
		return ofac.createDataPropertyAssertion(dpe, c1, c2);	
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
	 * @param rolExpression
	 * @return
	 */
	
	public DataPropertyExpression getPropertyExpression(OWLDataPropertyExpression dpeExpression)  {
		assert (dpeExpression instanceof OWLDataProperty); 
		return voc.getDataProperty(dpeExpression.asOWLDataProperty().getIRI().toString());
	}
	
	

	public static URIConstant getIndividual(OWLIndividual ind) {
		if (ind.isAnonymous()) 
			throw new RuntimeException("Found anonymous individual, this feature is not supported:" + ind);

		 return dfac.getConstantURI(ind.asOWLNamedIndividual().getIRI().toString());
	}
	

	
	
}
