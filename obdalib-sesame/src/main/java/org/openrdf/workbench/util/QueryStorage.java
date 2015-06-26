/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.workbench.util;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.app.AppConfiguration;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;
import org.openrdf.workbench.exceptions.BadRequestException;

/**
 * Provides an interface to the private repository with the saved queries.
 * 
 * @author Dale Visser
 */
public class QueryStorage {

	private static final Object LOCK = new Object();

	private static final QueryEvaluator EVAL = QueryEvaluator.INSTANCE;

	private static QueryStorage instance;

	public static QueryStorage getSingletonInstance(final AppConfiguration config)
		throws RepositoryException, IOException
	{
		synchronized (LOCK) {
			if (instance == null || instance.isShutdown()) {
				instance = new QueryStorage(config);
			}
			return instance;
		}
	}

	private boolean isShutdown() {
		return queries == null || !queries.isInitialized();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryStorage.class);

	private static final String PRE = "PREFIX : <https://openrdf.org/workbench/>\n";

	// SAVE needs xsd: prefix since explicit XSD data types will be substituted.
	private static final String SAVE = "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n"
			+ PRE
			+ "INSERT DATA { $<query> :userName $<userName> ; :queryName $<queryName> ; "
			+ ":repository $<repository> ; :shared $<shared> ; :queryLanguage $<queryLanguage> ; :query $<queryText> ; "
			+ ":infer $<infer> ; :rowsPerPage $<rowsPerPage> . }";

	private static final String ASK_EXISTS = PRE
			+ "ASK { [] :userName $<userName> ; :queryName $<queryName> ; :repository $<repository> . }";

	private static final String UPDATE_FILTER = "FILTER (?user = $<userName> || ?user = \"\" ) } ";

	private static final String READ_FILTER = "FILTER (?user = $<userName> || ?user = \"\" || ?shared) } ";

	private static final String ASK_UPDATABLE = PRE + "ASK { $<query> :userName ?user . " + UPDATE_FILTER;

	private static final String ASK_READABLE = PRE + "ASK { $<query> :userName ?user  ; :shared ?shared . "
			+ READ_FILTER;

	private static final String DELETE = PRE + "DELETE WHERE { $<query> :userName ?user ; ?p ?o . }";

	private static final String MATCH = ":shared ?s ; :queryLanguage ?ql ; :query ?q ; :rowsPerPage ?rpp .\n";

	private static final String UPDATE = PRE
			+ "DELETE { $<query> "
			+ MATCH
			+ "}\nINSERT { $<query> :shared $<shared> ; :queryLanguage $<queryLanguage> ; :query $<queryText> ; "
			+ ":infer $<infer> ; :rowsPerPage $<rowsPerPage> . } WHERE { $<query> :userName ?user ; " + MATCH
			+ UPDATE_FILTER;

	private static final String SELECT_URI = PRE
			+ "SELECT ?query { ?query :repository $<repository> ; :userName $<userName> ; :queryName $<queryName> . } ";

	private static final String SELECT_TEXT = PRE
			+ "SELECT ?queryText { [] :repository $<repository> ; :userName $<userName> ; :queryName $<queryName> ; :query ?queryText . } ";

	private static final String SELECT = PRE
			+ "SELECT ?query ?user ?queryName ?shared ?queryLn ?queryText ?infer ?rowsPerPage "
			+ "{ ?query :repository $<repository> ; :userName ?user ; :queryName ?queryName ; :shared ?shared ; "
			+ ":queryLanguage ?queryLn ; :query ?queryText ; :infer ?infer ; :rowsPerPage ?rowsPerPage .\n"
			+ READ_FILTER + "ORDER BY ?user ?queryName";

	private final Repository queries;

	private static final String USER_NAME = "$<userName>";

	private static final String REPOSITORY = "$<repository>";

	private static final String QUERY = "$<query>";

	private static final String QUERY_NAME = "$<queryName>";

	/**
	 * Create a new object for accessing the store of user queries.
	 * 
	 * @param appConfig
	 *        the application configuration, for obtaining the data directory
	 * @throws RepositoryException
	 *         if there is an issue creating the object to access the repository
	 * @throws IOException
	 */
	protected QueryStorage(final AppConfiguration appConfig)
		throws RepositoryException, IOException
	{
		queries = new SailRepository(new NativeStore(new File(appConfig.getDataDir(), "queries")));
		queries.initialize();
	}

	public void shutdown() {
		try {
			if (queries != null && queries.isInitialized()) {
				queries.shutDown();
			}
		}
		catch (RepositoryException e) {
			LOGGER.warn(e.getMessage());
		}
	}

