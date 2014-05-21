package org.semanticweb.ontop.ontology.impl;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
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

import org.semanticweb.ontop.model.Predicate;

/***
 * Indicates that a URI has been PUNNED, i.e., it has been used as a Class and
 * Property at the same type (or Object and Data property).
 */
public class PunningException extends Exception {

	private static final long serialVersionUID = 5273586443299868448L;
	
	private Predicate pred1 = null;
	private Predicate pred2 = null;

	public PunningException(String message, Predicate pred1, Predicate pred2) {
		super(message);
		this.setPred1(pred1);
		this.setPred2(pred2);
	}
	
	public PunningException(String message) {
		super(message);
	}
	
	public PunningException(Predicate pred1, Predicate pred2) {
		this("", pred1, pred2);
	}

	private void setPred1(Predicate pred1) {
		this.pred1 = pred1;
	}

	public Predicate getPred1() {
		return pred1;
	}

	private void setPred2(Predicate pred2) {
		this.pred2 = pred2;
	}

	public Predicate getPred2() {
		return pred2;
	}
}
