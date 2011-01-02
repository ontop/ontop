package org.obda.reformulation.dllite;

import java.util.List;

import org.obda.query.domain.Query;
import org.obda.reformulation.domain.Assertion;

public class IndexedDLRPerfectReformulator implements QueryRewriter {

//	
//	This is the same as above but we apply the following optimizations:
//		 * Hash indexes for every assertion (to improve applciation)
//		 * Uses indexed queries instead of simple queries
	
	public Query rewrite(Query input) {
		return null;
	}

	@Override
	public void updateAssertions(List<Assertion> ass) {
		// TODO Auto-generated method stub
		
	}

}
