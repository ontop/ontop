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
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.si.repository.SIRepositoryManager;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyFactoryImpl;
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

import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;

public class SemanticIndexRDFHandler extends AbstractRDFHandler {

	private final SIRepositoryManager repositoryManager;
	private final Connection connection;
	private final Ontology ontology;

    private static final int MAX_BUFFER_SIZE = 5000;

	private List<Statement> buffer;
	private int count;

	public SemanticIndexRDFHandler(SIRepositoryManager repositoryManager, Connection connection) {
		this.repositoryManager = repositoryManager;
		this.ontology = OntologyFactoryImpl.getInstance().createOntology();
		this.connection = connection;
		this.buffer = new ArrayList<>(MAX_BUFFER_SIZE);
		this.count = 0;
	}

	public void endRDF() throws RDFHandlerException {
		try {
			loadBuffer();
		}
		catch (SQLException e) {
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
		}
		catch (Exception e) {
			throw new RDFHandlerException(e);
		}
	}

	private void loadBuffer() throws SQLException {
		Iterator<Assertion> assertionIterator = buffer.stream()
				.map(st -> constructAssertion(st, ontology))
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
	private static Assertion constructAssertion(Statement st, Ontology ontology) {

		Resource subject = st.getSubject();
		final ObjectConstant c;
		if (subject instanceof IRI) {
			c = TERM_FACTORY.getConstantURI(subject.stringValue());
		}
		else if (subject instanceof BNode) {
			c = TERM_FACTORY.getConstantBNode(subject.stringValue());
		}
		else {
			throw new RuntimeException("Unsupported subject found in triple: "	+ st + " (Required URI or BNode)");
		}

        String predicateName = st.getPredicate().stringValue();
		Value object = st.getObject();

		// Create the assertion
		final Assertion assertion;
		try {
			if (predicateName.equals(IriConstants.RDF_TYPE)) {
                OClass oc = ontology.classes().create(object.stringValue());
				assertion = ontology.abox().createClassAssertion(oc, c);
			} 
			else {
				if (object instanceof IRI) {
                    ObjectPropertyExpression ope  = ontology.objectProperties().create(predicateName);
					ObjectConstant c2 = TERM_FACTORY.getConstantURI(object.stringValue());
					assertion = ontology.abox().createObjectPropertyAssertion(ope, c, c2);
				} 
				else if (object instanceof BNode) {
                    ObjectPropertyExpression ope  = ontology.objectProperties().create(predicateName);
					ObjectConstant c2 = TERM_FACTORY.getConstantBNode(object.stringValue());
					assertion = ontology.abox().createObjectPropertyAssertion(ope, c, c2);
				} 
				else if (object instanceof Literal) {
                    DataPropertyExpression dpe  = ontology.dataProperties().create(predicateName);
					Literal l = (Literal) object;
					Optional<String> lang = l.getLanguage();
					ValueConstant c2;
					if (!lang.isPresent()) {
						IRI datatype = l.getDatatype();
						Predicate.COL_TYPE type;
						if (datatype == null) {
							type = Predicate.COL_TYPE.LITERAL;
						} 
						else {
							type = TYPE_FACTORY.getDatatype(datatype);
							if (type == null)
								type = Predicate.COL_TYPE.UNSUPPORTED;
						}
						c2 = TERM_FACTORY.getConstantLiteral(l.getLabel(), type);
					} 
					else {
						c2 = TERM_FACTORY.getConstantLiteral(l.getLabel(), lang.get());
					}
					assertion = ontology.abox().createDataPropertyAssertion(dpe, c, c2);
				} 
				else {
					throw new RuntimeException("Unsupported object found in triple: " + st + " (Required URI, BNode or Literal)");
				}
			} 
		}
		catch (InconsistentOntologyException e) {
			throw new RuntimeException("InconsistentOntologyException: " + st);
		}
		return assertion;
	}

	public int getCount() {
		return count;
	}
}
