package it.unibz.inf.ontop.answering.resultset.impl;

import it.unibz.inf.ontop.answering.resultset.OntopCloseableIterator;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.spec.ontology.RDFFact;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class RDFFactCloseableIterator implements OntopCloseableIterator<RDFFact, OntopConnectionException> {

    private final AtomicBoolean closed = new AtomicBoolean(false);

    public final boolean isClosed() {
        return closed.get();
    }

    @Override
    public final void close() throws OntopConnectionException {
        if (closed.compareAndSet(false, true)) {
            handleClose();
        }
    }

    protected abstract void handleClose() throws OntopConnectionException;
}
