package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Query;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;

public interface QueryRewriter {

	public Query rewrite(Query input) throws Exception;

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
	public void setABoxDependencies(Ontology sigma);

}
