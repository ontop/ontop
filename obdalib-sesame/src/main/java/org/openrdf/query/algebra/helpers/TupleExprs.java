/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.query.algebra.helpers;

import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.TupleExpr;

/**
 * Utility methods for {@link TupleExpr} objects.
 * 
 * @author Jeen Broekstra
 * @since 2.7.3
 */
public class TupleExprs {

	/**
	 * Verifies if the supplied {@link TupleExpr} contains a {@link Projection}.
	 * If the supplied TupleExpr is a {@link Join} or contains a {@link Join},
	 * projections inside that Join's arguments will not be taken into
	 * account.
	 * 
	 * @param t
	 *        a tuple expression.
	 * @return <code>true</code> if the TupleExpr contains a projection (outside
	 *         of a Join), <code>false</code> otherwise.
	 * @since 2.7.3
	 */
	public static boolean containsProjection(TupleExpr t) {
		@SuppressWarnings("serial")
		class VisitException extends Exception {
		}
		final boolean[] result = new boolean[1];
		try {
			t.visit(new QueryModelVisitorBase<VisitException>() {

				@Override
				public void meet(Projection node)
					throws VisitException
				{
					result[0] = true;
					throw new VisitException();
				}

				@Override
				public void meet(Join node)
					throws VisitException
				{
					// projections already inside a Join need not be
					// taken into account
					result[0] = false;
					throw new VisitException();
				}
			});
		}
		catch (VisitException ex) {
			// Do nothing. We have thrown this exception on the first
			// meeting of Projection or Join.
		}
		return result[0];
	}
}
