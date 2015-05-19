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
 * Represents subClassExpression in OWL 2 QL specification.
 * 
 * subClassExpression := Class | subObjectSomeValuesFrom | DataSomeValuesFrom
 * 
 * where 
 * 		Class := IRI
 * 		subObjectSomeValuesFrom := 'ObjectSomeValuesFrom' '(' ObjectPropertyExpression owl:Thing ')'
 * 		DataSomeValuesFrom := 'DataSomeValuesFrom' '(' DataPropertyExpression { DataPropertyExpression } DataRange ')'
 * 
 * @author roman
 *
 */

public interface ClassExpression extends Description {
	
	/**
	 * checks (syntactically, irrespective of any ontology) whether the class expression is equivalent to owl:Nothing
	 * <p>
	 * owl:Nothing or the domain/range of owl:BottomObjectProperty or the domain of owl:BottomDataProperty
	 * 
	 * @return true if the class expression is equivalent to owl:Nothing 
	 */
	public boolean isNothing();
	
	/**
	 * checks (syntactically, irrespective of any ontology) whether the class expression is equivalent to owl:Thing
	 * <p>
	 * owl:Thing or the domain/range of owl:TopObjectProperty or the domain of owl:TopDataProperty
	 * 
	 * @return true if the class expression is equivalent to owl:Thing
	 */
	public boolean isThing();
}
