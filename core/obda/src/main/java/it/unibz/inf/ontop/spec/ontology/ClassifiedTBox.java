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

/**
 *  This is the interface for the class ClassifiedTBox where we are able
 *  to retrieve all the connection built in our DAG
 */
public interface ClassifiedTBox {
	
	/**
	 * object properties
	 * 
	 * @return object properties with their dag
	 */

	ClassifiedTBoxVocabularyCategory<ObjectPropertyExpression, ObjectPropertyExpression> objectProperties();
	
	/**
	 * data properties
	 * 
	 * @return data properties with their dag
	 */

	ClassifiedTBoxVocabularyCategory<DataPropertyExpression, DataPropertyExpression> dataProperties();

	/**
	 * classes
	 * 
	 * @return classes with their dag
	 */

	ClassifiedTBoxVocabularyCategory<ClassExpression, OClass> classes();
	
	/**
	 * datatypes and data property ranges
	 * 
	 * @return datatypes and data property ranges with their dag
	 */

	ClassifiedTBoxVocabularyCategory<DataRangeExpression, Datatype> dataRanges();


    /**
     * annotation properties
     *
     * @return annotation properties (without dag)
     */

    ClassifiedTBoxVocabularyCategory<AnnotationProperty, AnnotationProperty> annotationProperties();
}
