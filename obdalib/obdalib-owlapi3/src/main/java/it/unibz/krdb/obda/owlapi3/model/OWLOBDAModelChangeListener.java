package it.unibz.krdb.obda.owlapi3.model;

import java.util.List;

import org.semanticweb.owlapi.model.OWLException;

public interface OWLOBDAModelChangeListener {

	void modelChanged(List<? extends OWLOBDAModelChange> changes) throws OWLException;
}
