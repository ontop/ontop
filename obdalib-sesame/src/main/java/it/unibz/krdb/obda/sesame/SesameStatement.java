package it.unibz.krdb.obda.sesame;

/*
 * #%L
 * ontop-obdalib-sesame
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
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

public class SesameStatement implements Statement {
    private static final long serialVersionUID = 3398547980791013746L;
    
	private Resource subject = null;
	private URI predicate = null;
	private Value object = null;
	private Resource context = null;

	private SesameHelper helper = new SesameHelper();

	public SesameStatement(Assertion assertion) {
		
		if (assertion instanceof ObjectPropertyAssertion) {
			//object or data property assertion
			ObjectPropertyAssertion ba = (ObjectPropertyAssertion) assertion;
			ObjectConstant subj = ba.getSubject();
			Predicate pred = ba.getProperty().getPredicate();
			ObjectConstant obj = ba.getObject();
			
			// convert string into respective type
			subject = helper.getResource(subj);
			predicate = helper.createURI(pred.getName().toString()); // URI	
			object = helper.getResource(obj);
		} 
		else if (assertion instanceof DataPropertyAssertion) {
			//object or data property assertion
			DataPropertyAssertion ba = (DataPropertyAssertion) assertion;
			ObjectConstant subj = ba.getSubject();
			String pred = ba.getProperty().getName();
			ValueConstant obj = ba.getValue();
			
			// convert string into respective type
			subject = helper.getResource(subj);	
			predicate = helper.createURI(pred); // URI
			
			if (obj instanceof ValueConstant)
				object = helper.getLiteral((ValueConstant)obj);		
			else 
				throw new RuntimeException("Invalid constant as object!" + obj);
		} 
		else if (assertion instanceof ClassAssertion) { 
			//class assertion
			ClassAssertion ua = (ClassAssertion) assertion;
			ObjectConstant subj = ua.getIndividual();
			Predicate obj = ua.getConcept().getPredicate();
			
			// convert string into respective type
			subject = helper.getResource(subj);
			predicate = helper.createURI(OBDAVocabulary.RDF_TYPE); // URI
			object = helper.createURI(obj.getName().toString());	
		}
	}
	

	public Resource getSubject() {
		return subject;
	}

	public URI getPredicate() {
		return predicate;
	}

	public Value getObject() {
		return object;
	}

	public Resource getContext() {
		// TODO Auto-generated method stub
		return context;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Statement)) return false;

        Statement that = (Statement) o;

        Resource thatContext = that.getContext();
        if (context != null ? !context.equals(thatContext) : thatContext != null) return false;
        Value thatObject = that.getObject();
        if (object != null ? !object.equals(thatObject) : thatObject != null) return false;
        URI thatPredicate = that.getPredicate();
        if (predicate != null ? !predicate.equals(thatPredicate) : thatPredicate != null) return false;
        Resource thatSubject = that.getSubject();
        if (subject != null ? !subject.equals(thatSubject) : thatSubject != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int contextComponent = context != null ? context.hashCode() : 0;
        int subjectComponent = subject != null ? subject.hashCode() : 0;
        int predicateComponent = predicate != null ? predicate.hashCode() : 0;
        int objectComponent = object != null ? object.hashCode() : 0;
        return 1013 * contextComponent + 961 * subjectComponent + 31 * predicateComponent + objectComponent;
    }

	@Override
	public String toString() {
		return "("+subject+", "+predicate+", "+object+")";
	}
}
