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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 *  This is the interface for the class ClassifiedTBox where we are able
 *  to retrieve all the connection built in our DAG
 */
public interface ClassifiedTBox {
	
	/**
	 * object properties
	 * 
	 * @return object properties
	 */

	OntologyVocabularyCategory<ObjectPropertyExpression> objectProperties();

	EquivalencesDAG<ObjectPropertyExpression> objectPropertiesDAG();
	
	/**
	 * data properties
	 * 
	 * @return data properties
	 */

	OntologyVocabularyCategory<DataPropertyExpression> dataProperties();

	EquivalencesDAG<DataPropertyExpression> dataPropertiesDAG();

	/**
	 * classes
	 * 
	 * @return classes
	 */

	OntologyVocabularyCategory<OClass> classes();

    EquivalencesDAG<ClassExpression> classesDAG();
	
	/**
	 * datatypes and data property ranges
	 * 
	 * @return datatypes and data property ranges with their dag
	 */

    EquivalencesDAG<DataRangeExpression> dataRangesDAG();


    // DISJOINTNESS

    ImmutableList<NaryAxiom<ClassExpression>> disjointClasses();

    ImmutableList<NaryAxiom<ObjectPropertyExpression>> disjointObjectProperties();

    ImmutableList<NaryAxiom<DataPropertyExpression>> disjointDataProperties();


    // REFLEXIVITY / IRREFLEXIVITY

    ImmutableSet<ObjectPropertyExpression> reflexiveObjectProperties();

    ImmutableSet<ObjectPropertyExpression> irreflexiveObjectProperties();

    // FUNCTIONALITY

    ImmutableSet<ObjectPropertyExpression> functionalObjectProperties();

    ImmutableSet<DataPropertyExpression> functionalDataProperties();

}