	/**
	 * Checks whether the current user/password credentials can really access the
	 * current repository.
	 * 
	 * @param repository
	 *        the current repository
	 * @return true, if it is possible to request a statement from the repository
	 *         with the given credentials
	 * @throws RepositoryException
	 *         if there is an issue closing the connection
	 */
	public boolean checkAccess(final HTTPRepository repository)
		throws RepositoryException
	{
		LOGGER.info("repository: {}", repository.getRepositoryURL());
		boolean rval = true;
		RepositoryConnection con = null;
		try {
			con = repository.getConnection();

			// Manufacture an unlikely unique statement to check.
			URI uri = con.getValueFactory().createURI("urn:uuid:" + UUID.randomUUID());
			con.hasStatement(uri, uri, uri, false, uri);
		}
		catch (RepositoryException re) {
			rval = false;
		}
		finally {
			con.close();
		}
		return rval;
	}

	/**
	 * Save a query. UNSAFE from an injection point of view. It is the
	 * responsibility of the calling code to call checkAccess() with the full
	 * credentials first.
	 * 
	 * @param repository
	 *        the repository the query is associated with
	 * @param queryName
	 *        the name for the query
	 * @param userName
	 *        the user saving the query
	 * @param shared
	 *        whether the query is to be shared with other users
	 * @param queryLanguage
	 *        the language, SeRQL or SPARQL, of the query
	 * @param queryText
	 *        the actual query text
	 * @param infer
	 * @param rowsPerPage
	 *        rows to display per page, may be 0 (all), 10, 50, 100, or 200)
	 * @throws OpenRDFException
	 */
	public void saveQuery(final HTTPRepository repository, final String queryName, final String userName,
			final boolean shared, final QueryLanguage queryLanguage, final String queryText,
			final boolean infer, final int rowsPerPage)
		throws OpenRDFException
	{
		if (QueryLanguage.SPARQL != queryLanguage && QueryLanguage.SERQL != queryLanguage) {
			throw new RepositoryException("May only save SPARQL or SeRQL queries, not"
					+ queryLanguage.toString());
		}
		if (0 != rowsPerPage && 10 != rowsPerPage && 20 != rowsPerPage && 50 != rowsPerPage
				&& 100 != rowsPerPage)
		{
			throw new RepositoryException("Illegal value for rows per page: " + rowsPerPage);
		}
		this.checkQueryText(queryText);
		final QueryStringBuilder save = new QueryStringBuilder(SAVE);
		save.replaceURI(REPOSITORY, repository.getRepositoryURL());
		save.replaceURI(QUERY, "urn:uuid:" + UUID.randomUUID());
		save.replaceQuote(QUERY_NAME, queryName);
		this.replaceUpdateFields(save, userName, shared, queryLanguage, queryText, infer, rowsPerPage);
		updateQueryRepository(save.toString());
	}

	/**
	 * Determines whether the user with the given userName is allowed to update
	 * or delete the given query.
	 * 
	 * @param query
	 *        the node identifying the query of interest
	 * @param currentUser
	 *        the user to check access for
	 * @return <tt>true</tt> if the given query was saved by the given user or
	 *         the anonymous user
	 */
	public boolean canChange(final URI query, final String currentUser)
		throws RepositoryException, QueryEvaluationException, MalformedQueryException
	{
		return performAccessQuery(ASK_UPDATABLE, query, currentUser);
	}

	/**
	 * Determines whether the user with the given userName is allowed to read the
	 * given query.
	 * 
	 * @param query
	 *        the node identifying the query of interest
	 * @param currentUser
	 *        the user to check access for
	 * @return <tt>true</tt> if the given query was saved by either the given
	 *         user or the anonymous user, or is shared
	 */
	public boolean canRead(URI query, String currentUser)
		throws RepositoryException, QueryEvaluationException, MalformedQueryException
	{
		return performAccessQuery(ASK_READABLE, query, currentUser);
	}

	private boolean performAccessQuery(String accessSPARQL, URI query, String currentUser)
		throws RepositoryException, QueryEvaluationException, MalformedQueryException
	{
		final QueryStringBuilder canDelete = new QueryStringBuilder(accessSPARQL);
		canDelete.replaceURI(QUERY, query.toString());
		canDelete.replaceQuote(USER_NAME, currentUser);
		LOGGER.info("{}", canDelete);
		final RepositoryConnection connection = this.queries.getConnection();
		try {
			return connection.prepareBooleanQuery(QueryLanguage.SPARQL, canDelete.toString()).evaluate();
		}
		finally {
			connection.close();
		}
	}

