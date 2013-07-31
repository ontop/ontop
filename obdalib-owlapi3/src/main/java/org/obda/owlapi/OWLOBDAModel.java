/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.obda.owlapi;

import java.util.Set;

import org.obda.owlapi.OBDAModelManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

public interface OWLOBDAModel {

	public OBDAModelManager getOBDAModelManager();

	public boolean isEmpty();

	public int getDatasourceCount();

	public int getMappingAxiomCount();

	public Set<OBDADatasource> getDatasources();

	public Set<OWLEntity> getSignature();

	public Set<OWLClass> getClassesInSignature();

	public Set<OWLObjectProperty> getObjectPropertiesInSignature();

	public Set<OWLDataProperty> getDataPropertiesInSignature();

	public Set<OWLIndividual> getIndividualsInSignature();

	public Set<OBDAMappingAxiom> getMappingAxioms();

	public Set<OBDAMappingAxiom> getReferencingMappingAxioms(OWLEntity owlEntity);

	boolean containsEntityInSignature(OWLEntity owlEntity);

	boolean isDeclared(OWLEntity owlEntity);

	boolean containsClassInSignature(IRI owlClassIRI);

	boolean containsObjectPropertyInSignature(IRI owlObjectPropertyIRI);

	boolean containsDataPropertyInSignature(IRI owlDataPropertyIRI);

	boolean containsIndividualInSignature(IRI owlIndividualIRI);

}
