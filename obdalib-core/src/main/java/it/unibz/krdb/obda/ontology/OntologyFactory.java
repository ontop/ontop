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
 * 
 * @author Roman Kontchakov
 *
 */

public interface OntologyFactory {

	/**
	 * creates a modifiable ontology vocabulary 
	 * 
	 * @return created vocabulary
	 */
	
	public OntologyVocabulary createVocabulary();
	
	/**
	 * creates an ontology using a given vocabulary
	 * (the vocabulary is copied and fixes it)
	 * 
	 * @param voc
	 * @return
	 */
	
	public Ontology createOntology(ImmutableOntologyVocabulary voc);
	
	/**
	 * creates a class assertion 
	 *    (implements rule [C4])
	 * 
	 * @param ce 
	 * @param o
	 * @return
	 * @return null if ce is the top class ([C4])
	 * @throws InconsistentOntologyException if ce is the bottom class ([C4])
	 */
	
	public ClassAssertion createClassAssertion(OClass ce, ObjectConstant o) throws InconsistentOntologyException;
	
	
	/**
	 * creates an object property assertion 
	 * (ensures that the property is not inverse by swapping arguments if necessary)
	 *    (implements rule [O4])
	 * 
	 * @param ope
	 * @param o1
	 * @param o2
	 * @return null if ope is the top property ([O4])
	 * @throws InconsistentOntologyException if ope is the bottom property ([O4])
	 */
	
	public ObjectPropertyAssertion createObjectPropertyAssertion(ObjectPropertyExpression ope, ObjectConstant o1, ObjectConstant o2) throws InconsistentOntologyException;

	/**
	 * creates a data property assertion 
	 *    (implements rule [D4])
	 * 
	 * @param dpe
	 * @param o
	 * @param v
	 * @return null if dpe is the top property ([D4])
	 * @throws InconsistentOntologyException if dpe is the bottom property ([D4])
	 */
	
	public DataPropertyAssertion createDataPropertyAssertion(DataPropertyExpression dpe, ObjectConstant o, ValueConstant v) throws InconsistentOntologyException;
}
