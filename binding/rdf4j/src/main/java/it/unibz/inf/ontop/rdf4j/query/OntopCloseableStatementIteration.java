package it.unibz.inf.ontop.rdf4j.query;

import it.unibz.inf.ontop.answering.resultset.OntopCloseableIterator;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import it.unibz.inf.ontop.rdf4j.utils.RDF4JHelper;
import org.eclipse.rdf4j.common.iteration.AbstractCloseableIteration;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryEvaluationException;

import java.security.SecureRandom;

public class OntopCloseableStatementIteration extends AbstractCloseableIteration<Statement, QueryEvaluationException> {

    private final OntopCloseableIterator<RDFFact, OntopConnectionException> iterator;
    private final ValueFactory valueFactory;
    private final SecureRandom secureRandom;
    private final byte[] salt;

    public OntopCloseableStatementIteration(OntopCloseableIterator<RDFFact, OntopConnectionException> iterator) {
        this.iterator = iterator;
        this.valueFactory = SimpleValueFactory.getInstance();
        this.secureRandom = new SecureRandom();
        this.salt = initByteSalt();
    }

    @Override
    public boolean hasNext() throws QueryEvaluationException {
        try {
            return iterator.hasNext();
        } catch (OntopConnectionException | OntopResultConversionException e) {
            throw new QueryEvaluationException(e);
        }
    }

    @Override
    public Statement next() throws QueryEvaluationException {
        try {
            return convertToStatement(iterator.next());
        } catch (OntopConnectionException e) {
            throw new QueryEvaluationException(e);
        }
    }

    @Override
    public void remove() throws QueryEvaluationException {
        throw new UnsupportedOperationException("Ontop is a read-only system.");
    }

    @Override
    public void handleClose() {
        try {
            iterator.close();
        } catch (OntopConnectionException e) {
            throw new QueryEvaluationException(e);
        }
    }

    private Statement convertToStatement(RDFFact rdfFact) {
        return valueFactory.createStatement(
                RDF4JHelper.getResource(rdfFact.getSubject(), salt),
                valueFactory.createIRI(rdfFact.getProperty().getIRI().getIRIString()),
                RDF4JHelper.getValue(rdfFact.getObject(), salt));
    }

    private byte[] initByteSalt() {
        byte[] salt = new byte[20];
        secureRandom.nextBytes(salt);
        return salt;
    }
}
