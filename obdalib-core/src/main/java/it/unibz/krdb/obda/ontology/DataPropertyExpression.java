package it.unibz.krdb.obda.ontology;

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

/**
 * Represents DataPropertyExpression in OWL 2 QL specification.
 * 
 * DataPropertyExpression := DataProperty
 * 
 * where 
 * 		DataProperty := IRI
 * 
 * @author roman
 *
 */


import it.unibz.krdb.obda.model.Predicate;

public interface DataPropertyExpression extends Description {

	public Predicate getPredicate();

	/**
	 * returns the DataSomeValuesFrom for the domain of the data property
	 * 
	 * @return
	 */
	
	public DataSomeValuesFrom getDomain();

	/**
	 * returns the DataPropertyRangeExpression for the range of the data property
	 * 
	 * @return
	 */
	public DataPropertyRangeExpression getRange();
	
	public boolean isBottom();
	
	public boolean isTop();
}
