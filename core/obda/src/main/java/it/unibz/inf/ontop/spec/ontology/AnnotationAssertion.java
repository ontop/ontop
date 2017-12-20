package it.unibz.inf.ontop.spec.ontology;

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


import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ObjectConstant;

/**
 * Represents AnnotationAssertion from the OWL 2 QL Specification
 *
 * AnnotationAssertion := 'AnnotationAssertion' '(' axiomAnnotations AnnotationProperty AnnotationSubject AnnotationValue ')'
 * AnnotationSubject := IRI | AnonymousIndividual
 * AnnotationValue := AnonymousIndividual | IRI | Literal
 * 
 *
 *
 */


public interface AnnotationAssertion extends Assertion {

	AnnotationProperty getProperty();
	
	ObjectConstant getSubject();
	
	Constant getValue();
}
