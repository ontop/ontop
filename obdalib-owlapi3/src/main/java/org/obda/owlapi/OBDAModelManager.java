/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.obda.owlapi;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.obda.owlapi.OWLOBDAModel;
import org.obda.owlapi.OBDAModelChange;
import org.obda.owlapi.OBDAModelChangeListener;
import org.semanticweb.owlapi.model.IRI;

public interface OBDAModelManager {

	OBDADataFactory getOWLOBDADataFactory();

	Set<OWLOBDAModel> getOBDAModels();

	List<OBDAModelChange> applyChanges(List<? extends OBDAModelChange> changes);

	List<OBDAModelChange> applyChange(OBDAModelChange changes);

	List<OBDAModelChange> addAxiom(OWLOBDAModel mod, OBDAAxiom axiom);

	List<OBDAModelChange> addAxioms(OWLOBDAModel mod, Set<? extends OBDAAxiom> axioms);

	List<OBDAModelChange> removeAxiom(OWLOBDAModel mod, OBDAAxiom axiom);

	List<OBDAModelChange> removeAxioms(OWLOBDAModel mod, Set<? extends OBDAAxiom> axioms);

	OWLOBDAModel createOWLOBDAModel();

	OWLOBDAModel createOWLOBDAModel(Set<OBDAAxiom> axioms);

	OWLOBDAModel loadOWLOBDAModelFromModelDocument(IRI documentIRI);

	OWLOBDAModel loadOWLOBDAModelFromModelDocument(File file);
	
	OWLOBDAModel loadOWLOBDAModelFromModelDocument(InputStream inputStream);
	
	void addOntologyChangeListener(OBDAModelChangeListener listener);
	
	void removeOntologyChangeListener(OBDAModelChangeListener listener);

}
