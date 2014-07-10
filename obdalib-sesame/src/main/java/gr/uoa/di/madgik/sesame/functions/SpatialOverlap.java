/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2012, Pyravlos Team
 *
 * http://www.strabon.di.uoa.gr/
 */

package gr.uoa.di.madgik.sesame.functions;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;



/**
 * @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 *
 */
public class SpatialOverlap implements org.openrdf.query.algebra.evaluation.function.Function {

	/* (non-Javadoc)
	 * @see org.openrdf.query.algebra.evaluation.function.Function#evaluate(org.openrdf.model.ValueFactory, org.openrdf.model.Value[])
	 */
	@Override
	public Value evaluate(ValueFactory arg0, Value... arg1)
			throws ValueExprEvaluationException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.openrdf.query.algebra.evaluation.function.Function#getURI()
	 */
	@Override
	public String getURI() {
	
		return OBDAVocabulary.overlap;
	}
	
	

}
