package it.unibz.krdb.sql.api;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

public class CollectionValueExpression extends AbstractValueExpression {

	private static final long serialVersionUID = -8519994184134506857L;
	
	/**
	 * The name of the function operation.
	 */
	private String functionOp = "";
	
	@Override
	public void putSpecification(Object obj) {
		functionOp = (String)obj;
	}
	
	public String getSpecification() {
		return functionOp;
	}

	@Override
	public String toString() {
		return functionOp + "(" + factors.get(0) + ")";
	}
}
