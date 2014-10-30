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

import java.io.Serializable;
import java.util.Set;

public interface Ontology extends Cloneable, Serializable {

	public void addAxiom(SubClassOfAxiom assertion);

	public void addAxiom(SubPropertyOfAxiom assertion);

	public void addAxiom(DisjointClassesAxiom assertion);

	public void addAxiom(DisjointPropertiesAxiom assertion);

	public void addAxiom(FunctionalPropertyAxiom assertion);

	public void addAxiom(ClassAssertion assertion);

	public void addAxiom(PropertyAssertion assertion);
	
	@Deprecated
	public void addAssertionWithCheck(SubClassOfAxiom assertion);
	@Deprecated
	public void addAssertionWithCheck(SubPropertyOfAxiom assertion);
	@Deprecated
	public void addAssertionWithCheck(DisjointClassesAxiom assertion);
	@Deprecated
	public void addAssertionWithCheck(DisjointPropertiesAxiom assertion);
	@Deprecated
	public void addAssertionWithCheck(FunctionalPropertyAxiom assertion);
	@Deprecated
	public void addAssertionWithCheck(ClassAssertion assertion);
	@Deprecated
	public void addAssertionWithCheck(PropertyAssertion assertion);


	public void declareClass(OClass c);

	public void declareObjectProperty(ObjectPropertyExpression role);

	public void declareDataProperty(DataPropertyExpression role);
	
	
	public Set<ObjectPropertyExpression> getObjectProperties();

	public Set<DataPropertyExpression> getDataProperties();
	
	public Set<OClass> getClasses();
	
	
	
	public void merge(Ontology onto);
	
	public Ontology clone();

	
	
	public Set<SubClassOfAxiom> getSubClassAxioms();

	public Set<SubPropertyOfAxiom> getSubPropertyAxioms();
	
	public Set<DisjointClassesAxiom> getDisjointClassesAxioms();
	
	public Set<DisjointPropertiesAxiom> getDisjointPropertiesAxioms();
	
	public Set<FunctionalPropertyAxiom> getFunctionalPropertyAxioms();
	
	public Set<ClassAssertion> getClassAssertions();

	public Set<PropertyAssertion> getPropertyAssertions();
}
