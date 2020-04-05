package it.unibz.inf.ontop.spec.dbschema.impl;

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


import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQuery;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.*;


/**
 * This class is in charge of parsing the mappings and obtaining the list of schema names 
 * and tables.Afterwards from this list we obtain the metadata (not in this class).
 * @author Dag
 *
 */
public class SQLTableNameExtractor {


	public static Set<RelationID> getRealTables(QuotedIDFactory idfac, Collection<SQLPPTriplesMap> mappings){
		List<String> errorMessage = new LinkedList<>();
		Set<RelationID> tables = new HashSet<>();
		for (SQLPPTriplesMap axiom : mappings) {
			try {
				if (!(axiom.getSourceQuery() instanceof SQLPPSourceQuery)) {
					throw new IllegalArgumentException("getRealTables() only works for SQL-* mappings");
				}

				SQLPPSourceQuery sourceQuery = axiom.getSourceQuery();
				net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(sourceQuery.toString());
				if (!(statement instanceof Select))
					throw new JSQLParserException("The query is not a SELECT statement");
				Select selectQuery = (Select) statement;

				TableNameVisitor visitor = new TableNameVisitor(selectQuery, idfac);
				List<RelationID> queryTables = visitor.getRelations();

				for (RelationID table : queryTables)
					tables.add(table);
			} catch (Exception e) {
				errorMessage.add("Error in mapping with id: " + axiom.getId() + " \n Description: "
						+ ((e.getMessage()!= null) ? e.getMessage() : e.getCause())  + " \nMapping: [" + axiom.toString() + "]");

			}
		}
		if (errorMessage.size() > 0) {
			StringBuilder errors = new StringBuilder();
			for (String error : errorMessage) {
				errors.append(error + "\n");
			}
			final String msg = "There was an error parsing the following mappings. Please correct the issue(s) to continue.\n" + errors.toString();
			throw new RuntimeException(msg);
		}
		return tables;
	}


	private static class TableNameVisitor {

		private final QuotedIDFactory idfac;
		private final List<RelationID> relations = new LinkedList<>();

		// There are special names that are not table names but are parsed as tables.
		// These names are collected here and are not included in the table names
		private final Set<String> withTCEs = new HashSet<>();

		/**
		 * Main entry for this Tool class. A list of found tables is returned.
		 *
		 * @param select
		 */
		public TableNameVisitor(Select select, QuotedIDFactory idfac) throws JSQLParserException {
			this.idfac = idfac;

			if (select.getWithItemsList() != null) {
				for (WithItem withItem : select.getWithItemsList())
					withItem.accept(selectVisitor);
			}
			select.getSelectBody().accept(selectVisitor);
		}

		public List<RelationID> getRelations() {
			return relations;
		}

		private final SelectVisitor selectVisitor = new SelectVisitor() {
			/* Visit the FROM clause to find tables
             * Visit the JOIN and WHERE clauses to check if nested queries are present
             */
			@Override
			public void visit(PlainSelect plainSelect) {
				plainSelect.getFromItem().accept(fromItemVisitor);

				if (plainSelect.getJoins() != null)
					for (Join join : plainSelect.getJoins())
						join.getRightItem().accept(fromItemVisitor);

				if (plainSelect.getWhere() != null)
					plainSelect.getWhere().accept(expressionVisitor);

				for (SelectItem expr : plainSelect.getSelectItems())
					expr.accept(selectItemVisitor);
			}


			/*
             * Visit UNION, INTERSECT, MINUS and EXCEPT to search for table names
             */
			@Override
			public void visit(SetOperationList list) {
				for (SelectBody plainSelect : list.getSelects())
					plainSelect.accept(this);
			}

			@Override
			public void visit(WithItem withItem) {
				withTCEs.add(withItem.getName().toLowerCase());
				withItem.getSelectBody().accept(this);
			}
		};

