package org.obda.owlapi;

import java.util.List;

import org.obda.owlapi.OBDAModelChange;
import org.semanticweb.owlapi.model.OWLException;

public interface OBDAModelChangeListener {

	void modelChanged(List<? extends OBDAModelChange> changes) throws OWLException;
}
