package org.semanticweb.ontop.sesame;

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

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.semanticweb.ontop.model.BNode;
import org.semanticweb.ontop.model.Constant;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.URIConstant;
import org.semanticweb.ontop.model.ValueConstant;
import org.semanticweb.ontop.model.Predicate.COL_TYPE;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.ontology.Assertion;
import org.semanticweb.ontop.ontology.BinaryAssertion;
import org.semanticweb.ontop.ontology.ClassAssertion;
import org.semanticweb.ontop.ontology.UnaryAssertion;

public class SesameStatement implements Statement {
    private static final long serialVersionUID = 3398547980791013746L;
	private Resource subject = null;
	private URI predicate = null;
	private Value object = null;
	private Resource context = null;
	private ValueFactory fact = new ValueFactoryImpl();

	public SesameStatement(Assertion assertion) {
		
		Constant subj;

		if (assertion instanceof PropertyAssertion) {
			//object or data property assertion
			PropertyAssertion ba = (PropertyAssertion) assertion;
			subj = ba.getSubject();
			Predicate pred = ba.getProperty().getPredicate();
			Constant obj = ba.getValue2();
			
			// convert string into respective type
			if (subj instanceof BNode)
				subject = fact.createBNode(((BNode) subj).getName());
			else if (subj instanceof URIConstant)
				subject = fact.createURI(subj.getValue());
			else if (subj instanceof ValueConstant)
				throw new RuntimeException("Invalid ValueConstant as subject!");
			
			predicate = fact.createURI(pred.getName().toString()); // URI
			
			if (obj instanceof BNode)
				object = fact.createBNode(((BNode) obj).getName());
			else if (obj instanceof URIConstant)
				object = fact.createURI(obj.getValue());
			else if (obj instanceof ValueConstant)
				object = getLiteral((ValueConstant)obj);
			
			
		} else if (assertion instanceof ClassAssertion) { 
			//class assertion
			ClassAssertion ua = (ClassAssertion) assertion;
			subj = ua.getIndividual();
			String pred = OBDAVocabulary.RDF_TYPE;
			Predicate obj = ua.getConcept().getPredicate();
			
			// convert string into respective type
			if (subj instanceof BNode)
				subject = fact.createBNode(((BNode) subj).getName());
			else if (subj instanceof URIConstant)
				subject = fact.createURI(subj.getValue());
			else if (subj instanceof ValueConstant)
				throw new RuntimeException("Invalid ValueConstant as subject!");
			
			predicate = fact.createURI(pred); // URI
		
			object = fact.createURI(obj.getName().toString());
			
		}
	}
	
	public Literal getLiteral(ValueConstant literal)
	{
		URI datatype = null;
		if (literal.getType() == COL_TYPE.BOOLEAN)
			datatype = fact
					.createURI(OBDAVocabulary.XSD_BOOLEAN_URI);
		else if (literal.getType() == COL_TYPE.DATETIME)
			datatype = fact
					.createURI(OBDAVocabulary.XSD_DATETIME_URI);
        else if (literal.getType() == COL_TYPE.DATE)
            datatype = fact
                    .createURI(OBDAVocabulary.XSD_DATE_URI);
        else if (literal.getType() == COL_TYPE.TIME)
            datatype = fact
                    .createURI(OBDAVocabulary.XSD_TIME_URI);
        else if (literal.getType() == COL_TYPE.YEAR)
            datatype = fact
                    .createURI(OBDAVocabulary.XSD_YEAR_URI);
		else if (literal.getType() == COL_TYPE.DECIMAL)
			datatype = fact
					.createURI(OBDAVocabulary.XSD_DECIMAL_URI);
		else if (literal.getType() == COL_TYPE.DOUBLE)
			datatype = fact
					.createURI(OBDAVocabulary.XSD_DOUBLE_URI);
        else if (literal.getType() == COL_TYPE.FLOAT)
            datatype = fact
                    .createURI(OBDAVocabulary.XSD_FLOAT_URI);
		else if (literal.getType() == COL_TYPE.INTEGER)
			datatype = fact
					.createURI(OBDAVocabulary.XSD_INTEGER_URI);
        else if (literal.getType() == COL_TYPE.NON_NEGATIVE_INTEGER)
            datatype = fact
                    .createURI(OBDAVocabulary.XSD_NON_NEGATIVE_INTEGER_URI);
        else if (literal.getType() == COL_TYPE.POSITIVE_INTEGER)
            datatype = fact
                    .createURI(OBDAVocabulary.XSD_NEGATIVE_INTEGER_URI);
        else if (literal.getType() == COL_TYPE.POSITIVE_INTEGER)
            datatype = fact
                    .createURI(OBDAVocabulary.XSD_POSITIVE_INTEGER_URI);
        else if (literal.getType() == COL_TYPE.NON_POSITIVE_INTEGER)
            datatype = fact
                    .createURI(OBDAVocabulary.XSD_NON_POSITIVE_INTEGER_URI);
        else if (literal.getType() == COL_TYPE.UNSIGNED_INT)
            datatype = fact
                    .createURI(OBDAVocabulary.XSD_UNSIGNED_INT_URI);
        else if (literal.getType() == COL_TYPE.INT)
            datatype = fact
                    .createURI(OBDAVocabulary.XSD_INT_URI);
        else if (literal.getType() == COL_TYPE.LONG)
            datatype = fact
                    .createURI(OBDAVocabulary.XSD_LONG_URI);
		else if (literal.getType() == COL_TYPE.LITERAL)
			datatype = null;
		else if (literal.getType() == COL_TYPE.LITERAL_LANG)
			{
				datatype = null;
				return fact.createLiteral(literal.getValue(), literal.getLanguage());
			}
		else if (literal.getType() == COL_TYPE.OBJECT)
			datatype = fact
					.createURI(OBDAVocabulary.XSD_STRING_URI);
		else if (literal.getType() == COL_TYPE.STRING)
			datatype = fact
					.createURI(OBDAVocabulary.XSD_STRING_URI);
		Literal value = fact.createLiteral(literal.getValue(), datatype);
		return value;
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
	public String toString()
	{
		return "("+subject+", "+predicate+", "+object+")";
	}
}
