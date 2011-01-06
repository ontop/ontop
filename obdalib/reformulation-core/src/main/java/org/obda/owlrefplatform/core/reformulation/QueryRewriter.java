package org.obda.owlrefplatform.core.reformulation;

import java.util.List;

import org.obda.owlrefplatform.core.ontology.Assertion;
import org.obda.query.domain.Query;

public interface QueryRewriter {

	public Query rewrite(Query input) throws Exception;
	public void updateAssertions(List<Assertion> ass);
}
