package it.unibz.krdb.obda.owlapi3;

import org.semanticweb.owlapi.model.OWLException;

public interface OWLConnection {

	public void close() throws OWLException;

	public OWLStatement createStatement() throws OWLException;

	public void commit() throws OWLException;

	public void setAutoCommit(boolean autocommit) throws OWLException;

	public boolean getAutoCommit() throws OWLException;

	public boolean isClosed() throws OWLException;

	public boolean isReadOnly() throws OWLException;

	public void rollBack() throws OWLException;
}
