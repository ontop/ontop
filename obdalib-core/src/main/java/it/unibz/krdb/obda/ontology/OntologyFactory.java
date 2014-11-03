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

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;

public interface OntologyFactory {

	public OClass createClass(String uri);

	public Datatype createDataType(Predicate p);
	
	public ObjectPropertyExpression createObjectProperty(String uri);
	
	public DataPropertyExpression createDataProperty(String uri);

	
	
	public Ontology createOntology();



	public SomeValuesFrom createPropertySomeRestriction(PropertyExpression role);
	
	public DataPropertyRangeExpression createDataPropertyRange(DataPropertyExpression role);
		
	
	public PropertyAssertion createPropertyAssertion(PropertyExpression attribute, ObjectConstant o1, Constant o2);
	
	public ClassAssertion createClassAssertion(OClass concept, ObjectConstant object);

}
