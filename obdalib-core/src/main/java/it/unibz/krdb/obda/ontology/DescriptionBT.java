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
 * Represents partially ordered classes and object and data properties
 * 
 * isTop/isBottom are two syntactic checks for the largest/smallest element in the partial order  
 * 
 * @author Roman Kontchakov
 *
 */


public interface DescriptionBT extends Description {
	/**
	 * checks (syntactically, irrespective of any ontology) whether the class or property expression 
	 *       is equivalent to owl:Nothing/owl:BottomObjectProperty/owl:BottomDataProperty
	 * 
	 * @return true if the expression is equivalent to the smallest element 
	 */
	public boolean isBottom();
	
	/**
	 * checks (syntactically, irrespective of any ontology) whether the class or property expression 
	 *          is equivalent to owl:Thing/owl:TopObjectProperty/owl:TopDataProperty
	 * 
	 * @return true if the expression is equivalent to largest element
	 */
	public boolean isTop();
}
