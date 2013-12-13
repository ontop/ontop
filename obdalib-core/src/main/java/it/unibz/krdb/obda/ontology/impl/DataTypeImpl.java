package it.unibz.krdb.obda.ontology.impl;

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

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.DataType;

public class DataTypeImpl implements DataType {
	
	private static final long serialVersionUID = -6228610469212615956L;
	
	private Predicate predicate;
	
	public DataTypeImpl(Predicate p) {
		predicate = p;
	}
	
	public Predicate getPredicate() {
		return predicate;
	}
	
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		return predicate.getName().toString();
	}
}