		private final FromItemVisitor fromItemVisitor = new FromItemVisitor() {

		/*
		 * Visit Table and store its value in the list of TableJSQL (non-Javadoc)
		 * We maintain duplicate tables to retrieve the different aliases assigned
		 * we use the class TableJSQL to handle quotes and user case choice if present
		 *
		 * @see net.sf.jsqlparser.statement.select.FromItemVisitor#visit(net.sf.jsqlparser.schema.Table)
		 */

			@Override
			public void visit(Table table) {
				if (!withTCEs.contains(table.getFullyQualifiedName().toLowerCase())) {

					RelationID relationId = idfac.createRelationID(table.getSchemaName(), table.getName());
					relations.add(relationId);
				}
			}

			@Override
			public void visit(SubSelect subSelect) {
				subSelect.getSelectBody().accept(selectVisitor);
			}

			@Override
			public void visit(SubJoin subjoin) {
				subjoin.getLeft().accept(this);
				subjoin.getJoin().getRightItem().accept(this);
			}

			@Override
			public void visit(LateralSubSelect lateralSubSelect) {
				lateralSubSelect.getSubSelect().getSelectBody().accept(selectVisitor);
			}

			@Override
			public void visit(ValuesList valuesList) {
			}

			@Override
			public void visit(TableFunction tableFunction) {

			}
		};


		private final SelectItemVisitor selectItemVisitor = new SelectItemVisitor() {
			@Override
			public void visit(AllColumns expr) {
				//Do nothing!
			}

			@Override
			public void visit(AllTableColumns arg0) {
				//Do nothing!
			}

			@Override
			public void visit(SelectExpressionItem expr) {
				expr.getExpression().accept(expressionVisitor);
			}
		};

