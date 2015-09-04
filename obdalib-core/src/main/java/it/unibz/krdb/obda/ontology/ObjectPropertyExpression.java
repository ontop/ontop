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

import it.unibz.krdb.obda.model.Predicate;

/**
 * Represents ObjectPropertyExpression from the OWL 2 QL Specification
 * 
 * ObjectPropertyExpression := ObjectProperty | InverseObjectProperty
 * InverseObjectProperty := 'ObjectInverseOf' '(' ObjectProperty ')'
 * 
 * Support for owl:topObjectProperty and owl:bottomObjectProperty
 *     - the inverses of the two coincide with themselves 
 * 
 * @author Roman Kontchakov
 *
 */

public interface ObjectPropertyExpression extends DescriptionBT {

	/**
	 * checks whether the property expression is the inverse of an object property
	 *
	 * @return true if the property is the inverse (false otherwise)
	 */
	
	public boolean isInverse();

	/**
	 * the name of the object property
	 * 
	 * @return the predicate symbol that corresponds to the object property name
	 */
	
	public Predicate getPredicate();

	public String getName();
	
	/**
	 * the inverse of the object property
	 * <p>
	 *    (the inverse of an inverted property is the property itself)
	 * <p>   
	 *    (the inverses of owl:topObjectProperty and owl:bottomObjectProperty
	 *      are the properties themselves)
	 * 
	 * @return
	 */
	
	public ObjectPropertyExpression getInverse();

	/**
	 * the domain class expression for the object property 
	 * <p>
	 * (in DL, the unqualified existential quantifier \exists R for property R)
	 * 
	 * @return class expression for the domain
	 */
	
	public ObjectSomeValuesFrom getDomain();
	
	/**
	 * the range class expression for the object property
	 * <p> 
	 * (the range of a property coincides with the domain of its inverse)
	 * <p>
	 * (in DL, the unqualified existential quantifier \exists R^- for property R)
	 * 
	 * @return class expression for the range
	 */
	
	public ObjectSomeValuesFrom getRange();
}
