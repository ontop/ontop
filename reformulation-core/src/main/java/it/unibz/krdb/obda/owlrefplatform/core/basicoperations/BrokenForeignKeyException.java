package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

/*
 * #%L
 * ontop-reformulation-core
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

import it.unibz.krdb.sql.Reference;

public class BrokenForeignKeyException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String message;
	
	public BrokenForeignKeyException() {
		super();
	}
	
	public BrokenForeignKeyException(Reference reference, String message) {
		super("Broken integrity constraint: " + reference.getReferenceName());
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder(super.getMessage());
		sb.append(" ");
		sb.append("(Reason: ");
		sb.append(message);
		sb.append(")");
		return sb.toString();
	}
}
