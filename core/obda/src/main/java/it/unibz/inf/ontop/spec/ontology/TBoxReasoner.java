package it.unibz.inf.ontop.spec.ontology;

/*
 * #%L
 * ontop-reformulation-core
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


import java.util.Collection;

/**
 *  This is the interface for the class TBoxReasoner where we are able to retrieve all the connection built in our DAG 
 */
public interface TBoxReasoner {
	
	/**
	 * Return the DAG of properties
	 * 
	 * @return DAG 
	 */

	EquivalencesDAG<ObjectPropertyExpression> getObjectPropertyDAG();
	
	/**
	 * Return the DAG of properties
	 * 
	 * @return DAG 
	 */

	EquivalencesDAG<DataPropertyExpression> getDataPropertyDAG();

	/**
	 * Return the DAG of classes
	 * 
	 * @return DAG 
	 */

	EquivalencesDAG<ClassExpression> getClassDAG();
	
	/**
	 * Return the DAG of datatypes and data property ranges
	 * 
	 * @return DAG 
	 */

	EquivalencesDAG<DataRangeExpression> getDataRangeDAG();


	/**
	 * return all declared classes
	 *
	 * @return
	 */

	Collection<OClass> getClasses();

	/**
	 * return all declared object properties
	 *
	 * @return
	 */

	Collection<ObjectPropertyExpression> getObjectProperties();

	/**
	 * return all declared data properties
	 *
	 * @return
	 */

	Collection<DataPropertyExpression> getDataProperties();

	Collection<AnnotationProperty> getAnnotationProperties();

	/**
	 * check whether the class has been declared and return the class object
	 *
	 * @param uri
	 * @return
	 * @throws RuntimeException if the class has not been declared
	 */

	OClass getClass(String uri);


	/**
	 * check whether the object property has been declared and return the class object
	 *
	 * @param uri
	 * @return
	 * @throws RuntimeException if the object property has not been declared
	 */

	ObjectPropertyExpression getObjectProperty(String uri);


	/**
	 * check whether the data property has been declared and return the class object
	 *
	 * @param uri
	 * @return
	 * @throws RuntimeException if the data property has not been declared
	 */

	DataPropertyExpression getDataProperty(String uri);

	AnnotationProperty getAnnotationProperty(String uri);

	Datatype getDatatype(String uri);

	boolean containsClass(String uri);

	boolean containsObjectProperty(String uri);

	boolean containsDataProperty(String uri);

	boolean containsAnnotationProperty(String uri);

}
