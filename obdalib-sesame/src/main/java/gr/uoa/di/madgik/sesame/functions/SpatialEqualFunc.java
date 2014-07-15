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
public class SpatialEqualFunc extends BinaryValueOperator{
	/**
	 * Checks if two RDF spatial literals spatially Equal.
	 */

		/*--------------*
		 * Constructors *
		 *--------------*/

		public void SpatialEqualFunc() {
		}
		
		public SpatialEqualFunc(ValueExpr leftArg, ValueExpr rightArg) {
			super(leftArg, rightArg);
		}

	

		/*---------*
		 * Methods *
		 *---------*/

	

		@Override
		public boolean equals(Object other) {
			return other instanceof SpatialEqualFunc && super.equals(other);
		}

		@Override
		public int hashCode() {
			return super.hashCode() ^ "SpatialEqualFunc".hashCode();
		}

		@Override
		public SpatialEqualFunc clone() {
			return (SpatialEqualFunc)super.clone();
		}

		@Override
		public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
				throws X {
			visitor.meet(this);
						
		}

}


		
	
