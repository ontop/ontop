package it.unibz.inf.ontop.rdf4j.rio.helpers;

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

import it.unibz.inf.ontop.model.IriConstants;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.si.repository.SIRepositoryManager;
import it.unibz.inf.ontop.spec.ontology.Assertion;
import it.unibz.inf.ontop.spec.ontology.AssertionFactory;
import it.unibz.inf.ontop.spec.ontology.InconsistentOntologyException;
import it.unibz.inf.ontop.spec.ontology.impl.AssertionFactoryImpl;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;


public class SemanticIndexRDFHandler extends AbstractRDFHandler {

	private static final AssertionFactory ASSERTION_FACTORY = AssertionFactoryImpl.getInstance();
	private static final RDF RDF_FACTORY = new SimpleRDF();
	private final SIRepositoryManager repositoryManager;
	private final Connection connection;

	private List<Statement> buffer;
	private int MAX_BUFFER_SIZE = 5000;
	private int count;
	private final TermFactory termFactory;
	private final TypeFactory typeFactory;

	public SemanticIndexRDFHandler(SIRepositoryManager repositoryManager, Connection connection,
								   TermFactory termFactory, TypeFactory typeFactory) {
		this.repositoryManager = repositoryManager;
		this.connection = connection;
		this.termFactory = termFactory;
		this.typeFactory = typeFactory;
		this.buffer = new ArrayList<>(MAX_BUFFER_SIZE);
		this.count = 0;
	}

	public void endRDF() throws RDFHandlerException {
		try {
			loadBuffer();
		} catch (SQLException e) {
			throw new RDFHandlerException(e);
		}
	}

	@Override
	public void handleStatement(Statement st) throws RDFHandlerException {
		// Add statement to buffer
		try {
			buffer.add(st);
			if (buffer.size() == MAX_BUFFER_SIZE) {
				loadBuffer();
			}
		} catch (Exception e) {
			throw new RDFHandlerException(e);
		}
	}

	private void loadBuffer() throws SQLException {
		Iterator<Assertion> assertionIterator = buffer.stream()
				.map(this::constructAssertion)
				.iterator();
		count += repositoryManager.insertData(connection, assertionIterator, 5000, 500);
		buffer.clear();
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
		if (currSubject instanceof IRI) {
			c = termFactory.getConstantURI(currSubject.stringValue());
		} else if (currSubject instanceof BNode) {
			c = termFactory.getConstantBNode(currSubject.stringValue());
		} else {
			throw new RuntimeException("Unsupported subject found in triple: "	+ st.toString() + " (Required URI or BNode)");
		}

		IRI currPredicate = st.getPredicate();
		Value currObject = st.getObject();

		IRI currentIRI;
		boolean isProperty;
		if (currObject instanceof Literal) {
			currentIRI = currPredicate;
			isProperty = true;
		} else {
			String predStringValue = currPredicate.stringValue();
			if (predStringValue.equals(IriConstants.RDF_TYPE)) {
					currentIRI = (IRI)currObject;
					isProperty = false;
			} else {
				currentIRI = currPredicate;
				isProperty = true;
			}
		}
		
		// Create the assertion
		Assertion assertion;
		try {
			if (!isProperty) {
				assertion = ASSERTION_FACTORY.createClassAssertion(currentIRI.stringValue(), c);
			} 
			else {
				if (currObject instanceof IRI) {
					ObjectConstant c2 = termFactory.getConstantURI(currObject.stringValue());
					assertion = ASSERTION_FACTORY.createObjectPropertyAssertion(currentIRI.stringValue(), c, c2);
				} 
				else if (currObject instanceof BNode) {
					ObjectConstant c2 = termFactory.getConstantBNode(currObject.stringValue());
					assertion = ASSERTION_FACTORY.createObjectPropertyAssertion(currentIRI.stringValue(), c, c2);
				} 
				else if (currObject instanceof Literal) {
					Literal l = (Literal) currObject;				
					Optional<String> lang = l.getLanguage();
					ValueConstant c2;
					if (!lang.isPresent()) {
						IRI datatype = l.getDatatype();
						RDFDatatype type;
						
						if (datatype == null) {
							type = typeFactory.getXsdStringDatatype();
						} 
						else {
							type = typeFactory.getOptionalDatatype(RDF_FACTORY.createIRI(datatype.stringValue()))
									.orElseGet(typeFactory::getUnsupportedDatatype);
						}			
						
						c2 = termFactory.getConstantLiteral(l.getLabel(), type);
					} 
					else {
						c2 = termFactory.getConstantLiteral(l.getLabel(), lang.get());
					}
					assertion = ASSERTION_FACTORY.createDataPropertyAssertion(currentIRI.stringValue(), c, c2);
				} 
				else {
					throw new RuntimeException("Unsupported object found in triple: " + st.toString() + " (Required URI, BNode or Literal)");
				}
			}
		}
		catch (InconsistentOntologyException e) {
			throw new RuntimeException("InconsistentOntologyException: " + currentIRI + " " + currSubject + " " + currObject);
		}
		return assertion;
	}

	public int getCount() {
		return count;
	}
}
