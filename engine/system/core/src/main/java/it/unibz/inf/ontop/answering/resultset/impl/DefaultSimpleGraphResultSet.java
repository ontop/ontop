package it.unibz.inf.ontop.answering.resultset.impl;

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.algebra.BNodeGenerator;
import org.eclipse.rdf4j.query.algebra.Extension;
import org.eclipse.rdf4j.query.algebra.ExtensionElem;
import org.eclipse.rdf4j.query.algebra.ProjectionElem;
import org.eclipse.rdf4j.query.algebra.ProjectionElemList;
import org.eclipse.rdf4j.query.algebra.ValueConstant;
import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.sail.SailException;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.reformulation.input.ConstructTemplate;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.answering.resultset.SimpleGraphResultSet;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.RDF4JHelper;

public class DefaultSimpleGraphResultSet implements SimpleGraphResultSet {

	private final StatementIterator iterator;

	private final int fetchSize;

	private final TermFactory termFactory;

	public DefaultSimpleGraphResultSet(
			TupleResultSet tupleResultSet,
			ConstructTemplate constructTemplate,
			TermFactory termFactory,
			org.apache.commons.rdf.api.RDF rdfFactory,
			boolean preloadStatements)
			throws OntopConnectionException {
		this.termFactory = termFactory;
		this.fetchSize = tupleResultSet.getFetchSize();
		try {
			iterator =
					new StatementIterator(
							tupleResultSet, constructTemplate, termFactory, rdfFactory, preloadStatements);
		} catch (OntopResultConversionException e) {
			throw new SailException(e.getCause());
		}
	}

	@Override
	public boolean hasNext() {
		return iterator().hasNext();
	}

	@Override
	public RDFFact next() {
		Statement statement = iterator().next();
		ObjectConstant subjectConstant = null;
		IRIConstant propertyConstant;
		RDFConstant objectConstant;
		if (statement.getSubject() instanceof IRI) {
			subjectConstant = termFactory.getConstantIRI(statement.getSubject().stringValue());
		} else if (statement.getSubject() instanceof BNode) {
			subjectConstant = termFactory.getConstantBNode(statement.getSubject().stringValue());
		}
		propertyConstant = termFactory.getConstantIRI(statement.getPredicate().stringValue());
		if (statement.getObject() instanceof IRI) {
			objectConstant = termFactory.getConstantIRI(statement.getObject().stringValue());
		} else if (statement.getObject() instanceof BNode) {
			objectConstant = termFactory.getConstantBNode(statement.getObject().stringValue());
		} else {
			objectConstant =
					termFactory.getRDFLiteralConstant(statement.getObject().stringValue(), XSD.STRING);
		}
		return RDFFact.createTripleFact(subjectConstant, propertyConstant, objectConstant);
	}

	@Override
	public Iterator<Statement> iterator() {
		return iterator;
	}

	@Override
	public void close() throws OntopConnectionException {
		// doesn't do anything, kept for conformance
	}

	/**
	 * The method to actually process the current result set Row. Construct a list of assertions from
	 * the current result set row. In case of describe it is called to process and store all the
	 * results from a resultset. In case of construct it is called upon next, to process the only
	 * current result set.
	 */
	@Override
	public int getFetchSize() {
		return fetchSize;
	}

	@Override
	public void addNewResult(Statement statement) {
		iterator.addNewStatement(statement);
	}

	@Override
	public void addStatementClosable(AutoCloseable sqlStatement) {
		iterator.sqlStatement = sqlStatement;
	}

	private static class StatementIterator implements Iterator<Statement> {
		private final TupleResultSet resultSet;
		private final ConstructTemplate constructTemplate;
		private final TermFactory termFactory;
		private final org.apache.commons.rdf.api.RDF rdfFactory;
		private ImmutableMap<String, ValueExpr> extMap;
		private final Queue<Statement> statementBuffer;
		private final ValueFactory valueFactory;
		private final SecureRandom random;
		private AutoCloseable sqlStatement;
		private final byte[] salt;
		private final boolean preloadStatements;

