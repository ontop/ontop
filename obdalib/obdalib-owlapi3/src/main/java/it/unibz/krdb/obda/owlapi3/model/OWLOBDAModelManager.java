package it.unibz.krdb.obda.owlapi3.model;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

public interface OWLOBDAModelManager {

	OWLOBDADataFactory getOWLOBDADataFactory();

	Set<OWLOBDAModel> getOBDAModels();

	List<OWLOBDAModelChange> applyChanges(List<? extends OWLOBDAModelChange> changes);

	List<OWLOBDAModelChange> applyChange(OWLOBDAModelChange changes);

	List<OWLOBDAModelChange> addAxiom(OWLOBDAModel mod, OWLOBDAAxiom axiom);

	List<OWLOBDAModelChange> addAxioms(OWLOBDAModel mod, Set<? extends OWLOBDAAxiom> axioms);

	List<OWLOBDAModelChange> removeAxiom(OWLOBDAModel mod, OWLOBDAAxiom axiom);

	List<OWLOBDAModelChange> removeAxioms(OWLOBDAModel mod, Set<? extends OWLOBDAAxiom> axioms);

	OWLOBDAModel createOWLOBDAModel();

	OWLOBDAModel createOWLOBDAModel(Set<OWLOBDAAxiom> axioms);

	OWLOBDAModel loadOWLOBDAModelFromModelDocument(IRI documentIRI);

	OWLOBDAModel loadOWLOBDAModelFromModelDocument(File file);
	
	OWLOBDAModel loadOWLOBDAModelFromModelDocument(InputStream inputStream);
	
	void addOntologyChangeListener(OWLOBDAModelChangeListener listener);
	
	void removeOntologyChangeListener(OWLOBDAModelChangeListener listener);

}
