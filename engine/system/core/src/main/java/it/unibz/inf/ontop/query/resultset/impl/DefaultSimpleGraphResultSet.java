package it.unibz.inf.ontop.query.resultset.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.query.ConstructTemplate;
import it.unibz.inf.ontop.query.resultset.GraphResultSet;
import it.unibz.inf.ontop.query.resultset.OntopBindingSet;
import it.unibz.inf.ontop.query.resultset.OntopCloseableIterator;
import it.unibz.inf.ontop.query.resultset.TupleResultSet;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.RDF;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.algebra.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

public class DefaultSimpleGraphResultSet implements GraphResultSet {

	private final TupleResultSet resultSet;
	private final ConstructTemplate constructTemplate;
	private final TermFactory termFactory;
	private final org.apache.commons.rdf.api.RDF rdfFactory;
	private final boolean excludeInvalidTriples;

	private final OntopCloseableIterator<RDFFact, OntopConnectionException> iterator;

	public DefaultSimpleGraphResultSet(
			TupleResultSet tupleResultSet,
			ConstructTemplate constructTemplate,
			TermFactory termFactory,
			RDF rdfFactory, boolean excludeInvalidTriples) {
		this.resultSet = tupleResultSet;
		this.constructTemplate = constructTemplate;
		this.termFactory = termFactory;
		this.rdfFactory = rdfFactory;
		this.excludeInvalidTriples = excludeInvalidTriples;

		this.iterator = new ResultSetIterator();
	}

	@Override
	public boolean hasNext() throws OntopConnectionException, OntopResultConversionException {
		return iterator.hasNext();
	}

	@Override
	public RDFFact next() throws OntopConnectionException {
		return iterator.next();
	}

	@Override
	public OntopCloseableIterator<RDFFact, OntopConnectionException> iterator() {
		return iterator;
	}

	@Override
	public void close() throws OntopConnectionException {
		iterator.close();
	}

	private class ResultSetIterator extends RDFFactCloseableIterator {
		private final Queue<RDFFact> statementBuffer = new LinkedList<>();
		private final ImmutableMap<String, ValueExpr> extMap;

		private ResultSetIterator() {
			Extension ex = constructTemplate.getExtension();
			extMap = ex != null
					? ex.getElements().stream()
							.collect(ImmutableCollectors.toMap(ExtensionElem::getName, ExtensionElem::getExpr))
					: null;
		}

		@Override
		public boolean hasNext() throws OntopConnectionException, OntopResultConversionException {
			addStatementFromResultSet();
			boolean hasNext = !statementBuffer.isEmpty();
			if (!hasNext) {
				handleClose();
			}
			return hasNext;
		}

		@Override
		public RDFFact next() throws OntopConnectionException {
			if (statementBuffer.isEmpty()) {
				handleClose();
			}
			return statementBuffer.remove();
		}

		@Override
		public void handleClose() throws OntopConnectionException {
			try {
				if (resultSet.isConnectionAlive()) {
					// The responsibility to close or not the DB statement is delegated to the underlying tuple result set
					resultSet.close();
				}
			} catch (Exception e) {
				throw new OntopConnectionException(e);
			}
		}

		private void addStatementFromResultSet() throws OntopConnectionException, OntopResultConversionException {
			while (statementBuffer.isEmpty()) {
				if (!resultSet.isConnectionAlive() || !resultSet.hasNext())
					return;

				try {
					OntopBindingSet bindingSet = resultSet.next();
					for (ProjectionElemList peList : constructTemplate.getProjectionElemList()) {
						List<ProjectionElem> elements = peList.getElements();
						int size = elements.size();
						for (int i = 0; i < size / 3; i++) {
							try {
								RDFConstant subjectConstant = getConstant(elements.get(i * 3), bindingSet);
								RDFConstant propertyConstant = getConstant(elements.get(i * 3 + 1), bindingSet);
								RDFConstant objectConstant = getConstant(elements.get(i * 3 + 2), bindingSet);
								if (subjectConstant instanceof ObjectConstant
										&& propertyConstant instanceof IRIConstant
										&& objectConstant != null) {
									statementBuffer.add(
											RDFFact.createTripleFact(
													(ObjectConstant)subjectConstant,
													(IRIConstant)propertyConstant,
													objectConstant));
								}
								else {
									// TODO: inform the query logger that a triple has been excluded
								}
							}
							// OntopResultConversionException is never thrown by the implementation
							//  of OntopBindingSet.getConstant
							catch (OntopResultConversionException e) {
								if (!excludeInvalidTriples)
									throw e;
								// TODO: inform the query logger that a triple has been excluded
							}
						}
					}
				}
				// OntopResultConversionException is thrown by resultSet.next()
				// this, however, does not quite agree with the specification
				// as it skips all related triples (not just the one offender)
				catch (OntopResultConversionException e) {
					if (!excludeInvalidTriples)
						throw e;
					// TODO: inform the query logger that a triple has been excluded
				}
			}
		}

		private RDFConstant getConstant(ProjectionElem node, OntopBindingSet bindingSet)
				throws OntopResultConversionException {
			String nodeName = node.getName();
			ValueExpr ve = extMap != null
					? extMap.get(nodeName)
					: null;

			RDFConstant constant;
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

				String label = Optional.ofNullable(((BNodeGenerator) ve).getNodeIdExpr())
								// If defined, we expected the b-node label to be constant
								// (as appearing in the CONSTRUCT block)
								.filter(e -> e instanceof ValueConstant)
								.map(e -> (ValueConstant)e)
								.map(ValueConstant::getValue)
								.map(Value::stringValue)
								.map(s -> s + rowId)
								.orElseGet(() -> nodeName + rowId);

				constant = termFactory.getConstantBNode(label);
			} else {
				constant = bindingSet.getConstant(nodeName); // the actual implementation never throws OntopResultConversionException
			}
			return constant;
		}
	}
}
