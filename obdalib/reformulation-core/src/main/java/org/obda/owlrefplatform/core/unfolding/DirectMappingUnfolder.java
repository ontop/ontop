package org.obda.owlrefplatform.core.unfolding;

import it.unibz.krdb.obda.model.DatalogProgram;


/**
 * The unfolder for the direct partial evaluation
 * 
 * @author obda
 *
 */
public class DirectMappingUnfolder implements UnfoldingMechanism {

	public DatalogProgram unfold(DatalogProgram query) {
		return query;
	}


}