		private final ExpressionVisitor expressionVisitor = new ExpressionVisitor() {

			/*
             * We do the same procedure for all Binary Expressions
             */
			@Override
			public void visit(Addition addition) {
				visitBinaryExpression(addition);
			}

			@Override
			public void visit(AndExpression andExpression) {
				visitBinaryExpression(andExpression);
			}

			@Override
			public void visit(Between between) {
				between.getLeftExpression().accept(this);
				between.getBetweenExpressionStart().accept(this);
				between.getBetweenExpressionEnd().accept(this);
			}

			@Override
			public void visit(Column tableColumn) {
				//it does nothing here, everything is good
			}

			@Override
			public void visit(Division division) {
				visitBinaryExpression(division);
			}

			@Override
			public void visit(DoubleValue doubleValue) {
			}

			@Override
			public void visit(EqualsTo equalsTo) {
				visitBinaryExpression(equalsTo);
			}

			@Override
			public void visit(Function function) {
				if (function.getParameters() != null)
					for (Expression ex : function.getParameters().getExpressions())
						ex.accept(this);
			}

			@Override
			public void visit(GreaterThan greaterThan) {
				visitBinaryExpression(greaterThan);
			}

			@Override
			public void visit(GreaterThanEquals greaterThanEquals) {
				visitBinaryExpression(greaterThanEquals);
			}

			@Override
			public void visit(InExpression inExpression) {

				if (inExpression.getLeftItemsList() != null) {
					ItemsList leftItemsList = inExpression.getLeftItemsList();
					leftItemsList.accept(itemsListVisitor);
				}
				else {
					inExpression.getLeftExpression().accept(this);
				}
				inExpression.getRightItemsList().accept(itemsListVisitor);
			}

			@Override
			public void visit(IsNullExpression isNullExpression) {
			}

			@Override
			public void visit(JdbcParameter jdbcParameter) {
			}

			@Override
			public void visit(LikeExpression likeExpression) {
				visitBinaryExpression(likeExpression);
			}

			@Override
			public void visit(ExistsExpression existsExpression) {
				existsExpression.getRightExpression().accept(this);
			}

			@Override
			public void visit(LongValue longValue) {
			}

			@Override
			public void visit(HexValue hexValue) {

			}

			@Override
			public void visit(MinorThan minorThan) {
				visitBinaryExpression(minorThan);
			}

			@Override
			public void visit(MinorThanEquals minorThanEquals) {
				visitBinaryExpression(minorThanEquals);
			}

			@Override
			public void visit(Multiplication multiplication) {
				visitBinaryExpression(multiplication);
			}

			@Override
			public void visit(NotEqualsTo notEqualsTo) {
				visitBinaryExpression(notEqualsTo);
			}

			@Override
			public void visit(NullValue nullValue) {
			}

			@Override
			public void visit(OrExpression orExpression) {
				visitBinaryExpression(orExpression);
			}

			@Override
			public void visit(Parenthesis parenthesis) {
				parenthesis.getExpression().accept(this);
			}

			@Override
			public void visit(StringValue stringValue) {
			}

			@Override
			public void visit(Subtraction subtraction) {
				visitBinaryExpression(subtraction);
			}

			private void visitBinaryExpression(BinaryExpression binaryExpression) {
				binaryExpression.getLeftExpression().accept(this);
				binaryExpression.getRightExpression().accept(this);
			}

			@Override
			public void visit(DateValue dateValue) {
			}

			@Override
			public void visit(TimestampValue timestampValue) {
			}

			@Override
			public void visit(TimeValue timeValue) {
			}

			@Override
			public void visit(CaseExpression caseExpression) {
			}

			@Override
			public void visit(WhenClause whenClause) {
			}

			@Override
			public void visit(AllComparisonExpression allComparisonExpression) {
				allComparisonExpression.getSubSelect().getSelectBody().accept(selectVisitor);
			}

			@Override
			public void visit(AnyComparisonExpression anyComparisonExpression) {
				anyComparisonExpression.getSubSelect().getSelectBody().accept(selectVisitor);
			}

			@Override
			public void visit(Concat concat) {
				visitBinaryExpression(concat);
			}

			@Override
			public void visit(Matches matches) {
				visitBinaryExpression(matches);
			}

			@Override
			public void visit(BitwiseAnd bitwiseAnd) {
				visitBinaryExpression(bitwiseAnd);
			}

			@Override
			public void visit(BitwiseOr bitwiseOr) {
				visitBinaryExpression(bitwiseOr);
			}

			@Override
			public void visit(BitwiseXor bitwiseXor) {
				visitBinaryExpression(bitwiseXor);
			}

			@Override
			public void visit(CastExpression cast) {
				cast.getLeftExpression().accept(this);
			}

			@Override
			public void visit(Modulo modulo) {
				visitBinaryExpression(modulo);
			}

			@Override
			public void visit(AnalyticExpression analytic) {
			}

			@Override
			public void visit(WithinGroupExpression withinGroupExpression) {
			}

			@Override
			public void visit(ExtractExpression eexpr) {
			}

			@Override
			public void visit(IntervalExpression iexpr) {
			}

			@Override
			public void visit(JdbcNamedParameter jdbcNamedParameter) {
			}

			@Override
			public void visit(OracleHierarchicalExpression arg0) {
			}

			@Override
			public void visit(RegExpMatchOperator rexpr) {
//			visitBinaryExpression(rexpr);
			}


			@Override
			public void visit(SignedExpression arg0) {
			}

			@Override
			public void visit(JsonExpression arg0) {
			}

			@Override
			public void visit(JsonOperator jsonOperator) {
			}

			@Override
			public void visit(RegExpMySQLOperator arg0) {
			}

			@Override
			public void visit(UserVariable userVariable) {
			}

			@Override
			public void visit(NumericBind numericBind) {
			}

			@Override
			public void visit(KeepExpression keepExpression) {
			}

			@Override
			public void visit(MySQLGroupConcat mySQLGroupConcat) {
			}

			@Override
			public void visit(RowConstructor rowConstructor) {
			}

			@Override
			public void visit(OracleHint oracleHint) {
			}

			@Override
			public void visit(TimeKeyExpression timeKeyExpression) {
			}

			@Override
			public void visit(DateTimeLiteralExpression dateTimeLiteralExpression) {
			}

			@Override
			public void visit(NotExpression notExpression) {
			}

			@Override
			public void visit(SubSelect subSelect) {
				subSelect.getSelectBody().accept(selectVisitor);
			}
		};


		private final ItemsListVisitor itemsListVisitor = new ItemsListVisitor() {
			@Override
			public void visit(ExpressionList expressionList) {
				for (Expression expression : expressionList.getExpressions())
					expression.accept(expressionVisitor);
			}

			@Override
			public void visit(MultiExpressionList multiExprList) {
				for (ExpressionList exprList : multiExprList.getExprList())
					exprList.accept(this);
			}

			@Override
			public void visit(SubSelect subSelect) {
				subSelect.getSelectBody().accept(selectVisitor);
			}
		};

	}
}