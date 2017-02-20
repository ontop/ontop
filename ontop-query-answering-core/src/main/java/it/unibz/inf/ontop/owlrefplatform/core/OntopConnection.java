package it.unibz.inf.ontop.owlrefplatform.core;

import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.model.OBDAConnection;

public interface OntopConnection extends OBDAConnection {

	@Override
	OntopStatement createStatement() throws OntopConnectionException;

}
