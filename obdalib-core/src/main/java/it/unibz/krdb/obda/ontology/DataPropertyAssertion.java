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
 * Represents DataPropertyAssertion from OWL 2 QL Specification
 * 
 * DataPropertyAssertion := 'DataPropertyAssertion' '(' axiomAnnotations DataPropertyExpression Individual Literal ')'
 * 
 */

import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.ValueConstant;

/**
 * Represents DataPropertyAssertion from the OWL 2 QL Specification
 * 
 * DataPropertyAssertion := 'DataPropertyAssertion' '(' axiomAnnotations 
 * 								DataPropertyExpression sourceIndividual targetValue ')'
 * DataPropertyExpression := DataProperty
 * 
 *  Support for owl:topDataProperty and owl:bottomDataProperty
 * 
 * @author Roman Kontchakov
 *
 */


public interface DataPropertyAssertion extends Assertion {

	public DataPropertyExpression getProperty();
	
	public ObjectConstant getSubject();
	
	public ValueConstant getValue();	
}
