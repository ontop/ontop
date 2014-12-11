package it.unibz.krdb.sql.api;

/*
 * #%L
 * ontop-obdalib-core
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

import it.unibz.krdb.obda.parser.AggregationVisitor;
import it.unibz.krdb.obda.parser.AliasMapVisitor;
import it.unibz.krdb.obda.parser.ColumnsVisitor;
import it.unibz.krdb.obda.parser.JoinConditionVisitor;
import it.unibz.krdb.obda.parser.ProjectionVisitor;
import it.unibz.krdb.obda.parser.WhereClauseVisitor;
import it.unibz.krdb.obda.parser.SubSelectVisitor;
import it.unibz.krdb.obda.parser.TableNameVisitor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

/**
 * A structure to store the parsed SQL query string. It returns the information
 * about the query using the visitor classes
 */
public class ParsedSQLQuery implements Serializable {

	private static final long serialVersionUID = -4590590361733833782L;

	private String query;
	private Statement stm;
	boolean deepParsing = false; // used to remove all quotes from the query

	private Select selectQuery; // the parsed query

	public static Pattern pQuotes = Pattern.compile("[\"`\\[][^\\.]*[\"`\\]]");
	private List<RelationJSQL> tables;
	private List<SelectJSQL> subSelects;
	private Map<String, String> aliasMap;
	private List<Expression> joins;
	private SelectionJSQL whereClause;
	private ProjectionJSQL projection;
	private AggregationJSQL groupByClause;


	/**
	 * Parse a query given as a String
	 * 
	 * @param queryString
	 *            the SQL query to parse
	 * @param deepParsing
	 *            if true removes quotes from columns and generate exceptions
	 *            for unsupported query in the mapping otherwise, keeps the
	 *            quotes from the columns, and support all the SQLs which can be
	 *            parsed by JSQLParser
	 * @throws JSQLParserException
	 */


	public ParsedSQLQuery(String queryString, boolean deepParsing)
			throws JSQLParserException {

		/**
		 * pattern used to remove quotes from the beginning and the end of
		 * columns
		 */

		query = queryString;

		this.deepParsing = deepParsing;

		stm = CCJSqlParserUtil.parse(query);

		if (stm instanceof Select) {
			selectQuery = (Select) stm;

			// getting the values we also eliminate or handle the quotes if
			// deepParsing is set to true
			if (deepParsing) {
				tables = getTables();
				whereClause = getWhereClause();
				projection = getProjection();
				joins = getJoinConditions();
				aliasMap = getAliasMap();
				groupByClause = getGroupByClause();
			}

		}
		// catch exception about wrong inserted columns
		else
			throw new JSQLParserException(
					"The inserted query is not a SELECT statement");

	}

	/**
	 * The query is not parsed again
	 * 
	 * @param statement
	 *            we pass already a parsed statement
	 * @param deepParsing
	 *            if true removes quotes from columns and generate exceptions
	 *            for unsupported query in the mapping
	 * @throws JSQLParserException
	 */
	public ParsedSQLQuery(Statement statement, boolean deepParsing)
			throws JSQLParserException {

//		pQuotes = Pattern.compile("[\"`\\[].*[\"`\\]]");

		query = statement.toString();

		stm = statement;

		this.deepParsing = deepParsing;

		if (stm instanceof Select) {
			selectQuery = (Select) stm;

			/**
			 * Getting the values we also eliminate or handle the quotes if
			 * deepParsing is set to true and we throw errors for unsupported values
			 */

			if (deepParsing) {
				tables = getTables();
				whereClause = getWhereClause();
				projection = getProjection();
				joins = getJoinConditions();
				aliasMap = getAliasMap();
				groupByClause = getGroupByClause();
			}

		}
		// catch exception about wrong inserted columns
		else
			throw new JSQLParserException(
					"The inserted query is not a SELECT statement");

	}

