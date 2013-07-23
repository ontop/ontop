package it.unibz.krdb.obda.owlapi3;

import it.unibz.krdb.obda.model.OBDAException;

public interface OWLQueryReasoner {

	public OWLStatement getStatement() throws Exception;

	OWLConnection getConnection() throws OBDAException;

}
