/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.parser;

import it.unibz.krdb.sql.api.RelationJSQL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.update.Update;

/**
 * Find all used tables within an select statement.
 */
public class TablesNameVisitor implements SelectVisitor, FromItemVisitor, ExpressionVisitor, ItemsListVisitor {

	/**
	 * Store the table selected, updated, deleted or replaced by the SQL query in Relations
	 */
	
	private ArrayList<RelationJSQL> tables;

	/**
	 * There are special names, that are not table names but are parsed as
	 * tables. These names are collected here and are not included in the tables
	 * - names anymore.
	 */
	private List<String> otherItemNames;

	/**
	 * Main entry for this Tool class. A list of found tables is returned.
	 *
	 * @param delete
	 * @return
	 */
	public List<RelationJSQL> getTableList(Delete delete) {
		init();
		tables.add(new RelationJSQL(delete.getTable()));
		delete.getWhere().accept(this);

		return tables;
	}

	/**
	 * Main entry for this Tool class. A list of found tables is returned.
	 *
	 * @param insert
	 * @return
	 */
	public List<RelationJSQL> getTableList(Insert insert) {
		init();
		tables.add( new RelationJSQL (insert.getTable()));
		if (insert.getItemsList() != null) {
			insert.getItemsList().accept(this);
		}

		return tables;
	}

	/**
	 * Main entry for this Tool class. A list of found tables is returned.
	 *
	 * @param replace
	 * @return
	 */
	public List<RelationJSQL> getTableList(Replace replace) {
		init();
		tables.add(new RelationJSQL(replace.getTable()));
		if (replace.getExpressions() != null) {
			for (Expression expression : replace.getExpressions()) {
				expression.accept(this);
			}
		}
		if (replace.getItemsList() != null) {
			replace.getItemsList().accept(this);
		}

		return tables;
	}

	/**
	 * Main entry for this Tool class. A list of found tables is returned.
	 *
	 * @param select
	 * @return
	 */
	public ArrayList<RelationJSQL> getTableList(Select select) {
		init();
		if (select.getWithItemsList() != null) {
			for (WithItem withItem : select.getWithItemsList()) {
				withItem.accept(this);
			}
		}
		select.getSelectBody().accept(this);

		return tables;
	}
	

	
	

	/**
	 * Main entry for this Tool class. A list of found tables is returned.
	 *
	 * @param update
	 * @return
	 */
	public List<RelationJSQL> getTableList(Update update) {
		init();
		tables.add(new RelationJSQL(update.getTable()));
		if (update.getExpressions() != null) {
			for (Expression expression : update.getExpressions()) {
				expression.accept(this);
			}
		}

		if (update.getFromItem() != null) {
			update.getFromItem().accept(this);
		}

		if (update.getJoins() != null) {
			for (Join join : update.getJoins()) {
				join.getRightItem().accept(this);
			}
		}

		if (update.getWhere() != null) {
			update.getWhere().accept(this);
		}

		return tables;
	}

	@Override
	public void visit(WithItem withItem) {
		otherItemNames.add(withItem.getName().toLowerCase());
		withItem.getSelectBody().accept(this);
	}

	@Override
	public void visit(PlainSelect plainSelect) {
		plainSelect.getFromItem().accept(this);

		if (plainSelect.getJoins() != null) {
			for (Join join : plainSelect.getJoins()) {
				join.getRightItem().accept(this);
			}
		}
		if (plainSelect.getWhere() != null) {
			plainSelect.getWhere().accept(this);
		}

	}

	@Override
	public void visit(Table tableName) {
		if (!otherItemNames.contains(tableName.getWholeTableName().toLowerCase())
				&& !tables.contains(tableName)) {
			tables.add(new RelationJSQL(tableName));
		}
	}

	@Override
	public void visit(SubSelect subSelect) {
		subSelect.getSelectBody().accept(this);
	}

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
		inExpression.getLeftExpression().accept(this);
		inExpression.getRightItemsList().accept(this);
	}

	@Override
	public void visit(InverseExpression inverseExpression) {
		inverseExpression.getExpression().accept(this);
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

	public void visitBinaryExpression(BinaryExpression binaryExpression) {
		binaryExpression.getLeftExpression().accept(this);
		binaryExpression.getRightExpression().accept(this);
	}

	@Override
	public void visit(ExpressionList expressionList) {
		for (Expression expression : expressionList.getExpressions()) {
			expression.accept(this);
		}

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.CaseExpression)
	 */
	@Override
	public void visit(CaseExpression caseExpression) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.WhenClause)
	 */
	@Override
	public void visit(WhenClause whenClause) {
	}

	@Override
	public void visit(AllComparisonExpression allComparisonExpression) {
		allComparisonExpression.getSubSelect().getSelectBody().accept(this);
	}

	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) {
		anyComparisonExpression.getSubSelect().getSelectBody().accept(this);
	}

	@Override
	public void visit(SubJoin subjoin) {
		subjoin.getLeft().accept(this);
		subjoin.getJoin().getRightItem().accept(this);
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
	public void visit(SetOperationList list) {
		for (PlainSelect plainSelect : list.getPlainSelects()) {
			visit(plainSelect);
		}
	}

	@Override
	public void visit(ExtractExpression eexpr) {
	}

	@Override
	public void visit(LateralSubSelect lateralSubSelect) {
		lateralSubSelect.getSubSelect().getSelectBody().accept(this);
	}

	@Override
	public void visit(MultiExpressionList multiExprList) {
		for (ExpressionList exprList : multiExprList.getExprList()) {
			exprList.accept(this);
		}
	}

	@Override
	public void visit(ValuesList valuesList) {
	}

	private void init() {
		otherItemNames = new ArrayList<String>();
		tables = new ArrayList<RelationJSQL>();

	}

	@Override
	public void visit(IntervalExpression iexpr) {
	}

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
    }

	@Override
	public void visit(OracleHierarchicalExpression arg0) {
		// TODO Auto-generated method stub
		
	}
}
