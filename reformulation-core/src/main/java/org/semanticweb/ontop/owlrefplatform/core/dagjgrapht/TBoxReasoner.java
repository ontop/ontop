package org.semanticweb.ontop.owlrefplatform.core.dagjgrapht;

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


import org.semanticweb.ontop.ontology.ClassExpression;
import org.semanticweb.ontop.ontology.DataPropertyExpression;
import org.semanticweb.ontop.ontology.DataRangeExpression;
import org.semanticweb.ontop.ontology.OClass;
import org.semanticweb.ontop.ontology.ObjectPropertyExpression;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.EquivalencesDAG;

/**
 *  This is the interface for the class TBoxReasoner where we are able to retrieve all the connection built in our DAG 
 */
public interface TBoxReasoner {
	
	/**
	 * Return the DAG of properties
	 * 
	 * @return DAG 
	 */

	public EquivalencesDAG<ObjectPropertyExpression> getObjectPropertyDAG();
	
	/**
	 * Return the DAG of properties
	 * 
	 * @return DAG 
	 */

	public EquivalencesDAG<DataPropertyExpression> getDataPropertyDAG();

	/**
	 * Return the DAG of classes
	 * 
	 * @return DAG 
	 */

	public EquivalencesDAG<ClassExpression> getClassDAG();
	
	/**
	 * Return the DAG of datatypes and data property ranges
	 * 
	 * @return DAG 
	 */

	public EquivalencesDAG<DataRangeExpression> getDataRangeDAG();
	
	/**
	 * 
	 * @param p: a description
	 * @return null if p is the representative of its own class **or p is not part of the graph**
	 *         the representative of the equivalence class otherwise  
	 */

	public OClass getClassRepresentative(OClass p);
	
	public ObjectPropertyExpression getObjectPropertyRepresentative(ObjectPropertyExpression p);
	
	public DataPropertyExpression getDataPropertyRepresentative(DataPropertyExpression p);
}
