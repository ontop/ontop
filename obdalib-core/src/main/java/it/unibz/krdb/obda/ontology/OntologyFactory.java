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
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;

public interface OntologyFactory {

	@Deprecated
	public Ontology createOntology();

	public Ontology createOntology(OntologyVocabularyBuilder vb);
	
	public OntologyVocabularyBuilder createVocabularyBuilder();
	
	public OClass createClass(String uri);

	public OClass getThing();

	public OClass getNothing();
	
	
	
	public Datatype createDataType(Predicate.COL_TYPE type);
	
	
	
	public ObjectPropertyExpression createObjectProperty(String uri);
	
	public ObjectPropertyExpression getTopObjectProperty();
	
	public ObjectPropertyExpression getBottomObjectProperty();

	
	
	public DataPropertyExpression createDataProperty(String uri);

	public DataPropertyExpression getTopDataProperty();
	
	public DataPropertyExpression getBottomDataProperty();
	
	
	/**
	 * creates an object property assertion 
	 * (ensures that the property is not inverse by swapping arguments if necessary)
	 * 
	 * @param prop
	 * @param o1
	 * @param o2
	 * @return
	 */
	
	public ObjectPropertyAssertion createObjectPropertyAssertion(ObjectPropertyExpression prop, ObjectConstant o1, ObjectConstant o2);

	/**
	 * creates a data property assertion 
	 * 
	 * @param prop
	 * @param o1
	 * @param o2
	 * @return
	 */
	
	public DataPropertyAssertion createDataPropertyAssertion(DataPropertyExpression prop, ObjectConstant o1, ValueConstant o2);

	/**
	 * creates a class assertion 
	 * 
	 * @param concept
	 * @param o
	 * @return
	 */
	
	public ClassAssertion createClassAssertion(OClass concept, ObjectConstant o);

}
