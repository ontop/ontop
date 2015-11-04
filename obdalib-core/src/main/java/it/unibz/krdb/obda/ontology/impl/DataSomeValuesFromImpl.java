package it.unibz.krdb.obda.ontology.impl;

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

import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataSomeValuesFrom;
import it.unibz.krdb.obda.ontology.Datatype;

/**
 * DataSomeValuesFrom in OWL 2 QL Specification
 * <p>
 * DataSomeValuesFrom := 'DataSomeValuesFrom' '(' DataPropertyExpression DataRange ')'
 * <p>
 * Implements (partially) rule [D5] in methods isTop and isBottom<br>
 *    - the class expression is equivalent to top if the property is top (the data range cannot be empty)<br>
 *    - the class expression is equivalent to bot if the property is bot
 * 
 * @author Roman Kontchakov
 *
 */

public class DataSomeValuesFromImpl implements DataSomeValuesFrom {

	private static final long serialVersionUID = 593821958539751283L;
	
	private final DataPropertyExpression property;
	private final Datatype filler;
	private final String string;

	DataSomeValuesFromImpl(DataPropertyExpression property, Datatype filler) {
		this.property = property;
		this.filler = filler;
		this.string =  new StringBuilder().append("E ").append(property.toString()).append(".") 
						.append(filler.toString()).toString();
	}

	@Override
	public DataPropertyExpression getProperty() {
		return property;
	}

	@Override
	public Datatype getDatatype() {
		return filler;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (obj instanceof DataSomeValuesFromImpl) {
			DataSomeValuesFromImpl other = (DataSomeValuesFromImpl) obj;
			return property.equals(other.property) && filler.equals(other.filler);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return string.hashCode();
	}

	@Override
	public String toString() {
		return string;
	}

	@Override
	public boolean isBottom() {
		return property.isBottom();
	}

	@Override
	public boolean isTop() {
		return property.isTop();
	}
}
