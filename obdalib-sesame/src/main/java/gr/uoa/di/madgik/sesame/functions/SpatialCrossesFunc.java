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

import org.openrdf.query.algebra.BinaryValueOperator;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.query.algebra.ValueExpr;

/**
 * @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 *
 */
public class SpatialCrossesFunc extends BinaryValueOperator{
	/**
	 * Checks if two RDF spatial literals spatially Crosses.
	 */

		/*--------------*
		 * Constructors *
		 *--------------*/

		public void SpatialCrossesFunc() {
		}
		
		public SpatialCrossesFunc(ValueExpr leftArg, ValueExpr rightArg) {
			super(leftArg, rightArg);
		}

	

		/*---------*
		 * Methods *
		 *---------*/

	

		@Override
		public boolean equals(Object other) {
			return other instanceof SpatialCrossesFunc && super.equals(other);
		}

		@Override
		public int hashCode() {
			return super.hashCode() ^ "SpatialCrossesFunc".hashCode();
		}

		@Override
		public SpatialCrossesFunc clone() {
			return (SpatialCrossesFunc)super.clone();
		}

		@Override
		public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
				throws X {
			visitor.meet(this);
						
		}

}


		
	
