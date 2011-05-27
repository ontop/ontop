package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.DataSource;

/**
 * A simple listener interface that notifies its classes when a 
 * abox dump was succesfull
 * 
 * @author Manfred Gerstgrasser
 *
 */

public interface ABoxDumpListener {

	/**
	 * Notifies that a abox dump was successful
	 * 
	 * @param ds the data source to which the dump was made
	 */
	public void dump_successful(DataSource ds);
}
