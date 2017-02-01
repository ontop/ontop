package it.unibz.inf.ontop.owlrefplatform.core;

import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.model.OBDAConnection;

/**
 * Creates IQuestStatement (mandatory) and SIQuestStatement (optional).
 *
 * TODO: rename it (in the future) QuestConnection.
 */
public interface OntopConnection extends OBDAConnection {

	@Override
	OntopStatement createStatement() throws OntopConnectionException;

}
