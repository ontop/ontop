package inf.unibz.it.ucq.domain;

import inf.unibz.it.obda.exception.QueryResultException;

import java.sql.SQLException;
import java.util.List;


public interface QueryResult {

public boolean isScrollable() throws QueryResultException;

	/**
	 * Returns the constant of position <code>i</code> in this evaluation result
	 *
	 * @param i
	 *            the index of the constant in this evaluation result
	 * @return the constant at position <code>i</code>
	 * @throws UnrecognizedTypeException
	 */
	public Constant getConstantFromColumn(int i) throws QueryResultException;

	/**
	 * Fetches the next row belonging to this evaluation result
	 *
	 * @return <code>true</code> if there are further rows to fetch
	 * @throws InclusionAssertionException
	 * @throws UnsupportedLanguageException
	 * @throws OntologyAlphabetException
	 * @throws UnrecognizedTypeException
	 */
	public boolean nextRow() throws QueryResultException;

	public boolean previousRow() throws QueryResultException;

	public int getRowsCount() throws QueryResultException;

	/**
	 * Tests whether this evaluation result is empty
	 *
	 * @return <code>true</code> if this evaluation result is empty
	 */
	public boolean isEmpty() throws QueryResultException;

	/**
	 * Close this evaluation result
	 */
	public void close() throws QueryResultException;

	/**
	 * It returns the number of columns in the evaluation result
	 *
	 * @return the arity of the result; -1 if the result was not computed
	 */
	public int getColumnCount() throws QueryResultException;

	/**
	 * Retrieves whether the cursor is on the first row of this
	 * IEvaluationResult object.
	 *
	 * @return <code>true</code> if the cursor is on the first row;
	 *         <code>false</code> otherwise
	 * @throws SQLException
	 *             if a database access error occurs
	 */
	public boolean isFirst() throws QueryResultException;

	/**
	 * Retrieves whether the cursor is before the first row of this
	 * IEvaluationResult object.
	 *
	 * @return <code>true</code> if the cursor is before the first row;
	 *         <code>false</code> otherwise
	 * @throws SQLException
	 *             if a database access error occurs
	 */
	public boolean isBeforeFirst() throws QueryResultException;

	/**
	 * Retrieves whether the cursor is on the last row of this IEvaluationResult
	 * object.
	 *
	 * @return <code>true</code> if the cursor is on the last row;
	 *         <code>false</code> otherwise
	 * @throws SQLException
	 *             if a database access error occurs
	 */
	public boolean isLast() throws QueryResultException;

	/**
	 * Retrieves whether the cursor is after the last row of this
	 * IEvaluationResult object.
	 *
	 * @return <code>true</code> if the cursor is after the last row;
	 *         <code>false</code> otherwise
	 * @throws SQLException
	 *             if a database access error occurs
	 */
	public boolean isAfterLast() throws QueryResultException;


	public List<String> getSignature() throws QueryResultException;

}
