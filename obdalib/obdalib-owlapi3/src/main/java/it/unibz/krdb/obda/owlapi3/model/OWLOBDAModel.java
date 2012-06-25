package it.unibz.krdb.obda.owlapi3.model;

import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

public interface OWLOBDAModel {

	public OWLOBDAModelManager getOBDAModelManager();

	public boolean isEmpty();

	public int getDatasourceCount();

	public int getMappingAxiomCount();

	public Set<OWLRDBDatasource> getDatasources();

	public Set<OWLEntity> getSignature();

	public Set<OWLClass> getClassesInSignature();

	public Set<OWLObjectProperty> getObjectPropertiesInSignature();

	public Set<OWLDataProperty> getDataPropertiesInSignature();

	public Set<OWLIndividual> getIndividualsInSignature();

	public Set<OWLOBDAMappingAxiom> getMappingAxioms();

	public Set<OWLOBDAMappingAxiom> getReferencingMappingAxioms(OWLEntity owlEntity);

	boolean containsEntityInSignature(OWLEntity owlEntity);

	boolean isDeclared(OWLEntity owlEntity);

	boolean containsClassInSignature(IRI owlClassIRI);

	boolean containsObjectPropertyInSignature(IRI owlObjectPropertyIRI);

	boolean containsDataPropertyInSignature(IRI owlDataPropertyIRI);

	boolean containsIndividualInSignature(IRI owlIndividualIRI);

}
