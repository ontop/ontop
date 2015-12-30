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

import it.unibz.krdb.obda.model.ObjectConstant;

/**
 * Represents ObjectPropertyAssertion from the OWL 2 QL Specification
 * 
 * ObjectPropertyAssertion := 'ObjectPropertyAssertion' '(' axiomAnnotations 
 * 									ObjectPropertyExpression sourceIndividual targetIndividual ')'
 * ObjectPropertyExpression := ObjectProperty | InverseObjectProperty
 * InverseObjectProperty := 'ObjectInverseOf' '(' ObjectProperty ')'
 * 
 * Support for owl:topObjectProperty and owl:bottomObjectProperty
 *     - the inverses of the two coincide with themselves 
 * 
 * @author Roman Kontchakov
 *
 */

public interface ObjectPropertyAssertion extends Assertion {

	public ObjectPropertyExpression getProperty();
	
	public ObjectConstant getSubject();
	
	public ObjectConstant getObject();	
}
