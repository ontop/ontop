package org.obda.owlrefplatform.core.reformulation;

import inf.unibz.it.obda.domain.Query;

import java.util.List;

import org.obda.owlrefplatform.core.ontology.Assertion;

public interface QueryRewriter {

	public Query rewrite(Query input) throws Exception;
	public void updateAssertions(List<Assertion> ass);
}
