package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.ontology.Ontology;

/***
 * A query reformulator that does nothing on the given query. 
 * 
 * @author mariano
 *
 */
public class DummyReformulator implements QueryRewriter {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8989177354924893482L;

	@Override
	public OBDAQuery rewrite(OBDAQuery input) throws OBDAException {
		return input;
	}

	@Override
	public void setTBox(Ontology ontology) {
		// NO-OP
		
	}

	@Override
	public void setCBox(Ontology sigma) {
		// NO-OP
		
	}

	@Override
	public void initialize() {
		// NO-OP
		
	}

}
