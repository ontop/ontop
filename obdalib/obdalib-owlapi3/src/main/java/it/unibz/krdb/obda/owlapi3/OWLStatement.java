package it.unibz.krdb.obda.owlapi3;

import java.util.List;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;

public interface OWLStatement {

	public void cancel() throws OWLException;

	// public void clearBatch() throws OWLException;

	public void close() throws OWLException;

	public OWLResultSet execute(String query) throws OWLException;

	public List<OWLAxiom> executeConstruct(String query) throws OWLException;

	public List<OWLAxiom> executeDescribe(String query) throws OWLException;

	public int executeUpdate(String query) throws OWLException;

	public OWLConnection getConnection() throws OWLException;

	public int getFetchSize() throws OWLException;

	public int getMaxRows() throws OWLException;

	/***
	 * To implement
	 * 
	 * @throws Exception
	 */
	public void getMoreResults() throws OWLException;

	public OWLResultSet getResultSet() throws OWLException;

	public int getQueryTimeout() throws OWLException;

	public void setFetchSize(int rows) throws OWLException;

	public void setMaxRows(int max) throws OWLException;

	public boolean isClosed() throws OWLException;

	/***
	 * Sets the number of seconds the driver will wait for a Statement object to
	 * execute to the given number of seconds. If the limit is exceeded, an
	 * SQLException is thrown.
	 * 
	 * @param seconds
	 *            the new query timeout limit in seconds; zero means no limit.
	 * @throws Exception
	 */
	public void setQueryTimeout(int seconds) throws Exception;

	public int getTupleCount(String query) throws OWLException;
}
