package org.obda.reformulation.dllite;

import java.util.List;

import org.obda.query.domain.Query;
import org.obda.reformulation.domain.Assertion;

public interface QueryRewriter {

	public Query rewrite(Query input) throws Exception;
	public void updateAssertions(List<Assertion> ass);
}
