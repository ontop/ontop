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
import it.unibz.krdb.obda.model.ValueConstant;

/**
 * factory for ABox assertions 
 * 
 * IMPORTANT: this factory does NOT check whether the class / property has been declared
 *
 * (the implementation relies on OntologyFactory and so, it checks for top / bottom concept / property, 
 * 			@see rules [C4], [O4], [D4]) 
 *
 * @author Roman Kontchakov
 *
 */

public interface AssertionFactory {

	/**
	 * creates a class assertion 
	 * 
	 * @param className
	 * @param o
	 * @return
	 * @return null if it is top class ([C4])
	 * @throws InconsistentOntologyException if it is the bottom class ([C4])
	 */
	
	public ClassAssertion createClassAssertion(String className, ObjectConstant o) throws InconsistentOntologyException;

	/**
	 * creates an object property assertion 
	 * 
	 * @param propertyName
	 * @param o1
	 * @param o2
	 * @return null if it is top object property ([O4])
	 * @throws InconsistentOntologyException if it is the bottom data property ([O4])
	 */
	
	public ObjectPropertyAssertion createObjectPropertyAssertion(String propertyName, ObjectConstant o1, ObjectConstant o2) throws InconsistentOntologyException;

	/**
	 * creates a data property assertion (without checking any vocabulary)
	 * 
	 * @param propertyName
	 * @param o
	 * @param v
	 * @return null if it is top data property ([D4])
	 * @throws InconsistentOntologyException if it is the bottom data property ([D4])
	 */
	
	public DataPropertyAssertion createDataPropertyAssertion(String propertyName, ObjectConstant o, ValueConstant v) throws InconsistentOntologyException;
		
}
