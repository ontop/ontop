package it.unibz.inf.ontop.owlrefplatform.core;

import it.unibz.inf.ontop.model.OBDAConnection;
import it.unibz.inf.ontop.model.OBDAException;

/**
 * Creates IQuestStatement (mandatory) and SIQuestStatement (optional).
 *
 * TODO: rename it (in the future) QuestConnection.
 */
public interface IQuestConnection extends OBDAConnection {

	/**
	 * For both modes.
	 */
	@Override
	IQuestStatement createStatement() throws OBDAException;

	/**
	 * For the classic mode.
	 * MAY NOT BE SUPPORTED by certain implementations.
	 */
	//SIQuestStatement createSIStatement() throws OBDAException;
}
