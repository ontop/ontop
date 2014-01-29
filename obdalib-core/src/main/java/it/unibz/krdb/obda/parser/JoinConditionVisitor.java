/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */

package it.unibz.krdb.obda.parser;

import it.unibz.krdb.sql.api.TableJSQL;
import it.unibz.krdb.sql.api.VisitedQuery;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
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
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;

/**
 * 
 * Visitor class allows to retrieve the JoinConditions in a select statement.
 *
 */

public class JoinConditionVisitor implements SelectVisitor, ExpressionVisitor, FromItemVisitor {
	
	ArrayList<Expression> joinConditions;
	boolean notSupported = false;
	
	/**
	 * Obtain the join conditions in a format "expression condition expression"
	 * for example "table1.home = table2.house"
	 * 
	 * @param select statement with the parsed query
	 * @return a list of string containing the join conditions
	 */
	public ArrayList<Expression> getJoinConditions(Select select)  throws JSQLParserException {
		joinConditions = new ArrayList<Expression>();
		select.getSelectBody().accept(this);
	
		if(notSupported) // used to throw exception for the currently unsupported methods
			throw new JSQLParserException("Query not yet supported");
		
		return joinConditions;
	}

	/*
	 * visit Plainselect, search for the join conditions, we do not consider the simple join that are considered in selection
	 * 
	 * @see net.sf.jsqlparser.statement.select.SelectVisitor#visit(net.sf.jsqlparser.statement.select.PlainSelect)
	 */
	@Override
	public void visit(PlainSelect plainSelect) {
		FromItem fromItem = plainSelect.getFromItem();
		fromItem.accept(this);
		
		List<Join> joins = plainSelect.getJoins();
		
		if (joins != null)
		for (Join join : joins){
			Expression expr = join.getOnExpression();
			
			if (join.getUsingColumns()!=null) // JOIN USING column
				for (Column column : join.getUsingColumns())
				{
					String columnName= column.getColumnName();
					
					if(VisitedQuery.pQuotes.matcher(columnName).matches())
					{
						columnName=columnName.substring(1, columnName.length()-1);
						column.setColumnName(columnName);
					}
					if (fromItem instanceof Table && join.getRightItem() instanceof Table) {
						Table table1 = (Table)fromItem;
						BinaryExpression bexpr = new EqualsTo();
						Column column1 = new Column();
						column1.setColumnName(columnName);
						column1.setTable(table1);
						bexpr.setLeftExpression(column1);
						
						Column column2 = new Column();
						column2.setColumnName(columnName);
						column2.setTable((Table)join.getRightItem());
						bexpr.setRightExpression(column2);
						joinConditions.add(bexpr);
								//plainSelect.getFromItem()+"."+columnName+ bexpr.getStringExpression() +join.getRightItem()+"."+columnName);
						
						
					} else {
						//more complex structure in FROM or JOIN e.g. subselects
					//	plainSelect.getFromItem().accept(this);
					//	join.getRightItem().accept(this);
						notSupported = true;
						
					}
				}
					
			else{ //JOIN ON cond
				if(expr!=null) {
					join.getRightItem().accept(this);
					expr.accept(this);
					
				} 
				//we do not consider simple joins
//				else
//					if(join.isSimple())
//						joinConditions.add(plainSelect.getWhere().toString());
					
			}
				
		}
	}

	@Override
	public void visit(SetOperationList operations) { //UNION
		 notSupported = true;
		// we do not consider the case of union
		/*for (PlainSelect plainSelect: operations.getPlainSelects()){
			plainSelect.getFromItem().accept(this);
			
			List<Join> joins = plainSelect.getJoins();
			
			if (joins != null)
			for (Join join : joins){
				Expression expr = join.getOnExpression();
				
				
				if (join.getUsingColumns()!=null)
					for (Column column : join.getUsingColumns()){
						joinConditions.add(plainSelect.getFromItem()+"."+column.getColumnName()+" = "+join.getRightItem()+"."+column.getColumnName());
					}
						
				else{
					if(expr!=null)
//						joinConditions.add(expr.toString());
						expr.accept(this);
//					
						
				}
			}
		}*/
		
	}

	@Override
	public void visit(WithItem arg0) {
		// we do not consider the case of with
		
	}

	@Override
	public void visit(NullValue arg0) {
		//we do not execute anything 
		
	}

	@Override
	public void visit(Function arg0) {
		//we do not execute anything 
		
	}

	@Override
	public void visit(InverseExpression arg0) {
		//we do not execute anything 
		
	}

	@Override
	public void visit(JdbcParameter arg0) {
		//we do not execute anything 
		
	}

	@Override
	public void visit(JdbcNamedParameter arg0) {
		//we do not execute anything 
		
	}

	@Override
	public void visit(DoubleValue arg0) {
		//we do not execute anything 
		
	}

