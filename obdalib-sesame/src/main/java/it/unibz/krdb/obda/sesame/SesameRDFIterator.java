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

import it.unibz.krdb.obda.model.DatatypeFactory;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

public class SesameRDFIterator extends RDFHandlerBase implements Iterator<Assertion> {
	
	private final OBDADataFactory obdafac = OBDADataFactoryImpl.getInstance();
	private final OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	private final DatatypeFactory dtfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();

	private BlockingQueue<Statement> buffer;
	private Iterator<Statement> iterator;
	private int size = 50000;
	
	private boolean endRDF = false;
	private boolean fromIterator = false;

	public SesameRDFIterator() {
		buffer = new ArrayBlockingQueue<Statement>(size, true);
	}

	public SesameRDFIterator(Iterator<Statement> it) {
		this.iterator = it;
		this.fromIterator = true;
	}

	public void startRDF() throws RDFHandlerException {
		endRDF = false;
	}

	public void endRDF() throws RDFHandlerException {
		endRDF = true;
	}

	@Override
	public void handleStatement(Statement st) throws RDFHandlerException {
		// Add statement to buffer
		try {
			buffer.put(st);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean hasNext() {
		if (fromIterator) {
			return iterator.hasNext();
		}
		else {
			Statement stmt = buffer.peek();
			if (stmt == null) {
				if (endRDF) {
					return false;
				} else {
					return true;
				}
			}
			return true;
		}
	}

	public Assertion next() {
		Statement stmt = null;
		if (fromIterator) {
			stmt = iterator.next();
		} else {
			try {
				stmt = buffer.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (stmt == null) {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
		}
		Assertion assertion = constructAssertion(stmt);
		return assertion;
	}

	/***
	 * Constructs an ABox assertion with the data from the current result set.
	 * This can be a Class, Object or Data Property assertion. It is a class
	 * assertion if the predicate is rdf:type. Its an Object property if the
	 * predicate is not type and the object is URI or BNode. Its a data property
	 * if the predicate is not rdf:type and the object is a Literal.
	 */
	private Assertion constructAssertion(Statement st) {
		Resource currSubject = st.getSubject();
		
		ObjectConstant c = null;
		if (currSubject instanceof URI) {
			c = obdafac.getConstantURI(currSubject.stringValue());
		} else if (currSubject instanceof BNode) {
			c = obdafac.getConstantBNode(currSubject.stringValue());
		} else {
			throw new RuntimeException("Unsupported subject found in triple: "	+ st.toString() + " (Required URI or BNode)");
		}

		URI currPredicate = st.getPredicate();
		Value currObject = st.getObject();

		Predicate currentPredicate = null;
		if (currObject instanceof Literal) {
			currentPredicate = obdafac.getDataPropertyPredicate(currPredicate.stringValue());
		} else {
			String predStringValue = currPredicate.stringValue();
			if (predStringValue.equals(OBDAVocabulary.RDF_TYPE)) {
//				if (!(predStringValue.endsWith("/owl#Thing"))
//						|| predStringValue.endsWith("/owl#Nothing")
//						|| predStringValue.endsWith("/owl#Ontology")) {
					currentPredicate = obdafac.getClassPredicate(currObject.stringValue());
//				} else {
//					return null;
//				}
			} else {
				currentPredicate = obdafac.getObjectPropertyPredicate(currPredicate.stringValue());
			}
		}
		
		// Create the assertion
		Assertion assertion = null;
		if (currentPredicate.getArity() == 1) {
			OClass concept = ofac.createClass(currentPredicate.getName());
			assertion = ofac.createClassAssertion(concept, c);
		} 
		else if (currentPredicate.getArity() == 2) {
			if (currObject instanceof URI) {
				ObjectConstant c2 = obdafac.getConstantURI(currObject.stringValue());
				ObjectPropertyExpression prop = ofac.createObjectProperty(currentPredicate.getName());
				assertion = ofac.createObjectPropertyAssertion(prop, c, c2);
			} 
			else if (currObject instanceof BNode) {
				ObjectConstant c2 = obdafac.getConstantBNode(currObject.stringValue());
				ObjectPropertyExpression prop = ofac.createObjectProperty(currentPredicate.getName());
				assertion = ofac.createObjectPropertyAssertion(prop, c, c2);
			} 
			else if (currObject instanceof Literal) {
				Literal l = (Literal) currObject;
				Predicate.COL_TYPE type = getColumnType(l.getDatatype());
				String lang = l.getLanguage();
				ValueConstant c2;
				if (lang == null) {
					c2 = obdafac.getConstantLiteral(l.getLabel(), type);
				} else {
					c2 = obdafac.getConstantLiteral(l.getLabel(), lang);
				}
				DataPropertyExpression prop = ofac.createDataProperty(currentPredicate.getName());
				assertion = ofac.createDataPropertyAssertion(prop, c, c2);			
			} else {
				throw new RuntimeException("Unsupported object found in triple: " + st.toString() + " (Required URI, BNode or Literal)");
			}
		} else {
			throw new RuntimeException("Unsupported statement: " + st.toString());
		}
		return assertion;
	}

	private Predicate.COL_TYPE getColumnType(URI datatype) {
		if (datatype == null) {
			return Predicate.COL_TYPE.LITERAL;
		} 
		else {
			String uri = datatype.stringValue();
			Predicate.COL_TYPE type = dtfac.getDataType(uri);
			if (type != null)
				return type;
			return Predicate.COL_TYPE.UNSUPPORTED;
		}			
	}

	/**
	 * Removes the entry at the beginning in the buffer.
	 */
	public void remove() {
		buffer.poll();
	}
}
