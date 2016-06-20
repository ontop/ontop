package it.unibz.inf.ontop.sql.api;

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

import it.unibz.inf.ontop.parser.AliasMapVisitor;
import it.unibz.inf.ontop.parser.ColumnsVisitor;
import it.unibz.inf.ontop.parser.JoinConditionVisitor;
import it.unibz.inf.ontop.parser.ProjectionVisitor;
import it.unibz.inf.ontop.parser.WhereClauseVisitor;
import it.unibz.inf.ontop.parser.TableNameVisitor;
import it.unibz.inf.ontop.sql.QuotedID;
import it.unibz.inf.ontop.sql.QuotedIDFactory;
import it.unibz.inf.ontop.sql.RelationID;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

/**
 * A structure to store the parsed SQL query string. It returns the information
 * about the query using the visitor classes
 */

public class ParsedSQLQuery implements Serializable {

	private static final long serialVersionUID = -4590590361733833782L;

	private final String query;
	private final boolean deepParsing; // used to remove all quotes from the query

	private final Select selectQuery; // the parsed query

	private final QuotedIDFactory idfac;
	
	// maps aliases or relation names to relation names (identity on the relation names)
	private Map<RelationID, RelationID> tables;
	private List<RelationID> relations;
	private Map<QuotedID, Expression> aliasMap;
	private List<Expression> joins;
	private Expression whereClause;
	private List<SelectExpressionItem> projection;


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


	public ParsedSQLQuery(String queryString, boolean deepParsing, QuotedIDFactory idfac) throws JSQLParserException {
		this(CCJSqlParserUtil.parse(queryString), deepParsing, idfac);
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
	private ParsedSQLQuery(Statement statement, boolean deepParsing, QuotedIDFactory idfac) throws JSQLParserException {
		this.idfac = idfac;
		this.query = statement.toString();
		this.deepParsing = deepParsing;

		if (statement instanceof Select) {
			selectQuery = (Select) statement;

			// Getting the values we also eliminate or handle the quotes if
			// deepParsing is set to true and we throw errors for unsupported values

			if (deepParsing) {
				tables = getTables();
				whereClause = getWhereClause(); // bring the names in WHERE clause into NORMAL FORM
				projection = getProjection(); // bring the names in FROM clause into NORMAL FORM
				joins = getJoinConditions(); // bring the names in JOIN clauses into NORMAL FORM
				aliasMap = getAliasMap();    // bring the alias names in Expr AS Alias into NORMAL FORM 
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
	public Map<RelationID, RelationID> getTables() throws JSQLParserException {

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
	public List<SelectExpressionItem> getProjection() throws JSQLParserException {
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
	public List<Column> getColumns() {
		ColumnsVisitor visitor = new ColumnsVisitor(selectQuery);
		return visitor.getColumns();
	}


	/**
	 * Set the object construction for the SELECT clause, modifying the current
	 * statement
	 * Set the object construction for the WHERE clause, modifying the current
	 * statement
	 *
	 *
	 * META-MAPPING EXPANDER
	 *
	 * @param columnList
	 * @param distinct
	 * @param whereClause
	 */

	public static String getModifiedQuery(ParsedSQLQuery original, List<SelectExpressionItem> columnList, boolean distinct, Expression whereClause) {
		ParsedSQLQuery parsed = null;
		try {
			parsed = new ParsedSQLQuery(original.selectQuery, false, original.idfac);
		}
		catch (JSQLParserException e) {
			e.printStackTrace();
		}

		ProjectionVisitor visitor = new ProjectionVisitor(parsed.idfac);
		parsed.selectQuery.getSelectBody().accept(new SelectVisitor() {

			@Override
			public void visit(PlainSelect plainSelect) {
				if (distinct) {
					plainSelect.setDistinct(new Distinct());

					plainSelect.getSelectItems().clear();
					plainSelect.getSelectItems().addAll(columnList);
				}
				else {
					plainSelect.getSelectItems().clear();
					if (!columnList.isEmpty()) {
						plainSelect.getSelectItems().addAll(columnList);
					}
					else {
						plainSelect.getSelectItems().add(new AllColumns());
					}
				}
			}

			@Override
			public void visit(SetOperationList setOpList) {
				setOpList.getPlainSelects().get(0).accept(this);
			}

			@Override
			public void visit(WithItem withItem) {
				withItem.getSelectBody().accept(this);
			}});


		if (whereClause != null) {
			WhereClauseVisitor sel = new WhereClauseVisitor(parsed.idfac);
			parsed.selectQuery.getSelectBody().accept(new SelectVisitor() {
				@Override
				public void visit(PlainSelect plainSelect) {
					plainSelect.setWhere(whereClause);
				}
				@Override
				public void visit(SetOperationList setOpList) {
					// we do not consider the case of UNION
					// ROMAN (22 Sep 2015): not sure why it is applied to the first one only
					setOpList.getPlainSelects().get(0).accept(this);
				}
				@Override
				public void visit(WithItem withItem) {
					// we do not consider the case for WITH
				}
			});
		}

		return parsed.toString();
	}


	public static void normalizeColumnName(QuotedIDFactory idfac, Column tableColumn) {
		QuotedID columnName = idfac.createAttributeID(tableColumn.getColumnName());
		tableColumn.setColumnName(columnName.getSQLRendering());

		Table table = tableColumn.getTable();
		RelationID tableName = idfac.createRelationID(table.getSchemaName(), table.getName());
		table.setSchemaName(tableName.getSchemaSQLRendering());
		table.setName(tableName.getTableNameSQLRendering());
	}
	
}