	/**
	 * Unquote the query and throw errors for unsupported values
	 * 
	 * @throws JSQLParserException
	 */
	public void unquote() throws JSQLParserException {
		this.deepParsing = true;

		tables = getTables();
		whereClause = getWhereClause();
		projection = getProjection();
		joins = getJoinConditions();
		aliasMap = getAliasMap();
		groupByClause = getGroupByClause();

	}

	@Override
	public String toString() {
		return selectQuery.toString();
	}

	/**
	 * Returns all the tables in this query.
	 */
	public List<RelationJSQL> getTables() throws JSQLParserException {

		if (tables == null) {
			TableNameVisitor visitor = new TableNameVisitor();
			tables = visitor.getTables(selectQuery, deepParsing);
		}
		return tables;
	}

	/**
	 * Returns all the subSelect in this query .
	 */
	public List<SelectJSQL> getSubSelects() {

		if (subSelects == null) {
			SubSelectVisitor visitor = new SubSelectVisitor();
			subSelects = visitor.getSubSelects(selectQuery, deepParsing);
		}
		return subSelects;
	}

	/**
	 * Get the string construction of alias name.
	 */
	public Map<String, String> getAliasMap() {
		if (aliasMap == null) {
			AliasMapVisitor visitor = new AliasMapVisitor();
			aliasMap = visitor.getAliasMap(selectQuery, deepParsing);
		}
		return aliasMap;
	}

	/**
	 * Get the string construction of the join condition. The string has the
	 * format of "VAR1=VAR2".
	 */
	public List<Expression> getJoinConditions() throws JSQLParserException {
		if (joins == null) {
			JoinConditionVisitor visitor = new JoinConditionVisitor();
			joins = visitor.getJoinConditions(selectQuery, deepParsing);
		}
		return joins;
	}

	/**
	 * Get the object construction for the WHERE clause.
	 * 
	 * @throws JSQLParserException
	 */
	public SelectionJSQL getWhereClause() throws JSQLParserException {
		if (whereClause == null) {
			WhereClauseVisitor visitor = new WhereClauseVisitor();
			whereClause = visitor.getWhereClause(selectQuery, deepParsing);
		}
		return whereClause;
	}

	/**
	 * Get the object construction for the SELECT clause.
	 * 
	 * @throws JSQLParserException
	 */
	public ProjectionJSQL getProjection() throws JSQLParserException {
		if (projection == null) {
			ProjectionVisitor visitor = new ProjectionVisitor();
			projection = visitor.getProjection(selectQuery, deepParsing);
		}
		return projection;

	}

	/**
	 * Get the list of columns alias
	 * 
	 * @return
	 */
	public List<String> getColumns() {
		ColumnsVisitor visitor = new ColumnsVisitor();

		return visitor.getColumns(selectQuery);
	}

	/**
	 * Set the object construction for the SELECT clause, modifying the current
	 * statement
	 * 
	 * @param projection
	 */

	public void setProjection(ProjectionJSQL projection) {
		ProjectionVisitor visitor = new ProjectionVisitor();
		visitor.setProjection(selectQuery, projection);
		this.projection = projection;
	}

	/**
	 * Set the object construction for the WHERE clause, modifying the current
	 * statement
	 * 
	 * @param whereClause
	 */

	public void setWhereClause(SelectionJSQL whereClause) {
		WhereClauseVisitor sel = new WhereClauseVisitor();
		sel.setWhereClause(selectQuery, whereClause);
		this.whereClause = whereClause;
	}

	/**
	 * Constructs the GROUP BY statement based on the Aggregation object.
	 */
	public AggregationJSQL getGroupByClause() {
		if (groupByClause == null) {
			AggregationVisitor agg = new AggregationVisitor();
			groupByClause = agg.getAggregation(selectQuery, deepParsing);
		}

		return groupByClause;
	}

	public Statement getStatement() {
		return selectQuery;
	}

}
