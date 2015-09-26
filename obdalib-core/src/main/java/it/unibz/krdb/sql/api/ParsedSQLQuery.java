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
import it.unibz.krdb.sql.QuotedID;
import it.unibz.krdb.sql.QuotedIDFactory;
import it.unibz.krdb.sql.QuotedIDFactoryStandardSQL;
import it.unibz.krdb.sql.RelationID;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

/**
 * A structure to store the parsed SQL query string. It returns the information
 * about the query using the visitor classes
 */
public class ParsedSQLQuery implements Serializable {

	private static final long serialVersionUID = -4590590361733833782L;

	private final String query;
	private final boolean deepParsing; // used to remove all quotes from the query

	private Select selectQuery; // the parsed query

//	public static Pattern pQuotes = Pattern.compile("[\"`\\['][^\\.]*[\"`\\]']");

	private final QuotedIDFactory idfac = new QuotedIDFactoryStandardSQL();
	
	
	private List<TableJSQL> tables;
	private List<RelationID> relations;
	private List<SelectJSQL> subSelects;
	private Map<QuotedID, Expression> aliasMap;
	private List<Expression> joins;
	private Expression whereClause;
	private ProjectionJSQL projection;
	//private AggregationJSQL groupByClause;


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


	public ParsedSQLQuery(String queryString, boolean deepParsing) throws JSQLParserException {
		query = queryString;
		this.deepParsing = deepParsing;
		Statement stm = CCJSqlParserUtil.parse(query);
		init(stm);
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
	public ParsedSQLQuery(Statement statement, boolean deepParsing) throws JSQLParserException {
		query = statement.toString();
		this.deepParsing = deepParsing;
		init(statement);
	}

	private final void init(Statement stm) throws JSQLParserException {
		if (stm instanceof Select) {
			selectQuery = (Select) stm;

			// Getting the values we also eliminate or handle the quotes if
			// deepParsing is set to true and we throw errors for unsupported values

			if (deepParsing) {
				tables = getTables();
				whereClause = getWhereClause(); // bring the names in WHERE clause into NORMAL FORM
				projection = getProjection(); // bring the names in FROM clause into NORMAL FORM
				joins = getJoinConditions(); // bring the names in JOIN clauses into NORMAL FORM
				aliasMap = getAliasMap();    // bring the alias names in Expr AS Alias into NORMAL FORM 
				//groupByClause = getGroupByClause();
			}
		}
		// catch exception about wrong inserted columns
		else
			throw new JSQLParserException("The inserted query is not a SELECT statement");
	}

	@Override
	public String toString() {
		return selectQuery.toString();
	}

	/**
	 * Returns all the tables in this query (RO now).
	 * 
	 * USED FOR CREATING DATALOG RULES AND PROVIDING METADATA WITH THE LIST OF TABLES
	 * 
	 */
	public List<TableJSQL> getTables() throws JSQLParserException {

		if (tables == null) {
			TableNameVisitor visitor = new TableNameVisitor(selectQuery, deepParsing, idfac);
			tables = visitor.getTables();
			relations = visitor.getRelations();
		}
		return tables;
	}
	
	public List<RelationID> getRelations() throws JSQLParserException {
		getTables();
		return relations;
	}

	/**
	 * Returns all the subSelect in this query .
	 * 
	 * USED FOR CREATING DATALOG RULES (LOOKUP TABLE)
	 * 
	 */
	public List<SelectJSQL> getSubSelects() {

		if (subSelects == null) {
			SubSelectVisitor visitor = new SubSelectVisitor(selectQuery);
			subSelects = visitor.getSubSelects();
		}
		return subSelects;
	}

	/**
	 * Get the string construction of alias name.
	 * 
	 * CREATING DATALOG RULES (LOOKUP TABLE)
	 * 
	 * MAPS EXPRESSION -> NAME
	 * 
	 */
	public Map<QuotedID, Expression> getAliasMap() {
		if (aliasMap == null) {
			AliasMapVisitor visitor = new AliasMapVisitor(selectQuery, idfac);
			aliasMap = visitor.getAliasMap();
		}
		return aliasMap;
	}

	/**
	 * Get the string construction of the join condition. The string has the
	 * format of "VAR1=VAR2".
	 * 
	 * CREATING DATALOG RULES (JOIN CONDITIONS)
	 * 
	 */
	public List<Expression> getJoinConditions() throws JSQLParserException {
		if (joins == null) {
			JoinConditionVisitor visitor = new JoinConditionVisitor(selectQuery, deepParsing, idfac);
			joins = visitor.getJoinConditions();
		}
		return joins;
	}

	/**
	 * Get the object construction for the WHERE clause.
	 * 
	 * CREATING DATALOG RULES
	 * AND META-MAPPING EXPANDER
	 * 
	 * @throws JSQLParserException
	 */
	public Expression getWhereClause() throws JSQLParserException {
		if (whereClause == null) {
			WhereClauseVisitor visitor = new WhereClauseVisitor(idfac);
			// CHANGES TABLE SCHEMA / NAME / ALIASES AND COLUMN NAMES
			whereClause = visitor.getWhereClause(selectQuery, deepParsing);
		}
		return whereClause;
	}

	/**
	 * Get the object construction for the SELECT clause (CHANGES TABLE AND COLUMN NAMES).
	 * 
	 * CREATING DATALOG RULES
	 * AND META-MAPPING EXPANDER
	 * 
	 * @throws JSQLParserException
	 */
	public ProjectionJSQL getProjection() throws JSQLParserException {
		if (projection == null) {
			ProjectionVisitor visitor = new ProjectionVisitor(idfac);
			projection = visitor.getProjection(selectQuery, deepParsing);
		}
		return projection;

	}

	/**
	 * Get the list of columns (RO)
	 * 
	 * ONLY FOR CREATING VIEWS!
	 * 
	 * @return
	 */
	public List<String> getColumns() {
		ColumnsVisitor visitor = new ColumnsVisitor(selectQuery);
		return visitor.getColumns();
	}

	/**
	 * Set the object construction for the SELECT clause, modifying the current
	 * statement
	 * 
	 * META-MAPPING EXPANDER
	 * 
	 * @param projection
	 */

	public void setProjection(ProjectionJSQL projection) {
		ProjectionVisitor visitor = new ProjectionVisitor(idfac);
		visitor.setProjection(selectQuery, projection);
		this.projection = projection;
	}

	/**
	 * Set the object construction for the WHERE clause, modifying the current
	 * statement
	 * 
	 * META-MAPPING EXPANDER
	 * 
	 * @param whereClause
	 */

	public void setWhereClause(Expression whereClause) {
		WhereClauseVisitor sel = new WhereClauseVisitor(idfac);
		sel.setWhereClause(selectQuery, whereClause);
		this.whereClause = whereClause;
	}

	/**
	 * Constructs the GROUP BY statement based on the Aggregation object.
	 * 
	 * FUTURE USE
	 * 
	 */
/*	
	public AggregationJSQL getGroupByClause() {
		if (groupByClause == null) {
			AggregationVisitor agg = new AggregationVisitor();
			groupByClause = agg.getAggregation(selectQuery, deepParsing);
		}

		return groupByClause;
	}
*/

	public Select getStatement() {
		return selectQuery;
	}

	public static void normalizeColumnName(QuotedIDFactory idfac, Column tableColumn) {
		QuotedID columnName = idfac.createFromString(tableColumn.getColumnName());
		tableColumn.setColumnName(columnName.getSQLRendering());

		Table table = tableColumn.getTable();
		RelationID tableName = idfac.createRelationFromString(table.getSchemaName(), table.getName());
		table.setSchemaName(tableName.getSchema().getSQLRendering());
		table.setName(tableName.getTable().getSQLRendering());
	}
	
}
