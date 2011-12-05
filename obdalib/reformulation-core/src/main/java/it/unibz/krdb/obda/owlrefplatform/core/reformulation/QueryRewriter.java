package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import java.io.Serializable;

import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;

public interface QueryRewriter extends Serializable {

	public OBDAQuery rewrite(OBDAQuery input) throws Exception;

	/***
	 * Sets the ontology that this rewriter should use to compute any
	 * reformulation.
	 * 
	 * @param ontology
	 */
	public void setTBox(Ontology ontology);

	/**
	 * Sets the ABox dependencies that the reformulator can use to optimize the
	 * reformulations (if it is able to do so).
	 * 
	 * @param sigma
	 */
	public void setCBox(Ontology sigma);
	
	
	/***
	 * Initializes the rewriter. This method must be called before calling "rewrite"
	 * and after the TBox and CBox have been updated.
	 */
	public void initialize();

}