	public boolean askExists(final HTTPRepository repository, final String queryName, final String userName)
		throws QueryEvaluationException, RepositoryException, MalformedQueryException
	{
		final QueryStringBuilder ask = new QueryStringBuilder(ASK_EXISTS);
		ask.replaceURI(REPOSITORY, repository.getRepositoryURL());
		ask.replaceQuote(QUERY_NAME, queryName);
		ask.replaceQuote(USER_NAME, userName);
		LOGGER.info("{}", ask);
		final RepositoryConnection connection = this.queries.getConnection();
		try {
			return connection.prepareBooleanQuery(QueryLanguage.SPARQL, ask.toString()).evaluate();
		}
		finally {
			connection.close();
		}
	}

	/**
	 * Delete the given query for the given user. It is the responsibility of the
	 * calling code to call checkAccess() and canDelete() with the full
	 * credentials first.
	 * 
	 * @param query
	 * @param userName
	 * @throws RepositoryException
	 * @throws UpdateExecutionException
	 * @throws MalformedQueryException
	 */
	public void deleteQuery(final URI query, final String userName)
		throws RepositoryException, UpdateExecutionException, MalformedQueryException
	{
		final QueryStringBuilder delete = new QueryStringBuilder(DELETE);
		delete.replaceQuote(QueryStorage.USER_NAME, userName);
		delete.replaceURI(QUERY, query.toString());
		updateQueryRepository(delete.toString());
	}

	/**
	 * Update the entry for the given query. It is the responsibility of the
	 * calling code to call checkAccess() with the full credentials first.
	 * 
	 * @param query
	 *        the query to update
	 * @param userName
	 *        the user name
	 * @param shared
	 *        whether to share with other users
	 * @param queryLanguage
	 *        the query language
	 * @param queryText
	 *        the text of the query
	 * @param infer
	 * @param rowsPerPage
	 *        the rows per page to display of the query
	 * @throws RepositoryException
	 *         if a problem occurs during the update
	 * @throws UpdateExecutionException
	 *         if a problem occurs during the update
	 * @throws MalformedQueryException
	 *         if a problem occurs during the update
	 */
	public void updateQuery(final URI query, final String userName, final boolean shared,
			final QueryLanguage queryLanguage, final String queryText, final boolean infer, final int rowsPerPage)
		throws RepositoryException, UpdateExecutionException, MalformedQueryException
	{
		final QueryStringBuilder update = new QueryStringBuilder(UPDATE);
		update.replaceURI(QUERY, query);
		this.replaceUpdateFields(update, userName, shared, queryLanguage, queryText, infer, rowsPerPage);
		this.updateQueryRepository(update.toString());
	}

	/**
	 * Prepares a query to retrieve the queries accessible to the given user in
	 * the given repository. When evaluated, the query result will have the
	 * following binding names: query, user, queryName, shared, queryLn,
	 * queryText, rowsPerPage. It is the responsibility of the calling code to
	 * call checkAccess() with the full credentials first.
	 * 
	 * @param repository
	 *        that the saved queries run against
	 * @param userName
	 *        that is requesting the saved queries
	 * @param builder
	 *        receives a list of all the saved queries against the given
	 *        repository and accessible to the given user
	 * @throws RepositoryException
	 *         if there's a problem connecting to the saved queries repository
	 * @throws MalformedQueryException
	 *         if the query is not legal SPARQL
	 * @throws QueryEvaluationException
	 *         if there is a problem while attempting to evaluate the query
	 * @throws QueryResultHandlerException
	 */
	public void selectSavedQueries(final HTTPRepository repository, final String userName,
			final TupleResultBuilder builder)
		throws RepositoryException, MalformedQueryException, QueryEvaluationException,
		QueryResultHandlerException
	{
		final QueryStringBuilder select = new QueryStringBuilder(SELECT);
		select.replaceQuote(USER_NAME, userName);
		select.replaceURI(REPOSITORY, repository.getRepositoryURL());
		final RepositoryConnection connection = this.queries.getConnection();
		try {
			EVAL.evaluateTupleQuery(builder,
					connection.prepareTupleQuery(QueryLanguage.SPARQL, select.toString()));
		}
		finally {
			connection.close();
		}
	}

