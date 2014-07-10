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
package org.openrdf.query.algebra.evaluation;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.ValueExpr;

/**
 * An exception indicating that a {@link ValueExpr} could not be evaluated due
 * to illegal or incompatible values. When thrown, the result of the evaluation
 * should be considered to be "unknown".
 * 
 * @author Arjohn Kampman
 */
public class ValueExprEvaluationException extends QueryEvaluationException {

	private static final long serialVersionUID = -3633440570594631529L;

	public ValueExprEvaluationException() {
		super();
	}

	public ValueExprEvaluationException(String message) {
		super(message);
	}

	public ValueExprEvaluationException(String message, Throwable t) {
		super(message, t);
	}

	public ValueExprEvaluationException(Throwable t) {
		super(t);
	}
}