	@Override
	public void visit(LongValue arg0) {
		//we do not execute anything 
		
	}

	@Override
	public void visit(DateValue arg0) {
		//we do not execute anything 
		
	}

	@Override
	public void visit(TimeValue arg0) {
		//we do not execute anything 
		
	}

	@Override
	public void visit(TimestampValue arg0) {
		//we do not execute anything 
		
	}

	@Override
	public void visit(Parenthesis parenthesis) {
		parenthesis.getExpression().accept(this);
		
	}

	@Override
	public void visit(StringValue arg0) {
		//we do not execute anything 
		
	}

	/*
	 * We handle in the same way all BinaryExpression
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.Addition)
	 */
	@Override
	public void visit(Addition addition) {
		visitBinaryExpression(addition);
		
	}

	/*
	 * We handle in the same way all BinaryExpression
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.Division)
	 */
	@Override
	public void visit(Division arg0) {
		visitBinaryExpression(arg0);
		
	}

	/*
	 * We handle in the same way all BinaryExpression
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.Multiplication)
	 */
	@Override
	public void visit(Multiplication arg0) {
		visitBinaryExpression(arg0);
		
	}

	/*
	 * We handle in the same way all BinaryExpression
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.arithmetic.Subtraction)
	 */
	@Override
	public void visit(Subtraction arg0) {
		visitBinaryExpression(arg0);
		
	}

	/*
	 * We handle in the same way all BinaryExpression
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.conditional.AndExpression)
	 */
	@Override
	public void visit(AndExpression arg0) {
		visitBinaryExpression(arg0);
		
	}
	
	/*
	 * We handle in the same way all BinaryExpression
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.conditional.OrExpression)
	 */
	@Override
	public void visit(OrExpression arg0) {
		visitBinaryExpression(arg0);
		
	}

	@Override
	public void visit(Between arg0) {
	//we do not consider the case of BETWEEN	
		
	}

	/*
	 * We store in join conditions the binary expression that are not nested, 
	 * for the others we continue to visit the subexpression
	 * Example: AndExpression and OrExpression always have subexpression.
	 */
	
	public void visitBinaryExpression(BinaryExpression binaryExpression) {
		Expression left = binaryExpression.getLeftExpression();
		Expression right = binaryExpression.getRightExpression();
		
		if (!(left instanceof BinaryExpression) && 
				!(right instanceof BinaryExpression)) {
			
			left.accept(this);
			right.accept(this);
			joinConditions.add(binaryExpression);
		}
		else
		{
			left.accept(this);
			right.accept(this);
		}
		
	}
	
	/*
	 *  We handle in the same way all BinaryExpression
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.EqualsTo)
	 */
	@Override
	public void visit(EqualsTo arg0) {
		visitBinaryExpression(arg0);

	}

	/*
	 *  We handle in the same way all BinaryExpression
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.GreaterThan)
	 */
	@Override
	public void visit(GreaterThan arg0) {
		visitBinaryExpression(arg0);
		
		
	}

	/*
	 *  We handle in the same way all BinaryExpression
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals)
	 */
	@Override
	public void visit(GreaterThanEquals arg0) {
		visitBinaryExpression(arg0);
		
		
	}


	@Override
	public void visit(InExpression arg0) {
		//we do not support the case for IN condition
		
		
	}

	@Override
	public void visit(IsNullExpression arg0) {
		//we do not execute anything
		
	}

	/*
	 * We handle in the same way all BinaryExpression
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.LikeExpression)
	 */
	@Override
	public void visit(LikeExpression arg0) {
		visitBinaryExpression(arg0);
		
	}

	/*
	 * We handle in the same way all BinaryExpression
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.MinorThan)
	 */
	@Override
	public void visit(MinorThan arg0) {
		visitBinaryExpression(arg0);
		
		
	}

	/*
	 * We handle in the same way all BinaryExpression
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.MinorThanEquals)
	 */
	@Override
	public void visit(MinorThanEquals arg0) {
		visitBinaryExpression(arg0);
		
		
	}

	/*
	 * We handle in the same way all BinaryExpression
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.NotEqualsTo)
	 */
	@Override
	public void visit(NotEqualsTo arg0) {
		visitBinaryExpression(arg0);
		
		
	}

	/*
	 * Remove quotes from columns if they are present (non-Javadoc)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.schema.Column)
	 */
	@Override
	public void visit(Column col) {
		Table table= col.getTable();
		if(table.getName()!=null){
			
			TableJSQL fixTable = new TableJSQL(table);
			table.setAlias(fixTable.getAlias());
			table.setName(fixTable.getTableName());
			table.setSchemaName(fixTable.getSchema());
		
		}
		String columnName= col.getColumnName();
		if(VisitedQuery.pQuotes.matcher(columnName).matches())
			col.setColumnName(columnName.substring(1, columnName.length()-1));
		
	}
	
