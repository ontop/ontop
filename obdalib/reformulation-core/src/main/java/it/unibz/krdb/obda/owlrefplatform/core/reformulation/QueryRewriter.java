package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Query;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;

import java.util.List;


public interface QueryRewriter {

	public Query rewrite(Query input) throws Exception;
	
	public void updateAssertions(List<Assertion> ass);
}