	/**
	 * Returns the URI for the saved query in the given repository with the given
	 * name, owned by the given owner.
	 * 
	 * @param repository
	 *        The repository the query is associated with.
	 * @param owner
	 *        The user that saved the query.
	 * @param queryName
	 *        The name given to the query.
	 * @return if it exists, the URI referring to the specified saved query.
	 * @throws OpenRDFException
	 *         if issues occur performing the necessary queries.
	 * @throws BadRequestException
	 *         if the the specified stored query doesn't exist
	 */
	public URI selectSavedQuery(final HTTPRepository repository, final String owner, final String queryName)
		throws OpenRDFException, BadRequestException
	{
		final QueryStringBuilder select = new QueryStringBuilder(SELECT_URI);
		select.replaceQuote(QueryStorage.USER_NAME, owner);
		select.replaceURI(REPOSITORY, repository.getRepositoryURL());
		select.replaceQuote(QUERY_NAME, queryName);
		final RepositoryConnection connection = this.queries.getConnection();
		final TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, select.toString());
		try {
			final TupleQueryResult result = query.evaluate();
			if (result.hasNext()) {
				return (URI)(result.next().getValue("query"));
			}
			else {
				throw new BadRequestException("Could not find query entry in storage.");
			}
		}
		finally {
			connection.close();
		}
	}

	/**
	 * Retrieves the specified query text. No security checks are done here. If
	 * the saved query exists, its text is returned.
	 * 
	 * @param repository
	 *        Repository that the saved query is associated with.
	 * @param owner
	 *        The user that saved the query.
	 * @param queryName
	 *        The name given to the saved query.
	 * @return the text of the saved query, if it exists
	 * @throws OpenRDFException
	 *         if a problem occurs accessing storage
	 * @throws BadRequestException
	 *         if the specified query doesn't exist
	 */
	public String getQueryText(final HTTPRepository repository, final String owner, final String queryName)
		throws OpenRDFException, BadRequestException
	{
		final QueryStringBuilder select = new QueryStringBuilder(SELECT_TEXT);
		select.replaceQuote(QueryStorage.USER_NAME, owner);
		select.replaceURI(REPOSITORY, repository.getRepositoryURL());
		select.replaceQuote(QUERY_NAME, queryName);
		final RepositoryConnection connection = this.queries.getConnection();
		final TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, select.toString());
		try {
			final TupleQueryResult result = query.evaluate();
			if (result.hasNext()) {
				return result.next().getValue("queryText").stringValue();
			}
			else {
				throw new BadRequestException("Could not find query entry in storage.");
			}
		}
		finally {
			connection.close();
		}
	}

	private void updateQueryRepository(final String update)
		throws RepositoryException, UpdateExecutionException, MalformedQueryException
	{
		LOGGER.info("SPARQL/Update of Query Storage:\n--\n{}\n--", update);
		final RepositoryConnection connection = this.queries.getConnection();
		try {
			connection.prepareUpdate(QueryLanguage.SPARQL, update).execute();
		}
		finally {
			connection.close();
		}
	}

	/**
	 * Perform replacement on several common fields for update operations.
	 * 
	 * @param userName
	 *        the name of the current user
	 * @param shared
	 *        whether the saved query is to be shared with other users
	 * @param queryLanguage
	 *        the language of the saved query
	 * @param queryText
	 *        the actual text of the query to save
	 * @param infer
	 * @param rowsPerPage
	 *        the rows per page to display for results
	 */
	private void replaceUpdateFields(final QueryStringBuilder builder, final String userName,
			final boolean shared, final QueryLanguage queryLanguage, final String queryText,
			final boolean infer, final int rowsPerPage)
	{
		builder.replaceQuote(USER_NAME, userName);
		builder.replace("$<shared>", QueryStringBuilder.xsdQuote(String.valueOf(shared), "boolean"));
		builder.replaceQuote("$<queryLanguage>", queryLanguage.toString());
		checkQueryText(queryText);
		builder.replace("$<queryText>", QueryStringBuilder.quote(queryText, "'''", "'''"));
		builder.replace("$<infer>", QueryStringBuilder.xsdQuote(String.valueOf(infer), "boolean"));
		builder.replace("$<rowsPerPage>",
				QueryStringBuilder.xsdQuote(String.valueOf(rowsPerPage), "unsignedByte"));
	}

	/**
	 * Imposes the rule that the query may not contain '''-quoted string, since
	 * that is how we'll be quoting it in our SPARQL/Update statements. Quoting
	 * the query with ''' assuming all string literals in the query are of the
	 * STRING_LITERAL1, STRING_LITERAL2 or STRING_LITERAL_LONG2 types.
	 * 
	 * @param queryText
	 *        the query text
	 */
	private void checkQueryText(final String queryText) {
		if (queryText.indexOf("'''") > 0) {
			throw new IllegalArgumentException("queryText may not contain '''-quoted strings.");
		}
	}
}