		private StatementIterator(
				TupleResultSet resultSet,
				ConstructTemplate constructTemplate,
				TermFactory termFactory,
				org.apache.commons.rdf.api.RDF rdfFactory,
				boolean preloadStatements)
				throws OntopConnectionException, OntopResultConversionException {
			this.resultSet = resultSet;
			this.constructTemplate = constructTemplate;
			this.termFactory = termFactory;
			this.rdfFactory = rdfFactory;
			intExtMap();
			this.statementBuffer = new LinkedList<>();
			this.valueFactory = SimpleValueFactory.getInstance();
			this.random = new SecureRandom();
			this.salt = initByteSalt();
			this.preloadStatements = preloadStatements;
			if (preloadStatements) {
				while (resultSet.hasNext()) {
					addStatementsFromSet(resultSet.next());
				}
				if (sqlStatement != null) {
					try {
						sqlStatement.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				resultSet.close();
			}
		}

		@Override
		public boolean hasNext() {
			try {
				if (statementBuffer.isEmpty() && resultSetHasNext()) {
					addStatementsFromSet(resultSet.next());
				}
				boolean hasNext = !statementBuffer.isEmpty();
				if (!hasNext && !preloadStatements) {
					resultSet.close();
					if (sqlStatement != null) {
						sqlStatement.close();
					}
				}
				return hasNext;
			} catch (Exception e) {
				throw new SailException(e.getMessage());
			}
		}

		@Override
		public Statement next() {
			try {
				if (statementBuffer.isEmpty() && !preloadStatements) {
					resultSet.close();
					if (sqlStatement != null) {
						sqlStatement.close();
					}
				}
				return statementBuffer.remove();
			} catch (Exception e) {
				throw new SailException(e.getMessage());
			}
		}

		private void addStatementsFromSet(OntopBindingSet bindingSet)
				throws OntopResultConversionException {
			for (ProjectionElemList peList : constructTemplate.getProjectionElemList()) {
				int size = peList.getElements().size();
				for (int i = 0; i < size / 3; i++) {
					ObjectConstant subjectConstant =
							(ObjectConstant) getConstant(peList.getElements().get(i * 3), bindingSet);
					IRIConstant propertyConstant =
							(IRIConstant) getConstant(peList.getElements().get(i * 3 + 1), bindingSet);
					RDFConstant objectConstant =
							(RDFConstant) getConstant(peList.getElements().get(i * 3 + 2), bindingSet);
					if (subjectConstant != null && propertyConstant != null && objectConstant != null) {
						addStatement(subjectConstant, propertyConstant, objectConstant);
					}
				}
			}
		}

		private void addStatement(
				ObjectConstant subjectConstant, IRIConstant propertyConstant, RDFConstant objectConstant) {
			statementBuffer.add(
					valueFactory.createStatement(
							RDF4JHelper.getResource(subjectConstant, salt),
							valueFactory.createIRI(propertyConstant.getIRI().getIRIString()),
							RDF4JHelper.getValue(objectConstant, salt)));
		}

		private Constant getConstant(ProjectionElem node, OntopBindingSet bindingSet)
				throws OntopResultConversionException {
			Constant constant = null;
			String nodeName = node.getSourceName();
			ValueExpr ve = null;

			if (extMap != null) {
				ve = extMap.get(nodeName);
			}

			if (ve instanceof ValueConstant) {
				ValueConstant vc = (ValueConstant) ve;
				if (vc.getValue() instanceof IRI) {
					constant = termFactory.getConstantIRI(rdfFactory.createIRI(vc.getValue().stringValue()));
				} else if (vc.getValue() instanceof Literal) {
					constant = termFactory.getRDFLiteralConstant(vc.getValue().stringValue(), XSD.STRING);
				} else {
					constant = termFactory.getConstantBNode(vc.getValue().stringValue());
				}
			} else if (ve instanceof BNodeGenerator) {
				// See https://www.w3.org/TR/sparql11-query/#tempatesWithBNodes
				String rowId = bindingSet.getRowUUIDStr();

				String label =
						Optional.ofNullable(((BNodeGenerator) ve).getNodeIdExpr())
								// If defined, we expected the b-node label to be constant (as appearing in the
								// CONSTRUCT block)
								.filter(e -> e instanceof ValueConstant)
								.map(v -> ((ValueConstant) v).getValue().stringValue())
								.map(s -> s + rowId)
								.orElseGet(() -> nodeName + rowId);

				constant = termFactory.getConstantBNode(label);
			} else {
				constant = bindingSet.getConstant(nodeName);
			}
			return constant;
		}

		private byte[] initByteSalt() {
			byte[] salt = new byte[20];
			random.nextBytes(salt);
			return salt;
		}

		private void addNewStatement(Statement statement) {
			statementBuffer.add(statement);
		}

		private void intExtMap() {
			Extension ex = constructTemplate.getExtension();
			if (ex != null) {
				extMap =
						ex.getElements().stream()
								.collect(ImmutableCollectors.toMap(ExtensionElem::getName, ExtensionElem::getExpr));
			} else {
				extMap = null;
			}
		}

		private boolean resultSetHasNext() {
			try {
				if (preloadStatements) {
					return false;
				}
				if (!resultSet.isConnectionAlive()) {
					return false;
				}
				return resultSet.hasNext();
			} catch (OntopConnectionException | OntopResultConversionException e) {
				throw new SailException(e.getCause());
			}
		}
	}
}
