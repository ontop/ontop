package org.obda.owlapi;

import org.semanticweb.owlapi.model.OWLDataProperty;

public interface OBDADataPropertyAssertionTemplate extends OBDAAssertionTemplate {
	OWLDataProperty getEntity();

}
