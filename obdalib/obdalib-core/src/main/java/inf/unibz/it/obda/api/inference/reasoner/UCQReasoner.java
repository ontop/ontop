/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro.
 * All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public
 * License v.3 (see OBDAAPI_LICENSE.txt for details). The components of this
 * work include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, 
 * b) third-party components licensed under terms that may be different from 
 *   those of the LGPL.  Information about such licenses can be found in the 
 *   file named OBDAAPI_3DPARTY-LICENSES.txt.
 */
package inf.unibz.it.obda.api.inference.reasoner;

import inf.unibz.it.ucq.domain.QueryResult;
import inf.unibz.it.ucq.domain.UnionOfConjunctiveQueries;


//TODO: Split into UCQ reasoner and Quonto reasoner

public interface UCQReasoner {
	
	public QueryResult answerUCQ(UnionOfConjunctiveQueries query) throws Exception;
	
	public QueryResult answerEQL(String query) throws Exception;
	
	public String unfoldEQL(String query) throws Exception;
	
	public UnionOfConjunctiveQueries getRewritting(UnionOfConjunctiveQueries query) throws Exception;
	
	public String getUnfolding(UnionOfConjunctiveQueries query) throws Exception;
	
}