	/*
	 * We visit also the subselect to find nested joins
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.statement.select.SubSelect)
	 */
	@Override
	public void visit(SubSelect subSelect) {
		if (subSelect.getSelectBody() instanceof PlainSelect) {

			PlainSelect subSelBody = (PlainSelect) (subSelect.getSelectBody());

			if (subSelBody.getJoins() != null || subSelBody.getWhere() != null) {
				notSupported = true;
			} else {
				subSelBody.accept(this);
			}
		} else
			notSupported = true;
	}

	@Override
	public void visit(CaseExpression arg0) {
		// we do not support case expression
		notSupported = true;
		
	}

	@Override
	public void visit(WhenClause arg0) {
		// we do not support when expression
		notSupported = true;
	}

	@Override
	public void visit(ExistsExpression exists) {
		// we do not support exists
		notSupported = true;
	}

	/*
	 * We visit the subselect in ALL(...)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.AllComparisonExpression)
	 */
	@Override
	public void visit(AllComparisonExpression all) {
		notSupported = true;
	}

	/*
	 * We visit the subselect in ANY(...)
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.AnyComparisonExpression)
	 */
	@Override
	public void visit(AnyComparisonExpression any) {
		notSupported = true;
	}

	/*
	 * We handle in the same way all BinaryExpression
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.MinorThanEquals)
	 */
	@Override
	public void visit(Concat arg0) {
		notSupported = true;
	}

	/*
	 * We handle in the same way all BinaryExpression
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.MinorThanEquals)
	 */
	@Override
	public void visit(Matches arg0) {
		notSupported = true;
	}

	/*
	 * We handle in the same way all BinaryExpression
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.MinorThanEquals)
	 */
	@Override
	public void visit(BitwiseAnd arg0) {
		notSupported = true;
	}

	/*
	 * We handle in the same way all BinaryExpression
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.MinorThanEquals)
	 */
	@Override
	public void visit(BitwiseOr arg0) {
		notSupported = true;
	}

	/*
	 * We handle in the same way all BinaryExpression
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.MinorThanEquals)
	 */
	@Override
	public void visit(BitwiseXor arg0) {
		notSupported = true;
	}

	@Override
	public void visit(CastExpression arg0) {
		// we do not consider CAST expression
		notSupported = true;
	}

	/*
	 * We handle in the same way all BinaryExpression
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.operators.relational.MinorThanEquals)
	 */
	@Override
	public void visit(Modulo arg0) {
		notSupported = true;
	}

	@Override
	public void visit(AnalyticExpression arg0) {
		// we do not consider AnalyticExpression
		notSupported = true;
	}

	@Override
	public void visit(ExtractExpression arg0) {
		// we do not consider ExtractExpression
		notSupported = true;
	}

	@Override
	public void visit(IntervalExpression arg0) {
		// we do not consider IntervalExpression
		notSupported = true;
	}

	@Override
	public void visit(OracleHierarchicalExpression arg0) {
		// we do not consider OracleHierarchicalExpression
		notSupported = true;
	}

	@Override
	public void visit(Table tableName) {
		// we do not execute anything
	}

	/*
	 * search for the subjoin conditions, we do not consider the simple join that are considered in selection
	 * @see net.sf.jsqlparser.statement.select.FromItemVisitor#visit(net.sf.jsqlparser.statement.select.SubJoin)
	 */
	@Override
	public void visit(SubJoin subjoin) {
		notSupported = true;
//		Join join =subjoin.getJoin();
//		Expression expr = join.getOnExpression();
//		
//		
//		if (join.getUsingColumns()!=null)
//			for (Column column : join.getUsingColumns())
//			{
//				String columnName= column.getColumnName();
//				
//				if(VisitedQuery.pQuotes.matcher(columnName).matches())
//				{
//					columnName=columnName.substring(1, columnName.length()-1);
//					column.setColumnName(columnName);
//				}
//				
//				joinConditions.add(subjoin.getLeft()+"."+column.getColumnName()+" = "+join.getRightItem()+"."+column.getColumnName());
//			}
//				
//		else{
//			if(expr!=null)
//				expr.accept(this);
//		}
//		
	}

	/*
	 *  We visit also the lateralsubselect to find nested joins
	 * @see net.sf.jsqlparser.statement.select.FromItemVisitor#visit(net.sf.jsqlparser.statement.select.LateralSubSelect)
	 */
	@Override
	public void visit(LateralSubSelect lateralSubSelect) {
		notSupported = true;
	}

	@Override
	public void visit(ValuesList valuesList) {
		// we do not execute anything
	}

	@Override
	public void visit(RegExpMatchOperator arg0) {
		// TODO Auto-generated method stub
		notSupported = true;
	}

}
